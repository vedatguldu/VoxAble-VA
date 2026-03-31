package com.voxable.feature_auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.voxable.feature_auth.presentation.login.LoginScreen
import com.voxable.feature_auth.presentation.register.RegisterScreen

const val AUTH_GRAPH_ROUTE = "auth_graph"
const val LOGIN_ROUTE = "login"
const val REGISTER_ROUTE = "register"

fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    onAuthSuccess: () -> Unit
) {
    navigation(
        startDestination = LOGIN_ROUTE,
        route = AUTH_GRAPH_ROUTE
    ) {
        composable(LOGIN_ROUTE) {
            LoginScreen(
                onLoginSuccess = onAuthSuccess,
                onNavigateToRegister = {
                    navController.navigate(REGISTER_ROUTE)
                }
            )
        }

        composable(REGISTER_ROUTE) {
            RegisterScreen(
                onRegisterSuccess = onAuthSuccess,
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
    }
}
