package com.yourdomain.walletmateeu.ui.feature_timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.flowlayout.FlowRow
import com.yourdomain.walletmateeu.ui.components.CategoryDropDown
import com.yourdomain.walletmateeu.ui.components.ClickableFakeTextField
import com.yourdomain.walletmateeu.ui.theme.WalletMateEUTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    onSaveCompleted: () -> Unit,
    viewModel: AddEditTransactionViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val allTags by viewModel.allTags.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val filteredCategories = remember(categories, uiState.transactionType) {
        categories.filter { it.type == uiState.transactionType }
    }

    LaunchedEffect(key1 = true) {
        viewModel.events.collect { event ->
            if (event is AddEditTransactionViewModel.UiEvent.SaveSuccess) {
                onSaveCompleted()
            }
        }
    }

    if (uiState.isDatePickerDialogVisible) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.date)
        DatePickerDialog(
            onDismissRequest = { viewModel.onEvent(AddEditTransactionEvent.OnDatePickerDismiss) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val correctedDate = it + TimeZone.getDefault().getOffset(it)
                        viewModel.onEvent(AddEditTransactionEvent.OnDateSelected(correctedDate))
                    }
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { viewModel.onEvent(AddEditTransactionEvent.OnDatePickerDismiss) }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (uiState.isEditMode) "Edit Transaction" else "Add Transaction") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { if (!uiState.isSaving) viewModel.onEvent(AddEditTransactionEvent.OnSaveClick) }) {
                if (uiState.isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Save")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {}
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row {
                FilterChip(selected = uiState.transactionType == "EXPENSE", onClick = { viewModel.onEvent(AddEditTransactionEvent.OnTypeChange("EXPENSE")) }, label = { Text("Expense") })
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(selected = uiState.transactionType == "INCOME", onClick = { viewModel.onEvent(AddEditTransactionEvent.OnTypeChange("INCOME")) }, label = { Text("Income") })
            }
            OutlinedTextField(value = uiState.title, onValueChange = { viewModel.onEvent(AddEditTransactionEvent.OnTitleChange(it)) }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
            OutlinedTextField(value = uiState.amount, onValueChange = { viewModel.onEvent(AddEditTransactionEvent.OnAmountChange(it)) }, label = { Text("Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done), modifier = Modifier.fillMaxWidth(), singleLine = true)
            ClickableFakeTextField(value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(uiState.date)), label = "Date", onClick = { viewModel.onEvent(AddEditTransactionEvent.OnDateClick) }, trailingIcon = Icons.Default.DateRange)

            // 공용 컴포넌트 사용
            CategoryDropDown(
                categories = filteredCategories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { category -> viewModel.onEvent(AddEditTransactionEvent.OnCategorySelect(category)) }
            )

            Text("Tags", style = MaterialTheme.typography.titleMedium)
            FlowRow(modifier = Modifier.fillMaxWidth(), mainAxisSpacing = 8.dp, crossAxisSpacing = 4.dp) {
                allTags.forEach { tag ->
                    val isSelected = uiState.selectedTags.contains(tag)
                    FilterChip(selected = isSelected, onClick = { viewModel.onEvent(AddEditTransactionEvent.OnTagSelected(tag)) }, label = { Text(tag.name) })
                }
            }
        }
    }
}

// 이 파일에 있던 로컬 CategoryDropDown 정의는 삭제되었습니다.

@Preview(showBackground = true)
@Composable
fun AddEditTransactionScreenPreview() {
    WalletMateEUTheme {
        AddEditTransactionScreen(onSaveCompleted = {})
    }
}