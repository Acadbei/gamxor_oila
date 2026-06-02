from django.contrib import admin

from .models import (
    ActivityFeedItem,
    AppNotification,
    AuthToken,
    BroadcastMessage,
    CareProfile,
    Family,
    FamilyInvitation,
    FamilyMember,
    LocationPing,
    OtpChallenge,
    SosAlert,
    SosDelivery,
    TrustedPlace,
)
from .services import dispatch_broadcast


@admin.register(Family)
class FamilyAdmin(admin.ModelAdmin):
    list_display = ("id", "name", "owner", "address", "next_check_in_at")
    search_fields = ("name", "owner__username")


@admin.register(CareProfile)
class CareProfileAdmin(admin.ModelAdmin):
    list_display = ("id", "full_name", "phone", "family", "address", "updated_at")
    search_fields = ("full_name", "phone", "user__username", "address")
    list_filter = ("family",)


@admin.register(FamilyMember)
class FamilyMemberAdmin(admin.ModelAdmin):
    list_display = (
        "id",
        "name",
        "phone",
        "family",
        "relation",
        "status",
        "battery_level",
        "place_label",
        "last_seen_at",
    )
    search_fields = ("name", "phone", "family__name", "address", "place_label")
    list_filter = ("status", "family")


@admin.register(FamilyInvitation)
class FamilyInvitationAdmin(admin.ModelAdmin):
    list_display = ("id", "name", "family", "phone", "status", "is_platform_user", "sent_at")
    search_fields = ("name", "phone", "family__name")
    list_filter = ("status", "is_platform_user")


@admin.register(AppNotification)
class AppNotificationAdmin(admin.ModelAdmin):
    list_display = ("id", "title", "family", "category", "is_read", "created_at")
    search_fields = ("title", "message", "family__name")
    list_filter = ("category", "is_read")


@admin.register(BroadcastMessage)
class BroadcastMessageAdmin(admin.ModelAdmin):
    list_display = ("id", "title", "created_by", "delivered_families", "created_at")
    search_fields = ("title", "message")
    readonly_fields = ("delivered_families", "created_at")
    fields = ("title", "message", "delivered_families", "created_at")

    def save_model(self, request, obj, form, change):
        is_new = obj.pk is None
        obj.created_by = request.user
        super().save_model(request, obj, form, change)
        if is_new:
            dispatch_broadcast(obj)


@admin.register(ActivityFeedItem)
class ActivityFeedItemAdmin(admin.ModelAdmin):
    list_display = ("id", "title", "family", "severity", "created_at")
    search_fields = ("title", "subtitle", "family__name")
    list_filter = ("severity",)


@admin.register(SosAlert)
class SosAlertAdmin(admin.ModelAdmin):
    list_display = ("id", "family", "triggered_by", "status", "triggered_at", "resolved_at")
    list_filter = ("status",)


@admin.register(SosDelivery)
class SosDeliveryAdmin(admin.ModelAdmin):
    list_display = ("id", "alert", "member", "state", "delivered_at")
    list_filter = ("state",)


@admin.register(LocationPing)
class LocationPingAdmin(admin.ModelAdmin):
    list_display = ("id", "member", "latitude", "longitude", "created_at")
    list_filter = ("member__family",)


@admin.register(TrustedPlace)
class TrustedPlaceAdmin(admin.ModelAdmin):
    list_display = ("id", "family", "name", "kind", "radius_meters")
    list_filter = ("kind",)


@admin.register(OtpChallenge)
class OtpChallengeAdmin(admin.ModelAdmin):
    list_display = ("id", "phone", "challenge_id", "is_verified", "expires_at", "consumed_at")
    search_fields = ("phone",)


@admin.register(AuthToken)
class AuthTokenAdmin(admin.ModelAdmin):
    list_display = ("id", "user", "expires_at", "last_used_at", "created_at")
    search_fields = ("user__username", "key")
