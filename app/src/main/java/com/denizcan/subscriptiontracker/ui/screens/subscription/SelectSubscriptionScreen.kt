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
import com.denizcan.subscriptiontracker.ui.theme.LocalSpacing
import com.denizcan.subscriptiontracker.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectSubscriptionScreen(
    onNavigateBack: () -> Unit,
    onCustomSubscription: () -> Unit,
    onSubscriptionSelected: (PredefinedSubscription) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<SubscriptionCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val spacing = LocalSpacing.current

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
                        .padding(horizontal = spacing.medium),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onCustomSubscription,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(spacing.small))
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
                    .padding(spacing.medium),
                placeholder = { Text("Servis ara...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            // Kategori filtreleme
            CategoryFilterChips(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                spacing = spacing
            )

            // Hazır üyelikler listesi
            val filteredSubscriptions = remember(selectedCategory, searchQuery) {
                PredefinedSubscriptions.subscriptions
                    .filter { subscription ->
                        (selectedCategory == null || subscription.category == selectedCategory) &&
                        (searchQuery.isEmpty() || subscription.name.contains(searchQuery, ignoreCase = true))
                    }
                    .groupBy { it.category }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = spacing.extraLarge)
            ) {
                filteredSubscriptions.forEach { (category, subscriptions) ->
                    item(key = category.name) {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(
                                start = spacing.medium,
                                end = spacing.medium,
                                top = spacing.medium,
                                bottom = spacing.small
                            )
                        )
                    }

                    items(
                        items = subscriptions,
                        key = { it.name }
                    ) { subscription ->
                        SubscriptionCard(
                            subscription = subscription,
                            onClick = { onSubscriptionSelected(subscription) },
                            spacing = spacing
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(spacing.medium))
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionCard(
    subscription: PredefinedSubscription,
    onClick: () -> Unit,
    spacing: Spacing
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(spacing.medium)
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
            }
        }
    }
} 