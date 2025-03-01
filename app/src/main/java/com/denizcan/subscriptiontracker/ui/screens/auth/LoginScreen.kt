package com.denizcan.subscriptiontracker.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.denizcan.subscriptiontracker.R
import com.denizcan.subscriptiontracker.viewmodel.AuthViewModel
import com.denizcan.subscriptiontracker.viewmodel.AuthState
import com.denizcan.subscriptiontracker.ui.theme.LocalSpacing
import com.denizcan.subscriptiontracker.ui.theme.getScreenClass
import com.denizcan.subscriptiontracker.ui.theme.ScreenClass

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onGoogleSignIn: () -> Unit,
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val spacing = LocalSpacing.current
    val screenClass = getScreenClass()
    
    LaunchedEffect(authState) {
        println("LoginScreen - AuthState değişti: $authState")
        if (authState is AuthState.Success) {
            println("LoginScreen - Giriş başarılı, ana sayfaya yönlendiriliyor")
            onLoginSuccess()
        }
    }

    if (authState is AuthState.Error) {
        Toast.makeText(
            LocalContext.current,
            (authState as AuthState.Error).message,
            Toast.LENGTH_LONG
        ).show()
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
                text = "Giriş Yap",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = spacing.large)
            )

            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }

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
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Giriş Yap")
                }
            }

            Divider(
                modifier = Modifier
                    .padding(vertical = spacing.medium)
                    .fillMaxWidth()
            )

            OutlinedButton(
                onClick = onGoogleSignIn,
                modifier = Modifier.fillMaxWidth(),
                enabled = authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = spacing.small)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(spacing.small))
                        Text("Google ile Giriş Yap")
                    }
                }
            }

            TextButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.padding(top = spacing.medium)
            ) {
                Text("Hesabın yok mu? Kayıt ol")
            }
        }
    }
} 