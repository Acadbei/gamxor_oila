from django.conf import settings
from django.db.models import Q
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_http_methods

from .auth import api_login_required
from .models import AppNotification, FamilyInvitation, FamilyMember, SosAlert, SosAlertStatus
from .presenters import (
    activity_payload,
    dashboard_payload,
    invitation_payload,
    location_history_payload,
    member_payload,
    notification_payload,
    profile_payload,
    sos_routes_payload,
    sos_alert_payload,
)
from .services import (
    accept_invitation,
    create_otp,
    dismiss_notification,
    ensure_family,
    get_verified_challenge,
    login_user,
    mark_all_notifications_read,
    mark_notification_read,
    member_location_history,
    record_location,
    register_user,
    sync_self_member,
    sos_routes_for_profile,
    resolve_sos,
    remove_invitation,
    send_invitation,
    trigger_sos,
    update_member,
    update_profile,
    verify_otp,
)
from .routing import RoutePlanningError
from .utils import parse_json_body, pick


def error_response(message: str, status: int = 400):
    return JsonResponse({"detail": message}, status=status)


def family_member_for_profile(profile, member_id: int):
    family = ensure_family(profile)
    return family.members.select_related("linked_profile").filter(pk=member_id, is_active=True).first()


def invitation_for_profile(profile, invitation_id: int):
    family = ensure_family(profile)
    return (
        FamilyInvitation.objects.select_related("platform_profile", "family", "invited_by")
        .filter(pk=invitation_id)
        .filter(
            Q(family=family)
            | Q(platform_profile=profile)
            | Q(phone=profile.phone)
        )
        .first()
    )


def notification_for_profile(profile, notification_id: int):
    family = ensure_family(profile)
    return family.notifications.select_related("invitation").filter(pk=notification_id).first()


def sos_for_profile(profile, alert_id: int):
    family = ensure_family(profile)
    return family.sos_alerts.select_related("triggered_by").filter(pk=alert_id).first()


@csrf_exempt
@require_http_methods(["GET"])
def healthcheck(request):
    return JsonResponse({"ok": True, "service": "familycare-api"})


@csrf_exempt
@require_http_methods(["POST"])
def request_code_view(request):
    try:
        payload = parse_json_body(request)
        challenge, is_registered = create_otp(pick(payload, "phone"))
    except ValueError as exc:
        return error_response(str(exc))

    response = {
        "challengeId": str(challenge.challenge_id),
        "isRegistered": is_registered,
        "expiresAt": challenge.expires_at.isoformat(),
    }
    if settings.DEBUG:
        response["otpHint"] = challenge.code
    return JsonResponse(response, status=201)


@csrf_exempt
@require_http_methods(["POST"])
def verify_code_view(request):
    try:
        payload = parse_json_body(request)
        challenge = verify_otp(
            phone=pick(payload, "phone"),
            challenge_id=pick(payload, "challengeId", "challenge_id"),
            code=pick(payload, "code", "otp"),
        )
    except ValueError as exc:
        return error_response(str(exc))

    return JsonResponse(
        {
            "verified": True,
            "challengeId": str(challenge.challenge_id),
            "verifiedAt": challenge.verified_at.isoformat() if challenge.verified_at else None,
        }
    )


@csrf_exempt
@require_http_methods(["POST"])
def register_view(request):
    try:
        payload = parse_json_body(request)
        challenge = get_verified_challenge(
            phone=pick(payload, "phone"),
            challenge_id=pick(payload, "challengeId", "challenge_id"),
            code=pick(payload, "code"),
        )
        profile, token = register_user(
            full_name=pick(payload, "fullName", "full_name"),
            phone=pick(payload, "phone"),
            challenge=challenge,
        )
    except ValueError as exc:
        return error_response(str(exc))

    return JsonResponse(
        {
            "token": getattr(token, "raw_key", token.key),
            "expiresAt": token.expires_at.isoformat(),
            "profile": profile_payload(profile),
            "dashboard": dashboard_payload(profile),
        },
        status=201,
    )


