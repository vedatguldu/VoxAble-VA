package com.voxable.core_database.di

import android.content.Context
import androidx.room.Room
import com.voxable.core.util.Constants
import com.voxable.core_database.VoxAbleDatabase
import com.voxable.core_database.dao.DownloadDao
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
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: VoxAbleDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideDownloadDao(database: VoxAbleDatabase): DownloadDao {
        return database.downloadDao()
    }
}
