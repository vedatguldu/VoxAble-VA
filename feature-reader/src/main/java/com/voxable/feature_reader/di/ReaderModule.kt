package com.voxable.feature_reader.di

import android.content.Context
import com.voxable.feature_reader.data.repository.ReaderRepositoryImpl
import com.voxable.feature_reader.domain.repository.ReaderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReaderModule {

    @Provides
    @Singleton
    fun provideReaderRepository(
        @ApplicationContext context: Context
    ): ReaderRepository {
        return ReaderRepositoryImpl(context)
    }
}
