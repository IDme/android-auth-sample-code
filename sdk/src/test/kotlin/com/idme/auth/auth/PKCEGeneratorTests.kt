package com.idme.auth.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class PKCEGeneratorTests {

    @Test
    fun `code verifier is 43 characters (Base64URL of 32 bytes)`() {
        val pkce = PKCEGenerator()
        assertEquals(43, pkce.codeVerifier.length)
    }

    @Test
    fun `code verifier contains only URL-safe characters`() {
        val pkce = PKCEGenerator()
        val urlSafe = Regex("^[A-Za-z0-9_-]+$")
        assertTrue(urlSafe.matches(pkce.codeVerifier))
    }

    @Test
    fun `code challenge is SHA256 of the verifier`() {
        val verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
        val pkce = PKCEGenerator(verifier)

        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(verifier.toByteArray(Charsets.UTF_8))
        val expected = com.idme.auth.utilities.Base64URL.encode(hash)

        assertEquals(expected, pkce.codeChallenge)
    }

    @Test
    fun `challenge method is S256`() {
        val pkce = PKCEGenerator()
        assertEquals("S256", pkce.codeChallengeMethod)
    }

    @Test
    fun `different instances produce different verifiers`() {
        val pkce1 = PKCEGenerator()
        val pkce2 = PKCEGenerator()
        assertNotEquals(pkce1.codeVerifier, pkce2.codeVerifier)
    }

    @Test
    fun `RFC 7636 Appendix B test vector`() {
        val verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
        val expectedChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"
        val pkce = PKCEGenerator(verifier)
        assertEquals(expectedChallenge, pkce.codeChallenge)
    }
}
