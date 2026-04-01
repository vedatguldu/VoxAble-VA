package com.voxable.feature_reader.domain.usecase

import com.voxable.core.util.Resource
import com.voxable.feature_reader.domain.model.TtsEvent
import com.voxable.feature_reader.domain.repository.BookReaderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReadAloudUseCase @Inject constructor(
    private val repository: BookReaderRepository
) {
    suspend fun start(text: String, language: String = "tr-TR", speed: Float = 1.0f, pitch: Float = 1.0f): Resource<Unit> =
        repository.startReading(text, language, speed, pitch)

    suspend fun pause(): Resource<Unit> = repository.pauseReading()
    suspend fun resume(): Resource<Unit> = repository.resumeReading()
    suspend fun stop(): Resource<Unit> = repository.stopReading()
    fun getTtsEvents(): Flow<TtsEvent> = repository.ttsEvents()
}
