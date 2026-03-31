package com.voxable.feature_media.presentation

import com.voxable.feature_media.domain.model.MediaItem
import com.voxable.feature_media.domain.model.Playlist
import com.voxable.feature_media.domain.model.SubtitleTrack
import com.voxable.feature_media.domain.repository.PlaybackState

data class MediaPlayerState(
    val currentMediaItem: MediaItem? = null,
    val playlistItems: List<MediaItem> = emptyList(),
    val currentPlaylistIndex: Int = -1,
    val isPlaying: Boolean = false,
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val availableSubtitleTracks: List<SubtitleTrack> = emptyList(),
    val selectedSubtitleTrack: SubtitleTrack? = null,
    val currentSubtitleText: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showControls: Boolean = true,
    val showSubtitleSettings: Boolean = false,
    val showPlaylistSelector: Boolean = false
)

sealed interface MediaPlayerEvent {
    data class ShowError(val message: String) : MediaPlayerEvent
    data class ShowMessage(val message: String) : MediaPlayerEvent
    object PlaylistLoaded : MediaPlayerEvent
    object SubtitlesLoaded : MediaPlayerEvent
    object PlaybackEnded : MediaPlayerEvent
}