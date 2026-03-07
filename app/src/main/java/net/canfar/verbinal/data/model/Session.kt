package net.canfar.verbinal.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SkahaSessionResponse(
    @SerialName("id") val id: String = "",
    @SerialName("userid") val userId: String = "",
    @SerialName("runAsUID") val runAsUID: String = "",
    @SerialName("runAsGID") val runAsGID: String = "",
    @SerialName("supplementalGroups") val supplementalGroups: List<Int>? = null,
    @SerialName("image") val image: String = "",
    @SerialName("type") val type: String = "",
    @SerialName("status") val status: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("startTime") val startTime: String = "",
    @SerialName("expiryTime") val expiryTime: String = "",
    @SerialName("connectURL") val connectURL: String = "",
    @SerialName("requestedRAM") val requestedRAM: String? = null,
    @SerialName("requestedCPUCores") val requestedCPUCores: String? = null,
    @SerialName("requestedGPUCores") val requestedGPUCores: String? = null,
    @SerialName("ramInUse") val ramInUse: String? = null,
    @SerialName("cpuCoresInUse") val cpuCoresInUse: String? = null,
    @SerialName("isFixedResources") val isFixedResources: Boolean? = null,
)

data class Session(
    val id: String = "",
    val sessionType: String = "",
    val sessionName: String = "",
    val status: String = "",
    val containerImage: String = "",
    val startedTime: String = "",
    val expiresTime: String = "",
    val memoryUsage: String? = null,
    val memoryAllocated: String = "",
    val cpuUsage: String? = null,
    val cpuAllocated: String = "",
    val gpuAllocated: String? = null,
    val isFixedResources: Boolean = true,
    val connectUrl: String? = null,
    val requestedRAM: String? = null,
    val requestedCPU: String? = null,
    val requestedGPU: String? = null,
)

fun SkahaSessionResponse.toSession() = Session(
    id = id,
    sessionType = type,
    sessionName = name,
    status = status,
    containerImage = image,
    startedTime = startTime,
    expiresTime = expiryTime,
    connectUrl = connectURL,
    memoryAllocated = requestedRAM ?: "",
    memoryUsage = ramInUse,
    cpuAllocated = requestedCPUCores ?: "",
    cpuUsage = cpuCoresInUse,
    gpuAllocated = requestedGPUCores,
    isFixedResources = isFixedResources ?: true,
    requestedRAM = requestedRAM,
    requestedCPU = requestedCPUCores,
    requestedGPU = requestedGPUCores,
)
