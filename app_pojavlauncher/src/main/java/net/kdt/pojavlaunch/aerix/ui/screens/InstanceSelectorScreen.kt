/*
 * Fire Launcher Tactical Cartridge Selector Screen.
 * Replaces the single-column instance list with a 2-column chamfered Cartridge Deck,
 * placing + FORGE & VAULT actions on the Left and Title + Close on the Right.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package net.kdt.pojavlaunch.aerix.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.aerix.bridge.AerixInstance
import net.kdt.pojavlaunch.aerix.bridge.GameDownloadTarget
import net.kdt.pojavlaunch.aerix.bridge.PojavInstanceBridge
import net.kdt.pojavlaunch.aerix.bridge.PojavLaunchBridge
import net.kdt.pojavlaunch.aerix.components.AerixGhostButton
import net.kdt.pojavlaunch.aerix.components.AerixImageWell
import net.kdt.pojavlaunch.aerix.components.AerixPrimaryButton
import net.kdt.pojavlaunch.aerix.components.QuietEmptyState
import net.kdt.pojavlaunch.aerix.components.liquidGlass
import net.kdt.pojavlaunch.aerix.theme.AerixMetrics
import net.kdt.pojavlaunch.aerix.theme.AerixRadii
import net.kdt.pojavlaunch.aerix.theme.AerixSpacing
import net.kdt.pojavlaunch.aerix.theme.AerixSurface
import net.kdt.pojavlaunch.aerix.theme.AerixThemeManager
import net.kdt.pojavlaunch.aerix.theme.FireShapes
import net.kdt.pojavlaunch.aerix.ui.AerixNav
import net.kdt.pojavlaunch.aerix.ui.LauncherSection

@Composable
fun InstanceSelectorScreen(activity: FragmentActivity, navigate: (LauncherSection) -> Unit) {
    val instances = PojavInstanceBridge.instances
    val selected = PojavInstanceBridge.selected
    val accent = AerixThemeManager.currentAccent()
    val vanilla = stringResource(R.string.aerix_home_vanilla)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = AerixSpacing.lg, vertical = AerixSpacing.smPlus),
        verticalArrangement = Arrangement.spacedBy(AerixSpacing.smPlus)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(AerixSpacing.xsPlus)) {
                    AerixPrimaryButton(
                        text = stringResource(R.string.aerix_home_new_instance),
                        iconRes = R.drawable.aerix_ic_add,
                        onClick = { AerixNav.navigate(LauncherSection.CREATE_INSTANCE) }
                    )
                    AerixGhostButton(
                        text = stringResource(R.string.aerix_home_manage_all),
                        iconRes = R.drawable.aerix_ic_dashboard_filled,
                        onClick = { navigate(LauncherSection.LIBRARY) }
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AerixSpacing.sm)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "// CARTRIDGE SELECTOR",
                            color = AerixSurface.fireAmber,
                            fontSize = 9.sp,
                            letterSpacing = 1.4.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${instances.size} CORES AVAILABLE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = AerixSurface.textPrimary
                        )
                    }
                    Surface(
                        onClick = { AerixNav.back() },
                        shape = FireShapes.hudChip,
                        color = AerixSurface.firePanelRaised,
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.aerix_ic_close),
                            contentDescription = "Close",
                            tint = AerixSurface.fireAmber,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(17.dp)
                        )
                    }
                }
            }
        }

        if (instances.isEmpty()) {
            item {
                QuietEmptyState(
                    iconRes = R.drawable.aerix_ic_videogame_asset_outlined,
                    text = stringResource(R.string.aerix_library_empty),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            val rows = instances.chunked(2)
            items(rows.size) { idx ->
                val pair = rows[idx]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AerixSpacing.smPlus)
                ) {
                    pair.forEach { instance ->
                        FireCartridgeSelectorCard(
                            instance = instance,
                            isSelected = instance == selected,
                            vanilla = vanilla,
                            accent = accent,
                            onSelect = {
                                PojavInstanceBridge.select(instance)
                                AerixNav.back()
                            },
                            onEdit = { PojavInstanceBridge.edit(activity, instance) },
                            onLaunch = {
                                PojavInstanceBridge.select(instance)
                                PojavLaunchBridge.play(activity)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(2 - pair.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun FireCartridgeSelectorCard(
    instance: AerixInstance,
    isSelected: Boolean,
    vanilla: String,
    accent: Color,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onLaunch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mcVersion = GameDownloadTarget.minecraftVersionIn(instance.versionId) ?: instance.versionId
    val loader = loaderLabel(instance.versionId, vanilla)
    val subtitle = if (loader == vanilla) mcVersion else "$loader \u2022 $mcVersion"

    Surface(
        modifier = modifier
            .liquidGlass(
                shape = FireShapes.armorMedium,
                tint = accent,
                strength = if (isSelected) 0.92f else 0.78f,
                elevation = AerixMetrics.glassSubtleElevation
            )
            .clickable(role = Role.Button, onClick = onSelect),
        shape = FireShapes.armorMedium,
        color = if (isSelected) accent.copy(alpha = 0.20f) else AerixSurface.firePanelRaised,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) AerixSurface.fireAmber else accent.copy(alpha = 0.28f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AerixSpacing.smPlus),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AerixSpacing.sm)
        ) {
            AerixImageWell(
                image = instance.icon,
                fallback = R.drawable.aerix_ic_videogame_asset_outlined,
                size = 42.dp,
                radius = AerixRadii.control
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = instance.name,
                    color = AerixSurface.textPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle.uppercase(),
                    color = if (isSelected) AerixSurface.fireAmber else AerixSurface.textSecondary,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(
                    painter = painterResource(R.drawable.aerix_ic_edit_outlined),
                    contentDescription = stringResource(R.string.aerix_home_instance_settings),
                    tint = AerixSurface.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Surface(
                onClick = onLaunch,
                shape = FireShapes.reactorTrigger,
                color = accent,
                border = BorderStroke(1.dp, AerixSurface.fireAmber)
            ) {
                Row(
                    modifier = Modifier
                        .background(accent)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.aerix_ic_play_arrow_filled),
                        contentDescription = stringResource(R.string.aerix_home_play),
                        tint = AerixSurface.onFireAccentDefault,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "IGNITE",
                        color = AerixSurface.onFireAccentDefault,
                        fontSize = 9.5.sp,
                        letterSpacing = 0.7.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
