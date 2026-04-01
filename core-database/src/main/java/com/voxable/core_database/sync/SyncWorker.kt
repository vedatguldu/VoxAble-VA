package com.voxable.core_database.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.voxable.core_database.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager tarafından çalıştırılan arka plan sync worker'ı.
 *
 * @HiltWorker + @AssistedInject ile Hilt dependency injection destekler.
 * Ağ kısıtlı (CONNECTED) olarak planlanır; WorkManager backoff ile yeniden dener.
 *
 * Sonuç:
 *  - Result.success → tüm işlemler tamamlandı veya yapacak iş yoktu
 *  - Result.retry  → geçici hata (ağ, kimlik doğrulama), WorkManager exponential backoff uygular
 *  - Result.failure → kalıcı hata veya maksimum deneme sayısı aşıldı
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            when (val result = syncRepository.syncNow()) {
                is SyncResult.Success -> {
                    Result.success(
                        workDataOf(
                            OUTPUT_SYNCED_COUNT to result.syncedCount,
                            OUTPUT_PULLED_COUNT to result.pulledCount
                        )
                    )
                }

                is SyncResult.PartialSuccess -> {
                    // Bazı işlemler başarısız → WorkManager retry
                    if (result.failedCount > 0 && runAttemptCount < MAX_RUN_ATTEMPTS) {
                        Result.retry()
                    } else {
                        Result.success(
                            workDataOf(
                                OUTPUT_SYNCED_COUNT to result.syncedCount,
                                OUTPUT_FAILED_COUNT to result.failedCount
                            )
                        )
                    }
                }

                is SyncResult.Failure -> {
                    if (result.retryable && runAttemptCount < MAX_RUN_ATTEMPTS) {
                        Result.retry()
                    } else {
                        Result.failure(workDataOf(OUTPUT_ERROR to result.error))
                    }
                }

                // Ağ yoksa WorkManager kendi kısıtını bekler
                SyncResult.NoConnectionAvailable -> Result.retry()

                // Başka iş zaten çalışıyor → sonuç olumlu say (yarış yok)
                SyncResult.AlreadySyncing -> Result.success()

                // Yapacak iş yok
                SyncResult.NothingToSync -> Result.success()
            }
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RUN_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure(workDataOf(OUTPUT_ERROR to (e.message ?: "Bilinmeyen hata")))
            }
        }
    }

    companion object {
        /** WorkManager'ın kendi retry sayacını bu limitin altında tutuyoruz */
        private const val MAX_RUN_ATTEMPTS = 3

        // Output data keys
        const val OUTPUT_SYNCED_COUNT = "synced_count"
        const val OUTPUT_PULLED_COUNT = "pulled_count"
        const val OUTPUT_FAILED_COUNT = "failed_count"
        const val OUTPUT_ERROR = "error"
    }
}
