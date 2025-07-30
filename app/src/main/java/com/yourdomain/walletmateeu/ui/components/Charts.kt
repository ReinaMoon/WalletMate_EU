package com.yourdomain.walletmateeu.ui.components

import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

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
                setValueTextColor(Color.White.toArgb()) // 흰색으로 변경하여 가독성 확보
            }
            chart.invalidate()
        },
        modifier = modifier
    )
}

@Composable
fun LineChartComposable(lineData: LineData, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.setDrawGridLines(false)
                axisRight.isEnabled = false
                axisLeft.setDrawGridLines(true)
                axisLeft.axisMinimum = 0f
                legend.isEnabled = true
                setTouchEnabled(true)
                setPinchZoom(true)
            }
        },
        update = { chart ->
            chart.data = lineData
            chart.xAxis.valueFormatter = object : ValueFormatter() {
                private val format = SimpleDateFormat("M/d", Locale.getDefault())
                override fun getFormattedValue(value: Float): String {
                    return format.format(Date(value.toLong()))
                }
            }
            chart.invalidate()
        },
        modifier = modifier
    )
}