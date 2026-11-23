/*
 * Fire Launcher Top Tactical Command Bar & Bottom Floating Molten Dock.
 * Replaces both vertical side rails of the old UI:
 *   - Top Command Header: Operator ID (Top-Left), System Utility Strip & Fire Crest (Top-Center),
 *     Active Instance Cartridge + Blazing IGNITE Launch Trigger (Top-Right).
 *   - Bottom Command Dock: Horizontal chamfered navigation deck across the bottom edge.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package net.kdt.pojavlaunch.aerix.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.aerix.bridge.AerixAccount
import net.kdt.pojavlaunch.aerix.bridge.GameDownloadTarget
import net.kdt.pojavlaunch.aerix.bridge.PojavAccountBridge
import net.kdt.pojavlaunch.aerix.bridge.PojavInstanceBridge
import net.kdt.pojavlaunch.aerix.components.AerixImageWell
import net.kdt.pojavlaunch.aerix.components.bringFocusedItemIntoView
import net.kdt.pojavlaunch.aerix.components.liquidGlass
import net.kdt.pojavlaunch.aerix.guide.AerixGuideState
import net.kdt.pojavlaunch.aerix.guide.guideAnchor
import net.kdt.pojavlaunch.aerix.theme.AERIX_COMPACT_HEIGHT_THRESHOLD_DP
import net.kdt.pojavlaunch.aerix.theme.AerixMetrics
import net.kdt.pojavlaunch.aerix.theme.AerixRadii
import net.kdt.pojavlaunch.aerix.theme.AerixSpacing
import net.kdt.pojavlaunch.aerix.theme.AerixSurface
import net.kdt.pojavlaunch.aerix.theme.AerixThemeManager
import net.kdt.pojavlaunch.aerix.theme.FireShapes
import net.kdt.pojavlaunch.aerix.theme.ThemeMotion
import net.kdt.pojavlaunch.aerix.theme.accentGlow
import net.kdt.pojavlaunch.aerix.theme.accentRing

/** Primary destinations kept visible while the launcher is open. */
enum class LauncherSection {
    HOME,
    AERIX_CLIENT,
    LIBRARY,
    SERVERS,
    SOCIALS,
    DISCOVER,
    OTHER_RELEASES,
    DOWNLOADS,
    CONTENT,
    FILES,
    LOGS,
    SETTINGS,
    CONTRIBUTION,
    MODPACKS,
    ACCOUNTS,
    ABOUT,
    CREATE_INSTANCE,
    INSTANCE_EDITOR,
    QUICK_TOOLS,
    INSTANCE_SELECTOR,
    ADD_ACCOUNT,
    OFFLINE_ACCOUNT,
    AERIX_ACCOUNT,
    CONTRIBUTION_DETAIL
}

/**
 * Bottom Floating Molten Command Dock.
 * Replaces the old left-hand vertical pill rail: every primary destination now sits in a
 * chamfered horizontal tactical dock anchored along the bottom edge.
 */
