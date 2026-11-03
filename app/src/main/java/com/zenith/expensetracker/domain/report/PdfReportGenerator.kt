package com.zenith.expensetracker.domain.report

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.zenith.expensetracker.data.local.dao.CategoryExpenseTotal
import com.zenith.expensetracker.data.model.Account
import com.zenith.expensetracker.data.model.Category
import com.zenith.expensetracker.data.model.Transaction
import com.zenith.expensetracker.data.model.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfReportGenerator {

    fun generateReport(
        context: Context,
        startDate: Long,
        endDate: Long,
        transactions: List<Transaction>,
        categories: Map<Long, Category>,
        accounts: Map<Long, Account>,
        categoryTotals: List<CategoryExpenseTotal>,
        currency: String
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (595 x 842 pt)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#004D40")
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.parseColor("#00796B")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 9f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val incomePaint = Paint().apply {
            color = Color.parseColor("#2E7D32")
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val expensePaint = Paint().apply {
            color = Color.parseColor("#C62828")
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        val cardBgPaint = Paint().apply {
            color = Color.parseColor("#F5F5F5")
            style = Paint.Style.FILL
        }

        var y = 40f
        val startX = 35f
        val endX = 560f

        // 1. Top Header Banner
        canvas.drawText("EASY EXPENSE TRACKER", startX, y, titlePaint)
        y += 15f
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val periodText = "Statement Period: ${dateFormat.format(Date(startDate))} – ${dateFormat.format(Date(endDate))} • Developed by Kunal Turkar"
        canvas.drawText(periodText, startX, y, subtitlePaint)

        val timeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val generatedText = "Generated on: ${timeFormat.format(Date())}"
        val genTextWidth = subtitlePaint.measureText(generatedText)
        canvas.drawText(generatedText, endX - genTextWidth, y, subtitlePaint)
        y += 15f
        canvas.drawLine(startX, y, endX, y, linePaint)
        y += 20f

        // 2. Financial Summary Cards
        var totalIncome = 0.0
        var totalExpense = 0.0
        for (tx in transactions) {
            when (tx.type) {
                TransactionType.INCOME -> totalIncome += tx.amount
                TransactionType.EXPENSE -> totalExpense += tx.amount
                TransactionType.TRANSFER -> {}
            }
        }
        val netSavings = totalIncome - totalExpense
        val savingsRate = if (totalIncome > 0) ((netSavings / totalIncome) * 100).toInt() else 0

        val cardWidth = (endX - startX - 20) / 3
        val cardHeight = 55f

        // Income Card
        canvas.drawRoundRect(startX, y, startX + cardWidth, y + cardHeight, 8f, 8f, cardBgPaint)
        canvas.drawText("TOTAL INCOME", startX + 10, y + 18, subtitlePaint)
        canvas.drawText("$currency ${String.format("%.2f", totalIncome)}", startX + 10, y + 40, incomePaint)

        // Expense Card
        val expCardX = startX + cardWidth + 10
        canvas.drawRoundRect(expCardX, y, expCardX + cardWidth, y + cardHeight, 8f, 8f, cardBgPaint)
        canvas.drawText("TOTAL EXPENSE", expCardX + 10, y + 18, subtitlePaint)
        canvas.drawText("$currency ${String.format("%.2f", totalExpense)}", expCardX + 10, y + 40, expensePaint)

        // Net Savings Card
        val savCardX = expCardX + cardWidth + 10
        canvas.drawRoundRect(savCardX, y, savCardX + cardWidth, y + cardHeight, 8f, 8f, cardBgPaint)
        canvas.drawText("NET SAVINGS ($savingsRate%)", savCardX + 10, y + 18, subtitlePaint)
        val netPaint = if (netSavings >= 0) incomePaint else expensePaint
        canvas.drawText("$currency ${String.format("%.2f", netSavings)}", savCardX + 10, y + 40, netPaint)

        y += cardHeight + 25f

        // 3. Top Spending Categories Breakdown
        if (categoryTotals.isNotEmpty()) {
            canvas.drawText("Top Category Expenses", startX, y, headerPaint)
            y += 15f

            var catCount = 0
            for (catTotal in categoryTotals.sortedByDescending { it.totalAmount }) {
                if (catCount >= 4) break
                val catName = categories[catTotal.categoryId]?.name ?: "Uncategorized"
                val catPercent = if (totalExpense > 0) ((catTotal.totalAmount / totalExpense) * 100).toInt() else 0

                canvas.drawText("• $catName", startX + 10, y, bodyPaint)
                val amountStr = "$currency ${String.format("%.2f", catTotal.totalAmount)} ($catPercent%)"
                val textW = bodyPaint.measureText(amountStr)
                canvas.drawText(amountStr, endX - textW, y, bodyPaint)
                y += 14f
                catCount++
            }
            y += 10f
        }

        // 4. Itemized Transaction Ledger
        canvas.drawLine(startX, y, endX, y, linePaint)
        y += 18f
        canvas.drawText("Transaction Ledger (${transactions.size} records)", startX, y, headerPaint)
        y += 16f

        // Table Header
        val colDate = startX
        val colType = startX + 75
        val colCat = startX + 145
        val colAcc = startX + 265
        val colNote = startX + 360
        val colAmount = endX - 70

        canvas.drawText("Date", colDate, y, subtitlePaint)
        canvas.drawText("Type", colType, y, subtitlePaint)
        canvas.drawText("Category", colCat, y, subtitlePaint)
        canvas.drawText("Account", colAcc, y, subtitlePaint)
        canvas.drawText("Notes", colNote, y, subtitlePaint)
        canvas.drawText("Amount", colAmount, y, subtitlePaint)
        y += 8f
        canvas.drawLine(startX, y, endX, y, linePaint)
        y += 14f

        val itemDateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        var rowCount = 0
        for (tx in transactions) {
            if (y > 800) break // Fit on first page or limit

            val dateStr = itemDateFormat.format(Date(tx.timestamp))
            val catName = categories[tx.categoryId]?.name ?: if (tx.type == TransactionType.TRANSFER) "Transfer" else "General"
            val accName = accounts[tx.sourceAccountId]?.name ?: "Account"
            val noteStr = if (tx.note.length > 20) tx.note.take(18) + "…" else tx.note

            canvas.drawText(dateStr, colDate, y, bodyPaint)
            canvas.drawText(tx.type.name.take(4), colType, y, bodyPaint)
            canvas.drawText(catName.take(18), colCat, y, bodyPaint)
            canvas.drawText(accName.take(15), colAcc, y, bodyPaint)
            canvas.drawText(noteStr, colNote, y, bodyPaint)

            val amountStr = when (tx.type) {
                TransactionType.INCOME -> "+$currency${String.format("%.2f", tx.amount)}"
                TransactionType.EXPENSE -> "-$currency${String.format("%.2f", tx.amount)}"
                TransactionType.TRANSFER -> "$currency${String.format("%.2f", tx.amount)}"
            }
            val paint = when (tx.type) {
                TransactionType.INCOME -> incomePaint
                TransactionType.EXPENSE -> expensePaint
                TransactionType.TRANSFER -> bodyPaint
            }
            canvas.drawText(amountStr, colAmount, y, paint)

            y += 14f
            rowCount++
        }

        // Footer
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 8f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        canvas.drawText("Easy Expense Tracker • Developed by Kunal Turkar • 100% Private & Offline", startX, 825f, footerPaint)

        pdfDocument.finishPage(page)

        // Save PDF to cache dir
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val fileName = "Easy_Expense_Statement_${System.currentTimeMillis()}.pdf"
        val file = File(reportsDir, fileName)
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        fos.close()
        pdfDocument.close()

        return file
    }
}
