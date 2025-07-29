package com.yourdomain.walletmateeu.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yourdomain.walletmateeu.data.local.model.CategoryEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transactionEntity: TransactionEntity,
    allCategories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (TransactionEntity) -> Unit
) {
    var title by remember { mutableStateOf(transactionEntity.title) }
    var amount by remember { mutableStateOf(transactionEntity.amount.toString()) }
    var date by remember { mutableStateOf(transactionEntity.date) }
    var isDatePickerVisible by remember { mutableStateOf(false) }

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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Edit Transaction", style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") })
                OutlinedTextField(
                    value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select Date") },
                    modifier = Modifier.fillMaxWidth().clickable { isDatePickerVisible = true }
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = {
                        val updatedTransaction = transactionEntity.copy(
                            title = title,
                            amount = amount.toDoubleOrNull() ?: transactionEntity.amount,
                            date = date,
                            lastModified = System.currentTimeMillis()
                        )
                        onConfirm(updatedTransaction)
                    }) { Text("Save") }
                }
            }
        }
    }
}