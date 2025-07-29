package com.yourdomain.walletmateeu.ui.feature_timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourdomain.walletmateeu.data.local.model.TransactionWithCategory
import com.yourdomain.walletmateeu.ui.components.EditTransactionDialog
import com.yourdomain.walletmateeu.util.DummyData
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.filled.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = hiltViewModel(),
    onNavigateToAddTransaction: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isEditDialogOpen && uiState.transactionToEdit != null) {
        EditTransactionDialog(
            transactionWithCategory = uiState.transactionToEdit!!,
            allCategories = uiState.allCategories,
            onDismiss = { viewModel.onDismissEditDialog() },
            onConfirm = { updatedTransaction ->
                viewModel.onUpdateTransaction(updatedTransaction)
            },
            onDelete = { transactionId ->
                viewModel.onDeleteTransaction(transactionId)
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddTransaction) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = when (uiState.selectedFilter) {
                "ALL" -> 0
                "EXPENSE" -> 1
                else -> 2
            }) {
                Tab(selected = uiState.selectedFilter == "ALL", onClick = { viewModel.onFilterChange("ALL") }, text = { Text("All") })
                Tab(selected = uiState.selectedFilter == "EXPENSE", onClick = { viewModel.onFilterChange("EXPENSE") }, text = { Text("Expenses") })
                Tab(selected = uiState.selectedFilter == "INCOME", onClick = { viewModel.onFilterChange("INCOME") }, text = { Text("Incomes") })
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.transactions) { transactionWithCategory ->
                    TransactionItem(
                        transactionWithCategory = transactionWithCategory,
                        onClick = { viewModel.onTransactionClick(transactionWithCategory) }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transactionWithCategory: TransactionWithCategory,
    onClick: () -> Unit
) {
    val transaction = transactionWithCategory.transaction
    val category = transactionWithCategory.category // 이제 nullable일 수 있음

    // 카테고리가 null이면 기본값(회색, 기본 아이콘)을 사용
    val categoryColor = category?.color?.let { try { Color(android.graphics.Color.parseColor(it)) } catch (e: Exception) { Color.Gray } } ?: Color.Gray
    val categoryIcon = category?.icon?.let { DummyData.categoryIcons[it] } ?: Icons.Default.Category
    val categoryName = category?.name ?: "Uncategorized"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(categoryColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = categoryName,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.title, style = MaterialTheme.typography.titleMedium)
                Text(text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(transaction.date)), style = MaterialTheme.typography.bodySmall)
            }
            // 금액을 소수점 두 자리로 포맷팅하여 표시
            Text(
                text = String.format("%.2f EUR", transaction.amount),
                color = if (transaction.type == "EXPENSE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}
