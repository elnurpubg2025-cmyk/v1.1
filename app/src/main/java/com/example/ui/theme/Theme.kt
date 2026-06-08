package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CosmicMidnightScheme = darkColorScheme(
    primary = ComicPrimary,
    secondary = ComicSecondary,
    background = ComicBackground,
    surface = ComicSurface,
    onBackground = ComicOnBackground,
    onSurface = ComicOnSurface,
    primaryContainer = ComicSecondary.copy(alpha = 0.2f),
    onPrimaryContainer = ComicPrimary,
    surfaceVariant = ComicSurface.copy(alpha = 0.8f),
    onSurfaceVariant = ComicOnSurface.copy(alpha = 0.8f)
)

val OceanBreezeScheme = darkColorScheme(
    primary = OceanPrimary,
    secondary = OceanSecondary,
    background = OceanBackground,
    surface = OceanSurface,
    onBackground = OceanOnBackground,
    onSurface = OceanOnSurface,
    primaryContainer = OceanSecondary.copy(alpha = 0.2f),
    onPrimaryContainer = OceanPrimary,
    surfaceVariant = OceanSurface.copy(alpha = 0.8f),
    onSurfaceVariant = OceanOnSurface.copy(alpha = 0.8f)
)

val EmeraldShineScheme = darkColorScheme(
    primary = EmeraldPrimary,
    secondary = EmeraldSecondary,
    background = EmeraldBackground,
    surface = EmeraldSurface,
    onBackground = EmeraldOnBackground,
    onSurface = EmeraldOnSurface,
    primaryContainer = EmeraldSecondary.copy(alpha = 0.2f),
    onPrimaryContainer = EmeraldPrimary,
    surfaceVariant = EmeraldSurface.copy(alpha = 0.8f),
    onSurfaceVariant = EmeraldOnSurface.copy(alpha = 0.8f)
)

val AmoledSlateScheme = darkColorScheme(
    primary = AmoledPrimary,
    secondary = AmoledSecondary,
    background = AmoledBackground,
    surface = AmoledSurface,
    onBackground = AmoledOnBackground,
    onSurface = AmoledOnSurface,
    primaryContainer = Color(0xFF212121),
    onPrimaryContainer = AmoledPrimary,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = AmoledOnSurface.copy(alpha = 0.8f)
)

val ElegantDarkScheme = darkColorScheme(
    primary = ElegantPrimary,
    secondary = ElegantPrimary,
    background = ElegantBackground,
    surface = ElegantSurface,
    onBackground = ElegantOnBackground,
    onSurface = ElegantOnSurface,
    primaryContainer = ElegantSecondary,
    onPrimaryContainer = ElegantPrimary,
    surfaceVariant = ElegantSurfaceVariant,
    onSurfaceVariant = ElegantOnSurfaceVariant,
    outline = ElegantOutline
)

@Composable
fun KivuTheme(
    themeName: String = "Elegant Dark",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        "Ocean Breeze" -> OceanBreezeScheme
        "Emerald Shine" -> EmeraldShineScheme
        "Amoled Slate" -> AmoledSlateScheme
        "Cosmic Midnight" -> CosmicMidnightScheme
        "Elegant Dark" -> ElegantDarkScheme
        else -> ElegantDarkScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
