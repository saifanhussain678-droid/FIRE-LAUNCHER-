package net.kdt.pojavlaunch.cloud

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.aerix.bridge.PojavInstanceBridge
import net.kdt.pojavlaunch.instances.Instance
import net.kdt.pojavlaunch.instances.InstanceIconProvider
import net.kdt.pojavlaunch.instances.Instances
import net.kdt.pojavlaunch.modloaders.FabriclikeUtils
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.utils.FileUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipFile

object CloudClientInstaller {

    private const val USER_AGENT = "CloudLauncher/2.0 (Android; CloudClient)"
    private val installLock = AtomicBoolean(false)

    /** Every Minecraft release from 1.21.1 up to 26.3 (newest-first, matching Option 2 UI). */
    val CLIENT_VERSIONS: List<String> = listOf(
        "26.3",
        "26.2",
        "26.1.2",
        "26.1.1",
        "26.1",
        "1.21.11",
        "1.21.10",
        "1.21.9",
        "1.21.8",
        "1.21.7",
        "1.21.6",
        "1.21.5",
        "1.21.4",
        "1.21.3",
        "1.21.2",
        "1.21.1"
    )

    data class ModSpec(
        val key: String,
        val displayTitle: String,
        val slugs: List<String>,
        val githubRepo: String? = null
    )

    /** Curated FPS Boost + Account Switcher Mod Pack shown in the Option 2 UI + auto-installed into mods/ */
    val MOD_PACK: List<ModSpec> = listOf(
        ModSpec("sodium", "Sodium (+300% FPS)", listOf("sodium"), "CaffeineMC/sodium"),
        ModSpec("lithium", "Lithium (Physics & Tick Boost)", listOf("lithium"), "CaffeineMC/lithium"),
        ModSpec("fpsdisplay", "FPS Display (Live FPS HUD)", listOf("fpsdisplay", "sodium-extra"), "FlashyReese/sodium-extra-fabric"),
        ModSpec("ias", "In-Game Account Switcher", listOf("in-game-account-switcher"), "The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher"),
        ModSpec("ferrite-core", "FerriteCore (-45% RAM)", listOf("ferrite-core"), null),
        ModSpec("immediatelyfast", "ImmediatelyFast", listOf("immediatelyfast"), "RaphiMC/ImmediatelyFast"),
        ModSpec("entityculling", "EntityCulling", listOf("entityculling"), "tr7zw/EntityCulling"),
        ModSpec("moreculling", "MoreCulling", listOf("moreculling"), "fxmorin/MoreCulling"),
        ModSpec("dynamic-fps", "Dynamic FPS", listOf("dynamic-fps", "krypton", "modernfix"), "juliand665/Dynamic-FPS"),
        ModSpec("fabric-api", "Fabric API", listOf("fabric-api"), "FabricMC/fabric")
    )

    @Volatile var isInstalling: Boolean = false
        private set
    @Volatile var installingVersion: String = ""
        private set
    @Volatile var statusText: String = "Ready · Select any Cloud Client version (1.21.1 — 26.3)"
        private set
    @Volatile var progressPct: Int = 0
        private set

    fun instanceNameFor(mcVersion: String): String = "Cloud Client $mcVersion"

    fun isCloudClientInstance(inst: Instance?): Boolean {
        if (inst == null) return false
        val nm = inst.name.orEmpty().trim()
        return nm.startsWith("Cloud Client ", ignoreCase = true)
    }

    fun findInstalled(mcVersion: String, all: List<Instance> = runCatching { Instances.loadAllInstances() }.getOrDefault(emptyList())): Instance? {
        val target = instanceNameFor(mcVersion)
        return all.firstOrNull { inst ->
            inst.name.equals(target, ignoreCase = true)
        }
    }

    fun buildCatalogJson(): JSONObject {
        val all = runCatching { Instances.loadAllInstances() }.getOrDefault(emptyList())
        val selected = runCatching { Instances.loadSelectedInstance() }.getOrNull()

        val verArr = JSONArray()
        for (ver in CLIENT_VERSIONS) {
            val inst = findInstalled(ver, all)
            val modsCount = runCatching {
                if (inst?.gameDirectory != null) {
                    File(inst.gameDirectory, "mods").listFiles()?.count {
                        it.isFile && it.extension.equals("jar", ignoreCase = true)
                    } ?: 0
                } else 0
            }.getOrDefault(0)

            verArr.put(
                JSONObject()
                    .put("version", ver)
                    .put("name", instanceNameFor(ver))
                    .put("installed", inst != null && modsCount > 0)
                    .put("active", inst != null && selected != null && inst.name == selected.name)
                    .put("modsCount", modsCount)
                    .put("java", if (ver.startsWith("26.")) 25 else 21)
            )
        }

        val modsArr = JSONArray()
        for (m in MOD_PACK) {
            modsArr.put(
                JSONObject()
                    .put("key", m.key)
                    .put("title", m.displayTitle)
            )
        }

        return JSONObject()
            .put("installing", isInstalling)
            .put("installingVersion", installingVersion)
            .put("status", statusText)
            .put("progress", progressPct)
            .put("versions", verArr)
            .put("mods", modsArr)
    }

