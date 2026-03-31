package com.voxable.feature_currency.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.voxable.feature_currency.presentation.CurrencyScreen

const val CURRENCY_ROUTE = "currency"

fun NavGraphBuilder.currencyScreen(
    onBack: () -> Unit
) {
    composable(route = CURRENCY_ROUTE) {
        CurrencyScreen(onBack = onBack)
    }
}
