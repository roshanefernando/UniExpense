package com.example.uniexpense.utils

import java.security.MessageDigest
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

/**
 * Password hashing lives here (Kotlin/Android's own crypto APIs) rather than
 * hand-rolled in C++: Android's java.security.MessageDigest is the audited,
 * correct implementation, and re-implementing SHA-256 by hand in native code
 * would only add risk without adding real OOP value. The C++ layer still
 * owns the *comparison* logic (User.login()) once hashes are computed.
 */
object PasswordUtils {
    // A fixed app-level salt is layered on top of the password before hashing.
    // This is a lightweight offline-app measure, not a replacement for a
    // proper per-user random salt in a server-backed product.
    private const val SALT = "UniExpense_v1_salt"

    fun hash(plainPassword: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest((SALT + plainPassword).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

object CurrencyFormatter {
    fun format(amount: Double): String {
        val nf = NumberFormat.getNumberInstance(Locale.US)
        nf.maximumFractionDigits = 2
        nf.minimumFractionDigits = 2
        return "Rs. ${nf.format(amount)}"
    }
}

object DateUtils {
    fun todayIso(): String {
        val cal = Calendar.getInstance()
        return "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun currentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    fun monthName(month: Int): String {
        val names = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        return names.getOrElse(month - 1) { "" }
    }
}
