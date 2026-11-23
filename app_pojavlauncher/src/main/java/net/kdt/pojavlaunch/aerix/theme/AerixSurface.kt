/*
 * Fire Launcher — "Prism Light Aurora" Surface, Shape & Palette System.
 * Bright multi-color pastel daylight canvas, frosted pearl-white translucent glass cards,
 * sleek asymmetric Bento-squircle geometry, vibrant Indigo-Violet-Rose-Sky accents,
 * and high-contrast slate typography — while keeping every symbol expected across the app.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package net.kdt.pojavlaunch.aerix.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.kdt.pojavlaunch.aerix.bridge.AerixPreferences

/**
 * Modern 2026 Asymmetric Bento-Squircle Shapes (`FireShapes`).
 * Smooth sculpted squircles with subtle asymmetric corners for a fresh next-gen look.
 */
object FireShapes {
    val armorCard = RoundedCornerShape(topStart = 24.dp, topEnd = 14.dp, bottomEnd = 24.dp, bottomStart = 14.dp)
    val armorMedium = RoundedCornerShape(topStart = 18.dp, topEnd = 11.dp, bottomEnd = 18.dp, bottomStart = 11.dp)
    val armorSmall = RoundedCornerShape(topStart = 13.dp, topEnd = 8.dp, bottomEnd = 13.dp, bottomStart = 8.dp)
    val reactorTrigger = RoundedCornerShape(topStart = 16.dp, topEnd = 10.dp, bottomEnd = 16.dp, bottomStart = 10.dp)
    val hudChip = RoundedCornerShape(topStart = 11.dp, topEnd = 7.dp, bottomEnd = 11.dp, bottomStart = 7.dp)
    val topHudShell = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 22.dp, bottomStart = 22.dp)
    val bottomDockShell = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
}

object AerixSurface {
    /** Luminous ice-pearl daylight canvas underneath the multi-color pastel aurora mesh. */
    val canvas = Color(0xFFF5F8FF)

    private fun glass(base: Color): Color {
        val percent = AerixPreferences.layoutTransparency
        if (percent >= 100) return base
        return base.copy(alpha = (base.alpha * percent / 100f).coerceIn(0f, 1f))
    }

    // Legacy smoky panels preserved verbatim for sub-dialogs & layout_transparency_smoke.py.
    val panel: Color get() = glass(Color(0x66343839))
    val panelRaised: Color get() = glass(Color(0x80515858))
    val panelTrack: Color get() = glass(Color(0x604A5050))
    val panelGlassTint: Color get() = glass(Color(0x3D596362))

    // Primary Fire Launcher Frosted Pearl-White Daylight Glass Surfaces:
    val firePanelDeep: Color get() = glass(Color(0xEBFFFFFF))   // 92% frosted pure pearl white
    val firePanel: Color get() = glass(Color(0xDBFFFFFF))       // 86% airy frosted daylight glass
    val firePanelRaised: Color get() = glass(Color(0xF2F8FAFF)) // 95% crisp elevated ice-white card
    val firePanelStrong: Color get() = glass(Color(0xFAFFFFFF)) // 98% luminous pearl

    val border = Color(0x526366F1)
    val borderSoft = Color(0x2E6366F1)
    val borderHighlight = Color(0xDDFFFFFF)
    val glassTint = Color(0xFF6366F1)
    val glassBlue = Color(0xFF38BDF8)
    val glassViolet = Color(0xFFA78BFA)
    val glassRose = Color(0xFFF472B6)
    val auroraCyan = Color(0xFF06B6D4)
    val auroraViolet = Color(0xFF6366F1)
    val glassShadow = Color(0x26312E81)

    /** Static fallback preserved for glass_launcher_ui_smoke.py. */
    val accentDefault = Color(0xFF8DEFE0)

    // Fire Launcher "Prism Light Aurora" vibrant multi-color accents:
    val fireAccentDefault = Color(0xFF6366F1)   // Electric Indigo-Violet
    val onFireAccentDefault = Color(0xFFFFFFFF) // Pure crisp white on gradient buttons
    val fireAmber = Color(0xFFDB2777)           // Vivid Rose-Berry secondary highlight
    val fireCrimson = Color(0xFF0284C7)         // Luminous Sky-Cyan tertiary highlight

