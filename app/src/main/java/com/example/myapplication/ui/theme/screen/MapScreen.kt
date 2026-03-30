package com.example.myapplication.ui.theme.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

import com.example.myapplication.viewmodel.LocationViewModel

@Composable
fun MapWithStoryFromServer(
    viewModel: LocationViewModel = viewModel()
) {
    val context = LocalContext.current
    val users by viewModel.users.collectAsState()

    val defaultPoint = GeoPoint(41.3111, 69.2797)

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(12.0)
            controller.setCenter(defaultPoint)
        }
    }

    LaunchedEffect(users) {
        mapView.overlays.clear()

        users.forEach { user ->
            val point = GeoPoint(user.lat, user.lon)

            val marker = Marker(mapView).apply {
                position = point
                title = user.name
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }

            mapView.overlays.add(marker)
        }

        if (users.isNotEmpty()) {
            val firstUserPoint = GeoPoint(users[0].lat, users[0].lon)
            mapView.controller.setCenter(firstUserPoint)
        }

        mapView.invalidate()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 12.dp, end = 12.dp)
                .background(Color(0xFFF2F2F7), RoundedCornerShape(20.dp))
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(users) { user ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        val point = GeoPoint(user.lat, user.lon)
                        mapView.controller.animateTo(point)
                        mapView.controller.setZoom(15.0)
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.LightGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = user.name,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}