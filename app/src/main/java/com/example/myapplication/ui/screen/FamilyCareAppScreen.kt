package com.example.myapplication.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Person
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
import com.example.myapplication.data.model.CaregiverProfile
import com.example.myapplication.data.model.DemoActionResult
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.data.model.SosContactState
import com.example.myapplication.ui.component.SymbolChip
import com.example.myapplication.ui.theme.GlowMint
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
    Profile("Profil", Icons.Default.Person)
}

@Composable
fun FamilyCareApp(
    viewModel: LocationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRegistrationFlow by rememberSaveable { mutableStateOf(false) }

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
                        val result = viewModel.completeRegistration(fullName = fullName, phone = phone)
                        if (result.success) {
                            showRegistrationFlow = false
                        }
                        result
                    },
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
                    onDismissNotification = viewModel::dismissNotification,
                    onStartRegistration = {
                        viewModel.resetAuthState()
                        showRegistrationFlow = true
                    },
                    onTriggerSos = viewModel::triggerSos,
                    onDismissSos = viewModel::clearSosState
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
    onSaveProfile: (CaregiverProfile) -> DemoActionResult,
    onSendInvitation: (String, String, String) -> DemoActionResult,
    onDismissNotification: (Int) -> DemoActionResult,
    onStartRegistration: () -> Unit,
    onTriggerSos: () -> Unit,
    onDismissSos: () -> Unit
) {
    val navigationTabs = listOf(DemoTab.Home, DemoTab.Map, DemoTab.Profile)
    var selectedTab by rememberSaveable { mutableStateOf(DemoTab.Map) }
    var showSosConfirm by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val showMessage: (String) -> Unit = { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
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
                if (selectedTab == DemoTab.Map && !uiState.isRegistered) {
                    MapTopBar(
                        uiState = uiState,
                        onRegister = onStartRegistration
                    )
                } else if (selectedTab != DemoTab.Map) {
                    ShellTopBar(
                        selectedTab = selectedTab,
                        uiState = uiState,
                        onRegister = onStartRegistration
                    )
                }
            },
            floatingActionButton = {
                Surface(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 10.dp)
                        .clickable(onClick = { showSosConfirm = true }),
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
                            handleResult(onDismissNotification(notificationId))
                        }
                    )

                    DemoTab.Map -> FamilyMapScreen(
                        modifier = Modifier.padding(padding),
                        uiState = uiState,
                        onSelectMember = onSelectMember
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
                        onTriggerSos()
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
                TextButton(onClick = { if (!uiState.sosState.isSending) onDismissSos() }) {
                    Text(if (uiState.sosState.isSending) "Jarayon..." else "Yopish")
                }
            }
        )
    }
}

@Composable
private fun MapTopBar(
    uiState: DemoUiState,
    onRegister: () -> Unit
) {
    Surface(color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!uiState.isRegistered) {
                Button(onClick = onRegister, shape = RoundedCornerShape(18.dp)) {
                    Text("Ro'yxatdan o'tish")
                }
            }
        }
    }
}

@Composable
private fun ShellTopBar(
    selectedTab: DemoTab,
    uiState: DemoUiState,
    onRegister: () -> Unit
) {
    val (title, subtitle, accent) = when (selectedTab) {
        DemoTab.Home -> Triple("Uy", "Masofa, oila va SOS holati", GlowSky)
        DemoTab.Map -> Triple("Xarita", "Jonli joylashuv va oilaviy markerlar", GlowSand)
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
