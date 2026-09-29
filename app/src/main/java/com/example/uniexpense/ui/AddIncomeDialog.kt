package com.example.uniexpense.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.uniexpense.NativeBridge
import com.example.uniexpense.R
import com.example.uniexpense.databinding.DialogAddIncomeBinding
import com.example.uniexpense.model.Income
import com.example.uniexpense.model.IncomeSources
import com.example.uniexpense.repository.IncomeRepository
import com.example.uniexpense.utils.DateUtils
import com.example.uniexpense.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar

class AddIncomeDialog : BottomSheetDialogFragment() {

    private var _binding: DialogAddIncomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogAddIncomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()

        binding.actvSource.setAdapter(ArrayAdapter(ctx, android.R.layout.simple_list_item_1, IncomeSources.ALL))
        binding.etDate.setText(DateUtils.todayIso())
        binding.etDate.setOnClickListener { showDatePicker() }

        binding.btnSave.setOnClickListener { save() }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day -> binding.etDate.setText("%04d-%02d-%02d".format(year, month + 1, day)) },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun save() {
        val ctx = requireContext()
        val source = binding.actvSource.text?.toString()?.trim().orEmpty()
        val amountText = binding.tilAmount.editText?.text?.toString()?.trim().orEmpty()
        val date = binding.etDate.text?.toString().orEmpty()
        val note = binding.tilNote.editText?.text?.toString()?.trim().orEmpty()

        if (source.isEmpty()) {
            Toast.makeText(ctx, R.string.category_required, Toast.LENGTH_SHORT).show(); return
        }
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            Toast.makeText(ctx, R.string.amount_must_be_positive, Toast.LENGTH_SHORT).show(); return
        }
        if (date.length != 10) {
            Toast.makeText(ctx, R.string.invalid_date, Toast.LENGTH_SHORT).show(); return
        }
        if (!NativeBridge.nativeAddIncomeValidate(source, amount, date)) {
            Toast.makeText(ctx, R.string.amount_must_be_positive, Toast.LENGTH_SHORT).show(); return
        }

        val userId = SessionManager.getUserId(ctx)
        val income = Income(userId = userId, source = source, amount = amount, date = date, note = note.ifEmpty { null })
        IncomeRepository(ctx).insert(income)
        Toast.makeText(ctx, R.string.income_saved, Toast.LENGTH_SHORT).show()
        // Dashboard reloads its totals in onResume, which fires automatically
        // once this bottom sheet is dismissed.
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = AddIncomeDialog()
    }
}
