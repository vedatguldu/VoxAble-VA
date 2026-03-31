package com.voxable.feature_media.domain.usecase

import com.voxable.feature_media.domain.repository.MediaPlayerRepository

class SelectSubtitleTrackUseCase(private val repository: MediaPlayerRepository) {
    suspend operator fun invoke(trackId: String?) {
        repository.selectSubtitleTrack(trackId)
    }
}