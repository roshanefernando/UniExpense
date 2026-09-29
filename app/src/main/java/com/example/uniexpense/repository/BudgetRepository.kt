package com.example.uniexpense.repository

import android.content.ContentValues
import android.content.Context
import com.example.uniexpense.database.DatabaseHelper
import com.example.uniexpense.model.Budget

class BudgetRepository(context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)

    /** Insert or update the budget for this user/month/year (one row per user per month). */
    fun setBudget(userId: Long, amount: Double, month: Int, year: Int): Boolean {
        val db = dbHelper.writableDatabase
        val existing = getBudget(userId, month, year)
        val values = ContentValues().apply {
            put("user_id", userId)
            put("monthly_budget", amount)
            put("month", month)
            put("year", year)
        }
        return if (existing != null) {
            db.update(
                "budget", values, "budget_id = ?",
                arrayOf(existing.budgetId.toString())
            ) > 0
        } else {
            db.insert("budget", null, values) != -1L
        }
    }

    fun getBudget(userId: Long, month: Int, year: Int): Budget? {
        val db = dbHelper.readableDatabase
        db.rawQuery(
            "SELECT budget_id, user_id, monthly_budget, month, year FROM budget WHERE user_id = ? AND month = ? AND year = ? LIMIT 1",
            arrayOf(userId.toString(), month.toString(), year.toString())
        ).use { c ->
            if (!c.moveToFirst()) return null
            return Budget(c.getLong(0), c.getLong(1), c.getDouble(2), c.getInt(3), c.getInt(4))
        }
    }
}
