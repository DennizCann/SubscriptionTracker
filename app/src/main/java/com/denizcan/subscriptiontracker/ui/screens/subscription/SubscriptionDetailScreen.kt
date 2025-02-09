package com.denizcan.subscriptiontracker.ui.screens.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
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
import kotlin.math.ceil

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
    var totalSpent by remember { mutableDoubleStateOf(0.0) }

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

            println("Plan Geçmişi Yüklendi - Üyelik: ${sub.name}")
            println("Geçmiş Kayıt Sayısı: ${history.size}")

            // Toplam harcamayı hesapla
            var total = 0.0
            val now = Date()

            // Her plan dönemi için hesaplama yap
            history.forEach { entry ->
                val endDate = entry.endDate ?: now
                val startDate = entry.startDate

                // Başlangıç ve bitiş tarihleri arasındaki ay farkını hesapla
                val startCalendar = Calendar.getInstance().apply { time = startDate }
                val endCalendar = Calendar.getInstance().apply { time = endDate }

                val yearDiff = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR)
                var monthDiff = endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH) + (yearDiff * 12)

                // Gün bazında düzeltme
                if (endCalendar.get(Calendar.DAY_OF_MONTH) < startCalendar.get(Calendar.DAY_OF_MONTH)) {
                    monthDiff--
                }

                // En az 1 ay sayılacak şekilde ayarlama
                val effectiveMonths = maxOf(1, monthDiff)

                // Ödeme periyoduna göre toplam ödeme sayısını hesapla
                val payments = when (sub.paymentPeriod) {
                    PaymentPeriod.MONTHLY -> effectiveMonths
                    PaymentPeriod.QUARTERLY -> ((effectiveMonths - 1) / 3) + 1
                    PaymentPeriod.YEARLY -> ((effectiveMonths - 1) / 12) + 1
                }

                // Bu dönem için toplam harcama
                val planTotal = entry.price * payments
                total += planTotal

                println("""
                    Plan Detayları:
                    Plan: ${entry.plan}
                    Başlangıç: ${formatDate(startDate)}
                    Bitiş: ${formatDate(endDate)}
                    Fiyat: ${entry.price}
                    Ay Farkı: $monthDiff
                    Efektif Ay: $effectiveMonths
                    Ödeme Sayısı: $payments
                    Dönem Toplamı: $planTotal
                    Ara Toplam: $total
                    ------------------------
                """.trimIndent())
            }

            // Plan geçmişi boşsa mevcut planı kullan
            if (history.isEmpty()) {
                val startCalendar = Calendar.getInstance().apply { time = sub.startDate }
                val endCalendar = Calendar.getInstance().apply { time = now }
                
                val months = ((endCalendar.timeInMillis - startCalendar.timeInMillis) / 
                    (1000L * 60 * 60 * 24 * 30)).toInt()
                
                val payments = when (sub.paymentPeriod) {
                    PaymentPeriod.MONTHLY -> months
                    PaymentPeriod.QUARTERLY -> (months + 2) / 3
                    PaymentPeriod.YEARLY -> (months + 11) / 12
                }

                total = sub.price * maxOf(1, payments)

                println("""
                    Plan Geçmişi Boş - Mevcut Plan Kullanılıyor
                    Başlangıç: ${formatDate(sub.startDate)}
                    Fiyat: ${sub.price}
                    Ay Sayısı: $months
                    Ödeme Sayısı: $payments
                    Toplam: $total
                """.trimIndent())
            }

            totalSpent = total
            println("Final Toplam: $totalSpent")
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Timeline çizgisi ve nokta
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .background(
                                                        if (entry.endDate == null) 
                                                            MaterialTheme.colorScheme.primary
                                                        else 
                                                            MaterialTheme.colorScheme.surfaceVariant,
                                                        shape = CircleShape
                                                    )
                                            )
                                            if (index < planHistory.size - 1) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(2.dp)
                                                        .height(120.dp)
                                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                                )
                                            }
                                        }

                                        // Plan detayları
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (entry.endDate == null && index == planHistory.size - 1)
                                                    MaterialTheme.colorScheme.primaryContainer
                                                else
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Plan başlığı ve durum
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = entry.plan,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    if (entry.endDate == null && index == planHistory.size - 1) {
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

                                                // Fiyat ve periyot
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = formatCurrency(entry.price),
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = when(sub.paymentPeriod) {
                                                            PaymentPeriod.MONTHLY -> "Aylık"
                                                            PaymentPeriod.QUARTERLY -> "3 Aylık"
                                                            PaymentPeriod.YEARLY -> "Yıllık"
                                                        },
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                Divider(modifier = Modifier.padding(vertical = 4.dp))

                                                // Tarih bilgileri
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = "Başlangıç",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Text(
                                                            text = formatDate(entry.startDate),
                                                            style = MaterialTheme.typography.bodyMedium
                                                        )
                                                    }
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text(
                                                            text = if (entry.endDate == null) "Devam Ediyor" else "Bitiş",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Text(
                                                            text = entry.endDate?.let { formatDate(it) } ?: "—",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = if (entry.endDate == null) 
                                                                MaterialTheme.colorScheme.primary 
                                                            else 
                                                                MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }

                                                // Değişim bilgisi
                                                if (index > 0) {
                                                    val previousPlan = planHistory[index - 1]
                                                    val priceChange = entry.price - previousPlan.price
                                                    val priceChangePercent = (priceChange / previousPlan.price) * 100

                                                    Card(
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = if (priceChange > 0)
                                                                Color(0xFFFFEBEE)
                                                            else
                                                                Color(0xFFE8F5E9)
                                                        ),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(
                                                                text = if (priceChange > 0) "Fiyat Artışı" else "Fiyat Düşüşü",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = if (priceChange > 0) 
                                                                    Color(0xFFE57373) 
                                                                else 
                                                                    Color(0xFF81C784)
                                                            )
                                                            Text(
                                                                text = "${if (priceChange > 0) "+" else ""}${formatCurrency(priceChange)} (${String.format("%.1f", priceChangePercent)}%)",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = if (priceChange > 0) 
                                                                    Color(0xFFE57373) 
                                                                else 
                                                                    Color(0xFF81C784)
                                                            )
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
        SubscriptionCategory.AI -> Color(0xFF7986CB)
        SubscriptionCategory.NEWS -> Color(0xFFF06292)
        SubscriptionCategory.FOOD -> Color(0xFFFF8A65)
        SubscriptionCategory.OTHER -> Color(0xFF78909C)
    }
} 
