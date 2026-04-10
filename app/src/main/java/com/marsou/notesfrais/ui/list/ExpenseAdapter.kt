package com.marsou.notesfrais.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.marsou.notesfrais.R
import com.marsou.notesfrais.data.model.Expense
import com.marsou.notesfrais.databinding.ItemExpenseBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ExpenseAdapter(
    private val onItemClick: (Expense) -> Unit,
    private val onDeleteClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(DIFF_CALLBACK) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExpenseViewHolder(
        private val binding: ItemExpenseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            binding.apply {
                tvTitle.text = expense.title
                tvAmount.text = currencyFormat.format(expense.amount)
                tvDate.text = dateFormat.format(expense.date)
                tvCategory.text = expense.category.label

                val categoryIcon = when (expense.category) {
                    com.marsou.notesfrais.data.model.ExpenseCategory.TRANSPORT -> R.drawable.ic_transport
                    com.marsou.notesfrais.data.model.ExpenseCategory.RESTAURATION -> R.drawable.ic_restaurant
                    com.marsou.notesfrais.data.model.ExpenseCategory.HEBERGEMENT -> R.drawable.ic_hotel
                    com.marsou.notesfrais.data.model.ExpenseCategory.FOURNITURES -> R.drawable.ic_supplies
                    com.marsou.notesfrais.data.model.ExpenseCategory.COMMUNICATION -> R.drawable.ic_communication
                    com.marsou.notesfrais.data.model.ExpenseCategory.FORMATION -> R.drawable.ic_training
                    com.marsou.notesfrais.data.model.ExpenseCategory.DIVERS -> R.drawable.ic_misc
                }
                ivCategoryIcon.setImageResource(categoryIcon)

                root.setOnClickListener { onItemClick(expense) }
                btnDelete.setOnClickListener { onDeleteClick(expense) }

                if (expense.receiptImagePath != null) {
                    ivReceiptIndicator.setImageResource(R.drawable.ic_receipt)
                } else {
                    ivReceiptIndicator.setImageResource(0)
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Expense>() {
            override fun areItemsTheSame(oldItem: Expense, newItem: Expense) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Expense, newItem: Expense) =
                oldItem == newItem
        }
    }
}
