package com.voxable.feature_auth.domain.repository

import com.voxable.core.util.Resource
import com.voxable.feature_auth.domain.model.UserPreferences
import com.voxable.feature_auth.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    // Kimlik doğrulama
    suspend fun login(email: String, password: String): Resource<String>
    suspend fun register(email: String, password: String, displayName: String): Resource<String>
    suspend fun signInWithGoogle(idToken: String): Resource<String>
    suspend fun signOut()
    suspend fun resetPassword(email: String): Resource<Unit>
    fun isLoggedIn(): Boolean
    fun getCurrentUserId(): String?

    // Profil yönetimi
    suspend fun getUserProfile(uid: String): Resource<UserProfile>
    fun observeUserProfile(uid: String): Flow<UserProfile?>
    suspend fun updateUserProfile(profile: UserProfile): Resource<Unit>
    suspend fun updateProfilePhoto(uid: String, photoUrl: String): Resource<Unit>
    suspend fun deleteAccount(): Resource<Unit>

    // Kullanıcı tercihleri (Firestore)
    suspend fun getUserPreferences(uid: String): Resource<UserPreferences>
    suspend fun saveUserPreferences(uid: String, preferences: UserPreferences): Resource<Unit>

    // Veri geri yükleme
    suspend fun restoreUserData(uid: String): Resource<UserProfile>
}
