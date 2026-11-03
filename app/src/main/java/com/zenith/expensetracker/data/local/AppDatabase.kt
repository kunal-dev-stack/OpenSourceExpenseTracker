package com.zenith.expensetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zenith.expensetracker.data.local.dao.*
import com.zenith.expensetracker.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Transaction::class,
        Category::class,
        Account::class,
        Budget::class,
        ReminderSetting::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zenith_expense_tracker.db"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed initial categories and default accounts
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabase(database)
                    }
                }
            }

            private suspend fun seedDatabase(db: AppDatabase) {
                // Default Categories for Expense
                val expenseCategories = listOf(
                    Category(name = "Food & Dining", iconName = "restaurant", colorHex = "#FF7043", type = TransactionType.EXPENSE, isDefault = true),
                    Category(name = "Groceries", iconName = "shopping_cart", colorHex = "#66BB6A", type = TransactionType.EXPENSE, isDefault = true),
                    Category(name = "Transportation", iconName = "directions_car", colorHex = "#42A5F5", type = TransactionType.EXPENSE, isDefault = true),
                    Category(name = "Shopping", iconName = "shopping_bag", colorHex = "#AB47BC", type = TransactionType.EXPENSE, isDefault = true),
                    Category(name = "Housing & Rent", iconName = "home", colorHex = "#FFA726", type = TransactionType.EXPENSE, isDefault = true),
                    Category(name = "Bills & Utilities", iconName = "receipt_long", colorHex = "#EF5350", type = TransactionType.EXPENSE, isDefault = true),
                    Category(name = "Entertainment", iconName = "movie", colorHex = "#EC407A", type = TransactionType.EXPENSE, isDefault = true),
                    Category(name = "Health & Medical", iconName = "medical_services", colorHex = "#26A69A", type = TransactionType.EXPENSE, isDefault = true),
                    Category(name = "Education", iconName = "school", colorHex = "#5C6BC0", type = TransactionType.EXPENSE, isDefault = true),
                    Category(name = "Personal Care", iconName = "spa", colorHex = "#8D6E63", type = TransactionType.EXPENSE, isDefault = true)
                )

                // Default Categories for Income
                val incomeCategories = listOf(
                    Category(name = "Salary", iconName = "payments", colorHex = "#00C853", type = TransactionType.INCOME, isDefault = true),
                    Category(name = "Freelance", iconName = "laptop_mac", colorHex = "#00B0FF", type = TransactionType.INCOME, isDefault = true),
                    Category(name = "Investments", iconName = "trending_up", colorHex = "#AA00FF", type = TransactionType.INCOME, isDefault = true),
                    Category(name = "Gift / Bonus", iconName = "card_giftcard", colorHex = "#FFD600", type = TransactionType.INCOME, isDefault = true),
                    Category(name = "Other Income", iconName = "account_balance_wallet", colorHex = "#26A69A", type = TransactionType.INCOME, isDefault = true)
                )

                db.categoryDao().insertCategories(expenseCategories + incomeCategories)

                // Default Accounts
                val accounts = listOf(
                    Account(name = "Cash", type = AccountType.CASH, balance = 2000.0, currency = "₹", colorHex = "#4CAF50", isDefault = true, lowBalanceThreshold = 500.0),
                    Account(name = "Primary Bank", type = AccountType.BANK_CHECKING, balance = 25000.0, currency = "₹", colorHex = "#2196F3", isDefault = false, lowBalanceThreshold = 2000.0),
                    Account(name = "Savings Account", type = AccountType.SAVINGS, balance = 50000.0, currency = "₹", colorHex = "#9C27B0", isDefault = false, lowBalanceThreshold = 5000.0)
                )

                db.accountDao().insertAccounts(accounts)

                // Default Settings
                db.reminderDao().upsertSettings(
                    ReminderSetting(
                        id = 1,
                        dailyReminderEnabled = true,
                        dailyReminderHour = 20,
                        dailyReminderMinute = 30,
                        addMoneyAlertEnabled = true,
                        budgetAlertsEnabled = true,
                        selectedCurrency = "₹"
                    )
                )
            }
        }
    }
}
