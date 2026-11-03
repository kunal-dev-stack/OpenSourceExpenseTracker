package com.zenith.expensetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: Double,
    val currency: String = "₹",
    val colorHex: String = "#00A86B",
    val isDefault: Boolean = false,
    val lowBalanceThreshold: Double = 500.0 // Triggers "Add Money" notification when balance drops below this
)
