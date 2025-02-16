package com.denizcan.subscriptiontracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import com.denizcan.subscriptiontracker.ui.theme.surfaceGradientStart
import com.denizcan.subscriptiontracker.ui.theme.surfaceGradientEnd
import com.denizcan.subscriptiontracker.ui.theme.darkSurfaceGradientStart
import com.denizcan.subscriptiontracker.ui.theme.darkSurfaceGradientEnd

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = if (MaterialTheme.colorScheme.background == Color(0xFF120B1A)) {
        // Koyu mor gradient
        listOf(
            surfaceGradientStart,
            surfaceGradientStart.copy(alpha = 0.99f),
            surfaceGradientEnd.copy(alpha = 0.99f),
            surfaceGradientEnd
        )
    } else {
        // Daha koyu mor gradient
        listOf(
            darkSurfaceGradientStart,
            darkSurfaceGradientStart.copy(alpha = 0.99f),
            darkSurfaceGradientEnd.copy(alpha = 0.99f),
            darkSurfaceGradientEnd
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = colors,
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY,
                    tileMode = TileMode.Clamp  // Clamp modu ile daha yumuşak geçiş
                )
            ),
        content = content
    )
} 