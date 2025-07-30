package com.yourdomain.walletmateeu.ui.feature_dashboard

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.yourdomain.walletmateeu.data.local.model.CategoryEntity
import com.yourdomain.walletmateeu.data.local.model.TagEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionWithCategoryAndTags
import com.yourdomain.walletmateeu.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

// --- UI 상태 및 데이터 클래스 추가 ---
enum class DateFilterType(val displayName: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    THIS_YEAR("This Year"),
    ALL_TIME("All Time"),
    CUSTOM("Custom")
}

data class DashboardUiState(
    val transactions: List<TransactionWithCategoryAndTags> = emptyList(),
    val transactionToEdit: TransactionWithCategoryAndTags? = null,
    val isEditDialogOpen: Boolean = false,
    val allCategories: List<CategoryEntity> = emptyList(),
    val allTags: List<TagEntity> = emptyList(),
    val pieChartData: PieData? = null,
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val balance: Double = 0.0,
    val transactionFilter: String = "ALL", // "ALL", "INCOME", "EXPENSE"
    val dateFilter: DateFilterType = DateFilterType.THIS_MONTH, // 기간 필터 상태
    val startDate: Long = 0L, // 사용자 지정 시작일
    val endDate: Long = 0L,   // 사용자 지정 종료일
    val isBottomSheetVisible: Boolean = false, // 필터 메뉴(바텀 시트) 표시 여부
    val isDateRangePickerVisible: Boolean = false // 날짜 범위 선택기 표시 여부
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // 기간 필터 또는 거래 필터가 변경될 때마다 데이터 스트림을 다시 시작
        viewModelScope.launch {
            _uiState.map { Pair(it.dateFilter, it.transactionFilter) }
                .distinctUntilChanged()
                .flatMapLatest { (dateFilter, transactionFilter) ->
                    val (start, end) = getDateRange(dateFilter, _uiState.value.startDate, _uiState.value.endDate)

                    val transactionsFlow = if (dateFilter == DateFilterType.ALL_TIME) {
                        repository.getAllTransactionsWithCategoryAndTags()
                    } else {
                        repository.getTransactionsWithCategoryAndTagsBetweenDates(start, end)
                    }

                    // 거래, 카테고리, 태그 데이터를 결합
                    combine(transactionsFlow, repository.getAllCategories(), repository.getAllTags()) { transactions, categories, tags ->
                        Triple(transactions, categories, tags)
                    }
                }.collect { (transactions, categories, tags) ->
                    updateUiWith(transactions, categories, tags)
                }
        }
    }

    private fun updateUiWith(transactions: List<TransactionWithCategoryAndTags>, categories: List<CategoryEntity>, tags: List<TagEntity>) {
        val totalIncome = transactions.filter { it.transaction.type == "INCOME" }.sumOf { it.transaction.amount }
        val totalExpense = transactions.filter { it.transaction.type == "EXPENSE" }.sumOf { it.transaction.amount }
        val balance = totalIncome - totalExpense

        // 파이 차트 데이터 (선택된 기간의 지출만으로 계산)
        val expenseByCategory = transactions
            .filter { it.transaction.type == "EXPENSE" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.transaction.amount } }
        val pieEntries = expenseByCategory.map { (category, sum) -> PieEntry(sum.toFloat(), category?.name ?: "Uncategorized") }
        val pieColors = expenseByCategory.keys.map { category -> category?.color?.let { try { Color(android.graphics.Color.parseColor(it)).toArgb() } catch (e: Exception) { Color.Gray.toArgb() } } ?: Color.Gray.toArgb() }
        val pieDataSet = PieDataSet(pieEntries, "").apply { colors = pieColors; valueTextColor = Color.Black.toArgb(); valueTextSize = 12f }
        val pieData = if (pieEntries.isNotEmpty()) PieData(pieDataSet) else null

        // 거래 타입 필터 적용
        val filteredTransactions = when (_uiState.value.transactionFilter) {
            "INCOME" -> transactions.filter { it.transaction.type == "INCOME" }
            "EXPENSE" -> transactions.filter { it.transaction.type == "EXPENSE" }
            else -> transactions
        }

        _uiState.update { currentState ->
            currentState.copy(
                transactions = filteredTransactions,
                allCategories = categories,
                allTags = tags,
                pieChartData = pieData,
                totalExpense = totalExpense,
                totalIncome = totalIncome,
                balance = balance
            )
        }
    }

    // --- 이벤트 핸들러 ---
    fun onTransactionFilterChanged(filter: String) {
        _uiState.update { it.copy(transactionFilter = filter) }
    }

    fun onDateFilterChanged(filterType: DateFilterType) {
        if (filterType == DateFilterType.CUSTOM) {
            _uiState.update { it.copy(isBottomSheetVisible = false, isDateRangePickerVisible = true) }
        } else {
            _uiState.update { it.copy(dateFilter = filterType, isBottomSheetVisible = false) }
        }
    }

    fun onCustomDateRangeSelected(startDate: Long, endDate: Long) {
        _uiState.update {
            it.copy(
                dateFilter = DateFilterType.CUSTOM,
                startDate = startDate,
                endDate = endDate,
                isDateRangePickerVisible = false
            )
        }
    }

    fun showFilterBottomSheet(show: Boolean) {
        _uiState.update { it.copy(isBottomSheetVisible = show) }
    }

    fun showDateRangePicker(show: Boolean) {
        _uiState.update { it.copy(isDateRangePickerVisible = show) }
    }

    fun onTransactionClick(transaction: TransactionWithCategoryAndTags) {
        _uiState.update { it.copy(transactionToEdit = transaction, isEditDialogOpen = true) }
    }

    fun onDismissEditDialog() {
        _uiState.update { it.copy(isEditDialogOpen = false, transactionToEdit = null) }
    }

    fun onUpdateTransaction(transaction: TransactionEntity, tags: List<TagEntity>) {
        viewModelScope.launch { repository.updateTransaction(transaction, tags); onDismissEditDialog() }
    }

    fun onDeleteTransaction(transactionId: String) {
        viewModelScope.launch { repository.deleteTransactionById(transactionId); onDismissEditDialog() }
    }

    // 날짜 범위를 계산하는 헬퍼 함수
    private fun getDateRange(filterType: DateFilterType, customStart: Long, customEnd: Long): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val todayStart = calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        val todayEnd = calendar.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }.timeInMillis

        return when (filterType) {
            DateFilterType.TODAY -> todayStart to todayEnd
            DateFilterType.THIS_WEEK -> {
                calendar.timeInMillis = todayStart
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val weekStart = calendar.timeInMillis
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                val weekEnd = calendar.timeInMillis
                weekStart to weekEnd
            }
            DateFilterType.THIS_MONTH -> {
                calendar.timeInMillis = todayStart
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val monthStart = calendar.timeInMillis
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                val monthEnd = calendar.timeInMillis
                monthStart to monthEnd
            }
            DateFilterType.THIS_YEAR -> {
                calendar.timeInMillis = todayStart
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                val yearStart = calendar.timeInMillis
                calendar.add(Calendar.YEAR, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                val yearEnd = calendar.timeInMillis
                yearStart to yearEnd
            }
            DateFilterType.ALL_TIME -> 0L to Long.MAX_VALUE
            DateFilterType.CUSTOM -> customStart to customEnd
        }
    }
}