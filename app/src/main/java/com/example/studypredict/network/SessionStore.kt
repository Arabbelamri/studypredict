package com.example.studypredict.network

import android.content.Context

object SessionStore {
    private const val PREF_NAME = "studypredict_session"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_EMAIL = "email"
    private const val KEY_DISPLAY_NAME = "display_name"

    fun saveSession(
        context: Context,
        accessToken: String,
        refreshToken: String,
        email: String,
        displayName: String
    ) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_EMAIL, email)
            .putString(KEY_DISPLAY_NAME, displayName)
            .apply()
    }

    fun getAccessToken(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_REFRESH_TOKEN, null)
    }

    fun getEmail(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_EMAIL, null)
    }

    fun getDisplayName(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DISPLAY_NAME, null)
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_EMAIL)
            .remove(KEY_DISPLAY_NAME)
            .apply()
    }
}
