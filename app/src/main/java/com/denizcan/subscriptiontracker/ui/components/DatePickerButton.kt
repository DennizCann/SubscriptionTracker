package com.denizcan.subscriptiontracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

data class QuickDateOption(
    val label: String,
    val date: Date
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerButton(
    date: Date,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Tarih Seç",
    showQuickOptions: Boolean = true
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = SimpleDateFormat("d MMMM yyyy", Locale("tr"))
    
    Column(modifier = modifier) {
        // Ana tarih seçici buton
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, contentDescription = "Takvim")
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = dateFormatter.format(date),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Hızlı tarih seçenekleri
        if (showQuickOptions) {
            val calendar = Calendar.getInstance()
            val quickOptions = remember {
                listOf(
                    QuickDateOption("Bugün", Calendar.getInstance().time),
                    QuickDateOption("Yarın", Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }.time),
                    QuickDateOption("Gelecek Hafta", Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, 1) }.time),
                    QuickDateOption("Gelecek Ay", Calendar.getInstance().apply { add(Calendar.MONTH, 1) }.time)
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(quickOptions) { option ->
                    AssistChip(
                        onClick = { onDateSelected(option.date) },
                        label = { Text(option.label) }
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.time
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(Date(millis))
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
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
} 