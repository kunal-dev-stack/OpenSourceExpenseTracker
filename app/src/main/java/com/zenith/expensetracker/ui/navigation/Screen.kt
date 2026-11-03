package com.zenith.expensetracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Overview", Icons.Default.Dashboard)
    object Transactions : Screen("transactions", "Ledger", Icons.AutoMirrored.Filled.ReceiptLong)
    object AddTransaction : Screen("add_transaction", "Add", Icons.Default.AddCircle)
    object Analytics : Screen("analytics", "Reports", Icons.Default.PieChart)
    object Budgets : Screen("budgets", "Budgets", Icons.Default.Savings)
    object Accounts : Screen("accounts", "Accounts", Icons.Default.AccountBalance)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}
