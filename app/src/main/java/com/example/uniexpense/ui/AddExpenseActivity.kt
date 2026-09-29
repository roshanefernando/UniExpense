package com.example.uniexpense.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.uniexpense.NativeBridge
import com.example.uniexpense.R
import com.example.uniexpense.databinding.ActivityAddExpenseBinding
import com.example.uniexpense.model.Expense
import com.example.uniexpense.model.ExpenseCategories
import com.example.uniexpense.repository.ExpenseRepository
import com.example.uniexpense.utils.DateUtils
import com.example.uniexpense.utils.SessionManager
import java.util.Calendar

class AddExpenseActivity : BaseActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var expenseRepo: ExpenseRepository
    private var userId: Long = -1
    private var editingExpenseId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = SessionManager.getUserId(this)
        expenseRepo = ExpenseRepository(this)

        binding.actvCategory.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, ExpenseCategories.ALL)
        )
        binding.etDate.setText(DateUtils.todayIso())
        binding.etDate.setOnClickListener { showDatePicker() }
        binding.tilDate.setEndIconOnClickListener { showDatePicker() }

        editingExpenseId = intent.getLongExtra(EXTRA_EXPENSE_ID, -1)
        if (editingExpenseId != -1L) {
            binding.textTitle.setText(R.string.edit_expense_title)
            prefillForEdit()
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { save() }
    }

    private fun prefillForEdit() {
        val expense = expenseRepo.getExpenses(userId).find { it.expenseId == editingExpenseId } ?: return
        binding.actvCategory.setText(expense.category, false)
        binding.tilAmount.editText?.setText(expense.amount.toString())
        binding.etDate.setText(expense.date)
        binding.tilNote.editText?.setText(expense.note)
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        val current = binding.etDate.text?.toString().orEmpty()
        if (current.length == 10) {
            runCatching {
                val parts = current.split("-")
                cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }
        }
        DatePickerDialog(
            this,
            { _, year, month, day ->
                binding.etDate.setText("%04d-%02d-%02d".format(year, month + 1, day))
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun save() {
        val category = binding.actvCategory.text?.toString()?.trim().orEmpty()
        val amountText = binding.tilAmount.editText?.text?.toString()?.trim().orEmpty()
        val date = binding.etDate.text?.toString().orEmpty()
        val note = binding.tilNote.editText?.text?.toString()?.trim().orEmpty()

        if (category.isEmpty()) {
            toast(getString(R.string.category_required)); return
        }
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            toast(getString(R.string.amount_must_be_positive)); return
        }
        if (date.length != 10) {
            toast(getString(R.string.invalid_date)); return
        }
        if (!NativeBridge.nativeAddExpenseValidate(category, amount, date)) {
            toast(getString(R.string.amount_must_be_positive)); return
        }

        if (editingExpenseId != -1L) {
            val updated = Expense(editingExpenseId, userId, category, amount, date, note.ifEmpty { null })
            expenseRepo.update(updated)
            toast(getString(R.string.expense_updated))
        } else {
            val expense = Expense(userId = userId, category = category, amount = amount, date = date, note = note.ifEmpty { null })
            expenseRepo.insert(expense)
            toast(getString(R.string.expense_saved))
        }
        finish()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    companion object {
        const val EXTRA_EXPENSE_ID = "expense_id"
    }
}
