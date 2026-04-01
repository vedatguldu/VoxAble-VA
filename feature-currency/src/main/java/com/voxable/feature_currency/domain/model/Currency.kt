package com.voxable.feature_currency.domain.model

data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val country: String,
    val banknoteKeywords: List<String>,
    val denominations: List<Int>
)
