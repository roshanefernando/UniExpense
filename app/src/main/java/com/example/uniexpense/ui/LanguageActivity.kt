package com.example.uniexpense.ui

import android.content.Intent
import android.os.Bundle
import com.example.uniexpense.databinding.ActivityLanguageBinding
import com.example.uniexpense.utils.LocaleHelper

class LanguageActivity : BaseActivity() {

    private lateinit var binding: ActivityLanguageBinding
    private var fromProfile = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fromProfile = intent.getBooleanExtra(EXTRA_FROM_PROFILE, false)

        when (LocaleHelper.getSavedLanguage(this)) {
            "si" -> binding.rbSinhala.isChecked = true
            "ta" -> binding.rbTamil.isChecked = true
            else -> binding.rbEnglish.isChecked = true
        }

        binding.btnContinue.setOnClickListener {
            val code = when (binding.radioGroupLanguage.checkedRadioButtonId) {
                binding.rbSinhala.id -> "si"
                binding.rbTamil.id -> "ta"
                else -> "en"
            }
            LocaleHelper.setSavedLanguage(this, code)

            if (fromProfile) {
                setResult(RESULT_OK)
                finish()
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_FROM_PROFILE = "from_profile"
    }
}
