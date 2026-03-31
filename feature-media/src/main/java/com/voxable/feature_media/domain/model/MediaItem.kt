package com.voxable.feature_media.domain.model

import android.net.Uri

data class MediaItem(
    val id: String,
    val title: String,
    val uri: Uri,
    val type: MediaType,
    val duration: Long = 0L,
    val thumbnail: Uri? = null,
    val mimeType: String? = null,
    val metadata: MediaMetadata? = null
)

enum class MediaType {
    AUDIO,
    VIDEO,
    LIVE_AUDIO,
    LIVE_VIDEO
}

data class MediaMetadata(
    val artist: String? = null,
    val album: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val description: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val bitrate: Int? = null
)