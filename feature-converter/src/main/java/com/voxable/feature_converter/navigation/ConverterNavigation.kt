package com.voxable.feature_converter.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.voxable.feature_converter.presentation.ConverterScreen
import com.voxable.feature_converter.presentation.fileconverter.FileConverterScreen

const val CONVERTER_ROUTE = "converter"
const val FILE_CONVERTER_ROUTE = "file_converter"

fun NavGraphBuilder.converterScreen(
    onBack: () -> Unit,
    onNavigateToFileConverter: () -> Unit
) {
    composable(route = CONVERTER_ROUTE) {
        ConverterScreen(
            onBack = onBack,
            onNavigateToFileConverter = onNavigateToFileConverter
        )
    }
}

fun NavGraphBuilder.fileConverterScreen(
    onBack: () -> Unit
) {
    composable(route = FILE_CONVERTER_ROUTE) {
        FileConverterScreen(onBack = onBack)
    }
}
