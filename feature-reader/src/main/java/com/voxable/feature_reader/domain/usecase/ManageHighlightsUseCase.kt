package com.voxable.feature_reader.domain.usecase

import com.voxable.feature_reader.data.local.HighlightDao
import com.voxable.feature_reader.data.local.HighlightEntity
import com.voxable.feature_reader.domain.model.Highlight
import com.voxable.feature_reader.domain.model.HighlightColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ManageHighlightsUseCase @Inject constructor(
    private val highlightDao: HighlightDao
) {
    suspend fun addHighlight(highlight: Highlight): Long {
        return highlightDao.insert(highlight.toEntity())
    }

    suspend fun updateHighlight(highlight: Highlight) {
        highlightDao.update(highlight.toEntity())
    }

    suspend fun deleteHighlight(highlightId: Long) {
        highlightDao.delete(highlightId)
    }

    suspend fun getHighlightsForDocument(documentId: String): List<Highlight> {
        return highlightDao.getByDocumentId(documentId).map { it.toDomain() }
    }

    suspend fun getHighlightsForChapter(documentId: String, chapterIndex: Int): List<Highlight> {
        return highlightDao.getByChapter(documentId, chapterIndex).map { it.toDomain() }
    }

    fun observeHighlightsForDocument(documentId: String): Flow<List<Highlight>> {
        return highlightDao.observeByDocumentId(documentId).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun observeHighlightsForChapter(documentId: String, chapterIndex: Int): Flow<List<Highlight>> {
        return highlightDao.observeByChapter(documentId, chapterIndex).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getNotesForDocument(documentId: String): List<Highlight> {
        return highlightDao.getNotesOnly(documentId).map { it.toDomain() }
    }

    suspend fun deleteAllForDocument(documentId: String) {
        highlightDao.deleteAllForDocument(documentId)
    }

    private fun Highlight.toEntity() = HighlightEntity(
        id = id,
        documentId = documentId,
        chapterIndex = chapterIndex,
        startOffset = startOffset,
        endOffset = endOffset,
        highlightedText = highlightedText,
        color = color.argb,
        note = note,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis()
    )

    private fun HighlightEntity.toDomain() = Highlight(
        id = id,
        documentId = documentId,
        chapterIndex = chapterIndex,
        startOffset = startOffset,
        endOffset = endOffset,
        highlightedText = highlightedText,
        color = HighlightColor.fromArgb(color),
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
