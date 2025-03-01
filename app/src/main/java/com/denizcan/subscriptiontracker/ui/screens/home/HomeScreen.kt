package com.denizcan.subscriptiontracker.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.DismissDirection
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.ui.unit.Dp
import com.denizcan.subscriptiontracker.model.SubscriptionCategory
import com.denizcan.subscriptiontracker.viewmodel.AuthState
import com.denizcan.subscriptiontracker.ui.theme.LocalSpacing
import com.denizcan.subscriptiontracker.ui.theme.ScreenClass
import com.denizcan.subscriptiontracker.ui.theme.Spacing
import com.denizcan.subscriptiontracker.ui.theme.getScreenClass

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
    val subscriptionState by subscriptionViewModel.subscriptionState.collectAsState()
    val spacing = LocalSpacing.current
    val screenClass = getScreenClass()
    
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
                when (screenClass) {
                    ScreenClass.COMPACT -> CompactHomeLayout(
                        padding = padding,
                        spacing = spacing,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        state = state,
                        onDelete = { id -> subscriptionViewModel.deleteSubscription(id) },
                        onItemClick = onSubscriptionClick
                    )
                    else -> ExpandedHomeLayout(
                        padding = padding,
                        spacing = spacing,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        state = state,
                        onDelete = { id -> subscriptionViewModel.deleteSubscription(id) },
                        onItemClick = onSubscriptionClick
                    )
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
private fun CompactHomeLayout(
    padding: PaddingValues,
    spacing: Spacing,
    selectedCategory: SubscriptionCategory?,
    onCategorySelected: (SubscriptionCategory?) -> Unit,
    state: SubscriptionState.Success,
    onDelete: (String) -> Unit,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        CategoryFilterChips(
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected,
            spacing = spacing
        )

        SummaryCard(
            totalMonthlyExpense = state.totalMonthlyExpense,
            upcomingPayment = state.upcomingPayment?.let {
                "${it.name} - ${formatDate(it.nextPaymentDate)}"
            } ?: "Yaklaşan ödeme yok",
            modifier = Modifier.padding(horizontal = spacing.medium, vertical = spacing.small)
        )

        Text(
            text = "Aktif Üyelikler",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = spacing.medium, vertical = spacing.small)
        )

        val filteredSubscriptions = if (selectedCategory != null) {
            state.subscriptions.filter { it.category == selectedCategory }
        } else {
            state.subscriptions
        }

        if (filteredSubscriptions.isEmpty()) {
            EmptySubscriptions()
        } else {
            Box(modifier = Modifier.weight(1f)) {
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
                    onDelete = onDelete,
                    onItemClick = onItemClick,
                    spacing = spacing
                )
            }
        }
    }
}

