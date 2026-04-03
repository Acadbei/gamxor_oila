package com.example.myapplication.ui.theme.screen

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.ColorUtils
import com.example.myapplication.R
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.data.model.FamilyMember
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.io.File
import kotlin.math.roundToInt

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
    val avatarBitmaps = remember(household, context) {
        household.associate { member ->
            member.id to loadAvatarBitmap(context, member.avatarUri)
        }
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
                    avatarBitmap = avatarBitmaps[member.id],
                    isSelected = member.id == selectedMember.id,
                    hasActiveSos = hasActiveSos
                )
                infoWindow = FamilyMemberInfoWindow(
                    mapView = mapView,
                    member = member,
                    hasActiveSos = hasActiveSos
                )
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
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
                avatarBitmap = avatarBitmaps[member.id],
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
                MemberAvatarImage(
                    member = member,
                    size = 44.dp,
                    hasActiveSos = hasActiveSos
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

@Composable
private fun MemberAvatarImage(
    member: FamilyMember,
    size: Dp,
    hasActiveSos: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sizePx = with(LocalDensity.current) { size.roundToPx().coerceAtLeast(1) }
    val sourceAvatar = remember(member.avatarUri, context) {
        loadAvatarBitmap(context, member.avatarUri)
    }
    val portraitBitmap = remember(
        member.avatarSeed,
        member.avatarUri,
        sizePx,
        hasActiveSos,
        sourceAvatar
    ) {
        createPortraitAvatarBitmap(
            sizePx = sizePx,
            seed = member.avatarSeed,
            avatarBitmap = sourceAvatar,
            hasActiveSos = hasActiveSos
        )
    }

    Image(
        bitmap = portraitBitmap.asImageBitmap(),
        contentDescription = member.name,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.82f), CircleShape),
        contentScale = ContentScale.Crop
    )
}

private fun createAvatarMarkerDrawable(
    context: Context,
    seed: Int,
    avatarBitmap: Bitmap?,
    isSelected: Boolean,
    hasActiveSos: Boolean
): Drawable {
    val density = context.resources.displayMetrics.density
    val width = (84f * density).roundToInt()
    val height = width
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val accentColor = markerPalette(seed, hasActiveSos).third
    val shellRect = RectF(
        8f * density,
        8f * density,
        width - 8f * density,
        height - 8f * density
    )
    val photoShellRect = RectF(
        shellRect.left + 6f * density,
        shellRect.top + 6f * density,
        shellRect.right - 6f * density,
        shellRect.bottom - 6f * density
    )
    val portraitRect = RectF(
        photoShellRect.left + 3f * density,
        photoShellRect.top + 3f * density,
        photoShellRect.right - 3f * density,
        photoShellRect.bottom - 3f * density
    )

    val shellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f,
            shellRect.top,
            0f,
            shellRect.bottom,
            0xFFF6F1EB.toInt(),
            0xFFFFFFFF.toInt(),
            Shader.TileMode.CLAMP
        )
    }
    val shellStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = if (isSelected || hasActiveSos) 2.4f * density else 1.4f * density
        color = when {
            hasActiveSos -> ColorUtils.setAlphaComponent(accentColor, 226)
            isSelected -> ColorUtils.setAlphaComponent(accentColor, 150)
            else -> AndroidColor.argb(28, 95, 102, 119)
        }
    }
    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.argb(28, 61, 57, 77)
    }
    val groundShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.argb(24, 74, 70, 90)
    }
    val portraitBitmap = createPortraitAvatarBitmap(
        sizePx = portraitRect.width().roundToInt().coerceAtLeast(1),
        seed = seed,
        avatarBitmap = avatarBitmap,
        hasActiveSos = hasActiveSos
    )
    val photoFramePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f,
            photoShellRect.top,
            0f,
            photoShellRect.bottom,
            0xFFF4EFE9.toInt(),
            0xFFFBFAF8.toInt(),
            Shader.TileMode.CLAMP
        )
    }
    val photoFrameStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.4f * density
        color = AndroidColor.argb(38, 255, 255, 255)
    }
    val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (hasActiveSos) {
            accentColor
        } else {
            ColorUtils.setAlphaComponent(accentColor, if (isSelected) 176 else 116)
        }
    }
    val badgeHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ColorUtils.setAlphaComponent(accentColor, if (hasActiveSos) 72 else 42)
    }
    val portraitPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    canvas.save()
    canvas.translate(0f, 4f * density)
    canvas.drawOval(shellRect, shadowPaint)
    canvas.restore()

    canvas.drawOval(
        RectF(
            shellRect.left + 12f * density,
            shellRect.bottom - 6f * density,
            shellRect.right - 12f * density,
            shellRect.bottom + 4f * density
        ),
        groundShadowPaint
    )
    canvas.drawOval(shellRect, shellPaint)

    canvas.drawOval(
        RectF(
            photoShellRect.left + 1.5f * density,
            photoShellRect.top + 3.5f * density,
            photoShellRect.right + 1.5f * density,
            photoShellRect.bottom + 4.5f * density
        ),
        shadowPaint
    )
    canvas.drawOval(photoShellRect, photoFramePaint)
    canvas.drawBitmap(portraitBitmap, null, portraitRect, portraitPaint)
    canvas.drawOval(photoShellRect, photoFrameStrokePaint)

    if (isSelected || hasActiveSos) {
        val badgeCx = photoShellRect.right - 4f * density
        val badgeCy = photoShellRect.top + 7f * density
        val badgeRadius = 5.2f * density
        canvas.drawCircle(badgeCx, badgeCy, badgeRadius + 3f * density, badgeHaloPaint)
        canvas.drawCircle(badgeCx, badgeCy, badgeRadius, badgePaint)
    }
    canvas.drawOval(shellRect, shellStrokePaint)

    return BitmapDrawable(context.resources, bitmap)
}

