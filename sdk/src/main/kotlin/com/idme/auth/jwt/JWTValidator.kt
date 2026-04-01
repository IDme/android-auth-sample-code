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
        val now = System.currentTimeMillis()

        // Issuer — mandatory per OpenID Connect Core 1.0 Section 3.1.3.7
        val iss = payload["iss"] as? String
            ?: throw IDmeAuthError.JWTClaimInvalid("iss", "Missing issuer claim")
        if (iss != issuer) {
            throw IDmeAuthError.JWTClaimInvalid("iss", "Expected $issuer, got $iss")
        }

        // Audience — mandatory per OpenID Connect Core 1.0 Section 3.1.3.7
        val aud = payload["aud"]
            ?: throw IDmeAuthError.JWTClaimInvalid("aud", "Missing audience claim")
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

        // Expiration — with clock skew tolerance
        val exp = payload["exp"]
        if (exp != null) {
            val expTime = (exp as? Number)?.toLong()?.times(1000)
            if (expTime != null && now >= expTime + CLOCK_SKEW_MS) {
                throw IDmeAuthError.JWTClaimInvalid("exp", "Token has expired")
            }
        }

        // Not Before — reject tokens that are not yet valid
        val nbf = payload["nbf"]
        if (nbf != null) {
            val nbfTime = (nbf as? Number)?.toLong()?.times(1000)
            if (nbfTime != null && now < nbfTime - CLOCK_SKEW_MS) {
                throw IDmeAuthError.JWTClaimInvalid("nbf", "Token not yet valid")
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

    companion object {
        private const val CLOCK_SKEW_MS = 30_000L // 30 seconds
    }
}
