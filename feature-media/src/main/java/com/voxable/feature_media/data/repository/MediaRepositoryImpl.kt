package com.voxable.feature_media.data.repository

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.voxable.core.util.Resource
import com.voxable.feature_media.domain.repository.MediaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MediaRepository {

    private var player: ExoPlayer? = null

    private fun getOrCreatePlayer(): ExoPlayer {
        return player ?: ExoPlayer.Builder(context).build().also {
            player = it
        }
    }

    override suspend fun play(uri: String): Resource<Unit> {
        return try {
            val exoPlayer = getOrCreatePlayer()
            val mediaItem = MediaItem.fromUri(uri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Medya oynat\u0131lamad\u0131")
        }
    }

    override suspend fun pause(): Resource<Unit> {
        return try {
            player?.pause()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Medya duraklatılamadı")
        }
    }

    override suspend fun stop(): Resource<Unit> {
        return try {
            player?.stop()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Medya durdurulamadı")
        }
    }

    override suspend fun seekTo(positionMs: Long): Resource<Unit> {
        return try {
            player?.seekTo(positionMs)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Konum değiştirilemedi")
        }
    }

    override suspend fun setVolume(volume: Float): Resource<Unit> {
        return try {
            player?.volume = volume.coerceIn(0f, 1f)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Ses seviyesi ayarlanamadı")
        }
    }

    override fun isPlaying(): Boolean = player?.isPlaying == true

    override fun getCurrentPositionMs(): Long = player?.currentPosition ?: 0L

    override fun getDurationMs(): Long = player?.duration ?: 0L

    override fun release() {
        player?.release()
        player = null
    }
}
