package com.idme.auth.auth

import com.idme.auth.errors.IDmeAuthError
import com.idme.auth.mocks.MockHTTPClient
import com.idme.auth.mocks.TestFixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenExchangeRequestTests {

    @Test
    fun `successful token exchange`() = runTest {
        val mockHTTP = MockHTTPClient()
        mockHTTP.enqueue(TestFixtures.TOKEN_RESPONSE_JSON, 200)

        val exchange = TokenExchangeRequest(TestFixtures.singleConfig, mockHTTP)
        val response = exchange.exchange("test-code", "test-verifier")

        assertEquals("new-access-token", response.accessToken)
        assertEquals("new-refresh-token", response.refreshToken)
        assertEquals("Bearer", response.tokenType)
        assertEquals(3600, response.expiresIn)
    }

    @Test
    fun `sends correct body parameters`() = runTest {
        val mockHTTP = MockHTTPClient()
        mockHTTP.enqueue(TestFixtures.TOKEN_RESPONSE_JSON, 200)

        val exchange = TokenExchangeRequest(TestFixtures.singleConfig, mockHTTP)
        exchange.exchange("auth-code", "verifier-123")

        val request = mockHTTP.capturedRequests.first()
        val body = request.body!!
        assertEquals("authorization_code", body["grant_type"])
        assertEquals("auth-code", body["code"])
        assertEquals("verifier-123", body["code_verifier"])
        assertEquals("test-client-id", body["client_id"])
    }

    @Test
    fun `includes client secret when configured`() = runTest {
        val mockHTTP = MockHTTPClient()
        mockHTTP.enqueue(TestFixtures.TOKEN_RESPONSE_JSON, 200)

        val exchange = TokenExchangeRequest(TestFixtures.oauthConfig, mockHTTP)
        exchange.exchange("auth-code", null)

        val request = mockHTTP.capturedRequests.first()
        assertEquals("test-client-secret", request.body!!["client_secret"])
    }

    @Test(expected = IDmeAuthError.TokenExchangeFailed::class)
    fun `throws on HTTP error`() = runTest {
        val mockHTTP = MockHTTPClient()
        mockHTTP.enqueue("Bad Request", 400)

        val exchange = TokenExchangeRequest(TestFixtures.singleConfig, mockHTTP)
        exchange.exchange("bad-code", null)
    }

    @Test
    fun `uses correct sandbox endpoint`() = runTest {
        val mockHTTP = MockHTTPClient()
        mockHTTP.enqueue(TestFixtures.TOKEN_RESPONSE_JSON, 200)

        val exchange = TokenExchangeRequest(TestFixtures.sandboxConfig, mockHTTP)
        exchange.exchange("code", null)

        val request = mockHTTP.capturedRequests.first()
        assertTrue(request.url.contains("api.idmelabs.com/oauth/token"))
    }
}
