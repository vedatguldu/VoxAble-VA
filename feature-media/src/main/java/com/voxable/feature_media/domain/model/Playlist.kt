package com.voxable.feature_media.domain.model

import android.net.Uri

data class Playlist(
    val id: String,
    val name: String,
    val items: List<PlaylistItem>,
    val uri: Uri? = null,
    val format: PlaylistFormat = PlaylistFormat.M3U8
)

data class PlaylistItem(
    val id: String,
    val title: String,
    val uri: Uri,
    val duration: Long = 0L,
    val metadata: Map<String, String> = emptyMap()
)

enum class PlaylistFormat {
    M3U,
    M3U8,
    PLS,
    XSPF,
    UNKNOWN
}

data class PlaylistMetadata(
    val version: String? = null,
    val targetDuration: Int? = null,
    val mediaSequence: Int? = null,
    val endList: Boolean = false,
    val isLive: Boolean = false
)