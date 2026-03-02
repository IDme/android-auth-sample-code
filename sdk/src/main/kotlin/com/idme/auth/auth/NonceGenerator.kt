package com.idme.auth.auth

import com.idme.auth.utilities.Base64URL
import java.security.SecureRandom

/** Generates a cryptographically random nonce for OIDC flows. */
object NonceGenerator {
    /** Generates a random nonce string. */
    fun generate(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64URL.encode(bytes)
    }
}
