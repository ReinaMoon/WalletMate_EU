package com.yourdomain.walletmateeu.data.repository

import com.yourdomain.walletmateeu.data.local.dao.CategoryDao
import com.yourdomain.walletmateeu.data.local.dao.TransactionDao
import com.yourdomain.walletmateeu.data.local.model.CategoryEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionTagCrossRef
import com.yourdomain.walletmateeu.data.local.model.TransactionWithCategory
import com.yourdomain.walletmateeu.data.local.dao.TagDao
import com.yourdomain.walletmateeu.data.local.model.TagEntity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

import javax.inject.Inject

class AppRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val tagDao: TagDao
) {
    init {
        // 앱이 시작될 때 (Repository가 처음 생성될 때)
        // 백그라운드 스레드에서 기본 카테고리 생성 로직을 실행합니다.
        CoroutineScope(Dispatchers.IO).launch {
            ensureDefaultCategoriesExist()
        }
    }
    private suspend fun ensureDefaultCategoriesExist() {
        val categories = categoryDao.getAllCategories().first() // 현재 모든 카테고리를 한 번 가져옴
        val hasDefaultExpense = categories.any { it.name == "Uncategorized" && it.type == "EXPENSE" }
        val hasDefaultIncome = categories.any { it.name == "Uncategorized" && it.type == "INCOME" }

        if (!hasDefaultExpense) {
            categoryDao.insertCategory(
                CategoryEntity(
                    id = "default_expense_id",
                    name = "Uncategorized",
                    type = "EXPENSE",
                    icon = "ic_default", // 적절한 아이콘 이름
                    color = "#808080"  // 회색
                )
            )
        }
        if (!hasDefaultIncome) {
            categoryDao.insertCategory(
                CategoryEntity(
                    id = "default_income_id",
                    name = "Uncategorized",
                    type = "INCOME",
                    icon = "ic_default",
                    color = "#808080"
                )
            )
        }
    }
    fun getAllTransactionsWithCategory(): Flow<List<TransactionWithCategory>> {
        return transactionDao.getAllTransactionsWithCategory()
    }

    suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    fun getAllCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories()
    }

    suspend fun insertCategory(category: CategoryEntity) {
        categoryDao.insertCategory(category)
    }

    suspend fun deleteCategoryById(categoryId: String) {
        categoryDao.deleteCategoryById(categoryId)
    }
    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun getTransactionById(id: String): TransactionEntity? {
        return transactionDao.getTransactionById(id)
    }

    // --- Tag 관련 함수들 ---
    fun getAllTags(): Flow<List<TagEntity>> = tagDao.getAllTags()
    suspend fun insertTag(tag: TagEntity) = tagDao.insertTag(tag)
    suspend fun addTagToTransaction(crossRef: TransactionTagCrossRef) = tagDao.addTagToTransaction(crossRef)
    suspend fun removeTagFromTransaction(transactionId: String, tagId: String) = tagDao.removeTagFromTransaction(transactionId, tagId)

    // --- 전체 삭제 함수 ---
    suspend fun clearAllData() {
        transactionDao.deleteAllTransactions()
        categoryDao.deleteAllCategories()
        tagDao.deleteAllTags()
        tagDao.deleteAllTagCrossRefs()
        // 앱 재시작 시 기본 카테고리가 다시 생성될 것임
    }

}