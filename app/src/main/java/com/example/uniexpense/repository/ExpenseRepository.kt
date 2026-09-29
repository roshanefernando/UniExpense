package com.example.uniexpense.repository

import android.content.ContentValues
import android.content.Context
import com.example.uniexpense.database.DatabaseHelper
import com.example.uniexpense.model.Expense

class ExpenseRepository(context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)

    fun insert(expense: Expense): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("user_id", expense.userId)
            put("category", expense.category)
            put("amount", expense.amount)
            put("date", expense.date)
            put("note", expense.note)
        }
        return db.insert("expenses", null, values)
    }

    fun update(expense: Expense): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("category", expense.category)
            put("amount", expense.amount)
            put("date", expense.date)
            put("note", expense.note)
        }
        return db.update(
            "expenses", values,
            "expense_id = ? AND user_id = ?",
            arrayOf(expense.expenseId.toString(), expense.userId.toString())
        ) > 0
    }

    fun delete(expenseId: Long, userId: Long): Boolean {
        val db = dbHelper.writableDatabase
        return db.delete(
            "expenses", "expense_id = ? AND user_id = ?",
            arrayOf(expenseId.toString(), userId.toString())
        ) > 0
    }

    /** All expenses for a user, newest first, with optional category/search/date filters. */
    fun getExpenses(
        userId: Long,
        category: String? = null,
        searchQuery: String? = null,
        monthYear: Pair<Int, Int>? = null, // month(1-12) to year
        sortBy: SortOption = SortOption.DATE_DESC
    ): List<Expense> {
        val db = dbHelper.readableDatabase
        val where = StringBuilder("user_id = ?")
        val args = mutableListOf(userId.toString())

        if (!category.isNullOrEmpty() && category != "All") {
            where.append(" AND category = ?")
            args.add(category)
        }
        if (!searchQuery.isNullOrEmpty()) {
            where.append(" AND (category LIKE ? OR note LIKE ?)")
            args.add("%$searchQuery%")
            args.add("%$searchQuery%")
        }
        if (monthYear != null) {
            val monthStr = monthYear.first.toString().padStart(2, '0')
            where.append(" AND strftime('%Y-%m', date) = ?")
            args.add("${monthYear.second}-$monthStr")
        }

        val orderBy = when (sortBy) {
            SortOption.DATE_DESC -> "date DESC, expense_id DESC"
            SortOption.DATE_ASC -> "date ASC, expense_id ASC"
            SortOption.AMOUNT_DESC -> "amount DESC"
            SortOption.AMOUNT_ASC -> "amount ASC"
        }

        val list = mutableListOf<Expense>()
        db.rawQuery(
            "SELECT expense_id, user_id, category, amount, date, note FROM expenses WHERE $where ORDER BY $orderBy",
            args.toTypedArray()
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    Expense(
                        expenseId = c.getLong(0),
                        userId = c.getLong(1),
                        category = c.getString(2),
                        amount = c.getDouble(3),
                        date = c.getString(4),
                        note = c.getString(5)
                    )
                )
            }
        }
        return list
    }

    fun getRecent(userId: Long, limit: Int = 5): List<Expense> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Expense>()
        db.rawQuery(
            "SELECT expense_id, user_id, category, amount, date, note FROM expenses WHERE user_id = ? ORDER BY date DESC, expense_id DESC LIMIT ?",
            arrayOf(userId.toString(), limit.toString())
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    Expense(c.getLong(0), c.getLong(1), c.getString(2), c.getDouble(3), c.getString(4), c.getString(5))
                )
            }
        }
        return list
    }

    /** Sum of expenses for the user, optionally restricted to a month/year. */
    fun getTotal(userId: Long, month: Int? = null, year: Int? = null): Double {
        val db = dbHelper.readableDatabase
        val where = StringBuilder("user_id = ?")
        val args = mutableListOf(userId.toString())
        if (month != null && year != null) {
            where.append(" AND strftime('%Y-%m', date) = ?")
            args.add("$year-${month.toString().padStart(2, '0')}")
        }
        db.rawQuery("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE $where", args.toTypedArray()).use { c ->
            c.moveToFirst()
            return c.getDouble(0)
        }
    }

    /** category -> total spent, for the given month/year (used by Reports pie chart). */
    fun getCategoryBreakdown(userId: Long, month: Int, year: Int): Map<String, Double> {
        val db = dbHelper.readableDatabase
        val result = LinkedHashMap<String, Double>()
        val monthStr = "$year-${month.toString().padStart(2, '0')}"
        db.rawQuery(
            "SELECT category, SUM(amount) FROM expenses WHERE user_id = ? AND strftime('%Y-%m', date) = ? GROUP BY category ORDER BY SUM(amount) DESC",
            arrayOf(userId.toString(), monthStr)
        ).use { c ->
            while (c.moveToNext()) {
                result[c.getString(0)] = c.getDouble(1)
            }
        }
        return result
    }

    /** Total expense per month for the last [months] months (for the bar chart), oldest first. */
    fun getMonthlyTotals(userId: Long, months: Int = 6): List<Pair<String, Double>> {
        val db = dbHelper.readableDatabase
        val result = mutableListOf<Pair<String, Double>>()
        db.rawQuery(
            """
            SELECT strftime('%Y-%m', date) AS ym, SUM(amount)
            FROM expenses WHERE user_id = ?
            GROUP BY ym ORDER BY ym DESC LIMIT ?
            """.trimIndent(),
            arrayOf(userId.toString(), months.toString())
        ).use { c ->
            while (c.moveToNext()) {
                result.add(c.getString(0) to c.getDouble(1))
            }
        }
        return result.reversed()
    }

    enum class SortOption { DATE_DESC, DATE_ASC, AMOUNT_DESC, AMOUNT_ASC }
}
