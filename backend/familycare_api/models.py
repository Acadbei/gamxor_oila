from datetime import time
import uuid

from django.contrib.auth.models import User
from django.db import models
from django.utils import timezone


class MemberStatus(models.TextChoices):
    SAFE = "SAFE", "Safe"
    MOVING = "MOVING", "Moving"
    NEEDS_ATTENTION = "NEEDS_ATTENTION", "Needs attention"


class FeedSeverity(models.TextChoices):
    POSITIVE = "POSITIVE", "Positive"
    NEUTRAL = "NEUTRAL", "Neutral"
    WARNING = "WARNING", "Warning"


class InvitationStatus(models.TextChoices):
    PENDING_ACCEPTANCE = "PENDING_ACCEPTANCE", "Pending acceptance"
    WAITING_INSTALL = "WAITING_INSTALL", "Waiting install"
    ACCEPTED = "ACCEPTED", "Accepted"
    DECLINED = "DECLINED", "Declined"


class NotificationCategory(models.TextChoices):
    INVITE = "INVITE", "Invite"
    SAFETY = "SAFETY", "Safety"
    CRIME = "CRIME", "Crime"
    SYSTEM = "SYSTEM", "System"


class SosContactState(models.TextChoices):
    CALLING = "CALLING", "Calling"
    NOTIFIED = "NOTIFIED", "Notified"


class SosAlertStatus(models.TextChoices):
    ACTIVE = "ACTIVE", "Active"
    RESOLVED = "RESOLVED", "Resolved"


class Family(models.Model):
    name = models.CharField(max_length=120)
    address = models.CharField(max_length=255, blank=True)
    owner = models.ForeignKey(
        User,
        on_delete=models.PROTECT,
        related_name="owned_families",
    )
    next_check_in_at = models.TimeField(default=time(20, 0))
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["name", "id"]

    def __str__(self) -> str:
        return self.name


