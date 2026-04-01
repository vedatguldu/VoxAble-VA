package com.voxable.core_database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Her entity türü için son sync zamanını ve durumunu tutar.
 *
 * Bu tablo, "son sync'ten bu yana neler değişti?" sorusunu
 * yanıtlamak için delta pull işlemlerinde kullanılır.
 */
@Entity(tableName = "sync_status")
data class SyncStatusEntity(
    @PrimaryKey
    val entityType: String,

    /** Son başarılı sync zamanı (epoch ms). Delta pull bu değeri kullanır. */
    val lastSyncAt: Long = 0L,

    /** Son başarılı pull/push tamamlanma zamanı (epoch ms) */
    val lastSuccessAt: Long = 0L,

    /** Son hata mesajı (varsa) */
    val lastError: String? = null,

    /** Şu an kuyruktaki bekleyen işlem sayısı */
    val pendingCount: Int = 0,

    /** Bu entity türü için sync devam ediyor mu? */
    val isSyncing: Boolean = false
)
