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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.data.model.FamilyMember
import com.example.myapplication.ui.component.InfoLine
import com.example.myapplication.ui.component.StatusPill
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
            marker.alpha = if (memberId == selectedMember?.id) 1f else 0.84f
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
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Jonli xarita",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tanlangan a'zoni marker, chip yoki dashboard orqali markazga olib kelish mumkin.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

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
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
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
                            Column {
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${member.relation} • ${member.placeLabel}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusPill(status = member.status)
                        }

                        InfoLine("Manzil", member.address)
                        InfoLine("Safe zone", member.safeZone)
                        InfoLine("Oxirgi signal", member.lastUpdate)
                        InfoLine("Batareya va puls", "${member.battery}% • ${member.heartRate} bpm")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { onAction("${member.name} uchun qo'ng'iroq demo oynasi ochildi.") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Qo'ng'iroq")
                            }
                            OutlinedButton(
                                onClick = { onAction("${member.name} uchun yo'nalish demosi tayyorlandi.") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Yo'nalish")
                            }
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
    val background = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    }

    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = background
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column {
                Text(text = member.name, fontWeight = FontWeight.SemiBold)
                Text(
                    text = member.relation,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
