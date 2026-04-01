package com.voxable.feature_currency.di

import com.voxable.feature_currency.data.repository.CurrencyRecognitionRepositoryImpl
import com.voxable.feature_currency.data.repository.CurrencyRepositoryImpl
import com.voxable.feature_currency.domain.repository.CurrencyRecognitionRepository
import com.voxable.feature_currency.domain.repository.CurrencyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CurrencyModule {

    @Binds
    @Singleton
    abstract fun bindCurrencyRepository(
        impl: CurrencyRepositoryImpl
    ): CurrencyRepository

    @Binds
    @Singleton
    abstract fun bindCurrencyRecognitionRepository(
        impl: CurrencyRecognitionRepositoryImpl
    ): CurrencyRecognitionRepository
}
