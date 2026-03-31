package com.voxable.feature_media.domain.usecase

import com.voxable.feature_media.domain.repository.MediaPlayerRepository
import kotlinx.coroutines.flow.Flow

class GetCurrentPositionUseCase(private val repository: MediaPlayerRepository) {
    operator fun invoke(): Flow<Long> {
        return repository.getCurrentPosition()
    }
}