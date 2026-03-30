package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Evergreen,
    onPrimary = Color.White,
    secondary = Honey,
    onSecondary = Ink,
    tertiary = Meadow,
    onTertiary = Color.White,
    background = Mist,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Linen,
    onSurfaceVariant = Slate,
    error = Clay,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = NightMeadow,
    onPrimary = NightGreen,
    secondary = NightHoney,
    onSecondary = NightGreen,
    tertiary = Meadow,
    onTertiary = NightGreen,
    background = NightGreen,
    onBackground = NightInk,
    surface = NightSurface,
    onSurface = NightInk,
    surfaceVariant = Color(0xFF25413C),
    onSurfaceVariant = Color(0xFFB4C7C0),
    error = Color(0xFFFFB59F),
    onError = NightGreen
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