    private fun httpGetText(urlStr: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                connectTimeout = 12_000
                readTimeout = 18_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept", "application/json")
            }
            if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } else null
        } catch (_: Throwable) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun downloadToFileVerified(urlStr: String, targetFile: File, expectedSha512: String? = null): Boolean {
        val parent = targetFile.parentFile ?: return false
        if (!parent.exists()) parent.mkdirs()
        val tmp = File(parent, "${targetFile.name}.part")
        var conn: HttpURLConnection? = null
        try {
            conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 30_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", USER_AGENT)
            }
            if (conn.responseCode !in 200..299) return false
            conn.inputStream.use { input ->
                FileOutputStream(tmp).use { out ->
                    val buf = ByteArray(32 * 1024)
                    while (true) {
                        val r = input.read(buf)
                        if (r <= 0) break
                        out.write(buf, 0, r)
                    }
                    out.flush()
                }
            }
            if (tmp.length() <= 1024L) return false

            if (!expectedSha512.isNullOrBlank() && expectedSha512.length == 128) {
                val digest = MessageDigest.getInstance("SHA-512")
                tmp.inputStream().use { fis ->
                    val buf = ByteArray(16 * 1024)
                    while (true) {
                        val r = fis.read(buf)
                        if (r <= 0) break
                        digest.update(buf, 0, r)
                    }
                }
                val actual = digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
                if (!actual.equals(expectedSha512, ignoreCase = true)) {
                    tmp.delete()
                    return false
                }
            }

            // Confirm valid Fabric mod JAR containing fabric.mod.json
            val validFabricJar = runCatching {
                ZipFile(tmp).use { zf -> zf.getEntry("fabric.mod.json") != null }
            }.getOrDefault(false)
            if (!validFabricJar) {
                tmp.delete()
                return false
            }

            if (targetFile.exists()) targetFile.delete()
            return tmp.renameTo(targetFile)
        } catch (_: Throwable) {
            return false
        } finally {
            tmp.delete()
            conn?.disconnect()
        }
    }

    private fun readFabricModId(jarFile: File): String? = runCatching {
        ZipFile(jarFile).use { zip ->
            val entry = zip.getEntry("fabric.mod.json") ?: return@use null
            if (entry.size <= 0 || entry.size > 262_144) return@use null
            val txt = zip.getInputStream(entry).bufferedReader().use { it.readText() }
            JSONObject(txt).optString("id").takeIf { it.isNotBlank() }
        }
    }.getOrNull()

    private fun hasModIdInDir(modsDir: File, modId: String): Boolean {
        return modsDir.listFiles()?.any { f ->
            f.isFile && f.extension.equals("jar", true) && readFabricModId(f) == modId
        } == true
    }

    private fun installModrinthProject(
        modsDir: File,
        slugOrId: String,
        mcVersion: String,
        visitedProjects: MutableSet<String>,
        depth: Int = 0
    ): Boolean {
        if (!visitedProjects.add(slugOrId.lowercase())) return true

        val candidateVersions = ArrayList<String>()
        candidateVersions.add(mcVersion)
        // Allow patch-level alias inside 26.1.x family only if exact tag has no entry
        if (mcVersion == "26.1") candidateVersions.add("26.1.2")

        for (queryVer in candidateVersions) {
            val loadersParam = URLEncoder.encode("[\"fabric\"]", "UTF-8")
            val gvParam = URLEncoder.encode("[\"$queryVer\"]", "UTF-8")
            val apiUrl = "https://api.modrinth.com/v2/project/$slugOrId/version?loaders=$loadersParam&game_versions=$gvParam"
            val raw = httpGetText(apiUrl) ?: continue
            val arr = runCatching { JSONArray(raw) }.getOrNull() ?: continue
            if (arr.length() == 0) continue

            val verObj = arr.getJSONObject(0)

            // Automatically install any required Modrinth dependency (e.g. cloth-config, reeses-sodium-options)
            if (depth < 2) {
                val deps = verObj.optJSONArray("dependencies")
                if (deps != null) {
                    for (i in 0 until deps.length()) {
                        val dep = deps.optJSONObject(i) ?: continue
                        if (dep.optString("dependency_type") == "required") {
                            val depId = dep.optString("project_id")
                            if (depId.isNotBlank()) {
                                runCatching {
                                    installModrinthProject(modsDir, depId, queryVer, visitedProjects, depth + 1)
                                }
                            }
                        }
                    }
                }
            }

            val files = verObj.optJSONArray("files") ?: continue
            if (files.length() == 0) continue
            var chosen = files.getJSONObject(0)
            for (i in 0 until files.length()) {
                val f = files.getJSONObject(i)
                if (f.optBoolean("primary", false)) {
                    chosen = f
                    break
                }
            }

            val dlUrl = chosen.optString("url")
            val fileName = chosen.optString("filename")
            val sha512 = chosen.optJSONObject("hashes")?.optString("sha512")
            if (dlUrl.isBlank() || fileName.isBlank() || !fileName.endsWith(".jar", true)) continue
            val safeName = File(fileName).name
            val dest = File(modsDir, safeName)
            if (dest.isFile && dest.length() > 1024L) return true
            if (downloadToFileVerified(dlUrl, dest, sha512)) return true
        }
        return false
    }

    private fun installGitHubReleaseFallback(modsDir: File, repo: String, mcVersion: String): Boolean {
        val raw = httpGetText("https://api.github.com/repos/$repo/releases?per_page=20") ?: return false
        val rels = runCatching { JSONArray(raw) }.getOrNull() ?: return false
        for (i in 0 until rels.length()) {
            val rel = rels.optJSONObject(i) ?: continue
            val tag = rel.optString("tag_name", "")
            val assets = rel.optJSONArray("assets") ?: continue
            for (j in 0 until assets.length()) {
                val asset = assets.optJSONObject(j) ?: continue
                val name = asset.optString("name", "")
                if (!name.endsWith(".jar", true) || name.endsWith("-sources.jar", true) || name.endsWith("-dev.jar", true)) continue
                val lower = "$tag $name".lowercase()
                val matchesVer = lower.contains("mc$mcVersion".lowercase()) ||
                    lower.contains("+$mcVersion".lowercase()) ||
                    lower.contains("-$mcVersion".lowercase())
                val matchesFabric = lower.contains("fabric") || repo.startsWith("CaffeineMC/") || repo == "FabricMC/fabric"
                if (matchesVer && matchesFabric) {
                    val dlUrl = asset.optString("browser_download_url", "")
                    if (dlUrl.isNotBlank()) {
                        val dest = File(modsDir, File(name).name)
                        if (dest.isFile && dest.length() > 1024L) return true
                        if (downloadToFileVerified(dlUrl, dest, null)) return true
                    }
                }
            }
        }
        return false
    }

    private fun resolveAndInstallFabricProfile(mcVersion: String): String {
        // 1. Resolve newest compatible Fabric Loader via Pojav's FabriclikeUtils
        val loaders = runCatching { FabriclikeUtils.FABRIC_UTILS.downloadLoaderVersions(mcVersion) }.getOrNull()
        val newestBuild = loaders?.firstOrNull { it.stable }?.version
            ?: loaders?.firstOrNull()?.version
            ?: run {
                // Direct Fabric Meta lookup fallback
                val encMc = URLEncoder.encode(mcVersion, "UTF-8")
                val raw = httpGetText("https://meta.fabricmc.net/v2/versions/loader/$encMc")
                val arr = if (!raw.isNullOrBlank()) runCatching { JSONArray(raw) }.getOrNull() else null
                var picked = ""
                if (arr != null && arr.length() > 0) {
                    picked = arr.optJSONObject(0)?.optJSONObject("loader")?.optString("version").orEmpty()
                }
                picked.ifBlank { if (mcVersion.startsWith("26.")) "0.19.5" else "0.18.4" }
            }

        val viaPojav = runCatching { FabriclikeUtils.FABRIC_UTILS.install(mcVersion, newestBuild) }.getOrNull()
        if (!viaPojav.isNullOrBlank()) return viaPojav

        val encMc = URLEncoder.encode(mcVersion, "UTF-8")
        val encBuild = URLEncoder.encode(newestBuild, "UTF-8")
        val profileJson = httpGetText("https://meta.fabricmc.net/v2/versions/loader/$encMc/$encBuild/profile/json")
        if (!profileJson.isNullOrBlank()) {
            val id = runCatching { JSONObject(profileJson).getString("id") }.getOrNull()
            if (!id.isNullOrBlank()) {
                val dir = File(Tools.DIR_HOME_VERSION, id)
                FileUtils.ensureDirectory(dir)
                Tools.write(File(dir, "$id.json"), profileJson)
                return id
            }
        }

        // Synthesized offline fallback
        val synId = "fabric-loader-$newestBuild-$mcVersion"
        val root = JSONObject()
            .put("id", synId)
            .put("inheritsFrom", mcVersion)
            .put("type", "release")
            .put("mainClass", "net.fabricmc.loader.impl.launch.knot.KnotClient")
            .put("arguments", JSONObject().put("game", JSONArray()).put("jvm", JSONArray().put("-DFabricMcEmu= net.minecraft.client.main.Main ")))
            .put("libraries", JSONArray()
                .put(JSONObject().put("name", "net.fabricmc:intermediary:$mcVersion").put("url", "https://maven.fabricmc.net/"))
                .put(JSONObject().put("name", "net.fabricmc:fabric-loader:$newestBuild").put("url", "https://maven.fabricmc.net/"))
                .put(JSONObject().put("name", "net.fabricmc:sponge-mixin:0.15.4+mixin.0.8.7").put("url", "https://maven.fabricmc.net/"))
                .put(JSONObject().put("name", "org.ow2.asm:asm:9.7.1").put("url", "https://maven.fabricmc.net/"))
                .put(JSONObject().put("name", "org.ow2.asm:asm-analysis:9.7.1").put("url", "https://maven.fabricmc.net/"))
                .put(JSONObject().put("name", "org.ow2.asm:asm-commons:9.7.1").put("url", "https://maven.fabricmc.net/"))
                .put(JSONObject().put("name", "org.ow2.asm:asm-tree:9.7.1").put("url", "https://maven.fabricmc.net/"))
                .put(JSONObject().put("name", "org.ow2.asm:asm-util:9.7.1").put("url", "https://maven.fabricmc.net/"))
            )
        val dir = File(Tools.DIR_HOME_VERSION, synId)
        FileUtils.ensureDirectory(dir)
        Tools.write(File(dir, "$synId.json"), root.toString(2))
        return synId
    }

    private fun writeCloudLogoToInstance(context: Context, instance: Instance) {
        runCatching {
            val bmp = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, 128f, 128f,
                    intArrayOf(0xFFE0F2FE.toInt(), 0xFFEDE9FE.toInt()),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRoundRect(4f, 4f, 124f, 124f, 28f, 28f, bg)
            val logo = context.assets.open("cloud_ui/logo.png").use {
                BitmapFactory.decodeStream(it)
            }
            if (logo != null) {
                canvas.drawBitmap(Bitmap.createScaledBitmap(logo, 96, 96, true), 16f, 16f, null)
            }
            instance.encodeNewIcon(bmp)
            InstanceIconProvider.dropIcon(instance)
        }
    }

    private fun writeAutoActiveFpsConfigs(gameDir: File) {
        runCatching {
            val configDir = File(gameDir, "config").apply { mkdirs() }
            // Enable Grayray75 FPS-Display HUD automatically on first launch
            val fpsDisplayCfg = File(configDir, "fpsdisplay.json")
            if (!fpsDisplayCfg.exists()) {
                fpsDisplayCfg.writeText(
                    """{"enabled":true,"textColor":16777215,"textAlpha":255,"background":true,"showMinMaxAvg":true,"offsetX":6,"offsetY":6}"""
                )
            }
            // Enable Sodium Extra's in-game FPS counter & particle boosts automatically on first launch
            val sodiumExtraCfg = File(configDir, "sodium-extra-options.json")
            if (!sodiumExtraCfg.exists()) {
                sodiumExtraCfg.writeText(
                    """{"extra_settings":{"overlay_corner":"TOP_LEFT","show_fps":true,"show_fps_extended":true,"cloud_height":192},"animation_settings":{"animation":true},"particle_settings":{"particles":true}}"""
                )
            }
            // Ensure Cloud Launcher's Real FPS HUD is also turned ON
            LauncherPreferences.DEFAULT_PREF.edit().putBoolean("cloud_real_fps_hud", true).apply()
        }
    }

    fun installAsync(
        context: Context,
        mcVersion: String,
        autoLaunch: Boolean,
        onProgress: (Boolean, String, Int, String) -> Unit,
        onReadyToLaunch: () -> Unit
    ) {
        if (!installLock.compareAndSet(false, true)) {
            onProgress(true, "Cloud Client installation already in progress...", progressPct, installingVersion)
            return
        }

        val appCtx = context.applicationContext
        Thread({
            try {
                isInstalling = true
                installingVersion = mcVersion
                progressPct = 6
                statusText = "CLOUD CLIENT INSTALLER · Resolving Fabric Loader for $mcVersion..."
                onProgress(true, statusText, progressPct, mcVersion)

                val fabricProfileId = resolveAndInstallFabricProfile(mcVersion)

                progressPct = 18
                statusText = "CLOUD CLIENT INSTALLER · Creating Cloud Client $mcVersion instance..."
                onProgress(true, statusText, progressPct, mcVersion)

                val targetName = instanceNameFor(mcVersion)
                val folderSlug = "cloud_client_${mcVersion.replace('.', '_')}"
                val existing = findInstalled(mcVersion)

                val clientInst: Instance = if (existing != null) {
                    existing.name = targetName
                    existing.versionId = fabricProfileId
                    existing.sharedData = false
                    existing.write()
                    existing
                } else {
                    Instances.createInstance({ inst ->
                        inst.name = targetName
                        inst.versionId = fabricProfileId
                        inst.icon = "fabric"
                        inst.sharedData = false
                    }, folderSlug)
                }

                writeCloudLogoToInstance(appCtx, clientInst)

                val gameDir = clientInst.gameDirectory.apply { mkdirs() }
                val modsDir = File(gameDir, "mods").apply { mkdirs() }
                val visited = HashSet<String>()

                // Install Fabric API first so every FPS mod has its core dependency ready
                progressPct = 24
                statusText = "CLOUD CLIENT INSTALLER · Auto-Installing Fabric API for $mcVersion..."
                onProgress(true, statusText, progressPct, mcVersion)
                val fabOk = installModrinthProject(modsDir, "fabric-api", mcVersion, visited)
                if (!fabOk) {
                    installGitHubReleaseFallback(modsDir, "FabricMC/fabric", mcVersion)
                }

                // Install all FPS Boost Mods in the Option 2 bundle
                val nonCoreMods = MOD_PACK.filter { it.key != "fabric-api" }
                var installedCount = if (modsDir.listFiles()?.isNotEmpty() == true) 1 else 0

                nonCoreMods.forEachIndexed { idx, spec ->
                    val pct = 30 + ((idx + 1) * 58 / nonCoreMods.size.coerceAtLeast(1))
                    progressPct = pct
                    statusText = "CLOUD CLIENT INSTALLER · Auto-Installing ${spec.displayTitle} for $mcVersion..."
                    onProgress(true, statusText, progressPct, mcVersion)

                    var ok = false
                    for (slug in spec.slugs) {
                        if (installModrinthProject(modsDir, slug, mcVersion, visited)) {
                            ok = true
                            // For FPS Display, try installing both fpsdisplay and sodium-extra if available!
                            if (spec.key != "fpsdisplay") break
                        }
                    }
                    if (!ok && spec.githubRepo != null) {
                        ok = installGitHubReleaseFallback(modsDir, spec.githubRepo, mcVersion)
                    }
                    if (ok) installedCount++

                    // Android Mobile Renderer Compatibility: pair Sodium with Podium on 1.21.1 - 26.3!
                    if (spec.key == "sodium" && hasModIdInDir(modsDir, "sodium")) {
                        runCatching { installModrinthProject(modsDir, "podium", mcVersion, visited) }
                    }
                }

                // Bonus ultra-lightweight FPS boosters + ModMenu when published for mcVersion
                for (extraSlug in listOf("modmenu", "krypton", "modernfix")) {
                    runCatching { installModrinthProject(modsDir, extraSlug, mcVersion, visited) }
                }

                writeAutoActiveFpsConfigs(gameDir)
                CloudClientPackBuilder.installPackAndBridge(appCtx, gameDir, mcVersion)

                Instances.setSelectedInstance(clientInst)
                PojavInstanceBridge.reload()

                val totalJars = modsDir.listFiles()?.count { it.isFile && it.extension.equals("jar", true) } ?: installedCount
                progressPct = 100
                statusText = "Cloud Client $mcVersion Ready · $totalJars FPS Boost Mods Active!"
                isInstalling = false
                installingVersion = ""
                onProgress(false, statusText, 100, mcVersion)

                if (autoLaunch) {
                    onReadyToLaunch()
                }
            } catch (t: Throwable) {
                isInstalling = false
                installingVersion = ""
                progressPct = 0
                statusText = "Install error: ${t.message ?: "Check internet connection"}"
                onProgress(false, statusText, 0, mcVersion)
            } finally {
                installLock.set(false)
            }
        }, "CloudClientInstall-$mcVersion").start()
    }
}
