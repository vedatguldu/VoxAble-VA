package com.voxable.feature_media.data.parser

import com.voxable.feature_media.domain.model.Subtitle
import com.voxable.feature_media.domain.model.SubtitleFormat

class SubtitleParser {
    suspend fun parseSRT(content: String): List<Subtitle> {
        val subtitles = mutableListOf<Subtitle>()
        val blocks = content.split("\n\n")

        for (block in blocks) {
            val lines = block.trim().lines()
            if (lines.size < 3) continue

            val timeLine = lines[1]
            val textLines = lines.drop(2)

            try {
                val (startStr, endStr) = timeLine.split(" --> ")
                val startMs = parseTimeToMs(startStr.trim())
                val endMs = parseTimeToMs(endStr.trim())

                subtitles.add(
                    Subtitle(
                        startTimeMs = startMs,
                        endTimeMs = endMs,
                        text = textLines.joinToString("\n")
                    )
                )
            } catch (e: Exception) {
                // Skip malformed subtitle
                continue
            }
        }

        return subtitles
    }

    suspend fun parseVTT(content: String): List<Subtitle> {
        val subtitles = mutableListOf<Subtitle>()
        val lines = content.split("\n")
        val filtered = lines.dropWhile { !it.contains(" --> ") }

        var i = 0
        while (i < filtered.size) {
            val timeLine = filtered.getOrNull(i) ?: break
            if (!timeLine.contains(" --> ")) {
                i++
                continue
            }

            val (startStr, endStr) = timeLine.split(" --> ")
            val textLines = mutableListOf<String>()
            i++

            while (i < filtered.size && filtered[i].isNotEmpty()) {
                if (filtered[i].contains(" --> ")) {
                    break
                }
                textLines.add(filtered[i])
                i++
            }

            try {
                if (textLines.isNotEmpty()) {
                    subtitles.add(
                        Subtitle(
                            startTimeMs = parseTimeToMs(startStr.trim()),
                            endTimeMs = parseTimeToMs(endStr.trim()),
                            text = textLines.joinToString("\n")
                        )
                    )
                }
            } catch (e: Exception) {
                continue
            }
        }

        return subtitles
    }

    suspend fun parseSubtitles(content: String, format: SubtitleFormat): List<Subtitle> {
        return when (format) {
            SubtitleFormat.SRT -> parseSRT(content)
            SubtitleFormat.VTT, SubtitleFormat.WEBVTT -> parseVTT(content)
            else -> emptyList()
        }
    }

    suspend fun detectFormat(content: String): SubtitleFormat {
        return when {
            content.contains("#WEBVTT") -> SubtitleFormat.VTT
            content.contains(Regex("\\d{2}:\\d{2}:\\d{2},\\d{3} --> \\d{2}:\\d{2}:\\d{2},\\d{3}")) -> SubtitleFormat.SRT
            content.contains("[Script Info]") -> SubtitleFormat.ASS
            else -> SubtitleFormat.UNKNOWN
        }
    }

    private fun parseTimeToMs(timeStr: String): Long {
        return try {
            val parts = timeStr.split(":")
            val hours = parts.getOrNull(0)?.toLongOrNull() ?: 0
            val minutes = parts.getOrNull(1)?.toLongOrNull() ?: 0
            val secAndMs = parts.getOrNull(2)?.split(",") ?: listOf("0", "0")
            val seconds = secAndMs.getOrNull(0)?.toLongOrNull() ?: 0
            val ms = secAndMs.getOrNull(1)?.toLongOrNull() ?: 0

            (hours * 3600 + minutes * 60 + seconds) * 1000 + ms
        } catch (e: Exception) {
            0L
        }
    }
}