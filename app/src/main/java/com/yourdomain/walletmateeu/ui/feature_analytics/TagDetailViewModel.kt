package com.yourdomain.walletmateeu.ui.feature_analytics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdomain.walletmateeu.data.local.model.TransactionWithCategoryAndTags
import com.yourdomain.walletmateeu.data.repository.AppRepository
import com.yourdomain.walletmateeu.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.net.URLDecoder
import javax.inject.Inject

data class TagDetailUiState(
    val tagName: String = "",
    val transactions: List<TransactionWithCategoryAndTags> = emptyList(),
    val totalAmount: Double = 0.0,
    val currency: String = "EUR" // 통화 상태 추가
)

@HiltViewModel
class TagDetailViewModel @Inject constructor(
    private val repository: AppRepository,
    savedStateHandle: SavedStateHandle,
    userPreferencesRepository: UserPreferencesRepository // Repository 주입
) : ViewModel() {

    private val tagId: String = savedStateHandle.get<String>("tagId")!!
    private val tagName: String = URLDecoder.decode(savedStateHandle.get<String>("tagName")!!, "UTF-8")

    val uiState: StateFlow<TagDetailUiState> = combine(
        repository.getTransactionsForTag(tagId),
        userPreferencesRepository.currency
    ) { transactions, currency ->
        TagDetailUiState(
            tagName = tagName,
            transactions = transactions,
            totalAmount = transactions.sumOf { it.transaction.amount },
            currency = currency
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TagDetailUiState(tagName = tagName)
    )
}