package com.yourdomain.walletmateeu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yourdomain.walletmateeu.data.local.model.CategoryEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionWithCategory
import com.yourdomain.walletmateeu.util.DummyData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transactionWithCategory: TransactionWithCategory,
    allCategories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (TransactionEntity) -> Unit
) {
    // 다이얼로그 내부에서 자체적으로 모든 상태를 관리합니다.
    var title by remember { mutableStateOf(transactionWithCategory.transaction.title) }
    var amount by remember { mutableStateOf(transactionWithCategory.transaction.amount.toString()) }
    var date by remember { mutableStateOf(transactionWithCategory.transaction.date) }
    var selectedCategory by remember { mutableStateOf(transactionWithCategory.category) }
    var isDatePickerVisible by remember { mutableStateOf(false) }

    // 현재 거래 타입(수입/지출)에 맞는 카테고리만 필터링합니다.
    val filteredCategories = remember(allCategories) {
        allCategories.filter { it.type == transactionWithCategory.transaction.type }
    }

    if (isDatePickerVisible) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { isDatePickerVisible = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val correctedDate = it + TimeZone.getDefault().getOffset(it)
                        date = correctedDate
                    }
                    isDatePickerVisible = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerVisible = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Edit Transaction", style = MaterialTheme.typography.headlineSmall)

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })

                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") })

                // --- 카테고리 선택 드롭다운 (AddEditTransactionScreen의 것과 동일) ---
                var categoryMenuExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = categoryMenuExpanded, onExpandedChange = { categoryMenuExpanded = !it }) {
                    OutlinedTextField(
                        value = selectedCategory.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().clickable { isDatePickerVisible = true }
                    )
                    ExposedDropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }) {
                        filteredCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { /* ... DropdownMenuItem UI ... */ },
                                onClick = {
                                    selectedCategory = category
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select Date") },
                    modifier = Modifier.clickable { isDatePickerVisible = true }
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = {
                        val updatedTransaction = transactionWithCategory.transaction.copy(
                            title = title,
                            amount = amount.toDoubleOrNull() ?: transactionWithCategory.transaction.amount,
                            date = date,
                            categoryId = selectedCategory.id, // <<--- 수정된 카테고리 ID 반영
                            lastModified = System.currentTimeMillis()
                        )
                        onConfirm(updatedTransaction)
                    }) { Text("Save") }
                }
            }
        }
    }
}