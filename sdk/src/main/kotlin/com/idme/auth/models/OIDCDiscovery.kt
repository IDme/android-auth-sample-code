package com.idme.auth.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** OIDC discovery document from `.well-known/openid-configuration`. */
@Serializable
data class OIDCDiscovery(
    val issuer: String,
    @SerialName("authorization_endpoint") val authorizationEndpoint: String,
    @SerialName("token_endpoint") val tokenEndpoint: String,
    @SerialName("userinfo_endpoint") val userinfoEndpoint: String,
    @SerialName("jwks_uri") val jwksUri: String
)
