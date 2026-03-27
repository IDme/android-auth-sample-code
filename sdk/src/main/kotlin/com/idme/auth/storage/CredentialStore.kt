package com.idme.auth.storage

import com.idme.auth.models.Credentials
import com.idme.auth.utilities.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Interface for credential persistence, enabling mock injection in tests. */
interface CredentialStoring {
    fun save(credentials: Credentials)
    fun load(): Credentials?
    fun delete()
}

/**
 * In-memory credential store for testing purposes only.
 *
 * This store holds credentials in a plain String field with no encryption.
 * It is intentionally internal and must not be used in production.
 * The [IDmeAuth] public constructor uses [EncryptedCredentialStore] by default.
 */
internal class CredentialStore : CredentialStoring {
    private val json = Json { ignoreUnknownKeys = true }

    // In-memory storage as a fallback when no Android Context is available.
    // For production use, consumers should initialize with EncryptedCredentialStore.
    private var storedJson: String? = null

    override fun save(credentials: Credentials) {
        storedJson = json.encodeToString(credentials)
        Log.debug("Credentials saved")
    }

    override fun load(): Credentials? {
        val data = storedJson ?: return null
        return try {
            json.decodeFromString<Credentials>(data)
        } catch (_: Exception) {
            null
        }
    }

    override fun delete() {
        storedJson = null
        Log.debug("Credentials deleted")
    }
}
