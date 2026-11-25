package net.kdt.pojavlaunch.cloud

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import android.util.Base64
import net.kdt.pojavlaunch.Tools
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CloudMusicPlayer {
    @Volatile private var mediaPlayer: MediaPlayer? = null
    @Volatile var currentSongId: String = ""
        private set
    @Volatile var currentTitle: String = ""
        private set
    @Volatile var currentArtist: String = ""
        private set
    @Volatile var currentArtwork: String = ""
        private set
    @Volatile var isPlayingState: Boolean = false
        private set
    @Volatile var isPreparingState: Boolean = false
        private set

    var onStateChanged: (() -> Unit)? = null

    private fun notifyUi() {
        val cb = onStateChanged ?: return
        Tools.runOnUiThread {
            try { cb.invoke() } catch (_: Throwable) {}
        }
    }

    @Synchronized
    fun playUrl(
        appContext: Context,
        songId: String,
        title: String,
        artist: String,
        artwork: String,
        streamUrl: String
    ) {
        if (streamUrl.isBlank()) return
        stopInternal(notify = false)

        currentSongId = songId
        currentTitle = title
        currentArtist = artist
        currentArtwork = artwork
        isPreparingState = true
        isPlayingState = false
        notifyUi()

        Thread({
            try {
                val mp = MediaPlayer()
                mp.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                runCatching {
                    mp.setWakeMode(appContext.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                }
                mp.isLooping = false

                if (streamUrl.startsWith("/") || streamUrl.startsWith("file://")) {
                    val path = streamUrl.removePrefix("file://")
                    mp.setDataSource(path)
                } else {
                    val headers = HashMap<String, String>()
                    headers["User-Agent"] = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/124.0 Mobile Safari/537.36"
                    mp.setDataSource(appContext.applicationContext, Uri.parse(streamUrl), headers)
                }

                mp.setOnPreparedListener { player ->
                    synchronized(CloudMusicPlayer) {
                        if (mediaPlayer === player) {
                            isPreparingState = false
                            isPlayingState = true
                            runCatching { player.start() }
                        } else {
                            runCatching { player.release() }
                        }
                    }
                    notifyUi()
                }

                // Song ends naturally when finished — NO looping!
                mp.setOnCompletionListener { player ->
                    synchronized(CloudMusicPlayer) {
                        if (mediaPlayer === player) {
                            runCatching { player.reset() }
                            runCatching { player.release() }
                            mediaPlayer = null
                            isPlayingState = false
                            isPreparingState = false
                            currentSongId = ""
                            currentTitle = ""
                            currentArtist = ""
                            currentArtwork = ""
                        }
                    }
                    notifyUi()
                }

                mp.setOnErrorListener { player, _, _ ->
                    synchronized(CloudMusicPlayer) {
                        if (mediaPlayer === player) {
                            runCatching { player.reset() }
                            runCatching { player.release() }
                            mediaPlayer = null
                            isPlayingState = false
                            isPreparingState = false
                        }
                    }
                    notifyUi()
                    true
                }

                synchronized(CloudMusicPlayer) {
                    mediaPlayer = mp
                }
                mp.prepareAsync()
            } catch (_: Throwable) {
                synchronized(CloudMusicPlayer) {
                    isPreparingState = false
                    isPlayingState = false
                }
                notifyUi()
            }
        }, "CloudMusicPrepare").start()
    }

    @Synchronized
    private fun stopInternal(notify: Boolean) {
        val old = mediaPlayer
        mediaPlayer = null
        isPlayingState = false
        isPreparingState = false
        currentSongId = ""
        currentTitle = ""
        currentArtist = ""
        currentArtwork = ""
        if (old != null) {
            Thread({
                runCatching {
                    if (old.isPlaying) old.stop()
                }
                runCatching { old.reset() }
                runCatching { old.release() }
            }, "CloudMusicRelease").start()
        }
        if (notify) notifyUi()
    }

    fun stop() {
        stopInternal(notify = true)
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
                url.replace("_96.mp4", "_320.mp4").replace("_96_p.mp4", "_320.mp4")
            } else {
                url.replace("_96.mp4", "_160.mp4").replace("_96_p.mp4", "_160.mp4")
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

        // 1. Primary Full-Song Engine: Direct JioSaavn Web v4 API (Full 3-5 min 320/160kbps songs!)
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
                                    bestUrl = dlArr.optJSONObject(dlArr.length() - 1)?.optString("url", "") ?: ""
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
