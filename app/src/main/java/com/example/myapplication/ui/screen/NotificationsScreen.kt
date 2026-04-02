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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.AppNotification
import com.example.myapplication.data.model.DemoActionResult
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.data.model.FamilyInvitation
import com.example.myapplication.data.model.InvitationStatus
import com.example.myapplication.data.model.NotificationCategory
import com.example.myapplication.ui.component.InvitationStatusPill
import com.example.myapplication.ui.component.QuickActionTile
import com.example.myapplication.ui.component.SectionTitle
import com.example.myapplication.ui.component.SymbolChip
import com.example.myapplication.ui.theme.GlowRose
import com.example.myapplication.ui.theme.GlowSand
import com.example.myapplication.ui.theme.GlowSky

@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    uiState: DemoUiState,
    onAcceptInvite: (Int) -> DemoActionResult,
    onMarkNotificationRead: (Int) -> Unit,
    onMarkAllNotificationsRead: () -> Unit,
    onAction: (String) -> Unit
) {
    val unreadCount = uiState.notifications.count { !it.isRead }
    val pendingAcceptanceCount = uiState.invitations.count { it.status == InvitationStatus.PENDING_ACCEPTANCE }
    val safetyCount = uiState.notifications.count {
        it.category == NotificationCategory.SAFETY || it.category == NotificationCategory.CRIME
    }

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
        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 118.dp),
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
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Inbox",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Invite, signal va system oqimi",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Box(
                                modifier = Modifier.size(54.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SymbolChip(
                            icon = Icons.Default.NotificationsActive,
                            label = "$unreadCount unread",
                            accent = GlowSky
                        )
                        SymbolChip(
                            icon = Icons.Default.PersonAdd,
                            label = "$pendingAcceptanceCount accept",
                            accent = GlowSand
                        )
                        SymbolChip(
                            icon = Icons.Default.WarningAmber,
                            label = "$safetyCount safety",
                            accent = GlowRose
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.DoneAll,
                            label = "Read",
                            accent = MaterialTheme.colorScheme.primary,
                            onClick = {
                                onMarkAllNotificationsRead()
                                onAction("Barcha bildirishnomalar o'qildi deb belgilandi.")
                            }
                        )
                        QuickActionTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.PersonAdd,
                            label = "Invite",
                            accent = GlowSand,
                            onClick = { onAction("Invite oqimi Bosh sahifadagi Invite blokida.") }
                        )
                        QuickActionTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Shield,
                            label = "Alert",
                            accent = GlowRose,
                            onClick = { onAction("Safety alertlar shu sahifada yuqoridan ko'rinadi.") }
                        )
                    }
                }
            }
        }

        if (uiState.invitations.isNotEmpty()) {
            item {
                SectionTitle(
                    icon = Icons.Default.PersonAdd,
                    title = "Invite oqimi",
                    subtitle = "Accept kutilayotganlar"
                )
            }

            items(uiState.invitations, key = { it.id }) { invitation ->
                InvitationReviewCard(
                    invitation = invitation,
                    onAccept = {
                        val result = onAcceptInvite(invitation.id)
                        onAction(result.message)
                    }
                )
            }
        }

        item {
            SectionTitle(
                icon = Icons.Default.NotificationsActive,
                title = "Barcha bildirishnomalar",
                subtitle = "Yangi xabarlar yuqorida"
            )
        }

        if (uiState.notifications.isEmpty()) {
            item {
                Card(shape = MaterialTheme.shapes.extraLarge) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Bo'sh",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hozircha yangi signal yo'q.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.notifications, key = { it.id }) { notification ->
                NotificationCard(
                    notification = notification,
                    invitation = notification.inviteId?.let { inviteId ->
                        uiState.invitations.firstOrNull { it.id == inviteId }
                    },
                    onRead = { onMarkNotificationRead(notification.id) },
                    onAccept = {
                        val result = onAcceptInvite(it)
                        onAction(result.message)
                    }
                )
            }
        }
    }
}

@Composable
private fun InvitationReviewCard(
    invitation: FamilyInvitation,
    onAccept: () -> Unit
) {
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = invitation.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${invitation.relation} • ${invitation.phone}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                InvitationStatusPill(status = invitation.status)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SymbolChip(
                    icon = if (invitation.isPlatformUser) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.Send,
                    label = if (invitation.isPlatformUser) "Platforma" else "SMS",
                    accent = if (invitation.isPlatformUser) GlowSky else GlowSand
                )
                SymbolChip(
                    icon = Icons.Default.Info,
                    label = invitation.sentAtLabel,
                    accent = GlowRose
                )
            }

            if (invitation.status == InvitationStatus.PENDING_ACCEPTANCE) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Box(modifier = Modifier.width(8.dp))
                    Text("Accept keldi")
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: AppNotification,
    invitation: FamilyInvitation?,
    onRead: () -> Unit,
    onAccept: (Int) -> Unit
) {
    val accent = when (notification.category) {
        NotificationCategory.INVITE -> GlowSand
        NotificationCategory.SAFETY -> GlowRose
        NotificationCategory.CRIME -> GlowRose
        NotificationCategory.SYSTEM -> GlowSky
    }
    val icon = when (notification.category) {
        NotificationCategory.INVITE -> Icons.Default.PersonAdd
        NotificationCategory.SAFETY -> Icons.Default.WarningAmber
        NotificationCategory.CRIME -> Icons.Default.Shield
        NotificationCategory.SYSTEM -> Icons.Default.Info
    }

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                Color.White.copy(alpha = 0.92f)
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
            }
        )
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
                        shape = CircleShape,
                        color = accent
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = notification.timeLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!notification.isRead) {
                    SymbolChip(
                        icon = Icons.Default.NotificationsActive,
                        label = "Yangi",
                        accent = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = notification.message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!notification.isRead) {
                    TextButton(
                        onClick = onRead,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("O'qildi")
                    }
                }

                if (
                    notification.inviteId != null &&
                    invitation?.status == InvitationStatus.PENDING_ACCEPTANCE &&
                    notification.actionLabel != null
                ) {
                    OutlinedButton(
                        onClick = { onAccept(notification.inviteId) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(notification.actionLabel)
                    }
                }
            }
        }
    }
}
