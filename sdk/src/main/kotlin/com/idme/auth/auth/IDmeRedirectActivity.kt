package com.idme.auth.auth

import android.app.Activity
import android.os.Bundle

/**
 * Transparent Activity that captures the OAuth redirect from the browser.
 *
 * The consuming app must declare this Activity in their AndroidManifest.xml
 * with an intent-filter matching the redirect URI scheme:
 *
 * ```xml
 * <activity
 *     android:name="com.idme.auth.auth.IDmeRedirectActivity"
 *     android:exported="true"
 *     android:launchMode="singleTask"
 *     android:theme="@android:style/Theme.Translucent.NoTitleBar">
 *     <intent-filter>
 *         <action android:name="android.intent.action.VIEW" />
 *         <category android:name="android.intent.category.DEFAULT" />
 *         <category android:name="android.intent.category.BROWSABLE" />
 *         <data
 *             android:scheme="yourapp"
 *             android:host="idme"
 *             android:path="/callback" />
 *     </intent-filter>
 * </activity>
 * ```
 */
class IDmeRedirectActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent()
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent()
    }

    private fun handleIntent() {
        val uri = intent?.data
        if (uri != null) {
            IDmeAuthManager.handleRedirect(uri.toString())
        } else {
            IDmeAuthManager.handleCancel()
        }
        finish()
    }
}
