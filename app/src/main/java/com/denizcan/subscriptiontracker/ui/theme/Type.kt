package com.denizcan.subscriptiontracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Ekran genişliğine göre font boyutu hesaplama
@Composable
fun calculateFontSize(baseSize: Int): TextStyle {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    return when {
        screenWidth < 360.dp -> TextStyle(
            fontSize = (baseSize * 0.8).sp,
            lineHeight = (baseSize * 1.2).sp
        )
        screenWidth < 600.dp -> TextStyle(
            fontSize = baseSize.sp,
            lineHeight = (baseSize * 1.4).sp
        )
        else -> TextStyle(
            fontSize = (baseSize * 1.2).sp,
            lineHeight = (baseSize * 1.6).sp
        )
    }
}

// Dinamik spacing hesaplama
@Composable
fun calculateSpacing(baseSpacing: Dp): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    return when {
        screenWidth < 360.dp -> baseSpacing * 0.8f
        screenWidth < 600.dp -> baseSpacing
        else -> baseSpacing * 1.2f
    }
}

// Responsive Typography
@Composable
fun getResponsiveTypography(): Typography {
    return Typography(
        displayLarge = calculateFontSize(57).copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            letterSpacing = (-0.25).sp
        ),
        displayMedium = calculateFontSize(45).copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp
        ),
        displaySmall = calculateFontSize(36).copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp
        ),
        headlineLarge = calculateFontSize(32).copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp
        ),
        headlineMedium = calculateFontSize(28).copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp
        ),
        headlineSmall = calculateFontSize(24).copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp
        ),
        titleLarge = calculateFontSize(22).copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp
        ),
        titleMedium = calculateFontSize(16).copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.15.sp
        ),
        titleSmall = calculateFontSize(14).copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.1.sp
        ),
        bodyLarge = calculateFontSize(16).copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = calculateFontSize(14).copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.25.sp
        ),
        bodySmall = calculateFontSize(12).copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.4.sp
        ),
        labelLarge = calculateFontSize(14).copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.1.sp
        ),
        labelMedium = calculateFontSize(12).copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        ),
        labelSmall = calculateFontSize(11).copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
    )
}

// Varsayılan Typography (fallback için)
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)