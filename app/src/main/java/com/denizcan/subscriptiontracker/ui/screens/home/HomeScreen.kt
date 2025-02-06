package com.denizcan.subscriptiontracker.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denizcan.subscriptiontracker.viewmodel.AuthViewModel
import com.denizcan.subscriptiontracker.viewmodel.SubscriptionState
import com.denizcan.subscriptiontracker.viewmodel.SubscriptionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.DismissDirection
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.filled.ExitToApp
import com.denizcan.subscriptiontracker.model.SubscriptionCategory
import com.denizcan.subscriptiontracker.viewmodel.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    subscriptionViewModel: SubscriptionViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onLogout: () -> Unit,
    onAddSubscription: () -> Unit = {},
    onSubscriptionClick: (String) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf<SubscriptionCategory?>(null) }
    val authState by authViewModel.authState.collectAsState()
    val userName by authViewModel.userName.collectAsState()
    val subscriptionState by subscriptionViewModel.subscriptionState.collectAsState()
    
    // Ekran ilk yüklendiğinde verileri çek
    LaunchedEffect(Unit) {
        subscriptionViewModel.refresh()
    }

    // Oturum durumu değişikliklerini izle
    LaunchedEffect(authState) {
        println("HomeScreen - AuthState değişti: $authState")
        when (authState) {
            is AuthState.Initial -> {
                println("HomeScreen - Oturum kapandı, login ekranına yönlendiriliyor")
                onLogout()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Üyelik Takip") },
                actions = {
                    // Üyelik Ekle butonu
                    IconButton(onClick = onAddSubscription) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Üyelik Ekle",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Çıkış butonu
                    IconButton(
                        onClick = {
                            println("HomeScreen - Oturum kapatma başlatılıyor")
                            authViewModel.logout()
                        }
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Çıkış Yap",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (val state = subscriptionState) {
            is SubscriptionState.Loading -> {
                LoadingScreen()
            }
            
            is SubscriptionState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Kategori Filtreleme
                    CategoryFilterChips(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it }
                    )

                    // Özet Kart
                    SummaryCard(
                        totalMonthlyExpense = state.totalMonthlyExpense,
                        upcomingPayment = state.upcomingPayment?.let {
                            "${it.name} - ${formatDate(it.nextPaymentDate)}"
                        } ?: "Yaklaşan ödeme yok",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Üyelik Listesi Başlığı
                    Text(
                        text = "Aktif Üyelikler",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    val filteredSubscriptions = if (selectedCategory != null) {
                        state.subscriptions.filter { it.category == selectedCategory }
                    } else {
                        state.subscriptions
                    }

                    if (filteredSubscriptions.isEmpty()) {
                        EmptySubscriptions()
                    } else {
                        SubscriptionList(
                            subscriptions = filteredSubscriptions.map { sub ->
                                SubscriptionItem(
                                    id = sub.id,
                                    name = sub.name,
                                    plan = sub.plan,
                                    price = sub.price,
                                    nextPayment = formatDate(sub.nextPaymentDate),
                                    category = sub.category
                                )
                            },
                            onDelete = { id -> subscriptionViewModel.deleteSubscription(id) },
                            onItemClick = onSubscriptionClick
                        )
                    }
                }
            }
            
            is SubscriptionState.Error -> {
                ErrorMessage(
                    message = state.message,
                    onRetry = { subscriptionViewModel.refresh() }
                )
            }
        }
    }
}

@Composable
fun CategoryFilterChips(
    selectedCategory: SubscriptionCategory?,
    onCategorySelected: (SubscriptionCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Tümü") }
            )
        }
        
        items(SubscriptionCategory.values()) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(category.displayName) }
            )
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun SummaryCard(
    totalMonthlyExpense: Double,
    upcomingPayment: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Aylık Toplam",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = formatCurrency(totalMonthlyExpense),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column {
                Text(
                    text = "Yıllık Toplam",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = formatCurrency(totalMonthlyExpense * 12),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SubscriptionList(
    subscriptions: List<SubscriptionItem>,
    onDelete: (String) -> Unit = {},
    onItemClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(subscriptions) { subscription ->
            SubscriptionCard(
                subscription = subscription,
                onDelete = { onDelete(subscription.id) },
                onItemClick = { onItemClick(subscription.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SubscriptionCard(
    subscription: SubscriptionItem,
    onDelete: () -> Unit = {},
    onItemClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val dismissState = rememberDismissState { dismissValue ->
        when (dismissValue) {
            DismissValue.DismissedToStart -> {
                showDeleteDialog = true
                false
            }
            else -> false
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Üyeliği Sil") },
            text = { Text("${subscription.name} üyeliğini silmek istediğinizden emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Evet")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Hayır")
                }
            }
        )
    }

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        background = {
            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Sil",
                    color = Color.White
                )
            }
        }
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable(onClick = onItemClick)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Kategori renk indikatörü
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                getCategoryColor(subscription.category),
                                shape = MaterialTheme.shapes.small
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = subscription.name.first().toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    Column {
                        Text(
                            text = subscription.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = subscription.category.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Sonraki Ödeme: ${subscription.nextPayment}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Text(
                    text = formatCurrency(subscription.price),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun getCategoryColor(category: SubscriptionCategory): Color {
    return when (category) {
        SubscriptionCategory.STREAMING -> Color(0xFFE57373) // Kırmızı
        SubscriptionCategory.MUSIC -> Color(0xFF81C784) // Yeşil
        SubscriptionCategory.EDUCATION -> Color(0xFF64B5F6) // Mavi
        SubscriptionCategory.GAMING -> Color(0xFFBA68C8) // Mor
        SubscriptionCategory.SOFTWARE -> Color(0xFF4DB6AC) // Turkuaz
        SubscriptionCategory.SPORTS -> Color(0xFFFFB74D) // Turuncu
        SubscriptionCategory.STORAGE -> Color(0xFF90A4AE) // Gri
        SubscriptionCategory.PRODUCTIVITY -> Color(0xFF9575CD) // Mor
        SubscriptionCategory.OTHER -> Color(0xFF78909C) // Gri
    }
}

@Composable
fun EmptySubscriptions(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Henüz üyelik eklenmemiş",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Yeni üyelik eklemek için + butonuna tıklayın",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error
        )
        TextButton(onClick = onRetry) {
            Text("Tekrar Dene")
        }
    }
}

data class SubscriptionItem(
    val id: String,
    val name: String,
    val plan: String,
    val price: Double,
    val nextPayment: String,
    val category: SubscriptionCategory = SubscriptionCategory.OTHER
)

fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("tr", "TR")).format(amount)
}

fun formatDate(date: Date): String {
    return SimpleDateFormat("d MMMM yyyy", Locale("tr")).format(date)
} 