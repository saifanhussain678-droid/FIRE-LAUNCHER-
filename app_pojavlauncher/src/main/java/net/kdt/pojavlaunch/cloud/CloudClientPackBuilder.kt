package net.kdt.pojavlaunch.cloud

import android.content.Context
import android.graphics.*
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.atan2
import kotlin.math.hypot

object CloudClientPackBuilder {

    const val PACK_FILE_NAME = "Cloud-Client-Pack.zip"
    const val BRIDGE_JAR_NAME = "cloud_title_bridge.jar"
    private const val PACK_REV = "cloud_celestial_v2"

    private fun packFormatFor(mcVersion: String): Int = when {
        mcVersion == "26.3" -> 97
        mcVersion == "26.2" -> 88
        mcVersion.startsWith("26.1") -> 84
        mcVersion == "1.21.11" -> 75
        mcVersion == "1.21.9" || mcVersion == "1.21.10" -> 69
        mcVersion == "1.21.7" || mcVersion == "1.21.8" -> 64
        mcVersion == "1.21.6" -> 63
        mcVersion == "1.21.5" -> 55
        mcVersion == "1.21.4" -> 46
        mcVersion == "1.21.2" || mcVersion == "1.21.3" -> 42
        else -> 34
    }

    private fun formatMinorFor(mcVersion: String): Int = if (mcVersion == "26.3") 1 else 0

    fun installPackAndBridge(context: Context, gameDir: File, mcVersion: String) {
        runCatching {
            gameDir.mkdirs()
            // 1. Copy cloud_title_bridge.jar into mods/
            val modsDir = File(gameDir, "mods").apply { mkdirs() }
            val bridgeTarget = File(modsDir, BRIDGE_JAR_NAME)
            context.assets.open("cloud_ui/$BRIDGE_JAR_NAME").use { input ->
                val tmp = File(modsDir, "$BRIDGE_JAR_NAME.tmp")
                FileOutputStream(tmp).use { out -> input.copyTo(out) }
                if (bridgeTarget.exists()) bridgeTarget.delete()
                tmp.renameTo(bridgeTarget)
            }

            // 2. Generate Cloud-Client-Pack.zip if missing or outdated
            val rpDir = File(gameDir, "resourcepacks").apply { mkdirs() }
            val stampFile = File(rpDir, ".cloud_pack_stamp")
            val zipFile = File(rpDir, PACK_FILE_NAME)
            val desiredStamp = "$PACK_REV|$mcVersion"
            val curStamp = if (stampFile.isFile) runCatching { stampFile.readText().trim() }.getOrDefault("") else ""

            if (!zipFile.isFile || zipFile.length() < 2048L || curStamp != desiredStamp) {
                buildPackZip(context, zipFile, mcVersion)
                runCatching { stampFile.writeText(desiredStamp) }
            }

            // 3. Ensure Cloud-Client-Pack.zip is active in options.txt
            enablePackInOptions(gameDir)
        }
    }

    private fun enablePackInOptions(gameDir: File) {
        val optFile = File(gameDir, "options.txt")
        val lines = if (optFile.isFile) optFile.readLines() else emptyList()
        val map = LinkedHashMap<String, String>()
        for (line in lines) {
            val idx = line.indexOf(':')
            if (idx > 0) map[line.substring(0, idx)] = line.substring(idx + 1)
        }

        val curPacks = map["resourcePacks"].orEmpty()
        if (!curPacks.contains(PACK_FILE_NAME)) {
            val arr = runCatching { JSONArray(curPacks) }.getOrElse { JSONArray().put("vanilla") }
            arr.put("file/$PACK_FILE_NAME")
            map["resourcePacks"] = arr.toString()
        }
        map["panoramaScrollSpeed"] = "0.0"
        if (!map.containsKey("pauseOnLostFocus")) map["pauseOnLostFocus"] = "false"

        optFile.writeText(map.entries.joinToString("\n") { "${it.key}:${it.value}" } + "\n")
    }

