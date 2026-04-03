package com.voxable.core_network.interceptor

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor() : Interceptor {

    @Volatile
    private var cachedToken: String? = null

    @Volatile
    private var tokenExpiryMs: Long = 0L

    // Firebase ID token varsayılan olarak 1 saat geçerli; 55 dk'da yenile
    private companion object {
        const val TOKEN_REFRESH_MARGIN_MS = 55 * 60 * 1000L
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            return chain.proceed(originalRequest)
        }

        val token = getValidToken(currentUser)

        val newRequest = originalRequest.newBuilder().apply {
            if (token != null) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()

        return chain.proceed(newRequest)
    }

    private fun getValidToken(user: com.google.firebase.auth.FirebaseUser): String? {
        val now = System.currentTimeMillis()
        cachedToken?.let { token ->
            if (now < tokenExpiryMs) return token
        }

        return runBlocking {
            try {
                val result = user.getIdToken(false).await()
                cachedToken = result.token
                tokenExpiryMs = now + TOKEN_REFRESH_MARGIN_MS
                result.token
            } catch (e: Exception) {
                cachedToken = null
                tokenExpiryMs = 0L
                null
            }
        }
    }
}
