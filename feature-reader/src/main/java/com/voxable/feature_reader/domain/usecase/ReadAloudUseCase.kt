package com.voxable.feature_reader.domain.usecase

import com.voxable.feature_reader.domain.model.TtsEvent
import com.voxable.feature_reader.domain.repository.BookReaderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReadAloudUseCase @Inject constructor(
    private val repository: BookReaderRepository
) {
    suspend fun start(text: String, language: String = "tr-TR", speed: Float = 1.0f, pitch: Float = 1.0f) {
        repository.startReading(text, language, speed, pitch)
    }

    suspend fun pause() = repository.pauseReading()
    suspend fun resume() = repository.resumeReading()
    suspend fun stop() = repository.stopReading()
    fun getTtsEvents(): Flow<TtsEvent> = repository.getTtsEvents()
}
