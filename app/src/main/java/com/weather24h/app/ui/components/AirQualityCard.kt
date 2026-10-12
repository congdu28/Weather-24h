package com.weather24h.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather24h.app.data.model.AirQuality
import com.weather24h.app.data.model.WeatherData
import com.weather24h.app.ui.theme.LocalCardOpacity
import com.weather24h.app.ui.theme.LocalContentTextColor
import com.weather24h.app.ui.theme.LocalIsDarkTheme
import com.weather24h.app.ui.theme.LocalSubtextColor
import java.util.Locale
import kotlin.math.roundToInt

data class AirQualityLevelInfo(
    val label: String,
    val color: Color,
    val advice: String,
    val outdoorAdvice: String
)

fun getAirQualityLevelInfo(pm25: Double): AirQualityLevelInfo {
    return when {
        pm25 <= 12.0 -> AirQualityLevelInfo(
            label = "Tuyệt vời",
            color = Color(0xFF10B981),
            advice = "Chất lượng không khí lý tưởng, rất trong lành.",
            outdoorAdvice = "Rất an toàn để tập thể thao và hoạt động ngoài trời."
        )
        pm25 <= 35.4 -> AirQualityLevelInfo(
            label = "Tốt",
            color = Color(0xFF06D6A0),
            advice = "Không khí đạt chuẩn, mức độ ô nhiễm rất thấp.",
            outdoorAdvice = "Thích hợp cho mọi hoạt động vui chơi, thể dục ngoài trời."
        )
        pm25 <= 55.4 -> AirQualityLevelInfo(
            label = "Trung bình",
            color = Color(0xFFFBBF24),
            advice = "Chất lượng không khí ở mức chấp nhận được.",
            outdoorAdvice = "Nhóm người nhạy cảm nên cân nhắc giảm bớt vận động mạnh ngoài trời."
        )
        pm25 <= 150.4 -> AirQualityLevelInfo(
            label = "Kém",
            color = Color(0xFFF97316),
            advice = "Nồng độ bụi mịn PM2.5 ở mức cao, có thể ảnh hưởng hô hấp.",
            outdoorAdvice = "Nên đeo khẩu trang chống bụi PM2.5 khi ra ngoài, hạn chế mở cửa lâu."
        )
        else -> AirQualityLevelInfo(
            label = "Nguy hại",
            color = Color(0xFFEF4444),
            advice = "Cảnh báo ô nhiễm nghiêm trọng, nguy cơ hại sức khỏe.",
            outdoorAdvice = "Hạn chế ra ngoài, đóng kín cửa sổ và sử dụng máy lọc không khí."
        )
    }
}

