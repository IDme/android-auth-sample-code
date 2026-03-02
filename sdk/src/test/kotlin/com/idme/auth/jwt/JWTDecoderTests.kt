package com.idme.auth.jwt

import com.idme.auth.errors.IDmeAuthError
import com.idme.auth.utilities.Base64URL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class JWTDecoderTests {

    @Test
    fun `decodes a valid JWT`() {
        val header = Base64URL.encode(
            """{"alg":"RS256","kid":"test-kid","typ":"JWT"}""".toByteArray()
        )
        val payload = Base64URL.encode(
            """{"sub":"user-123","iss":"https://api.id.me","aud":"client-id","exp":9999999999}""".toByteArray()
        )
        val signature = Base64URL.encode(ByteArray(32) { 0xAB.toByte() })

        val jwt = "$header.$payload.$signature"
        val decoded = JWTDecoder.decode(jwt)

        assertEquals("RS256", decoded.header.alg)
        assertEquals("test-kid", decoded.header.kid)
        assertEquals("user-123", decoded.payload["sub"])
        assertEquals("https://api.id.me", decoded.payload["iss"])
        assertEquals("$header.$payload", decoded.signedPortion)
    }

    @Test(expected = IDmeAuthError.InvalidJWT::class)
    fun `rejects JWT with missing parts`() {
        JWTDecoder.decode("header.payload")
    }

    @Test(expected = IDmeAuthError.InvalidJWT::class)
    fun `rejects JWT with invalid header`() {
        JWTDecoder.decode("not-valid-base64.payload.signature")
    }

    @Test(expected = IDmeAuthError.InvalidJWT::class)
    fun `rejects JWT with missing alg`() {
        val header = Base64URL.encode(
            """{"kid":"test-kid","typ":"JWT"}""".toByteArray()
        )
        val payload = Base64URL.encode("{}".toByteArray())
        val signature = Base64URL.encode(byteArrayOf(0x01))

        JWTDecoder.decode("$header.$payload.$signature")
    }

    @Test
    fun `decodes JWT without kid`() {
        val header = Base64URL.encode(
            """{"alg":"RS256","typ":"JWT"}""".toByteArray()
        )
        val payload = Base64URL.encode(
            """{"sub":"user-123"}""".toByteArray()
        )
        val signature = Base64URL.encode(byteArrayOf(0x01))

        val decoded = JWTDecoder.decode("$header.$payload.$signature")

        assertEquals("RS256", decoded.header.alg)
        assertNull(decoded.header.kid)
    }
}
