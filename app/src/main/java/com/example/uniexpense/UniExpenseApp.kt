package com.example.uniexpense

import android.app.Application
import com.example.uniexpense.utils.ThemeManager

class UniExpenseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Must run before any Activity inflates a view, so the very first
        // screen already renders in the user's chosen theme.
        ThemeManager.applyMode(ThemeManager.getSavedMode(this))
    }
}
