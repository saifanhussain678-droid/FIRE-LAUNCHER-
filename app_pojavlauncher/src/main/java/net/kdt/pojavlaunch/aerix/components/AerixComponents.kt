/*
 * Fire Launcher Core Tactical UI Building Blocks.
 * Chamfered volcanic obsidian armor plates, magma reactor triggers, and angled HUD tags.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package net.kdt.pojavlaunch.aerix.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.kdt.pojavlaunch.aerix.theme.AerixMetrics
import net.kdt.pojavlaunch.aerix.theme.AerixRadii
import net.kdt.pojavlaunch.aerix.theme.AerixSpacing
import net.kdt.pojavlaunch.aerix.theme.AerixSurface
import net.kdt.pojavlaunch.aerix.theme.FireShapes
import kotlin.math.roundToInt

/** Chamfered volcanic obsidian armor card used for Fire Launcher panels. */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    radius: Dp = AerixRadii.panel,
    tint: Color = AerixSurface.glassTint,
    strength: Float = 0.92f,
    color: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = AerixSpacing.lg,
    content: @Composable ColumnScope.() -> Unit
) {
    val panelColor = if (color == Color.Unspecified) AerixSurface.firePanel else color
    val shape = CutCornerShape(
        topStart = radius,
        topEnd = 3.dp,
        bottomEnd = radius,
        bottomStart = 3.dp
    )
    val base = modifier
        .liquidGlass(shape = shape, tint = tint, strength = strength)
        .background(panelColor, shape)
    val clickable = if (onClick != null) base.clickable(role = Role.Button, onClick = onClick) else base
    Column(
        modifier = clickable.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(AerixSpacing.sm),
        content = content
    )
}

/** Chamfered Blazing Magma Reactor primary action trigger. */
@Composable
fun AerixPrimaryButton(
    text: String,
    @DrawableRes iconRes: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = AerixSurface.accent
) {
    val shape = FireShapes.reactorTrigger
    val brush = if (enabled) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF0EA5E9), accent, Color(0xFFEC4899))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(accent.copy(alpha = 0.28f), accent.copy(alpha = 0.22f))
        )
    }
    Surface(
        modifier = modifier
            .height(42.dp)
            .liquidGlass(shape = shape, tint = accent, strength = if (enabled) 0.96f else 0.4f, elevation = AerixMetrics.glassSelectedElevation),
        shape = shape,
        color = Color.Transparent,
        contentColor = if (enabled) AerixSurface.onAccent else AerixSurface.textMuted,
        border = BorderStroke(1.dp, if (enabled) AerixSurface.fireAmber.copy(alpha = 0.72f) else AerixSurface.borderSoft),
        enabled = enabled,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .background(brush)
                .padding(horizontal = AerixSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AerixSpacing.sm, Alignment.CenterHorizontally)
        ) {
            if (iconRes != null) {
                Icon(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(17.dp))
            }
            Text(
                text = text.uppercase(),
                fontSize = 11.sp,
                letterSpacing = 1.1.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }
    }
}

