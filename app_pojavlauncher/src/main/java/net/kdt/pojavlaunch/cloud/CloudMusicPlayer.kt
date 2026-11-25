package net.kdt.pojavlaunch.cloud

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.util.Base64
import net.kdt.pojavlaunch.Tools
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.atomic.AtomicLong
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CloudMusicPlayer {
    @Volatile private var mediaPlayer: MediaPlayer? = null
    private val playToken = AtomicLong(0L)

    @Volatile var currentSongId: String = ""
        private set
    @Volatile var currentTitle: String = ""
        private set
    @Volatile var currentArtist: String = ""
        private set
    @Volatile var currentArtwork: String = ""
        private set
    @Volatile var lastError: String = ""
        private set
    @Volatile var isPlayingState: Boolean = false
        private set
    @Volatile var isPreparingState: Boolean = false
        private set

    var onStateChanged: (() -> Unit)? = null
    var onWebFallbackRequested: ((String, String) -> Unit)? = null

    private fun notifyUi() {
        val cb = onStateChanged ?: return
        Tools.runOnUiThread {
            try { cb.invoke() } catch (_: Throwable) {}
        }
    }

    private fun ensureAudioReady(ctx: Context) {
        try {
            val am = ctx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setWillPauseWhenDucked(false)
                    .setOnAudioFocusChangeListener { /* Keep playing in background alongside game */ }
                    .build()
                am.requestAudioFocus(req)
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            }
        } catch (_: Throwable) {}
    }

    private fun candidateStreamUrls(rawUrl: String): List<String> {
        val clean = rawUrl.trim().replace("http://", "https://")
        if (!clean.contains("saavncdn.com")) return listOf(clean)

        val set = LinkedHashSet<String>()
        // _160.mp4 and _96.mp4 exist on 100% of Saavn tracks and download in <0.7s!
        val base96 = clean
            .replace("_320.mp4", "_96.mp4")
            .replace("_160.mp4", "_96.mp4")
        val base160 = base96.replace("_96.mp4", "_160.mp4")
        val base320 = base96.replace("_96.mp4", "_320.mp4")
        set.add(clean)
        set.add(base160)
        set.add(base96)
        set.add(base320)
        return set.toList()
    }

    private fun downloadToTempAudioFile(appContext: Context, rawUrl: String, token: Long): File? {
        val dir = File(appContext.cacheDir, "cloud_music_cache")
        if (!dir.exists()) dir.mkdirs()
        // Clean old temp tracks
        runCatching {
            dir.listFiles()?.forEach { f ->
                if (f.isFile && System.currentTimeMillis() - f.lastModified() > 60_000L) {
                    f.delete()
                }
            }
        }

        for (candidate in candidateStreamUrls(rawUrl)) {
            if (playToken.get() != token) return null
            var conn: HttpURLConnection? = null
            try {
                conn = (URL(candidate).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10_000
                    readTimeout = 20_000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/124.0 Mobile Safari/537.36")
                    setRequestProperty("Accept", "*/*")
                }
                val code = conn.responseCode
                if (code in 200..299) {
                    val ext = when {
                        candidate.contains(".mp3") -> ".mp3"
                        candidate.contains(".ogg") -> ".ogg"
                        candidate.contains(".wav") -> ".wav"
                        else -> ".m4a"
                    }
                    val outFile = File(dir, "track_${token}$ext")
                    var totalBytes = 0L
                    conn.inputStream.use { input ->
                        FileOutputStream(outFile).use { output ->
                            val buf = ByteArray(32 * 1024)
                            while (true) {
                                if (playToken.get() != token) {
                                    outFile.delete()
                                    return null
                                }
                                val r = input.read(buf)
                                if (r <= 0) break
                                output.write(buf, 0, r)
                                totalBytes += r
                            }
                            output.flush()
                        }
                    }
                    if (totalBytes > 2048L) {
                        outFile.setReadable(true, false)
                        return outFile
                    } else {
                        outFile.delete()
                    }
                }
            } catch (_: Throwable) {
            } finally {
                conn?.disconnect()
            }
        }
        return null
    }

    fun playUrl(
        appContext: Context,
        songId: String,
        title: String,
        artist: String,
        artwork: String,
        streamUrl: String
    ) {
        if (streamUrl.isBlank()) return
        val token = playToken.incrementAndGet()
        releaseCurrentPlayer()

        currentSongId = songId
        currentTitle = title
        currentArtist = artist
        currentArtwork = artwork
        lastError = ""
        isPreparingState = true
        isPlayingState = false
        notifyUi()

        Thread({
            try {
                val localFile: File? = if (streamUrl.startsWith("/") || streamUrl.startsWith("file://")) {
                    File(streamUrl.removePrefix("file://")).takeIf { it.isFile && it.length() > 0 }
                } else {
                    downloadToTempAudioFile(appContext.applicationContext, streamUrl, token)
                }

                if (playToken.get() != token) return@Thread

                if (localFile == null || !localFile.isFile) {
                    // Fallback to Chromium WebView HTML5 audio if direct download was blocked
                    isPreparingState = false
                    val fb = onWebFallbackRequested
                    if (fb != null) {
                        Tools.runOnUiThread { fb.invoke(songId, streamUrl) }
                    } else {
                        isPlayingState = false
                        notifyUi()
                    }
                    return@Thread
                }

                ensureAudioReady(appContext.applicationContext)

                val mp = MediaPlayer()
                // IMPORTANT: Do NOT call setWakeMode — AndroidManifest has no WAKE_LOCK permission,
                // and setWakeMode throws SecurityException inside mp.start() before _start() runs!
                mp.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                mp.isLooping = false
                mp.setVolume(1.0f, 1.0f)

                FileInputStream(localFile).use { fis ->
                    mp.setDataSource(fis.fd)
                    mp.prepare()
                }

                if (playToken.get() != token) {
                    runCatching { mp.release() }
                    return@Thread
                }

                // Song ends naturally when finished — NO looping!
                mp.setOnCompletionListener { player ->
                    synchronized(CloudMusicPlayer) {
                        if (mediaPlayer === player) {
                            mediaPlayer = null
                            isPlayingState = false
                            isPreparingState = false
                            currentSongId = ""
                            currentTitle = ""
                            currentArtist = ""
                            currentArtwork = ""
                        }
                    }
                    runCatching { player.reset() }
                    runCatching { player.release() }
                    notifyUi()
                }

                mp.setOnErrorListener { player, _, _ ->
                    synchronized(CloudMusicPlayer) {
                        if (mediaPlayer === player) {
                            mediaPlayer = null
                            isPlayingState = false
                            isPreparingState = false
                        }
                    }
                    runCatching { player.reset() }
                    runCatching { player.release() }
                    notifyUi()
                    true
                }

                synchronized(CloudMusicPlayer) {
                    if (playToken.get() != token) {
                        runCatching { mp.release() }
                        return@Thread
                    }
                    mediaPlayer = mp
                    mp.start()
                    isPreparingState = false
                    isPlayingState = true
                }
                notifyUi()
            } catch (t: Throwable) {
                lastError = t.message ?: "Playback error"
                synchronized(CloudMusicPlayer) {
                    isPreparingState = false
                    isPlayingState = false
                }
                val fb = onWebFallbackRequested
                if (fb != null && !streamUrl.startsWith("/")) {
                    Tools.runOnUiThread { fb.invoke(songId, streamUrl) }
                } else {
                    notifyUi()
                }
            }
        }, "CloudMusicPlay-$token").start()
    }

    @Synchronized
    private fun releaseCurrentPlayer() {
        val old = mediaPlayer
        mediaPlayer = null
        isPlayingState = false
        isPreparingState = false
        if (old != null) {
            Thread({
                runCatching { if (old.isPlaying) old.stop() }
                runCatching { old.reset() }
                runCatching { old.release() }
            }, "CloudMusicRelease").start()
        }
    }

    fun stop() {
        playToken.incrementAndGet()
        releaseCurrentPlayer()
        currentSongId = ""
        currentTitle = ""
        currentArtist = ""
        currentArtwork = ""
        notifyUi()
    }

    fun getStatusJson(): JSONObject {
        val mp = mediaPlayer
        val posSec = runCatching {
            if (mp != null && isPlayingState) mp.currentPosition / 1000 else 0
        }.getOrDefault(0)
        val durSec = runCatching {
            if (mp != null && isPlayingState) maxOf(0, mp.duration / 1000) else 0
        }.getOrDefault(0)

        return JSONObject()
            .put("playing", isPlayingState)
            .put("preparing", isPreparingState)
            .put("songId", currentSongId)
            .put("title", currentTitle)
            .put("artist", currentArtist)
            .put("artwork", currentArtwork)
            .put("positionSec", posSec)
            .put("durationSec", durSec)
            .put("error", lastError)
    }

    private fun unescapeHtml(raw: String): String {
        return raw
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&apos;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .trim()
    }

    private fun decryptSaavnUrl(encrypted: String, has320: Boolean): String? {
        if (encrypted.isBlank()) return null
        return try {
            val keySpec = SecretKeySpec("38346591".toByteArray(Charsets.US_ASCII), "DES")
            val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decoded = Base64.decode(encrypted.trim(), Base64.DEFAULT)
            var url = String(cipher.doFinal(decoded), Charsets.UTF_8).trim()
            url = url.replace("http://", "https://")
            url = if (has320) {
                url.replace("_96.mp4", "_160.mp4").replace("_96_p.mp4", "_160.mp4")
            } else {
                url.replace("_96_p.mp4", "_96.mp4")
            }
            url
        } catch (_: Throwable) {
            null
        }
    }

    private fun httpGetJson(urlStr: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 14_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/124.0 Mobile Safari/537.36")
                setRequestProperty("Accept", "application/json, text/plain, */*")
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

    fun searchSongsJson(query: String): JSONArray {
        val out = JSONArray()
        val seenIds = HashSet<String>()
        val cleanQ = query.trim().ifBlank { "Top Hindi & Global Hits" }
        val enc = URLEncoder.encode(cleanQ, "UTF-8")

        // 1. Primary Full-Song Engine: Direct JioSaavn Web v4 API
        try {
            val saavnRaw = httpGetJson(
                "https://www.jiosaavn.com/api.php?p=1&q=$enc&_format=json&_marker=0&api_version=4&ctx=web6dot0&n=40&__call=search.getResults"
            )
            if (!saavnRaw.isNullOrBlank()) {
                val results = JSONObject(saavnRaw).optJSONArray("results")
                if (results != null) {
                    for (i in 0 until results.length()) {
                        val item = results.optJSONObject(i) ?: continue
                        val more = item.optJSONObject("more_info")
                        val encMedia = more?.optString("encrypted_media_url", "") ?: ""
                        val has320 = more?.optString("320kbps", "false") == "true"
                        val streamUrl = decryptSaavnUrl(encMedia, has320) ?: continue
                        val id = "saavn_" + item.optString("id", i.toString())
                        if (!seenIds.add(id)) continue

                        val title = unescapeHtml(item.optString("title", item.optString("song", "Song")))
                        val subtitle = unescapeHtml(item.optString("subtitle", ""))
                        val img = item.optString("image", "").replace("150x150", "500x500").replace("http://", "https://")
                        val dur = more?.optString("duration", "0")?.toIntOrNull() ?: 0

                        out.put(
                            JSONObject()
                                .put("id", id)
                                .put("title", title)
                                .put("artist", subtitle)
                                .put("photo", img)
                                .put("streamUrl", streamUrl)
                                .put("durationSec", dur)
                        )
                    }
                }
            }
        } catch (_: Throwable) {}

        // 2. Secondary Full-Song Mirrors if direct endpoint returned <8 items
        if (out.length() < 8) {
            for (mirrorBase in listOf("https://saavn.sumit.co/api/search/songs", "https://saavn.dev/api/search/songs")) {
                try {
                    val mirrorRaw = httpGetJson("$mirrorBase?query=$enc&limit=35")
                    if (!mirrorRaw.isNullOrBlank()) {
                        val results = JSONObject(mirrorRaw).optJSONObject("data")?.optJSONArray("results")
                        if (results != null) {
                            for (i in 0 until results.length()) {
                                val item = results.optJSONObject(i) ?: continue
                                val dlArr = item.optJSONArray("downloadUrl")
                                var bestUrl = ""
                                if (dlArr != null && dlArr.length() > 0) {
                                    // Prefer 160kbps index (or last) for fast start + crisp audio
                                    val pickIdx = (dlArr.length() - 2).coerceAtLeast(0)
                                    bestUrl = dlArr.optJSONObject(pickIdx)?.optString("url", "")
                                        ?: dlArr.optJSONObject(dlArr.length() - 1)?.optString("url", "") ?: ""
                                }
                                if (bestUrl.isBlank()) continue
                                val id = "saavn_" + item.optString("id", i.toString())
                                if (!seenIds.add(id)) continue

                                val imgArr = item.optJSONArray("image")
                                val photo = if (imgArr != null && imgArr.length() > 0) {
                                    imgArr.optJSONObject(imgArr.length() - 1)?.optString("url", "") ?: ""
                                } else ""

                                val title = unescapeHtml(item.optString("name", "Song"))
                                val artistsArr = item.optJSONObject("artists")?.optJSONArray("primary")
                                val artist = if (artistsArr != null && artistsArr.length() > 0) {
                                    unescapeHtml(artistsArr.optJSONObject(0)?.optString("name", "") ?: "")
                                } else ""
                                val dur = item.optInt("duration", 0)

                                out.put(
                                    JSONObject()
                                        .put("id", id)
                                        .put("title", title)
                                        .put("artist", artist)
                                        .put("photo", photo)
                                        .put("streamUrl", bestUrl)
                                        .put("durationSec", dur)
                                )
                            }
                        }
                    }
                    if (out.length() >= 12) break
                } catch (_: Throwable) {}
            }
        }

        // 3. Global Fallback: Apple iTunes Music Catalog (100M+ worldwide songs)
        if (out.length() < 12) {
            try {
                val itunesRaw = httpGetJson("https://itunes.apple.com/search?term=$enc&media=music&entity=song&limit=35&country=IN")
                if (!itunesRaw.isNullOrBlank()) {
                    val results = JSONObject(itunesRaw).optJSONArray("results")
                    if (results != null) {
                        for (i in 0 until results.length()) {
                            val item = results.optJSONObject(i) ?: continue
                            val streamUrl = item.optString("previewUrl", "")
                            if (streamUrl.isBlank()) continue
                            val id = "it_" + item.optLong("trackId", i.toLong())
                            if (!seenIds.add(id)) continue

                            val title = item.optString("trackName", "Song")
                            val artist = item.optString("artistName", "")
                            val photo = item.optString("artworkUrl100", "").replace("100x100bb", "400x400bb")
                            val dur = (item.optLong("trackTimeMillis", 0L) / 1000L).toInt()

                            out.put(
                                JSONObject()
                                    .put("id", id)
                                    .put("title", title)
                                    .put("artist", artist)
                                    .put("photo", photo)
                                    .put("streamUrl", streamUrl)
                                    .put("durationSec", dur)
                            )
                        }
                    }
                }
            } catch (_: Throwable) {}
        }

        return out
    }

    fun scanDeviceSongsJson(): JSONArray {
        val out = JSONArray()
        val roots = listOf(
            File("/storage/emulated/0/Music"),
            File("/storage/emulated/0/Download"),
            File(Tools.DIR_GAME_HOME, "music")
        )
        var idx = 0
        for (dir in roots) {
            val files = runCatching { dir.listFiles() }.getOrNull() ?: continue
            for (f in files) {
                if (f.isFile && f.name.lowercase().let {
                        it.endsWith(".mp3") || it.endsWith(".m4a") || it.endsWith(".ogg") || it.endsWith(".wav")
                    }) {
                    out.put(
                        JSONObject()
                            .put("id", "local_${idx++}")
                            .put("title", f.nameWithoutExtension)
                            .put("artist", "Device Audio · ${dir.name}")
                            .put("photo", "")
                            .put("streamUrl", f.absolutePath)
                            .put("durationSec", 0)
                    )
                    if (idx >= 80) return out
                }
            }
        }
        return out
    }
}
