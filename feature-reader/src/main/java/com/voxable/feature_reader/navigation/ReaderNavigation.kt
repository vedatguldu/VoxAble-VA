package com.voxable.feature_reader.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.voxable.feature_reader.presentation.ReaderScreen

const val READER_ROUTE = "reader"

fun NavGraphBuilder.readerScreen(
    onBack: () -> Unit
) {
    composable(READER_ROUTE) {
        ReaderScreen(onBack = onBack)
    }
}
