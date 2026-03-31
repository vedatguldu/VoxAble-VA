package com.voxable.feature_auth.domain.repository

import com.voxable.core.util.Resource

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<String>
    suspend fun register(email: String, password: String, displayName: String): Resource<String>
    suspend fun signOut()
    fun isLoggedIn(): Boolean
    fun getCurrentUserId(): String?
}
