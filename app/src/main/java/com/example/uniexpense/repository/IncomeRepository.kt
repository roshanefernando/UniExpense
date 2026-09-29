package com.example.uniexpense.repository

import android.content.ContentValues
import android.content.Context
import com.example.uniexpense.database.DatabaseHelper
import com.example.uniexpense.model.Income

class IncomeRepository(context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)

    fun insert(income: Income): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("user_id", income.userId)
            put("source", income.source)
            put("amount", income.amount)
            put("date", income.date)
            put("note", income.note)
        }
        return db.insert("income", null, values)
    }

    fun update(income: Income): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("source", income.source)
            put("amount", income.amount)
            put("date", income.date)
            put("note", income.note)
        }
        return db.update(
            "income", values, "income_id = ? AND user_id = ?",
            arrayOf(income.incomeId.toString(), income.userId.toString())
        ) > 0
    }

    fun delete(incomeId: Long, userId: Long): Boolean {
        val db = dbHelper.writableDatabase
        return db.delete(
            "income", "income_id = ? AND user_id = ?",
            arrayOf(incomeId.toString(), userId.toString())
        ) > 0
    }

    fun getAll(userId: Long): List<Income> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Income>()
        db.rawQuery(
            "SELECT income_id, user_id, source, amount, date, note FROM income WHERE user_id = ? ORDER BY date DESC, income_id DESC",
            arrayOf(userId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                list.add(Income(c.getLong(0), c.getLong(1), c.getString(2), c.getDouble(3), c.getString(4), c.getString(5)))
            }
        }
        return list
    }

    fun getTotal(userId: Long, month: Int? = null, year: Int? = null): Double {
        val db = dbHelper.readableDatabase
        val where = StringBuilder("user_id = ?")
        val args = mutableListOf(userId.toString())
        if (month != null && year != null) {
            where.append(" AND strftime('%Y-%m', date) = ?")
            args.add("$year-${month.toString().padStart(2, '0')}")
        }
        db.rawQuery("SELECT COALESCE(SUM(amount), 0) FROM income WHERE $where", args.toTypedArray()).use { c ->
            c.moveToFirst()
            return c.getDouble(0)
        }
    }
}
