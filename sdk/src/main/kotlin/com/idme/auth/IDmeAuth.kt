package com.idme.auth

import android.app.Activity
import android.content.Context
import android.net.Uri
import com.idme.auth.auth.AuthorizationRequest
import com.idme.auth.auth.GroupsRequest
import com.idme.auth.auth.IDmeAuthManager
import com.idme.auth.auth.PKCEGenerator
import com.idme.auth.auth.TokenExchangeRequest
import com.idme.auth.configuration.IDmeAuthMode
import com.idme.auth.configuration.IDmeConfiguration
import com.idme.auth.configuration.IDmeVerificationType
import com.idme.auth.errors.IDmeAuthError
import com.idme.auth.jwt.JWKSClient
import com.idme.auth.jwt.JWKSFetching
import com.idme.auth.jwt.JWTDecoder
import com.idme.auth.jwt.JWTValidator
import com.idme.auth.models.AttributeResponse
import com.idme.auth.models.Credentials
import com.idme.auth.models.Policy
import com.idme.auth.models.UserInfo
import com.idme.auth.networking.APIEndpoint
import com.idme.auth.networking.DefaultHTTPClient
import com.idme.auth.networking.HTTPClient
import com.idme.auth.storage.EncryptedCredentialStore
import com.idme.auth.token.TokenManager
import com.idme.auth.token.TokenRefresher
import com.idme.auth.utilities.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Main entry point for the IDmeAuthSDK.
 *
 * Provides login, logout, token management, and user info retrieval.
 *
 * ```kotlin
 * val idme = IDmeAuth(
 *     configuration = IDmeConfiguration(
 *         clientId = "YOUR_CLIENT_ID",
 *         redirectURI = "yourapp://idme/callback",
 *         scopes = listOf(IDmeScope.MILITARY),
 *         verificationType = IDmeVerificationType.SINGLE
 *     ),
 *     context = applicationContext
 * )
 *
 * val credentials = idme.login(activity)
 * ```
 */
