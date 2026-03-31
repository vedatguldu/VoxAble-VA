package com.voxable.feature_media.domain.usecase

import com.voxable.feature_media.domain.repository.MediaPlayerRepository

class SeekToUseCase(private val repository: MediaPlayerRepository) {
    suspend operator fun invoke(positionMs: Long) {
        repository.seekTo(positionMs)
    }
}