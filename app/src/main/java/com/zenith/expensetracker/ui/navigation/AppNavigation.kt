package com.zenith.expensetracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.zenith.expensetracker.ui.screens.*
import com.zenith.expensetracker.ui.viewmodel.MainViewModel

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    val dashboardState by viewModel.dashboardState.collectAsState()
    val reportsState by viewModel.reportsState.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allAccounts by viewModel.allAccounts.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val reminderSettings by viewModel.reminderSettings.collectAsState()
    val budgets by viewModel.monthlyBudgets.collectAsState()
    val budgetMonth by viewModel.budgetMonth.collectAsState()
    val budgetYear by viewModel.budgetYear.collectAsState()
    val preferences by viewModel.preferences.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.Dashboard,
        Screen.Transactions,
        Screen.Budgets,
        Screen.Analytics,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            val showBottomBar = currentRoute in bottomNavItems.map { it.route }
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    state = dashboardState,
                    userName = preferences.userName,
                    onUserNameChange = { newName -> viewModel.setUserName(newName) },
                    onNavigateToAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                    onNavigateToAccounts = { navController.navigate(Screen.Accounts.route) },
                    onNavigateToTransactions = { navController.navigate(Screen.Transactions.route) },
                    onNavigateToReports = { navController.navigate(Screen.Analytics.route) },
                    onTogglePrivacy = { viewModel.setPrivacyMode(!dashboardState.privacyModeEnabled) },
                    onEditNetWorthAccount = { acc, newBal -> viewModel.adjustAccountBalance(acc, newBal) }
                )
            }

            composable(Screen.Transactions.route) {
                TransactionsScreen(
                    transactions = allTransactions,
                    categories = allCategories,
                    accounts = allAccounts,
                    currency = dashboardState.currency,
                    dateFormat = preferences.dateFormat,
                    onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) }
                )
            }

            composable(Screen.AddTransaction.route) {
                AddEditTransactionScreen(
                    categories = allCategories,
                    accounts = allAccounts,
                    currency = dashboardState.currency,
                    onSaveTransaction = { amount, type, categoryId, sourceAccountId, destinationAccountId, note, timestamp ->
                        viewModel.addTransaction(
                            amount = amount,
                            type = type,
                            categoryId = categoryId,
                            sourceAccountId = sourceAccountId,
                            destinationAccountId = destinationAccountId,
                            note = note,
                            timestamp = timestamp
                        )
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Budgets.route) {
                BudgetsScreen(
                    budgets = budgets,
                    categories = allCategories,
                    transactions = allTransactions,
                    reminderSettings = reminderSettings ?: com.zenith.expensetracker.data.model.ReminderSetting(),
                    currency = dashboardState.currency,
                    selectedMonth = budgetMonth,
                    selectedYear = budgetYear,
                    netWorth = dashboardState.netWorth,
                    accounts = allAccounts,
                    onEditNetWorthAccount = { acc, newBal -> viewModel.adjustAccountBalance(acc, newBal) },
                    onMonthChange = { m, y -> viewModel.selectBudgetMonth(m, y) },
                    onSaveBudget = { catId, limit, m, y -> viewModel.upsertBudget(catId, limit, m, y) },
                    onDeleteBudget = { b -> viewModel.deleteBudget(b) },
                    onUpdateReminderTime = { h, m, en -> viewModel.updateReminderTime(h, m, en) },
                    onTestDailyReminder = { viewModel.testDailyReminderNotification() },
                    onTestAddMoneyAlert = { viewModel.testAddMoneyNotification() },
                    onTestBudgetAlert = { viewModel.testBudgetAlertNotification() }
                )
            }

            composable(Screen.Analytics.route) {
                AnalyticsReportsScreen(
                    state = reportsState,
                    categories = allCategories,
                    currency = dashboardState.currency,
                    onMonthChange = { month, year -> viewModel.loadReportForMonth(month, year) },
                    onGeneratePdfReport = { viewModel.generateAndSharePdfReport() },
                    onExportCsv = { viewModel.generateAndShareCsv() }
                )
            }

            composable(Screen.Accounts.route) {
                AccountsScreen(
                    accounts = allAccounts,
                    currency = dashboardState.currency,
                    onAddAccount = { name, type, bal, thresh, color ->
                        viewModel.addAccount(name, type, bal, thresh, color)
                    },
                    onUpdateAccount = { acc -> viewModel.updateAccount(acc) },
                    onDeleteAccount = { acc -> viewModel.deleteAccount(acc) },
                    onNavigateToAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    preferences = preferences,
                    currentCurrency = dashboardState.currency,
                    categories = allCategories,
                    onUserNameChange = { viewModel.setUserName(it) },
                    onThemeModeChange = { viewModel.setThemeMode(it) },
                    onAccentThemeChange = { viewModel.setAccentTheme(it) },
                    onAmoledDarkChange = { viewModel.setAmoledDark(it) },
                    onDynamicColorChange = { viewModel.setDynamicColor(it) },
                    onDateFormatChange = { viewModel.setDateFormat(it) },
                    onShowDecimalsChange = { viewModel.setShowDecimals(it) },
                    onPrivacyModeChange = { viewModel.setPrivacyMode(it) },
                    onCurrencyChange = { viewModel.updateCurrency(it) },
                    onBudgetThresholdChange = { viewModel.setBudgetAlertThreshold(it) },
                    onAddCategory = { name, icon, color, type ->
                        viewModel.addCustomCategory(name, icon, color, type)
                    },
                    onDeleteCategory = { viewModel.deleteCategory(it) },
                    onResetAllData = { viewModel.resetAllData { } }
                )
            }
        }
    }
}
