package com.example.uniexpense.model

data class User(
    val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val language: String = "en"
)

data class Expense(
    val expenseId: Long = 0,
    val userId: Long,
    val category: String,
    val amount: Double,
    val date: String, // yyyy-MM-dd
    val note: String? = null
)

data class Income(
    val incomeId: Long = 0,
    val userId: Long,
    val source: String,
    val amount: Double,
    val date: String,
    val note: String? = null
)

data class Budget(
    val budgetId: Long = 0,
    val userId: Long,
    val monthlyBudget: Double,
    val month: Int, // 1-12
    val year: Int
)

/** Categories offered in the Add/Edit Expense screen. */
object ExpenseCategories {
    val ALL = listOf(
        "Food", "Transport", "Education", "Books", "Hostel/Rent",
        "Utilities", "Entertainment", "Shopping", "Health", "Other"
    )
}

/** Suggested income sources shown in the Add Income sheet. */
object IncomeSources {
    val ALL = listOf("Parents", "Scholarship", "Part-time job", "Allowance", "Other")
}
