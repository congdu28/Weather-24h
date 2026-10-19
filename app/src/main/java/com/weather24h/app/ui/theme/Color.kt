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
            WeatherType.CLEAR -> Triple(Color(0xFF050B1B), Color(0xFF0B1736), Color(0xFF122452))
            WeatherType.PARTLY_CLOUDY -> Triple(Color(0xFF070E20), Color(0xFF0E1C3C), Color(0xFF162952))
            WeatherType.CLOUDY -> Triple(Color(0xFF09101C), Color(0xFF121D30), Color(0xFF1A2840))
            WeatherType.FOG -> Triple(Color(0xFF0E1624), Color(0xFF162234), Color(0xFF202E42))
            WeatherType.DRIZZLE, WeatherType.RAIN -> Triple(Color(0xFF040A16), Color(0xFF0A182E), Color(0xFF112544))
            WeatherType.THUNDERSTORM -> Triple(Color(0xFF0E071C), Color(0xFF1B0E36), Color(0xFF27134A))
            WeatherType.SNOW -> Triple(Color(0xFF070F22), Color(0xFF0E1E40), Color(0xFF162E5E))
        }
    }

    // 2. BAN NGÀY - CHẾ ĐỘ SÁNG (Light Theme Daytime): Tươi tắn, rực rỡ, chân thực (như bầu trời nhiệt đới sống động)
    if (!isDarkTheme) {
        return when (type) {
            WeatherType.CLEAR -> Triple(Color(0xFF5BA0E5), Color(0xFF337AC4), Color(0xFF1B5192))
            WeatherType.PARTLY_CLOUDY -> Triple(Color(0xFF5394D6), Color(0xFF3574B4), Color(0xFF1E4C84))
            WeatherType.CLOUDY -> Triple(Color(0xFF4C82B8), Color(0xFF386894), Color(0xFF22446A))
            WeatherType.FOG -> Triple(Color(0xFF6B8298), Color(0xFF506880), Color(0xFF364B60))
            WeatherType.DRIZZLE, WeatherType.RAIN -> Triple(Color(0xFF3B5676), Color(0xFF28415E), Color(0xFF16283D))
            WeatherType.THUNDERSTORM -> Triple(Color(0xFF231F3A), Color(0xFF171426), Color(0xFF0F0D1A))
            WeatherType.SNOW -> Triple(Color(0xFF6B8EAE), Color(0xFF4A7094), Color(0xFF2A4868))
        }
    }

    // 3. BAN NGÀY - CHẾ ĐỘ TỐI (Dark Theme Daytime): Sắc nét, tương phản sâu, nổi bật thẻ bán trong suốt
    return when (type) {
        WeatherType.CLEAR -> Triple(Color(0xFF0369A1), Color(0xFF0284C7), Color(0xFF0C2B4E))
        WeatherType.PARTLY_CLOUDY -> Triple(Color(0xFF144D80), Color(0xFF195E96), Color(0xFF0E2A4A))
        WeatherType.CLOUDY -> Triple(Color(0xFF1E3A5F), Color(0xFF234468), Color(0xFF111E30))
        WeatherType.FOG -> Triple(Color(0xFF243342), Color(0xFF1E2A36), Color(0xFF11171E))
        WeatherType.DRIZZLE, WeatherType.RAIN -> Triple(Color(0xFF16253A), Color(0xFF1A2F48), Color(0xFF0D1826))
        WeatherType.THUNDERSTORM -> Triple(Color(0xFF1E1436), Color(0xFF170E2B), Color(0xFF0F081E))
        WeatherType.SNOW -> Triple(Color(0xFF1E3A5F), Color(0xFF194C78), Color(0xFF0C2440))
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
