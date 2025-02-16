package com.denizcan.subscriptiontracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.subscriptiontracker.model.PaymentPeriod
import com.denizcan.subscriptiontracker.model.Subscription
import com.denizcan.subscriptiontracker.model.SubscriptionCategory
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed class SubscriptionState {
    object Loading : SubscriptionState()
    data class Success(
        val subscriptions: List<Subscription>,
        val totalMonthlyExpense: Double,
        val upcomingPayment: Subscription?
    ) : SubscriptionState()
    data class Error(val message: String) : SubscriptionState()
}

data class PlanHistoryEntry(
    val plan: String,
    val price: Double,
    val startDate: Date,
    val endDate: Date?
)

class SubscriptionViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _subscriptionState = MutableStateFlow<SubscriptionState>(SubscriptionState.Loading)
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        loadSubscriptions()
    }

    fun refresh() {
        loadSubscriptions()
    }

    fun refreshSubscriptionDetails(subscriptionId: String) {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                
                val userId = auth.currentUser?.uid ?: throw Exception("Kullanıcı oturum açmamış")
                
                // Üyelik bilgilerini yenile
                val docRef = firestore.collection("users")
                    .document(userId)
                    .collection("subscriptions")
                    .document(subscriptionId)
                    .get()
                    .await()

                if (docRef.exists()) {
                    val data = docRef.data
                    if (data != null) {
                        val subscription = Subscription(
                            id = docRef.id,
                            name = data["name"] as? String ?: "",
                            plan = data["plan"] as? String ?: "",
                            price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                            category = try {
                                SubscriptionCategory.valueOf(data["category"] as? String ?: SubscriptionCategory.OTHER.name)
                            } catch (e: Exception) {
                                SubscriptionCategory.OTHER
                            },
                            startDate = (data["startDate"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                            nextPaymentDate = (data["nextPaymentDate"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                            paymentPeriod = try {
                                PaymentPeriod.valueOf(data["paymentPeriod"] as? String ?: PaymentPeriod.MONTHLY.name)
                            } catch (e: Exception) {
                                PaymentPeriod.MONTHLY
                            }
                        )
                        
                        // Plan geçmişini yenile
                        val planHistorySnapshot = firestore.collection("users")
                            .document(userId)
                            .collection("subscriptions")
                            .document(subscriptionId)
                            .collection("planHistory")
                            .orderBy("startDate")
                            .get()
                            .await()

                        val planHistory = planHistorySnapshot.documents.mapNotNull { doc ->
                            try {
                                val historyData = doc.data ?: return@mapNotNull null
                                PlanHistoryEntry(
                                    plan = historyData["plan"] as? String ?: return@mapNotNull null,
                                    price = (historyData["price"] as? Number)?.toDouble() ?: return@mapNotNull null,
                                    startDate = (historyData["startDate"] as? com.google.firebase.Timestamp)?.toDate() ?: return@mapNotNull null,
                                    endDate = (historyData["endDate"] as? com.google.firebase.Timestamp)?.toDate()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                        // State'i güncelle
                        _subscriptionState.value = SubscriptionState.Success(
                            subscriptions = (subscriptionState.value as? SubscriptionState.Success)?.subscriptions?.map {
                                if (it.id == subscriptionId) subscription else it
                            } ?: listOf(subscription),
                            totalMonthlyExpense = calculateMonthlyTotal((subscriptionState.value as? SubscriptionState.Success)?.subscriptions ?: emptyList()),
                            upcomingPayment = findUpcomingPayment((subscriptionState.value as? SubscriptionState.Success)?.subscriptions ?: emptyList())
                        )
                    }
                }
            } catch (e: Exception) {
                println("Yenileme hatası: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
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

                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection("subscriptions")
                    .get()
                    .await()

                println("Firestore sorgu sonucu: ${snapshot.documents.size} döküman bulundu")

                val subscriptions = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        println("Döküman verisi: $data")

                        val subscription = Subscription(
                            id = doc.id,
                            name = data["name"] as? String ?: return@mapNotNull null,
                            plan = data["plan"] as? String ?: "",
                            price = (data["price"] as? Number)?.toDouble() ?: return@mapNotNull null,
                            category = try {
                                SubscriptionCategory.valueOf(data["category"] as? String ?: SubscriptionCategory.OTHER.name)
                            } catch (e: Exception) {
                                SubscriptionCategory.OTHER
                            },
                            startDate = (data["startDate"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                            nextPaymentDate = (data["nextPaymentDate"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                            paymentPeriod = try {
                                PaymentPeriod.valueOf(data["paymentPeriod"] as? String ?: PaymentPeriod.MONTHLY.name)
                            } catch (e: Exception) {
                                PaymentPeriod.MONTHLY
                            }
                        )

                        // Plan geçmişini al ve son planın başlangıç tarihini kullan
                        val planHistorySnapshot = firestore.collection("users")
                            .document(userId)
                            .collection("subscriptions")
                            .document(doc.id)
                            .collection("planHistory")
                            .orderBy("startDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .await()

                        val lastPlan = if (!planHistorySnapshot.isEmpty) {
                            val lastPlanDoc = planHistorySnapshot.documents.first()
                            val lastPlanData = lastPlanDoc.data
                            if (lastPlanData != null) {
                                val startDate = (lastPlanData["startDate"] as? com.google.firebase.Timestamp)?.toDate()
                                if (startDate != null) {
                                    subscription.copy(
                                        startDate = startDate,
                                        nextPaymentDate = calculateCurrentNextPaymentDate(startDate, subscription.paymentPeriod)
                                    )
                                } else subscription
                            } else subscription
                        } else subscription

                        println("İşlenen abonelik: $lastPlan")
                        lastPlan
                    } catch (e: Exception) {
                        println("Döküman işleme hatası: ${e.message}")
                        null
                    }
                }

                println("Son abonelik listesi (${subscriptions.size} adet):")
                subscriptions.forEach { println("- ${it.name}: ${it.price} TL") }
                
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

    fun calculateNextPaymentDate(startDate: Date, period: PaymentPeriod): Date {
        // İlk ödeme başlangıç tarihinde olacak
        return startDate
    }

    fun calculateCurrentNextPaymentDate(startDate: Date, period: PaymentPeriod): Date {
        val calendar = Calendar.getInstance()
        val now = Calendar.getInstance()
        calendar.time = startDate

        // Başlangıç tarihinden itibaren şu anki tarihe kadar olan periyot sayısını hesapla
        when (period) {
            PaymentPeriod.MONTHLY -> {
                while (calendar.before(now) || calendar.equals(now)) {
                    calendar.add(Calendar.MONTH, 1)
                }
            }
            PaymentPeriod.QUARTERLY -> {
                while (calendar.before(now) || calendar.equals(now)) {
                    calendar.add(Calendar.MONTH, 3)
                }
            }
            PaymentPeriod.YEARLY -> {
                while (calendar.before(now) || calendar.equals(now)) {
                    calendar.add(Calendar.YEAR, 1)
                }
            }
        }
        
        return calendar.time
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

                val nextPaymentDate = calculateNextPaymentDate(startDate, paymentPeriod)
                val subscriptionId = UUID.randomUUID().toString()
                
                val subscription = Subscription(
                    id = subscriptionId,
                    name = name,
                    plan = plan,
                    price = price,
                    category = category,
                    startDate = startDate,
                    nextPaymentDate = nextPaymentDate,
                    paymentPeriod = paymentPeriod
                )

                val subscriptionMap = hashMapOf(
                    "name" to subscription.name,
                    "plan" to subscription.plan,
                    "price" to subscription.price,
                    "category" to subscription.category.name,
                    "startDate" to com.google.firebase.Timestamp(subscription.startDate),
                    "nextPaymentDate" to com.google.firebase.Timestamp(subscription.nextPaymentDate),
                    "paymentPeriod" to subscription.paymentPeriod.name
                )

                try {
                    // Önce aboneliği kaydet
                    firestore.collection("users")
                        .document(userId)
                        .collection("subscriptions")
                        .document(subscription.id)
                        .set(subscriptionMap)
                        .await()

                    // Sonra ilk plan geçmişini oluştur
                    createInitialPlanHistory(
                        userId = userId,
                        subscriptionId = subscription.id,
                        plan = plan,
                        price = price,
                        startDate = startDate
                    )

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
        return subscriptions.sumOf { it.price }
    }

    private fun findUpcomingPayment(subscriptions: List<Subscription>): Subscription? {
        return subscriptions.minByOrNull { it.nextPaymentDate }
    }

    fun deleteSubscription(subscriptionId: String) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    _subscriptionState.value = SubscriptionState.Error("Oturum açık değil")
                    return@launch
                }

                firestore.collection("users")
                    .document(userId)
                    .collection("subscriptions")
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
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    _subscriptionState.value = SubscriptionState.Error("Oturum açık değil")
                    return@launch
                }

                val subscriptionMap = hashMapOf(
                    "name" to subscription.name,
                    "plan" to subscription.plan,
                    "price" to subscription.price,
                    "category" to subscription.category.name,
                    "startDate" to com.google.firebase.Timestamp(subscription.startDate),
                    "nextPaymentDate" to com.google.firebase.Timestamp(subscription.nextPaymentDate),
                    "paymentPeriod" to subscription.paymentPeriod.name
                )

                firestore.collection("users")
                    .document(userId)
                    .collection("subscriptions")
                    .document(subscription.id)
                    .update(subscriptionMap.toMap())
                    .await()

                // State'i hemen güncelle
                val currentState = subscriptionState.value
                if (currentState is SubscriptionState.Success) {
                    val updatedSubscriptions = currentState.subscriptions.map { 
                        if (it.id == subscription.id) subscription else it 
                    }
                    _subscriptionState.value = SubscriptionState.Success(
                        subscriptions = updatedSubscriptions,
                        totalMonthlyExpense = calculateMonthlyTotal(updatedSubscriptions),
                        upcomingPayment = findUpcomingPayment(updatedSubscriptions)
                    )
                }

                loadSubscriptions() // Arka planda tam listeyi yenile
            } catch (e: Exception) {
                _subscriptionState.value = SubscriptionState.Error(e.message ?: "Üyelik güncellenemedi")
            }
        }
    }

    fun getSubscriptionById(id: String): Flow<Subscription?> = flow {
        try {
            val db = Firebase.firestore
            val auth = Firebase.auth
            val userId = auth.currentUser?.uid ?: throw Exception("Kullanıcı oturumu bulunamadı")

            val docRef = db.collection("users")
                .document(userId)
                .collection("subscriptions")
                .document(id)

            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val data = snapshot.data
                if (data != null) {
                    val subscription = Subscription(
                        id = snapshot.id,
                        name = data["name"] as? String ?: "",
                        plan = data["plan"] as? String ?: "",
                        price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                        category = try {
                            SubscriptionCategory.valueOf(data["category"] as? String ?: SubscriptionCategory.OTHER.name)
                        } catch (e: Exception) {
                            SubscriptionCategory.OTHER
                        },
                        startDate = (data["startDate"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                        nextPaymentDate = (data["nextPaymentDate"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                        paymentPeriod = try {
                            PaymentPeriod.valueOf(data["paymentPeriod"] as? String ?: PaymentPeriod.MONTHLY.name)
                        } catch (e: Exception) {
                            PaymentPeriod.MONTHLY
                        }
                    )
                    emit(subscription)
                } else {
                    emit(null)
                }
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            println("Üyelik detayları alınırken hata oluştu: ${e.message}")
            emit(null)
        }
    }

    fun upgradePlan(
        subscriptionId: String,
        newPlan: String,
        newPrice: Double,
        upgradeDate: Date
    ) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("Kullanıcı oturum açmamış")
                
                // Önce mevcut plan geçmişini al ve son planın bitiş tarihini güncelle
                val planHistorySnapshot = firestore.collection("users")
                    .document(userId)
                    .collection("subscriptions")
                    .document(subscriptionId)
                    .collection("planHistory")
                    .orderBy("startDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()

                if (!planHistorySnapshot.isEmpty) {
                    val lastPlanDoc = planHistorySnapshot.documents.first()
                    // Son planın bitiş tarihini güncelle
                    firestore.collection("users")
                        .document(userId)
                        .collection("subscriptions")
                        .document(subscriptionId)
                        .collection("planHistory")
                        .document(lastPlanDoc.id)
                        .update("endDate", com.google.firebase.Timestamp(upgradeDate))
                        .await()
                }

                // Yeni plan geçmişi ekle
                val newHistoryEntry = hashMapOf(
                    "plan" to newPlan,
                    "price" to newPrice,
                    "startDate" to com.google.firebase.Timestamp(upgradeDate),
                    "endDate" to null
                )

                firestore.collection("users")
                    .document(userId)
                    .collection("subscriptions")
                    .document(subscriptionId)
                    .collection("planHistory")
                    .add(newHistoryEntry)
                    .await()

                // Aboneliği güncelle
                val updates = hashMapOf<String, Any>(
                    "plan" to newPlan,
                    "price" to newPrice,
                    "startDate" to com.google.firebase.Timestamp(upgradeDate)
                )

                firestore.collection("users")
                    .document(userId)
                    .collection("subscriptions")
                    .document(subscriptionId)
                    .update(updates)
                    .await()

                // Subscription'ı yenile
                val docRef = firestore.collection("users")
                    .document(userId)
                    .collection("subscriptions")
                    .document(subscriptionId)
                    .get()
                    .await()

                if (docRef.exists()) {
                    val data = docRef.data
                    if (data != null) {
                        val updatedSubscription = Subscription(
                            id = docRef.id,
                            name = data["name"] as? String ?: "",
                            plan = data["plan"] as? String ?: "",
                            price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                            category = try {
                                SubscriptionCategory.valueOf(data["category"] as? String ?: SubscriptionCategory.OTHER.name)
                            } catch (e: Exception) {
                                SubscriptionCategory.OTHER
                            },
                            startDate = (data["startDate"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                            nextPaymentDate = (data["nextPaymentDate"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                            paymentPeriod = try {
                                PaymentPeriod.valueOf(data["paymentPeriod"] as? String ?: PaymentPeriod.MONTHLY.name)
                            } catch (e: Exception) {
                                PaymentPeriod.MONTHLY
                            }
                        )

                        // Plan geçmişini al
                        val planHistorySnapshot = firestore.collection("users")
                            .document(userId)
                            .collection("subscriptions")
                            .document(subscriptionId)
                            .collection("planHistory")
                            .orderBy("startDate")
                            .get()
                            .await()

                        val planHistory = planHistorySnapshot.documents.mapNotNull { doc ->
                            try {
                                val historyData = doc.data ?: return@mapNotNull null
                                PlanHistoryEntry(
                                    plan = historyData["plan"] as? String ?: return@mapNotNull null,
                                    price = (historyData["price"] as? Number)?.toDouble() ?: return@mapNotNull null,
                                    startDate = (historyData["startDate"] as? com.google.firebase.Timestamp)?.toDate() ?: return@mapNotNull null,
                                    endDate = (historyData["endDate"] as? com.google.firebase.Timestamp)?.toDate()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                        // State'i güncelle
                        _subscriptionState.value = SubscriptionState.Success(
                            subscriptions = (subscriptionState.value as? SubscriptionState.Success)?.subscriptions?.map {
                                if (it.id == subscriptionId) updatedSubscription else it
                            } ?: listOf(updatedSubscription),
                            totalMonthlyExpense = calculateMonthlyTotal((subscriptionState.value as? SubscriptionState.Success)?.subscriptions ?: emptyList()),
                            upcomingPayment = findUpcomingPayment((subscriptionState.value as? SubscriptionState.Success)?.subscriptions ?: emptyList())
                        )
                    }
                }

                loadSubscriptions()
            } catch (e: Exception) {
                println("Plan yükseltme hatası: ${e.message}")
                _subscriptionState.value = SubscriptionState.Error(e.message ?: "Plan yükseltilirken bir hata oluştu")
            }
        }
    }

    // Yeni abonelik eklerken ilk plan geçmişini de oluştur
    private suspend fun createInitialPlanHistory(
        userId: String,
        subscriptionId: String,
        plan: String,
        price: Double,
        startDate: Date
    ) {
        val historyEntry = hashMapOf(
            "plan" to plan,
            "price" to price,
            "startDate" to com.google.firebase.Timestamp(startDate),
            "endDate" to null
        )

        firestore.collection("users")
            .document(userId)
            .collection("subscriptions")
            .document(subscriptionId)
            .collection("planHistory")
            .add(historyEntry)
            .await()
    }

    fun getPlanHistory(subscriptionId: String): Flow<List<PlanHistoryEntry>> = flow {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("Kullanıcı oturum açmamış")
            
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("subscriptions")
                .document(subscriptionId)
                .collection("planHistory")
                .orderBy("startDate")
                .get()
                .await()

            val history = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    PlanHistoryEntry(
                        plan = data["plan"] as? String ?: return@mapNotNull null,
                        price = (data["price"] as? Number)?.toDouble() ?: return@mapNotNull null,
                        startDate = (data["startDate"] as? com.google.firebase.Timestamp)?.toDate() ?: return@mapNotNull null,
                        endDate = (data["endDate"] as? com.google.firebase.Timestamp)?.toDate()
                    )
                } catch (e: Exception) {
                    println("Plan geçmişi dönüştürme hatası: ${e.message}")
                    null
                }
            }
            emit(history)
        } catch (e: Exception) {
            println("Plan geçmişi yükleme hatası: ${e.message}")
            emit(emptyList())
        }
    }

    fun getEffectivePlanForDate(subscriptionId: String, date: Date): Flow<PlanHistoryEntry?> = flow {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("Kullanıcı oturum açmamış")
            
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("subscriptions")
                .document(subscriptionId)
                .collection("planHistory")
                .orderBy("startDate")
                .get()
                .await()

            val effectivePlan = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    PlanHistoryEntry(
                        plan = data["plan"] as? String ?: return@mapNotNull null,
                        price = (data["price"] as? Number)?.toDouble() ?: return@mapNotNull null,
                        startDate = (data["startDate"] as? com.google.firebase.Timestamp)?.toDate() ?: return@mapNotNull null,
                        endDate = (data["endDate"] as? com.google.firebase.Timestamp)?.toDate()
                    )
                } catch (e: Exception) {
                    println("Plan dönüştürme hatası: ${e.message}")
                    null
                }
            }.firstOrNull { entry ->
                date >= entry.startDate && (entry.endDate == null || date < entry.endDate)
            }

            emit(effectivePlan)
        } catch (e: Exception) {
            println("Etkin plan alma hatası: ${e.message}")
            emit(null)
        }
    }
} 