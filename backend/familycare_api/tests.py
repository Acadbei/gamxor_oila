import json
from unittest.mock import patch

from django.contrib.auth.models import User
from django.test import Client, TestCase
from django.test.utils import override_settings
import networkx as nx

from .models import AuthToken, BroadcastMessage, CareProfile, FamilyMember, LocationPing, NotificationCategory, SosAlert, SosDelivery
from .routing import PlannedRoute, haversine_meters, plan_route_for_points
from .services import dispatch_broadcast
from .utils import token_digest


@override_settings(DEBUG=True)
class ApiTestCase(TestCase):
    def setUp(self):
        self.client = Client()

    def post_json(self, path, payload, token=None):
        headers = {}
        if token:
            headers["HTTP_AUTHORIZATION"] = f"Token {token}"
        return self.client.post(
            path,
            data=json.dumps(payload),
            content_type="application/json",
            **headers,
        )

    def patch_json(self, path, payload, token=None):
        headers = {}
        if token:
            headers["HTTP_AUTHORIZATION"] = f"Token {token}"
        return self.client.patch(
            path,
            data=json.dumps(payload),
            content_type="application/json",
            **headers,
        )

    def get_json(self, path, token=None):
        headers = {}
        if token:
            headers["HTTP_AUTHORIZATION"] = f"Token {token}"
        return self.client.get(path, **headers)

    def register_owner(self, phone="+998 90 321 45 67", full_name="Nodir Yusupov"):
        request_response = self.post_json("/api/v1/auth/request-code/", {"phone": phone})
        self.assertEqual(request_response.status_code, 201)
        challenge_id = request_response.json()["challengeId"]

        verify_response = self.post_json(
            "/api/v1/auth/verify-code/",
            {"phone": phone, "challengeId": challenge_id, "code": "2580"},
        )
        self.assertEqual(verify_response.status_code, 200)

        register_response = self.post_json(
            "/api/v1/auth/register/",
            {"phone": phone, "fullName": full_name, "challengeId": challenge_id},
        )
        self.assertEqual(register_response.status_code, 201)
        return register_response.json()


class AuthFlowTests(ApiTestCase):
    def test_register_flow_returns_dashboard(self):
        payload = self.register_owner()

        self.assertIn("token", payload)
        dashboard = payload["dashboard"]
        self.assertEqual(dashboard["caregiverName"], "Nodir")
        self.assertEqual(dashboard["profile"]["phone"], "+998 90 321 45 67")
        self.assertEqual(dashboard["selfMember"]["name"], "Nodir Yusupov")
        self.assertEqual(dashboard["selfMember"]["relation"], "Men")
        self.assertEqual(dashboard["trustedPlacesCount"], 0)
        self.assertEqual(dashboard["profile"]["address"], "")
        self.assertEqual(dashboard["familyLabel"], "")

    def test_token_is_not_stored_in_plain_text(self):
        payload = self.register_owner()
        stored_token = AuthToken.objects.get(user__care_profile__phone="998903214567")
        self.assertNotEqual(stored_token.key, payload["token"])
        self.assertEqual(stored_token.key, token_digest(payload["token"]))

    def test_authenticated_endpoints_accept_common_mobile_auth_headers(self):
        payload = self.register_owner()
        token = payload["token"]

        for header_value in (f"Token {token}", f"Bearer {token}", token):
            with self.subTest(header_value=header_value.split(" ")[0]):
                response = self.client.get(
                    "/api/v1/dashboard/",
                    HTTP_AUTHORIZATION=header_value,
                )
                self.assertEqual(response.status_code, 200)

        response = self.client.get(
            "/api/v1/dashboard/",
            HTTP_X_AUTHORIZATION=f"Token {token}",
        )
        self.assertEqual(response.status_code, 200)


