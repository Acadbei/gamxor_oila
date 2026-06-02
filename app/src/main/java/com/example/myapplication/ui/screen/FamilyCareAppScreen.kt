package com.example.myapplication.ui.screen

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.BackendStatus
import com.example.myapplication.data.model.CaregiverProfile
import com.example.myapplication.data.model.DemoActionResult
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.data.model.SosContactState
import com.example.myapplication.location.fetchCurrentDeviceLocation
import com.example.myapplication.location.hasLocationPermissions
import com.example.myapplication.tracking.batteryOptimizationIntent
import com.example.myapplication.ui.component.SymbolChip
import com.example.myapplication.ui.theme.GlowMint
import com.example.myapplication.ui.theme.GlowRose
import com.example.myapplication.ui.theme.GlowSand
import com.example.myapplication.ui.theme.GlowSky
import com.example.myapplication.ui.theme.screen.FamilyMapScreen
import com.example.myapplication.viewmodel.LocationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class DemoTab(
    val label: String,
    val icon: ImageVector
) {
    Home("Uy", Icons.Default.Home),
    Map("Xarita", Icons.Default.LocationOn),
    Notifications("Inbox", Icons.Default.NotificationsActive),
    Profile("Profil", Icons.Default.Person)
}

@Composable
fun FamilyCareApp(
    viewModel: LocationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRegistrationFlow by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grantedPermissions ->
        if (grantedPermissions.values.any { it }) {
            fetchCurrentDeviceLocation(context) { snapshot ->
                snapshot?.let {
                    viewModel.syncMyLocation(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        address = it.address,
                        placeLabel = it.placeLabel
                    )
                }
            }
        }
    }
    val trackingPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grantedPermissions ->
        if (grantedPermissions.values.any { it }) {
            batteryOptimizationIntent(context)?.let { intent ->
                runCatching { context.startActivity(intent) }
            }
            viewModel.startLiveTracking()
        }
    }

    fun syncCurrentLocation() {
        if (hasLocationPermissions(context)) {
            fetchCurrentDeviceLocation(context) { snapshot ->
                snapshot?.let {
                    viewModel.syncMyLocation(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        address = it.address,
                        placeLabel = it.placeLabel
                    )
                }
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    fun startLiveTracking() {
        if (hasLocationPermissions(context)) {
            batteryOptimizationIntent(context)?.let { intent ->
                runCatching { context.startActivity(intent) }
            }
            viewModel.startLiveTracking()
        } else {
            val permissions = buildList {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
                add(Manifest.permission.ACCESS_COARSE_LOCATION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }.toTypedArray()
            trackingPermissionLauncher.launch(permissions)
        }
    }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            showRegistrationFlow = false
        }
        if (!uiState.isLoggedIn) {
            showRegistrationFlow = false
        }
    }

    LaunchedEffect(uiState.backendConnection.baseUrl) {
        while (true) {
            delay(30_000)
            // Foydalanuvchi tizimda bo'lsa - to'liq dashboard yangilaymiz
            // (oila a'zolari qo'shilishi/joylashuv o'zgarishini ko'rsin)
            // Aks holda - faqat server holatini tekshiramiz
            if (viewModel.uiState.value.isLoggedIn) {
                viewModel.refreshDemoSilent()
            } else {
                viewModel.checkBackendHealth(silent = true)
            }
        }
    }

    LaunchedEffect(uiState.isLoggedIn, uiState.isLiveTracking) {
        if (uiState.isLoggedIn && !uiState.isLiveTracking) {
            startLiveTracking()
        }
    }

    if (uiState.isLoading) {
        DemoBootScreen()
    } else {
        BackHandler(enabled = showRegistrationFlow) {
            viewModel.resetAuthState()
            showRegistrationFlow = false
        }

        Crossfade(targetState = showRegistrationFlow, label = "app-root") { registrationVisible ->
            if (registrationVisible) {
                DemoLoginScreen(
                    uiState = uiState,
                    onRequestCode = viewModel::requestCode,
                    onVerifyCode = viewModel::verifySmsCode,
                    onCompleteRegistration = { phone, fullName ->
                        viewModel.completeRegistration(fullName = fullName, phone = phone)
                    },
                    onUpdateServerUrl = viewModel::updateServerBaseUrl,
                    onCheckBackend = { viewModel.checkBackendHealth() },
                    onBack = {
                        viewModel.resetAuthState()
                        showRegistrationFlow = false
                    }
                )
            } else {
                FamilyCareShell(
                    uiState = uiState,
                    onSelectMember = viewModel::selectMember,
                    onSignOut = viewModel::signOut,
                    onSaveProfile = viewModel::saveProfile,
                    onSendInvitation = viewModel::sendInvitation,
                    onAcceptInvitation = viewModel::acceptInvitation,
                    onDismissInvitation = viewModel::dismissInvitation,
                    onMarkNotificationRead = viewModel::markNotificationRead,
                    onMarkAllNotificationsRead = viewModel::markAllNotificationsRead,
                    onDismissNotification = viewModel::dismissNotification,
                    onStartRegistration = {
                        viewModel.resetAuthState()
                        showRegistrationFlow = true
                    },
                    onTriggerSos = viewModel::triggerSos,
                    onDismissSos = viewModel::clearSosState,
                    onStopSos = viewModel::stopSos,
                    onToggleHistory = viewModel::toggleSelectedMemberHistory,
                    onToggleSosRoutes = viewModel::toggleSosRoutes,
                    onUpdateServerUrl = viewModel::updateServerBaseUrl,
                    onCheckBackend = { viewModel.checkBackendHealth() },
                    onSyncCurrentLocation = ::syncCurrentLocation
                )
            }
        }
    }
}

@Composable
private fun DemoBootScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Surface(
                modifier = Modifier.size(96.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.18f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Text(
                text = "Oilaviy parvarish",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Oilaviy xarita, SOS va profil bir joyda",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.88f)
            )
            CircularProgressIndicator(
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.25f)
            )
        }
    }
}

