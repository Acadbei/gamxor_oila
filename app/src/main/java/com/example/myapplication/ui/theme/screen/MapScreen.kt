package com.example.myapplication.ui.theme.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.data.model.FamilyMember
import com.example.myapplication.ui.component.InitialsAvatar
import com.example.myapplication.ui.component.initialsFromName
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
    val household = remember(uiState.selfMember, uiState.members) {
        listOf(uiState.selfMember) + uiState.members
    }
    val selectedMember = household.firstOrNull { it.id == uiState.selectedMemberId } ?: uiState.selfMember
    val defaultPoint = GeoPoint(selectedMember.lat, selectedMember.lon)
    val markerMap = remember { mutableMapOf<Int, Marker>() }
    val unused = onAction

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

    LaunchedEffect(household) {
        mapView.overlays.clear()
        markerMap.clear()

        household.forEach { member ->
            val point = GeoPoint(member.lat, member.lon)
            val marker = Marker(mapView).apply {
                position = point
                icon = createAvatarMarkerDrawable(
                    context = context,
                    initials = initialsFromName(member.name),
                    seed = member.avatarSeed,
                    isSelected = member.id == selectedMember.id
                )
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

    LaunchedEffect(selectedMember.id) {
        markerMap.forEach { (memberId, marker) ->
            val member = household.firstOrNull { it.id == memberId } ?: return@forEach
            marker.icon = createAvatarMarkerDrawable(
                context = context,
                initials = initialsFromName(member.name),
                seed = member.avatarSeed,
                isSelected = member.id == selectedMember.id
            )
        }

        mapView.controller.animateTo(GeoPoint(selectedMember.lat, selectedMember.lon))
        mapView.controller.setZoom(14.1)
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
                            onClick = { onSelectMember(member.id) }
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
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        } else {
            Color.Transparent
        }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .size(width = 68.dp, height = 92.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InitialsAvatar(
                initials = initialsFromName(member.name),
                seed = member.avatarSeed,
                size = 44.dp
            )
            Text(
                text = member.name.substringBefore(" "),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun createAvatarMarkerDrawable(
    context: Context,
    initials: String,
    seed: Int,
    isSelected: Boolean
): Drawable {
    val density = context.resources.displayMetrics.density
    val width = (70 * density).toInt()
    val height = (90 * density).toInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val centerX = width / 2f
    val circleRadius = 21f * density
    val centerY = 28f * density
    val tailBottom = height - 8f * density
    val bodyRect = RectF(
        centerX - circleRadius,
        centerY - circleRadius,
        centerX + circleRadius,
        centerY + circleRadius
    )

    val pinPath = Path().apply {
        moveTo(centerX, tailBottom)
        cubicTo(
            centerX + 14f * density,
            tailBottom - 16f * density,
            centerX + circleRadius,
            centerY + 18f * density,
            centerX + circleRadius,
            centerY + 5f * density
        )
        arcTo(bodyRect, 12f, -204f, false)
        cubicTo(
            centerX - circleRadius,
            centerY + 18f * density,
            centerX - 14f * density,
            tailBottom - 16f * density,
            centerX,
            tailBottom
        )
        close()
    }

    val (startColor, endColor) = markerPalette(seed)
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
        strokeWidth = if (isSelected) 3f * density else 2f * density
        color = if (isSelected) 0xFFEEF5FF.toInt() else 0xD9FFFFFF.toInt()
    }
    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x240C234F
    }
    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x26FFFFFF
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textAlign = Paint.Align.CENTER
        textSize = 15f * density
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT_BOLD, android.graphics.Typeface.BOLD)
    }

    canvas.save()
    canvas.translate(0f, 3f * density)
    canvas.drawPath(pinPath, shadowPaint)
    canvas.restore()
    canvas.drawPath(pinPath, fillPaint)
    canvas.drawPath(pinPath, strokePaint)
    canvas.drawCircle(centerX, centerY, circleRadius - 6f * density, innerPaint)

    val bounds = Rect()
    textPaint.getTextBounds(initials, 0, initials.length, bounds)
    val baseline = centerY + bounds.height() / 2f
    canvas.drawText(initials, centerX, baseline, textPaint)

    return BitmapDrawable(context.resources, bitmap)
}

private fun markerPalette(seed: Int): Pair<Int, Int> {
    return when (seed.mod(5)) {
        0 -> 0xFF0F4FD6.toInt() to 0xFF3B82F6.toInt()
        1 -> 0xFF1E63E9.toInt() to 0xFF5CA2FF.toInt()
        2 -> 0xFF2147A7.toInt() to 0xFF4A7EEA.toInt()
        3 -> 0xFF2A6DE0.toInt() to 0xFF7CB4FF.toInt()
        else -> 0xFF163B8C.toInt() to 0xFF4C8FFF.toInt()
    }
}