    private fun buildPackZip(context: Context, zipFile: File, mcVersion: String) {
        val fmt = packFormatFor(mcVersion)
        val minor = formatMinorFor(mcVersion)
        val modernRange = if (fmt >= 69) ",\n    \"min_format\": [$fmt, $minor],\n    \"max_format\": [$fmt, $minor]" else ""

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            val mcmeta = """
                {
                  "pack": {
                    "pack_format": $fmt$modernRange,
                    "description": "\u00a7b\u2726 Cloud Client $mcVersion \u00a77\u00b7 Celestial Sky UI & FPS Boost"
                  }
                }
            """.trimIndent()
            putText(zos, "pack.mcmeta", mcmeta)

            val logoIcon = runCatching {
                context.assets.open("cloud_ui/logo.png").use { BitmapFactory.decodeStream(it) }
            }.getOrNull()
            if (logoIcon != null) {
                putPng(zos, "pack.png", Bitmap.createScaledBitmap(logoIcon, 128, 128, true))
            }

            // 1. Title Screen Icy "CLOUD CLIENT" Emblem
            val titleLogo = buildCloudTitleLogo(logoIcon)
            putPng(zos, "assets/minecraft/textures/gui/title/minecraft.png", titleLogo)
            putPng(zos, "assets/minecraft/textures/gui/title/minceraft.png", titleLogo)

            val clearBmp = Bitmap.createBitmap(64, 16, Bitmap.Config.ARGB_8888)
            clearBmp.eraseColor(Color.TRANSPARENT)
            putPng(zos, "assets/minecraft/textures/gui/title/edition.png", clearBmp)
            putPng(zos, "assets/minecraft/textures/gui/title/background/panorama_overlay.png", clearBmp)

            // 2. Celestial Floating Sky-Islands Panorama Faces
            val faces = buildPanoramaFaces(context)
            faces.forEachIndexed { idx, face ->
                putPng(zos, "assets/minecraft/textures/gui/title/background/panorama_$idx.png", face)
                face.recycle()
            }

            // 3. Icy Cyan-Crystal Rune Button Sprites (1.20.2 - 26.3 nine-slice GUI sprites)
            val nineSliceMeta = """{"gui":{"scaling":{"type":"nine_slice","width":200,"height":20,"border":3}}}"""
            putPng(zos, "assets/minecraft/textures/gui/sprites/widget/button.png", buildCrystalButtonSprite(highlighted = false, disabled = false))
            putText(zos, "assets/minecraft/textures/gui/sprites/widget/button.png.mcmeta", nineSliceMeta)
            putPng(zos, "assets/minecraft/textures/gui/sprites/widget/button_highlighted.png", buildCrystalButtonSprite(highlighted = true, disabled = false))
            putText(zos, "assets/minecraft/textures/gui/sprites/widget/button_highlighted.png.mcmeta", nineSliceMeta)
            putPng(zos, "assets/minecraft/textures/gui/sprites/widget/button_disabled.png", buildCrystalButtonSprite(highlighted = false, disabled = true))
            putText(zos, "assets/minecraft/textures/gui/sprites/widget/button_disabled.png.mcmeta", nineSliceMeta)

            putText(
                zos,
                "assets/minecraft/texts/splashes.txt",
                "✦ Cloud Client!\n⚡ Auto FPS Boost Active!\n👤 Account Switcher Ready!\n☁ Powered by Cloud Launcher!"
            )
        }
    }

    private fun buildCloudTitleLogo(logoMark: Bitmap?): Bitmap {
        val scale = 4
        val w = 256 * scale
        val h = 64 * scale
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        if (logoMark != null) {
            c.drawBitmap(
                logoMark,
                null,
                RectF(14f * scale, 3f * scale, 52f * scale, 41f * scale),
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            )
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textSize = 24.5f * scale
            style = Paint.Style.STROKE
            strokeWidth = 2.8f * scale
            color = 0xFF082F49.toInt()
            setShadowLayer(3.5f * scale, 0f, 2f * scale, 0xCC020617.toInt())
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textSize = 24.5f * scale
            shader = LinearGradient(
                0f, 6f * scale, 0f, 38f * scale,
                intArrayOf(0xFFFFFFFF.toInt(), 0xFFBAE6FD.toInt(), 0xFF38BDF8.toInt()),
                floatArrayOf(0f, 0.52f, 1f),
                Shader.TileMode.CLAMP
            )
        }

        val tx = 56f * scale
        val ty = 31f * scale
        c.drawText("CLOUD CLIENT", tx, ty, strokePaint)
        c.drawText("CLOUD CLIENT", tx, ty, fillPaint)
        return bmp
    }

    private fun buildCrystalButtonSprite(highlighted: Boolean, disabled: Boolean): Bitmap {
        val w = 200
        val h = 20
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        val topCol = when {
            disabled -> 0xCC1E293B.toInt()
            highlighted -> 0xEE0284C7.toInt()
            else -> 0xE60C4A6E.toInt()
        }
        val botCol = when {
            disabled -> 0xCC0F172A.toInt()
            highlighted -> 0xEE0369A1.toInt()
            else -> 0xE6082F49.toInt()
        }
        val borderCol = when {
            disabled -> 0xFF475569.toInt()
            highlighted -> 0xFF7DD3FC.toInt()
            else -> 0xFF38BDF8.toInt()
        }

        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, 0f, h.toFloat(), topCol, botCol, Shader.TileMode.CLAMP)
        }
        c.drawRoundRect(RectF(0.5f, 0.5f, w - 0.5f, h - 0.5f), 3f, 3f, bg)

        val rim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.4f
            color = borderCol
        }
        c.drawRoundRect(RectF(1f, 1f, w - 1f, h - 1f), 2.5f, 2.5f, rim)
        return bmp
    }

    private fun blendRgb(c1: Int, c2: Int, t: Float): Int {
        val f = t.coerceIn(0f, 1f)
        val r = (Color.red(c1) * (1f - f) + Color.red(c2) * f).toInt()
        val g = (Color.green(c1) * (1f - f) + Color.green(c2) * f).toInt()
        val b = (Color.blue(c1) * (1f - f) + Color.blue(c2) * f).toInt()
        return Color.rgb(r, g, b)
    }

    private fun buildPanoramaFaces(context: Context): List<Bitmap> {
        val raw = runCatching {
            context.assets.open("cloud_ui/cloud_title_bg.jpg").use { BitmapFactory.decodeStream(it) }
        }.getOrNull() ?: return List(6) {
            Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888).apply { eraseColor(0xFF0EA5E9.toInt()) }
        }

        val srcW = raw.width.coerceAtLeast(2)
        val srcH = raw.height.coerceAtLeast(2)
        val srcPixels = IntArray(srcW * srcH)
        raw.getPixels(srcPixels, 0, srcW, 0, 0, srcW, srcH)
        raw.recycle()

        var topR = 0L; var topG = 0L; var topB = 0L
        var botR = 0L; var botG = 0L; var botB = 0L
        for (x in 0 until srcW) {
            val tc = srcPixels[x]
            topR += Color.red(tc); topG += Color.green(tc); topB += Color.blue(tc)
            val bc = srcPixels[(srcH - 1) * srcW + x]
            botR += Color.red(bc); botG += Color.green(bc); botB += Color.blue(bc)
        }
        val zenithCol = Color.rgb((topR / srcW).toInt(), (topG / srcW).toInt(), (topB / srcW).toInt())
        val nadirCol = Color.rgb((botR / srcW).toInt(), (botG / srcW).toInt(), (botB / srcW).toInt())

        val faceSize = 384
        val faces = ArrayList<Bitmap>(6)

        for (faceIdx in 0..5) {
            val outPixels = IntArray(faceSize * faceSize)
            for (py in 0 until faceSize) {
                val b = (2.0 * (py + 0.5) / faceSize) - 1.0
                for (px in 0 until faceSize) {
                    val a = (2.0 * (px + 0.5) / faceSize) - 1.0
                    val dx: Double; val dy: Double; val dz: Double
                    when (faceIdx) {
                        0 -> { dx = a; dy = -b; dz = 1.0 }
                        1 -> { dx = -1.0; dy = -b; dz = a }
                        2 -> { dx = -a; dy = -b; dz = -1.0 }
                        3 -> { dx = 1.0; dy = -b; dz = -a }
                        4 -> { dx = a; dy = 1.0; dz = b }
                        else -> { dx = a; dy = -1.0; dz = -b }
                    }
                    val horiz = hypot(dx, dz)
                    val pitch = atan2(dy, horiz)
                    val yaw = atan2(-dx, dz)
                    val normYaw = (yaw + Math.PI) / (2.0 * Math.PI)
                    val saw = (normYaw * 2.0) % 2.0
                    val mirroredU = if (saw <= 1.0) saw else (2.0 - saw)

                    val vRaw = 0.5 - (pitch / 1.08)
                    val col = when {
                        vRaw <= 0.0 -> zenithCol
                        vRaw >= 1.0 -> nadirCol
                        else -> {
                            val sx = (mirroredU * (srcW - 1)).toInt().coerceIn(0, srcW - 1)
                            val sy = (vRaw * (srcH - 1)).toInt().coerceIn(0, srcH - 1)
                            val sample = srcPixels[sy * srcW + sx]
                            if (vRaw < 0.10) blendRgb(zenithCol, sample, (vRaw / 0.10).toFloat())
                            else if (vRaw > 0.90) blendRgb(sample, nadirCol, ((vRaw - 0.90) / 0.10).toFloat())
                            else sample
                        }
                    }
                    outPixels[py * faceSize + px] = col or -0x1000000
                }
            }
            val bmp = Bitmap.createBitmap(faceSize, faceSize, Bitmap.Config.ARGB_8888)
            bmp.setPixels(outPixels, 0, faceSize, 0, 0, faceSize, faceSize)
            faces.add(bmp)
        }
        return faces
    }

    private fun putText(zos: ZipOutputStream, name: String, content: String) {
        zos.putNextEntry(ZipEntry(name))
        zos.write(content.toByteArray(Charsets.UTF_8))
        zos.closeEntry()
    }

    private fun putPng(zos: ZipOutputStream, name: String, bmp: Bitmap) {
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
        zos.putNextEntry(ZipEntry(name))
        zos.write(baos.toByteArray())
        zos.closeEntry()
    }
}
