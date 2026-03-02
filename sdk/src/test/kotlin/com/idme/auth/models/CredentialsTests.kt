package com.idme.auth.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CredentialsTests {

    @Test
    fun `isExpired is true for past dates`() {
        val expired = Credentials(
            accessToken = "token",
            tokenType = "Bearer",
            expiresAt = System.currentTimeMillis() - 60_000
        )
        assertTrue(expired.isExpired)

        val valid = Credentials(
            accessToken = "token",
            tokenType = "Bearer",
            expiresAt = System.currentTimeMillis() + 3600_000
        )
        assertFalse(valid.isExpired)
    }

    @Test
    fun `expiresWithin checks TTL threshold`() {
        val credentials = Credentials(
            accessToken = "token",
            tokenType = "Bearer",
            expiresAt = System.currentTimeMillis() + 30_000
        )

        assertTrue(credentials.expiresWithin(60))
        assertFalse(credentials.expiresWithin(10))
    }

    @Test
    fun `equals works correctly`() {
        val ts = System.currentTimeMillis() + 3600_000
        val a = Credentials(
            accessToken = "token",
            refreshToken = "refresh",
            tokenType = "Bearer",
            expiresAt = ts
        )
        val b = Credentials(
            accessToken = "token",
            refreshToken = "refresh",
            tokenType = "Bearer",
            expiresAt = ts
        )
        assertEquals(a, b)
    }
}
