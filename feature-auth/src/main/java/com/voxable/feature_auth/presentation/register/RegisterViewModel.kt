package com.voxable.feature_auth.presentation.register

import com.voxable.core.base.BaseViewModel
import com.voxable.core.util.Resource
import com.voxable.feature_auth.domain.usecase.GoogleSignInUseCase
import com.voxable.feature_auth.domain.usecase.RegisterUseCase
import com.voxable.feature_auth.domain.usecase.RestoreUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val restoreUserDataUseCase: RestoreUserDataUseCase
) : BaseViewModel<RegisterState, RegisterEvent>(RegisterState()) {

    fun onNameChange(name: String) {
        updateState { copy(displayName = name, nameError = null) }
    }

    fun onEmailChange(email: String) {
        updateState { copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        updateState { copy(password = password, passwordError = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        updateState { copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    fun onRegisterClick() {
        launch {
            updateState { copy(isLoading = true) }

            val result = registerUseCase(
                email = currentState.email,
                password = currentState.password,
                confirmPassword = currentState.confirmPassword,
                displayName = currentState.displayName
            )

            when (result) {
                is Resource.Success -> {
                    updateState { copy(isLoading = false) }
                    sendEvent(RegisterEvent.RegisterSuccess)
                }
                is Resource.Error -> {
                    updateState { copy(isLoading = false) }
                    sendEvent(RegisterEvent.ShowError(result.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    // ─── Google Sign-In ─────────────────────────────────────────────────

    fun onGoogleSignInClick() {
        sendEvent(RegisterEvent.LaunchGoogleSignIn)
    }

    fun onGoogleSignInResult(idToken: String) {
        launch {
            updateState { copy(isGoogleLoading = true) }

            when (val result = googleSignInUseCase(idToken)) {
                is Resource.Success -> {
                    restoreUserDataUseCase(result.data)
                    updateState { copy(isGoogleLoading = false) }
                    sendEvent(RegisterEvent.RegisterSuccess)
                }
                is Resource.Error -> {
                    updateState { copy(isGoogleLoading = false) }
                    sendEvent(RegisterEvent.ShowError(result.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onGoogleSignInError(message: String) {
        updateState { copy(isGoogleLoading = false) }
        sendEvent(RegisterEvent.ShowError(message))
    }
}
