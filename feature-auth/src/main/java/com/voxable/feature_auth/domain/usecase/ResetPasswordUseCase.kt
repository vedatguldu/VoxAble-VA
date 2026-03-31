package com.voxable.feature_auth.domain.usecase

import com.voxable.core.util.Resource
import com.voxable.feature_auth.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Resource<Unit> {
        if (email.isBlank()) return Resource.Error("E-posta adresi boş olamaz")
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Resource.Error("Geçerli bir e-posta adresi girin")
        }
        return authRepository.resetPassword(email.trim())
    }
}
