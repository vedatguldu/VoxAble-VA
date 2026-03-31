package com.voxable.feature_auth.presentation.register

import com.voxable.core.base.UiEvent
import com.voxable.core.base.UiState

data class RegisterState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
) : UiState

sealed class RegisterEvent : UiEvent {
    data object RegisterSuccess : RegisterEvent()
    data class ShowError(val message: String) : RegisterEvent()
}
