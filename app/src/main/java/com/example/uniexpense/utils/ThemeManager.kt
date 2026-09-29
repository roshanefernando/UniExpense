package com.example.uniexpense.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * User-controlled light/dark theme preference, independent of (but layered on
 * top of) the system setting. "system" (the default) follows the phone's own
 * dark-mode switch; "light"/"dark" pin the app regardless of the phone.
 *
 * AppCompatDelegate.setDefaultNightMode() automatically recreates any active
 * activities when the effective mode changes, so calling ThemeManager.setMode()
 * is enough to apply a change instantly - no manual recreate() needed.
 */
object ThemeManager {
    private const val PREFS = "uniexpense_theme"
    private const val KEY_MODE = "theme_mode"

    const val MODE_LIGHT = "light"
    const val MODE_DARK = "dark"
    const val MODE_SYSTEM = "system"

    fun getSavedMode(context: Context): String =
        prefs(context).getString(KEY_MODE, MODE_SYSTEM) ?: MODE_SYSTEM

    fun setMode(context: Context, mode: String) {
        prefs(context).edit().putString(KEY_MODE, mode).apply()
        applyMode(mode)
    }

    /** Call once, as early as possible (Application.onCreate), and again whenever setMode() runs. */
    fun applyMode(mode: String) {
        val nightMode = when (mode) {
            MODE_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            MODE_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
