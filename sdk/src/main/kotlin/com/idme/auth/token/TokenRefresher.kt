package com.idme.auth.token

import com.idme.auth.configuration.IDmeConfiguration
import com.idme.auth.errors.IDmeAuthError
import com.idme.auth.models.TokenResponse
import com.idme.auth.networking.APIEndpoint
import com.idme.auth.networking.DefaultHTTPClient
import com.idme.auth.networking.HTTPClient
import kotlinx.serialization.json.Json

/** Interface for token refresh, enabling mock injection in tests. */
interface TokenRefreshing {
    suspend fun refresh(refreshToken: String): TokenResponse
}

/** Handles the refresh_token grant type. */
class TokenRefresher(
    private val configuration: IDmeConfiguration,
    private val httpClient: HTTPClient = DefaultHTTPClient()
) : TokenRefreshing {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun refresh(refreshToken: String): TokenResponse {
        val tokenURL = APIEndpoint.token(configuration.environment)

        val body = mutableMapOf(
            "grant_type" to "refresh_token",
            "refresh_token" to refreshToken,
            "client_id" to configuration.clientId
        )

        if (configuration.clientSecret != null) {
            body["client_secret"] = configuration.clientSecret
        }

        val response = try {
            httpClient.postForm(tokenURL, body)
        } catch (e: IDmeAuthError) {
            throw e
        } catch (e: Exception) {
            throw IDmeAuthError.NetworkError(e.localizedMessage ?: "Unknown error")
        }

        if (response.statusCode !in 200..299) {
            throw IDmeAuthError.TokenRefreshFailed(response.statusCode, response.body)
        }

        return try {
            json.decodeFromString<TokenResponse>(response.body)
        } catch (e: Exception) {
            throw IDmeAuthError.DecodingFailed(e.localizedMessage ?: "Unknown error")
        }
    }
}
