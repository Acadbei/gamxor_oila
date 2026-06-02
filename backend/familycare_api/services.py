from datetime import timedelta
from decimal import Decimal
import secrets

from django.conf import settings
from django.contrib.auth.models import User
from django.db import transaction
from django.db.models import Q
from django.utils import timezone

from .models import (
    ActivityFeedItem,
    AppNotification,
    AuthToken,
    BroadcastMessage,
    CareProfile,
    Family,
    FamilyInvitation,
    FamilyMember,
    FeedSeverity,
    InvitationStatus,
    LocationPing,
    MemberStatus,
    NotificationCategory,
    OtpChallenge,
    SosAlert,
    SosAlertStatus,
    SosContactState,
    SosDelivery,
    TrustedPlace,
)
from .routing import PlannedRoute, plan_route_for_alert
from .utils import (
    canonical_phone,
    clamp_int,
    clean_text,
    format_phone,
    generate_token_key,
    parse_decimal,
    parse_lat_lon,
    token_digest,
)


DEFAULT_OTP_CODE = "2580"
OTP_EXPIRY_MINUTES = 10
AUTH_TOKEN_EXPIRY_DAYS = 30
MAX_AVATAR_SEED = 9999
MAX_DISTANCE_KM = Decimal("999.99")


def display_name(full_name: str) -> str:
    return (full_name or "").strip().split(" ")[0] if (full_name or "").strip() else ""


def default_family_name(full_name: str) -> str:
    parts = [part for part in (full_name or "").strip().split(" ") if part]
    if not parts:
        return "Family Care oilasi"
    return f"{parts[-1]} oilasi"


def create_notification(
    family: Family,
    title: str,
    message: str,
    category: str,
    invitation: FamilyInvitation | None = None,
    action_label: str = "",
):
    return AppNotification.objects.create(
        family=family,
        invitation=invitation,
        title=title,
        message=message,
        category=category,
        action_label=action_label,
    )


def create_activity(
    family: Family,
    title: str,
    subtitle: str,
    severity: str = FeedSeverity.NEUTRAL,
    member: FamilyMember | None = None,
):
    return ActivityFeedItem.objects.create(
        family=family,
        member=member,
        title=title,
        subtitle=subtitle,
        severity=severity,
    )


def issue_token(user: User) -> AuthToken:
    raw_token = generate_token_key()
    token = AuthToken.objects.create(
        user=user,
        key=token_digest(raw_token),
        expires_at=timezone.now() + timedelta(days=AUTH_TOKEN_EXPIRY_DAYS),
    )
    token.raw_key = raw_token
    return token


def create_otp(phone: str) -> tuple[OtpChallenge, bool]:
    normalized_phone = canonical_phone(phone)
    if len(normalized_phone) != 12:
        raise ValueError("Telefon raqamni to'liq kiriting.")

    OtpChallenge.objects.filter(
        phone=normalized_phone,
        consumed_at__isnull=True,
        expires_at__gt=timezone.now(),
    ).update(consumed_at=timezone.now())

    otp_code = DEFAULT_OTP_CODE if settings.DEBUG else f"{secrets.randbelow(1_000_000):06d}"
    challenge = OtpChallenge.objects.create(
        phone=normalized_phone,
        code=otp_code,
        expires_at=timezone.now() + timedelta(minutes=OTP_EXPIRY_MINUTES),
    )
    is_registered = CareProfile.objects.filter(phone=normalized_phone, family__isnull=False).exists()
    return challenge, is_registered


def verify_otp(phone: str, challenge_id, code: str) -> OtpChallenge:
    normalized_phone = canonical_phone(phone)
    normalized_code = "".join(character for character in str(code or "") if character.isdigit())
    challenge = OtpChallenge.objects.filter(challenge_id=challenge_id, phone=normalized_phone).first()

    if not challenge:
        raise ValueError("Tasdiqlash sessiyasi topilmadi.")
    if challenge.consumed_at:
        raise ValueError("Tasdiqlash sessiyasi ishlatib bo'lingan.")
    if challenge.expires_at <= timezone.now():
        raise ValueError("Kodning amal qilish vaqti tugagan.")
    if normalized_code != challenge.code:
        raise ValueError("Kod noto'g'ri.")

    challenge.is_verified = True
    challenge.verified_at = timezone.now()
    challenge.save()
    return challenge


