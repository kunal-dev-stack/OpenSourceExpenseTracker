package com.zenith.expensetracker.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun format(amount: Double, currencySymbol: String = "₹"): String {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "$currencySymbol ${formatter.format(amount)}"
    }
}

