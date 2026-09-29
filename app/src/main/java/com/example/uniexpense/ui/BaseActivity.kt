package com.example.uniexpense.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.uniexpense.utils.LocaleHelper

/**
 * Every activity extends this so that once a language is chosen on the
 * Language Selection screen, ALL screens (not just the ones after it)
 * render in that language - attachBaseContext runs before onCreate, so
 * resource lookups (strings.xml) resolve against the saved locale from the
 * very first frame.
 */
abstract class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val lang = LocaleHelper.getSavedLanguage(newBase) ?: "en"
        super.attachBaseContext(LocaleHelper.wrap(newBase, lang))
    }
}
