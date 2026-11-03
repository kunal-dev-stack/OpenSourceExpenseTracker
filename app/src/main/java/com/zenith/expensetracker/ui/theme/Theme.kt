package com.zenith.expensetracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.zenith.expensetracker.data.local.AccentTheme
import com.zenith.expensetracker.data.local.ThemeMode

private fun getDarkColorScheme(accent: AccentTheme, amoled: Boolean): ColorScheme {
    val bg = if (amoled) AmoledBackground else DarkBackground
    val surface = if (amoled) AmoledSurface else DarkSurface
    val surfaceVariant = if (amoled) AmoledSurfaceVariant else DarkSurfaceVariant
    val outline = if (amoled) AmoledOutline else DarkOutline

    val (primary, secondary, tertiary) = when (accent) {
        AccentTheme.EMERALD -> Triple(EmeraldPrimary, EmeraldSecondary, EmeraldTertiary)
        AccentTheme.SAPPHIRE -> Triple(SapphirePrimary, SapphireSecondary, SapphireTertiary)
        AccentTheme.VIOLET -> Triple(VioletPrimary, VioletSecondary, VioletTertiary)
        AccentTheme.SUNSET -> Triple(SunsetPrimary, SunsetSecondary, SunsetTertiary)
        AccentTheme.RUBY -> Triple(RubyPrimary, RubySecondary, RubyTertiary)
        AccentTheme.CYBER_TEAL -> Triple(CyberTealPrimary, CyberTealSecondary, CyberTealTertiary)
        AccentTheme.ROSE_GOLD -> Triple(RoseGoldPrimary, RoseGoldSecondary, RoseGoldTertiary)
    }

    return darkColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = primary.copy(alpha = 0.2f),
        onPrimaryContainer = primary,
        secondary = secondary,
        onSecondary = Color.White,
        tertiary = tertiary,
        background = bg,
        surface = surface,
        surfaceVariant = surfaceVariant,
        outline = outline,
        onBackground = DarkTextPrimary,
        onSurface = DarkTextPrimary,
        onSurfaceVariant = DarkTextSecondary
    )
}

private fun getLightColorScheme(accent: AccentTheme): ColorScheme {
    val (primary, secondary, tertiary) = when (accent) {
        AccentTheme.EMERALD -> Triple(EmeraldPrimary, EmeraldSecondary, EmeraldTertiary)
        AccentTheme.SAPPHIRE -> Triple(SapphirePrimary, SapphireSecondary, SapphireTertiary)
        AccentTheme.VIOLET -> Triple(VioletPrimary, VioletSecondary, VioletTertiary)
        AccentTheme.SUNSET -> Triple(SunsetPrimary, SunsetSecondary, SunsetTertiary)
        AccentTheme.RUBY -> Triple(RubyPrimary, RubySecondary, RubyTertiary)
        AccentTheme.CYBER_TEAL -> Triple(CyberTealPrimary, CyberTealSecondary, CyberTealTertiary)
        AccentTheme.ROSE_GOLD -> Triple(RoseGoldPrimary, RoseGoldSecondary, RoseGoldTertiary)
    }

    return lightColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = primary.copy(alpha = 0.12f),
        onPrimaryContainer = primary,
        secondary = secondary,
        onSecondary = Color.White,
        tertiary = tertiary,
        background = LightBackground,
        surface = LightSurface,
        surfaceVariant = LightSurfaceVariant,
        outline = LightOutline,
        onBackground = LightTextPrimary,
        onSurface = LightTextPrimary,
        onSurfaceVariant = LightTextSecondary
    )
}

@Composable
fun ZenithExpenseTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentTheme: AccentTheme = AccentTheme.EMERALD,
    dynamicColor: Boolean = false,
    amoledDark: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val baseScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            if (darkTheme && amoledDark) {
                baseScheme.copy(
                    background = AmoledBackground,
                    surface = AmoledSurface,
                    surfaceVariant = AmoledSurfaceVariant
                )
            } else {
                baseScheme
            }
        }
        darkTheme -> getDarkColorScheme(accentTheme, amoledDark)
        else -> getLightColorScheme(accentTheme)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
