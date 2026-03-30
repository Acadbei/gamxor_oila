package com.example.myapplication.ui.screen

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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.CaregiverProfile
import com.example.myapplication.data.model.DemoActionResult
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.data.model.SosContactState
import com.example.myapplication.ui.component.SymbolChip
import com.example.myapplication.ui.theme.GlowRose
import com.example.myapplication.ui.theme.GlowSand
import com.example.myapplication.ui.theme.GlowSky
import com.example.myapplication.ui.theme.screen.FamilyMapScreen
import com.example.myapplication.viewmodel.LocationViewModel
import kotlinx.coroutines.launch

private enum class DemoTab(
    val label: String,
    val icon: ImageVector
) {
    Home("Uy", Icons.Default.Home),
    Map("Xarita", Icons.Default.LocationOn),
    Notifications("Bildirish", Icons.Default.Notifications),
    Profile("Profil", Icons.Default.Person)
}

@Composable
fun FamilyCareApp(
    viewModel: LocationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> DemoBootScreen()
        !uiState.isLoggedIn -> DemoLoginScreen(
            uiState = uiState,
            onRequestCode = viewModel::requestCode,
            onLogin = viewModel::login,
            onQuickDemoLogin = viewModel::quickDemoLogin
        )
        else -> FamilyCareShell(
            uiState = uiState,
            onRefresh = viewModel::refreshDemo,
            onSelectMember = viewModel::selectMember,
            onSignOut = viewModel::signOut,
            onSaveProfile = viewModel::saveProfile,
            onSendInvitation = viewModel::sendInvitation,
            onAcceptInvitation = viewModel::acceptInvitation,
            onMarkNotificationRead = viewModel::markNotificationRead,
            onMarkAllNotificationsRead = viewModel::markAllNotificationsRead,
            onTriggerSos = viewModel::triggerSos,
            onDismissSos = viewModel::clearSosState
        )
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
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.88f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.52f)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Surface(
                modifier = Modifier.size(92.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.16f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Text(
                text = "Family Care",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Live sync • SOS • Invite",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.84f)
            )

            CircularProgressIndicator(
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.24f)
            )
        }
    }
}

@Composable
private fun FamilyCareShell(
    uiState: DemoUiState,
    onRefresh: () -> Unit,
    onSelectMember: (Int) -> Unit,
    onSignOut: () -> Unit,
    onSaveProfile: (CaregiverProfile) -> DemoActionResult,
    onSendInvitation: (String, String, String) -> DemoActionResult,
    onAcceptInvitation: (Int) -> DemoActionResult,
    onMarkNotificationRead: (Int) -> Unit,
    onMarkAllNotificationsRead: () -> Unit,
    onTriggerSos: () -> Unit,
    onDismissSos: () -> Unit
) {
    val navigationTabs = listOf(DemoTab.Home, DemoTab.Map, DemoTab.Profile)
    var selectedTab by rememberSaveable { mutableStateOf(DemoTab.Home) }
    var showSosConfirm by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val unreadNotificationsCount = remember(uiState.notifications) {
        uiState.notifications.count { !it.isRead }
    }

    val showMessage: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
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
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                ShellTopBar(
                    selectedTab = selectedTab,
                    familyLabel = uiState.familyLabel,
                    lastSyncLabel = uiState.lastSyncLabel,
                    unreadNotificationsCount = unreadNotificationsCount,
                    onOpenNotifications = { selectedTab = DemoTab.Notifications }
                )
            },
            floatingActionButton = {
                Surface(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 10.dp)
                        .clickable(onClick = { showSosConfirm = true }),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFD83F2C),
                    tonalElevation = 2.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
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
                        shape = RoundedCornerShape(26.dp),
                        color = Color.White.copy(alpha = 0.94f),
                        tonalElevation = 2.dp,
                        shadowElevation = 8.dp
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
                        onRefresh = onRefresh,
                        onSelectMember = onSelectMember,
                        onOpenMap = { memberId ->
                            memberId?.let(onSelectMember)
                            selectedTab = DemoTab.Map
                        },
                        onSendInvitation = onSendInvitation,
                        onPrimaryAction = showMessage
                    )

                    DemoTab.Map -> FamilyMapScreen(
                        modifier = Modifier.padding(padding),
                        uiState = uiState,
                        onSelectMember = onSelectMember,
                        onAction = showMessage
                    )

                    DemoTab.Notifications -> NotificationsScreen(
                        modifier = Modifier.padding(padding),
                        uiState = uiState,
                        onAcceptInvite = onAcceptInvitation,
                        onMarkNotificationRead = onMarkNotificationRead,
                        onMarkAllNotificationsRead = onMarkAllNotificationsRead,
                        onAction = showMessage
                    )

                    DemoTab.Profile -> ProfileScreen(
                        modifier = Modifier.padding(padding),
                        uiState = uiState,
                        onRefresh = onRefresh,
                        onSignOut = onSignOut,
                        onSaveProfile = onSaveProfile,
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
            text = {
                Text("Signal bosilishi bilan barcha oila a'zolariga demo qo'ng'iroq va favqulodda bildirish ketadi.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSosConfirm = false
                        onTriggerSos()
                    }
                ) {
                    Text("Yuborish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSosConfirm = false }) {
                    Text("Bekor")
                }
            }
        )
    }

    if (uiState.sosState.isActive) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.sosState.isSending) {
                    onDismissSos()
                }
            },
            title = { Text("SOS markazi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(uiState.sosState.summary)
                    uiState.sosState.contacts.forEach { contact ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
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
                                    Icons.Default.Notifications
                                },
                                label = if (contact.state == SosContactState.CALLING) {
                                    "Qo'ng'iroq"
                                } else {
                                    "Yuborildi"
                                },
                                accent = if (contact.state == SosContactState.CALLING) {
                                    GlowSand
                                } else {
                                    GlowSky
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (!uiState.sosState.isSending) onDismissSos()
                    }
                ) {
                    Text(if (uiState.sosState.isSending) "Jarayon..." else "Yopish")
                }
            }
        )
    }
}

@Composable
private fun ShellTopBar(
    selectedTab: DemoTab,
    familyLabel: String,
    lastSyncLabel: String,
    unreadNotificationsCount: Int,
    onOpenNotifications: () -> Unit
) {
    val (title, subtitle, accent) = when (selectedTab) {
        DemoTab.Home -> Triple("Pulse", familyLabel, GlowSky)
        DemoTab.Map -> Triple("Live Map", "Markerlar jonli", GlowSand)
        DemoTab.Notifications -> Triple("Inbox", "Taklif va signal", GlowRose)
        DemoTab.Profile -> Triple("Profile", "Ruxsat va sozlama", GlowSky)
    }

    Surface(color = Color.Transparent) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.9f),
                tonalElevation = 2.dp,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = accent
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = selectedTab.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SymbolChip(
                            icon = Icons.Default.LocationOn,
                            label = lastSyncLabel,
                            accent = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = onOpenNotifications) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotificationsCount > 0) {
                                        Badge {
                                            Text(unreadNotificationsCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Bildirishnomalar",
                                    tint = if (selectedTab == DemoTab.Notifications) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }
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
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        } else {
            Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
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
                        tint = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            Text(
                text = label,
                modifier = Modifier.padding(start = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
