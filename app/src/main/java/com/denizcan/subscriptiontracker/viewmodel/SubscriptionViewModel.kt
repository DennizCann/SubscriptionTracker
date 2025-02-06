package com.denizcan.subscriptiontracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.subscriptiontracker.model.PaymentPeriod
import com.denizcan.subscriptiontracker.model.Subscription
import com.denizcan.subscriptiontracker.model.SubscriptionCategory
import com.denizcan.subscriptiontracker.ui.screens.subscription.calculateNextPaymentDate
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
                        println("İşlenen abonelik: $subscription")
                        subscription
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
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        
        when (period) {
            PaymentPeriod.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            PaymentPeriod.QUARTERLY -> calendar.add(Calendar.MONTH, 3)
            PaymentPeriod.YEARLY -> calendar.add(Calendar.YEAR, 1)
        }
        
        return calendar.time
    }

    fun calculateCurrentNextPaymentDate(startDate: Date, period: PaymentPeriod): Date {
        val calendar = Calendar.getInstance()
        val now = Calendar.getInstance()
        calendar.time = startDate

        // Başlangıç tarihinden itibaren şu anki tarihe kadar olan periyot sayısını hesapla
        var periodsPassed = 0
        when (period) {
            PaymentPeriod.MONTHLY -> {
                periodsPassed = ((now.timeInMillis - calendar.timeInMillis) / 
                    (1000L * 60 * 60 * 24 * 30)).toInt()
                calendar.add(Calendar.MONTH, periodsPassed + 1)
            }
            PaymentPeriod.QUARTERLY -> {
                periodsPassed = ((now.timeInMillis - calendar.timeInMillis) / 
                    (1000L * 60 * 60 * 24 * 90)).toInt()
                calendar.add(Calendar.MONTH, (periodsPassed + 1) * 3)
            }
            PaymentPeriod.YEARLY -> {
                periodsPassed = ((now.timeInMillis - calendar.timeInMillis) / 
                    (1000L * 60 * 60 * 24 * 365)).toInt()
                calendar.add(Calendar.YEAR, periodsPassed + 1)
            }
        }

        // Eğer hesaplanan tarih geçmişte kaldıysa bir sonraki periyoda geç
        while (calendar.before(now)) {
            when (period) {
                PaymentPeriod.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                PaymentPeriod.QUARTERLY -> calendar.add(Calendar.MONTH, 3)
                PaymentPeriod.YEARLY -> calendar.add(Calendar.YEAR, 1)
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
                
                val subscription = Subscription(
                    id = UUID.randomUUID().toString(),
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
                    firestore.collection("users")
                        .document(userId)
                        .collection("subscriptions")
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

                loadSubscriptions() // Listeyi yenile
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
        effectiveDate: Date
    ) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    _subscriptionState.value = SubscriptionState.Error("Oturum açık değil")
                    return@launch
                }

                // Mevcut aboneliği al
                val docRef = firestore.collection("users")
                    .document(userId)
                    .collection("subscriptions")
                    .document(subscriptionId)

                val snapshot = docRef.get().await()
                if (!snapshot.exists()) {
                    _subscriptionState.value = SubscriptionState.Error("Abonelik bulunamadı")
                    return@launch
                }

                val data = snapshot.data
                if (data == null) {
                    _subscriptionState.value = SubscriptionState.Error("Abonelik verisi bulunamadı")
                    return@launch
                }

                // Mevcut planı geçmiş planlara ekle
                val currentPlan = PlanHistoryEntry(
                    plan = data["plan"] as String,
                    price = (data["price"] as Number).toDouble(),
                    startDate = (data["startDate"] as com.google.firebase.Timestamp).toDate(),
                    endDate = effectiveDate
                )

                // Yeni planı oluştur
                val newPlanEntry = PlanHistoryEntry(
                    plan = newPlan,
                    price = newPrice,
                    startDate = effectiveDate,
                    endDate = null
                )

                // Batch işlemi başlat
                val batch = firestore.batch()

                // Plan geçmişini güncelle
                val planHistoryRef = docRef.collection("planHistory")
                
                // Mevcut planı geçmişe ekle
                val currentPlanDoc = planHistoryRef.document()
                batch.set(currentPlanDoc, hashMapOf(
                    "plan" to currentPlan.plan,
                    "price" to currentPlan.price,
                    "startDate" to com.google.firebase.Timestamp(currentPlan.startDate),
                    "endDate" to com.google.firebase.Timestamp(currentPlan.endDate!!)
                ))

                // Yeni planı ekle
                val newPlanDoc = planHistoryRef.document()
                batch.set(newPlanDoc, hashMapOf(
                    "plan" to newPlanEntry.plan,
                    "price" to newPlanEntry.price,
                    "startDate" to com.google.firebase.Timestamp(newPlanEntry.startDate),
                    "endDate" to null
                ))

                // Ana abonelik dokümanını güncelle
                batch.update(docRef, hashMapOf<String, Any>(
                    "plan" to newPlan,
                    "price" to newPrice,
                    "lastUpgradeDate" to com.google.firebase.Timestamp(effectiveDate)
                ))

                // Batch işlemini uygula
                batch.commit().await()

                loadSubscriptions() // Listeyi yenile
            } catch (e: Exception) {
                _subscriptionState.value = SubscriptionState.Error(e.message ?: "Plan yükseltme işlemi başarısız oldu")
            }
        }
    }

    // Belirli bir tarihte geçerli olan plan ve fiyatı bul
    suspend fun getEffectivePlanForDate(subscriptionId: String, date: Date): PlanHistoryEntry? {
        val userId = auth.currentUser?.uid ?: return null
        
        val planHistorySnapshot = firestore.collection("users")
            .document(userId)
            .collection("subscriptions")
            .document(subscriptionId)
            .collection("planHistory")
            .get()
            .await()

        return planHistorySnapshot.documents
            .mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                PlanHistoryEntry(
                    plan = data["plan"] as String,
                    price = (data["price"] as Number).toDouble(),
                    startDate = (data["startDate"] as com.google.firebase.Timestamp).toDate(),
                    endDate = (data["endDate"] as? com.google.firebase.Timestamp)?.toDate()
                )
            }
            .firstOrNull { entry ->
                date >= entry.startDate && (entry.endDate == null || date < entry.endDate)
            }
    }

    suspend fun getPlanHistory(subscriptionId: String): List<PlanHistoryEntry> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        
        val planHistorySnapshot = firestore.collection("users")
            .document(userId)
            .collection("subscriptions")
            .document(subscriptionId)
            .collection("planHistory")
            .get()
            .await()

        return planHistorySnapshot.documents
            .mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                PlanHistoryEntry(
                    plan = data["plan"] as String,
                    price = (data["price"] as Number).toDouble(),
                    startDate = (data["startDate"] as com.google.firebase.Timestamp).toDate(),
                    endDate = (data["endDate"] as? com.google.firebase.Timestamp)?.toDate()
                )
            }
            .sortedBy { it.startDate }
    }
} 