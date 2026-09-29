package com.example.uniexpense.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.uniexpense.NativeBridge
import com.example.uniexpense.databinding.ActivityLoginBinding
import com.example.uniexpense.databinding.DialogForgotPasswordBinding
import com.example.uniexpense.repository.UserRepository
import com.example.uniexpense.utils.PasswordUtils
import com.example.uniexpense.utils.SessionManager

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userRepository = UserRepository(this)

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.tvForgotPassword.setOnClickListener { showForgotPasswordDialog() }
    }

    private fun attemptLogin() {
        val email = binding.tilEmail.editText?.text?.toString()?.trim().orEmpty()
        val password = binding.tilPassword.editText?.text?.toString().orEmpty()

        if (email.isEmpty() || password.isEmpty()) {
            toast(getString(com.example.uniexpense.R.string.empty_fields_error))
            return
        }
        if (!NativeBridge.nativeIsValidEmail(email)) {
            toast(getString(com.example.uniexpense.R.string.invalid_email_error))
            return
        }

        val user = userRepository.findByEmail(email)
        val inputHash = PasswordUtils.hash(password)

        val valid = user != null && NativeBridge.nativeLoginValidate(inputHash, user.passwordHash)
        if (valid) {
            SessionManager.saveSession(this, user!!.id)
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        } else {
            toast(getString(com.example.uniexpense.R.string.invalid_credentials))
        }
    }

    private fun showForgotPasswordDialog() {
        val dialogBinding = DialogForgotPasswordBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        dialogBinding.btnReset.setOnClickListener {
            val email = dialogBinding.tilEmail.editText?.text?.toString()?.trim().orEmpty()
            val newPass = dialogBinding.tilNewPassword.editText?.text?.toString().orEmpty()
            val confirmPass = dialogBinding.tilConfirmPassword.editText?.text?.toString().orEmpty()

            if (email.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                toast(getString(com.example.uniexpense.R.string.empty_fields_error)); return@setOnClickListener
            }
            if (!userRepository.emailExists(email)) {
                toast(getString(com.example.uniexpense.R.string.email_not_found)); return@setOnClickListener
            }
            if (!NativeBridge.nativeIsValidPasswordLength(newPass)) {
                toast(getString(com.example.uniexpense.R.string.password_too_short)); return@setOnClickListener
            }
            if (newPass != confirmPass) {
                toast(getString(com.example.uniexpense.R.string.passwords_do_not_match)); return@setOnClickListener
            }

            val user = userRepository.findByEmail(email)!!
            userRepository.updatePasswordHash(user.id, PasswordUtils.hash(newPass))
            toast(getString(com.example.uniexpense.R.string.password_reset_success))
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
