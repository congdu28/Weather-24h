package com.weather24h.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather24h.app.data.model.WeatherData
import com.weather24h.app.ui.theme.CardBorder
import com.weather24h.app.ui.theme.CardGlassBg
import kotlin.math.roundToInt

@Composable
fun WeatherMetricsGrid(
    weather: WeatherData,
    modifier: Modifier = Modifier
) {
    val current = weather.current
    val todayDaily = weather.daily7Days.firstOrNull()
    val uv = todayDaily?.uvIndexMax ?: current.uvIndex
    val aqi = weather.airQuality

    Column(modifier = modifier.fillMaxWidth()) {
        // Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CÁC CHỈ SỐ THỜI TIẾT",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Row 1: UV & Humidity
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                icon = Icons.Default.WbSunny,
                title = "CHỈ SỐ UV",
                value = String.format("%.1f", uv),
                subtext = getUvAdvice(uv),
                accentColor = getUvColor(uv),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            MetricCard(
                icon = Icons.Default.WaterDrop,
                title = "ĐỘ ẨM",
                value = "${current.humidity}%",
                subtext = if (current.humidity > 70) "Khá ẩm ướt" else "Độ ẩm dễ chịu",
                accentColor = Color(0xFF48CAE4),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 2: Wind & Feels Like
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                icon = Icons.Default.Air,
                title = "GIÓ",
                value = "${current.windSpeed.roundToInt()} km/h",
                subtext = "Hướng ${current.windDirectionText}",
                accentColor = Color(0xFF06D6A0),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            MetricCard(
                icon = Icons.Default.Thermostat,
                title = "CẢM NHẬN",
                value = "${current.feelsLike.roundToInt()}°C",
                subtext = if (current.feelsLike > current.temperature) "Cảm giác nóng hơn" else "Mát mẻ hơn",
                accentColor = Color(0xFFFFD166),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 3: Air Quality & Sunrise/Sunset
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                icon = Icons.Default.Spa,
                title = "KHÔNG KHÍ (AQI)",
                value = aqi?.let { "PM2.5: ${it.pm25.roundToInt()}" } ?: "Tốt",
                subtext = aqi?.statusText ?: "Chất lượng an toàn",
                accentColor = Color(0xFF06D6A0),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            MetricCard(
                icon = Icons.Default.WbTwilight,
                title = "MẶT TRỜI MỌC/LẶN",
                value = todayDaily?.sunrise ?: "05:45",
                subtext = "Lặn: ${todayDaily?.sunset ?: "18:15"}",
                accentColor = Color(0xFFF3904F),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 4: Pressure & Visibility
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                icon = Icons.Default.Speed,
                title = "ÁP SUẤT",
                value = "${current.surfacePressure.roundToInt()} hPa",
                subtext = "Áp suất chuẩn",
                accentColor = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            MetricCard(
                icon = Icons.Default.Visibility,
                title = "TẦM NHÌN",
                value = "${current.visibilityKm.roundToInt()} km",
                subtext = "Tầm nhìn xa tốt",
                accentColor = Color(0xFF118AB2),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MetricCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtext: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CardGlassBg)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtext,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}

fun getUvAdvice(uv: Double): String {
    return when {
        uv <= 2.0 -> "Thấp • An toàn"
        uv <= 5.0 -> "Vừa phải • Bảo vệ nhẹ"
        uv <= 7.0 -> "Cao • Bôi kem chống nắng"
        uv <= 10.0 -> "Rất cao • Cần che chắn"
        else -> "Cực độ • Tránh ra ngoài"
    }
}

fun getUvColor(uv: Double): Color {
    return when {
        uv <= 2.0 -> Color(0xFF06D6A0)
        uv <= 5.0 -> Color(0xFFFFD166)
        uv <= 7.0 -> Color(0xFFF77F00)
        else -> Color(0xFFEF476F)
    }
}
