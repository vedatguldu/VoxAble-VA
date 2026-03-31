package com.voxable.feature_media.domain.usecase

import com.voxable.feature_media.domain.model.MediaItem
import com.voxable.feature_media.domain.repository.MediaPlayerRepository

class PlayMediaUseCase(private val repository: MediaPlayerRepository) {
    suspend operator fun invoke(mediaItem: MediaItem) {
        repository.play(mediaItem)
    }
}