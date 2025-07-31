package com.yourdomain.walletmateeu.data.local.dao

import androidx.room.*
import com.yourdomain.walletmateeu.data.local.model.TitleCategoryMap
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

    @Transaction
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsWithCategoryAndTagsBetweenDates(startDate: Long, endDate: Long): Flow<List<TransactionWithCategoryAndTags>>

    // --- 이 함수를 추가하세요 ---
    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionWithCategoryAndTagsById(id: String): Flow<TransactionWithCategoryAndTags?>

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTitleCategoryMap(map: TitleCategoryMap)

    @Query("SELECT categoryId FROM title_category_map WHERE title = :title")
    suspend fun getCategoryIdForTitle(title: String): String?

    @Transaction
    @Query("""
        SELECT * FROM transactions
        WHERE id IN (
            SELECT transactionId FROM transaction_tag_cross_ref WHERE tagId = :tagId
        )
        ORDER BY date DESC
    """)
    fun getTransactionsForTag(tagId: String): Flow<List<TransactionWithCategoryAndTags>>

}