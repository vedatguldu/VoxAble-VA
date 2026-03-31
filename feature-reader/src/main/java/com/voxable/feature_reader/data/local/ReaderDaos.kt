package com.voxable.feature_reader.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
