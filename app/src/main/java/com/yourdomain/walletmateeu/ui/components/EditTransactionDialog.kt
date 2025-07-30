package com.yourdomain.walletmateeu.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.yourdomain.walletmateeu.data.local.model.CategoryEntity
import com.yourdomain.walletmateeu.data.local.model.TagEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionWithCategoryAndTags
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transactionWithCategoryAndTags: TransactionWithCategoryAndTags,
    allCategories: List<CategoryEntity>,
    allTags: List<TagEntity>,
    onDismiss: () -> Unit,
    onConfirm: (TransactionEntity, List<TagEntity>) -> Unit,
    onDelete: (String) -> Unit
) {
    val transactionEntity = transactionWithCategoryAndTags.transaction

    // --- 상태 관리 ---
    var title by remember { mutableStateOf(transactionEntity.title) }
    var amount by remember { mutableStateOf(String.format(Locale.US, "%.2f", transactionEntity.amount)) }
    var selectedCategory by remember { mutableStateOf(transactionWithCategoryAndTags.category) }
    val selectedTags = remember { mutableStateListOf<TagEntity>().also { it.addAll(transactionWithCategoryAndTags.tags) } }

    // <<--- 날짜 상태 추가 ---
    var date by remember { mutableStateOf(transactionEntity.date) }
    var isDatePickerVisible by remember { mutableStateOf(false) }

    val transactionType = transactionEntity.type
    val filteredCategories = remember(allCategories, transactionType) {
        allCategories.filter { it.type == transactionType }
    }

    // <<--- 날짜 선택 다이얼로그 추가 ---
    if (isDatePickerVisible) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { isDatePickerVisible = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        // 자정 시간 보정
                        val correctedDate = it + TimeZone.getDefault().getOffset(it)
                        date = correctedDate
                    }
                    isDatePickerVisible = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { isDatePickerVisible = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Transaction") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true)
                OutlinedTextField(value = amount, onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*(\\.\\d{0,2})?\$"))) { amount = it } }, label = { Text("Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)

                // <<--- 날짜 선택 UI 추가 ---
                ClickableFakeTextField(
                    value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date)),
                    label = "Date",
                    onClick = { isDatePickerVisible = true },
                    trailingIcon = Icons.Default.DateRange
                )

                CategoryDropDown(
                    categories = filteredCategories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )

                Text("Tags", style = MaterialTheme.typography.titleSmall)
                FlowRow(modifier = Modifier.fillMaxWidth(), mainAxisSpacing = 8.dp, crossAxisSpacing = 4.dp) {
                    allTags.forEach { tag ->
                        val isSelected = selectedTags.contains(tag)
                        FilterChip(
                            selected = isSelected,
                            onClick = { if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag) },
                            label = { Text(tag.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedTransaction = transactionEntity.copy(
                        title = title,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        categoryId = selectedCategory?.id,
                        date = date, // <<--- 수정된 날짜 반영
                        lastModified = System.currentTimeMillis()
                    )
                    onConfirm(updatedTransaction, selectedTags.toList())
                },
                enabled = title.isNotBlank() && amount.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onDelete(transactionEntity.id) }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}