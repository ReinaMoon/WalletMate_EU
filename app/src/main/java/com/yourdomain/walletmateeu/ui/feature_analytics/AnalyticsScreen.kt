package com.yourdomain.walletmateeu.ui.feature_analytics

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.PieData
import com.yourdomain.walletmateeu.ui.components.LineChartComposable
import com.yourdomain.walletmateeu.ui.components.PieChartComposable
import com.yourdomain.walletmateeu.ui.feature_dashboard.DateFilterType
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel(),
    onNavigateToTagDetail: (tagId: String, tagName: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabTitles = listOf("By Category", "By Tag")
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()

    // ViewModel의 탭 상태와 UI의 탭 상태를 동기화
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onTabSelected(pagerState.currentPage)
    }
    LaunchedEffect(uiState.selectedTabIndex) {
        if (uiState.selectedTabIndex != pagerState.currentPage) {
            pagerState.animateScrollToPage(uiState.selectedTabIndex)
        }
    }

    if (uiState.isFilterSheetVisible) {
        ModalBottomSheet(onDismissRequest = { viewModel.showFilterBottomSheet(false) }, sheetState = bottomSheetState) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text("Select Period", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                DateFilterType.values().filter { it != DateFilterType.CUSTOM }.forEach { filterType ->
                    ListItem(headlineContent = { Text(filterType.displayName) }, modifier = Modifier.clickable { viewModel.onDateFilterChanged(filterType) })
                }
                HorizontalDivider()
                ListItem(headlineContent = { Text("Custom Range...") }, leadingContent = { Icon(Icons.Default.DateRange, null) }, modifier = Modifier.clickable { viewModel.onDateFilterChanged(DateFilterType.CUSTOM) })
            }
        }
    }
    if (uiState.isDateRangePickerVisible) {
        val datePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = if (uiState.dateFilter == DateFilterType.CUSTOM) uiState.startDate else null,
            initialSelectedEndDateMillis = if (uiState.dateFilter == DateFilterType.CUSTOM) uiState.endDate else null
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
                    }, enabled = datePickerState.selectedEndDateMillis != null
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { viewModel.showDateRangePicker(false) }) { Text("Cancel") } }
        ) { DateRangePicker(state = datePickerState, modifier = Modifier.heightIn(max = 500.dp)) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                actions = {
                    OutlinedButton(onClick = { viewModel.showFilterBottomSheet(true) }) {
                        Text(uiState.dateFilter.displayName)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Period")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            viewModel.onTabSelected(index)
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = { Text(title) }
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> CategoryAnalysisPage(uiState)
                    1 -> TagAnalysisPage(uiState, onNavigateToTagDetail)
                }
            }
        }
    }
}

@Composable
fun CategoryAnalysisPage(uiState: AnalyticsUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SummaryCard(totalIncome = uiState.totalIncome, totalExpense = uiState.totalExpense) }
        item { TrendChartCard(lineData = uiState.trendChartData) }
        item { ExpenseChartCard(pieData = uiState.expensePieData) }
        item { IncomeChartCard(pieData = uiState.incomePieData) }
    }
}

@Composable
fun TagAnalysisPage(
    uiState: AnalyticsUiState,
    onNavigateToTagDetail: (tagId: String, tagName: String) -> Unit
) {
    if (uiState.tagAnalysisList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("No transactions with tags in this period.")
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(uiState.tagAnalysisList, key = { it.tag.id }) { tagAnalysis ->
            TagAnalysisItem(
                tagAnalysis = tagAnalysis,
                onClick = {
                    val encodedTagName = URLEncoder.encode(tagAnalysis.tag.name, "UTF-8")
                    onNavigateToTagDetail(tagAnalysis.tag.id, encodedTagName)
                }
            )
        }
    }
}

@Composable
fun TagAnalysisItem(tagAnalysis: TagAnalysis, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("#${tagAnalysis.tag.name}", style = MaterialTheme.typography.titleMedium)
                Text("${tagAnalysis.transactionCount} transactions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(String.format("%.2f EUR", tagAnalysis.totalAmount), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SummaryCard(totalIncome: Double, totalExpense: Double) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Income", style = MaterialTheme.typography.titleMedium)
                Text(String.format("%.2f EUR", totalIncome), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Expense", style = MaterialTheme.typography.titleMedium)
                Text(String.format("%.2f EUR", totalExpense), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun TrendChartCard(lineData: LineData?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Income vs Expense Trend", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            if (lineData != null && lineData.entryCount > 0) {
                LineChartComposable(lineData = lineData, modifier = Modifier.fillMaxWidth().height(250.dp))
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
                    Text("Not enough data to show trend.")
                }
            }
        }
    }
}

@Composable
fun ExpenseChartCard(pieData: PieData?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Expenses by Category", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            if (pieData != null && pieData.entryCount > 0) {
                PieChartComposable(pieData = pieData, modifier = Modifier.fillMaxWidth().height(300.dp))
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    Text("No expense data for this period.")
                }
            }
        }
    }
}

@Composable
fun IncomeChartCard(pieData: PieData?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Income by Category", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            if (pieData != null && pieData.entryCount > 0) {
                PieChartComposable(pieData = pieData, modifier = Modifier.fillMaxWidth().height(300.dp))
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    Text("No income data for this period.")
                }
            }
        }
    }
}