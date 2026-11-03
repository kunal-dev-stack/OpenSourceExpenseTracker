package com.zenith.expensetracker.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.zenith.expensetracker.data.local.AppDatabase
import com.zenith.expensetracker.data.model.Account
import com.zenith.expensetracker.data.model.Category
import com.zenith.expensetracker.data.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter

data class BackupData(
    val exportTimestamp: Long = System.currentTimeMillis(),
    val appName: String = "Easy Expense Tracker",
    val developer: String = "Kunal Turkar",
    val version: Int = 1,
    val accounts: List<Account>,
    val categories: List<Category>,
    val transactions: List<Transaction>
)

object BackupRestoreHelper {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun createBackup(context: Context, database: AppDatabase): File = withContext(Dispatchers.IO) {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val accounts = database.accountDao().getAllAccountsSync()
        val categories = database.categoryDao().getAllCategoriesSync()
        val transactions = database.transactionDao().getTransactionsInRangeSync(0, Long.MAX_VALUE)

        val backupData = BackupData(
            accounts = accounts,
            categories = categories,
            transactions = transactions
        )

        val file = File(backupDir, "easy_expense_backup_${System.currentTimeMillis()}.json")
        val writer = FileWriter(file)
        gson.toJson(backupData, writer)
        writer.flush()
        writer.close()
        file
    }

    suspend fun restoreBackup(database: AppDatabase, backupFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val reader = FileReader(backupFile)
            val backupData = gson.fromJson(reader, BackupData::class.java)
            reader.close()

            if (backupData != null) {
                database.accountDao().insertAccounts(backupData.accounts)
                database.categoryDao().insertCategories(backupData.categories)
                for (tx in backupData.transactions) {
                    database.transactionDao().insertTransaction(tx)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