/** Chamfered tactical obsidian outline button with left magma accent blade. */
@Composable
fun AerixGhostButton(
    text: String,
    @DrawableRes iconRes: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = AerixSurface.textPrimary
) {
    val accent = AerixSurface.accent
    val shape = FireShapes.armorSmall
    Surface(
        modifier = modifier
            .height(38.dp)
            .liquidGlass(shape = shape, tint = accent, strength = 0.65f, elevation = AerixMetrics.glassSubtleElevation),
        shape = shape,
        color = AerixSurface.firePanelRaised,
        contentColor = tint,
        border = BorderStroke(AerixSpacing.hairline, accent.copy(alpha = 0.42f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(end = AerixSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AerixSpacing.xs, Alignment.CenterHorizontally)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(accent)
            )
            Spacer(Modifier.width(AerixSpacing.xs))
            if (iconRes != null) {
                Icon(painter = painterResource(iconRes), contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
            }
            Text(
                text = text.uppercase(),
                fontSize = 10.5.sp,
                letterSpacing = 0.7.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Angled cut-corner HUD tag used for metadata (version id, loader, counts). */
@Composable
fun MetaPill(text: String, @DrawableRes iconRes: Int? = null, modifier: Modifier = Modifier) {
    val accent = AerixSurface.accent
    Surface(
        modifier = modifier,
        shape = FireShapes.hudChip,
        color = AerixSurface.firePanelRaised,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
        contentColor = AerixSurface.textPrimary
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AerixSpacing.smPlus, vertical = AerixSpacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AerixSpacing.xs)
        ) {
            if (iconRes != null) {
                Icon(painter = painterResource(iconRes), contentDescription = null, tint = accent, modifier = Modifier.size(12.dp))
            }
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Uppercase tactical section label with glowing magma tick and optional trailing action. */
@Composable
fun SectionLabel(title: String, modifier: Modifier = Modifier, action: String? = null, onAction: (() -> Unit)? = null) {
    val accent = AerixSurface.accent
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AerixSpacing.xs)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(12.dp)
                .background(accent)
        )
        Text(
            text = title.uppercase(),
            color = AerixSurface.fireAmber,
            fontSize = 10.sp,
            letterSpacing = 1.6.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f)
        )
        if (action != null && onAction != null) {
            Text(
                text = "[ ${action.uppercase()} ]",
                color = accent,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .clip(FireShapes.hudChip)
                    .clickable(onClick = onAction)
                    .padding(horizontal = AerixSpacing.sm, vertical = AerixSpacing.xxs)
            )
        }
    }
}

/** Tactical Page Header: vertical magma blade + FIRE OS micro-label + bold title + action deck. */
@Composable
fun PageHeader(title: String, subtitle: String?, modifier: Modifier = Modifier, trailing: @Composable (() -> Unit)? = null) {
    val accent = AerixSurface.accent
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = FireShapes.armorMedium,
        color = AerixSurface.firePanelDeep,
        border = BorderStroke(AerixSpacing.hairline, accent.copy(alpha = 0.36f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AerixSpacing.md, vertical = AerixSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AerixSpacing.smPlus)
        ) {
            if (trailing != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AerixSpacing.xsPlus)
                ) { trailing() }
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = if (trailing != null) Alignment.End else Alignment.Start
            ) {
                Text(
                    text = "// FIRE OS MODULE",
                    color = accent,
                    fontSize = 9.sp,
                    letterSpacing = 1.8.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = title.uppercase(),
                    color = AerixSurface.textPrimary,
                    fontSize = 18.sp,
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = AerixSurface.textSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(34.dp)
                    .background(Brush.verticalGradient(listOf(accent, AerixSurface.fireAmber)))
            )
        }
    }
}