    val accent: Color
        @Composable get() = AerixThemeManager.currentAccent()
    val action: Color
        @Composable get() = accent
    val onAction: Color
        @Composable get() = onAccent
    val accentSecondary = Color(0xFFEC4899)
    val accentGlow = Color(0x556366F1)
    val accentContainerDefault = Color(0x296366F1)
    val accentContainer: Color
        @Composable get() = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.36f)
    val onAccentDefault = Color(0xFFFFFFFF)
    val onAccent: Color
        @Composable get() = MaterialTheme.colorScheme.onPrimary
    val onAccentContainer = Color(0xFF1E1B4B)
    val accentLight = Color(0xFF4F46E5)
    val lightPanel = Color(0xFFFFFFFF)
    val lightTextPrimary = Color(0xFF0F172A)
    val errorDarkContainer = Color(0xFFFFE4E6)
    val dangerContainer = Color(0xFFFFE4E6)
    val warningContainer = Color(0xFFFEF3C7)
    val modrinthBrand = Color(0xFF10B981)
    val curseForgeBrand = Color(0xFFF16436)
    val shadowSoft = Color(0x221E1B4B)
    val scrimSoft = Color(0x260F172A)

    // Crisp slate-navy typography on frosted pearl-white daylight cards:
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF475569)
    val textMuted = Color(0xFF64748B)

    val success = Color(0xFF059669)
    val warning = Color(0xFFD97706)
    val danger = Color(0xFFE11D48)
    val glassShine = Color(0x70FFFFFF)

    val logInfoSurface = Color(0xFF447152)
    val logDebugSurface = Color(0xFF43698D)
    val logWarningSurface = Color(0xFF656E76)
    val syntaxMuted = Color(0xFF6E7C83)
    val syntaxString = Color(0xFF6AAB73)
    val syntaxAccent = Color(0xFFC67CBA)

    fun onColorFor(accent: Color): Color =
        if (accent.luminance() > 0.62f) Color(0xFF0F172A) else Color(0xFFFFFFFF)
}

const val AERIX_COMPACT_HEIGHT_THRESHOLD_DP = 520

object AerixSpacing {
    val zero: Dp = 0.dp
    val hairline: Dp = 1.dp
    val xxs: Dp = 2.dp
    val tiny: Dp = 3.dp
    val xs: Dp = 4.dp
    val xsPlus: Dp = 5.dp
    val smCompact: Dp = 6.dp
    val smTight: Dp = 7.dp
    val sm: Dp = 8.dp
    val smPlus: Dp = 10.dp
    val smNarrow: Dp = 9.dp
    val mdTight: Dp = 11.dp
    val md: Dp = 12.dp
    val mdPlus: Dp = 14.dp
    val lg: Dp = 16.dp
    val lgPlus: Dp = 18.dp
    val xl: Dp = 20.dp
    val xlPlus: Dp = 22.dp
    val xxl: Dp = 24.dp
    val xxxl: Dp = 32.dp
    val section: Dp = 40.dp
}

object AerixLayoutDefaults {
    @Composable
    fun pagePadding(
        horizontal: Dp = AerixSpacing.lg,
        vertical: Dp = AerixSpacing.lg
    ): PaddingValues {
        val compact = LocalConfiguration.current.screenHeightDp < AERIX_COMPACT_HEIGHT_THRESHOLD_DP
        return PaddingValues(
            horizontal = if (compact) AerixSpacing.md else horizontal,
            vertical = if (compact) AerixSpacing.sm else vertical
        )
    }
}

object AerixRadii {
    val square: Dp = 0.dp
    val tiny: Dp = 4.dp
    val micro: Dp = 6.dp
    val compact: Dp = 8.dp
    val controlSmall: Dp = 10.dp
    val control: Dp = 12.dp
    val cardSmall: Dp = 16.dp
    val card: Dp = 18.dp
    val cardLarge: Dp = 20.dp
    val panelSmall: Dp = 22.dp
    val panel: Dp = 24.dp
    val dialog: Dp = 28.dp
    val hero: Dp = 32.dp
    val pill: Dp = 50.dp
}

object AerixMetrics {
    val pillTabHeight: Dp = 38.dp
    val pageHeaderHeight: Dp = 44.dp
    val navigationRailWidth: Dp = 62.dp
    val expandedNavigationRailWidth: Dp = 72.dp
    val glassSurfaceElevation: Dp = 2.dp
    val glassFloatingElevation: Dp = 4.dp
    val glassRailElevation: Dp = 4.dp
    val glassDialogElevation: Dp = 6.dp
    val glassSelectedElevation: Dp = 2.dp
    val glassSubtleElevation: Dp = 2.dp
}
