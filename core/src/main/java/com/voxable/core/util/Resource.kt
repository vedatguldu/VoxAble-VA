package com.voxable.core.util

/**
 * Ağ veya veritabanı işlemlerinin sonucunu saran sealed class.
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}

/**
 * Resource üzerinde kolay map işlemi.
 */
inline fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> {
    return when (this) {
        is Resource.Success -> Resource.Success(transform(data))
        is Resource.Error -> Resource.Error(message, throwable)
        is Resource.Loading -> Resource.Loading
    }
}

/**
 * Resource sonucuna göre dallanma.
 */
inline fun <T> Resource<T>.onSuccess(action: (T) -> Unit): Resource<T> {
    if (this is Resource.Success) action(data)
    return this
}

inline fun <T> Resource<T>.onError(action: (String) -> Unit): Resource<T> {
    if (this is Resource.Error) action(message)
    return this
}
