package com.voxable.feature_currency.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.voxable.feature_currency.presentation.CurrencyScreen
import com.voxable.feature_currency.presentation.recognition.CurrencyRecognitionScreen

const val CURRENCY_ROUTE = "currency"
const val CURRENCY_RECOGNITION_ROUTE = "currency_recognition"

fun NavGraphBuilder.currencyScreen(
    onBack: () -> Unit,
    onNavigateToRecognition: () -> Unit = {}
) {
    composable(route = CURRENCY_ROUTE) {
        CurrencyScreen(
            onBack = onBack,
            onNavigateToRecognition = onNavigateToRecognition
        )
    }
}

fun NavGraphBuilder.currencyRecognitionScreen(
    onBack: () -> Unit
) {
    composable(route = CURRENCY_RECOGNITION_ROUTE) {
        CurrencyRecognitionScreen(onBack = onBack)
    }
}
