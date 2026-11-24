package net.kdt.pojavlaunch.cloud

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.fragment.app.FragmentActivity
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.aerix.bridge.*
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.fragments.MicrosoftLoginFragment
import net.kdt.pojavlaunch.instances.Instances
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max
import kotlin.math.min

class CloudBridge(
    private val activity: FragmentActivity,
    private val webViewProvider: () -> WebView?,
    private val onPickJar: () -> Unit,
    private val onPickSkin: () -> Unit
) {
    private val appCtx: Context = activity.applicationContext

    @Volatile private var customDlActive: Boolean = false
    @Volatile private var customDlTitle: String = ""
    @Volatile private var customDlPct: Int = 0

    private fun prefs() = LauncherPreferences.DEFAULT_PREF
        ?: appCtx.getSharedPreferences("${appCtx.packageName}_preferences", Context.MODE_PRIVATE)

    private fun evalJs(script: String) {
        Tools.runOnUiThread {
            try {
                webViewProvider()?.evaluateJavascript(script, null)
            } catch (_: Throwable) {}
        }
    }

    @JavascriptInterface
    fun getStateJson(): String {
        return try {
            PojavInstanceBridge.reload()
            PojavAccountBridge.reload()
            val p = prefs()
            val obj = JSONObject()

            val curAcc = PojavAccountBridge.current
            obj.put("accountName", curAcc?.username ?: p.getString("cloud_last_user", "Cloud_OP") ?: "Cloud_OP")
            obj.put("accountType", if (curAcc != null) curAcc.authType.name else "CLOUD ID")

            var skinDataUrl = ""
            if (curAcc != null) {
                val bytes = runCatching { PojavAccountBridge.resolveCustomSkinBytes(curAcc) }.getOrNull()
                if (bytes != null && bytes.isNotEmpty()) {
                    skinDataUrl = "data:image/png;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
                }
            }
            if (skinDataUrl.isEmpty()) {
                val localSkin = File(appCtx.filesDir, "cloud_custom_skin.png")
                if (localSkin.isFile && localSkin.length() in 64..512_000) {
                    skinDataUrl = "data:image/png;base64," + Base64.encodeToString(localSkin.readBytes(), Base64.NO_WRAP)
                }
            }
            obj.put("skinDataUrl", skinDataUrl)

            val accArr = JSONArray()
            val allAccs = PojavAccountBridge.accounts
            for (a in allAccs) {
                val ao = JSONObject()
                ao.put("username", a.username)
                ao.put("type", a.authType.name)
                ao.put("selected", curAcc != null && curAcc.key == a.key)
                accArr.put(ao)
            }
            obj.put("accounts", accArr)

            val selInst = PojavInstanceBridge.selected
            val instList = PojavInstanceBridge.instances
            obj.put("selectedInstance", selInst?.name ?: "Cloud 1.21.4")
            obj.put("selectedVersion", selInst?.versionId ?: "1.21.4")

            val instArr = JSONArray()
            for (inst in instList) {
                val io = JSONObject()
                io.put("name", inst.name)
                io.put("versionId", inst.versionId)
                io.put("selected", selInst != null && selInst.source === inst.source)
                instArr.put(io)
            }
            obj.put("instances", instArr)

            val maxRam = runCatching { max(2048, PojavSettingsBridge.maxRam(appCtx)) }.getOrDefault(8192)
            val freeRam = runCatching { PojavSettingsBridge.freeMemoryMb(appCtx) }.getOrDefault(2048)
            val ramMb = p.getInt("allocation", LauncherPreferences.PREF_RAM_ALLOCATION.takeIf { it > 0 } ?: 2048)
            val curRenderer = p.getString("renderer", LauncherPreferences.PREF_RENDERER ?: "opengles2") ?: "opengles2"

            val rendArr = JSONArray()
            var curRendererLabel = "MobileGlues (Vulkan 1.3 / OpenGL 4.6)"
            val choices = runCatching { PojavSettingsBridge.renderers(appCtx) }.getOrDefault(emptyList())
            if (choices.isNotEmpty()) {
                for (pair in choices) {
                    val id = pair.first
                    val lbl = pair.second
                    val ro = JSONObject()
                    ro.put("id", id)
                    ro.put("label", lbl)
                    rendArr.put(ro)
                    if (id == curRenderer) curRendererLabel = lbl
                }
            } else {
                for ((id, lbl) in listOf(
                    "opengles2" to "MobileGlues (Vulkan 1.3 / OpenGL 4.6)",
                    "vulkan_zink" to "Mesa Zink (Vulkan Desktop GL)",
                    "opengles3_ltw" to "LTW GLES 3.2 Translator"
                )) {
                    rendArr.put(JSONObject().put("id", id).put("label", lbl))
                    if (id == curRenderer) curRendererLabel = lbl
                }
            }

            obj.put("ramMb", ramMb)
            obj.put("maxRamMb", maxRam)
            obj.put("freeRamMb", freeRam)
            obj.put("renderer", curRenderer)
            obj.put("rendererLabel", curRendererLabel)
            obj.put("renderers", rendArr)
            obj.put("showRealFps", p.getBoolean("cloud_show_real_fps", true))
            obj.put("unlock120Hz", p.getBoolean("cloud_120hz", true))
            obj.put("sustainedPerf", p.getBoolean("sustainedPerformance", LauncherPreferences.PREF_SUSTAINED_PERFORMANCE))
            obj.put("fullScreen", AerixPreferences.fullScreen)
            obj.put("scaleFactor", p.getInt("resolutionRatio", (LauncherPreferences.PREF_SCALE_FACTOR * 100f).toInt().coerceIn(35, 100)))
            obj.put("jvmArgs", p.getString("javaArgs", LauncherPreferences.PREF_CUSTOM_JAVA_ARGS ?: "") ?: "")
            obj.put("keystrokes", p.getBoolean("cloud_keystrokes", true))
            obj.put("cpsCounter", p.getBoolean("cloud_cps", true))
            obj.put("toggleSprint", p.getBoolean("cloud_sprint", true))
            obj.put("zoomHud", p.getBoolean("cloud_zoom", true))
            obj.put("crosshair", p.getBoolean("cloud_crosshair", false))
            obj.put("preset", p.getString("cloud_fps_preset", "6gb") ?: "6gb")
            obj.put("packageName", appCtx.packageName)
            val vName = runCatching { appCtx.packageManager.getPackageInfo(appCtx.packageName, 0).versionName }.getOrNull() ?: "2.4.7"
            obj.put("versionName", vName)

            val hasTasks = customDlActive || PojavTaskBridge.hasTasks() || ProgressKeeper.hasOngoingTasks()
            obj.put("taskActive", hasTasks)
            obj.put("taskTitle", if (customDlActive) customDlTitle else "Cloud Engine Downloading Assets...")
            obj.put("taskPct", if (customDlActive) customDlPct else if (hasTasks) 55 else 0)
            obj.toString()
        } catch (t: Throwable) {
            JSONObject().put("accountName", "Cloud_OP").toString()
        }
    }

    @JavascriptInterface
    fun getProgressJson(): String {
        val ongoing = customDlActive || PojavTaskBridge.hasTasks() || ProgressKeeper.hasOngoingTasks()
        val o = JSONObject()
        o.put("active", ongoing)
        o.put("title", if (customDlActive) customDlTitle else "Downloading Minecraft Runtime & Assets...")
        o.put("pct", if (customDlActive) customDlPct else if (ongoing) 62 else 0)
        return o.toString()
    }

    @JavascriptInterface
    fun getVersionsJson(): String {
        val arr = JSONArray()
        val list = runCatching { PojavCreateInstanceBridge.gameVersions() }.getOrDefault(emptyList())
        for (v in list) {
            val o = JSONObject()
            o.put("id", v.id)
            o.put("type", v.type.name)
            arr.put(o)
        }
        return arr.toString()
    }

    @JavascriptInterface
    fun launchGame(): String {
        var retMsg = "Igniting Minecraft..."
        Tools.runOnUiThread {
            try {
                if (PojavAccountBridge.current == null) {
                    val existing = PojavAccountBridge.accounts
                    if (existing.isNotEmpty()) {
                        PojavAccountBridge.select(existing[0])
                    } else {
                        PojavAccountBridge.createOfflineAccount("Cloud_OP")
                        PojavAccountBridge.reload()
                    }
                }
                if (PojavInstanceBridge.selected == null) {
                    val all = PojavInstanceBridge.instances
                    if (all.isNotEmpty()) {
                        PojavInstanceBridge.select(all[0])
                    } else {
                        val created = Instances.createDefaultInstance()
                        Instances.setSelectedInstance(created)
                        PojavInstanceBridge.reload()
                    }
                }
                PojavLaunchBridge.play(activity)
            } catch (t: Throwable) {
                ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true)
            }
        }
        return retMsg
    }

    @JavascriptInterface
    fun joinServer(address: String): String {
        if (address.isBlank()) return "Invalid server address"
        Tools.runOnUiThread {
            try {
                val cls = Class.forName("net.kdt.pojavlaunch.aerix.bridge.PojavLaunchBridge")
                val m = cls.methods.firstOrNull { it.name == "setPendingServerAddress" }
                m?.invoke(PojavLaunchBridge, address.trim())
            } catch (_: Throwable) {}
            launchGame()
        }
        return "Joining $address..."
    }

    @JavascriptInterface
    fun selectInstance(index: Int) {
        Tools.runOnUiThread {
            val list = PojavInstanceBridge.instances
            if (index in list.indices) {
                PojavInstanceBridge.select(list[index])
            }
        }
    }

    @JavascriptInterface
    fun createInstance(loaderName: String, mcVersion: String, instanceName: String) {
        Thread({
            try {
                customDlActive = true
                customDlTitle = "Preparing $instanceName ($loaderName $mcVersion)..."
                customDlPct = 25
                val loader = runCatching { AerixLoader.valueOf(loaderName.uppercase()) }.getOrDefault(AerixLoader.VANILLA)
                val loaderVer: AerixLoaderVersion? = if (loader == AerixLoader.VANILLA) {
                    null
                } else {
                    customDlPct = 45
                    runCatching { PojavCreateInstanceBridge.loadLoaderVersions(loader, mcVersion).firstOrNull() }.getOrNull()
                }
                customDlPct = 70
                Tools.runOnUiThread {
                    PojavCreateInstanceBridge.install(
                        appCtx,
                        loader,
                        mcVersion,
                        loaderVer,
                        instanceName.ifBlank { "Cloud $mcVersion" },
                        null
                    ) { ok ->
                        customDlActive = false
                        PojavInstanceBridge.reload()
                        if (!ok) {
                            evalJs("window.CloudUI && CloudUI.toast('Instance setup finished — check Vault!')")
                        } else {
                            evalJs("window.CloudUI && (CloudUI.syncFromNative(), CloudUI.go('hq'), CloudUI.toast('Created ' + ${JSONObject.quote(instanceName)} + '!'))")
                        }
                    }
                }
            } catch (t: Throwable) {
                customDlActive = false
                evalJs("window.CloudUI && CloudUI.toast('Error: ' + ${JSONObject.quote(t.message ?: "Failed")})")
            }
        }, "CloudCreateInstance").start()
    }

    @JavascriptInterface
    fun renameInstance(index: Int, newName: String) {
        Tools.runOnUiThread {
            val list = PojavInstanceBridge.instances
            if (index in list.indices && newName.isNotBlank()) {
                PojavInstanceBridge.rename(list[index], newName.trim())
            }
        }
    }

    @JavascriptInterface
    fun cloneInstance(index: Int, copyName: String) {
        Tools.runOnUiThread {
            val list = PojavInstanceBridge.instances
            if (index in list.indices && copyName.isNotBlank()) {
                PojavInstanceBridge.copy(appCtx, list[index], copyName.trim())
            }
        }
    }

    @JavascriptInterface
    fun deleteInstance(index: Int) {
        Tools.runOnUiThread {
            val list = PojavInstanceBridge.instances
            if (index in list.indices) {
                PojavInstanceBridge.deleteInstance(list[index])
            }
        }
    }

    @JavascriptInterface
    fun microsoftLogin() {
        Tools.runOnUiThread {
            Tools.swapFragment(activity, MicrosoftLoginFragment::class.java, MicrosoftLoginFragment.TAG, null)
        }
    }

    @JavascriptInterface
    fun createOfflineAccount(username: String) {
        val clean = username.trim()
        if (clean.isEmpty()) return
        Tools.runOnUiThread {
            prefs().edit().putString("cloud_last_user", clean).apply()
            PojavAccountBridge.createOfflineAccount(clean)
            PojavAccountBridge.reload()
        }
    }

    @JavascriptInterface
    fun selectAccount(index: Int) {
        Tools.runOnUiThread {
            val list = PojavAccountBridge.accounts
            if (index in list.indices) {
                PojavAccountBridge.select(list[index])
            }
        }
    }

    @JavascriptInterface
    fun deleteAccount(index: Int) {
        Tools.runOnUiThread {
            val list = PojavAccountBridge.accounts
            if (index in list.indices) {
                PojavAccountBridge.delete(list[index])
            }
        }
    }

    @JavascriptInterface
    fun pickSkinFile() {
        Tools.runOnUiThread { onPickSkin() }
    }

    fun handlePickedSkinUri(uri: Uri) {
        Thread({
            try {
                val bytes = appCtx.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@Thread
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@Thread
                bmp.recycle()
                File(appCtx.filesDir, "cloud_custom_skin.png").writeBytes(bytes)
                val acc = PojavAccountBridge.current
                if (acc != null) {
                    Tools.runOnUiThread {
                        PojavAccountBridge.equipSkinFromUri(appCtx, acc, uri) {
                            evalJs("window.CloudUI && (CloudUI.syncFromNative(), CloudUI.toast('Custom HD Skin Equipped!'))")
                        }
                    }
                } else {
                    evalJs("window.CloudUI && (CloudUI.syncFromNative(), CloudUI.toast('Custom HD Skin Equipped!'))")
                }
            } catch (_: Throwable) {}
        }, "CloudSkinImport").start()
    }

    @JavascriptInterface
    fun applyPreset(preset: String) {
        val maxRam = runCatching { max(2048, PojavSettingsBridge.maxRam(appCtx)) }.getOrDefault(6144)
        val (targetRam, scale, flags) = when (preset.lowercase()) {
            "4gb" -> Triple(min(1792, maxRam), 75, "-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=45 -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20")
            "8gb" -> Triple(min(4096, maxRam), 100, "-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=35 -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=30 -XX:G1ReservePercent=15")
            else -> Triple(min(2816, maxRam), 85, "-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=40 -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=25")
        }
        val p = prefs()
        p.edit()
            .putString("cloud_fps_preset", preset.lowercase())
            .putInt("allocation", targetRam)
            .putInt("resolutionRatio", scale)
            .putString("javaArgs", flags)
            .putBoolean("sustainedPerformance", true)
            .putBoolean("cloud_120hz", true)
            .apply()
        runCatching {
            PojavSettingsBridge.writeInt(appCtx, "allocation", targetRam)
            PojavSettingsBridge.writeInt(appCtx, "resolutionRatio", scale)
            PojavSettingsBridge.writeString(appCtx, "javaArgs", flags)
            LauncherPreferences.loadPreferences(appCtx)
        }
        CloudInGameHud.applyHighRefreshRateIfEnabled(activity)
    }

    @JavascriptInterface
    fun setRamMb(mb: Int) {
        val clamped = mb.coerceAtLeast(512)
        prefs().edit().putInt("allocation", clamped).apply()
        runCatching {
            PojavSettingsBridge.writeInt(appCtx, "allocation", clamped)
            LauncherPreferences.loadPreferences(appCtx)
        }
    }

    @JavascriptInterface
    fun setRenderer(rendererId: String) {
        if (rendererId.isBlank()) return
        prefs().edit().putString("renderer", rendererId).apply()
        runCatching {
            PojavSettingsBridge.writeString(appCtx, "renderer", rendererId)
            LauncherPreferences.loadPreferences(appCtx)
        }
    }

    @JavascriptInterface
    fun setIntPref(key: String, value: Int) {
        prefs().edit().putInt(key, value).apply()
        runCatching {
            PojavSettingsBridge.writeInt(appCtx, key, value)
            LauncherPreferences.loadPreferences(appCtx)
        }
    }

    @JavascriptInterface
    fun setStringPref(key: String, value: String) {
        prefs().edit().putString(key, value).apply()
        runCatching {
            PojavSettingsBridge.writeString(appCtx, key, value)
            LauncherPreferences.loadPreferences(appCtx)
        }
    }

    @JavascriptInterface
    fun setToggle(key: String, enabled: Boolean) {
        val e = prefs().edit()
        when (key) {
            "showRealFps" -> {
                e.putBoolean("cloud_show_real_fps", enabled)
                e.putBoolean("show_fps", enabled)
            }
            "unlock120Hz" -> {
                e.putBoolean("cloud_120hz", enabled)
                Tools.runOnUiThread { CloudInGameHud.applyHighRefreshRateIfEnabled(activity) }
            }
            "fullScreen" -> AerixPreferences.saveFullScreen(enabled)
            "keystrokes" -> e.putBoolean("cloud_keystrokes", enabled)
            "cpsCounter" -> e.putBoolean("cloud_cps", enabled)
            "toggleSprint" -> e.putBoolean("cloud_sprint", enabled)
            "zoomHud" -> e.putBoolean("cloud_zoom", enabled)
            "crosshair" -> e.putBoolean("cloud_crosshair", enabled)
            else -> e.putBoolean(key, enabled)
        }
        e.apply()
        runCatching { LauncherPreferences.loadPreferences(appCtx) }
    }

    private fun activeGameDir(): File {
        val inst = runCatching { Instances.loadSelectedInstance() }.getOrNull()
        val dir = inst?.gameDirectory ?: File(Tools.DIR_GAME_NEW)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    @JavascriptInterface
    fun downloadUrlToSubfolder(fileUrl: String, fileName: String, subfolder: String) {
        val safeName = fileName.replace(Regex("[^a-zA-Z0-9._+-]"), "_").ifBlank { "cloud_asset.jar" }
        Thread({
            var conn: HttpURLConnection? = null
            try {
                customDlActive = true
                customDlTitle = "Downloading $safeName..."
                customDlPct = 5
                val targetDir = File(activeGameDir(), subfolder)
                if (!targetDir.exists()) targetDir.mkdirs()
                val outFile = File(targetDir, safeName)

                conn = (URL(fileUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "CloudLauncher/2.4.7 (Android)")
                }
                val total = conn.contentLengthLong
                conn.inputStream.use { input ->
                    FileOutputStream(outFile).use { output ->
                        val buf = ByteArray(16 * 1024)
                        var readBytes = 0L
                        while (true) {
                            val n = input.read(buf)
                            if (n <= 0) break
                            output.write(buf, 0, n)
                            readBytes += n
                            if (total > 0) {
                                customDlPct = ((readBytes * 100L) / total).toInt().coerceIn(5, 99)
                            }
                        }
                    }
                }
                customDlPct = 100
                Thread.sleep(300)
                customDlActive = false
                evalJs("window.CloudUI && CloudUI.toast('Installed ' + ${JSONObject.quote(safeName)} + '!')")
            } catch (t: Throwable) {
                customDlActive = false
                evalJs("window.CloudUI && CloudUI.toast('Download failed: ' + ${JSONObject.quote(t.message ?: "Network error")})")
            } finally {
                conn?.disconnect()
            }
        }, "CloudAssetDownloader").start()
    }

    @JavascriptInterface
    fun listFilesJson(subfolder: String): String {
        return try {
            val base = activeGameDir()
            val target = if (subfolder.isBlank()) base else File(base, subfolder)
            if (!target.exists() || !target.canonicalPath.startsWith(base.canonicalPath)) return "[]"
            val arr = JSONArray()
            val children = (target.listFiles() ?: emptyArray()).sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            for (f in children.take(120)) {
                val o = JSONObject()
                o.put("name", f.name)
                o.put("isDir", f.isDirectory)
                o.put("size", if (f.isDirectory) "Folder" else Tools.formatFileSize(f.length()))
                arr.put(o)
            }
            arr.toString()
        } catch (_: Throwable) {
            "[]"
        }
    }

    @JavascriptInterface
    fun deleteRelativeFile(relPath: String) {
        try {
            val base = activeGameDir()
            val f = File(base, relPath)
            if (f.exists() && f.canonicalPath.startsWith(base.canonicalPath) && f.isFile) {
                f.delete()
            }
        } catch (_: Throwable) {}
    }

    @JavascriptInterface
    fun openGameFolder() {
        Tools.runOnUiThread {
            runCatching { PojavInstanceBridge.openGameDirectory(activity) }
        }
    }

    @JavascriptInterface
    fun openControlsRig() {
        Tools.runOnUiThread {
            runCatching { PojavNavigator.openCustomControls(activity) }
        }
    }

    @JavascriptInterface
    fun openGamepad() {
        Tools.runOnUiThread {
            runCatching { PojavNavigator.openGamepadMapper(activity) }
        }
    }

    @JavascriptInterface
    fun runJarInstaller() {
        Tools.runOnUiThread { onPickJar() }
    }

    @JavascriptInterface
    fun clearCache() {
        Tools.runOnUiThread {
            runCatching { PojavSettingsBridge.clearMetadataCache(activity) }
        }
    }

    @JavascriptInterface
    fun openUrl(url: String) {
        Tools.runOnUiThread {
            runCatching { PojavNavigator.openUrl(activity, url) }
        }
    }

    @JavascriptInterface
    fun getConsoleLog(): String {
        return try {
            val logFile = File(Tools.DIR_GAME_HOME, "latestlog.txt")
            if (logFile.isFile) {
                val txt = logFile.readText(Charsets.UTF_8)
                if (txt.length > 16_000) txt.takeLast(16_000) else txt
            } else {
                "Cloud Launcher Prism Engine Ready.\nPackage: ${appCtx.packageName}\nLaunch an instance to stream real-time JVM & Renderer logs here."
            }
        } catch (t: Throwable) {
            "Console ready."
        }
    }

    @JavascriptInterface
    fun shareLogs() {
        Tools.runOnUiThread {
            runCatching { PojavNavigator.shareLogs(activity) }
        }
    }

    @JavascriptInterface
    fun reloadBackend() {
        Tools.runOnUiThread {
            runCatching {
                PojavInstanceBridge.reload()
                PojavAccountBridge.reload()
                PojavSettingsBridge.touch()
            }
        }
    }
}
