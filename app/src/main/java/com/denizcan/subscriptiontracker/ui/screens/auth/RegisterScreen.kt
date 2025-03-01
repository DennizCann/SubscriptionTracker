package com.denizcan.subscriptiontracker.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.denizcan.subscriptiontracker.viewmodel.AuthViewModel
import com.denizcan.subscriptiontracker.viewmodel.AuthState
import com.denizcan.subscriptiontracker.ui.theme.LocalSpacing
import com.denizcan.subscriptiontracker.ui.theme.getScreenClass
import com.denizcan.subscriptiontracker.ui.theme.ScreenClass

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val spacing = LocalSpacing.current
    val screenClass = getScreenClass()
    
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val maxWidth = when (screenClass) {
            ScreenClass.COMPACT -> Modifier.fillMaxWidth()
            else -> Modifier.width(400.dp)
        }

        Column(
            modifier = Modifier
                .then(maxWidth)
                .padding(spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            Text(
                text = "Kayıt Ol",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = spacing.large)
            )

            var name by remember { mutableStateOf("") }
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }

            if (authState is AuthState.Error) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(spacing.medium),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Ad Soyad") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-posta") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Şifre") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Button(
                onClick = { viewModel.register(name, email, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = authState !is AuthState.Loading && 
                    name.isNotBlank() && 
                    email.isNotBlank() && 
                    password.isNotBlank()
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Kayıt Ol")
                }
            }

            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.padding(top = spacing.medium)
            ) {
                Text("Zaten hesabın var mı? Giriş yap")
            }
        }
    }
} 