package com.example.uniexpense

/**
 * Single point of contact between Kotlin and the native C++ OOP layer
 * (User / Expense / Income / Budget / Report, see app/src/main/cpp).
 *
 * Design: Kotlin owns persistence (SQLite via the repository classes) and
 * UI. C++ owns validation and numeric business logic. Every function here
 * maps 1:1 to a JNIEXPORT function in NativeBridge.cpp - the method names
 * below MUST match the Java_com_example_uniexpense_NativeBridge_xxx names
 * on the C++ side exactly, or the app will crash with
 * UnsatisfiedLinkError at runtime.
 */
object NativeBridge {

    init {
        System.loadLibrary("uniexpense_native")
    }

    // ---- User ----
    external fun nativeIsValidName(name: String): Boolean
    external fun nativeIsValidEmail(email: String): Boolean
    external fun nativeIsValidPasswordLength(password: String): Boolean
    external fun nativeRegisterValidate(name: String, email: String, passwordHash: String): Boolean
    external fun nativeLoginValidate(inputHash: String, storedHash: String): Boolean
    external fun nativeUpdateProfileValidate(name: String, email: String): Boolean

    // ---- Expense ----
    external fun nativeAddExpenseValidate(category: String, amount: Double, date: String): Boolean

    // ---- Income ----
    external fun nativeAddIncomeValidate(source: String, amount: Double, date: String): Boolean

    // ---- Report / calculations ----
    external fun nativeCalculateTotal(amounts: DoubleArray): Double
    external fun nativeCalculateBalance(income: Double, expense: Double): Double

    // ---- Budget ----
    // Returns [isSet(0/1), exceeded(0/1), remaining, percentageUsed]
    external fun nativeCheckBudget(monthlyBudget: Double, totalSpent: Double): DoubleArray
}
