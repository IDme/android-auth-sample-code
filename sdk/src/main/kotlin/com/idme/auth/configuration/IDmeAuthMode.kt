package com.idme.auth.configuration

/** The authentication mode to use. */
enum class IDmeAuthMode(val value: String) {
    /** Standard OAuth 2.0 Authorization Code flow. Requires `clientSecret`. */
    OAUTH("oauth"),

    /** OAuth 2.0 with PKCE (recommended for mobile apps). No client secret needed. */
    OAUTH_PKCE("oauthPKCE"),

    /** OpenID Connect flow. Adds ID token validation with nonce. */
    OIDC("oidc")
}
