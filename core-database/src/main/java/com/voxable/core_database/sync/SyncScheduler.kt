package com.voxable.core_database.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.voxable.core.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WorkManager aracılığıyla periyodik ve anlık sync işlemlerini planlar.
 *
 * schedulePeriodicSync() → 15 dakikada bir, yalnızca ağ varken çalışır.
 * triggerImmediateSync()  → Hemen çalıştırmak üzere tek seferlik iş kuyruğa alır.
 *
 * VoxAbleApp.onCreate() içinde schedulePeriodicSync() çağrılmalıdır.
 */
@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    // ─── Periyodik Sync ───────────────────────────────────

    /**
     * Ağ bağlantısı gereksinimi ile 15 dakikada bir çalışan periyodik iş planlar.
     * Zaten planlanmışsa KEEP politikası nedeniyle yeniden oluşturulmaz.
     */
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = Constants.Sync.SYNC_INTERVAL_MINUTES,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                Constants.Sync.INITIAL_BACKOFF_SECONDS,
                TimeUnit.SECONDS
            )
            .addTag(TAG_SYNC)
            .build()

        workManager.enqueueUniquePeriodicWork(
            Constants.Sync.SYNC_WORKER_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    // ─── Anlık Sync ───────────────────────────────────────

    /**
     * Şu anda ağ varsa hemen, ağ yoksa bağlantı sağlanır sağlanmaz çalışacak
     * tek seferlik sync işi planlar.
     *
     * Zaten bekleyen bir anlık sync varsa REPLACE ile değiştirilir.
     */
    fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                Constants.Sync.INITIAL_BACKOFF_SECONDS,
                TimeUnit.SECONDS
            )
            .addTag(TAG_SYNC)
            .build()

        workManager.enqueueUniqueWork(
            Constants.Sync.IMMEDIATE_SYNC_WORKER_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    // ─── İptal ───────────────────────────────────────────

    /** Periyodik sync işini iptal eder */
    fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(Constants.Sync.SYNC_WORKER_NAME)
    }

    /** Tüm sync işlerini (periyodik + anlık) iptal eder */
    fun cancelAllSync() {
        workManager.cancelAllWorkByTag(TAG_SYNC)
    }

    // ─── Durum Gözlemleme ─────────────────────────────────

    /** Periyodik sync çalışma durumunu gerçek zamanlı gözlemler */
    fun observePeriodicSyncState(): Flow<WorkInfo?> =
        workManager
            .getWorkInfosForUniqueWorkFlow(Constants.Sync.SYNC_WORKER_NAME)
            .map { it.firstOrNull() }

    /** Anlık sync çalışma durumunu gerçek zamanlı gözlemler */
    fun observeImmediateSyncState(): Flow<WorkInfo?> =
        workManager
            .getWorkInfosForUniqueWorkFlow(Constants.Sync.IMMEDIATE_SYNC_WORKER_NAME)
            .map { it.firstOrNull() }

    companion object {
        const val TAG_SYNC = "voxable_sync"
    }
}
