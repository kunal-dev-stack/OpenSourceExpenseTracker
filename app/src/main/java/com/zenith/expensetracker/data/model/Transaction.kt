package com.zenith.expensetracker.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["categoryId"]),
        Index(value = ["sourceAccountId"])
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long? = null,
    val sourceAccountId: Long,
    val destinationAccountId: Long? = null,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val tags: String = ""
)
