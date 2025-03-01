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
import com.denizcan.subscriptiontracker.model.PredefinedSubscription
import com.denizcan.subscriptiontracker.viewmodel.SubscriptionViewModel
import java.util.*
import com.denizcan.subscriptiontracker.ui.components.DatePickerButton
import com.denizcan.subscriptiontracker.viewmodel.SubscriptionState
import com.denizcan.subscriptiontracker.ui.theme.LocalSpacing
import com.denizcan.subscriptiontracker.ui.theme.ScreenClass
import com.denizcan.subscriptiontracker.ui.theme.getScreenClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPredefinedSubscriptionScreen(
    predefinedSubscription: PredefinedSubscription,
    onNavigateBack: () -> Unit,
    viewModel: SubscriptionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var plan by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedPaymentPeriod by remember { mutableStateOf<PaymentPeriod>(PaymentPeriod.MONTHLY) }
    var startDate by remember { mutableStateOf(Date()) }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(predefinedSubscription.name) },
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

                // Tarih seçici
                DatePickerButton(
                    date = startDate,
                    onDateSelected = { date ->
                        startDate = date
                    },
                    label = "Başlangıç Tarihi"
                )

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
                        if (price.isBlank() || price.toDoubleOrNull() == null) {
                            showError = true
                            errorMessage = "Geçerli bir fiyat giriniz"
                            return@Button
                        }

                        isLoading = true
                        showError = false

                        viewModel.addSubscription(
                            name = predefinedSubscription.name,
                            plan = plan,
                            price = price.toDoubleOrNull() ?: 0.0,
                            category = predefinedSubscription.category,
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