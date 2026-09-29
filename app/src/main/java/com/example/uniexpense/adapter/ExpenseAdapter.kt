package com.example.uniexpense.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.uniexpense.databinding.ItemExpenseBinding
import com.example.uniexpense.model.Expense
import com.example.uniexpense.utils.CurrencyFormatter

class ExpenseAdapter(
    private var items: List<Expense>,
    private val showActions: Boolean = true,
    private val onEdit: (Expense) -> Unit = {},
    private val onDelete: (Expense) -> Unit = {}
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    fun submitList(newItems: List<Expense>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            binding.textCategory.text = expense.category
            binding.textAmount.text = CurrencyFormatter.format(expense.amount)
            binding.textDate.text = expense.date
            if (expense.note.isNullOrBlank()) {
                binding.textNote.visibility = android.view.View.GONE
            } else {
                binding.textNote.visibility = android.view.View.VISIBLE
                binding.textNote.text = expense.note
            }
            binding.categoryDot.setBackgroundColor(colorForCategory(expense.category))

            if (showActions) {
                binding.btnEdit.visibility = android.view.View.VISIBLE
                binding.btnDelete.visibility = android.view.View.VISIBLE
                binding.btnEdit.setOnClickListener { onEdit(expense) }
                binding.btnDelete.setOnClickListener { onDelete(expense) }
            } else {
                binding.btnEdit.visibility = android.view.View.GONE
                binding.btnDelete.visibility = android.view.View.GONE
            }
        }

        private fun colorForCategory(category: String): Int {
            return when (category) {
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
    }
}
