package com.marsou.notesfrais.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.marsou.notesfrais.R
import com.marsou.notesfrais.data.model.Expense
import com.marsou.notesfrais.databinding.FragmentExpenseListBinding
import java.text.NumberFormat
import java.util.Locale

class ExpenseListFragment : Fragment() {

    private var _binding: FragmentExpenseListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpenseListViewModel by viewModels()
    private lateinit var adapter: ExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
        setupObservers()
        setupFab()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(
            onItemClick = { expense ->
                val action = ExpenseListFragmentDirections
                    .actionExpenseListToAddEditExpense(expense.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { expense ->
                confirmDelete(expense)
            }
        )
        binding.recyclerView.apply {
            this.adapter = this@ExpenseListFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Swipe to delete
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val expense = adapter.currentList[viewHolder.adapterPosition]
                viewModel.deleteExpense(expense)
                Snackbar.make(binding.root, R.string.expense_deleted, Snackbar.LENGTH_SHORT).show()
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.recyclerView)
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.search(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupObservers() {
        viewModel.expenses.observe(viewLifecycleOwner) { expenses ->
            adapter.submitList(expenses)
            binding.tvEmptyState.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (expenses.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.totalAmount.observe(viewLifecycleOwner) { total ->
            val formatted = NumberFormat.getCurrencyInstance(Locale.FRANCE).format(total ?: 0.0)
            binding.tvTotalAmount.text = getString(R.string.total_amount, formatted)
        }
    }

    private fun setupFab() {
        binding.fabAddExpense.setOnClickListener {
            val action = ExpenseListFragmentDirections
                .actionExpenseListToAddEditExpense(-1L)
            findNavController().navigate(action)
        }
    }

    private fun confirmDelete(expense: Expense) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_confirm_title)
            .setMessage(getString(R.string.delete_confirm_message, expense.title))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteExpense(expense)
                Snackbar.make(binding.root, R.string.expense_deleted, Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
