package com.denizcan.subscriptiontracker.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denizcan.subscriptiontracker.model.Subscription
import com.denizcan.subscriptiontracker.model.PaymentPeriod
import com.denizcan.subscriptiontracker.viewmodel.SubscriptionState
import com.denizcan.subscriptiontracker.viewmodel.SubscriptionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: SubscriptionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val subscriptionState by viewModel.subscriptionState.collectAsState()
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Ay seçici ve takvim (sabit kısım)
        Column(modifier = Modifier.weight(1f)) {
            // Ay seçici
            MonthSelector(
                currentMonth = currentMonth,
                onPreviousMonth = {
                    currentMonth = (currentMonth.clone() as Calendar).apply {
                        add(Calendar.MONTH, -1)
                    }
                },
                onNextMonth = {
                    currentMonth = (currentMonth.clone() as Calendar).apply {
                        add(Calendar.MONTH, 1)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Takvim grid'i
            MonthCalendar(
                calendar = currentMonth,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                subscriptions = when (val state = subscriptionState) {
                    is SubscriptionState.Success -> state.subscriptions
                    else -> emptyList()
                }
            )
        }
        
        // Seçili gündeki ödemeler (kaydırılabilir kısım)
        if (selectedDate != null) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    Text(
                        text = "Ödemeler - ${SimpleDateFormat("d MMMM", Locale("tr")).format(selectedDate!!)}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                val paymentsForDate = when (val state = subscriptionState) {
                    is SubscriptionState.Success -> {
                        state.subscriptions.filter { subscription ->
                            // Önce başlangıç tarihini kontrol et
                            if (selectedDate!!.before(subscription.startDate)) {
                                return@filter false
                            }

                            val paymentCal = Calendar.getInstance().apply { time = subscription.nextPaymentDate }
                            val selectedCal = Calendar.getInstance().apply { time = selectedDate!! }
                            val startDate = Calendar.getInstance().apply { time = subscription.startDate }
                            
                            when (subscription.paymentPeriod) {
                                PaymentPeriod.MONTHLY -> {
                                    selectedCal.get(Calendar.DAY_OF_MONTH) == paymentCal.get(Calendar.DAY_OF_MONTH)
                                }
                                PaymentPeriod.QUARTERLY -> {
                                    val monthDiff = (selectedCal.get(Calendar.YEAR) - startDate.get(Calendar.YEAR)) * 12 +
                                            (selectedCal.get(Calendar.MONTH) - startDate.get(Calendar.MONTH))
                                    
                                    selectedCal.get(Calendar.DAY_OF_MONTH) == paymentCal.get(Calendar.DAY_OF_MONTH) &&
                                            monthDiff >= 0 && monthDiff % 3 == 0
                                }
                                PaymentPeriod.YEARLY -> {
                                    selectedCal.get(Calendar.MONTH) == paymentCal.get(Calendar.MONTH) &&
                                            selectedCal.get(Calendar.DAY_OF_MONTH) == paymentCal.get(Calendar.DAY_OF_MONTH) &&
                                            selectedCal.get(Calendar.YEAR) >= startDate.get(Calendar.YEAR)
                                }
                            }
                        }
                    }
                    else -> emptyList()
                }

                if (paymentsForDate.isEmpty()) {
                    item {
                        Text(
                            text = "Bu tarihte ödeme bulunmuyor",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(paymentsForDate) { subscription ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = subscription.name,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = subscription.plan,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "₺${subscription.price}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthSelector(
    currentMonth: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Text("←")
        }
        
        Text(
            text = SimpleDateFormat("MMMM yyyy", Locale("tr")).format(currentMonth.time),
            style = MaterialTheme.typography.titleLarge
        )
        
        IconButton(onClick = onNextMonth) {
            Text("→")
        }
    }
}

@Composable
fun MonthCalendar(
    calendar: Calendar,
    selectedDate: Date?,
    onDateSelected: (Date) -> Unit,
    subscriptions: List<Subscription>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Haftanın günleri başlığı
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Takvim grid'i
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Ayın ilk gününü hesapla
            val firstDayOfMonth = (calendar.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)
            
            // Boş günler
            items((if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2)) { _ ->
                Box(modifier = Modifier.aspectRatio(1f))
            }

            // Ayın günleri
            items(calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) { day ->
                val date = calendar.clone() as Calendar
                date.set(Calendar.DAY_OF_MONTH, day + 1)
                
                val hasPayment = subscriptions.any { subscription ->
                    // Önce başlangıç tarihini kontrol et
                    if (date.time.before(subscription.startDate)) {
                        return@any false
                    }

                    when (subscription.paymentPeriod) {
                        PaymentPeriod.MONTHLY -> {
                            val paymentDay = Calendar.getInstance().apply { 
                                time = subscription.nextPaymentDate 
                            }.get(Calendar.DAY_OF_MONTH)
                            
                            date.get(Calendar.DAY_OF_MONTH) == paymentDay
                        }
                        PaymentPeriod.QUARTERLY -> {
                            val nextPayment = Calendar.getInstance().apply { 
                                time = subscription.nextPaymentDate 
                            }
                            val startDate = Calendar.getInstance().apply {
                                time = subscription.startDate
                            }
                            val monthDiff = (date.get(Calendar.YEAR) - startDate.get(Calendar.YEAR)) * 12 +
                                    (date.get(Calendar.MONTH) - startDate.get(Calendar.MONTH))
                            
                            date.get(Calendar.DAY_OF_MONTH) == nextPayment.get(Calendar.DAY_OF_MONTH) &&
                                    monthDiff >= 0 && monthDiff % 3 == 0
                        }
                        PaymentPeriod.YEARLY -> {
                            val nextPayment = Calendar.getInstance().apply { 
                                time = subscription.nextPaymentDate 
                            }
                            val startDate = Calendar.getInstance().apply {
                                time = subscription.startDate
                            }
                            
                            date.get(Calendar.MONTH) == nextPayment.get(Calendar.MONTH) &&
                                    date.get(Calendar.DAY_OF_MONTH) == nextPayment.get(Calendar.DAY_OF_MONTH) &&
                                    date.get(Calendar.YEAR) >= startDate.get(Calendar.YEAR)
                        }
                    }
                }

                val isSelected = selectedDate?.let { selected ->
                    val selectedCal = Calendar.getInstance().apply { time = selected }
                    selectedCal.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                    selectedCal.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
                    selectedCal.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)
                } ?: false

                CalendarDay(
                    day = day + 1,
                    hasPayment = hasPayment,
                    isSelected = isSelected,
                    onClick = { onDateSelected(date.time) }
                )
            }
        }
    }
}

@Composable
fun CalendarDay(
    day: Int,
    hasPayment: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    hasPayment -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surface
                },
                shape = MaterialTheme.shapes.small
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    hasPayment -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            if (hasPayment) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        )
                )
            }
        }
    }
} 