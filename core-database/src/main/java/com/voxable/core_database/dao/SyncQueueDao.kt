package com.voxable.core_database.dao

import androidx.room.*
import com.voxable.core_database.entity.SyncQueueEntity
import com.voxable.core_database.sync.SyncQueueStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {

    // ─── Ekleme ───────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(syncQueueEntity: SyncQueueEntity): Long

    // ─── Sorgulama ────────────────────────────────────────

    @Query("SELECT * FROM sync_queue WHERE syncStatus = :status ORDER BY createdAt ASC")
    suspend fun getByStatus(status: String = SyncQueueStatus.PENDING.name): List<SyncQueueEntity>

    /**
     * Şu an işlenmeye hazır olan kayıtları döndürür.
     * nextRetryAt NULL ise (hiç denenmedi) veya geçmişte kalmışsa dahil edilir.
     */
    @Query("""
        SELECT * FROM sync_queue 
        WHERE syncStatus IN ('PENDING', 'FAILED') 
          AND (nextRetryAt IS NULL OR nextRetryAt <= :now)
          AND retryCount < maxRetries
        ORDER BY createdAt ASC
        LIMIT :limit
    """)
    suspend fun getReadyToSync(now: Long = System.currentTimeMillis(), limit: Int = 50): List<SyncQueueEntity>

    @Query("SELECT * FROM sync_queue WHERE entityType = :entityType AND entityId = :entityId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestForEntity(entityType: String, entityId: String): SyncQueueEntity?

    @Query("SELECT * FROM sync_queue WHERE syncStatus = 'CONFLICT'")
    fun observeConflicts(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE syncStatus NOT IN ('COMPLETED', 'CANCELLED') ORDER BY createdAt DESC")
    fun observeActiveQueue(): Flow<List<SyncQueueEntity>>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE syncStatus IN ('PENDING', 'IN_PROGRESS', 'FAILED')")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE syncStatus = 'CONFLICT'")
    fun observeConflictCount(): Flow<Int>

    // ─── Güncelleme ───────────────────────────────────────

    @Update
    suspend fun update(syncQueueEntity: SyncQueueEntity)

    @Query("UPDATE sync_queue SET syncStatus = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("""
        UPDATE sync_queue 
        SET syncStatus = :status,
            retryCount = retryCount + 1,
            lastAttemptAt = :now,
            nextRetryAt = :nextRetryAt,
            errorMessage = :errorMessage
        WHERE id = :id
    """)
    suspend fun markFailed(
        id: Long,
        status: String = SyncQueueStatus.FAILED.name,
        now: Long = System.currentTimeMillis(),
        nextRetryAt: Long,
        errorMessage: String?
    )

    @Query("UPDATE sync_queue SET syncStatus = 'IN_PROGRESS', lastAttemptAt = :now WHERE id = :id")
    suspend fun markInProgress(id: Long, now: Long = System.currentTimeMillis())

    @Query("UPDATE sync_queue SET syncStatus = 'COMPLETED' WHERE id = :id")
    suspend fun markCompleted(id: Long)

    @Query("UPDATE sync_queue SET syncStatus = 'CONFLICT', errorMessage = :conflictInfo WHERE id = :id")
    suspend fun markConflict(id: Long, conflictInfo: String)

    // ─── Silme ────────────────────────────────────────────

    @Query("DELETE FROM sync_queue WHERE syncStatus = 'COMPLETED'")
    suspend fun clearCompleted()

    @Query("DELETE FROM sync_queue WHERE syncStatus = 'CANCELLED'")
    suspend fun clearCancelled()

    @Query("DELETE FROM sync_queue WHERE entityType = :entityType AND entityId = :entityId")
    suspend fun cancelForEntity(entityType: String, entityId: String)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM sync_queue")
    suspend fun clearAll()

    // ─── Yeniden deneme ───────────────────────────────────

    @Query("UPDATE sync_queue SET syncStatus = 'PENDING', retryCount = 0, nextRetryAt = NULL, errorMessage = NULL WHERE syncStatus = 'FAILED'")
    suspend fun resetAllFailed()

    @Query("UPDATE sync_queue SET syncStatus = 'PENDING', retryCount = 0, nextRetryAt = NULL, errorMessage = NULL WHERE id = :id")
    suspend fun resetForRetry(id: Long)
}
