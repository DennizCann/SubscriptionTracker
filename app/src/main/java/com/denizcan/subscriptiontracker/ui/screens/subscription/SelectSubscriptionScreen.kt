package com.denizcan.subscriptiontracker.ui.screens.subscription

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denizcan.subscriptiontracker.model.PredefinedSubscription
import com.denizcan.subscriptiontracker.model.PredefinedSubscriptions
import com.denizcan.subscriptiontracker.model.SubscriptionCategory
import com.denizcan.subscriptiontracker.ui.screens.home.CategoryFilterChips

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectSubscriptionScreen(
    onNavigateBack: () -> Unit,
    onCustomSubscription: () -> Unit,
    onSubscriptionSelected: (PredefinedSubscription) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<SubscriptionCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Üyelik Seç") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Geri")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.height(60.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onCustomSubscription,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Özel Üyelik Ekle")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Arama çubuğu
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Servis ara...") },
                leadingIcon = { Icon(Icons.Default.Search, null) }
            )

            // Kategori filtreleme
            CategoryFilterChips(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            // Hazır üyelikler listesi
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 80.dp // BottomBar için ekstra padding
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filtrelenmiş üyelikleri kategorilere göre grupla
                val groupedSubscriptions = PredefinedSubscriptions.subscriptions
                    .filter { subscription ->
                        (selectedCategory == null || subscription.category == selectedCategory) &&
                        (searchQuery.isEmpty() || subscription.name.contains(searchQuery, ignoreCase = true))
                    }
                    .groupBy { it.category }

                groupedSubscriptions.forEach { (category, subscriptions) ->
                    item {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(subscriptions) { subscription ->
                        SubscriptionCard(
                            subscription = subscription,
                            onClick = { onSubscriptionSelected(subscription) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionCard(
    subscription: PredefinedSubscription,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subscription.category.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${subscription.plans.size} plan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 