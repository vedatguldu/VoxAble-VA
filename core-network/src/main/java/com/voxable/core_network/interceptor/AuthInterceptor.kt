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

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking {
            try {
                currentUser.getIdToken(false).await().token
            } catch (e: Exception) {
                null
            }
        }

        val newRequest = originalRequest.newBuilder().apply {
            if (token != null) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()

        return chain.proceed(newRequest)
    }
}
