package com.voxable.feature_downloader.data.parser

import com.voxable.feature_downloader.domain.model.MediaFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

/**
 * M3U8 (HLS) playlist parser.
 * Master playlist'ten kalite varyantlarını çıkarır.
 */
@Singleton
class M3u8Parser @Inject constructor(
    private val okHttpClient: OkHttpClient
) {

    /**
     * M3U8 URL'sini indirip parse eder.
     * Master playlist ise → varyant listesi döner.
     * Media playlist ise → tek format döner.
     */
    suspend fun parse(m3u8Url: String): List<MediaFormat> = withContext(Dispatchers.IO) {
        val content = fetchContent(m3u8Url) ?: return@withContext emptyList()
        val lines = content.lines().map { it.trim() }

        if (!lines.any { it.startsWith("#EXTM3U") }) return@withContext emptyList()

        val isMasterPlaylist = lines.any { it.startsWith("#EXT-X-STREAM-INF") }

        if (isMasterPlaylist) {
            parseMasterPlaylist(lines, m3u8Url)
        } else {
            listOf(
                MediaFormat(
                    formatId = "hls_default",
                    url = m3u8Url,
                    mimeType = "application/x-mpegURL",
                    extension = "ts",
                    quality = "Varsayılan",
                    isAudioOnly = lines.any { it.startsWith("#EXT-X-MEDIA:") && it.contains("TYPE=AUDIO") }
                )
            )
        }
    }

    private fun parseMasterPlaylist(lines: List<String>, baseUrl: String): List<MediaFormat> {
        val formats = mutableListOf<MediaFormat>()
        var index = 0

        while (index < lines.size) {
            val line = lines[index]
            if (line.startsWith("#EXT-X-STREAM-INF:")) {
                val attributes = parseAttributes(line.substringAfter("#EXT-X-STREAM-INF:"))
                val bandwidth = attributes["BANDWIDTH"]?.toLongOrNull()
                val resolution = attributes["RESOLUTION"]
                val codecs = attributes["CODECS"]
                val name = attributes["NAME"]

                // Sonraki satır URL olmalı
                val urlLine = lines.getOrNull(index + 1)
                if (urlLine != null && !urlLine.startsWith("#")) {
                    val absoluteUrl = resolveUrl(baseUrl, urlLine)
                    val isAudioOnly = resolution == null && codecs?.contains("mp4a") == true

                    val quality = when {
                        name != null -> name
                        resolution != null -> {
                            val height = resolution.substringAfter("x")
                            "${height}p"
                        }
                        bandwidth != null -> "${bandwidth / 1000}kbps"
                        else -> "Varyant ${formats.size + 1}"
                    }

                    formats.add(
                        MediaFormat(
                            formatId = "hls_${formats.size}",
                            url = absoluteUrl,
                            mimeType = "application/x-mpegURL",
                            extension = if (isAudioOnly) "m4a" else "ts",
                            quality = quality,
                            resolution = resolution,
                            bitrate = bandwidth,
                            codec = codecs,
                            isAudioOnly = isAudioOnly
                        )
                    )
                    index += 2
                    continue
                }
            }
            index++
        }

        return formats.sortedByDescending { it.bitrate ?: 0 }
    }

    private fun parseAttributes(attributeString: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val regex = Regex("""([A-Z-]+)=(?:"([^"]*)"|([\w.x]+))""")
        regex.findAll(attributeString).forEach { match ->
            val key = match.groupValues[1]
            val value = match.groupValues[2].ifEmpty { match.groupValues[3] }
            result[key] = value
        }
        return result
    }

    private fun resolveUrl(base: String, relative: String): String {
        if (relative.startsWith("http://") || relative.startsWith("https://")) return relative
        return try {
            URI(base).resolve(relative).toString()
        } catch (_: Exception) {
            val baseDir = base.substringBeforeLast("/")
            "$baseDir/$relative"
        }
    }

    private fun fetchContent(url: String): String? {
        return try {
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) response.body?.string() else null
        } catch (_: Exception) {
            null
        }
    }
}