@csrf_exempt
@require_http_methods(["POST"])
def login_view(request):
    try:
        payload = parse_json_body(request)
        challenge = get_verified_challenge(
            phone=pick(payload, "phone"),
            challenge_id=pick(payload, "challengeId", "challenge_id"),
            code=pick(payload, "code"),
        )
        profile, token = login_user(
            phone=pick(payload, "phone"),
            challenge=challenge,
        )
    except ValueError as exc:
        return error_response(str(exc))

    return JsonResponse(
        {
            "token": getattr(token, "raw_key", token.key),
            "expiresAt": token.expires_at.isoformat(),
            "profile": profile_payload(profile),
            "dashboard": dashboard_payload(profile),
        }
    )


@csrf_exempt
@require_http_methods(["POST"])
@api_login_required
def logout_view(request):
    request.auth_token.delete()
    return JsonResponse({"ok": True})


@require_http_methods(["GET"])
@api_login_required
def dashboard_view(request):
    ensure_family(request.profile)
    return JsonResponse(dashboard_payload(request.profile))


@csrf_exempt
@require_http_methods(["GET", "PATCH"])
@api_login_required
def profile_view(request):
    ensure_family(request.profile)
    if request.method == "GET":
        return JsonResponse(profile_payload(request.profile))

    try:
        payload = parse_json_body(request)
        profile = update_profile(request.profile, payload)
    except ValueError as exc:
        return error_response(str(exc))

    return JsonResponse(profile_payload(profile))


@require_http_methods(["GET"])
@api_login_required
def members_view(request):
    ensure_family(request.profile)
    family = request.profile.family
    members = list(family.members.select_related("linked_profile").filter(is_active=True).order_by("id"))
    self_member = next((member for member in members if member.linked_profile_id == request.profile.id), None)
    others = [member for member in members if member.linked_profile_id != request.profile.id]
    return JsonResponse(
        {
            "selfMember": member_payload(self_member, current_profile=request.profile) if self_member else None,
            "members": [member_payload(member, current_profile=request.profile) for member in others],
        }
    )


@csrf_exempt
@require_http_methods(["GET", "PATCH"])
@api_login_required
def member_detail_view(request, member_id: int):
    member = family_member_for_profile(request.profile, member_id)
    if not member:
        return error_response("A'zo topilmadi.", status=404)

    if request.method == "GET":
        return JsonResponse(member_payload(member, current_profile=request.profile))

    try:
        payload = parse_json_body(request)
        member = update_member(member, payload)
    except ValueError as exc:
        return error_response(str(exc))

    return JsonResponse(member_payload(member, current_profile=request.profile))


@csrf_exempt
@require_http_methods(["POST"])
@api_login_required
def member_location_view(request, member_id: int):
    member = family_member_for_profile(request.profile, member_id)
    if not member:
        return error_response("A'zo topilmadi.", status=404)

    try:
        payload = parse_json_body(request)
        member = record_location(member, payload)
    except ValueError as exc:
        return error_response(str(exc))

    return JsonResponse(member_payload(member, current_profile=request.profile))


@require_http_methods(["GET"])
@api_login_required
def member_location_history_view(request, member_id: int):
    member = family_member_for_profile(request.profile, member_id)
    if not member:
        return error_response("A'zo topilmadi.", status=404)

    raw_limit = request.GET.get("limit", "100")
    try:
        limit = int(raw_limit)
    except (TypeError, ValueError):
        return error_response("limit son bo'lishi kerak.")

    history = member_location_history(member, limit=limit)
    return JsonResponse(
        {
            "memberId": member.id,
            "history": [location_history_payload(item) for item in reversed(history)],
        }
    )


@csrf_exempt
@require_http_methods(["GET", "POST"])
@api_login_required
def invitations_view(request):
    ensure_family(request.profile)
    family = request.profile.family

    if request.method == "GET":
        invitations = (
            family.invitations
            .select_related("family", "invited_by", "platform_profile")
            .order_by("-sent_at", "-id")[:20]
        )
        return JsonResponse({"invitations": [invitation_payload(item) for item in invitations]})

    try:
        payload = parse_json_body(request)
        invitation = send_invitation(
            profile=request.profile,
            name=pick(payload, "name"),
            relation=pick(payload, "relation"),
            phone=pick(payload, "phone"),
        )
    except ValueError as exc:
        return error_response(str(exc))

    return JsonResponse(invitation_payload(invitation), status=201)


