package com.example.uniexpense.utils

import android.content.Context
import android.content.ContextWrapper
import java.util.Locale

/** Tracks the logged-in user id across app restarts. */
object SessionManager {
    private const val PREFS = "uniexpense_session"
    private const val KEY_USER_ID = "user_id"

    fun saveSession(context: Context, userId: Long) {
        prefs(context).edit().putLong(KEY_USER_ID, userId).apply()
    }

    fun getUserId(context: Context): Long =
        prefs(context).getLong(KEY_USER_ID, -1L)

    fun isLoggedIn(context: Context): Boolean = getUserId(context) != -1L

    fun clearSession(context: Context) {
        prefs(context).edit().remove(KEY_USER_ID).apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}

/** Persists the selected app language and applies it via a wrapped Context (attachBaseContext). */
object LocaleHelper {
    private const val PREFS = "uniexpense_locale"
    private const val KEY_LANG = "language_code"

    fun getSavedLanguage(context: Context): String? =
        prefs(context).getString(KEY_LANG, null)

    fun setSavedLanguage(context: Context, languageCode: String) {
        prefs(context).edit().putString(KEY_LANG, languageCode).apply()
    }

    fun hasSelectedLanguage(context: Context): Boolean = getSavedLanguage(context) != null

    /** Wrap a Context so all resources (strings.xml) resolve in [languageCode]. */
    fun wrap(context: Context, languageCode: String): ContextWrapper {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        val newContext = context.createConfigurationContext(config)
        return ContextWrapper(newContext)
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
