package com.voxable.feature_settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.voxable.feature_settings.presentation.SettingsScreen

const val SETTINGS_ROUTE = "settings"

fun NavGraphBuilder.settingsScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    composable(route = SETTINGS_ROUTE) {
        SettingsScreen(
            onBack = onBack,
            onSignOut = onSignOut,
            onNavigateToProfile = onNavigateToProfile
        )
    }
}
