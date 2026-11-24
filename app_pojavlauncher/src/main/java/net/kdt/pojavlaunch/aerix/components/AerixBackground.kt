/*
 * Fire Launcher — Multi-Color Light Pastel Aurora Background ("Prism Daylight").
 * Blends 5 soft luminous daylight hues (Sky Cyan, Pastel Lavender, Coral Rose,
 * Fresh Mint Aqua, and Sunlit Peach-Gold) over a pearl-white canvas.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package net.kdt.pojavlaunch.aerix.components

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kdt.pojavlaunch.aerix.bridge.AerixPreferences
import net.kdt.pojavlaunch.aerix.theme.AerixSurface
import net.kdt.pojavlaunch.aerix.theme.AerixThemeManager
import net.kdt.pojavlaunch.aerix.theme.ThemeMotion

private var wallpaperCacheKey: String? = null
private var wallpaperCacheBitmap: ImageBitmap? = null

private fun decodeWallpaper(context: Context, id: String, softness: Int = 1): ImageBitmap? {
    val bytes: ByteArray? = when (id) {
        AerixThemeManager.WALLPAPER_NONE -> null
        AerixThemeManager.WALLPAPER_CUSTOM -> {
            val file = AerixThemeManager.customWallpaperFile(context)
            if (file.exists()) file.readBytes() else null
        }
        else -> {
            val preset = AerixThemeManager.wallpapers.firstOrNull { it.id == id } ?: AerixThemeManager.wallpapers.first()
            runCatching { context.assets.open(preset.assetPath).use { it.readBytes() } }.getOrNull()
        }
    }
    if (bytes == null) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    val metrics = context.resources.displayMetrics
    val target = (maxOf(metrics.widthPixels, metrics.heightPixels) / softness).coerceAtLeast(1)
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= target) sample *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)?.asImageBitmap()
}

@Composable
fun AerixBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val context = LocalContext.current
    val id = AerixPreferences.selectedWallpaper
    val revision = AerixThemeManager.wallpaperRevision
    val blurDp = AerixPreferences.backgroundBlur
    val renderBlur = blurDp > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val softness = if (blurDp > 0 && !renderBlur) (1 + blurDp / 4).coerceIn(1, 6) else 1
    val cacheKey = "$id|$revision|$softness"
    var image by remember(cacheKey) { mutableStateOf(wallpaperCacheBitmap.takeIf { wallpaperCacheKey == cacheKey }) }
    LaunchedEffect(cacheKey) {
        if (animationById(id) != null) return@LaunchedEffect
        if (wallpaperCacheKey != cacheKey || wallpaperCacheBitmap == null) {
            val decoded = withContext(Dispatchers.IO) { runCatching { decodeWallpaper(context, id, softness) }.getOrNull() }
            wallpaperCacheKey = cacheKey
            wallpaperCacheBitmap = decoded
            image = decoded
        }
    }
    val motion = ThemeMotion.rememberEnabled()
    val animation = animationById(id)
    val accent = AerixThemeManager.currentAccent()

    Box(modifier.fillMaxSize().background(AerixSurface.canvas)) {
        // Always paint the 5-color luminous pastel aurora mesh base first!
        PrismLightAuroraMesh(modifier = Modifier.fillMaxSize())

        if (AerixThemeManager.isBundledClipId(id)) {
            val clip = bundledClipById(id.removePrefix(AerixThemeManager.WALLPAPER_CLIP_ASSET_PREFIX))
            if (clip != null) {
                RawVideoWallpaper(resId = clip.rawRes, motion = motion, modifier = Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(AerixThemeManager.screenScrim()))
                PrismDaylightSheenOverlay(accent = accent)
                content()
                return@Box
            }
        }
        if (AerixThemeManager.isClipId(id)) {
            val file = AerixThemeManager.clipFile(context, id)
            if (file != null && file.isFile) {
                MediaWallpaper(file = file, motion = motion, modifier = Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(AerixThemeManager.screenScrim()))
                PrismDaylightSheenOverlay(accent = accent)
                content()
                return@Box
            }
        }
        if (animation != null) {
            AnimatedScene(
                animation = animation,
                motion = motion,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.58f)
                    .let { if (renderBlur) it.blur(blurDp.dp * 0.5f) else it }
            )
            PrismLightAuroraMesh(modifier = Modifier.fillMaxSize().alpha(0.65f))
            PrismDaylightSheenOverlay(accent = accent)
            content()
            return@Box
        }
        val bitmap = image
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .let { if (renderBlur) it.blur(blurDp.dp) else it },
                contentScale = ContentScale.Crop
            )
            Box(Modifier.fillMaxSize().background(AerixThemeManager.screenScrim()))
        }
        PrismDaylightSheenOverlay(accent = accent)
        content()
    }
}

/**
 * Multi-color light pastel mesh canvas blending 5 airy daylight hues:
 * Sky Cyan (#7DD3FC), Pastel Rose-Pink (#F9A8D4), Luminous Lavender (#C4B5FD),
 * Fresh Mint Aqua (#6EE7B7), and Warm Sunlit Peach-Apricot (#FDBA74).
 */
@Composable
private fun PrismLightAuroraMesh(modifier: Modifier = Modifier) {
    Spacer(
        modifier = modifier.drawWithCache {
            val w = size.width
            val h = size.height
            val r = size.maxDimension * 0.66f
            val baseBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFEAF6FF),
                    Color(0xFFF3EEFF),
                    Color(0xFFFFF0F6),
                    Color(0xFFECFDF5)
                ),
                start = Offset.Zero,
                end = Offset(w, h)
            )
            val skyOrb = Brush.radialGradient(
                colors = listOf(Color(0xA67DD3FC), Color.Transparent),
                center = Offset(w * 0.12f, h * 0.16f),
                radius = r * 0.78f
            )
            val pinkOrb = Brush.radialGradient(
                colors = listOf(Color(0xA0F9A8D4), Color.Transparent),
                center = Offset(w * 0.88f, h * 0.18f),
                radius = r * 0.76f
            )
            val lavenderOrb = Brush.radialGradient(
                colors = listOf(Color(0x96C4B5FD), Color.Transparent),
                center = Offset(w * 0.50f, h * 0.46f),
                radius = r * 0.72f
            )
            val mintOrb = Brush.radialGradient(
                colors = listOf(Color(0x9E6EE7B7), Color.Transparent),
                center = Offset(w * 0.16f, h * 0.88f),
                radius = r * 0.74f
            )
            val peachOrb = Brush.radialGradient(
                colors = listOf(Color(0x9EFDBA74), Color(0x70FDE047), Color.Transparent),
                center = Offset(w * 0.86f, h * 0.86f),
                radius = r * 0.75f
            )
            onDrawBehind {
                drawRect(baseBrush)
                drawRect(skyOrb)
                drawRect(pinkOrb)
                drawRect(lavenderOrb)
                drawRect(mintOrb)
                drawRect(peachOrb)
            }
        }
    )
}

@Composable
private fun PrismDaylightSheenOverlay(accent: Color) {
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val w = size.width
                val h = size.height
                val sheen = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to Color.White.copy(alpha = 0.24f),
                        0.50f to Color.White.copy(alpha = 0.10f),
                        1.00f to accent.copy(alpha = 0.06f)
                    )
                )
                val step = 56.dp.toPx().coerceAtLeast(36f)
                val gridColor = Color.White.copy(alpha = 0.32f)
                onDrawBehind {
                    drawRect(sheen)
                    var x = step
                    while (x < w) {
                        drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                        x += step
                    }
                    var y = step
                    while (y < h) {
                        drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                        y += step
                    }
                }
            }
    )
}
