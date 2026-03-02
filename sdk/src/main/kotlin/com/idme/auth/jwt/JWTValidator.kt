package com.idme.auth.jwt

import com.idme.auth.errors.IDmeAuthError
import java.security.Signature

/** Validates JWT tokens: signature verification (RS256) and claims checking. */
class JWTValidator(
    private val jwksFetcher: JWKSFetching,
    private val issuer: String,
    private val clientId: String
) {
    /** Validates an ID token: decodes, verifies RS256 signature, and checks claims. */
    suspend fun validate(idToken: String, nonce: String?) {
        val decoded = JWTDecoder.decode(idToken)

        if (decoded.header.alg != "RS256") {
            throw IDmeAuthError.InvalidJWT("Unsupported algorithm: ${decoded.header.alg}")
        }

        // Fetch JWKS and find the matching RSA key
        val jwks = jwksFetcher.fetchJWKS()
        val rsaKeys = jwks.keys.filter { it.kty == "RSA" && it.n != null && it.e != null }
        val jwk = if (decoded.header.kid != null) {
            rsaKeys.firstOrNull { it.kid == decoded.header.kid }
                ?: throw IDmeAuthError.JWKSKeyNotFound(decoded.header.kid)
        } else {
            rsaKeys.firstOrNull()
                ?: throw IDmeAuthError.InvalidJWT("No RSA keys in JWKS and no kid in JWT header")
        }

        // Verify RS256 signature
        val publicKey = RSAKeyConverter.publicKey(jwk.n!!, jwk.e!!)
        val signedData = decoded.signedPortion.toByteArray(Charsets.UTF_8)

        val sig = Signature.getInstance("SHA256withRSA")
        sig.initVerify(publicKey)
        sig.update(signedData)

        if (!sig.verify(decoded.signatureData)) {
            throw IDmeAuthError.JWTSignatureInvalid
        }

        // Validate claims
        validateClaims(decoded.payload, nonce)
    }

    private fun validateClaims(payload: Map<String, Any>, nonce: String?) {
        // Issuer
        val iss = payload["iss"] as? String
        if (iss != null && iss != issuer) {
            throw IDmeAuthError.JWTClaimInvalid("iss", "Expected $issuer, got $iss")
        }

        // Audience
        val aud = payload["aud"]
        when (aud) {
            is String -> {
                if (aud != clientId) {
                    throw IDmeAuthError.JWTClaimInvalid("aud", "Expected $clientId, got $aud")
                }
            }
            is List<*> -> {
                if (!aud.contains(clientId)) {
                    throw IDmeAuthError.JWTClaimInvalid("aud", "Client ID not in audience array")
                }
            }
        }

        // Expiration
        val exp = payload["exp"]
        if (exp != null) {
            val expTime = when (exp) {
                is Number -> exp.toLong() * 1000
                else -> null
            }
            if (expTime != null && System.currentTimeMillis() >= expTime) {
                throw IDmeAuthError.JWTClaimInvalid("exp", "Token has expired")
            }
        }

        // Nonce (OIDC)
        if (nonce != null) {
            val tokenNonce = payload["nonce"] as? String
                ?: throw IDmeAuthError.JWTClaimInvalid("nonce", "Missing nonce in token")
            if (tokenNonce != nonce) {
                throw IDmeAuthError.JWTClaimInvalid("nonce", "Nonce mismatch")
            }
        }
    }
}
