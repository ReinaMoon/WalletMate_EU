package com.yourdomain.walletmateeu.ui.feature_analytics

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.yourdomain.walletmateeu.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AnalyticsUiState(
    val pieChartData: PieData? = null,
    val totalExpense: Double = 0.0
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    val uiState: StateFlow<AnalyticsUiState> = repository.getAllTransactionsWithCategoryAndTags() // <<--- 이름 수정
        .map { transactions ->
            val currentMonthExpenses = transactions.filter {
                it.transaction.type == "EXPENSE"
            }

            val expenseByCategory = currentMonthExpenses
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.transaction.amount } }

            val entries = expenseByCategory.map { (category, sum) ->
                PieEntry(sum.toFloat(), category?.name ?: "Uncategorized")
            }

            val colors = expenseByCategory.keys.map { category ->
                category?.color?.let { try { Color(android.graphics.Color.parseColor(it)).toArgb() } catch (e: Exception) { Color.Gray.toArgb() } } ?: Color.Gray.toArgb()
            }

            val dataSet = PieDataSet(entries, "Expenses by Category").apply {
                this.colors = colors
                valueTextSize = 12f
            }

            val total = currentMonthExpenses.sumOf { it.transaction.amount }

            AnalyticsUiState(
                pieChartData = if (entries.isNotEmpty()) PieData(dataSet) else null,
                totalExpense = total
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AnalyticsUiState()
        )
}