def get_verified_challenge(phone: str, challenge_id, code: str | None = None) -> OtpChallenge:
    normalized_phone = canonical_phone(phone)
    challenge = OtpChallenge.objects.filter(challenge_id=challenge_id, phone=normalized_phone).first()
    if not challenge:
        raise ValueError("Tasdiqlash sessiyasi topilmadi.")
    if challenge.consumed_at:
        raise ValueError("Tasdiqlash sessiyasi ishlatib bo'lingan.")
    if challenge.expires_at <= timezone.now():
        raise ValueError("Kodning amal qilish vaqti tugagan.")
    if challenge.is_verified:
        return challenge
    if code:
        return verify_otp(phone=phone, challenge_id=challenge_id, code=code)
    raise ValueError("Avval SMS kodni tasdiqlang.")


def consume_challenge(challenge: OtpChallenge):
    updated_rows = OtpChallenge.objects.filter(
        pk=challenge.pk,
        consumed_at__isnull=True,
    ).update(consumed_at=timezone.now())
    if updated_rows == 0:
        raise ValueError("Tasdiqlash sessiyasi ishlatib bo'lingan.")


def seed_default_trusted_places(family: Family, address: str):
    if family.trusted_places.exists():
        return

    TrustedPlace.objects.bulk_create(
        [
            TrustedPlace(
                family=family,
                name="Uy",
                address=address or "Yashash manzili",
                latitude=Decimal("41.311100"),
                longitude=Decimal("69.279700"),
                radius_meters=250,
                kind="home",
            ),
            TrustedPlace(
                family=family,
                name="Ish",
                address="Ofis hududi",
                latitude=Decimal("41.299500"),
                longitude=Decimal("69.240100"),
                radius_meters=300,
                kind="work",
            ),
            TrustedPlace(
                family=family,
                name="Dorixona",
                address="Mahalla dorixonasi",
                latitude=Decimal("41.300000"),
                longitude=Decimal("69.200000"),
                radius_meters=150,
                kind="health",
            ),
        ]
    )


def ensure_family(profile: CareProfile) -> Family:
    if profile.family_id:
        return profile.family

    family = Family.objects.create(
        name="",
        address=profile.address,
        owner=profile.user,
    )
    profile.family = family
    profile.save(update_fields=["family"])
    return family


def sync_self_member(profile: CareProfile, force_save: bool = False) -> FamilyMember:
    family = ensure_family(profile) if not profile.family_id else profile.family
    member = FamilyMember.objects.filter(family=family, linked_profile=profile).first()
    if not member:
        member = FamilyMember.objects.filter(family=family, phone=profile.phone).first()
    if not member:
        member = FamilyMember(family=family)
    previous_state = None if member.pk is None else {
        "linked_profile_id": member.linked_profile_id,
        "name": member.name,
        "phone": member.phone,
        "address": member.address,
        "avatar_seed": member.avatar_seed,
        "avatar_uri": member.avatar_uri,
        "is_active": member.is_active,
    }

    member.linked_profile = profile
    member.name = profile.full_name
    member.relation = member.relation or "Men"
    member.age = member.age or 31
    member.phone = profile.phone
    member.latitude = member.latitude or Decimal("41.311100")
    member.longitude = member.longitude or Decimal("69.279700")
    member.address = profile.address
    member.place_label = member.place_label or "Mening joylashuvim"
    member.battery_level = member.battery_level or 92
    member.steps = member.steps or 4680
    member.heart_rate = member.heart_rate or 76
    member.last_seen_at = timezone.now()
    member.status = member.status or MemberStatus.SAFE
    member.note = member.note or "Asosiy qurilma faol holatda."
    member.safe_zone = member.safe_zone or "Uy va ish yo'nalishi"
    member.schedule_label = member.schedule_label or "20:00 oilaviy check-in"
    member.avatar_seed = profile.avatar_seed
    member.avatar_uri = profile.avatar_uri
    member.distance_km = member.distance_km or Decimal("3.60")
    member.is_active = True
    should_save = force_save or member.pk is None or previous_state != {
        "linked_profile_id": member.linked_profile_id,
        "name": member.name,
        "phone": member.phone,
        "address": member.address,
        "avatar_seed": member.avatar_seed,
        "avatar_uri": member.avatar_uri,
        "is_active": member.is_active,
    }
    if should_save:
        member.save()
    return member


