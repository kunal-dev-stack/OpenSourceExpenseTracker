package com.zenith.expensetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zenith.expensetracker.data.model.Account
import com.zenith.expensetracker.data.model.AccountType
import com.zenith.expensetracker.ui.theme.ExpenseRed
import com.zenith.expensetracker.ui.theme.IncomeGreen
import java.util.Locale

class EditableAccountItem(
    val id: Long = 0,
    initialName: String,
    initialType: AccountType,
    initialBalanceText: String,
    initialColorHex: String,
    val isDefault: Boolean = false,
    initialThreshold: Double = 500.0,
    val isNew: Boolean = false
) {
    var name by mutableStateOf(initialName)
    var type by mutableStateOf(initialType)
    var balanceText by mutableStateOf(initialBalanceText)
    var colorHex by mutableStateOf(initialColorHex)
    var lowBalanceThreshold by mutableDoubleStateOf(initialThreshold)
    var isEditingName by mutableStateOf(false)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNetWorthDialog(
    accounts: List<Account>,
    currency: String,
    onDismiss: () -> Unit,
    onSaveAll: (updatedAccounts: List<Account>, newAccounts: List<Account>) -> Unit
) {
    // Populate mutable editable states
    val editableAccounts = remember(accounts) {
        mutableStateListOf<EditableAccountItem>().apply {
            addAll(accounts.map { acc ->
                EditableAccountItem(
                    id = acc.id,
                    initialName = acc.name,
                    initialType = acc.type,
                    initialBalanceText = if (acc.balance % 1.0 == 0.0) {
                        acc.balance.toLong().toString()
                    } else {
                        String.format(Locale.US, "%.2f", acc.balance)
                    },
                    initialColorHex = acc.colorHex,
                    isDefault = acc.isDefault,
                    initialThreshold = acc.lowBalanceThreshold,
                    isNew = false
                )
            })
        }
    }

    // Live Net Worth dynamically calculated
    val liveNetWorth = editableAccounts.sumOf {
        it.balanceText.toDoubleOrNull() ?: 0.0
    }

    val hasSecondaryBank = editableAccounts.any {
        it.name.contains("Secondary", ignoreCase = true)
    }

    fun addAmount(item: EditableAccountItem, delta: Double) {
        val current = item.balanceText.toDoubleOrNull() ?: 0.0
        val updated = (current + delta).coerceAtLeast(0.0)
        item.balanceText = if (updated % 1.0 == 0.0) {
            updated.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", updated)
        }
    }

    fun setAmount(item: EditableAccountItem, target: Double) {
        item.balanceText = if (target % 1.0 == 0.0) {
            target.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", target)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Header with Live Net Worth Banner (Sticky at Top)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                )
                            )
                        )
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
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
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Manage Net Worth",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = "Edit Cash, Banks & Savings together",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Consolidated Net Worth live display card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.Black.copy(alpha = 0.25f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Total Consolidated Net Worth",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    )
                                    Text(
                                        text = "$currency ${String.format(Locale.US, "%,.2f", liveNetWorth)}",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = IncomeGreen.copy(alpha = 0.35f)
                                ) {
                                    Text(
                                        text = "${editableAccounts.size} Accounts",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Scrollable Account Cards & Add Secondary Bank (Middle Body)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Active Accounts & Balances",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Render each account card (Cash, Primary Bank, Savings, etc.)
                    editableAccounts.forEachIndexed { index, item ->
                        val itemColor = try {
                            Color(android.graphics.Color.parseColor(item.colorHex))
                        } catch (_: Exception) {
                            MaterialTheme.colorScheme.primary
                        }

                        val itemIcon = getAccountIcon(item.name, item.type)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(1.dp, itemColor.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Account Title, Icon, Badge & Action
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(itemColor.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = itemIcon,
                                                contentDescription = null,
                                                tint = itemColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        if (item.isEditingName) {
                                            OutlinedTextField(
                                                value = item.name,
                                                onValueChange = { item.name = it },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f),
                                                label = { Text("Account Name", fontSize = 11.sp) },
                                                trailingIcon = {
                                                    IconButton(onClick = { item.isEditingName = false }) {
                                                        Icon(
                                                            Icons.Default.Check,
                                                            contentDescription = "Save Name",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            )
                                        } else {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = item.name,
                                                        style = MaterialTheme.typography.titleMedium.copy(
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    )
                                                    IconButton(
                                                        onClick = { item.isEditingName = true },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = "Rename",
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = item.type.displayName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    // If this is a newly added account, allow removing it
                                    if (item.isNew) {
                                        IconButton(
                                            onClick = { editableAccounts.removeAt(index) },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove",
                                                tint = ExpenseRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                // Balance Input Box
                                OutlinedTextField(
                                    value = item.balanceText,
                                    onValueChange = { input ->
                                        // Allow digits and single decimal dot
                                        if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d*$"""))) {
                                            item.balanceText = input
                                        }
                                    },
                                    label = { Text("Balance for ${item.name}") },
                                    prefix = {
                                        Text(
                                            text = "$currency ",
                                            fontWeight = FontWeight.Bold,
                                            color = itemColor
                                        )
                                    },
                                    placeholder = { Text("0.00") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon = {
                                        if (item.balanceText.isNotBlank() && item.balanceText != "0") {
                                            IconButton(onClick = { setAmount(item, 0.0) }) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Zero out",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                )

                                // Quick Amount Add Chips
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    item {
                                        SuggestionChip(
                                            onClick = { setAmount(item, 0.0) },
                                            label = { Text("₹0 (Zero)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                    items(listOf(1000, 5000, 10000, 25000, 50000)) { amt ->
                                        SuggestionChip(
                                            onClick = { addAmount(item, amt.toDouble()) },
                                            label = { Text("+${currency}$amt", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Option to Add Secondary Bank / Additional Bank Account
                    if (!hasSecondaryBank) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editableAccounts.add(
                                        EditableAccountItem(
                                            id = 0,
                                            initialName = "Secondary Bank",
                                            initialType = AccountType.BANK_CHECKING,
                                            initialBalanceText = "0",
                                            initialColorHex = "#00BCD4",
                                            isDefault = false,
                                            initialThreshold = 1000.0,
                                            isNew = true
                                        )
                                    )
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
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
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddCard,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "+ Add Secondary Bank",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Text(
                                        text = "Add a 2nd bank (HDFC, ICICI, SBI) to your Net Worth",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                FilledTonalButton(
                                    onClick = {
                                        editableAccounts.add(
                                            EditableAccountItem(
                                                id = 0,
                                                initialName = "Secondary Bank",
                                                initialType = AccountType.BANK_CHECKING,
                                                initialBalanceText = "0",
                                                initialColorHex = "#00BCD4",
                                                isDefault = false,
                                                initialThreshold = 1000.0,
                                                isNew = true
                                            )
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Add", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        // Button to add another custom bank or wallet
                        OutlinedButton(
                            onClick = {
                                val nextNum = editableAccounts.size + 1
                                editableAccounts.add(
                                    EditableAccountItem(
                                        id = 0,
                                        initialName = "Bank Account #$nextNum",
                                        initialType = AccountType.BANK_CHECKING,
                                        initialBalanceText = "0",
                                        initialColorHex = "#2196F3",
                                        isDefault = false,
                                        initialThreshold = 1000.0,
                                        isNew = true
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Another Bank / Wallet", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 4. Sticky Bottom Action Buttons
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 6.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val updatedList = mutableListOf<Account>()
                                val newList = mutableListOf<Account>()

                                for (item in editableAccounts) {
                                    val bal = item.balanceText.toDoubleOrNull() ?: 0.0
                                    if (item.isNew) {
                                        newList.add(
                                            Account(
                                                name = item.name.ifBlank { "Bank Account" },
                                                type = item.type,
                                                balance = bal,
                                                currency = currency,
                                                colorHex = item.colorHex,
                                                isDefault = item.isDefault,
                                                lowBalanceThreshold = item.lowBalanceThreshold
                                            )
                                        )
                                    } else {
                                        val original = accounts.find { it.id == item.id }
                                        if (original != null) {
                                            updatedList.add(
                                                original.copy(
                                                    name = item.name.ifBlank { original.name },
                                                    type = item.type,
                                                    balance = bal,
                                                    colorHex = item.colorHex,
                                                    lowBalanceThreshold = item.lowBalanceThreshold
                                                )
                                            )
                                        }
                                    }
                                }

                                onSaveAll(updatedList, newList)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1.8f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save All Balances",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getAccountIcon(name: String, type: AccountType): ImageVector {
    val lower = name.lowercase(Locale.ROOT)
    return when {
        lower.contains("cash") || type == AccountType.CASH -> Icons.Default.Payments
        lower.contains("saving") || type == AccountType.SAVINGS -> Icons.Default.Savings
        lower.contains("credit") || type == AccountType.CREDIT_CARD -> Icons.Default.CreditCard
        lower.contains("wallet") || type == AccountType.DIGITAL_WALLET -> Icons.Default.AccountBalanceWallet
        else -> Icons.Default.AccountBalance
    }
}
