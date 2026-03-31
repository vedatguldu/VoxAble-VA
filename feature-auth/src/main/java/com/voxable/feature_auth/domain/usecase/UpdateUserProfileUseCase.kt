package com.voxable.feature_auth.domain.usecase

import com.voxable.core.util.Resource
import com.voxable.feature_auth.domain.model.UserProfile
import com.voxable.feature_auth.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(profile: UserProfile): Resource<Unit> {
        if (profile.uid.isBlank()) return Resource.Error("Kullanıcı kimliği bulunamadı")
        if (profile.displayName.isNullOrBlank()) return Resource.Error("Ad Soyad boş olamaz")
        return authRepository.updateUserProfile(profile)
    }
}
