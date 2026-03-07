package net.canfar.verbinal.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: AuthTokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenProvider.token
        return if (!token.isNullOrEmpty() && request.header("Authorization") == null) {
            val newRequest =
                request
                    .newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(request)
        }
    }
}
