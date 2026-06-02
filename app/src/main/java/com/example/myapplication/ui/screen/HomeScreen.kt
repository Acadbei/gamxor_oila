package com.example.myapplication.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.AppNotification
import com.example.myapplication.data.model.BackendStatus
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.data.model.FamilyMember
import com.example.myapplication.data.model.NotificationCategory
import com.example.myapplication.data.model.SosAlert
import com.example.myapplication.ui.component.InitialsAvatar
import com.example.myapplication.ui.component.InfoLine
import com.example.myapplication.ui.component.SectionTitle
import com.example.myapplication.ui.component.StatusPill
import com.example.myapplication.ui.component.SymbolChip
import com.example.myapplication.ui.component.initialsFromName
import com.example.myapplication.ui.theme.DividerSoft
import com.example.myapplication.ui.theme.GlowMint
import com.example.myapplication.ui.theme.GlowRose
import com.example.myapplication.ui.theme.GlowSand
import com.example.myapplication.ui.theme.GlowSky

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: DemoUiState,
    onSelectMember: (Int) -> Unit,
    onCall: (String) -> Unit,
    onDismissCrimeAlert: (Int) -> Unit
) {
    val household = remember(uiState.selfMember, uiState.members) {
        listOf(uiState.selfMember) + uiState.members
    }
    val totalDistance = remember(household) { household.sumOf { it.distanceKm } }
    val crimeNotifications = remember(uiState.notifications) {
        uiState.notifications.filter { it.category == NotificationCategory.CRIME }
    }
    var expandedMemberId by rememberSaveable { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        Color.White,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                    )
                )
            ),
        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Assalomu alaykum, ${uiState.caregiverName}",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Bugungi oilaviy harakat, masofa va SOS nazorati shu yerda jamlangan.",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SymbolChip(
                            icon = Icons.Default.Group,
                            label = "${uiState.members.size} a'zo",
                            accent = GlowMint
                        )
                        SymbolChip(
                            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                            label = "${"%.1f".format(totalDistance)} km",
                            accent = GlowSand
                        )
                        SymbolChip(
                            icon = if (uiState.backendConnection.status == BackendStatus.ONLINE) {
                                Icons.Default.CloudDone
                            } else {
                                Icons.Default.CloudOff
                            },
                            label = uiState.backendConnection.label,
                            accent = if (uiState.backendConnection.status == BackendStatus.ONLINE) {
                                GlowMint
                            } else {
                                GlowRose
                            }
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OverviewStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Group,
                    value = uiState.members.size.toString(),
                    label = "Oila a'zolari",
                    accent = GlowSky
                )
                OverviewStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.WarningAmber,
                    value = uiState.activeSosAlerts.size.toString(),
                    label = "Aktiv SOS",
                    accent = GlowRose
                )
                OverviewStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Route,
                    value = "${"%.1f".format(totalDistance)}",
                    label = "km",
                    accent = GlowSand
                )
            }
        }

        if (uiState.activeSosAlerts.isNotEmpty()) {
            item {
                SectionTitle(
                    icon = Icons.Default.WarningAmber,
                    title = "SOS ogohlantirishlar",
                    subtitle = "Qizil kartalarda faol signal ko'rsatiladi"
                )
            }

            items(uiState.activeSosAlerts, key = { "alert-${it.memberId}" }) { alert ->
                SosAlertCard(
                    alert = alert,
                    onCall = { onCall(alert.phone) }
                )
            }
        }

        item {
            SectionTitle(
                icon = Icons.Default.Group,
                title = "Uy ahli",
                subtitle = "O'zingiz va barcha oila a'zolari"
            )
        }

        items(household, key = { "member-${it.id}" }) { member ->
            HouseholdMemberCard(
                member = member,
                isSelected = member.id == uiState.selectedMemberId,
                isExpanded = expandedMemberId == member.id,
                hasActiveSos = uiState.activeSosAlerts.any { it.memberId == member.id },
                onClick = {
                    onSelectMember(member.id)
                    expandedMemberId = if (expandedMemberId == member.id) null else member.id
                },
                onCall = { onCall(member.phone) }
            )
        }

        if (crimeNotifications.isNotEmpty()) {
            item {
                SectionTitle(
                    icon = Icons.Default.Shield,
                    title = "Hududiy ogohlantirishlar",
                    subtitle = "Yaqin atrofdagi jinoyatchilik xabarlari"
                )
            }

            items(crimeNotifications, key = { "crime-${it.id}" }) { notification ->
                CrimeNotificationCard(
                    notification = notification,
                    onDismiss = { onDismissCrimeAlert(notification.id) }
                )
            }
        }
    }
}

