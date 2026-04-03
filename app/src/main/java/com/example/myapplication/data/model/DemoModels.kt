package com.example.myapplication.data.model

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
    ACCEPTED
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

data class ProfilePermissions(
    val locationEnabled: Boolean = true,
    val microphoneEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val preciseLocationEnabled: Boolean = true,
    val backgroundRefreshEnabled: Boolean = true
)

data class CaregiverProfile(
    val fullName: String = "Nodir Yusupov",
    val phone: String = "+998 90 321 45 67",
    val email: String = "nodir@familycare.demo",
    val familyLabel: String = "Yusupovlar oilasi",
    val address: String = "Yunusobod, 9-kvartal",
    val emergencyContact: String = "+998 90 777 00 11",
    val avatarSeed: Int = 0,
    val avatarUri: String = "",
    val bio: String = "Oilaviy monitoring, xavfsizlik va SOS boshqaruvi uchun mas'ulman.",
    val permissions: ProfilePermissions = ProfilePermissions()
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
    val isCurrentUser: Boolean = false
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
    val isPlatformUser: Boolean
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
    val isSendingCode: Boolean = false,
    val isVerifying: Boolean = false,
    val isRefreshing: Boolean = false,
    val otpRequested: Boolean = false,
    val otpHint: String = "2580",
    val loginError: String? = null,
    val caregiverName: String = "Nodir",
    val familyLabel: String = "Yusupovlar oilasi",
    val lastSyncLabel: String = "Hozirgina",
    val profile: CaregiverProfile = CaregiverProfile(),
    val selfMember: FamilyMember = defaultSelfMember(),
    val members: List<FamilyMember> = emptyList(),
    val invitations: List<FamilyInvitation> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val activityFeed: List<ActivityFeedItem> = emptyList(),
    val selectedMemberId: Int? = null,
    val nextCheckInLabel: String = "20:00 da umumiy check-in",
    val trustedPlacesCount: Int = 3,
    val activeSosAlerts: List<SosAlert> = emptyList(),
    val sosState: SosUiState = SosUiState()
)

private fun defaultSelfMember() = FamilyMember(
    id = 0,
    name = "Nodir Yusupov",
    relation = "Men",
    age = 31,
    lat = 41.3111,
    lon = 69.2797,
    address = "Yunusobod, 9-kvartal",
    placeLabel = "Uy",
    battery = 92,
    steps = 4680,
    heartRate = 76,
    lastUpdate = "Hozirgina",
    status = MemberStatus.SAFE,
    note = "Asosiy qurilma faol holatda.",
    safeZone = "Uy va ish yo'nalishi",
    phone = "+998 90 321 45 67",
    schedule = "20:00 oilaviy check-in",
    avatarSeed = 0,
    avatarUri = "",
    distanceKm = 3.6,
    isCurrentUser = true
)
