package com.voxable.feature_media.domain.usecase

import com.voxable.feature_media.domain.repository.MediaPlayerRepository

class SetPlaybackSpeedUseCase(private val repository: MediaPlayerRepository) {
    suspend operator fun invoke(speed: Float) {
        repository.setPlaybackSpeed(speed)
    }
}