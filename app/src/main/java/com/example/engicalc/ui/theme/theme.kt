package com.example.engicalc.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- 1. DARK COLOR PALETTE ---
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF9F0A), // Your signature premium orange
    secondary = Color.LightGray,
    tertiary = Color(0xFFE57373), // Red for "Clear" actions
    background = Color.Black,     // Pitch black background
    surface = Color(0xFF171717),  // Dark gray for button containers/surfaces
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

// --- 2. LIGHT COLOR PALETTE ---
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFF9F0A),
    secondary = Color.DarkGray,
    tertiary = Color(0xFFE57373),
    background = Color(0xFFF3F3F3),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// --- 3. THE MAIN THEME COMPOSABLE ---
@Composable
fun EngiCalcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor to false by default to ensure the calculator keeps its premium colors
    // rather than inheriting pastel colors from the user's Android 12+ wallpaper.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme // You can also force DarkColorScheme here if you want a dark-only app
    }

    // Configures the system status bar at the top of the phone
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // This is the bridge! It combines your colors and your Type.kt file.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // <-- Hooks into your custom fonts!
        content = content
    )
}