package com.denizcan.subscriptiontracker.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denizcan.subscriptiontracker.viewmodel.SettingsViewModel
import com.denizcan.subscriptiontracker.ui.theme.LocalSpacing
import com.denizcan.subscriptiontracker.ui.theme.ScreenClass
import com.denizcan.subscriptiontracker.ui.theme.Spacing
import com.denizcan.subscriptiontracker.ui.theme.getScreenClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val settingsState by viewModel.settingsState.collectAsState()
    val spacing = LocalSpacing.current
    val screenClass = getScreenClass()

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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .then(
                        if (screenClass == ScreenClass.COMPACT) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier.width(500.dp)
                        }
                    )
                    .padding(padding),
                contentPadding = PaddingValues(spacing.large),
                verticalArrangement = Arrangement.spacedBy(spacing.medium)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(spacing.large),
                            verticalArrangement = Arrangement.spacedBy(spacing.small)
                        ) {
                            Text(
                                text = "🚧 Yapım Aşamasında",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Bu sayfa henüz geliştirme aşamasındadır. Yakında tüm özellikleriyle birlikte kullanıma sunulacaktır.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                item {
                    SettingsSection(
                        title = "Tema Ayarları",
                        spacing = spacing
                    ) {
                        SettingsSwitchItem(
                            title = "Sistem Temasını Kullan",
                            icon = Icons.Default.Phone,
                            checked = settingsState.isSystemTheme,
                            onCheckedChange = { viewModel.updateSystemTheme(it) },
                            spacing = spacing
                        )

                        if (!settingsState.isSystemTheme) {
                            SettingsSwitchItem(
                                title = "Koyu Tema",
                                icon = Icons.Default.Settings,
                                checked = settingsState.isDarkMode,
                                onCheckedChange = { viewModel.updateDarkMode(it) },
                                spacing = spacing
                            )
                        }
                    }
                }

                item {
                    SettingsSection(
                        title = "Bildirim Ayarları",
                        spacing = spacing
                    ) {
                        SettingsSwitchItem(
                            title = "Ödeme Hatırlatıcıları",
                            icon = Icons.Default.Notifications,
                            checked = settingsState.isNotificationsEnabled,
                            onCheckedChange = { viewModel.updateNotifications(it) },
                            spacing = spacing
                        )
                    }
                }

                item {
                    SettingsSection(
                        title = "Para Birimi",
                        spacing = spacing
                    ) {
                        SettingsDropdownItem(
                            title = "Varsayılan Para Birimi",
                            icon = Icons.Default.Settings,
                            selectedValue = settingsState.selectedCurrency,
                            options = listOf("TRY", "USD", "EUR", "GBP"),
                            onOptionSelected = { viewModel.updateCurrency(it) },
                            spacing = spacing
                        )
                    }
                }

                item {
                    SettingsSection(
                        title = "Dil Ayarları",
                        spacing = spacing
                    ) {
                        SettingsDropdownItem(
                            title = "Uygulama Dili",
                            icon = Icons.Default.Settings,
                            selectedValue = settingsState.selectedLanguage,
                            options = listOf("Türkçe", "English"),
                            onOptionSelected = { viewModel.updateLanguage(it) },
                            spacing = spacing
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    spacing: Spacing,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.small)
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
    onCheckedChange: (Boolean) -> Unit,
    spacing: Spacing
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.small)
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
    onOptionSelected: (String) -> Unit,
    spacing: Spacing
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.small)
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
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = option,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ) 
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
} 