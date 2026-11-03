package com.zenith.expensetracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zenith.expensetracker.ui.theme.ExpenseRed
import com.zenith.expensetracker.ui.theme.IncomeGreen

@Composable
fun CashFlowBarChart(
    income: Double,
    expense: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    val maxVal = maxOf(income, expense, 1.0)
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(income, expense) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, tween(600))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val barWidth = 48.dp.toPx()
            val canvasHeight = size.height
            val centerX = size.width / 2

            val incomeBarHeight = ((income / maxVal) * (canvasHeight - 20f)).toFloat() * animatedProgress.value
            val expenseBarHeight = ((expense / maxVal) * (canvasHeight - 20f)).toFloat() * animatedProgress.value

            // Income Bar (Left)
            val incomeLeft = centerX - barWidth - 16.dp.toPx()
            val incomeTop = canvasHeight - incomeBarHeight
            drawRoundRect(
                color = IncomeGreen,
                topLeft = Offset(incomeLeft, incomeTop),
                size = Size(barWidth, incomeBarHeight),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // Expense Bar (Right)
            val expenseLeft = centerX + 16.dp.toPx()
            val expenseTop = canvasHeight - expenseBarHeight
            drawRoundRect(
                color = ExpenseRed,
                topLeft = Offset(expenseLeft, expenseTop),
                size = Size(barWidth, expenseBarHeight),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // Baseline
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                start = Offset(20f, canvasHeight),
                end = Offset(size.width - 20f, canvasHeight),
                strokeWidth = 2f
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Income",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "+$currency${String.format("%.2f", income)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = IncomeGreen
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Expense",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "-$currency${String.format("%.2f", expense)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = ExpenseRed
                )
            }
        }
    }
}

