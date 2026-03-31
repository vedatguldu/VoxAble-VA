package com.voxable.feature_auth.presentation.login

import com.voxable.core.base.BaseViewModel
import com.voxable.core.util.Resource
import com.voxable.feature_auth.domain.usecase.GoogleSignInUseCase
import com.voxable.feature_auth.domain.usecase.LoginUseCase
import com.voxable.feature_auth.domain.usecase.ResetPasswordUseCase
import com.voxable.feature_auth.domain.usecase.RestoreUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val restoreUserDataUseCase: RestoreUserDataUseCase
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
                    // Giriş sonrası kullanıcı verisini geri yükle
                    restoreUserDataUseCase(result.data)
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

    // ─── Google Sign-In ─────────────────────────────────────────────────

    fun onGoogleSignInClick() {
        sendEvent(LoginEvent.LaunchGoogleSignIn)
    }

    fun onGoogleSignInResult(idToken: String) {
        launch {
            updateState { copy(isGoogleLoading = true) }

            when (val result = googleSignInUseCase(idToken)) {
                is Resource.Success -> {
                    restoreUserDataUseCase(result.data)
                    updateState { copy(isGoogleLoading = false) }
                    sendEvent(LoginEvent.LoginSuccess)
                }
                is Resource.Error -> {
                    updateState { copy(isGoogleLoading = false) }
                    sendEvent(LoginEvent.ShowError(result.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onGoogleSignInError(message: String) {
        updateState { copy(isGoogleLoading = false) }
        sendEvent(LoginEvent.ShowError(message))
    }

    // ─── Şifre sıfırlama ───────────────────────────────────────────────

    fun onForgotPasswordClick() {
        updateState {
            copy(
                showForgotPasswordDialog = true,
                forgotPasswordEmail = email,
                forgotPasswordSent = false
            )
        }
    }

    fun onForgotPasswordEmailChange(email: String) {
        updateState { copy(forgotPasswordEmail = email) }
    }

    fun onForgotPasswordDismiss() {
        updateState { copy(showForgotPasswordDialog = false) }
    }

    fun onSendPasswordReset() {
        launch {
            updateState { copy(isLoading = true) }

            when (val result = resetPasswordUseCase(currentState.forgotPasswordEmail)) {
                is Resource.Success -> {
                    updateState {
                        copy(
                            isLoading = false,
                            forgotPasswordSent = true
                        )
                    }
                    sendEvent(LoginEvent.ShowSuccess("Şifre sıfırlama e-postası gönderildi"))
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
