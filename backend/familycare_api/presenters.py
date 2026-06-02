from datetime import timedelta

from django.utils import timezone

from .models import FamilyInvitation, InvitationStatus, SosAlertStatus
from .utils import as_float, format_phone, next_check_in_label, relative_time_label


def isoformat(value):
    return timezone.localtime(value).isoformat() if value else None


def profile_permissions_payload(profile):
    return {
        "locationEnabled": profile.location_enabled,
        "microphoneEnabled": profile.microphone_enabled,
        "notificationsEnabled": profile.notifications_enabled,
        "preciseLocationEnabled": profile.precise_location_enabled,
        "backgroundRefreshEnabled": profile.background_refresh_enabled,
    }


def profile_payload(profile):
    family_label = profile.family.name if profile.family_id and profile.family else ""
    return {
        "id": profile.id,
        "fullName": profile.full_name,
        "phone": format_phone(profile.phone),
        "email": profile.email,
        "familyLabel": family_label,
        "address": profile.address,
        "emergencyContact": format_phone(profile.emergency_contact),
        "avatarSeed": profile.avatar_seed,
        "avatarUri": profile.avatar_uri,
        "bio": profile.bio,
        "permissions": profile_permissions_payload(profile),
    }


def is_member_online(member) -> bool:
    if not member or not member.last_seen_at:
        return False
    return timezone.now() - member.last_seen_at <= timedelta(minutes=5)


def member_presence_label(member) -> str:
    if is_member_online(member):
        return "Online"
    relative_label = relative_time_label(member.last_seen_at)
    return f"Offline ({relative_label})" if relative_label else "Offline"


def member_payload(member, current_profile=None):
    is_current_user = bool(current_profile and member.linked_profile_id == current_profile.id)
    return {
        "id": member.id,
        "name": member.name,
        "relation": "Men" if is_current_user else member.relation,
        "age": member.age,
        "lat": as_float(member.latitude),
        "lon": as_float(member.longitude),
        "address": member.address,
        "placeLabel": member.place_label,
        "battery": member.battery_level,
        "steps": member.steps,
        "heartRate": member.heart_rate,
        "lastUpdate": relative_time_label(member.last_seen_at),
        "lastSeenAt": isoformat(member.last_seen_at),
        "status": member.status,
        "note": member.note,
        "safeZone": member.safe_zone,
        "phone": format_phone(member.phone),
        "schedule": member.schedule_label,
        "avatarSeed": member.avatar_seed,
        "avatarUri": member.avatar_uri,
        "distanceKm": as_float(member.distance_km),
        "isCurrentUser": is_current_user,
        "isOnline": is_member_online(member),
        "presenceLabel": member_presence_label(member),
    }


def location_history_payload(ping):
    return {
        "id": ping.id,
        "memberId": ping.member_id,
        "lat": as_float(ping.latitude),
        "lon": as_float(ping.longitude),
        "address": ping.address,
        "placeLabel": ping.place_label,
        "battery": ping.battery_level,
        "steps": ping.steps,
        "heartRate": ping.heart_rate,
        "distanceKm": as_float(ping.distance_km),
        "createdAt": isoformat(ping.created_at),
        "timeLabel": relative_time_label(ping.created_at),
    }


def route_point_payload(point):
    return {
        "lat": point["lat"],
        "lon": point["lon"],
    }


def sos_route_payload(route):
    return {
        "alertId": route.alert_id,
        "memberId": route.member_id,
        "memberName": route.member_name,
        "relation": route.relation,
        "phone": route.phone,
        "sourceMemberId": route.source_member_id,
        "sourceLat": route.source_lat,
        "sourceLon": route.source_lon,
        "targetLat": route.target_lat,
        "targetLon": route.target_lon,
        "dijkstraPoints": [route_point_payload(point) for point in route.dijkstra_points],
        "astarPoints": [route_point_payload(point) for point in route.astar_points],
        "dijkstraLengthMeters": route.dijkstra_length_meters,
        "astarLengthMeters": route.astar_length_meters,
        "isSamePath": route.is_same_path,
        "graphNodes": route.graph_nodes,
        "graphEdges": route.graph_edges,
    }


def activity_payload(item):
    return {
        "id": item.id,
        "memberId": item.member_id,
        "title": item.title,
        "subtitle": item.subtitle,
        "timeLabel": relative_time_label(item.created_at),
        "severity": item.severity,
        "createdAt": isoformat(item.created_at),
    }


def invitation_payload(invitation, current_profile=None):
    is_incoming = bool(
        current_profile
        and invitation.platform_profile_id == current_profile.id
        and invitation.family_id != current_profile.family_id
    )
    return {
        "id": invitation.id,
        "name": invitation.name,
        "relation": invitation.relation,
        "phone": format_phone(invitation.phone),
        "sentAtLabel": relative_time_label(invitation.sent_at),
        "sentAt": isoformat(invitation.sent_at),
        "status": invitation.status,
        "isPlatformUser": invitation.is_platform_user,
        "acceptedAt": isoformat(invitation.accepted_at),
        "familyName": invitation.family.name if invitation.family_id else "",
        "invitedByName": invitation.invited_by.full_name if invitation.invited_by_id else "",
        "isIncoming": is_incoming,
    }


