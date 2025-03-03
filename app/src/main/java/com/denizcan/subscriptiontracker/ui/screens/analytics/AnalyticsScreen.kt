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
import com.denizcan.subscriptiontracker.ui.theme.LocalSpacing
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
import com.denizcan.subscriptiontracker.model.PaymentPeriod
import com.denizcan.subscriptiontracker.ui.theme.ScreenClass
import com.denizcan.subscriptiontracker.ui.theme.Spacing
import com.denizcan.subscriptiontracker.ui.theme.getScreenClass
import com.denizcan.subscriptiontracker.viewmodel.PlanHistoryEntry
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.Calendar
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: SubscriptionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val subscriptionState by viewModel.subscriptionState.collectAsState()
    val spacing = LocalSpacing.current
    val screenClass = getScreenClass()

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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = subscriptionState) {
                is SubscriptionState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is SubscriptionState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .then(
                                if (screenClass == ScreenClass.COMPACT) {
                                    Modifier.fillMaxWidth()
                                } else {
                                    Modifier.width(600.dp)
                                }
                            )
                            .padding(padding),
                        verticalArrangement = Arrangement.spacedBy(spacing.medium)
                    ) {
                        item {
                            MonthlyExpenseCard(
                                subscriptions = state.subscriptions,
                                modifier = Modifier.padding(horizontal = spacing.large),
                                spacing = spacing,
                                viewModel = viewModel
                            )
                        }

                        item {
                            MonthlyTrendCard(
                                subscriptions = state.subscriptions,
                                modifier = Modifier.padding(horizontal = spacing.large),
                                viewModel = viewModel,
                                spacing = spacing
                            )
                        }

                        item {
                            CategoryPieChartCard(
                                subscriptions = state.subscriptions,
                                modifier = Modifier.padding(horizontal = spacing.large),
                                spacing = spacing,
                                viewModel = viewModel
                            )
                        }

                        item {
                            SubscriptionStatsCard(
                                subscriptions = state.subscriptions,
                                modifier = Modifier.padding(horizontal = spacing.large),
                                spacing = spacing,
                                viewModel = viewModel
                            )
                        }

                        item {
                            TopSubscriptionsCard(
                                subscriptions = state.subscriptions,
                                modifier = Modifier.padding(horizontal = spacing.large),
                                spacing = spacing,
                                viewModel = viewModel
                            )
                        }
                    }
                }
                else -> {
                    Text(
                        text = "Bir hata oluştu",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyExpenseCard(
    subscriptions: List<Subscription>,
    modifier: Modifier = Modifier,
    spacing: Spacing,
    viewModel: SubscriptionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val monthlyAverage = subscriptions.sumOf { subscription ->
        viewModel.calculateMonthlyAmount(subscription)
    }
    
    val yearlyTotal = subscriptions.sumOf { subscription ->
        viewModel.calculateYearlyAmount(subscription)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(spacing.large)
        ) {
            Text(
                text = "Harcama Özeti",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(spacing.medium))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Aylık Ortalama",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatCurrency(monthlyAverage),
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
    viewModel: SubscriptionViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    spacing: Spacing
) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    var planHistoryMap by remember { mutableStateOf<Map<String, List<PlanHistoryEntry>>>(emptyMap()) }
    
    // Son 5 ayın isimlerini al
    val monthNames = remember {
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM", Locale("tr"))
        List(5) { i ->
            val currentMonth = calendar.clone() as Calendar
            currentMonth.add(Calendar.MONTH, -4 + i)
            monthFormat.format(currentMonth.time)
        }
    }
    
    // Plan geçmişlerini yükle
    LaunchedEffect(subscriptions) {
        val newPlanHistoryMap = mutableMapOf<String, List<PlanHistoryEntry>>()
        subscriptions.forEach { subscription ->
            viewModel.getPlanHistory(subscription.id).collect { history ->
                newPlanHistoryMap[subscription.id] = history
            }
        }
        planHistoryMap = newPlanHistoryMap
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(spacing.large)) {
            Text(
                text = "Aylık Trend",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(spacing.medium))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AndroidView(
                    factory = { context ->
                        LineChart(context).apply {
                            description.isEnabled = false
                            legend.isEnabled = false
                            setTouchEnabled(true)
                            setScaleEnabled(false)
                            setDrawGridBackground(false)
                            
                            xAxis.apply {
                                position = XAxis.XAxisPosition.BOTTOM
                                textColor = android.graphics.Color.WHITE
                                setDrawGridLines(false)
                                granularity = 1f
                                valueFormatter = object : ValueFormatter() {
                                    override fun getFormattedValue(value: Float): String {
                                        return monthNames.getOrNull(value.toInt()) ?: ""
                                    }
                                }
                            }
                            
                            axisLeft.apply {
                                textColor = android.graphics.Color.WHITE
                                setDrawGridLines(true)
                                valueFormatter = object : ValueFormatter() {
                                    override fun getFormattedValue(value: Float): String {
                                        return formatCurrency(value.toDouble())
                                    }
                                }
                            }
                            
                            axisRight.isEnabled = false
                            
                            setExtraOffsets(20f, 20f, 20f, 20f)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { chart ->
                        val entries = mutableListOf<Entry>()
                        val calendar = Calendar.getInstance()
                        
                        // Bugünün tarihini al
                        val today = calendar.time
                        
                        // 4 ay öncesine git
                        calendar.add(Calendar.MONTH, -4)
                        
                        // Son 5 ay için veri noktaları oluştur
                        for (i in 0 until 5) {
                            // Ay başı ve sonu tarihlerini ayarla
                            calendar.set(Calendar.DAY_OF_MONTH, 1)
                            val monthStart = calendar.time
                            
                            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                            val monthEnd = calendar.time
                            
                            var monthlyTotal = 0.0
                            
                            // Her üyelik için o aydaki harcamayı hesapla
                            subscriptions.forEach { subscription ->
                                val planHistory = planHistoryMap[subscription.id] ?: emptyList()
                                
                                // Bu ay için geçerli olan planı bul
                                val effectivePlan = planHistory.findLast { entry ->
                                    val entryStart = entry.startDate
                                    val entryEnd = entry.endDate ?: today
                                    
                                    // Plan bu ay içinde aktif mi kontrol et
                                    (entryStart <= monthEnd && entryEnd >= monthStart)
                                }
                                
                                // Eğer plan bulunduysa fiyatı ekle
                                effectivePlan?.let { plan ->
                                    monthlyTotal += when (subscription.paymentPeriod) {
                                        PaymentPeriod.MONTHLY -> plan.price
                                        PaymentPeriod.QUARTERLY -> plan.price / 3.0
                                        PaymentPeriod.YEARLY -> plan.price / 12.0
                                    }
                                }
                            }
                            
                            entries.add(Entry(i.toFloat(), monthlyTotal.toFloat()))
                            calendar.add(Calendar.MONTH, 1)
                        }

                        val dataSet = LineDataSet(entries, "Aylık Harcama").apply {
                            color = primaryColor
                            setCircleColor(primaryColor)
                            lineWidth = 2f
                            circleRadius = 4f
                            setDrawValues(true)
                            valueTextSize = 10f
                            valueTextColor = primaryColor
                            valueFormatter = object : ValueFormatter() {
                                override fun getFormattedValue(value: Float): String {
                                    return formatCurrency(value.toDouble())
                                }
                            }
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
    modifier: Modifier = Modifier,
    spacing: Spacing,
    viewModel: SubscriptionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val categoryAmounts = subscriptions
        .groupBy { it.category }
        .mapValues { (_, subs) ->
            subs.sumOf { subscription ->
                viewModel.calculateMonthlyAmount(subscription)
            }
        }
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
        Column(modifier = Modifier.padding(spacing.large)) {
            Text(
                text = "Kategori Dağılımı",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(spacing.medium))
            
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
                        val entries = categoryAmounts.map { (category, amount) ->
                            PieEntry(
                                amount.toFloat(),
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
    modifier: Modifier = Modifier,
    spacing: Spacing,
    viewModel: SubscriptionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val totalSubscriptions = subscriptions.size
    
    // Kategorilere göre aylık harcamaları hesapla
    val categoryExpenses = subscriptions
        .groupBy { it.category }
        .mapValues { (_, subs) -> 
            subs.sumOf { subscription ->
                viewModel.calculateMonthlyAmount(subscription)
            }
        }
    
    // En çok harcama yapılan kategoriyi ve tutarını bul
    val mostExpensiveCategory = categoryExpenses.maxByOrNull { it.value }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
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
                    icon = Icons.Default.CheckCircle,
                    label = "En Yüksek Kategori",
                    value = mostExpensiveCategory?.key?.displayName ?: "-",
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    icon = Icons.Default.Info,
                    label = "Kategori Maliyeti",
                    value = formatCurrency(mostExpensiveCategory?.value ?: 0.0),
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
    modifier: Modifier = Modifier,
    spacing: Spacing,
    viewModel: SubscriptionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // Abonelikleri aylık maliyetlerine göre sırala
    val sortedSubscriptions = subscriptions
        .sortedByDescending { subscription ->
            viewModel.calculateMonthlyAmount(subscription)
        }
        .take(5)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(spacing.large)
        ) {
            Text(
                text = "En Yüksek Abonelikler",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(spacing.medium))

            sortedSubscriptions.forEach { subscription ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = subscription.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = when(subscription.paymentPeriod) {
                                PaymentPeriod.MONTHLY -> "Aylık"
                                PaymentPeriod.QUARTERLY -> "3 Aylık"
                                PaymentPeriod.YEARLY -> "Yıllık"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = formatCurrency(viewModel.calculateMonthlyAmount(subscription)),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (subscription != sortedSubscriptions.last()) {
                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
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