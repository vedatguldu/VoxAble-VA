package com.voxable.feature_reader.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE id = :bookmarkId")
    suspend fun delete(bookmarkId: Long)

    @Query("SELECT * FROM bookmarks WHERE documentId = :documentId ORDER BY createdAt DESC")
    suspend fun getByDocumentId(documentId: String): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks WHERE documentId = :documentId ORDER BY createdAt DESC")
    fun observeByDocumentId(documentId: String): Flow<List<BookmarkEntity>>

    @Query("DELETE FROM bookmarks WHERE documentId = :documentId")
    suspend fun deleteAllForDocument(documentId: String)
}

@Dao
interface ReadingPositionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(position: ReadingPositionEntity)

    @Query("SELECT * FROM reading_positions WHERE documentId = :documentId")
    suspend fun getByDocumentId(documentId: String): ReadingPositionEntity?

    @Query("SELECT * FROM reading_positions WHERE documentId = :documentId")
    fun observeByDocumentId(documentId: String): Flow<ReadingPositionEntity?>

    @Query("DELETE FROM reading_positions WHERE documentId = :documentId")
    suspend fun delete(documentId: String)
}

@Dao
interface HighlightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(highlight: HighlightEntity): Long

    @Update
    suspend fun update(highlight: HighlightEntity)

    @Query("DELETE FROM highlights WHERE id = :highlightId")
    suspend fun delete(highlightId: Long)

    @Query("SELECT * FROM highlights WHERE documentId = :documentId ORDER BY chapterIndex, startOffset")
    suspend fun getByDocumentId(documentId: String): List<HighlightEntity>

    @Query("SELECT * FROM highlights WHERE documentId = :documentId AND chapterIndex = :chapterIndex ORDER BY startOffset")
    suspend fun getByChapter(documentId: String, chapterIndex: Int): List<HighlightEntity>

    @Query("SELECT * FROM highlights WHERE documentId = :documentId ORDER BY chapterIndex, startOffset")
    fun observeByDocumentId(documentId: String): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE documentId = :documentId AND chapterIndex = :chapterIndex ORDER BY startOffset")
    fun observeByChapter(documentId: String, chapterIndex: Int): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE documentId = :documentId AND note IS NOT NULL AND note != '' ORDER BY createdAt DESC")
    suspend fun getNotesOnly(documentId: String): List<HighlightEntity>

    @Query("DELETE FROM highlights WHERE documentId = :documentId")
    suspend fun deleteAllForDocument(documentId: String)
}