@csrf_exempt
@require_http_methods(["DELETE"])
@api_login_required
def invitation_delete_view(request, invitation_id: int):
    invitation = invitation_for_profile(request.profile, invitation_id)
    if not invitation:
        return error_response("Taklif topilmadi.", status=404)
    remove_invitation(invitation)
    return JsonResponse({"ok": True})


@csrf_exempt
@require_http_methods(["POST"])
@api_login_required
def invitation_accept_view(request, invitation_id: int):
    invitation = invitation_for_profile(request.profile, invitation_id)
    if not invitation:
        return error_response("Taklif topilmadi.", status=404)

    try:
        invitation = accept_invitation(invitation, accepted_by=request.profile)
    except ValueError as exc:
        return error_response(str(exc))

    return JsonResponse(invitation_payload(invitation))


@require_http_methods(["GET"])
@api_login_required
def notifications_view(request):
    ensure_family(request.profile)
    notifications = (
        request.profile.family.notifications
        .select_related("invitation")
        .order_by("-created_at", "-id")[:30]
    )
    return JsonResponse({"notifications": [notification_payload(item) for item in notifications]})


@csrf_exempt
@require_http_methods(["POST"])
@api_login_required
def notification_read_view(request, notification_id: int):
    notification = notification_for_profile(request.profile, notification_id)
    if not notification:
        return error_response("Bildirishnoma topilmadi.", status=404)
    mark_notification_read(notification)
    return JsonResponse(notification_payload(notification))


@csrf_exempt
@require_http_methods(["POST"])
@api_login_required
def notifications_read_all_view(request):
    ensure_family(request.profile)
    mark_all_notifications_read(request.profile.family)
    notifications = (
        request.profile.family.notifications
        .select_related("invitation")
        .order_by("-created_at", "-id")[:30]
    )
    return JsonResponse({"notifications": [notification_payload(item) for item in notifications]})


@csrf_exempt
@require_http_methods(["DELETE"])
@api_login_required
def notification_delete_view(request, notification_id: int):
    notification = notification_for_profile(request.profile, notification_id)
    if not notification:
        return error_response("Bildirishnoma topilmadi.", status=404)
    dismiss_notification(notification)
    return JsonResponse({"ok": True})


@require_http_methods(["GET"])
@api_login_required
def activity_feed_view(request):
    ensure_family(request.profile)
    items = (
        request.profile.family.activity_items
        .select_related("member")
        .order_by("-created_at", "-id")[:20]
    )
    return JsonResponse({"activityFeed": [activity_payload(item) for item in items]})


@require_http_methods(["GET"])
@api_login_required
def sos_view(request):
    ensure_family(request.profile)
    alerts = (
        request.profile.family.sos_alerts.filter(status=SosAlertStatus.ACTIVE)
        .select_related("triggered_by")
        .order_by("-triggered_at", "-id")
    )
    return JsonResponse({"alerts": [sos_alert_payload(alert) for alert in alerts]})


@require_http_methods(["GET"])
@api_login_required
def sos_routes_view(request):
    ensure_family(request.profile)
    self_member = sync_self_member(request.profile)

    try:
        routes = sos_routes_for_profile(request.profile)
    except RoutePlanningError as exc:
        return error_response(str(exc), status=422)

    return JsonResponse(
        sos_routes_payload(
            source_member_id=self_member.id,
            routes=routes,
        )
    )


@csrf_exempt
@require_http_methods(["POST"])
@api_login_required
def sos_trigger_view(request):
    alert = trigger_sos(request.profile)
    return JsonResponse(sos_alert_payload(alert), status=201)


@csrf_exempt
@require_http_methods(["POST"])
@api_login_required
def sos_resolve_view(request, alert_id: int):
    alert = sos_for_profile(request.profile, alert_id)
    if not alert:
        return error_response("SOS holati topilmadi.", status=404)
    alert = resolve_sos(alert)
    return JsonResponse(sos_alert_payload(alert))
