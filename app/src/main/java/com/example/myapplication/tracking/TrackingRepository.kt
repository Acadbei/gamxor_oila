package com.example.myapplication.tracking

import android.content.Context
import android.location.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class TrackingRepository private constructor(
    private val routePointDao: RoutePointDao
) {
    val allRoutePoints: Flow<List<RoutePointEntity>> = routePointDao.observeAllPoints()
    val allRouteLatLng: Flow<List<LatLng>> = allRoutePoints.map { points -> points.map { it.toLatLng() } }
    val allRoutes: Flow<List<List<LatLng>>> = allRoutePoints.map { points ->
        points.groupBy { it.routeId }.values.map { routePoints ->
            routePoints.map { it.toLatLng() }
        }
    }
    val routeIds: Flow<List<String>> = routePointDao.observeRouteIds()

    fun observeRoute(routeId: String): Flow<List<LatLng>> {
        return routePointDao.observeRoute(routeId).map { points -> points.map { it.toLatLng() } }
    }

    suspend fun newRouteId(): String = "${System.currentTimeMillis()}-${UUID.randomUUID()}"

    suspend fun latestRouteId(): String? = routePointDao.latestRouteId()

    suspend fun insertLocation(routeId: String, location: Location) {
        val lastPoint = routePointDao.latestPoint(routeId)
        if (lastPoint != null && !shouldPersist(location, lastPoint)) return

        routePointDao.insert(
            RoutePointEntity(
                routeId = routeId,
                latitude = location.latitude,
                longitude = location.longitude,
                accuracyMeters = location.accuracy.takeIf { location.hasAccuracy() },
                speedMetersPerSecond = location.speed.takeIf { location.hasSpeed() },
                bearingDegrees = location.bearing.takeIf { location.hasBearing() },
                timestamp = location.time.takeIf { it > 0L } ?: System.currentTimeMillis()
            )
        )
    }

    suspend fun clear() = routePointDao.clear()

    private fun shouldPersist(location: Location, lastPoint: RoutePointEntity): Boolean {
        val distanceMeters = distanceMeters(
            lat1 = lastPoint.latitude,
            lon1 = lastPoint.longitude,
            lat2 = location.latitude,
            lon2 = location.longitude
        )
        val elapsedMs = kotlin.math.abs((location.time.takeIf { it > 0L } ?: System.currentTimeMillis()) - lastPoint.timestamp)
        if (distanceMeters < MIN_DISTANCE_METERS && elapsedMs < MAX_STALE_INTERVAL_MS) return false
        if (location.hasAccuracy() && location.accuracy > MAX_ACCURACY_METERS) return false
        return true
    }

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radiusMeters = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        return radiusMeters * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    companion object {
        private const val MIN_DISTANCE_METERS = 8.0
        private const val MAX_ACCURACY_METERS = 80f
        private const val MAX_STALE_INTERVAL_MS = 30_000L

        @Volatile
        private var INSTANCE: TrackingRepository? = null

        fun getInstance(context: Context): TrackingRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TrackingRepository(
                    TrackingDatabase.getInstance(context).routePointDao()
                ).also { INSTANCE = it }
            }
        }
    }
}
