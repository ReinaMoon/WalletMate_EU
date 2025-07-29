package com.yourdomain.walletmateeu.ui.feature_timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdomain.walletmateeu.data.local.model.CategoryEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionWithCategory
import com.yourdomain.walletmateeu.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimelineUiState(
    val transactions: List<TransactionWithCategory> = emptyList(),
    val transactionToEdit: TransactionWithCategory? = null,
    val isEditDialogOpen: Boolean = false,
    val selectedFilter: String = "ALL",
    val allCategories: List<CategoryEntity> = emptyList() // <<--- 카테고리 목록 추가
)

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // 거래 내역과 카테고리 목록을 모두 구독합니다.
            repository.getAllTransactionsWithCategory().combine(repository.getAllCategories()) { transactions, categories ->
                Pair(transactions, categories)
            }.collect { (transactions, categories) ->
                _uiState.update { it.copy(
                    transactions = filterTransactions(transactions, it.selectedFilter),
                    allCategories = categories
                ) }
            }
        }
    }

    private fun filterTransactions(list: List<TransactionWithCategory>, filter: String): List<TransactionWithCategory> {
        return when (filter) {
            "EXPENSE" -> list.filter { it.transaction.type == "EXPENSE" }
            "INCOME" -> list.filter { it.transaction.type == "INCOME" }
            else -> list
        }
    }

    fun onFilterChange(filter: String) {
        // 필터만 변경하고, transactions는 init 블록의 collect가 알아서 업데이트합니다.
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun onTransactionClick(transaction: TransactionWithCategory) {
        _uiState.update { it.copy(transactionToEdit = transaction, isEditDialogOpen = true) }
    }

    fun onDismissEditDialog() {
        _uiState.update { it.copy(isEditDialogOpen = false, transactionToEdit = null) }
    }

    fun onUpdateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            onDismissEditDialog()
        }
    }
}