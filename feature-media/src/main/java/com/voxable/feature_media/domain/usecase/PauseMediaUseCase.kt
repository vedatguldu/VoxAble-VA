package com.voxable.feature_media.domain.usecase

import com.voxable.feature_media.domain.repository.MediaPlayerRepository

class PauseMediaUseCase(private val repository: MediaPlayerRepository) {
    suspend operator fun invoke() {
        repository.pause()
    }
}