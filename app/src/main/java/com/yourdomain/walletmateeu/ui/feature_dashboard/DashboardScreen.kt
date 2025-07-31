package com.yourdomain.walletmateeu.ui.feature_dashboard

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.data.BarData
import com.google.accompanist.flowlayout.FlowRow
import com.yourdomain.walletmateeu.R
import com.yourdomain.walletmateeu.data.local.model.TransactionWithCategoryAndTags
import com.yourdomain.walletmateeu.ui.components.EditTransactionDialog
import com.yourdomain.walletmateeu.ui.components.HorizontalBarChartComposable
import com.yourdomain.walletmateeu.util.IconHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToTransactionDetail: (String) -> Unit // 상세 화면 이동 콜백
) {
    val uiState by viewModel.uiState.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState()

    if (uiState.isBottomSheetVisible) {
        ModalBottomSheet(onDismissRequest = { viewModel.showFilterBottomSheet(false) }, sheetState = bottomSheetState) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(stringResource(R.string.analytics_select_period), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                DateFilterType.values().filter { it != DateFilterType.CUSTOM }.forEach { filterType ->
                    ListItem(
                        headlineContent = { Text(filterType.displayName) },
                        modifier = Modifier.clickable { viewModel.onDateFilterChanged(filterType) }
                    )
                }
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text(stringResource(id = R.string.date_filter_custom)) },
                    leadingContent = { Icon(Icons.Default.DateRange, null) },
                    modifier = Modifier.clickable { viewModel.onDateFilterChanged(DateFilterType.CUSTOM) }
                )
            }
        }
    }

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
                        val start = datePickerState.selectedStartDateMillis; val end = datePickerState.selectedEndDateMillis
                        if (start != null && end != null) {
                            val correctedEnd = Calendar.getInstance().apply { timeInMillis = end; set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
                            viewModel.onCustomDateRangeSelected(start, correctedEnd)
                        }
                    },
                    enabled = datePickerState.selectedEndDateMillis != null
                ) { Text(stringResource(R.string.dialog_ok)) }
            },
            dismissButton = { TextButton(onClick = { viewModel.showDateRangePicker(false) }) { Text(stringResource(R.string.dialog_cancel)) } }
        ) { DateRangePicker(state = datePickerState, modifier = Modifier.heightIn(max = 500.dp)) }
    }

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
                Icon(Icons.Default.Add, stringResource(R.string.add_transaction_title))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                BalanceCard(
                    balance = uiState.balance,
                    totalIncome = uiState.totalIncome,
                    totalExpense = uiState.totalExpense,
                    currency = uiState.currency
                )
            }
            item {
                IncomeExpenseProgressBarCard(
                    totalIncome = uiState.totalIncome,
                    totalExpense = uiState.totalExpense,
                    currency = uiState.currency,
                    dateFilterName = uiState.dateFilter.displayName
                )
            }
            stickyHeader {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp, top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_transactions),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.weight(1f))
                        OutlinedButton(onClick = { viewModel.showFilterBottomSheet(true) }) {
                            Text(uiState.dateFilter.displayName)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = stringResource(R.string.analytics_select_period))
                        }
                    }
                    val filters = listOf(stringResource(R.string.dashboard_filter_all), stringResource(R.string.dashboard_income), stringResource(R.string.dashboard_expense))
                    val selectedTabIndex = when (uiState.transactionFilter) { "INCOME" -> 1; "EXPENSE" -> 2; else -> 0 }
                    TabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier.padding(top = 8.dp)) {
                        filters.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { viewModel.onTransactionFilterChanged(when (index) { 1 -> "INCOME"; 2 -> "EXPENSE"; else -> "ALL" }) },
                                text = { Text(title) }
                            )
                        }
                    }
                }
            }
            if (uiState.transactions.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                        Text(stringResource(R.string.dashboard_no_transactions_period), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(uiState.transactions, key = { it.transaction.id }) { transactionWithCategoryAndTags ->
                    TransactionItem(
                        transactionWithCategoryAndTags = transactionWithCategoryAndTags,
                        currency = uiState.currency,
                        onClick = {
                            // 수정 다이얼로그 대신 상세 화면으로 이동
                            onNavigateToTransactionDetail(transactionWithCategoryAndTags.transaction.id)
                        }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun TransactionItem(
    transactionWithCategoryAndTags: TransactionWithCategoryAndTags,
    currency: String,
    onClick: () -> Unit
) {
    val transaction = transactionWithCategoryAndTags.transaction
    val category = transactionWithCategoryAndTags.category
    val tags = transactionWithCategoryAndTags.tags
    val categoryColor = category?.color?.let { try { Color(android.graphics.Color.parseColor(it)) } catch (e: Exception) { Color.Gray } } ?: Color.Gray
    val categoryIcon = category?.icon?.let { IconHelper.getIcon(it) } ?: Icons.Default.Category
    val categoryName = category?.name ?: "Uncategorized"

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
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
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = transaction.title, style = MaterialTheme.typography.titleMedium)
                    if (transaction.imageUri != null) {
                        Icon(
                            imageVector = Icons.Default.Attachment,
                            contentDescription = "Attachment",
                            modifier = Modifier.size(16.dp).padding(start = 4.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
                text = String.format(Locale.US, "%.2f %s", transaction.amount, currency),
                color = if (transaction.type == "EXPENSE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun BalanceCard(balance: Double, totalIncome: Double, totalExpense: Double, currency: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.dashboard_balance), style = MaterialTheme.typography.titleMedium)
            Text(text = String.format("%.2f %s", balance, currency), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.dashboard_income), style = MaterialTheme.typography.bodyMedium)
                    Text(String.format("%.2f", totalIncome), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.dashboard_expense), style = MaterialTheme.typography.bodyMedium)
                    Text(String.format("%.2f", totalExpense), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun IncomeExpenseProgressBarCard(
    totalIncome: Double,
    totalExpense: Double,
    currency: String,
    dateFilterName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.income_vs_expense_title, dateFilterName),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            val total = totalIncome + totalExpense
            if (total > 0) {
                val incomePercentage = (totalIncome / total).toFloat()
                Row(
                    modifier = Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(12.dp))
                ) {
                    if (incomePercentage > 0f) {
                        Box(modifier = Modifier.fillMaxHeight().weight(incomePercentage).background(MaterialTheme.colorScheme.primaryContainer))
                    }
                    if (incomePercentage < 1f) {
                        Box(modifier = Modifier.fillMaxHeight().weight(1f - incomePercentage).background(MaterialTheme.colorScheme.errorContainer))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = stringResource(R.string.dashboard_income) + String.format(": %.2f %s", totalIncome, currency), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Text(text = stringResource(R.string.dashboard_expense) + String.format(": %.2f %s", totalExpense, currency), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.dashboard_no_data))
                }
            }
        }
    }
}