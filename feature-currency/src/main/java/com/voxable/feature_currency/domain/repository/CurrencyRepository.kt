package com.voxable.feature_currency.domain.repository

import com.voxable.core.util.Resource
import com.voxable.feature_currency.domain.model.CurrencyRate

interface CurrencyRepository {
    suspend fun getExchangeRates(baseCurrency: String): Resource<CurrencyRate>
    suspend fun convert(amount: Double, from: String, to: String): Resource<Double>
}
