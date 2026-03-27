package com.idme.auth.auth

import android.app.Activity
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.idme.auth.errors.IDmeAuthError
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CompletableDeferred

/**
 * Singleton that manages the bridge between the Chrome Custom Tab auth flow
 * and the coroutine-based SDK API.
 *
 * Flow:
 * 1. [launchAuth] stores a [CompletableDeferred] keyed by the session's `state` value and opens a Custom Tab.
 * 2. The browser redirects to the app's scheme, which is caught by [IDmeRedirectActivity].
 * 3. [handleRedirect] extracts the `state` from the callback URL and routes the result to the
 *    matching deferred, preventing cross-flow code injection.
 * 4. [launchAuth] resumes and returns the callback URL to the caller.
 */
internal object IDmeAuthManager {
    private val pending = ConcurrentHashMap<String, CompletableDeferred<String>>()

    /**
     * Launches the authentication flow in a Chrome Custom Tab and suspends
     * until the redirect is received.
     *
     * @param activity The Activity to launch from.
     * @param authUrl The authorization URL to open.
     * @param sessionId The `state` value for this flow; used to route the callback to the correct coroutine.
     * @return The full callback URL string including query parameters.
     */
    suspend fun launchAuth(activity: Activity, authUrl: String, sessionId: String): String {
        val deferred = CompletableDeferred<String>()
        pending[sessionId] = deferred

        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()

        customTabsIntent.launchUrl(activity, Uri.parse(authUrl))

        return try {
            deferred.await()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw IDmeAuthError.UserCancelled
        } finally {
            pending.remove(sessionId)
        }
    }

    /** Called by [IDmeRedirectActivity] when the redirect URI is received. Routes to the matching session. */
    internal fun handleRedirect(callbackUrl: String) {
        val state = try {
            Uri.parse(callbackUrl).getQueryParameter("state")
        } catch (_: Exception) {
            null
        }
        if (state != null) {
            pending[state]?.complete(callbackUrl)
        } else {
            // No state in callback (error or non-compliant response) — deliver to any waiting flow
            pending.values.firstOrNull()?.complete(callbackUrl)
        }
    }

    /** Called by [IDmeRedirectActivity] when no URI data is present. Cancels all pending flows. */
    internal fun handleCancel() {
        pending.values.forEach { it.completeExceptionally(IDmeAuthError.UserCancelled) }
        pending.clear()
    }
}
