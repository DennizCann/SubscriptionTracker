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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.denizcan.subscriptiontracker.ui.components.GradientBackground

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
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Yenileme fonksiyonu
    val refreshSubscription = {
        viewModel.refreshSubscriptionDetails(subscriptionId)
    }

    LaunchedEffect(subscriptionId) {
        viewModel.getPlanHistory(subscriptionId).collect { history ->
            planHistory = history
            // Plan geçmişini aldıktan sonra takvimi güncelle
            viewModel.refreshSubscriptionDetails(subscriptionId)
        }
    }

    LaunchedEffect(subscription, isRefreshing) {
        if (!isRefreshing) {
            subscription?.let { sub ->
                editedName = sub.name
                editedPlan = sub.plan
                editedPrice = sub.price.toString()
                editedCategory = sub.category
                editedPaymentPeriod = sub.paymentPeriod
                editedStartDate = sub.startDate

                // Plan geçmişini al
                viewModel.getPlanHistory(sub.id).collect { history ->
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
        }
    }

    if (showDatePicker && editedStartDate != null) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = editedStartDate!!.time
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            editedStartDate = Date(it)
                        }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Tamam")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("İptal")
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                headlineContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                weekdayContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                subheadContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                yearContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                currentYearContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                dayContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                todayContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                todayDateBorderColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.padding(16.dp),
                    colors = DatePickerDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        headlineContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        weekdayContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        subheadContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        yearContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        currentYearContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                        selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                        dayContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                        todayContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        todayDateBorderColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
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
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    selectedUpgradeDate = Date(it)
                                }
                                showUpgradeDatePicker = false
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Tamam")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showUpgradeDatePicker = false },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("İptal")
                        }
                    },
                    colors = DatePickerDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        headlineContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        weekdayContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        subheadContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        yearContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        currentYearContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                        selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                        dayContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                        todayContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        todayDateBorderColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        DatePicker(
                            state = datePickerState,
                            modifier = Modifier.padding(16.dp),
                            colors = DatePickerDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                headlineContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                weekdayContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                subheadContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                yearContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                currentYearContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                                selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                                dayContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                                todayContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                todayDateBorderColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
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
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Mevcut Fiyat: ${formatCurrency(sub.price)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        OutlinedTextField(
                            value = upgradedPlan,
                            onValueChange = { upgradedPlan = it },
                            label = { Text("Yeni Plan Adı") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        
                        OutlinedTextField(
                            value = upgradedPrice,
                            onValueChange = { upgradedPrice = it },
                            label = { Text("Yeni Fiyat") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        OutlinedButton(
                            onClick = { showUpgradeDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
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
                    Button(
                        onClick = {
                            val newPrice = upgradedPrice.toDoubleOrNull()
                            if (upgradedPlan.isNotBlank() && newPrice != null) {
                                viewModel.upgradePlan(
                                    subscriptionId = sub.id,
                                    newPlan = upgradedPlan,
                                    newPrice = newPrice,
                                    upgradeDate = selectedUpgradeDate
                                )
                                showUpgradePlanDialog = false
                                refreshSubscription()
                            }
                        }
                    ) {
                        Text("Kaydet")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showUpgradePlanDialog = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("İptal")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showEditDialog && subscription != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Üyelik Düzenle") },
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
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editedPlan,
                        onValueChange = { editedPlan = it },
                        label = { Text("Plan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editedPrice,
                        onValueChange = { editedPrice = it },
                        label = { Text("Fiyat") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(
                        text = "Kategori",
                        style = MaterialTheme.typography.titleMedium
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(SubscriptionCategory.values()) { category ->
                            FilterChip(
                                selected = category == editedCategory,
                                onClick = { editedCategory = category },
                                label = { Text(category.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Text(
                        text = "Ödeme Periyodu",
                        style = MaterialTheme.typography.titleMedium
                    )

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
                                }) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Başlangıç Tarihi: ${formatDate(editedStartDate!!)}")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedName.isNotBlank() && 
                            editedCategory != null && 
                            editedPaymentPeriod != null && 
                            editedPlan.isNotBlank()
                        ) {
                            val updatedSubscription = subscription!!.copy(
                                name = editedName,
                                plan = editedPlan,
                                price = editedPrice.toDoubleOrNull() ?: subscription!!.price,
                                category = editedCategory!!,
                                paymentPeriod = editedPaymentPeriod!!,
                                startDate = editedStartDate!!
                            )
                            viewModel.updateSubscription(updatedSubscription)
                            showEditDialog = false
                            refreshSubscription()
                            viewModel.refresh()
                        }
                    }
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("İptal")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showDeleteDialog && subscription != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Üyeliği Sil") },
            text = { Text("Bu üyeliği silmek istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSubscription(subscription!!.id)
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("İptal")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,  // Scaffold arka planını şeffaf yap
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
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Plan Yükselt")
                        }
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Düzenle")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Üyeliği Sil")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,  // TopAppBar arka planını şeffaf yap
                        scrolledContainerColor = Color.Transparent
                    )
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
                                "Aylık Ücret" to formatCurrency(when (sub.paymentPeriod) {
                                    PaymentPeriod.MONTHLY -> sub.price
                                    PaymentPeriod.QUARTERLY -> sub.price / 3.0
                                    PaymentPeriod.YEARLY -> sub.price / 12.0
                                }),
                                "Yıllık Maliyet" to formatCurrency(when (sub.paymentPeriod) {
                                    PaymentPeriod.MONTHLY -> sub.price * 12.0
                                    PaymentPeriod.QUARTERLY -> sub.price * 4.0
                                    PaymentPeriod.YEARLY -> sub.price
                                })
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
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailCard(
    title: String,
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
