package com.yourdomain.walletmateeu.ui.feature_timeline

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdomain.walletmateeu.data.local.model.CategoryEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionEntity
import com.yourdomain.walletmateeu.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddTransactionUiState(
    val title: String = "",
    val amount: String = "",
    val transactionType: String = "EXPENSE",
    val selectedCategory: CategoryEntity? = null,
    val date: Long = System.currentTimeMillis(),
    val isDatePickerDialogVisible: Boolean = false,
    val isEditMode: Boolean = false // <<--- 이 라인을 다시 추가하세요.
)

sealed class AddTransactionEvent {
    data class OnTitleChange(val title: String) : AddTransactionEvent()
    data class OnAmountChange(val amount: String) : AddTransactionEvent()
    data class OnTypeChange(val type: String) : AddTransactionEvent()
    data class OnCategorySelect(val category: CategoryEntity) : AddTransactionEvent()
    object OnDateClick : AddTransactionEvent()
    data class OnDateSelected(val date: Long) : AddTransactionEvent()
    object OnDatePickerDismiss : AddTransactionEvent()
    object OnSaveClick : AddTransactionEvent()
}

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    var uiState by mutableStateOf(AddTransactionUiState())
        private set

    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onEvent(event: AddTransactionEvent) {
        when (event) {
            is AddTransactionEvent.OnTitleChange -> uiState = uiState.copy(title = event.title)
            is AddTransactionEvent.OnAmountChange -> {
                if (event.amount.matches(Regex("^\\d*\\.?\\d*\$"))) {
                    uiState = uiState.copy(amount = event.amount)
                }
            }
            is AddTransactionEvent.OnTypeChange -> {
                uiState = uiState.copy(transactionType = event.type, selectedCategory = null) // 카테고리 초기화
            }
            is AddTransactionEvent.OnCategorySelect -> uiState = uiState.copy(selectedCategory = event.category)
            is AddTransactionEvent.OnDateClick -> uiState = uiState.copy(isDatePickerDialogVisible = true)
            is AddTransactionEvent.OnDateSelected -> {
                uiState = uiState.copy(date = event.date, isDatePickerDialogVisible = false)
            }
            is AddTransactionEvent.OnDatePickerDismiss -> uiState = uiState.copy(isDatePickerDialogVisible = false)
            is AddTransactionEvent.OnSaveClick -> saveTransaction()
        }
    }

    private fun saveTransaction() {
        if (uiState.title.isBlank() || uiState.amount.isBlank()) return

        viewModelScope.launch {
            val categoryToSave = uiState.selectedCategory ?: getDefaultCategory()
            val newTransaction = TransactionEntity(
                id = UUID.randomUUID().toString(),
                title = uiState.title,
                amount = uiState.amount.toDoubleOrNull() ?: 0.0,
                type = uiState.transactionType,
                date = uiState.date,
                categoryId = categoryToSave.id,
                lastModified = System.currentTimeMillis()
            )
            repository.insertTransaction(newTransaction)
        }
    }

    private suspend fun getDefaultCategory(): CategoryEntity {
        val defaultId = if (uiState.transactionType == "EXPENSE") "default_expense_id" else "default_income_id"
        return repository.getAllCategories().first().find { it.id == defaultId }!!
    }
}