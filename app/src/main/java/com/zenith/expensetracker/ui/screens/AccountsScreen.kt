package com.zenith.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.expensetracker.data.model.Account
import com.zenith.expensetracker.data.model.AccountType
import com.zenith.expensetracker.ui.theme.ExpenseRed
import com.zenith.expensetracker.ui.theme.IncomeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    accounts: List<Account>,
    currency: String,
    onAddAccount: (name: String, type: AccountType, balance: Double, lowThreshold: Double, colorHex: String) -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onUpdateAccount: ((Account) -> Unit)? = null,
    onDeleteAccount: ((Account) -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<Account?>(null) }
    var accountName by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf(AccountType.CASH) }
    var balanceString by remember { mutableStateOf("") }
    var thresholdString by remember { mutableStateOf("500") }

    val totalNetWorth = accounts.sumOf { it.balance }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Wallets & Accounts",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Overview"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAccountDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Account")
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

            // Net Worth Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Consolidated Net Worth",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currency ${String.format("%.2f", totalNetWorth)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${accounts.size} Active Accounts",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Button(
                                onClick = onNavigateToAddTransaction,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Transfer Funds", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "All Accounts",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(accounts, key = { it.id }) { acc ->
                val isLow = acc.balance < acc.lowBalanceThreshold
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        val icon = when (acc.type) {
                            AccountType.CASH -> Icons.Default.Payments
                            AccountType.BANK_CHECKING -> Icons.Default.AccountBalance
                            AccountType.SAVINGS -> Icons.Default.Savings
                            AccountType.CREDIT_CARD -> Icons.Default.CreditCard
                            AccountType.DIGITAL_WALLET -> Icons.Default.AccountBalanceWallet
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = acc.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = acc.type.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isLow) {
                                Text(
                                    text = "⚠️ Below alert limit ($currency${String.format("%.2f", acc.lowBalanceThreshold)})",
                                    fontSize = 10.sp,
                                    color = ExpenseRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$currency ${String.format("%.2f", acc.balance)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLow) ExpenseRed else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = { accountToEdit = acc },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Balance",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Button(
                                    onClick = onNavigateToAddTransaction,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isLow) ExpenseRed else IncomeGreen
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Add Money", fontSize = 10.sp, color = Color.White)
                                }
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

    // Edit Account Balance Dialog
    accountToEdit?.let { currentAcc ->
        var editName by remember(currentAcc) { mutableStateOf(currentAcc.name) }
        var editBalance by remember(currentAcc) { mutableStateOf(currentAcc.balance.toInt().toString()) }
        var editThreshold by remember(currentAcc) { mutableStateOf(currentAcc.lowBalanceThreshold.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { accountToEdit = null },
            title = { Text("Edit ${currentAcc.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Account Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editBalance,
                        onValueChange = { editBalance = it },
                        label = { Text("Current Balance ($currency)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editThreshold,
                        onValueChange = { editThreshold = it },
                        label = { Text("Low Balance Alert Threshold ($currency)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val bal = editBalance.toDoubleOrNull() ?: currentAcc.balance
                        val thresh = editThreshold.toDoubleOrNull() ?: currentAcc.lowBalanceThreshold
                        if (onUpdateAccount != null) {
                            onUpdateAccount(
                                currentAcc.copy(
                                    name = editName.ifBlank { currentAcc.name },
                                    balance = bal,
                                    lowBalanceThreshold = thresh
                                )
                            )
                        }
                        accountToEdit = null
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                if (onDeleteAccount != null && accounts.size > 1) {
                    TextButton(
                        onClick = {
                            onDeleteAccount(currentAcc)
                            accountToEdit = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
                    ) {
                        Text("Delete")
                    }
                } else {
                    TextButton(onClick = { accountToEdit = null }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Add Account Dialog
    if (showAddAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAddAccountDialog = false },
            title = { Text("Add New Account / Wallet") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = { Text("Account Name (e.g. HDFC Bank, Cash)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Account Type:", style = MaterialTheme.typography.labelSmall)
                    AccountType.values().forEach { type ->
                        val isSelected = accountType == type
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { accountType = type }
                            )
                            Text(type.displayName, fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = balanceString,
                        onValueChange = { balanceString = it },
                        label = { Text("Initial Balance ($currency)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = thresholdString,
                        onValueChange = { thresholdString = it },
                        label = { Text("Low Balance Alert Threshold ($currency)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val bal = balanceString.toDoubleOrNull() ?: 0.0
                        val thresh = thresholdString.toDoubleOrNull() ?: 500.0
                        if (accountName.isNotBlank()) {
                            onAddAccount(accountName, accountType, bal, thresh, "#00A86B")
                            showAddAccountDialog = false
                            accountName = ""
                            balanceString = ""
                        }
                    }
                ) {
                    Text("Add Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

