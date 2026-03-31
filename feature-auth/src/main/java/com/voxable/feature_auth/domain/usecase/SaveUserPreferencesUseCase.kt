package com.voxable.feature_auth.domain.usecase

import com.voxable.core.util.Resource
import com.voxable.feature_auth.domain.model.UserPreferences
import com.voxable.feature_auth.domain.repository.AuthRepository
import javax.inject.Inject

class SaveUserPreferencesUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(uid: String, preferences: UserPreferences): Resource<Unit> {
        if (uid.isBlank()) return Resource.Error("Kullanıcı kimliği bulunamadı")
        return authRepository.saveUserPreferences(uid, preferences)
    }
}
