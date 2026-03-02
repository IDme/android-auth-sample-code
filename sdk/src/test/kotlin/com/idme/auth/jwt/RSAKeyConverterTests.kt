package com.idme.auth.jwt

import com.idme.auth.errors.IDmeAuthError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.security.interfaces.RSAPublicKey

class RSAKeyConverterTests {

    @Test
    fun `creates PublicKey from JWK components`() {
        // Well-known RSA 2048 test key (from RFC 7517)
        val n = "0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx4cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMstn64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2QvzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbISD08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqbw0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw"
        val e = "AQAB"

        val key = RSAKeyConverter.publicKey(n, e)

        assertNotNull(key)
        assertEquals("RSA", key.algorithm)
        val rsaKey = key as RSAPublicKey
        assertNotNull(rsaKey.modulus)
        assertNotNull(rsaKey.publicExponent)
    }

    @Test(expected = IDmeAuthError.InvalidJWT::class)
    fun `throws on invalid Base64URL input`() {
        RSAKeyConverter.publicKey("!!!invalid!!!", "AQAB")
    }
}
