package com.voxable.feature_auth.presentation.login

import com.voxable.core.base.UiEvent
import com.voxable.core.base.UiState

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val showForgotPasswordDialog: Boolean = false,
    val forgotPasswordEmail: String = "",
    val forgotPasswordSent: Boolean = false
) : UiState

sealed class LoginEvent : UiEvent {
    data object LoginSuccess : LoginEvent()
    data class ShowError(val message: String) : LoginEvent()
    data class ShowSuccess(val message: String) : LoginEvent()
    data object LaunchGoogleSignIn : LoginEvent()
}
