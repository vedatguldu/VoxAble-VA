package com.voxable.feature_auth.domain.model

data class UserProfile(
    val uid: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val phoneNumber: String? = null,
    val createdAt: Long = 0L,
    val lastLoginAt: Long = 0L,
    val authProvider: AuthProvider = AuthProvider.EMAIL
)

enum class AuthProvider {
    EMAIL,
    GOOGLE
}
