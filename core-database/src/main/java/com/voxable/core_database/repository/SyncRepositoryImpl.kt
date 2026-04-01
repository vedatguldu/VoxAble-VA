package com.voxable.core_database.repository

import com.voxable.core.util.Constants
import com.voxable.core.util.NetworkMonitor
import com.voxable.core_database.dao.SyncQueueDao
import com.voxable.core_database.dao.SyncStatusDao
import com.voxable.core_database.entity.SyncQueueEntity
import com.voxable.core_database.entity.SyncStatusEntity
import com.voxable.core_database.sync.ConflictResolutionStrategy
import com.voxable.core_database.sync.FirestorePullResult
import com.voxable.core_database.sync.FirestorePushResult
import com.voxable.core_database.sync.FirestoreSyncManager
import com.voxable.core_database.sync.SyncOperation
import com.voxable.core_database.sync.SyncQueueStatus
import com.voxable.core_database.sync.SyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/**
 * SyncRepository'nin tam uygulaması.
 *
 * - NetworkMonitor ile ağ durumunu kontrol eder
 * - SyncQueueDao üzerinden işlemleri okur / günceller
 * - FirestoreSyncManager aracılığıyla Firestore'a push/pull yapar
 * - Başarısız işlemler için exponential backoff hesaplar
 */
