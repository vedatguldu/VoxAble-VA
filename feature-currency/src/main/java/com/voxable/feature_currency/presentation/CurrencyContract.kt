package com.voxable.feature_currency.presentation

import com.voxable.core.base.UiEvent
import com.voxable.core.base.UiState

data class CurrencyState(
    val amount: String = "1",
    val fromCurrency: String = "TRY",
    val toCurrency: String = "USD",
    val result: String = "",
    val availableCurrencies: List<String> = listOf(
        "TRY", "USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "CNY", "RUB"
    ),
    val isLoading: Boolean = false,
    val lastUpdated: String = "",
    val error: String? = null
) : UiState

sealed interface CurrencyEvent : UiEvent {
    data class ShowError(val message: String) : CurrencyEvent
}
