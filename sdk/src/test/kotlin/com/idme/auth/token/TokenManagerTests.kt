package com.idme.auth.token

import com.idme.auth.errors.IDmeAuthError
import com.idme.auth.mocks.MockCredentialStore
import com.idme.auth.mocks.MockTokenRefresher
import com.idme.auth.mocks.TestFixtures
import com.idme.auth.models.TokenResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TokenManagerTests {

    @Test
    fun `store and retrieve credentials`() = runTest {
        val store = MockCredentialStore()
        val refresher = MockTokenRefresher()
        val manager = TokenManager(store, refresher)

        val creds = TestFixtures.makeCredentials()
        manager.store(creds)

        val retrieved = manager.currentCredentials()
        assertEquals(creds.accessToken, retrieved?.accessToken)
    }

    @Test
    fun `returns non-expired token without refreshing`() = runTest {
        val store = MockCredentialStore()
        val refresher = MockTokenRefresher()
        val manager = TokenManager(store, refresher)

        val creds = TestFixtures.makeCredentials(expiresInMs = 3600_000)
        manager.store(creds)

        val valid = manager.validCredentials(60)
        assertEquals(creds.accessToken, valid.accessToken)
        assertEquals(0, refresher.refreshCallCount)
    }

    @Test
    fun `refreshes expiring token`() = runTest {
        val store = MockCredentialStore()
        val refresher = MockTokenRefresher()
        refresher.result = TokenResponse(
            accessToken = "refreshed-token",
            tokenType = "Bearer",
            expiresIn = 3600,
            refreshToken = "new-refresh",
            idToken = null,
            scope = null
        )
        val manager = TokenManager(store, refresher)

        val creds = TestFixtures.makeCredentials(expiresInMs = 30_000)
        manager.store(creds)

        val valid = manager.validCredentials(60)
        assertEquals("refreshed-token", valid.accessToken)
        assertEquals(1, refresher.refreshCallCount)
    }

    @Test(expected = IDmeAuthError.NotAuthenticated::class)
    fun `throws notAuthenticated when no credentials`() = runTest {
        val store = MockCredentialStore()
        val refresher = MockTokenRefresher()
        val manager = TokenManager(store, refresher)

        manager.validCredentials()
    }

    @Test(expected = IDmeAuthError.RefreshTokenExpired::class)
    fun `throws when no refresh token available`() = runTest {
        val store = MockCredentialStore()
        val refresher = MockTokenRefresher()
        val manager = TokenManager(store, refresher)

        val creds = TestFixtures.makeCredentials(refreshToken = null, expiresInMs = 30_000)
        manager.store(creds)

        manager.validCredentials(60)
    }

    @Test
    fun `clear removes credentials`() = runTest {
        val store = MockCredentialStore()
        val refresher = MockTokenRefresher()
        val manager = TokenManager(store, refresher)

        val creds = TestFixtures.makeCredentials()
        manager.store(creds)
        manager.clear()

        val retrieved = manager.currentCredentials()
        assertNull(retrieved)
        assertEquals(1, store.deleteCallCount)
    }
}
