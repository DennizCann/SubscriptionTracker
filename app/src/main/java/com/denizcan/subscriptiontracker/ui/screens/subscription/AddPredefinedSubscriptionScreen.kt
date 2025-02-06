package com.denizcan.subscriptiontracker.ui.screens.subscription

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denizcan.subscriptiontracker.model.PaymentPeriod
import com.denizcan.subscriptiontracker.model.PredefinedPlan
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
    var selectedPlan by remember { mutableStateOf<PredefinedPlan?>(null) }
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
                        selectedPlan?.let { plan ->
                            viewModel.addSubscription(
                                name = predefinedSubscription.name,
                                plan = plan.name,
                                price = plan.price,
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
                    enabled = selectedPlan != null
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

            // Plan seçimi
            Text("Plan Seçin", style = MaterialTheme.typography.titleMedium)
            predefinedSubscription.plans.forEach { planItem ->
                PlanSelectionCard(
                    plan = planItem,
                    isSelected = planItem == selectedPlan,
                    onClick = { selectedPlan = planItem }
                )
            }

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

@Composable
fun PlanSelectionCard(
    plan: PredefinedPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = plan.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "₺${plan.price}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
} 