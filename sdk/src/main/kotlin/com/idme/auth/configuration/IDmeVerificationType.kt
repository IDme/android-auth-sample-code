package com.idme.auth.configuration

/** The integration pattern for verification. */
enum class IDmeVerificationType(val value: String) {
    /**
     * Single scope/policy -- routes to `/oauth/authorize`.
     * Use when the integration targets a single verification community.
     */
    SINGLE("single"),

    /**
     * Multiple scopes/policies -- routes to `groups.id.me`.
     * Presents a UI for the user to choose their verification community.
     * **Production only** -- SDK throws an error if combined with `SANDBOX`.
     */
    GROUPS("groups")
}
