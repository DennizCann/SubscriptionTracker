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
    AI("Yapay Zeka"),
    NEWS("Haber/Dergi"),
    FOOD("Yemek"),
    OTHER("Diğer")
}

data class PredefinedSubscription(
    val name: String,
    val category: SubscriptionCategory
)

object PredefinedSubscriptions {
    val subscriptions = listOf(
        // Streaming/Video
        PredefinedSubscription(
            name = "Netflix",
            category = SubscriptionCategory.STREAMING
        ),
        PredefinedSubscription(
            name = "Prime Video",
            category = SubscriptionCategory.STREAMING
        ),
        PredefinedSubscription(
            name = "Disney+",
            category = SubscriptionCategory.STREAMING
        ),
        PredefinedSubscription(
            name = "BluTV",
            category = SubscriptionCategory.STREAMING
        ),
        PredefinedSubscription(
            name = "MUBI",
            category = SubscriptionCategory.STREAMING
        ),
        PredefinedSubscription(
            name = "Gain",
            category = SubscriptionCategory.STREAMING
        ),
        PredefinedSubscription(
            name = "beIN CONNECT",
            category = SubscriptionCategory.STREAMING
        ),
        PredefinedSubscription(
            name = "HBO Max",
            category = SubscriptionCategory.STREAMING
        ),
        PredefinedSubscription(
            name = "Apple TV+",
            category = SubscriptionCategory.STREAMING
        ),
        
        // Müzik
        PredefinedSubscription(
            name = "Spotify",
            category = SubscriptionCategory.MUSIC
        ),
        PredefinedSubscription(
            name = "Apple Music",
            category = SubscriptionCategory.MUSIC
        ),
        PredefinedSubscription(
            name = "YouTube Music",
            category = SubscriptionCategory.MUSIC
        ),
        PredefinedSubscription(
            name = "Deezer",
            category = SubscriptionCategory.MUSIC
        ),
        PredefinedSubscription(
            name = "Tidal",
            category = SubscriptionCategory.MUSIC
        ),
        PredefinedSubscription(
            name = "Amazon Music",
            category = SubscriptionCategory.MUSIC
        ),
        
        // Eğitim
        PredefinedSubscription(
            name = "Udemy",
            category = SubscriptionCategory.EDUCATION
        ),
        PredefinedSubscription(
            name = "Coursera Plus",
            category = SubscriptionCategory.EDUCATION
        ),
        PredefinedSubscription(
            name = "Duolingo Plus",
            category = SubscriptionCategory.EDUCATION
        ),
        PredefinedSubscription(
            name = "Tureng Premium",
            category = SubscriptionCategory.EDUCATION
        ),
        PredefinedSubscription(
            name = "LinkedIn Learning",
            category = SubscriptionCategory.EDUCATION
        ),
        PredefinedSubscription(
            name = "Masterclass",
            category = SubscriptionCategory.EDUCATION
        ),
        PredefinedSubscription(
            name = "Pluralsight",
            category = SubscriptionCategory.EDUCATION
        ),
        PredefinedSubscription(
            name = "Skillshare",
            category = SubscriptionCategory.EDUCATION
        ),
        
        // Oyun
        PredefinedSubscription(
            name = "Xbox Game Pass",
            category = SubscriptionCategory.GAMING
        ),
        PredefinedSubscription(
            name = "PlayStation Plus",
            category = SubscriptionCategory.GAMING
        ),
        PredefinedSubscription(
            name = "EA Play",
            category = SubscriptionCategory.GAMING
        ),
        PredefinedSubscription(
            name = "GeForce NOW",
            category = SubscriptionCategory.GAMING
        ),
        PredefinedSubscription(
            name = "Nintendo Switch Online",
            category = SubscriptionCategory.GAMING
        ),
        PredefinedSubscription(
            name = "Ubisoft+",
            category = SubscriptionCategory.GAMING
        ),
        
        // Yazılım/Araçlar
        PredefinedSubscription(
            name = "Microsoft 365",
            category = SubscriptionCategory.SOFTWARE
        ),
        PredefinedSubscription(
            name = "Adobe Creative Cloud",
            category = SubscriptionCategory.SOFTWARE
        ),
        PredefinedSubscription(
            name = "JetBrains All Products",
            category = SubscriptionCategory.SOFTWARE
        ),
        PredefinedSubscription(
            name = "GitHub Copilot",
            category = SubscriptionCategory.SOFTWARE
        ),
        PredefinedSubscription(
            name = "Figma",
            category = SubscriptionCategory.SOFTWARE
        ),
        PredefinedSubscription(
            name = "1Password",
            category = SubscriptionCategory.SOFTWARE
        ),
        PredefinedSubscription(
            name = "NordVPN",
            category = SubscriptionCategory.SOFTWARE
        ),
        
        // Spor/Fitness
        PredefinedSubscription(
            name = "beIN SPORTS",
            category = SubscriptionCategory.SPORTS
        ),
        PredefinedSubscription(
            name = "S Sport Plus",
            category = SubscriptionCategory.SPORTS
        ),
        PredefinedSubscription(
            name = "Fitbod",
            category = SubscriptionCategory.SPORTS
        ),
        PredefinedSubscription(
            name = "Strava",
            category = SubscriptionCategory.SPORTS
        ),
        PredefinedSubscription(
            name = "Nike Training Club",
            category = SubscriptionCategory.SPORTS
        ),
        PredefinedSubscription(
            name = "MyFitnessPal",
            category = SubscriptionCategory.SPORTS
        ),
        
        // Depolama
        PredefinedSubscription(
            name = "Google One",
            category = SubscriptionCategory.STORAGE
        ),
        PredefinedSubscription(
            name = "iCloud+",
            category = SubscriptionCategory.STORAGE
        ),
        PredefinedSubscription(
            name = "Dropbox",
            category = SubscriptionCategory.STORAGE
        ),
        PredefinedSubscription(
            name = "OneDrive",
            category = SubscriptionCategory.STORAGE
        ),
        PredefinedSubscription(
            name = "pCloud",
            category = SubscriptionCategory.STORAGE
        ),
        
        // Üretkenlik
        PredefinedSubscription(
            name = "Notion",
            category = SubscriptionCategory.PRODUCTIVITY
        ),
        PredefinedSubscription(
            name = "Evernote",
            category = SubscriptionCategory.PRODUCTIVITY
        ),
        PredefinedSubscription(
            name = "Todoist",
            category = SubscriptionCategory.PRODUCTIVITY
        ),
        PredefinedSubscription(
            name = "Grammarly",
            category = SubscriptionCategory.PRODUCTIVITY
        ),
        PredefinedSubscription(
            name = "Asana",
            category = SubscriptionCategory.PRODUCTIVITY
        ),
        PredefinedSubscription(
            name = "Monday.com",
            category = SubscriptionCategory.PRODUCTIVITY
        ),

        // Yapay Zeka
        PredefinedSubscription(
            name = "ChatGPT Plus",
            category = SubscriptionCategory.AI
        ),
        PredefinedSubscription(
            name = "Claude Pro",
            category = SubscriptionCategory.AI
        ),
        PredefinedSubscription(
            name = "Midjourney",
            category = SubscriptionCategory.AI
        ),
        PredefinedSubscription(
            name = "Copilot Pro",
            category = SubscriptionCategory.AI
        ),
        PredefinedSubscription(
            name = "Perplexity Pro",
            category = SubscriptionCategory.AI
        ),
        PredefinedSubscription(
            name = "Poe",
            category = SubscriptionCategory.AI
        ),
        PredefinedSubscription(
            name = "Runway",
            category = SubscriptionCategory.AI
        ),

        // Haber/Dergi
        PredefinedSubscription(
            name = "Apple News+",
            category = SubscriptionCategory.NEWS
        ),
        PredefinedSubscription(
            name = "Medium",
            category = SubscriptionCategory.NEWS
        ),
        PredefinedSubscription(
            name = "The New York Times",
            category = SubscriptionCategory.NEWS
        ),
        PredefinedSubscription(
            name = "Bloomberg",
            category = SubscriptionCategory.NEWS
        ),
        PredefinedSubscription(
            name = "The Economist",
            category = SubscriptionCategory.NEWS
        ),
        PredefinedSubscription(
            name = "Financial Times",
            category = SubscriptionCategory.NEWS
        ),

        // Yemek
        PredefinedSubscription(
            name = "Yemeksepeti",
            category = SubscriptionCategory.FOOD
        ),
        PredefinedSubscription(
            name = "Getir",
            category = SubscriptionCategory.FOOD
        ),
        PredefinedSubscription(
            name = "Migros Hemen",
            category = SubscriptionCategory.FOOD
        ),
        PredefinedSubscription(
            name = "Trendyol Yemek",
            category = SubscriptionCategory.FOOD
        )
    )
} 