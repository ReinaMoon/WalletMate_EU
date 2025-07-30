package com.yourdomain.walletmateeu.ui.feature_dashboard

import android.graphics.Typeface
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.accompanist.flowlayout.FlowRow
import com.yourdomain.walletmateeu.data.local.model.TransactionWithCategoryAndTags
import com.yourdomain.walletmateeu.ui.components.EditTransactionDialog
import com.yourdomain.walletmateeu.util.IconHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToAddTransaction: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState()

    // --- 필터 메뉴(Bottom Sheet) ---
    if (uiState.isBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.showFilterBottomSheet(false) },
            sheetState = bottomSheetState
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text("Select Period", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                DateFilterType.values().forEach { filterType ->
                    if (filterType != DateFilterType.CUSTOM) {
                        ListItem(
                            headlineContent = { Text(filterType.displayName) },
                            modifier = Modifier.clickable { viewModel.onDateFilterChanged(filterType) }
                        )
                    }
                }
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Custom Range...") },
                    leadingContent = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    modifier = Modifier.clickable { viewModel.onDateFilterChanged(DateFilterType.CUSTOM) }
                )
            }
        }
    }

    // --- 날짜 범위 선택기(Date Range Picker) ---
    if (uiState.isDateRangePickerVisible) {
        val datePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = if(uiState.dateFilter == DateFilterType.CUSTOM) uiState.startDate else null,
            initialSelectedEndDateMillis = if(uiState.dateFilter == DateFilterType.CUSTOM) uiState.endDate else null
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.showDateRangePicker(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = datePickerState.selectedStartDateMillis
                        val end = datePickerState.selectedEndDateMillis
                        if (start != null && end != null) {
                            // 종료일은 23:59:59로 설정하여 하루 전체를 포함하도록 함
                            val correctedEnd = Calendar.getInstance().apply {
                                timeInMillis = end
                                set(Calendar.HOUR_OF_DAY, 23)
                                set(Calendar.MINUTE, 59)
                                set(Calendar.SECOND, 59)
                            }.timeInMillis
                            viewModel.onCustomDateRangeSelected(start, correctedEnd)
                        }
                    },
                    enabled = datePickerState.selectedEndDateMillis != null
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { viewModel.showDateRangePicker(false) }) { Text("Cancel") } }
        ) {
            DateRangePicker(state = datePickerState, modifier = Modifier.heightIn(max = 500.dp))
        }
    }

    // --- 수정 다이얼로그 ---
    if (uiState.isEditDialogOpen && uiState.transactionToEdit != null) {
        EditTransactionDialog(
            transactionWithCategoryAndTags = uiState.transactionToEdit!!,
            allCategories = uiState.allCategories,
            allTags = uiState.allTags,
            onDismiss = { viewModel.onDismissEditDialog() },
            onConfirm = { updatedTransaction, tags -> viewModel.onUpdateTransaction(updatedTransaction, tags) },
            onDelete = { transactionId -> viewModel.onDeleteTransaction(transactionId) }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddTransaction) {
                Icon(Icons.Default.Add, "Add Transaction")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { BalanceCard(balance = uiState.balance, totalIncome = uiState.totalIncome, totalExpense = uiState.totalExpense) }
            item { ExpensesChartCard(pieData = uiState.pieChartData) }

            stickyHeader {
                Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Transactions", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.weight(1f))
                        OutlinedButton(onClick = { viewModel.showFilterBottomSheet(true) }) {
                            Text(uiState.dateFilter.displayName)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select date range")
                        }
                    }

                    val filters = listOf("All", "Income", "Expense")
                    val selectedTabIndex = when (uiState.transactionFilter) {
                        "INCOME" -> 1
                        "EXPENSE" -> 2
                        else -> 0
                    }
                    TabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier.padding(top = 8.dp)) {
                        filters.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = {
                                    val newFilter = when (index) {
                                        1 -> "INCOME"
                                        2 -> "EXPENSE"
                                        else -> "ALL"
                                    }
                                    viewModel.onTransactionFilterChanged(newFilter)
                                },
                                text = { Text(title) }
                            )
                        }
                    }
                }
            }

            if (uiState.transactions.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                        Text("No transactions in this period.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(uiState.transactions, key = { it.transaction.id }) { transactionWithCategoryAndTags ->
                    TransactionItem(
                        transactionWithCategoryAndTags = transactionWithCategoryAndTags,
                        onClick = { viewModel.onTransactionClick(transactionWithCategoryAndTags) }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun TransactionItem(transactionWithCategoryAndTags: TransactionWithCategoryAndTags, onClick: () -> Unit) {
    val transaction = transactionWithCategoryAndTags.transaction
    val category = transactionWithCategoryAndTags.category
    val tags = transactionWithCategoryAndTags.tags
    val categoryColor = category?.color?.let { try { Color(android.graphics.Color.parseColor(it)) } catch (e: Exception) { Color.Gray } } ?: Color.Gray
    val categoryIcon = category?.icon?.let { IconHelper.getIcon(it) } ?: Icons.Default.Category
    val categoryName = category?.name ?: "Uncategorized"

    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(transaction.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(mainAxisSpacing = 6.dp, crossAxisSpacing = 4.dp) {
                        tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ) {
                                Text(
                                    text = "#${tag.name}",
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format(Locale.US, "%.2f", transaction.amount),
                color = if (transaction.type == "EXPENSE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun BalanceCard(balance: Double, totalIncome: Double, totalExpense: Double) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Balance", style = MaterialTheme.typography.titleMedium)
            Text(
                text = String.format("%.2f EUR", balance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Income", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        String.format("%.2f", totalIncome),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Expense", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        String.format("%.2f", totalExpense),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun ExpensesChartCard(pieData: PieData?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("This Month's Expenses", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            if (pieData != null && pieData.entryCount > 0) {
                PieChartComposable(pieData = pieData, modifier = Modifier.fillMaxWidth().height(250.dp))
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No expense data for this month.")
                }
            }
        }
    }
}

@Composable
fun PieChartComposable(pieData: PieData, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                holeRadius = 58f
                transparentCircleRadius = 61f
                setUsePercentValues(true)
                setEntryLabelColor(Color.Black.toArgb())
                setEntryLabelTypeface(Typeface.DEFAULT_BOLD)
                setEntryLabelTextSize(12f)
                legend.isEnabled = false
            }
        },
        update = { chart ->
            chart.data = pieData.apply {
                setValueFormatter(PercentFormatter(chart))
                setValueTextSize(12f)
                setValueTextColor(Color.Black.toArgb())
            }
            chart.animateY(1400)
            chart.invalidate()
        },
        modifier = modifier
    )
}