def refresh_pending_invitations(profile: CareProfile) -> list[FamilyInvitation]:
    invitations = list(
        FamilyInvitation.objects.filter(
            phone=profile.phone,
            status=InvitationStatus.WAITING_INSTALL,
            platform_profile__isnull=True,
        ).exclude(family=profile.family).select_related("family", "invited_by")
    )
    for invitation in invitations:
        invitation.platform_profile = profile
        invitation.is_platform_user = True
        invitation.status = InvitationStatus.PENDING_ACCEPTANCE
        invitation.save(update_fields=["platform_profile", "is_platform_user", "status"])
        create_notification(
            invitation.family,
            title=f"{invitation.name} ilovaga kirdi",
            message="Endi taklifni qabul qiluvchi tomonda tasdiqlash mumkin.",
            category=NotificationCategory.INVITE,
            invitation=invitation,
        )
    return invitations


def incoming_invitations_for_profile(profile: CareProfile):
    return FamilyInvitation.objects.filter(
        platform_profile=profile,
        status__in=[InvitationStatus.PENDING_ACCEPTANCE, InvitationStatus.WAITING_INSTALL],
    ).exclude(family=profile.family).select_related("family", "invited_by")


def deactivate_profile_memberships(profile: CareProfile, keep_family: Family | None = None):
    FamilyMember.objects.filter(linked_profile=profile).exclude(family=keep_family).update(
        linked_profile=None,
        is_active=False,
    )


@transaction.atomic
def register_user(full_name: str, phone: str, challenge: OtpChallenge):
    trimmed_name = (full_name or "").strip()
    normalized_phone = canonical_phone(phone)

    if not trimmed_name:
        raise ValueError("Ismingizni kiriting.")
    if len(normalized_phone) != 12:
        raise ValueError("Telefon raqamni tekshiring.")
    if not challenge.is_verified:
        raise ValueError("Avval SMS kodni tasdiqlang.")

    profile = CareProfile.objects.select_related("user", "family").filter(phone=normalized_phone).first()
    if profile and profile.family_id:
        raise ValueError("Bu profil allaqachon ro'yxatdan o'tgan.")

    if profile:
        user = profile.user
        profile.full_name = trimmed_name
        profile.save(update_fields=["full_name"])
    else:
        user = User.objects.create_user(
            username=normalized_phone,
            password=generate_token_key(),
            first_name=display_name(trimmed_name),
        )
        profile = CareProfile.objects.create(
            user=user,
            full_name=trimmed_name,
            phone=normalized_phone,
            emergency_contact=normalized_phone,
        )

    ensure_family(profile)
    sync_self_member(profile, force_save=True)
    refresh_pending_invitations(profile)
    consume_challenge(challenge)
    return profile, issue_token(user)


@transaction.atomic
def login_user(phone: str, challenge: OtpChallenge):
    normalized_phone = canonical_phone(phone)
    if len(normalized_phone) != 12:
        raise ValueError("Telefon raqamni tekshiring.")
    if not challenge.is_verified:
        raise ValueError("Avval SMS kodni tasdiqlang.")

    profile = CareProfile.objects.select_related("user", "family").filter(phone=normalized_phone).first()
    if not profile:
        raise ValueError("Bu raqam uchun profil topilmadi.")

    ensure_family(profile)
    sync_self_member(profile)
    refresh_pending_invitations(profile)
    consume_challenge(challenge)
    return profile, issue_token(profile.user)


