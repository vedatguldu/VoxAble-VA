package com.voxable.feature_auth.domain.usecase

import com.voxable.core.util.Resource
import com.voxable.feature_auth.domain.model.UserProfile
import com.voxable.feature_auth.domain.repository.AuthRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(uid: String): Resource<UserProfile> {
        if (uid.isBlank()) return Resource.Error("Kullanıcı kimliği bulunamadı")
        return authRepository.getUserProfile(uid)
    }
}
