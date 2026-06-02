package com.example.myapplication.tracking

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "route_points",
    indices = [
        Index(value = ["routeId", "timestamp"]),
        Index(value = ["timestamp"])
    ]
)
data class RoutePointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routeId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?,
    val speedMetersPerSecond: Float?,
    val bearingDegrees: Float?,
    val timestamp: Long
) {
    fun toLatLng(): LatLng = LatLng(latitude = latitude, longitude = longitude)
}
