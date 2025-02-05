package com.denizcan.subscriptiontracker.model

import java.util.Date

data class Subscription(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val plan: String = "",
    val price: Double = 0.0,
    val category: SubscriptionCategory = SubscriptionCategory.OTHER,
    val paymentPeriod: PaymentPeriod = PaymentPeriod.MONTHLY,
    val startDate: Date = Date(),
    val nextPaymentDate: Date = Date(),
    val isActive: Boolean = true
)
