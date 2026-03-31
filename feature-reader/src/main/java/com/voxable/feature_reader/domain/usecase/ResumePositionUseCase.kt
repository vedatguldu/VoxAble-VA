package com.voxable.feature_reader.domain.usecase

import com.voxable.feature_reader.domain.model.ReadingPosition
import com.voxable.feature_reader.domain.repository.BookReaderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ResumePositionUseCase @Inject constructor(
    private val repository: BookReaderRepository
) {
    suspend fun save(position: ReadingPosition) = repository.saveReadingPosition(position)
    fun get(documentId: String): Flow<ReadingPosition?> = repository.getReadingPosition(documentId)
}
