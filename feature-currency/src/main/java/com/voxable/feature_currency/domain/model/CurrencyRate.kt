package com.voxable.feature_currency.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CurrencyRate(
    val base: String,
    val rates: Map<String, Double>,
    val lastUpdated: String = ""
)
