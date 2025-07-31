package com.yourdomain.walletmateeu.ui.feature_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdomain.walletmateeu.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.yourdomain.walletmateeu.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch




@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: AppRepository,
    private val userPreferencesRepository: UserPreferencesRepository // <<--- 주입
) : ViewModel() {

    // 현재 저장된 통화를 실시간으로 관찰
    val currentCurrency: StateFlow<String> = userPreferencesRepository.currency
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "EUR"
        )

    fun onClearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    // 사용자가 새로운 통화를 선택했을 때 호출되는 함수
    fun onCurrencySelected(currency: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveCurrency(currency)
        }
    }
}