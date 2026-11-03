package com.zenith.expensetracker.data.local.dao

import androidx.room.*
import com.zenith.expensetracker.data.model.Transaction
import com.zenith.expensetracker.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

data class CategoryExpenseTotal(
    val categoryId: Long,
    val totalAmount: Double
)

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getTransactionsInRange(startTime: Long, endTime: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getTransactionsInRangeSync(startTime: Long, endTime: Long): List<Transaction>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalExpenseInRange(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalIncomeInRange(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT categoryId, SUM(amount) as totalAmount FROM transactions WHERE type = 'EXPENSE' AND timestamp >= :startTime AND timestamp <= :endTime GROUP BY categoryId")
    fun getCategoryExpenseTotals(startTime: Long, endTime: Long): Flow<List<CategoryExpenseTotal>>

    @Query("SELECT categoryId, SUM(amount) as totalAmount FROM transactions WHERE type = 'EXPENSE' AND timestamp >= :startTime AND timestamp <= :endTime GROUP BY categoryId")
    suspend fun getCategoryExpenseTotalsSync(startTime: Long, endTime: Long): List<CategoryExpenseTotal>

    @Query("SELECT SUM(amount) FROM transactions WHERE categoryId = :categoryId AND type = 'EXPENSE' AND timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getExpenseForCategoryInRange(categoryId: Long, startTime: Long, endTime: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
}
