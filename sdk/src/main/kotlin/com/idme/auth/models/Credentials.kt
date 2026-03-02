package com.idme.auth.models

import kotlinx.serialization.Serializable

/** Public model representing the authenticated user's tokens. */
@Serializable
data class Credentials(
    /** The OAuth access token. */
    val accessToken: String,

    /** The OAuth refresh token (if provided). */
    val refreshToken: String? = null,

    /** The OIDC ID token (if OIDC mode was used). */
    val idToken: String? = null,

    /** The token type (typically "Bearer"). */
    val tokenType: String,

    /** The epoch millisecond timestamp when the access token expires. */
    val expiresAt: Long
) {
    /** Whether the access token has expired. */
    val isExpired: Boolean
        get() = System.currentTimeMillis() >= expiresAt

    /** Whether the access token will expire within the given number of seconds. */
    fun expiresWithin(seconds: Long): Boolean =
        System.currentTimeMillis() + (seconds * 1000) >= expiresAt
}
