package net.canfar.verbinal.data.repository

import net.canfar.verbinal.data.model.AuthResult
import net.canfar.verbinal.data.model.UserInfo
import net.canfar.verbinal.data.remote.AuthApi
import net.canfar.verbinal.data.remote.AuthTokenProvider
import net.canfar.verbinal.data.remote.UserApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository
@Inject
constructor(
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val tokenStorage: TokenStorage,
    private val tokenProvider: AuthTokenProvider,
) {
    var isAuthenticated: Boolean = false
        private set
    var currentUsername: String? = null
        private set

    suspend fun login(
        username: String,
        password: String,
        rememberMe: Boolean,
    ): AuthResult {
        return try {
            val response = authApi.login(username, password)
            if (response.isSuccessful) {
                val token =
                    response.body()?.trim() ?: return AuthResult(
                        success = false,
                        errorMessage = "Empty token received",
                    )
                tokenProvider.token = token
                currentUsername = username
                isAuthenticated = true

                if (rememberMe) {
                    tokenStorage.saveToken(token, username)
                    tokenStorage.saveCredentials(username, password)
                } else {
                    tokenStorage.clearAll()
                }

                AuthResult(success = true, token = token, username = username)
            } else {
                val errorMsg =
                    if (response.code() == 401) {
                        "Invalid username or password."
                    } else {
                        "Login failed: ${response.code()}"
                    }
                AuthResult(success = false, errorMessage = errorMsg)
            }
        } catch (e: Exception) {
            AuthResult(success = false, errorMessage = "Network error: ${e.message}")
        }
    }

    suspend fun loginWithStoredCredentials(): AuthResult? {
        val (username, password) = tokenStorage.loadCredentials()
        if (username != null && password != null) {
            return login(username, password, rememberMe = true)
        }
        return null
    }

    suspend fun validateToken(token: String): String? = try {
        val response = authApi.whoami("Bearer $token")
        if (response.isSuccessful) {
            val body = response.body()?.trim()
            val username = body?.let { parseUsernameFromResponse(it) }
            if (username != null) {
                tokenProvider.token = token
                currentUsername = username
                isAuthenticated = true
            }
            username
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }

    private fun parseUsernameFromResponse(body: String): String? {
        if (!body.contains("<")) return body
        val regex = Regex("<internalID>([^<]+)</internalID>")
        return regex.find(body)?.groupValues?.get(1)?.trim()
    }

    suspend fun getUserInfo(username: String): UserInfo? = try {
        val response = userApi.getUserInfo(username)
        if (response.isSuccessful) response.body() else null
    } catch (_: Exception) {
        null
    }

    fun logout() {
        tokenProvider.clear()
        currentUsername = null
        isAuthenticated = false
        tokenStorage.clearAll()
    }

    fun hasStoredSession(): Boolean = tokenStorage.hasStoredSession()

    fun loadStoredToken(): Pair<String?, String?> = tokenStorage.loadToken()
}