class IDmeAuth(
    private val configuration: IDmeConfiguration,
    private val tokenManager: TokenManager,
    private val httpClient: HTTPClient,
    private val jwksFetcher: JWKSFetching
) {
    private var lastNonce: String? = null

    /** Creates a new IDmeAuth instance with the given configuration. Credentials are stored in EncryptedSharedPreferences. */
    constructor(configuration: IDmeConfiguration, context: Context) : this(
        configuration = configuration,
        tokenManager = TokenManager(
            credentialStore = EncryptedCredentialStore(context),
            refresher = TokenRefresher(configuration, DefaultHTTPClient())
        ),
        httpClient = DefaultHTTPClient(),
        jwksFetcher = JWKSClient(configuration.environment, DefaultHTTPClient())
    )

    private val json = Json { ignoreUnknownKeys = true }

    // MARK: - Login

    /**
     * Starts the authentication flow using a Chrome Custom Tab.
     *
     * @param activity The Activity to launch the auth session from.
     * @return The authenticated user's credentials.
     */
    suspend fun login(activity: Activity): Credentials {
        validateConfiguration()

        val authURL: String
        val state: String
        val nonce: String?
        val pkce: PKCEGenerator?

        when (configuration.verificationType) {
            IDmeVerificationType.SINGLE -> {
                val request = AuthorizationRequest(configuration)
                authURL = request.url
                state = request.state
                nonce = request.nonce
                pkce = request.pkce
            }
            IDmeVerificationType.GROUPS -> {
                val request = GroupsRequest(configuration)
                authURL = request.url
                state = request.state
                nonce = request.nonce
                pkce = request.pkce
            }
        }

        this.lastNonce = nonce

        Log.info("Starting auth session: ${configuration.verificationType.value} mode")

        val callbackURL = IDmeAuthManager.launchAuth(activity, authURL, state)

        val code = extractAuthorizationCode(callbackURL, state)

        val tokenExchange = TokenExchangeRequest(configuration, httpClient)
        val tokenResponse = tokenExchange.exchange(code, pkce?.codeVerifier)

        // Validate ID token for OIDC mode
        if (configuration.authMode == IDmeAuthMode.OIDC && tokenResponse.idToken != null) {
            val issuer = "${configuration.environment.apiBaseURL}oidc"
            val validator = JWTValidator(jwksFetcher, issuer, configuration.clientId)
            validator.validate(tokenResponse.idToken, nonce)
        }

        val credentials = tokenResponse.toCredentials()
        tokenManager.store(credentials)

        Log.info("Login successful")
        return credentials
    }

    // MARK: - Credentials

    /**
     * Returns valid credentials, automatically refreshing if they expire within [minTTL] seconds.
     *
     * @param minTTL Minimum time-to-live in seconds. Defaults to 60.
     * @return Valid credentials.
     */
    suspend fun credentials(minTTL: Long = 60): Credentials {
        return tokenManager.validCredentials(minTTL)
    }

    // MARK: - Policies

    /**
     * Fetches the available verification policies for the organization.
     *
     * Uses the client credentials (client_id and client_secret) to authenticate via HTTP Basic Auth.
     * The policy `handle` can be used as the OAuth `scope` parameter.
     *
     * @return A list of available policies.
     */
    suspend fun policies(): List<Policy> {
        val url = APIEndpoint.policies(configuration.environment)
        val credentials = "${configuration.clientId}:${configuration.clientSecret ?: ""}"
        val encoded = java.util.Base64.getEncoder()
            .encodeToString(credentials.toByteArray(Charsets.UTF_8))
        val headers = mapOf("Authorization" to "Basic $encoded")

        val response = try {
            httpClient.get(url, headers)
        } catch (e: IDmeAuthError) {
            throw e
        } catch (e: Exception) {
            throw IDmeAuthError.NetworkError(e.localizedMessage ?: "Unknown error")
        }

        if (response.statusCode !in 200..299) {
            throw IDmeAuthError.UnexpectedResponse(response.statusCode)
        }

        return try {
            json.decodeFromString<List<Policy>>(response.body)
        } catch (e: Exception) {
            throw IDmeAuthError.DecodingFailed(e.localizedMessage ?: "Unknown error")
        }
    }

    // MARK: - User Info

    /**
     * Fetches the authenticated user's profile information.
     *
     * @return The user's profile info.
     */
    suspend fun userInfo(): UserInfo {
        val creds = tokenManager.validCredentials(60)
        val url = APIEndpoint.userInfo(configuration.environment)

        val headers = mapOf("Authorization" to "Bearer ${creds.accessToken}")

        val response = try {
            httpClient.get(url, headers)
        } catch (e: IDmeAuthError) {
            throw e
        } catch (e: Exception) {
            throw IDmeAuthError.NetworkError(e.localizedMessage ?: "Unknown error")
        }

        if (response.statusCode !in 200..299) {
            throw IDmeAuthError.UnexpectedResponse(response.statusCode)
        }

        val jsonBody = extractJSON(response.body)

        return try {
            json.decodeFromString<UserInfo>(jsonBody)
        } catch (e: Exception) {
            throw IDmeAuthError.DecodingFailed(e.localizedMessage ?: "Unknown error")
        }
    }

    // MARK: - Raw Payload

    /**
     * Fetches the raw payload from the userinfo endpoint as key-value pairs.
     *
     * The endpoint returns a JWT; this method decodes it and returns all claims
     * as string key-value pairs, preserving the full payload.
     *
     * @return A list of (key, value) pairs from the JWT payload.
     */
    suspend fun rawPayload(): List<Pair<String, String>> {
        val creds = tokenManager.validCredentials(60)
        val url = APIEndpoint.userInfo(configuration.environment)

        val headers = mapOf("Authorization" to "Bearer ${creds.accessToken}")

        val response = try {
            httpClient.get(url, headers)
        } catch (e: IDmeAuthError) {
            throw e
        } catch (e: Exception) {
            throw IDmeAuthError.NetworkError(e.localizedMessage ?: "Unknown error")
        }

        if (response.statusCode !in 200..299) {
            throw IDmeAuthError.UnexpectedResponse(response.statusCode)
        }

        val jsonBody = extractJSON(response.body)

        val jsonObject = try {
            Json.parseToJsonElement(jsonBody).jsonObject
        } catch (e: Exception) {
            throw IDmeAuthError.DecodingFailed("Payload is not a JSON object")
        }

        return jsonObject.entries
            .sortedBy { it.key }
            .map { (key, value) -> key to stringValue(value) }
    }

    // MARK: - Attributes (OAuth)

    /**
     * Fetches the authenticated user's attributes (OAuth mode).
     *
     * Returns the ID.me attributes/status format used by OAuth and PKCE flows.
     * For OIDC flows, use [userInfo] instead.
     *
     * @return The user's attributes and verification statuses.
     */
    suspend fun attributes(): AttributeResponse {
        val creds = tokenManager.validCredentials(60)
        val url = APIEndpoint.userInfo(configuration.environment)

        val headers = mapOf("Authorization" to "Bearer ${creds.accessToken}")

        val response = try {
            httpClient.get(url, headers)
        } catch (e: IDmeAuthError) {
            throw e
        } catch (e: Exception) {
            throw IDmeAuthError.NetworkError(e.localizedMessage ?: "Unknown error")
        }

        if (response.statusCode !in 200..299) {
            throw IDmeAuthError.UnexpectedResponse(response.statusCode)
        }

        val jsonBody = extractJSON(response.body)

        return try {
            json.decodeFromString<AttributeResponse>(jsonBody)
        } catch (e: Exception) {
            throw IDmeAuthError.DecodingFailed(e.localizedMessage ?: "Unknown error")
        }
    }

    // MARK: - Logout

    /** Clears all stored credentials and tokens. */
    fun logout() {
        try {
            tokenManager.clearSync()
        } catch (_: Exception) { }
        Log.info("User logged out")
    }

    // MARK: - Private

    /**
     * Extracts JSON data from a response that may be plain JSON or a JWT.
     * If the body is a JWT, the signature is verified before claims are extracted.
     */
    private suspend fun extractJSON(body: String): String {
        val trimmed = body.trim().trim('"')
        if (trimmed.startsWith("eyJ")) {
            val issuer = "${configuration.environment.apiBaseURL}oidc"
            val validator = JWTValidator(jwksFetcher, issuer, configuration.clientId)
            validator.validate(trimmed, null)
            val decoded = JWTDecoder.decode(trimmed)
            return Json.encodeToString(JsonObject.serializer(), JsonObject(decoded.payload.mapValues { (_, v) ->
                JsonPrimitive(v.toString())
            }))
        }
        return body
    }

    private fun stringValue(value: kotlinx.serialization.json.JsonElement): String {
        return when {
            value is JsonPrimitive -> value.content
            value is kotlinx.serialization.json.JsonArray -> value.jsonArray.joinToString(", ") {
                stringValue(it)
            }
            value is JsonObject -> value.toString()
            else -> value.toString()
        }
    }

    private fun validateConfiguration() {
        if (configuration.authMode == IDmeAuthMode.OAUTH && configuration.clientSecret == null) {
            throw IDmeAuthError.MissingClientSecret
        }

        if (configuration.verificationType == IDmeVerificationType.GROUPS &&
            configuration.environment == com.idme.auth.configuration.IDmeEnvironment.SANDBOX
        ) {
            throw IDmeAuthError.GroupsNotAvailableInSandbox
        }

        val scheme = Uri.parse(configuration.redirectURI).scheme
        if (scheme == null || scheme.lowercase() in DISALLOWED_REDIRECT_SCHEMES) {
            throw IDmeAuthError.InvalidRedirectURI
        }
    }

    private fun extractAuthorizationCode(url: String, expectedState: String): String {
        val uri = Uri.parse(url) ?: throw IDmeAuthError.InvalidCallbackURL
        // Check for error response
        val error = uri.getQueryParameter("error")
        if (error != null) {
            if (error == "access_denied") {
                throw IDmeAuthError.UserCancelled
            }
            val description = uri.getQueryParameter("error_description") ?: error
            throw IDmeAuthError.TokenExchangeFailed(0, description)
        }

        // Validate state — mandatory; a missing state is treated as a CSRF/injection attempt
        val returnedState = uri.getQueryParameter("state")
            ?: throw IDmeAuthError.StateMismatch
        if (returnedState != expectedState) {
            throw IDmeAuthError.StateMismatch
        }

        // Extract code
        return uri.getQueryParameter("code")
            ?: throw IDmeAuthError.MissingAuthorizationCode
    }

    companion object {
        private val DISALLOWED_REDIRECT_SCHEMES = setOf("http", "https", "javascript", "file", "data")
    }
}
