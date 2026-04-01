package com.voxable.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.voxable.feature_auth.navigation.authNavGraph
import com.voxable.feature_auth.navigation.AUTH_GRAPH_ROUTE
import com.voxable.feature_converter.navigation.converterScreen
import com.voxable.feature_currency.navigation.currencyRecognitionScreen
import com.voxable.feature_currency.navigation.currencyScreen
import com.voxable.feature_downloader.navigation.downloaderScreen
import com.voxable.feature_home.navigation.homeScreen
import com.voxable.feature_media.navigation.mediaScreen
import com.voxable.feature_ocr.navigation.ocrScreen
import com.voxable.feature_reader.navigation.readerScreen
import com.voxable.feature_settings.navigation.settingsScreen

@Composable
fun VoxAbleNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = AUTH_GRAPH_ROUTE
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Auth flow (login/register)
        authNavGraph(
            navController = navController,
            onAuthSuccess = {
                navController.navigate(TopLevelDestination.HOME.route) {
                    popUpTo(AUTH_GRAPH_ROUTE) { inclusive = true }
                }
            }
        )

        // Bottom bar destinations
        homeScreen(
            onNavigateToReader = { navController.navigate("reader") },
            onNavigateToMedia = { navController.navigate("media") },
            onNavigateToOcr = { navController.navigate("ocr") },
            onNavigateToCurrency = { navController.navigate("currency") },
            onNavigateToConverter = { navController.navigate("converter") },
            onNavigateToDownloader = { navController.navigate("downloader") }
        )

        readerScreen(
            onBack = { navController.popBackStack() }
        )

        mediaScreen(
            onBack = { navController.popBackStack() }
        )

        // Feature screens
        ocrScreen(
            onBack = { navController.popBackStack() }
        )

        currencyScreen(
            onBack = { navController.popBackStack() },
            onNavigateToRecognition = { navController.navigate("currency_recognition") }
        )

        currencyRecognitionScreen(
            onBack = { navController.popBackStack() }
        )

        converterScreen(
            onBack = { navController.popBackStack() }
        )

        downloaderScreen(
            onBack = { navController.popBackStack() }
        )

        settingsScreen(
            onBack = { navController.popBackStack() },
            onSignOut = {
                navController.navigate(AUTH_GRAPH_ROUTE) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}
