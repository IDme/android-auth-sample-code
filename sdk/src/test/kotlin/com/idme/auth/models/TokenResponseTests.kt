package com.idme.auth.models

import com.idme.auth.mocks.TestFixtures
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class TokenResponseTests {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes standard token response`() {
        val response = json.decodeFromString<TokenResponse>(TestFixtures.TOKEN_RESPONSE_JSON)

        assertEquals("new-access-token", response.accessToken)
        assertEquals("Bearer", response.tokenType)
        assertEquals(3600, response.expiresIn)
        assertEquals("new-refresh-token", response.refreshToken)
        assertEquals("military", response.scope)
    }

    @Test
    fun `decodes response with ID token`() {
        val jsonStr = """
            {
                "access_token": "at",
                "token_type": "Bearer",
                "expires_in": 1800,
                "refresh_token": "rt",
                "id_token": "eyJhbGciOiJSUzI1NiJ9.payload.sig",
                "scope": "openid profile"
            }
        """
        val response = json.decodeFromString<TokenResponse>(jsonStr)
        assertEquals("eyJhbGciOiJSUzI1NiJ9.payload.sig", response.idToken)
    }

    @Test
    fun `converts to Credentials`() {
        val response = json.decodeFromString<TokenResponse>(TestFixtures.TOKEN_RESPONSE_JSON)
        val credentials = response.toCredentials()

        assertEquals("new-access-token", credentials.accessToken)
        assertEquals("new-refresh-token", credentials.refreshToken)
        assertEquals("Bearer", credentials.tokenType)
        assertFalse(credentials.isExpired)
    }

    @Test
    fun `decodes minimal response without optional fields`() {
        val jsonStr = """
            {
                "access_token": "at",
                "token_type": "Bearer",
                "expires_in": 3600
            }
        """
        val response = json.decodeFromString<TokenResponse>(jsonStr)
        assertEquals("at", response.accessToken)
        assertNull(response.refreshToken)
        assertNull(response.idToken)
        assertNull(response.scope)
    }
}
