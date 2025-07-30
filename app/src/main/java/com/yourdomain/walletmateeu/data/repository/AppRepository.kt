package com.yourdomain.walletmateeu.data.repository

import com.yourdomain.walletmateeu.data.local.dao.CategoryDao
import com.yourdomain.walletmateeu.data.local.dao.TagDao
import com.yourdomain.walletmateeu.data.local.dao.TransactionDao
import com.yourdomain.walletmateeu.data.local.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val tagDao: TagDao
) {

    fun getTransactionsForTag(tagId: String): Flow<List<TransactionWithCategoryAndTags>> {
        return transactionDao.getTransactionsWithCategoryAndTagsByTagId(tagId)
    }

    // Transaction
    fun getAllTransactionsWithCategoryAndTags(): Flow<List<TransactionWithCategoryAndTags>> {
        return transactionDao.getAllTransactionsWithCategoryAndTags()
    }

    // --- 추가된 부분 시작 ---
    fun getTransactionsWithCategoryAndTagsBetweenDates(startDate: Long, endDate: Long): Flow<List<TransactionWithCategoryAndTags>> {
        return transactionDao.getTransactionsWithCategoryAndTagsBetweenDates(startDate, endDate)
    }
    // --- 추가된 부분 끝 ---

    suspend fun insertTransaction(transaction: TransactionEntity, tags: List<TagEntity>) {
        transactionDao.insertTransaction(transaction)
        tags.forEach { tag ->
            tagDao.addTagToTransaction(TransactionTagCrossRef(transaction.id, tag.id))
        }
    }
    suspend fun updateTransaction(transaction: TransactionEntity, tags: List<TagEntity>) {
        transactionDao.updateTransaction(transaction)
        tagDao.deleteAllTagCrossRefsForTransaction(transaction.id)
        tags.forEach { tag ->
            tagDao.addTagToTransaction(TransactionTagCrossRef(transaction.id, tag.id))
        }
    }

    suspend fun getTransactionById(id: String): TransactionEntity? =
        transactionDao.getTransactionById(id)

    fun getTransactionWithTags(transactionId: String): Flow<TransactionWithTags?> =
        transactionDao.getTransactionWithTags(transactionId)

    suspend fun deleteTransactionById(id: String) =
        transactionDao.deleteTransactionById(id)

    // Category
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    suspend fun insertCategory(category: CategoryEntity) = categoryDao.insertCategory(category)
    suspend fun deleteCategoryById(categoryId: String) = categoryDao.deleteCategoryById(categoryId)

    // Tag
    fun getAllTags(): Flow<List<TagEntity>> = tagDao.getAllTags()
    suspend fun insertTag(tag: TagEntity) = tagDao.insertTag(tag)
    suspend fun updateTag(tag: TagEntity) = tagDao.updateTag(tag)

    suspend fun deleteTagById(tagId: String) {
        tagDao.deleteTagCrossRefsByTagId(tagId)
        tagDao.deleteTagById(tagId)
    }

    // Settings
    suspend fun clearAllData() {
        tagDao.deleteAllTagCrossRefs()
        transactionDao.deleteAllTransactions()
        categoryDao.deleteAllCategories()
        tagDao.deleteAllTags()
    }
}