private fun buildPhotoPinPath(
    rect: RectF,
    tipY: Float,
    density: Float
): Path {
    val centerX = rect.centerX()
    val archBaseY = rect.top + rect.width() * 0.84f
    val lowerControlY = rect.bottom - 4f * density
    val sideInset = 11f * density
    val tipCurveHeight = 10f * density

    return Path().apply {
        moveTo(centerX, tipY)
        cubicTo(
            centerX - 9f * density,
            tipY - tipCurveHeight,
            rect.left + sideInset,
            rect.bottom - 2f * density,
            rect.left,
            lowerControlY
        )
        lineTo(rect.left, archBaseY)
        arcTo(
            RectF(
                rect.left,
                rect.top,
                rect.right,
                rect.top + rect.width()
            ),
            180f,
            -180f,
            false
        )
        lineTo(rect.right, lowerControlY)
        cubicTo(
            rect.right - sideInset,
            rect.bottom - 2f * density,
            centerX + 9f * density,
            tipY - tipCurveHeight,
            centerX,
            tipY
        )
        close()
    }
}

private fun createPortraitAvatarBitmap(
    sizePx: Int,
    seed: Int,
    avatarBitmap: Bitmap?,
    hasActiveSos: Boolean
): Bitmap {
    val safeSize = sizePx.coerceAtLeast(1)
    return Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888).also { portrait ->
        val canvas = Canvas(portrait)
        drawAvatarCircle(
            canvas = canvas,
            rect = RectF(0f, 0f, safeSize.toFloat(), safeSize.toFloat()),
            seed = seed,
            avatarBitmap = avatarBitmap,
            hasActiveSos = hasActiveSos
        )
    }
}

private fun drawAvatarCircle(
    canvas: Canvas,
    rect: RectF,
    seed: Int,
    avatarBitmap: Bitmap?,
    hasActiveSos: Boolean
) {
    val clipPath = Path().apply {
        addOval(rect, Path.Direction.CW)
    }
    canvas.save()
    canvas.clipPath(clipPath)
    if (avatarBitmap != null) {
        drawBitmapCenterCrop(canvas, rect, avatarBitmap)
    } else {
        drawGeneratedAvatarPortrait(
            canvas = canvas,
            rect = rect,
            seed = seed,
            hasActiveSos = hasActiveSos
        )
    }
    canvas.restore()
}

