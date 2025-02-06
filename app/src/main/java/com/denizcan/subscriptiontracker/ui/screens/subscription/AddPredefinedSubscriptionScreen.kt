package com.denizcan.subscriptiontracker.ui.screens.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denizcan.subscriptiontracker.model.PaymentPeriod
import com.denizcan.subscriptiontracker.model.PredefinedSubscription
import com.denizcan.subscriptiontracker.ui.components.DatePickerButton
import com.denizcan.subscriptiontracker.ui.components.PaymentPeriodSelector
import com.denizcan.subscriptiontracker.viewmodel.SubscriptionViewModel
import java.util.*

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
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        if (plan.isNotBlank() && price.isNotBlank()) {
                            viewModel.addSubscription(
                                name = predefinedSubscription.name,
                                plan = plan,
                                price = price.toDoubleOrNull() ?: 0.0,
                                category = predefinedSubscription.category,
                                paymentPeriod = selectedPaymentPeriod,
                                startDate = startDate
                            )
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = plan.isNotBlank() && price.isNotBlank()
                ) {
                    Text("Kaydet")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Servis bilgileri
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = predefinedSubscription.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = predefinedSubscription.category.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Plan girişi
            Text("Plan Bilgileri", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = plan,
                onValueChange = { plan = it },
                label = { Text("Plan Adı") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Fiyat") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Başlangıç tarihi
            Text("Başlangıç Tarihi", style = MaterialTheme.typography.titleMedium)
            DatePickerButton(
                date = startDate,
                onDateSelected = { startDate = it }
            )

            // Ödeme periyodu
            Text("Ödeme Periyodu", style = MaterialTheme.typography.titleMedium)
            PaymentPeriodSelector(
                selectedPeriod = selectedPaymentPeriod,
                onPeriodSelected = { selectedPaymentPeriod = it }
            )
        }
    }
} 