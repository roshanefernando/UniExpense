package com.example.uniexpense.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.uniexpense.NativeBridge
import com.example.uniexpense.R
import com.example.uniexpense.databinding.ActivityProfileBinding
import com.example.uniexpense.databinding.DialogChangePasswordBinding
import com.example.uniexpense.databinding.DialogEditProfileBinding
import com.example.uniexpense.repository.UserRepository
import com.example.uniexpense.utils.LocaleHelper
import com.example.uniexpense.utils.PasswordUtils
import com.example.uniexpense.utils.SessionManager
import com.example.uniexpense.utils.ThemeManager

class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var userRepo: UserRepository
    private var userId: Long = -1

    private val languageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = SessionManager.getUserId(this)
        userRepo = UserRepository(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.optionEditProfile.optionLabel.setText(R.string.edit_profile)
        binding.optionChangeLanguage.optionLabel.setText(R.string.change_language)
        binding.optionAppearance.optionLabel.setText(R.string.appearance)
        binding.optionChangePassword.optionLabel.setText(R.string.change_password)

        binding.optionEditProfile.root.setOnClickListener { showEditProfileDialog() }
        binding.optionChangeLanguage.root.setOnClickListener {
            languageLauncher.launch(Intent(this, LanguageActivity::class.java).apply {
                putExtra(LanguageActivity.EXTRA_FROM_PROFILE, true)
            })
        }
        binding.optionAppearance.root.setOnClickListener { showThemeDialog() }
        binding.optionChangePassword.root.setOnClickListener { showChangePasswordDialog() }
        binding.btnLogout.setOnClickListener { confirmLogout() }

        loadProfile()
    }

    private fun loadProfile() {
        val user = userRepo.findById(userId) ?: return
        binding.textName.text = user.name
        binding.textEmail.text = user.email
        binding.textLanguage.text = when (LocaleHelper.getSavedLanguage(this)) {
            "si" -> getString(R.string.lang_sinhala)
            "ta" -> getString(R.string.lang_tamil)
            else -> getString(R.string.lang_english)
        }
    }

    private fun showThemeDialog() {
        val modes = arrayOf(ThemeManager.MODE_LIGHT, ThemeManager.MODE_DARK, ThemeManager.MODE_SYSTEM)
        val labels = arrayOf(getString(R.string.theme_light), getString(R.string.theme_dark), getString(R.string.theme_system))
        val currentIndex = modes.indexOf(ThemeManager.getSavedMode(this)).let { if (it == -1) 2 else it }

        AlertDialog.Builder(this)
            .setTitle(R.string.choose_theme)
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                ThemeManager.setMode(this, modes[which])
                dialog.dismiss()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun showEditProfileDialog() {
        val user = userRepo.findById(userId) ?: return
        val dialogBinding = DialogEditProfileBinding.inflate(LayoutInflater.from(this))
        dialogBinding.tilName.editText?.setText(user.name)
        dialogBinding.tilEmail.editText?.setText(user.email)

        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()
        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.tilName.editText?.text?.toString()?.trim().orEmpty()
            val email = dialogBinding.tilEmail.editText?.text?.toString()?.trim().orEmpty()

            if (name.isEmpty() || email.isEmpty()) {
                toast(getString(R.string.empty_fields_error)); return@setOnClickListener
            }
            if (!NativeBridge.nativeUpdateProfileValidate(name, email)) {
                toast(getString(R.string.invalid_email_error)); return@setOnClickListener
            }
            if (email != user.email && userRepo.emailExists(email)) {
                toast(getString(R.string.email_already_exists)); return@setOnClickListener
            }
            userRepo.updateProfile(userId, name, email)
            toast(getString(R.string.profile_updated))
            loadProfile()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showChangePasswordDialog() {
        val user = userRepo.findById(userId) ?: return
        val dialogBinding = DialogChangePasswordBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        dialogBinding.btnSave.setOnClickListener {
            val current = dialogBinding.tilCurrentPassword.editText?.text?.toString().orEmpty()
            val newPass = dialogBinding.tilNewPassword.editText?.text?.toString().orEmpty()
            val confirm = dialogBinding.tilConfirmPassword.editText?.text?.toString().orEmpty()

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                toast(getString(R.string.empty_fields_error)); return@setOnClickListener
            }
            val currentHash = PasswordUtils.hash(current)
            if (!NativeBridge.nativeLoginValidate(currentHash, user.passwordHash)) {
                toast(getString(R.string.wrong_current_password)); return@setOnClickListener
            }
            if (!NativeBridge.nativeIsValidPasswordLength(newPass)) {
                toast(getString(R.string.password_too_short)); return@setOnClickListener
            }
            if (newPass != confirm) {
                toast(getString(R.string.passwords_do_not_match)); return@setOnClickListener
            }

            userRepo.updatePasswordHash(userId, PasswordUtils.hash(newPass))
            toast(getString(R.string.password_changed))
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle(R.string.logout_confirm_title)
            .setMessage(R.string.logout_confirm_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                SessionManager.clearSession(this)
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
