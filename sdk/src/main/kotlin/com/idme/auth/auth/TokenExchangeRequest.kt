package com.idme.auth.auth

import com.idme.auth.configuration.IDmeConfiguration
import com.idme.auth.configuration.IDmeScope
import com.idme.auth.errors.IDmeAuthError
import com.idme.auth.models.TokenResponse
import com.idme.auth.networking.APIEndpoint
import com.idme.auth.networking.DefaultHTTPClient
import com.idme.auth.networking.HTTPClient
import com.idme.auth.utilities.Log
import kotlinx.serialization.json.Json

/** Handles the OAuth token exchange (authorization code -> tokens). */
class TokenExchangeRequest(
    private val configuration: IDmeConfiguration,
    private val httpClient: HTTPClient = DefaultHTTPClient()
) {
    private val json = Json { ignoreUnknownKeys = true }

    /** Exchanges an authorization code for tokens. */
    suspend fun exchange(code: String, codeVerifier: String?): TokenResponse {
        val tokenURL = APIEndpoint.token(configuration.environment)

        val body = mutableMapOf(
            "grant_type" to "authorization_code",
            "code" to code,
            "redirect_uri" to configuration.redirectURI,
            "client_id" to configuration.clientId,
            "scope" to IDmeScope.authorizeString(configuration.scopes)
        )

        if (codeVerifier != null) {
            body["code_verifier"] = codeVerifier
        }

        if (configuration.clientSecret != null) {
            body["client_secret"] = configuration.clientSecret
        }

        Log.debug("Token exchange URL: $tokenURL")
        Log.debug("Token exchange body: $body")

        val response = try {
            httpClient.postForm(tokenURL, body)
        } catch (e: IDmeAuthError) {
            throw e
        } catch (e: Exception) {
            throw IDmeAuthError.NetworkError(e.localizedMessage ?: "Unknown error")
        }

        if (response.statusCode !in 200..299) {
            throw IDmeAuthError.TokenExchangeFailed(response.statusCode, response.body)
        }

        return try {
            json.decodeFromString<TokenResponse>(response.body)
        } catch (e: Exception) {
            throw IDmeAuthError.DecodingFailed(e.localizedMessage ?: "Unknown error")
        }
    }
}
