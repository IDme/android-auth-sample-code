package com.idme.auth.configuration

/** Type-safe OAuth scopes supported by ID.me. */
enum class IDmeScope(val value: String) {
    // OIDC standard scopes
    OPENID("openid"),
    PROFILE("profile"),
    EMAIL("email"),

    // ID.me verification scopes
    MILITARY("military"),
    FIRST_RESPONDER("first_responder"),
    NURSE("nurse"),
    TEACHER("teacher"),
    STUDENT("student"),
    GOVERNMENT_EMPLOYEE("government"),
    LOW_INCOME("low_income");

    companion object {
        /** Space-separated scope string for the authorize endpoint. */
        fun authorizeString(scopes: List<IDmeScope>): String =
            scopes.joinToString(" ") { it.value }

        /** Comma-separated scope string for the groups endpoint. */
        fun groupsString(scopes: List<IDmeScope>): String =
            scopes.joinToString(",") { it.value }

        /** Finds a scope by its raw value string, or null if not found. */
        fun fromValue(value: String): IDmeScope? =
            entries.firstOrNull { it.value == value }
    }
}
