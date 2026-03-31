package com.voxable.core.base

import com.voxable.core.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Tüm repository'ler için temel sınıf.
 * Ortak hata yönetimi ve coroutine dispatcher'ları sağlar.
 */
abstract class BaseRepository {

    protected suspend fun <T> safeCall(
        call: suspend () -> T
    ): Resource<T> {
        return try {
            Resource.Success(call())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Bilinmeyen bir hata oluştu")
        }
    }

    protected suspend fun <T> safeCallOnIo(
        call: suspend () -> T
    ): Resource<T> {
        return withContext(Dispatchers.IO) {
            safeCall(call)
        }
    }
}
