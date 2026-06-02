package com.example.myapplication.data.model

import com.example.myapplication.tracking.LatLng

enum class MemberStatus {
    SAFE,
    MOVING,
    NEEDS_ATTENTION
}

enum class FeedSeverity {
    POSITIVE,
    NEUTRAL,
    WARNING
}

enum class InvitationStatus {
    PENDING_ACCEPTANCE,
    WAITING_INSTALL,
    ACCEPTED,
    DECLINED
}

enum class NotificationCategory {
    INVITE,
    SAFETY,
    CRIME,
    SYSTEM
}

enum class SosContactState {
    CALLING,
    NOTIFIED
}

enum class BackendStatus {
    CHECKING,
    ONLINE,
    OFFLINE
}

object DemoModelDefaults {
    const val defaultOtpHint = "2580"
    const val defaultSyncLabel = "Hozirgina"
    const val defaultPlaceLabel = "Mening joylashuvim"
    const val defaultRelationSelf = "Men"
    const val defaultPresenceOnline = "Online"
    const val defaultPresenceOffline = "Offline"
    const val defaultCaregiverName = "Profil"
    const val defaultSafeZone = "Uy va ish yo'nalishi"
    const val defaultDeviceNote = "Asosiy qurilma faol holatda."
    const val defaultSchedule = "20:00 oilaviy check-in"
    const val defaultCheckInLabel = "20:00 da umumiy check-in"
    const val fallbackLatitude = 41.3111
    const val fallbackLongitude = 69.2797
}

data class ProfilePermissions(
    val locationEnabled: Boolean = true,
    val microphoneEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val preciseLocationEnabled: Boolean = true,
    val backgroundRefreshEnabled: Boolean = true
)

data class CaregiverProfile(
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val familyLabel: String = "",
    val address: String = "",
    val emergencyContact: String = "",
    val avatarSeed: Int = 0,
    val avatarUri: String = "",
    val bio: String = "",
    val permissions: ProfilePermissions = ProfilePermissions()
)

data class BackendConnection(
    val status: BackendStatus = BackendStatus.CHECKING,
    val label: String = "Tekshirilmoqda",
    val detail: String = "Server holati aniqlanmoqda.",
    val baseUrl: String = "",
    val checkedAtLabel: String = ""
)

data class FamilyMember(
    val id: Int,
    val name: String,
    val relation: String,
    val age: Int,
    val lat: Double,
    val lon: Double,
    val address: String,
    val placeLabel: String,
    val battery: Int,
    val steps: Int,
    val heartRate: Int,
    val lastUpdate: String,
    val status: MemberStatus,
    val note: String,
    val safeZone: String,
    val phone: String,
    val schedule: String,
    val avatarSeed: Int = 0,
    val avatarUri: String = "",
    val distanceKm: Double = 0.0,
    val isCurrentUser: Boolean = false,
    val isOnline: Boolean = false,
    val presenceLabel: String = "Offline",
    val lastSeenAt: String? = null
)

data class LocationHistoryPoint(
    val id: Int,
    val memberId: Int,
    val lat: Double,
    val lon: Double,
    val address: String,
    val placeLabel: String,
    val battery: Int,
    val steps: Int,
    val heartRate: Int,
    val distanceKm: Double,
    val createdAt: String? = null,
    val timeLabel: String = ""
)

data class SosRouteTrace(
    val alertId: Int,
    val memberId: Int,
    val memberName: String,
    val relation: String,
    val phone: String,
    val sourceMemberId: Int,
    val sourceLat: Double,
    val sourceLon: Double,
    val targetLat: Double,
    val targetLon: Double,
    val dijkstraRoute: List<LatLng> = emptyList(),
    val astarRoute: List<LatLng> = emptyList(),
    val dijkstraLengthMeters: Double = 0.0,
    val astarLengthMeters: Double = 0.0,
    val isSamePath: Boolean = false,
    val graphNodes: Int = 0,
    val graphEdges: Int = 0
)

data class ActivityFeedItem(
    val id: Int,
    val memberId: Int?,
    val title: String,
    val subtitle: String,
    val timeLabel: String,
    val severity: FeedSeverity
)

