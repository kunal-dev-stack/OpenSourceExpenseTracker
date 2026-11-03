package com.zenith.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.expensetracker.data.model.Account
import com.zenith.expensetracker.data.model.Transaction
import com.zenith.expensetracker.data.model.TransactionType
import com.zenith.expensetracker.ui.components.EditNetWorthDialog
import com.zenith.expensetracker.ui.components.StatCard
import com.zenith.expensetracker.ui.theme.ExpenseRed
import com.zenith.expensetracker.ui.theme.IncomeGreen
import com.zenith.expensetracker.ui.viewmodel.DashboardUiState
import com.zenith.expensetracker.utils.CategoryIconHelper
import com.zenith.expensetracker.utils.DateUtils

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    userName: String = "",
    onUserNameChange: ((String) -> Unit)? = null,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToReports: () -> Unit,
    onTogglePrivacy: () -> Unit,
    onEditNetWorthAccount: ((Account, Double) -> Unit)? = null,
    onSaveAccountsBatch: ((List<Account>, List<Account>) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showEditNetWorthDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var nameInput by remember(userName) { mutableStateOf(userName) }

    // Prompt user for name on start if not set yet
    LaunchedEffect(userName) {
        if (userName.isBlank()) {
            showEditNameDialog = true
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // App Top Bar: App name smaller, User name large & customizable
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                nameInput = userName
                                showEditNameDialog = true
                            }
                    ) {
                        // App Name Smaller
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Easy Expense Tracker",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // User Name Large
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (userName.isNotBlank()) "Hello, $userName 👋" else "Welcome! Tap to add name 👋",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Name",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onTogglePrivacy) {
                            Icon(
                                imageVector = if (state.privacyModeEnabled) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Privacy Mode",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onNavigateToReports) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "Reports",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 1. Total Net Worth Hero Card with Gradient
            item {
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(primaryColor, secondaryColor)
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Total Net Worth",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )
                                // Futuristic Edit Balance Quick-Trigger
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.clickable {
                                        showEditNetWorthDialog = true
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Net Worth",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Edit",
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${state.accounts.size} Accounts",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (state.privacyModeEnabled) "••••••••" else "${state.currency} ${String.format("%.2f", state.netWorth)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (state.privacyModeEnabled) "Savings: ••••••" else "Net Savings: ${state.currency} ${String.format("%.2f", state.netSavings)}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                            val rate = if (state.monthlyIncome > 0) ((state.netSavings / state.monthlyIncome) * 100).toInt() else 0
                            Text(
                                text = "Savings Rate: $rate%",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.95f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 2. Low Balance "Add Money" Alert Banner
            if (state.lowBalanceAccounts.isNotEmpty()) {
                item {
                    val lowAcc = state.lowBalanceAccounts.first()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToAccounts() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Alert",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Add Money Alert: ${lowAcc.name} is Low!",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = if (state.privacyModeEnabled) "Balance is under safety threshold" else "Balance: ${state.currency}${String.format("%.2f", lowAcc.balance)} (Under threshold ${state.currency}${String.format("%.2f", lowAcc.lowBalanceThreshold)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = onNavigateToAccounts,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Add Funds", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // 3. Monthly Income & Expense Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "This Month Income",
                        amount = if (state.privacyModeEnabled) 0.0 else state.monthlyIncome,
                        currency = state.currency,
                        icon = Icons.Default.ArrowDownward,
                        accentColor = IncomeGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "This Month Expense",
                        amount = if (state.privacyModeEnabled) 0.0 else state.monthlyExpense,
                        currency = state.currency,
                        icon = Icons.Default.ArrowUpward,
                        accentColor = ExpenseRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Accounts Horizontal Scroll
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Wallets & Accounts",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = onNavigateToAccounts) {
                        Text("Manage", fontSize = 13.sp)
                    }
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.accounts, key = { it.id }) { acc ->
                        AccountItemCard(
                            account = acc,
                            currency = state.currency,
                            privacyMode = state.privacyModeEnabled
                        )
                    }
                }
            }

            // 5. Recent Transactions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = onNavigateToTransactions) {
                        Text("See All", fontSize = 13.sp)
                    }
                }
            }

            if (state.recentTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transactions yet. Tap '+' to record your first expense!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(state.recentTransactions, key = { it.id }) { tx ->
                    val category = state.categoriesMap[tx.categoryId]
                    val catName = category?.name ?: if (tx.type == TransactionType.TRANSFER) "Transfer" else "General"
                    val accName = state.accountsMap[tx.sourceAccountId]?.name ?: "Account"
                    RecentTransactionRow(
                        transaction = tx,
                        categoryName = catName,
                        categoryIcon = category?.iconName,
                        categoryColorHex = category?.colorHex,
                        accountName = accName,
                        currency = state.currency,
                        privacyMode = state.privacyModeEnabled
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Edit Net Worth Dialog (Save all accounts together & Add Secondary Bank)
    if (showEditNetWorthDialog) {
        EditNetWorthDialog(
            accounts = state.accounts,
            currency = state.currency,
            onDismiss = { showEditNetWorthDialog = false },
            onSaveAll = { updatedAccounts, newAccounts ->
                if (onSaveAccountsBatch != null) {
                    onSaveAccountsBatch(updatedAccounts, newAccounts)
                } else if (onEditNetWorthAccount != null) {
                    updatedAccounts.forEach { acc ->
                        onEditNetWorthAccount(acc, acc.balance)
                    }
                }
                showEditNetWorthDialog = false
            }
        )
    }

    // Enter / Edit User Name Dialog (Shown on start if name is blank or when tapped)
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = {
                // If name is blank, allow dismissing but keep placeholder
                showEditNameDialog = false
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (userName.isBlank()) "Welcome!" else "Edit Name",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "What should we call you?",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter your name to personalize your Easy Expense Tracker experience:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Your Name") },
                        placeholder = { Text("e.g. Alex, Rahul, Sarah") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = nameInput.trim()
                        if (trimmed.isNotBlank() && onUserNameChange != null) {
                            onUserNameChange(trimmed)
                        }
                        showEditNameDialog = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Name")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text(if (userName.isBlank()) "Skip" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun AccountItemCard(
    account: Account,
    currency: String,
    privacyMode: Boolean,
    modifier: Modifier = Modifier
) {
    val accColor = try {
        Color(android.graphics.Color.parseColor(account.colorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .width(160.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = accColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = account.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                maxLines = 1
            )
            Text(
                text = if (privacyMode) "••••••" else "$currency ${String.format("%.2f", account.balance)}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (account.balance < account.lowBalanceThreshold) ExpenseRed else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun RecentTransactionRow(
    transaction: Transaction,
    categoryName: String,
    categoryIcon: String?,
    categoryColorHex: String?,
    accountName: String,
    currency: String,
    privacyMode: Boolean,
    modifier: Modifier = Modifier
) {
    val sign = when (transaction.type) {
        TransactionType.EXPENSE -> "-"
        TransactionType.INCOME -> "+"
        TransactionType.TRANSFER -> "⇄"
    }

    val amountColor = when (transaction.type) {
        TransactionType.EXPENSE -> ExpenseRed
        TransactionType.INCOME -> IncomeGreen
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.tertiary
    }

    val iconColor = try {
        if (categoryColorHex != null) Color(android.graphics.Color.parseColor(categoryColorHex)) else amountColor
    } catch (_: Exception) {
        amountColor
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CategoryIconHelper.getIconByName(categoryIcon ?: ""),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "$accountName • ${DateUtils.formatDate(transaction.timestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (transaction.note.isNotBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }
            }

            Text(
                text = if (privacyMode) "••••••" else "$sign $currency ${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = amountColor
            )
        }
    }
}
