package com.yourdomain.walletmateeu.data.local.dao

import androidx.room.*
import com.yourdomain.walletmateeu.data.local.model.TransactionEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionWithCategoryAndTags
import com.yourdomain.walletmateeu.data.local.model.TransactionWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactionsWithCategoryAndTags(): Flow<List<TransactionWithCategoryAndTags>>

    // --- 추가된 부분 시작 ---
    @Transaction
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsWithCategoryAndTagsBetweenDates(startDate: Long, endDate: Long): Flow<List<TransactionWithCategoryAndTags>>
    // --- 추가된 부분 끝 ---

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    fun getTransactionWithTags(transactionId: String): Flow<TransactionWithTags?>

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    // --- 이 함수를 추가하세요 ---
    @Transaction
    @Query("""
        SELECT * FROM transactions
        WHERE id IN (
            SELECT transactionId FROM transaction_tag_cross_ref WHERE tagId = :tagId
        )
        ORDER BY date DESC
    """)
    fun getTransactionsWithCategoryAndTagsByTagId(tagId: String): Flow<List<TransactionWithCategoryAndTags>>
}
