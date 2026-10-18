package com.weather24h.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather24h.app.data.model.AirQuality
import com.weather24h.app.data.model.WeatherData
import com.weather24h.app.ui.theme.LocalCardOpacity
import com.weather24h.app.ui.theme.LocalCardGlassBg
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

fun getAirQualityLevelInfo(aqi: AirQuality): AirQualityLevelInfo {
    val usEpa = aqi.aqiScale?.startsWith("US EPA") == true
    val band = if (usEpa) aqi.aqiIndex.coerceIn(1, 6) else when {
        aqi.aqiIndex <= 20 -> 1
        aqi.aqiIndex <= 40 -> 2
        aqi.aqiIndex <= 60 -> 3
        aqi.aqiIndex <= 80 -> 4
        aqi.aqiIndex <= 100 -> 5
        else -> 6
    }
    return when (band) {
        1 -> AirQualityLevelInfo("Tốt", Color(0xFF10B981), "Chất lượng không khí tốt.", "Có thể sinh hoạt ngoài trời như thường lệ.")
        2 -> AirQualityLevelInfo(if (usEpa) "Trung bình" else "Khá", Color(0xFF84CC16), "Chất lượng không khí ở mức khá hoặc trung bình.", "Người nhạy cảm nên theo dõi cảm giác khi hoạt động ngoài trời.")
        3 -> AirQualityLevelInfo(if (usEpa) "Cần lưu ý" else "Trung bình", Color(0xFFFBBF24), "Chất lượng không khí cần được chú ý.", "Người nhạy cảm nên cân nhắc giảm vận động mạnh ngoài trời.")
        4 -> AirQualityLevelInfo("Kém", Color(0xFFF97316), "Chất lượng không khí kém.", "Nên giảm thời gian hoạt động ngoài trời, nhất là với nhóm nhạy cảm.")
        5 -> AirQualityLevelInfo("Rất kém", Color(0xFFEF476F), "Chất lượng không khí rất kém.", "Nên hạn chế hoạt động ngoài trời.")
        else -> AirQualityLevelInfo(if (usEpa) "Nguy hại" else "Cực kỳ kém", Color(0xFFB91C1C), "Chất lượng không khí ở mức xấu nhất của thang đo.", "Nên tránh hoạt động ngoài trời khi có thể.")
    }
}

@Composable
fun AirQualityCard(
    weather: WeatherData,
    modifier: Modifier = Modifier
) {
    val aqi = weather.airQuality
    if (aqi == null) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = LocalCardGlassBg.current,
            modifier = modifier.fillMaxWidth()
        ) {
            Text(
                text = "Chưa có dữ liệu chất lượng không khí",
                color = LocalSubtextColor.current,
                modifier = Modifier.padding(18.dp)
            )
        }
        return
    }
    val pm25 = aqi.pm25
    val pm10 = aqi.pm10
    val level = getAirQualityLevelInfo(aqi)

    val isDark = LocalIsDarkTheme.current
    val cardOpacity = LocalCardOpacity.current
    val contentColor = LocalContentTextColor.current
    val subtextColor = LocalSubtextColor.current

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.65f * cardOpacity) else Color.White.copy(alpha = 0.85f * cardOpacity),
        border = BorderStroke(0.5.dp, LocalCardBorder.current),
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
                            text = "${aqi.aqiScale ?: "AQI"} • Bụi mịn PM2.5/PM10",
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
                    border = BorderStroke(0.5.dp, level.color.copy(alpha = 0.25f))
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
                        0.5.dp,
                        if (isDark) Color.White.copy(alpha = 0.08f) else Color(0x150F172A)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${aqi.aqiScale ?: "AQI"}: ",
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

            // Thước đo dải màu đa mức độ PM2.5 (Visual Gradient Scale Gauge bằng Canvas không subcomposition)
            Column(modifier = Modifier.fillMaxWidth()) {
                val progressRatio = remember(pm25) { (pm25.toFloat() / 150f).coerceIn(0.03f, 0.97f) }
                val levelColor = level.color

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                ) {
                    val trackHeight = 6.dp.toPx()
                    val trackY = (size.height - trackHeight) / 2f
                    val cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())

                    // Thanh dải màu gradient nền
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF10B981), // Tuyệt vời
                                Color(0xFF06D6A0), // Tốt
                                Color(0xFFFBBF24), // Trung bình
                                Color(0xFFF97316), // Kém
                                Color(0xFFEF4444)  // Nguy hại
                            )
                        ),
                        topLeft = Offset(0f, trackY),
                        size = Size(size.width, trackHeight),
                        cornerRadius = cornerRadius
                    )

                    // Điểm mốc chỉ số hiện tại
                    val indicatorX = size.width * progressRatio
                    val centerY = size.height / 2f
                    val outerRadius = 6.dp.toPx()

                    drawCircle(
                        color = Color.White,
                        radius = outerRadius,
                        center = Offset(indicatorX, centerY)
                    )
                    drawCircle(
                        color = levelColor,
                        radius = outerRadius,
                        center = Offset(indicatorX, centerY),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Nhãn mốc thước đo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "0", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = subtextColor.copy(alpha = 0.85f))
                    Text(text = "25", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = subtextColor.copy(alpha = 0.85f))
                    Text(text = "50", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = subtextColor.copy(alpha = 0.85f))
                    Text(text = "100", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = subtextColor.copy(alpha = 0.85f))
                    Text(text = "150+ µg/m³", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = subtextColor.copy(alpha = 0.85f))
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
                border = BorderStroke(0.5.dp, level.color.copy(alpha = 0.20f)),
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
            Text(
                text = if (aqi.aqiScale == "European AQI") "Nguồn AQI: CAMS qua Open-Meteo" else "Nguồn AQI: WeatherAPI.com",
                color = subtextColor,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
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
            0.5.dp,
            if (isDark) Color.White.copy(alpha = 0.08f) else Color(0x150F172A)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
        ) {
            Text(
                text = title,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = subtextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = unit,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = subtextColor,
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                fontSize = 10.sp,
                color = subtextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
