package com.voxable.core_database.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.voxable.core_database.repository.SyncRepository
import com.voxable.core_database.repository.SyncRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Sync altyapısını Hilt'e kayıt eden modül.
 *
 * - FirebaseFirestore ve FirebaseAuth singleton olarak sağlanır.
 * - SyncRepository arayüzü SyncRepositoryImpl'e bağlanır.
 *
 * Not: WorkManager–Hilt entegrasyonu için HiltWorkerFactory,
 * VoxAbleApp.workManagerConfiguration içinde yapılandırılır.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    /** SyncRepository arayüzünü SyncRepositoryImpl'e bağlar */
    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository

    companion object {

        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore =
            FirebaseFirestore.getInstance()

        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth =
            FirebaseAuth.getInstance()
    }
}
