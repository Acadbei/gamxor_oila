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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.data.model.FamilyMember
import com.example.myapplication.ui.component.InfoLine
import com.example.myapplication.ui.component.InitialsAvatar
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
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.92f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "A'zolar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Marker tanlang",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(end = 104.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.94f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
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
                                    initials = initials(member.name),
                                    seed = member.id,
                                    size = 54.dp
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = member.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "${member.relation} • ${member.placeLabel}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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
                                icon = Icons.Default.AccessTime,
                                label = member.lastUpdate,
                                accent = GlowSky
                            )
                        }

                        InfoLine(Icons.Default.LocationOn, "Manzil", member.address)
                        InfoLine(Icons.Default.Security, "Safe zone", member.safeZone)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MapActionButton(
                                icon = Icons.Default.Phone,
                                accent = MaterialTheme.colorScheme.primary,
                                onClick = { onAction("${member.name} uchun demo qo'ng'iroq oynasi ochildi.") }
                            )
                            MapActionButton(
                                icon = Icons.Default.Route,
                                accent = GlowSky,
                                onClick = { onAction("${member.name} uchun yo'nalish demosi tayyorlandi.") }
                            )
                            MapActionButton(
                                icon = Icons.Default.MyLocation,
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
private fun MapActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = 0.14f)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .padding(9.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent
            )
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
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            Color.White.copy(alpha = 0.86f)
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InitialsAvatar(
                initials = initials(member.name),
                seed = member.id,
                size = 34.dp
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodySmall,
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
