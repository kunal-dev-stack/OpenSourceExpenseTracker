package com.zenith.expensetracker.domain.report

import android.content.Context
import com.zenith.expensetracker.data.model.Account
import com.zenith.expensetracker.data.model.Category
import com.zenith.expensetracker.data.model.Transaction
import com.zenith.expensetracker.data.model.TransactionType
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object CsvReportGenerator {

    fun generateCsv(
        context: Context,
        transactions: List<Transaction>,
        categories: Map<Long, Category>,
        accounts: Map<Long, Account>,
        currency: String
    ): File {
        val exportDir = File(context.filesDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val fileName = "Easy_Expense_Transactions_${System.currentTimeMillis()}.csv"
        val file = File(exportDir, fileName)

        val writer = FileWriter(file)
        writer.append("# Easy Expense Tracker Ledger Export • Developed by Kunal Turkar\n")
        // CSV Header
        writer.append("ID,Date,Time,Type,Category,Account,Destination Account,Amount,Currency,Note,Tags\n")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        for (tx in transactions) {
            val date = Date(tx.timestamp)
            val dateStr = dateFormat.format(date)
            val timeStr = timeFormat.format(date)
            val catName = categories[tx.categoryId]?.name ?: if (tx.type == TransactionType.TRANSFER) "Transfer" else ""
            val accName = accounts[tx.sourceAccountId]?.name ?: "Account"
            val destAccName = tx.destinationAccountId?.let { accounts[it]?.name } ?: ""
            val safeNote = "\"${tx.note.replace("\"", "\"\"")}\""
            val safeTags = "\"${tx.tags.replace("\"", "\"\"")}\""

            writer.append("${tx.id},")
            writer.append("$dateStr,")
            writer.append("$timeStr,")
            writer.append("${tx.type.name},")
            writer.append("\"$catName\",")
            writer.append("\"$accName\",")
            writer.append("\"$destAccName\",")
            writer.append("${String.format(Locale.US, "%.2f", tx.amount)},")
            writer.append("\"$currency\",")
            writer.append("$safeNote,")
            writer.append("$safeTags\n")
        }

        writer.flush()
        writer.close()
        return file
    }
}

