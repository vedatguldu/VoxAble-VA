package com.voxable.core_database.repository

import com.voxable.core_database.entity.SyncQueueEntity
import com.voxable.core_database.entity.SyncStatusEntity
import com.voxable.core_database.sync.ConflictResolutionStrategy
import com.voxable.core_database.sync.SyncOperation
import com.voxable.core_database.sync.SyncResult
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first sync sisteminin birincil giriş noktası.
 *
 * ViewModel'ler veya diğer repository'ler aracılığıyla:
 * - Yerel değişiklikleri kuyruğa ekleyebilir
 * - Sync tetikleyebilir
 * - Sync durumunu gözlemleyebilir
 */
interface SyncRepository {

    // ─── Gözlemleme ───────────────────────────────────────

    /** Tüm aktif (PENDING/IN_PROGRESS/FAILED) işlemleri gözlemler */
    val activeQueueFlow: Flow<List<SyncQueueEntity>>

    /** Toplam bekleyen işlem sayısı */
    val pendingCountFlow: Flow<Int>

    /** Çakışan işlem sayısı */
    val conflictCountFlow: Flow<Int>

    /** Tüm entity türleri için sync durumu */
    val allSyncStatusFlow: Flow<List<SyncStatusEntity>>

    /** Belirtilen entity türü için sync durumu */
    fun syncStatusFlow(entityType: String): Flow<SyncStatusEntity?>

    // ─── Kuyruğa Ekleme ───────────────────────────────────

    /**
     * Bir sync işlemini kuyruğa ekler.
     *
     * @param entityType    "user", "download", "settings" vb.
     * @param entityId      Entity'nin birincil anahtarı (String)
     * @param operation     INSERT, UPDATE veya DELETE
     * @param payload       JSON string — entity verisi
     * @param strategy      Çakışma çözüm stratejisi
     * @return Kuyruktaki kaydın ID'si
     */
    suspend fun enqueue(
        entityType: String,
        entityId: String,
        operation: SyncOperation,
        payload: Map<String, Any?>,
        strategy: ConflictResolutionStrategy = ConflictResolutionStrategy.LATEST_WINS
    ): Long

    // ─── Sync Tetikleme ───────────────────────────────────

    /**
     * Tüm bekleyen işlemleri şimdi sync et.
     * Ağ yoksa NoConnectionAvailable döner.
     */
    suspend fun syncNow(): SyncResult

    /**
     * Belirli bir entity türü için hem push hem pull yapar.
     */
    suspend fun syncEntityType(entityType: String): SyncResult

    /**
     * Firestore'dan değişiklikleri çek ve yerel DB'yi güncelle.
     * Pull, push'tan sonra önce yapılır (server-ahead policy).
     */
    suspend fun pullFromServer(entityType: String, afterTimestamp: Long = 0L): SyncResult

    // ─── İptal / Yeniden Deneme ───────────────────────────

    /** Başarısız tüm işlemleri yeniden kuyruğa al */
    suspend fun retryFailed()

    /** Belirli bir işlemi yeniden dene */
    suspend fun retryById(id: Long)

    /** Tamamlanmış işlemleri kuyruğu temizle */
    suspend fun clearCompleted()

    /** Belirli entity için bekleyen tüm işlemleri iptal et */
    suspend fun cancelForEntity(entityType: String, entityId: String)

    /** Tüm sync verisini sıfırla (logout gibi durumlarda) */
    suspend fun clearAllSync()
}
