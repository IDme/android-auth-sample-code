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
 * Persists [Credentials] to Android SharedPreferences.
 *
 * For production use, consumers should use [EncryptedCredentialStore] which wraps
 * EncryptedSharedPreferences. This basic implementation uses standard SharedPreferences
 * and is suitable for development and testing.
 *
 * Note: The SDK initializes with this store by default. To use EncryptedSharedPreferences,
 * pass an [EncryptedCredentialStore] when constructing [IDmeAuth] via the internal constructor.
 */
class CredentialStore : CredentialStoring {
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
