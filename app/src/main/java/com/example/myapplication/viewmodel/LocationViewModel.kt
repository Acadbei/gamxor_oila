package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ActivityFeedItem
import com.example.myapplication.data.model.AppNotification
import com.example.myapplication.data.model.CaregiverProfile
import com.example.myapplication.data.model.DemoActionResult
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.data.model.FamilyInvitation
import com.example.myapplication.data.model.FamilyMember
import com.example.myapplication.data.model.FeedSeverity
import com.example.myapplication.data.model.InvitationStatus
import com.example.myapplication.data.model.MemberStatus
import com.example.myapplication.data.model.NotificationCategory
import com.example.myapplication.data.model.SosContact
import com.example.myapplication.data.model.SosContactState
import com.example.myapplication.data.model.SosUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel() {

    private val initialProfile = CaregiverProfile()

    private val initialMembers = listOf(
        FamilyMember(
            id = 1,
            name = "Anvar",
            relation = "Dada",
            age = 61,
            lat = 41.3111,
            lon = 69.2797,
            address = "Yunusobod, 4-daha",
            placeLabel = "Uy atrofida",
            battery = 81,
            steps = 3821,
            heartRate = 74,
            lastUpdate = "1 daqiqa oldin",
            status = MemberStatus.SAFE,
            note = "Ertalabki yurishdan qaytdi, holati yaxshi.",
            safeZone = "Uy hududi",
            phone = "+998 90 123 45 67",
            schedule = "21:00 dori eslatmasi"
        ),
        FamilyMember(
            id = 2,
            name = "Dilnoza",
            relation = "Ona",
            age = 56,
            lat = 41.2995,
            lon = 69.2401,
            address = "Chilonzor, 12-kvartal",
            placeLabel = "Ishdan qaytish yo'lida",
            battery = 54,
            steps = 6140,
            heartRate = 82,
            lastUpdate = "Hozirgina",
            status = MemberStatus.MOVING,
            note = "Telefon GPS faol, mashinada harakatlanmoqda.",
            safeZone = "Ish va uy yo'nalishi",
            phone = "+998 93 555 66 77",
            schedule = "19:30 oilaviy qo'ng'iroq"
        ),
        FamilyMember(
            id = 3,
            name = "Bobur",
            relation = "O'g'il",
            age = 14,
            lat = 41.3278,
            lon = 69.2811,
            address = "Maktab, Olmazor tumani",
            placeLabel = "Maktab hududi",
            battery = 33,
            steps = 7280,
            heartRate = 92,
            lastUpdate = "3 daqiqa oldin",
            status = MemberStatus.NEEDS_ATTENTION,
            note = "Batareya past, darsdan keyin zaryadlatish kerak.",
            safeZone = "Maktab geozonasi",
            phone = "+998 97 700 11 22",
            schedule = "16:30 dan keyin repititor"
        ),
        FamilyMember(
            id = 4,
            name = "O'ktam",
            relation = "Bobo",
            age = 72,
            lat = 41.3000,
            lon = 69.2000,
            address = "Sergeli, 6-bekat",
            placeLabel = "Mahalla dorixonasi",
            battery = 69,
            steps = 2910,
            heartRate = 76,
            lastUpdate = "8 daqiqa oldin",
            status = MemberStatus.SAFE,
            note = "Dorilarni oldi, uyga qaytishga tayyor.",
            safeZone = "Mahalla va dorixona",
            phone = "+998 91 444 99 00",
            schedule = "18:00 qand nazorati"
        )
    )

    private val inviteCandidates = listOf(
        FamilyMember(
            id = 5,
            name = "Madina",
            relation = "Singil",
            age = 24,
            lat = 41.3420,
            lon = 69.2865,
            address = "Mirzo-Ulug'bek, TTZ-2",
            placeLabel = "Universitet hududi",
            battery = 76,
            steps = 5022,
            heartRate = 79,
            lastUpdate = "Hozirgina",
            status = MemberStatus.SAFE,
            note = "Platformada faol. Taklif qabul qilingach jonli monitoring ulanadi.",
            safeZone = "Universitet va uy yo'nalishi",
            phone = "+998 94 222 11 00",
            schedule = "20:30 safe-arrival check-in"
        ),
        FamilyMember(
            id = 6,
            name = "Jasur",
            relation = "Amaki",
            age = 48,
            lat = 41.2754,
            lon = 69.2293,
            address = "Shayxontohur, Labzak",
            placeLabel = "Ofis hududi",
            battery = 63,
            steps = 4489,
            heartRate = 80,
            lastUpdate = "2 daqiqa oldin",
            status = MemberStatus.MOVING,
            note = "Ish rejimida. Platformada taklifni qabul qilishi mumkin.",
            safeZone = "Ofis va uy hududi",
            phone = "+998 88 410 77 66",
            schedule = "18:45 yo'l holati eslatmasi"
        )
    ).associateBy { canonicalPhone(it.phone) }

    private val initialFeed = listOf(
        ActivityFeedItem(
            id = 1,
            memberId = 2,
            title = "Dilnoza yo'lga chiqdi",
            subtitle = "Chilonzordan Yunusobod tomonga harakatlanyapti.",
            timeLabel = "Hozirgina",
            severity = FeedSeverity.POSITIVE
        ),
        ActivityFeedItem(
            id = 2,
            memberId = 3,
            title = "Boburning batareyasi 33%",
            subtitle = "Maktabdan chiqishidan oldin powerbank kerak bo'lishi mumkin.",
            timeLabel = "3 daqiqa oldin",
            severity = FeedSeverity.WARNING
        ),
        ActivityFeedItem(
            id = 3,
            memberId = 1,
            title = "Anvar uy hududiga qaytdi",
            subtitle = "Ertalabki yurish tugadi, GPS uy geozonasida.",
            timeLabel = "12 daqiqa oldin",
            severity = FeedSeverity.NEUTRAL
        ),
        ActivityFeedItem(
            id = 4,
            memberId = 4,
            title = "O'ktam dorixonada",
            subtitle = "Rejadagi dori eslatmasi bajarildi.",
            timeLabel = "18 daqiqa oldin",
            severity = FeedSeverity.POSITIVE
        )
    )

    private val initialNotifications = listOf(
        AppNotification(
            id = 1,
            title = "Boburning batareyasi past",
            message = "33% qoldi. Powerbank yoki qo'ng'iroq bilan eslatish tavsiya qilinadi.",
            timeLabel = "3 daqiqa oldin",
            category = NotificationCategory.SAFETY
        ),
        AppNotification(
            id = 2,
            title = "Profilni yakunlang",
            message = "Rasm, emergency contact va ruxsatlarni to'ldirib demo'ni to'liq ko'ring.",
            timeLabel = "12 daqiqa oldin",
            category = NotificationCategory.SYSTEM
        ),
        AppNotification(
            id = 3,
            title = "Dilnoza check-in oynasi yaqinlashdi",
            message = "19:30 dagi oilaviy qo'ng'iroq rejasini tasdiqlash mumkin.",
            timeLabel = "Bugun",
            category = NotificationCategory.SYSTEM,
            isRead = true
        )
    )

    private val _uiState = MutableStateFlow(DemoUiState())
    val uiState: StateFlow<DemoUiState> = _uiState.asStateFlow()

    init {
        loadDemo()
    }

    private fun loadDemo() {
        viewModelScope.launch {
            delay(650)
            _uiState.value = DemoUiState(
                isLoading = false,
                caregiverName = displayName(initialProfile.fullName),
                familyLabel = initialProfile.familyLabel,
                profile = initialProfile,
                members = initialMembers,
                invitations = emptyList(),
                notifications = initialNotifications,
                activityFeed = initialFeed,
                selectedMemberId = 2
            )
        }
    }

    fun requestCode(phone: String) {
        val normalizedPhone = phone.filter(Char::isDigit)
        if (normalizedPhone.length < 9) {
            _uiState.update { it.copy(loginError = "Telefon raqamni to'liq kiriting.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSendingCode = true,
                    loginError = null,
                    otpRequested = false
                )
            }
            delay(900)
            _uiState.update {
                it.copy(
                    isSendingCode = false,
                    otpRequested = true,
                    loginError = null,
                    otpHint = "2580"
                )
            }
        }
    }

    fun login(phone: String, code: String) {
        val normalizedPhone = phone.filter(Char::isDigit)
        val normalizedCode = code.filter(Char::isDigit)

        if (normalizedPhone.length < 9) {
            _uiState.update { it.copy(loginError = "Telefon raqamni tekshiring.") }
            return
        }

        if (normalizedCode.length < 4) {
            _uiState.update { it.copy(loginError = "SMS kodni kiriting.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, loginError = null) }
            delay(850)

            if (normalizedCode == "2580" || normalizedCode == "123456") {
                _uiState.update { it.copy(isVerifying = false, isLoggedIn = true, loginError = null) }
            } else {
                _uiState.update {
                    it.copy(
                        isVerifying = false,
                        loginError = "Kod noto'g'ri. Demo uchun 2580 dan foydalaning."
                    )
                }
            }
        }
    }

    fun quickDemoLogin() {
        _uiState.update {
            it.copy(
                otpRequested = true,
                isLoggedIn = true,
                loginError = null
            )
        }
    }

    fun selectMember(memberId: Int) {
        _uiState.update { it.copy(selectedMemberId = memberId) }
    }

    fun refreshDemo() {
        val currentState = _uiState.value
        if (currentState.isRefreshing || currentState.members.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            delay(700)

            val currentIndex = currentState.members.indexOfFirst { it.id == currentState.selectedMemberId }
                .takeIf { it >= 0 } ?: 0
            val nextMember = currentState.members[(currentIndex + 1) % currentState.members.size]

            val updatedMembers = currentState.members.map { member ->
                if (member.id == nextMember.id) {
                    member.copy(
                        battery = (member.battery - 1).coerceAtLeast(18),
                        steps = member.steps + 164,
                        lastUpdate = "Hozirgina",
                        status = if (member.status == MemberStatus.NEEDS_ATTENTION) {
                            MemberStatus.MOVING
                        } else {
                            member.status
                        }
                    )
                } else {
                    member
                }
            }

            val updatedFeed = buildList {
                add(
                    ActivityFeedItem(
                        id = nextFeedId(currentState),
                        memberId = nextMember.id,
                        title = "${nextMember.name} lokatsiyasi yangilandi",
                        subtitle = "${nextMember.placeLabel} bo'yicha yangi nuqta olindi.",
                        timeLabel = "Hozirgina",
                        severity = FeedSeverity.POSITIVE
                    )
                )
                addAll(currentState.activityFeed.take(5))
            }

            val updatedNotification = AppNotification(
                id = nextNotificationId(currentState),
                title = "Lokatsiya yangilandi",
                message = "${nextMember.name} bo'yicha yangi lokatsiya nuqtasi olindi.",
                timeLabel = "Hozirgina",
                category = NotificationCategory.SYSTEM
            )

            _uiState.update {
                it.copy(
                    isRefreshing = false,
                    lastSyncLabel = "Hozirgina",
                    members = updatedMembers,
                    notifications = listOf(updatedNotification) + it.notifications.take(7),
                    activityFeed = updatedFeed,
                    selectedMemberId = nextMember.id
                )
            }
        }
    }

    fun saveProfile(profile: CaregiverProfile): DemoActionResult {
        val trimmedName = profile.fullName.trim()
        val canonicalPhone = canonicalPhone(profile.phone)

        if (trimmedName.isBlank()) {
            return DemoActionResult(false, "To'liq ismni kiriting.")
        }

        if (canonicalPhone.length != 12) {
            return DemoActionResult(false, "Profil telefon raqami to'liq emas.")
        }

        if (profile.email.isNotBlank() && !profile.email.contains("@")) {
            return DemoActionResult(false, "Email manzilini tekshiring.")
        }

        val normalizedProfile = profile.copy(
            fullName = trimmedName,
            phone = formatPhone(canonicalPhone),
            emergencyContact = formatFlexiblePhone(profile.emergencyContact)
        )

        _uiState.update { state ->
            val savedNotification = AppNotification(
                id = nextNotificationId(state),
                title = "Profil saqlandi",
                message = "Shaxsiy ma'lumotlar va ruxsatlar yangilandi.",
                timeLabel = "Hozirgina",
                category = NotificationCategory.SYSTEM
            )

            state.copy(
                caregiverName = displayName(normalizedProfile.fullName),
                familyLabel = normalizedProfile.familyLabel.ifBlank { state.familyLabel },
                lastSyncLabel = "Profil yangilandi",
                profile = normalizedProfile,
                notifications = listOf(savedNotification) + state.notifications.take(7)
            )
        }

        return DemoActionResult(true, "Profil ma'lumotlari saqlandi.")
    }

    fun sendInvitation(
        name: String,
        relation: String,
        phone: String
    ): DemoActionResult {
        val state = _uiState.value
        val canonicalPhone = canonicalPhone(phone)

        if (canonicalPhone.length != 12) {
            return DemoActionResult(false, "Telefon raqamni to'liq kiriting.")
        }

        if (state.members.any { canonicalPhone(it.phone) == canonicalPhone }) {
            return DemoActionResult(false, "Bu raqam allaqachon oilaga ulangan.")
        }

        if (state.invitations.any { canonicalPhone(it.phone) == canonicalPhone && it.status != InvitationStatus.ACCEPTED }) {
            return DemoActionResult(false, "Bu raqamga taklif allaqachon yuborilgan.")
        }

        val candidate = inviteCandidates[canonicalPhone]
        val resolvedName = name.trim().ifBlank { candidate?.name ?: "Yangi a'zo" }
        val resolvedRelation = relation.trim().ifBlank { candidate?.relation ?: "Qarindosh" }
        val isPlatformUser = candidate != null
        val invitationStatus = if (isPlatformUser) {
            InvitationStatus.PENDING_ACCEPTANCE
        } else {
            InvitationStatus.WAITING_INSTALL
        }

        _uiState.update { current ->
            val inviteId = nextInvitationId(current)
            val invitation = FamilyInvitation(
                id = inviteId,
                name = resolvedName,
                relation = resolvedRelation,
                phone = formatPhone(canonicalPhone),
                sentAtLabel = "Hozirgina",
                status = invitationStatus,
                isPlatformUser = isPlatformUser
            )

            val inviteNotification = AppNotification(
                id = nextNotificationId(current),
                title = if (isPlatformUser) {
                    "$resolvedName platformada topildi"
                } else {
                    "SMS taklif yuborildi"
                },
                message = if (isPlatformUser) {
                    "Taklif yuborildi. Accept qilsa oilaga avtomatik ulanadi."
                } else {
                    "${formatPhone(canonicalPhone)} raqamiga yuklab olish havolasi yuborildi."
                },
                timeLabel = "Hozirgina",
                category = NotificationCategory.INVITE,
                inviteId = inviteId,
                actionLabel = if (isPlatformUser) "Qabul qilindi" else null
            )

            val inviteFeed = ActivityFeedItem(
                id = nextFeedId(current),
                memberId = null,
                title = if (isPlatformUser) {
                    "$resolvedName ga platforma taklifi yuborildi"
                } else {
                    "${formatPhone(canonicalPhone)} ga SMS taklif yuborildi"
                },
                subtitle = if (isPlatformUser) {
                    "$resolvedRelation roli bilan ulanishni kutyapti."
                } else {
                    "Ilovani o'rnatgach oilaga qo'shish mumkin bo'ladi."
                },
                timeLabel = "Hozirgina",
                severity = FeedSeverity.NEUTRAL
            )

            current.copy(
                lastSyncLabel = "Taklif yuborildi",
                invitations = listOf(invitation) + current.invitations,
                notifications = listOf(inviteNotification) + current.notifications.take(7),
                activityFeed = listOf(inviteFeed) + current.activityFeed.take(5)
            )
        }

        return DemoActionResult(
            success = true,
            message = if (isPlatformUser) {
                "Taklif yuborildi. Qabul qilinsa oilaga qo'shiladi."
            } else {
                "SMS invitation yuborildi. U platformaga qo'shilgach bog'lashni davom ettirasiz."
            }
        )
    }

    fun acceptInvitation(inviteId: Int): DemoActionResult {
        val state = _uiState.value
        val invitation = state.invitations.firstOrNull { it.id == inviteId }
            ?: return DemoActionResult(false, "Taklif topilmadi.")

        if (invitation.status == InvitationStatus.ACCEPTED) {
            return DemoActionResult(false, "Bu taklif allaqachon qabul qilingan.")
        }

        if (invitation.status == InvitationStatus.WAITING_INSTALL) {
            return DemoActionResult(false, "Bu foydalanuvchi hali platformaga kirmagan.")
        }

        val candidate = inviteCandidates[canonicalPhone(invitation.phone)]
            ?: return DemoActionResult(false, "Platforma foydalanuvchisi topilmadi.")

        _uiState.update { current ->
            val linkedMembers = if (current.members.any { canonicalPhone(it.phone) == canonicalPhone(candidate.phone) }) {
                current.members
            } else {
                current.members + candidate
            }

            val acceptedNotification = AppNotification(
                id = nextNotificationId(current),
                title = "${invitation.name} taklifni qabul qildi",
                message = "${invitation.relation} endi oilaviy dashboardga ulandi.",
                timeLabel = "Hozirgina",
                category = NotificationCategory.INVITE
            )

            val acceptedFeed = ActivityFeedItem(
                id = nextFeedId(current),
                memberId = candidate.id,
                title = "${invitation.name} oilaga qo'shildi",
                subtitle = "Invitation accept qilindi va monitoring aktivlashdi.",
                timeLabel = "Hozirgina",
                severity = FeedSeverity.POSITIVE
            )

            current.copy(
                lastSyncLabel = "Taklif qabul qilindi",
                members = linkedMembers,
                selectedMemberId = candidate.id,
                invitations = current.invitations.map { item ->
                    if (item.id == inviteId) item.copy(status = InvitationStatus.ACCEPTED) else item
                },
                notifications = listOf(acceptedNotification) + current.notifications.map { item ->
                    if (item.inviteId == inviteId) item.copy(isRead = true, actionLabel = null) else item
                }.take(7),
                activityFeed = listOf(acceptedFeed) + current.activityFeed.take(5)
            )
        }

        return DemoActionResult(true, "${invitation.name} taklifni qabul qildi.")
    }

    fun markNotificationRead(notificationId: Int) {
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map { notification ->
                    if (notification.id == notificationId) {
                        notification.copy(isRead = true)
                    } else {
                        notification
                    }
                }
            )
        }
    }

    fun markAllNotificationsRead() {
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map { it.copy(isRead = true) }
            )
        }
    }

    fun triggerSos() {
        val currentState = _uiState.value
        if (currentState.sosState.isSending || currentState.members.isEmpty()) return

        val initialContacts = currentState.members.map { member ->
            SosContact(
                memberId = member.id,
                name = member.name,
                phone = member.phone,
                state = SosContactState.CALLING
            )
        }

        _uiState.update {
            it.copy(
                sosState = SosUiState(
                    isActive = true,
                    isSending = true,
                    summary = "SOS qo'ng'iroqlari va bildirishnomalar yuborilmoqda.",
                    sentAtLabel = "Hozirgina",
                    contacts = initialContacts
                ),
                lastSyncLabel = "SOS ishga tushdi"
            )
        }

        viewModelScope.launch {
            currentState.members.forEachIndexed { index, member ->
                delay(450)
                _uiState.update { state ->
                    val updatedContacts = state.sosState.contacts.map { contact ->
                        if (contact.memberId == member.id) {
                            contact.copy(state = SosContactState.NOTIFIED)
                        } else {
                            contact
                        }
                    }

                    val sosFeedItem = ActivityFeedItem(
                        id = nextFeedId(state),
                        memberId = member.id,
                        title = "SOS: ${member.name} xabardor qilindi",
                        subtitle = "${member.phone} raqamiga demo qo'ng'iroq va alert yuborildi.",
                        timeLabel = "Hozirgina",
                        severity = FeedSeverity.WARNING
                    )

                    state.copy(
                        activityFeed = listOf(sosFeedItem) + state.activityFeed.take(5),
                        sosState = state.sosState.copy(
                            contacts = updatedContacts,
                            summary = "Yuborildi: ${index + 1}/${currentState.members.size}"
                        )
                    )
                }
            }

            _uiState.update { state ->
                val sosNotification = AppNotification(
                    id = nextNotificationId(state),
                    title = "SOS markazi yakunlandi",
                    message = "${state.members.size} ta oila a'zosiga favqulodda signal yuborildi.",
                    timeLabel = "Hozirgina",
                    category = NotificationCategory.SAFETY
                )

                state.copy(
                    sosState = state.sosState.copy(
                        isSending = false,
                        summary = "Barcha oila a'zolariga SOS qo'ng'iroq va bildirish yuborildi."
                    ),
                    lastSyncLabel = "SOS yakunlandi",
                    notifications = listOf(sosNotification) + state.notifications.take(7)
                )
            }
        }
    }

    fun clearSosState() {
        _uiState.update {
            it.copy(sosState = SosUiState())
        }
    }

    fun signOut() {
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                isSendingCode = false,
                isVerifying = false,
                otpRequested = false,
                loginError = null,
                lastSyncLabel = "Hozirgina",
                sosState = SosUiState()
            )
        }
    }

    private fun nextFeedId(state: DemoUiState): Int = (state.activityFeed.maxOfOrNull { it.id } ?: 0) + 1

    private fun nextNotificationId(state: DemoUiState): Int = (state.notifications.maxOfOrNull { it.id } ?: 0) + 1

    private fun nextInvitationId(state: DemoUiState): Int = (state.invitations.maxOfOrNull { it.id } ?: 0) + 1

    private fun displayName(fullName: String): String =
        fullName.trim().substringBefore(" ").ifBlank { fullName.trim() }

    private fun formatFlexiblePhone(value: String): String {
        val canonical = canonicalPhone(value)
        return if (canonical.length == 12) formatPhone(canonical) else value.trim()
    }

    private fun formatPhone(phone: String): String {
        val digits = canonicalPhone(phone)
        return if (digits.length == 12 && digits.startsWith("998")) {
            "+998 ${digits.substring(3, 5)} ${digits.substring(5, 8)} ${digits.substring(8, 10)} ${digits.substring(10, 12)}"
        } else {
            phone.trim()
        }
    }

    private fun canonicalPhone(phone: String): String {
        val digits = phone.filter(Char::isDigit)
        return when {
            digits.length == 9 -> "998$digits"
            digits.length >= 12 && digits.startsWith("998") -> digits.takeLast(12)
            else -> digits
        }
    }
}
