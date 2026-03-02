package com.idme.auth.token

import com.idme.auth.errors.IDmeAuthError
import com.idme.auth.mocks.MockHTTPClient
import com.idme.auth.mocks.TestFixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenRefresherTests {

    @Test
    fun `successful refresh`() = runTest {
        val mockHTTP = MockHTTPClient()
        mockHTTP.enqueue(TestFixtures.TOKEN_RESPONSE_JSON, 200)

        val refresher = TokenRefresher(TestFixtures.singleConfig, mockHTTP)
        val response = refresher.refresh("old-refresh-token")

        assertEquals("new-access-token", response.accessToken)
        assertEquals("new-refresh-token", response.refreshToken)
    }

    @Test
    fun `sends correct body parameters`() = runTest {
        val mockHTTP = MockHTTPClient()
        mockHTTP.enqueue(TestFixtures.TOKEN_RESPONSE_JSON, 200)

        val refresher = TokenRefresher(TestFixtures.singleConfig, mockHTTP)
        refresher.refresh("refresh-123")

        val request = mockHTTP.capturedRequests.first()
        val body = request.body!!
        assertEquals("refresh_token", body["grant_type"])
        assertEquals("refresh-123", body["refresh_token"])
        assertEquals("test-client-id", body["client_id"])
    }

    @Test(expected = IDmeAuthError.TokenRefreshFailed::class)
    fun `throws on HTTP error`() = runTest {
        val mockHTTP = MockHTTPClient()
        mockHTTP.enqueue("Unauthorized", 401)

        val refresher = TokenRefresher(TestFixtures.singleConfig, mockHTTP)
        refresher.refresh("bad-token")
    }

    @Test
    fun `includes client secret when configured`() = runTest {
        val mockHTTP = MockHTTPClient()
        mockHTTP.enqueue(TestFixtures.TOKEN_RESPONSE_JSON, 200)

        val refresher = TokenRefresher(TestFixtures.oauthConfig, mockHTTP)
        refresher.refresh("refresh-token")

        val request = mockHTTP.capturedRequests.first()
        assertEquals("test-client-secret", request.body!!["client_secret"])
    }
}
