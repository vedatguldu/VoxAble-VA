package com.voxable.core_database.dao

import androidx.room.*
import com.voxable.core_database.entity.SyncStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncStatusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(syncStatusEntity: SyncStatusEntity)

    @Query("SELECT * FROM sync_status WHERE entityType = :entityType")
    suspend fun getByType(entityType: String): SyncStatusEntity?

    @Query("SELECT * FROM sync_status WHERE entityType = :entityType")
    fun observe(entityType: String): Flow<SyncStatusEntity?>

    @Query("SELECT * FROM sync_status")
    fun observeAll(): Flow<List<SyncStatusEntity>>

    @Query("UPDATE sync_status SET isSyncing = :isSyncing WHERE entityType = :entityType")
    suspend fun setSyncing(entityType: String, isSyncing: Boolean)

    @Query("UPDATE sync_status SET lastSyncAt = :timestamp, lastSuccessAt = :timestamp, lastError = NULL, isSyncing = 0 WHERE entityType = :entityType")
    suspend fun markSuccess(entityType: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE sync_status SET lastError = :error, isSyncing = 0 WHERE entityType = :entityType")
    suspend fun markError(entityType: String, error: String)

    @Query("UPDATE sync_status SET pendingCount = :count WHERE entityType = :entityType")
    suspend fun updatePendingCount(entityType: String, count: Int)

    @Query("DELETE FROM sync_status")
    suspend fun clearAll()
}
