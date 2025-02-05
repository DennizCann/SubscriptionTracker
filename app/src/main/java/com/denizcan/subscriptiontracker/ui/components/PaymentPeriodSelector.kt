package com.denizcan.subscriptiontracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denizcan.subscriptiontracker.model.PaymentPeriod

@Composable
fun PaymentPeriodSelector(
    selectedPeriod: PaymentPeriod,
    onPeriodSelected: (PaymentPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PaymentPeriod.values().forEach { period ->
            FilterChip(
                selected = period == selectedPeriod,
                onClick = { onPeriodSelected(period) },
                label = {
                    Text(when(period) {
                        PaymentPeriod.MONTHLY -> "Aylık"
                        PaymentPeriod.QUARTERLY -> "3 Aylık"
                        PaymentPeriod.YEARLY -> "Yıllık"
                    })
                }
            )
        }
    }
} 