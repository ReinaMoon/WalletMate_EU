package com.yourdomain.walletmateeu.ui.feature_analytics

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.PieData
import com.yourdomain.walletmateeu.R
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
    val tabTitles = listOf(stringResource(R.string.analytics_by_category), stringResource(R.string.analytics_by_tag))
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()

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
                ) { Text(stringResource(R.string.dialog_ok)) }
            },
            dismissButton = { TextButton(onClick = { viewModel.showDateRangePicker(false) }) { Text(stringResource(R.string.dialog_cancel)) } }
        ) { DateRangePicker(state = datePickerState, modifier = Modifier.heightIn(max = 500.dp)) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.analytics_title)) },
                actions = {
                    OutlinedButton(onClick = { viewModel.showFilterBottomSheet(true) }) {
                        Text(uiState.dateFilter.displayName)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = stringResource(R.string.analytics_select_period))
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
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                                viewModel.onTabSelected(index)
                            }
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
        item { SummaryCard(totalIncome = uiState.totalIncome, totalExpense = uiState.totalExpense, currency = uiState.currency) }
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
            Text(stringResource(R.string.analytics_no_tag_data_period))
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
                currency = uiState.currency,
                onClick = {
                    val encodedTagName = URLEncoder.encode(tagAnalysis.tag.name, "UTF-8")
                    onNavigateToTagDetail(tagAnalysis.tag.id, encodedTagName)
                }
            )
        }
    }
}

@Composable
fun TagAnalysisItem(tagAnalysis: TagAnalysis, currency: String, onClick: () -> Unit) {
    val tagColor = try { Color(android.graphics.Color.parseColor(tagAnalysis.tag.color)) } catch (e: Exception) { MaterialTheme.colorScheme.surfaceVariant }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(tagColor))
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text("#${tagAnalysis.tag.name}", style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.tag_detail_transactions_count, tagAnalysis.transactionCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                String.format("%.2f %s", tagAnalysis.totalAmount, currency),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SummaryCard(totalIncome: Double, totalExpense: Double, currency: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.dashboard_income), style = MaterialTheme.typography.titleMedium)
                Text(
                    String.format("%.2f %s", totalIncome, currency),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.dashboard_expense), style = MaterialTheme.typography.titleMedium)
                Text(
                    String.format("%.2f %s", totalExpense, currency),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun TrendChartCard(lineData: LineData?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.analytics_trend), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            if (lineData != null && lineData.entryCount > 0) {
                LineChartComposable(lineData = lineData, modifier = Modifier.fillMaxWidth().height(250.dp))
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.analytics_no_trend_data))
                }
            }
        }
    }
}

@Composable
fun ExpenseChartCard(pieData: PieData?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.analytics_expenses_by_category), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            if (pieData != null && pieData.entryCount > 0) {
                PieChartComposable(pieData = pieData, modifier = Modifier.fillMaxWidth().height(300.dp))
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.analytics_no_expense_data_period))
                }
            }
        }
    }
}

@Composable
fun IncomeChartCard(pieData: PieData?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.analytics_incomes_by_category), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            if (pieData != null && pieData.entryCount > 0) {
                PieChartComposable(pieData = pieData, modifier = Modifier.fillMaxWidth().height(300.dp))
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.analytics_no_income_data_period))
                }
            }
        }
    }
}