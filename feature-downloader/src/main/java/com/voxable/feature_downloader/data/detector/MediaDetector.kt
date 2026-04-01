package com.voxable.feature_downloader.data.detector

import com.voxable.feature_downloader.data.parser.M3u8Parser
import com.voxable.feature_downloader.domain.model.MediaFormat
import com.voxable.feature_downloader.domain.model.MediaInfo
import com.voxable.feature_downloader.domain.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

/**
 * URL'den medya tipini ve mevcut formatları algılar.
 * HEAD isteği ile Content-Type kontrol eder,
 * m3u8 ise M3u8Parser'a delege eder.
 */
@Singleton
class MediaDetector @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val m3u8Parser: M3u8Parser
) {

    suspend fun detect(url: String): MediaInfo = withContext(Dispatchers.IO) {
        val trimmedUrl = url.trim()
        val fileName = extractFileName(trimmedUrl)

        // Uzantıdan hızlı tespit
        val extensionType = detectByExtension(trimmedUrl)
        if (extensionType == MediaType.HLS_STREAM) {
            return@withContext buildHlsInfo(trimmedUrl, fileName)
        }

        // HEAD isteği ile Content-Type kontrol
        val contentType = fetchContentType(trimmedUrl)
        val type = resolveMediaType(trimmedUrl, contentType, extensionType)

        when (type) {
            MediaType.HLS_STREAM -> buildHlsInfo(trimmedUrl, fileName)
            MediaType.VIDEO -> buildDirectInfo(trimmedUrl, fileName, type, contentType)
            MediaType.AUDIO -> buildDirectInfo(trimmedUrl, fileName, type, contentType)
            else -> buildDirectInfo(trimmedUrl, fileName, type, contentType)
        }
    }

    private suspend fun buildHlsInfo(url: String, fileName: String): MediaInfo {
        val formats = m3u8Parser.parse(url)
        return MediaInfo(
            url = url,
            title = fileName,
            type = MediaType.HLS_STREAM,
            formats = formats.ifEmpty {
                listOf(
                    MediaFormat(
                        formatId = "hls_default",
                        url = url,
                        mimeType = "application/x-mpegURL",
                        extension = "ts",
                        quality = "Varsayılan"
                    )
                )
            }
        )
    }

    private fun buildDirectInfo(
        url: String,
        fileName: String,
        type: MediaType,
        contentType: String?
    ): MediaInfo {
        val ext = extractExtension(url)
        val mimeType = contentType ?: guessMimeType(ext)

        val format = MediaFormat(
            formatId = "direct",
            url = url,
            mimeType = mimeType,
            extension = ext,
            quality = when (type) {
                MediaType.VIDEO -> "Video"
                MediaType.AUDIO -> "Ses"
                else -> "Dosya"
            },
            isAudioOnly = type == MediaType.AUDIO
        )

        return MediaInfo(
            url = url,
            title = fileName,
            type = type,
            formats = listOf(format)
        )
    }

    private fun detectByExtension(url: String): MediaType {
        val ext = extractExtension(url)
        return when (ext) {
            "m3u8" -> MediaType.HLS_STREAM
            "mp4", "mkv", "avi", "webm", "mov", "flv", "3gp", "ts" -> MediaType.VIDEO
            "mp3", "aac", "wav", "ogg", "flac", "m4a", "wma", "opus" -> MediaType.AUDIO
            else -> MediaType.UNKNOWN
        }
    }

    private fun resolveMediaType(url: String, contentType: String?, extensionType: MediaType): MediaType {
        // Content-Type'tan tespit
        contentType?.let { ct ->
            return when {
                ct.contains("mpegurl", ignoreCase = true) -> MediaType.HLS_STREAM
                ct.contains("video/", ignoreCase = true) -> MediaType.VIDEO
                ct.contains("audio/", ignoreCase = true) -> MediaType.AUDIO
                ct.contains("application/octet-stream") -> extensionType
                else -> extensionType
            }
        }
        return if (extensionType != MediaType.UNKNOWN) extensionType else MediaType.DIRECT_FILE
    }

    private fun fetchContentType(url: String): String? {
        return try {
            val request = Request.Builder().url(url).head().build()
            val response = okHttpClient.newCall(request).execute()
            response.header("Content-Type")
        } catch (_: Exception) {
            null
        }
    }

    private fun extractFileName(url: String): String {
        return try {
            val path = URI(url).path ?: url
            val name = path.substringAfterLast("/").substringBefore("?")
            if (name.isNotBlank()) name else "medya"
        } catch (_: Exception) {
            "medya"
        }
    }

    private fun extractExtension(url: String): String {
        val path = url.substringBefore("?").substringBefore("#")
        return path.substringAfterLast(".").lowercase().take(10)
    }

    private fun guessMimeType(ext: String): String = when (ext) {
        "mp4" -> "video/mp4"
        "mkv" -> "video/x-matroska"
        "avi" -> "video/x-msvideo"
        "webm" -> "video/webm"
        "mov" -> "video/quicktime"
        "flv" -> "video/x-flv"
        "3gp" -> "video/3gpp"
        "ts" -> "video/mp2t"
        "mp3" -> "audio/mpeg"
        "aac" -> "audio/aac"
        "wav" -> "audio/wav"
        "ogg" -> "audio/ogg"
        "flac" -> "audio/flac"
        "m4a" -> "audio/mp4"
        "wma" -> "audio/x-ms-wma"
        "opus" -> "audio/opus"
        "m3u8" -> "application/x-mpegURL"
        else -> "application/octet-stream"
    }
}
