package com.voxable.feature_auth.presentation.profile

import com.voxable.core.base.UiEvent
import com.voxable.core.base.UiState
import com.voxable.feature_auth.domain.model.AuthProvider
import com.voxable.feature_auth.domain.model.UserPreferences

data class ProfileState(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val phoneNumber: String = "",
    val authProvider: AuthProvider = AuthProvider.EMAIL,
    val preferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val nameError: String? = null
) : UiState

sealed class ProfileEvent : UiEvent {
    data object ProfileUpdated : ProfileEvent()
    data object PreferencesSaved : ProfileEvent()
    data object AccountDeleted : ProfileEvent()
    data object SignedOut : ProfileEvent()
    data class ShowError(val message: String) : ProfileEvent()
    data class ShowSuccess(val message: String) : ProfileEvent()
}
