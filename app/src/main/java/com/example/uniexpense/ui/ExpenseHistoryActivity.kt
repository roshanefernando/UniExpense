package com.example.uniexpense.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uniexpense.R
import com.example.uniexpense.adapter.ExpenseAdapter
import com.example.uniexpense.databinding.ActivityExpenseHistoryBinding
import com.example.uniexpense.model.Expense
import com.example.uniexpense.model.ExpenseCategories
import com.example.uniexpense.repository.ExpenseRepository
import com.example.uniexpense.utils.SessionManager

class ExpenseHistoryActivity : BaseActivity() {

    private lateinit var binding: ActivityExpenseHistoryBinding
    private lateinit var expenseRepo: ExpenseRepository
    private lateinit var adapter: ExpenseAdapter
    private var userId: Long = -1

    private var currentCategory: String = "All"
    private var currentSort: ExpenseRepository.SortOption = ExpenseRepository.SortOption.DATE_DESC
    private var currentQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = SessionManager.getUserId(this)
        expenseRepo = ExpenseRepository(this)

        adapter = ExpenseAdapter(emptyList(), showActions = true,
            onEdit = { expense ->
                startActivity(Intent(this, AddExpenseActivity::class.java).apply {
                    putExtra(AddExpenseActivity.EXTRA_EXPENSE_ID, expense.expenseId)
                })
            },
            onDelete = { expense -> confirmDelete(expense) }
        )
        binding.recyclerExpenses.layoutManager = LinearLayoutManager(this)
        binding.recyclerExpenses.adapter = adapter

        setupFilters()
        binding.btnBack.setOnClickListener { finish() }

        binding.tilSearch.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString().orEmpty()
                reload()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        reload()
    }

    private fun setupFilters() {
        val categories = listOf(getString(R.string.filter_all_categories)) + ExpenseCategories.ALL
        binding.actvCategoryFilter.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, categories))
        binding.actvCategoryFilter.setText(categories[0], false)
        binding.actvCategoryFilter.setOnItemClickListener { _, _, position, _ ->
            currentCategory = if (position == 0) "All" else categories[position]
            reload()
        }

        val sortOptions = listOf(
            getString(R.string.sort_newest), getString(R.string.sort_oldest),
            getString(R.string.sort_amount_high), getString(R.string.sort_amount_low)
        )
        binding.actvSort.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, sortOptions))
        binding.actvSort.setText(sortOptions[0], false)
        binding.actvSort.setOnItemClickListener { _, _, position, _ ->
            currentSort = when (position) {
                0 -> ExpenseRepository.SortOption.DATE_DESC
                1 -> ExpenseRepository.SortOption.DATE_ASC
                2 -> ExpenseRepository.SortOption.AMOUNT_DESC
                else -> ExpenseRepository.SortOption.AMOUNT_ASC
            }
            reload()
        }
    }

    private fun reload() {
        val list = expenseRepo.getExpenses(
            userId = userId,
            category = currentCategory,
            searchQuery = currentQuery.ifBlank { null },
            sortBy = currentSort
        )
        adapter.submitList(list)
        binding.textEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerExpenses.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun confirmDelete(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_confirm_title)
            .setMessage(R.string.delete_confirm_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                expenseRepo.delete(expense.expenseId, userId)
                reload()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}
