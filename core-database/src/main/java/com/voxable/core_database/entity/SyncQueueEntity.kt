package com.voxable.core_database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.voxable.core_database.sync.ConflictResolutionStrategy
import com.voxable.core_database.sync.SyncOperation
import com.voxable.core_database.sync.SyncQueueStatus

/**
 * Yerel yazma işlemlerini sıralayan sync kuyruğu.
 *
 * Her kayıt, uygulamanın Firestore'a göndermesi gereken
 * bir INSERT/UPDATE/DELETE işlemini temsil eder.
 * İş başarısız olursa exponential backoff ile yeniden denenir.
 */
@Entity(
    tableName = "sync_queue",
    indices = [
        Index(value = ["entityType", "entityId"]),
        Index(value = ["syncStatus"]),
        Index(value = ["nextRetryAt"])
    ]
)
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Entity türü: "user", "download", "settings" */
    val entityType: String,

    /** Sync edilecek entity'nin birincil anahtarı */
    val entityId: String,

    /** Yapılacak işlem: INSERT, UPDATE, DELETE */
    val operation: String,

    /** JSON string olarak serileştirilmiş entity verisi */
    val payload: String,

    /** Mevcut durum */
    val syncStatus: String = SyncQueueStatus.PENDING.name,

    /** Kaç kez denendiği */
    val retryCount: Int = 0,

    /** Maksimum deneme sayısı */
    val maxRetries: Int = 3,

    /** Son deneme zamanı (epoch ms) */
    val lastAttemptAt: Long? = null,

    /** Bir sonraki deneme için bekleme zamanı (epoch ms) */
    val nextRetryAt: Long? = null,

    /** Başarısız olursa hata mesajı */
    val errorMessage: String? = null,

    /** Çakışma çözüm stratejisi */
    val conflictResolution: String = ConflictResolutionStrategy.LATEST_WINS.name,

    /**
     * Optimistic locking için versiyon numarası.
     * Sunucu bu değeri saklıyorsa ve client daha küçük bir value gönderirse
     * → CONFLICT olarak işaretlenir.
     */
    val version: Long = System.currentTimeMillis(),

    val createdAt: Long = System.currentTimeMillis()
)
