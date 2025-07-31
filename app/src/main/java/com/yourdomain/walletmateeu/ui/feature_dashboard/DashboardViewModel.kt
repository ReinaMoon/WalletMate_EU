package com.yourdomain.walletmateeu.ui.feature_dashboard

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.yourdomain.walletmateeu.data.local.model.CategoryEntity
import com.yourdomain.walletmateeu.data.local.model.TagEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionWithCategoryAndTags
import com.yourdomain.walletmateeu.data.repository.AppRepository
import com.yourdomain.walletmateeu.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class DashboardUiState(
    val transactions: List<TransactionWithCategoryAndTags> = emptyList(),
    val transactionToEdit: TransactionWithCategoryAndTags? = null,
    val isEditDialogOpen: Boolean = false,
    val allCategories: List<CategoryEntity> = emptyList(),
    val allTags: List<TagEntity> = emptyList(),
    val expenseBarData: BarData? = null,
    val expenseCategoriesForChart: List<String> = emptyList(),
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val balance: Double = 0.0,
    val transactionFilter: String = "ALL",
    val dateFilter: DateFilterType = DateFilterType.THIS_MONTH,
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val isBottomSheetVisible: Boolean = false,
    val isDateRangePickerVisible: Boolean = false,
    val currency: String = "EUR",
    val isSearchActive: Boolean = false,
    val searchQuery: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: AppRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState
                .map { Triple(it.dateFilter, it.transactionFilter, it.searchQuery) }
                .distinctUntilChanged()
                .flatMapLatest { (dateFilterType, transactionFilter, query) ->
                    val (start, end) = getDateRange(dateFilterType, _uiState.value.startDate, _uiState.value.endDate)

                    val transactionsFlow = when {
                        query.isNotBlank() -> repository.searchTransactions(query)
                        dateFilterType == DateFilterType.ALL_TIME -> repository.getAllTransactionsWithCategoryAndTags()
                        else -> repository.getTransactionsWithCategoryAndTagsBetweenDates(start, end)
                    }

                    combine(
                        transactionsFlow,
                        repository.getAllCategories(),
                        repository.getAllTags(),
                        userPreferencesRepository.currency
                    ) { transactions, categories, tags, currency ->
                        // 데이터를 묶어서 하위 스트림으로 전달
                        Triple(transactions, categories, tags) to currency
                    }
                }
                .collect { (data, currency) ->
                    val (transactions, categories, tags) = data
                    // 최종 UI 상태 업데이트
                    updateUiWith(transactions, categories, tags, _uiState.value.transactionFilter, currency)
                }
        }
    }

    private fun updateUiWith(
        transactions: List<TransactionWithCategoryAndTags>,
        categories: List<CategoryEntity>,
        tags: List<TagEntity>,
        transactionFilter: String,
        currency: String
    ) {
        val totalIncome = transactions.filter { it.transaction.type == "INCOME" }.sumOf { it.transaction.amount }
        val totalExpense = transactions.filter { it.transaction.type == "EXPENSE" }.sumOf { it.transaction.amount }
        val balance = totalIncome - totalExpense

        val expenseByCategory = transactions
            .filter { it.transaction.type == "EXPENSE" }
            .groupBy { it.category?.name ?: "Uncategorized" }
            .mapValues { entry -> entry.value.sumOf { it.transaction.amount } }
            .toList()
            .sortedByDescending { (_, value) -> value }
            .take(5)

        val barEntries = expenseByCategory.mapIndexed { index, (_, sum) -> BarEntry(index.toFloat(), sum.toFloat()) }
        val barColors = expenseByCategory.map { (name, _) ->
            categories.find { it.name == name }?.color?.let { try { Color(android.graphics.Color.parseColor(it)).toArgb() } catch (e: Exception) { Color.Gray.toArgb() } } ?: Color.Gray.toArgb()
        }
        val barDataSet = BarDataSet(barEntries, "Expenses").apply {
            colors = barColors
            setDrawValues(true)
            valueTextColor = Color.Black.toArgb()
        }

        val filteredTransactions = when (transactionFilter) {
            "INCOME" -> transactions.filter { it.transaction.type == "INCOME" }
            "EXPENSE" -> transactions.filter { it.transaction.type == "EXPENSE" }
            else -> transactions
        }.sortedByDescending { it.transaction.date }

        _uiState.update { currentState ->
            currentState.copy(
                transactions = filteredTransactions,
                allCategories = categories,
                allTags = tags,
                expenseBarData = if (barEntries.isNotEmpty()) BarData(barDataSet) else null,
                expenseCategoriesForChart = expenseByCategory.map { it.first },
                totalExpense = totalExpense,
                totalIncome = totalIncome,
                balance = balance,
                currency = currency
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onSearchActiveChange(isActive: Boolean) {
        _uiState.update { it.copy(isSearchActive = isActive) }
        if (!isActive) {
            onSearchQueryChange("")
        }
    }

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
        _uiState.update { it.copy(dateFilter = DateFilterType.CUSTOM, startDate = startDate, endDate = endDate, isDateRangePickerVisible = false) }
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
        viewModelScope.launch {
            repository.updateTransaction(transaction, tags)
            onDismissEditDialog()
        }
    }

    fun onDeleteTransaction(transactionId: String) {
        viewModelScope.launch {
            repository.deleteTransactionById(transactionId)
            onDismissEditDialog()
        }
    }

    private fun getDateRange(filterType: DateFilterType, customStart: Long = 0L, customEnd: Long = 0L): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val todayStart = calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        return when (filterType) {
            DateFilterType.TODAY -> todayStart to calendar.apply { timeInMillis = todayStart; add(Calendar.DAY_OF_YEAR, 1); add(Calendar.MILLISECOND, -1) }.timeInMillis
            DateFilterType.THIS_WEEK -> { val weekStart = calendar.apply { timeInMillis = todayStart; set(Calendar.DAY_OF_WEEK, firstDayOfWeek) }.timeInMillis; weekStart to calendar.apply { timeInMillis = weekStart; add(Calendar.WEEK_OF_YEAR, 1); add(Calendar.MILLISECOND, -1) }.timeInMillis }
            DateFilterType.THIS_MONTH -> { val monthStart = calendar.apply { timeInMillis = todayStart; set(Calendar.DAY_OF_MONTH, 1) }.timeInMillis; monthStart to calendar.apply { timeInMillis = monthStart; add(Calendar.MONTH, 1); add(Calendar.MILLISECOND, -1) }.timeInMillis }
            DateFilterType.THIS_YEAR -> { val yearStart = calendar.apply { timeInMillis = todayStart; set(Calendar.DAY_OF_YEAR, 1) }.timeInMillis; yearStart to calendar.apply { timeInMillis = yearStart; add(Calendar.YEAR, 1); add(Calendar.MILLISECOND, -1) }.timeInMillis }
            DateFilterType.ALL_TIME -> 0L to Long.MAX_VALUE
            DateFilterType.CUSTOM -> customStart to customEnd
        }
    }
}