class InvitationTests(ApiTestCase):
    def test_platform_invitation_can_be_accepted(self):
        owner_payload = self.register_owner()
        owner_profile = CareProfile.objects.get(phone="998903214567")

        candidate_user = User.objects.create_user(username="998942221100", password="secret")
        candidate_profile = CareProfile.objects.create(
            user=candidate_user,
            full_name="Madina Yusupova",
            phone="998942221100",
            address="Mirzo Ulug'bek, TTZ-2",
            avatar_seed=5,
        )

        invite_response = self.post_json(
            "/api/v1/invitations/",
            {"phone": "+998 94 222 11 00", "name": "", "relation": "Singil"},
            token=owner_payload["token"],
        )
        self.assertEqual(invite_response.status_code, 201)
        invitation = invite_response.json()
        self.assertTrue(invitation["isPlatformUser"])

        request_response = self.post_json(
            "/api/v1/auth/request-code/",
            {"phone": "+998 94 222 11 00"},
        )
        challenge_id = request_response.json()["challengeId"]
        self.post_json(
            "/api/v1/auth/verify-code/",
            {"phone": "+998 94 222 11 00", "challengeId": challenge_id, "code": "2580"},
        )
        login_response = self.post_json(
            "/api/v1/auth/login/",
            {"phone": "+998 94 222 11 00", "challengeId": challenge_id},
        )
        self.assertEqual(login_response.status_code, 200)
        self.assertEqual(len(login_response.json()["dashboard"]["incomingInvitations"]), 1)

        accept_response = self.post_json(
            f"/api/v1/invitations/{invitation['id']}/accept/",
            {},
            token=login_response.json()["token"],
        )
        self.assertEqual(accept_response.status_code, 200)

        candidate_profile.refresh_from_db()
        self.assertEqual(candidate_profile.family_id, owner_profile.family_id)
        self.assertTrue(
            FamilyMember.objects.filter(
                family=owner_profile.family,
                phone="998942221100",
                linked_profile=candidate_profile,
                name="Madina Yusupova",
            ).exists()
        )

    def test_waiting_install_invitation_becomes_incoming_after_registration(self):
        owner_payload = self.register_owner()

        invite_response = self.post_json(
            "/api/v1/invitations/",
            {"phone": "+998 90 555 66 77", "name": "Aziza", "relation": "Singil"},
            token=owner_payload["token"],
        )
        self.assertEqual(invite_response.status_code, 201)
        self.assertFalse(invite_response.json()["isPlatformUser"])

        candidate_payload = self.register_owner(
            phone="+998 90 555 66 77",
            full_name="Aziza Karimova",
        )
        incoming = candidate_payload["dashboard"]["incomingInvitations"]
        self.assertEqual(len(incoming), 1)
        self.assertTrue(incoming[0]["isIncoming"])
        self.assertEqual(incoming[0]["familyName"], "")

    def test_invitation_delete_removes_invite(self):
        owner_payload = self.register_owner()

        invite_response = self.post_json(
            "/api/v1/invitations/",
            {"phone": "+998 90 555 66 77", "name": "Aziza", "relation": "Singil"},
            token=owner_payload["token"],
        )
        invitation_id = invite_response.json()["id"]

        response = self.client.delete(
            f"/api/v1/invitations/{invitation_id}/",
            HTTP_AUTHORIZATION=f"Token {owner_payload['token']}",
        )
        self.assertEqual(response.status_code, 200)
        self.assertFalse(CareProfile.objects.get(phone="998903214567").family.invitations.exists())


class SosFlowTests(ApiTestCase):
    def test_trigger_sos_creates_deliveries_and_notification(self):
        owner_payload = self.register_owner()
        token = owner_payload["token"]
        owner_profile = CareProfile.objects.get(phone="998903214567")

        FamilyMember.objects.create(
            family=owner_profile.family,
            name="Anvar Yusupov",
            relation="Dada",
            age=61,
            phone="998901234567",
        )
        FamilyMember.objects.create(
            family=owner_profile.family,
            name="Dilnoza Yusupova",
            relation="Ona",
            age=56,
            phone="998935556677",
        )

        response = self.post_json("/api/v1/sos/trigger/", {}, token=token)
        self.assertEqual(response.status_code, 201)

        alert = SosAlert.objects.get()
        self.assertEqual(alert.triggered_by.phone, "998903214567")
        self.assertEqual(SosDelivery.objects.filter(alert=alert).count(), 2)
        self.assertTrue(
            owner_profile.family.notifications.filter(category=NotificationCategory.SAFETY).exists()
        )


