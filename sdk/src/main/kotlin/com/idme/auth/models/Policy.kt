package com.idme.auth.models

import kotlinx.serialization.Serializable

/**
 * A verification policy available for the organization.
 *
 * Returned by the `/api/public/v3/policies` endpoint.
 * The policy `handle` is used as the OAuth `scope` parameter.
 */
@Serializable
data class Policy(
    /** Human-readable name (e.g. "Military Verification"). */
    val name: String,

    /** Machine-readable handle used as the OAuth scope (e.g. "military"). */
    val handle: String,

    /** Whether the policy is currently active. */
    val active: Boolean,

    /** Groups contained within this policy. */
    val groups: List<Group>
) {
    @Serializable
    data class Group(
        /** Human-readable group name (e.g. "Military"). */
        val name: String,

        /** Machine-readable group handle. */
        val handle: String
    )
}
