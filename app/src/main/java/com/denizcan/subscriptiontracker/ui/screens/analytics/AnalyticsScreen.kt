package com.denizcan.subscriptiontracker.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denizcan.subscriptiontracker.viewmodel.SubscriptionViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import com.denizcan.subscriptiontracker.model.Subscription
import com.denizcan.subscriptiontracker.model.SubscriptionCategory
import com.denizcan.subscriptiontracker.viewmodel.SubscriptionState
import java.text.NumberFormat
import java.util.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: SubscriptionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val subscriptionState by viewModel.subscriptionState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analiz") }
            )
        }
    ) { padding ->
        when (val state = subscriptionState) {
            is SubscriptionState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is SubscriptionState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        MonthlyExpenseCard(
                            subscriptions = state.subscriptions,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    item {
                        MonthlyTrendCard(
                            subscriptions = state.subscriptions,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    item {
                        CategoryPieChartCard(
                            subscriptions = state.subscriptions,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    item {
                        SubscriptionStatsCard(
                            subscriptions = state.subscriptions,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    item {
                        FutureExpenseCard(
                            subscriptions = state.subscriptions,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    item {
                        TopSubscriptionsCard(
                            subscriptions = state.subscriptions,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            else -> {
                Text("Bir hata oluştu")
            }
        }
    }
}

@Composable
fun MonthlyExpenseCard(
    subscriptions: List<Subscription>,
    modifier: Modifier = Modifier
) {
    val monthlyTotal = subscriptions.sumOf { it.price }
    val yearlyTotal = monthlyTotal * 12

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Harcama Özeti",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Aylık Toplam",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatCurrency(monthlyTotal),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Column {
                    Text(
                        text = "Yıllık Toplam",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatCurrency(yearlyTotal),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyTrendCard(
    subscriptions: List<Subscription>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Aylık Trend",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AndroidView(
                    factory = { context ->
                        LineChart(context).apply {
                            description.isEnabled = false
                            setTouchEnabled(true)
                            isDragEnabled = true
                            setScaleEnabled(false)
                            setPinchZoom(false)
                            legend.isEnabled = false
                            axisRight.isEnabled = false
                            axisLeft.apply {
                                setDrawGridLines(false)
                                setDrawAxisLine(true)
                                textColor = primaryColor
                            }
                            xAxis.apply {
                                setDrawGridLines(false)
                                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                                textColor = primaryColor
                                setDrawAxisLine(true)
                            }
                            setDrawBorders(false)
                            setDrawGridBackground(false)
                            setViewPortOffsets(50f, 20f, 30f, 50f)
                            animateX(1000)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { chart ->
                        val entries = ArrayList<Entry>()
                        val calendar = Calendar.getInstance()
                        val monthlyTotals = mutableMapOf<Int, Float>()
                        
                        // Son 6 ayın verilerini hazırla
                        for (i in 0..5) {
                            calendar.time = Date()
                            calendar.add(Calendar.MONTH, -i)
                            val monthlyTotal = subscriptions.sumOf { it.price }.toFloat()
                            monthlyTotals[5 - i] = monthlyTotal
                        }

                        // Sıralı bir şekilde Entry'leri oluştur
                        monthlyTotals.entries.sortedBy { it.key }.forEach { (month, total) ->
                            entries.add(Entry(month.toFloat(), total))
                        }

                        val dataSet = LineDataSet(entries, "Aylık Harcama").apply {
                            color = primaryColor
                            setCircleColor(primaryColor)
                            lineWidth = 2f
                            circleRadius = 4f
                            setDrawValues(true)
                            valueTextSize = 10f
                            valueTextColor = primaryColor
                            mode = LineDataSet.Mode.CUBIC_BEZIER
                            cubicIntensity = 0.2f
                            setDrawFilled(true)
                            fillColor = primaryColor
                            fillAlpha = 30
                            setDrawCircles(true)
                            setDrawCircleHole(true)
                            circleHoleRadius = 2f
                        }

                        val lineData = LineData(dataSet)
                        chart.data = lineData
                        chart.invalidate()
                    }
                )
            }
        }
    }
}

@Composable
fun CategoryPieChartCard(
    subscriptions: List<Subscription>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val chartColors = listOf(
        android.graphics.Color.parseColor("#2196F3"),  // Mavi
        android.graphics.Color.parseColor("#4CAF50"),  // Yeşil
        android.graphics.Color.parseColor("#FFC107"),  // Sarı
        android.graphics.Color.parseColor("#E91E63"),  // Pembe
        android.graphics.Color.parseColor("#9C27B0"),  // Mor
        android.graphics.Color.parseColor("#FF5722"),  // Turuncu
        android.graphics.Color.parseColor("#795548"),  // Kahverengi
        android.graphics.Color.parseColor("#607D8B")   // Gri
    )
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Kategori Dağılımı",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp)
            ) {
                AndroidView(
                    factory = { context ->
                        PieChart(context).apply {
                            description.isEnabled = false
                            isDrawHoleEnabled = true
                            holeRadius = 30f
                            setHoleColor(android.graphics.Color.TRANSPARENT)
                            transparentCircleRadius = 35f
                            setDrawCenterText(false)
                            legend.apply {
                                isEnabled = true
                                textSize = 9f
                                formSize = 9f
                                form = com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE
                                verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                                horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                                orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL
                                setDrawInside(false)
                                xEntrySpace = 6f
                                yEntrySpace = 2f
                                textColor = android.graphics.Color.BLACK
                                maxSizePercent = 1f
                            }
                            setDrawEntryLabels(false)
                            setUsePercentValues(true)
                            setExtraOffsets(24f, 24f, 24f, 32f)
                            minOffset = 0f
                            rotationAngle = 0f
                            isRotationEnabled = true
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { chart ->
                        // Aktif abonelikleri filtrele
                        val activeSubscriptions = subscriptions.filter { it.isActive }
                        
                        // Toplam aylık masrafı hesapla
                        val totalMonthlyExpense = activeSubscriptions.sumOf { it.price }
                        
                        // Kategorilere göre topla ve yüzdeleri hesapla
                        val categoryTotals = activeSubscriptions
                            .groupBy { it.category }
                            .mapValues { (_, subs) -> subs.sumOf { it.price } }
                            .toList()
                            .sortedByDescending { it.second }

                        val entries = categoryTotals.map { (category, amount) ->
                            val percentage = (amount / totalMonthlyExpense) * 100
                            PieEntry(
                                percentage.toFloat(),
                                "${formatCurrency(amount)}\n${category.displayName.take(20)}"
                            )
                        }

                        val dataSet = PieDataSet(entries, "").apply {
                            colors = chartColors.take(entries.size)
                            valueTextSize = 10f
                            valueTextColor = android.graphics.Color.BLACK
                            valueFormatter = PercentFormatter()
                            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                            valueLineColor = android.graphics.Color.BLACK
                            valueLinePart1Length = 0.3f
                            valueLinePart2Length = 0.5f
                            valueLineWidth = 1.5f
                            sliceSpace = 3f
                        }

                        val pieData = PieData(dataSet).apply {
                            setValueTextSize(10f)
                            setValueTextColor(android.graphics.Color.BLACK)
                            setValueFormatter(PercentFormatter())
                        }

                        chart.data = pieData
                        chart.invalidate()
                    }
                )
            }
        }
    }
}

@Composable
fun SubscriptionStatsCard(
    subscriptions: List<Subscription>,
    modifier: Modifier = Modifier
) {
    val activeSubscriptions = subscriptions.count { it.isActive }
    val totalSubscriptions = subscriptions.size
    val averagePrice = if (subscriptions.isNotEmpty()) {
        subscriptions.sumOf { it.price } / subscriptions.size
    } else 0.0

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Abonelik İstatistikleri",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    value = activeSubscriptions.toString(),
                    label = "Aktif"
                )
                StatItem(
                    icon = Icons.Default.List,
                    value = totalSubscriptions.toString(),
                    label = "Toplam"
                )
                StatItem(
                    icon = Icons.Default.Info,
                    value = formatCurrency(averagePrice),
                    label = "Ortalama"
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun FutureExpenseCard(
    subscriptions: List<Subscription>,
    modifier: Modifier = Modifier
) {
    val monthlyTotal = subscriptions.sumOf { it.price }
    val nextMonthEstimate = monthlyTotal * 1.1 // %10 artış tahmini

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Gelecek Ay Tahmini",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tahmini Harcama",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatCurrency(nextMonthEstimate),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Card(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = "+10%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun TopSubscriptionsCard(
    subscriptions: List<Subscription>,
    modifier: Modifier = Modifier
) {
    val topSubscriptions = subscriptions
        .sortedByDescending { it.price }
        .take(5)

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "En Yüksek Abonelikler",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            topSubscriptions.forEach { subscription ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = subscription.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatCurrency(subscription.price),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("tr", "TR")).format(amount)
} 