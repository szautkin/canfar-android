package net.canfar.verbinal.ui.dashboard.sessions

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.canfar.verbinal.data.model.Session
import net.canfar.verbinal.data.repository.SessionRepository
import javax.inject.Inject

data class SessionListUiState(
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val hasError: Boolean = false,
    val sessions: List<Session> = emptyList(),
    val isPolling: Boolean = false,
    val pollCountdown: Int = 0,
    val renewMessage: String? = null,
)

@HiltViewModel
class SessionListViewModel
@Inject
constructor(
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SessionListUiState())
    val uiState: StateFlow<SessionListUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null

    var onSessionsRefreshed: (() -> Unit)? = null

    fun loadSessions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, hasError = false, errorMessage = "")
            try {
                val sessions = sessionRepository.getSessions()
                _uiState.value = _uiState.value.copy(isLoading = false, sessions = sessions)
                onSessionsRefreshed?.invoke()

                if (hasPendingSessions()) startPolling() else stopPolling()
            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load sessions: ${e.message}",
                        hasError = true,
                    )
            }
        }
    }

    private fun hasPendingSessions(): Boolean = _uiState.value.sessions.any { it.status in listOf("Pending", "Terminating") }

    private fun startPolling() {
        if (pollJob != null) return
        _uiState.value = _uiState.value.copy(isPolling = true)
        pollJob =
            viewModelScope.launch {
                while (true) {
                    for (i in 60 downTo 1) {
                        _uiState.value = _uiState.value.copy(pollCountdown = i)
                        delay(1000)
                    }
                    loadSessionsInternal()
                    if (!hasPendingSessions()) break
                }
                stopPolling()
            }
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
        _uiState.value = _uiState.value.copy(isPolling = false)
    }

    private suspend fun loadSessionsInternal() {
        try {
            val sessions = sessionRepository.getSessions()
            _uiState.value = _uiState.value.copy(sessions = sessions)
            onSessionsRefreshed?.invoke()
        } catch (_: Exception) {
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            val success = sessionRepository.deleteSession(sessionId)
            if (success) {
                _uiState.value =
                    _uiState.value.copy(
                        sessions = _uiState.value.sessions.filter { it.id != sessionId },
                    )
                delay(3000)
                loadSessions()
            }
        }
    }

    fun renewSession(sessionId: String) {
        viewModelScope.launch {
            try {
                sessionRepository.renewSession(sessionId)
                _uiState.value = _uiState.value.copy(renewMessage = "Session extended successfully!")
                loadSessions()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(renewMessage = "Extend failed: ${e.message}")
            }
        }
    }

    fun clearRenewMessage() {
        _uiState.value = _uiState.value.copy(renewMessage = null)
    }

    suspend fun getSessionEvents(sessionId: String): String? = sessionRepository.getSessionEvents(sessionId)

    suspend fun getSessionLogs(sessionId: String): String? = sessionRepository.getSessionLogs(sessionId)

    fun openSessionInBrowser(
        session: Session,
        context: Context,
    ) {
        if (!session.connectUrl.isNullOrEmpty() && session.status == "Running") {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(session.connectUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
