package com.example.myapplication.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.location.LocationManagerCompat
import java.io.IOException
import java.util.Locale

data class DeviceLocationSnapshot(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val placeLabel: String
)

fun hasLocationPermissions(context: Context): Boolean {
    val finePermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PermissionChecker.PERMISSION_GRANTED
    val coarsePermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PermissionChecker.PERMISSION_GRANTED
    return finePermission || coarsePermission
}

@SuppressLint("MissingPermission")
fun fetchCurrentDeviceLocation(
    context: Context,
    onResult: (DeviceLocationSnapshot?) -> Unit
) {
    if (!hasLocationPermissions(context)) {
        onResult(null)
        return
    }

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (locationManager == null) {
        onResult(null)
        return
    }

    val provider = when {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
        else -> null
    }

    if (provider == null) {
        onResult(null)
        return
    }

    LocationManagerCompat.getCurrentLocation(
        locationManager,
        provider,
        CancellationSignal(),
        ContextCompat.getMainExecutor(context)
    ) { location ->
        if (location == null) {
            onResult(null)
            return@getCurrentLocation
        }

        val address = resolveAddress(context, location.latitude, location.longitude)
        onResult(
            DeviceLocationSnapshot(
                latitude = location.latitude,
                longitude = location.longitude,
                address = address,
                placeLabel = "Jonli joylashuv"
            )
        )
    }
}

private fun resolveAddress(context: Context, latitude: Double, longitude: Double): String {
    return runCatching {
        val geocoder = Geocoder(context, Locale.forLanguageTag("uz"))
        @Suppress("DEPRECATION")
        val address = geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
        listOfNotNull(
            address?.thoroughfare,
            address?.subLocality,
            address?.locality
        ).joinToString(", ").ifBlank {
            "${"%.5f".format(Locale.US, latitude)}, ${"%.5f".format(Locale.US, longitude)}"
        }
    }.recover {
        if (it is IOException) {
            "${"%.5f".format(Locale.US, latitude)}, ${"%.5f".format(Locale.US, longitude)}"
        } else {
            "${"%.5f".format(Locale.US, latitude)}, ${"%.5f".format(Locale.US, longitude)}"
        }
    }.getOrDefault("${"%.5f".format(Locale.US, latitude)}, ${"%.5f".format(Locale.US, longitude)}")
}
