package com.voxable.feature_reader.domain.usecase

import com.voxable.core.util.Resource
import com.voxable.feature_reader.domain.model.ReadingPosition
import com.voxable.feature_reader.domain.repository.BookReaderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ResumePositionUseCase @Inject constructor(
    private val repository: BookReaderRepository
) {
    suspend fun save(position: ReadingPosition): Resource<Unit> = repository.saveReadingPosition(position)
    suspend fun get(documentId: String): Resource<ReadingPosition?> = repository.getReadingPosition(documentId)
    fun observe(documentId: String): Flow<ReadingPosition?> = repository.observeReadingPosition(documentId)
}
