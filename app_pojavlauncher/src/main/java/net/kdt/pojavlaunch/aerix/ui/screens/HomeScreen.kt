/*
 * Fire Launcher Home Screen ("INFERNO COMMAND HQ").
 * Complete ground-up tactical redesign:
 *   - Left Wing: 360° Operator Hologram Chamber on Molten Magma Pedestal + Wardrobe / Cloud ID chips
 *   - Center Deck: HUD Mode Switcher + 2x2 Chamfered Tactical Action Matrix + Engine Telemetry Strip
 *   - Right Wing: Active Reactor Core Hero Module + Vertical Rapid Instance Cartridge Rack
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package net.kdt.pojavlaunch.aerix.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.aerix.bridge.AerixInstance
import net.kdt.pojavlaunch.aerix.bridge.AerixLauncherUiStyle
import net.kdt.pojavlaunch.aerix.bridge.AerixPreferences
import net.kdt.pojavlaunch.aerix.bridge.GameDownloadTarget
import net.kdt.pojavlaunch.aerix.bridge.PojavAccountBridge
import net.kdt.pojavlaunch.aerix.bridge.PojavInstanceBridge
import net.kdt.pojavlaunch.aerix.bridge.PojavLaunchBridge
import net.kdt.pojavlaunch.aerix.bridge.PojavNavigator
import net.kdt.pojavlaunch.aerix.bridge.PojavTaskBridge
import net.kdt.pojavlaunch.aerix.components.AerixGhostButton
import net.kdt.pojavlaunch.aerix.components.AerixImageWell
import net.kdt.pojavlaunch.aerix.components.AerixPrimaryButton
import net.kdt.pojavlaunch.aerix.components.AerixSkinPreview3D
import net.kdt.pojavlaunch.aerix.components.MetaPill
import net.kdt.pojavlaunch.aerix.components.QuickActionTile
import net.kdt.pojavlaunch.aerix.components.QuietEmptyState
import net.kdt.pojavlaunch.aerix.components.SectionLabel
import net.kdt.pojavlaunch.aerix.components.liquidGlass
import net.kdt.pojavlaunch.aerix.social.SocialBackend
import net.kdt.pojavlaunch.aerix.theme.AerixMetrics
import net.kdt.pojavlaunch.aerix.theme.AerixRadii
import net.kdt.pojavlaunch.aerix.theme.AerixSpacing
import net.kdt.pojavlaunch.aerix.theme.AerixSurface
import net.kdt.pojavlaunch.aerix.theme.AerixThemeManager
import net.kdt.pojavlaunch.aerix.theme.FireShapes
import net.kdt.pojavlaunch.aerix.ui.AerixNav
import net.kdt.pojavlaunch.aerix.ui.LauncherSection
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

private const val DOLL_DRAG_DEG_PER_PX = 0.45f
private const val DOLL_FLING_FRICTION = 5f
private const val DOLL_FLING_MIN_VELOCITY = 60f
private const val DOLL_FLING_STOP_VELOCITY = 20f

private fun instanceVersionLabel(versionId: String, vanilla: String): String {
    val mcVersion = GameDownloadTarget.minecraftVersionIn(versionId) ?: versionId
    val loader = loaderLabel(versionId, vanilla)
    return if (loader == vanilla) mcVersion else "$loader $mcVersion"
}

private class HomeActions(
    val play: () -> Unit,
    val playInstance: (AerixInstance) -> Unit,
    val select: (AerixInstance) -> Unit,
    val edit: (AerixInstance) -> Unit,
    val create: () -> Unit,
    val socials: () -> Unit,
    val explore: () -> Unit,
    val library: () -> Unit,
    val files: () -> Unit,
    val controls: () -> Unit,
    val runJar: () -> Unit,
    val shareLogs: () -> Unit,
    val gamepad: () -> Unit,
    val content: () -> Unit,
    val downloads: () -> Unit,
    val fileManager: () -> Unit,
    val logs: () -> Unit,
    val accounts: () -> Unit,
    val aerixAccount: () -> Unit,
    val quickTools: () -> Unit,
    val instanceSelector: () -> Unit
)

@Composable
fun HomeScreen(activity: FragmentActivity, navigate: (LauncherSection) -> Unit) {
    LaunchedEffect(Unit) {
        PojavAccountBridge.ensureLoaded()
    }
    val socialContext = LocalContext.current
    LaunchedEffect(Unit) {
        SocialBackend.attach(socialContext)
        if (SocialBackend.signedIn) {
            SocialBackend.refreshFriends()
            SocialBackend.refreshParties()
            SocialBackend.refreshPartyInvites()
            SocialBackend.unreadCount()
        }
    }

    val instances = PojavInstanceBridge.instances
    val selected = PojavInstanceBridge.selected
    val accent = AerixThemeManager.currentAccent()
    val taskCount = PojavTaskBridge.taskCount
    val jarLauncher = rememberLauncherForActivityResult(OpenDocumentWithExtension("jar")) { uri ->
        if (uri != null) PojavNavigator.runJar(activity, uri)
    }
    val actions = HomeActions(
        play = { PojavLaunchBridge.play(activity) },
        playInstance = { instance ->
            PojavInstanceBridge.select(instance)
            PojavLaunchBridge.play(activity)
        },
        select = { PojavInstanceBridge.select(it) },
        edit = { PojavInstanceBridge.edit(activity, it) },
        create = { AerixNav.navigate(LauncherSection.CREATE_INSTANCE) },
        socials = { navigate(LauncherSection.SOCIALS) },
        explore = { navigate(LauncherSection.DISCOVER) },
        library = { navigate(LauncherSection.LIBRARY) },
        files = { PojavInstanceBridge.openGameDirectory(activity) },
        controls = { PojavNavigator.openCustomControls(activity) },
        runJar = { if (PojavNavigator.canRunJar(activity)) jarLauncher.launch(null) },
        shareLogs = { PojavNavigator.shareLogs(activity) },
        gamepad = { PojavNavigator.openGamepadMapper(activity) },
        content = {
            selected?.let { PojavInstanceBridge.edit(activity, it) } ?: navigate(LauncherSection.LIBRARY)
        },
        downloads = { navigate(LauncherSection.DISCOVER) },
        fileManager = { navigate(LauncherSection.FILES) },
        logs = { navigate(LauncherSection.LOGS) },
        accounts = { navigate(LauncherSection.ACCOUNTS) },
        aerixAccount = { AerixNav.navigate(LauncherSection.AERIX_ACCOUNT) },
        quickTools = { navigate(LauncherSection.QUICK_TOOLS) },
        instanceSelector = { navigate(LauncherSection.INSTANCE_SELECTOR) }
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AerixSpacing.smPlus, vertical = AerixSpacing.xs)
    ) {
        val wideLandscape = maxWidth >= 620.dp && maxWidth > maxHeight
        if (wideLandscape) {
            if (AerixPreferences.uiStyle == AerixLauncherUiStyle.PREMIUM) {
                FireOverdriveCommandDeck(
                    selected = selected,
                    instances = instances,
                    accent = accent,
                    taskCount = taskCount,
                    actions = actions
                )
            } else {
                FireReactorTriWingHq(
                    selected = selected,
                    instances = instances,
                    accent = accent,
                    taskCount = taskCount,
                    actions = actions
                )
            }
        } else {
            FireCompactPortraitHq(
                selected = selected,
                instances = instances,
                accent = accent,
                taskCount = taskCount,
                actions = actions
            )
        }
    }
}

/**
 * Primary Fire Launcher Landscape HQ ("REACTOR SPLIT"):
 *   - LEFT WING (0.30f): 360° Operator Hologram Chamber on Molten Pedestal
 *   - CENTER DECK (0.38f): HUD Mode Switch + 2x2 Tactical Matrix + Telemetry Diagnostics Strip
 *   - RIGHT WING (0.32f): Active Reactor Hero Module + Rapid Instance Cartridge Rack
 */
