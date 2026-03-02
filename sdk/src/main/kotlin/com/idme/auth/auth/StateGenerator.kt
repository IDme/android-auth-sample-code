package com.idme.auth.auth

import com.idme.auth.utilities.Base64URL
import java.security.SecureRandom

/** Generates a cryptographically random state parameter for CSRF protection. */
object StateGenerator {
    /** Generates a random state string. */
    fun generate(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64URL.encode(bytes)
    }
}
