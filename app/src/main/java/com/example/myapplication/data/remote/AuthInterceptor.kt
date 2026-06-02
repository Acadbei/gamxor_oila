package com.example.myapplication.data.remote

import com.example.myapplication.data.local.LocalUserStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val userStore: LocalUserStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        val authorizationValue = runCatching { userStore.loadAuthToken() }
            .getOrNull()
            ?.toAuthorizationHeaderValue()

        if (authorizationValue != null) {
            requestBuilder.header(AUTHORIZATION_HEADER, authorizationValue)
        }

        return chain.proceed(requestBuilder.build())
    }

    private fun String.toAuthorizationHeaderValue(): String? {
        val sanitized = trim()
        if (sanitized.isBlank()) return null
        return if (sanitized.contains(' ')) sanitized else "$defaultScheme $sanitized"
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val defaultScheme = "Token"
    }
}
