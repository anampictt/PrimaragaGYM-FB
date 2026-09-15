package com.pws.primaragagym.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize

/**
 * Shimmer effect modifier for smooth loading state animations across the application.
 */
fun Modifier.shimmerEffect(
    baseColor: Color = Color(0xFFE8EAF0),
    highlightColor: Color = Color(0xFFF6F7FB),
    durationMillis: Int = 1200
): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val width = if (size.width > 0) size.width.toFloat() else 350f
    val startOffsetX by transition.animateFloat(
        initialValue = -1.5f * width,
        targetValue = 1.5f * width,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerAnimation"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                baseColor,
                highlightColor,
                baseColor
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + width, size.height.toFloat().coerceAtLeast(100f))
        )
    ).onGloballyPositioned {
        size = it.size
    }
}
