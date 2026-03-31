package com.voxable.feature_auth.presentation.register

import com.voxable.core.base.BaseViewModel
import com.voxable.core.util.Resource
import com.voxable.feature_auth.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
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
}
