package com.voxable.feature_media.data.loader

import android.content.Context
import android.net.Uri
import com.voxable.feature_media.domain.model.SubtitleFormat
import com.voxable.feature_media.data.parser.SubtitleParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubtitleLoader(private val context: Context) {
    private val parser = SubtitleParser()

    suspend fun loadSubtitleFile(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader().use { it.readText() }
            } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun detectSubtitleFormat(content: String): SubtitleFormat {
        return parser.detectFormat(content)
    }

    suspend fun parseSubtitleContent(content: String, format: SubtitleFormat) =
        parser.parseSubtitles(content, format)

    suspend fun loadAndParseSubtitles(uri: Uri): Pair<String, com.voxable.feature_media.domain.model.SubtitleData>? =
        withContext(Dispatchers.IO) {
            try {
                val content = loadSubtitleFile(uri)
                val format = detectSubtitleFormat(content)
                val subtitles = parseSubtitleContent(content, format)
                val language = uri.lastPathSegment?.substringBefore(".")?.takeLast(2) ?: "unknown"

                language to com.voxable.feature_media.domain.model.SubtitleData(
                    trackId = uri.lastPathSegment.orEmpty(),
                    language = language,
                    subtitles = subtitles,
                    format = format
                )
            } catch (e: Exception) {
                null
            }
        }
}