@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val syncStatusDao: SyncStatusDao,
    private val firestoreSyncManager: FirestoreSyncManager,
    private val networkMonitor: NetworkMonitor
) : SyncRepository {

    /**
     * Eş zamanlı sync çakışmasını önlemek için flag.
     * compareAndSet garantisi ile thread-safe.
     */
    private val isSyncing = AtomicBoolean(false)

    // ─── Gözlemleme ───────────────────────────────────────

    override val activeQueueFlow: Flow<List<SyncQueueEntity>> =
        syncQueueDao.observeActiveQueue()

    override val pendingCountFlow: Flow<Int> =
        syncQueueDao.observePendingCount()

    override val conflictCountFlow: Flow<Int> =
        syncQueueDao.observeConflictCount()

    override val allSyncStatusFlow: Flow<List<SyncStatusEntity>> =
        syncStatusDao.observeAll()

    override fun syncStatusFlow(entityType: String): Flow<SyncStatusEntity?> =
        syncStatusDao.observe(entityType)

    // ─── Kuyruğa Ekleme ───────────────────────────────────

    override suspend fun enqueue(
        entityType: String,
        entityId: String,
        operation: SyncOperation,
        payload: Map<String, Any?>,
        strategy: ConflictResolutionStrategy
    ): Long {
        val jsonPayload = firestoreSyncManager.serializePayload(payload)
        val entity = SyncQueueEntity(
            entityType = entityType,
            entityId = entityId,
            operation = operation.name,
            payload = jsonPayload,
            conflictResolution = strategy.name,
            version = System.currentTimeMillis()
        )
        val id = syncQueueDao.enqueue(entity)

        // Sync status kaydı henüz yoksa oluştur
        if (syncStatusDao.getByType(entityType) == null) {
            syncStatusDao.upsert(SyncStatusEntity(entityType = entityType))
        }

        return id
    }

    // ─── Sync Tetikleme ───────────────────────────────────

    override suspend fun syncNow(): SyncResult {
        if (!networkMonitor.isOnline.first()) {
            return SyncResult.NoConnectionAvailable
        }

        if (!isSyncing.compareAndSet(false, true)) {
            return SyncResult.AlreadySyncing
        }

        return try {
            val readyItems = syncQueueDao.getReadyToSync(now = System.currentTimeMillis())
            if (readyItems.isEmpty()) {
                return SyncResult.NothingToSync
            }

            var syncedCount = 0
            var failedCount = 0
            var conflictCount = 0
            val errors = mutableListOf<String>()

            for (item in readyItems) {
                syncQueueDao.markInProgress(item.id)
                syncStatusDao.setSyncing(item.entityType, true)

                when (val result = processQueueItem(item)) {
                    is ItemSyncResult.Success -> {
                        syncQueueDao.markCompleted(item.id)
                        syncStatusDao.markSuccess(item.entityType)
                        syncedCount++
                    }
                    is ItemSyncResult.Conflict -> {
                        syncQueueDao.markConflict(item.id, result.info)
                        syncStatusDao.setSyncing(item.entityType, false)
                        conflictCount++
                    }
                    is ItemSyncResult.Failed -> {
                        val nextRetry = calculateNextRetryAt(item.retryCount)
                        syncQueueDao.markFailed(
                            id = item.id,
                            now = System.currentTimeMillis(),
                            nextRetryAt = nextRetry,
                            errorMessage = result.error
                        )
                        syncStatusDao.markError(item.entityType, result.error)
                        failedCount++
                        errors += result.error
                    }
                }
            }

            when {
                failedCount == 0 && conflictCount == 0 ->
                    SyncResult.Success(syncedCount = syncedCount)
                syncedCount > 0 ->
                    SyncResult.PartialSuccess(syncedCount, failedCount, conflictCount, errors)
                else ->
                    SyncResult.Failure("$failedCount işlem başarısız oldu", retryable = true)
            }
        } finally {
            isSyncing.set(false)
        }
    }

    override suspend fun syncEntityType(entityType: String): SyncResult {
        if (!networkMonitor.isOnline.first()) return SyncResult.NoConnectionAvailable

        val items = syncQueueDao.getByStatus(SyncQueueStatus.PENDING.name)
            .filter { it.entityType == entityType }
        if (items.isEmpty()) return SyncResult.NothingToSync

        var syncedCount = 0
        var failedCount = 0
        val errors = mutableListOf<String>()

        for (item in items) {
            syncQueueDao.markInProgress(item.id)
            when (val result = processQueueItem(item)) {
                is ItemSyncResult.Success -> {
                    syncQueueDao.markCompleted(item.id)
                    syncedCount++
                }
                is ItemSyncResult.Conflict ->
                    syncQueueDao.markConflict(item.id, result.info)
                is ItemSyncResult.Failed -> {
                    val nextRetry = calculateNextRetryAt(item.retryCount)
                    syncQueueDao.markFailed(
                        id = item.id,
                        now = System.currentTimeMillis(),
                        nextRetryAt = nextRetry,
                        errorMessage = result.error
                    )
                    errors += result.error
                    failedCount++
                }
            }
        }

        return if (failedCount == 0) SyncResult.Success(syncedCount)
        else SyncResult.PartialSuccess(syncedCount, failedCount, errors = errors)
    }

    override suspend fun pullFromServer(entityType: String, afterTimestamp: Long): SyncResult {
        if (!networkMonitor.isOnline.first()) return SyncResult.NoConnectionAvailable

        val collection = entityTypeToCollection(entityType)
        val since = if (afterTimestamp > 0L) afterTimestamp
        else syncStatusDao.getByType(entityType)?.lastSyncAt ?: 0L

        return when (val result = firestoreSyncManager.pullCollection(collection, since)) {
            is FirestorePullResult.Success -> {
                syncStatusDao.markSuccess(entityType)
                SyncResult.Success(syncedCount = 0, pulledCount = result.documents.size)
            }
            is FirestorePullResult.NetworkError -> SyncResult.NoConnectionAvailable
            is FirestorePullResult.Failure -> SyncResult.Failure(result.message)
            is FirestorePullResult.NotAuthenticated ->
                SyncResult.Failure("Kimlik doğrulama gerekli", retryable = false)
        }
    }

    // ─── İptal / Yeniden Deneme ───────────────────────────

    override suspend fun retryFailed() = syncQueueDao.resetAllFailed()

    override suspend fun retryById(id: Long) = syncQueueDao.resetForRetry(id)

    override suspend fun clearCompleted() = syncQueueDao.clearCompleted()

    override suspend fun cancelForEntity(entityType: String, entityId: String) =
        syncQueueDao.cancelForEntity(entityType, entityId)

    override suspend fun clearAllSync() {
        syncQueueDao.clearAll()
        syncStatusDao.clearAll()
    }

    // ─── Özel yardımcılar ─────────────────────────────────

    /**
     * Tek bir sync kuyruğu kaydını işler.
     * DELETE için deleteDocument, diğerleri için pushDocument çağırır.
     */
    private suspend fun processQueueItem(item: SyncQueueEntity): ItemSyncResult {
        val data = firestoreSyncManager.deserializePayload(item.payload)
        val collection = entityTypeToCollection(item.entityType)
        val strategy = ConflictResolutionStrategy.valueOf(item.conflictResolution)

        // Maksimum deneme sayısını geç → kalıcı hata
        if (item.retryCount >= item.maxRetries) {
            return ItemSyncResult.Failed("Maksimum deneme sayısı (${item.maxRetries}) aşıldı")
        }

        return when (item.operation) {
            SyncOperation.DELETE.name -> {
                when (val r = firestoreSyncManager.deleteDocument(collection, item.entityId)) {
                    FirestorePushResult.Success -> ItemSyncResult.Success
                    is FirestorePushResult.NetworkError -> ItemSyncResult.Failed(r.message)
                    is FirestorePushResult.Failure -> ItemSyncResult.Failed(r.message)
                    FirestorePushResult.NotAuthenticated ->
                        ItemSyncResult.Failed("Kimlik doğrulama gerekli")
                    is FirestorePushResult.Conflict ->
                        ItemSyncResult.Conflict(r.serverData ?: "Silinecek belge zaten değişmiş")
                }
            }
            else -> {
                when (val r = firestoreSyncManager.pushDocument(
                    collection = collection,
                    documentId = item.entityId,
                    data = data,
                    clientVersion = item.version,
                    conflictResolution = strategy
                )) {
                    FirestorePushResult.Success -> ItemSyncResult.Success
                    is FirestorePushResult.NetworkError -> ItemSyncResult.Failed(r.message)
                    is FirestorePushResult.Failure -> ItemSyncResult.Failed(r.message)
                    FirestorePushResult.NotAuthenticated ->
                        ItemSyncResult.Failed("Kimlik doğrulama gerekli")
                    is FirestorePushResult.Conflict ->
                        ItemSyncResult.Conflict(r.serverData ?: "Sunucu çakışması")
                }
            }
        }
    }

    /** Entity türünü Firestore koleksiyonu adına çevirir */
    private fun entityTypeToCollection(entityType: String): String = when (entityType) {
        Constants.Sync.ENTITY_USER -> Constants.Firebase.USERS_COLLECTION
        Constants.Sync.ENTITY_DOWNLOAD -> Constants.Firebase.DOWNLOADS_COLLECTION
        Constants.Sync.ENTITY_SETTINGS -> Constants.Firebase.SETTINGS_COLLECTION
        else -> entityType
    }

    /**
     * Exponential backoff ile sonraki deneme zamanını hesaplar:
     * nextRetryAt = now + INITIAL_BACKOFF_SECONDS * 2^retryCount (en fazla 1 saat)
     */
    private fun calculateNextRetryAt(retryCount: Int): Long {
        val backoffSeconds = Constants.Sync.INITIAL_BACKOFF_SECONDS *
            2.0.pow(retryCount.toDouble()).toLong().coerceAtMost(3600L)
        return System.currentTimeMillis() + backoffSeconds * 1000L
    }

    // ─── İç sonuç tipi ────────────────────────────────────

    private sealed class ItemSyncResult {
        data object Success : ItemSyncResult()
        data class Conflict(val info: String) : ItemSyncResult()
        data class Failed(val error: String) : ItemSyncResult()
    }
}