@Composable
fun AirQualityCard(
    weather: WeatherData,
    modifier: Modifier = Modifier
) {
    val aqi = weather.airQuality ?: AirQuality(pm25 = 16.0, pm10 = 26.0, aqiIndex = 32)
    val pm25 = aqi.pm25
    val pm10 = aqi.pm10
    val level = getAirQualityLevelInfo(pm25)

    val isDark = LocalIsDarkTheme.current
    val cardOpacity = LocalCardOpacity.current
    val contentColor = LocalContentTextColor.current
    val subtextColor = LocalSubtextColor.current

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.65f * cardOpacity) else Color.White.copy(alpha = 0.85f * cardOpacity),
        border = BorderStroke(
            1.dp,
            if (isDark) Color.White.copy(alpha = 0.15f * cardOpacity) else level.color.copy(alpha = 0.35f * cardOpacity)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Title & Status Badge (100% responsive, never overflows)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(level.color.copy(alpha = if (isDark) 0.20f else 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🍃", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "CHẤT LƯỢNG KHÔNG KHÍ & BỤI MỊN",
                            color = level.color,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Chỉ số AQI Châu Âu & Bụi mịn PM2.5/PM10",
                            color = subtextColor,
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Badge trạng thái gọn gàng
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = level.color.copy(alpha = if (isDark) 0.20f else 0.15f),
                    border = BorderStroke(1.dp, level.color.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = level.label,
                        color = level.color,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Khối hiển thị Nồng độ PM2.5 chính & AQI
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${pm25.roundToInt()}",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = level.color,
                        lineHeight = 36.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.padding(bottom = 3.dp)) {
                        Text(
                            text = "µg/m³",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                        Text(
                            text = "Bụi siêu mịn PM2.5",
                            fontSize = 10.5.sp,
                            color = subtextColor
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F5F9),
                    border = BorderStroke(
                        1.dp,
                        if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFCBD5E1)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AQI: ",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = subtextColor
                        )
                        Text(
                            text = "${aqi.aqiIndex}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = level.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Thước đo dải màu đa mức độ PM2.5 (Visual Gradient Scale Gauge)
            Column(modifier = Modifier.fillMaxWidth()) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                ) {
                    val barWidth = maxWidth
                    // Vị trí con trỏ tỉ lệ theo pm25 (tối đa 150 µg/m³)
                    val progressRatio = (pm25.toFloat() / 150f).coerceIn(0.03f, 0.97f)
                    val indicatorOffset = barWidth * progressRatio

                    // Dải màu gradient nền
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF10B981), // Tuyệt vời
                                        Color(0xFF06D6A0), // Tốt
                                        Color(0xFFFBBF24), // Trung bình
                                        Color(0xFFF97316), // Kém
                                        Color(0xFFEF4444)  // Nguy hại
                                    )
                                )
                            )
                    )

                    // Điểm mốc chỉ số hiện tại
                    Box(
                        modifier = Modifier
                            .offset(x = indicatorOffset - 6.dp)
                            .size(12.dp)
                            .align(Alignment.CenterStart)
                            .background(Color.White, CircleShape)
                            .border(2.dp, level.color, CircleShape)
                    )
                }

                // Nhãn mốc thước đo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "0", fontSize = 9.sp, color = subtextColor)
                    Text(text = "12 (Tốt)", fontSize = 9.sp, color = subtextColor)
                    Text(text = "35 (TB)", fontSize = 9.sp, color = subtextColor)
                    Text(text = "55 (Kém)", fontSize = 9.sp, color = subtextColor)
                    Text(text = "150+ (Hại)", fontSize = 9.sp, color = subtextColor)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3 ô chỉ số phụ chi tiết (PM2.5, PM10, Đánh giá)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubMetricBox(
                    title = "PM2.5",
                    value = "${String.format(Locale.US, "%.1f", pm25)}",
                    unit = "µg/m³",
                    subtext = "Bụi siêu mịn",
                    isDark = isDark,
                    contentColor = contentColor,
                    subtextColor = subtextColor,
                    modifier = Modifier.weight(1f)
                )
                SubMetricBox(
                    title = "PM10",
                    value = "${String.format(Locale.US, "%.1f", pm10)}",
                    unit = "µg/m³",
                    subtext = "Bụi hô hấp",
                    isDark = isDark,
                    contentColor = contentColor,
                    subtextColor = subtextColor,
                    modifier = Modifier.weight(1f)
                )
                SubMetricBox(
                    title = "ĐÁNH GIÁ",
                    value = level.label,
                    unit = "",
                    subtext = level.advice,
                    isDark = isDark,
                    contentColor = level.color,
                    subtextColor = subtextColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Khuyến nghị sức khỏe & sinh hoạt ngoài trời
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = level.color.copy(alpha = if (isDark) 0.12f else 0.08f),
                border = BorderStroke(1.dp, level.color.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💡", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Khuyến cáo sức khỏe:",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = level.color
                        )
                        Text(
                            text = level.outdoorAdvice,
                            fontSize = 11.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF334155),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubMetricBox(
    title: String,
    value: String,
    unit: String,
    subtext: String,
    isDark: Boolean,
    contentColor: Color,
    subtextColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF8FAFC),
        border = BorderStroke(
            1.dp,
            if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE2E8F0)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = subtextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = unit,
                        fontSize = 9.sp,
                        color = subtextColor,
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                fontSize = 9.5.sp,
                color = subtextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
