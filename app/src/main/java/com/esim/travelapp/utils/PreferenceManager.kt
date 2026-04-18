package com.esim.travelapp.utils

import android.content.Context
import android.content.SharedPreferences

object PreferenceManager {
    private const val PREF_NAME = "esim_travel_app_pref"
    private const val USER_ID = "user_id"
    private const val USER_NAME = "user_name"
    private const val USER_EMAIL = "user_email"
    private const val NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val DATA_USAGE_TRACKING = "data_usage_tracking"
    private const val LANGUAGE_PREFERENCE = "language_preference"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // User Management
    fun saveUser(context: Context, userId: Int, name: String, email: String) {
        getPreferences(context).edit().apply {
            putInt(USER_ID, userId)
            putString(USER_NAME, name)
            putString(USER_EMAIL, email)
            apply()
        }
    }

    fun getUserId(context: Context): Int {
        return getPreferences(context).getInt(USER_ID, -1)
    }

    fun getUserName(context: Context): String {
        return getPreferences(context).getString(USER_NAME, "User") ?: "User"
    }

    fun getUserEmail(context: Context): String {
        return getPreferences(context).getString(USER_EMAIL, "") ?: ""
    }

    fun isLoggedIn(context: Context): Boolean {
        return getUserId(context) != -1
    }

    fun clearUser(context: Context) {
        getPreferences(context).edit().clear().apply()
    }

    // Settings Management
    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun getNotificationsEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(NOTIFICATIONS_ENABLED, true)
    }

    fun setDataUsageTrackingEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(DATA_USAGE_TRACKING, enabled).apply()
    }

    fun getDataUsageTrackingEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(DATA_USAGE_TRACKING, true)
    }

    fun setLanguagePreference(context: Context, language: String) {
        getPreferences(context).edit().putString(LANGUAGE_PREFERENCE, language).apply()
    }

    fun getLanguagePreference(context: Context): String {
        return getPreferences(context).getString(LANGUAGE_PREFERENCE, "English") ?: "English"
    }
}