def update_profile(profile: CareProfile, payload: dict) -> CareProfile:
    full_name = clean_text(payload.get("fullName") or payload.get("full_name") or profile.full_name, max_length=120)
    phone = canonical_phone(payload.get("phone", profile.phone))

    if not full_name:
        raise ValueError("Ism va familiyani kiriting.")
    if len(phone) != 12:
        raise ValueError("Telefon raqami to'liq emas.")

    if CareProfile.objects.exclude(pk=profile.pk).filter(phone=phone).exists():
        raise ValueError("Bu telefon boshqa foydalanuvchiga biriktirilgan.")

    previous_phone = profile.phone
    profile.full_name = full_name
    profile.phone = phone
    profile.email = clean_text(payload.get("email") or profile.email, max_length=254)
    profile.address = clean_text(payload.get("address") or profile.address, max_length=255)
    profile.emergency_contact = canonical_phone(payload.get("emergencyContact", profile.emergency_contact))
    profile.avatar_seed = clamp_int(
        payload.get("avatarSeed", profile.avatar_seed),
        min_value=0,
        max_value=MAX_AVATAR_SEED,
        field_name="avatarSeed",
    )
    profile.avatar_uri = clean_text(
        payload.get("avatarUri") or payload.get("avatar_uri") or profile.avatar_uri
    )
    profile.bio = clean_text(payload.get("bio") or profile.bio)

    permissions = payload.get("permissions") or {}
    profile.location_enabled = permissions.get("locationEnabled", profile.location_enabled)
    profile.microphone_enabled = permissions.get("microphoneEnabled", profile.microphone_enabled)
    profile.notifications_enabled = permissions.get("notificationsEnabled", profile.notifications_enabled)
    profile.precise_location_enabled = permissions.get("preciseLocationEnabled", profile.precise_location_enabled)
    profile.background_refresh_enabled = permissions.get(
        "backgroundRefreshEnabled",
        profile.background_refresh_enabled,
    )
    if profile.emergency_contact and len(profile.emergency_contact) != 12:
        raise ValueError("Favqulodda kontakt raqami noto'g'ri.")
    profile.save()

    if previous_phone != phone:
        profile.user.username = phone
        profile.user.first_name = display_name(full_name)
        profile.user.save(update_fields=["username", "first_name"])

    family_label = (payload.get("familyLabel") or payload.get("family_label") or "").strip()
    if profile.family_id:
        profile.family.name = family_label
        profile.family.address = profile.address
        profile.family.save(update_fields=["name", "address"])

    sync_self_member(profile, force_save=True)
    refresh_pending_invitations(profile)
    return profile


@transaction.atomic
def send_invitation(profile: CareProfile, name: str, relation: str, phone: str) -> FamilyInvitation:
    family = ensure_family(profile)
    normalized_phone = canonical_phone(phone)
    platform_profile = CareProfile.objects.filter(phone=normalized_phone).exclude(pk=profile.pk).first()
    resolved_name = clean_text(name, max_length=120) or (
        clean_text(platform_profile.full_name, max_length=120) if platform_profile else ""
    )
    resolved_relation = clean_text(relation, max_length=60)
    if len(normalized_phone) != 12:
        raise ValueError("Telefon raqamni to'liq kiriting.")
    if not resolved_name:
        raise ValueError("Taklif uchun ism kiriting.")
    if not resolved_relation:
        raise ValueError("Qarindoshlikni kiriting.")
    if normalized_phone == profile.phone:
        raise ValueError("O'zingizni qayta taklif qila olmaysiz.")
    if family.members.filter(phone=normalized_phone, is_active=True).exists():
        raise ValueError("Bu raqam allaqachon oilaga ulangan.")
    if family.invitations.filter(phone=normalized_phone).exclude(status=InvitationStatus.ACCEPTED).exists():
        raise ValueError("Bu raqamga taklif allaqachon yuborilgan.")

    is_platform_user = bool(platform_profile)
    status = InvitationStatus.PENDING_ACCEPTANCE if is_platform_user else InvitationStatus.WAITING_INSTALL

    invitation = FamilyInvitation.objects.create(
        family=family,
        invited_by=profile,
        platform_profile=platform_profile,
        name=resolved_name,
        relation=resolved_relation,
        phone=normalized_phone,
        status=status,
        is_platform_user=is_platform_user,
    )

    return invitation


