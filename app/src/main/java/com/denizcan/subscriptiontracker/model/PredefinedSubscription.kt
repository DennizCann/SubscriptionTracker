package com.denizcan.subscriptiontracker.model

enum class SubscriptionCategory(val displayName: String) {
    STREAMING("Video/Dizi/Film"),
    MUSIC("Müzik"),
    EDUCATION("Eğitim"),
    GAMING("Oyun"),
    SOFTWARE("Yazılım/Araçlar"),
    SPORTS("Spor"),
    STORAGE("Depolama"),
    PRODUCTIVITY("Üretkenlik"),
    OTHER("Diğer")
}

data class PredefinedSubscription(
    val name: String,
    val category: SubscriptionCategory,
    val plans: List<PredefinedPlan>
)

data class PredefinedPlan(
    val name: String,
    val price: Double
)

object PredefinedSubscriptions {
    val subscriptions = listOf(
        // Streaming/Video
        PredefinedSubscription(
            name = "Netflix",
            category = SubscriptionCategory.STREAMING,
            plans = listOf(
                PredefinedPlan("Temel Plan", 63.99),
                PredefinedPlan("Standart Plan", 97.99),
                PredefinedPlan("Özel Plan", 130.99)
            )
        ),
        PredefinedSubscription(
            name = "Prime Video",
            category = SubscriptionCategory.STREAMING,
            plans = listOf(
                PredefinedPlan("Aylık", 39.99),
                PredefinedPlan("Yıllık", 399.99)
            )
        ),
        PredefinedSubscription(
            name = "Disney+",
            category = SubscriptionCategory.STREAMING,
            plans = listOf(
                PredefinedPlan("Aylık", 64.99),
                PredefinedPlan("Yıllık", 649.90)
            )
        ),
        PredefinedSubscription(
            name = "BluTV",
            category = SubscriptionCategory.STREAMING,
            plans = listOf(
                PredefinedPlan("Standart", 89.90),
                PredefinedPlan("Premium", 119.90)
            )
        ),
        PredefinedSubscription(
            name = "MUBI",
            category = SubscriptionCategory.STREAMING,
            plans = listOf(
                PredefinedPlan("Aylık", 49.99),
                PredefinedPlan("Yıllık", 449.90)
            )
        ),
        PredefinedSubscription(
            name = "Gain",
            category = SubscriptionCategory.STREAMING,
            plans = listOf(
                PredefinedPlan("Aylık", 34.99),
                PredefinedPlan("Yıllık", 349.90)
            )
        ),
        
        // Müzik
        PredefinedSubscription(
            name = "Spotify",
            category = SubscriptionCategory.MUSIC,
            plans = listOf(
                PredefinedPlan("Bireysel", 49.99),
                PredefinedPlan("Duo", 63.99),
                PredefinedPlan("Aile", 89.99),
                PredefinedPlan("Öğrenci", 24.99)
            )
        ),
        PredefinedSubscription(
            name = "Apple Music",
            category = SubscriptionCategory.MUSIC,
            plans = listOf(
                PredefinedPlan("Bireysel", 44.99),
                PredefinedPlan("Aile", 64.99),
                PredefinedPlan("Öğrenci", 22.49)
            )
        ),
        PredefinedSubscription(
            name = "YouTube Music",
            category = SubscriptionCategory.MUSIC,
            plans = listOf(
                PredefinedPlan("Bireysel", 29.99),
                PredefinedPlan("Aile", 59.99)
            )
        ),
        PredefinedSubscription(
            name = "Deezer",
            category = SubscriptionCategory.MUSIC,
            plans = listOf(
                PredefinedPlan("Bireysel", 44.99),
                PredefinedPlan("Aile", 67.49),
                PredefinedPlan("Öğrenci", 22.49)
            )
        ),
        
        // Eğitim
        PredefinedSubscription(
            name = "Udemy",
            category = SubscriptionCategory.EDUCATION,
            plans = listOf(
                PredefinedPlan("Personal Plan", 199.99)
            )
        ),
        PredefinedSubscription(
            name = "Coursera Plus",
            category = SubscriptionCategory.EDUCATION,
            plans = listOf(
                PredefinedPlan("Aylık", 499.00),
                PredefinedPlan("Yıllık", 3999.00)
            )
        ),
        PredefinedSubscription(
            name = "Duolingo Plus",
            category = SubscriptionCategory.EDUCATION,
            plans = listOf(
                PredefinedPlan("Aylık", 94.99),
                PredefinedPlan("Yıllık", 599.99)
            )
        ),
        PredefinedSubscription(
            name = "Tureng Premium",
            category = SubscriptionCategory.EDUCATION,
            plans = listOf(
                PredefinedPlan("Aylık", 29.99),
                PredefinedPlan("Yıllık", 299.99)
            )
        ),
        
        // Oyun
        PredefinedSubscription(
            name = "Xbox Game Pass",
            category = SubscriptionCategory.GAMING,
            plans = listOf(
                PredefinedPlan("PC", 99.00),
                PredefinedPlan("Ultimate", 149.00)
            )
        ),
        PredefinedSubscription(
            name = "PlayStation Plus",
            category = SubscriptionCategory.GAMING,
            plans = listOf(
                PredefinedPlan("Essential", 149.00),
                PredefinedPlan("Extra", 239.00),
                PredefinedPlan("Deluxe", 269.00)
            )
        ),
        PredefinedSubscription(
            name = "EA Play",
            category = SubscriptionCategory.GAMING,
            plans = listOf(
                PredefinedPlan("Aylık", 49.90),
                PredefinedPlan("Yıllık", 299.90)
            )
        ),
        PredefinedSubscription(
            name = "GeForce NOW",
            category = SubscriptionCategory.GAMING,
            plans = listOf(
                PredefinedPlan("Priority", 150.00),
                PredefinedPlan("Ultimate", 250.00)
            )
        ),
        
        // Yazılım/Araçlar
        PredefinedSubscription(
            name = "Microsoft 365",
            category = SubscriptionCategory.SOFTWARE,
            plans = listOf(
                PredefinedPlan("Bireysel", 99.99),
                PredefinedPlan("Aile", 149.99)
            )
        ),
        PredefinedSubscription(
            name = "Adobe Creative Cloud",
            category = SubscriptionCategory.SOFTWARE,
            plans = listOf(
                PredefinedPlan("Fotoğraf Planı", 249.00),
                PredefinedPlan("Tüm Uygulamalar", 749.00)
            )
        ),
        PredefinedSubscription(
            name = "JetBrains All Products",
            category = SubscriptionCategory.SOFTWARE,
            plans = listOf(
                PredefinedPlan("Aylık", 249.00),
                PredefinedPlan("Yıllık", 2490.00)
            )
        ),
        
        // Spor/Fitness
        PredefinedSubscription(
            name = "beIN CONNECT",
            category = SubscriptionCategory.SPORTS,
            plans = listOf(
                PredefinedPlan("Standart", 179.00),
                PredefinedPlan("Premium", 229.00)
            )
        ),
        PredefinedSubscription(
            name = "S Sport Plus",
            category = SubscriptionCategory.SPORTS,
            plans = listOf(
                PredefinedPlan("Aylık", 179.00),
                PredefinedPlan("Yıllık", 1499.00)
            )
        ),
        PredefinedSubscription(
            name = "Fitbod",
            category = SubscriptionCategory.SPORTS,
            plans = listOf(
                PredefinedPlan("Aylık", 89.99),
                PredefinedPlan("Yıllık", 449.99)
            )
        ),
        
        // Depolama
        PredefinedSubscription(
            name = "Google One",
            category = SubscriptionCategory.STORAGE,
            plans = listOf(
                PredefinedPlan("100 GB", 19.99),
                PredefinedPlan("200 GB", 29.99),
                PredefinedPlan("2 TB", 99.99)
            )
        ),
        PredefinedSubscription(
            name = "iCloud+",
            category = SubscriptionCategory.STORAGE,
            plans = listOf(
                PredefinedPlan("50 GB", 14.99),
                PredefinedPlan("200 GB", 44.99),
                PredefinedPlan("2 TB", 149.99)
            )
        ),
        PredefinedSubscription(
            name = "Dropbox",
            category = SubscriptionCategory.STORAGE,
            plans = listOf(
                PredefinedPlan("Plus", 119.99),
                PredefinedPlan("Family", 199.99)
            )
        ),
        
        // Üretkenlik
        PredefinedSubscription(
            name = "Notion",
            category = SubscriptionCategory.PRODUCTIVITY,
            plans = listOf(
                PredefinedPlan("Plus", 96.00),
                PredefinedPlan("Business", 192.00)
            )
        ),
        PredefinedSubscription(
            name = "Evernote",
            category = SubscriptionCategory.PRODUCTIVITY,
            plans = listOf(
                PredefinedPlan("Personal", 149.99),
                PredefinedPlan("Professional", 199.99)
            )
        )
    )
} 