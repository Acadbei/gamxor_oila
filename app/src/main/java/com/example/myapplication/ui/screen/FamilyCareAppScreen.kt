package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.data.model.SosContactState
import com.example.myapplication.ui.theme.screen.FamilyMapScreen
import com.example.myapplication.viewmodel.LocationViewModel
import kotlinx.coroutines.launch

private enum class DemoTab(val label: String) {
    Home("Bosh sahifa"),
    Map("Xarita"),
    Profile("Profil")
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
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.78f)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(84.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.18f)
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

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(20.dp))

            Text(
                text = "Family Care Demo",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Oila lokatsiyasi va parvarish nazorati yuklanmoqda",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.86f)
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(28.dp))

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
    onTriggerSos: () -> Unit,
    onDismissSos: () -> Unit
) {
    val tabs = DemoTab.entries
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showSosConfirm by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSosConfirm = true },
                containerColor = Color(0xFFC84531),
                contentColor = Color.White
            ) {
                Text("SOS", fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            when (tab) {
                                DemoTab.Home -> Icon(Icons.Default.Home, contentDescription = null)
                                DemoTab.Map -> Icon(Icons.Default.LocationOn, contentDescription = null)
                                DemoTab.Profile -> Icon(Icons.Default.Person, contentDescription = null)
                            }
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        when (tabs[selectedTabIndex]) {
            DemoTab.Home -> HomeScreen(
                modifier = Modifier.padding(padding),
                uiState = uiState,
                onRefresh = onRefresh,
                onSelectMember = onSelectMember,
                onOpenMap = { memberId ->
                    memberId?.let(onSelectMember)
                    selectedTabIndex = DemoTab.Map.ordinal
                },
                onPrimaryAction = showMessage
            )

            DemoTab.Map -> FamilyMapScreen(
                modifier = Modifier.padding(padding),
                uiState = uiState,
                onSelectMember = onSelectMember,
                onAction = showMessage
            )

            DemoTab.Profile -> ProfileScreen(
                modifier = Modifier.padding(padding),
                uiState = uiState,
                onRefresh = onRefresh,
                onSignOut = onSignOut,
                onAction = showMessage
            )
        }
    }

    if (showSosConfirm) {
        AlertDialog(
            onDismissRequest = { showSosConfirm = false },
            title = { Text("SOS signal yuborilsinmi?") },
            text = {
                Text("Bosilgandan so'ng barcha oila a'zolariga demo qo'ng'iroq va shoshilinch bildirish yuboriladi.")
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
                    Text("Bekor qilish")
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
                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
                    Text(uiState.sosState.summary)
                    uiState.sosState.contacts.forEach { contact ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(contact.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    contact.phone,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                text = if (contact.state == SosContactState.CALLING) {
                                    "Qo'ng'iroq qilinmoqda"
                                } else {
                                    "Xabardor qilindi"
                                },
                                color = if (contact.state == SosContactState.CALLING) {
                                    Color(0xFFC06C17)
                                } else {
                                    Color(0xFF216C48)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (uiState.sosState.isSending) {
                    TextButton(onClick = {}) {
                        Text("Jarayonda...")
                    }
                } else {
                    TextButton(onClick = onDismissSos) {
                        Text("Yopish")
                    }
                }
            }
        )
    }
}
