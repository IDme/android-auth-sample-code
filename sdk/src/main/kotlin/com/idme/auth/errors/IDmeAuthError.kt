package com.idme.auth.errors

/** All errors thrown by the IDmeAuthSDK. */
sealed class IDmeAuthError(override val message: String) : Exception(message) {

    // MARK: - Configuration

    /** The client secret is required for `OAUTH` mode but was not provided. */
    data object MissingClientSecret : IDmeAuthError(
        "Client secret is required for OAuth (non-PKCE) mode."
    )

    /** The groups verification type is only available in production. */
    data object GroupsNotAvailableInSandbox : IDmeAuthError(
        "The groups verification type is only available in production."
    )

    /** The redirect URI is invalid. */
    data object InvalidRedirectURI : IDmeAuthError(
        "The redirect URI is invalid."
    )

    // MARK: - Auth Flow

    /** The user cancelled the authentication session. */
    data object UserCancelled : IDmeAuthError(
        "The user cancelled the authentication session."
    )

    /** The state parameter in the callback did not match the original request. */
    data object StateMismatch : IDmeAuthError(
        "The state parameter did not match. Possible CSRF attack."
    )

    /** No authorization code was found in the callback URL. */
    data object MissingAuthorizationCode : IDmeAuthError(
        "No authorization code was found in the callback."
    )

    /** The callback URL could not be parsed. */
    data object InvalidCallbackURL : IDmeAuthError(
        "The callback URL could not be parsed."
    )

    // MARK: - Token

    /** The token exchange request failed. */
    data class TokenExchangeFailed(val statusCode: Int, val errorMessage: String) : IDmeAuthError(
        "Token exchange failed ($statusCode): $errorMessage"
    )

    /** No credentials are available (user not logged in). */
    data object NotAuthenticated : IDmeAuthError(
        "No credentials available. Please log in first."
    )

    /** The refresh token is missing or expired. */
    data object RefreshTokenExpired : IDmeAuthError(
        "The refresh token is missing or expired."
    )

    /** Token refresh failed. */
    data class TokenRefreshFailed(val statusCode: Int, val errorMessage: String) : IDmeAuthError(
        "Token refresh failed ($statusCode): $errorMessage"
    )

    // MARK: - JWT

    /** The JWT string is malformed. */
    data class InvalidJWT(val reason: String) : IDmeAuthError(
        "Invalid JWT: $reason"
    )

    /** The JWT signature verification failed. */
    data object JWTSignatureInvalid : IDmeAuthError(
        "JWT signature verification failed."
    )

    /** A JWT claim failed validation (e.g., expired, wrong audience). */
    data class JWTClaimInvalid(val claim: String, val reason: String) : IDmeAuthError(
        "JWT claim '$claim' is invalid: $reason"
    )

    /** No matching key was found in the JWKS for the JWT's `kid`. */
    data class JWKSKeyNotFound(val kid: String) : IDmeAuthError(
        "No matching key found in JWKS for kid '$kid'."
    )

    // MARK: - Network

    /** A network request failed. */
    data class NetworkError(val underlying: String) : IDmeAuthError(
        "Network error: $underlying"
    )

    /** The server returned an unexpected response. */
    data class UnexpectedResponse(val statusCode: Int) : IDmeAuthError(
        "Unexpected server response ($statusCode)."
    )

    /** The response body could not be decoded. */
    data class DecodingFailed(val underlying: String) : IDmeAuthError(
        "Decoding failed: $underlying"
    )

    // MARK: - Storage

    /** A storage operation failed. */
    data class StorageError(val underlying: String) : IDmeAuthError(
        "Storage error: $underlying"
    )
}
