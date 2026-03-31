package com.voxable.feature_currency.presentation

import com.voxable.core.base.BaseViewModel
import com.voxable.core.util.onError
import com.voxable.core.util.onSuccess
import com.voxable.feature_currency.domain.usecase.ConvertCurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val convertCurrencyUseCase: ConvertCurrencyUseCase
) : BaseViewModel<CurrencyState, CurrencyEvent>(CurrencyState()) {

    init {
        convert()
    }

    fun onAmountChanged(amount: String) {
        // Sadece sayısal girdi kabul et
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d*$"))) {
            updateState { copy(amount = amount) }
            convert()
        }
    }

    fun onFromCurrencyChanged(currency: String) {
        updateState { copy(fromCurrency = currency) }
        convert()
    }

    fun onToCurrencyChanged(currency: String) {
        updateState { copy(toCurrency = currency) }
        convert()
    }

    fun onSwapCurrencies() {
        updateState {
            copy(
                fromCurrency = toCurrency,
                toCurrency = fromCurrency
            )
        }
        convert()
    }

    private fun convert() {
        val amount = currentState.amount.toDoubleOrNull() ?: return

        launch {
            updateState { copy(isLoading = true, error = null) }
            convertCurrencyUseCase(amount, currentState.fromCurrency, currentState.toCurrency)
                .onSuccess { result ->
                    updateState {
                        copy(
                            result = "%.4f".format(result),
                            isLoading = false
                        )
                    }
                }
                .onError { message ->
                    updateState { copy(isLoading = false, error = message) }
                    sendEvent(CurrencyEvent.ShowError(message))
                }
        }
    }
}
