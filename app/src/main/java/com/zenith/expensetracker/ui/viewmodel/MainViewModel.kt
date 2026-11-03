package com.zenith.expensetracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zenith.expensetracker.data.local.AccentTheme
import com.zenith.expensetracker.data.local.AppDatabase
import com.zenith.expensetracker.data.local.AppPreferences
import com.zenith.expensetracker.data.local.ThemeMode
import com.zenith.expensetracker.data.local.UserPreferences
import com.zenith.expensetracker.data.local.dao.CategoryExpenseTotal
import com.zenith.expensetracker.data.model.*
import com.zenith.expensetracker.data.repository.ExpenseRepository
import com.zenith.expensetracker.domain.notification.AlarmScheduler
import com.zenith.expensetracker.domain.notification.NotificationHelper
import com.zenith.expensetracker.domain.report.CsvReportGenerator
import com.zenith.expensetracker.domain.report.PdfReportGenerator
import com.zenith.expensetracker.domain.report.ReportShareHelper
import com.zenith.expensetracker.ui.components.PieChartSlice
import com.zenith.expensetracker.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import androidx.compose.ui.graphics.Color

data class DashboardUiState(
    val netWorth: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val netSavings: Double = 0.0,
    val accounts: List<Account> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val categoriesMap: Map<Long, Category> = emptyMap(),
    val accountsMap: Map<Long, Account> = emptyMap(),
    val lowBalanceAccounts: List<Account> = emptyList(),
    val currency: String = "₹",
    val privacyModeEnabled: Boolean = false
)