@Composable
private fun FamilyCareShell(
    uiState: DemoUiState,
    onSelectMember: (Int) -> Unit,
    onSignOut: () -> Unit,
    onSaveProfile: suspend (CaregiverProfile) -> DemoActionResult,
    onSendInvitation: suspend (String, String, String) -> DemoActionResult,
    onAcceptInvitation: suspend (Int) -> DemoActionResult,
    onDismissInvitation: suspend (Int) -> DemoActionResult,
    onMarkNotificationRead: (Int) -> Unit,
    onMarkAllNotificationsRead: () -> Unit,
    onDismissNotification: suspend (Int) -> DemoActionResult,
    onStartRegistration: () -> Unit,
    onTriggerSos: () -> Unit,
    onDismissSos: () -> Unit,
    onStopSos: () -> Unit,
    onToggleHistory: () -> Unit,
    onToggleSosRoutes: () -> Unit,
    onUpdateServerUrl: suspend (String) -> DemoActionResult,
    onCheckBackend: () -> Unit,
    onSyncCurrentLocation: () -> Unit
) {
    val navigationTabs = listOf(DemoTab.Home, DemoTab.Map, DemoTab.Notifications, DemoTab.Profile)
    var selectedTab by rememberSaveable { mutableStateOf(DemoTab.Map) }
    var showSosConfirm by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val showMessage: (String) -> Unit = { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    fun requireAuthenticatedAction(): Boolean {
        if (uiState.isLoggedIn) return true
        showMessage("Ma'lumot kiritish uchun avval ro'yxatdan o'ting yoki ilovaga kiring.")
        onStartRegistration()
        return false
    }

    val handleResult: (DemoActionResult) -> Unit = { result ->
        showMessage(result.message)
    }

    val launchDialer: (String) -> Unit = { phone ->
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${dialablePhoneNumber(phone)}"))
        runCatching { context.startActivity(dialIntent) }
            .onFailure { showMessage("Telefon ilovasini ochib bo'lmadi.") }
    }

    LaunchedEffect(uiState.sosState.isActive, uiState.sosState.isSending, uiState.sosState.summary) {
        if (uiState.sosState.isActive && !uiState.sosState.isSending && uiState.sosState.summary.isNotBlank()) {
            snackbarHostState.showSnackbar(uiState.sosState.summary)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        Color.White,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                if (selectedTab == DemoTab.Map) {
                    MapTopBar(
                        uiState = uiState,
                        onRegister = onStartRegistration,
                        onRefreshServer = onCheckBackend,
                        onSyncLocation = {
                            if (requireAuthenticatedAction()) onSyncCurrentLocation()
                        }
                    )
                } else {
                    ShellTopBar(
                        selectedTab = selectedTab,
                        uiState = uiState,
                        onRegister = onStartRegistration,
                        onRefreshServer = onCheckBackend
                    )
                }
            },
            floatingActionButton = {
                Surface(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 10.dp)
                        .clickable(onClick = {
                            if (requireAuthenticatedAction()) showSosConfirm = true
                        }),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.error,
                    tonalElevation = 3.dp,
                    shadowElevation = 10.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "SOS",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = Color.White.copy(alpha = 0.96f),
                        tonalElevation = 2.dp,
                        shadowElevation = 10.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            navigationTabs.forEach { tab ->
                                CompactNavItem(
                                    modifier = Modifier.weight(1f),
                                    selected = selectedTab == tab,
                                    onClick = { selectedTab = tab },
                                    icon = tab.icon,
                                    label = tab.label
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Crossfade(
                targetState = selectedTab,
                modifier = Modifier.fillMaxSize(),
                label = "family-tabs"
            ) { tab ->
                when (tab) {
                    DemoTab.Home -> HomeScreen(
                        modifier = Modifier.padding(padding),
                        uiState = uiState,
                        onSelectMember = onSelectMember,
                        onCall = launchDialer,
                        onDismissCrimeAlert = { notificationId ->
                            scope.launch {
                                handleResult(onDismissNotification(notificationId))
                            }
                        }
                    )

                    DemoTab.Map -> FamilyMapScreen(
                        modifier = Modifier.padding(padding),
                        uiState = uiState,
                        onSelectMember = onSelectMember,
                        onToggleHistory = {
                            if (requireAuthenticatedAction()) onToggleHistory()
                        },
                        onToggleSosRoutes = {
                            if (requireAuthenticatedAction()) onToggleSosRoutes()
                        }
                    )

                    DemoTab.Notifications -> NotificationsScreen(
                        modifier = Modifier.padding(padding),
                        uiState = uiState,
                        onAcceptInvite = onAcceptInvitation,
                        onDismissInvite = onDismissInvitation,
                        onMarkNotificationRead = onMarkNotificationRead,
                        onMarkAllNotificationsRead = onMarkAllNotificationsRead,
                        onDismissNotification = onDismissNotification,
                        onAction = showMessage
                    )

                    DemoTab.Profile -> ProfileScreen(
                        modifier = Modifier.padding(padding),
                        uiState = uiState,
                        onSignOut = {
                            onSignOut()
                            showMessage("Profil ro'yxatdan chiqarildi.")
                        },
                        onSaveProfile = onSaveProfile,
                        onSendInvitation = onSendInvitation,
                        onRegister = onStartRegistration,
                        onUpdateServerUrl = onUpdateServerUrl,
                        onCheckBackend = onCheckBackend,
                        onSyncCurrentLocation = onSyncCurrentLocation,
                        onAction = showMessage
                    )
                }
            }
        }
    }

    if (showSosConfirm) {
        AlertDialog(
            onDismissRequest = { showSosConfirm = false },
            title = { Text("SOS yuborilsinmi?") },
            text = { Text("Signal bosilishi bilan barcha oila a'zolariga ogohlantiruvchi xabar yuboriladi.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSosConfirm = false
                        if (requireAuthenticatedAction()) onTriggerSos()
                    }
                ) {
                    Text("Yuborish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSosConfirm = false }) {
                    Text("Bekor qilish")
                }
            }
        )
    }

    if (uiState.sosState.isActive) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.sosState.isSending) onDismissSos()
            },
            title = { Text("SOS markazi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(uiState.sosState.summary)
                    uiState.sosState.contacts.forEach { contact ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(contact.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = contact.phone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            SymbolChip(
                                icon = if (contact.state == SosContactState.CALLING) {
                                    Icons.Default.WarningAmber
                                } else {
                                    Icons.Default.Person
                                },
                                label = if (contact.state == SosContactState.CALLING) "Yuborilmoqda" else "Yuborildi",
                                accent = if (contact.state == SosContactState.CALLING) GlowRose else GlowMint
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !uiState.sosState.isSending && uiState.sosState.alertId != null,
                    onClick = { onStopSos() }
                ) {
                    Text(if (uiState.sosState.isSending) "Jarayon..." else "To'xtatish")
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!uiState.sosState.isSending) onDismissSos() }) {
                    Text("Yopish")
                }
            }
        )
    }
}

