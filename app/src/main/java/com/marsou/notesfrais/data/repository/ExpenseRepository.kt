package com.marsou.notesfrais.data.repository

import com.marsou.notesfrais.data.dao.ExpenseDao
import com.marsou.notesfrais.data.model.Expense
import com.marsou.notesfrais.data.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow
import java.util.Date

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    val totalAmount: Flow<Double?> = expenseDao.getTotalAmount()

    suspend fun getExpenseById(id: Long): Expense? = expenseDao.getExpenseById(id)

    fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>> =
        expenseDao.getExpensesByCategory(category)

    fun getExpensesByDateRange(startDate: Date, endDate: Date): Flow<List<Expense>> =
        expenseDao.getExpensesByDateRange(startDate, endDate)

    fun getTotalByCategory(category: ExpenseCategory): Flow<Double?> =
        expenseDao.getTotalByCategory(category)

    fun searchExpenses(query: String): Flow<List<Expense>> =
        expenseDao.searchExpenses(query)

    suspend fun insertExpense(expense: Expense): Long = expenseDao.insertExpense(expense)

    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)

    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)

    suspend fun deleteExpenseById(id: Long) = expenseDao.deleteExpenseById(id)
}