data class ReportsUiState(
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val pieSlices: List<PieChartSlice> = emptyList(),
    val categoryTotals: List<CategoryExpenseTotal> = emptyList(),
    val isGeneratingPdf: Boolean = false,
    val lastGeneratedPdf: File? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = ExpenseRepository(database, application)
    private val appPreferences = AppPreferences(application)

    val preferences: StateFlow<UserPreferences> = appPreferences.preferences

    // Budget Month & Year Selection
    private val currentCal = Calendar.getInstance()
    private val _budgetMonth = MutableStateFlow(currentCal.get(Calendar.MONTH) + 1)
    val budgetMonth: StateFlow<Int> = _budgetMonth.asStateFlow()

    private val _budgetYear = MutableStateFlow(currentCal.get(Calendar.YEAR))
    val budgetYear: StateFlow<Int> = _budgetYear.asStateFlow()

    val allTransactions = repository.allTransactions.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allAccounts = repository.allAccounts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allCategories = repository.allCategories.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val reminderSettings = repository.reminderSettings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ReminderSetting()
    )

    // Dynamic Monthly Budgets responding to selected budget month & year
    val monthlyBudgets: StateFlow<List<Budget>> = combine(_budgetMonth, _budgetYear) { m, y ->
        Pair(m, y)
    }.flatMapLatest { (m, y) ->
        repository.getBudgetsForMonth(m, y)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _dashboardState = MutableStateFlow(DashboardUiState())
    val dashboardState: StateFlow<DashboardUiState> = _dashboardState.asStateFlow()

    private val _reportsState = MutableStateFlow(ReportsUiState())
    val reportsState: StateFlow<ReportsUiState> = _reportsState.asStateFlow()

    init {
        // Create Notification channels
        NotificationHelper.createNotificationChannels(application)

        // Observe and combine for Dashboard
        viewModelScope.launch {
            combine(
                repository.allTransactions,
                repository.allAccounts,
                repository.allCategories,
                repository.reminderSettings,
                appPreferences.preferences
            ) { txs, accounts, categories, settings, userPrefs ->
                val currency = settings?.selectedCurrency ?: "₹"
                val catMap = categories.associateBy { it.id }
                val accMap = accounts.associateBy { it.id }

                val nowCal = Calendar.getInstance()
                val startOfMonth = DateUtils.getStartOfMonth(
                    nowCal.get(Calendar.MONTH) + 1,
                    nowCal.get(Calendar.YEAR)
                )
                val endOfMonth = DateUtils.getEndOfMonth(
                    nowCal.get(Calendar.MONTH) + 1,
                    nowCal.get(Calendar.YEAR)
                )

                val monthlyTxs = txs.filter { it.timestamp in startOfMonth..endOfMonth }
                var income = 0.0
                var expense = 0.0

                for (t in monthlyTxs) {
                    when (t.type) {
                        TransactionType.INCOME -> income += t.amount
                        TransactionType.EXPENSE -> expense += t.amount
                        TransactionType.TRANSFER -> {}
                    }
                }

                val netWorth = accounts.sumOf { it.balance }
                val lowBal = accounts.filter { it.balance < it.lowBalanceThreshold }
                val recent = txs.take(6)

                DashboardUiState(
                    netWorth = netWorth,
                    monthlyIncome = income,
                    monthlyExpense = expense,
                    netSavings = income - expense,
                    accounts = accounts,
                    recentTransactions = recent,
                    categoriesMap = catMap,
                    accountsMap = accMap,
                    lowBalanceAccounts = lowBal,
                    currency = currency,
                    privacyModeEnabled = userPrefs.privacyMode
                )
            }.collect { state ->
                _dashboardState.value = state
            }
        }

        // Initialize Reports for current month
        loadReportForMonth(
            currentCal.get(Calendar.MONTH) + 1,
            currentCal.get(Calendar.YEAR)
        )
    }

    // Budget Custom Month Selection & Editing
    fun selectBudgetMonth(month: Int, year: Int) {
        _budgetMonth.value = month
        _budgetYear.value = year
    }

    fun upsertBudget(categoryId: Long, monthlyLimit: Double, month: Int, year: Int) {
        viewModelScope.launch {
            repository.upsertBudget(
                Budget(
                    categoryId = categoryId,
                    monthlyLimit = monthlyLimit,
                    month = month,
                    year = year
                )
            )
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    fun deleteBudgetById(id: Long) {
        viewModelScope.launch {
            repository.deleteBudgetById(id)
        }
    }

    // Reports Month Loading
    fun loadReportForMonth(month: Int, year: Int) {
        viewModelScope.launch {
            val start = DateUtils.getStartOfMonth(month, year)
            val end = DateUtils.getEndOfMonth(month, year)

            val txs = repository.getTransactionsInRangeSync(start, end)
            val catTotals = repository.getCategoryExpenseTotalsSync(start, end)
            val categories = allCategories.value.associateBy { it.id }

            var income = 0.0
            var expense = 0.0
            for (t in txs) {
                if (t.type == TransactionType.INCOME) income += t.amount
                if (t.type == TransactionType.EXPENSE) expense += t.amount
            }

            val slices = catTotals.mapNotNull { catTotal ->
                val category = categories[catTotal.categoryId]
                if (category != null && catTotal.totalAmount > 0) {
                    val color = try {
                        Color(android.graphics.Color.parseColor(category.colorHex))
                    } catch (_: Exception) {
                        Color.Gray
                    }
                    PieChartSlice(category.name, catTotal.totalAmount, color)
                } else null
            }

            _reportsState.value = ReportsUiState(
                selectedMonth = month,
                selectedYear = year,
                totalIncome = income,
                totalExpense = expense,
                pieSlices = slices,
                categoryTotals = catTotals
            )
        }
    }

    // Transaction Management (Add, Update, Delete)
    fun addTransaction(
        amount: Double,
        type: TransactionType,
        categoryId: Long?,
        sourceAccountId: Long,
        destinationAccountId: Long?,
        note: String,
        timestamp: Long
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                Transaction(
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    sourceAccountId = sourceAccountId,
                    destinationAccountId = destinationAccountId,
                    note = note,
                    timestamp = timestamp
                )
            )
            loadReportForMonth(_reportsState.value.selectedMonth, _reportsState.value.selectedYear)
        }
    }

    fun updateTransaction(newTransaction: Transaction, oldTransaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(newTransaction, oldTransaction)
            loadReportForMonth(_reportsState.value.selectedMonth, _reportsState.value.selectedYear)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            loadReportForMonth(_reportsState.value.selectedMonth, _reportsState.value.selectedYear)
        }
    }

    // Accounts
    fun addAccount(name: String, type: AccountType, balance: Double, lowThreshold: Double, colorHex: String) {
        viewModelScope.launch {
            repository.insertAccount(
                Account(
                    name = name,
                    type = type,
                    balance = balance,
                    currency = _dashboardState.value.currency,
                    colorHex = colorHex,
                    lowBalanceThreshold = lowThreshold
                )
            )
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    fun adjustAccountBalance(account: Account, newBalance: Double) {
        viewModelScope.launch {
            repository.updateAccount(account.copy(balance = newBalance))
        }
    }

    // Categories
    fun addCustomCategory(name: String, iconName: String, colorHex: String, type: TransactionType) {
        viewModelScope.launch {
            repository.insertCategory(
                Category(
                    name = name,
                    iconName = iconName,
                    colorHex = colorHex,
                    type = type,
                    isDefault = false
                )
            )
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // Reminder Settings
    fun updateReminderTime(hour: Int, minute: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            val current = reminderSettings.value ?: ReminderSetting()
            val updated = current.copy(
                dailyReminderEnabled = isEnabled,
                dailyReminderHour = hour,
                dailyReminderMinute = minute
            )
            repository.updateReminderSettings(updated)

            if (isEnabled) {
                AlarmScheduler.scheduleDailyReminder(getApplication(), hour, minute)
            } else {
                AlarmScheduler.cancelDailyReminder(getApplication())
            }
        }
    }

    fun updateCurrency(newCurrency: String) {
        viewModelScope.launch {
            val current = reminderSettings.value ?: ReminderSetting()
            repository.updateReminderSettings(current.copy(selectedCurrency = newCurrency))
        }
    }

    // User Preferences
    fun setThemeMode(mode: ThemeMode) = appPreferences.setThemeMode(mode)
    fun setAccentTheme(accent: AccentTheme) = appPreferences.setAccentTheme(accent)
    fun setDynamicColor(enabled: Boolean) = appPreferences.setDynamicColor(enabled)
    fun setAmoledDark(enabled: Boolean) = appPreferences.setAmoledDark(enabled)
    fun setDateFormat(format: String) = appPreferences.setDateFormat(format)
    fun setShowDecimals(show: Boolean) = appPreferences.setShowDecimals(show)
    fun setPrivacyMode(enabled: Boolean) = appPreferences.setPrivacyMode(enabled)
    fun setBudgetAlertThreshold(threshold: Int) = appPreferences.setBudgetAlertThreshold(threshold)
    fun setDefaultLowBalanceThreshold(threshold: Double) = appPreferences.setDefaultLowBalanceThreshold(threshold)

    fun resetAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.clearAllTransactionsAndBudgets()
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    // PDF and CSV Generation
    fun generateAndSharePdfReport() {
        viewModelScope.launch(Dispatchers.IO) {
            _reportsState.update { it.copy(isGeneratingPdf = true) }
            val state = _reportsState.value
            val start = DateUtils.getStartOfMonth(state.selectedMonth, state.selectedYear)
            val end = DateUtils.getEndOfMonth(state.selectedMonth, state.selectedYear)

            val txs = repository.getTransactionsInRangeSync(start, end)
            val catTotals = repository.getCategoryExpenseTotalsSync(start, end)
            val categories = allCategories.value.associateBy { it.id }
            val accounts = allAccounts.value.associateBy { it.id }

            val pdfFile = PdfReportGenerator.generateReport(
                context = getApplication(),
                startDate = start,
                endDate = end,
                transactions = txs,
                categories = categories,
                accounts = accounts,
                categoryTotals = catTotals,
                currency = _dashboardState.value.currency
            )

            withContext(Dispatchers.Main) {
                _reportsState.update { it.copy(isGeneratingPdf = false, lastGeneratedPdf = pdfFile) }
                ReportShareHelper.viewOrPrintPdf(getApplication(), pdfFile)
            }
        }
    }

    fun generateAndShareCsv() {
        viewModelScope.launch(Dispatchers.IO) {
            val txs = repository.allTransactions.first()
            val categories = allCategories.value.associateBy { it.id }
            val accounts = allAccounts.value.associateBy { it.id }

            val csvFile = CsvReportGenerator.generateCsv(
                context = getApplication(),
                transactions = txs,
                categories = categories,
                accounts = accounts,
                currency = _dashboardState.value.currency
            )

            withContext(Dispatchers.Main) {
                ReportShareHelper.shareFile(
                    getApplication(),
                    csvFile,
                    "text/csv",
                    "Export Easy Expense Transactions"
                )
            }
        }
    }

    // Test Notification Triggers for User Verification
    fun testDailyReminderNotification() {
        NotificationHelper.sendDailyExpenseReminder(getApplication())
    }

    fun testAddMoneyNotification() {
        NotificationHelper.sendLowBalanceNotification(
            context = getApplication(),
            accountName = "Cash Wallet",
            currentBalance = 150.0,
            threshold = 500.0,
            currency = _dashboardState.value.currency
        )
    }

    fun testBudgetAlertNotification() {
        NotificationHelper.sendBudgetAlertNotification(
            context = getApplication(),
            categoryName = "Food & Dining",
            spent = 4800.0,
            limit = 5000.0,
            currency = _dashboardState.value.currency
        )
    }
}
