package com.weather24h.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather24h.app.data.model.WeatherData
import kotlin.math.roundToInt

/**
 * Phần hiển thị thời tiết hiện tại chính (Seamless Hero Display)
 * Thiết kế hòa quyện trực tiếp vào nền trời khí quyển sống động (không viền hộp trắng đục cồng kềnh)
 * Phông chữ trắng sắc nét với bóng đổ mềm mại, tương phản hoàn hảo trên mọi hình nền thời tiết.
 */
@Composable
fun CurrentWeatherCard(
    weather: WeatherData,
    onLocationClick: () -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    showLunarCalendar: Boolean = true,
    modifier: Modifier = Modifier
) {
    val condition = weather.current.condition

    // Bóng đổ nhẹ cho chữ để luôn tương phản rõ ràng trên nền mây trắng, nắng hay bầu trời đêm
    val heroTextShadow = Shadow(
        color = Color.Black.copy(alpha = 0.38f),
        offset = Offset(0f, 2f),
        blurRadius = 8f
    )

    val tempShadow = Shadow(
        color = Color.Black.copy(alpha = 0.30f),
        offset = Offset(0f, 3f),
        blurRadius = 12f
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Tên Địa điểm (Nhấn để tìm kiếm / chọn thành phố) & Nút Yêu thích ⭐
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onLocationClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (weather.location.isCurrentLocation) Icons.Default.MyLocation else Icons.Default.LocationOn,
                        contentDescription = "Vị trí",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = weather.location.name,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        style = TextStyle(shadow = heroTextShadow),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Chọn địa điểm",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Nút Đánh dấu yêu thích ⭐ (Hiệu ứng nảy lò xo mượt mà)
                val starScale by animateFloatAsState(
                    targetValue = if (isFavorite) 1.22f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.45f, stiffness = 500f),
                    label = "StarAnim"
                )
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Yêu thích",
                        tint = if (isFavorite) Color(0xFFFFD166) else Color.White.copy(alpha = 0.75f),
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer(scaleX = starScale, scaleY = starScale)
                    )
                }
            }

            // Tên chi tiết phụ (tỉnh thành / quốc gia - tránh lặp lại tên thành phố chính)
            val subText = weather.location.subLocationText.ifBlank {
                if (weather.location.displayName != weather.location.name) weather.location.displayName else ""
            }
            if (subText.isNotBlank()) {
                Text(
                    text = subText,
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 13.sp,
                    style = TextStyle(shadow = heroTextShadow),
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // Nhãn hiển thị nếu đang xem ngoại tuyến
            if (weather.isOffline) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.35f),
                    border = BorderStroke(0.5.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "📡 Ngoại tuyến (Đã lưu lúc ${weather.lastUpdated})",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        style = TextStyle(shadow = heroTextShadow),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            // Nhãn Âm lịch Việt Nam chuẩn xác
            if (showLunarCalendar) {
                val todayLunar = com.weather24h.app.util.VietnameseLunarCalendar.getTodayLunar()
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.22f),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.22f))
                ) {
                    Text(
                        text = "🗓️ ${todayLunar.formattedLunarShort} (${todayLunar.dayCanChi}) • ${todayLunar.solarTerm}",
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        style = TextStyle(shadow = heroTextShadow),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Nhiệt độ cỡ lớn thanh thoát (86sp - Phong cách hiện đại hòa quyện nền)
            Text(
                text = "${weather.current.temperature.roundToInt()}°",
                color = Color.White,
                fontSize = 86.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-2.5).sp,
                style = TextStyle(shadow = tempShadow),
                textAlign = TextAlign.Center
            )

            // 3. Tình trạng thời tiết & Biên độ nhiệt độ (Cao/Thấp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "${condition.description}   ${weather.maxTemp24h.roundToInt()}° / ${weather.minTemp24h.roundToInt()}°",
                    color = Color.White,
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Medium,
                    style = TextStyle(shadow = heroTextShadow),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Cảm giác thật
            Text(
                text = "Cảm giác thật: ${weather.current.feelsLike.roundToInt()}°",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                style = TextStyle(shadow = heroTextShadow),
                textAlign = TextAlign.Center
            )

            // 4. Chip chất lượng không khí AQI bán trong suốt thanh lịch (như ảnh mẫu)
            val aqi = weather.airQuality
            if (aqi != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.22f),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.22f)),
                    modifier = Modifier.clip(RoundedCornerShape(20.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "🍃 AQI ${aqi.aqiIndex} • ${aqi.statusText}",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            style = TextStyle(shadow = heroTextShadow)
                        )
                    }
                }
            }

            // 5. Thời gian cập nhật
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${if (weather.isOffline) "Đã lưu" else "Cập nhật"} ${weather.lastUpdated}",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Normal,
                style = TextStyle(shadow = heroTextShadow),
                textAlign = TextAlign.Center
            )
        }
    }
}
