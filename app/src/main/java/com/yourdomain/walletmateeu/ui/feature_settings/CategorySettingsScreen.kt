package com.yourdomain.walletmateeu.ui.feature_settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourdomain.walletmateeu.data.local.model.CategoryEntity
import com.yourdomain.walletmateeu.ui.components.ColorPickerDialog
import com.yourdomain.walletmateeu.ui.theme.WalletMateEUTheme
import com.yourdomain.walletmateeu.util.DummyData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySettingsScreen(
    viewModel: CategorySettingsViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val uiState = viewModel.uiState
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    if (uiState.isColorPickerVisible) {
        ColorPickerDialog(
            initialColor = uiState.selectedColor,
            onColorSelected = { color -> viewModel.onColorSelected(color) },
            onDismiss = { viewModel.onColorPickerDismiss() }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Manage Categories") }) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        onDeleteClick = { viewModel.onDeleteCategory(category.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Add New Category", style = MaterialTheme.typography.titleMedium)
                    Row {
                        FilterChip(selected = uiState.newCategoryType == "EXPENSE", onClick = { viewModel.onNewCategoryTypeChange("EXPENSE") }, label = { Text("Expense") })
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(selected = uiState.newCategoryType == "INCOME", onClick = { viewModel.onNewCategoryTypeChange("INCOME") }, label = { Text("Income") })
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 48.dp),
                        modifier = Modifier.heightIn(max = 104.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(DummyData.categoryIcons.toList()) { (iconName, icon) ->
                            val isSelected = uiState.selectedIcon == iconName
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(uiState.selectedColor)
                                    .clickable { viewModel.onIconSelected(iconName) }
                                    .border(
                                        width = 3.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = iconName,
                                    tint = Color.White,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                    Text("Select Color", style = MaterialTheme.typography.titleSmall)
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
                                        color = if (uiState.selectedColor.toArgb() == color.toArgb()) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(uiState.selectedColor).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape))
                        OutlinedTextField(value = uiState.customColorHex, onValueChange = { viewModel.onCustomColorHexChange(it) }, label = { Text("Hex Code (#RRGGBB)") }, modifier = Modifier.weight(1f), isError = !uiState.isCustomColorValid, singleLine = true)
                        Button(onClick = { viewModel.onColorPickerClick() }) { Text("Pick") }
                    }
                    OutlinedTextField(
                        value = uiState.newCategoryName,
                        onValueChange = { viewModel.onNewCategoryNameChange(it) },
                        label = { Text("Category name") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    viewModel.onAddCategory()
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                },
                                enabled = uiState.newCategoryName.isNotBlank()
                            ) { Icon(Icons.Default.Add, contentDescription = "Add category") }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (uiState.newCategoryName.isNotBlank()) {
                                viewModel.onAddCategory()
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        })
                    )
                }
            }
        }
    }
}

// --- CategoryItem UI 수정 (미리보기와 동일한 스타일로) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryItem(
    category: CategoryEntity,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = try { Color(android.graphics.Color.parseColor(category.color)) } catch (e: Exception) { Color.Black }

    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 아이콘 UI를 미리보기 스타일과 동일하게 변경
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(categoryColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = DummyData.categoryIcons[category.icon] ?: Icons.Default.Add,
                    contentDescription = category.name,
                    tint = Color.White, // 흰색 아이콘
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(text = category.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(text = category.type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            IconButton(onClick = onDeleteClick) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete ${category.name}", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CategorySettingsScreenPreview() {
    WalletMateEUTheme {
        CategorySettingsScreen()
    }
}