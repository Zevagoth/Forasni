package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = ForasniBurgundyDark,
    onPrimary = ForasniOnBurgundyDark,
    primaryContainer = ForasniBurgundyContainerDark,
    onPrimaryContainer = ForasniOnBurgundyContainerDark,
    secondary = ForasniEmeraldDark,
    onSecondary = ForasniOnEmeraldDark,
    secondaryContainer = ForasniEmeraldContainerDark,
    onSecondaryContainer = ForasniOnEmeraldContainerDark,
    tertiary = ForasniGoldDark,
    onTertiary = ForasniOnGoldDark,
    tertiaryContainer = ForasniGoldContainerDark,
    onTertiaryContainer = ForasniOnGoldContainerDark,
    background = ForasniBackgroundDark,
    onBackground = ForasniOnSurfaceDark,
    surface = ForasniSurfaceDark,
    onSurface = ForasniOnSurfaceDark,
    surfaceVariant = ForasniSurfaceVariantDark,
    onSurfaceVariant = ForasniOnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = ForasniBurgundy,
    onPrimary = ForasniOnBurgundy,
    primaryContainer = ForasniBurgundyContainer,
    onPrimaryContainer = ForasniOnBurgundyContainer,
    secondary = ForasniEmerald,
    onSecondary = ForasniOnEmerald,
    secondaryContainer = ForasniEmeraldContainer,
    onSecondaryContainer = ForasniOnEmeraldContainer,
    tertiary = ForasniGold,
    onTertiary = ForasniOnGold,
    tertiaryContainer = ForasniGoldContainer,
    onTertiaryContainer = ForasniOnGoldContainer,
    background = ForasniSandBackground,
    onBackground = ForasniOnSand,
    surface = ForasniSurface,
    onSurface = ForasniOnSand,
    surfaceVariant = ForasniSurfaceVariant,
    onSurfaceVariant = ForasniOnSurfaceVariant,
    outline = ForasniOutline
)

@Composable
fun ForasniTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
