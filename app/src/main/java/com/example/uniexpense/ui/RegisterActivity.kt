package com.example.uniexpense.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.uniexpense.NativeBridge
import com.example.uniexpense.R
import com.example.uniexpense.databinding.ActivityRegisterBinding
import com.example.uniexpense.repository.UserRepository
import com.example.uniexpense.utils.LocaleHelper
import com.example.uniexpense.utils.PasswordUtils

class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userRepository = UserRepository(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.tvBackToLogin.setOnClickListener { finish() }
        binding.btnRegister.setOnClickListener { attemptRegister() }
    }

    private fun attemptRegister() {
        val name = binding.tilName.editText?.text?.toString()?.trim().orEmpty()
        val email = binding.tilEmail.editText?.text?.toString()?.trim().orEmpty()
        val password = binding.tilPassword.editText?.text?.toString().orEmpty()
        val confirm = binding.tilConfirmPassword.editText?.text?.toString().orEmpty()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            toast(getString(R.string.empty_fields_error)); return
        }
        if (!NativeBridge.nativeIsValidName(name)) {
            toast(getString(R.string.name_required)); return
        }
        if (!NativeBridge.nativeIsValidEmail(email)) {
            toast(getString(R.string.invalid_email_error)); return
        }
        if (!NativeBridge.nativeIsValidPasswordLength(password)) {
            toast(getString(R.string.password_too_short)); return
        }
        if (password != confirm) {
            toast(getString(R.string.passwords_do_not_match)); return
        }
        if (userRepository.emailExists(email)) {
            toast(getString(R.string.email_already_exists)); return
        }

        val passwordHash = PasswordUtils.hash(password)
        // Native validation of the fields headed into User::registerUser().
        if (!NativeBridge.nativeRegisterValidate(name, email, passwordHash)) {
            toast(getString(R.string.empty_fields_error)); return
        }

        val language = LocaleHelper.getSavedLanguage(this) ?: "en"
        val newId = userRepository.insertUser(name, email, passwordHash, language)
        if (newId == -1L) {
            toast(getString(R.string.email_already_exists)); return
        }

        toast(getString(R.string.registration_success))
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
