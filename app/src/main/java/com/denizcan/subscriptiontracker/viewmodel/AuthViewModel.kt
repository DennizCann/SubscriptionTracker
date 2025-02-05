package com.denizcan.subscriptiontracker.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.denizcan.subscriptiontracker.R

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val context = application

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    private val _userName = MutableStateFlow<String>("")
    val userName: StateFlow<String> = _userName

    private val googleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        GoogleSignIn.getClient(context, gso)
    }

    init {
        auth.currentUser?.let { user ->
            _authState.value = AuthState.Success(user)
            loadUserData(user.uid)
        }
    }

    fun signInWithGoogle(activity: Activity, launcher: ActivityResultLauncher<Intent>) {
        viewModelScope.launch {
            try {
                println("Google Sign-In başlatılıyor")
                _authState.value = AuthState.Loading
                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            } catch (e: Exception) {
                println("Google Sign-In başlatma hatası: ${e.message}")
                _authState.value = AuthState.Error("Google ile giriş başlatılamadı")
            }
        }
    }

    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            println("Google Sign-In sonucu işleniyor")
            _authState.value = AuthState.Loading
            val account = task.getResult(ApiException::class.java)
            println("Google hesabı alındı: ${account.email}")
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            viewModelScope.launch {
                try {
                    println("Firebase kimlik doğrulama başladı")
                    val result = auth.signInWithCredential(credential).await()
                    result.user?.let { user ->
                        println("Firebase kimlik doğrulama başarılı: ${user.email}")
                        val userDoc = hashMapOf(
                            "name" to user.displayName,
                            "email" to user.email
                        )

                        firestore.collection("users")
                            .document(user.uid)
                            .set(userDoc)
                            .await()

                        _userName.value = user.displayName ?: ""
                        _authState.value = AuthState.Success(user)
                        println("AuthState güncellendi: Success")
                    }
                } catch (e: Exception) {
                    println("Firebase hatası: ${e.message}")
                    _authState.value = AuthState.Error(e.message ?: "Google ile giriş başarısız")
                }
            }
        } catch (e: ApiException) {
            println("Google Sign-In hatası: ${e.statusCode} - ${e.message}")
            _authState.value = AuthState.Error("Google ile giriş başarısız: ${e.statusCode}")
        }
    }

    private fun loadUserData(userId: String) {
        viewModelScope.launch {
            try {
                val userDoc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()

                _userName.value = userDoc.getString("name") ?: ""
            } catch (e: Exception) {
                // Hata durumunda işlem yapılabilir
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let { user ->
                    loadUserData(user.uid)
                    _authState.value = AuthState.Success(user)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Giriş başarısız")
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.let { user ->
                    val userDoc = hashMapOf(
                        "name" to name,
                        "email" to email
                    )
                    firestore.collection("users")
                        .document(user.uid)
                        .set(userDoc)
                        .await()

                    _authState.value = AuthState.Success(user)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Kayıt başarısız")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                println("Oturum kapatma işlemi başladı")
                _authState.value = AuthState.Loading

                // Önce mevcut Google hesabını kontrol edelim
                val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
                if (googleAccount != null) {
                    println("Google hesabı bulundu, Google oturumu kapatılıyor")
                    try {
                        googleSignInClient.revokeAccess().await() // Google bağlantısını tamamen kaldır
                        googleSignInClient.signOut().await()
                        println("Google oturumu başarıyla kapatıldı")
                    } catch (e: Exception) {
                        println("Google oturumu kapatma hatası: ${e.message}")
                    }
                }

                // Firebase oturumunu kapatalım
                auth.signOut()
                println("Firebase oturumu kapatıldı")

                // Tüm state'leri sıfırlayalım
                _userName.value = ""
                
                // Oturum durumunu kontrol et
                if (auth.currentUser == null && GoogleSignIn.getLastSignedInAccount(context) == null) {
                    println("Tüm oturumlar başarıyla kapatıldı, Initial state'e geçiliyor")
                    _authState.value = AuthState.Initial
                } else {
                    println("Oturum kapatma başarısız oldu, hata state'ine geçiliyor")
                    throw Exception("Oturum tam olarak kapatılamadı")
                }

                println("Oturum kapatma işlemi tamamlandı")
            } catch (e: Exception) {
                println("Oturum kapatma hatası: ${e.message}")
                println("Hata detayı: ${e.stackTraceToString()}")
                _authState.value = AuthState.Error("Oturum kapatılırken bir hata oluştu: ${e.message}")
            }
        }
    }

    // Oturum durumunu kontrol etmek için yeni bir fonksiyon
    fun checkAuthState() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                val googleAccount = GoogleSignIn.getLastSignedInAccount(context)

                if (currentUser == null && googleAccount == null) {
                    println("Aktif oturum bulunamadı, Initial state'e geçiliyor")
                    _authState.value = AuthState.Initial
                    _userName.value = ""
                } else if (currentUser != null) {
                    println("Aktif Firebase oturumu bulundu")
                    _authState.value = AuthState.Success(currentUser)
                    loadUserData(currentUser.uid)
                }
            } catch (e: Exception) {
                println("Oturum durumu kontrol hatası: ${e.message}")
                _authState.value = AuthState.Error("Oturum durumu kontrol edilirken hata oluştu")
            }
        }
    }

    fun getCurrentUser() = auth.currentUser
}