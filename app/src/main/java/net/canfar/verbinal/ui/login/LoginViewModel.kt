package net.canfar.verbinal.ui.login

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

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoggingIn: Boolean = false,
    val errorMessage: String = "",
    val hasError: Boolean = false,
    val rememberMe: Boolean = true,
    val loginSuccess: Boolean = false,
    val loggedInUsername: String = "",
    val loggedInUserInfo: UserInfo? = null,
)

@HiltViewModel
class LoginViewModel
@Inject
constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun onRememberMeChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(rememberMe = value)
    }

    fun login() {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.value =
                state.copy(
                    errorMessage = "Username and password are required.",
                    hasError = true,
                )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoggingIn = true, hasError = false, errorMessage = "")

            val result = authRepository.login(state.username, state.password, state.rememberMe)

            if (result.success) {
                val userInfo = authRepository.getUserInfo(state.username)
                _uiState.value =
                    _uiState.value.copy(
                        isLoggingIn = false,
                        hasError = false,
                        loginSuccess = true,
                        loggedInUsername = state.username,
                        loggedInUserInfo = userInfo,
                        password = "",
                    )
            } else {
                _uiState.value =
                    _uiState.value.copy(
                        isLoggingIn = false,
                        errorMessage = result.errorMessage ?: "Login failed.",
                        hasError = true,
                    )
            }
        }
    }

    fun resetLoginSuccess() {
        _uiState.value = _uiState.value.copy(loginSuccess = false)
    }
}
