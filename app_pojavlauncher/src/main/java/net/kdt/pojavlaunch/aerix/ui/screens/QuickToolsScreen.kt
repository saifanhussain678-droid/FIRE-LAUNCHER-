/*
 * Fire Launcher Tactical Quick Tools Screen ("ENGINE OPS MATRIX").
 * Replaces the old single-column list with a 2-column chamfered Tactical Operations Grid
 * and an inverted Command Header (Close/Back on Top-Right, Ops Readout on Top-Left).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package net.kdt.pojavlaunch.aerix.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.aerix.bridge.PojavInstanceBridge
import net.kdt.pojavlaunch.aerix.bridge.PojavNavigator
import net.kdt.pojavlaunch.aerix.components.liquidGlass
import net.kdt.pojavlaunch.aerix.theme.AerixMetrics
import net.kdt.pojavlaunch.aerix.theme.AerixSpacing
import net.kdt.pojavlaunch.aerix.theme.AerixSurface
import net.kdt.pojavlaunch.aerix.theme.AerixThemeManager
import net.kdt.pojavlaunch.aerix.theme.FireShapes
import net.kdt.pojavlaunch.aerix.ui.AerixNav
import net.kdt.pojavlaunch.aerix.ui.LauncherSection
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension

private data class FireOpsSpec(
    val iconRes: Int,
    val title: String,
    val subtitle: String,
    val tag: String,
    val onClick: () -> Unit
)

@Composable
fun QuickToolsScreen(activity: FragmentActivity, navigate: (LauncherSection) -> Unit) {
    val selected = PojavInstanceBridge.selected
    val accent = AerixThemeManager.currentAccent()
    val jarLauncher = rememberLauncherForActivityResult(OpenDocumentWithExtension("jar")) { uri ->
        if (uri != null) PojavNavigator.runJar(activity, uri)
    }

    val ops = listOf(
        FireOpsSpec(
            iconRes = R.drawable.aerix_ic_touch_app_outlined,
            title = stringResource(R.string.aerix_home_controls),
            subtitle = stringResource(R.string.aerix_home_controls_sub),
            tag = "HUD",
            onClick = { PojavNavigator.openCustomControls(activity) }
        ),
        FireOpsSpec(
            iconRes = R.drawable.aerix_ic_sports_esports_outlined,
            title = stringResource(R.string.aerix_home_gamepad),
            subtitle = stringResource(R.string.aerix_home_gamepad_sub),
            tag = "INPUT",
            onClick = { PojavNavigator.openGamepadMapper(activity) }
        ),
        FireOpsSpec(
            iconRes = R.drawable.aerix_ic_java,
            title = stringResource(R.string.aerix_home_run_jar),
            subtitle = stringResource(R.string.aerix_home_run_jar_sub),
            tag = "JVM",
            onClick = { if (PojavNavigator.canRunJar(activity)) jarLauncher.launch(null) }
        ),
        FireOpsSpec(
            iconRes = R.drawable.aerix_ic_folder_outlined,
            title = stringResource(R.string.aerix_home_game_folder),
            subtitle = stringResource(R.string.aerix_home_files_sub),
            tag = "PATH",
            onClick = { PojavInstanceBridge.openGameDirectory(activity) }
        ),
        FireOpsSpec(
            iconRes = R.drawable.aerix_ic_folder_filled,
            title = stringResource(R.string.aerix_nav_files),
            subtitle = stringResource(R.string.aerix_quick_tools_files_sub),
            tag = "FS",
            onClick = { navigate(LauncherSection.FILES) }
        ),
        FireOpsSpec(
            iconRes = R.drawable.aerix_ic_terminal_outlined,
            title = stringResource(R.string.aerix_nav_logs),
            subtitle = stringResource(R.string.aerix_quick_tools_logs_sub),
            tag = "STDIO",
            onClick = { navigate(LauncherSection.LOGS) }
        ),
        FireOpsSpec(
            iconRes = R.drawable.aerix_ic_share_filled,
            title = stringResource(R.string.aerix_home_share_logs),
            subtitle = stringResource(R.string.aerix_home_share_logs_sub),
            tag = "DIAG",
            onClick = { PojavNavigator.shareLogs(activity) }
        ),
        FireOpsSpec(
            iconRes = R.drawable.aerix_ic_extension_outlined,
            title = stringResource(R.string.aerix_home_content),
            subtitle = stringResource(R.string.aerix_quick_tools_content_sub),
            tag = "MODS",
            onClick = {
                selected?.let { PojavInstanceBridge.edit(activity, it) } ?: navigate(LauncherSection.LIBRARY)
            }
        )
    )

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "// SYSTEM UTILITIES",
                        color = AerixSurface.fireAmber,
                        fontSize = 9.5.sp,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(R.string.aerix_quick_tools_title).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
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
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.aerix_ic_close),
                            contentDescription = "Back",
                            tint = AerixSurface.fireAmber,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "EXIT OPS",
                            color = AerixSurface.textPrimary,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        val rows = ops.chunked(2)
        items(rows.size) { rowIdx ->
            val pair = rows[rowIdx]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AerixSpacing.smPlus)
            ) {
                pair.forEach { spec ->
                    FireOpsCard(spec = spec, accent = accent, modifier = Modifier.weight(1f))
                }
                repeat(2 - pair.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun FireOpsCard(
    spec: FireOpsSpec,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = spec.onClick,
        modifier = modifier.liquidGlass(
            shape = FireShapes.armorMedium,
            tint = accent,
            strength = 0.80f,
            elevation = AerixMetrics.glassSubtleElevation
        ),
        shape = FireShapes.armorMedium,
        color = AerixSurface.firePanelRaised,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.36f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AerixSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AerixSpacing.smPlus)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(FireShapes.armorSmall)
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(spec.iconRes),
                    contentDescription = null,
                    tint = AerixSurface.fireAmber,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = spec.title,
                        color = AerixSurface.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = spec.tag,
                        color = AerixSurface.fireAmber,
                        fontSize = 8.5.sp,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    text = spec.subtitle,
                    color = AerixSurface.textSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                painter = painterResource(R.drawable.aerix_ic_chevron_right),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
