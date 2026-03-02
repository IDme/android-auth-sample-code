package com.idme.auth.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Internal model representing the JSON response from `/oauth/token`. */
@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("id_token") val idToken: String? = null,
    val scope: String? = null
) {
    /** Converts to the public [Credentials] type. */
    fun toCredentials(): Credentials = Credentials(
        accessToken = accessToken,
        refreshToken = refreshToken,
        idToken = idToken,
        tokenType = tokenType,
        expiresAt = System.currentTimeMillis() + (expiresIn * 1000L)
    )
}
