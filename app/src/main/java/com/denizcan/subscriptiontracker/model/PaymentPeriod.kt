package com.denizcan.subscriptiontracker.model

enum class PaymentPeriod {
    MONTHLY, QUARTERLY, YEARLY;

    val displayName: String
        get() = when (this) {
            MONTHLY -> "Aylık"
            QUARTERLY -> "3 Aylık"
            YEARLY -> "Yıllık"
        }
}