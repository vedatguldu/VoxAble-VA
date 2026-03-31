package com.voxable.feature_media.domain.repository

import com.voxable.core.util.Resource

interface MediaRepository {
    suspend fun play(uri: String): Resource<Unit>
    suspend fun pause(): Resource<Unit>
    suspend fun stop(): Resource<Unit>
    suspend fun seekTo(positionMs: Long): Resource<Unit>
    suspend fun setVolume(volume: Float): Resource<Unit>
    fun isPlaying(): Boolean
    fun getCurrentPositionMs(): Long
    fun getDurationMs(): Long
    fun release()
}
