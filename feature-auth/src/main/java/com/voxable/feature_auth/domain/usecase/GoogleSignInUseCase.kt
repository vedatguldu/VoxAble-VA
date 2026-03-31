package com.voxable.feature_auth.domain.usecase

import com.voxable.core.util.Resource
import com.voxable.feature_auth.domain.repository.AuthRepository
import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Resource<String> {
        if (idToken.isBlank()) return Resource.Error("Google kimlik doğrulama başarısız oldu")
        return authRepository.signInWithGoogle(idToken)
    }
}
