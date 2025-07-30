package com.yourdomain.walletmateeu.ui.feature_analytics

import android.graphics.Typeface
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.formatter.PercentFormatter

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Monthly Expenses", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = String.format("Total: %.2f EUR", uiState.totalExpense),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 파이 차트를 표시할 공간
                if (uiState.pieChartData != null && uiState.pieChartData!!.entryCount > 0) {
                    PieChartComposable(
                        pieData = uiState.pieChartData!!,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                } else {
                    Text("No expense data for this month.")
                }
            }
        }
    }
}

// MPAndroidChart의 PieChart를 Compose에서 사용할 수 있도록 래핑하는 Composable
@Composable
fun PieChartComposable(pieData: PieData, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                // 차트 기본 설정
                description.isEnabled = false
                isDrawHoleEnabled = true
                holeRadius = 58f
                transparentCircleRadius = 61f
                setUsePercentValues(true)
                setEntryLabelColor(Color.Black.toArgb())
                setEntryLabelTypeface(Typeface.DEFAULT_BOLD)
                setEntryLabelTextSize(12f)
                legend.isEnabled = false // 범례는 따로 Compose로 만드는 것이 더 예쁨
            }
        },
        update = { chart ->
            // 데이터가 업데이트될 때마다 차트를 다시 그림
            chart.data = pieData.apply {
                setValueFormatter(PercentFormatter(chart))
                setValueTextSize(12f)
                setValueTextColor(Color.Black.toArgb())
            }
            chart.invalidate() // 차트 새로고침
        },
        modifier = modifier
    )
}