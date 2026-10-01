package com.example.ui.theme

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

// Samsung One UI AMOLED Dark Palette (Pitch Black for Galaxy A53 Super AMOLED)
val OneUiDarkColorScheme = darkColorScheme(
    primary = OneUiBlueDark,
    onPrimary = Color.White,
    primaryContainer = OneUiBlueContainerDark,
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = OneUiSecondaryDark,
    onSecondary = Color(0xFF1E293B),
    secondaryContainer = OneUiCardDark,
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = OneUiViolet,
    background = OneUiAmoledBlack, // Pure AMOLED Pitch Black
    onBackground = Color(0xFFF1F5F9),
    surface = OneUiSurfaceDark,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = OneUiCardDark,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = OneUiBorderDark,
    outlineVariant = Color(0xFF1F2430)
)

// Samsung One UI Porcelain Light Palette
val OneUiLightColorScheme = lightColorScheme(
    primary = OneUiBlue,
    onPrimary = Color.White,
    primaryContainer = OneUiBlueContainerLight,
    onPrimaryContainer = Color(0xFF003882),
    secondary = OneUiSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = OneUiCardVariantLight,
    onSecondaryContainer = Color(0xFF1E293B),
    tertiary = OneUiViolet,
    background = OneUiBackgroundLight,
    onBackground = Color(0xFF0F172A),
    surface = OneUiPorcelainWhite,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = OneUiCardVariantLight,
    onSurfaceVariant = Color(0xFF475569),
    outline = OneUiBorderLight,
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Allow dynamic color on Android 12+ if desired, but default to Samsung One UI brand palette
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> OneUiDarkColorScheme
        else -> OneUiLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = OneUiShapes,
        content = content
    )
}
