package com.voxable.feature_media.domain.usecase

import com.voxable.feature_media.domain.repository.MediaPlayerRepository

class SetVolumeUseCase(private val repository: MediaPlayerRepository) {
    suspend operator fun invoke(volumePercent: Float) {
        repository.setVolume(volumePercent)
    }
}