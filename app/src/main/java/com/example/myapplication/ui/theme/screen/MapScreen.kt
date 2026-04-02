package com.example.myapplication.ui.theme.screen

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.R
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.data.model.FamilyMember
import com.example.myapplication.ui.component.InitialsAvatar
import com.example.myapplication.ui.component.initialsFromName
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

@Composable
fun FamilyMapScreen(
    modifier: Modifier = Modifier,
    uiState: DemoUiState,
    onSelectMember: (Int) -> Unit
) {
    val context = LocalContext.current
    val household = remember(uiState.selfMember, uiState.members) {
        listOf(uiState.selfMember) + uiState.members
    }
    val activeSosMemberIds = remember(uiState.activeSosAlerts) {
        uiState.activeSosAlerts.map { it.memberId }.toSet()
    }
    val selectedMember = household.firstOrNull { it.id == uiState.selectedMemberId } ?: uiState.selfMember
    val defaultPoint = GeoPoint(selectedMember.lat, selectedMember.lon)
    val markerMap = remember { mutableMapOf<Int, Marker>() }
    var openInfoMemberId by rememberSaveable { mutableStateOf<Int?>(null) }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(12.8)
            controller.setCenter(defaultPoint)
        }
    }

    DisposableEffect(mapView) {
        onDispose { mapView.onDetach() }
    }

    LaunchedEffect(household, activeSosMemberIds) {
        mapView.overlays.clear()
        markerMap.clear()

        mapView.overlays.add(
            MapEventsOverlay(
                object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        openInfoMemberId = null
                        InfoWindow.closeAllInfoWindowsOn(mapView)
                        mapView.invalidate()
                        return false
                    }

                    override fun longPressHelper(p: GeoPoint?): Boolean = false
                }
            )
        )

        household.forEach { member ->
            val hasActiveSos = member.id in activeSosMemberIds
            val marker = Marker(mapView).apply {
                position = GeoPoint(member.lat, member.lon)
                icon = createAvatarMarkerDrawable(
                    context = mapView.context,
                    seed = member.avatarSeed,
                    isSelected = member.id == selectedMember.id,
                    hasActiveSos = hasActiveSos
                )
                infoWindow = FamilyMemberInfoWindow(
                    mapView = mapView,
                    member = member,
                    hasActiveSos = hasActiveSos
                )
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { tappedMarker, _ ->
                    openInfoMemberId = member.id
                    onSelectMember(member.id)
                    InfoWindow.closeAllInfoWindowsOn(mapView)
                    tappedMarker.showInfoWindow()
                    true
                }
            }

            markerMap[member.id] = marker
            mapView.overlays.add(marker)
        }

        mapView.invalidate()
    }

    LaunchedEffect(selectedMember.id, activeSosMemberIds) {
        markerMap.forEach { (memberId, marker) ->
            val member = household.firstOrNull { it.id == memberId } ?: return@forEach
            marker.icon = createAvatarMarkerDrawable(
                context = mapView.context,
                seed = member.avatarSeed,
                isSelected = member.id == selectedMember.id,
                hasActiveSos = memberId in activeSosMemberIds
            )
        }

        mapView.controller.animateTo(GeoPoint(selectedMember.lat, selectedMember.lon))
        mapView.controller.setZoom(14.1)

        InfoWindow.closeAllInfoWindowsOn(mapView)
        openInfoMemberId?.let { memberId ->
            markerMap[memberId]?.showInfoWindow()
        }
        mapView.invalidate()
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.08f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.12f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f))
            ) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(household, key = { it.id }) { member ->
                        MapPersonChip(
                            member = member,
                            isSelected = member.id == selectedMember.id,
                            hasActiveSos = member.id in activeSosMemberIds,
                            onClick = {
                                onSelectMember(member.id)
                                openInfoMemberId = member.id
                                InfoWindow.closeAllInfoWindowsOn(mapView)
                                markerMap[member.id]?.showInfoWindow()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MapPersonChip(
    member: FamilyMember,
    isSelected: Boolean,
    hasActiveSos: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        hasActiveSos -> MaterialTheme.colorScheme.error.copy(alpha = 0.14f)
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        else -> Color.Transparent
    }
    val textColor = when {
        hasActiveSos -> MaterialTheme.colorScheme.error
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .size(width = 72.dp, height = 98.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                InitialsAvatar(
                    initials = initialsFromName(member.name),
                    seed = member.avatarSeed,
                    size = 44.dp
                )
                if (hasActiveSos) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error
                    ) {
                        Box(modifier = Modifier.size(10.dp))
                    }
                }
            }
            Text(
                text = member.name.substringBefore(" "),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun createAvatarMarkerDrawable(
    context: Context,
    seed: Int,
    isSelected: Boolean,
    hasActiveSos: Boolean
): Drawable {
    val density = context.resources.displayMetrics.density
    val width = (70 * density).toInt()
    val height = (78 * density).toInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val centerX = width / 2f
    val bubbleRadius = 22f * density
    val bubbleCenterY = 27f * density
    val bubbleBottomY = bubbleCenterY + bubbleRadius - 1f * density
    val tipY = height - 8f * density
    val pointerPath = Path().apply {
        moveTo(centerX, tipY)
        cubicTo(
            centerX + 4f * density,
            tipY - 5f * density,
            centerX + 8f * density,
            bubbleBottomY - 3f * density,
            centerX + 9f * density,
            bubbleBottomY - 5f * density
        )
        lineTo(centerX - 9f * density, bubbleBottomY - 5f * density)
        cubicTo(
            centerX - 8f * density,
            bubbleBottomY - 3f * density,
            centerX - 4f * density,
            tipY - 5f * density,
            centerX,
            tipY
        )
        close()
    }

    val (startColor, endColor, iconColor) = markerPalette(seed, hasActiveSos)
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            startColor,
            endColor,
            Shader.TileMode.CLAMP
        )
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = if (isSelected) 3.2f * density else 2.1f * density
        color = if (isSelected) 0xFFFFFFFF.toInt() else 0xE6FFFFFF.toInt()
    }
    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x26000000
    }
    val faceBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
    }
    val facePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = iconColor
    }

    canvas.save()
    canvas.translate(0f, 3f * density)
    canvas.drawCircle(centerX, bubbleCenterY, bubbleRadius, shadowPaint)
    canvas.drawPath(pointerPath, shadowPaint)
    canvas.restore()
    canvas.drawCircle(centerX, bubbleCenterY, bubbleRadius, fillPaint)
    canvas.drawPath(pointerPath, fillPaint)
    canvas.drawCircle(centerX, bubbleCenterY, bubbleRadius, strokePaint)
    canvas.drawPath(pointerPath, strokePaint)

    canvas.drawCircle(centerX, bubbleCenterY, bubbleRadius - 6.5f * density, faceBackgroundPaint)
    canvas.drawCircle(centerX, bubbleCenterY - 4.5f * density, 7f * density, facePaint)
    canvas.drawRoundRect(
        RectF(
            centerX - 13f * density,
            bubbleCenterY + 3f * density,
            centerX + 13f * density,
            bubbleCenterY + 15f * density
        ),
        12f * density,
        12f * density,
        facePaint
    )

    return BitmapDrawable(context.resources, bitmap)
}

