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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.example.myapplication.data.model.DemoActionResult
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.data.model.FamilyInvitation
import com.example.myapplication.data.model.FamilyMember
import com.example.myapplication.data.model.InvitationStatus
import com.example.myapplication.data.model.MemberStatus
import com.example.myapplication.ui.component.ActivityFeedRow
import com.example.myapplication.ui.component.InfoLine
import com.example.myapplication.ui.component.InitialsAvatar
import com.example.myapplication.ui.component.InvitationStatusPill
import com.example.myapplication.ui.component.MemberRow
import com.example.myapplication.ui.component.MetricCard
import com.example.myapplication.ui.component.QuickActionTile
import com.example.myapplication.ui.component.SectionTitle
import com.example.myapplication.ui.component.StatusPill
import com.example.myapplication.ui.component.SymbolChip
import com.example.myapplication.ui.theme.GlowRose
import com.example.myapplication.ui.theme.GlowSand
import com.example.myapplication.ui.theme.GlowSky

private enum class MemberListFilter(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ALL("All", Icons.Default.Group),
    ATTENTION("Alert", Icons.Default.WarningAmber),
    MOVING("Move", Icons.Default.Route),
    SAFE("Safe", Icons.Default.CheckCircle)
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: DemoUiState,
    onRefresh: () -> Unit,
    onSelectMember: (Int) -> Unit,
    onOpenMap: (Int?) -> Unit,
    onSendInvitation: (String, String, String) -> DemoActionResult,
    onPrimaryAction: (String) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var activeFilter by rememberSaveable { mutableStateOf(MemberListFilter.ALL) }
    var inviteName by rememberSaveable { mutableStateOf("") }
    var inviteRelation by rememberSaveable { mutableStateOf("") }
    var invitePhone by rememberSaveable { mutableStateOf("") }

    val metrics = remember(uiState.members, uiState.invitations) {
        HomeMetrics(
            alertCount = uiState.members.count { it.status == MemberStatus.NEEDS_ATTENTION },
            movingCount = uiState.members.count { it.status == MemberStatus.MOVING },
            pendingInvites = uiState.invitations.count { it.status != InvitationStatus.ACCEPTED }
        )
    }

    val filteredMembers = remember(uiState.members, searchQuery, activeFilter) {
        uiState.members.filter { member ->
            val query = searchQuery.trim().lowercase()
            val matchesQuery = query.isBlank() ||
                member.name.lowercase().contains(query) ||
                member.relation.lowercase().contains(query) ||
                member.placeLabel.lowercase().contains(query)

            val matchesFilter = when (activeFilter) {
                MemberListFilter.ALL -> true
                MemberListFilter.ATTENTION -> member.status == MemberStatus.NEEDS_ATTENTION
                MemberListFilter.MOVING -> member.status == MemberStatus.MOVING
                MemberListFilter.SAFE -> member.status == MemberStatus.SAFE
            }

            matchesQuery && matchesFilter
        }
    }

    val selectedMember = remember(uiState.selectedMemberId, uiState.members) {
        uiState.members.firstOrNull { it.id == uiState.selectedMemberId } ?: uiState.members.firstOrNull()
    }
    val detailMember = filteredMembers.firstOrNull { it.id == selectedMember?.id } ?: selectedMember
    val lowBatteryMember = remember(uiState.members) { uiState.members.minByOrNull { it.battery } }

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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.92f)
                                )
                            )
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Assalomu alaykum, ${uiState.caregiverName}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Live holat bir qarashda.",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SymbolChip(
                            icon = Icons.Default.Group,
                            label = "${uiState.members.size} a'zo",
                            accent = GlowSky
                        )
                        SymbolChip(
                            icon = Icons.Default.LocationOn,
                            label = uiState.lastSyncLabel,
                            accent = GlowSand
                        )
                        SymbolChip(
                            icon = Icons.Default.Shield,
                            label = "${uiState.trustedPlacesCount} zona",
                            accent = GlowRose
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Refresh,
                            label = if (uiState.isRefreshing) "Sync..." else "Sync",
                            accent = MaterialTheme.colorScheme.primary,
                            onClick = onRefresh
                        )
                        QuickActionTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Map,
                            label = "Map",
                            accent = Color(0xFF245D86),
                            onClick = { onOpenMap(selectedMember?.id) }
                        )
                        QuickActionTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.WarningAmber,
                            label = "Focus",
                            accent = MaterialTheme.colorScheme.error,
                            onClick = {
                                lowBatteryMember?.let { member ->
                                    onSelectMember(member.id)
                                    onPrimaryAction("${member.name} kuzatuv markazga olindi.")
                                }
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
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Group,
                    value = uiState.members.size.toString(),
                    label = "A'zo",
                    hint = "Ulangan oilaviy doira",
                    accent = MaterialTheme.colorScheme.primary
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.WarningAmber,
                    value = metrics.alertCount.toString(),
                    label = "Alert",
                    hint = "Diqqat talab qilayotganlar",
                    accent = MaterialTheme.colorScheme.error
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Route,
                    value = metrics.movingCount.toString(),
                    label = "Yo'lda",
                    hint = "Harakatdagi a'zolar",
                    accent = Color(0xFF245D86)
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.PersonAdd,
                    value = metrics.pendingInvites.toString(),
                    label = "Invite",
                    hint = "Qabul kutilayotganlar",
                    accent = Color(0xFF8E650E)
                )
            }
        }

        lowBatteryMember?.let { member ->
            item {
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SymbolChip(
                                icon = Icons.Default.BatteryAlert,
                                label = "Past batareya",
                                accent = GlowSand
                            )
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${member.battery}% • ${member.placeLabel}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        QuickActionTile(
                            icon = Icons.Default.LocationOn,
                            label = "Open",
                            accent = MaterialTheme.colorScheme.primary,
                            onClick = {
                                onSelectMember(member.id)
                                onOpenMap(member.id)
                            }
                        )
                    }
                }
            }
        }

        item {
            SectionTitle(
                icon = Icons.Default.PersonAdd,
                title = "Invite",
                subtitle = "Telefon orqali oilaga bog'lash"
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
                    OutlinedTextField(
                        value = invitePhone,
                        onValueChange = { invitePhone = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        label = { Text("Telefon") },
                        placeholder = { Text("+998 90 123 45 67") },
                        shape = MaterialTheme.shapes.large
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = inviteName,
                            onValueChange = { inviteName = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                            label = { Text("Ism") },
                            placeholder = { Text("Madina") },
                            shape = MaterialTheme.shapes.large
                        )
                        OutlinedTextField(
                            value = inviteRelation,
                            onValueChange = { inviteRelation = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                            label = { Text("Roli") },
                            placeholder = { Text("Singil") },
                            shape = MaterialTheme.shapes.large
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SymbolChip(
                            icon = Icons.Default.NotificationsActive,
                            label = "Accept oqimi",
                            accent = GlowSky
                        )
                        SymbolChip(
                            icon = Icons.AutoMirrored.Filled.Send,
                            label = "SMS yoki app",
                            accent = GlowSand
                        )
                    }

                    Button(
                        onClick = {
                            val result = onSendInvitation(inviteName, inviteRelation, invitePhone)
                            onPrimaryAction(result.message)
                            if (result.success) {
                                invitePhone = ""
                                inviteName = ""
                                inviteRelation = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Box(modifier = Modifier.width(8.dp))
                        Text("Invitation yuborish")
                    }
                }
            }
        }

        if (uiState.invitations.isNotEmpty()) {
            item {
                SectionTitle(
                    icon = Icons.Default.NotificationsActive,
                    title = "Invite holati",
                    subtitle = "So'nggi yuborilganlar"
                )
            }

            items(uiState.invitations.take(4), key = { it.id }) { invitation ->
                InvitationCard(invitation = invitation)
            }
        }

        item {
            SectionTitle(
                icon = Icons.Default.Group,
                title = "Oila",
                subtitle = "Qidiruv + filter"
            )
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text("A'zo qidirish") },
                placeholder = { Text("Ism, rol yoki joy") },
                shape = MaterialTheme.shapes.large
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(MemberListFilter.entries) { filter ->
                    FilterChip(
                        selected = activeFilter == filter,
                        onClick = { activeFilter = filter },
                        label = { Text(filter.label) },
                        leadingIcon = {
                            Icon(
                                imageVector = filter.icon,
                                contentDescription = null,
                                modifier = Modifier.width(18.dp)
                            )
                        }
                    )
                }
            }
        }

        if (filteredMembers.isEmpty()) {
            item {
                Card(shape = MaterialTheme.shapes.extraLarge) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Natija yo'q",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Qidiruvni soddalashtiring yoki filterni almashtiring.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredMembers, key = { it.id }) { member ->
                MemberRow(
                    member = member,
                    isSelected = member.id == detailMember?.id,
                    onClick = { onSelectMember(member.id) }
                )
            }
        }

        detailMember?.let { member ->
            item {
                SectionTitle(
                    icon = Icons.Default.Shield,
                    title = "Focus card",
                    subtitle = "Tanlangan a'zo"
                )
            }

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
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                InitialsAvatar(
                                    initials = memberInitials(member.name),
                                    seed = member.id,
                                    size = 64.dp
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = member.name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "${member.relation} • ${member.age} yosh",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            StatusPill(status = member.status)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SymbolChip(
                                icon = Icons.Default.BatteryAlert,
                                label = "${member.battery}%",
                                accent = GlowSand
                            )
                            SymbolChip(
                                icon = Icons.Default.Favorite,
                                label = "${member.heartRate} bpm",
                                accent = GlowRose
                            )
                            SymbolChip(
                                icon = Icons.Default.Route,
                                label = "${member.steps}",
                                accent = GlowSky
                            )
                        }

                        InfoLine(Icons.Default.LocationOn, "Joy", member.placeLabel)
                        InfoLine(Icons.Default.Map, "Manzil", member.address)
                        InfoLine(Icons.Default.AccessTime, "Oxirgi signal", member.lastUpdate)
                        InfoLine(Icons.Default.Schedule, "Reja", member.schedule)
                        InfoLine(Icons.AutoMirrored.Filled.TextSnippet, "Izoh", member.note)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickActionTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.NotificationsActive,
                                label = "Check-in",
                                accent = MaterialTheme.colorScheme.primary,
                                onClick = { onPrimaryAction("${member.name} uchun demo check-in yuborildi.") }
                            )
                            QuickActionTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Map,
                                label = "Map",
                                accent = Color(0xFF245D86),
                                onClick = { onOpenMap(member.id) }
                            )
                            QuickActionTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Phone,
                                label = "Call",
                                accent = MaterialTheme.colorScheme.tertiary,
                                onClick = { onPrimaryAction("${member.name} uchun demo qo'ng'iroq tayyorlandi.") }
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionTitle(
                icon = Icons.Default.NotificationsActive,
                title = "Feed",
                subtitle = "So'nggi harakatlar"
            )
        }

        items(uiState.activityFeed, key = { it.id }) { item ->
            ActivityFeedRow(item = item)
        }
    }
}

@Composable
private fun InvitationCard(invitation: FamilyInvitation) {
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InitialsAvatar(
                        initials = memberInitials(invitation.name),
                        seed = invitation.id,
                        size = 48.dp
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = invitation.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${invitation.relation} • ${invitation.phone}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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
                    icon = Icons.Default.AccessTime,
                    label = invitation.sentAtLabel,
                    accent = GlowRose
                )
            }
        }
    }
}

private data class HomeMetrics(
    val alertCount: Int,
    val movingCount: Int,
    val pendingInvites: Int
)

private fun memberInitials(fullName: String): String {
    val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "FC"
        parts.size == 1 -> parts.first().take(2).uppercase()
        else -> "${parts.first().first()}${parts.last().first()}".uppercase()
    }
}
