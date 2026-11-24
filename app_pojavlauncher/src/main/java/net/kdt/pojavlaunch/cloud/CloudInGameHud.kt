package net.kdt.pojavlaunch.cloud

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.*
import android.widget.*
import net.kdt.pojavlaunch.CustomControlsActivity
import net.kdt.pojavlaunch.game.GameActivity
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import java.util.Locale

object CloudInGameHud {
    private const val OVERLAY_TAG = "cloud_ingame_hud_root"

    fun applyLauncherCanvas(activity: Activity) {
        if (activity !is GameActivity) {
            activity.window?.setBackgroundDrawable(ColorDrawable(0xFFF4FAFF.toInt()))
        }
        applyHighRefreshRateIfEnabled(activity)
    }

    fun applyHighRefreshRateIfEnabled(activity: Activity) {
        try {
            val prefs = LauncherPreferences.DEFAULT_PREF
                ?: activity.getSharedPreferences("${activity.packageName}_preferences", Context.MODE_PRIVATE)
            if (!prefs.getBoolean("cloud_120hz", true)) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val display = activity.windowManager?.defaultDisplay ?: return
                val best = display.supportedModes.maxByOrNull { it.refreshRate } ?: return
                val lp = activity.window.attributes
                if (lp.preferredDisplayModeId != best.modeId) {
                    lp.preferredDisplayModeId = best.modeId
                    activity.window.attributes = lp
                }
            }
        } catch (_: Throwable) {}
    }

    fun attachIfNeeded(activity: Activity) {
        applyHighRefreshRateIfEnabled(activity)
        if (activity !is GameActivity) return
        val decor = activity.window?.decorView as? ViewGroup ?: return
        if (decor.findViewWithTag<View>(OVERLAY_TAG) != null) return

        // Restyle any legacy dark loading screen into Cloud Prism Light
        runCatching {
            val f = GameActivity::class.java.getDeclaredField("mLoadingScreen")
            f.isAccessible = true
            (f.get(activity) as? View)?.setBackgroundColor(0xFFF4FAFF.toInt())
        }

        val root = FrameLayout(activity).apply {
            tag = OVERLAY_TAG
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = false
            isFocusable = false
        }

        val prefs = LauncherPreferences.DEFAULT_PREF
            ?: activity.getSharedPreferences("${activity.packageName}_preferences", Context.MODE_PRIVATE)

        // 1. Top-Left Single Dark Glass Box: Real FPS Display + Optional Keystrokes/CPS
        val hudCol = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            val pad = dp(activity, 12f)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.START
            ).apply {
                setMargins(pad, pad, 0, 0)
            }
        }

        val fpsBox = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(activity, 8f), dp(activity, 6f), dp(activity, 11f), dp(activity, 6f))
            background = makeFpsDrawable(activity, 0xFF22C55E.toInt())
        }

        val leftAccentBar = View(activity).apply {
            layoutParams = LinearLayout.LayoutParams(dp(activity, 4f), dp(activity, 36f)).apply {
                rightMargin = dp(activity, 8f)
            }
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 2f).toFloat()
                setColor(0xFF22C55E.toInt())
            }
        }

        val fpsText = TextView(activity).apply {
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTextColor(0xFFF8FAFC.toInt())
            setLineSpacing(dp(activity, 1.5f).toFloat(), 1f)
            text = "Fps - 60\nRenderer - MobileGlues\nMs - 16.6 ms"
        }

        fpsBox.addView(leftAccentBar)
        fpsBox.addView(fpsText)
        hudCol.addView(fpsBox)

        // RGB Keystrokes + CPS row
        val clickTimestamps = ArrayDeque<Long>()
        val cpsBadge = TextView(activity).apply {
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            setTextColor(0xFFF8FAFC.toInt())
            setPadding(dp(activity, 8f), dp(activity, 4f), dp(activity, 8f), dp(activity, 4f))
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 6f).toFloat()
                setColor(0xCC0F172A.toInt())
                setStroke(dp(activity, 1f), 0xFF38BDF8.toInt())
            }
            text = "W A S D · 0 CPS"
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(activity, 5f)
            layoutParams = lp
        }
        hudCol.addView(cpsBadge)
        root.addView(hudCol)

        // Touch spy to measure real player CPS without consuming events
        root.setOnTouchListener { _, ev ->
            if (ev.actionMasked == MotionEvent.ACTION_DOWN || ev.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                val now = System.currentTimeMillis()
                clickTimestamps.addLast(now)
                while (clickTimestamps.isNotEmpty() && now - clickTimestamps.first() > 1000L) {
                    clickTimestamps.removeFirst()
                }
            }
            false
        }

        // 2. Optional Center Crosshair
        val crosshairView = TextView(activity).apply {
            text = "+"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTextColor(0xDDFFFFFF.toInt())
            setShadowLayer(4f, 0f, 0f, 0xFF000000.toInt())
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
            visibility = if (prefs.getBoolean("cloud_crosshair", false)) View.VISIBLE else View.GONE
        }
        root.addView(crosshairView)

        // 3. Centered Frosted Prism CLOUD LAUNCHER MENU Modal (Screen 7)
        val modalBackdrop = FrameLayout(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(0x550F172A)
            visibility = View.GONE
            isClickable = true
        }

        val modalCard = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            val p = dp(activity, 16f)
            setPadding(p, dp(activity, 14f), p, p)
            layoutParams = FrameLayout.LayoutParams(
                dp(activity, 300f),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(0xF5FFFFFF.toInt(), 0xF2EAF6FF.toInt(), 0xF2FCE7F3.toInt())
            ).apply {
                cornerRadius = dp(activity, 22f).toFloat()
                setStroke(dp(activity, 2f), 0xFFFFFFFF.toInt())
            }
            elevation = dp(activity, 14f).toFloat()
        }

        // Option 1 3D Prismatic Flame + Cube Logo at top of menu
        val logoBadge = ImageView(activity).apply {
            val sz = dp(activity, 52f)
            layoutParams = LinearLayout.LayoutParams(sz, sz).apply {
                bottomMargin = dp(activity, 6f)
            }
            val pad = dp(activity, 5f)
            setPadding(pad, pad, pad, pad)
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 14f).toFloat()
                setColor(0xFFFFFFFF.toInt())
                setStroke(dp(activity, 1.5f), 0xFFE0F2FE.toInt())
            }
            runCatching {
                activity.assets.open("cloud_ui/logo.png").use { stream ->
                    setImageBitmap(BitmapFactory.decodeStream(stream))
                }
            }
        }
        modalCard.addView(logoBadge)

        val menuTitle = TextView(activity).apply {
            text = "CLOUD LAUNCHER MENU"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF0F172A.toInt())
            letterSpacing = 0.05f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(activity, 10f) }
        }
        modalCard.addView(menuTitle)

        fun addMenuBtn(label: String, highlight: Boolean = false, onClick: (TextView) -> Unit): TextView {
            val btn = TextView(activity).apply {
                text = label
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(if (highlight) 0xFFFFFFFF.toInt() else 0xFF0F172A.toInt())
                setPadding(dp(activity, 14f), dp(activity, 9f), dp(activity, 14f), dp(activity, 9f))
                background = if (highlight) {
                    GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        intArrayOf(0xFF22D3EE.toInt(), 0xFFC084FC.toInt(), 0xFFF472B6.toInt())
                    ).apply {
                        cornerRadius = dp(activity, 99f).toFloat()
                    }
                } else {
                    GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        intArrayOf(0xE8E0F2FE.toInt(), 0xE8FCE7F3.toInt())
                    ).apply {
                        cornerRadius = dp(activity, 99f).toFloat()
                        setStroke(dp(activity, 1f), 0xFFFFFFFF.toInt())
                    }
                }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(activity, 7f) }
            }
            btn.setOnClickListener { onClick(btn) }
            modalCard.addView(btn)
            return btn
        }

        addMenuBtn("► Resume Game", highlight = true) {
            modalBackdrop.visibility = View.GONE
        }

        addMenuBtn("Real FPS Display: ${if (prefs.getBoolean("cloud_show_real_fps", true)) "ON" else "OFF"}") { b ->
            val next = !prefs.getBoolean("cloud_show_real_fps", true)
            prefs.edit().putBoolean("cloud_show_real_fps", next).putBoolean("show_fps", next).apply()
            b.text = "Real FPS Display: ${if (next) "ON" else "OFF"}"
            fpsBox.visibility = if (next) View.VISIBLE else View.GONE
        }

        addMenuBtn("Customize Touch Controls Rig") {
            modalBackdrop.visibility = View.GONE
            runCatching {
                activity.startActivity(Intent(activity, CustomControlsActivity::class.java))
            }
        }

        addMenuBtn("Toggle Virtual Mouse / Keyboard") {
            modalBackdrop.visibility = View.GONE
            runCatching { GameActivity.toggleMouse(activity) }
        }

        addMenuBtn("Live Console Log") {
            modalBackdrop.visibility = View.GONE
            runCatching {
                val lf = GameActivity::class.java.getDeclaredField("loggerView")
                lf.isAccessible = true
                val lv = lf.get(activity) as? View
                if (lv != null) {
                    lv.visibility = if (lv.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                }
            }
        }

        addMenuBtn("Exit to Cloud Launcher") {
            modalBackdrop.visibility = View.GONE
            activity.finish()
        }

        modalBackdrop.setOnClickListener { modalBackdrop.visibility = View.GONE }
        modalCard.setOnClickListener { /* keep modal open */ }
        modalBackdrop.addView(modalCard)

        // Floating top-right Cloud Menu button + intercept GameActivity's mDrawerPullButton
        val cloudMenuPill = TextView(activity).apply {
            text = "☁ CLOUD"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f)
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF0F172A.toInt())
            setPadding(dp(activity, 11f), dp(activity, 6f), dp(activity, 11f), dp(activity, 6f))
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(0xEEFFFFFF.toInt(), 0xEEE0F2FE.toInt(), 0xEEFCE7F3.toInt())
            ).apply {
                cornerRadius = dp(activity, 99f).toFloat()
                setStroke(dp(activity, 1.5f), 0xFFFFFFFF.toInt())
            }
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.END
            ).apply {
                val m = dp(activity, 12f)
                setMargins(0, m, m, 0)
            }
            setOnClickListener {
                modalBackdrop.visibility = if (modalBackdrop.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
        }

        root.addView(cloudMenuPill)
        root.addView(modalBackdrop)
        decor.addView(root)

        // Hook GameActivity's mDrawerPullButton when not in controls editor
        runCatching {
            val btnField = GameActivity::class.java.getDeclaredField("mDrawerPullButton")
            btnField.isAccessible = true
            val editField = GameActivity::class.java.getDeclaredField("isInEditor")
            editField.isAccessible = true
            (btnField.get(activity) as? View)?.setOnClickListener {
                val editing = runCatching { editField.getBoolean(activity) }.getOrDefault(false)
                if (editing) {
                    activity.onClickedMenu()
                } else {
                    modalBackdrop.visibility = if (modalBackdrop.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                }
            }
        }

        // Start Real Choreographer Nanosecond FPS + Frame-Time Probe
        var lastFrameNs = 0L
        var accumNs = 0L
        var frameCount = 0
        var lastUiUpdateMs = 0L

        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (!root.isAttachedToWindow || activity.isFinishing) return
                if (lastFrameNs != 0L) {
                    val delta = frameTimeNanos - lastFrameNs
                    if (delta in 100_000L..500_000_000L) {
                        accumNs += delta
                        frameCount++
                    }
                }
                lastFrameNs = frameTimeNanos

                val nowMs = frameTimeNanos / 1_000_000L
                if (nowMs - lastUiUpdateMs >= 350L && frameCount > 0) {
                    lastUiUpdateMs = nowMs

                    val showFps = prefs.getBoolean("cloud_show_real_fps", true)
                    fpsBox.visibility = if (showFps) View.VISIBLE else View.GONE
                    val showKs = prefs.getBoolean("cloud_keystrokes", true) || prefs.getBoolean("cloud_cps", true)
                    cpsBadge.visibility = if (showKs) View.VISIBLE else View.GONE
                    crosshairView.visibility = if (prefs.getBoolean("cloud_crosshair", false)) View.VISIBLE else View.GONE

                    if (showFps) {
                        val avgNs = accumNs.toDouble() / frameCount.toDouble()
                        val fps = (1_000_000_000.0 / avgNs).toInt().coerceIn(1, 360)
                        val ms = avgNs / 1_000_000.0

                        val statusColor = when {
                            fps >= 60 -> 0xFF22C55E.toInt() // Green (60+)
                            fps >= 30 -> 0xFFFACC15.toInt() // Yellow (30-59)
                            else -> 0xFFEF4444.toInt()      // Red (<30)
                        }
                        fpsBox.background = makeFpsDrawable(activity, statusColor)
                        (leftAccentBar.background as? GradientDrawable)?.setColor(statusColor)

                        val rendName = friendlyRendererName(prefs.getString("renderer", LauncherPreferences.PREF_RENDERER))
                        val msStr = String.format(Locale.US, "%.1f ms", ms)
                        val sb = SpannableStringBuilder()
                        val l1Start = sb.length
                        sb.append("Fps - ")
                        val v1Start = sb.length
                        sb.append(fps.toString())
                        sb.setSpan(ForegroundColorSpan(statusColor), v1Start, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                        sb.append("\nRenderer - ")
                        val v2Start = sb.length
                        sb.append(rendName)
                        sb.setSpan(ForegroundColorSpan(0xFF38BDF8.toInt()), v2Start, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                        sb.append("\nMs - ")
                        val v3Start = sb.length
                        sb.append(msStr)
                        sb.setSpan(ForegroundColorSpan(statusColor), v3Start, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                        fpsText.text = sb
                    }

                    if (showKs) {
                        val nowWall = System.currentTimeMillis()
                        while (clickTimestamps.isNotEmpty() && nowWall - clickTimestamps.first() > 1000L) {
                            clickTimestamps.removeFirst()
                        }
                        cpsBadge.text = "W  A  S  D  LMB · ${clickTimestamps.size} CPS"
                    }

                    accumNs = 0L
                    frameCount = 0
                }
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        Choreographer.getInstance().postFrameCallback(callback)
    }

    private fun friendlyRendererName(raw: String?): String {
        val r = (raw ?: "").lowercase()
        return when {
            r.contains("zink") || r.contains("vulkan") -> "Vulkan Zink"
            r.contains("ltw") -> "LTW GLES3"
            r.contains("turnip") || r.contains("freedreno") -> "Turnip Vulkan"
            r.contains("gl4es") -> "GL4ES"
            else -> "MobileGlues"
        }
    }

    private fun makeFpsDrawable(ctx: Context, strokeColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            cornerRadius = dp(ctx, 8f).toFloat()
            setColor(0xD90B1324.toInt())
            setStroke(dp(ctx, 1.5f), strokeColor)
        }
    }

    private fun dp(ctx: Context, v: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, ctx.resources.displayMetrics).toInt()
}
