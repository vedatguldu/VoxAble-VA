package com.voxable.feature_converter.di

import com.voxable.feature_converter.data.repository.FileConverterRepositoryImpl
import com.voxable.feature_converter.domain.fileconverter.repository.FileConverterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FileConverterModule {

    @Binds
    @Singleton
    abstract fun bindFileConverterRepository(
        impl: FileConverterRepositoryImpl
    ): FileConverterRepository
}