private fun markerPalette(
    seed: Int,
    hasActiveSos: Boolean
): Triple<Int, Int, Int> {
    if (hasActiveSos) {
        return when (seed.mod(3)) {
            0 -> Triple(0xFFD32F2F.toInt(), 0xFFF05B5B.toInt(), 0xFFC62828.toInt())
            1 -> Triple(0xFFC62828.toInt(), 0xFFEF6C6C.toInt(), 0xFFB71C1C.toInt())
            else -> Triple(0xFFB71C1C.toInt(), 0xFFE85D5D.toInt(), 0xFF991B1B.toInt())
        }
    }

    return when (seed.mod(5)) {
        0 -> Triple(0xFF0F4FD6.toInt(), 0xFF3B82F6.toInt(), 0xFF1A49B8.toInt())
        1 -> Triple(0xFF1E63E9.toInt(), 0xFF5CA2FF.toInt(), 0xFF2055C5.toInt())
        2 -> Triple(0xFF2147A7.toInt(), 0xFF4A7EEA.toInt(), 0xFF1E459C.toInt())
        3 -> Triple(0xFF2A6DE0.toInt(), 0xFF7CB4FF.toInt(), 0xFF1D5CC7.toInt())
        else -> Triple(0xFF163B8C.toInt(), 0xFF4C8FFF.toInt(), 0xFF12377F.toInt())
    }
}

private class FamilyMemberInfoWindow(
    mapView: MapView,
    private val member: FamilyMember,
    private val hasActiveSos: Boolean
) : InfoWindow(R.layout.view_map_member_info, mapView) {

    override fun onOpen(item: Any?) {
        val density = mView.resources.displayMetrics.density
        val accentColor = if (hasActiveSos) 0xFFD32F2F.toInt() else 0xFF0F4FD6.toInt()
        val softAccent = if (hasActiveSos) 0xFFFFE3E3.toInt() else 0xFFDCEBFF.toInt()

        mView.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 26f * density
            setColor(0xFFFFFFFF.toInt())
            setStroke((1.5f * density).toInt().coerceAtLeast(1), accentColor)
        }
        mView.elevation = 12f * density

        val badge = mView.findViewById<TextView>(R.id.map_info_badge)
        val name = mView.findViewById<TextView>(R.id.map_info_name)
        val relation = mView.findViewById<TextView>(R.id.map_info_relation)
        val place = mView.findViewById<TextView>(R.id.map_info_place)
        val phone = mView.findViewById<TextView>(R.id.map_info_phone)

        badge.text = if (hasActiveSos) "SOS" else member.relation
        badge.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 999f
            setColor(softAccent)
        }
        badge.backgroundTintList = ColorStateList.valueOf(softAccent)
        badge.setTextColor(accentColor)

        name.text = member.name
        relation.text = if (hasActiveSos) {
            "${member.relation} • Tezkor nazorat kerak"
        } else {
            member.relation
        }
        place.text = member.placeLabel
        phone.text = member.phone
    }

    override fun onClose() = Unit
}
