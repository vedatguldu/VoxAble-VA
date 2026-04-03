package com.voxable.feature_home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.voxable.feature_home.presentation.HomeScreen

const val HOME_ROUTE = "home"

fun NavGraphBuilder.homeScreen(
    onNavigateToReader: () -> Unit,
    onNavigateToMedia: () -> Unit,
    onNavigateToOcr: () -> Unit,
    onNavigateToColorRecognition: () -> Unit,
    onNavigateToQrScanner: () -> Unit,
    onNavigateToCurrency: () -> Unit,
    onNavigateToConverter: () -> Unit,
    onNavigateToDownloader: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    composable(HOME_ROUTE) {
        HomeScreen(
            onNavigateToReader = onNavigateToReader,
            onNavigateToMedia = onNavigateToMedia,
            onNavigateToOcr = onNavigateToOcr,
            onNavigateToColorRecognition = onNavigateToColorRecognition,
            onNavigateToQrScanner = onNavigateToQrScanner,
            onNavigateToCurrency = onNavigateToCurrency,
            onNavigateToConverter = onNavigateToConverter,
            onNavigateToDownloader = onNavigateToDownloader,
            onNavigateToSettings = onNavigateToSettings
        )
    }
}
