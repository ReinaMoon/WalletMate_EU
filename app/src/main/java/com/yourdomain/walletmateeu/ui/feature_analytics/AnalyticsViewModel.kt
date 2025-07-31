package com.yourdomain.walletmateeu.ui.feature_analytics

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.*
import com.yourdomain.walletmateeu.data.local.model.TagEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionWithCategoryAndTags
import com.yourdomain.walletmateeu.data.repository.AppRepository
import com.yourdomain.walletmateeu.data.repository.UserPreferencesRepository
import com.yourdomain.walletmateeu.ui.feature_dashboard.DateFilterType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class TagAnalysis(val tag: TagEntity, val totalAmount: Double, val transactionCount: Int)
data class AnalyticsUiState(
    val expensePieData: PieData? = null,
    val incomePieData: PieData? = null,
    val trendChartData: LineData? = null,
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val tagAnalysisList: List<TagAnalysis> = emptyList(),
    val selectedTabIndex: Int = 0,
    val dateFilter: DateFilterType = DateFilterType.THIS_MONTH,
    val isFilterSheetVisible: Boolean = false,
    val isDateRangePickerVisible: Boolean = false,
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val currency: String = "EUR" // 통화 상태 추가
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: AppRepository,
    private val userPreferencesRepository: UserPreferencesRepository // 이 줄을 추가하세요.
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    private val _dateFilterState = MutableStateFlow(Pair(DateFilterType.THIS_MONTH, 0L to 0L))

    val uiState: StateFlow<AnalyticsUiState> = combine(
        _dateFilterState.flatMapLatest { (filterType, dateRange) ->
            val (start, end) = if (filterType == DateFilterType.CUSTOM) dateRange else getDateRange(filterType)
            if (filterType == DateFilterType.ALL_TIME) {
                repository.getAllTransactionsWithCategoryAndTags()
            } else {
                repository.getTransactionsWithCategoryAndTagsBetweenDates(start, end)
            }
        },
        repository.getAllTags(),
        userPreferencesRepository.currency,
        _uiState
    ) { transactions, tags, currency, currentState ->
        val expenses = transactions.filter { it.transaction.type == "EXPENSE" }
        val incomes = transactions.filter { it.transaction.type == "INCOME" }
        val tagAnalysis = tags.map { tag ->
            val relatedTransactions = transactions.filter { it.tags.contains(tag) }
            TagAnalysis(
                tag = tag,
                totalAmount = relatedTransactions.sumOf { it.transaction.amount },
                transactionCount = relatedTransactions.size
            )
        }.filter { it.transactionCount > 0 }.sortedByDescending { it.totalAmount }

        currentState.copy(
            totalExpense = expenses.sumOf { it.transaction.amount },
            totalIncome = incomes.sumOf { it.transaction.amount },
            expensePieData = createPieData(expenses),
            incomePieData = createPieData(incomes),
            trendChartData = createLineData(transactions),
            tagAnalysisList = tagAnalysis,
            currency = currency
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
    )

    // 이하 이벤트 핸들러 및 helper 함수들은 이전 답변과 동일 (생략하지 않음)
    fun onTabSelected(index: Int) { _uiState.update { it.copy(selectedTabIndex = index) } }
    fun onDateFilterChanged(filterType: DateFilterType) { if (filterType == DateFilterType.CUSTOM) { _uiState.update { it.copy(isFilterSheetVisible = false, isDateRangePickerVisible = true) } } else { _dateFilterState.value = Pair(filterType, 0L to 0L); _uiState.update { it.copy(dateFilter = filterType, isFilterSheetVisible = false) } } }
    fun onCustomDateRangeSelected(startDate: Long, endDate: Long) { _dateFilterState.value = Pair(DateFilterType.CUSTOM, startDate to endDate); _uiState.update { it.copy(dateFilter = DateFilterType.CUSTOM, startDate = startDate, endDate = endDate, isDateRangePickerVisible = false) } }
    fun showFilterBottomSheet(show: Boolean) { _uiState.update { it.copy(isFilterSheetVisible = show) } }
    fun showDateRangePicker(show: Boolean) { _uiState.update { it.copy(isDateRangePickerVisible = show) } }
    private fun createPieData(transactions: List<TransactionWithCategoryAndTags>): PieData? { if (transactions.isEmpty()) return null; val grouped = transactions.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.transaction.amount } }; val entries = grouped.map { (category, sum) -> PieEntry(sum.toFloat(), category?.name ?: "Uncategorized") }; val colors = grouped.keys.map { category -> category?.color?.let { try { Color(android.graphics.Color.parseColor(it)).toArgb() } catch (e: Exception) { Color.Gray.toArgb() } } ?: Color.Gray.toArgb() }; val dataSet = PieDataSet(entries, "").apply { this.colors = colors; valueTextColor = Color.White.toArgb() }; return PieData(dataSet) }
    private fun createLineData(transactions: List<TransactionWithCategoryAndTags>): LineData? { if (transactions.isEmpty()) return null; val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); val groupedByDay = transactions.groupBy { sdf.format(Date(it.transaction.date)) }; val expenseEntries = mutableListOf<Entry>(); val incomeEntries = mutableListOf<Entry>(); groupedByDay.keys.sorted().forEach { dateString -> val date = sdf.parse(dateString)?.time ?: 0L; val dayTransactions = groupedByDay[dateString] ?: emptyList(); val dailyExpense = dayTransactions.filter { it.transaction.type == "EXPENSE" }.sumOf { it.transaction.amount }.toFloat(); val dailyIncome = dayTransactions.filter { it.transaction.type == "INCOME" }.sumOf { it.transaction.amount }.toFloat(); if (dailyExpense > 0) expenseEntries.add(Entry(date.toFloat(), dailyExpense)); if (dailyIncome > 0) incomeEntries.add(Entry(date.toFloat(), dailyIncome)) }; val expenseDataSet = LineDataSet(expenseEntries, "Expense").apply { color = Color.Red.toArgb(); valueTextColor = Color.Red.toArgb(); setCircleColor(Color.Red.toArgb()) }; val incomeDataSet = LineDataSet(incomeEntries, "Income").apply { color = Color(0xFF3871C1).toArgb(); valueTextColor = Color(0xFF3871C1).toArgb(); setCircleColor(Color(0xFF3871C1).toArgb()) }; return LineData(incomeDataSet, expenseDataSet) }
    private fun getDateRange(filterType: DateFilterType): Pair<Long, Long> { val calendar = Calendar.getInstance(); val todayStart = calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis; return when (filterType) { DateFilterType.TODAY -> todayStart to calendar.apply { timeInMillis = todayStart; add(Calendar.DAY_OF_YEAR, 1); add(Calendar.MILLISECOND, -1) }.timeInMillis; DateFilterType.THIS_WEEK -> { val weekStart = calendar.apply { timeInMillis = todayStart; set(Calendar.DAY_OF_WEEK, firstDayOfWeek) }.timeInMillis; weekStart to calendar.apply { timeInMillis = weekStart; add(Calendar.WEEK_OF_YEAR, 1); add(Calendar.MILLISECOND, -1) }.timeInMillis }; DateFilterType.THIS_MONTH -> { val monthStart = calendar.apply { timeInMillis = todayStart; set(Calendar.DAY_OF_MONTH, 1) }.timeInMillis; monthStart to calendar.apply { timeInMillis = monthStart; add(Calendar.MONTH, 1); add(Calendar.MILLISECOND, -1) }.timeInMillis }; DateFilterType.THIS_YEAR -> { val yearStart = calendar.apply { timeInMillis = todayStart; set(Calendar.DAY_OF_YEAR, 1) }.timeInMillis; yearStart to calendar.apply { timeInMillis = yearStart; add(Calendar.YEAR, 1); add(Calendar.MILLISECOND, -1) }.timeInMillis }; DateFilterType.ALL_TIME -> 0L to Long.MAX_VALUE; DateFilterType.CUSTOM -> throw IllegalArgumentException("Custom date range should be handled separately") } }
}