package com.idme.auth.mocks

import com.idme.auth.errors.IDmeAuthError
import com.idme.auth.networking.HTTPClient
import com.idme.auth.networking.HTTPResponse

class MockHTTPClient : HTTPClient {
    private val responses = mutableListOf<HTTPResponse>()
    val capturedRequests = mutableListOf<CapturedRequest>()
    private var callIndex = 0

    data class CapturedRequest(
        val url: String,
        val method: String,
        val headers: Map<String, String>,
        val body: Map<String, String>? = null
    )

    fun enqueue(body: String, statusCode: Int) {
        responses.add(HTTPResponse(statusCode, body))
    }

    override suspend fun get(url: String, headers: Map<String, String>): HTTPResponse {
        capturedRequests.add(CapturedRequest(url, "GET", headers))
        if (callIndex >= responses.size) {
            throw IDmeAuthError.NetworkError("No mock response available")
        }
        return responses[callIndex++]
    }

    override suspend fun postForm(url: String, body: Map<String, String>): HTTPResponse {
        capturedRequests.add(CapturedRequest(url, "POST", emptyMap(), body))
        if (callIndex >= responses.size) {
            throw IDmeAuthError.NetworkError("No mock response available")
        }
        return responses[callIndex++]
    }
}