class ProfileSyncTests(ApiTestCase):
    def test_profile_patch_updates_family_and_self_member(self):
        owner_payload = self.register_owner()
        token = owner_payload["token"]
        owner_profile = CareProfile.objects.get(phone="998903214567")

        response = self.patch_json(
            "/api/v1/me/profile/",
            {
                "fullName": "Nodir Karimov",
                "phone": "+998 90 321 45 67",
                "address": "Yunusobod, 7-kvartal",
                "familyLabel": "Karimov oilasi",
            },
            token=token,
        )
        self.assertEqual(response.status_code, 200)

        owner_profile.refresh_from_db()
        self.assertEqual(owner_profile.family.name, "Karimov oilasi")
        self.assertTrue(
            FamilyMember.objects.filter(
                family=owner_profile.family,
                linked_profile=owner_profile,
                name="Nodir Karimov",
                address="Yunusobod, 7-kvartal",
            ).exists()
        )

    def test_profile_phone_patch_updates_username(self):
        owner_payload = self.register_owner()
        token = owner_payload["token"]
        owner_profile = CareProfile.objects.get(phone="998903214567")

        response = self.patch_json(
            "/api/v1/me/profile/",
            {
                "fullName": "Nodir Karimov",
                "phone": "+998 90 111 22 33",
            },
            token=token,
        )
        self.assertEqual(response.status_code, 200)

        owner_profile.refresh_from_db()
        owner_profile.user.refresh_from_db()
        self.assertEqual(owner_profile.phone, "998901112233")
        self.assertEqual(owner_profile.user.username, "998901112233")


class LocationValidationTests(ApiTestCase):
    def test_invalid_location_payload_returns_400(self):
        owner_payload = self.register_owner()
        member_id = owner_payload["dashboard"]["selfMember"]["id"]

        response = self.post_json(
            f"/api/v1/members/{member_id}/location/",
            {"lat": 200, "lon": 69.2},
            token=owner_payload["token"],
        )
        self.assertEqual(response.status_code, 400)
        self.assertIn("detail", response.json())

    def test_member_location_history_returns_points_for_selected_member(self):
        owner_payload = self.register_owner()
        token = owner_payload["token"]
        member_id = owner_payload["dashboard"]["selfMember"]["id"]

        first_response = self.post_json(
            f"/api/v1/members/{member_id}/location/",
            {
                "lat": 41.3111,
                "lon": 69.2797,
                "address": "Birinchi nuqta",
                "placeLabel": "Uy",
            },
            token=token,
        )
        self.assertEqual(first_response.status_code, 200)

        second_response = self.post_json(
            f"/api/v1/members/{member_id}/location/",
            {
                "lat": 41.315,
                "lon": 69.284,
                "address": "Ikkinchi nuqta",
                "placeLabel": "Ko'cha",
            },
            token=token,
        )
        self.assertEqual(second_response.status_code, 200)

        history_response = self.get_json(
            f"/api/v1/members/{member_id}/location-history/",
            token=token,
        )
        self.assertEqual(history_response.status_code, 200)
        payload = history_response.json()

        self.assertEqual(payload["memberId"], member_id)
        self.assertEqual(len(payload["history"]), 2)
        self.assertEqual(payload["history"][0]["placeLabel"], "Uy")
        self.assertEqual(payload["history"][1]["placeLabel"], "Ko'cha")
        self.assertEqual(payload["history"][0]["memberId"], member_id)
        self.assertEqual(LocationPing.objects.filter(member_id=member_id).count(), 2)


