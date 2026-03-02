package com.idme.auth.auth

import com.idme.auth.utilities.Base64URL
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Generates PKCE code verifier and challenge per RFC 7636.
 */
class PKCEGenerator {
    /** The code verifier (43-128 character URL-safe string). */
    val codeVerifier: String

    /** The S256 code challenge derived from the verifier. */
    val codeChallenge: String

    /** The challenge method (always "S256"). */
    val codeChallengeMethod: String = "S256"

    constructor() {
        codeVerifier = generateVerifier()
        codeChallenge = generateChallenge(codeVerifier)
    }

    /** Initializes with a known verifier (for testing). */
    constructor(codeVerifier: String) {
        this.codeVerifier = codeVerifier
        this.codeChallenge = generateChallenge(codeVerifier)
    }

    companion object {
        /** Generates a cryptographically random code verifier. */
        private fun generateVerifier(): String {
            val bytes = ByteArray(32)
            SecureRandom().nextBytes(bytes)
            return Base64URL.encode(bytes)
        }

        /** Generates an S256 code challenge from the verifier. */
        fun generateChallenge(verifier: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(verifier.toByteArray(Charsets.UTF_8))
            return Base64URL.encode(hash)
        }
    }
}
