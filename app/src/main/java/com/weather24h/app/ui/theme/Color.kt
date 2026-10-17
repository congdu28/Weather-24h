package com.weather24h.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.weather24h.app.data.model.WeatherType

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val PrimaryBlue = Color(0xFF0284C7)
val SkyBlue = Color(0xFF38BDF8)
val AccentYellow = Color(0xFFFFD166)
val AccentGreen = Color(0xFF06D6A0)
val AccentRed = Color(0xFFEF476F)
val DarkNavy = Color(0xFF0F172A)

// Card glass background with solid backing to prevent contrast issues
val CardGlassBgDark = Color(0xB80F172A) // Deep frosted navy (high contrast against sky)
val CardBorderDark = Color(0x38FFFFFF)

val CardGlassBgLight = Color(0xF2FFFFFF) // Frosted pure white
val CardBorderLight = Color(0x1F0F172A)

// Default fallbacks
val CardGlassBg = CardGlassBgDark
val CardBorder = CardBorderDark

enum class AmbientGlowStyle(
    val title: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color
) {
    NATURAL(
        title = "Tự nhiên (Theo thời tiết)",
        description = "Ánh sáng đổi nhịp nhàng theo thời tiết thực tế (Nắng, Mưa, Mây, Dông)",
        primaryColor = Color(0xFF38BDF8),
        secondaryColor = Color(0xFF818CF8),
        accentColor = Color(0xFF0284C7)
    ),
    CYBERPUNK(
        title = "Neon Cyberpunk",
        description = "Ánh sáng tím hồng neon dạ quang hiện đại & huyền ảo",
        primaryColor = Color(0xFFEC4899),
        secondaryColor = Color(0xFF8B5CF6),
        accentColor = Color(0xFF06B6D4)
    ),
    AURORA(
        title = "Cực quang Aurora",
        description = "Sắc xanh ngọc lục bảo và lam ngọc phát sáng phương Bắc",
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFF06B6D4),
        accentColor = Color(0xFF3B82F6)
    ),
    SUNSET(
        title = "Hoàng hôn Sunset",
        description = "Ánh cam hoàng hôn pha tím đào ấm áp và thơ mộng",
        primaryColor = Color(0xFFF97316),
        secondaryColor = Color(0xFFEC4899),
        accentColor = Color(0xFF6366F1)
    ),
    OCEAN(
        title = "Đại dương Ocean",
        description = "Xanh biển thẳm sâu, mát mắt và thư thái",
        primaryColor = Color(0xFF0284C7),
        secondaryColor = Color(0xFF0EA5E9),
        accentColor = Color(0xFF38BDF8)
    ),
    MIDNIGHT(
        title = "Đêm đen Minimalist",
        description = "Đen than chì tối giản, sâu lắng, tối ưu pin OLED",
        primaryColor = Color(0xFF334155),
        secondaryColor = Color(0xFF1E293B),
        accentColor = Color(0xFF0F172A)
    )
}

enum class AtmospherePerformanceMode(
    val title: String,
    val description: String
) {
    BALANCED(
        title = "Tự động tối ưu (Khuyên dùng)",
        description = "Tự động giảm tải hạt khi cuộn màn hình hoặc khi máy bật Tiết kiệm pin"
    ),
    HIGH_QUALITY(
        title = "Chất lượng cao nhất",
        description = "Luôn duy trì đầy đủ số lượng hạt và chuyển động mượt mà 60/120 FPS"
    ),
    BATTERY_SAVER(
        title = "Tiết kiệm pin tối đa",
        description = "Giảm 60% số lượng hạt và lược bỏ hiệu ứng nặng để máy luôn mát và tiết kiệm pin"
    )
}

fun getSkyGradientColors(type: WeatherType, isDay: Boolean, isDarkTheme: Boolean = true): Triple<Color, Color, Color> {
    if (!isDarkTheme) {
        // Chế độ Sáng (Light Theme): Nền trời tươi sáng, thanh thoát, dịu mắt
        return when (type) {
            WeatherType.CLEAR -> Triple(Color(0xFFBAE6FD), Color(0xFFE0F2FE), Color(0xFFF0F9FF))
            WeatherType.PARTLY_CLOUDY -> Triple(Color(0xFFCBD5E1), Color(0xFFE2E8F0), Color(0xFFF8FAFC))
            WeatherType.CLOUDY, WeatherType.FOG -> Triple(Color(0xFF94A3B8), Color(0xFFCBD5E1), Color(0xFFF1F5F9))
            WeatherType.DRIZZLE, WeatherType.RAIN -> Triple(Color(0xFF7DD3FC), Color(0xFFBAE6FD), Color(0xFFE2E8F0))
            WeatherType.THUNDERSTORM -> Triple(Color(0xFF64748B), Color(0xFF94A3B8), Color(0xFFCBD5E1))
            WeatherType.SNOW -> Triple(Color(0xFFE0F2FE), Color(0xFFF0FDF4), Color(0xFFFFFFFF))
        }
    }

    // Chế độ Tối (Dark Theme): Bầu trời khí quyển trầm đậm, bảo vệ mắt và tôn nổi bật icon
    if (!isDay) {
        return when (type) {
            WeatherType.THUNDERSTORM -> Triple(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF311042))
            WeatherType.RAIN -> Triple(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF1E293B))
            else -> Triple(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF1E293B))
        }
    }
    return when (type) {
        WeatherType.CLEAR -> Triple(Color(0xFF0369A1), Color(0xFF0284C7), Color(0xFF0C4A6E))
        WeatherType.PARTLY_CLOUDY -> Triple(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF0F172A))
        WeatherType.CLOUDY, WeatherType.FOG -> Triple(Color(0xFF1E293B), Color(0xFF475569), Color(0xFF0F172A))
        WeatherType.DRIZZLE, WeatherType.RAIN -> Triple(Color(0xFF0F172A), Color(0xFF1E3A5F), Color(0xFF0369A1))
        WeatherType.THUNDERSTORM -> Triple(Color(0xFF1E1B4B), Color(0xFF2E1065), Color(0xFF0F172A))
        WeatherType.SNOW -> Triple(Color(0xFF1E293B), Color(0xFF38BDF8).copy(alpha = 0.4f), Color(0xFF0F172A))
    }
}

fun getSkyGradient(type: WeatherType, isDay: Boolean, isDarkTheme: Boolean = true): Brush {
    val (top, mid, bottom) = getSkyGradientColors(type, isDay, isDarkTheme)
    return Brush.verticalGradient(listOf(top, mid, bottom))
}

@Composable
fun rememberAnimatedSkyGradient(
    type: WeatherType,
    isDay: Boolean,
    isDarkTheme: Boolean = true
): Brush {
    val targetColors = remember(type, isDay, isDarkTheme) {
        getSkyGradientColors(type, isDay, isDarkTheme)
    }

    val animSpec = tween<Color>(durationMillis = 850, easing = FastOutSlowInEasing)
    val topColor by animateColorAsState(targetValue = targetColors.first, animationSpec = animSpec, label = "SkyTop")
    val midColor by animateColorAsState(targetValue = targetColors.second, animationSpec = animSpec, label = "SkyMid")
    val bottomColor by animateColorAsState(targetValue = targetColors.third, animationSpec = animSpec, label = "SkyBottom")

    return remember(topColor, midColor, bottomColor) {
        Brush.verticalGradient(listOf(topColor, midColor, bottomColor))
    }
}
