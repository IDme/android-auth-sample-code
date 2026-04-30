package com.idme.auth

import com.idme.auth.mocks.MockCredentialStore
import com.idme.auth.mocks.MockHTTPClient
import com.idme.auth.mocks.MockJWKSFetcher
import com.idme.auth.mocks.MockTokenRefresher
import com.idme.auth.mocks.TestFixtures
import com.idme.auth.token.TokenManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IDmeAuthAttributesTest {

    private val attributesJson = """
        {
            "attributes": [
                {"handle": "email", "name": "Email", "value": "test@example.com"},
                {"handle": "fname", "name": "First Name", "value": "John"}
            ],
            "status": [
                {"group": "military", "subgroups": ["Veteran"], "verified": true}
            ]
        }
    """.trimIndent()

    private fun buildAuth(httpClient: MockHTTPClient): IDmeAuth {
        val store = MockCredentialStore()
        store.save(TestFixtures.makeCredentials(expiresInMs = 3_600_000))
        return IDmeAuth(
            configuration = TestFixtures.singleConfig,
            tokenManager = TokenManager(store, MockTokenRefresher()),
            httpClient = httpClient,
            jwksFetcher = MockJWKSFetcher()
        )
    }

    @Test
    fun `attributes calls attributes endpoint not userinfo`() = runTest {
        val httpClient = MockHTTPClient()
        httpClient.enqueue(body = attributesJson, statusCode = 200)

        buildAuth(httpClient).attributes()

        val calledURL = httpClient.capturedRequests.single().url
        assertTrue(
            "Expected /api/public/v3/attributes.json but got: $calledURL",
            calledURL.endsWith("api/public/v3/attributes.json")
        )
    }

    @Test
    fun `attributes returns populated status block`() = runTest {
        val httpClient = MockHTTPClient()
        httpClient.enqueue(body = attributesJson, statusCode = 200)

        val result = buildAuth(httpClient).attributes()

        assertEquals(1, result.status.size)
        val status = result.status[0]
        assertEquals("military", status.group)
        assertTrue(status.verified)
        assertEquals(listOf("Veteran"), status.subgroups)
    }

    @Test
    fun `attributes returns populated attributes list`() = runTest {
        val httpClient = MockHTTPClient()
        httpClient.enqueue(body = attributesJson, statusCode = 200)

        val result = buildAuth(httpClient).attributes()

        assertEquals(2, result.attributes.size)
        val email = result.attributes.first { it.handle == "email" }
        assertEquals("test@example.com", email.value)
    }
}
