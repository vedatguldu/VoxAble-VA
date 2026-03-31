package com.voxable.feature_reader.data.repository

import android.content.Context
import android.net.Uri
import com.voxable.core.base.BaseRepository
import com.voxable.core.util.Resource
import com.voxable.feature_reader.data.local.BookmarkDao
import com.voxable.feature_reader.data.local.BookmarkEntity
import com.voxable.feature_reader.data.local.ReadingPositionDao
import com.voxable.feature_reader.data.local.ReadingPositionEntity
import com.voxable.feature_reader.data.parser.DocumentParserFactory
import com.voxable.feature_reader.data.tts.WordTrackingTtsEngine
import com.voxable.feature_reader.domain.model.BookDocument
import com.voxable.feature_reader.domain.model.Bookmark
import com.voxable.feature_reader.domain.model.DocumentFormat
import com.voxable.feature_reader.domain.model.ReadingPosition
import com.voxable.feature_reader.domain.model.TtsEvent
import com.voxable.feature_reader.domain.repository.BookReaderRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookReaderRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val parserFactory: DocumentParserFactory,
    private val ttsEngine: WordTrackingTtsEngine,
    private val bookmarkDao: BookmarkDao,
    private val readingPositionDao: ReadingPositionDao
) : BaseRepository(), BookReaderRepository {

    private var currentDocument: BookDocument? = null

    // ─── Belge ayrıştırma ───────────────────────────────────────────

    override suspend fun openDocument(uri: Uri): Resource<BookDocument> {
        return safeCallOnIo {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("Dosya açılamadı")

            val fileName = getFileNameFromUri(uri) ?: "document"
            val extension = fileName.substringAfterLast('.', "txt")
            val format = DocumentFormat.fromExtension(extension)
                ?: throw Exception("Desteklenmeyen format: .$extension")

            val tempFile = File(context.cacheDir, "reader_${UUID.randomUUID()}.$extension")
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            val parser = parserFactory.getParser(format)
            val document = parser.parse(tempFile, uri.toString())

            currentDocument = document
            document
        }
    }

    override suspend fun getChapterContent(documentId: String, chapterIndex: Int): Resource<String> {
        return safeCall {
            val doc = currentDocument
                ?: throw Exception("Belge yüklenmemiş")
            if (doc.id != documentId) throw Exception("Belge kimliği uyuşmuyor")
            val chapter = doc.chapters.getOrNull(chapterIndex)
                ?: throw Exception("Bölüm bulunamadı: $chapterIndex")
            chapter.content
        }
    }

    // ─── TTS ────────────────────────────────────────────────────────

    override suspend fun startReading(
        text: String, language: String, speed: Float, pitch: Float
    ): Resource<Unit> {
        return safeCall {
            val success = ttsEngine.speak(text, language, speed, pitch)
            if (!success) throw Exception("TTS başlatılamadı")
        }
    }

    override suspend fun pauseReading(): Resource<Unit> {
        return safeCall { ttsEngine.pause() }
    }

    override suspend fun resumeReading(): Resource<Unit> {
        return safeCall { /* caller kalan metni yeniden başlatmalı */ }
    }

    override suspend fun stopReading(): Resource<Unit> {
        return safeCall { ttsEngine.stop() }
    }

    override fun isSpeaking(): Boolean = ttsEngine.isSpeaking()

    override fun ttsEvents(): Flow<TtsEvent> = ttsEngine.ttsEvents

    // ─── Yer imleri ─────────────────────────────────────────────────

    override suspend fun addBookmark(bookmark: Bookmark): Resource<Long> {
        return safeCallOnIo {
            val entity = BookmarkEntity(
                documentId = bookmark.documentId,
                title = bookmark.title,
                chapterIndex = bookmark.chapterIndex,
                pageIndex = bookmark.pageIndex,
                characterOffset = bookmark.characterOffset,
                note = bookmark.note,
                createdAt = bookmark.createdAt
            )
            bookmarkDao.insert(entity)
        }
    }

    override suspend fun removeBookmark(bookmarkId: Long): Resource<Unit> {
        return safeCallOnIo { bookmarkDao.delete(bookmarkId) }
    }

    override suspend fun getBookmarks(documentId: String): Resource<List<Bookmark>> {
        return safeCallOnIo {
            bookmarkDao.getByDocumentId(documentId).map { it.toDomain() }
        }
    }

    override fun observeBookmarks(documentId: String): Flow<List<Bookmark>> {
        return bookmarkDao.observeByDocumentId(documentId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // ─── Okuma konumu ───────────────────────────────────────────────

    override suspend fun saveReadingPosition(position: ReadingPosition): Resource<Unit> {
        return safeCallOnIo {
            val entity = ReadingPositionEntity(
                documentId = position.documentId,
                chapterIndex = position.chapterIndex,
                pageIndex = position.pageIndex,
                characterOffset = position.characterOffset,
                progressPercent = position.progressPercent,
                updatedAt = position.updatedAt
            )
            readingPositionDao.upsert(entity)
        }
    }

    override suspend fun getReadingPosition(documentId: String): Resource<ReadingPosition?> {
        return safeCallOnIo {
            readingPositionDao.getByDocumentId(documentId)?.toDomain()
        }
    }

    override fun observeReadingPosition(documentId: String): Flow<ReadingPosition?> {
        return readingPositionDao.observeByDocumentId(documentId).map { it?.toDomain() }
    }

    // ─── Yardımcılar ────────────────────────────────────────────────

    private fun getFileNameFromUri(uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) it.getString(nameIndex) else null
            } else null
        }
    }

    private fun BookmarkEntity.toDomain() = Bookmark(
        id = id,
        documentId = documentId,
        title = title,
        chapterIndex = chapterIndex,
        pageIndex = pageIndex,
        characterOffset = characterOffset,
        note = note,
        createdAt = createdAt
    )

    private fun ReadingPositionEntity.toDomain() = ReadingPosition(
        documentId = documentId,
        chapterIndex = chapterIndex,
        pageIndex = pageIndex,
        characterOffset = characterOffset,
        progressPercent = progressPercent,
        updatedAt = updatedAt
    )
}
