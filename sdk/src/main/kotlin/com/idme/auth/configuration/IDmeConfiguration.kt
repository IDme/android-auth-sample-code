package com.idme.auth.configuration

import android.net.Uri

/** Configuration for the IDmeAuth client. */
data class IDmeConfiguration(
    /** The OAuth client ID issued by ID.me. */
    val clientId: String,

    /** The redirect URI registered with ID.me (e.g., "yourapp://idme/callback"). */
    val redirectURI: String,

    /** The OAuth scopes to request. */
    val scopes: List<IDmeScope>,

    /** The environment to use (sandbox or production). */
    val environment: IDmeEnvironment = IDmeEnvironment.PRODUCTION,

    /** The authentication mode (OAuth, OAuth+PKCE, or OIDC). */
    val authMode: IDmeAuthMode = IDmeAuthMode.OAUTH_PKCE,

    /** The verification type (single scope or groups/multi-scope). */
    val verificationType: IDmeVerificationType = IDmeVerificationType.SINGLE,

    /** The client secret. Required for `.oauth` mode; unused for `.oauthPKCE`. */
    val clientSecret: String? = null
) {
    /** The redirect URI scheme extracted from the URI string. */
    val redirectScheme: String?
        get() = Uri.parse(redirectURI).scheme
}