class RoutingTests(ApiTestCase):
    def build_test_graph(self):
        graph = nx.DiGraph()
        graph.add_node(1, lat=41.0, lon=69.0)
        graph.add_node(2, lat=41.0, lon=69.01)
        graph.add_node(3, lat=41.01, lon=69.01)
        graph.add_node(4, lat=41.01, lon=69.02)
        length_12 = haversine_meters(41.0, 69.0, 41.0, 69.01)
        length_23 = haversine_meters(41.0, 69.01, 41.01, 69.01)
        length_34 = haversine_meters(41.01, 69.01, 41.01, 69.02)
        graph.add_edge(1, 2, length=length_12)
        graph.add_edge(2, 3, length=length_23)
        graph.add_edge(3, 4, length=length_34)
        graph.add_edge(1, 3, length=length_12 + length_23 + 500.0)
        graph.add_edge(2, 4, length=length_23 + length_34 + 500.0)
        return graph

    def test_astar_and_dijkstra_return_same_optimal_path(self):
        graph = self.build_test_graph()
        expected_length = graph[1][2]["length"] + graph[2][3]["length"] + graph[3][4]["length"]

        with patch("familycare_api.routing._load_route_graph", return_value=graph):
            resolved_graph, dijkstra_nodes, astar_nodes, dijkstra_length, astar_length = plan_route_for_points(
                41.0001,
                69.0001,
                41.0101,
                69.0199,
            )

        self.assertEqual(resolved_graph.number_of_nodes(), 4)
        self.assertEqual(dijkstra_nodes, [1, 2, 3, 4])
        self.assertEqual(astar_nodes, [1, 2, 3, 4])
        self.assertAlmostEqual(dijkstra_length, expected_length, places=5)
        self.assertAlmostEqual(astar_length, expected_length, places=5)

    def test_sos_routes_endpoint_serializes_planned_route(self):
        owner_payload = self.register_owner()
        token = owner_payload["token"]
        owner_profile = CareProfile.objects.get(phone="998903214567")
        self.assertIsNotNone(owner_profile.family_id)

        fake_route = PlannedRoute(
            alert_id=88,
            member_id=123,
            member_name="Anvar Yusupov",
            relation="Dada",
            phone="998901234567",
            source_member_id=owner_payload["dashboard"]["selfMember"]["id"],
            source_lat=41.3111,
            source_lon=69.2797,
            target_lat=41.3118,
            target_lon=69.2819,
            dijkstra_points=[{"lat": 41.3111, "lon": 69.2797}, {"lat": 41.3118, "lon": 69.2819}],
            astar_points=[{"lat": 41.3111, "lon": 69.2797}, {"lat": 41.3118, "lon": 69.2819}],
            dijkstra_length_meters=250.0,
            astar_length_meters=250.0,
            is_same_path=True,
            graph_nodes=12,
            graph_edges=20,
        )

        with patch("familycare_api.views.sos_routes_for_profile", return_value=[fake_route]):
            response = self.get_json("/api/v1/sos/routes/", token=token)

        self.assertEqual(response.status_code, 200)
        payload = response.json()
        self.assertEqual(payload["sourceMemberId"], owner_payload["dashboard"]["selfMember"]["id"])
        self.assertEqual(len(payload["routes"]), 1)
        self.assertEqual(payload["routes"][0]["memberName"], "Anvar Yusupov")
        self.assertEqual(len(payload["routes"][0]["astarPoints"]), 2)
        self.assertEqual(len(payload["routes"][0]["dijkstraPoints"]), 2)


class BroadcastTests(ApiTestCase):
    def test_dispatch_broadcast_creates_notifications_for_all_families(self):
        first_owner = self.register_owner()
        self.register_owner(phone="+998 91 222 33 44", full_name="Malika Karimova")

        admin_user = CareProfile.objects.get(phone="998903214567").user
        broadcast = BroadcastMessage.objects.create(
            title="Tizim yangilandi",
            message="Bugun barcha foydalanuvchilar uchun yangi xususiyatlar yoqildi.",
            created_by=admin_user,
        )

        dispatch_broadcast(broadcast)

        self.assertEqual(broadcast.delivered_families, 2)
        self.assertEqual(
            CareProfile.objects.get(phone="998903214567").family.notifications.filter(
                title="Tizim yangilandi"
            ).count(),
            1,
        )
        self.assertEqual(
            CareProfile.objects.get(phone="998912223344").family.notifications.filter(
                title="Tizim yangilandi"
            ).count(),
            1,
        )
