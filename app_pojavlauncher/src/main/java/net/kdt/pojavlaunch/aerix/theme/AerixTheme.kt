/*
 * Fire Launcher — Prism Light Aurora Material3 Theme.
 * Pairs bright multi-color pastel daylight surfaces with frosted pearl-white glass,
 * deep slate typography, and vibrant Indigo-Violet-Rose-Sky accents.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package net.kdt.pojavlaunch.aerix.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import net.kdt.pojavlaunch.aerix.bridge.AerixColorTheme
import net.kdt.pojavlaunch.aerix.bridge.AerixPreferences

private val defaults = Typography()

private fun TextStyle.aerixText(family: FontFamily, weight: FontWeight = fontWeight ?: FontWeight.Normal) = copy(
    fontFamily = family,
    fontWeight = weight
)

private fun aerixTypography(family: FontFamily) = Typography(
    displayLarge = defaults.displayLarge.aerixText(family, FontWeight.Bold),
    displayMedium = defaults.displayMedium.aerixText(family, FontWeight.Bold),
    displaySmall = defaults.displaySmall.aerixText(family, FontWeight.Bold),
    headlineLarge = defaults.headlineLarge.aerixText(family, FontWeight.Bold),
    headlineMedium = defaults.headlineMedium.aerixText(family, FontWeight.SemiBold),
    headlineSmall = defaults.headlineSmall.aerixText(family, FontWeight.SemiBold),
    titleLarge = defaults.titleLarge.aerixText(family, FontWeight.SemiBold),
    titleMedium = defaults.titleMedium.aerixText(family, FontWeight.Medium),
    titleSmall = defaults.titleSmall.aerixText(family, FontWeight.Medium),
    bodyLarge = defaults.bodyLarge.aerixText(family),
    bodyMedium = defaults.bodyMedium.aerixText(family),
    bodySmall = defaults.bodySmall.aerixText(family),
    labelLarge = defaults.labelLarge.aerixText(family, FontWeight.Medium),
    labelMedium = defaults.labelMedium.aerixText(family, FontWeight.Medium),
    labelSmall = defaults.labelSmall.aerixText(family, FontWeight.Medium),
)

private val fireMaterialShapes = Shapes(
    extraSmall = FireShapes.hudChip,
    small = FireShapes.armorSmall,
    medium = FireShapes.armorMedium,
    large = FireShapes.armorCard,
    extraLarge = FireShapes.armorCard
)

private fun aerixDark(): ColorScheme = lightColorScheme(
    primary = AerixSurface.fireAccentDefault,
    onPrimary = AerixSurface.onFireAccentDefault,
    primaryContainer = AerixSurface.accentContainerDefault,
    onPrimaryContainer = AerixSurface.onAccentContainer,
    secondary = AerixSurface.fireAmber,
    onSecondary = Color.White,
    secondaryContainer = AerixSurface.firePanelRaised,
    onSecondaryContainer = AerixSurface.textPrimary,
    tertiary = AerixSurface.fireCrimson,
    onTertiary = Color.White,
    tertiaryContainer = AerixSurface.firePanelRaised,
    onTertiaryContainer = AerixSurface.textPrimary,
    error = AerixSurface.danger,
    onError = Color.White,
    errorContainer = AerixSurface.errorDarkContainer,
    onErrorContainer = AerixSurface.textPrimary,
    background = AerixSurface.canvas,
    onBackground = AerixSurface.textPrimary,
    surface = AerixSurface.firePanel,
    onSurface = AerixSurface.textPrimary,
    surfaceVariant = AerixSurface.firePanelRaised,
    onSurfaceVariant = AerixSurface.textSecondary,
    outline = AerixSurface.textMuted,
    outlineVariant = AerixSurface.border,
    scrim = Color(0x660F172A),
    inverseSurface = AerixSurface.lightTextPrimary,
    inverseOnSurface = AerixSurface.lightPanel,
    inversePrimary = AerixSurface.accentLight,
    surfaceDim = AerixSurface.canvas,
    surfaceBright = AerixSurface.firePanelRaised,
    surfaceContainerLowest = AerixSurface.canvas,
    surfaceContainerLow = AerixSurface.panelTrack,
    surfaceContainer = AerixSurface.firePanel,
    surfaceContainerHigh = AerixSurface.firePanelRaised,
    surfaceContainerHighest = AerixSurface.firePanelStrong
)

private fun customAccentScheme(accent: Color): ColorScheme {
    val onAccent = if (accent.luminance() > 0.62f) Color(0xFF0F172A) else Color.White
    return aerixDark().copy(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = accent.copy(alpha = 0.22f),
        onPrimaryContainer = AerixSurface.textPrimary,
        inversePrimary = accent
    )
}

@Composable
fun AerixLauncherTheme(content: @Composable () -> Unit) {
    val theme = AerixPreferences.colorTheme
    val custom = AerixPreferences.customColor
    val scheme = when (theme) {
        AerixColorTheme.AERIX -> aerixDark()
        AerixColorTheme.CUSTOM -> customAccentScheme(Color(custom))
    }
    AerixFonts.initialize(LocalContext.current)
    val fontId = AerixFonts.selectedId
    val typography = remember(fontId) { aerixTypography(AerixFonts.selected().family()) }
    MaterialTheme(
        colorScheme = scheme,
        typography = typography,
        shapes = fireMaterialShapes,
        content = content
    )
}
