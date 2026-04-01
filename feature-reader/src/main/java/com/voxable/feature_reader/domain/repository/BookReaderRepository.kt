package com.voxable.feature_reader.domain.repository

import android.net.Uri
import com.voxable.core.util.Resource
import com.voxable.feature_reader.domain.model.BookDocument
import com.voxable.feature_reader.domain.model.Bookmark
import com.voxable.feature_reader.domain.model.ReadingPosition
import com.voxable.feature_reader.domain.model.TtsEvent
import kotlinx.coroutines.flow.Flow

interface BookReaderRepository {
    suspend fun openDocument(uri: Uri): Resource<BookDocument>
    suspend fun getChapterContent(documentId: String, chapterIndex: Int): Resource<String>
    suspend fun startReading(text: String, language: String = "tr", speed: Float = 1.0f, pitch: Float = 1.0f): Resource<Unit>
    suspend fun pauseReading(): Resource<Unit>
    suspend fun resumeReading(): Resource<Unit>
    suspend fun stopReading(): Resource<Unit>
    fun isSpeaking(): Boolean
    fun ttsEvents(): Flow<TtsEvent>
    suspend fun addBookmark(bookmark: Bookmark): Resource<Long>
    suspend fun removeBookmark(bookmarkId: Long): Resource<Unit>
    suspend fun getBookmarks(documentId: String): Resource<List<Bookmark>>
    fun observeBookmarks(documentId: String): Flow<List<Bookmark>>
    suspend fun saveReadingPosition(position: ReadingPosition): Resource<Unit>
    suspend fun getReadingPosition(documentId: String): Resource<ReadingPosition?>
    fun observeReadingPosition(documentId: String): Flow<ReadingPosition?>
}
