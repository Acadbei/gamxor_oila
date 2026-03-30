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

enum class SosContactState {
    CALLING,
    NOTIFIED
}

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
    val schedule: String
)

data class ActivityFeedItem(
    val id: Int,
    val memberId: Int?,
    val title: String,
    val subtitle: String,
    val timeLabel: String,
    val severity: FeedSeverity
)

data class SosContact(
    val memberId: Int,
    val name: String,
    val phone: String,
    val state: SosContactState
)

data class SosUiState(
    val isActive: Boolean = false,
    val isSending: Boolean = false,
    val summary: String = "",
    val sentAtLabel: String = "",
    val contacts: List<SosContact> = emptyList()
)

data class DemoUiState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false,
    val isSendingCode: Boolean = false,
    val isVerifying: Boolean = false,
    val isRefreshing: Boolean = false,
    val otpRequested: Boolean = false,
    val otpHint: String = "2580",
    val loginError: String? = null,
    val caregiverName: String = "Nodir",
    val familyLabel: String = "Yusupovlar oilasi",
    val lastSyncLabel: String = "Hozirgina",
    val members: List<FamilyMember> = emptyList(),
    val activityFeed: List<ActivityFeedItem> = emptyList(),
    val selectedMemberId: Int? = null,
    val nextCheckInLabel: String = "20:00 da umumiy check-in",
    val trustedPlacesCount: Int = 3,
    val sosState: SosUiState = SosUiState()
)
