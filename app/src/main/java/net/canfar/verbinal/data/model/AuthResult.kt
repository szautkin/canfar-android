package net.canfar.verbinal.data.model

data class AuthResult(
    val success: Boolean,
    val token: String? = null,
    val username: String? = null,
    val errorMessage: String? = null,
)
