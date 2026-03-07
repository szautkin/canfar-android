package net.canfar.verbinal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val username: String = "",
    val statusMessage: String = "",
    val userInfo: UserInfo? = null,
)

@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = "Checking authentication...")

            val (token, username) = authRepository.loadStoredToken()
            if (token != null && username != null) {
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
                } else {
                    _uiState.value =
                        _uiState.value.copy(
                            statusMessage = "Session expired. Please log in.",
                            isLoading = false,
                        )
                }
            } else {
                _uiState.value =
                    _uiState.value.copy(
                        statusMessage = "Please log in to continue.",
                        isLoading = false,
                    )
            }
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