@Composable
private fun OverviewStatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    accent: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f)),
        border = BorderStroke(1.dp, DividerSoft.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = accent.copy(alpha = 0.18f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (accent == GlowRose) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CrimeNotificationCard(
    notification: AppNotification,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.97f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = notification.timeLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                SymbolChip(
                    icon = Icons.Default.WarningAmber,
                    label = "Ogohlantirish",
                    accent = MaterialTheme.colorScheme.error
                )
            }

            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Box(modifier = Modifier.width(6.dp))
                Text(
                    text = "O'chirish",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SosAlertCard(
    alert: SosAlert,
    onCall: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InitialsAvatar(
                        initials = initialsFromName(alert.name),
                        seed = alert.memberId,
                        size = 54.dp
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = alert.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${alert.relation} | ${alert.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                SymbolChip(
                    icon = Icons.Default.WarningAmber,
                    label = "SOS",
                    accent = MaterialTheme.colorScheme.error,
                    filled = true
                )
            }

            InfoLine(Icons.Default.LocationOn, "Joylashuv", alert.address)
            InfoLine(Icons.Default.AccessTime, "Oxirgi signal", alert.lastUpdate)

            Button(onClick = onCall, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Phone, contentDescription = null)
                Box(modifier = Modifier.width(8.dp))
                Text("Qo'ng'iroq qilish")
            }
        }
    }
}

@Composable
private fun HouseholdMemberCard(
    member: FamilyMember,
    isSelected: Boolean,
    isExpanded: Boolean,
    hasActiveSos: Boolean,
    onClick: () -> Unit,
    onCall: () -> Unit
) {
    val borderColor = when {
        hasActiveSos -> MaterialTheme.colorScheme.error.copy(alpha = 0.28f)
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.26f)
        else -> DividerSoft.copy(alpha = 0.45f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                hasActiveSos -> MaterialTheme.colorScheme.error.copy(alpha = 0.06f)
                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                else -> Color.White.copy(alpha = 0.95f)
            }
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        initials = initialsFromName(member.name),
                        seed = member.avatarSeed,
                        size = 56.dp
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = member.relation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (!isExpanded) {
                            Text(
                                text = member.placeLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusPill(status = member.status)
                    SymbolChip(
                        icon = Icons.Default.AccessTime,
                        label = member.presenceLabel,
                        accent = if (member.isOnline) GlowMint else GlowSand
                    )
                    if (member.isCurrentUser) {
                        SymbolChip(
                            icon = Icons.Default.LocationOn,
                            label = "Mening profilim",
                            accent = GlowMint
                        )
                    }
                }
            }

            if (isExpanded) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SymbolChip(
                        icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                        label = "${"%.1f".format(member.distanceKm)} km yurgan",
                        accent = GlowSand
                    )
                    SymbolChip(
                        icon = Icons.Default.Phone,
                        label = "${member.battery}%",
                        accent = GlowSky
                    )
                }

                InfoLine(Icons.Default.LocationOn, "Manzil", member.address)
                InfoLine(Icons.Default.Phone, "Telefon", member.phone)
                InfoLine(Icons.Default.AccessTime, "Oxirgi yangilanish", member.lastUpdate)

                Button(onClick = onCall, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Phone, contentDescription = null)
                    Box(modifier = Modifier.width(8.dp))
                    Text("Qo'ng'iroq qilish")
                }
            }
        }
    }
}
