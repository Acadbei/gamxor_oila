package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.CaregiverProfile
import com.example.myapplication.data.model.DemoActionResult
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.data.model.ProfilePermissions
import com.example.myapplication.ui.component.InfoLine
import com.example.myapplication.ui.component.InitialsAvatar
import com.example.myapplication.ui.component.ProfileSwitchRow
import com.example.myapplication.ui.component.QuickActionTile
import com.example.myapplication.ui.component.SectionTitle
import com.example.myapplication.ui.component.SymbolChip
import com.example.myapplication.ui.theme.GlowRose
import com.example.myapplication.ui.theme.GlowSand
import com.example.myapplication.ui.theme.GlowSky

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    uiState: DemoUiState,
    onRefresh: () -> Unit,
    onSignOut: () -> Unit,
    onSaveProfile: (CaregiverProfile) -> DemoActionResult,
    onAction: (String) -> Unit
) {
    val profile = uiState.profile

    var fullName by rememberSaveable(profile.fullName) { mutableStateOf(profile.fullName) }
    var familyLabel by rememberSaveable(profile.familyLabel) { mutableStateOf(profile.familyLabel) }
    var phone by rememberSaveable(profile.phone) { mutableStateOf(profile.phone) }
    var email by rememberSaveable(profile.email) { mutableStateOf(profile.email) }
    var address by rememberSaveable(profile.address) { mutableStateOf(profile.address) }
    var emergencyContact by rememberSaveable(profile.emergencyContact) { mutableStateOf(profile.emergencyContact) }
    var bio by rememberSaveable(profile.bio) { mutableStateOf(profile.bio) }
    var avatarSeed by rememberSaveable(profile.avatarSeed) { mutableIntStateOf(profile.avatarSeed) }
    var locationEnabled by rememberSaveable(profile.permissions.locationEnabled) {
        mutableStateOf(profile.permissions.locationEnabled)
    }
    var microphoneEnabled by rememberSaveable(profile.permissions.microphoneEnabled) {
        mutableStateOf(profile.permissions.microphoneEnabled)
    }
    var notificationsEnabled by rememberSaveable(profile.permissions.notificationsEnabled) {
        mutableStateOf(profile.permissions.notificationsEnabled)
    }
    var preciseLocationEnabled by rememberSaveable(profile.permissions.preciseLocationEnabled) {
        mutableStateOf(profile.permissions.preciseLocationEnabled)
    }
    var backgroundRefreshEnabled by rememberSaveable(profile.permissions.backgroundRefreshEnabled) {
        mutableStateOf(profile.permissions.backgroundRefreshEnabled)
    }

    val completionScore = listOf(
        fullName.isNotBlank(),
        familyLabel.isNotBlank(),
        phone.filter(Char::isDigit).length >= 9,
        email.contains("@"),
        address.isNotBlank(),
        emergencyContact.filter(Char::isDigit).length >= 9,
        locationEnabled,
        notificationsEnabled
    ).count { it }
    val completionProgress = completionScore / 8f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        Color.White,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                )
            ),
        contentPadding = PaddingValues(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 144.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            InitialsAvatar(
                                initials = initials(fullName),
                                seed = avatarSeed,
                                size = 72.dp
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = fullName.ifBlank { "Family Coordinator" },
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = familyLabel.ifBlank { uiState.familyLabel },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SymbolChip(
                                        icon = Icons.Default.Group,
                                        label = "${uiState.members.size} a'zo",
                                        accent = GlowSky
                                    )
                                    SymbolChip(
                                        icon = Icons.Default.Shield,
                                        label = "${uiState.trustedPlacesCount} zona",
                                        accent = GlowSand
                                    )
                                }
                            }
                        }

                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { completionProgress },
                                modifier = Modifier.size(58.dp),
                                strokeWidth = 6.dp,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Text(
                                text = "${(completionProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.CameraAlt,
                            label = "Avatar",
                            accent = GlowRose,
                            onClick = { avatarSeed = (avatarSeed + 1) % 5 }
                        )
                        QuickActionTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Sync,
                            label = if (uiState.isRefreshing) "Sync..." else "Sync",
                            accent = MaterialTheme.colorScheme.primary,
                            onClick = onRefresh
                        )
                        QuickActionTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.NotificationsActive,
                            label = "Inbox",
                            accent = GlowSky,
                            onClick = { onAction("Bildirishnomalar tepada o'ng burchakda.") }
                        )
                    }

                    InfoLine(Icons.Default.Group, "Monitoring", uiState.familyLabel)
                    InfoLine(Icons.Default.LocationOn, "Bazaviy manzil", address)
                    InfoLine(Icons.Default.Schedule, "Keyingi check-in", uiState.nextCheckInLabel)
                }
            }
        }

        item {
            SectionTitle(
                icon = Icons.Default.Person,
                title = "Identity",
                subtitle = "Asosiy profil"
            )
        }

        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("To'liq ism") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.large
                        )
                        OutlinedTextField(
                            value = familyLabel,
                            onValueChange = { familyLabel = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Oila nomi") },
                            leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.large
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Telefon") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.large
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Email") },
                            leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.large
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Manzil") },
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.large
                        )
                        OutlinedTextField(
                            value = emergencyContact,
                            onValueChange = { emergencyContact = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("SOS kontakt") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.large
                        )
                    }

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Bio") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.TextSnippet, contentDescription = null) },
                        minLines = 3,
                        shape = MaterialTheme.shapes.large
                    )
                }
            }
        }

        item {
            SectionTitle(
                icon = Icons.Default.Shield,
                title = "Access",
                subtitle = "Ruxsat va monitoring"
            )
        }

        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileSwitchRow(
                        icon = Icons.Default.LocationOn,
                        title = "Lokatsiya",
                        subtitle = "Jonli xarita va safe-zone",
                        checked = locationEnabled,
                        onCheckedChange = { locationEnabled = it }
                    )
                    ProfileSwitchRow(
                        icon = Icons.Default.CheckCircle,
                        title = "Aniq nuqta",
                        subtitle = "Precision GPS",
                        checked = preciseLocationEnabled,
                        onCheckedChange = { preciseLocationEnabled = it }
                    )
                    ProfileSwitchRow(
                        icon = Icons.Default.Mic,
                        title = "Audio",
                        subtitle = "SOS va check-in",
                        checked = microphoneEnabled,
                        onCheckedChange = { microphoneEnabled = it }
                    )
                    ProfileSwitchRow(
                        icon = Icons.Default.NotificationsActive,
                        title = "Push",
                        subtitle = "Invite va alert",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                    ProfileSwitchRow(
                        icon = Icons.Default.Sync,
                        title = "Fon sync",
                        subtitle = "Ilova yopiq bo'lsa ham",
                        checked = backgroundRefreshEnabled,
                        onCheckedChange = { backgroundRefreshEnabled = it }
                    )
                }
            }
        }

        item {
            SectionTitle(
                icon = Icons.Default.Save,
                title = "Actions",
                subtitle = "Saqlash va hisob"
            )
        }

        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SymbolChip(
                            icon = Icons.Default.CheckCircle,
                            label = "Ready ${(completionProgress * 100).toInt()}%",
                            accent = GlowSky
                        )
                        SymbolChip(
                            icon = Icons.Default.NotificationsActive,
                            label = if (notificationsEnabled) "Push on" else "Push off",
                            accent = if (notificationsEnabled) GlowSand else GlowRose
                        )
                    }

                    Button(
                        onClick = {
                            val result = onSaveProfile(
                                CaregiverProfile(
                                    fullName = fullName,
                                    phone = phone,
                                    email = email,
                                    familyLabel = familyLabel,
                                    address = address,
                                    emergencyContact = emergencyContact,
                                    avatarSeed = avatarSeed,
                                    bio = bio,
                                    permissions = ProfilePermissions(
                                        locationEnabled = locationEnabled,
                                        microphoneEnabled = microphoneEnabled,
                                        notificationsEnabled = notificationsEnabled,
                                        preciseLocationEnabled = preciseLocationEnabled,
                                        backgroundRefreshEnabled = backgroundRefreshEnabled
                                    )
                                )
                            )
                            onAction(result.message)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Box(modifier = Modifier.width(8.dp))
                        Text("Profilni saqlash")
                    }

                    TextButton(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Box(modifier = Modifier.width(8.dp))
                        Text("Demo hisobdan chiqish")
                    }
                }
            }
        }
    }
}

private fun initials(fullName: String): String {
    val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "FC"
        parts.size == 1 -> parts.first().take(2).uppercase()
        else -> "${parts.first().first()}${parts.last().first()}".uppercase()
    }
}