class CareProfile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE, related_name="care_profile")
    family = models.ForeignKey(
        Family,
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name="profiles",
    )
    full_name = models.CharField(max_length=120)
    phone = models.CharField(max_length=20, unique=True)
    email = models.EmailField(blank=True)
    address = models.CharField(max_length=255, blank=True)
    emergency_contact = models.CharField(max_length=20, blank=True)
    avatar_seed = models.PositiveSmallIntegerField(default=0)
    avatar_uri = models.TextField(blank=True)
    bio = models.TextField(blank=True)
    location_enabled = models.BooleanField(default=True)
    microphone_enabled = models.BooleanField(default=False)
    notifications_enabled = models.BooleanField(default=True)
    precise_location_enabled = models.BooleanField(default=True)
    background_refresh_enabled = models.BooleanField(default=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["full_name", "id"]

    def __str__(self) -> str:
        return f"{self.full_name} ({self.phone})"


class TrustedPlace(models.Model):
    family = models.ForeignKey(Family, on_delete=models.CASCADE, related_name="trusted_places")
    name = models.CharField(max_length=120)
    address = models.CharField(max_length=255, blank=True)
    latitude = models.DecimalField(max_digits=9, decimal_places=6)
    longitude = models.DecimalField(max_digits=9, decimal_places=6)
    radius_meters = models.PositiveIntegerField(default=200)
    kind = models.CharField(max_length=40, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["name", "id"]

    def __str__(self) -> str:
        return f"{self.family.name}: {self.name}"


class FamilyMember(models.Model):
    family = models.ForeignKey(Family, on_delete=models.CASCADE, related_name="members")
    linked_profile = models.ForeignKey(
        CareProfile,
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name="linked_members",
    )
    name = models.CharField(max_length=120)
    relation = models.CharField(max_length=60)
    age = models.PositiveIntegerField(default=0)
    phone = models.CharField(max_length=20)
    latitude = models.DecimalField(max_digits=9, decimal_places=6, default=0)
    longitude = models.DecimalField(max_digits=9, decimal_places=6, default=0)
    address = models.CharField(max_length=255, blank=True)
    place_label = models.CharField(max_length=120, blank=True)
    battery_level = models.PositiveSmallIntegerField(default=100)
    steps = models.PositiveIntegerField(default=0)
    heart_rate = models.PositiveSmallIntegerField(default=0)
    last_seen_at = models.DateTimeField(default=timezone.now)
    status = models.CharField(max_length=24, choices=MemberStatus.choices, default=MemberStatus.SAFE)
    note = models.TextField(blank=True)
    safe_zone = models.CharField(max_length=120, blank=True)
    schedule_label = models.CharField(max_length=120, blank=True)
    avatar_seed = models.PositiveSmallIntegerField(default=0)
    avatar_uri = models.TextField(blank=True)
    distance_km = models.DecimalField(max_digits=6, decimal_places=2, default=0)
    is_active = models.BooleanField(default=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ["id"]
        constraints = [
            models.UniqueConstraint(fields=["family", "phone"], name="family_member_phone_unique"),
            models.UniqueConstraint(fields=["family", "linked_profile"], name="family_linked_profile_unique"),
        ]

    def __str__(self) -> str:
        return f"{self.name} - {self.family.name}"


class LocationPing(models.Model):
    member = models.ForeignKey(FamilyMember, on_delete=models.CASCADE, related_name="location_pings")
    latitude = models.DecimalField(max_digits=9, decimal_places=6)
    longitude = models.DecimalField(max_digits=9, decimal_places=6)
    address = models.CharField(max_length=255, blank=True)
    place_label = models.CharField(max_length=120, blank=True)
    battery_level = models.PositiveSmallIntegerField(default=100)
    steps = models.PositiveIntegerField(default=0)
    heart_rate = models.PositiveSmallIntegerField(default=0)
    distance_km = models.DecimalField(max_digits=6, decimal_places=2, default=0)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-created_at", "-id"]

    def __str__(self) -> str:
        return f"{self.member.name} @ {self.created_at:%Y-%m-%d %H:%M}"


class ActivityFeedItem(models.Model):
    family = models.ForeignKey(Family, on_delete=models.CASCADE, related_name="activity_items")
    member = models.ForeignKey(
        FamilyMember,
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name="activity_items",
    )
    title = models.CharField(max_length=160)
    subtitle = models.CharField(max_length=255)
    severity = models.CharField(max_length=12, choices=FeedSeverity.choices, default=FeedSeverity.NEUTRAL)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-created_at", "-id"]

    def __str__(self) -> str:
        return self.title


class FamilyInvitation(models.Model):
    family = models.ForeignKey(Family, on_delete=models.CASCADE, related_name="invitations")
    invited_by = models.ForeignKey(
        CareProfile,
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name="sent_invitations",
    )
    platform_profile = models.ForeignKey(
        CareProfile,
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name="received_invitations",
    )
    name = models.CharField(max_length=120)
    relation = models.CharField(max_length=60)
    phone = models.CharField(max_length=20)
    status = models.CharField(
        max_length=24,
        choices=InvitationStatus.choices,
        default=InvitationStatus.WAITING_INSTALL,
    )
    is_platform_user = models.BooleanField(default=False)
    sent_at = models.DateTimeField(auto_now_add=True)
    accepted_at = models.DateTimeField(null=True, blank=True)

    class Meta:
        ordering = ["-sent_at", "-id"]

    def __str__(self) -> str:
        return f"{self.name} - {self.phone}"


class AppNotification(models.Model):
    family = models.ForeignKey(Family, on_delete=models.CASCADE, related_name="notifications")
    invitation = models.ForeignKey(
        FamilyInvitation,
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name="notifications",
    )
    title = models.CharField(max_length=160)
    message = models.CharField(max_length=400)
    category = models.CharField(max_length=12, choices=NotificationCategory.choices)
    is_read = models.BooleanField(default=False)
    action_label = models.CharField(max_length=40, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-created_at", "-id"]

    def __str__(self) -> str:
        return self.title


class BroadcastMessage(models.Model):
    title = models.CharField(max_length=160)
    message = models.CharField(max_length=500)
    created_by = models.ForeignKey(
        User,
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name="broadcast_messages",
    )
    delivered_families = models.PositiveIntegerField(default=0)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-created_at", "-id"]

    def __str__(self) -> str:
        return self.title


class SosAlert(models.Model):
    family = models.ForeignKey(Family, on_delete=models.CASCADE, related_name="sos_alerts")
    triggered_by = models.ForeignKey(
        FamilyMember,
        null=True,
        blank=True,
        on_delete=models.SET_NULL,
        related_name="triggered_alerts",
    )
    summary = models.CharField(max_length=255)
    address = models.CharField(max_length=255, blank=True)
    status = models.CharField(max_length=12, choices=SosAlertStatus.choices, default=SosAlertStatus.ACTIVE)
    triggered_at = models.DateTimeField(auto_now_add=True)
    resolved_at = models.DateTimeField(null=True, blank=True)

    class Meta:
        ordering = ["-triggered_at", "-id"]

    def __str__(self) -> str:
        return f"SOS {self.family.name} #{self.pk}"


class SosDelivery(models.Model):
    alert = models.ForeignKey(SosAlert, on_delete=models.CASCADE, related_name="deliveries")
    member = models.ForeignKey(FamilyMember, on_delete=models.CASCADE, related_name="sos_deliveries")
    state = models.CharField(max_length=12, choices=SosContactState.choices, default=SosContactState.CALLING)
    delivered_at = models.DateTimeField(null=True, blank=True)

    class Meta:
        ordering = ["member_id"]
        constraints = [
            models.UniqueConstraint(fields=["alert", "member"], name="sos_delivery_unique_member"),
        ]

    def __str__(self) -> str:
        return f"{self.alert_id} -> {self.member.name}"


class OtpChallenge(models.Model):
    challenge_id = models.UUIDField(default=uuid.uuid4, editable=False, unique=True)
    phone = models.CharField(max_length=20)
    code = models.CharField(max_length=6)
    expires_at = models.DateTimeField()
    is_verified = models.BooleanField(default=False)
    verified_at = models.DateTimeField(null=True, blank=True)
    consumed_at = models.DateTimeField(null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-created_at", "-id"]

    def __str__(self) -> str:
        return f"{self.phone} ({self.challenge_id})"


class AuthToken(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name="auth_tokens")
    key = models.CharField(max_length=64, unique=True, default="", blank=True)
    expires_at = models.DateTimeField()
    last_used_at = models.DateTimeField(null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-created_at", "-id"]

    def __str__(self) -> str:
        return f"Token for {self.user.username}"
