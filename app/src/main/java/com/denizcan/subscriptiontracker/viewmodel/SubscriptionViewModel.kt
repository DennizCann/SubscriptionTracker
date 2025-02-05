package com.denizcan.subscriptiontracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.subscriptiontracker.model.PaymentPeriod
import com.denizcan.subscriptiontracker.model.Subscription
import com.denizcan.subscriptiontracker.model.SubscriptionCategory
import com.denizcan.subscriptiontracker.ui.screens.subscription.calculateNextPaymentDate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

sealed class SubscriptionState {
    object Loading : SubscriptionState()
    data class Success(
        val subscriptions: List<Subscription>,
        val totalMonthlyExpense: Double,
        val upcomingPayment: Subscription?
    ) : SubscriptionState()
    data class Error(val message: String) : SubscriptionState()
}

class SubscriptionViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _subscriptionState = MutableStateFlow<SubscriptionState>(SubscriptionState.Loading)
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState

    init {
        loadSubscriptions()
    }

    fun refresh() {
        loadSubscriptions()
    }

    private fun loadSubscriptions() {
        viewModelScope.launch {
            try {
                _subscriptionState.value = SubscriptionState.Loading
                val userId = auth.currentUser?.uid
                println("Abonelikler yükleniyor - Kullanıcı ID: $userId")

                if (userId == null) {
                    println("Kullanıcı ID bulunamadı")
                    _subscriptionState.value = SubscriptionState.Error("Oturum açık değil")
                    return@launch
                }

                val snapshot = firestore.collection("subscriptions")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isActive", true)
                    .get()
                    .await()

                println("Firestore sorgu sonucu: ${snapshot.documents.size} döküman bulundu")

                val subscriptions = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        println("Döküman verisi: $data")

                        val subscription = Subscription(
                            id = doc.id,
                            userId = data["userId"] as? String ?: return@mapNotNull null,
                            name = data["name"] as? String ?: return@mapNotNull null,
                            plan = data["plan"] as? String ?: "",
                            price = (data["price"] as? Number)?.toDouble() ?: return@mapNotNull null,
                            category = try {
                                SubscriptionCategory.valueOf(data["category"] as? String ?: SubscriptionCategory.OTHER.name)
                            } catch (e: Exception) {
                                SubscriptionCategory.OTHER
                            },
                            paymentPeriod = try {
                                PaymentPeriod.valueOf(data["paymentPeriod"] as? String ?: PaymentPeriod.MONTHLY.name)
                            } catch (e: Exception) {
                                PaymentPeriod.MONTHLY
                            },
                            startDate = (data["startDate"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                            nextPaymentDate = (data["nextPaymentDate"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                            isActive = data["isActive"] as? Boolean ?: true
                        )
                        println("İşlenen abonelik: $subscription")
                        subscription
                    } catch (e: Exception) {
                        println("Döküman işleme hatası: ${e.message}")
                        null
                    }
                }

                println("Son abonelik listesi (${subscriptions.size} adet):")
                subscriptions.forEach { println("- ${it.name}: ${it.price} TL (${it.paymentPeriod})") }
                
                val totalMonthly = calculateMonthlyTotal(subscriptions)
                println("Toplam aylık gider: $totalMonthly TL")
                
                val nextPayment = findUpcomingPayment(subscriptions)
                println("Bir sonraki ödeme: ${nextPayment?.name ?: "Yok"}")

                _subscriptionState.value = SubscriptionState.Success(
                    subscriptions = subscriptions,
                    totalMonthlyExpense = totalMonthly,
                    upcomingPayment = nextPayment
                )
            } catch (e: Exception) {
                println("Abonelik yükleme hatası: ${e.message}")
                println("Hata detayı: ${e.stackTraceToString()}")
                _subscriptionState.value = SubscriptionState.Error(e.message ?: "Abonelikler yüklenirken bir hata oluştu")
            }
        }
    }

    fun addSubscription(
        name: String,
        plan: String,
        price: Double,
        category: SubscriptionCategory,
        paymentPeriod: PaymentPeriod,
        startDate: Date
    ) {
        viewModelScope.launch {
            try {
                _subscriptionState.value = SubscriptionState.Loading
                
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    println("Hata: Kullanıcı oturum açmamış")
                    _subscriptionState.value = SubscriptionState.Error("Lütfen önce oturum açın")
                    return@launch
                }
                println("Üyelik ekleniyor - Kullanıcı ID: $userId")

                val subscription = Subscription(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    name = name,
                    plan = plan,
                    price = price,
                    category = category,
                    paymentPeriod = paymentPeriod,
                    startDate = startDate,
                    nextPaymentDate = calculateNextPaymentDate(startDate, paymentPeriod),
                    isActive = true
                )

                val subscriptionMap = hashMapOf(
                    "id" to subscription.id,
                    "userId" to subscription.userId,
                    "name" to subscription.name,
                    "plan" to subscription.plan,
                    "price" to subscription.price,
                    "category" to subscription.category.name,
                    "paymentPeriod" to subscription.paymentPeriod.name,
                    "startDate" to com.google.firebase.Timestamp(subscription.startDate),
                    "nextPaymentDate" to com.google.firebase.Timestamp(subscription.nextPaymentDate),
                    "isActive" to subscription.isActive
                )

                try {
                    firestore.collection("subscriptions")
                        .document(subscription.id)
                        .set(subscriptionMap)
                        .await()

                    println("Üyelik başarıyla kaydedildi - ID: ${subscription.id}")
                    loadSubscriptions()
                } catch (e: Exception) {
                    println("Firestore kayıt hatası: ${e.message}")
                    println("Hata detayı: ${e.stackTraceToString()}")
                    _subscriptionState.value = SubscriptionState.Error("Üyelik kaydedilemedi: ${e.message}")
                }
            } catch (e: Exception) {
                println("Üyelik ekleme hatası: ${e.message}")
                println("Stack trace: ${e.stackTraceToString()}")
                _subscriptionState.value = SubscriptionState.Error(e.message ?: "Üyelik eklenirken bir hata oluştu")
            }
        }
    }

    private fun calculateMonthlyTotal(subscriptions: List<Subscription>): Double {
        return subscriptions.sumOf { subscription ->
            when (subscription.paymentPeriod) {
                PaymentPeriod.MONTHLY -> subscription.price
                PaymentPeriod.YEARLY -> subscription.price / 12
                PaymentPeriod.QUARTERLY -> subscription.price / 3
            }
        }
    }

    private fun findUpcomingPayment(subscriptions: List<Subscription>): Subscription? {
        return subscriptions.minByOrNull { it.nextPaymentDate }
    }

    fun deleteSubscription(subscriptionId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("subscriptions")
                    .document(subscriptionId)
                    .delete()
                    .await()

                loadSubscriptions() // Listeyi yenile
            } catch (e: Exception) {
                _subscriptionState.value = SubscriptionState.Error(e.message ?: "Üyelik silinemedi")
            }
        }
    }

    fun updateSubscription(subscription: Subscription) {
        viewModelScope.launch {
            try {
                val subscriptionMap = hashMapOf(
                    "name" to subscription.name,
                    "plan" to subscription.plan,
                    "price" to subscription.price,
                    "startDate" to com.google.firebase.Timestamp(subscription.startDate),
                    "nextPaymentDate" to com.google.firebase.Timestamp(subscription.nextPaymentDate),
                    "paymentPeriod" to subscription.paymentPeriod.name,
                    "category" to subscription.category.name,
                    "isActive" to subscription.isActive
                )

                firestore.collection("subscriptions")
                    .document(subscription.id)
                    .update(subscriptionMap.toMap())
                    .await()

                loadSubscriptions() // Listeyi yenile
            } catch (e: Exception) {
                _subscriptionState.value = SubscriptionState.Error(e.message ?: "Üyelik güncellenemedi")
            }
        }
    }
} 