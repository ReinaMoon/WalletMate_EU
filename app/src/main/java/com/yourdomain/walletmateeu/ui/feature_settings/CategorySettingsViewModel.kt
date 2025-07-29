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
import com.yourdomain.walletmateeu.util.DummyData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// UI의 전체 상태를 나타내는 데이터 클래스
data class CategorySettingsUiState(
    val newCategoryName: String = "",
    val newCategoryType: String = "EXPENSE", // 기본값은 지출
    val selectedIcon: String = DummyData.categoryIcons.keys.first(), // 첫 번째 아이콘을 기본값으로
    val selectedColor: Color = DummyData.categoryColors.first(), // 첫 번째 색상을 기본값으로
    val isColorPickerVisible: Boolean = false, // 다이얼로그 표시 상태
    val customColorHex: String = "", // <<--- Hex 코드 입력을 위한 상태 추가
    val isCustomColorValid: Boolean = true // <<--- Hex 코드 유효성 상태 추가
)

@HiltViewModel
class CategorySettingsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    // 실시간 카테고리 목록
    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI 상태를 하나의 객체로 관리
    var uiState by mutableStateOf(CategorySettingsUiState())
        private set

    fun onNewCategoryNameChange(name: String) {
        uiState = uiState.copy(newCategoryName = name)
    }

    fun onNewCategoryTypeChange(type: String) {
        uiState = uiState.copy(newCategoryType = type)
    }

    fun onIconSelected(iconName: String) {
        uiState = uiState.copy(selectedIcon = iconName)
    }

    fun onAddCategory() {
        if (uiState.newCategoryName.isBlank()) return

        viewModelScope.launch {
            // Color 객체를 Hex 문자열로 변환하여 저장
            val colorString = String.format("#%08X", uiState.selectedColor.toArgb())

            val newCategory = CategoryEntity(
                id = UUID.randomUUID().toString(),
                name = uiState.newCategoryName.trim(),
                type = uiState.newCategoryType,
                icon = uiState.selectedIcon,
                color = colorString
            )
            repository.insertCategory(newCategory)

            // 입력 상태 초기화
            uiState = uiState.copy(newCategoryName = "")
        }
    }

    fun onDeleteCategory(categoryId: String) {
        viewModelScope.launch {
            repository.deleteCategoryById(categoryId)
        }
    }
    // 컬러 피커 다이얼로그를 여는 함수
    fun onColorPickerClick() {
        uiState = uiState.copy(isColorPickerVisible = true)
    }

    // 컬러 피커 다이얼로그를 닫는 함수
    fun onColorPickerDismiss() {
        uiState = uiState.copy(isColorPickerVisible = false)
    }

    fun onColorSelected(color: Color) {
        val hexCode = String.format("#%08X", color.toArgb()).substring(2) // 알파값을 제외한 #RRGGBB 형태
        uiState = uiState.copy(
            selectedColor = color,
            customColorHex = "#$hexCode", // Hex 입력 필드도 함께 업데이트
            isCustomColorValid = true,
            isColorPickerVisible = false // 컬러 피커가 열려있었다면 닫는다.
        )
    }

    // Hex 코드 입력값이 변경될 때 호출
    fun onCustomColorHexChange(hex: String) {
        val filteredHex = if (hex.startsWith("#")) hex else "#$hex"
        val filteredHexClean = filteredHex.filter { it.isLetterOrDigit() || it == '#' }.take(7)

        uiState = uiState.copy(customColorHex = filteredHexClean)

        if (isValidHexColor(filteredHexClean)) {
            val color = Color(android.graphics.Color.parseColor(filteredHexClean))
            uiState = uiState.copy(
                selectedColor = color, // 유효하면 선택된 색상도 즉시 업데이트
                isCustomColorValid = true
            )
        } else {
            uiState = uiState.copy(isCustomColorValid = false)
        }
    }

    // Hex 코드가 유효한지 검증하는 private 함수
    private fun isValidHexColor(hex: String): Boolean {
        return hex.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$"))
    }

}