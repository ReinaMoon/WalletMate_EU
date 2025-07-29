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
    val allCategories: List<CategoryEntity> = emptyList()
)

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    init {
        // 1. 모든 트랜잭션을 구독합니다.
        val allTransactionsFlow = repository.getAllTransactionsWithCategory()
        // 2. 모든 카테고리를 구독합니다.
        val allCategoriesFlow = repository.getAllCategories()
        // 3. 현재 선택된 필터를 구독합니다.
        val selectedFilterFlow = _uiState.map { it.selectedFilter }.distinctUntilChanged()

        // 이 세 가지 Flow가 변경될 때마다 조합하여 최종 UI 상태를 만듭니다.
        viewModelScope.launch {
            combine(allTransactionsFlow, allCategoriesFlow, selectedFilterFlow) { transactions, categories, filter ->
                TimelineUiState(
                    transactions = filterTransactions(transactions, filter),
                    allCategories = categories,
                    selectedFilter = filter,
                    isEditDialogOpen = _uiState.value.isEditDialogOpen,
                    transactionToEdit = _uiState.value.transactionToEdit
                )
            }.collect { newState ->
                _uiState.value = newState
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

    // --- 삭제 함수 추가 ---
    fun onDeleteTransaction(transactionId: String) {
        viewModelScope.launch {
            repository.deleteTransactionById(transactionId)
            onDismissEditDialog() // 삭제 후 다이얼로그가 열려있었다면 닫습니다.
        }
    }
}