private fun drawGeneratedAvatarPortrait(
    canvas: Canvas,
    rect: RectF,
    seed: Int,
    hasActiveSos: Boolean
) {
    val palette = portraitPalette(seed, hasActiveSos)
    val featureColor = AndroidColor.argb(170, 58, 43, 37)
    val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            palette.backgroundStart,
            palette.backgroundEnd,
            Shader.TileMode.CLAMP
        )
    }
    val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ColorUtils.setAlphaComponent(AndroidColor.WHITE, if (hasActiveSos) 66 else 48)
    }
    val shoulderShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.argb(20, 0, 0, 0)
    }
    val jacketPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = palette.shirtColor
    }
    val collarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.argb(240, 255, 255, 255)
    }
    val skinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = palette.skinColor
    }
    val hairPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = palette.hairColor
    }
    val featurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = featureColor
        style = Paint.Style.STROKE
        strokeWidth = rect.width() * 0.032f
        strokeCap = Paint.Cap.ROUND
    }
    val glassesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.argb(118, 54, 53, 65)
        style = Paint.Style.STROKE
        strokeWidth = rect.width() * 0.018f
    }

    canvas.drawRect(rect, backgroundPaint)
    canvas.drawCircle(
        rect.right - rect.width() * 0.18f,
        rect.top + rect.height() * 0.18f,
        rect.width() * 0.22f,
        glowPaint
    )

    val centerX = rect.centerX()
    val shoulderRect = RectF(
        centerX - rect.width() * 0.38f,
        rect.bottom - rect.height() * 0.30f,
        centerX + rect.width() * 0.38f,
        rect.bottom + rect.height() * 0.10f
    )
    canvas.drawOval(
        RectF(
            shoulderRect.left,
            shoulderRect.top + rect.height() * 0.06f,
            shoulderRect.right,
            shoulderRect.bottom
        ),
        shoulderShadowPaint
    )
    canvas.drawRoundRect(
        shoulderRect,
        rect.width() * 0.24f,
        rect.width() * 0.24f,
        jacketPaint
    )

    val collarPath = Path().apply {
        moveTo(centerX - rect.width() * 0.12f, shoulderRect.top)
        lineTo(centerX, shoulderRect.top + rect.height() * 0.12f)
        lineTo(centerX + rect.width() * 0.12f, shoulderRect.top)
        close()
    }
    canvas.drawPath(collarPath, collarPaint)

    val neckRect = RectF(
        centerX - rect.width() * 0.065f,
        rect.top + rect.height() * 0.50f,
        centerX + rect.width() * 0.065f,
        rect.top + rect.height() * 0.64f
    )
    canvas.drawRoundRect(
        neckRect,
        rect.width() * 0.04f,
        rect.width() * 0.04f,
        skinPaint
    )

    val headCenterY = rect.top + rect.height() * 0.39f
    val headRadius = rect.width() * 0.20f
    canvas.drawCircle(centerX, headCenterY, headRadius, skinPaint)
    drawHairStyle(
        canvas = canvas,
        centerX = centerX,
        headCenterY = headCenterY,
        headRadius = headRadius,
        seed = seed,
        hairPaint = hairPaint
    )

    val eyeY = headCenterY + rect.height() * 0.02f
    val eyeOffset = headRadius * 0.38f
    val eyeHalfWidth = rect.width() * 0.03f
    canvas.drawLine(
        centerX - eyeOffset - eyeHalfWidth,
        eyeY,
        centerX - eyeOffset + eyeHalfWidth,
        eyeY,
        featurePaint
    )
    canvas.drawLine(
        centerX + eyeOffset - eyeHalfWidth,
        eyeY,
        centerX + eyeOffset + eyeHalfWidth,
        eyeY,
        featurePaint
    )
    canvas.drawArc(
        RectF(
            centerX - headRadius * 0.22f,
            headCenterY + headRadius * 0.12f,
            centerX + headRadius * 0.22f,
            headCenterY + headRadius * 0.42f
        ),
        12f,
        156f,
        false,
        featurePaint
    )

    if (seed.mod(4) == 1 || seed.mod(4) == 3) {
        val frameTop = eyeY - rect.width() * 0.03f
        val frameBottom = eyeY + rect.width() * 0.035f
        val frameRadius = rect.width() * 0.025f
        val leftFrame = RectF(
            centerX - eyeOffset - rect.width() * 0.07f,
            frameTop,
            centerX - eyeOffset + rect.width() * 0.01f,
            frameBottom
        )
        val rightFrame = RectF(
            centerX + eyeOffset - rect.width() * 0.01f,
            frameTop,
            centerX + eyeOffset + rect.width() * 0.07f,
            frameBottom
        )
        canvas.drawRoundRect(leftFrame, frameRadius, frameRadius, glassesPaint)
        canvas.drawRoundRect(rightFrame, frameRadius, frameRadius, glassesPaint)
        canvas.drawLine(leftFrame.right, eyeY, rightFrame.left, eyeY, glassesPaint)
    }
}

