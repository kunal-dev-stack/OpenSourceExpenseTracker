package com.zenith.expensetracker.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.expensetracker.data.local.AccentTheme
import com.zenith.expensetracker.data.local.AppDatabase
import com.zenith.expensetracker.data.local.ThemeMode
import com.zenith.expensetracker.data.local.UserPreferences
import com.zenith.expensetracker.data.model.Category
import com.zenith.expensetracker.data.model.TransactionType
import com.zenith.expensetracker.domain.report.ReportShareHelper
import com.zenith.expensetracker.ui.theme.ExpenseRed
import com.zenith.expensetracker.utils.BackupRestoreHelper
import com.zenith.expensetracker.utils.CategoryIconHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    currentCurrency: String,
    categories: List<Category>,
    onUserNameChange: ((String) -> Unit)? = null,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAccentThemeChange: (AccentTheme) -> Unit,
    onAmoledDarkChange: (Boolean) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onDateFormatChange: (String) -> Unit,
    onShowDecimalsChange: (Boolean) -> Unit,
    onPrivacyModeChange: (Boolean) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onBudgetThresholdChange: (Int) -> Unit,
    onAddCategory: (name: String, iconName: String, colorHex: String, type: TransactionType) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onResetAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showNameDialog by remember { mutableStateOf(false) }
    var nameInput by remember(preferences.userName) { mutableStateOf(preferences.userName) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showDateFormatDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showManageCategoriesDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val currencies = listOf(
        "₹" to "Indian Rupee (INR)",
        "$" to "US Dollar (USD)",
        "€" to "Euro (EUR)",
        "£" to "British Pound (GBP)",
        "¥" to "Japanese Yen (JPY)",
        "C$" to "Canadian Dollar (CAD)",
        "A$" to "Australian Dollar (AUD)",
        "₩" to "South Korean Won (KRW)",
        "د.إ" to "UAE Dirham (AED)",
        "CHF" to "Swiss Franc (CHF)",
        "S$" to "Singapore Dollar (SGD)",
        "฿" to "Thai Baht (THB)",
        "R$" to "Brazilian Real (BRL)",
        "kr" to "Swedish Krona (SEK)",
        "₽" to "Russian Ruble (RUB)"
    )

    val dateFormats = listOf(
        "dd MMM yyyy" to "03 Nov 2026",
        "EEE, dd MMM yyyy" to "Tue, 03 Nov 2026",
        "dd/MM/yyyy" to "03/11/2026",
        "MM/dd/yyyy" to "11/03/2026",
        "yyyy-MM-dd" to "2026-11-03"
    )

    Scaffold { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Preferences & Settings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Personalize appearance, currencies, categories and data privacy",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // User Profile Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            nameInput = preferences.userName
                            showNameDialog = true
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (preferences.userName.isNotBlank()) preferences.userName else "Enter Your Name",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Displayed on your dashboard and personal reports",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(Icons.Default.Edit, contentDescription = "Edit Name", modifier = Modifier.size(18.dp))
                    }
                }
            }

            // 1. Theme & Appearance Section
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
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Appearance & Theme",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        // Theme Mode Selector (System, Light, Dark)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                ThemeMode.LIGHT to ("☀️ Light"),
                                ThemeMode.DARK to ("🌙 Dark"),
                                ThemeMode.SYSTEM to ("⚙️ System")
                            ).forEach { (mode, title) ->
                                val isSelected = preferences.themeMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .clickable { onThemeModeChange(mode) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // AMOLED Black switch (available when Dark or System is selected)
                        if (preferences.themeMode != ThemeMode.LIGHT) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("AMOLED Pure Black", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                    Text("True black background for OLED screens", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = preferences.amoledDark,
                                    onCheckedChange = onAmoledDarkChange
                                )
                            }
                        }

                        // Dynamic Color switch (Android 12+)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Dynamic Wallpaper Colors", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                    Text("Apply Material You palette from system wallpaper", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = preferences.dynamicColor,
                                    onCheckedChange = onDynamicColorChange
                                )
                            }
                        }

                        // Accent Theme Colors
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Accent Color Scheme",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(AccentTheme.values()) { accent ->
                                    val isSelected = preferences.accentTheme == accent
                                    val accentColor = Color(android.graphics.Color.parseColor(accent.primaryColorHex))
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onAccentThemeChange(accent) }
                                            .padding(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(accentColor)
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = accent.displayName.split(" ").first(),
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Localization & Privacy Section
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Localization & Privacy",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        // Currency Selector Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCurrencyDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
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
                                    Text(
                                        text = currentCurrency,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 18.sp
                                    )
                                }
                                Column {
                                    Text("Default Currency", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                    Text("Active: $currentCurrency (Tap to change)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }

                        HorizontalDivider()

                        // Date Format Selector Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDateFormatDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Date Format", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                Text("Format: ${preferences.dateFormat}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }

                        HorizontalDivider()

                        // Show Decimals Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Show Decimal Places (.00)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                Text("Display amounts with cents/paise precision", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = preferences.showDecimals,
                                onCheckedChange = onShowDecimalsChange
                            )
                        }

                        HorizontalDivider()

                        // Privacy Mode (Hide Balances)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Privacy Mode (Hide Numbers)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                Text("Mask balance with •••••• on dashboard", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = preferences.privacyMode,
                                onCheckedChange = onPrivacyModeChange
                            )
                        }
                    }
                }
            }

            // Budget Alerts Configuration
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
                        Text(
                            text = "Budget Alert Threshold",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Choose when you want the system to warn you that your budget is almost exhausted. Currently alert triggers at: ${preferences.budgetAlertThreshold}% of limit.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(70, 75, 80, 85, 90).forEach { thresh ->
                                val isSelected = preferences.budgetAlertThreshold == thresh
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onBudgetThresholdChange(thresh) },
                                    label = { Text("$thresh%") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Category Customization Section
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
                        Text(
                            text = "Custom Category Management",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Create custom expense and income categories with personalized icons and colors.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showAddCategoryDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Category", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { showManageCategoriesDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View All (${categories.size})", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 4. Data Backup, Restore & Reset
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
                        Text(
                            text = "Data Security & Backup",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Export an encrypted JSON database snapshot or clear data safely.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val db = AppDatabase.getInstance(context)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val file = BackupRestoreHelper.createBackup(context, db)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Backup saved: ${file.name}", Toast.LENGTH_SHORT).show()
                                            ReportShareHelper.shareFile(
                                                context,
                                                file,
                                                "application/json",
                                                "Easy Expense Tracker Backup"
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export Backup", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { showResetConfirmDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset Data", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 5. About Section (Software: Easy Expense Tracker, Developer: Kunal Turkar)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Easy Expense Tracker",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                                )
                                Text(
                                    text = "Version 1.0.0 (Production Stable)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Lead Developer",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Kunal Turkar",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        Text(
                            text = "Easy Expense Tracker is engineered as a 100% private, offline-first personal financial companion. Your financial ledger is stored exclusively on your device with native PDF statements, custom budgets, instant keypad arithmetic, and smart low-balance reminders.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AssistChip(
                                onClick = {},
                                label = { Text("🔒 100% Offline") }
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text("🚫 Zero Ads") }
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text("✨ Material 3") }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Currency Picker Dialog
    if (showCurrencyDialog) {
        var query by remember { mutableStateOf("") }
        val filteredCurrencies = remember(query) {
            currencies.filter {
                it.first.contains(query, ignoreCase = true) || it.second.contains(query, ignoreCase = true)
            }
        }

        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Choose Currency Symbol") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search currency...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    LazyColumn(modifier = Modifier.height(240.dp)) {
                        items(filteredCurrencies) { (symbol, name) ->
                            val isSelected = symbol == currentCurrency
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable {
                                        onCurrencyChange(symbol)
                                        showCurrencyDialog = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(symbol, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp))
                                Text(name, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Date Format Dialog
    if (showDateFormatDialog) {
        AlertDialog(
            onDismissRequest = { showDateFormatDialog = false },
            title = { Text("Choose Date Format") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    dateFormats.forEach { (pattern, preview) ->
                        val isSelected = pattern == preferences.dateFormat
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable {
                                    onDateFormatChange(pattern)
                                    showDateFormatDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(pattern, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Preview: $preview", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDateFormatDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Add Custom Category Dialog
    if (showAddCategoryDialog) {
        var catName by remember { mutableStateOf("") }
        var catType by remember { mutableStateOf(TransactionType.EXPENSE) }
        var selectedIcon by remember { mutableStateOf("restaurant") }
        var selectedColor by remember { mutableStateOf("#FF7043") }

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Create New Category") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("Category Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Type Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(4.dp)
                    ) {
                        FilterChip(
                            selected = catType == TransactionType.EXPENSE,
                            onClick = { catType = TransactionType.EXPENSE },
                            label = { Text("Expense") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterChip(
                            selected = catType == TransactionType.INCOME,
                            onClick = { catType = TransactionType.INCOME },
                            label = { Text("Income") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Pick Icon:", style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(CategoryIconHelper.availableIcons) { (iconKey, _) ->
                            val isSelected = iconKey == selectedIcon
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .clickable { selectedIcon = iconKey },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    CategoryIconHelper.getIconByName(iconKey),
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Text("Pick Color:", style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(CategoryIconHelper.availableColors) { hex ->
                            val isSelected = hex == selectedColor
                            val color = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (catName.isNotBlank()) {
                            onAddCategory(catName.trim(), selectedIcon, selectedColor, catType)
                            showAddCategoryDialog = false
                        }
                    }
                ) {
                    Text("Add Category")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // View All Categories Dialog
    if (showManageCategoriesDialog) {
        AlertDialog(
            onDismissRequest = { showManageCategoriesDialog = false },
            title = { Text("All Categories (${categories.size})") },
            text = {
                LazyColumn(modifier = Modifier.height(280.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val catColor = try {
                            Color(android.graphics.Color.parseColor(cat.colorHex))
                        } catch (_: Exception) {
                            MaterialTheme.colorScheme.primary
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(catColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(CategoryIconHelper.getIconByName(cat.iconName), contentDescription = null, tint = catColor, modifier = Modifier.size(18.dp))
                                }
                                Column {
                                    Text(cat.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(cat.type.name, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (!cat.isDefault) {
                                IconButton(
                                    onClick = { onDeleteCategory(cat) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showManageCategoriesDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Reset All Data Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset All Data?") },
            text = {
                Text(
                    "This action will permanently delete all logged transactions, clear account balances, and remove custom budgets. This cannot be undone!\n\nAre you sure you want to reset Easy Expense Tracker?",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllData()
                        showResetConfirmDialog = false
                        Toast.makeText(context, "All data reset successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Yes, Reset Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit User Name Dialog in Settings
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
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
                            text = "Personalize Name",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Set the display name for your dashboard",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        showNameDialog = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Name")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
