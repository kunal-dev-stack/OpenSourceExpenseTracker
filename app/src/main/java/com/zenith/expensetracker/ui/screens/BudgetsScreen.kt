package com.zenith.expensetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.zenith.expensetracker.data.model.Budget
import com.zenith.expensetracker.data.model.Category
import com.zenith.expensetracker.data.model.ReminderSetting
import com.zenith.expensetracker.data.model.Transaction
import com.zenith.expensetracker.data.model.TransactionType
import com.zenith.expensetracker.ui.components.EditNetWorthDialog
import com.zenith.expensetracker.ui.theme.ExpenseRed
import com.zenith.expensetracker.ui.theme.IncomeGreen
import com.zenith.expensetracker.ui.theme.WarningAmber
import com.zenith.expensetracker.utils.CategoryIconHelper
import com.zenith.expensetracker.utils.DateUtils
import java.text.DateFormatSymbols

@Composable
fun BudgetsScreen(
    budgets: List<Budget>,
    categories: List<Category>,
    transactions: List<Transaction>,
    reminderSettings: ReminderSetting,
    currency: String,
    selectedMonth: Int,
    selectedYear: Int,
    netWorth: Double = 0.0,
    accounts: List<com.zenith.expensetracker.data.model.Account> = emptyList(),
    onEditNetWorthAccount: ((com.zenith.expensetracker.data.model.Account, Double) -> Unit)? = null,
    onSaveAccountsBatch: ((List<com.zenith.expensetracker.data.model.Account>, List<com.zenith.expensetracker.data.model.Account>) -> Unit)? = null,
    onMonthChange: (month: Int, year: Int) -> Unit,
    onSaveBudget: (categoryId: Long, limit: Double, month: Int, year: Int) -> Unit,
    onDeleteBudget: (Budget) -> Unit,
    onUpdateReminderTime: (hour: Int, minute: Int, enabled: Boolean) -> Unit,
    onTestDailyReminder: () -> Unit,
    onTestAddMoneyAlert: () -> Unit,
    onTestBudgetAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditNetWorthDialog by remember { mutableStateOf(false) }
    var budgetToEdit by remember { mutableStateOf<Budget?>(null) }
    var budgetToDelete by remember { mutableStateOf<Budget?>(null) }

    val catMap = remember(categories) { categories.associateBy { it.id } }
    val monthName = DateFormatSymbols().months.getOrElse(selectedMonth - 1) { "Month" }

    // Calculate month boundary timestamps
    val startOfMonth = remember(selectedMonth, selectedYear) {
        DateUtils.getStartOfMonth(selectedMonth, selectedYear)
    }
    val endOfMonth = remember(selectedMonth, selectedYear) {
        DateUtils.getEndOfMonth(selectedMonth, selectedYear)
    }

    // Filter transactions for the selected month
    val monthExpenses = remember(transactions, startOfMonth, endOfMonth) {
        transactions.filter {
            it.timestamp in startOfMonth..endOfMonth && it.type == TransactionType.EXPENSE
        }
    }
    val totalSpentInMonth = remember(monthExpenses) {
        monthExpenses.sumOf { it.amount }
    }
    val categorySpentMap = remember(monthExpenses) {
        monthExpenses
            .filter { it.categoryId != null }
            .groupBy { it.categoryId!! }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }
    }

    // Overall budget has categoryId = 0L
    val overallBudget = remember(budgets) {
        budgets.find { it.categoryId == 0L }
    }
    val categoryBudgets = remember(budgets) {
        budgets.filter { it.categoryId != 0L }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Title and Month Navigator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Monthly Budgets",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Track and plan limits for any custom month",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Month navigator pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (selectedMonth > 1) {
                                        onMonthChange(selectedMonth - 1, selectedYear)
                                    } else {
                                        onMonthChange(12, selectedYear - 1)
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month", modifier = Modifier.size(20.dp))
                            }
                            Text(
                                text = "$monthName $selectedYear",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(
                                onClick = {
                                    if (selectedMonth < 12) {
                                        onMonthChange(selectedMonth + 1, selectedYear)
                                    } else {
                                        onMonthChange(1, selectedYear + 1)
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            // Net Worth Summary Banner with Edit Balance Trigger
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Total Net Worth",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$currency ${String.format("%.2f", netWorth)}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { showEditNetWorthDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Net Worth", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 1. Overall Monthly Budget Card
            item {
                if (overallBudget != null) {
                    val progress = if (overallBudget.monthlyLimit > 0) {
                        (totalSpentInMonth / overallBudget.monthlyLimit).toFloat()
                    } else 0f
                    val percentage = (progress * 100).toInt()
                    val remaining = overallBudget.monthlyLimit - totalSpentInMonth
                    val statusColor = when {
                        percentage >= 100 -> ExpenseRed
                        percentage >= 80 -> WarningAmber
                        else -> IncomeGreen
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                                            Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Total Monthly Spending Goal",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "$monthName $selectedYear",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { budgetToEdit = overallBudget },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Goal", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { budgetToDelete = overallBudget },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Goal", tint = ExpenseRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = statusColor,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Spent: $currency ${String.format("%.2f", totalSpentInMonth)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (remaining >= 0) "Remaining: $currency ${String.format("%.2f", remaining)}"
                                        else "Over budget: $currency ${String.format("%.2f", -remaining)}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = statusColor
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Limit: $currency ${String.format("%.2f", overallBudget.monthlyLimit)}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "$percentage% Used",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = statusColor
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSaveBudget(0L, 25000.0, selectedMonth, selectedYear)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AddChart, contentDescription = null, tint = Color.White)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Set Total Monthly Budget Goal",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Set an overall monthly spending limit for $monthName $selectedYear",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = {
                                    onSaveBudget(0L, 25000.0, selectedMonth, selectedYear)
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Set Goal", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 2. Category Budgets Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category Budgets ($monthName)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Category Budget", fontSize = 13.sp)
                    }
                }
            }

            // 3. Category Budgets List
            if (categoryBudgets.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No category budgets configured for $monthName $selectedYear.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(onClick = { showAddDialog = true }) {
                                Text("Add Budget for $monthName")
                            }
                        }
                    }
                }
            } else {
                items(categoryBudgets, key = { it.id }) { budget ->
                    val category = catMap[budget.categoryId]
                    val catName = category?.name ?: "Category #${budget.categoryId}"
                    val spent = categorySpentMap[budget.categoryId] ?: 0.0
                    val progress = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit).toFloat() else 0f
                    val percentage = (progress * 100).toInt()
                    val remaining = budget.monthlyLimit - spent

                    val statusColor = when {
                        percentage >= 100 -> ExpenseRed
                        percentage >= 75 -> WarningAmber
                        else -> IncomeGreen
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { budgetToEdit = budget },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val catColor = try {
                                        Color(android.graphics.Color.parseColor(category?.colorHex ?: "#10B981"))
                                    } catch (_: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(catColor.copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = CategoryIconHelper.getIconByName(category?.iconName ?: ""),
                                            contentDescription = null,
                                            tint = catColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = catName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = if (remaining >= 0) "Remaining: $currency ${String.format("%.2f", remaining)}"
                                            else "Over by: $currency ${String.format("%.2f", -remaining)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = statusColor
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = statusColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "$percentage%",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = statusColor
                                        )
                                    }
                                    IconButton(
                                        onClick = { budgetToEdit = budget },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { budgetToDelete = budget },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = statusColor,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Spent: $currency ${String.format("%.2f", spent)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Limit: $currency ${String.format("%.2f", budget.monthlyLimit)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Smart Notification Hub & Live Android Testing
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Daily Expense Reminder",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Alert at ${String.format("%02d:%02d", reminderSettings.dailyReminderHour, reminderSettings.dailyReminderMinute)} every day",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = reminderSettings.dailyReminderEnabled,
                                onCheckedChange = { isChecked ->
                                    onUpdateReminderTime(
                                        reminderSettings.dailyReminderHour,
                                        reminderSettings.dailyReminderMinute,
                                        isChecked
                                    )
                                }
                            )
                        }

                        HorizontalDivider()

                        Text(
                            text = "Live System Notification Testing:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onTestDailyReminder,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("🔔 Log Prompt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onTestAddMoneyAlert,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("⚠️ Low Balance", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onTestBudgetAlert,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("🚨 Budget Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Add Custom Budget Dialog (Supports any custom month & Overall Spending Goal)
    if (showAddDialog) {
        var selectedCatId by remember { mutableStateOf<Long?>(null) }
        var isOverallGoal by remember { mutableStateOf(false) }
        var limitInput by remember { mutableStateOf("") }
        var targetMonth by remember { mutableIntStateOf(selectedMonth) }
        var targetYear by remember { mutableIntStateOf(selectedYear) }

        val expenseCategories = remember(categories) {
            categories.filter { it.type == TransactionType.EXPENSE }
        }

        // Auto-select first category if not overall and none selected
        LaunchedEffect(expenseCategories) {
            if (selectedCatId == null && expenseCategories.isNotEmpty()) {
                selectedCatId = expenseCategories.first().id
            }
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
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
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Set Budget Limit",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${DateFormatSymbols().months.getOrElse(targetMonth - 1) { "" }} $targetYear",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Option Selector: Overall Goal or Category Budget
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isOverallGoal) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { isOverallGoal = false }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Category Budget",
                                fontSize = 12.sp,
                                fontWeight = if (!isOverallGoal) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isOverallGoal) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isOverallGoal) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable {
                                    isOverallGoal = true
                                    selectedCatId = 0L
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Total Monthly Goal",
                                fontSize = 12.sp,
                                fontWeight = if (isOverallGoal) FontWeight.Bold else FontWeight.Medium,
                                color = if (isOverallGoal) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (!isOverallGoal) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Choose Category",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            // Elegant Visual Grid with Icon & Tinted Backgrounds
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(expenseCategories) { cat ->
                                    val isSelected = cat.id == selectedCatId
                                    val catColor = try {
                                        Color(android.graphics.Color.parseColor(cat.colorHex))
                                    } catch (_: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { selectedCatId = cat.id }
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) catColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) catColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 10.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) catColor else catColor.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = CategoryIconHelper.getIconByName(cat.iconName),
                                                    contentDescription = cat.name,
                                                    tint = if (isSelected) Color.White else catColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Text(
                                                text = cat.name,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "This sets a ceiling for your total monthly expenditure across all categories.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Limit input field with currency prefix
                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { limitInput = it },
                        label = { Text("Budget Limit") },
                        prefix = {
                            Text(
                                text = "$currency ",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        placeholder = { Text("e.g. 5000") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Quick increment chips (Scrollable horizontally so numbers stay clean and horizontal)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf(500, 1000, 2000, 5000, 10000)) { inc ->
                            SuggestionChip(
                                onClick = {
                                    val current = limitInput.toDoubleOrNull() ?: 0.0
                                    limitInput = (current + inc).toInt().toString()
                                },
                                label = {
                                    Text(
                                        text = "+$inc",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = limitInput.toDoubleOrNull() ?: 0.0
                        val targetCatId = if (isOverallGoal) 0L else selectedCatId
                        if (targetCatId != null && limit > 0) {
                            onSaveBudget(targetCatId, limit, targetMonth, targetYear)
                            showAddDialog = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Budget")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Existing Budget Dialog
    budgetToEdit?.let { budget ->
        var editLimitString by remember(budget) { mutableStateOf(budget.monthlyLimit.toInt().toString()) }
        val categoryName = if (budget.categoryId == 0L) {
            "Total Monthly Spending Goal"
        } else {
            catMap[budget.categoryId]?.name ?: "Category"
        }

        AlertDialog(
            onDismissRequest = { budgetToEdit = null },
            title = { Text("Edit Budget: $categoryName") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Month: ${DateFormatSymbols().months.getOrElse(budget.month - 1) { "" }} ${budget.year}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = editLimitString,
                        onValueChange = { editLimitString = it },
                        label = { Text("Limit ($currency)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf(500, 1000, 2000, 5000, 10000)) { inc ->
                            SuggestionChip(
                                onClick = {
                                    val current = editLimitString.toDoubleOrNull() ?: 0.0
                                    editLimitString = (current + inc).toInt().toString()
                                },
                                label = {
                                    Text(
                                        text = "+$inc",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newLimit = editLimitString.toDoubleOrNull() ?: 0.0
                        if (newLimit > 0) {
                            onSaveBudget(budget.categoryId, newLimit, budget.month, budget.year)
                            budgetToEdit = null
                        }
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDeleteBudget(budget)
                        budgetToEdit = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
                ) {
                    Text("Delete")
                }
            }
        )
    }

    // Confirm Delete Dialog
    budgetToDelete?.let { budget ->
        AlertDialog(
            onDismissRequest = { budgetToDelete = null },
            title = { Text("Delete Budget?") },
            text = {
                Text("Are you sure you want to remove this budget limit for ${DateFormatSymbols().months.getOrElse(budget.month - 1) { "" }} ${budget.year}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteBudget(budget)
                        budgetToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { budgetToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Net Worth Dialog in BudgetsScreen (Save all accounts together & Add Secondary Bank)
    if (showEditNetWorthDialog) {
        EditNetWorthDialog(
            accounts = accounts,
            currency = currency,
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
}