data class FamilyInvitation(
    val id: Int,
    val name: String,
    val relation: String,
    val phone: String,
    val sentAtLabel: String,
    val status: InvitationStatus,
    val isPlatformUser: Boolean,
    val familyName: String = "",
    val invitedByName: String = "",
    val isIncoming: Boolean = false
)

data class AppNotification(
    val id: Int,
    val title: String,
    val message: String,
    val timeLabel: String,
    val category: NotificationCategory,
    val isRead: Boolean = false,
    val inviteId: Int? = null,
    val actionLabel: String? = null
)

data class SosContact(
    val memberId: Int,
    val name: String,
    val phone: String,
    val state: SosContactState
)

data class SosAlert(
    val memberId: Int,
    val name: String,
    val relation: String,
    val phone: String,
    val address: String,
    val lastUpdate: String
)

data class SosUiState(
    val alertId: Int? = null,
    val isActive: Boolean = false,
    val isSending: Boolean = false,
    val summary: String = "",
    val sentAtLabel: String = "",
    val contacts: List<SosContact> = emptyList()
)

data class DemoActionResult(
    val success: Boolean,
    val message: String
)

data class DemoUiState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false,
    val isRegistered: Boolean = false,
    val hasExistingAccount: Boolean = false,
    val isSendingCode: Boolean = false,
    val isVerifying: Boolean = false,
    val isCodeVerified: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSendingInvitation: Boolean = false,
    val otpRequested: Boolean = false,
    val otpHint: String = DemoModelDefaults.defaultOtpHint,
    val loginError: String? = null,
    val caregiverName: String = "",
    val familyLabel: String = "",
    val lastSyncLabel: String = DemoModelDefaults.defaultSyncLabel,
    val profile: CaregiverProfile = CaregiverProfile(),
    val selfMember: FamilyMember = defaultSelfMember(),
    val members: List<FamilyMember> = emptyList(),
    val invitations: List<FamilyInvitation> = emptyList(),
    val incomingInvitations: List<FamilyInvitation> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val activityFeed: List<ActivityFeedItem> = emptyList(),
    val selectedMemberId: Int? = null,
    val isHistoryVisible: Boolean = false,
    val isHistoryLoading: Boolean = false,
    val historyMemberId: Int? = null,
    val selectedMemberHistory: List<LocationHistoryPoint> = emptyList(),
    val liveRoute: List<LatLng> = emptyList(),
    val storedRoutes: List<List<LatLng>> = emptyList(),
    val isLiveTracking: Boolean = false,
    val isSosRouteVisible: Boolean = false,
    val isSosRouteLoading: Boolean = false,
    val sosRoutes: List<SosRouteTrace> = emptyList(),
    val nextCheckInLabel: String = DemoModelDefaults.defaultCheckInLabel,
    val trustedPlacesCount: Int = 3,
    val activeSosAlerts: List<SosAlert> = emptyList(),
    val sosState: SosUiState = SosUiState(),
    val backendConnection: BackendConnection = BackendConnection()
)

private fun defaultSelfMember() = FamilyMember(
    id = 0,
    name = "",
    relation = DemoModelDefaults.defaultRelationSelf,
    age = 31,
    lat = DemoModelDefaults.fallbackLatitude,
    lon = DemoModelDefaults.fallbackLongitude,
    address = "",
    placeLabel = DemoModelDefaults.defaultPlaceLabel,
    battery = 92,
    steps = 4680,
    heartRate = 76,
    lastUpdate = DemoModelDefaults.defaultSyncLabel,
    status = MemberStatus.SAFE,
    note = DemoModelDefaults.defaultDeviceNote,
    safeZone = DemoModelDefaults.defaultSafeZone,
    phone = "",
    schedule = DemoModelDefaults.defaultSchedule,
    avatarSeed = 0,
    avatarUri = "",
    distanceKm = 3.6,
    isCurrentUser = true,
    isOnline = true,
    presenceLabel = DemoModelDefaults.defaultPresenceOnline,
    lastSeenAt = null
)
