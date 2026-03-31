package com.voxable.feature_auth.domain.usecase

import com.voxable.core.util.Resource
import com.voxable.feature_auth.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        confirmPassword: String,
        displayName: String
    ): Resource<String> {
        if (email.isBlank()) return Resource.Error("E-posta adresi boş olamaz")
        if (displayName.isBlank()) return Resource.Error("Ad Soyad boş olamaz")
        if (password.isBlank()) return Resource.Error("Şifre boş olamaz")
        if (password.length < 6) return Resource.Error("Şifre en az 6 karakter olmalıdır")
        if (password != confirmPassword) return Resource.Error("Şifreler eşleşmiyor")
        return authRepository.register(email.trim(), password, displayName.trim())
    }
}
