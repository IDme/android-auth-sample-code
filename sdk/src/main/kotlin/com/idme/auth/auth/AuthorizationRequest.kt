package com.idme.auth.auth

import com.idme.auth.configuration.IDmeAuthMode
import com.idme.auth.configuration.IDmeConfiguration
import com.idme.auth.configuration.IDmeScope
import com.idme.auth.errors.IDmeAuthError
import com.idme.auth.networking.APIEndpoint

/**
 * Builds the authorization URL for single scope/policy flows.
 */
class AuthorizationRequest(configuration: IDmeConfiguration) {
    val url: String
    val state: String
    val nonce: String?
    val pkce: PKCEGenerator?

    init {
        if (android.net.Uri.parse(configuration.redirectURI).scheme == null) {
            throw IDmeAuthError.InvalidRedirectURI
        }

        state = StateGenerator.generate()

        val params = mutableListOf(
            "client_id" to configuration.clientId,
            "redirect_uri" to configuration.redirectURI,
            "response_type" to "code",
            "scope" to IDmeScope.authorizeString(configuration.scopes),
            "state" to state
        )

        // PKCE parameters
        pkce = if (configuration.authMode == IDmeAuthMode.OAUTH_PKCE ||
            configuration.authMode == IDmeAuthMode.OIDC
        ) {
            PKCEGenerator().also { gen ->
                params.add("code_challenge" to gen.codeChallenge)
                params.add("code_challenge_method" to gen.codeChallengeMethod)
            }
        } else {
            null
        }

        // OIDC nonce
        nonce = if (configuration.authMode == IDmeAuthMode.OIDC) {
            NonceGenerator.generate().also { n ->
                params.add("nonce" to n)
            }
        } else {
            null
        }

        val baseUrl = APIEndpoint.authorize(configuration.environment)
        val queryString = params.joinToString("&") { (key, value) ->
            "$key=${java.net.URLEncoder.encode(value, "UTF-8")}"
        }
        url = "$baseUrl?$queryString"
    }
}
