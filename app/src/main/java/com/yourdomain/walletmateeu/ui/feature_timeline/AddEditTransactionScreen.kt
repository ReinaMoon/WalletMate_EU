package com.yourdomain.walletmateeu.ui.feature_timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourdomain.walletmateeu.data.local.model.CategoryEntity
import com.yourdomain.walletmateeu.ui.theme.WalletMateEUTheme
import com.yourdomain.walletmateeu.util.DummyData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.yourdomain.walletmateeu.ui.components.ClickableFakeTextField


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    onSaveCompleted: () -> Unit,
    viewModel: AddEditTransactionViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val categories by viewModel.categories.collectAsState()

    val filteredCategories = remember(categories, uiState.transactionType) {
        categories.filter { it.type == uiState.transactionType }
    }

    // --- DatePicker Dialog 로직 ---
    if (uiState.isDatePickerDialogVisible) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.date)
        DatePickerDialog(
            onDismissRequest = { viewModel.onEvent(AddTransactionEvent.OnDatePickerDismiss) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val correctedDate = it + TimeZone.getDefault().getOffset(it)
                        viewModel.onEvent(AddTransactionEvent.OnDateSelected(correctedDate))
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(AddTransactionEvent.OnDatePickerDismiss) }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(if (uiState.isEditMode) "Edit Transaction" else "Add Transaction")
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.onEvent(AddTransactionEvent.OnSaveClick)
                onSaveCompleted()
            }) {
                Text("Save")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .clickable { viewModel.onEvent(AddTransactionEvent.OnDateClick)},
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 지출 / 수입 선택
            Row {
                FilterChip(
                    selected = uiState.transactionType == "EXPENSE",
                    onClick = { viewModel.onEvent(AddTransactionEvent.OnTypeChange("EXPENSE")) },
                    label = { Text("Expense") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = uiState.transactionType == "INCOME",
                    onClick = { viewModel.onEvent(AddTransactionEvent.OnTypeChange("INCOME")) },
                    label = { Text("Income") }
                )
            }

            // 제목 입력
            OutlinedTextField(value = uiState.title, onValueChange = { viewModel.onEvent(AddTransactionEvent.OnTitleChange(it)) }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            // 금액 입력
            OutlinedTextField(value = uiState.amount, onValueChange = { viewModel.onEvent(AddTransactionEvent.OnAmountChange(it)) }, label = { Text("Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), singleLine = true)

            // 날짜 선택
            ClickableFakeTextField(
                value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(uiState.date)),
                label = "Date",
                onClick = { viewModel.onEvent(AddTransactionEvent.OnDateClick) },
                trailingIcon = Icons.Default.DateRange
            )

            // 카테고리 선택
            CategoryDropDown(
                categories = filteredCategories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { category ->
                    viewModel.onEvent(AddTransactionEvent.OnCategorySelect(category))
                }
            )

            // --- 태그 UI (올바른 위치) ---
            Text("Tags (coming soon)", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropDown(
    categories: List<CategoryEntity>,
    selectedCategory: CategoryEntity?,
    onCategorySelected: (CategoryEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            placeholder = { Text("Uncategorized") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val categoryColor = try { Color(android.graphics.Color.parseColor(category.color)) } catch (e: Exception) { Color.Black }
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(categoryColor)) {
                                Icon(
                                    imageVector = DummyData.categoryIcons[category.icon] ?: Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(category.name)
                        }
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddEditTransactionScreenPreview() {
    WalletMateEUTheme {
        AddEditTransactionScreen(onSaveCompleted = {})
    }
}