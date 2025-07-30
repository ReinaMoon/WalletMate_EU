package com.yourdomain.walletmateeu.ui.feature_settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdomain.walletmateeu.util.IconHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class IconPickerViewModel : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText

    val allIcons = IconHelper.allIconNames.sorted()

    val filteredIcons = searchText
        .combine(MutableStateFlow(allIcons)) { text, icons ->
            if (text.isBlank()) {
                icons
            } else {
                icons.filter { it.contains(text, ignoreCase = true) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = allIcons
        )

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }
}