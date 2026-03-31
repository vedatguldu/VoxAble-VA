package com.voxable.feature_auth.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.voxable.core.base.BaseRepository
import com.voxable.core.util.Constants
import com.voxable.core.util.Resource
import com.voxable.core_database.dao.UserDao
import com.voxable.core_database.entity.UserEntity
import com.voxable.feature_auth.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : BaseRepository(), AuthRepository {

    override suspend fun login(email: String, password: String): Resource<String> {
        return safeCallOnIo {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Kullanıcı bulunamadı")

            // Yerel veritabanına kaydet
            userDao.insertUser(
                UserEntity(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName,
                    photoUrl = user.photoUrl?.toString()
                )
            )

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

            // Profil güncelle
            val profileUpdates = userProfileChangeRequest {
                this.displayName = displayName
            }
            user.updateProfile(profileUpdates).await()

            // Firestore'a kaydet
            val userData = hashMapOf(
                "uid" to user.uid,
                "email" to email,
                "displayName" to displayName,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection(Constants.Firebase.USERS_COLLECTION)
                .document(user.uid)
                .set(userData)
                .await()

            // Yerel veritabanına kaydet
            userDao.insertUser(
                UserEntity(
                    uid = user.uid,
                    email = email,
                    displayName = displayName,
                    photoUrl = null
                )
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

    override fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}
