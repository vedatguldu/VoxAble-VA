package com.voxable.core_database.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import com.voxable.core.util.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Firestore ile düşük seviye okuma/yazma işlemlerini yönetir.
 *
 * Push → yerel kayıt Firestore'a gönderilir
 * Pull → Firestore'dan değişiklikler çekilir
 * Conflict detection → versiyon numarası karşılaştırması
 */
@Singleton
class FirestoreSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    // ─── Push (Local → Firestore) ─────────────────────────

    /**
     * Bir entity'yi Firestore'a yazar.
     *
     * @return FirestorePushResult — başarı veya çakışma bilgisi
     */
    suspend fun pushDocument(
        collection: String,
        documentId: String,
        data: Map<String, Any?>,
        clientVersion: Long,
        conflictResolution: ConflictResolutionStrategy
    ): FirestorePushResult {
        val uid = currentUserId ?: return FirestorePushResult.NotAuthenticated

        val userScopedRef = firestore
            .collection(Constants.Firebase.USERS_COLLECTION)
            .document(uid)
            .collection(collection)
            .document(documentId)

        return try {
            firestore.runTransaction { transaction ->
                val serverSnapshot = transaction.get(userScopedRef)
                val serverVersion = serverSnapshot.getLong(Constants.Sync.FIELD_VERSION)
                val serverUpdatedAt = serverSnapshot.getLong(Constants.Sync.FIELD_UPDATED_AT)
                val clientUpdatedAt = (data[Constants.Sync.FIELD_UPDATED_AT] as? Long)
                    ?: System.currentTimeMillis()

                if (serverSnapshot.exists() && serverVersion != null) {
                    val conflict = resolveConflict(
                        strategy = conflictResolution,
                        clientUpdatedAt = clientUpdatedAt,
                        serverUpdatedAt = serverUpdatedAt ?: 0L,
                        clientVersion = clientVersion,
                        serverVersion = serverVersion
                    )

                    when (conflict) {
                        ConflictOutcome.CLIENT_WINS -> {
                            val mergedData = data.toMutableMap().apply {
                                put(Constants.Sync.FIELD_VERSION, clientVersion + 1)
                                put(Constants.Sync.FIELD_CLIENT_ID, uid)
                            }
                            transaction.set(userScopedRef, mergedData)
                            null // no conflict to return
                        }
                        ConflictOutcome.SERVER_WINS -> {
                            // Read server data and return it so caller can update local DB
                            serverSnapshot.data
                        }
                        ConflictOutcome.CONFLICT -> {
                            null // return conflict signal
                        }
                    }
                } else {
                    // No server document yet — create it
                    val newData = data.toMutableMap().apply {
                        put(Constants.Sync.FIELD_VERSION, clientVersion)
                        put(Constants.Sync.FIELD_CLIENT_ID, uid)
                    }
                    transaction.set(userScopedRef, newData)
                    null
                }
            }.await()

            FirestorePushResult.Success
        } catch (e: FirebaseFirestoreException) {
            when (e.code) {
                FirebaseFirestoreException.Code.ABORTED,
                FirebaseFirestoreException.Code.FAILED_PRECONDITION ->
                    FirestorePushResult.Conflict("Firestore transaction aborted: ${e.message}")
                FirebaseFirestoreException.Code.UNAVAILABLE,
                FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                    FirestorePushResult.NetworkError(e.message ?: "Firestore unreachable")
                else -> FirestorePushResult.Failure(e.message ?: "Unknown Firestore error")
            }
        } catch (e: Exception) {
            FirestorePushResult.Failure(e.message ?: "Unknown error")
        }
    }

    /**
     * Bir entity'yi Firestore'dan siler.
     */
    suspend fun deleteDocument(collection: String, documentId: String): FirestorePushResult {
        val uid = currentUserId ?: return FirestorePushResult.NotAuthenticated

        return try {
            firestore.collection(Constants.Firebase.USERS_COLLECTION)
                .document(uid)
                .collection(collection)
                .document(documentId)
                .delete()
                .await()
            FirestorePushResult.Success
        } catch (e: Exception) {
            FirestorePushResult.Failure(e.message ?: "Delete failed")
        }
    }

    // ─── Pull (Firestore → Local) ─────────────────────────

    /**
     * Belirtilen zamandan sonra güncellenen tüm belgeleri çeker (delta pull).
     *
     * @param collection Firestore koleksiyonu
     * @param afterTimestamp Bu epoch ms değerinden daha yeni belgeler
     */
    suspend fun pullCollection(
        collection: String,
        afterTimestamp: Long = 0L
    ): FirestorePullResult {
        val uid = currentUserId ?: return FirestorePullResult.NotAuthenticated

        return try {
            val snapshot = firestore
                .collection(Constants.Firebase.USERS_COLLECTION)
                .document(uid)
                .collection(collection)
                .whereGreaterThan(Constants.Sync.FIELD_UPDATED_AT, afterTimestamp)
                .get(Source.SERVER)
                .await()

            val documents = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    RemoteDocument(
                        id = doc.id,
                        data = data,
                        version = doc.getLong(Constants.Sync.FIELD_VERSION) ?: 0L,
                        updatedAt = doc.getLong(Constants.Sync.FIELD_UPDATED_AT) ?: 0L
                    )
                }
            }
            FirestorePullResult.Success(documents)
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                FirestorePullResult.NetworkError
            } else {
                FirestorePullResult.Failure(e.message ?: "Pull failed")
            }
        } catch (e: Exception) {
            FirestorePullResult.Failure(e.message ?: "Pull failed")
        }
    }

    /**
     * Tek bir belgeyi Firestore'dan çeker.
     */
    suspend fun fetchDocument(collection: String, documentId: String): RemoteDocument? {
        val uid = currentUserId ?: return null

        return try {
            val snapshot = firestore
                .collection(Constants.Firebase.USERS_COLLECTION)
                .document(uid)
                .collection(collection)
                .document(documentId)
                .get(Source.SERVER)
                .await()

            if (snapshot.exists()) {
                snapshot.data?.let { data ->
                    RemoteDocument(
                        id = snapshot.id,
                        data = data,
                        version = snapshot.getLong(Constants.Sync.FIELD_VERSION) ?: 0L,
                        updatedAt = snapshot.getLong(Constants.Sync.FIELD_UPDATED_AT) ?: 0L
                    )
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // ─── Real-time listener ───────────────────────────────

    /**
     * Bir koleksiyonu gerçek zamanlı dinler.
     * Flow kapanınca listener otomatik kaldırılır.
     */
    fun observeCollection(collection: String): Flow<List<RemoteDocument>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore
            .collection(Constants.Firebase.USERS_COLLECTION)
            .document(uid)
            .collection(collection)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }
                val documents = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { data ->
                        RemoteDocument(
                            id = doc.id,
                            data = data,
                            version = doc.getLong(Constants.Sync.FIELD_VERSION) ?: 0L,
                            updatedAt = doc.getLong(Constants.Sync.FIELD_UPDATED_AT) ?: 0L
                        )
                    }
                }
                trySend(documents)
            }

        awaitClose { listener.remove() }
    }

    // ─── Payload yardımcıları ─────────────────────────────

    /**
     * JSON string payload'u Firestore uyumlu Map'e dönüştürür.
     */
    fun deserializePayload(jsonPayload: String): Map<String, Any?> {
        return try {
            val json = JSONObject(jsonPayload)
            json.keys().asSequence().associateWith { key -> json.opt(key) }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Map verisini JSON string'e dönüştürür (queue'ya yazarken).
     */
    fun serializePayload(data: Map<String, Any?>): String {
        return try {
            JSONObject(data.filterValues { it != null } as Map<String, Any>).toString()
        } catch (e: Exception) {
            "{}"
        }
    }

    // ─── Çakışma çözümü ──────────────────────────────────

    private fun resolveConflict(
        strategy: ConflictResolutionStrategy,
        clientUpdatedAt: Long,
        serverUpdatedAt: Long,
        clientVersion: Long,
        serverVersion: Long
    ): ConflictOutcome {
        return when (strategy) {
            ConflictResolutionStrategy.SERVER_WINS -> ConflictOutcome.SERVER_WINS
            ConflictResolutionStrategy.CLIENT_WINS -> ConflictOutcome.CLIENT_WINS
            ConflictResolutionStrategy.MANUAL -> ConflictOutcome.CONFLICT
            ConflictResolutionStrategy.LATEST_WINS -> {
                when {
                    clientUpdatedAt > serverUpdatedAt -> ConflictOutcome.CLIENT_WINS
                    serverUpdatedAt > clientUpdatedAt -> ConflictOutcome.SERVER_WINS
                    clientVersion > serverVersion -> ConflictOutcome.CLIENT_WINS
                    else -> ConflictOutcome.SERVER_WINS
                }
            }
        }
    }

    private enum class ConflictOutcome {
        CLIENT_WINS, SERVER_WINS, CONFLICT
    }
}

// ─── Result types ────────────────────────────────────────

sealed class FirestorePushResult {
    data object Success : FirestorePushResult()
    data class Conflict(val serverData: String?) : FirestorePushResult()
    data class NetworkError(val message: String) : FirestorePushResult()
    data class Failure(val message: String) : FirestorePushResult()
    data object NotAuthenticated : FirestorePushResult()
}

sealed class FirestorePullResult {
    data class Success(val documents: List<RemoteDocument>) : FirestorePullResult()
    data object NetworkError : FirestorePullResult()
    data class Failure(val message: String) : FirestorePullResult()
    data object NotAuthenticated : FirestorePullResult()
}

data class RemoteDocument(
    val id: String,
    val data: Map<String, Any?>,
    val version: Long,
    val updatedAt: Long
)
