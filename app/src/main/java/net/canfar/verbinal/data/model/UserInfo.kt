package net.canfar.verbinal.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    @SerialName("username") val username: String = "",
    @SerialName("email") val email: String? = null,
    @SerialName("firstName") val firstName: String? = null,
    @SerialName("lastName") val lastName: String? = null,
    @SerialName("institute") val institute: String? = null,
    @SerialName("internalID") val internalId: String? = null,
)
