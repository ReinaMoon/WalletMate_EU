package com.yourdomain.walletmateeu.data.repository

import com.yourdomain.walletmateeu.data.local.dao.CategoryDao
import com.yourdomain.walletmateeu.data.local.dao.TagDao
import com.yourdomain.walletmateeu.data.local.dao.TransactionDao
import com.yourdomain.walletmateeu.data.local.model.CategoryEntity
import com.yourdomain.walletmateeu.data.local.model.TagEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionTagCrossRef
import com.yourdomain.walletmateeu.data.local.model.TransactionWithCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val tagDao: TagDao
) {

    // --- Transaction ---
    fun getAllTransactionsWithCategory(): Flow<List<TransactionWithCategory>> {
        return transactionDao.getAllTransactionsWithCategory()
    }
    suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }
    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }
    suspend fun getTransactionById(id: String): TransactionEntity? {
        return transactionDao.getTransactionById(id)
    }
    suspend fun deleteTransactionById(id: String) {
        transactionDao.deleteTransactionById(id)
    }

    // --- Category ---
    fun getAllCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories()
    }
    suspend fun insertCategory(category: CategoryEntity) {
        categoryDao.insertCategory(category)
    }
    suspend fun deleteCategoryById(categoryId: String) {
        categoryDao.deleteCategoryById(categoryId)
    }

    // --- Tag ---
    fun getAllTags(): Flow<List<TagEntity>> = tagDao.getAllTags()
    suspend fun insertTag(tag: TagEntity) = tagDao.insertTag(tag)
    suspend fun addTagToTransaction(crossRef: TransactionTagCrossRef) = tagDao.addTagToTransaction(crossRef)
    suspend fun removeTagFromTransaction(transactionId: String, tagId: String) = tagDao.removeTagFromTransaction(transactionId, tagId)

    // --- Settings ---
    suspend fun clearAllData() {
        transactionDao.deleteAllTransactions()
        categoryDao.deleteAllCategories()
        tagDao.deleteAllTags()
        tagDao.deleteAllTagCrossRefs()
        // 앱 재시작 시 기본 카테고리가 다시 생성될 것임
    }
}