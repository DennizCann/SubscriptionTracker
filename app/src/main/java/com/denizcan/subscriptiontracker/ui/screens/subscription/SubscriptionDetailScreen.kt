package com.denizcan.subscriptiontracker.ui.screens.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denizcan.subscriptiontracker.model.PaymentPeriod
import com.denizcan.subscriptiontracker.model.SubscriptionCategory
import com.denizcan.subscriptiontracker.viewmodel.PlanHistoryEntry
import com.denizcan.subscriptiontracker.viewmodel.SubscriptionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDetailScreen(
    subscriptionId: String,
    onNavigateBack: () -> Unit,
    viewModel: SubscriptionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val subscription by viewModel.getSubscriptionById(subscriptionId).collectAsState(initial = null)
    var showEditDialog by remember { mutableStateOf(false) }
    var showUpgradePlanDialog by remember { mutableStateOf(false) }
    var upgradedPlan by remember { mutableStateOf("") }
    var upgradedPrice by remember { mutableStateOf("") }
    var editedName by remember { mutableStateOf("") }
    var editedPlan by remember { mutableStateOf("") }
    var editedPrice by remember { mutableStateOf("") }
    var editedCategory by remember { mutableStateOf<SubscriptionCategory?>(null) }
    var editedPaymentPeriod by remember { mutableStateOf<PaymentPeriod?>(null) }
    var editedStartDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var planHistory by remember { mutableStateOf<List<PlanHistoryEntry>>(emptyList()) }
    var totalSpent by remember { mutableStateOf(0.0) }

    LaunchedEffect(subscription) {
        subscription?.let { sub ->
            editedName = sub.name
            editedPlan = sub.plan
            editedPrice = sub.price.toString()
            editedCategory = sub.category
            editedPaymentPeriod = sub.paymentPeriod
            editedStartDate = sub.startDate

            // Plan geçmişini al
            val history = viewModel.getPlanHistory(sub.id)
            planHistory = history

            // Toplam harcamayı hesapla
            var total = 0.0
            val now = Date()
            
            history.forEach { entry ->
                val endDate = entry.endDate ?: now
                val startDate = entry.startDate
                
                // Bu plan dönemindeki ay sayısını hesapla
                val diffInMillis = endDate.time - startDate.time
                val months = (diffInMillis / (1000.0 * 60 * 60 * 24 * 30)).toInt()
                
                // Bu plan dönemindeki toplam harcamayı ekle
                total += entry.price * (months + 1)
            }
            
            totalSpent = total
        }
    }

    if (showDatePicker && editedStartDate != null) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = editedStartDate!!.time
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        editedStartDate = Date(it)
                    }
                    showDatePicker = false
                }) {
                    Text("Tamam")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("İptal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    subscription?.let { sub ->
        if (showUpgradePlanDialog) {
            var selectedUpgradeDate by remember { mutableStateOf(sub.nextPaymentDate) }
            var showUpgradeDatePicker by remember { mutableStateOf(false) }

            if (showUpgradeDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = selectedUpgradeDate.time
                )
                
                DatePickerDialog(
                    onDismissRequest = { showUpgradeDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                selectedUpgradeDate = Date(it)
                            }
                            showUpgradeDatePicker = false
                        }) {
                            Text("Tamam")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showUpgradeDatePicker = false }) {
                            Text("İptal")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            AlertDialog(
                onDismissRequest = { showUpgradePlanDialog = false },
                title = { Text("Planı Yükselt") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Mevcut Plan: ${sub.plan}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Mevcut Fiyat: ${formatCurrency(sub.price)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        OutlinedTextField(
                            value = upgradedPlan,
                            onValueChange = { upgradedPlan = it },
                            label = { Text("Yeni Plan Adı") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = upgradedPrice,
                            onValueChange = { upgradedPrice = it },
                            label = { Text("Yeni Fiyat") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedButton(
                            onClick = { showUpgradeDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Yükseltme Tarihi: ${formatDate(selectedUpgradeDate)}")
                        }

                        Text(
                            text = "Not: Yeni plan ve fiyat seçilen tarihten itibaren geçerli olacaktır.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val newPrice = upgradedPrice.toDoubleOrNull()
                            if (upgradedPlan.isBlank() || newPrice == null) {
                                return@TextButton
                            }

                            viewModel.upgradePlan(
                                subscriptionId = sub.id,
                                newPlan = upgradedPlan,
                                newPrice = newPrice,
                                effectiveDate = selectedUpgradeDate
                            )
                            showUpgradePlanDialog = false
                        }
                    ) {
                        Text("Yükselt")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUpgradePlanDialog = false }) {
                        Text("İptal")
                    }
                }
            )
        }
    }

    if (showEditDialog && subscription != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Üyeliği Düzenle") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Üyelik Adı") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = editedPlan,
                        onValueChange = { editedPlan = it },
                        label = { Text("Plan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = editedPrice,
                        onValueChange = { editedPrice = it },
                        label = { Text("Fiyat") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Kategori", style = MaterialTheme.typography.titleSmall)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(SubscriptionCategory.values()) { category ->
                            FilterChip(
                                selected = category == editedCategory,
                                onClick = { editedCategory = category },
                                label = { Text(category.displayName) }
                            )
                        }
                    }

                    Text("Ödeme Periyodu", style = MaterialTheme.typography.titleSmall)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(PaymentPeriod.values()) { period ->
                            FilterChip(
                                selected = period == editedPaymentPeriod,
                                onClick = { editedPaymentPeriod = period },
                                label = { Text(when(period) {
                                    PaymentPeriod.MONTHLY -> "Aylık"
                                    PaymentPeriod.QUARTERLY -> "3 Aylık"
                                    PaymentPeriod.YEARLY -> "Yıllık"
                                }) }
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Başlangıç Tarihi: ${formatDate(editedStartDate!!)}")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val price = editedPrice.toDoubleOrNull()
                        if (editedName.isBlank() || price == null || editedCategory == null || editedPaymentPeriod == null) {
                            return@TextButton
                        }

                        val updatedSubscription = subscription!!.copy(
                            name = editedName,
                            plan = editedPlan,
                            price = price,
                            category = editedCategory!!,
                            paymentPeriod = editedPaymentPeriod!!,
                            startDate = editedStartDate!!,
                            nextPaymentDate = viewModel.calculateNextPaymentDate(editedStartDate!!, editedPaymentPeriod!!)
                        )
                        viewModel.updateSubscription(updatedSubscription)
                        showEditDialog = false
                    }
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subscription?.name ?: "Üyelik Detayı") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { showUpgradePlanDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Planı Yükselt")
                    }
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Düzenle")
                    }
                }
            )
        }
    ) { padding ->
        if (subscription == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val sub = subscription!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Kategori ve İsim Başlığı
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    getCategoryColor(sub.category),
                                    shape = MaterialTheme.shapes.small
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sub.name.first().toString(),
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Column {
                            Text(
                                text = sub.name,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = sub.category.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Divider()
                }

                // Detay Kartları
                item {
                    DetailCard(
                        title = "Ödeme Bilgileri",
                        items = listOf(
                            "Plan" to sub.plan,
                            "Aylık Ücret" to formatCurrency(sub.price),
                            "Yıllık Maliyet" to formatCurrency(sub.price * 12)
                        )
                    )
                }

                item {
                    DetailCard(
                        title = "Zaman Bilgileri",
                        items = listOf(
                            "Başlangıç Tarihi" to formatDate(sub.startDate),
                            "Sonraki Ödeme" to formatDate(viewModel.calculateCurrentNextPaymentDate(sub.startDate, sub.paymentPeriod)),
                            "Üyelik Süresi" to calculateDuration(sub.startDate)
                        )
                    )
                }

                item {
                    DetailCard(
                        title = "Maliyet Özeti",
                        items = listOf(
                            "Toplam Harcama" to formatCurrency(totalSpent)
                        )
                    )
                }

                // Plan Geçmişi Kartı
                if (planHistory.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Plan Geçmişi",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                planHistory.forEachIndexed { index, entry ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (entry.endDate == null)
                                                MaterialTheme.colorScheme.primaryContainer
                                            else
                                                MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = entry.plan,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = formatCurrency(entry.price),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                if (entry.endDate == null) {
                                                    Card(
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = MaterialTheme.colorScheme.primary
                                                        )
                                                    ) {
                                                        Text(
                                                            text = "Aktif Plan",
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onPrimary
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = formatDate(entry.startDate),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = " → ",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = entry.endDate?.let { formatDate(it) } ?: "Devam ediyor",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (entry.endDate == null)
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            val duration = if (entry.endDate != null) {
                                                val months = ((entry.endDate.time - entry.startDate.time) / 
                                                    (1000L * 60 * 60 * 24 * 30)).toInt()
                                                when {
                                                    months >= 12 -> "${months / 12} yıl ${months % 12} ay"
                                                    months > 0 -> "$months ay"
                                                    else -> "1 aydan az"
                                                }
                                            } else {
                                                val months = ((Date().time - entry.startDate.time) / 
                                                    (1000L * 60 * 60 * 24 * 30)).toInt()
                                                when {
                                                    months >= 12 -> "${months / 12} yıl ${months % 12} ay"
                                                    months > 0 -> "$months ay"
                                                    else -> "1 aydan az"
                                                }
                                            }

                                            Text(
                                                text = "Süre: $duration",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }

                                    if (index < planHistory.size - 1) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailCard(
    title: String,
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun calculateDuration(startDate: Date): String {
    val now = Date()
    val diffInMillis = now.time - startDate.time
    val days = diffInMillis / (1000 * 60 * 60 * 24)
    val months = days / 30
    val years = months / 12

    return when {
        years > 0 -> "$years yıl ${months % 12} ay"
        months > 0 -> "$months ay"
        else -> "$days gün"
    }
}

private fun formatDate(date: Date): String {
    return SimpleDateFormat("d MMMM yyyy", Locale("tr")).format(date)
}

private fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("tr", "TR")).format(amount)
}

private fun getCategoryColor(category: SubscriptionCategory): Color {
    return when (category) {
        SubscriptionCategory.STREAMING -> Color(0xFFE57373)
        SubscriptionCategory.MUSIC -> Color(0xFF81C784)
        SubscriptionCategory.EDUCATION -> Color(0xFF64B5F6)
        SubscriptionCategory.GAMING -> Color(0xFFBA68C8)
        SubscriptionCategory.SOFTWARE -> Color(0xFF4DB6AC)
        SubscriptionCategory.SPORTS -> Color(0xFFFFB74D)
        SubscriptionCategory.STORAGE -> Color(0xFF90A4AE)
        SubscriptionCategory.PRODUCTIVITY -> Color(0xFF9575CD)
        SubscriptionCategory.OTHER -> Color(0xFF78909C)
    }
} 
