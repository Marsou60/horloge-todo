package com.marsou.notesfrais.data.database

import androidx.room.TypeConverter
import com.marsou.notesfrais.data.model.ExpenseCategory
import java.util.Date

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromCategory(value: String?): ExpenseCategory? {
        return value?.let { ExpenseCategory.valueOf(it) }
    }

    @TypeConverter
    fun categoryToString(category: ExpenseCategory?): String? {
        return category?.name
    }
}
