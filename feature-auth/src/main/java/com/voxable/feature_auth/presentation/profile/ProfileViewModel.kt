package com.voxable.feature_auth.presentation.profile

import com.voxable.core.base.BaseViewModel
import com.voxable.core.util.Resource
import com.voxable.feature_auth.domain.model.UserPreferences
import com.voxable.feature_auth.domain.model.UserProfile
import com.voxable.feature_auth.domain.repository.AuthRepository
import com.voxable.feature_auth.domain.usecase.GetUserProfileUseCase
import com.voxable.feature_auth.domain.usecase.SaveUserPreferencesUseCase
import com.voxable.feature_auth.domain.usecase.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val saveUserPreferencesUseCase: SaveUserPreferencesUseCase,
    private val authRepository: AuthRepository
) : BaseViewModel<ProfileState, ProfileEvent>(ProfileState()) {

    init {
        loadProfile()
    }

    fun loadProfile() {
        val uid = authRepository.getCurrentUserId() ?: return
        launch {
            updateState { copy(isLoading = true) }

            when (val result = getUserProfileUseCase(uid)) {
                is Resource.Success -> {
                    val profile = result.data
                    updateState {
                        copy(
                            uid = profile.uid,
                            email = profile.email,
                            displayName = profile.displayName ?: "",
                            photoUrl = profile.photoUrl,
                            phoneNumber = profile.phoneNumber ?: "",
                            authProvider = profile.authProvider,
                            isLoading = false
                        )
                    }
                    loadPreferences(uid)
                }
                is Resource.Error -> {
                    updateState { copy(isLoading = false) }
                    sendEvent(ProfileEvent.ShowError(result.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun loadPreferences(uid: String) {
        launch {
            when (val result = authRepository.getUserPreferences(uid)) {
                is Resource.Success -> {
                    updateState { copy(preferences = result.data) }
                }
                is Resource.Error -> {
                    // Tercihler yüklenemezse varsayılanları kullan
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onDisplayNameChange(name: String) {
        updateState { copy(displayName = name, nameError = null) }
    }

    fun onPhoneNumberChange(phone: String) {
        updateState { copy(phoneNumber = phone) }
    }

    fun toggleEditing() {
        updateState { copy(isEditing = !isEditing) }
    }

    fun onSaveProfile() {
        if (currentState.displayName.isBlank()) {
            updateState { copy(nameError = "Ad Soyad boş olamaz") }
            return
        }

        launch {
            updateState { copy(isSaving = true) }

            val profile = UserProfile(
                uid = currentState.uid,
                email = currentState.email,
                displayName = currentState.displayName,
                photoUrl = currentState.photoUrl,
                phoneNumber = currentState.phoneNumber.ifBlank { null },
                authProvider = currentState.authProvider
            )

            when (val result = updateUserProfileUseCase(profile)) {
                is Resource.Success -> {
                    updateState { copy(isSaving = false, isEditing = false) }
                    sendEvent(ProfileEvent.ProfileUpdated)
                    sendEvent(ProfileEvent.ShowSuccess("Profil güncellendi"))
                }
                is Resource.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEvent(ProfileEvent.ShowError(result.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    // ─── Tercih güncellemeleri ──────────────────────────────────────────

    fun onDarkModeChange(enabled: Boolean) {
        val updated = currentState.preferences.copy(darkMode = enabled)
        updateState { copy(preferences = updated) }
        savePreferences(updated)
    }

    fun onFontSizeChange(size: Float) {
        val updated = currentState.preferences.copy(fontSize = size)
        updateState { copy(preferences = updated) }
        savePreferences(updated)
    }

    fun onHighContrastChange(enabled: Boolean) {
        val updated = currentState.preferences.copy(highContrast = enabled)
        updateState { copy(preferences = updated) }
        savePreferences(updated)
    }

    fun onTalkBackHintsChange(enabled: Boolean) {
        val updated = currentState.preferences.copy(talkBackHints = enabled)
        updateState { copy(preferences = updated) }
        savePreferences(updated)
    }

    fun onReduceMotionChange(enabled: Boolean) {
        val updated = currentState.preferences.copy(reduceMotion = enabled)
        updateState { copy(preferences = updated) }
        savePreferences(updated)
    }

    fun onHapticFeedbackChange(enabled: Boolean) {
        val updated = currentState.preferences.copy(hapticFeedback = enabled)
        updateState { copy(preferences = updated) }
        savePreferences(updated)
    }

    private fun savePreferences(preferences: UserPreferences) {
        val uid = currentState.uid.ifBlank { return }
        launch {
            when (val result = saveUserPreferencesUseCase(uid, preferences)) {
                is Resource.Success -> {
                    sendEvent(ProfileEvent.PreferencesSaved)
                }
                is Resource.Error -> {
                    sendEvent(ProfileEvent.ShowError("Tercihler kaydedilemedi"))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    // ─── Hesap işlemleri ────────────────────────────────────────────

    fun onSignOut() {
        launch {
            authRepository.signOut()
            sendEvent(ProfileEvent.SignedOut)
        }
    }

    fun onDeleteAccount() {
        launch {
            updateState { copy(isLoading = true) }
            when (val result = authRepository.deleteAccount()) {
                is Resource.Success -> {
                    updateState { copy(isLoading = false) }
                    sendEvent(ProfileEvent.AccountDeleted)
                }
                is Resource.Error -> {
                    updateState { copy(isLoading = false) }
                    sendEvent(ProfileEvent.ShowError(result.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
