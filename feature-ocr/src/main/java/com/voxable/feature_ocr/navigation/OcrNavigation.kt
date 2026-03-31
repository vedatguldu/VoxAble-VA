package com.voxable.feature_ocr.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.voxable.feature_ocr.presentation.OcrScreen

const val OCR_ROUTE = "ocr"

fun NavGraphBuilder.ocrScreen(
    onBack: () -> Unit
) {
    composable(route = OCR_ROUTE) {
        OcrScreen(onBack = onBack)
    }
}
