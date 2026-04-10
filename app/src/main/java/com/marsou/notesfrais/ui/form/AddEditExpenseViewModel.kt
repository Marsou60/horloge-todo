package com.marsou.notesfrais.ui.form

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.marsou.notesfrais.data.database.ExpenseDatabase
import com.marsou.notesfrais.data.model.Expense
import com.marsou.notesfrais.data.model.ExpenseCategory
import com.marsou.notesfrais.data.repository.ExpenseRepository
import kotlinx.coroutines.launch
import java.util.Date

class AddEditExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository

    private val _expense = MutableLiveData<Expense?>()
    val expense: LiveData<Expense?> = _expense

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    private val _ocrExtractedData = MutableLiveData<OcrExtractedData?>()
    val ocrExtractedData: LiveData<OcrExtractedData?> = _ocrExtractedData

    init {
        val dao = ExpenseDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(dao)
    }

    fun loadExpense(id: Long) {
        if (id <= 0) {
            _expense.value = null
            return
        }
        viewModelScope.launch {
            _expense.value = repository.getExpenseById(id)
        }
    }

    fun saveExpense(
        title: String,
        amountStr: String,
        category: ExpenseCategory,
        date: Date,
        description: String,
        receiptImagePath: String?,
        ocrRawText: String?
    ) {
        val amount = amountStr.replace(",", ".").toDoubleOrNull()
        if (title.isBlank() || amount == null || amount <= 0) {
            _saveResult.value = false
            return
        }

        viewModelScope.launch {
            val currentExpense = _expense.value
            if (currentExpense != null) {
                repository.updateExpense(
                    currentExpense.copy(
                        title = title,
                        amount = amount,
                        category = category,
                        date = date,
                        description = description,
                        receiptImagePath = receiptImagePath ?: currentExpense.receiptImagePath,
                        ocrRawText = ocrRawText ?: currentExpense.ocrRawText
                    )
                )
            } else {
                repository.insertExpense(
                    Expense(
                        title = title,
                        amount = amount,
                        category = category,
                        date = date,
                        description = description,
                        receiptImagePath = receiptImagePath,
                        ocrRawText = ocrRawText
                    )
                )
            }
            _saveResult.value = true
        }
    }

    fun applyOcrData(data: OcrExtractedData) {
        _ocrExtractedData.value = data
    }

    fun clearOcrData() {
        _ocrExtractedData.value = null
    }

    data class OcrExtractedData(
        val suggestedAmount: Double?,
        val suggestedTitle: String?,
        val rawText: String,
        val imagePath: String
    )
}
