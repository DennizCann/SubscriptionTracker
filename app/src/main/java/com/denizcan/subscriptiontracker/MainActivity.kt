package com.denizcan.subscriptiontracker

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.denizcan.subscriptiontracker.model.PredefinedSubscriptions
import com.denizcan.subscriptiontracker.navigation.BottomNavItem
import com.denizcan.subscriptiontracker.navigation.Screen
import com.denizcan.subscriptiontracker.ui.screens.auth.LoginScreen
import com.denizcan.subscriptiontracker.ui.screens.auth.RegisterScreen
import com.denizcan.subscriptiontracker.ui.screens.home.HomeScreen
import com.denizcan.subscriptiontracker.ui.screens.subscription.AddSubscriptionScreen
import com.denizcan.subscriptiontracker.ui.screens.subscription.SelectSubscriptionScreen
import com.denizcan.subscriptiontracker.ui.theme.SubscriptionTrackerTheme
import com.denizcan.subscriptiontracker.ui.screens.calendar.CalendarScreen
import com.denizcan.subscriptiontracker.ui.screens.subscription.AddPredefinedSubscriptionScreen
import com.denizcan.subscriptiontracker.viewmodel.AuthState
import com.denizcan.subscriptiontracker.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.denizcan.subscriptiontracker.ui.screens.analytics.AnalyticsScreen

class MainActivity : ComponentActivity() {
    private val viewModel: AuthViewModel by viewModels()
    
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            println("MainActivity - Google Sign-In sonucu alındı")
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            viewModel.handleGoogleSignInResult(task)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SubscriptionTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        onGoogleSignIn = { viewModel.signInWithGoogle(this@MainActivity, googleSignInLauncher) },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    onGoogleSignIn: () -> Unit,
    viewModel: AuthViewModel
) {
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsState()

    // Tek bir LaunchedEffect ile tüm state değişikliklerini yönet
    LaunchedEffect(authState) {
        println("AppNavigation - AuthState değişti: $authState")
        when (authState) {
            is AuthState.Success -> {
                println("AppNavigation - Giriş başarılı, ana sayfaya yönlendiriliyor")
                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
            is AuthState.Initial -> {
                println("AppNavigation - Oturum kapalı, giriş sayfasına yönlendiriliyor")
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
            is AuthState.Error -> {
                println("AppNavigation - Hata oluştu: ${(authState as AuthState.Error).message}")
            }
            else -> {}
        }
    }

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Calendar,
        BottomNavItem.Analytics,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            if (navController.currentBackStackEntryAsState().value?.destination?.route in items.map { it.route }) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (authState is AuthState.Success) Screen.Home.route else Screen.Login.route,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onLoginSuccess = {},
                    onGoogleSignIn = onGoogleSignIn,
                    viewModel = viewModel
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onRegisterSuccess = {}
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    onAddSubscription = {
                        navController.navigate(Screen.SelectSubscription.route)
                    },
                    onEditSubscription = { id ->
                        navController.navigate("${Screen.AddSubscription.route}/$id")
                    }
                )
            }

            composable(Screen.SelectSubscription.route) {
                SelectSubscriptionScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onCustomSubscription = {
                        navController.navigate(Screen.AddCustomSubscription.route)
                    },
                    onSubscriptionSelected = { predefinedSubscription ->
                        val index = PredefinedSubscriptions.subscriptions.indexOf(predefinedSubscription)
                        navController.navigate(Screen.AddPredefinedSubscription.route.replace("{subscriptionId}", index.toString()))
                    }
                )
            }

            composable(
                route = Screen.AddPredefinedSubscription.route,
                arguments = listOf(navArgument("subscriptionId") { type = NavType.IntType })
            ) { backStackEntry ->
                val subscriptionId = backStackEntry.arguments?.getInt("subscriptionId") ?: 0
                val predefinedSubscription = PredefinedSubscriptions.subscriptions[subscriptionId]
                
                AddPredefinedSubscriptionScreen(
                    predefinedSubscription = predefinedSubscription,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.AddCustomSubscription.route) {
                AddSubscriptionScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.AddSubscription.route) {
                AddSubscriptionScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Calendar.route) {
                CalendarScreen()
            }
            
            composable(Screen.Analytics.route) {
                AnalyticsScreen()
            }
            
            composable(Screen.Profile.route) {
                Text("Profil - Yakında") // Geçici olarak
            }
        }
    }
}