@transaction.atomic
def accept_invitation(invitation: FamilyInvitation, accepted_by: CareProfile | None = None) -> FamilyInvitation:
    if invitation.status == InvitationStatus.ACCEPTED:
        raise ValueError("Bu taklif allaqachon qabul qilingan.")
    if invitation.family_id and accepted_by and accepted_by.family_id == invitation.family_id:
        raise ValueError("Bu foydalanuvchi allaqachon shu oilaga ulangan.")
    candidate = accepted_by or invitation.platform_profile
    if invitation.status == InvitationStatus.WAITING_INSTALL and not candidate:
        raise ValueError("Bu foydalanuvchi hali platformaga kirmagan.")
    if not candidate:
        raise ValueError("Taklifni qabul qiluvchi profil topilmadi.")

    invitation.platform_profile = candidate
    invitation.is_platform_user = True
    if invitation.status == InvitationStatus.WAITING_INSTALL:
        invitation.status = InvitationStatus.PENDING_ACCEPTANCE

    deactivate_profile_memberships(candidate, keep_family=invitation.family)
    candidate.family = invitation.family
    candidate.save()

    member = invitation.family.members.filter(
        Q(phone=invitation.phone) | Q(linked_profile=candidate)
    ).first()
    if not member:
        member = FamilyMember(family=invitation.family)

    member.linked_profile = candidate
    member.name = invitation.name
    member.relation = invitation.relation
    member.age = member.age or 24
    member.phone = invitation.phone
    member.latitude = member.latitude or Decimal("41.342000")
    member.longitude = member.longitude or Decimal("69.286500")
    member.address = candidate.address or member.address or "Joylashuv mavjud emas"
    member.place_label = member.place_label or "Oilaviy xarita"
    member.battery_level = member.battery_level or 76
    member.steps = member.steps or 5022
    member.heart_rate = member.heart_rate or 79
    member.last_seen_at = timezone.now()
    member.status = member.status or MemberStatus.SAFE
    member.note = member.note or "Taklif qabul qilinsa xaritaga ulanadi."
    member.safe_zone = member.safe_zone or "Oilaviy hudud"
    member.schedule_label = member.schedule_label or "20:30 safe-arrival check-in"
    member.avatar_seed = candidate.avatar_seed
    member.avatar_uri = candidate.avatar_uri
    member.distance_km = member.distance_km or Decimal("3.80")
    member.is_active = True
    member.save()

    invitation.status = InvitationStatus.ACCEPTED
    invitation.accepted_at = timezone.now()
    invitation.save(update_fields=["platform_profile", "is_platform_user", "status", "accepted_at"])

    return invitation


