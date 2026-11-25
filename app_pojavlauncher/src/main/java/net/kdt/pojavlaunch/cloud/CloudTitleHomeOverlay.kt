package net.kdt.pojavlaunch.cloud

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.instances.Instances
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties
import kotlin.math.sin

object CloudTitleHomeOverlay {

    fun createAndAttach(
        activity: Activity,
        hudRoot: FrameLayout,
        onOpenCloudSettings: () -> Unit
    ): View {
        val prefs = LauncherPreferences.DEFAULT_PREF
            ?: activity.getSharedPreferences("${activity.packageName}_preferences", Context.MODE_PRIVATE)

        val selInst = runCatching { Instances.loadSelectedInstance() }.getOrNull()
        val gameDir: File = selInst?.gameDirectory ?: File(Tools.DIR_GAME_HOME)
        val mcVer = runCatching {
            val nm = selInst?.name.orEmpty()
            val vid = selInst?.versionId.orEmpty()
            nm.removePrefix("Cloud Client ").trim()
                .takeIf { it.matches(Regex("(?:1\\.\\d+(?:\\.\\d+)?|26\\.\\d+(?:\\.\\d+)?)")) }
                ?: vid.substringAfterLast("-", "1.21.11")
        }.getOrDefault("1.21.11")

        // Ensure bridge jar exists for Fabric/Cloud Client instances even if launched from external shortcut
        runCatching {
            val nm = selInst?.name.orEmpty()
            val vid = selInst?.versionId.orEmpty()
            if (nm.startsWith("Cloud Client", true) || vid.contains("fabric", true)) {
                val modsDir = File(gameDir, "mods").apply { mkdirs() }
                val bridgeJar = File(modsDir, CloudClientPackBuilder.BRIDGE_JAR_NAME)
                if (!bridgeJar.isFile) {
                    activity.assets.open("cloud_ui/${CloudClientPackBuilder.BRIDGE_JAR_NAME}").use { inp ->
                        bridgeJar.writeBytes(inp.readBytes())
                    }
                }
            }
        }

        val container = FrameLayout(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
            isClickable = true
            isFocusable = true
        }

        // 1. Full-Bleed Celestial Sky-Islands + Cyan Waterfalls Background
        val bgImg = ImageView(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            runCatching {
                activity.assets.open("cloud_ui/cloud_title_bg.jpg").use {
                    setImageBitmap(BitmapFactory.decodeStream(it))
                }
            }
        }
        container.addView(bgImg)

        var suppressedUntilLeaveTitle = false
        var lastCmdSentAt = 0L

        fun sendMenuCommand(cmd: String, hideOverlayImmediately: Boolean = true) {
            lastCmdSentAt = System.currentTimeMillis()
            if (hideOverlayImmediately) {
                container.visibility = View.GONE
            }
            Thread({
                runCatching {
                    val cfg = File(gameDir, "config").apply { mkdirs() }
                    val tmp = File(cfg, "cloud_title_cmd.txt.tmp")
                    val target = File(cfg, "cloud_title_cmd.txt")
                    FileOutputStream(tmp).use { it.write(cmd.toByteArray(Charsets.UTF_8)) }
                    if (target.exists()) target.delete()
                    tmp.renameTo(target)
                }
            }, "CloudTitleCmd").start()
        }

        // 2. Top-Center Icy Crystal "CLOUD CLIENT" Header + Subtitle
        val topHeader = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.CENTER_HORIZONTAL
            ).apply {
                topMargin = dp(activity, 14f)
            }
        }

        val logoRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val flameLogo = ImageView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(dp(activity, 38f), dp(activity, 38f)).apply {
                rightMargin = dp(activity, 8f)
            }
            runCatching {
                activity.assets.open("cloud_ui/logo.png").use {
                    setImageBitmap(BitmapFactory.decodeStream(it))
                }
            }
        }
        val icyTitle = object : TextView(activity) {
            override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
                super.onSizeChanged(w, h, oldw, oldh)
                if (h > 0) {
                    paint.shader = LinearGradient(
                        0f, 0f, 0f, h.toFloat(),
                        intArrayOf(0xFFFFFFFF.toInt(), 0xFFBAE6FD.toInt(), 0xFF38BDF8.toInt()),
                        floatArrayOf(0f, 0.55f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
            }
        }.apply {
            text = "CLOUD CLIENT"
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
            letterSpacing = 0.05f
            setShadowLayer(dp(activity, 5f).toFloat(), 0f, dp(activity, 2.5f).toFloat(), 0xEE082F49.toInt())
        }
        logoRow.addView(flameLogo)
        logoRow.addView(icyTitle)

        val subPill = TextView(activity).apply {
            text = "MC $mcVer | Fabric · Cloud Client"
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f)
            setTextColor(0xFFE0F2FE.toInt())
            setPadding(dp(activity, 12f), dp(activity, 3f), dp(activity, 12f), dp(activity, 3f))
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 6f).toFloat()
                setColor(0xA6082F49.toInt())
                setStroke(dp(activity, 1f), 0x8838BDF8.toInt())
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(activity, 3f)
            }
        }
        topHeader.addView(logoRow)
        topHeader.addView(subPill)
        container.addView(topHeader)

        // 3. Top-Right 3 Frosted Cyan Square Buttons: MODS | ACCOUNTS (IAS) | LAUNCHER SETTINGS
        val topRightRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.END
            ).apply {
                topMargin = dp(activity, 12f)
                rightMargin = dp(activity, 14f)
            }
        }

        fun makeTopSquareTile(iconTxt: String, labelTxt: String, onClick: () -> Unit): View {
            return LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                val sz = dp(activity, 52f)
                layoutParams = LinearLayout.LayoutParams(sz, sz).apply {
                    leftMargin = dp(activity, 6f)
                }
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(0xCC0EA5E9.toInt(), 0xD90C4A6E.toInt())
                ).apply {
                    cornerRadius = dp(activity, 9f).toFloat()
                    setStroke(dp(activity, 1.5f), 0xFFBAE6FD.toInt())
                }
                addView(TextView(activity).apply {
                    text = iconTxt
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                    setTextColor(0xFFFFFFFF.toInt())
                    gravity = Gravity.CENTER
                })
                addView(TextView(activity).apply {
                    text = labelTxt
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 7.5f)
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(0xFFE0F2FE.toInt())
                    gravity = Gravity.CENTER
                    maxLines = 2
                })
                setOnClickListener { onClick() }
            }
        }

        topRightRow.addView(makeTopSquareTile("⚔", "MODS") { sendMenuCommand("MODS") })
        topRightRow.addView(makeTopSquareTile("👤", "ACCOUNTS") { sendMenuCommand("ACCOUNTS") })
        topRightRow.addView(makeTopSquareTile("⚙", "SETTINGS") { onOpenCloudSettings() })
        container.addView(topRightRow)

        // 4. Center Cyan-Crystal Rune Buttons: SINGLEPLAYER / MULTIPLAYER / OPTIONS
        val centerCol = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(
                dp(activity, 250f),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            ).apply {
                topMargin = dp(activity, 10f)
            }
        }

        fun makeCrystalRuneButton(icon: String, title: String, widthDp: Float = 250f, onClick: () -> Unit): View {
            val row = FrameLayout(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    if (widthDp > 0) dp(activity, widthDp) else ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(activity, 36f)
                ).apply {
                    bottomMargin = dp(activity, 8f)
                }
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(0xEE0369A1.toInt(), 0xF2082F49.toInt(), 0xEE0C4A6E.toInt())
                ).apply {
                    cornerRadius = dp(activity, 6f).toFloat()
                    setStroke(dp(activity, 1.8f), 0xFF7DD3FC.toInt())
                }
                setPadding(dp(activity, 12f), 0, dp(activity, 12f), 0)
                setOnClickListener { onClick() }
            }
            if (icon.isNotEmpty()) {
                row.addView(TextView(activity).apply {
                    text = icon
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    setTextColor(0xFFBAE6FD.toInt())
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.START or Gravity.CENTER_VERTICAL
                    )
                })
            }
            row.addView(TextView(activity).apply {
                text = title
                typeface = Typeface.MONOSPACE
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
                setTextColor(0xFFFFFFFF.toInt())
                letterSpacing = 0.06f
                setShadowLayer(dp(activity, 3f).toFloat(), 0f, dp(activity, 1f).toFloat(), 0xDD020617.toInt())
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            })
            return row
        }

        centerCol.addView(makeCrystalRuneButton("🗡", "SINGLEPLAYER") { sendMenuCommand("SINGLEPLAYER") })
        centerCol.addView(makeCrystalRuneButton("🖥", "MULTIPLAYER") { sendMenuCommand("MULTIPLAYER") })
        centerCol.addView(makeCrystalRuneButton("⚙", "OPTIONS") { sendMenuCommand("OPTIONS") })
        container.addView(centerCol)

        // 5. Right-Side Standing Equipped 3D Minecraft Skin on the Glowing Rune Stone Pedestal!
        val skinStandingView = EquippedSkinStandingView(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                dp(activity, 150f),
                dp(activity, 215f),
                Gravity.BOTTOM or Gravity.END
            ).apply {
                rightMargin = dp(activity, 38f)
                bottomMargin = dp(activity, 44f)
            }
        }
        container.addView(skinStandingView)

        // 6. Bottom Welcome Banner + QUIT + Language/Accessibility + Branding
        val bottomCenterCol = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            ).apply {
                bottomMargin = dp(activity, 8f)
            }
        }

        val welcomeBanner = TextView(activity).apply {
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f)
            setTextColor(0xFFF8FAFC.toInt())
            setPadding(dp(activity, 12f), dp(activity, 4f), dp(activity, 12f), dp(activity, 4f))
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 5f).toFloat()
                setColor(0xB80F172A.toInt())
            }
        }

        val onlineLine = TextView(activity).apply {
            text = "Cloud Client ($mcVer) — Online & FPS Boost Active"
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            setTextColor(0xFF4ADE80.toInt())
            setShadowLayer(dp(activity, 3f).toFloat(), 0f, dp(activity, 1f).toFloat(), 0xEE020617.toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(activity, 4f)
                bottomMargin = dp(activity, 5f)
            }
        }

        val quitBtn = makeCrystalRuneButton("", "QUIT", widthDp = 128f) { sendMenuCommand("QUIT") }

        bottomCenterCol.addView(welcomeBanner)
        bottomCenterCol.addView(onlineLine)
        bottomCenterCol.addView(quitBtn)
        container.addView(bottomCenterCol)

        // Bottom-Left Version & Copyright
        val bottomLeft = TextView(activity).apply {
            text = "CLOUD CLIENT · MC $mcVer\nPowered by Cloud Launcher"
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f)
            setTextColor(0xFFE2E8F0.toInt())
            setShadowLayer(dp(activity, 3f).toFloat(), 0f, dp(activity, 1f).toFloat(), 0xDD020617.toInt())
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.START
            ).apply {
                leftMargin = dp(activity, 12f)
                bottomMargin = dp(activity, 10f)
            }
        }
        container.addView(bottomLeft)

        // Bottom-Right Language & Accessibility
        val bottomRight = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.END
            ).apply {
                rightMargin = dp(activity, 12f)
                bottomMargin = dp(activity, 8f)
            }
        }
        fun makeSmallBottomBtn(txt: String, onClick: () -> Unit): View {
            return TextView(activity).apply {
                text = txt
                typeface = Typeface.MONOSPACE
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                setTextColor(0xFFF8FAFC.toInt())
                setPadding(dp(activity, 10f), dp(activity, 6f), dp(activity, 10f), dp(activity, 6f))
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(0xDD0369A1.toInt(), 0xEE082F49.toInt())
                ).apply {
                    cornerRadius = dp(activity, 5f).toFloat()
                    setStroke(dp(activity, 1.3f), 0xFF7DD3FC.toInt())
                }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    leftMargin = dp(activity, 6f)
                }
                setOnClickListener { onClick() }
            }
        }
        bottomRight.addView(makeSmallBottomBtn("🌐 Language") { sendMenuCommand("LANGUAGE") })
        bottomRight.addView(makeSmallBottomBtn("Accessibility") { sendMenuCommand("ACCESSIBILITY") })
        container.addView(bottomRight)

        fun updateWelcomeUsername(uname: String) {
            val clean = uname.ifBlank { "Cloud_OP" }
            val sb = SpannableStringBuilder("Welcome, ")
            val s = sb.length
            sb.append(clean)
            sb.setSpan(ForegroundColorSpan(0xFFFBBF24.toInt()), s, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.append("! | Account Switcher Active")
            welcomeBanner.text = sb
        }
        updateWelcomeUsername(prefs.getString("cloud_active_username", "Cloud_OP") ?: "Cloud_OP")

        // Insert at index 0 of hudRoot so FPS box & Floating Cloud Menu stay visible on top!
        hudRoot.addView(container, 0)

        // Poll config/cloud_title_state.properties every 180ms to show/hide Celestial Home Screen seamlessly!
        val stateFile = File(gameDir, "config/cloud_title_state.properties")
        val poller = object : Runnable {
            override fun run() {
                if (activity.isFinishing || !hudRoot.isAttachedToWindow) return
                try {
                    val now = System.currentTimeMillis()
                    var showTitle = false
                    if (stateFile.isFile && now - lastCmdSentAt > 450L) {
                        val p = Properties()
                        FileInputStream(stateFile).use { p.load(it) }
                        val updated = p.getProperty("updated", "0").toLongOrNull() ?: 0L
                        val onTitle = p.getProperty("onTitleScreen", "false") == "true"
                        val uname = p.getProperty("username", "")
                        if (uname.isNotBlank()) {
                            updateWelcomeUsername(uname)
                        }
                        if (now - updated < 2800L && onTitle) {
                            if (!suppressedUntilLeaveTitle) showTitle = true
                        } else if (!onTitle) {
                            suppressedUntilLeaveTitle = false
                        }
                    }
                    val targetVis = if (showTitle) View.VISIBLE else View.GONE
                    if (container.visibility != targetVis) {
                        container.visibility = targetVis
                        if (showTitle) {
                            skinStandingView.reloadSkinIfNeeded()
                        }
                    }
                } catch (_: Throwable) {}
                hudRoot.postDelayed(this, 180L)
            }
        }
        hudRoot.postDelayed(poller, 350L)

        return container
    }

    /**
     * Renders the player's ACTUAL equipped 64x64 / 64x32 Minecraft Skin in a crisp 3D isometric
     * standing pose (complete with 3D head + outer hat/helmet layer, torso + jacket, arms + sleeves,
     * legs + pants, glowing starry cyan cape, and subtle idle breathing) on the right stone pedestal!
     */
    private class EquippedSkinStandingView(context: Context) : View(context) {
        private var skinBmp: Bitmap? = null
        private var lastSkinMtime: Long = -1L
        private val pixelPaint = Paint().apply {
            isAntiAlias = false
            isFilterBitmap = false
        }
        private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val capePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val shadeSidePaint = Paint().apply {
            color = 0x38000000
        }
        private val shadeTopPaint = Paint().apply {
            color = 0x22FFFFFF
        }

        init {
            reloadSkinIfNeeded()
        }

        fun reloadSkinIfNeeded() {
            runCatching {
                val f1 = File(context.filesDir, "cloud_equipped_skin.png")
                val f2 = File(context.filesDir, "cloud_custom_skin.png")
                val chosen = when {
                    f1.isFile && f1.length() > 64 -> f1
                    f2.isFile && f2.length() > 64 -> f2
                    else -> null
                }
                if (chosen != null) {
                    if (chosen.lastModified() != lastSkinMtime || skinBmp == null) {
                        val decoded = BitmapFactory.decodeFile(chosen.absolutePath)
                        if (decoded != null && decoded.width >= 64 && decoded.height >= 32) {
                            skinBmp = decoded
                            lastSkinMtime = chosen.lastModified()
                        }
                    }
                } else if (skinBmp == null) {
                    context.assets.open("cloud_ui/skins/skin_cloud_op.png").use {
                        skinBmp = BitmapFactory.decodeStream(it)
                    }
                }
            }
            invalidate()
        }

        private fun drawSkinPart(
            canvas: Canvas,
            src: Bitmap,
            sx: Int, sy: Int, sw: Int, sh: Int,
            dst: RectF
        ) {
            if (sy + sh > src.height || sx + sw > src.width) return
            canvas.drawBitmap(src, Rect(sx, sy, sx + sw, sy + sh), dst, pixelPaint)
        }

        private fun draw3DBoxPart(
            canvas: Canvas,
            src: Bitmap,
            // Front UV, Right-Side UV (visible on viewer's right since character faces slightly left!), Top UV
            fx: Int, fy: Int, fw: Int, fh: Int,
            rx: Int, ry: Int, rw: Int, rh: Int,
            tx: Int, ty: Int, tw: Int, th: Int,
            x: Float, y: Float, w: Float, h: Float, sideW: Float
        ) {
            // Front face
            val frontRect = RectF(x, y, x + w, y + h)
            drawSkinPart(canvas, src, fx, fy, fw, fh, frontRect)

            // 3D side face (skewed upward slightly for 3/4 isometric depth!)
            canvas.save()
            val sideSkew = -0.28f
            canvas.translate(x + w, y)
            val m = Matrix()
            m.setSkew(0f, sideSkew)
            canvas.concat(m)
            val sideRect = RectF(0f, 0f, sideW, h)
            drawSkinPart(canvas, src, rx, ry, rw, rh, sideRect)
            canvas.drawRect(sideRect, shadeSidePaint)
            canvas.restore()

            // 3D top face
            val topH = sideW * 0.28f
            if (topH > 1.5f && ty + th <= src.height) {
                canvas.save()
                canvas.translate(x, y - topH)
                val tm = Matrix()
                tm.setSkew(-1.0f, 0f)
                // Draw subtle highlight strip along top edge
                val topRect = RectF(sideW * 0.3f, 0f, w + sideW * 0.3f, topH)
                drawSkinPart(canvas, src, tx, ty, tw, th, topRect)
                canvas.drawRect(topRect, shadeTopPaint)
                canvas.restore()
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val bmp = skinBmp ?: return
            val has64 = bmp.height >= 64

            val w = width.toFloat()
            val h = height.toFloat()
            val cx = w * 0.46f
            val baseY = h * 0.92f

            // Subtle idle breathing bob
            val t = (System.currentTimeMillis() % 2800L) / 2800.0 * Math.PI * 2.0
            val bob = (sin(t) * dp(context, 2.2f)).toFloat()

            // 1. Glowing cyan rune aura under feet on stone pedestal
            auraPaint.shader = RadialGradient(
                cx + dp(context, 4f), baseY,
                dp(context, 42f).toFloat(),
                intArrayOf(0xAA38BDF8.toInt(), 0x440EA5E9.toInt(), 0x000EA5E9),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.save()
            canvas.scale(1f, 0.36f, cx, baseY)
            canvas.drawCircle(cx + dp(context, 4f), baseY, dp(context, 42f).toFloat(), auraPaint)
            canvas.restore()

            val u = (h * 0.78f) / 32f // 1 Minecraft pixel unit (character is 32 px tall)
            val sideDepth = u * 2.2f

            // 2. Flowing Celestial Cyan Cape behind torso & legs
            val capePath = Path().apply {
                val topY = baseY - 24f * u + bob
                val botY = baseY - 3f * u
                moveTo(cx - 4.5f * u, topY)
                lineTo(cx + 6.5f * u, topY)
                lineTo(cx + 10.5f * u, botY)
                lineTo(cx - 7.5f * u, botY + u)
                close()
            }
            capePaint.shader = LinearGradient(
                cx, baseY - 24f * u, cx, baseY,
                intArrayOf(0xEE0284C7.toInt(), 0xDD082F49.toInt(), 0xEE38BDF8.toInt()),
                floatArrayOf(0f, 0.65f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawPath(capePath, capePaint)

            // 3. Right & Left Legs (12 units tall, 4 units wide each)
            val legH = 12f * u
            val legW = 4f * u
            val legTop = baseY - legH

            // Viewer-left leg (Right Leg in MC skin: UV 4,20)
            draw3DBoxPart(
                canvas, bmp,
                4, 20, 4, 12,
                0, 20, 4, 12,
                4, 16, 4, 4,
                cx - legW, legTop, legW, legH, sideDepth * 0.45f
            )
            if (has64) {
                drawSkinPart(canvas, bmp, 4, 36, 4, 12, RectF(cx - legW - 0.25f * u, legTop - 0.2f * u, cx + 0.2f * u, baseY))
            }

            // Viewer-right leg (Left Leg in 64x64 skin: UV 20,52; fallback 4,20)
            val llX = if (has64) 20 else 4
            val llY = if (has64) 52 else 20
            val llsX = if (has64) 24 else 8
            draw3DBoxPart(
                canvas, bmp,
                llX, llY, 4, 12,
                llsX, llY, 4, 12,
                llX, llY - 4, 4, 4,
                cx, legTop, legW, legH, sideDepth * 0.7f
            )
            if (has64) {
                drawSkinPart(canvas, bmp, 4, 52, 4, 12, RectF(cx - 0.15f * u, legTop - 0.2f * u, cx + legW + 0.3f * u, baseY))
            }

            // 4. Viewer-left Arm (Right Arm: UV 44,20)
            val bodyTop = legTop - 12f * u + bob
            val armW = 4f * u
            val armH = 12f * u
            draw3DBoxPart(
                canvas, bmp,
                44, 20, 4, 12,
                40, 20, 4, 12,
                44, 16, 4, 4,
                cx - 4f * u - armW, bodyTop, armW, armH, sideDepth * 0.35f
            )
            if (has64) {
                drawSkinPart(canvas, bmp, 44, 36, 4, 12, RectF(cx - 4f * u - armW - 0.3f * u, bodyTop - 0.2f * u, cx - 4f * u + 0.2f * u, bodyTop + armH + 0.2f * u))
            }

            // 5. Torso + Jacket Overlay (UV 20,20 w=8,h=12)
            val torsoW = 8f * u
            val torsoH = 12f * u
            draw3DBoxPart(
                canvas, bmp,
                20, 20, 8, 12,
                28, 20, 4, 12,
                20, 16, 8, 4,
                cx - 4f * u, bodyTop, torsoW, torsoH, sideDepth * 0.7f
            )
            if (has64) {
                drawSkinPart(canvas, bmp, 20, 36, 8, 12, RectF(cx - 4.3f * u, bodyTop - 0.25f * u, cx + 4.3f * u, bodyTop + torsoH + 0.25f * u))
            }

            // 6. Viewer-right Arm (Left Arm in 64x64: UV 36,52; fallback 44,20)
            val laX = if (has64) 36 else 44
            val laY = if (has64) 52 else 20
            val lasX = if (has64) 40 else 48
            draw3DBoxPart(
                canvas, bmp,
                laX, laY, 4, 12,
                lasX, laY, 4, 12,
                laX, laY - 4, 4, 4,
                cx + 4f * u, bodyTop, armW, armH, sideDepth * 0.85f
            )
            if (has64) {
                drawSkinPart(canvas, bmp, 52, 52, 4, 12, RectF(cx + 3.8f * u, bodyTop - 0.25f * u, cx + 4f * u + armW + 0.35f * u, bodyTop + armH + 0.25f * u))
            }

            // 7. 3D Head + Outer Hat/Helmet Layer (UV 8,8 w=8,h=8; side UV 16,8; top UV 8,0; Hat UV 40,8)
            val headSize = 8f * u
            val headTop = bodyTop - headSize + 0.1f * u
            val headLeft = cx - 4.1f * u
            draw3DBoxPart(
                canvas, bmp,
                8, 8, 8, 8,
                16, 8, 8, 8,
                8, 0, 8, 8,
                headLeft, headTop, headSize, headSize, sideDepth
            )
            // Outer Hat / Helmet 3D Layer (UV 40,8 front + 48,8 side)
            val hatPad = 0.48f * u
            draw3DBoxPart(
                canvas, bmp,
                40, 8, 8, 8,
                48, 8, 8, 8,
                40, 0, 8, 8,
                headLeft - hatPad, headTop - hatPad, headSize + hatPad * 2f, headSize + hatPad * 2f, sideDepth * 1.05f
            )

            if (isShown) {
                postInvalidateDelayed(70L)
            }
        }
    }

    private fun dp(ctx: Context, v: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, ctx.resources.displayMetrics).toInt()
}
