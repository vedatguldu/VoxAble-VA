package com.voxable.feature_reader.domain.repository

import android.net.Uri
import com.voxable.feature_reader.domain.model.BookDocument
import com.voxable.feature_reader.domain.model.Bookmark
import com.voxable.feature_reader.domain.model.ReadingPosition
import com.voxable.feature_reader.domain.model.TtsEvent
import kotlinx.coroutines.flow.Flow

interface BookReaderRepository {
    suspend fun openDocument(uri: Uri): BookDocument
    suspend fun startReading(text: String, language: String, speed: Float, pitch: Float)
    suspend fun pauseReading()
    suspend fun resumeReading()
    suspend fun stopReading()
    fun getTtsEvents(): Flow<TtsEvent>
    suspend fun addBookmark(bookmark: Bookmark)
    suspend fun removeBookmark(bookmarkId: Long)
    fun getBookmarks(documentId: String): Flow<List<Bookmark>>
    suspend fun saveReadingPosition(position: ReadingPosition)
    fun getReadingPosition(documentId: String): Flow<ReadingPosition?>
}
