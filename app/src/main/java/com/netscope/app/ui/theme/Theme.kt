package com.netscope.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val displayName: String, val badgeColor: Color) {
    BLACK("Black", Color(0xFF0A0B09)),
    WHITE("White", Color(0xFFF5F5F1)),
    RED("Red", Color(0xFFE11D48))
}

// Milemark Logistics Freight Color Palette
// Primary brand colors for NetScope
val MilemarkOrange = Color(0xFFED673F) // var(--orange)
val MilemarkInk    = Color(0xFF1B1C19) // var(--ink)
val MilemarkPaper  = Color(0xFFF5F5F1) // var(--paper)
val MilemarkMist   = Color(0xFFE8E8E3) // var(--mist)
val MilemarkMuted  = Color(0xFF66675F) // var(--muted)

val CrimsonRed     = Color(0xFFEF4444)
val NeonRed        = Color(0xFFFF2E51)
val EmeraldGreen   = Color(0xFF10B981)
val CyanAccent     = Color(0xFF06B6D4)
val AmberAccent    = Color(0xFFF59E0B)

// 1. BLACK THEME (Obsidian Carbon / Night Freight)
private val BlackColorScheme = darkColorScheme(
    primary = MilemarkOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A1B0D),
    onPrimaryContainer = Color(0xFFFFD9CE),
    secondary = CrimsonRed,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF450A0A),
    onSecondaryContainer = Color(0xFFFEE2E2),
    tertiary = CyanAccent,
    background = Color(0xFF0A0B09),
    onBackground = MilemarkPaper,
    surface = Color(0xFF141512),
    onSurface = MilemarkPaper,
    surfaceVariant = Color(0xFF1C1E19),
    onSurfaceVariant = Color(0xFFA1A39B),
    outline = Color(0xFF2A2C26),
    error = CrimsonRed
)

// 2. WHITE THEME (Milemark Paper / Logistics Day)
private val WhiteColorScheme = lightColorScheme(
    primary = MilemarkOrange,
    onPrimary = MilemarkInk,
    primaryContainer = Color(0xFFFFECE5),
    onPrimaryContainer = Color(0xFF8A2B0E),
    secondary = CrimsonRed,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEE2E2),
    onSecondaryContainer = Color(0xFF991B1B),
    tertiary = Color(0xFF0284C7),
    background = MilemarkPaper,
    onBackground = MilemarkInk,
    surface = Color.White,
    onSurface = MilemarkInk,
    surfaceVariant = MilemarkMist,
    onSurfaceVariant = MilemarkMuted,
    outline = Color(0xFFD4D4CD),
    error = CrimsonRed
)

// 3. RED THEME (Crimson Telemetry / High-Contrast Alert Dispatch)
private val RedColorScheme = darkColorScheme(
    primary = NeonRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5C0718),
    onPrimaryContainer = Color(0xFFFFD1DA),
    secondary = MilemarkOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A1B0D),
    onSecondaryContainer = Color(0xFFFFD9CE),
    tertiary = Color(0xFFFB7185),
    background = Color(0xFF0D0305),
    onBackground = Color(0xFFFFF0F3),
    surface = Color(0xFF1A060A),
    onSurface = Color(0xFFFFF0F3),
    surfaceVariant = Color(0xFF290B12),
    onSurfaceVariant = Color(0xFFE294A8),
    outline = Color(0xFF4A1522),
    error = Color(0xFFFF5252)
)

// Backwards-compatible aliases
val DarkBackground = Color(0xFF0A0B09)
val DarkSurface = Color(0xFF141512)
val DarkSurfaceVariant = Color(0xFF1C1E19)
val DarkBorder = Color(0xFF2A2C26)

val LightBackground = MilemarkPaper
val LightSurface = Color.White
val LightSurfaceVariant = MilemarkMist
val LightBorder = Color(0xFFD4D4CD)

val Zinc950 = Color(0xFF0A0B09)
val Zinc900 = Color(0xFF141512)
val Zinc850 = Color(0xFF1C1E19)
val Zinc800 = Color(0xFF2A2C26)
val Zinc700 = Color(0xFF3F3F46)
val Zinc400 = Color(0xFFA1A39B)
val EmeraldAccent = EmeraldGreen
val IndigoAccent = CrimsonRed
val RoseAccent = CrimsonRed
val BlueAccent = CyanAccent
val AmberOrange = AmberAccent
val RoseRed = CrimsonRed
val VioletPurple = CrimsonRed

@Composable
fun NetScopeTheme(
    themeMode: AppThemeMode = AppThemeMode.BLACK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.BLACK -> BlackColorScheme
        AppThemeMode.WHITE -> WhiteColorScheme
        AppThemeMode.RED -> RedColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