@Composable
fun AerixNavigationRail(
    selectedSection: LauncherSection,
    onNavigate: (LauncherSection) -> Unit,
    onCreateInstance: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val activeAccent = AerixThemeManager.currentAccent()
    val configuration = LocalConfiguration.current
    val compact = configuration.screenHeightDp < AERIX_COMPACT_HEIGHT_THRESHOLD_DP

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AerixSpacing.smPlus, vertical = if (compact) 3.dp else 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .liquidGlass(
                    shape = FireShapes.bottomDockShell,
                    tint = activeAccent,
                    strength = 0.94f,
                    elevation = AerixMetrics.glassRailElevation
                ),
            shape = FireShapes.bottomDockShell,
            color = AerixSurface.firePanelDeep,
            contentColor = AerixSurface.textPrimary,
            border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.38f))
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .padding(horizontal = AerixSpacing.sm, vertical = if (compact) 4.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 7.dp)
            ) {
                // Bottom-Left Prism Brand Anchor
                Row(
                    modifier = Modifier
                        .clip(FireShapes.hudChip)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0x2638BDF8), activeAccent.copy(alpha = 0.16f), Color(0x26EC4899))
                            )
                        )
                        .border(BorderStroke(1.dp, activeAccent.copy(alpha = 0.55f)), FireShapes.hudChip)
                        .bringFocusedItemIntoView()
                        .clickable(role = Role.Button) { onNavigate(LauncherSection.HOME) }
                        .semantics { contentDescription = "Fire Launcher HQ" }
                        .padding(horizontal = 8.dp, vertical = if (compact) 5.dp else 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_aerix_mark),
                        contentDescription = "FIRE LAUNCHER",
                        modifier = Modifier.size(if (compact) 18.dp else 21.dp)
                    )
                    if (!compact || configuration.screenWidthDp >= 640) {
                        Text(
                            text = "FIRE LAUNCHER",
                            fontSize = 9.5.sp,
                            letterSpacing = 1.1.sp,
                            fontWeight = FontWeight.Black,
                            color = AerixSurface.textPrimary
                        )
                    }
                }

                DockCommandItem(
                    iconRes = R.drawable.aerix_ic_home_filled,
                    label = "HQ",
                    selected = selectedSection == LauncherSection.HOME,
                    accentColor = activeAccent,
                    compact = compact
                ) { onNavigate(LauncherSection.HOME) }

                DockCommandItem(
                    iconRes = R.drawable.aerix_ic_dashboard_filled,
                    label = "VAULT",
                    selected = librarySelected(selectedSection),
                    accentColor = activeAccent,
                    compact = compact,
                    guideKey = AerixGuideState.LIBRARY
                ) { onNavigate(LauncherSection.LIBRARY) }

                DockCommandItem(
                    iconRes = R.drawable.aerix_ic_add,
                    label = "FORGE",
                    selected = selectedSection == LauncherSection.CREATE_INSTANCE,
                    accentColor = activeAccent,
                    compact = compact,
                    highlighted = true,
                    guideKey = AerixGuideState.CREATE,
                    onClick = onCreateInstance
                )

                DockCommandItem(
                    iconRes = R.drawable.aerix_ic_public,
                    label = "ARMORY",
                    selected = discoverSelected(selectedSection),
                    accentColor = activeAccent,
                    compact = compact
                ) { onNavigate(LauncherSection.DISCOVER) }

                DockCommandItem(
                    iconRes = R.drawable.aerix_ic_archive_filled,
                    label = "PACKS",
                    selected = selectedSection == LauncherSection.MODPACKS,
                    accentColor = activeAccent,
                    compact = compact,
                    guideKey = AerixGuideState.MODPACKS
                ) { onNavigate(LauncherSection.MODPACKS) }

                DockCommandItem(
                    iconRes = R.drawable.aerix_ic_sports_esports_outlined,
                    label = "REALMS",
                    selected = selectedSection == LauncherSection.SERVERS,
                    accentColor = activeAccent,
                    compact = compact
                ) { onNavigate(LauncherSection.SERVERS) }

                DockCommandItem(
                    iconRes = R.drawable.aerix_ic_client_crystal,
                    label = "CLIENT",
                    selected = selectedSection == LauncherSection.AERIX_CLIENT,
                    accentColor = activeAccent,
                    compact = compact
                ) { onNavigate(LauncherSection.AERIX_CLIENT) }

                DockCommandItem(
                    iconRes = R.drawable.aerix_ic_people,
                    label = "CREW",
                    selected = selectedSection == LauncherSection.SOCIALS,
                    accentColor = activeAccent,
                    compact = compact
                ) { onNavigate(LauncherSection.SOCIALS) }

                DockCommandItem(
                    iconRes = R.drawable.aerix_ic_rocket_launch_filled,
                    label = "DROPS",
                    selected = selectedSection == LauncherSection.OTHER_RELEASES,
                    accentColor = activeAccent,
                    compact = compact
                ) { onNavigate(LauncherSection.OTHER_RELEASES) }

                DockCommandItem(
                    iconRes = R.drawable.aerix_ic_group_outlined,
                    label = "TEAM",
                    selected = selectedSection == LauncherSection.CONTRIBUTION || selectedSection == LauncherSection.CONTRIBUTION_DETAIL,
                    accentColor = activeAccent,
                    compact = compact
                ) { onNavigate(LauncherSection.CONTRIBUTION) }
            }
        }
    }
}

