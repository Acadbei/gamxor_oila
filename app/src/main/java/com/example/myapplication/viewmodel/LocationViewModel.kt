package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.LocalUserStore
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
import com.example.myapplication.data.model.SosAlert
import com.example.myapplication.data.model.SosContact
import com.example.myapplication.data.model.SosContactState
import com.example.myapplication.data.model.SosUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val initialProfile = CaregiverProfile()
    private val userStore = LocalUserStore(application)

    private val initialMembers = listOf(
        FamilyMember(
            id = 1,
            name = "Anvar Yusupov",
            relation = "Dada",
            age = 61,
            lat = 41.3118,
            lon = 69.2819,
            address = "Yunusobod, 4-daha",
            placeLabel = "Uy atrofida",
            battery = 81,
            steps = 3821,
            heartRate = 74,
            lastUpdate = "1 daqiqa oldin",
            status = MemberStatus.SAFE,
            note = "Tonggi yurishdan qaytdi.",
            safeZone = "Uy hududi",
            phone = "+998 90 123 45 67",
            schedule = "21:00 dori eslatmasi",
            avatarSeed = 1,
            distanceKm = 2.8
        ),
        FamilyMember(
            id = 2,
            name = "Dilnoza Yusupova",
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
            note = "Transportda harakatlanmoqda.",
            safeZone = "Ish va uy yo'nalishi",
            phone = "+998 93 555 66 77",
            schedule = "19:30 oilaviy qo'ng'iroq",
            avatarSeed = 2,
            distanceKm = 4.9
        ),
        FamilyMember(
            id = 3,
            name = "Bobur Yusupov",
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
            note = "Batareya past, zaryad kerak.",
            safeZone = "Maktab geozonasi",
            phone = "+998 97 700 11 22",
            schedule = "16:30 repititor",
            avatarSeed = 3,
            distanceKm = 5.7
        ),
        FamilyMember(
            id = 4,
            name = "O'ktam Yusupov",
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
            note = "Dorilarni olib uyga qaytmoqda.",
            safeZone = "Mahalla va dorixona",
            phone = "+998 91 444 99 00",
            schedule = "18:00 qand nazorati",
            avatarSeed = 4,
            distanceKm = 1.9
        )
    )

    private val inviteCandidates = listOf(
        FamilyMember(
            id = 5,
            name = "Madina Yusupova",
            relation = "Singil",
            age = 24,
            lat = 41.3420,
            lon = 69.2865,
            address = "Mirzo Ulug'bek, TTZ-2",
            placeLabel = "Universitet hududi",
            battery = 76,
            steps = 5022,
            heartRate = 79,
            lastUpdate = "Hozirgina",
            status = MemberStatus.SAFE,
            note = "Taklif qabul qilinsa xaritaga ulanadi.",
            safeZone = "Universitet va uy yo'nalishi",
            phone = "+998 94 222 11 00",
            schedule = "20:30 safe-arrival check-in",
            avatarSeed = 5,
            distanceKm = 3.8
        ),
        FamilyMember(
            id = 6,
            name = "Jasur Karimov",
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
            note = "Yo'lda, GPS faol.",
            safeZone = "Ofis va uy hududi",
            phone = "+998 88 410 77 66",
            schedule = "18:45 yo'l holati eslatmasi",
            avatarSeed = 6,
            distanceKm = 4.1
        )
    ).associateBy { canonicalPhone(it.phone) }

    private val initialFeed = listOf(
        ActivityFeedItem(
            id = 1,
            memberId = 0,
            title = "Ilova ishga tushdi",
            subtitle = "Xarita va ro'yxatdan o'tish oqimi tayyor.",
            timeLabel = "Hozirgina",
            severity = FeedSeverity.POSITIVE
        ),
        ActivityFeedItem(
            id = 2,
            memberId = 2,
            title = "Dilnoza yo'lda",
            subtitle = "Chilonzordan Yunusobod tomonga harakatlanyapti.",
            timeLabel = "Hozirgina",
            severity = FeedSeverity.NEUTRAL
        ),
        ActivityFeedItem(
            id = 3,
            memberId = 3,
            title = "Bobur SOS yubordi",
            subtitle = "Maktab hududidan tezkor signal kelgan.",
            timeLabel = "3 daqiqa oldin",
            severity = FeedSeverity.WARNING
        )
    )

    private val initialNotifications = listOf(
        AppNotification(
            id = 1,
            title = "Ro'yxatdan o'tishni yakunlang",
            message = "Yuqoridagi tugma orqali profilni aktivlashtirsangiz oila ulanishi mustahkamlanadi.",
            timeLabel = "Hozirgina",
            category = NotificationCategory.SYSTEM
        ),
        AppNotification(
            id = 2,
            title = "Boburdan SOS signali",
            message = "Maktab hududidan tezkor yordam signali keldi.",
            timeLabel = "3 daqiqa oldin",
            category = NotificationCategory.SAFETY
        ),
        AppNotification(
            id = 3,
            title = "Yunusobodda kechki ogohlantirish",
            message = "Mahalladagi ichki ko'chada shubhali shaxs haqida xabar kelib tushdi. Bolalarni yolg'iz yubormaslik tavsiya etiladi.",
            timeLabel = "12 daqiqa oldin",
            category = NotificationCategory.CRIME
        ),
        AppNotification(
            id = 4,
            title = "Olmazorda telefon o'g'irlash holati",
            message = "Maktab yaqinida telefon tortib olish bo'yicha murojaat qayd etildi. Farzandlaringiz bilan aloqa vositalarini ehtiyot qilishni eslating.",
            timeLabel = "28 daqiqa oldin",
            category = NotificationCategory.CRIME
        )
    )

    private val _uiState = MutableStateFlow(DemoUiState())
    val uiState: StateFlow<DemoUiState> = _uiState.asStateFlow()

    init {
        loadDemo()
    }

    private fun loadDemo() {
        viewModelScope.launch {
            delay(500)
            val restoredProfile = userStore.loadProfile(initialProfile)
            val isRegistered = userStore.loadRegistrationState()
            val bootNotifications = if (isRegistered) {
                listOf(
                    AppNotification(
                        id = 1,
                        title = "Profil faol",
                        message = "Sizning profilingiz ro'yxatdan o'tgan va saqlab qo'yilgan.",
                        timeLabel = "Hozirgina",
                        category = NotificationCategory.SYSTEM
                    ),
                    *initialNotifications.filterNot { it.category == NotificationCategory.SYSTEM }.toTypedArray()
                )
            } else {
                initialNotifications
            }
            _uiState.value = DemoUiState(
                isLoading = false,
                isLoggedIn = true,
                isRegistered = isRegistered,
                caregiverName = displayName(restoredProfile.fullName),
                familyLabel = restoredProfile.familyLabel,
                profile = restoredProfile,
                selfMember = selfMemberFrom(restoredProfile),
                members = initialMembers,
                invitations = emptyList(),
                notifications = bootNotifications,
                activityFeed = initialFeed,
                selectedMemberId = 0,
                activeSosAlerts = listOf(
                    SosAlert(
                        memberId = 3,
                        name = "Bobur Yusupov",
                        relation = "O'g'il",
                        phone = "+998 97 700 11 22",
                        address = "Maktab, Olmazor tumani",
                        lastUpdate = "3 daqiqa oldin"
                    )
                )
            )
        }
    }

    fun resetAuthState() {
        _uiState.update {
            it.copy(
                otpRequested = false,
                isSendingCode = false,
                isVerifying = false,
                loginError = null
            )
        }
    }

    fun requestCode(phone: String) {
        if (canonicalPhone(phone).length != 12) {
            _uiState.update { it.copy(loginError = "Telefon raqamni to'liq kiriting.") }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSendingCode = true,
                    otpRequested = false,
                    loginError = null
                )
            }
            delay(700)
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

    fun verifySmsCode(phone: String, code: String): DemoActionResult {
        val normalizedPhone = canonicalPhone(phone)
        val normalizedCode = code.filter(Char::isDigit)

        val result = when {
            normalizedPhone.length != 12 -> DemoActionResult(false, "Telefon raqamni tekshiring.")
            normalizedCode.length < 4 -> DemoActionResult(false, "SMS kodni kiriting.")
            normalizedCode == "2580" || normalizedCode == "123456" -> DemoActionResult(true, "Kod tasdiqlandi.")
            else -> DemoActionResult(false, "Kod noto'g'ri. Demo uchun 2580 dan foydalaning.")
        }

        _uiState.update {
            it.copy(
                isVerifying = false,
                loginError = if (result.success) null else result.message
            )
        }

        return result
    }

    fun completeRegistration(fullName: String, phone: String): DemoActionResult {
        val state = _uiState.value
        val trimmedName = fullName.trim()
        val canonicalPhone = canonicalPhone(phone)

        val result = when {
            trimmedName.isBlank() -> DemoActionResult(false, "Ismingizni kiriting.")
            canonicalPhone.length != 12 -> DemoActionResult(false, "Telefon raqamni tekshiring.")
            state.isRegistered -> DemoActionResult(false, "Profil allaqachon ro'yxatdan o'tgan.")
            else -> DemoActionResult(true, "Ro'yxatdan o'tish yakunlandi.")
        }

        if (!result.success) {
            _uiState.update { it.copy(loginError = result.message) }
            return result
        }

        val updatedProfile = state.profile.copy(
            fullName = trimmedName,
            phone = formatPhone(canonicalPhone)
        )

        _uiState.update { current ->
            val notification = AppNotification(
                id = nextNotificationId(current),
                title = "Profil faollashtirildi",
                message = "Telefon raqam va ism muvaffaqiyatli tasdiqlandi.",
                timeLabel = "Hozirgina",
                category = NotificationCategory.SYSTEM
            )

            current.copy(
                isRegistered = true,
                otpRequested = false,
                isSendingCode = false,
                isVerifying = false,
                loginError = null,
                caregiverName = displayName(updatedProfile.fullName),
                lastSyncLabel = "Ro'yxatdan o'tildi",
                profile = updatedProfile,
                selfMember = selfMemberFrom(updatedProfile, current.selfMember),
                notifications = listOf(notification) + current.notifications.take(7)
            )
        }

        userStore.saveProfile(updatedProfile)
        userStore.saveRegistrationState(true)
        return result
    }

    fun selectMember(memberId: Int) {
        _uiState.update { it.copy(selectedMemberId = memberId) }
    }

    fun refreshDemo() {
        val currentState = _uiState.value
        if (currentState.isRefreshing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            delay(600)

            val allMembers = listOf(currentState.selfMember) + currentState.members
            val currentIndex = allMembers.indexOfFirst { it.id == currentState.selectedMemberId }
                .takeIf { it >= 0 } ?: 0
            val nextMember = allMembers[(currentIndex + 1) % allMembers.size]

            val updatedSelf = if (nextMember.id == currentState.selfMember.id) {
                currentState.selfMember.copy(
                    battery = (currentState.selfMember.battery - 1).coerceAtLeast(20),
                    steps = currentState.selfMember.steps + 120,
                    distanceKm = currentState.selfMember.distanceKm + 0.2,
                    lastUpdate = "Hozirgina"
                )
            } else {
                currentState.selfMember
            }

            val updatedMembers = currentState.members.map { member ->
                if (member.id == nextMember.id) {
                    member.copy(
                        battery = (member.battery - 1).coerceAtLeast(18),
                        steps = member.steps + 164,
                        distanceKm = member.distanceKm + 0.3,
                        lastUpdate = "Hozirgina",
                        status = if (member.status == MemberStatus.NEEDS_ATTENTION) MemberStatus.MOVING else member.status
                    )
                } else {
                    member
                }
            }

            val updatedFeed = listOf(
                ActivityFeedItem(
                    id = nextFeedId(currentState),
                    memberId = nextMember.id,
                    title = "${nextMember.name} joylashuvi yangilandi",
                    subtitle = "${nextMember.placeLabel} bo'yicha yangi nuqta olindi.",
                    timeLabel = "Hozirgina",
                    severity = FeedSeverity.POSITIVE
                )
            ) + currentState.activityFeed.take(5)

            _uiState.update {
                it.copy(
                    isRefreshing = false,
                    lastSyncLabel = "Hozirgina",
                    selfMember = updatedSelf,
                    members = updatedMembers,
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
            return DemoActionResult(false, "Ism va familiyani kiriting.")
        }

        if (canonicalPhone.length != 12) {
            return DemoActionResult(false, "Telefon raqami to'liq emas.")
        }

        val normalizedProfile = profile.copy(
            fullName = trimmedName,
            phone = formatPhone(canonicalPhone),
            familyLabel = profile.familyLabel.ifBlank { _uiState.value.familyLabel },
            address = profile.address.trim().ifBlank { _uiState.value.profile.address },
            emergencyContact = formatFlexiblePhone(profile.emergencyContact)
        )

        _uiState.update { state ->
            val savedNotification = AppNotification(
                id = nextNotificationId(state),
                title = "Profil saqlandi",
                message = "Shaxsiy ma'lumotlar yangilandi.",
                timeLabel = "Hozirgina",
                category = NotificationCategory.SYSTEM
            )

            state.copy(
                caregiverName = displayName(normalizedProfile.fullName),
                familyLabel = normalizedProfile.familyLabel,
                lastSyncLabel = "Profil yangilandi",
                profile = normalizedProfile,
                selfMember = selfMemberFrom(normalizedProfile, state.selfMember),
                notifications = listOf(savedNotification) + state.notifications.take(7)
            )
        }

        userStore.saveProfile(normalizedProfile)
        return DemoActionResult(true, "Profil ma'lumotlari saqlandi.")
    }

    fun sendInvitation(
        name: String,
        relation: String,
        phone: String
    ): DemoActionResult {
        val state = _uiState.value
        val normalizedPhone = canonicalPhone(phone)

        if (normalizedPhone.length != 12) {
            return DemoActionResult(false, "Telefon raqamni to'liq kiriting.")
        }

        if (canonicalPhone(state.selfMember.phone) == normalizedPhone || state.members.any { canonicalPhone(it.phone) == normalizedPhone }) {
            return DemoActionResult(false, "Bu raqam allaqachon oilaga ulangan.")
        }

        if (state.invitations.any { canonicalPhone(it.phone) == normalizedPhone && it.status != InvitationStatus.ACCEPTED }) {
            return DemoActionResult(false, "Bu raqamga taklif allaqachon yuborilgan.")
        }

        val candidate = inviteCandidates[normalizedPhone]
        val resolvedName = name.trim().ifBlank { candidate?.name ?: "Yangi a'zo" }
        val resolvedRelation = relation.trim().ifBlank { candidate?.relation ?: "Qarindosh" }
        val isPlatformUser = candidate != null
        val invitationStatus = if (isPlatformUser) InvitationStatus.PENDING_ACCEPTANCE else InvitationStatus.WAITING_INSTALL

        _uiState.update { current ->
            val inviteId = nextInvitationId(current)
            val invitation = FamilyInvitation(
                id = inviteId,
                name = resolvedName,
                relation = resolvedRelation,
                phone = formatPhone(normalizedPhone),
                sentAtLabel = "Hozirgina",
                status = invitationStatus,
                isPlatformUser = isPlatformUser
            )

            val inviteNotification = AppNotification(
                id = nextNotificationId(current),
                title = if (isPlatformUser) "$resolvedName platformada topildi" else "SMS taklif yuborildi",
                message = if (isPlatformUser) {
                    "Taklif qabul qilinsa oilaviy xaritaga ulanadi."
                } else {
                    "${formatPhone(normalizedPhone)} raqamiga ilova havolasi yuborildi."
                },
                timeLabel = "Hozirgina",
                category = NotificationCategory.INVITE,
                inviteId = inviteId,
                actionLabel = if (isPlatformUser) "Qabul qilindi" else null
            )

            current.copy(
                lastSyncLabel = "Taklif yuborildi",
                invitations = listOf(invitation) + current.invitations,
                notifications = listOf(inviteNotification) + current.notifications.take(7)
            )
        }

        return DemoActionResult(
            success = true,
            message = if (isPlatformUser) {
                "Taklif yuborildi. Qabul qilinsa oilaga ulanadi."
            } else {
                "SMS taklif yuborildi."
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
                message = "${invitation.relation} endi oilaviy kuzatuvga ulandi.",
                timeLabel = "Hozirgina",
                category = NotificationCategory.INVITE
            )

            current.copy(
                lastSyncLabel = "Taklif qabul qilindi",
                members = linkedMembers,
                selectedMemberId = candidate.id,
                invitations = current.invitations.map { item ->
                    if (item.id == inviteId) item.copy(status = InvitationStatus.ACCEPTED) else item
                },
                notifications = listOf(acceptedNotification) + current.notifications.take(7)
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

    fun dismissNotification(notificationId: Int): DemoActionResult {
        val state = _uiState.value
        val target = state.notifications.firstOrNull { it.id == notificationId }
            ?: return DemoActionResult(false, "Bildirishnoma topilmadi.")

        _uiState.update { current ->
            current.copy(
                notifications = current.notifications.filterNot { it.id == notificationId },
                lastSyncLabel = "Bildirishnoma o'chirildi"
            )
        }

        return DemoActionResult(true, "\"${target.title}\" o'chirildi.")
    }

    fun triggerSos() {
        val currentState = _uiState.value
        if (currentState.sosState.isSending || currentState.members.isEmpty()) return

        val contacts = currentState.members.map { member ->
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
                    summary = "SOS signal barcha oila a'zolariga yuborilmoqda.",
                    sentAtLabel = "Hozirgina",
                    contacts = contacts
                ),
                lastSyncLabel = "SOS ishga tushdi"
            )
        }

        viewModelScope.launch {
            currentState.members.forEachIndexed { index, member ->
                delay(350)
                _uiState.update { state ->
                    val updatedContacts = state.sosState.contacts.map { contact ->
                        if (contact.memberId == member.id) {
                            contact.copy(state = SosContactState.NOTIFIED)
                        } else {
                            contact
                        }
                    }

                    state.copy(
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
                        summary = "Barcha oila a'zolariga SOS signal yuborildi."
                    ),
                    notifications = listOf(sosNotification) + state.notifications.take(7),
                    lastSyncLabel = "SOS yakunlandi"
                )
            }
        }
    }

    fun clearSosState() {
        _uiState.update { it.copy(sosState = SosUiState()) }
    }

    fun signOut() {
        _uiState.update { state ->
            state.copy(
                isRegistered = false,
                otpRequested = false,
                isSendingCode = false,
                isVerifying = false,
                loginError = null,
                lastSyncLabel = "Ro'yxatdan chiqildi"
            )
        }
        userStore.saveRegistrationState(false)
    }

    private fun selfMemberFrom(profile: CaregiverProfile, previous: FamilyMember? = null): FamilyMember {
        return FamilyMember(
            id = 0,
            name = profile.fullName,
            relation = "Men",
            age = previous?.age ?: 31,
            lat = previous?.lat ?: 41.3111,
            lon = previous?.lon ?: 69.2797,
            address = profile.address,
            placeLabel = "Mening joylashuvim",
            battery = previous?.battery ?: 92,
            steps = previous?.steps ?: 4680,
            heartRate = previous?.heartRate ?: 76,
            lastUpdate = "Hozirgina",
            status = MemberStatus.SAFE,
            note = "Asosiy qurilma faol holatda.",
            safeZone = "Uy va ish yo'nalishi",
            phone = formatFlexiblePhone(profile.phone),
            schedule = "20:00 oilaviy check-in",
            avatarSeed = profile.avatarSeed,
            distanceKm = previous?.distanceKm ?: 3.6,
            isCurrentUser = true
        )
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