@Composable
private fun FireReactorTriWingHq(
    selected: AerixInstance?,
    instances: List<AerixInstance>,
    accent: Color,
    taskCount: Int,
    actions: HomeActions
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(AerixSpacing.smPlus)
    ) {
        // 1. FAR LEFT: 3D Operator Hologram Chamber (moved out of Center and out of Right!)
        OperatorHologramChamber(
            accent = accent,
            actions = actions,
            modifier = Modifier
                .weight(0.30f)
                .fillMaxHeight()
        )

        // 2. CENTER: Tactical Mission Matrix + Engine Telemetry
        Column(
            modifier = Modifier
                .weight(0.38f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(AerixSpacing.sm)
        ) {
            SameVersionNudgeStrip(selected = selected, accent = accent, onOpenSocials = actions.socials)
            TacticalMissionMatrix(
                count = instances.size,
                accent = accent,
                actions = actions,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            ReactorTelemetryBar(
                taskCount = taskCount,
                accent = accent,
                actions = actions,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 3. FAR RIGHT: Active Reactor Core + Vertical Instance Cartridge Rack
        Column(
            modifier = Modifier
                .weight(0.32f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(AerixSpacing.sm)
        ) {
            ActiveReactorHeroCard(
                selected = selected,
                accent = accent,
                actions = actions,
                modifier = Modifier.fillMaxWidth()
            )
            InstanceCartridgeRack(
                instances = instances,
                selected = selected,
                accent = accent,
                actions = actions,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}

/**
 * Alternate Fire Launcher Mode ("COMMAND DECK"):
 *   - TOP BANNER: Full-width Active Reactor + HUD Switch + Quick Launch
 *   - BOTTOM SPLIT: Left = Cartridge Grid + Telemetry, Center = 2x2 Matrix, Right-Center = Hologram Pod
 */
@Composable
private fun FireOverdriveCommandDeck(
    selected: AerixInstance?,
    instances: List<AerixInstance>,
    accent: Color,
    taskCount: Int,
    actions: HomeActions
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AerixSpacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AerixSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActiveReactorHeroCard(
                selected = selected,
                accent = accent,
                actions = actions,
                modifier = Modifier.weight(0.64f)
            )
            Column(
                modifier = Modifier.weight(0.36f),
                verticalArrangement = Arrangement.spacedBy(AerixSpacing.xs)
            ) {
                FireHudStyleSwitch(modifier = Modifier.fillMaxWidth())
                ReactorTelemetryBar(
                    taskCount = taskCount,
                    accent = accent,
                    actions = actions,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AerixSpacing.smPlus)
        ) {
            OperatorHologramChamber(
                accent = accent,
                actions = actions,
                modifier = Modifier
                    .weight(0.28f)
                    .fillMaxHeight()
            )
            InstanceCartridgeRack(
                instances = instances,
                selected = selected,
                accent = accent,
                actions = actions,
                modifier = Modifier
                    .weight(0.36f)
                    .fillMaxHeight()
            )
            TacticalMissionMatrix(
                count = instances.size,
                accent = accent,
                actions = actions,
                showSwitch = false,
                modifier = Modifier
                    .weight(0.36f)
                    .fillMaxHeight()
            )
        }
    }
}

/** Responsive vertical Tactical HQ for portrait / narrow screens. */
@Composable
private fun FireCompactPortraitHq(
    selected: AerixInstance?,
    instances: List<AerixInstance>,
    accent: Color,
    taskCount: Int,
    actions: HomeActions
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AerixSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AerixSpacing.smPlus)
    ) {
        item { FireHudStyleSwitch(modifier = Modifier.fillMaxWidth()) }
        item { SameVersionNudgeStrip(selected = selected, accent = accent, onOpenSocials = actions.socials) }
        item { ActiveReactorHeroCard(selected = selected, accent = accent, actions = actions, modifier = Modifier.fillMaxWidth()) }
        item {
            OperatorHologramChamber(
                accent = accent,
                actions = actions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            )
        }
        item {
            TacticalMissionMatrix(
                count = instances.size,
                accent = accent,
                actions = actions,
                showSwitch = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item { ReactorTelemetryBar(taskCount = taskCount, accent = accent, actions = actions, modifier = Modifier.fillMaxWidth()) }
        item {
            InstanceCartridgeRack(
                instances = instances,
                selected = selected,
                accent = accent,
                actions = actions,
                lazy = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Left-Wing 360° Operator Hologram Chamber with glowing Molten Lava Pedestal. */
@Composable
private fun OperatorHologramChamber(
    accent: Color,
    actions: HomeActions,
    modifier: Modifier = Modifier
) {
    val account = PojavAccountBridge.current
    var dollAzimuth by remember(account?.username) { mutableFloatStateOf(28f) }
    val flingScope = rememberCoroutineScope()
    val velocityTracker = remember(account?.username) { VelocityTracker() }
    var flingJob by remember(account?.username) { mutableStateOf<Job?>(null) }

    Surface(
        modifier = modifier.liquidGlass(
            shape = FireShapes.armorCard,
            tint = accent,
            strength = 0.88f,
            elevation = AerixMetrics.glassSubtleElevation
        ),
        shape = FireShapes.armorCard,
        color = AerixSurface.firePanel,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.48f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AerixSpacing.smPlus),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "OPERATOR STUDIO",
                        color = AerixSurface.textPrimary,
                        fontSize = 11.5.sp,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = account?.username ?: "UNLINKED PILOT",
                        color = AerixSurface.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                MetaPill(text = "${dollAzimuth.roundToInt()}°")
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds()
                    .drawWithCache {
                        val coreGlow = Brush.radialGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.30f),
                                AerixSurface.fireAmber.copy(alpha = 0.10f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.72f),
                            radius = size.minDimension * 0.56f
                        )
                        onDrawBehind {
                            drawRect(coreGlow)
                        }
                    }
                    .pointerInput(account?.username) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                flingJob?.cancel()
                                flingJob = null
                                velocityTracker.resetTracking()
                            },
                            onDragEnd = {
                                val releaseVelocity = -velocityTracker.calculateVelocity().x * DOLL_DRAG_DEG_PER_PX
                                flingJob?.cancel()
                                if (abs(releaseVelocity) < DOLL_FLING_MIN_VELOCITY) {
                                    flingJob = null
                                    return@detectHorizontalDragGestures
                                }
                                flingJob = flingScope.launch {
                                    var velocity = releaseVelocity
                                    var lastFrame = withFrameNanos { it }
                                    while (abs(velocity) > DOLL_FLING_STOP_VELOCITY) {
                                        ensureActive()
                                        val now = withFrameNanos { it }
                                        val dt = ((now - lastFrame) / 1_000_000_000f).coerceIn(0f, 0.05f)
                                        lastFrame = now
                                        dollAzimuth = (dollAzimuth + velocity * dt).mod(360f)
                                        velocity *= exp(-DOLL_FLING_FRICTION * dt)
                                    }
                                    flingJob = null
                                }
                            },
                            onDragCancel = { flingJob?.cancel(); flingJob = null },
                            onHorizontalDrag = { change, dragAmount ->
                                velocityTracker.addPosition(change.uptimeMillis, change.position)
                                dollAzimuth = (dollAzimuth - dragAmount * DOLL_DRAG_DEG_PER_PX).mod(360f)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AerixSkinPreview3D(
                    account = account,
                    azimuth = dollAzimuth.roundToInt(),
                    interactionEnabled = false,
                    useClientCape = true,
                    showOfficialCape = true,
                    animationName = "NewIdle",
                    cameraDistance = 48,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AerixSpacing.xs)
            ) {
                Surface(
                    onClick = actions.accounts,
                    modifier = Modifier.weight(1f),
                    shape = FireShapes.hudChip,
                    color = AerixSurface.firePanelRaised,
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (account == null) "+ LINK ACCOUNT" else "SKIN WARDROBE",
                        color = AerixSurface.textPrimary,
                        fontSize = 9.5.sp,
                        letterSpacing = 0.6.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp)
                    )
                }
                Surface(
                    onClick = actions.aerixAccount,
                    modifier = Modifier.weight(1f),
                    shape = FireShapes.hudChip,
                    color = accent.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, AerixSurface.fireAmber.copy(alpha = 0.55f))
                ) {
                    Text(
                        text = if (SocialBackend.signedIn) "CLOUD ID ✓" else "CLOUD CAPE",
                        color = AerixSurface.fireAmber,
                        fontSize = 9.5.sp,
                        letterSpacing = 0.6.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

/** Center 2x2 Asymmetric Chamfered Tactical Deploy Matrix. */
@Composable
private fun TacticalMissionMatrix(
    count: Int,
    accent: Color,
    actions: HomeActions,
    modifier: Modifier = Modifier,
    showSwitch: Boolean = true
) {
    val playableNow = SocialBackend.friends.count { it.online && !it.pendingIncoming && !it.pendingOutgoing }
    val waiting = SocialBackend.totalActionableBadge
    val socialSubtitle = when {
        playableNow > 0 && waiting > 0 -> "$playableNow online · $waiting alerts"
        playableNow > 0 -> "$playableNow squad online"
        waiting > 0 -> "$waiting unread / invite${if (waiting == 1) "" else "s"}"
        else -> "Party, chat & live join"
    }

    Surface(
        modifier = modifier.liquidGlass(
            shape = FireShapes.armorCard,
            tint = accent,
            strength = 0.78f,
            elevation = AerixMetrics.glassSubtleElevation
        ),
        shape = FireShapes.armorCard,
        color = AerixSurface.firePanel,
        border = BorderStroke(1.dp, AerixSurface.borderSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AerixSpacing.smPlus),
            verticalArrangement = Arrangement.spacedBy(AerixSpacing.xsPlus)
        ) {
            if (showSwitch) {
                FireHudStyleSwitch(modifier = Modifier.fillMaxWidth())
            }
            SectionLabel(
                title = "TACTICAL COMMAND MATRIX",
                action = "ALL OPS",
                onAction = actions.quickTools
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(AerixSpacing.xsPlus)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AerixSpacing.xsPlus)
                ) {
                    QuickActionTile(
                        iconRes = R.drawable.aerix_ic_people,
                        title = "CREW & PARTY",
                        subtitle = socialSubtitle,
                        accent = Color(0xFF38BDF8),
                        onClick = actions.socials,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        iconRes = R.drawable.aerix_ic_public,
                        title = "MOD ARMORY",
                        subtitle = "Mods, Shaders & Packs",
                        accent = Color(0xFFF472B6),
                        onClick = actions.explore,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AerixSpacing.xsPlus)
                ) {
                    QuickActionTile(
                        iconRes = R.drawable.aerix_ic_touch_app_outlined,
                        title = "CONTROL RIG",
                        subtitle = "Touch HUD & Keybinds",
                        accent = Color(0xFF34D399),
                        onClick = actions.controls,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        iconRes = R.drawable.aerix_ic_java,
                        title = "RUN .JAR",
                        subtitle = "$count cores in Vault",
                        accent = Color(0xFFFB923C),
                        onClick = actions.runJar,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/** Center-Bottom Engine Telemetry & Diagnostics Micro-Bar. */
@Composable
private fun ReactorTelemetryBar(
    taskCount: Int,
    accent: Color,
    actions: HomeActions,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.liquidGlass(
            shape = FireShapes.armorMedium,
            tint = accent,
            strength = 0.72f,
            elevation = AerixMetrics.glassSubtleElevation
        ),
        shape = FireShapes.armorMedium,
        color = AerixSurface.firePanelDeep,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AerixSpacing.smPlus, vertical = AerixSpacing.xsPlus),
            verticalArrangement = Arrangement.spacedBy(AerixSpacing.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(if (taskCount > 0) AerixSurface.fireAmber else AerixSurface.success)
                    )
                    Text(
                        text = if (taskCount > 0) "ENGINE BUSY · $taskCount JOBS" else "REACTOR STANDBY · READY",
                        color = AerixSurface.textPrimary,
                        fontSize = 10.sp,
                        letterSpacing = 0.9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    text = "DIAGNOSTICS",
                    color = AerixSurface.textMuted,
                    fontSize = 9.sp,
                    letterSpacing = 1.1.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TelemetryMiniButton("GAMEPAD", R.drawable.aerix_ic_sports_esports_outlined, accent, actions.gamepad)
                TelemetryMiniButton("GAME DIR", R.drawable.aerix_ic_folder_outlined, accent, actions.files)
                TelemetryMiniButton("FILE OPS", R.drawable.aerix_ic_folder_filled, accent, actions.fileManager)
                TelemetryMiniButton("CONSOLE", R.drawable.aerix_ic_terminal_outlined, accent, actions.logs)
                TelemetryMiniButton("SHARE LOG", R.drawable.aerix_ic_share_filled, accent, actions.shareLogs)
                TelemetryMiniButton("MODS", R.drawable.aerix_ic_extension_outlined, accent, actions.content)
            }
        }
    }
}

@Composable
private fun TelemetryMiniButton(label: String, iconRes: Int, accent: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = FireShapes.hudChip,
        color = AerixSurface.firePanelRaised,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.34f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(painterResource(iconRes), contentDescription = null, tint = AerixSurface.fireAmber, modifier = Modifier.size(13.dp))
            Text(label, color = AerixSurface.textPrimary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
        }
    }
}

/** Right-Top Active Reactor Core Hero Card. */
@Composable
private fun ActiveReactorHeroCard(
    selected: AerixInstance?,
    accent: Color,
    actions: HomeActions,
    modifier: Modifier = Modifier
) {
    val vanilla = stringResource(R.string.aerix_home_vanilla)
    Surface(
        modifier = modifier.liquidGlass(
            shape = FireShapes.armorCard,
            tint = accent,
            strength = 0.92f,
            elevation = AerixMetrics.glassSubtleElevation
        ),
        shape = FireShapes.armorCard,
        color = AerixSurface.firePanelDeep,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0x3338BDF8),
                            AerixSurface.firePanelDeep,
                            Color(0x29EC4899)
                        )
                    )
                )
                .padding(AerixSpacing.smPlus),
            verticalArrangement = Arrangement.spacedBy(AerixSpacing.xsPlus)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "// ACTIVE REACTOR CORE",
                    color = AerixSurface.fireAmber,
                    fontSize = 9.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (selected != null) "ARMED" else "UNLOADED",
                    color = if (selected != null) AerixSurface.success else AerixSurface.warning,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AerixSpacing.sm)
            ) {
                AerixImageWell(
                    image = selected?.icon,
                    fallback = R.drawable.aerix_ic_videogame_asset_outlined,
                    size = 44.dp,
                    radius = AerixRadii.card
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = (selected?.name ?: stringResource(R.string.aerix_home_no_active_world)).uppercase(),
                        color = AerixSurface.textPrimary,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = selected?.let { instanceVersionLabel(it.versionId, vanilla) }
                            ?: stringResource(R.string.aerix_home_create_first),
                        color = AerixSurface.fireAmber,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (selected != null) {
                    IconButton(
                        onClick = { actions.edit(selected) },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(FireShapes.hudChip)
                            .background(AerixSurface.firePanelRaised)
                            .border(BorderStroke(1.dp, accent.copy(alpha = 0.45f)), FireShapes.hudChip)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.aerix_ic_settings_filled),
                            contentDescription = stringResource(R.string.aerix_home_instance_settings),
                            tint = AerixSurface.fireAmber,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AerixSpacing.xs)
            ) {
                AerixPrimaryButton(
                    text = if (selected != null) "LAUNCH CORE" else "FORGE INSTANCE",
                    iconRes = if (selected != null) R.drawable.aerix_ic_play_arrow_filled else R.drawable.aerix_ic_add,
                    onClick = if (selected != null) actions.play else actions.create,
                    modifier = Modifier.weight(1f)
                )
                AerixGhostButton(
                    text = "SWITCH",
                    iconRes = R.drawable.aerix_ic_dashboard_filled,
                    onClick = actions.instanceSelector
                )
            }
        }
    }
}

/** Right-Bottom Vertical Rapid Instance Cartridge Rack. */
@Composable
private fun InstanceCartridgeRack(
    instances: List<AerixInstance>,
    selected: AerixInstance?,
    accent: Color,
    actions: HomeActions,
    modifier: Modifier = Modifier,
    lazy: Boolean = true
) {
    Surface(
        modifier = modifier.liquidGlass(
            shape = FireShapes.armorCard,
            tint = accent,
            strength = 0.76f,
            elevation = AerixMetrics.glassSubtleElevation
        ),
        shape = FireShapes.armorCard,
        color = AerixSurface.firePanel,
        border = BorderStroke(1.dp, AerixSurface.borderSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AerixSpacing.smPlus),
            verticalArrangement = Arrangement.spacedBy(AerixSpacing.xsPlus)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SectionLabel(
                    title = "CARTRIDGE RACK (${instances.size})",
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "+ FORGE",
                        color = AerixSurface.fireAmber,
                        fontSize = 9.5.sp,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .clip(FireShapes.hudChip)
                            .background(accent.copy(alpha = 0.2f))
                            .clickable(onClick = actions.create)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                    Text(
                        text = "VAULT ▸",
                        color = accent,
                        fontSize = 9.5.sp,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .clip(FireShapes.hudChip)
                            .clickable(onClick = actions.library)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            if (instances.isEmpty()) {
                QuietEmptyState(
                    iconRes = R.drawable.aerix_ic_videogame_asset_outlined,
                    text = stringResource(R.string.aerix_library_empty),
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (lazy) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(instances, key = { it.id }) { instance ->
                        CartridgeRow(instance = instance, isSelected = instance == selected, accent = accent, actions = actions)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    instances.take(8).forEach { instance ->
                        CartridgeRow(instance = instance, isSelected = instance == selected, accent = accent, actions = actions)
                    }
                }
            }
        }
    }
}

@Composable
private fun CartridgeRow(
    instance: AerixInstance,
    isSelected: Boolean,
    accent: Color,
    actions: HomeActions
) {
    val vanilla = stringResource(R.string.aerix_home_vanilla)
    val shape = FireShapes.armorSmall
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isSelected) accent.copy(alpha = 0.20f) else AerixSurface.firePanelRaised)
            .border(
                BorderStroke(1.dp, if (isSelected) AerixSurface.fireAmber else AerixSurface.borderSoft),
                shape
            )
            .clickable(role = Role.Button) { actions.select(instance) }
            .padding(horizontal = AerixSpacing.sm, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AerixSpacing.xsPlus)
    ) {
        AerixImageWell(
            image = instance.icon,
            fallback = R.drawable.aerix_ic_videogame_asset_outlined,
            size = 30.dp,
            radius = AerixRadii.tiny
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = instance.name,
                color = AerixSurface.textPrimary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = instanceVersionLabel(instance.versionId, vanilla),
                color = if (isSelected) AerixSurface.fireAmber else AerixSurface.textSecondary,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = { actions.edit(instance) }, modifier = Modifier.size(28.dp)) {
            Icon(
                painter = painterResource(R.drawable.aerix_ic_edit_outlined),
                contentDescription = stringResource(R.string.aerix_home_instance_settings),
                tint = AerixSurface.textSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
        Surface(
            onClick = { actions.playInstance(instance) },
            shape = FireShapes.hudChip,
            color = accent
        ) {
            Icon(
                painter = painterResource(R.drawable.aerix_ic_play_arrow_filled),
                contentDescription = stringResource(R.string.aerix_home_play),
                tint = AerixSurface.onFireAccentDefault,
                modifier = Modifier
                    .padding(horizontal = 7.dp, vertical = 5.dp)
                    .size(14.dp)
            )
        }
    }
}

@Composable
private fun SameVersionNudgeStrip(
    selected: AerixInstance?,
    accent: Color,
    onOpenSocials: () -> Unit
) {
    val myVer = remember(selected?.versionId) {
        SocialBackend.extractMcVersion(selected?.versionId)
    } ?: return
    val matching = SocialBackend.friends.filter { f ->
        !f.pendingIncoming && !f.pendingOutgoing && f.online && f.mcVersion.equals(myVer, ignoreCase = true)
    }
    if (matching.isEmpty()) return
    val names = matching.take(2).joinToString(", ") { it.effectiveName }
    Surface(
        shape = FireShapes.armorSmall,
        color = accent.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, AerixSurface.fireAmber.copy(alpha = 0.65f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onOpenSocials)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AerixSpacing.smPlus, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AerixSpacing.xsPlus)
        ) {
            Icon(
                painter = painterResource(R.drawable.aerix_ic_people),
                contentDescription = null,
                tint = AerixSurface.fireAmber,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "$names ${if (matching.size == 1) "is" else "are"} live on $myVer — JOIN SQUAD",
                color = AerixSurface.textPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(R.drawable.aerix_ic_chevron_right),
                contentDescription = null,
                tint = AerixSurface.fireAmber,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

/** Chamfered Tactical HUD Layout Mode Switcher. */
@Composable
private fun FireHudStyleSwitch(modifier: Modifier = Modifier) {
    val selected = AerixPreferences.uiStyle
    val accent = AerixThemeManager.currentAccent()
    Row(
        modifier = modifier
            .clip(FireShapes.armorSmall)
            .background(AerixSurface.firePanelDeep)
            .border(1.dp, accent.copy(alpha = 0.4f), FireShapes.armorSmall)
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AerixLauncherUiStyle.values().forEach { style ->
            val active = style == selected
            val pillAlpha by animateFloatAsState(
                if (active) 1f else 0f,
                animationSpec = tween(220, easing = FastOutSlowInEasing),
                label = "fireHudModePill"
            )
            Text(
                text = style.label,
                modifier = Modifier
                    .weight(1f)
                    .clip(FireShapes.hudChip)
                    .background(accent.copy(alpha = 0.88f * pillAlpha))
                    .clickable { AerixPreferences.saveUiStyle(style) }
                    .padding(vertical = 5.dp),
                color = if (active) AerixSurface.onFireAccentDefault else AerixSurface.textSecondary,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}
