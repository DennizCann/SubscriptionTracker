package com.denizcan.subscriptiontracker.model

import java.util.Date

data class Subscription(
    val id: String = "",
    val name: String = "",
    val plan: String = "",
    val price: Double = 0.0,
    val category: SubscriptionCategory = SubscriptionCategory.OTHER,
    val nextPaymentDate: Date = Date(),
    val startDate: Date = Date(),
    val paymentPeriod: PaymentPeriod = PaymentPeriod.MONTHLY
)
