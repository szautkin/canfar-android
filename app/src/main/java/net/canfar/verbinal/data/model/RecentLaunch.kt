package net.canfar.verbinal.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RecentLaunch(
    val name: String = "",
    val type: String = "",
    val image: String = "",
    val imageLabel: String = "",
    val project: String = "",
    val resourceType: String = "flexible",
    val cores: Int = 0,
    val ram: Int = 0,
    val gpus: Int = 0,
    val launchedAt: Long = 0L, // epoch millis
)
