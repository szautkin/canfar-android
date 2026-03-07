package net.canfar.verbinal.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SkahaStatsResponse(
    @SerialName("cores") val cores: CoreStats = CoreStats(),
    @SerialName("ram") val ram: RamStats = RamStats(),
)

@Serializable
data class CoreStats(
    @SerialName("requestedCPUCores") val requestedCPUCores: Double = 0.0,
    @SerialName("cpuCoresAvailable") val cpuCoresAvailable: Double = 0.0,
)

@Serializable
data class RamStats(
    @SerialName("requestedRAM") val requestedRAM: String = "",
    @SerialName("ramAvailable") val ramAvailable: String = "",
)
