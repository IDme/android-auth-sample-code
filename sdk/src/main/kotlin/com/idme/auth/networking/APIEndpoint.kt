package com.idme.auth.networking

import com.idme.auth.configuration.IDmeEnvironment

/** API endpoint paths for ID.me services. */
object APIEndpoint {

    /** OAuth authorization endpoint (single scope/policy). */
    fun authorize(environment: IDmeEnvironment): String =
        "${environment.apiBaseURL}oauth/authorize"

    /** Groups endpoint (multiple scopes/policies, production only). */
    fun groups(environment: IDmeEnvironment): String =
        environment.groupsBaseURL

    /** OAuth token endpoint. */
    fun token(environment: IDmeEnvironment): String =
        "${environment.apiBaseURL}oauth/token"

    /** UserInfo endpoint. */
    fun userInfo(environment: IDmeEnvironment): String =
        "${environment.apiBaseURL}api/public/v3/userinfo"

    /** Policies endpoint. */
    fun policies(environment: IDmeEnvironment): String =
        "${environment.apiBaseURL}api/public/v3/policies"

    /** OIDC discovery endpoint. */
    fun discovery(environment: IDmeEnvironment): String =
        environment.discoveryURL

    /** JWKS endpoint. */
    fun jwks(environment: IDmeEnvironment): String =
        environment.jwksURL
}
