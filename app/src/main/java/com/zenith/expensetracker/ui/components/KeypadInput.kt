package com.zenith.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KeypadInput(
    amountString: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1", "2", "3", "+100"),
        listOf("4", "5", "6", "+500"),
        listOf("7", "8", "9", "+1000"),
        listOf(".", "0", "CLEAR", "DEL")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    KeypadButton(
                        label = key,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when (key) {
                                "CLEAR" -> onAmountChange("0")
                                "DEL" -> {
                                    if (amountString.length > 1) {
                                        onAmountChange(amountString.dropLast(1))
                                    } else {
                                        onAmountChange("0")
                                    }
                                }
                                "+100" -> {
                                    val current = amountString.toDoubleOrNull() ?: 0.0
                                    onAmountChange((current + 100).toInt().toString())
                                }
                                "+500" -> {
                                    val current = amountString.toDoubleOrNull() ?: 0.0
                                    onAmountChange((current + 500).toInt().toString())
                                }
                                "+1000" -> {
                                    val current = amountString.toDoubleOrNull() ?: 0.0
                                    onAmountChange((current + 1000).toInt().toString())
                                }
                                "." -> {
                                    if (!amountString.contains(".")) {
                                        onAmountChange("$amountString.")
                                    }
                                }
                                else -> {
                                    if (amountString == "0") {
                                        onAmountChange(key)
                                    } else if (amountString.length < 10) {
                                        onAmountChange(amountString + key)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isPreset = label.startsWith("+")
    val isAction = label == "CLEAR" || label == "DEL"

    val containerColor = when {
        isPreset -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        isAction -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        isPreset -> MaterialTheme.colorScheme.primary
        isAction -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (label == "DEL") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Delete",
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = label,
                fontSize = if (isPreset || isAction) 13.sp else 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

