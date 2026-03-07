package net.canfar.verbinal.data.model

data class SessionLaunchParams(
    val type: String = "notebook",
    val name: String = "",
    val image: String = "",
    val cores: Int = 2,
    val ram: Int = 8,
    val gpus: Int = 0,
    val cmd: String? = null,
    val registryUsername: String? = null,
    val registrySecret: String? = null,
)
