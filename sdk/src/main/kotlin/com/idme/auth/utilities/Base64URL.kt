package com.idme.auth.utilities

import com.idme.auth.errors.IDmeAuthError
import java.util.Base64

/** Base64URL encoding/decoding per RFC 4648 section 5. */
object Base64URL {

    /** Encodes raw bytes to a Base64URL string (no padding). */
    fun encode(data: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(data)

    /** Decodes a Base64URL string to raw bytes. Throws [IDmeAuthError.InvalidJWT] on invalid input. */
    fun decode(string: String): ByteArray =
        try {
            Base64.getUrlDecoder().decode(string)
        } catch (e: Exception) {
            throw IDmeAuthError.InvalidJWT("Invalid Base64URL encoding: ${e.message}")
        }
}
