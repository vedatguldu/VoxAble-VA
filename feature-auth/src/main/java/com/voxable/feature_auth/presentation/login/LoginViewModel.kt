package com.voxable.feature_auth.presentation.login

import com.voxable.core.base.BaseViewModel
import com.voxable.core.util.Resource
import com.voxable.feature_auth.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : BaseViewModel<LoginState, LoginEvent>(LoginState()) {

    fun onEmailChange(email: String) {
        updateState { copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        updateState { copy(password = password, passwordError = null) }
    }

    fun onLoginClick() {
        launch {
            updateState { copy(isLoading = true) }

            when (val result = loginUseCase(currentState.email, currentState.password)) {
                is Resource.Success -> {
                    updateState { copy(isLoading = false) }
                    sendEvent(LoginEvent.LoginSuccess)
                }
                is Resource.Error -> {
                    updateState { copy(isLoading = false) }
                    sendEvent(LoginEvent.ShowError(result.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
