package com.voxable.feature_media.data.manager

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import android.net.Uri as AndroidNetUri
import com.voxable.feature_media.domain.model.MediaItem as DomainMediaItem
import com.voxable.feature_media.domain.repository.PlaybackState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ExoPlayerManager(private val context: Context) {
    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context))
            .setTrackSelector(DefaultTrackSelector(context))
            .build()
    }

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.IDLE)
    private val _isPlaying = MutableStateFlow(false)
    private val _currentPosition = MutableStateFlow(0L)
    private val _duration = MutableStateFlow(0L)
    private val _playbackSpeed = MutableStateFlow(1.0f)

    val playbackState: Flow<PlaybackState> = _playbackState.asStateFlow()
    val isPlaying: Flow<Boolean> = _isPlaying.asStateFlow()
    val currentPosition: Flow<Long> = _currentPosition.asStateFlow()
    val duration: Flow<Long> = _duration.asStateFlow()
    val playbackSpeed: Flow<Float> = _playbackSpeed.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(@Player.State state: Int) {
            val playbackState = when (state) {
                Player.STATE_IDLE -> PlaybackState.IDLE
                Player.STATE_BUFFERING -> PlaybackState.BUFFERING
                Player.STATE_READY -> PlaybackState.READY
                Player.STATE_ENDED -> PlaybackState.ENDED
                else -> PlaybackState.IDLE
            }
            _playbackState.update { playbackState }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.update { isPlaying }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            @Player.DiscontinuityReason reason: Int
        ) {
            _currentPosition.update { exoPlayer.currentPosition }
        }
    }

    init {
        exoPlayer.addListener(playerListener)
    }

    fun loadMedia(mediaItem: DomainMediaItem) {
        val exoMediaItem = MediaItem.fromUri(mediaItem.uri)
        exoPlayer.setMediaItem(exoMediaItem)
        exoPlayer.prepare()
    }

    fun play() {
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _currentPosition.update { positionMs }
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer.setPlaybackSpeed(speed)
        _playbackSpeed.update { speed }
    }

    fun setVolume(volume: Float) {
        exoPlayer.volume = volume.coerceIn(0f, 1f)
    }

    fun getCurrentPosition(): Long = exoPlayer.currentPosition
    fun getDuration(): Long = exoPlayer.duration
    fun isPlaying(): Boolean = exoPlayer.isPlaying

    fun release() {
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
    }

    fun getExoPlayer(): ExoPlayer = exoPlayer
}