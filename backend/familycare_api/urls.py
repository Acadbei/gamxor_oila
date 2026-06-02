from django.urls import path

from . import views


urlpatterns = [
    path("health/", views.healthcheck, name="healthcheck"),
    path("auth/request-code/", views.request_code_view, name="request-code"),
    path("auth/verify-code/", views.verify_code_view, name="verify-code"),
    path("auth/register/", views.register_view, name="register"),
    path("auth/login/", views.login_view, name="login"),
    path("auth/logout/", views.logout_view, name="logout"),
    path("dashboard/", views.dashboard_view, name="dashboard"),
    path("me/profile/", views.profile_view, name="profile"),
    path("members/", views.members_view, name="members"),
    path("members/<int:member_id>/", views.member_detail_view, name="member-detail"),
    path("members/<int:member_id>/location/", views.member_location_view, name="member-location"),
    path("members/<int:member_id>/location-history/", views.member_location_history_view, name="member-location-history"),
    path("invitations/", views.invitations_view, name="invitations"),
    path("invitations/<int:invitation_id>/accept/", views.invitation_accept_view, name="invitation-accept"),
    path("invitations/<int:invitation_id>/", views.invitation_delete_view, name="invitation-delete"),
    path("notifications/", views.notifications_view, name="notifications"),
    path("notifications/read-all/", views.notifications_read_all_view, name="notifications-read-all"),
    path("notifications/<int:notification_id>/read/", views.notification_read_view, name="notification-read"),
    path("notifications/<int:notification_id>/", views.notification_delete_view, name="notification-delete"),
    path("activity-feed/", views.activity_feed_view, name="activity-feed"),
    path("sos/", views.sos_view, name="sos"),
    path("sos/routes/", views.sos_routes_view, name="sos-routes"),
    path("sos/trigger/", views.sos_trigger_view, name="sos-trigger"),
    path("sos/<int:alert_id>/resolve/", views.sos_resolve_view, name="sos-resolve"),
]
