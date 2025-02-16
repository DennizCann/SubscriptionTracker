package com.denizcan.subscriptiontracker.ui.theme

import androidx.compose.ui.graphics.Color

// Ana renkler
val md_theme_light_primary = Color(0xFFB388FF)         // Parlak mor
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFF151520) // Biraz daha açık mavi-mor
val md_theme_light_onPrimaryContainer = Color(0xFFFFFFFF)

// İkincil renkler
val md_theme_light_secondary = Color(0xFF9575CD)       // Lavanta
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFF13131E) // Biraz daha açık mavi-mor
val md_theme_light_onSecondaryContainer = Color(0xFFFFFFFF)

// Üçüncül renkler
val md_theme_light_tertiary = Color(0xFF7E57C2)        // Orta mor
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFF12121D) // Biraz daha açık mavi-mor
val md_theme_light_onTertiaryContainer = Color(0xFFFFFFFF)

// Hata renkleri
val md_theme_light_error = Color(0xFFFF5252)           // Parlak hata rengi
val md_theme_light_errorContainer = Color(0xFF151520)   // Biraz daha açık mavi-mor
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFFFFFFFF)

// Arka plan renkleri
val md_theme_light_background = Color(0xFF090910)      // Çok koyu, neredeyse siyah
val md_theme_light_onBackground = Color(0xFFFFFFFF)    // Beyaz yazı
val md_theme_light_surface = Color(0xFF12121D)         // Biraz daha açık mavi-mor
val md_theme_light_onSurface = Color(0xFFFFFFFF)       // Beyaz yazı
val md_theme_light_surfaceVariant = Color(0xFF1D1428)  // Daha açık mor-mavi
val md_theme_light_onSurfaceVariant = Color(0xFFF8F0FF) // Hafif mor beyaz
val md_theme_light_outline = Color(0xFFB39DDB)         // Parlak outline
val md_theme_light_inverseOnSurface = Color(0xFF090910)
val md_theme_light_inverseSurface = Color(0xFFFFFFFF)
val md_theme_light_inversePrimary = Color(0xFFB388FF)
val md_theme_light_surfaceTint = Color(0xFFB388FF)
val md_theme_light_outlineVariant = Color(0xFF9E91AE)
val md_theme_light_scrim = Color(0xFF000000)

// Koyu tema renkleri
val md_theme_dark_primary = Color(0xFFD4BBFF)
val md_theme_dark_onPrimary = Color(0xFF280084)
val md_theme_dark_primaryContainer = Color(0xFF151520)
val md_theme_dark_onPrimaryContainer = Color(0xFFFFFFFF)

val md_theme_dark_secondary = Color(0xFFCBBEFF)
val md_theme_dark_onSecondary = Color(0xFF2E009C)
val md_theme_dark_secondaryContainer = Color(0xFF13131E)
val md_theme_dark_onSecondaryContainer = Color(0xFFFFFFFF)

val md_theme_dark_tertiary = Color(0xFFBEA5FF)
val md_theme_dark_onTertiary = Color(0xFF25006E)
val md_theme_dark_tertiaryContainer = Color(0xFF12121D)
val md_theme_dark_onTertiaryContainer = Color(0xFFFFFFFF)

val md_theme_dark_error = Color(0xFFFF5252)
val md_theme_dark_errorContainer = Color(0xFF151520)
val md_theme_dark_onError = Color(0xFFFFFFFF)
val md_theme_dark_onErrorContainer = Color(0xFFFFFFFF)

// Koyu tema için arka plan renkleri
val md_theme_dark_background = Color(0xFF090910)       // Çok koyu, neredeyse siyah
val md_theme_dark_onBackground = Color(0xFFFFFFFF)     // Beyaz yazı
val md_theme_dark_surface = Color(0xFF12121D)          // Biraz daha açık mavi-mor
val md_theme_dark_onSurface = Color(0xFFFFFFFF)        // Beyaz yazı
val md_theme_dark_surfaceVariant = Color(0xFF151520)   // Biraz daha açık mavi-mor
val md_theme_dark_onSurfaceVariant = Color(0xFFF8F0FF) // Hafif mor beyaz
val md_theme_dark_outline = Color(0xFFB39DDB)          // Parlak outline
val md_theme_dark_inverseOnSurface = Color(0xFF090910)
val md_theme_dark_inverseSurface = Color(0xFFFFFFFF)
val md_theme_dark_inversePrimary = Color(0xFFB388FF)
val md_theme_dark_surfaceTint = Color(0xFFD4BBFF)
val md_theme_dark_outlineVariant = Color(0xFF483D59)
val md_theme_dark_scrim = Color(0xFF000000)

// Yeni eklenen yardımcı renkler
val surfaceGradientStart = Color(0xFF090910)    // Çok koyu, neredeyse siyah
val surfaceGradientEnd = Color(0xFF12121D)      // Biraz daha açık mavi-mor

val darkSurfaceGradientStart = Color(0xFF090910) // Çok koyu, neredeyse siyah
val darkSurfaceGradientEnd = Color(0xFF12121D)   // Biraz daha açık mavi-mor

// Kart arka planları için
val cardBackgroundLight = Color(0xFF151520).copy(alpha = 0.99f)  // Biraz daha açık mavi-mor
val cardBackgroundDark = Color(0xFF12121D).copy(alpha = 0.99f)   // Biraz daha açık mavi-mor

// Özel kategori renkleri - Daha parlak ve kontrast renkler
val categoryColors = mapOf(
    "STREAMING" to Color(0xFFFF4081),      // Neon pembe
    "MUSIC" to Color(0xFF00E5FF),          // Parlak cyan
    "EDUCATION" to Color(0xFF448AFF),      // Parlak mavi
    "GAMING" to Color(0xFFE040FB),         // Neon mor
    "SOFTWARE" to Color(0xFF00E676),       // Neon yeşil
    "SPORTS" to Color(0xFFFFAB40),         // Neon turuncu
    "STORAGE" to Color(0xFF90A4AE),        // Gri-mavi
    "PRODUCTIVITY" to Color(0xFF7C4DFF),    // Parlak mor
    "AI" to Color(0xFF536DFE),             // Parlak indigo
    "NEWS" to Color(0xFFFF4081),           // Neon pembe
    "FOOD" to Color(0xFFFF6E40),           // Neon somon
    "OTHER" to Color(0xFFB39DDB)           // Parlak lavanta
)