@transaction.atomic
def update_member(member: FamilyMember, payload: dict) -> FamilyMember:
    if "name" in payload:
        member.name = clean_text(payload["name"] or member.name, max_length=120)
    if "relation" in payload:
        member.relation = clean_text(payload["relation"] or member.relation, max_length=60)
    if "age" in payload and payload["age"] is not None:
        member.age = clamp_int(payload["age"], min_value=0, max_value=130, field_name="age")
    if "address" in payload:
        member.address = clean_text(payload["address"] or member.address, max_length=255)
    if "placeLabel" in payload or "place_label" in payload:
        member.place_label = clean_text(
            payload.get("placeLabel") or payload.get("place_label") or member.place_label,
            max_length=120,
        )
    if "battery" in payload:
        member.battery_level = clamp_int(payload["battery"], min_value=0, max_value=100, field_name="battery")
    if "steps" in payload:
        member.steps = clamp_int(payload["steps"], min_value=0, max_value=1_000_000, field_name="steps")
    if "heartRate" in payload or "heart_rate" in payload:
        heart_rate = payload.get("heartRate", payload.get("heart_rate"))
        member.heart_rate = clamp_int(heart_rate, min_value=0, max_value=250, field_name="heartRate")
    if "status" in payload:
        if payload["status"] not in MemberStatus.values:
            raise ValueError("status noto'g'ri.")
        member.status = payload["status"]
    if "note" in payload:
        member.note = clean_text(payload["note"] or member.note)
    if "safeZone" in payload or "safe_zone" in payload:
        member.safe_zone = clean_text(
            payload.get("safeZone") or payload.get("safe_zone") or member.safe_zone,
            max_length=120,
        )
    if "schedule" in payload:
        member.schedule_label = clean_text(payload["schedule"] or member.schedule_label, max_length=120)
    if "avatarSeed" in payload or "avatar_seed" in payload:
        avatar_seed = payload.get("avatarSeed", payload.get("avatar_seed"))
        member.avatar_seed = clamp_int(avatar_seed, min_value=0, max_value=MAX_AVATAR_SEED, field_name="avatarSeed")
    if "avatarUri" in payload or "avatar_uri" in payload:
        member.avatar_uri = clean_text(payload.get("avatarUri") or payload.get("avatar_uri") or member.avatar_uri)
    member.save()
    return member


@transaction.atomic
def record_location(member: FamilyMember, payload: dict) -> FamilyMember:
    lat = payload.get("lat", payload.get("latitude"))
    lon = payload.get("lon", payload.get("longitude"))
    if lat is None or lon is None:
        raise ValueError("lat va lon qiymatlari kerak.")

    member.latitude = parse_lat_lon(lat, field_name="lat", min_value="-90", max_value="90")
    member.longitude = parse_lat_lon(lon, field_name="lon", min_value="-180", max_value="180")
    member.address = clean_text(payload.get("address") or member.address, max_length=255)
    member.place_label = clean_text(
        payload.get("placeLabel") or payload.get("place_label") or member.place_label,
        max_length=120,
    )
    member.battery_level = clamp_int(
        payload.get("battery", member.battery_level),
        min_value=0,
        max_value=100,
        field_name="battery",
    )
    member.steps = clamp_int(
        payload.get("steps", member.steps),
        min_value=0,
        max_value=1_000_000,
        field_name="steps",
    )
    member.heart_rate = clamp_int(
        payload.get("heartRate", payload.get("heart_rate", member.heart_rate)),
        min_value=0,
        max_value=250,
        field_name="heartRate",
    )
    member.distance_km = parse_decimal(
        payload.get("distanceKm", payload.get("distance_km", member.distance_km)),
        field_name="distanceKm",
    )
    if member.distance_km < 0 or member.distance_km > MAX_DISTANCE_KM:
        raise ValueError("distanceKm noto'g'ri.")
    member.last_seen_at = timezone.now()
    member.save()

    LocationPing.objects.create(
        member=member,
        latitude=member.latitude,
        longitude=member.longitude,
        address=member.address,
        place_label=member.place_label,
        battery_level=member.battery_level,
        steps=member.steps,
        heart_rate=member.heart_rate,
        distance_km=member.distance_km,
    )
    create_activity(
        member.family,
        title=f"{member.name} joylashuvi yangilandi",
        subtitle=f"{member.place_label or 'Yangi nuqta'} bo'yicha koordinata qabul qilindi.",
        severity=FeedSeverity.POSITIVE,
        member=member,
    )
    return member


def member_location_history(member: FamilyMember, limit: int = 100):
    safe_limit = max(1, min(int(limit or 100), 500))
    return list(
        member.location_pings.order_by("-created_at", "-id")[:safe_limit]
    )


