package com.marsou.notesfrais.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class ExpenseCategory(val label: String) {
    TRANSPORT("Transport"),
    RESTAURATION("Restauration"),
    HEBERGEMENT("Hébergement"),
    FOURNITURES("Fournitures"),
    COMMUNICATION("Communication"),
    FORMATION("Formation"),
    DIVERS("Divers")
}

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: ExpenseCategory,
    val date: Date,
    val description: String = "",
    val receiptImagePath: String? = null,
    val ocrRawText: String? = null,
    val createdAt: Date = Date()
)
