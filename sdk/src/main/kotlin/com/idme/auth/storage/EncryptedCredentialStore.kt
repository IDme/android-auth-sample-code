package com.idme.auth.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.idme.auth.models.Credentials
import com.idme.auth.utilities.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Persists [Credentials] to Android EncryptedSharedPreferences,
 * backed by the Android Keystore.
 *
 * This is the recommended store for production use:
 * ```kotlin
 * val store = EncryptedCredentialStore(context)
 * // Pass to IDmeAuth via the internal constructor or configuration
 * ```
 */
class EncryptedCredentialStore(context: Context) : CredentialStoring {

    private val json = Json { ignoreUnknownKeys = true }

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun save(credentials: Credentials) {
        val data = json.encodeToString(credentials)
        prefs.edit().putString(KEY_CREDENTIALS, data).apply()
        Log.debug("Credentials saved to encrypted storage")
    }

    override fun load(): Credentials? {
        val data = prefs.getString(KEY_CREDENTIALS, null) ?: return null
        return try {
            json.decodeFromString<Credentials>(data)
        } catch (_: Exception) {
            null
        }
    }

    override fun delete() {
        prefs.edit().remove(KEY_CREDENTIALS).apply()
        Log.debug("Credentials deleted from encrypted storage")
    }

    companion object {
        private const val PREFS_FILE = "idme_auth_sdk_prefs"
        private const val KEY_CREDENTIALS = "idme_credentials"
    }
}
