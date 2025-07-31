package com.yourdomain.walletmateeu.ui.feature_settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourdomain.walletmateeu.R
import com.yourdomain.walletmateeu.data.local.model.TagEntity
import com.yourdomain.walletmateeu.util.DummyData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSettingsScreen(
    viewModel: TagSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
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
            title = { Text(stringResource(R.string.tag_settings_delete_dialog_title)) },
            text = { Text(stringResource(R.string.tag_settings_delete_dialog_message, tagToDelete.name)) },
            confirmButton = {
                Button(onClick = { viewModel.onConfirmDelete() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text(stringResource(R.string.dialog_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDeleteDialog() }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tag_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_button_desc))
                    }
                }

            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tags, key = { it.id }) { tag ->
                    TagItem(
                        tag = tag,
                        onEditClick = { viewModel.onEditClick(tag) },
                        onDeleteClick = { viewModel.onDeleteClick(tag) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.category_settings_add_new), style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(DummyData.categoryColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { viewModel.onColorSelected(color) }
                                .border(
                                    width = 2.dp,
                                    color = if (viewModel.selectedColor.toArgb() == color.toArgb()) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                        )
                    }
                }
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { viewModel.onNewTagNameChange(it) },
                    label = { Text(stringResource(R.string.tag_settings_add_new)) },
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
}

@Composable
fun TagItem(
    tag: TagEntity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tagColor = try { Color(android.graphics.Color.parseColor(tag.color)) } catch (e: Exception) { MaterialTheme.colorScheme.secondaryContainer }
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(tagColor))
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
        title = { Text(stringResource(R.string.tag_settings_edit_title, originalTagName)) },
        text = {
            OutlinedTextField(
                value = editedTagName,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.tag_settings_new_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = editedTagName.isNotBlank() && editedTagName != originalTagName
            ) {
                Text(stringResource(R.string.transaction_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}