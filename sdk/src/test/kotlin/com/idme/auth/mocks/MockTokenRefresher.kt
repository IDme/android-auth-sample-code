package com.idme.auth.mocks

import com.idme.auth.models.TokenResponse
import com.idme.auth.token.TokenRefreshing

class MockTokenRefresher : TokenRefreshing {
    var result: TokenResponse? = null
    var error: Exception? = null
    var refreshCallCount = 0

    override suspend fun refresh(refreshToken: String): TokenResponse {
        refreshCallCount++
        error?.let { throw it }
        return result!!
    }
}
