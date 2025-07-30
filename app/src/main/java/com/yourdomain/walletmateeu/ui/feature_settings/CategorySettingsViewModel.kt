package com.yourdomain.walletmateeu.ui.feature_settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdomain.walletmateeu.data.local.model.CategoryEntity
import com.yourdomain.walletmateeu.data.repository.AppRepository
import com.yourdomain.walletmateeu.util.DummyData // DummyData 색상은 유지
import com.yourdomain.walletmateeu.util.IconHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CategorySettingsUiState(
    val newCategoryName: String = "",
    val newCategoryType: String = "EXPENSE",
    // 기본 아이콘을 IconHelper에서 가져옵니다.
    val selectedIcon: String = "Home",
    val selectedColor: Color = DummyData.categoryColors.first(),
    val isColorPickerVisible: Boolean = false,
    val customColorHex: String = "",
    val isCustomColorValid: Boolean = true
)

@HiltViewModel
class CategorySettingsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var uiState by mutableStateOf(CategorySettingsUiState())
        private set

    fun onNewCategoryNameChange(name: String) {
        uiState = uiState.copy(newCategoryName = name)
    }

    fun onNewCategoryTypeChange(type: String) {
        uiState = uiState.copy(newCategoryType = type)
    }

    // 아이콘 피커에서 선택된 아이콘 이름을 받아 UI 상태를 업데이트합니다.
    fun onIconSelected(iconName: String) {
        uiState = uiState.copy(selectedIcon = iconName)
    }

    fun onAddCategory() {
        if (uiState.newCategoryName.isBlank()) return

        viewModelScope.launch {
            val colorString = String.format("#%08X", uiState.selectedColor.toArgb())

            val newCategory = CategoryEntity(
                id = UUID.randomUUID().toString(),
                name = uiState.newCategoryName.trim(),
                type = uiState.newCategoryType,
                icon = uiState.selectedIcon,
                color = colorString
            )
            repository.insertCategory(newCategory)
            uiState = uiState.copy(newCategoryName = "")
        }
    }

    fun onDeleteCategory(categoryId: String) {
        viewModelScope.launch {
            repository.deleteCategoryById(categoryId)
        }
    }

    fun onColorPickerClick() {
        uiState = uiState.copy(isColorPickerVisible = true)
    }

    fun onColorPickerDismiss() {
        uiState = uiState.copy(isColorPickerVisible = false)
    }

    fun onColorSelected(color: Color) {
        val hexCode = String.format("#%06X", (0xFFFFFF and color.toArgb()))
        uiState = uiState.copy(
            selectedColor = color,
            customColorHex = hexCode,
            isCustomColorValid = true,
            isColorPickerVisible = false
        )
    }

    fun onCustomColorHexChange(hex: String) {
        val filteredHex = if (hex.startsWith("#")) hex else "#$hex"
        val filteredHexClean = filteredHex.filter { it.isLetterOrDigit() || it == '#' }.take(7)

        uiState = uiState.copy(customColorHex = filteredHexClean)

        if (isValidHexColor(filteredHexClean)) {
            val color = Color(android.graphics.Color.parseColor(filteredHexClean))
            uiState = uiState.copy(
                selectedColor = color,
                isCustomColorValid = true
            )
        } else {
            uiState = uiState.copy(isCustomColorValid = false)
        }
    }

    private fun isValidHexColor(hex: String): Boolean {
        return hex.matches(Regex("^#([A-Fa-f0-9]{6})$"))
    }
}