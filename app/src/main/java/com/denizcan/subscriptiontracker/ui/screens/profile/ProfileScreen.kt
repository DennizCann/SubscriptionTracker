package com.denizcan.subscriptiontracker.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denizcan.subscriptiontracker.viewmodel.AuthViewModel
import com.denizcan.subscriptiontracker.viewmodel.SubscriptionViewModel
import com.denizcan.subscriptiontracker.viewmodel.SubscriptionState
import com.denizcan.subscriptiontracker.ui.theme.LocalSpacing
import com.denizcan.subscriptiontracker.ui.theme.ScreenClass
import com.denizcan.subscriptiontracker.ui.theme.Spacing
import com.denizcan.subscriptiontracker.ui.theme.getScreenClass
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = viewModel(),
    subscriptionViewModel: SubscriptionViewModel = viewModel(),
    onNavigateToSettings: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val userName by authViewModel.userName.collectAsState()
    val userEmail = authViewModel.getCurrentUser()?.email ?: ""
    val spacing = LocalSpacing.current
    val screenClass = getScreenClass()
    
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .then(
                        if (screenClass == ScreenClass.COMPACT) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier.width(400.dp)
                        }
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(spacing.large),
                verticalArrangement = Arrangement.spacedBy(spacing.medium)
            ) {
                // Profil Başlığı
                ProfileHeader(
                    userName = userName,
                    email = userEmail,
                    spacing = spacing
                )

                Divider()

                // İstatistikler
                StatisticsSection(
                    subscriptionViewModel = subscriptionViewModel,
                    spacing = spacing
                )

                Divider()

                // Ayarlar Butonu
                OutlinedButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Text("Uygulama Ayarları")
                    }
                }

                // Çıkış Yap Butonu
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Text("Çıkış Yap")
                    }
                }
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Çıkış Yap") },
                text = { Text("Çıkış yapmak istediğinize emin misiniz?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            authViewModel.logout()
                            showLogoutDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Evet")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLogoutDialog = false },
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
}

@Composable
private fun ProfileHeader(
    userName: String,
    email: String,
    spacing: Spacing
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(spacing.medium))
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = email,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatisticsSection(
    subscriptionViewModel: SubscriptionViewModel,
    spacing: Spacing
) {
    val subscriptionState by subscriptionViewModel.subscriptionState.collectAsState()
    
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
                text = "Abonelik İstatistikleri",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(
                    icon = Icons.Default.Info,
                    title = "Toplam Abonelik",
                    value = when (subscriptionState) {
                        is SubscriptionState.Success -> (subscriptionState as SubscriptionState.Success).subscriptions.size.toString()
                        else -> "0"
                    }
                )
                
                StatisticItem(
                    icon = Icons.Default.Info,
                    title = "Aylık Toplam",
                    value = when (subscriptionState) {
                        is SubscriptionState.Success -> {
                            val amount = (subscriptionState as SubscriptionState.Success).totalMonthlyExpense
                            val format = NumberFormat.getCurrencyInstance(Locale("tr", "TR")).apply {
                                maximumFractionDigits = 3
                                minimumFractionDigits = 0
                            }
                            format.format(amount)
                        }
                        else -> "0 ₺"
                    }
                )
            }
        }
    }
}

@Composable
private fun StatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSection(
    onLogoutClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Ayarlar",
                style = MaterialTheme.typography.titleMedium
            )
            
            // Ayarlar listesi
            SettingsItem(
                icon = Icons.Default.Settings,
                title = "Uygulama Ayarları",
                onClick = { /* TODO: Uygulama ayarları */ }
            )
            
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Hakkında",
                onClick = { /* TODO: Hakkında sayfası */ }
            )
            
            SettingsItem(
                icon = Icons.Default.ExitToApp,
                title = "Çıkış Yap",
                onClick = onLogoutClick,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = tint
            )
        }
    }
} 