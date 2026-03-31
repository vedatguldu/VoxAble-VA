package com.voxable.feature_converter.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.voxable.feature_converter.presentation.ConverterScreen

const val CONVERTER_ROUTE = "converter"

fun NavGraphBuilder.converterScreen(
    onBack: () -> Unit
) {
    composable(route = CONVERTER_ROUTE) {
        ConverterScreen(onBack = onBack)
    }
}
