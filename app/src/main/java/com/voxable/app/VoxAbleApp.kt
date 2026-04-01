package com.voxable.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.voxable.core_database.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Uygulama giriş noktası.
 *
 * Configuration.Provider uygulanarak HiltWorkerFactory WorkManager'a iletilir.
 * Bu sayede @HiltWorker ile işaretlenmiş SyncWorker Hilt'ten dependency alabilir.
 *
 * AndroidManifest.xml içinde WorkManager'ın otomatik başlatılmasını devre dışı
 * bırakmak GEREKİR (aksi hälde HiltWorkerFactory tanınmaz):
 *   <provider android:name="androidx.startup.InitializationProvider"
 *             tools:node="remove" />
 * VEYA bu Application, Configuration.Provider olarak kayıt edilir —
 * WorkManager bunu algılar ve kendi factory'si yerine HiltWorkerFactory kullanır.
 */
@HiltAndroidApp
class VoxAbleApp : Application(), Configuration.Provider {

    /** HiltWorkerFactory, @HiltWorker ile işaretlenmiş Worker'ları oluşturur */
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var syncScheduler: SyncScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Periyodik sync'i uygulama her başladığında planla.
        // WorkManager KEEP politikası sayesinde zaten planlanmışsa tekrar oluşturulmaz.
        syncScheduler.schedulePeriodicSync()
    }
}
