package com.example.uniexpense.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.uniexpense.databinding.ActivitySplashBinding
import com.example.uniexpense.utils.LocaleHelper
import com.example.uniexpense.utils.SessionManager

class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            routeNext()
        }, 1400)
    }

    private fun routeNext() {
        val next = when {
            !LocaleHelper.hasSelectedLanguage(this) -> LanguageActivity::class.java
            !SessionManager.isLoggedIn(this) -> LoginActivity::class.java
            else -> DashboardActivity::class.java
        }
        startActivity(Intent(this, next))
        finish()
    }
}
