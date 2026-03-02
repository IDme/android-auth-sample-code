package com.idme.auth.token

import com.idme.auth.errors.IDmeAuthError
import com.idme.auth.models.Credentials
import com.idme.auth.storage.CredentialStoring
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages token lifecycle: storage, retrieval, and refresh.
 * Coalesces concurrent refresh requests into a single network call.
 */
class TokenManager(
    private val credentialStore: CredentialStoring,
    private val refresher: TokenRefreshing
) {
    private var cachedCredentials: Credentials? = null
    private val mutex = Mutex()
    private var refreshDeferred: Deferred<Credentials>? = null

    /** Returns stored credentials, loading from storage if necessary. */
    fun currentCredentials(): Credentials? {
        if (cachedCredentials != null) return cachedCredentials
        val loaded = credentialStore.load()
        cachedCredentials = loaded
        return loaded
    }

    /** Stores new credentials in both memory and persistent storage. */
    fun store(credentials: Credentials) {
        cachedCredentials = credentials
        credentialStore.save(credentials)
    }

    /**
     * Returns valid credentials, refreshing if they expire within [minTTL] seconds.
     * Coalesces concurrent refresh calls into a single network request.
     */
    suspend fun validCredentials(minTTL: Long = 60): Credentials {
        val credentials = currentCredentials()
            ?: throw IDmeAuthError.NotAuthenticated

        if (!credentials.expiresWithin(minTTL)) {
            return credentials
        }

        // Coalesce concurrent refresh requests
        return mutex.withLock {
            // Re-check: another coroutine may have already refreshed
            val current = cachedCredentials
            if (current != null && !current.expiresWithin(minTTL)) {
                return@withLock current
            }

            val refreshToken = credentials.refreshToken
                ?: throw IDmeAuthError.RefreshTokenExpired

            val existing = refreshDeferred
            if (existing != null && existing.isActive) {
                return@withLock existing.await()
            }

            coroutineScope {
                val deferred = async {
                    val tokenResponse = refresher.refresh(refreshToken)
                    val newCredentials = tokenResponse.toCredentials()
                    store(newCredentials)
                    newCredentials
                }
                refreshDeferred = deferred
                try {
                    deferred.await()
                } finally {
                    refreshDeferred = null
                }
            }
        }
    }

    /** Clears all stored credentials. */
    suspend fun clear() {
        mutex.withLock {
            cachedCredentials = null
            refreshDeferred?.cancel()
            refreshDeferred = null
            credentialStore.delete()
        }
    }

    /** Clears all stored credentials synchronously (for logout from non-suspend context). */
    fun clearSync() {
        cachedCredentials = null
        refreshDeferred?.cancel()
        refreshDeferred = null
        credentialStore.delete()
    }
}
