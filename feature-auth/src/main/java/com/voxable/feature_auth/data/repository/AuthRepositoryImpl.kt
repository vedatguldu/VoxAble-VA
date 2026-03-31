package com.voxable.feature_auth.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.voxable.core.base.BaseRepository
import com.voxable.core.util.Constants
import com.voxable.core.util.Resource
import com.voxable.core_database.dao.UserDao
import com.voxable.core_database.entity.UserEntity
import com.voxable.feature_auth.domain.model.AuthProvider
import com.voxable.feature_auth.domain.model.UserPreferences
import com.voxable.feature_auth.domain.model.UserProfile
import com.voxable.feature_auth.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : BaseRepository(), AuthRepository {

    // ─── Kimlik doğrulama ───────────────────────────────────────────────

    override suspend fun login(email: String, password: String): Resource<String> {
        return safeCallOnIo {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Kullanıcı bulunamadı")

            saveUserLocally(
                uid = user.uid,
                email = user.email ?: "",
                displayName = user.displayName,
                photoUrl = user.photoUrl?.toString()
            )

            // Firestore son giriş zamanını güncelle
            updateLastLogin(user.uid)

            user.uid
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Resource<String> {
        return safeCallOnIo {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Kullanıcı oluşturulamadı")

            // Firebase Auth profil güncelle
            val profileUpdates = userProfileChangeRequest {
                this.displayName = displayName
            }
            user.updateProfile(profileUpdates).await()

            val now = System.currentTimeMillis()

            // Firestore'a kullanıcı verisi kaydet
            val userData = hashMapOf(
                "uid" to user.uid,
                "email" to email,
                "displayName" to displayName,
                "photoUrl" to null,
                "phoneNumber" to null,
                "authProvider" to AuthProvider.EMAIL.name,
                "createdAt" to now,
                "lastLoginAt" to now
            )
            firestore.collection(Constants.Firebase.USERS_COLLECTION)
                .document(user.uid)
                .set(userData)
                .await()

            // Varsayılan tercihleri Firestore'a kaydet
            saveDefaultPreferences(user.uid)

            // Yerel veritabanına kaydet
            saveUserLocally(
                uid = user.uid,
                email = email,
                displayName = displayName,
                photoUrl = null
            )

            user.uid
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Resource<String> {
        return safeCallOnIo {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Google ile giriş başarısız")
            val isNewUser = result.additionalUserInfo?.isNewUser == true

            if (isNewUser) {
                val now = System.currentTimeMillis()
                val userData = hashMapOf(
                    "uid" to user.uid,
                    "email" to (user.email ?: ""),
                    "displayName" to user.displayName,
                    "photoUrl" to user.photoUrl?.toString(),
                    "phoneNumber" to user.phoneNumber,
                    "authProvider" to AuthProvider.GOOGLE.name,
                    "createdAt" to now,
                    "lastLoginAt" to now
                )
                firestore.collection(Constants.Firebase.USERS_COLLECTION)
                    .document(user.uid)
                    .set(userData)
                    .await()

                saveDefaultPreferences(user.uid)
            } else {
                updateLastLogin(user.uid)
            }

            saveUserLocally(
                uid = user.uid,
                email = user.email ?: "",
                displayName = user.displayName,
                photoUrl = user.photoUrl?.toString()
            )

            user.uid
        }
    }

    override suspend fun signOut() {
        firebaseAuth.currentUser?.uid?.let { uid ->
            userDao.deleteUser(uid)
        }
        firebaseAuth.signOut()
    }

    override suspend fun resetPassword(email: String): Resource<Unit> {
        return safeCallOnIo {
            firebaseAuth.sendPasswordResetEmail(email).await()
        }
    }

    override fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    // ─── Profil yönetimi ────────────────────────────────────────────────

    override suspend fun getUserProfile(uid: String): Resource<UserProfile> {
        return safeCallOnIo {
            val doc = firestore.collection(Constants.Firebase.USERS_COLLECTION)
                .document(uid)
                .get()
                .await()

            if (!doc.exists()) throw Exception("Kullanıcı profili bulunamadı")

            UserProfile(
                uid = doc.getString("uid") ?: uid,
                email = doc.getString("email") ?: "",
                displayName = doc.getString("displayName"),
                photoUrl = doc.getString("photoUrl"),
                phoneNumber = doc.getString("phoneNumber"),
                authProvider = try {
                    AuthProvider.valueOf(doc.getString("authProvider") ?: "EMAIL")
                } catch (_: Exception) {
                    AuthProvider.EMAIL
                },
                createdAt = doc.getLong("createdAt") ?: 0L,
                lastLoginAt = doc.getLong("lastLoginAt") ?: 0L
            )
        }
    }

    override fun observeUserProfile(uid: String): Flow<UserProfile?> {
        return userDao.observeUser(uid).map { entity ->
            entity?.let {
                UserProfile(
                    uid = it.uid,
                    email = it.email,
                    displayName = it.displayName,
                    photoUrl = it.photoUrl,
                    createdAt = it.createdAt,
                    lastLoginAt = it.updatedAt
                )
            }
        }
    }

    override suspend fun updateUserProfile(profile: UserProfile): Resource<Unit> {
        return safeCallOnIo {
            val updates = hashMapOf<String, Any?>(
                "displayName" to profile.displayName,
                "phoneNumber" to profile.phoneNumber,
                "photoUrl" to profile.photoUrl
            )
            firestore.collection(Constants.Firebase.USERS_COLLECTION)
                .document(profile.uid)
                .update(updates)
                .await()

            // Firebase Auth profilini de güncelle
            firebaseAuth.currentUser?.let { user ->
                val profileUpdates = userProfileChangeRequest {
                    displayName = profile.displayName
                    if (profile.photoUrl != null) {
                        photoUri = android.net.Uri.parse(profile.photoUrl)
                    }
                }
                user.updateProfile(profileUpdates).await()
            }

            // Yerel veritabanını güncelle
            saveUserLocally(
                uid = profile.uid,
                email = profile.email,
                displayName = profile.displayName,
                photoUrl = profile.photoUrl
            )
        }
    }

    override suspend fun updateProfilePhoto(uid: String, photoUrl: String): Resource<Unit> {
        return safeCallOnIo {
            firestore.collection(Constants.Firebase.USERS_COLLECTION)
                .document(uid)
                .update("photoUrl", photoUrl)
                .await()

            firebaseAuth.currentUser?.let { user ->
                val profileUpdates = userProfileChangeRequest {
                    photoUri = android.net.Uri.parse(photoUrl)
                }
                user.updateProfile(profileUpdates).await()
            }
        }
    }

    override suspend fun deleteAccount(): Resource<Unit> {
        return safeCallOnIo {
            val user = firebaseAuth.currentUser ?: throw Exception("Oturum açık değil")
            val uid = user.uid

            // Firestore verilerini sil
            firestore.collection(Constants.Firebase.USERS_COLLECTION)
                .document(uid)
                .delete()
                .await()

            firestore.collection(Constants.Firebase.SETTINGS_COLLECTION)
                .document(uid)
                .delete()
                .await()

            // Yerel veritabanını temizle
            userDao.deleteUser(uid)

            // Firebase Auth hesabını sil
            user.delete().await()
        }
    }

    // ─── Kullanıcı tercihleri ───────────────────────────────────────────

    override suspend fun getUserPreferences(uid: String): Resource<UserPreferences> {
        return safeCallOnIo {
            val doc = firestore.collection(Constants.Firebase.SETTINGS_COLLECTION)
                .document(uid)
                .get()
                .await()

            if (!doc.exists()) {
                // Varsayılan tercihleri oluştur ve döndür
                val defaults = UserPreferences()
                saveDefaultPreferences(uid)
                return@safeCallOnIo defaults
            }

            UserPreferences(
                darkMode = doc.getBoolean("darkMode") ?: false,
                fontSize = (doc.getDouble("fontSize") ?: 1.0).toFloat(),
                highContrast = doc.getBoolean("highContrast") ?: false,
                talkBackHints = doc.getBoolean("talkBackHints") ?: true,
                language = doc.getString("language") ?: "tr",
                reduceMotion = doc.getBoolean("reduceMotion") ?: false,
                hapticFeedback = doc.getBoolean("hapticFeedback") ?: true
            )
        }
    }

    override suspend fun saveUserPreferences(
        uid: String,
        preferences: UserPreferences
    ): Resource<Unit> {
        return safeCallOnIo {
            val prefsMap = hashMapOf(
                "darkMode" to preferences.darkMode,
                "fontSize" to preferences.fontSize.toDouble(),
                "highContrast" to preferences.highContrast,
                "talkBackHints" to preferences.talkBackHints,
                "language" to preferences.language,
                "reduceMotion" to preferences.reduceMotion,
                "hapticFeedback" to preferences.hapticFeedback,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection(Constants.Firebase.SETTINGS_COLLECTION)
                .document(uid)
                .set(prefsMap)
                .await()
        }
    }

    // ─── Veri geri yükleme ──────────────────────────────────────────────

    override suspend fun restoreUserData(uid: String): Resource<UserProfile> {
        return safeCallOnIo {
            // Firestore'dan profili çek
            val profileResult = getUserProfile(uid)
            val profile = when (profileResult) {
                is Resource.Success -> profileResult.data
                is Resource.Error -> throw Exception(profileResult.message)
                is Resource.Loading -> throw Exception("Beklenmeyen durum")
            }

            // Yerel veritabanına kaydet
            saveUserLocally(
                uid = profile.uid,
                email = profile.email,
                displayName = profile.displayName,
                photoUrl = profile.photoUrl
            )

            // Tercihleri de geri yükle (önbellekleme için)
            getUserPreferences(uid)

            profile
        }
    }

    // ─── Yardımcı metotlar ──────────────────────────────────────────────

    private suspend fun saveUserLocally(
        uid: String,
        email: String,
        displayName: String?,
        photoUrl: String?
    ) {
        userDao.insertUser(
            UserEntity(
                uid = uid,
                email = email,
                displayName = displayName,
                photoUrl = photoUrl,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private suspend fun updateLastLogin(uid: String) {
        try {
            firestore.collection(Constants.Firebase.USERS_COLLECTION)
                .document(uid)
                .update("lastLoginAt", System.currentTimeMillis())
                .await()
        } catch (_: Exception) {
            // Son giriş güncellemesi kritik değil, hata yutulabilir
        }
    }

    private suspend fun saveDefaultPreferences(uid: String) {
        val defaults = UserPreferences()
        val prefsMap = hashMapOf(
            "darkMode" to defaults.darkMode,
            "fontSize" to defaults.fontSize.toDouble(),
            "highContrast" to defaults.highContrast,
            "talkBackHints" to defaults.talkBackHints,
            "language" to defaults.language,
            "reduceMotion" to defaults.reduceMotion,
            "hapticFeedback" to defaults.hapticFeedback,
            "updatedAt" to System.currentTimeMillis()
        )
        firestore.collection(Constants.Firebase.SETTINGS_COLLECTION)
            .document(uid)
            .set(prefsMap)
            .await()
    }
}
