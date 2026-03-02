package com.idme.auth.mocks

import com.idme.auth.configuration.IDmeAuthMode
import com.idme.auth.configuration.IDmeConfiguration
import com.idme.auth.configuration.IDmeEnvironment
import com.idme.auth.configuration.IDmeScope
import com.idme.auth.configuration.IDmeVerificationType
import com.idme.auth.models.Credentials

object TestFixtures {
    const val CLIENT_ID = "test-client-id"
    const val REDIRECT_URI = "testapp://idme/callback"
    const val CLIENT_SECRET = "test-client-secret"

    val singleConfig = IDmeConfiguration(
        clientId = CLIENT_ID,
        redirectURI = REDIRECT_URI,
        scopes = listOf(IDmeScope.MILITARY),
        environment = IDmeEnvironment.PRODUCTION,
        authMode = IDmeAuthMode.OAUTH_PKCE,
        verificationType = IDmeVerificationType.SINGLE
    )

    val groupsConfig = IDmeConfiguration(
        clientId = CLIENT_ID,
        redirectURI = REDIRECT_URI,
        scopes = listOf(IDmeScope.MILITARY, IDmeScope.FIRST_RESPONDER),
        environment = IDmeEnvironment.PRODUCTION,
        authMode = IDmeAuthMode.OAUTH_PKCE,
        verificationType = IDmeVerificationType.GROUPS
    )

    val oauthConfig = IDmeConfiguration(
        clientId = CLIENT_ID,
        redirectURI = REDIRECT_URI,
        scopes = listOf(IDmeScope.MILITARY),
        environment = IDmeEnvironment.PRODUCTION,
        authMode = IDmeAuthMode.OAUTH,
        verificationType = IDmeVerificationType.SINGLE,
        clientSecret = CLIENT_SECRET
    )

    val oidcConfig = IDmeConfiguration(
        clientId = CLIENT_ID,
        redirectURI = REDIRECT_URI,
        scopes = listOf(IDmeScope.OPENID, IDmeScope.PROFILE, IDmeScope.EMAIL),
        environment = IDmeEnvironment.PRODUCTION,
        authMode = IDmeAuthMode.OIDC,
        verificationType = IDmeVerificationType.SINGLE
    )

    val sandboxConfig = IDmeConfiguration(
        clientId = CLIENT_ID,
        redirectURI = REDIRECT_URI,
        scopes = listOf(IDmeScope.MILITARY),
        environment = IDmeEnvironment.SANDBOX,
        authMode = IDmeAuthMode.OAUTH_PKCE,
        verificationType = IDmeVerificationType.SINGLE
    )

    fun makeCredentials(
        accessToken: String = "test-access-token",
        refreshToken: String? = "test-refresh-token",
        idToken: String? = null,
        expiresInMs: Long = 3600_000
    ): Credentials = Credentials(
        accessToken = accessToken,
        refreshToken = refreshToken,
        idToken = idToken,
        tokenType = "Bearer",
        expiresAt = System.currentTimeMillis() + expiresInMs
    )

    const val TOKEN_RESPONSE_JSON = """
        {
            "access_token": "new-access-token",
            "token_type": "Bearer",
            "expires_in": 3600,
            "refresh_token": "new-refresh-token",
            "scope": "military"
        }
    """

    const val USER_INFO_JSON = """
        {
            "sub": "user-123",
            "email": "test@example.com",
            "email_verified": true,
            "given_name": "John",
            "family_name": "Doe",
            "name": "John Doe"
        }
    """
}
