package com.idme.auth.storage

import com.idme.auth.mocks.MockCredentialStore
import com.idme.auth.mocks.TestFixtures
import com.idme.auth.models.Credentials
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CredentialStoreTests {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `credentials round-trip through JSON serialization`() {
        val credentials = TestFixtures.makeCredentials(
            accessToken = "access-123",
            refreshToken = "refresh-456",
            idToken = "id-789"
        )

        val data = json.encodeToString(credentials)
        val decoded = json.decodeFromString<Credentials>(data)

        assertEquals("access-123", decoded.accessToken)
        assertEquals("refresh-456", decoded.refreshToken)
        assertEquals("id-789", decoded.idToken)
        assertEquals("Bearer", decoded.tokenType)
    }

    @Test
    fun `handles nil optional fields`() {
        val credentials = TestFixtures.makeCredentials(
            refreshToken = null,
            idToken = null
        )

        val data = json.encodeToString(credentials)
        val decoded = json.decodeFromString<Credentials>(data)

        assertNull(decoded.refreshToken)
        assertNull(decoded.idToken)
    }

    @Test
    fun `mock store save and load`() {
        val store = MockCredentialStore()
        val credentials = TestFixtures.makeCredentials()

        store.save(credentials)
        val loaded = store.load()

        assertNotNull(loaded)
        assertEquals(credentials.accessToken, loaded!!.accessToken)
        assertEquals(1, store.saveCallCount)
    }

    @Test
    fun `mock store delete`() {
        val store = MockCredentialStore()
        store.stored = TestFixtures.makeCredentials()

        store.delete()

        assertNull(store.load())
        assertEquals(1, store.deleteCallCount)
    }
}