def sos_routes_for_profile(profile: CareProfile) -> list[PlannedRoute]:
    family = ensure_family(profile)
    self_member = sync_self_member(profile)
    routes: list[PlannedRoute] = []
    active_alerts = (
        family.sos_alerts.filter(status=SosAlertStatus.ACTIVE)
        .select_related("triggered_by")
        .order_by("-triggered_at", "-id")
    )

    for alert in active_alerts:
        target = alert.triggered_by
        if not target:
            continue

        routes.append(
            plan_route_for_alert(
                alert_id=alert.id,
                source_member_id=self_member.id,
                source_lat=float(self_member.latitude),
                source_lon=float(self_member.longitude),
                target_member_id=target.id,
                target_name=target.name,
                relation=target.relation,
                phone=target.phone,
                target_lat=float(target.latitude),
                target_lon=float(target.longitude),
            )
        )

    return routes


@transaction.atomic
def trigger_sos(profile: CareProfile) -> SosAlert:
    family = ensure_family(profile)
    self_member = sync_self_member(profile)
    existing_alert = family.sos_alerts.filter(status=SosAlertStatus.ACTIVE, triggered_by=self_member).first()
    if existing_alert:
        return existing_alert

    summary = "Barcha oila a'zolariga SOS signal yuborildi."
    alert = SosAlert.objects.create(
        family=family,
        triggered_by=self_member,
        summary=summary,
        address=self_member.address,
        status=SosAlertStatus.ACTIVE,
    )

    deliveries = []
    for member in family.members.filter(is_active=True).exclude(pk=self_member.pk):
        deliveries.append(
            SosDelivery(
                alert=alert,
                member=member,
                state=SosContactState.NOTIFIED,
                delivered_at=timezone.now(),
            )
        )
    if deliveries:
        SosDelivery.objects.bulk_create(deliveries)

    create_notification(
        family,
        title="SOS markazi yakunlandi",
        message=f"{len(deliveries)} ta oila a'zosiga favqulodda signal yuborildi.",
        category=NotificationCategory.SAFETY,
    )
    create_activity(
        family,
        title="SOS signal yuborildi",
        subtitle=f"{self_member.name} tomonidan favqulodda signal ishga tushirildi.",
        severity=FeedSeverity.WARNING,
        member=self_member,
    )
    return alert


@transaction.atomic
def resolve_sos(alert: SosAlert) -> SosAlert:
    alert.status = SosAlertStatus.RESOLVED
    alert.resolved_at = timezone.now()
    alert.save()
    create_activity(
        alert.family,
        title="SOS yopildi",
        subtitle="Favqulodda holat yakunlandi.",
        severity=FeedSeverity.POSITIVE,
        member=alert.triggered_by,
    )
    return alert


def mark_notification_read(notification: AppNotification):
    if not notification.is_read:
        notification.is_read = True
        notification.save(update_fields=["is_read"])


def mark_all_notifications_read(family: Family):
    family.notifications.filter(is_read=False).update(is_read=True)


def dismiss_notification(notification: AppNotification):
    notification.delete()


def remove_invitation(invitation: FamilyInvitation):
    invitation.notifications.all().delete()
    invitation.delete()


@transaction.atomic
def dispatch_broadcast(broadcast: BroadcastMessage) -> BroadcastMessage:
    families = list(Family.objects.all())
    AppNotification.objects.bulk_create(
        [
            AppNotification(
                family=family,
                title=broadcast.title,
                message=broadcast.message,
                category=NotificationCategory.SYSTEM,
            )
            for family in families
        ]
    )
    ActivityFeedItem.objects.bulk_create(
        [
            ActivityFeedItem(
                family=family,
                title="Admin xabari yuborildi",
                subtitle=broadcast.title,
                severity=FeedSeverity.NEUTRAL,
            )
            for family in families
        ]
    )
    broadcast.delivered_families = len(families)
    broadcast.save(update_fields=["delivered_families"])
    return broadcast
