package com.voxable.feature_currency.data.repository

import com.voxable.core.util.Resource
import com.voxable.core_network.api.ApiService
import com.voxable.feature_currency.domain.model.CurrencyRate
import com.voxable.feature_currency.domain.repository.CurrencyRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : CurrencyRepository {

    private var cachedRates: CurrencyRate? = null

    override suspend fun getExchangeRates(baseCurrency: String): Resource<CurrencyRate> {
        return try {
            val response = apiService.getCurrencyRates(baseCurrency)
            if (response.success && response.data != null) {
                val rates = CurrencyRate(
                    base = baseCurrency,
                    rates = response.data
                )
                cachedRates = rates
                Resource.Success(rates)
            } else {
                Resource.Error(response.message ?: "Döviz kurları alınamadı")
            }
        } catch (e: Exception) {
            // Önbellekten dön
            cachedRates?.let { Resource.Success(it) }
                ?: Resource.Error(e.message ?: "Ağ hatası oluştu")
        }
    }

    override suspend fun convert(amount: Double, from: String, to: String): Resource<Double> {
        val ratesResult = getExchangeRates(from)
        return when (ratesResult) {
            is Resource.Success -> {
                val rate = ratesResult.data.rates[to]
                if (rate != null) {
                    Resource.Success(amount * rate)
                } else {
                    Resource.Error("$to için döviz kuru bulunamadı")
                }
            }
            is Resource.Error -> Resource.Error(ratesResult.message)
            is Resource.Loading -> Resource.Loading
        }
    }
}
