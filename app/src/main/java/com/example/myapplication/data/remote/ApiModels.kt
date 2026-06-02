package com.example.myapplication.data.remote

import com.example.myapplication.data.model.ActivityFeedItem
import com.example.myapplication.data.model.AppNotification
import com.example.myapplication.data.model.CaregiverProfile
import com.example.myapplication.data.model.FamilyInvitation
import com.example.myapplication.data.model.FamilyMember
import com.example.myapplication.data.model.LocationHistoryPoint
import com.example.myapplication.data.model.SosAlert
import com.example.myapplication.data.model.SosUiState

data class RequestCodeRequest(
    val phone: String = ""
)

data class RequestCodeResponse(
    val challengeId: String = "",
    val isRegistered: Boolean = false,
    val expiresAt: String = "",
    val otpHint: String? = null
)

data class VerifyCodeRequest(
    val phone: String = "",
    val challengeId: String = "",
    val code: String = ""
)

data class VerifyCodeResponse(
    val verified: Boolean = false,
    val challengeId: String = "",
    val verifiedAt: String? = null
)

data class RegisterRequest(
    val phone: String = "",
    val fullName: String = "",
    val challengeId: String = ""
)

data class LoginRequest(
    val phone: String = "",
    val challengeId: String = ""
)

data class InvitationRequest(
    val name: String = "",
    val relation: String = "",
    val phone: String = ""
)

data class UpdateLocationRequest(
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val address: String = "",
    val placeLabel: String = ""
)

data class EmptyRequest(
    val placeholder: String? = null
)

data class ApiOperationResponse(
    val success: Boolean = false,
    val detail: String? = null
)

data class AuthResponse(
    val token: String = "",
    val expiresAt: String = "",
    val profile: CaregiverProfile = CaregiverProfile(),
    val dashboard: DashboardDto = DashboardDto()
)

data class DashboardHeaderDto(
    val caregiverName: String = "",
    val familyLabel: String = "",
    val lastSyncLabel: String = "",
    val nextCheckInLabel: String = "",
    val trustedPlacesCount: Int = 0
)

data class DashboardCollectionsDto(
    val members: List<FamilyMember> = emptyList(),
    val invitations: List<FamilyInvitation> = emptyList(),
    val incomingInvitations: List<FamilyInvitation> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val activityFeed: List<ActivityFeedItem> = emptyList(),
    val activeSosAlerts: List<SosAlert> = emptyList()
)

data class DashboardDto(
    val isRegistered: Boolean = false,
    val caregiverName: String = "",
    val familyLabel: String = "",
    val lastSyncLabel: String = "",
    val nextCheckInLabel: String = "",
    val trustedPlacesCount: Int = 0,
    val profile: CaregiverProfile = CaregiverProfile(),
    val selfMember: FamilyMember? = null,
    val members: List<FamilyMember> = emptyList(),
    val invitations: List<FamilyInvitation> = emptyList(),
    val incomingInvitations: List<FamilyInvitation> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val activityFeed: List<ActivityFeedItem> = emptyList(),
    val selectedMemberId: Int? = null,
    val activeSosAlerts: List<SosAlert> = emptyList(),
    val sosState: SosUiState = SosUiState()
) {
    val header: DashboardHeaderDto
        get() = DashboardHeaderDto(
            caregiverName = caregiverName,
            familyLabel = familyLabel,
            lastSyncLabel = lastSyncLabel,
            nextCheckInLabel = nextCheckInLabel,
            trustedPlacesCount = trustedPlacesCount
        )

    val collections: DashboardCollectionsDto
        get() = DashboardCollectionsDto(
            members = members,
            invitations = invitations,
            incomingInvitations = incomingInvitations,
            notifications = notifications,
            activityFeed = activityFeed,
            activeSosAlerts = activeSosAlerts
        )
}

data class HealthResponse(
    val ok: Boolean = false,
    val service: String = ""
)

data class NotificationListResponse(
    val notifications: List<AppNotification> = emptyList()
)

data class InvitationListResponse(
    val invitations: List<FamilyInvitation> = emptyList()
)

data class ActivityFeedResponse(
    val activityFeed: List<ActivityFeedItem> = emptyList()
)

data class SosAlertListResponse(
    val alerts: List<SosAlert> = emptyList()
)

data class MemberListResponse(
    val selfMember: FamilyMember? = null,
    val members: List<FamilyMember> = emptyList()
)

data class LocationHistoryResponse(
    val memberId: Int = 0,
    val history: List<LocationHistoryPoint> = emptyList()
)

data class RoutePointDto(
    val lat: Double = 0.0,
    val lon: Double = 0.0
)

data class SosRouteDto(
    val alertId: Int = 0,
    val memberId: Int = 0,
    val memberName: String = "",
    val relation: String = "",
    val phone: String = "",
    val sourceMemberId: Int = 0,
    val sourceLat: Double = 0.0,
    val sourceLon: Double = 0.0,
    val targetLat: Double = 0.0,
    val targetLon: Double = 0.0,
    val dijkstraPoints: List<RoutePointDto> = emptyList(),
    val astarPoints: List<RoutePointDto> = emptyList(),
    val dijkstraLengthMeters: Double = 0.0,
    val astarLengthMeters: Double = 0.0,
    val isSamePath: Boolean = false,
    val graphNodes: Int = 0,
    val graphEdges: Int = 0
)

data class SosRoutesResponse(
    val sourceMemberId: Int = 0,
    val routes: List<SosRouteDto> = emptyList()
)
