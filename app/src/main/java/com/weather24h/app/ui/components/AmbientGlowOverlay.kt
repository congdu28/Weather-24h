package com.weather24h.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.weather24h.app.data.model.WeatherType
import com.weather24h.app.ui.theme.AmbientGlowStyle

/**
 * Lớp phủ ánh sáng hào quang nền động (Dynamic Ambient Light & Glow Overlay)
 * Tỏa ánh sáng huyền ảo theo các phong cách thị giác người dùng lựa chọn (Cyberpunk, Cực quang, Hoàng hôn, Tự nhiên,...)
 * Đồng bộ nhịp thở sinh động và cường độ mượt mà.
 */
@Composable
fun AmbientGlowOverlay(
    style: AmbientGlowStyle,
    intensity: Float,
    weatherType: WeatherType,
    isDay: Boolean,
    isScrolling: Boolean = false,
    isPowerSaveMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (intensity <= 0.01f) return

    val infiniteTransition = rememberInfiniteTransition(label = "AmbientGlowTransition")

    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse1"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 1.12f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(5600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse2"
    )

    val shiftX by infiniteTransition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(6500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShiftX"
    )

    // Xác định bộ màu sắc ánh sáng hào quang
    val (primary, secondary, accent) = if (style == AmbientGlowStyle.NATURAL) {
        when {
            weatherType == WeatherType.CLEAR && isDay -> Triple(Color(0xFFF59E0B), Color(0xFF38BDF8), Color(0xFFFBBF24))
            weatherType == WeatherType.CLEAR && !isDay -> Triple(Color(0xFF6366F1), Color(0xFF3B82F6), Color(0xFF1E1B4B))
            weatherType == WeatherType.PARTLY_CLOUDY && isDay -> Triple(Color(0xFFFBBF24), Color(0xFF38BDF8), Color(0xFF93C5FD))
            weatherType == WeatherType.PARTLY_CLOUDY && !isDay -> Triple(Color(0xFF818CF8), Color(0xFF38BDF8), Color(0xFF1E293B))
            weatherType == WeatherType.CLOUDY -> Triple(Color(0xFF64748B), Color(0xFF38BDF8), Color(0xFF1E293B))
            weatherType == WeatherType.THUNDERSTORM -> Triple(Color(0xFF8B5CF6), Color(0xFF06B6D4), Color(0xFF4C1D95))
            weatherType == WeatherType.RAIN || weatherType == WeatherType.DRIZZLE -> Triple(Color(0xFF0284C7), Color(0xFF06B6D4), Color(0xFF1E3A5F))
            weatherType == WeatherType.SNOW -> Triple(Color(0xFF38BDF8), Color(0xFFE0F2FE), Color(0xFF0284C7))
            weatherType == WeatherType.FOG -> Triple(Color(0xFF94A3B8), Color(0xFF64748B), Color(0xFF38BDF8))
            else -> Triple(Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFF0284C7))
        }
    } else {
        Triple(style.primaryColor, style.secondaryColor, style.accentColor)
    }

    val alphaBase = (intensity * 0.42f).coerceIn(0f, 0.85f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val effectiveShiftX = shiftX

        // Quầng sáng 1: Phía trên bên phải
        val center1 = Offset(w * 0.75f + effectiveShiftX, h * 0.22f)
        val radius1 = (w * 0.65f) * pulse1
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primary.copy(alpha = alphaBase),
                    secondary.copy(alpha = alphaBase * 0.4f),
                    Color.Transparent
                ),
                center = center1,
                radius = radius1
            ),
            center = center1,
            radius = radius1
        )

        // Nếu ở chế độ Tiết kiệm pin, bỏ qua quầng 2 & 3 để tiết kiệm 66% GPU draw calls
        if (!isPowerSaveMode) {
            // Quầng sáng 2: Phía dưới bên trái
            val center2 = Offset(w * 0.25f - effectiveShiftX, h * 0.68f)
            val radius2 = (w * 0.75f) * pulse2
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        secondary.copy(alpha = alphaBase * 0.8f),
                        accent.copy(alpha = alphaBase * 0.35f),
                        Color.Transparent
                    ),
                    center = center2,
                    radius = radius2
                ),
                center = center2,
                radius = radius2
            )

            // Quầng sáng 3: Ánh sáng lan tỏa nhẹ đỉnh màn hình
            val center3 = Offset(w * 0.5f, h * 0.06f)
            val radius3 = w * 0.8f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = alphaBase * 0.32f),
                        Color.Transparent
                    ),
                    center = center3,
                    radius = radius3
                ),
                center = center3,
                radius = radius3
            )
        }
    }
}
