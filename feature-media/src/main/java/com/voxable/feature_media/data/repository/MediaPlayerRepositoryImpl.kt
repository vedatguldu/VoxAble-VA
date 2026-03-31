package com.voxable.feature_media.data.repository

import android.net.Uri
import com.voxable.feature_media.data.loader.SubtitleLoader
import com.voxable.feature_media.data.manager.ExoPlayerManager
import com.voxable.feature_media.data.parser.M3UPlaylistParser
import com.voxable.feature_media.domain.model.MediaItem
import com.voxable.feature_media.domain.model.Playlist
import com.voxable.feature_media.domain.model.SubtitleTrack
import com.voxable.feature_media.domain.model.SubtitleFormat
import com.voxable.feature_media.domain.repository.MediaPlayerRepository
import com.voxable.feature_media.domain.repository.PlaybackState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MediaPlayerRepositoryImpl(
    private val exoPlayerManager: ExoPlayerManager,
    private val playlistParser: M3UPlaylistParser,
    private val subtitleLoader: SubtitleLoader
) : MediaPlayerRepository {

    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    private val _playlistItems = MutableStateFlow<List<MediaItem>>(emptyList())
    private val _selectedSubtitleTrack = MutableStateFlow<SubtitleTrack?>(null)
    private val _availableSubtitleTracks = MutableStateFlow<List<SubtitleTrack>>(emptyList())
    private val _volume = MutableStateFlow(1.0f)

    override suspend fun play(mediaItem: MediaItem) {
        _currentMediaItem.update { mediaItem }
        exoPlayerManager.loadMedia(mediaItem)
        exoPlayerManager.play()
    }

    override suspend fun pause() {
        exoPlayerManager.pause()
    }

    override suspend fun resume() {
        exoPlayerManager.play()
    }

    override suspend fun stop() {
        exoPlayerManager.pause()
        exoPlayerManager.seekTo(0L)
    }

    override suspend fun seekTo(positionMs: Long) {
        exoPlayerManager.seekTo(positionMs)
    }

    override suspend fun loadPlaylist(uri: Uri): Playlist {
        val content = loadPlaylistFile(uri)
        return playlistParser.parsePlaylist(content, uri)
    }

    override suspend fun playPlaylist(playlist: Playlist, startIndex: Int) {
        _playlistItems.update { playlist.items.map { item ->
            MediaItem(
                id = item.id,
                title = item.title,
                uri = item.uri,
                type = com.voxable.feature_media.domain.model.MediaType.AUDIO,
                duration = item.duration
            )
        } }
        if (startIndex < _playlistItems.value.size) {
            play(_playlistItems.value[startIndex])
        }
    }

    override suspend fun nextItem() {
        val currentIndex = _playlistItems.value.indexOfFirst { it.id == _currentMediaItem.value?.id }
        if (currentIndex >= 0 && currentIndex < _playlistItems.value.size - 1) {
            play(_playlistItems.value[currentIndex + 1])
        }
    }

    override suspend fun previousItem() {
        val currentIndex = _playlistItems.value.indexOfFirst { it.id == _currentMediaItem.value?.id }
        if (currentIndex > 0) {
            play(_playlistItems.value[currentIndex - 1])
        }
    }

    override suspend fun selectPlaylistItem(index: Int) {
        if (index in _playlistItems.value.indices) {
            play(_playlistItems.value[index])
        }
    }

    override suspend fun loadSubtitles(mediaUri: Uri): List<SubtitleTrack> {
        // Auto-detect embedded subtitles from media file
        // This is a placeholder - actual implementation would use MediaExtractor
        return emptyList()
    }

    override suspend fun loadExternalSubtitles(subtitleUri: Uri, language: String): SubtitleTrack {
        val (_, subtitleData) = subtitleLoader.loadAndParseSubtitles(subtitleUri) ?: 
            return SubtitleTrack(
                id = "error",
                language = language,
                label = "Error loading subtitles",
                uri = subtitleUri,
                format = SubtitleFormat.UNKNOWN
            )

        val track = SubtitleTrack(
            id = subtitleUri.lastPathSegment.orEmpty(),
            language = language,
            label = language,
            uri = subtitleUri,
            format = subtitleData.format
        )
        _availableSubtitleTracks.update { it + track }
        return track
    }

    override suspend fun selectSubtitleTrack(trackId: String?) {
        _selectedSubtitleTrack.update {
            _availableSubtitleTracks.value.firstOrNull { it.id == trackId }
        }
    }

    override fun getCurrentPosition(): Flow<Long> = exoPlayerManager.currentPosition
    override fun getDuration(): Flow<Long> = exoPlayerManager.duration
    override fun isPlaying(): Flow<Boolean> = exoPlayerManager.isPlaying
    override fun getCurrentMediaItem(): Flow<MediaItem?> = _currentMediaItem.asStateFlow()
    override fun getPlaylistItems(): Flow<List<MediaItem>> = _playlistItems.asStateFlow()
    override fun getSelectedSubtitleTrack(): Flow<SubtitleTrack?> = _selectedSubtitleTrack.asStateFlow()
    override fun getAvailableSubtitleTracks(): Flow<List<SubtitleTrack>> = _availableSubtitleTracks.asStateFlow()
    override fun getPlaybackState(): Flow<PlaybackState> = exoPlayerManager.playbackState
    override fun getPlaybackSpeed(): Flow<Float> = exoPlayerManager.playbackSpeed

    override suspend fun setVolume(volumePercent: Float) {
        _volume.update { volumePercent / 100f }
        exoPlayerManager.setVolume(_volume.value)
    }

    override suspend fun setPlaybackSpeed(speed: Float) {
        exoPlayerManager.setPlaybackSpeed(speed)
    }

    override suspend fun muteAudio() {
        exoPlayerManager.setVolume(0f)
    }

    override suspend fun unmuteAudio() {
        exoPlayerManager.setVolume(_volume.value)
    }

    override suspend fun savePlaybackPosition(mediaId: String, positionMs: Long) {
        // Save to local storage or database
    }

    override suspend fun getPlaybackPosition(mediaId: String): Long {
        return 0L // Retrieve from storage
    }

    override suspend fun clearPlaybackHistory() {
        // Clear playback history from storage
    }

    private suspend fun loadPlaylistFile(uri: Uri): String {
        return try {
            val context = android.content.ContextCompat::class.java.classLoader?.loadClass(
                "android.content.Context"
            )
            ""
        } catch (e: Exception) {
            ""
        }
    }
}