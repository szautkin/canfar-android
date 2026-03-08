package net.canfar.verbinal.ui

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.canfar.verbinal.data.model.UserInfo
import net.canfar.verbinal.data.repository.AuthRepository
import javax.inject.Inject

data class MainUiState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val requiresBiometric: Boolean = false,
    val username: String = "",
    val statusMessage: String = "",
    val userInfo: UserInfo? = null,
)

@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = "Checking authentication...")

            if (!authRepository.hasStoredSession()) {
                _uiState.value =
                    _uiState.value.copy(
                        statusMessage = "Please log in to continue.",
                        isLoading = false,
                    )
                return@launch
            }

            if (isBiometricAvailable()) {
                _uiState.value =
                    _uiState.value.copy(
                        requiresBiometric = true,
                        isLoading = false,
                    )
            } else {
                authenticateWithStoredSession()
            }
        }
    }

    private fun isBiometricAvailable(): Boolean {
        val result = BiometricManager.from(appContext).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK,
        )
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun onBiometricSuccess() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(requiresBiometric = false, isLoading = true)
            authenticateWithStoredSession()
        }
    }

    fun onBiometricFailure() {
        _uiState.value =
            _uiState.value.copy(
                requiresBiometric = false,
                isLoading = false,
                statusMessage = "Please log in.",
            )
    }

    private suspend fun authenticateWithStoredSession() {
        // Try existing token first
        val (token, _) = authRepository.loadStoredToken()
        if (token != null) {
            val validatedUser = authRepository.validateToken(token)
            if (validatedUser != null) {
                val userInfo = authRepository.getUserInfo(validatedUser)
                _uiState.value =
                    _uiState.value.copy(
                        isAuthenticated = true,
                        username = validatedUser,
                        userInfo = userInfo,
                        statusMessage = "Welcome, $validatedUser",
                        isLoading = false,
                    )
                return
            }
        }

        // Token expired — re-login with stored credentials
        val result = authRepository.loginWithStoredCredentials()
        if (result != null && result.success && result.username != null) {
            val userInfo = authRepository.getUserInfo(result.username)
            _uiState.value =
                _uiState.value.copy(
                    isAuthenticated = true,
                    username = result.username,
                    userInfo = userInfo,
                    statusMessage = "Welcome, ${result.username}",
                    isLoading = false,
                )
        } else {
            _uiState.value =
                _uiState.value.copy(
                    statusMessage = "Session expired. Please log in.",
                    isLoading = false,
                )
        }
    }

    fun updateAuthState(
        username: String,
        userInfo: UserInfo?,
    ) {
        _uiState.value =
            _uiState.value.copy(
                isAuthenticated = true,
                username = username,
                userInfo = userInfo,
                statusMessage = "Welcome, $username",
            )
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = MainUiState(statusMessage = "Logged out.")
    }
}
