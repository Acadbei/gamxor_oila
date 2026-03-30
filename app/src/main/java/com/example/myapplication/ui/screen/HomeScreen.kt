package com.example.myapplication.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.data.model.FamilyMember
import com.example.myapplication.data.model.MemberStatus
import com.example.myapplication.ui.component.ActivityFeedRow
import com.example.myapplication.ui.component.InfoLine
import com.example.myapplication.ui.component.MemberRow
import com.example.myapplication.ui.component.MetricCard
import com.example.myapplication.ui.component.SectionTitle
import com.example.myapplication.ui.component.StatusPill

private enum class MemberListFilter(val label: String) {
    ALL("Barchasi"),
    ATTENTION("Diqqat"),
    MOVING("Harakatda"),
    SAFE("Xavfsiz")
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: DemoUiState,
    onRefresh: () -> Unit,
    onSelectMember: (Int) -> Unit,
    onOpenMap: (Int?) -> Unit,
    onPrimaryAction: (String) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var activeFilter by rememberSaveable { mutableStateOf(MemberListFilter.ALL) }

    val metrics = remember(uiState.members) {
        HomeMetrics(
            alertCount = uiState.members.count { it.status == MemberStatus.NEEDS_ATTENTION },
            movingCount = uiState.members.count { it.status == MemberStatus.MOVING },
            averageBattery = uiState.members.map { it.battery }.average().toInt()
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
    val detailMember = filteredMembers.firstOrNull { it.id == selectedMember?.id } ?: filteredMembers.firstOrNull()
    val lowBatteryMember = remember(uiState.members) { uiState.members.minByOrNull { it.battery } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Assalomu alaykum, ${uiState.caregiverName}",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.familyLabel} holati bitta oynada jamlandi. Oxirgi sinxron: ${uiState.lastSyncLabel}.",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onRefresh,
                            enabled = !uiState.isRefreshing
                        ) {
                            Text(if (uiState.isRefreshing) "Yangilanmoqda..." else "Yangilash")
                        }
                        OutlinedButton(
                            onClick = { onOpenMap(selectedMember?.id) },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.32f))
                        ) {
                            Text("Jonli xarita", color = MaterialTheme.colorScheme.onPrimary)
                        }
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
                    value = uiState.members.size.toString(),
                    label = "Ulangan a'zo",
                    accent = MaterialTheme.colorScheme.primary
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    value = metrics.alertCount.toString(),
                    label = "Ogohlantirish",
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
                    value = metrics.movingCount.toString(),
                    label = "Harakatda",
                    accent = MaterialTheme.colorScheme.tertiary
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    value = "${metrics.averageBattery}%",
                    label = "O'rtacha batareya",
                    accent = Color(0xFFBF8B30)
                )
            }
        }

        lowBatteryMember?.let { member ->
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Tez tavsiya",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${member.name} hozir eng past batareyada: ${member.battery}%. Demo paytida shu a'zoni tekshirib ko'ring.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = {
                            onSelectMember(member.id)
                            onOpenMap(member.id)
                        }) {
                            Text("Shu a'zoni ochish")
                        }
                    }
                }
            }
        }

        item {
            SectionTitle(
                title = "Oila a'zolari",
                subtitle = "Qidiruv va tez filterlar orqali kerakli odamni darhol toping."
            )
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { androidx.compose.material3.Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text("A'zo qidirish") },
                placeholder = { Text("Ism, qarindoshlik yoki joy bo'yicha") }
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(MemberListFilter.entries) { filter ->
                    FilterChip(
                        selected = activeFilter == filter,
                        onClick = { activeFilter = filter },
                        label = { Text(filter.label) }
                    )
                }
            }
        }

        if (filteredMembers.isEmpty()) {
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Natija topilmadi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Qidiruv yoki filterlarni yengillashtirib ko'ring.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = {
                            searchQuery = ""
                            activeFilter = MemberListFilter.ALL
                        }) {
                            Text("Filterlarni tozalash")
                        }
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
                    title = "Tanlangan a'zo",
                    subtitle = "Asosiy ma'lumot va tez amallar bir joyda."
                )
            }

            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            androidx.compose.foundation.layout.Column {
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${member.relation}, ${member.age} yosh",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusPill(status = member.status)
                        }

                        InfoLine("Hozirgi joy", member.placeLabel)
                        InfoLine("Manzil", member.address)
                        InfoLine("Oxirgi yangilanish", member.lastUpdate)
                        InfoLine("Yurak urishi", "${member.heartRate} bpm")
                        InfoLine("Bugungi qadam", "${member.steps} qadam")
                        InfoLine("Reja", member.schedule)
                        InfoLine("Izoh", member.note)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { onPrimaryAction("${member.name} uchun demo check-in yuborildi.") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Check-in")
                            }
                            OutlinedButton(
                                onClick = { onOpenMap(member.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Xaritada ko'rish")
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionTitle(
                title = "So'nggi faoliyat",
                subtitle = "Muhim harakatlar feed ko'rinishida saqlanadi."
            )
        }

        items(uiState.activityFeed, key = { it.id }) { item ->
            ActivityFeedRow(item = item)
        }
    }
}

private data class HomeMetrics(
    val alertCount: Int,
    val movingCount: Int,
    val averageBattery: Int
)