def notification_payload(notification):
    return {
        "id": notification.id,
        "title": notification.title,
        "message": notification.message,
        "timeLabel": relative_time_label(notification.created_at),
        "createdAt": isoformat(notification.created_at),
        "category": notification.category,
        "isRead": notification.is_read,
        "inviteId": notification.invitation_id,
        "actionLabel": notification.action_label or None,
    }


def sos_delivery_payload(delivery):
    return {
        "memberId": delivery.member_id,
        "name": delivery.member.name,
        "phone": format_phone(delivery.member.phone),
        "state": delivery.state,
        "deliveredAt": isoformat(delivery.delivered_at),
    }


def sos_alert_payload(alert):
    member = alert.triggered_by
    return {
        "id": alert.id,
        "memberId": member.id if member else None,
        "name": member.name if member else "Noma'lum",
        "relation": member.relation if member else "",
        "phone": format_phone(member.phone) if member else "",
        "address": alert.address,
        "lastUpdate": relative_time_label(alert.triggered_at),
        "status": alert.status,
        "summary": alert.summary,
        "triggeredAt": isoformat(alert.triggered_at),
    }


def sos_state_payload(self_member, family):
    if not self_member:
        return {
            "alertId": None,
            "isActive": False,
            "isSending": False,
            "summary": "",
            "sentAtLabel": "",
            "contacts": [],
        }

    latest_alert = (
        family.sos_alerts.filter(status=SosAlertStatus.ACTIVE, triggered_by=self_member)
        .prefetch_related("deliveries__member")
        .first()
    )
    if not latest_alert:
        return {
            "alertId": None,
            "isActive": False,
            "isSending": False,
            "summary": "",
            "sentAtLabel": "",
            "contacts": [],
        }

    return {
        "alertId": latest_alert.id,
        "isActive": True,
        "isSending": False,
        "summary": latest_alert.summary,
        "sentAtLabel": relative_time_label(latest_alert.triggered_at),
        "contacts": [sos_delivery_payload(delivery) for delivery in latest_alert.deliveries.all()],
    }


def sos_routes_payload(source_member_id: int, routes):
    return {
        "sourceMemberId": source_member_id,
        "routes": [sos_route_payload(route) for route in routes],
    }


def dashboard_payload(profile):
    family = profile.family
    members = list(
        family.members.filter(is_active=True)
        .select_related("linked_profile")
        .order_by("id")
    )
    self_member = next((member for member in members if member.linked_profile_id == profile.id), None)
    related_members = [member for member in members if member.linked_profile_id != profile.id]
    active_alerts = list(
        family.sos_alerts.filter(status=SosAlertStatus.ACTIVE)
        .select_related("triggered_by")
        .prefetch_related("deliveries__member")
    )
    notifications = list(
        family.notifications.select_related("invitation").order_by("-created_at", "-id")[:20]
    )
    activity_items = list(
        family.activity_items.select_related("member").order_by("-created_at", "-id")[:12]
    )
    incoming_invitations = list(
        FamilyInvitation.objects.filter(
            platform_profile=profile,
            status__in=[InvitationStatus.PENDING_ACCEPTANCE, InvitationStatus.WAITING_INSTALL],
        )
        .exclude(family=family)
        .select_related("family", "invited_by")
        .order_by("-sent_at", "-id")[:20]
    )

    selected_member_id = self_member.id if self_member else (related_members[0].id if related_members else None)
    last_touch = self_member.last_seen_at if self_member else profile.updated_at

    family_label = family.name or ""

    return {
        "isRegistered": True,
        "caregiverName": profile.full_name.strip().split(" ")[0] if profile.full_name.strip() else profile.full_name,
        "familyLabel": family_label,
        "lastSyncLabel": relative_time_label(last_touch),
        "nextCheckInLabel": next_check_in_label(family.next_check_in_at),
        "trustedPlacesCount": 0,
        "profile": profile_payload(profile),
        "selfMember": member_payload(self_member, current_profile=profile) if self_member else None,
        "members": [member_payload(member, current_profile=profile) for member in related_members],
        "invitations": [
            invitation_payload(invitation, current_profile=profile)
            for invitation in (
                family.invitations
                .select_related("family", "invited_by", "platform_profile")
                .order_by("-sent_at", "-id")[:20]
            )
        ],
        "incomingInvitations": [
            invitation_payload(invitation, current_profile=profile)
            for invitation in incoming_invitations
        ],
        "notifications": [notification_payload(notification) for notification in notifications],
        "activityFeed": [activity_payload(item) for item in activity_items],
        "selectedMemberId": selected_member_id,
        "activeSosAlerts": [sos_alert_payload(alert) for alert in active_alerts],
        "sosState": sos_state_payload(self_member, family),
    }
