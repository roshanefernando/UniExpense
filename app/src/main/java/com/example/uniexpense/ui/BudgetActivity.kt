package com.example.uniexpense.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.uniexpense.NativeBridge
import com.example.uniexpense.R
import com.example.uniexpense.databinding.ActivityBudgetBinding
import com.example.uniexpense.repository.BudgetRepository
import com.example.uniexpense.repository.ExpenseRepository
import com.example.uniexpense.utils.CurrencyFormatter
import com.example.uniexpense.utils.DateUtils
import com.example.uniexpense.utils.SessionManager

class BudgetActivity : BaseActivity() {

    private lateinit var binding: ActivityBudgetBinding
    private lateinit var budgetRepo: BudgetRepository
    private lateinit var expenseRepo: ExpenseRepository
    private var userId: Long = -1
    private val month = DateUtils.currentMonth()
    private val year = DateUtils.currentYear()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = SessionManager.getUserId(this)
        budgetRepo = BudgetRepository(this)
        expenseRepo = ExpenseRepository(this)

        binding.textMonth.text = "${DateUtils.monthName(month)} $year"
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSaveBudget.setOnClickListener { saveBudget() }
    }

    override fun onResume() {
        super.onResume()
        reload()
    }

    private fun reload() {
        val totalSpent = expenseRepo.getTotal(userId, month, year)
        val budget = budgetRepo.getBudget(userId, month, year)

        if (budget == null) {
            binding.textNoBudget.visibility = View.VISIBLE
            binding.layoutBudgetStatus.visibility = View.GONE
            return
        }

        binding.textNoBudget.visibility = View.GONE
        binding.layoutBudgetStatus.visibility = View.VISIBLE
        binding.tilBudgetAmount.editText?.setText(budget.monthlyBudget.toString())

        val status = NativeBridge.nativeCheckBudget(budget.monthlyBudget, totalSpent)
        val exceeded = status[1] == 1.0
        val remaining = status[2]
        val percentUsed = status[3]

        binding.textBudgetAmount.text = CurrencyFormatter.format(budget.monthlyBudget)
        binding.textSpent.text = CurrencyFormatter.format(totalSpent)
        binding.textRemaining.text = CurrencyFormatter.format(if (remaining < 0) 0.0 else remaining)
        binding.progressBudget.progress = percentUsed.coerceIn(0.0, 100.0).toInt()
        binding.textUsedPercent.text = "%.1f%% used".format(percentUsed)

        if (exceeded) {
            binding.textExceeded.visibility = View.VISIBLE
            binding.textExceeded.text = getString(R.string.budget_exceeded_by, CurrencyFormatter.format(-remaining))
        } else {
            binding.textExceeded.visibility = View.GONE
        }
    }

    private fun saveBudget() {
        val amountText = binding.tilBudgetAmount.editText?.text?.toString()?.trim().orEmpty()
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            Toast.makeText(this, R.string.amount_must_be_positive, Toast.LENGTH_SHORT).show()
            return
        }
        budgetRepo.setBudget(userId, amount, month, year)
        Toast.makeText(this, R.string.budget_saved, Toast.LENGTH_SHORT).show()
        reload()
    }
}
