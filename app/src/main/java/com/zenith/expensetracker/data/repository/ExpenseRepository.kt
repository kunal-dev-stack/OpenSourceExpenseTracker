package com.zenith.expensetracker.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.zenith.expensetracker.data.local.AppDatabase
import com.zenith.expensetracker.data.local.dao.CategoryExpenseTotal
import com.zenith.expensetracker.data.model.*
import com.zenith.expensetracker.domain.notification.NotificationHelper
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ExpenseRepository(
    private val database: AppDatabase,
    private val context: Context
) {
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val accountDao = database.accountDao()
    private val budgetDao = database.budgetDao()
    private val reminderDao = database.reminderDao()

    // Transaction Streams
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val netWorth: Flow<Double?> = accountDao.getTotalNetWorth()
    val reminderSettings: Flow<ReminderSetting?> = reminderDao.getReminderSettings()

    fun getTransactionsInRange(startTime: Long, endTime: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsInRange(startTime, endTime)
    }

    suspend fun getTransactionsInRangeSync(startTime: Long, endTime: Long): List<Transaction> {
        return transactionDao.getTransactionsInRangeSync(startTime, endTime)
    }

    fun getTotalExpenseInRange(startTime: Long, endTime: Long): Flow<Double?> {
        return transactionDao.getTotalExpenseInRange(startTime, endTime)
    }

    fun getTotalIncomeInRange(startTime: Long, endTime: Long): Flow<Double?> {
        return transactionDao.getTotalIncomeInRange(startTime, endTime)
    }

    fun getCategoryExpenseTotals(startTime: Long, endTime: Long): Flow<List<CategoryExpenseTotal>> {
        return transactionDao.getCategoryExpenseTotals(startTime, endTime)
    }

    suspend fun getCategoryExpenseTotalsSync(startTime: Long, endTime: Long): List<CategoryExpenseTotal> {
        return transactionDao.getCategoryExpenseTotalsSync(startTime, endTime)
    }

    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>> {
        return budgetDao.getBudgetsForMonth(month, year)
    }

    suspend fun getBudgetsForMonthSync(month: Int, year: Int): List<Budget> {
        return budgetDao.getBudgetsForMonthSync(month, year)
    }

    // Atomic Transaction Add with Balances & Notifications
    suspend fun addTransaction(transaction: Transaction): Long {
        var newId: Long = 0
        database.withTransaction {
            newId = transactionDao.insertTransaction(transaction)

            when (transaction.type) {
                TransactionType.EXPENSE -> {
                    // Deduct from source account
                    accountDao.updateAccountBalance(transaction.sourceAccountId, -transaction.amount)

                    // Check low balance alert
                    val account = accountDao.getAccountById(transaction.sourceAccountId)
                    if (account != null && account.balance < account.lowBalanceThreshold) {
                        NotificationHelper.sendLowBalanceNotification(
                            context = context,
                            accountName = account.name,
                            currentBalance = account.balance,
                            threshold = account.lowBalanceThreshold,
                            currency = account.currency
                        )
                    }

                    // Check budget threshold alert
                    transaction.categoryId?.let { catId ->
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = transaction.timestamp
                        val month = cal.get(Calendar.MONTH) + 1
                        val year = cal.get(Calendar.YEAR)

                        val budget = budgetDao.getBudgetForCategory(catId, month, year)
                        if (budget != null) {
                            cal.set(Calendar.DAY_OF_MONTH, 1)
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            cal.set(Calendar.SECOND, 0)
                            val startOfMonth = cal.timeInMillis

                            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                            cal.set(Calendar.HOUR_OF_DAY, 23)
                            cal.set(Calendar.MINUTE, 59)
                            cal.set(Calendar.SECOND, 59)
                            val endOfMonth = cal.timeInMillis

                            val totalSpent = transactionDao.getExpenseForCategoryInRange(catId, startOfMonth, endOfMonth) ?: 0.0
                            val category = categoryDao.getCategoryById(catId)

                            if (category != null && totalSpent >= budget.monthlyLimit * 0.8) {
                                NotificationHelper.sendBudgetAlertNotification(
                                    context = context,
                                    categoryName = category.name,
                                    spent = totalSpent,
                                    limit = budget.monthlyLimit,
                                    currency = account?.currency ?: "₹"
                                )
                            }
                        }
                    }
                }
                TransactionType.INCOME -> {
                    // Credit to source account
                    accountDao.updateAccountBalance(transaction.sourceAccountId, transaction.amount)
                }
                TransactionType.TRANSFER -> {
                    // Deduct from source, add to destination
                    accountDao.updateAccountBalance(transaction.sourceAccountId, -transaction.amount)
                    transaction.destinationAccountId?.let { destId ->
                        accountDao.updateAccountBalance(destId, transaction.amount)
                    }
                }
            }
        }
        return newId
    }

    suspend fun updateTransaction(newTransaction: Transaction, oldTransaction: Transaction) {
        database.withTransaction {
            // 1. Revert old transaction's impact
            when (oldTransaction.type) {
                TransactionType.EXPENSE -> {
                    accountDao.updateAccountBalance(oldTransaction.sourceAccountId, oldTransaction.amount)
                }
                TransactionType.INCOME -> {
                    accountDao.updateAccountBalance(oldTransaction.sourceAccountId, -oldTransaction.amount)
                }
                TransactionType.TRANSFER -> {
                    accountDao.updateAccountBalance(oldTransaction.sourceAccountId, oldTransaction.amount)
                    oldTransaction.destinationAccountId?.let { destId ->
                        accountDao.updateAccountBalance(destId, -oldTransaction.amount)
                    }
                }
            }

            // 2. Apply new transaction's impact
            when (newTransaction.type) {
                TransactionType.EXPENSE -> {
                    accountDao.updateAccountBalance(newTransaction.sourceAccountId, -newTransaction.amount)
                }
                TransactionType.INCOME -> {
                    accountDao.updateAccountBalance(newTransaction.sourceAccountId, newTransaction.amount)
                }
                TransactionType.TRANSFER -> {
                    accountDao.updateAccountBalance(newTransaction.sourceAccountId, -newTransaction.amount)
                    newTransaction.destinationAccountId?.let { destId ->
                        accountDao.updateAccountBalance(destId, newTransaction.amount)
                    }
                }
            }

            // 3. Update the record
            transactionDao.updateTransaction(newTransaction)
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        database.withTransaction {
            // Revert account balances
            when (transaction.type) {
                TransactionType.EXPENSE -> {
                    accountDao.updateAccountBalance(transaction.sourceAccountId, transaction.amount)
                }
                TransactionType.INCOME -> {
                    accountDao.updateAccountBalance(transaction.sourceAccountId, -transaction.amount)
                }
                TransactionType.TRANSFER -> {
                    accountDao.updateAccountBalance(transaction.sourceAccountId, transaction.amount)
                    transaction.destinationAccountId?.let { destId ->
                        accountDao.updateAccountBalance(destId, -transaction.amount)
                    }
                }
            }
            transactionDao.deleteTransaction(transaction)
        }
    }

    suspend fun insertAccount(account: Account): Long = accountDao.insertAccount(account)
    suspend fun updateAccount(account: Account) = accountDao.updateAccount(account)
    suspend fun deleteAccount(account: Account) = accountDao.deleteAccount(account)

    suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category)
    suspend fun updateCategory(category: Category) = categoryDao.updateCategory(category)
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)

    suspend fun upsertBudget(budget: Budget): Long = budgetDao.upsertBudget(budget)
    suspend fun deleteBudget(budget: Budget) = budgetDao.deleteBudget(budget)
    suspend fun deleteBudgetById(id: Long) = budgetDao.deleteBudgetById(id)

    suspend fun updateReminderSettings(settings: ReminderSetting) = reminderDao.upsertSettings(settings)

    suspend fun clearAllTransactionsAndBudgets() {
        database.withTransaction {
            val transactions = transactionDao.getTransactionsInRangeSync(0, Long.MAX_VALUE)
            for (tx in transactions) {
                transactionDao.deleteTransaction(tx)
            }
            val accounts = accountDao.getAllAccountsSync()
            for (acc in accounts) {
                accountDao.updateAccount(acc.copy(balance = 0.0))
            }
        }
    }
}
