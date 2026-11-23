/*
 * Fire Launcher — Frosted Pearl-Light Prism Glass Modifier.
 * Renders an airy daylight specular sheen and an iridescent multi-color pastel rim
 * (Sky Cyan -> Electric Indigo -> Rose Pink) across every Bento-squircle card.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package net.kdt.pojavlaunch.aerix.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import net.kdt.pojavlaunch.aerix.theme.AerixMetrics
import net.kdt.pojavlaunch.aerix.theme.AerixSpacing
import net.kdt.pojavlaunch.aerix.theme.AerixSurface

fun Modifier.liquidGlass(
    shape: Shape,
    tint: Color = AerixSurface.glassTint,
    strength: Float = 1f,
    elevation: Dp = AerixMetrics.glassSurfaceElevation,
    clipContent: Boolean = true
): Modifier {
    val intensity = strength.coerceIn(0f, 1f)
    val shadowed = this.shadow(
        elevation = elevation * intensity,
        shape = shape,
        clip = false,
        ambientColor = AerixSurface.glassShadow,
        spotColor = AerixSurface.glassShadow
    )
    val glassModifier = if (clipContent) shadowed.clip(shape) else shadowed

    return glassModifier.drawWithCache {
        val width = size.width.coerceAtLeast(1f)
        val height = size.height.coerceAtLeast(1f)
        val sheen = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.55f * intensity),
                tint.copy(alpha = 0.06f * intensity),
                Color(0xFFFCE7F3).copy(alpha = 0.14f * intensity),
                Color(0xFFE0F2FE).copy(alpha = 0.18f * intensity)
            ),
            start = Offset.Zero,
            end = Offset(width, height)
        )
        val rim = Brush.linearGradient(
            colors = listOf(
                Color(0xFF38BDF8).copy(alpha = 0.52f * intensity),
                tint.copy(alpha = 0.45f * intensity),
                Color(0xFFF472B6).copy(alpha = 0.52f * intensity)
            ),
            start = Offset.Zero,
            end = Offset(width, height)
        )
        val outline = shape.createOutline(size, layoutDirection, this)
        val rimWidth = AerixSpacing.hairline.toPx() * intensity

        onDrawWithContent {
            drawOutline(outline = outline, color = Color.White.copy(alpha = 0.28f * intensity))
            drawOutline(outline = outline, brush = sheen)
            drawContent()
            if (rimWidth > 0f) {
                drawOutline(outline = outline, brush = rim, style = Stroke(width = rimWidth))
            }
        }
    }
}