@Composable
private fun MapTopBar(
    uiState: DemoUiState,
    onRegister: () -> Unit,
    onRefreshServer: () -> Unit,
    onSyncLocation: () -> Unit
) {
    Surface(color = Color.Transparent) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackendStatusChip(
                    status = uiState.backendConnection.status,
                    label = uiState.backendConnection.label,
                    onClick = onRefreshServer
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onSyncLocation) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Text("Sync")
                    }

                    if (!uiState.isRegistered) {
                        Button(onClick = onRegister, shape = RoundedCornerShape(18.dp)) {
                            Text("Ro'yxatdan o'tish")
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.92f),
                tonalElevation = 2.dp,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Xarita sinxronizatsiyasi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = uiState.backendConnection.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Server: ${uiState.backendConnection.baseUrl}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ShellTopBar(
    selectedTab: DemoTab,
    uiState: DemoUiState,
    onRegister: () -> Unit,
    onRefreshServer: () -> Unit
) {
    val (title, subtitle, accent) = when (selectedTab) {
        DemoTab.Home -> Triple("Uy", "Masofa, oila va SOS holati", GlowSky)
        DemoTab.Map -> Triple("Xarita", "Jonli joylashuv va oilaviy markerlar", GlowSand)
        DemoTab.Notifications -> Triple("Inbox", "Invite, signal va system oqimi", GlowRose)
        DemoTab.Profile -> Triple("Profil", "Shaxsiy ma'lumotlar va oila qo'shish", GlowSky)
    }

    Surface(color = Color.Transparent) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.95f),
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!uiState.isRegistered) {
                            Button(onClick = onRegister, shape = RoundedCornerShape(18.dp)) {
                                Text("Ro'yxatdan o'tish")
                            }
                        } else {
                            SymbolChip(
                                icon = Icons.Default.Person,
                                label = "Aktiv profil",
                                accent = GlowMint
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SymbolChip(
                            modifier = Modifier.weight(1f),
                            icon = selectedTab.icon,
                            label = uiState.familyLabel,
                            accent = accent
                        )
                        SymbolChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.LocationOn,
                            label = uiState.lastSyncLabel,
                            accent = GlowSky
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BackendStatusChip(
                            modifier = Modifier.weight(1f),
                            status = uiState.backendConnection.status,
                            label = uiState.backendConnection.label,
                            onClick = onRefreshServer
                        )
                        SymbolChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Sync,
                            label = uiState.backendConnection.checkedAtLabel,
                            accent = GlowSand
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackendStatusChip(
    modifier: Modifier = Modifier,
    status: BackendStatus,
    label: String,
    onClick: () -> Unit
) {
    val icon = when (status) {
        BackendStatus.ONLINE -> Icons.Default.CloudDone
        BackendStatus.CHECKING -> Icons.Default.Sync
        BackendStatus.OFFLINE -> Icons.Default.CloudOff
    }
    val accent = when (status) {
        BackendStatus.ONLINE -> GlowMint
        BackendStatus.CHECKING -> GlowSand
        BackendStatus.OFFLINE -> GlowRose
    }

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = accent.copy(alpha = 0.22f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CompactNavItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.11f)
        } else {
            Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                } else {
                    Color.Transparent
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = label,
                modifier = Modifier.padding(start = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
