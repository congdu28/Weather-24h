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
val CardBorderDark = Color(0x18FFFFFF)

val CardGlassBgLight = Color(0xF2FFFFFF) // Frosted pure white
val CardBorderLight = Color(0x100F172A)

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
        description = "Duy trì chuyển động mượt mà liên tục, tự động giảm tải hạt khi máy bật Tiết kiệm pin"
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
    // 1. BAN ĐÊM (Night Sky): Bầu trời vũ trụ sâu thẳm, giàu sắc thái theo từng trạng thái khí quyển
    if (!isDay) {
        return when (type) {
            WeatherType.CLEAR -> Triple(Color(0xFF020617), Color(0xFF0B132B), Color(0xFF1C2541))
            WeatherType.PARTLY_CLOUDY -> Triple(Color(0xFF0A0F1D), Color(0xFF162035), Color(0xFF1E293B))
            WeatherType.CLOUDY -> Triple(Color(0xFF0B111D), Color(0xFF182232), Color(0xFF283548))
            WeatherType.FOG -> Triple(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
            WeatherType.DRIZZLE, WeatherType.RAIN -> Triple(Color(0xFF020914), Color(0xFF0C1E34), Color(0xFF163252))
            WeatherType.THUNDERSTORM -> Triple(Color(0xFF090414), Color(0xFF1B0E33), Color(0xFF2D1052))
            WeatherType.SNOW -> Triple(Color(0xFF050B18), Color(0xFF0D1D38), Color(0xFF173560))
        }
    }

    // 2. BAN NGÀY - CHẾ ĐỘ SÁNG (Light Theme Daytime): Tươi tắn, rực rỡ, chân thực
    if (!isDarkTheme) {
        return when (type) {
            WeatherType.CLEAR -> Triple(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFBAE6FD))
            WeatherType.PARTLY_CLOUDY -> Triple(Color(0xFF0EA5E9), Color(0xFF7DD3FC), Color(0xFFE0F2FE))
            WeatherType.CLOUDY -> Triple(Color(0xFF64748B), Color(0xFF94A3B8), Color(0xFFE2E8F0))
            WeatherType.FOG -> Triple(Color(0xFF64748B), Color(0xFFCBD5E1), Color(0xFFF1F5F9))
            WeatherType.DRIZZLE, WeatherType.RAIN -> Triple(Color(0xFF38BDF8), Color(0xFF60A5FA), Color(0xFFDBEAFE))
            WeatherType.THUNDERSTORM -> Triple(Color(0xFF475569), Color(0xFF64748B), Color(0xFF94A3B8))
            WeatherType.SNOW -> Triple(Color(0xFF7DD3FC), Color(0xFFBAE6FD), Color(0xFFF8FAFC))
        }
    }

    // 3. BAN NGÀY - CHẾ ĐỘ TỐI (Dark Theme Daytime): Sắc nét, tương phản sâu, nổi bật thẻ bán trong suốt
    return when (type) {
        WeatherType.CLEAR -> Triple(Color(0xFF0369A1), Color(0xFF0284C7), Color(0xFF075985))
        WeatherType.PARTLY_CLOUDY -> Triple(Color(0xFF0F4C81), Color(0xFF1D6F9E), Color(0xFF1E3A5F))
        WeatherType.CLOUDY -> Triple(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF475569))
        WeatherType.FOG -> Triple(Color(0xFF1E293B), Color(0xFF3B485A), Color(0xFF475569))
        WeatherType.DRIZZLE, WeatherType.RAIN -> Triple(Color(0xFF0F172A), Color(0xFF1E3A5F), Color(0xFF2B5480))
        WeatherType.THUNDERSTORM -> Triple(Color(0xFF18112C), Color(0xFF281845), Color(0xFF16253C))
        WeatherType.SNOW -> Triple(Color(0xFF0F172A), Color(0xFF1E3A5F), Color(0xFF0284C7))
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
