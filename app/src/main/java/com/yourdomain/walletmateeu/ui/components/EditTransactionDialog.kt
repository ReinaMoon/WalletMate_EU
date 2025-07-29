package com.yourdomain.walletmateeu.ui.components

import androidx.compose.foundation.background
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
import com.yourdomain.walletmateeu.ui.theme.WalletMateEUTheme // 테마 임포트가 필요할 수 있습니다.
import com.yourdomain.walletmateeu.util.DummyData
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transactionWithCategory: TransactionWithCategory,
    allCategories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (TransactionEntity) -> Unit,
    onDelete: (String) -> Unit
) {
    var title by remember { mutableStateOf(transactionWithCategory.transaction.title) }
    var amount by remember { mutableStateOf(transactionWithCategory.transaction.amount.toString()) }
    var date by remember { mutableStateOf(transactionWithCategory.transaction.date) }
    var selectedCategory by remember { mutableStateOf(transactionWithCategory.category) }
    var isDatePickerVisible by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val filteredCategories = remember(allCategories) {
        allCategories.filter { it.type == transactionWithCategory.transaction.type }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(transactionWithCategory.transaction.id)
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") }
            }
        )
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

                var categoryMenuExpanded by remember { mutableStateOf(false) }

                // --- 여기를 수정합니다 ---
                ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    // onExpandedChange를 통해 expanded 상태를 변경합니다.
                    // 이제 TextField를 클릭하면 이 람다가 호출됩니다.
                    onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "Uncategorized",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor() // 이 modifier는 Box가 클릭 영역을 인식하는 데 필수적입니다.
                    )
                    ExposedDropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        filteredCategories.forEach { category ->
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
                                    selectedCategory = category
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                ClickableFakeTextField(
                    value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date)),
                    label = "Date",
                    onClick = { isDatePickerVisible = true },
                    trailingIcon = Icons.Default.DateRange
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete") }
                    Row {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Button(onClick = {
                            val updatedTransaction = transactionWithCategory.transaction.copy(
                                title = title,
                                amount = amount.toDoubleOrNull() ?: transactionWithCategory.transaction.amount,
                                date = date,
                                categoryId = selectedCategory?.id,
                                lastModified = System.currentTimeMillis()
                            )
                            onConfirm(updatedTransaction)
                        }) { Text("Save") }
                    }
                }
            }
        }
    }
}