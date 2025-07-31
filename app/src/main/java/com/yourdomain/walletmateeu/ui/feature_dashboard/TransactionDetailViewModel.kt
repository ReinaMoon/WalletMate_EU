package com.yourdomain.walletmateeu.ui.feature_dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdomain.walletmateeu.data.local.model.TransactionWithCategoryAndTags
import com.yourdomain.walletmateeu.data.repository.AppRepository
import com.yourdomain.walletmateeu.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class TransactionDetailUiState(
    val transaction: TransactionWithCategoryAndTags? = null,
    val currency: String = "EUR"
)

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val repository: AppRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId: String = savedStateHandle.get<String>("transactionId")!!

    val uiState: StateFlow<TransactionDetailUiState> = combine(
        // --- 이 부분이 수정되었습니다 ---
        repository.getTransactionWithCategoryAndTagsById(transactionId), // 올바른 함수 호출
        userPreferencesRepository.currency
    ) { transaction, currency ->
        TransactionDetailUiState(
            transaction = transaction, // 강제 형변환 제거
            currency = currency
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionDetailUiState()
    )


    fun onDeleteTransaction() {
        // TODO: 삭제 로직 구현
    }
}