package com.denizcan.subscriptiontracker.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Calendar : Screen("calendar")
    object Analytics : Screen("analytics")
    object Profile : Screen("profile")
    object SelectSubscription : Screen("select_subscription")
    object AddCustomSubscription : Screen("add_custom_subscription")
    object AddSubscription : Screen("add_subscription")
    object AddPredefinedSubscription : Screen("add_predefined_subscription/{subscriptionId}")
    object SubscriptionDetail : Screen("subscription_detail")
} 