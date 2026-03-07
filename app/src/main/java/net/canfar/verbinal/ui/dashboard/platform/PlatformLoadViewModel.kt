package net.canfar.verbinal.ui.dashboard.platform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.canfar.verbinal.data.repository.PlatformRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class PlatformLoadUiState(
    val isLoading: Boolean = false,
    val cpuUsed: Double = 0.0,
    val cpuAvailable: Double = 0.0,
    val cpuPercent: Double = 0.0,
    val ramUsedGB: Double = 0.0,
    val ramAvailableGB: Double = 0.0,
    val ramPercent: Double = 0.0,
    val lastUpdate: String = "",
    val errorMessage: String = "",
    val hasError: Boolean = false,
)

@HiltViewModel
class PlatformLoadViewModel
@Inject
constructor(
    private val platformRepository: PlatformRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlatformLoadUiState())
    val uiState: StateFlow<PlatformLoadUiState> = _uiState.asStateFlow()

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, hasError = false)
            try {
                val stats = platformRepository.getStats()
                if (stats != null) {
                    val cpuUsed = stats.cores.requestedCPUCores
                    val cpuAvailable = stats.cores.cpuCoresAvailable
                    val cpuPercent = if (cpuAvailable > 0) cpuUsed / cpuAvailable * 100 else 0.0

                    val ramUsedGB = parseRamGB(stats.ram.requestedRAM)
                    val ramAvailableGB = parseRamGB(stats.ram.ramAvailable)
                    val ramPercent = if (ramAvailableGB > 0) ramUsedGB / ramAvailableGB * 100 else 0.0

                    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            cpuUsed = cpuUsed,
                            cpuAvailable = cpuAvailable,
                            cpuPercent = cpuPercent,
                            ramUsedGB = ramUsedGB,
                            ramAvailableGB = ramAvailableGB,
                            ramPercent = ramPercent,
                            lastUpdate = timeFormat.format(Date()),
                        )
                }
            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load stats: ${e.message}",
                        hasError = true,
                    )
            }
        }
    }

    private fun parseRamGB(ramString: String): Double {
        if (ramString.isEmpty()) return 0.0
        val numeric = ramString.trimEnd('G', 'g', 'B', 'b', ' ')
        return numeric.toDoubleOrNull() ?: 0.0
    }
}
