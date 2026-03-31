package com.voxable.feature_media.domain.repository

import android.net.Uri
import com.voxable.feature_media.domain.model.MediaItem
import com.voxable.feature_media.domain.model.Playlist
import com.voxable.feature_media.domain.model.SubtitleTrack
import kotlinx.coroutines.flow.Flow

interface MediaPlayerRepository {
    // Playback control
    suspend fun play(mediaItem: MediaItem)
    suspend fun pause()
    suspend fun resume()
    suspend fun stop()
    suspend fun seekTo(positionMs: Long)
    
    // Playlist
    suspend fun loadPlaylist(uri: Uri): Playlist
    suspend fun playPlaylist(playlist: Playlist, startIndex: Int = 0)
    suspend fun nextItem()
    suspend fun previousItem()
    suspend fun selectPlaylistItem(index: Int)
    
    // Subtitle management
    suspend fun loadSubtitles(mediaUri: Uri): List<SubtitleTrack>
    suspend fun loadExternalSubtitles(subtitleUri: Uri, language: String): SubtitleTrack
    suspend fun selectSubtitleTrack(trackId: String?)
    
    // State flows
    fun getCurrentPosition(): Flow<Long>
    fun getDuration(): Flow<Long>
    fun isPlaying(): Flow<Boolean>
    fun getCurrentMediaItem(): Flow<MediaItem?>
    fun getPlaylistItems(): Flow<List<MediaItem>>
    fun getSelectedSubtitleTrack(): Flow<SubtitleTrack?>
    fun getAvailableSubtitleTracks(): Flow<List<SubtitleTrack>>
    fun getPlaybackState(): Flow<PlaybackState>
    fun getPlaybackSpeed(): Flow<Float>
    
    // Volume and audio
    suspend fun setVolume(volumePercent: Float)
    suspend fun setPlaybackSpeed(speed: Float)
    suspend fun muteAudio()
    suspend fun unmuteAudio()
    
    // Settings
    suspend fun savePlaybackPosition(mediaId: String, positionMs: Long)
    suspend fun getPlaybackPosition(mediaId: String): Long
    suspend fun clearPlaybackHistory()
}

enum class PlaybackState {
    IDLE,
    BUFFERING,
    READY,
    PLAYING,
    PAUSED,
    ENDED,
    ERROR
}