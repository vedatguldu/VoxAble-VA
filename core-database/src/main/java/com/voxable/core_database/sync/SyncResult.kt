package com.voxable.core_database.sync

/**
 * Bir sync çalıştırmasının sonucunu temsil eder.
 */
sealed class SyncResult {

    /** Tüm işlemler başarıyla tamamlandı */
    data class Success(
        val syncedCount: Int,
        val pulledCount: Int = 0
    ) : SyncResult()

    /** Bazı işlemler başarılı, bazıları başarısız */
    data class PartialSuccess(
        val syncedCount: Int,
        val failedCount: Int,
        val conflictCount: Int = 0,
        val errors: List<String> = emptyList()
    ) : SyncResult()

    /** Sync tamamen başarısız */
    data class Failure(
        val error: String,
        val retryable: Boolean = true
    ) : SyncResult()

    /** Ağ bağlantısı yok, sync ertelendi */
    data object NoConnectionAvailable : SyncResult()

    /** Başka bir sync zaten çalışıyor */
    data object AlreadySyncing : SyncResult()

    /** Bekleyen işlem yok */
    data object NothingToSync : SyncResult()
}
