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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yourdomain.walletmateeu.data.local.model.CategoryEntity
import com.yourdomain.walletmateeu.ui.components.ColorPickerDialog
import com.yourdomain.walletmateeu.ui.theme.WalletMateEUTheme
import com.yourdomain.walletmateeu.util.DummyData
import com.yourdomain.walletmateeu.util.IconHelper
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySettingsScreen(
    onNavigateToIconPicker: () -> Unit,
    navController: NavController,
    viewModel: CategorySettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val uiState = viewModel.uiState
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val iconPickerResult = navController.currentBackStackEntry
        ?.savedStateHandle?.getLiveData<String>("selected_icon")
    LaunchedEffect(iconPickerResult) {
        iconPickerResult?.observeForever { iconName ->
            if (iconName != null) {
                viewModel.onIconSelected(iconName)
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_icon")
            }
        }
    }

    if (uiState.isColorPickerVisible) {
        ColorPickerDialog(
            initialColor = uiState.selectedColor,
            onColorSelected = { color -> viewModel.onColorSelected(color) },
            onDismiss = { viewModel.onColorPickerDismiss() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories") },
                // <<--- 뒤로가기 아이콘 추가 ---
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(categories, key = { it.id }) { category ->
                    CategoryItem(category = category, onDeleteClick = { viewModel.onDeleteCategory(category.id) })
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Add New Category", style = MaterialTheme.typography.titleLarge)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(selected = uiState.newCategoryType == "EXPENSE", onClick = { viewModel.onNewCategoryTypeChange("EXPENSE") }, label = { Text("Expense") })
                        FilterChip(selected = uiState.newCategoryType == "INCOME", onClick = { viewModel.onNewCategoryTypeChange("INCOME") }, label = { Text("Income") })
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(uiState.selectedColor)
                                .clickable(onClick = onNavigateToIconPicker),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconHelper.getIcon(uiState.selectedIcon),
                                contentDescription = "Selected Icon: ${uiState.selectedIcon}",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        TextButton(onClick = onNavigateToIconPicker) { Text("Change") }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Color", style = MaterialTheme.typography.labelLarge)
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

@Composable
fun CategoryItem(
    category: CategoryEntity,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = try { Color(android.graphics.Color.parseColor(category.color)) } catch (e: Exception) { Color.Gray }
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(categoryColor), contentAlignment = Alignment.Center) {
                Icon(imageVector = IconHelper.getIcon(category.icon), contentDescription = category.name, tint = Color.White, modifier = Modifier.size(24.dp))
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
        // Preview는 NavController 의존성 때문에 직접 실행하기 복잡합니다.
    }
}