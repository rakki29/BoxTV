package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

enum class VirtualOsVersion(val code: String) {
    TV_12("TV_12"),
    TV_11("TV_11");

    companion object {
        fun fromCode(code: String): VirtualOsVersion =
            entries.firstOrNull { it.code == code } ?: TV_12
    }
}

private val Tv12ColorScheme = darkColorScheme(
    primary = Tv12CyanPrimary,
    onPrimary = Tv12OnPrimary,
    primaryContainer = Tv12CyanContainer,
    onPrimaryContainer = TvTextPrimary,
    secondary = Tv12IndigoSecondary,
    onSecondary = TvObsidianBg,
    tertiary = Tv12CoralTertiary,
    background = TvObsidianBg,
    onBackground = TvTextPrimary,
    surface = TvMidnightSurface,
    onSurface = TvTextPrimary,
    surfaceVariant = TvSurfaceElevated,
    onSurfaceVariant = TvTextSecondary,
    surfaceContainerHigh = TvSurfaceElevated,
    surfaceContainerHighest = TvSurfaceHighest,
    outline = TvOutline,
    error = TvError
)

private val Tv11ColorScheme = darkColorScheme(
    primary = Tv11EmeraldPrimary,
    onPrimary = Tv11OnPrimary,
    primaryContainer = Tv11EmeraldContainer,
    onPrimaryContainer = TvTextPrimary,
    secondary = Tv11CobaltSecondary,
    onSecondary = TvObsidianBg,
    tertiary = Tv11AmberTertiary,
    background = TvObsidianBg,
    onBackground = TvTextPrimary,
    surface = TvMidnightSurface,
    onSurface = TvTextPrimary,
    surfaceVariant = TvSurfaceElevated,
    onSurfaceVariant = TvTextSecondary,
    surfaceContainerHigh = TvSurfaceElevated,
    surfaceContainerHighest = TvSurfaceHighest,
    outline = TvOutline,
    error = TvError
)

private val TvShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun BoxTvVirtualTheme(
    virtualOsVersion: VirtualOsVersion = VirtualOsVersion.TV_12,
    content: @Composable () -> Unit
) {
    val colorScheme = when (virtualOsVersion) {
        VirtualOsVersion.TV_12 -> Tv12ColorScheme
        VirtualOsVersion.TV_11 -> Tv11ColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TvTypography,
        shapes = TvShapes,
        content = content
    )
}
