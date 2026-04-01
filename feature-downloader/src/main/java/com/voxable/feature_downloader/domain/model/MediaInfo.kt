package com.voxable.feature_downloader.domain.model

/**
 * URL'den algılanan medya bilgisi.
 */
data class MediaInfo(
    val url: String,
    val title: String,
    val type: MediaType,
    val formats: List<MediaFormat>,
    val thumbnailUrl: String? = null,
    val duration: Long? = null
)

enum class MediaType {
    VIDEO,
    AUDIO,
    HLS_STREAM,
    DIRECT_FILE,
    UNKNOWN
}
