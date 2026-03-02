package com.idme.auth.jwt

import com.idme.auth.errors.IDmeAuthError
import com.idme.auth.utilities.Base64URL
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec

/**
 * Converts JWK RSA components (n, e) to a Java PublicKey for signature verification.
 */
object RSAKeyConverter {

    /**
     * Converts a JWK to a Java PublicKey.
     *
     * @param n The RSA modulus as a Base64URL-encoded string.
     * @param e The RSA exponent as a Base64URL-encoded string.
     * @return A PublicKey suitable for RS256 verification.
     */
    fun publicKey(n: String, e: String): PublicKey {
        val modulusBytes = Base64URL.decode(n)
            ?: throw IDmeAuthError.InvalidJWT("Invalid Base64URL in JWK modulus")
        val exponentBytes = Base64URL.decode(e)
            ?: throw IDmeAuthError.InvalidJWT("Invalid Base64URL in JWK exponent")

        val modulus = BigInteger(1, modulusBytes)
        val exponent = BigInteger(1, exponentBytes)

        val keySpec = RSAPublicKeySpec(modulus, exponent)
        return try {
            KeyFactory.getInstance("RSA").generatePublic(keySpec)
        } catch (ex: Exception) {
            throw IDmeAuthError.InvalidJWT("Failed to create RSA public key: ${ex.localizedMessage}")
        }
    }
}
