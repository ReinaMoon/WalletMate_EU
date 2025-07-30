package com.yourdomain.walletmateeu.ui.feature_settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourdomain.walletmateeu.data.local.model.TagEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSettingsScreen(
    viewModel: TagSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit // <<--- 1. 파라미터 추가
) {
    val tags by viewModel.tags.collectAsState()
    val newTagName = viewModel.newTagName
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val tagToEdit = viewModel.tagToEdit
    val tagToDelete = viewModel.tagToDelete

    if (tagToEdit != null) {
        EditTagDialog(
            originalTagName = tagToEdit.name,
            editedTagName = viewModel.editedTagName,
            onNameChange = viewModel::onEditedTagNameChange,
            onConfirm = {
                viewModel.onConfirmEdit()
                keyboardController?.hide()
                focusManager.clearFocus()
            },
            onDismiss = viewModel::onDismissEditDialog
        )
    }

    if (tagToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDeleteDialog() },
            title = { Text("Delete Tag") },
            text = { Text("Are you sure you want to delete the tag '#${tagToDelete.name}'?") },
            confirmButton = {
                Button(onClick = { viewModel.onConfirmDelete() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDeleteDialog() }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Tags") },
                // <<--- 2. 뒤로가기 아이콘 추가 ---
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
        ) {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tags, key = { it.id }) { tag ->
                    TagItem(tag = tag, onEditClick = { viewModel.onEditClick(tag) }, onDeleteClick = { viewModel.onDeleteClick(tag) })
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = newTagName,
                onValueChange = viewModel::onNewTagNameChange,
                label = { Text("New tag name (e.g. summer_trip)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            viewModel.onAddTag()
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        },
                        enabled = newTagName.isNotBlank()
                    ) { Icon(Icons.Default.Add, contentDescription = "Add tag") }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (newTagName.isNotBlank()) {
                        viewModel.onAddTag()
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                })
            )
        }
    }
}

// 아래의 다른 Composable들은 변경 없습니다.
@Composable
fun TagItem(
    tag: TagEntity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${tag.name}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit ${tag.name}",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete ${tag.name}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EditTagDialog(
    originalTagName: String,
    editedTagName: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Tag '#$originalTagName'") },
        text = {
            OutlinedTextField(
                value = editedTagName,
                onValueChange = onNameChange,
                label = { Text("New tag name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = editedTagName.isNotBlank() && editedTagName != originalTagName
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}