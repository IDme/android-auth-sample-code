package com.idme.auth.utilities

import java.util.Base64

/** Base64URL encoding/decoding per RFC 4648 section 5. */
object Base64URL {

    /** Encodes raw bytes to a Base64URL string (no padding). */
    fun encode(data: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(data)

    /** Decodes a Base64URL string to raw bytes. */
    fun decode(string: String): ByteArray? =
        try {
            Base64.getUrlDecoder().decode(string)
        } catch (_: Exception) {
            null
        }
}
