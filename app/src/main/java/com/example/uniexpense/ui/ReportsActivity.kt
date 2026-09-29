package com.example.uniexpense.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.example.uniexpense.NativeBridge
import com.example.uniexpense.R
import com.example.uniexpense.adapter.BarChartView
import com.example.uniexpense.adapter.PieChartView
import com.example.uniexpense.databinding.ActivityReportsBinding
import com.example.uniexpense.databinding.ItemLegendBinding
import com.example.uniexpense.repository.BudgetRepository
import com.example.uniexpense.repository.ExpenseRepository
import com.example.uniexpense.repository.IncomeRepository
import com.example.uniexpense.utils.CurrencyFormatter
import com.example.uniexpense.utils.DateUtils
import com.example.uniexpense.utils.SessionManager
import java.util.Calendar

class ReportsActivity : BaseActivity() {

    private lateinit var binding: ActivityReportsBinding
    private lateinit var expenseRepo: ExpenseRepository
    private lateinit var incomeRepo: IncomeRepository
    private lateinit var budgetRepo: BudgetRepository
    private var userId: Long = -1
    private var viewingCurrentMonth = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = SessionManager.getUserId(this)
        expenseRepo = ExpenseRepository(this)
        incomeRepo = IncomeRepository(this)
        budgetRepo = BudgetRepository(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.rowIncome.rowLabel.text = getString(R.string.total_income)
        binding.rowExpense.rowLabel.text = getString(R.string.total_expenses)
        binding.rowBalance.rowLabel.text = getString(R.string.balance)
        binding.rowBudget.rowLabel.text = getString(R.string.monthly_budget)
        binding.rowRemaining.rowLabel.text = getString(R.string.remaining_budget)

        binding.toggleMonth.check(binding.btnCurrentMonth.id)
        binding.toggleMonth.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                viewingCurrentMonth = checkedId == binding.btnCurrentMonth.id
                loadReport()
            }
        }

        loadReport()
    }

    private fun targetMonthYear(): Pair<Int, Int> {
        if (viewingCurrentMonth) return DateUtils.currentMonth() to DateUtils.currentYear()
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        return (cal.get(Calendar.MONTH) + 1) to cal.get(Calendar.YEAR)
    }

    private fun loadReport() {
        val (month, year) = targetMonthYear()

        val totalIncome = incomeRepo.getTotal(userId, month, year)
        val totalExpense = expenseRepo.getTotal(userId, month, year)

        if (totalIncome == 0.0 && totalExpense == 0.0) {
            binding.textNotEnoughData.visibility = View.VISIBLE
            binding.layoutReportContent.visibility = View.GONE
            return
        }
        binding.textNotEnoughData.visibility = View.GONE
        binding.layoutReportContent.visibility = View.VISIBLE

        val balance = NativeBridge.nativeCalculateBalance(totalIncome, totalExpense)
        val budget = budgetRepo.getBudget(userId, month, year)

        binding.rowIncome.rowValue.text = CurrencyFormatter.format(totalIncome)
        binding.rowExpense.rowValue.text = CurrencyFormatter.format(totalExpense)
        binding.rowBalance.rowValue.text = CurrencyFormatter.format(balance)

        if (budget == null) {
            binding.rowBudget.rowValue.text = getString(R.string.no_budget_set)
            binding.rowRemaining.rowValue.text = getString(R.string.no_budget_set)
        } else {
            val status = NativeBridge.nativeCheckBudget(budget.monthlyBudget, totalExpense)
            binding.rowBudget.rowValue.text = CurrencyFormatter.format(budget.monthlyBudget)
            binding.rowRemaining.rowValue.text = CurrencyFormatter.format(status[2])
        }

        loadCategoryBreakdown(month, year)
        loadMonthlyBarChart()
    }

    private fun loadCategoryBreakdown(month: Int, year: Int) {
        val breakdown = expenseRepo.getCategoryBreakdown(userId, month, year)
        val slices = breakdown.map { (category, amount) ->
            PieChartView.Slice(category, amount, colorForCategory(category))
        }
        binding.pieChart.setData(slices)

        binding.legendContainer.removeAllViews()
        for (slice in slices) {
            val legend = ItemLegendBinding.inflate(LayoutInflater.from(this), binding.legendContainer, false)
            legend.legendDot.setBackgroundColor(slice.color)
            legend.legendLabel.text = slice.label
            legend.legendValue.text = CurrencyFormatter.format(slice.value)
            binding.legendContainer.addView(legend.root)
        }
    }

    private fun loadMonthlyBarChart() {
        val monthly = expenseRepo.getMonthlyTotals(userId, 6)
        val bars = monthly.map { (yearMonth, total) ->
            val shortLabel = yearMonth.substring(5, 7) // "MM"
            BarChartView.Bar(shortLabel, total)
        }
        binding.barChart.setData(bars)
    }

    private fun colorForCategory(category: String): Int = when (category) {
        "Food" -> Color.parseColor("#F79009")
        "Transport" -> Color.parseColor("#2E8FF0")
        "Education" -> Color.parseColor("#7A5AF8")
        "Books" -> Color.parseColor("#EE46BC")
        "Hostel/Rent" -> Color.parseColor("#F04438")
        "Utilities" -> Color.parseColor("#0BA5EC")
        "Entertainment" -> Color.parseColor("#F63D68")
        "Shopping" -> Color.parseColor("#EAAA08")
        "Health" -> Color.parseColor("#12B76A")
        else -> Color.parseColor("#98A2B3")
    }
}
