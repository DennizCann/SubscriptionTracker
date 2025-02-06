package com.denizcan.subscriptiontracker.ui.screens.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.denizcan.subscriptiontracker.viewmodel.SubscriptionState

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
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        startDate = Date(it)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Yeni Üyelik Ekle",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Üyelik Adı") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = plan,
            onValueChange = { plan = it },
            label = { Text("Plan") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Fiyat") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Kategori seçimi
        Text(
            text = "Kategori",
            style = MaterialTheme.typography.titleMedium
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(SubscriptionCategory.values()) { category ->
                FilterChip(
                    selected = category == selectedCategory,
                    onClick = { selectedCategory = category },
                    label = { Text(when(category) {
                        SubscriptionCategory.STREAMING -> "Streaming"
                        SubscriptionCategory.MUSIC -> "Müzik"
                        SubscriptionCategory.GAMING -> "Oyun"
                        SubscriptionCategory.EDUCATION -> "Eğitim"
                        SubscriptionCategory.SOFTWARE -> "Yazılım"
                        SubscriptionCategory.SPORTS -> "Spor"
                        SubscriptionCategory.STORAGE -> "Depolama"
                        SubscriptionCategory.PRODUCTIVITY -> "Verimlilik"
                        SubscriptionCategory.OTHER -> "Diğer"
                    }) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ödeme periyodu seçimi
        Text(
            text = "Ödeme Periyodu",
            style = MaterialTheme.typography.titleMedium
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
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

        Spacer(modifier = Modifier.height(16.dp))

        // Başlangıç tarihi seçici
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Başlangıç Tarihi: ${dateFormatter.format(startDate)}")
        }

        if (showError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
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

                println("Üyelik ekleme başlatılıyor:")
                println("Ad: $name")
                println("Plan: $plan")
                println("Fiyat: $price")
                println("Kategori: $selectedCategory")
                println("Ödeme Periyodu: $selectedPaymentPeriod")
                println("Başlangıç Tarihi: $startDate")

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
