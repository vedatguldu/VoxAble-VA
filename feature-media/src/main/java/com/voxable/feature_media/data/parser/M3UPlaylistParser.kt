package com.voxable.feature_media.data.parser

import android.net.Uri
import com.voxable.feature_media.domain.model.Playlist
import com.voxable.feature_media.domain.model.PlaylistItem
import com.voxable.feature_media.domain.model.PlaylistFormat
import org.jsoup.Jsoup

class M3UPlaylistParser {
    suspend fun parsePlaylist(content: String, uri: Uri): Playlist {
        val lines = content.lines().map { it.trim() }
        val items = mutableListOf<PlaylistItem>()
        var currentDuration = 0L
        var currentMetadata = mutableMapOf<String, String>()
        var index = 0

        for (line in lines) {
            when {
                line.isEmpty() || line.startsWith("#EXTM3U") -> continue
                line.startsWith("#EXTINF:") -> {
                    // Parse duration and title
                    val parts = line.substringAfter(":").split(",", limit = 2)
                    if (parts.isNotEmpty()) {
                        currentDuration = (parts[0].toDoubleOrNull() ?: 0.0).toLong() * 1000
                        if (parts.size > 1) {
                            currentMetadata["title"] = parts[1].trim()
                        }
                    }
                }
                line.startsWith("#") -> {
                    // Parse metadata tags
                    val tag = line.substringAfter("#")
                    if (tag.contains("=")) {
                        val (key, value) = tag.split("=", limit = 2)
                        currentMetadata[key.trim().lowercase()] = value.trim()
                    }
                }
                line.isNotEmpty() && !line.startsWith("#") -> {
                    // This is a media URI
                    val mediaUri = if (line.startsWith("http")) {
                        Uri.parse(line)
                    } else {
                        Uri.parse(uri.scheme + "://" + uri.authority + "/" + line)
                    }

                    val title = currentMetadata["title"] ?: "Track ${items.size + 1}"
                    items.add(
                        PlaylistItem(
                            id = "${items.size}",
                            title = title,
                            uri = mediaUri,
                            duration = currentDuration,
                            metadata = currentMetadata.toMap()
                        )
                    )
                    currentMetadata.clear()
                    currentDuration = 0L
                    index++
                }
            }
        }

        val format = if (content.contains("#EXT-X-")) PlaylistFormat.M3U8 else PlaylistFormat.M3U
        return Playlist(
            id = uri.lastPathSegment ?: "playlist",
            name = uri.lastPathSegment ?: "Unnamed Playlist",
            items = items,
            uri = uri,
            format = format
        )
    }
}