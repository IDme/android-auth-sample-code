package com.idme.auth.configuration

/** The ID.me environment to connect to. */
enum class IDmeEnvironment(val value: String) {
    SANDBOX("sandbox"),
    PRODUCTION("production");

    /** Base URL for the API (authorize, token, userinfo). */
    val apiBaseURL: String
        get() = when (this) {
            SANDBOX -> "https://api.idmelabs.com/"
            PRODUCTION -> "https://api.id.me/"
        }

    /** Base URL for the groups endpoint (production only). */
    val groupsBaseURL: String
        get() = "https://groups.id.me"

    /** OIDC discovery URL. */
    val discoveryURL: String
        get() = "${apiBaseURL}.well-known/openid-configuration"

    /** JWKS URL. */
    val jwksURL: String
        get() = "${apiBaseURL}oidc/.well-known/jwks"
}
