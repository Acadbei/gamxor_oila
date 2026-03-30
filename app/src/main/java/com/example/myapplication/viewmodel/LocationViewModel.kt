package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ActivityFeedItem
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.data.model.FamilyMember
import com.example.myapplication.data.model.FeedSeverity
import com.example.myapplication.data.model.MemberStatus
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
                members = initialMembers,
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
                        id = (currentState.activityFeed.maxOfOrNull { it.id } ?: 0) + 1,
                        memberId = nextMember.id,
                        title = "${nextMember.name} lokatsiyasi yangilandi",
                        subtitle = "${nextMember.placeLabel} bo'yicha yangi nuqta olindi.",
                        timeLabel = "Hozirgina",
                        severity = FeedSeverity.POSITIVE
                    )
                )
                addAll(currentState.activityFeed.take(4))
            }

            _uiState.update {
                it.copy(
                    isRefreshing = false,
                    lastSyncLabel = "Hozirgina",
                    members = updatedMembers,
                    activityFeed = updatedFeed,
                    selectedMemberId = nextMember.id
                )
            }
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

                    val nextFeedId = (state.activityFeed.maxOfOrNull { it.id } ?: 0) + 1
                    val sosFeedItem = ActivityFeedItem(
                        id = nextFeedId,
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

            _uiState.update {
                it.copy(
                    sosState = it.sosState.copy(
                        isSending = false,
                        summary = "Barcha oila a'zolariga SOS qo'ng'iroq va bildirish yuborildi."
                    ),
                    lastSyncLabel = "SOS yakunlandi"
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
}
