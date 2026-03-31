package com.voxable.feature_media.domain.model

import android.net.Uri

data class SubtitleTrack(
    val id: String,
    val language: String,
    val label: String,
    val uri: Uri,
    val format: SubtitleFormat,
    val mimeType: String? = null,
    val isDefault: Boolean = false
)

enum class SubtitleFormat {
    SRT,
    VTT,
    ASS,
    SSA,
    SUB,
    TTML,
    WEBVTT,
    UNKNOWN
}

data class Subtitle(
    val startTimeMs: Long,
    val endTimeMs: Long,
    val text: String,
    val formatting: SubtitleFormatting? = null
)

data class SubtitleFormatting(
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val color: String? = null,
    val backgroundColor: String? = null
)

data class SubtitleData(
    val trackId: String,
    val language: String,
    val subtitles: List<Subtitle>,
    val format: SubtitleFormat
)