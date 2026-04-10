package com.marsou.notesfrais.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.marsou.notesfrais.data.database.ExpenseDatabase
import com.marsou.notesfrais.data.model.Expense
import com.marsou.notesfrais.data.model.ExpenseCategory
import com.marsou.notesfrais.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ExpenseListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository

    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<ExpenseCategory?>(null)

    val expenses: LiveData<List<Expense>>
    val totalAmount: LiveData<Double?>

    init {
        val dao = ExpenseDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(dao)

        expenses = searchQuery.flatMapLatest { query ->
            if (query.isBlank()) repository.allExpenses
            else repository.searchExpenses(query)
        }.asLiveData()

        totalAmount = repository.totalAmount.asLiveData()
    }

    fun search(query: String) {
        searchQuery.value = query
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun filterByCategory(category: ExpenseCategory?) {
        selectedCategory.value = category
    }
}