/** Pastel-tinted Bento glass card matching the Prism Light 2x2 matrix design. */
@Composable
fun QuickActionTile(
    @DrawableRes iconRes: Int,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = FireShapes.armorMedium
    val pastelFill = Brush.linearGradient(
        colors = listOf(
            accent.copy(alpha = 0.26f),
            accent.copy(alpha = 0.12f),
            Color.White.copy(alpha = 0.90f)
        )
    )
    Column(
        modifier = modifier
            .liquidGlass(shape = shape, tint = accent, strength = 0.85f)
            .background(pastelFill, shape)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.88f)), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = AerixSpacing.md, vertical = AerixSpacing.smPlus),
        verticalArrangement = Arrangement.spacedBy(AerixSpacing.xsPlus)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(FireShapes.hudChip)
                    .background(Color.White.copy(alpha = 0.86f))
                    .border(BorderStroke(1.dp, accent.copy(alpha = 0.28f)), FireShapes.hudChip),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = AerixSurface.textPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }
            Icon(
                painter = painterResource(R.drawable.aerix_ic_chevron_right),
                contentDescription = null,
                tint = AerixSurface.textSecondary,
                modifier = Modifier.size(15.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title.uppercase(),
                color = AerixSurface.textPrimary,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = AerixSurface.textSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Empty state shown when a list has nothing to display. */
@Composable
fun QuietEmptyState(@DrawableRes iconRes: Int, text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(AerixSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AerixSpacing.sm)
    ) {
        Icon(painter = painterResource(iconRes), contentDescription = null, tint = AerixSurface.accent.copy(alpha = 0.7f), modifier = Modifier.size(32.dp))
        Text(text = text, color = AerixSurface.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

/** Chamfered segmented command tabs. */
@Composable
fun PillTabs(tabs: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val accent = AerixSurface.accent
    Row(
        modifier = modifier
            .liquidGlass(shape = FireShapes.armorSmall, strength = 0.7f)
            .background(AerixSurface.firePanelDeep, FireShapes.armorSmall)
            .padding(AerixSpacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(AerixSpacing.xxs)
    ) {
        tabs.forEachIndexed { index, label ->
            val isSelected = index == selected
            Box(
                modifier = Modifier
                    .height(AerixMetrics.pillTabHeight - AerixSpacing.xs)
                    .clip(FireShapes.hudChip)
                    .background(
                        if (isSelected) Brush.horizontalGradient(listOf(Color(0xFFFF2A00), accent))
                        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .clickable(role = Role.Tab) { onSelect(index) }
                    .padding(horizontal = AerixSpacing.md),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label.uppercase(),
                    color = if (isSelected) AerixSurface.onAccent else AerixSurface.textSecondary,
                    fontSize = 10.5.sp,
                    letterSpacing = 0.6.sp,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

/** Settings row container shared by switch / slider / text / action rows. */
@Composable
private fun SettingRowFrame(
    title: String,
    summary: String?,
    @DrawableRes iconRes: Int?,
    onClick: (() -> Unit)?,
    trailing: @Composable (() -> Unit)?,
    below: @Composable (() -> Unit)? = null
) {
    val shape = FireShapes.armorSmall
    val base = Modifier
        .fillMaxWidth()
        .clip(shape)
        .background(AerixSurface.firePanel, shape)
        .border(BorderStroke(AerixSpacing.hairline, AerixSurface.borderSoft), shape)
    Column(
        modifier = (if (onClick != null) base.clickable(onClick = onClick) else base)
            .padding(horizontal = AerixSpacing.md, vertical = AerixSpacing.smPlus)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (iconRes != null) {
                Icon(painter = painterResource(iconRes), contentDescription = null, tint = AerixSurface.accent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(AerixSpacing.md))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = AerixSurface.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                if (!summary.isNullOrEmpty()) {
                    Text(text = summary, color = AerixSurface.textSecondary, fontSize = 11.sp, lineHeight = 14.sp)
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(AerixSpacing.sm))
                trailing()
            }
        }
        if (below != null) below()
    }
}

@Composable
fun SwitchSettingRow(title: String, summary: String?, checked: Boolean, onCheckedChange: (Boolean) -> Unit, @DrawableRes iconRes: Int? = null) {
    SettingRowFrame(
        title = title,
        summary = summary,
        iconRes = iconRes,
        onClick = { onCheckedChange(!checked) },
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AerixSurface.onAccent,
                    checkedTrackColor = AerixSurface.accent,
                    uncheckedThumbColor = AerixSurface.textSecondary,
                    uncheckedTrackColor = AerixSurface.firePanelTrack,
                    uncheckedBorderColor = AerixSurface.borderSoft
                )
            )
        }
    )
}

@Composable
fun SliderSettingRow(
    title: String,
    summary: String?,
    value: Int,
    range: IntRange,
    step: Int,
    suffix: String,
    onValueChange: (Int) -> Unit,
    @DrawableRes iconRes: Int? = null
) {
    var dragValue by remember(value) { mutableFloatStateOf(value.toFloat()) }
    val steps = if (step > 0) ((range.last - range.first) / step - 1).coerceAtLeast(0) else 0
    SettingRowFrame(
        title = title,
        summary = summary,
        iconRes = iconRes,
        onClick = null,
        trailing = {
            Text(text = "${dragValue.roundToInt()}$suffix", color = AerixSurface.fireAmber, fontSize = 12.sp, fontWeight = FontWeight.Black)
        },
        below = {
            Slider(
                value = dragValue.coerceIn(range.first.toFloat(), range.last.toFloat()),
                onValueChange = { dragValue = it },
                onValueChangeFinished = {
                    val snapped = if (step > 0) {
                        (range.first + ((dragValue - range.first) / step).roundToInt() * step).coerceIn(range.first, range.last)
                    } else dragValue.roundToInt()
                    dragValue = snapped.toFloat()
                    onValueChange(snapped)
                },
                valueRange = range.first.toFloat()..range.last.toFloat(),
                steps = if (steps in 1..200) steps else 0,
                colors = SliderDefaults.colors(
                    thumbColor = AerixSurface.fireAmber,
                    activeTrackColor = AerixSurface.accent,
                    inactiveTrackColor = AerixSurface.firePanelTrack
                )
            )
        }
    )
}

@Composable
fun TextSettingRow(title: String, summary: String?, value: String, onValueChange: (String) -> Unit, @DrawableRes iconRes: Int? = null) {
    var text by remember(value) { mutableStateOf(value) }
    SettingRowFrame(
        title = title,
        summary = summary,
        iconRes = iconRes,
        onClick = null,
        trailing = null,
        below = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                shape = FireShapes.armorSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AerixSpacing.xs),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AerixSurface.accent,
                    unfocusedBorderColor = AerixSurface.borderSoft,
                    focusedTextColor = AerixSurface.textPrimary,
                    unfocusedTextColor = AerixSurface.textPrimary,
                    cursorColor = AerixSurface.accent
                ),
                trailingIcon = {
                    if (text != value) {
                        Text(
                            text = "SAVE",
                            color = AerixSurface.fireAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .clip(FireShapes.hudChip)
                                .clickable { onValueChange(text) }
                                .padding(AerixSpacing.sm)
                        )
                    }
                }
            )
        }
    )
}

@Composable
fun ActionSettingRow(title: String, summary: String?, onClick: () -> Unit, @DrawableRes iconRes: Int? = null, value: String? = null) {
    SettingRowFrame(
        title = title,
        summary = summary,
        iconRes = iconRes,
        onClick = onClick,
        trailing = {
            Text(text = value ?: "▸", color = AerixSurface.accent, fontSize = if (value == null) 18.sp else 12.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
    )
}

/** Converts any Android drawable (instance icons, skin faces) into an ImageBitmap for Compose. */
fun Drawable.toAerixBitmap(sizePx: Int = 128): ImageBitmap {
    if (this is BitmapDrawable && bitmap != null) return bitmap.asImageBitmap()
    val w = if (intrinsicWidth > sizePx) intrinsicWidth else sizePx
    val h = if (intrinsicHeight > sizePx) intrinsicHeight else sizePx
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, w, h)
    draw(canvas)
    return bitmap.asImageBitmap()
}

/** Chamfered image well used for instance icons and player faces. */
@Composable
fun AerixImageWell(image: ImageBitmap?, @DrawableRes fallback: Int, size: Dp, modifier: Modifier = Modifier, radius: Dp = AerixRadii.control) {
    val shape = CutCornerShape(topStart = radius, topEnd = 2.dp, bottomEnd = radius, bottomStart = 2.dp)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(AerixSurface.firePanelRaised)
            .border(BorderStroke(AerixSpacing.hairline, AerixSurface.border), shape),
        contentAlignment = Alignment.Center
    ) {
        if (image != null) {
            Image(bitmap = image, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(size))
        } else {
            Icon(painter = painterResource(fallback), contentDescription = null, tint = AerixSurface.fireAmber, modifier = Modifier.size(size * 0.55f))
        }
    }
}
