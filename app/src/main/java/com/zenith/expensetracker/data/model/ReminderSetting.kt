package com.zenith.expensetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_settings")
data class ReminderSetting(
    @PrimaryKey
    val id: Int = 1,
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderHour: Int = 20, // 8 PM
    val dailyReminderMinute: Int = 30, // 8:30 PM
    val addMoneyAlertEnabled: Boolean = true,
    val budgetAlertsEnabled: Boolean = true,
    val selectedCurrency: String = "₹"
)
