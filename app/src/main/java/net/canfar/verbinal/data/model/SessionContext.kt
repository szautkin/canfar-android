package net.canfar.verbinal.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SessionContext(
    @SerialName("cores") val cores: ResourceOptions = ResourceOptions(),
    @SerialName("memoryGB") val memoryGB: ResourceOptions = ResourceOptions(),
    @SerialName("gpus") val gpus: GpuOptions = GpuOptions(),
)

@Serializable
data class ResourceOptions(
    @SerialName("default") val default_: Int = 0,
    @SerialName("options") val options: List<Int> = emptyList(),
)

@Serializable
data class GpuOptions(
    @SerialName("options") val options: List<Int> = emptyList(),
)
