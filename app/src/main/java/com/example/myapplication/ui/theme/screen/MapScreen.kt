package com.example.myapplication.ui.theme.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.data.model.FamilyMember
import com.example.myapplication.ui.component.InfoLine
import com.example.myapplication.ui.component.InitialsAvatar
import com.example.myapplication.ui.component.QuickActionTile
import com.example.myapplication.ui.component.StatusPill
import com.example.myapplication.ui.component.SymbolChip
import com.example.myapplication.ui.theme.GlowRose
import com.example.myapplication.ui.theme.GlowSand
import com.example.myapplication.ui.theme.GlowSky
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun FamilyMapScreen(
    modifier: Modifier = Modifier,
    uiState: DemoUiState,
    onSelectMember: (Int) -> Unit,
    onAction: (String) -> Unit
) {
    val context = LocalContext.current
    val selectedMember = uiState.members.firstOrNull { it.id == uiState.selectedMemberId } ?: uiState.members.firstOrNull()
    val defaultPoint = selectedMember?.let { GeoPoint(it.lat, it.lon) } ?: GeoPoint(41.3111, 69.2797)
    val markerMap = remember { mutableMapOf<Int, Marker>() }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(12.8)
            controller.setCenter(defaultPoint)
        }
    }

    DisposableEffect(mapView) {
        onDispose {
            mapView.onDetach()
        }
    }

    LaunchedEffect(uiState.members) {
        mapView.overlays.clear()
        markerMap.clear()

        uiState.members.forEach { member ->
            val point = GeoPoint(member.lat, member.lon)
            val marker = Marker(mapView).apply {
                position = point
                title = "${member.name} • ${member.relation}"
                subDescription = "${member.placeLabel} • ${member.battery}%"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ ->
                    onSelectMember(member.id)
                    true
                }
            }
            markerMap[member.id] = marker
            mapView.overlays.add(marker)
        }

        mapView.invalidate()
    }

    LaunchedEffect(selectedMember?.id) {
        markerMap.forEach { (memberId, marker) ->
            marker.alpha = if (memberId == selectedMember?.id) 1f else 0.82f
        }

        selectedMember?.let { member ->
            mapView.controller.animateTo(GeoPoint(member.lat, member.lon))
            mapView.controller.setZoom(14.2)
        }

        mapView.invalidate()
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.92f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Live Map",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Marker tanlang yoki chipdan o'ting",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SymbolChip(
                                icon = Icons.Default.Map,
                                label = "${uiState.members.size} marker",
                                accent = GlowSky
                            )
                            SymbolChip(
                                icon = Icons.Default.Security,
                                label = "${uiState.trustedPlacesCount} zona",
                                accent = GlowSand
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(end = 8.dp)
                    ) {
                        items(uiState.members, key = { it.id }) { member ->
                            MapMemberChip(
                                member = member,
                                isSelected = member.id == selectedMember?.id,
                                onClick = { onSelectMember(member.id) }
                            )
                        }
                    }
                }
            }

            selectedMember?.let { member ->
                Card(
                    modifier = Modifier.navigationBarsPadding(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.94f)
                    )
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
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                InitialsAvatar(
                                    initials = initials(member.name),
                                    seed = member.id,
                                    size = 62.dp
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = member.name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "${member.relation} • ${member.placeLabel}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            StatusPill(status = member.status)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SymbolChip(
                                icon = Icons.Default.Battery5Bar,
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
                                label = member.safeZone,
                                accent = GlowSky
                            )
                        }

                        InfoLine(Icons.Default.LocationOn, "Manzil", member.address)
                        InfoLine(Icons.Default.Security, "Safe zone", member.safeZone)
                        InfoLine(Icons.Default.AccessTime, "Oxirgi signal", member.lastUpdate)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickActionTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Phone,
                                label = "Call",
                                accent = MaterialTheme.colorScheme.primary,
                                onClick = { onAction("${member.name} uchun demo qo'ng'iroq oynasi ochildi.") }
                            )
                            QuickActionTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Route,
                                label = "Route",
                                accent = GlowSky,
                                onClick = { onAction("${member.name} uchun yo'nalish demosi tayyorlandi.") }
                            )
                            QuickActionTile(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.MyLocation,
                                label = "Center",
                                accent = GlowRose,
                                onClick = {
                                    mapView.controller.animateTo(GeoPoint(member.lat, member.lon))
                                    mapView.controller.setZoom(15.0)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapMemberChip(
    member: FamilyMember,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            Color.White.copy(alpha = 0.86f)
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InitialsAvatar(
                initials = initials(member.name),
                seed = member.id,
                size = 40.dp
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = member.relation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
