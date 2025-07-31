package com.yourdomain.walletmateeu.ui.feature_settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdomain.walletmateeu.data.local.model.TagEntity
import com.yourdomain.walletmateeu.data.repository.AppRepository
import com.yourdomain.walletmateeu.util.DummyData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TagSettingsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    // --- UI State ---
    val tags: StateFlow<List<TagEntity>> = repository.getAllTags()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var newTagName by mutableStateOf("")
        private set
    var selectedColor by mutableStateOf(DummyData.categoryColors.first()) // <<--- 색상 상태 추가
        private set

    var tagToEdit by mutableStateOf<TagEntity?>(null)
        private set
    var editedTagName by mutableStateOf("")
        private set

    var tagToDelete by mutableStateOf<TagEntity?>(null)
        private set

    fun onNewTagNameChange(name: String) {
        newTagName = name
    }

    fun onColorSelected(color: Color) { // <<--- 색상 선택 핸들러 추가
        selectedColor = color
    }

    fun onAddTag() {
        if (newTagName.isBlank()) return

        viewModelScope.launch {
            val colorString = String.format("#%08X", selectedColor.toArgb())
            val newTag = TagEntity(
                id = UUID.randomUUID().toString(),
                name = newTagName.trim().replace(" ", "_"),
                color = colorString // <<--- 색상 저장
            )
            repository.insertTag(newTag)
            newTagName = "" // 입력 필드 초기화
        }
    }

    // --- Event Handlers for Editing a Tag ---
    fun onEditClick(tag: TagEntity) {
        tagToEdit = tag
        editedTagName = tag.name
    }

    fun onEditedTagNameChange(name: String) {
        editedTagName = name
    }

    fun onConfirmEdit() {
        val tag = tagToEdit ?: return
        if (editedTagName.isBlank() || editedTagName.trim() == tag.name) {
            onDismissEditDialog()
            return
        }
        viewModelScope.launch {
            val updatedTag = tag.copy(
                name = editedTagName.trim().replace(" ", "_") // Replace spaces
            )
            repository.updateTag(updatedTag)
            onDismissEditDialog()
        }
    }

    fun onDismissEditDialog() {
        tagToEdit = null
        editedTagName = ""
    }

    // --- Event Handlers for Deleting a Tag ---
    fun onDeleteClick(tag: TagEntity) {
        tagToDelete = tag
    }

    fun onConfirmDelete() {
        val tagId = tagToDelete?.id ?: return
        viewModelScope.launch {
            repository.deleteTagById(tagId)
            onDismissDeleteDialog()
        }
    }

    fun onDismissDeleteDialog() {
        tagToDelete = null
    }
}