/**
 * Top Tactical HUD Command Header Bar.
 * Replaces the old right-side utility rail AND relocates Account (Top-Left), System Utilities
 * (Top-Center), and Active Instance Cartridge + IGNITE Play Trigger (Top-Right).
 */
@Composable
fun AerixHomeUtilityRail(
    selectedSection: LauncherSection,
    onNavigate: (LauncherSection) -> Unit,
    onOpenProfile: () -> Unit,
    onQuickTools: () -> Unit,
    onInstances: () -> Unit,
    onIgnite: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val account = PojavAccountBridge.current
    val selectedInstance = PojavInstanceBridge.selected
    val activeAccent = AerixThemeManager.currentAccent()
    val configuration = LocalConfiguration.current
    val compact = configuration.screenHeightDp < AERIX_COMPACT_HEIGHT_THRESHOLD_DP
    val wide = configuration.screenWidthDp >= 700
    val versionTag = remember(selectedInstance) {
        selectedInstance?.let {
            GameDownloadTarget.minecraftVersionIn(it.versionId) ?: it.versionId
        } ?: "NO CORE"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AerixSpacing.smPlus, vertical = if (compact) 3.dp else 5.dp)
            .liquidGlass(
                shape = FireShapes.topHudShell,
                tint = activeAccent,
                strength = 0.92f,
                elevation = AerixMetrics.glassRailElevation
            ),
        shape = FireShapes.topHudShell,
        color = AerixSurface.firePanelDeep,
        contentColor = AerixSurface.textPrimary,
        border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.36f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AerixSpacing.smPlus, vertical = if (compact) 4.dp else 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AerixSpacing.xsPlus)
        ) {
            // 1. TOP-LEFT: Operator Account Pod + FIRE LAUNCHER Brand Title
            AccountAvatarHudPod(
                account = account,
                selected = selectedSection == LauncherSection.ACCOUNTS ||
                    selectedSection == LauncherSection.ADD_ACCOUNT ||
                    selectedSection == LauncherSection.OFFLINE_ACCOUNT,
                compact = compact,
                onClick = { onNavigate(LauncherSection.ACCOUNTS) }
            )

            if (wide) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_aerix_mark),
                        contentDescription = "FIRE LAUNCHER Logo",
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "FIRE LAUNCHER",
                        color = AerixSurface.textPrimary,
                        fontSize = 12.sp,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                }
            }

            // 2. TOP-CENTER-LEFT: Tactical Utility Triggers (Settings, Ops, Files, Logs, Social ID)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                TopHudUtilityChip(
                    iconRes = R.drawable.aerix_ic_settings_filled,
                    label = "CONFIG",
                    selected = settingsSelected(selectedSection),
                    accentColor = activeAccent,
                    showLabel = wide,
                    guideKey = AerixGuideState.SETTINGS
                ) { onNavigate(LauncherSection.SETTINGS) }

                TopHudUtilityChip(
                    iconRes = R.drawable.aerix_ic_build_outlined,
                    label = "OPS",
                    selected = selectedSection == LauncherSection.QUICK_TOOLS,
                    accentColor = activeAccent,
                    showLabel = wide
                ) { onQuickTools() }

                TopHudUtilityChip(
                    iconRes = R.drawable.aerix_ic_folder_outlined,
                    label = "FILES",
                    selected = selectedSection == LauncherSection.FILES,
                    accentColor = activeAccent,
                    showLabel = wide
                ) { onNavigate(LauncherSection.FILES) }

                TopHudUtilityChip(
                    iconRes = R.drawable.aerix_ic_terminal_outlined,
                    label = "CONSOLE",
                    selected = selectedSection == LauncherSection.LOGS,
                    accentColor = activeAccent,
                    showLabel = wide
                ) { onNavigate(LauncherSection.LOGS) }

                TopHudUtilityChip(
                    iconRes = R.drawable.aerix_ic_person_outlined,
                    label = "ID CARD",
                    selected = selectedSection == LauncherSection.SOCIALS,
                    accentColor = activeAccent,
                    showLabel = wide
                ) { onOpenProfile() }

                if (wide) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "// FIRE OS · ${selectedSection.name.replace('_', ' ')}",
                        color = AerixSurface.textMuted,
                        fontSize = 9.sp,
                        letterSpacing = 1.3.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
            }

            // 3. TOP-RIGHT: Active Instance Cartridge + Blazing IGNITE Reactor Launch Button
            Surface(
                modifier = Modifier
                    .height(if (compact) 32.dp else 36.dp)
                    .widthIn(max = if (wide) 190.dp else 132.dp)
                    .bringFocusedItemIntoView(),
                shape = FireShapes.hudChip,
                color = AerixSurface.firePanelRaised,
                border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.45f)),
                onClick = onInstances
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AerixImageWell(
                        image = selectedInstance?.icon,
                        fallback = R.drawable.aerix_ic_videogame_asset_outlined,
                        size = if (compact) 20.dp else 22.dp,
                        radius = AerixRadii.tiny
                    )
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = selectedInstance?.name ?: "SELECT CORE",
                            color = AerixSurface.textPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = versionTag.uppercase(),
                            color = AerixSurface.fireAmber,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            val igniteBrush = Brush.linearGradient(
                colors = listOf(Color(0xFF22D3EE), Color(0xFF818CF8), Color(0xFFF472B6))
            )
            Surface(
                modifier = Modifier
                    .height(if (compact) 32.dp else 36.dp)
                    .guideAnchor(AerixGuideState.PLAY)
                    .bringFocusedItemIntoView(),
                shape = FireShapes.reactorTrigger,
                color = Color.Transparent,
                border = BorderStroke(1.dp, AerixSurface.fireAmber.copy(alpha = 0.85f)),
                onClick = onIgnite
            ) {
                Row(
                    modifier = Modifier
                        .background(igniteBrush)
                        .padding(horizontal = if (compact) 11.dp else 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.aerix_ic_play_arrow_filled),
                        contentDescription = stringResource(R.string.aerix_home_play),
                        tint = AerixSurface.onFireAccentDefault,
                        modifier = Modifier.size(if (compact) 15.dp else 17.dp)
                    )
                    Text(
                        text = if (selectedInstance != null) "IGNITE" else "FORGE",
                        color = AerixSurface.onFireAccentDefault,
                        fontSize = if (compact) 10.5.sp else 11.5.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun librarySelected(section: LauncherSection): Boolean =
    section == LauncherSection.LIBRARY || section == LauncherSection.INSTANCE_EDITOR || section == LauncherSection.CONTENT

private fun discoverSelected(section: LauncherSection): Boolean =
    section == LauncherSection.DISCOVER || section == LauncherSection.DOWNLOADS

private fun settingsSelected(section: LauncherSection): Boolean =
    section == LauncherSection.SETTINGS || section == LauncherSection.ABOUT

@Composable
private fun DockCommandItem(
    iconRes: Int,
    label: String,
    selected: Boolean,
    accentColor: Color,
    compact: Boolean,
    highlighted: Boolean = false,
    guideKey: String? = null,
    onClick: () -> Unit
) {
    val motionOn = ThemeMotion.rememberEnabled()
    val liveAccent = ThemeMotion.rememberPulse(accentColor, selected, motionOn)
    val containerColor by animateColorAsState(
        targetValue = when {
            selected -> liveAccent
            highlighted -> liveAccent.copy(alpha = 0.16f)
            else -> AerixSurface.firePanel
        },
        animationSpec = tween(150),
        label = "dockContainerColor"
    )
    val contentTint by animateColorAsState(
        targetValue = when {
            selected -> AerixSurface.onFireAccentDefault
            highlighted -> AerixSurface.fireAmber
            else -> AerixSurface.textSecondary
        },
        animationSpec = tween(150),
        label = "dockIconTint"
    )
    val itemShape = FireShapes.armorSmall
    Box(
        modifier = Modifier
            .then(if (guideKey != null) Modifier.guideAnchor(guideKey) else Modifier)
            .height(if (compact) 31.dp else 36.dp)
            .clip(itemShape)
            .background(containerColor)
            .accentGlow(liveAccent, selected, motionOn)
            .accentRing(liveAccent, itemShape, selected, motionOn, AerixSpacing.hairline)
            .border(
                BorderStroke(
                    1.dp,
                    when {
                        selected -> AerixSurface.fireAmber
                        highlighted -> liveAccent.copy(alpha = 0.6f)
                        else -> AerixSurface.borderSoft
                    }
                ),
                itemShape
            )
            .bringFocusedItemIntoView()
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { contentDescription = label }
            .padding(horizontal = if (compact) 9.dp else 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = contentTint,
                modifier = Modifier.size(if (compact) 15.dp else 16.dp)
            )
            Text(
                text = label,
                color = if (selected) contentTint else AerixSurface.textPrimary,
                fontSize = if (compact) 9.5.sp else 10.sp,
                letterSpacing = 0.8.sp,
                fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TopHudUtilityChip(
    iconRes: Int,
    label: String,
    selected: Boolean,
    accentColor: Color,
    showLabel: Boolean,
    guideKey: String? = null,
    onClick: () -> Unit
) {
    val shape = FireShapes.hudChip
    Surface(
        modifier = Modifier
            .then(if (guideKey != null) Modifier.guideAnchor(guideKey) else Modifier)
            .height(32.dp)
            .bringFocusedItemIntoView(),
        shape = shape,
        color = if (selected) accentColor else AerixSurface.firePanel,
        border = BorderStroke(
            1.dp,
            if (selected) AerixSurface.fireAmber else accentColor.copy(alpha = 0.3f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (showLabel) 9.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = if (selected) AerixSurface.onFireAccentDefault else AerixSurface.fireAmber,
                modifier = Modifier.size(15.dp)
            )
            if (showLabel) {
                Text(
                    text = label,
                    color = if (selected) AerixSurface.onFireAccentDefault else AerixSurface.textPrimary,
                    fontSize = 9.5.sp,
                    letterSpacing = 0.7.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun AccountAvatarHudPod(
    account: AerixAccount?,
    selected: Boolean,
    compact: Boolean,
    onClick: () -> Unit
) {
    val activeAccent = AerixThemeManager.currentAccent()
    val accountsLabel = stringResource(R.string.aerix_nav_accounts)
    val shape = FireShapes.armorSmall
    Surface(
        modifier = Modifier
            .height(if (compact) 32.dp else 36.dp)
            .bringFocusedItemIntoView()
            .semantics { contentDescription = account?.username ?: accountsLabel },
        shape = shape,
        color = if (selected) activeAccent.copy(alpha = 0.24f) else AerixSurface.firePanelRaised,
        border = BorderStroke(1.dp, if (selected) AerixSurface.fireAmber else activeAccent.copy(alpha = 0.5f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AerixImageWell(
                image = account?.face,
                fallback = R.drawable.aerix_head_steve,
                size = if (compact) 22.dp else 24.dp,
                radius = AerixRadii.tiny
            )
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = account?.username ?: "+ OPERATOR",
                    color = AerixSurface.textPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!compact) {
                    Text(
                        text = if (account != null) "ONLINE ID" else "TAP LOGIN",
                        color = if (account != null) AerixSurface.fireAmber else activeAccent,
                        fontSize = 8.sp,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
