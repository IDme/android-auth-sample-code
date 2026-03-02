package com.idme.auth.utilities

/** Internal logger wrapper using Android's Log. */
object Log {
    private const val TAG = "IDmeAuthSDK"

    fun debug(message: String) {
        android.util.Log.d(TAG, message)
    }

    fun info(message: String) {
        android.util.Log.i(TAG, message)
    }

    fun error(message: String) {
        android.util.Log.e(TAG, message)
    }
}