private fun drawHairStyle(
    canvas: Canvas,
    centerX: Float,
    headCenterY: Float,
    headRadius: Float,
    seed: Int,
    hairPaint: Paint
) {
    val fullRect = RectF(
        centerX - headRadius * 1.14f,
        headCenterY - headRadius * 1.18f,
        centerX + headRadius * 1.14f,
        headCenterY + headRadius * 1.00f
    )

    when (seed.mod(4)) {
        0 -> {
            canvas.drawArc(fullRect, 182f, 176f, true, hairPaint)
        }

        1 -> {
            val partPath = Path().apply {
                moveTo(centerX - headRadius * 1.02f, headCenterY + headRadius * 0.10f)
                lineTo(centerX - headRadius * 0.72f, headCenterY - headRadius * 0.98f)
                lineTo(centerX + headRadius * 0.90f, headCenterY - headRadius * 0.56f)
                lineTo(centerX + headRadius * 0.98f, headCenterY + headRadius * 0.20f)
                lineTo(centerX - headRadius * 0.96f, headCenterY + headRadius * 0.20f)
                close()
            }
            canvas.drawPath(partPath, hairPaint)
        }

        2 -> {
            canvas.drawRoundRect(
                RectF(
                    centerX - headRadius * 1.12f,
                    headCenterY - headRadius * 1.06f,
                    centerX + headRadius * 1.12f,
                    headCenterY + headRadius * 0.34f
                ),
                headRadius * 0.82f,
                headRadius * 0.82f,
                hairPaint
            )
            canvas.drawRoundRect(
                RectF(
                    centerX - headRadius * 0.98f,
                    headCenterY - headRadius * 0.18f,
                    centerX + headRadius * 0.98f,
                    headCenterY + headRadius * 0.18f
                ),
                headRadius * 0.28f,
                headRadius * 0.28f,
                hairPaint
            )
        }

        else -> {
            canvas.drawArc(fullRect, 184f, 176f, true, hairPaint)
            canvas.drawCircle(
                centerX + headRadius * 0.62f,
                headCenterY - headRadius * 0.82f,
                headRadius * 0.34f,
                hairPaint
            )
        }
    }
}

private data class PortraitPalette(
    val backgroundStart: Int,
    val backgroundEnd: Int,
    val shirtColor: Int,
    val hairColor: Int,
    val skinColor: Int
)

private fun portraitPalette(
    seed: Int,
    hasActiveSos: Boolean
): PortraitPalette {
    val (_, endColor, accentColor) = markerPalette(seed, hasActiveSos)
    val shirtVariants = listOf(
        accentColor,
        0xFF2D8CFF.toInt(),
        0xFF0FAF9A.toInt(),
        0xFFEA7B52.toInt(),
        0xFF7A6DFF.toInt()
    )
    val hairVariants = listOf(
        0xFF2D221D.toInt(),
        0xFF513628.toInt(),
        0xFF6D4A35.toInt(),
        0xFF1F1A18.toInt()
    )
    val skinVariants = listOf(
        0xFFF7D8C1.toInt(),
        0xFFEAC19F.toInt(),
        0xFFD8A57A.toInt(),
        0xFFC78B5D.toInt()
    )
    val shirtColor = shirtVariants[seed.mod(shirtVariants.size)]
    val hairColor = hairVariants[seed.mod(hairVariants.size)]
    val skinColor = skinVariants[(seed + 1).mod(skinVariants.size)]

    return PortraitPalette(
        backgroundStart = ColorUtils.blendARGB(accentColor, AndroidColor.WHITE, 0.78f),
        backgroundEnd = ColorUtils.blendARGB(endColor, AndroidColor.WHITE, 0.48f),
        shirtColor = ColorUtils.blendARGB(shirtColor, AndroidColor.WHITE, 0.12f),
        hairColor = hairColor,
        skinColor = skinColor
    )
}

private fun drawBitmapCenterCrop(
    canvas: Canvas,
    rect: RectF,
    bitmap: Bitmap
) {
    if (bitmap.width <= 0 || bitmap.height <= 0) return

    val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    val scale = maxOf(
        rect.width() / bitmap.width.toFloat(),
        rect.height() / bitmap.height.toFloat()
    )
    val dx = rect.left + (rect.width() - bitmap.width * scale) / 2f
    val dy = rect.top + (rect.height() - bitmap.height * scale) / 2f
    val matrix = Matrix().apply {
        setScale(scale, scale)
        postTranslate(dx, dy)
    }
    shader.setLocalMatrix(matrix)

    val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.shader = shader
    }
    canvas.drawOval(rect, bitmapPaint)
}

private fun loadAvatarBitmap(
    context: Context,
    avatarUri: String
): Bitmap? {
    val normalizedUri = avatarUri.trim()
    if (normalizedUri.isBlank()) return null

    return runCatching {
        val uri = Uri.parse(normalizedUri)
        when {
            uri.scheme == "file" -> BitmapFactory.decodeFile(uri.path)
            uri.scheme.isNullOrBlank() -> BitmapFactory.decodeFile(File(normalizedUri).absolutePath)
            else -> context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        }
    }.getOrNull()
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
