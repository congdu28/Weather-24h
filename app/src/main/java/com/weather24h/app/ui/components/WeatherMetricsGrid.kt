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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather24h.app.data.model.WeatherData
import com.weather24h.app.ui.theme.LocalCardBorder
import com.weather24h.app.ui.theme.LocalCardGlassBg
import com.weather24h.app.ui.theme.LocalContentTextColor
import com.weather24h.app.ui.theme.LocalIsDarkTheme
import com.weather24h.app.ui.theme.LocalSubtextColor
import kotlin.math.roundToInt

@Composable
fun WeatherMetricsGrid(
    weather: WeatherData,
    modifier: Modifier = Modifier
) {
    val current = weather.current
    val todayDaily = weather.daily7Days.firstOrNull()
    val displayUv = if (current.isDay) {
        if (current.uvIndex > 0.0) current.uvIndex else (todayDaily?.uvIndexMax ?: 0.0)
    } else {
        current.uvIndex.coerceAtLeast(0.0)
    }
    val uvSubtext = if (!current.isDay && displayUv <= 0.5) {
        val peak = todayDaily?.uvIndexMax
        if (peak != null && peak > 0.0) {
            "Ban đêm an toàn • Đỉnh ngày: ${String.format("%.1f", peak)}"
        } else {
            "Ban đêm • An toàn"
        }
    } else {
        getUvAdvice(displayUv)
    }

    val humiditySubtext = when {
        current.humidity >= 85 -> "Rất ẩm • Dễ đọng sương"
        current.humidity >= 70 -> "Khá ẩm ướt"
        current.humidity >= 45 -> "Độ ẩm lý tưởng, dễ chịu"
        else -> "Không khí khô • Cần cấp ẩm"
    }

    val windDesc = when {
        current.windSpeed < 6.0 -> "Gió nhẹ"
        current.windSpeed < 20.0 -> "Gió vừa"
        current.windSpeed < 39.0 -> "Gió mạnh"
        else -> "Gió giật mạnh"
    }

    val tempDiff = current.feelsLike - current.temperature
    val feelsLikeSubtext = when {
        tempDiff >= 2.5 -> "Oi bức hơn thực tế (+${tempDiff.roundToInt()}°)"
        tempDiff >= 0.8 -> "Cảm giác ấm/nóng hơn"
        tempDiff <= -2.0 -> "Lạnh hơn do gió (${tempDiff.roundToInt()}°)"
        tempDiff <= -0.8 -> "Cảm giác dịu mát hơn"
        else -> "Tương đương nhiệt độ thực"
    }

    val visibilitySubtext = when {
        current.visibilityKm >= 10.0 -> "Tầm nhìn xa rất tốt"
        current.visibilityKm >= 5.0 -> "Tầm nhìn khá rõ"
        current.visibilityKm >= 2.0 -> "Hạn chế nhẹ do mù/ẩm"
        else -> "Tầm nhìn kém • Chú ý lái xe"
    }

    val pressureSubtext = when {
        current.surfacePressure < 1003.0 -> "Áp thấp • Dễ có mây/mưa"
        current.surfacePressure > 1020.0 -> "Áp cao • Thời tiết ổn định"
        else -> "Áp suất khí quyển ổn định"
    }

    val effectivePrecip = current.effectivePrecipitationMm
    val precipSubtext = when {
        effectivePrecip >= 5.0 -> "Mưa lớn • Chú ý đường trơn"
        effectivePrecip >= 1.5 -> "Mưa vừa hiện tại"
        effectivePrecip > 0.0 -> "Mưa nhỏ hiện tại"
        else -> "Hiện tại không có mưa"
    }

    val isDark = LocalIsDarkTheme.current
    val contentColor = LocalContentTextColor.current

    Column(modifier = modifier.fillMaxWidth()) {
        // Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CÁC CHỈ SỐ THỜI TIẾT",
                color = contentColor.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Row 1: UV & Humidity
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            MetricCard(
                icon = Icons.Default.WbSunny,
                title = "CHỈ SỐ UV",
                value = String.format("%.1f", displayUv),
                subtext = uvSubtext,
                accentColor = getUvColor(displayUv),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            Spacer(modifier = Modifier.width(12.dp))
            MetricCard(
                icon = Icons.Default.WaterDrop,
                title = "ĐỘ ẨM",
                value = "${current.humidity}%",
                subtext = humiditySubtext,
                accentColor = Color(0xFF48CAE4),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 2: Wind & Feels Like
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            MetricCard(
                icon = Icons.Default.Air,
                title = "GIÓ",
                value = "${current.windSpeed.roundToInt()} km/h",
                subtext = "Hướng ${current.windDirectionText} • $windDesc",
                accentColor = Color(0xFF06D6A0),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            Spacer(modifier = Modifier.width(12.dp))
            MetricCard(
                icon = Icons.Default.Thermostat,
                title = "CẢM NHẬN",
                value = "${current.feelsLike.roundToInt()}°C",
                subtext = feelsLikeSubtext,
                accentColor = Color(0xFFFFD166),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 3: Sunrise/Sunset & Visibility
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            MetricCard(
                icon = Icons.Default.WbTwilight,
                title = "MẶT TRỜI MỌC/LẶN",
                value = todayDaily?.sunrise ?: "05:45",
                subtext = "Lặn: ${todayDaily?.sunset ?: "18:15"}",
                accentColor = Color(0xFFF3904F),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            Spacer(modifier = Modifier.width(12.dp))
            MetricCard(
                icon = Icons.Default.Visibility,
                title = "TẦM NHÌN",
                value = "${current.visibilityKm.roundToInt()} km",
                subtext = visibilitySubtext,
                accentColor = Color(0xFF118AB2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 4: Pressure & Precipitation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            MetricCard(
                icon = Icons.Default.Speed,
                title = "ÁP SUẤT",
                value = "${current.surfacePressure.roundToInt()} hPa",
                subtext = pressureSubtext,
                accentColor = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF334155),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            Spacer(modifier = Modifier.width(12.dp))
            MetricCard(
                icon = Icons.Default.Umbrella,
                title = "LƯỢNG MƯA",
                value = "$effectivePrecip mm",
                subtext = precipSubtext,
                accentColor = Color(0xFF38BDF8),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
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
            .background(LocalCardGlassBg.current)
            .border(0.5.dp, LocalCardBorder.current, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    color = LocalSubtextColor.current,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                color = LocalContentTextColor.current,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtext,
                color = LocalSubtextColor.current,
                fontSize = 11.5.sp,
                lineHeight = 15.sp,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
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
