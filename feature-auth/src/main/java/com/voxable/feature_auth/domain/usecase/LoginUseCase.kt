package com.voxable.feature_auth.domain.usecase

import com.voxable.core.util.Resource
import com.voxable.feature_auth.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Resource<String> {
        if (email.isBlank()) return Resource.Error("E-posta adresi boş olamaz")
        if (password.isBlank()) return Resource.Error("Şifre boş olamaz")
        if (password.length < 6) return Resource.Error("Şifre en az 6 karakter olmalıdır")
        return authRepository.login(email.trim(), password)
    }
}
