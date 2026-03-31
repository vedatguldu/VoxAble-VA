package com.voxable.core_network.api

import com.voxable.core_network.model.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("currency/rates")
    suspend fun getCurrencyRates(
        @Query("base") baseCurrency: String = "TRY"
    ): ApiResponse<Map<String, Double>>

    @GET("converter/units")
    suspend fun getConversionUnits(
        @Query("type") type: String
    ): ApiResponse<List<String>>
}
