package com.zenith.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.expensetracker.data.model.Account
import com.zenith.expensetracker.data.model.Category
import com.zenith.expensetracker.data.model.TransactionType
import com.zenith.expensetracker.ui.components.KeypadInput
import com.zenith.expensetracker.ui.theme.BlueTransfer
import com.zenith.expensetracker.ui.theme.ExpenseRed
import com.zenith.expensetracker.ui.theme.IncomeGreen
import com.zenith.expensetracker.utils.CategoryIconHelper

@Composable
fun AddEditTransactionScreen(
    categories: List<Category>,
    accounts: List<Account>,
    currency: String,
    onSaveTransaction: (
        amount: Double,
        type: TransactionType,
        categoryId: Long?,
        sourceAccountId: Long,
        destinationAccountId: Long?,
        note: String,
        timestamp: Long
    ) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountString by remember { mutableStateOf("0") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedSourceAccountId by remember {
        mutableStateOf(accounts.firstOrNull()?.id ?: 1L)
    }
    var selectedDestinationAccountId by remember {
        mutableStateOf(accounts.getOrNull(1)?.id ?: 2L)
    }
    var note by remember { mutableStateOf("") }

    // Filter categories by transaction type
    val visibleCategories = remember(categories, transactionType) {
        categories.filter { it.type == transactionType }
    }

    // Auto-select first category if available
    LaunchedEffect(visibleCategories) {
        if (selectedCategoryId == null || visibleCategories.none { it.id == selectedCategoryId }) {
            selectedCategoryId = visibleCategories.firstOrNull()?.id
        }
    }

    val themeColor = when (transactionType) {
        TransactionType.EXPENSE -> ExpenseRed
        TransactionType.INCOME -> IncomeGreen
        TransactionType.TRANSFER -> BlueTransfer
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "New ${transactionType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Transaction Type Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    TransactionType.EXPENSE to "Expense",
                    TransactionType.INCOME to "Income",
                    TransactionType.TRANSFER to "Transfer"
                ).forEach { (type, label) ->
                    val isSelected = transactionType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) themeColor else Color.Transparent)
                            .clickable { transactionType = type }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 2. Amount Display Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(themeColor.copy(alpha = 0.12f))
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currency,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = amountString,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = themeColor
                    )
                }
            }

            // 3. Source Account Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (transactionType == TransactionType.TRANSFER) "From:" else "Account:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(accounts) { acc ->
                        val isSelected = acc.id == selectedSourceAccountId
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSourceAccountId = acc.id },
                            label = { Text(acc.name, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // 4. Target Account Selector (Transfer only)
            if (transactionType == TransactionType.TRANSFER) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "To Account:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(accounts.filter { it.id != selectedSourceAccountId }) { acc ->
                            val isSelected = acc.id == selectedDestinationAccountId
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDestinationAccountId = acc.id },
                                label = { Text(acc.name, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // 5. Category Horizontal Grid (Expense / Income)
            if (transactionType != TransactionType.TRANSFER) {
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(visibleCategories) { cat ->
                        val isSelected = cat.id == selectedCategoryId
                        val catColor = try {
                            Color(android.graphics.Color.parseColor(cat.colorHex))
                        } catch (_: Exception) {
                            themeColor
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) catColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedCategoryId = cat.id }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(catColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = CategoryIconHelper.getIconByName(cat.iconName),
                                    contentDescription = cat.name,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cat.name,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // 6. Note input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add note (e.g. Starbucks, Groceries, Uber)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 7. Numeric Keypad
            KeypadInput(
                amountString = amountString,
                onAmountChange = { amountString = it }
            )

            // 8. Save Button
            Button(
                onClick = {
                    val amountVal = amountString.toDoubleOrNull() ?: 0.0
                    if (amountVal > 0) {
                        onSaveTransaction(
                            amountVal,
                            transactionType,
                            if (transactionType == TransactionType.TRANSFER) null else selectedCategoryId,
                            selectedSourceAccountId,
                            if (transactionType == TransactionType.TRANSFER) selectedDestinationAccountId else null,
                            note,
                            System.currentTimeMillis()
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Save ${transactionType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
