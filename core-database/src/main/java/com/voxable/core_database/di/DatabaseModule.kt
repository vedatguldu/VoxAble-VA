package com.voxable.core_database.di

import android.content.Context
import androidx.room.Room
import com.voxable.core.util.Constants
import com.voxable.core_database.VoxAbleDatabase
import com.voxable.core_database.dao.DownloadDao
import com.voxable.core_database.dao.SyncQueueDao
import com.voxable.core_database.dao.SyncStatusDao
import com.voxable.core_database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): VoxAbleDatabase {
        return Room.databaseBuilder(
            context,
            VoxAbleDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .addMigrations(VoxAbleDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: VoxAbleDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideDownloadDao(database: VoxAbleDatabase): DownloadDao = database.downloadDao()

    @Provides
    @Singleton
    fun provideSyncQueueDao(database: VoxAbleDatabase): SyncQueueDao = database.syncQueueDao()

    @Provides
    @Singleton
    fun provideSyncStatusDao(database: VoxAbleDatabase): SyncStatusDao = database.syncStatusDao()
}
