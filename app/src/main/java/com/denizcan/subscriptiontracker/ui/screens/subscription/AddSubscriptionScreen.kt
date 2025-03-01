package com.denizcan.subscriptiontracker.ui.screens.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denizcan.subscriptiontracker.model.PaymentPeriod
import com.denizcan.subscriptiontracker.viewmodel.SubscriptionViewModel
import java.util.*
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePicker
import java.text.SimpleDateFormat
import com.denizcan.subscriptiontracker.model.SubscriptionCategory
import com.denizcan.subscriptiontracker.ui.components.DatePickerButton
import com.denizcan.subscriptiontracker.viewmodel.SubscriptionState
import com.denizcan.subscriptiontracker.ui.theme.LocalSpacing
import com.denizcan.subscriptiontracker.ui.theme.ScreenClass
import com.denizcan.subscriptiontracker.ui.theme.getScreenClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionScreen(
    onNavigateBack: () -> Unit,
    viewModel: SubscriptionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var name by remember { mutableStateOf("") }
    var plan by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<SubscriptionCategory?>(null) }
    var selectedPaymentPeriod by remember { mutableStateOf<PaymentPeriod>(PaymentPeriod.MONTHLY) }
    var startDate by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val spacing = LocalSpacing.current
    val screenClass = getScreenClass()

    val subscriptionState by viewModel.subscriptionState.collectAsState()

    LaunchedEffect(subscriptionState) {
        when (subscriptionState) {
            is SubscriptionState.Error -> {
                showError = true
                errorMessage = (subscriptionState as SubscriptionState.Error).message
                isLoading = false
            }
            is SubscriptionState.Success -> {
                if (isLoading) {
                    isLoading = false
                    onNavigateBack()
                }
            }
            else -> {}
        }
    }

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("tr"))

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.time
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            startDate = Date(it)
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
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                headlineContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                currentYearContentColor = MaterialTheme.colorScheme.primary,
                selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                dayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                todayContentColor = MaterialTheme.colorScheme.primary,
                todayDateBorderColor = MaterialTheme.colorScheme.primary
            )
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Üyelik Ekle") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .then(
                        if (screenClass == ScreenClass.COMPACT) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier.width(500.dp)
                        }
                    )
                    .padding(padding)
                    .padding(spacing.large)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(spacing.medium)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Üyelik Adı") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = plan,
                    onValueChange = { plan = it },
                    label = { Text("Plan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Fiyat") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Kategori seçimi
                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.titleMedium
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.small),
                    modifier = Modifier.padding(vertical = spacing.small)
                ) {
                    items(SubscriptionCategory.values()) { category ->
                        FilterChip(
                            selected = category == selectedCategory,
                            onClick = { selectedCategory = category },
                            label = { Text(category.displayName) }
                        )
                    }
                }

                // Ödeme periyodu seçimi
                Text(
                    text = "Ödeme Periyodu",
                    style = MaterialTheme.typography.titleMedium
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.small),
                    modifier = Modifier.padding(vertical = spacing.small)
                ) {
                    items(PaymentPeriod.values()) { period ->
                        FilterChip(
                            selected = period == selectedPaymentPeriod,
                            onClick = { selectedPaymentPeriod = period },
                            label = { Text(when(period) {
                                PaymentPeriod.MONTHLY -> "Aylık"
                                PaymentPeriod.QUARTERLY -> "3 Aylık"
                                PaymentPeriod.YEARLY -> "Yıllık"
                            }) }
                        )
                    }
                }

                // Tarih seçiciler
                Column(
                    verticalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    DatePickerButton(
                        date = startDate,
                        onDateSelected = { date ->
                            startDate = date
                        },
                        label = "Başlangıç Tarihi"
                    )
                }

                if (showError) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(spacing.medium),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            showError = true
                            errorMessage = "Üyelik adı boş olamaz"
                            return@Button
                        }

                        if (price.isBlank() || price.toDoubleOrNull() == null) {
                            showError = true
                            errorMessage = "Geçerli bir fiyat giriniz"
                            return@Button
                        }

                        if (selectedCategory == null) {
                            showError = true
                            errorMessage = "Lütfen bir kategori seçiniz"
                            return@Button
                        }

                        isLoading = true
                        showError = false

                        viewModel.addSubscription(
                            name = name,
                            plan = plan,
                            price = price.toDoubleOrNull() ?: 0.0,
                            category = selectedCategory!!,
                            paymentPeriod = selectedPaymentPeriod,
                            startDate = startDate
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}
