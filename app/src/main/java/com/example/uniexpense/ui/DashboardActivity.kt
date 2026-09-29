package com.example.uniexpense.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uniexpense.NativeBridge
import com.example.uniexpense.R
import com.example.uniexpense.adapter.ExpenseAdapter
import com.example.uniexpense.databinding.ActivityDashboardBinding
import com.example.uniexpense.databinding.ItemQuickActionBinding
import com.example.uniexpense.repository.BudgetRepository
import com.example.uniexpense.repository.ExpenseRepository
import com.example.uniexpense.repository.IncomeRepository
import com.example.uniexpense.repository.UserRepository
import com.example.uniexpense.utils.CurrencyFormatter
import com.example.uniexpense.utils.DateUtils
import com.example.uniexpense.utils.SessionManager

class DashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var expenseRepo: ExpenseRepository
    private lateinit var incomeRepo: IncomeRepository
    private lateinit var budgetRepo: BudgetRepository
    private lateinit var userRepo: UserRepository
    private var userId: Long = -1
    private lateinit var recentAdapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = SessionManager.getUserId(this)
        if (userId == -1L) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        expenseRepo = ExpenseRepository(this)
        incomeRepo = IncomeRepository(this)
        budgetRepo = BudgetRepository(this)
        userRepo = UserRepository(this)

        recentAdapter = ExpenseAdapter(emptyList(), showActions = false)
        binding.recyclerRecentExpenses.layoutManager = LinearLayoutManager(this)
        binding.recyclerRecentExpenses.adapter = recentAdapter

        setupQuickActions()
        setupBottomNav()

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun setupQuickActions() {
        bindAction(binding.actionAddExpense, R.drawable.ic_add, R.string.add_expense) {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }
        bindAction(binding.actionAddIncome, R.drawable.ic_add, R.string.add_income) {
            AddIncomeDialog.newInstance().show(supportFragmentManager, "add_income")
        }
        bindAction(binding.actionHistory, R.drawable.ic_history, R.string.expense_history) {
            startActivity(Intent(this, ExpenseHistoryActivity::class.java))
        }
        bindAction(binding.actionBudget, R.drawable.ic_budget, R.string.budget) {
            startActivity(Intent(this, BudgetActivity::class.java))
        }
        bindAction(binding.actionReports, R.drawable.ic_reports, R.string.reports) {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
    }

    private fun bindAction(included: ItemQuickActionBinding, iconRes: Int, labelRes: Int, onClick: () -> Unit) {
        included.iconAction.setImageResource(iconRes)
        included.labelAction.setText(labelRes)
        included.root.setOnClickListener { onClick() }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_home
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_history -> { startActivity(Intent(this, ExpenseHistoryActivity::class.java)); false }
                R.id.nav_add -> { startActivity(Intent(this, AddExpenseActivity::class.java)); false }
                R.id.nav_budget -> { startActivity(Intent(this, BudgetActivity::class.java)); false }
                R.id.nav_reports -> { startActivity(Intent(this, ReportsActivity::class.java)); false }
                else -> false
            }
        }
    }

    private fun loadDashboardData() {
        val user = userRepo.findById(userId)
        binding.textGreeting.text = getString(R.string.greeting, user?.name ?: "")

        val month = DateUtils.currentMonth()
        val year = DateUtils.currentYear()

        val totalIncome = incomeRepo.getTotal(userId, month, year)
        val totalExpense = expenseRepo.getTotal(userId, month, year)
        val balance = NativeBridge.nativeCalculateBalance(totalIncome, totalExpense)

        binding.textTotalIncome.text = CurrencyFormatter.format(totalIncome)
        binding.textTotalExpenses.text = CurrencyFormatter.format(totalExpense)
        binding.textBalance.text = CurrencyFormatter.format(balance)

        val budget = budgetRepo.getBudget(userId, month, year)
        if (budget == null) {
            binding.textBudget.text = getString(R.string.no_budget_set)
            binding.textRemainingBudget.text = getString(R.string.no_budget_set)
            binding.textWarning.visibility = View.GONE
        } else {
            binding.textBudget.text = CurrencyFormatter.format(budget.monthlyBudget)
            val status = NativeBridge.nativeCheckBudget(budget.monthlyBudget, totalExpense)
            val exceeded = status[1] == 1.0
            val remaining = status[2]
            val percentUsed = status[3]
            binding.textRemainingBudget.text = CurrencyFormatter.format(remaining)

            when {
                exceeded -> {
                    binding.textWarning.visibility = View.VISIBLE
                    binding.textWarning.text = getString(R.string.budget_warning_exceeded)
                }
                percentUsed >= 80.0 -> {
                    binding.textWarning.visibility = View.VISIBLE
                    binding.textWarning.text = getString(R.string.budget_warning_near)
                }
                else -> binding.textWarning.visibility = View.GONE
            }
        }

        val recent = expenseRepo.getRecent(userId, 5)
        recentAdapter.submitList(recent)
        binding.textEmptyExpenses.visibility = if (recent.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerRecentExpenses.visibility = if (recent.isEmpty()) View.GONE else View.VISIBLE
    }
}
