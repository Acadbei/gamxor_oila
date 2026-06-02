package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.BackendConnection
import com.example.myapplication.data.model.BackendStatus
import com.example.myapplication.data.model.CaregiverProfile
import com.example.myapplication.data.model.DemoActionResult
import com.example.myapplication.data.model.DemoModelDefaults
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.data.model.FamilyMember
import com.example.myapplication.data.model.MemberStatus
import com.example.myapplication.data.model.SosRouteTrace
import com.example.myapplication.data.model.SosUiState
import com.example.myapplication.data.remote.DashboardDto
import com.example.myapplication.data.remote.RemoteRepository
import com.example.myapplication.data.remote.UpdateLocationRequest
import com.example.myapplication.data.remote.SosRouteDto
import com.example.myapplication.tracking.LocationTrackingService
import com.example.myapplication.tracking.LatLng
import com.example.myapplication.tracking.TrackingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONObject

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val initialProfile = CaregiverProfile()
    private val repository = RemoteRepository.getInstance(application)
    private val trackingRepository = TrackingRepository.getInstance(application)

    private var pendingChallengeId: String? = null
    private var hasAutoOpenedDefaultHistory = false

    private val _uiState = MutableStateFlow(
        DemoUiState(
            backendConnection = checkingConnection()
        )
    )
    val uiState: StateFlow<DemoUiState> = _uiState.asStateFlow()

    init {
        observeStoredRoutes()
        loadSession()
    }

    private fun observeStoredRoutes() {
        viewModelScope.launch {
            trackingRepository.allRoutes.collect { routes ->
                _uiState.update {
                    it.copy(
                        storedRoutes = routes,
                        liveRoute = routes.lastOrNull().orEmpty()
                    )
                }
            }
        }
    }

    private fun loadSession() {
        viewModelScope.launch {
            val restoredProfile = repository.loadProfile(initialProfile)
            val savedToken = repository.loadAuthToken()
            val savedRegistrationState = repository.loadRegistrationState()

            if (savedToken.isNullOrBlank()) {
                _uiState.value = guestState(restoredProfile).copy(
                    backendConnection = checkingConnection()
                )
                checkBackendHealth(silent = true)
                return@launch
            }

            runCatching { repository.dashboard() }
                .onSuccess { dashboard ->
                    applyDashboard(dashboard)
                }
                .onFailure { error ->
                    if (error is HttpException && error.code() in listOf(401, 403)) {
                        repository.clearAuthToken()
                        repository.saveRegistrationState(false)
                        _uiState.value = guestState(restoredProfile).copy(
                            loginError = "Sessiya tugagan. Qayta kiring.",
                            backendConnection = onlineConnection(
                                detail = "Server javob berdi, lekin sessiya yaroqsiz."
                            )
                        )
                    } else {
                        updateConnectionFromError(error)
                        _uiState.value = guestState(restoredProfile).copy(
                            isLoggedIn = true,
                            isRegistered = savedRegistrationState,
                            loginError = errorMessage(error),
                            backendConnection = _uiState.value.backendConnection
                        )
                    }
                }
        }
    }

    fun resetAuthState() {
        pendingChallengeId = null
        _uiState.update {
            it.copy(
                hasExistingAccount = false,
                isSendingCode = false,
                isVerifying = false,
                isCodeVerified = false,
                otpRequested = false,
                loginError = null
            )
        }
    }

    fun requestCode(phone: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSendingCode = true,
                    isCodeVerified = false,
                    otpRequested = false,
                    loginError = null
                )
            }

            runCatching { repository.requestCode(phone) }
                .onSuccess { response ->
                    noteSuccessfulConnection("SMS xizmati tayyor.")
                    pendingChallengeId = response.challengeId
                    _uiState.update {
                        it.copy(
                            isSendingCode = false,
                            otpRequested = true,
                            hasExistingAccount = response.isRegistered,
                            otpHint = response.otpHint ?: it.otpHint,
                            loginError = null
                        )
                    }
                }
                .onFailure { error ->
                    updateConnectionFromError(error)
                    _uiState.update {
                        it.copy(
                            isSendingCode = false,
                            otpRequested = false,
                            isCodeVerified = false,
                            loginError = errorMessage(error)
                        )
                    }
                }
        }
    }

    fun verifySmsCode(phone: String, code: String) {
        val challengeId = pendingChallengeId
        if (challengeId.isNullOrBlank()) {
            _uiState.update { it.copy(loginError = "Avval SMS kod so'rang.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, loginError = null) }
            runCatching { repository.verifyCode(phone = phone, challengeId = challengeId, code = code) }
                .onSuccess {
                    noteSuccessfulConnection("Kod tasdiqlandi.")
                    _uiState.update {
                        it.copy(
                            isVerifying = false,
                            isCodeVerified = true,
                            loginError = null
                        )
                    }
                }
                .onFailure { error ->
                    updateConnectionFromError(error)
                    _uiState.update {
                        it.copy(
                            isVerifying = false,
                            isCodeVerified = false,
                            loginError = errorMessage(error)
                        )
                    }
                }
        }
    }

    fun completeRegistration(fullName: String, phone: String) {
        val challengeId = pendingChallengeId
        if (challengeId.isNullOrBlank()) {
            _uiState.update { it.copy(loginError = "Tasdiqlash sessiyasi topilmadi.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, loginError = null) }
            val existingAccount = _uiState.value.hasExistingAccount

            val request = if (existingAccount) {
                runCatching { repository.login(phone = phone, challengeId = challengeId) }
            } else {
                runCatching {
                    repository.register(
                        phone = phone,
                        fullName = fullName,
                        challengeId = challengeId
                    )
                }.recoverCatching { error ->
                    if (error is HttpException && error.code() == 400) {
                        val detail = httpErrorDetail(error)
                        if (detail.contains("allaqachon ro'yxatdan o'tgan", ignoreCase = true)) {
                            repository.login(phone = phone, challengeId = challengeId)
                        } else {
                            throw error
                        }
                    } else {
                        throw error
                    }
                }
            }

            request
                .onSuccess { response ->
                    pendingChallengeId = null
                    repository.saveAuthToken(response.token)
                    applyDashboard(response.dashboard)
                }
                .onFailure { error ->
                    updateConnectionFromError(error)
                    _uiState.update {
                        it.copy(
                            isVerifying = false,
                            loginError = errorMessage(error)
                        )
                    }
                }
        }
    }

    fun selectMember(memberId: Int) {
        val shouldReloadHistory = _uiState.value.isHistoryVisible && _uiState.value.selectedMemberId != memberId
        _uiState.update {
            it.copy(
                selectedMemberId = memberId,
                isHistoryLoading = shouldReloadHistory,
                historyMemberId = if (shouldReloadHistory) null else it.historyMemberId,
                selectedMemberHistory = if (shouldReloadHistory) emptyList() else it.selectedMemberHistory
            )
        }
        if (shouldReloadHistory) {
            loadMemberHistory(memberId)
        }
    }

    fun toggleSelectedMemberHistory() {
        val selectedMemberId = _uiState.value.selectedMemberId ?: _uiState.value.selfMember.id
        if (selectedMemberId <= 0) return

        val shouldHide = _uiState.value.isHistoryVisible && _uiState.value.historyMemberId == selectedMemberId
        if (shouldHide) {
            _uiState.update {
                it.copy(
                    isHistoryVisible = false,
                    isHistoryLoading = false,
                    historyMemberId = null,
                    selectedMemberHistory = emptyList()
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isHistoryVisible = true,
                isHistoryLoading = true,
                historyMemberId = null,
                selectedMemberHistory = emptyList()
            )
        }
        loadMemberHistory(selectedMemberId)
    }

    fun toggleSosRoutes() {
        val currentState = _uiState.value
        if (currentState.isSosRouteLoading) return

        if (currentState.isSosRouteVisible) {
            clearSosRoutes()
            return
        }

        if (currentState.activeSosAlerts.isEmpty()) {
            _uiState.update {
                it.copy(loginError = "Faol SOS signal topilmadi.")
            }
            return
        }

        loadSosRoutes()
    }

    fun refreshDemo() {
        refreshDashboard(silent = false)
    }

    fun refreshDemoSilent() {
        if (_uiState.value.isRefreshing) return
        refreshDashboard(silent = true)
    }

    fun checkBackendHealth(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _uiState.update { it.copy(backendConnection = checkingConnection()) }
            }
            runCatching { repository.healthcheck() }
                .onSuccess {
                    noteSuccessfulConnection("Server bilan aloqa bor.")
                }
                .onFailure { error ->
                    updateConnectionFromError(error)
                }
        }
    }

    suspend fun updateServerBaseUrl(rawBaseUrl: String): DemoActionResult {
        val normalizedBaseUrl = repository.updateBaseUrl(rawBaseUrl)
        _uiState.update {
            it.copy(
                backendConnection = checkingConnection(baseUrl = normalizedBaseUrl)
            )
        }

        return runCatching {
            repository.healthcheck()
            if (_uiState.value.isLoggedIn) repository.dashboard() else null
        }.fold(
            onSuccess = { dashboard ->
                if (dashboard != null) {
                    applyDashboard(dashboard)
                } else {
                    _uiState.update {
                        it.copy(
                            backendConnection = onlineConnection(
                                baseUrl = normalizedBaseUrl,
                                detail = "Yangi server manzili saqlandi."
                            )
                        )
                    }
                }
                DemoActionResult(true, "Server manzili yangilandi: $normalizedBaseUrl")
            },
            onFailure = { error ->
                updateConnectionFromError(error)
                DemoActionResult(
                    false,
                    "Server manzili saqlandi, lekin ulanib bo'lmadi: ${errorMessage(error)}"
                )
            }
        )
    }

    fun syncMyLocation(
        latitude: Double,
        longitude: Double,
        address: String,
        placeLabel: String = "Jonli joylashuv"
    ) {
        if (!_uiState.value.isLoggedIn) return

        val memberId = _uiState.value.selfMember.id
        if (memberId <= 0) {
            // selfMember ID serverdan hali kelmagan - dashboard orqali sinxronlaymiz
            viewModelScope.launch {
                _uiState.update { it.copy(lastSyncLabel = "Joylashuv yangilanmoqda...") }
                runCatching { repository.dashboard() }
                    .onSuccess { dashboard -> applyDashboard(dashboard) }
                    .onFailure { error -> updateConnectionFromError(error) }
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(lastSyncLabel = "Joylashuv yuborilmoqda...")
            }

            runCatching {
                repository.updateMemberLocation(
                    memberId = memberId,
                    payload = UpdateLocationRequest(
                        lat = latitude,
                        lon = longitude,
                        address = address,
                        placeLabel = placeLabel
                    )
                )
                repository.dashboard()
            }.onSuccess { dashboard ->
                applyDashboard(dashboard)
            }.onFailure { error ->
                updateConnectionFromError(error)
                _uiState.update { it.copy(lastSyncLabel = "Joylashuv yuborib bo'lmadi.") }
            }
        }
    }

    fun startLiveTracking() {
        if (_uiState.value.isLiveTracking) return
        viewModelScope.launch {
            val routeId = trackingRepository.newRouteId()
            _uiState.update { it.copy(isLiveTracking = true) }
            LocationTrackingService.start(getApplication(), routeId)
        }
    }

    fun stopLiveTracking() {
        if (!_uiState.value.isLiveTracking) return
        LocationTrackingService.stop(getApplication())
        _uiState.update { it.copy(isLiveTracking = false) }
    }

    suspend fun saveProfile(profile: CaregiverProfile): DemoActionResult {
        return performDashboardAction(successMessage = "Profil ma'lumotlari saqlandi.") {
            repository.saveProfile(profile)
        }
    }

    suspend fun sendInvitation(
        name: String,
        relation: String,
        phone: String
    ): DemoActionResult {
        _uiState.update { it.copy(isSendingInvitation = true) }
        return try {
            performDashboardAction(successMessage = "Taklif yuborildi.") {
                repository.sendInvitation(
                    name = name.trim(),
                    relation = relation.trim(),
                    phone = phone.trim()
                )
            }
        } finally {
            _uiState.update { it.copy(isSendingInvitation = false) }
        }
    }

    suspend fun acceptInvitation(inviteId: Int): DemoActionResult {
        return performDashboardAction(
            successMessage = "Taklif qabul qilindi va oilaga ulanish yakunlandi."
        ) {
            repository.acceptInvitation(inviteId)
        }
    }

    suspend fun dismissInvitation(inviteId: Int): DemoActionResult {
        return performDashboardAction(successMessage = "Taklif o'chirildi.") {
            repository.dismissInvitation(inviteId)
        }
    }

    fun markNotificationRead(notificationId: Int) {
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map { notification ->
                    if (notification.id == notificationId) notification.copy(isRead = true) else notification
                }
            )
        }

        viewModelScope.launch {
            runCatching { repository.markNotificationRead(notificationId) }
                .onSuccess { noteSuccessfulConnection("Bildirishnoma o'qildi.") }
                .onFailure {
                    updateConnectionFromError(it)
                    refreshDashboard(silent = true)
                }
        }
    }

    fun markAllNotificationsRead() {
        _uiState.update { state ->
            state.copy(notifications = state.notifications.map { it.copy(isRead = true) })
        }

        viewModelScope.launch {
            runCatching { repository.markAllNotificationsRead() }
                .onSuccess { noteSuccessfulConnection("Barcha bildirishnomalar o'qildi.") }
                .onFailure {
                    updateConnectionFromError(it)
                    refreshDashboard(silent = true)
                }
        }
    }

    suspend fun dismissNotification(notificationId: Int): DemoActionResult {
        return performDashboardAction(successMessage = "Bildirishnoma o'chirildi.") {
            repository.dismissNotification(notificationId)
        }
    }

    fun triggerSos() {
        val currentState = _uiState.value
        if (currentState.sosState.isSending) return

        _uiState.update {
            it.copy(
                sosState = it.sosState.copy(
                    isActive = true,
                    isSending = true,
                    summary = "SOS signal yuborilmoqda."
                ),
                lastSyncLabel = "SOS yuborilmoqda"
            )
        }

        viewModelScope.launch {
            runCatching {
                repository.triggerSos()
                repository.dashboard()
            }.onSuccess { dashboard ->
                applyDashboard(dashboard)
            }.onFailure { error ->
                updateConnectionFromError(error)
                _uiState.update {
                    it.copy(
                        sosState = SosUiState(),
                        loginError = errorMessage(error)
                    )
                }
            }
        }
    }

    fun clearSosState() {
        _uiState.update { it.copy(sosState = SosUiState()) }
    }

    fun stopSos() {
        val alertId = _uiState.value.sosState.alertId ?: return
        if (_uiState.value.sosState.isSending) return

        _uiState.update {
            it.copy(
                sosState = it.sosState.copy(
                    isSending = true,
                    summary = "SOS signal to'xtatilmoqda."
                ),
                lastSyncLabel = "SOS to'xtatilmoqda"
            )
        }

        viewModelScope.launch {
            runCatching {
                repository.resolveSos(alertId)
                repository.dashboard()
            }.onSuccess { dashboard ->
                applyDashboard(dashboard)
            }.onFailure { error ->
                updateConnectionFromError(error)
                _uiState.update {
                    it.copy(
                        sosState = it.sosState.copy(isSending = false),
                        loginError = errorMessage(error)
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            runCatching { repository.logout() }
            repository.clearAuthToken()
            repository.saveRegistrationState(false)
            val fallbackProfile = repository.loadProfile(initialProfile)
            pendingChallengeId = null
            hasAutoOpenedDefaultHistory = false
            clearSosRoutes()
            _uiState.value = guestState(fallbackProfile).copy(
                backendConnection = checkingConnection()
            )
            checkBackendHealth(silent = true)
        }
    }

    private fun loadMemberHistory(memberId: Int) {
        viewModelScope.launch {
            runCatching { repository.getMemberLocationHistory(memberId) }
                .onSuccess { response ->
                    noteSuccessfulConnection("Yo'l tarixi yuklandi.")
                    _uiState.update {
                        it.copy(
                            isHistoryVisible = true,
                            isHistoryLoading = false,
                            historyMemberId = response.memberId,
                            selectedMemberHistory = response.history
                        )
                    }
                }
                .onFailure { error ->
                    updateConnectionFromError(error)
                    _uiState.update {
                        it.copy(
                            isHistoryLoading = false,
                            isHistoryVisible = false,
                            historyMemberId = null,
                            selectedMemberHistory = emptyList(),
                            loginError = errorMessage(error)
                        )
                    }
                }
        }
    }

    private fun loadSosRoutes(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _uiState.update {
                    it.copy(
                        isSosRouteVisible = true,
                        isSosRouteLoading = true,
                        loginError = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSosRouteVisible = true,
                        isSosRouteLoading = true
                    )
                }
            }

            runCatching { repository.getSosRoutes() }
                .onSuccess { response ->
                    noteSuccessfulConnection("SOS marshruti tayyor.")
                    _uiState.update {
                        it.copy(
                            isSosRouteVisible = response.routes.isNotEmpty(),
                            isSosRouteLoading = false,
                            sosRoutes = response.routes.map { dto -> dto.toModel() }
                        )
                    }
                }
                .onFailure { error ->
                    updateConnectionFromError(error)
                    _uiState.update {
                        it.copy(
                            isSosRouteVisible = false,
                            isSosRouteLoading = false,
                            sosRoutes = emptyList(),
                            loginError = errorMessage(error)
                        )
                    }
                }
        }
    }

    private fun clearSosRoutes() {
        _uiState.update {
            it.copy(
                isSosRouteVisible = false,
                isSosRouteLoading = false,
                sosRoutes = emptyList()
            )
        }
    }

    private fun SosRouteDto.toModel(): SosRouteTrace {
        return SosRouteTrace(
            alertId = alertId,
            memberId = memberId,
            memberName = memberName,
            relation = relation,
            phone = phone,
            sourceMemberId = sourceMemberId,
            sourceLat = sourceLat,
            sourceLon = sourceLon,
            targetLat = targetLat,
            targetLon = targetLon,
            dijkstraRoute = dijkstraPoints.map { point -> LatLng(point.lat, point.lon) },
            astarRoute = astarPoints.map { point -> LatLng(point.lat, point.lon) },
            dijkstraLengthMeters = dijkstraLengthMeters,
            astarLengthMeters = astarLengthMeters,
            isSamePath = isSamePath,
            graphNodes = graphNodes,
            graphEdges = graphEdges
        )
    }

    private fun refreshDashboard(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _uiState.update { it.copy(isRefreshing = true) }
            }
            runCatching { repository.dashboard() }
                .onSuccess { dashboard ->
                    applyDashboard(dashboard)
                }
                .onFailure { error ->
                    updateConnectionFromError(error)
                    if (!silent) {
                        _uiState.update {
                            it.copy(
                                isRefreshing = false,
                                loginError = errorMessage(error)
                            )
                        }
                    }
                }
        }
    }

    private fun applyDashboard(dashboard: DashboardDto) {
        val resolvedProfile = dashboard.profile
        val resolvedSelfMember = dashboard.selfMember
            ?: selfMemberFrom(resolvedProfile, _uiState.value.selfMember.takeIf { it.id > 0 })
        val resolvedSelectedMemberId = dashboard.selectedMemberId ?: resolvedSelfMember.id
        val shouldAutoOpenDefaultHistory = !hasAutoOpenedDefaultHistory &&
                resolvedSelectedMemberId > 0 &&
                !_uiState.value.isHistoryVisible
        val shouldRefreshSosRoutes = _uiState.value.isSosRouteVisible && dashboard.activeSosAlerts.isNotEmpty()

        pendingChallengeId = null
        repository.saveCachedProfile(resolvedProfile)
        repository.saveRegistrationState(dashboard.isRegistered)

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isLoggedIn = true,
            isRegistered = dashboard.isRegistered,
            hasExistingAccount = false,
            isSendingCode = false,
            isVerifying = false,
            isCodeVerified = false,
            isRefreshing = false,
            isSendingInvitation = false,
            otpRequested = false,
            loginError = null,
            caregiverName = dashboard.caregiverName,
            familyLabel = dashboard.familyLabel,
            lastSyncLabel = dashboard.lastSyncLabel,
            profile = resolvedProfile,
            selfMember = resolvedSelfMember,
            members = dashboard.members,
            invitations = dashboard.invitations,
            incomingInvitations = dashboard.incomingInvitations,
            notifications = dashboard.notifications,
            activityFeed = dashboard.activityFeed,
            selectedMemberId = resolvedSelectedMemberId,
            nextCheckInLabel = dashboard.nextCheckInLabel,
            trustedPlacesCount = dashboard.trustedPlacesCount,
            activeSosAlerts = dashboard.activeSosAlerts,
            sosState = dashboard.sosState,
            backendConnection = onlineConnection(
                detail = "Server bilan sinxronlangan."
            )
        )

        if (dashboard.activeSosAlerts.isEmpty()) {
            clearSosRoutes()
        } else if (shouldRefreshSosRoutes) {
            loadSosRoutes(silent = true)
        }

        if (shouldAutoOpenDefaultHistory) {
            hasAutoOpenedDefaultHistory = true
            _uiState.update {
                it.copy(
                    isHistoryVisible = true,
                    isHistoryLoading = true,
                    historyMemberId = null,
                    selectedMemberHistory = emptyList()
                )
            }
            loadMemberHistory(resolvedSelectedMemberId)
        }
    }

    private fun guestState(restoredProfile: CaregiverProfile): DemoUiState {
        return DemoUiState(
            isLoading = false,
            isLoggedIn = false,
            isRegistered = false,
            caregiverName = displayName(restoredProfile.fullName).ifBlank {
                DemoModelDefaults.defaultCaregiverName
            },
            familyLabel = restoredProfile.familyLabel,
            profile = restoredProfile,
            selfMember = selfMemberFrom(restoredProfile),
            members = emptyList(),
            invitations = emptyList(),
            incomingInvitations = emptyList(),
            notifications = emptyList(),
            activityFeed = emptyList(),
            selectedMemberId = 0,
            activeSosAlerts = emptyList(),
            sosState = SosUiState(),
            isSosRouteVisible = false,
            isSosRouteLoading = false,
            sosRoutes = emptyList(),
            backendConnection = checkingConnection()
        )
    }

    private fun selfMemberFrom(profile: CaregiverProfile, previous: FamilyMember? = null): FamilyMember {
        return FamilyMember(
            id = previous?.id ?: 0,
            name = profile.fullName,
            relation = DemoModelDefaults.defaultRelationSelf,
            age = previous?.age ?: 31,
            lat = previous?.lat ?: DemoModelDefaults.fallbackLatitude,
            lon = previous?.lon ?: DemoModelDefaults.fallbackLongitude,
            address = previous?.address ?: profile.address,
            placeLabel = previous?.placeLabel ?: DemoModelDefaults.defaultPlaceLabel,
            battery = previous?.battery ?: 92,
            steps = previous?.steps ?: 4680,
            heartRate = previous?.heartRate ?: 76,
            lastUpdate = previous?.lastUpdate ?: DemoModelDefaults.defaultSyncLabel,
            status = previous?.status ?: MemberStatus.SAFE,
            note = previous?.note ?: DemoModelDefaults.defaultDeviceNote,
            safeZone = previous?.safeZone ?: DemoModelDefaults.defaultSafeZone,
            phone = profile.phone,
            schedule = previous?.schedule ?: DemoModelDefaults.defaultSchedule,
            avatarSeed = profile.avatarSeed,
            avatarUri = profile.avatarUri,
            distanceKm = previous?.distanceKm ?: 3.6,
            isCurrentUser = true,
            isOnline = previous?.isOnline ?: true,
            presenceLabel = previous?.presenceLabel ?: DemoModelDefaults.defaultPresenceOnline,
            lastSeenAt = previous?.lastSeenAt
        )
    }

    private fun displayName(fullName: String): String =
        fullName.trim().substringBefore(" ").ifBlank { fullName.trim() }

    private fun errorMessage(error: Throwable): String {
        return when (error) {
            is HttpException -> {
                httpErrorDetail(error)
                    .takeIf { it.isNotBlank() }
                    ?: "Server xatosi: ${error.code()}"
            }

            is IOException -> "Server bilan aloqa qilib bo'lmadi. Backend ishlayotganini tekshiring."
            else -> error.message ?: "Noma'lum xatolik."
        }
    }

    private fun httpErrorDetail(error: HttpException): String {
        val rawBody = runCatching {
            error.response()?.errorBody()?.string().orEmpty()
        }.getOrDefault("")
        if (rawBody.isBlank()) return ""

        return runCatching {
            JSONObject(rawBody).optString("detail")
        }.getOrDefault("").ifBlank {
            Regex("\"detail\"\\s*:\\s*\"([^\"]+)\"")
                .find(rawBody)
                ?.groupValues
                ?.getOrNull(1)
                ?.replace("\\u0027", "'")
                .orEmpty()
        }
    }

    private fun updateConnectionFromError(error: Throwable) {
        when (error) {
            is IOException -> {
                _uiState.update {
                    it.copy(
                        backendConnection = offlineConnection(
                            detail = "Serverga ulanib bo'lmadi."
                        )
                    )
                }
            }

            is HttpException -> {
                _uiState.update {
                    it.copy(
                        backendConnection = onlineConnection(
                            detail = "Server javob berdi (${error.code()})."
                        )
                    )
                }
            }

            else -> {
                _uiState.update {
                    it.copy(
                        backendConnection = offlineConnection(
                            detail = "Server holatini tekshirish kerak."
                        )
                    )
                }
            }
        }
    }

    private fun noteSuccessfulConnection(detail: String) {
        _uiState.update {
            it.copy(
                backendConnection = onlineConnection(detail = detail)
            )
        }
    }

    // Action bajaradi; muvaffaqiyatli bo'lsa dashboard yangilanadi.
    // Action muvaffaqiyatli bo'lsa - dashboard yuklashdan qat'i nazar success qaytariladi.
    private suspend fun performDashboardAction(
        successMessage: String,
        action: suspend () -> Unit
    ): DemoActionResult {
        if (!_uiState.value.isLoggedIn || repository.loadAuthToken().isNullOrBlank()) {
            return DemoActionResult(false, "Avval ro'yxatdan o'ting yoki ilovaga kiring.")
        }

        val actionResult = runCatching { action() }
        if (actionResult.isFailure) {
            val error = actionResult.exceptionOrNull()!!
            updateConnectionFromError(error)
            return DemoActionResult(false, errorMessage(error))
        }

        // Action muvaffaqiyatli - dashboard yangilaymiz
        runCatching { repository.dashboard() }
            .onSuccess { applyDashboard(it) }
            .onFailure { error ->
                updateConnectionFromError(error)
                // Fonda qayta urinib ko'ramiz
                viewModelScope.launch { refreshDashboard(silent = true) }
            }

        return DemoActionResult(true, successMessage)
    }

    private fun checkingConnection(baseUrl: String = repository.currentBaseUrl()): BackendConnection {
        return BackendConnection(
            status = BackendStatus.CHECKING,
            label = "Tekshirilmoqda",
            detail = "Server holati aniqlanmoqda.",
            baseUrl = baseUrl,
            checkedAtLabel = currentClockLabel()
        )
    }

    private fun onlineConnection(
        baseUrl: String = repository.currentBaseUrl(),
        detail: String
    ): BackendConnection {
        return BackendConnection(
            status = BackendStatus.ONLINE,
            label = "Online",
            detail = detail,
            baseUrl = baseUrl,
            checkedAtLabel = currentClockLabel()
        )
    }

    private fun offlineConnection(
        baseUrl: String = repository.currentBaseUrl(),
        detail: String
    ): BackendConnection {
        return BackendConnection(
            status = BackendStatus.OFFLINE,
            label = "Offline",
            detail = detail,
            baseUrl = baseUrl,
            checkedAtLabel = currentClockLabel()
        )
    }

    private fun currentClockLabel(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
}
