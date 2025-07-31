package com.yourdomain.walletmateeu.ui.feature_timeline

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdomain.walletmateeu.data.local.model.CategoryEntity
import com.yourdomain.walletmateeu.data.local.model.TagEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionEntity
import com.yourdomain.walletmateeu.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay


data class AddEditTransactionUiState(
    val title: String = "",
    val amount: String = "",
    val transactionType: String = "EXPENSE",
    val selectedCategory: CategoryEntity? = null,
    val date: Long = System.currentTimeMillis(),
    val isDatePickerDialogVisible: Boolean = false,
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val selectedTags: List<TagEntity> = emptyList(),
    val imageUri: Uri? = null
)

sealed class AddEditTransactionEvent {
    data class OnTitleChange(val title: String) : AddEditTransactionEvent()
    data class OnAmountChange(val amount: String) : AddEditTransactionEvent()
    data class OnTypeChange(val type: String) : AddEditTransactionEvent()
    data class OnCategorySelect(val category: CategoryEntity?) : AddEditTransactionEvent()
    object OnDateClick : AddEditTransactionEvent()
    data class OnDateSelected(val date: Long) : AddEditTransactionEvent()
    object OnDatePickerDismiss : AddEditTransactionEvent()
    data class OnTagSelected(val tag: TagEntity) : AddEditTransactionEvent()
    data class OnImagePicked(val uri: Uri?) : AddEditTransactionEvent()
    object OnRemoveImage : AddEditTransactionEvent()
    object OnSaveClick : AddEditTransactionEvent()
}

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    private val repository: AppRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState by mutableStateOf(AddEditTransactionUiState())
        private set

    private val _eventChannel = Channel<UiEvent>()
    val events = _eventChannel.receiveAsFlow()

    private var currentTransactionId: String? = null

    private var searchJob: Job? = null

    val allTags: StateFlow<List<TagEntity>> = repository.getAllTags()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
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
            repository.getTransactionWithTags(transactionId).first()?.let { transactionWithTags ->
                val transaction = transactionWithTags.transaction
                val category = categories.first().find { it.id == transaction.categoryId }
                uiState = uiState.copy(
                    title = transaction.title,
                    amount = String.format(Locale.US, "%.2f", transaction.amount),
                    transactionType = transaction.type,
                    selectedCategory = category,
                    date = transaction.date,
                    selectedTags = transactionWithTags.tags,
                    imageUri = transaction.imageUri?.let { Uri.parse(it) },
                    isEditMode = true
                )
            }
        }
    }

    fun onEvent(event: AddEditTransactionEvent) {
        when (event) {
            is AddEditTransactionEvent.OnTitleChange -> {
                uiState = uiState.copy(title = event.title)
                // --- 스마트 카테고리 추천 로직 ---
                searchJob?.cancel() // 이전 검색 작업 취소
                searchJob = viewModelScope.launch {
                    delay(500L) // 0.5초 디바운싱
                    if (event.title.isNotBlank()) {
                        val categoryId = repository.getCategoryIdForTitle(event.title)
                        if (categoryId != null) {
                            val category = categories.value.find { it.id == categoryId }
                            if (category != null && category.type == uiState.transactionType) {
                                uiState = uiState.copy(selectedCategory = category)
                            }
                        }
                    }
                }
            }
            is AddEditTransactionEvent.OnAmountChange -> {
                if (event.amount.isEmpty() || event.amount.matches(Regex("^\\d*(\\.\\d{0,2})?\$"))) {
                    uiState = uiState.copy(amount = event.amount)
                }
            }
            is AddEditTransactionEvent.OnTypeChange -> {
                uiState = uiState.copy(transactionType = event.type, selectedCategory = null)
            }
            is AddEditTransactionEvent.OnCategorySelect -> uiState = uiState.copy(selectedCategory = event.category)
            is AddEditTransactionEvent.OnDateClick -> uiState = uiState.copy(isDatePickerDialogVisible = true)
            is AddEditTransactionEvent.OnDateSelected -> {
                uiState = uiState.copy(date = event.date, isDatePickerDialogVisible = false)
            }
            is AddEditTransactionEvent.OnDatePickerDismiss -> uiState = uiState.copy(isDatePickerDialogVisible = false)
            is AddEditTransactionEvent.OnTagSelected -> {
                val currentTags = uiState.selectedTags.toMutableList()
                if (currentTags.contains(event.tag)) {
                    currentTags.remove(event.tag)
                } else {
                    currentTags.add(event.tag)
                }
                uiState = uiState.copy(selectedTags = currentTags)
            }
            // --- 이 부분이 추가되었습니다 ---
            is AddEditTransactionEvent.OnImagePicked -> {
                uiState = uiState.copy(imageUri = event.uri)
            }
            is AddEditTransactionEvent.OnRemoveImage -> {
                uiState = uiState.copy(imageUri = null)
            }
            // --- 여기까지 추가 ---
            is AddEditTransactionEvent.OnSaveClick -> saveTransaction()
        }
    }

    private fun saveTransaction() {
        if (uiState.isSaving || uiState.title.isBlank() || uiState.amount.isBlank()) return
        uiState = uiState.copy(isSaving = true)

        viewModelScope.launch {
            // --- 이 부분이 수정되었습니다 ---
            val amountValue = uiState.amount.toDoubleOrNull() ?: 0.0

            if (uiState.isEditMode) {
                repository.getTransactionById(currentTransactionId!!)?.let {
                    val updatedTransaction = it.copy(
                        title = uiState.title,
                        amount = amountValue,
                        type = uiState.transactionType,
                        categoryId = uiState.selectedCategory?.id,
                        date = uiState.date,
                        imageUri = uiState.imageUri?.toString(),
                        lastModified = System.currentTimeMillis()
                    )
                    repository.updateTransaction(updatedTransaction, uiState.selectedTags)
                }
            } else {
                val newTransaction = TransactionEntity(
                    id = UUID.randomUUID().toString(),
                    title = uiState.title,
                    amount = amountValue,
                    type = uiState.transactionType,
                    date = uiState.date,
                    categoryId = uiState.selectedCategory?.id,
                    imageUri = uiState.imageUri?.toString(),
                    lastModified = System.currentTimeMillis()
                )
                repository.insertTransaction(newTransaction, uiState.selectedTags)
            }
            _eventChannel.send(UiEvent.SaveSuccess)
        }
    }

    sealed class UiEvent {
        object SaveSuccess : UiEvent()
    }
}