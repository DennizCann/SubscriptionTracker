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
                    contentAlignment = androidx.compose.ui.Alignment.Center
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
                        CategoryDistributionCard(
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
fun CategoryDistributionCard(
    subscriptions: List<Subscription>,
    modifier: Modifier = Modifier
) {
    val categoryTotals = subscriptions
        .groupBy { it.category }
        .mapValues { it.value.sumOf { subscription -> subscription.price } }
        .toList()
        .sortedByDescending { it.second }

    val total = subscriptions.sumOf { it.price }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Kategori Dağılımı",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            categoryTotals.forEach { (category, amount) ->
                val percentage = (amount / total * 100).toInt()
                CategoryProgressBar(
                    category = category,
                    amount = amount,
                    percentage = percentage
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CategoryProgressBar(
    category: SubscriptionCategory,
    amount: Double,
    percentage: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${formatCurrency(amount)} (%$percentage)",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        LinearProgressIndicator(
            progress = percentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
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