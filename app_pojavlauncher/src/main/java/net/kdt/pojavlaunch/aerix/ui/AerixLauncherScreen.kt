/*
 * Fire Launcher Shell Screen.
 * Complete structural inversion from the legacy side-rail layout:
 *   - Top Edge: Tactical HUD Command Header (Operator ID, Utilities, Active Cartridge, IGNITE Trigger)
 *   - Center Viewport: 100% full-width edge-to-edge screen stage
 *   - Bottom Edge: Floating Molten Horizontal Command Dock
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package net.kdt.pojavlaunch.aerix.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.fragment.app.FragmentActivity
import net.kdt.pojavlaunch.aerix.bridge.AerixInstallQueue
import net.kdt.pojavlaunch.aerix.bridge.AerixPreferences
import net.kdt.pojavlaunch.aerix.bridge.AerixTransitionType
import net.kdt.pojavlaunch.aerix.bridge.AerixWindowInsets
import net.kdt.pojavlaunch.aerix.bridge.PojavAccountBridge
import net.kdt.pojavlaunch.aerix.bridge.PojavInstanceBridge
import net.kdt.pojavlaunch.aerix.bridge.PojavInstanceEditorBridge
import net.kdt.pojavlaunch.aerix.bridge.PojavLaunchBridge
import net.kdt.pojavlaunch.aerix.components.AerixBackground
import net.kdt.pojavlaunch.aerix.discord.AerixDiscordPresence
import net.kdt.pojavlaunch.aerix.festivals.FestivalEffectsHost
import net.kdt.pojavlaunch.aerix.festivals.FestivalTitleText
import net.kdt.pojavlaunch.aerix.festivals.rememberTodayFestivals
import net.kdt.pojavlaunch.aerix.guide.AerixGuideOverlay
import net.kdt.pojavlaunch.aerix.guide.AerixGuideState
import net.kdt.pojavlaunch.aerix.guide.GuideRegistry
import net.kdt.pojavlaunch.aerix.guide.LocalGuideRegistry
import net.kdt.pojavlaunch.aerix.guide.rememberGuideSteps
import net.kdt.pojavlaunch.aerix.social.SocialBackend
import net.kdt.pojavlaunch.aerix.theme.AerixSpacing
import net.kdt.pojavlaunch.aerix.theme.AerixSurface
import net.kdt.pojavlaunch.aerix.theme.AerixThemeManager
import net.kdt.pojavlaunch.aerix.ui.screens.AboutScreen
import net.kdt.pojavlaunch.aerix.ui.screens.AccountsScreen
import net.kdt.pojavlaunch.aerix.ui.screens.AddAccountScreen
import net.kdt.pojavlaunch.aerix.ui.screens.AerixAccountSetupScreen
import net.kdt.pojavlaunch.aerix.ui.screens.AerixContribution
import net.kdt.pojavlaunch.aerix.ui.screens.AerixInstallPanel
import net.kdt.pojavlaunch.aerix.ui.screens.AerixInstallPill
import net.kdt.pojavlaunch.aerix.ui.screens.ClientHubScreen
import net.kdt.pojavlaunch.aerix.ui.screens.ClientOwnerPage
import net.kdt.pojavlaunch.aerix.ui.screens.ContributionDetailScreen
import net.kdt.pojavlaunch.aerix.ui.screens.ContributionScreen
import net.kdt.pojavlaunch.aerix.ui.screens.CreateInstanceScreen
import net.kdt.pojavlaunch.aerix.ui.screens.DiscoverScreen
import net.kdt.pojavlaunch.aerix.ui.screens.FileManagerScreen
import net.kdt.pojavlaunch.aerix.ui.screens.HomeScreen
import net.kdt.pojavlaunch.aerix.ui.screens.InstanceEditorScreen
import net.kdt.pojavlaunch.aerix.ui.screens.InstanceSelectorScreen
import net.kdt.pojavlaunch.aerix.ui.screens.LibraryScreen
import net.kdt.pojavlaunch.aerix.ui.screens.LogScreen
import net.kdt.pojavlaunch.aerix.ui.screens.ModpacksScreen
import net.kdt.pojavlaunch.aerix.ui.screens.OfflineAccountScreen
import net.kdt.pojavlaunch.aerix.ui.screens.QuickToolsScreen
import net.kdt.pojavlaunch.aerix.ui.screens.ServersScreen
import net.kdt.pojavlaunch.aerix.ui.screens.SettingsScreen
import net.kdt.pojavlaunch.aerix.ui.screens.SocialsNavigation
import net.kdt.pojavlaunch.aerix.ui.screens.SocialsScreen

@Composable
fun AerixLauncherScreen(activity: FragmentActivity) {
    AerixNav.init()
    net.kdt.pojavlaunch.aerix.ui.screens.FirstRunNotificationPopupHost(activity)
    net.kdt.pojavlaunch.aerix.ui.screens.LauncherUpdateNoticeHost(activity)
    val section = AerixNav.section
    var clientOwnerPage by rememberSaveable { mutableStateOf(ClientOwnerPage.OWNERS) }
    val navigate: (LauncherSection) -> Unit = { target ->
        when (target) {
            LauncherSection.SETTINGS -> AerixNav.navigate(LauncherSection.SETTINGS)
            LauncherSection.ABOUT -> {
                AerixNav.settingsTab = AerixNav.TAB_ABOUT
                AerixNav.navigate(LauncherSection.SETTINGS)
            }
            LauncherSection.CONTRIBUTION -> {
                AerixContribution.openedId = null
                AerixNav.navigate(LauncherSection.CONTRIBUTION)
            }
            LauncherSection.AERIX_CLIENT -> {
                clientOwnerPage = ClientOwnerPage.OWNERS
                AerixNav.navigate(LauncherSection.AERIX_CLIENT)
            }
            LauncherSection.DOWNLOADS -> AerixNav.navigate(LauncherSection.DISCOVER)
            LauncherSection.CONTENT -> AerixNav.navigate(LauncherSection.INSTANCE_EDITOR)
            else -> AerixNav.navigate(target)
        }
    }
    val duration = AerixPreferences.transitionMillis()
    val transitionType = AerixPreferences.swapAnimateType
    val extent = AerixPreferences.extentScale()
    val festivals = rememberTodayFestivals()
    val guideRegistry = remember { GuideRegistry() }
    val guideSteps = rememberGuideSteps()
    val notificationPromptAnswered = AerixPreferences.notificationPromptShown
    LaunchedEffect(notificationPromptAnswered) {
        if (!notificationPromptAnswered) return@LaunchedEffect
        AerixGuideState.startOnFirstRun()
        if (AerixGuideState.running && AerixNav.section != LauncherSection.HOME) {
            AerixNav.navigate(LauncherSection.HOME)
        }
    }

    LaunchedEffect(Unit) {
        AerixDiscordPresence.attach(activity)
    }
    val activePartySnapshot = SocialBackend.parties
        .firstOrNull { !it.isExpired() }
        ?.let { "${it.id}:${it.memberCount}:${it.maxMembers}" }
    LaunchedEffect(section, activePartySnapshot) {
        AerixDiscordPresence.inLauncher(section.name)
    }

    BackHandler(enabled = AerixNav.canGoBack) { AerixNav.back() }

    CompositionLocalProvider(LocalGuideRegistry provides guideRegistry) {
    AerixBackground {
      FestivalEffectsHost(festivals = festivals, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().padding(AerixWindowInsets.padding())) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. TOP COMMAND HEADER BAR (replaces right-side rail + relocates Play/Account/Utilities)
                AerixHomeUtilityRail(
                    selectedSection = section,
                    onNavigate = navigate,
                    onOpenProfile = {
                        SocialsNavigation.requestMyProfile()
                        navigate(LauncherSection.SOCIALS)
                    },
                    onQuickTools = { navigate(LauncherSection.QUICK_TOOLS) },
                    onInstances = { navigate(LauncherSection.INSTANCE_SELECTOR) },
                    onIgnite = {
                        if (PojavInstanceBridge.selected != null) {
                            PojavLaunchBridge.play(activity)
                        } else {
                            AerixNav.navigate(LauncherSection.CREATE_INSTANCE)
                        }
                    }
                )

                // Login progress conduit right under the Top Command Header
                val progress = PojavAccountBridge.loginProgress
                if (progress < 1f) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AerixSpacing.lg),
                        color = if (PojavAccountBridge.loginError) AerixSurface.danger else AerixThemeManager.currentAccent(),
                        trackColor = Color.Transparent
                    )
                }

                // 2. CENTER FULL-WIDTH STAGE (zero side rails — 100% horizontal viewport)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = section,
                        transitionSpec = { sectionTransition(transitionType, duration, extent) },
                        label = "aerixSection"
                    ) { target ->
                        when (target) {
                            LauncherSection.OTHER_RELEASES -> net.kdt.pojavlaunch.aerix.ui.screens.OtherReleasesScreen(activity, navigate)
                            LauncherSection.HOME -> HomeScreen(activity, navigate)
                            LauncherSection.QUICK_TOOLS -> QuickToolsScreen(activity, navigate)
                            LauncherSection.INSTANCE_SELECTOR -> InstanceSelectorScreen(activity, navigate)
                            LauncherSection.AERIX_CLIENT -> ClientHubScreen(activity, clientOwnerPage) { clientOwnerPage = it }
                            LauncherSection.LIBRARY -> LibraryScreen(activity)
                            LauncherSection.SERVERS -> ServersScreen(activity)
                            LauncherSection.MODPACKS -> ModpacksScreen(navigate)
                            LauncherSection.SOCIALS -> SocialsScreen(activity) { AerixNav.finishTo(LauncherSection.HOME) }
                            LauncherSection.DISCOVER, LauncherSection.DOWNLOADS -> DiscoverScreen(activity, navigate)
                            LauncherSection.FILES -> FileManagerScreen(activity)
                            LauncherSection.LOGS -> LogScreen(activity)
                            LauncherSection.SETTINGS -> SettingsScreen(
                                activity = activity,
                                tabKey = AerixNav.settingsTab,
                                onTabChanged = { AerixNav.settingsTab = it }
                            )
                            LauncherSection.CONTRIBUTION -> ContributionScreen(onOpen = { member ->
                                AerixContribution.openedId = member.id
                                AerixNav.navigate(LauncherSection.CONTRIBUTION_DETAIL)
                            })
                            LauncherSection.CONTRIBUTION_DETAIL -> {
                                val member = AerixContribution.byId(AerixContribution.openedId)
                                if (member == null) {
                                    ContributionScreen(onOpen = { opened -> AerixContribution.openedId = opened.id })
                                } else {
                                    ContributionDetailScreen(member = member, onBack = { AerixNav.back() })
                                }
                            }
                            LauncherSection.ACCOUNTS -> AccountsScreen(activity)
                            LauncherSection.AERIX_ACCOUNT -> AerixAccountSetupScreen(activity) { AerixNav.back() }
                            LauncherSection.ABOUT -> AboutScreen(activity)
                            LauncherSection.CREATE_INSTANCE -> CreateInstanceScreen(activity) {
                                AerixNav.finishTo(LauncherSection.LIBRARY)
                            }
                            LauncherSection.INSTANCE_EDITOR, LauncherSection.CONTENT -> InstanceEditorScreen(activity) {
                                PojavInstanceEditorBridge.close()
                                AerixNav.finishTo(LauncherSection.LIBRARY)
                            }
                            LauncherSection.ADD_ACCOUNT -> AddAccountScreen(
                                activity = activity,
                                onClose = { AerixNav.finishTo(LauncherSection.ACCOUNTS) },
                                onOffline = { AerixNav.navigate(LauncherSection.OFFLINE_ACCOUNT) }
                            )
                            LauncherSection.OFFLINE_ACCOUNT -> OfflineAccountScreen(
                                activity = activity,
                                onClose = { AerixNav.navigate(LauncherSection.ADD_ACCOUNT) },
                                onCreated = { AerixNav.finishTo(LauncherSection.ACCOUNTS) }
                            )
                        }
                    }
                }

                // 3. BOTTOM FLOATING MOLTEN COMMAND DOCK (replaces left vertical rail)
                if (section != LauncherSection.SOCIALS) {
                    AerixNavigationRail(
                        selectedSection = section,
                        onNavigate = navigate,
                        onCreateInstance = { AerixNav.navigate(LauncherSection.CREATE_INSTANCE) }
                    )
                }
            }

            FestivalTitleText(
                festivals = festivals,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = AerixSpacing.section, start = AerixSpacing.lg)
            )

            // Live join toast anchored at BottomEnd above the dock
            net.kdt.pojavlaunch.aerix.social.SocialBackend.activeJoinToast?.let { toast ->
                net.kdt.pojavlaunch.aerix.ui.screens.SocialJoinToastBanner(
                    toast = toast,
                    onJoin = {
                        net.kdt.pojavlaunch.aerix.social.SocialBackend.dismissJoinToast()
                        val party = toast.partyId?.let { pid ->
                            net.kdt.pojavlaunch.aerix.social.SocialBackend.parties.firstOrNull { it.id == pid }
                        }
                        net.kdt.pojavlaunch.aerix.bridge.PojavLaunchBridge.joinSocialSession(
                            activity = activity,
                            mcVersion = party?.mcVersion ?: toast.mcVersion,
                            loaderName = party?.loader ?: toast.loader,
                            instanceHint = party?.instanceName,
                            modpackSource = party?.modpackSource,
                            modpackId = party?.modpackId,
                            modpackIconUrl = party?.modpackIconUrl,
                            serverAddress = party?.serverAddress ?: toast.serverAddress,
                            onNavigate = navigate
                        )
                    },
                    onDismiss = { net.kdt.pojavlaunch.aerix.social.SocialBackend.dismissJoinToast() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = AerixSpacing.section, end = AerixSpacing.lg)
                )
            }

            // Install pill relocated to TopStart (below Top Command Header)
            if (AerixInstallQueue.visible) {
                if (AerixInstallQueue.panelOpen) {
                    AerixInstallPanel(onMinimize = { AerixInstallQueue.minimizePanel() })
                } else {
                    AerixInstallPill(
                        onExpand = { AerixInstallQueue.openPanel() },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = AerixSpacing.section + AerixSpacing.sm, start = AerixSpacing.lg)
                    )
                }
            }

            if (AerixGuideState.running && section == LauncherSection.HOME) {
                key(AerixGuideState.runId) {
                    AerixGuideOverlay(
                        steps = guideSteps,
                        onFinished = { AerixGuideState.finish() }
                    )
                }
            }
        }
      }
    }
    }
}

private fun AnimatedContentTransitionScope<LauncherSection>.sectionTransition(
    type: AerixTransitionType,
    duration: Int,
    extent: Float
): ContentTransform {
    if (duration == 0 || type == AerixTransitionType.CLOSE) {
        return fadeIn(snap()) togetherWith fadeOut(snap())
    }
    val exit = fadeOut(tween((duration * 0.75f).toInt().coerceAtLeast(80), easing = FastOutSlowInEasing))
    val forward = targetState.ordinal >= initialState.ordinal
    return when (type) {
        AerixTransitionType.JELLY_BOUNCE -> (fadeIn(tween(duration)) +
            scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                initialScale = 1f - 0.08f * extent)) togetherWith exit
        AerixTransitionType.BOUNCE -> (fadeIn(tween(duration)) +
            slideInVertically(spring<IntOffset>(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessLow)) {
                (it / 10 * extent).toInt()
            }) togetherWith exit
        AerixTransitionType.SLICE_IN -> (fadeIn(tween(duration)) +
            slideInHorizontally(tween(duration, easing = FastOutSlowInEasing)) {
                val distance = (it / 6 * extent).toInt()
                if (forward) distance else -distance
            }) togetherWith exit
        AerixTransitionType.CLOSE -> fadeIn(snap()) togetherWith fadeOut(snap())
    }
}
