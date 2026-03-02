package com.idme.auth.auth

import android.app.Activity
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.idme.auth.errors.IDmeAuthError
import kotlinx.coroutines.CompletableDeferred

/**
 * Singleton that manages the bridge between the Chrome Custom Tab auth flow
 * and the coroutine-based SDK API.
 *
 * Flow:
 * 1. [launchAuth] stores a [CompletableDeferred] and opens a Custom Tab.
 * 2. The browser redirects to the app's scheme, which is caught by [IDmeRedirectActivity].
 * 3. [handleRedirect] completes the deferred with the callback URL.
 * 4. [launchAuth] resumes and returns the callback URL to the caller.
 */
internal object IDmeAuthManager {
    private var pendingAuth: CompletableDeferred<String>? = null

    /**
     * Launches the authentication flow in a Chrome Custom Tab and suspends
     * until the redirect is received.
     *
     * @param activity The Activity to launch from.
     * @param authUrl The authorization URL to open.
     * @return The full callback URL string including query parameters.
     */
    suspend fun launchAuth(activity: Activity, authUrl: String): String {
        // Cancel any existing pending auth
        pendingAuth?.cancel()

        val deferred = CompletableDeferred<String>()
        pendingAuth = deferred

        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()

        customTabsIntent.launchUrl(activity, Uri.parse(authUrl))

        return try {
            deferred.await()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw IDmeAuthError.UserCancelled
        } finally {
            pendingAuth = null
        }
    }

    /** Called by [IDmeRedirectActivity] when the redirect URI is received. */
    internal fun handleRedirect(callbackUrl: String) {
        pendingAuth?.complete(callbackUrl)
    }

    /** Called by [IDmeRedirectActivity] when no URI data is present. */
    internal fun handleCancel() {
        pendingAuth?.completeExceptionally(IDmeAuthError.UserCancelled)
    }
}
