package com.idme.auth.models

import kotlinx.serialization.Serializable

/** JSON Web Key Set response. */
@Serializable
data class JWKS(
    val keys: List<JWK>
)

/** A single JSON Web Key. */
@Serializable
data class JWK(
    val kty: String,
    val kid: String? = null,
    val use: String? = null,
    val alg: String? = null,
    val n: String? = null,  // RSA modulus (Base64URL) -- null for non-RSA keys
    val e: String? = null   // RSA exponent (Base64URL) -- null for non-RSA keys
)
