package com.idme.auth.utilities

/** Internal logger wrapper using Android's Log. Disabled by default; enable via [isEnabled]. */
object Log {
    private const val TAG = "IDmeAuthSDK"

    /** Set to true to enable SDK log output. Disabled by default to prevent credential leakage in release builds. */
    var isEnabled: Boolean = false

    fun debug(message: String) {
        if (isEnabled) android.util.Log.d(TAG, message)
    }

    fun info(message: String) {
        if (isEnabled) android.util.Log.i(TAG, message)
    }

    fun error(message: String) {
        if (isEnabled) android.util.Log.e(TAG, message)
    }
}
