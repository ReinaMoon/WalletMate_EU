package com.yourdomain.walletmateeu.ui.feature_timeline

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdomain.walletmateeu.data.local.model.CategoryEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionEntity
import com.yourdomain.walletmateeu.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

// --- UI 상태를 정의하는 데이터 클래스 ---
data class AddTransactionUiState(
    val title: String = "",
    val amount: String = "",
    val transactionType: String = "EXPENSE",
    val selectedCategory: CategoryEntity? = null,
    val date: Long = System.currentTimeMillis(),
    val isDatePickerDialogVisible: Boolean = false,
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false
)

// --- UI 이벤트를 정의하는 Sealed Class (이 부분이 누락되었습니다) ---
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
    private val repository: AppRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState by mutableStateOf(AddTransactionUiState())
        private set

    private val _eventChannel = Channel<UiEvent>()
    val events = _eventChannel.receiveAsFlow()

    private var currentTransactionId: String? = null

    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        savedStateHandle.get<String>("transactionId")?.let { transactionId ->
            if (transactionId.isNotBlank()) {
                currentTransactionId = transactionId
                loadTransaction(transactionId)
            }
        }
    }

    private fun loadTransaction(transactionId: String) {
        viewModelScope.launch {
            val transaction = repository.getTransactionById(transactionId)
            if (transaction != null) {
                val category = categories.first().find { it.id == transaction.categoryId }
                uiState = uiState.copy(
                    title = transaction.title,
                    amount = transaction.amount.toString(),
                    transactionType = transaction.type,
                    selectedCategory = category,
                    date = transaction.date,
                    isEditMode = true
                )
            }
        }
    }

    fun onEvent(event: AddTransactionEvent) {
        when (event) {
            is AddTransactionEvent.OnTitleChange -> uiState = uiState.copy(title = event.title)
            is AddTransactionEvent.OnAmountChange -> {
                if (event.amount.matches(Regex("^\\d*\\.?\\d*\$"))) {
                    uiState = uiState.copy(amount = event.amount)
                }
            }
            is AddTransactionEvent.OnTypeChange -> {
                uiState = uiState.copy(transactionType = event.type, selectedCategory = null)
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
        if (uiState.isSaving) return
        if (uiState.title.isBlank() || uiState.amount.isBlank()) return

        uiState = uiState.copy(isSaving = true)

        viewModelScope.launch {
            val categoryToSave = uiState.selectedCategory ?: getDefaultCategory()
            if (categoryToSave == null) {
                uiState = uiState.copy(isSaving = false)
                return@launch
            }

            if (uiState.isEditMode) {
                val transactionToUpdate = repository.getTransactionById(currentTransactionId!!)
                if (transactionToUpdate != null) {
                    val updatedTransaction = transactionToUpdate.copy(
                        title = uiState.title,
                        amount = uiState.amount.toDoubleOrNull() ?: 0.0,
                        type = uiState.transactionType,
                        categoryId = categoryToSave.id,
                        date = uiState.date,
                        lastModified = System.currentTimeMillis()
                    )
                    repository.updateTransaction(updatedTransaction)
                }
            } else {
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
            _eventChannel.send(UiEvent.SaveSuccess)
        }
    }

    private suspend fun getDefaultCategory(): CategoryEntity? {
        val defaultId = if (uiState.transactionType == "EXPENSE") "default_expense_id" else "default_income_id"
        return repository.getAllCategories().firstOrNull()?.find { it.id == defaultId }
    }

    sealed class UiEvent {
        object SaveSuccess : UiEvent()
    }
}