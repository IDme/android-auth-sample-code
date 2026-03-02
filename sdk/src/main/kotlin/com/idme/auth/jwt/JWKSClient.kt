package com.idme.auth.jwt

import com.idme.auth.configuration.IDmeEnvironment
import com.idme.auth.errors.IDmeAuthError
import com.idme.auth.models.JWKS
import com.idme.auth.networking.APIEndpoint
import com.idme.auth.networking.DefaultHTTPClient
import com.idme.auth.networking.HTTPClient
import kotlinx.serialization.json.Json

/** Interface for fetching JWKS, enabling mock injection. */
interface JWKSFetching {
    suspend fun fetchJWKS(): JWKS
}

/** Fetches and caches the JSON Web Key Set from ID.me. */
class JWKSClient(
    private val environment: IDmeEnvironment,
    private val httpClient: HTTPClient = DefaultHTTPClient(),
    private val cacheTTL: Long = 3600_000L // 1 hour in milliseconds
) : JWKSFetching {

    private var cached: JWKS? = null
    private var cacheTime: Long = 0

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetchJWKS(): JWKS {
        val now = System.currentTimeMillis()
        val cachedValue = cached
        if (cachedValue != null && (now - cacheTime) < cacheTTL) {
            return cachedValue
        }

        val url = APIEndpoint.jwks(environment)

        val response = try {
            httpClient.get(url, emptyMap())
        } catch (e: IDmeAuthError) {
            throw e
        } catch (e: Exception) {
            throw IDmeAuthError.NetworkError(e.localizedMessage ?: "Unknown error")
        }

        if (response.statusCode !in 200..299) {
            throw IDmeAuthError.UnexpectedResponse(response.statusCode)
        }

        val jwks = try {
            json.decodeFromString<JWKS>(response.body)
        } catch (e: Exception) {
            throw IDmeAuthError.DecodingFailed(e.localizedMessage ?: "Unknown error")
        }

        cached = jwks
        cacheTime = now

        return jwks
    }
}