@Composable
private fun ExpandedHomeLayout(
    padding: PaddingValues,
    spacing: Spacing,
    selectedCategory: SubscriptionCategory?,
    onCategorySelected: (SubscriptionCategory?) -> Unit,
    state: SubscriptionState.Success,
    onDelete: (String) -> Unit,
    onItemClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        // Sol Panel (Kategori ve Özet)
        Column(
            modifier = Modifier
                .weight(0.4f)
                .padding(end = spacing.medium)
        ) {
            CategoryFilterChips(
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected,
                spacing = spacing,
                isVertical = true
            )

            SummaryCard(
                totalMonthlyExpense = state.totalMonthlyExpense,
                upcomingPayment = state.upcomingPayment?.let {
                    "${it.name} - ${formatDate(it.nextPaymentDate)}"
                } ?: "Yaklaşan ödeme yok",
                modifier = Modifier.padding(vertical = spacing.medium)
            )
        }

        // Sağ Panel (Üyelik Listesi)
        Column(
            modifier = Modifier
                .weight(0.6f)
        ) {
            Text(
                text = "Aktif Üyelikler",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = spacing.medium)
            )

            val filteredSubscriptions = if (selectedCategory != null) {
                state.subscriptions.filter { it.category == selectedCategory }
            } else {
                state.subscriptions
            }

            if (filteredSubscriptions.isEmpty()) {
                EmptySubscriptions()
            } else {
                Box(modifier = Modifier.weight(1f)) {
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
                        onDelete = onDelete,
                        onItemClick = onItemClick,
                        spacing = spacing
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryFilterChips(
    selectedCategory: SubscriptionCategory?,
    onCategorySelected: (SubscriptionCategory?) -> Unit,
    spacing: Spacing,
    isVertical: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (isVertical) {
        Column(
            modifier = modifier.padding(vertical = spacing.small),
            verticalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Tümü") }
            )
            
            SubscriptionCategory.values().forEach { category ->
                FilterChip(
                    selected = category == selectedCategory,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.displayName) }
                )
            }
        }
    } else {
        LazyRow(
            modifier = modifier
                .padding(horizontal = spacing.medium, vertical = spacing.small)
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing.small)
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
    val spacing = LocalSpacing.current
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(spacing.medium)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
            
            Spacer(modifier = Modifier.height(spacing.medium))
            
            Text(
                text = "Yaklaşan Ödeme",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = upcomingPayment,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun SubscriptionList(
    subscriptions: List<SubscriptionItem>,
    onDelete: (String) -> Unit = {},
    onItemClick: (String) -> Unit = {},
    spacing: Spacing,
    modifier: Modifier = Modifier
) {
    val screenClass = getScreenClass()
    
    when (screenClass) {
        ScreenClass.COMPACT -> {
            // Dikey liste görünümü
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = spacing.medium, vertical = spacing.small),
                verticalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                items(
                    items = subscriptions,
                    key = { it.id }
                ) { subscription ->
                    SubscriptionCard(
                        subscription = subscription,
                        onDelete = { onDelete(subscription.id) },
                        onItemClick = { onItemClick(subscription.id) },
                        spacing = spacing,
                        isCompact = true
                    )
                }
            }
        }
        else -> {
            // Grid görünümü
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = spacing.medium, vertical = spacing.small),
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                verticalArrangement = Arrangement.spacedBy(spacing.medium)
            ) {
                items(
                    items = subscriptions,
                    key = { it.id }
                ) { subscription ->
                    SubscriptionCard(
                        subscription = subscription,
                        onDelete = { onDelete(subscription.id) },
                        onItemClick = { onItemClick(subscription.id) },
                        spacing = spacing,
                        isCompact = false
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SubscriptionCard(
    subscription: SubscriptionItem,
    onDelete: () -> Unit = {},
    onItemClick: () -> Unit = {},
    spacing: Spacing,
    isCompact: Boolean,
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
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    if (isCompact) {
        // Kompakt görünüm (swipe to delete ile)
        SwipeToDismiss(
            state = dismissState,
            directions = setOf(DismissDirection.EndToStart),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.small),
            background = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.error, shape = MaterialTheme.shapes.medium)
                        .padding(horizontal = spacing.medium),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text("Sil", color = MaterialTheme.colorScheme.onError)
                }
            }
        ) {
            CompactSubscriptionCardContent(
                subscription = subscription,
                onItemClick = onItemClick,
                spacing = spacing,
                modifier = modifier
            )
        }
    } else {
        // Geniş ekran görünümü
        ExpandedSubscriptionCardContent(
            subscription = subscription,
            onDelete = { showDeleteDialog = true },
            onItemClick = onItemClick,
            spacing = spacing,
            modifier = modifier
        )
    }
}

@Composable
private fun CompactSubscriptionCardContent(
    subscription: SubscriptionItem,
    onItemClick: () -> Unit,
    spacing: Spacing,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(spacing.medium)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                CategoryIndicator(
                    category = subscription.category,
                    name = subscription.name
                )
                
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

@Composable
private fun ExpandedSubscriptionCardContent(
    subscription: SubscriptionItem,
    onDelete: () -> Unit,
    onItemClick: () -> Unit,
    spacing: Spacing,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(spacing.medium)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    CategoryIndicator(
                        category = subscription.category,
                        name = subscription.name,
                        size = 48.dp
                    )
                    
                    Column {
                        Text(
                            text = subscription.name,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = subscription.category.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(spacing.medium))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Plan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = subscription.plan,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Aylık Ödeme",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(subscription.price),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(spacing.small))
            
            Text(
                text = "Sonraki Ödeme",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subscription.nextPayment,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun CategoryIndicator(
    category: SubscriptionCategory,
    name: String,
    size: Dp = 40.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                getCategoryColor(category),
                shape = MaterialTheme.shapes.small
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.first().toString(),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )
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
        SubscriptionCategory.AI -> Color(0xFF7986CB) // İndigo
        SubscriptionCategory.NEWS -> Color(0xFFF06292) // Pembe
        SubscriptionCategory.FOOD -> Color(0xFFFF8A65) // Turuncu-Kırmızı
        SubscriptionCategory.OTHER -> Color(0xFF78909C) // Gri
    }
}

@Composable
fun EmptySubscriptions(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Henüz Hiç Üyeliğiniz Yok",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Üyeliklerinizi takip etmek için sağ üst köşedeki + butonuna tıklayarak yeni bir üyelik ekleyebilirsiniz.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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