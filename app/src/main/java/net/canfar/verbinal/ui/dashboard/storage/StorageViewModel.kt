package net.canfar.verbinal.ui.dashboard.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.canfar.verbinal.data.repository.StorageRepository
import javax.inject.Inject
import kotlin.math.roundToInt

data class StorageUiState(
    val isLoading: Boolean = false,
    val usedGB: Double = 0.0,
    val quotaGB: Double = 0.0,
    val usagePercent: Double = 0.0,
    val isWarning: Boolean = false,
    val errorMessage: String = "",
    val hasError: Boolean = false,
    val hasData: Boolean = false,
)

@HiltViewModel
class StorageViewModel
@Inject
constructor(
    private val storageRepository: StorageRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StorageUiState())
    val uiState: StateFlow<StorageUiState> = _uiState.asStateFlow()

    fun loadQuota(username: String) {
        if (username.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, hasError = false)
            try {
                val quota = storageRepository.getQuota(username)
                if (quota != null) {
                    val usedGB = (quota.usedGB * 100).roundToInt() / 100.0
                    val quotaGB = (quota.quotaGB * 100).roundToInt() / 100.0
                    val usagePercent = (quota.usagePercent * 10).roundToInt() / 10.0
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            usedGB = usedGB,
                            quotaGB = quotaGB,
                            usagePercent = usagePercent,
                            isWarning = usagePercent > 90,
                            hasData = true,
                        )
                }
            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load storage: ${e.message}",
                        hasError = true,
                    )
            }
        }
    }
}
