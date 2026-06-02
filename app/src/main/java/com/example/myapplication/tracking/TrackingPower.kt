package com.example.myapplication.tracking

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

fun batteryOptimizationIntent(context: Context): Intent? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return null
    if (powerManager.isIgnoringBatteryOptimizations(context.packageName)) return null
    return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
        data = Uri.parse("package:${context.packageName}")
    }
}
