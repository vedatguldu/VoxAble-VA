package com.voxable.feature_ocr.di

import com.voxable.feature_ocr.data.repository.OcrRepositoryImpl
import com.voxable.feature_ocr.domain.repository.OcrRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OcrModule {

    @Binds
    @Singleton
    abstract fun bindOcrRepository(
        impl: OcrRepositoryImpl
    ): OcrRepository
}
