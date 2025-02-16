package com.denizcan.subscriptiontracker.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denizcan.subscriptiontracker.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val settingsState by viewModel.settingsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Uygulama Ayarları") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Geri")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsSection(title = "Tema Ayarları") {
                    SettingsSwitchItem(
                        title = "Sistem Temasını Kullan",
                        icon = Icons.Default.Phone,
                        checked = settingsState.isSystemTheme,
                        onCheckedChange = { viewModel.updateSystemTheme(it) }
                    )

                    if (!settingsState.isSystemTheme) {
                        SettingsSwitchItem(
                            title = "Koyu Tema",
                            icon = Icons.Default.Settings,
                            checked = settingsState.isDarkMode,
                            onCheckedChange = { viewModel.updateDarkMode(it) }
                        )
                    }
                }
            }

            item {
                SettingsSection(title = "Bildirim Ayarları") {
                    SettingsSwitchItem(
                        title = "Ödeme Hatırlatıcıları",
                        icon = Icons.Default.Notifications,
                        checked = settingsState.isNotificationsEnabled,
                        onCheckedChange = { viewModel.updateNotifications(it) }
                    )
                }
            }

            item {
                SettingsSection(title = "Para Birimi") {
                    SettingsDropdownItem(
                        title = "Varsayılan Para Birimi",
                        icon = Icons.Default.Settings,
                        selectedValue = settingsState.selectedCurrency,
                        options = listOf("TRY", "USD", "EUR", "GBP"),
                        onOptionSelected = { viewModel.updateCurrency(it) }
                    )
                }
            }

            item {
                SettingsSection(title = "Dil Ayarları") {
                    SettingsDropdownItem(
                        title = "Uygulama Dili",
                        icon = Icons.Default.Settings,
                        selectedValue = settingsState.selectedLanguage,
                        options = listOf("Türkçe", "English"),
                        onOptionSelected = { viewModel.updateLanguage(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            content()
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(text = title)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsDropdownItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(text = title)
        }
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(selectedValue)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
} 