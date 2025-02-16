package com.denizcan.subscriptiontracker.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denizcan.subscriptiontracker.viewmodel.SubscriptionViewModel
import com.denizcan.subscriptiontracker.model.Subscription
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import com.denizcan.subscriptiontracker.viewmodel.PlanHistoryEntry
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
                            modifier = Modifier.padding(16.dp),
                            viewModel = viewModel
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
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
    modifier: Modifier = Modifier,
    viewModel: SubscriptionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    var planHistoryMap by remember { mutableStateOf<Map<String, List<PlanHistoryEntry>>>(emptyMap()) }
    
    // Plan geçmişlerini yükle
    LaunchedEffect(subscriptions) {
        subscriptions.forEach { subscription ->
            viewModel.getPlanHistory(subscription.id).collect { history ->
                planHistoryMap = planHistoryMap + (subscription.id to history)
            }
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
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
                        val now = Date()
                        
                        // Son 6 ayın verilerini hazırla
                        for (i in 0..5) {
                            calendar.time = now
                            calendar.add(Calendar.MONTH, -i)
                            
                            // Bu ay başlangıcı
                            calendar.set(Calendar.DAY_OF_MONTH, 1)
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)
                            val monthStart = calendar.time
                            
                            // Bu ay sonu
                            calendar.add(Calendar.MONTH, 1)
                            calendar.add(Calendar.MILLISECOND, -1)
                            val monthEnd = calendar.time
                            
                            // Bu aydaki toplam harcamayı hesapla
                            var monthlyTotal = 0.0
                            
                            subscriptions.forEach { subscription ->
                                val planHistory = planHistoryMap[subscription.id] ?: emptyList()
                                
                                // Bu ay için geçerli olan planı bul
                                val effectivePlan = planHistory.findLast { entry ->
                                    entry.startDate <= monthEnd && 
                                    (entry.endDate == null || entry.endDate > monthStart)
                                }
                                
                                // Eğer plan bulunduysa fiyatı ekle
                                if (effectivePlan != null) {
                                    monthlyTotal += effectivePlan.price
                                }
                            }
                            
                            entries.add(0, Entry((5 - i).toFloat(), monthlyTotal.toFloat()))
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
    val chartColors = listOf(
        android.graphics.Color.parseColor("#E57373"), // Kırmızı
        android.graphics.Color.parseColor("#81C784"), // Yeşil
        android.graphics.Color.parseColor("#64B5F6"), // Mavi
        android.graphics.Color.parseColor("#BA68C8"), // Mor
        android.graphics.Color.parseColor("#4DB6AC"), // Turkuaz
        android.graphics.Color.parseColor("#FFB74D"), // Turuncu
        android.graphics.Color.parseColor("#90A4AE"), // Gri
        android.graphics.Color.parseColor("#9575CD"), // Mor
        android.graphics.Color.parseColor("#78909C")  // Gri
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Kategori Dağılımı",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AndroidView(
                    factory = { context ->
                        PieChart(context).apply {
                            description.isEnabled = false
                            setUsePercentValues(true)
                            legend.apply {
                                isEnabled = true
                                textColor = android.graphics.Color.WHITE
                                textSize = 12f
                            }
                            setDrawEntryLabels(false)
                            setDrawCenterText(true)
                            centerText = "Kategori\nDağılımı"
                            setCenterTextSize(14f)
                            setCenterTextColor(android.graphics.Color.WHITE)
                            setHoleColor(android.graphics.Color.TRANSPARENT)
                            setTransparentCircleColor(android.graphics.Color.TRANSPARENT)
                            setTransparentCircleAlpha(110)
                            holeRadius = 58f
                            transparentCircleRadius = 61f
                            setDrawCenterText(true)
                            setDrawHoleEnabled(true)
                            setDrawRoundedSlices(true)
                            animateY(1000)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { chart ->
                        // Kategori bazlı harcamaları hesapla
                        val categoryExpenses = subscriptions
                            .groupBy { it.category }
                            .mapValues { (_, subs) -> subs.sumOf { it.price } }
                            .toList()
                            .sortedByDescending { it.second }

                        // Toplam aylık harcama
                        val totalMonthlyExpense = subscriptions.sumOf { it.price }

                        val entries = categoryExpenses.map { (category, amount) ->
                            val percentage = (amount / totalMonthlyExpense) * 100
                            PieEntry(
                                percentage.toFloat(),
                                category.displayName
                            )
                        }

                        val dataSet = PieDataSet(entries, "").apply {
                            setColors(chartColors)
                            valueTextSize = 14f
                            valueTextColor = android.graphics.Color.WHITE
                            valueFormatter = PercentFormatter(chart)
                        }

                        chart.data = PieData(dataSet)
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
    val totalSubscriptions = subscriptions.size
    val totalMonthlyExpense = subscriptions.sumOf { it.price }
    val averageExpense = if (totalSubscriptions > 0) totalMonthlyExpense / totalSubscriptions else 0.0
    val mostExpensiveCategory = subscriptions
        .groupBy { it.category }
        .mapValues { (_, subs) -> subs.sumOf { it.price } }
        .maxByOrNull { it.value }
        ?.key

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "İstatistikler",
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.List,
                    label = "Toplam Üyelik",
                    value = totalSubscriptions.toString(),
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    icon = Icons.Default.Info,
                    label = "Ort. Aylık Maliyet",
                    value = formatCurrency(averageExpense),
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    icon = Icons.Default.CheckCircle,
                    label = "En Yüksek Kategori",
                    value = mostExpensiveCategory?.displayName ?: "-",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
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
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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

@Composable
private fun SettingsSection(
    onLogoutClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Ayarlar",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // ... existing code ...
        }
    }
} 