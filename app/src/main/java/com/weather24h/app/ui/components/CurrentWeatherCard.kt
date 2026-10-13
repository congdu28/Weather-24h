package com.weather24h.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather24h.app.data.model.WeatherCondition
import com.weather24h.app.data.model.WeatherData
import com.weather24h.app.ui.theme.CardBorder
import com.weather24h.app.ui.theme.CardGlassBg
import kotlin.math.roundToInt

@Composable
fun CurrentWeatherCard(
    weather: WeatherData,
    onLocationClick: () -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val condition = weather.current.condition

    val isDark = com.weather24h.app.ui.theme.LocalIsDarkTheme.current
    val cardBg = com.weather24h.app.ui.theme.LocalCardGlassBg.current
    val cardBorder = com.weather24h.app.ui.theme.LocalCardBorder.current
    val textColor = com.weather24h.app.ui.theme.LocalContentTextColor.current
    val subtextColor = com.weather24h.app.ui.theme.LocalSubtextColor.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(28.dp))
            .padding(vertical = 22.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Location Row with Star Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onLocationClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (weather.location.isCurrentLocation) Icons.Default.MyLocation else Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = weather.location.name,
                        color = textColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select City",
                        tint = textColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Nút Đánh dấu yêu thích ⭐ (Hiệu ứng chạm nhạy phản hồi tức thì)
                val starScale by animateFloatAsState(
                    targetValue = if (isFavorite) 1.2f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.45f, stiffness = 500f),
                    label = "StarAnim"
                )
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Yêu thích",
                        tint = if (isFavorite) Color(0xFFFFD166) else textColor.copy(alpha = 0.65f),
                        modifier = Modifier
                            .size(23.dp)
                            .graphicsLayer(scaleX = starScale, scaleY = starScale)
                    )
                }
            }

            if (weather.location.country.isNotEmpty()) {
                Text(
                    text = weather.location.displayName,
                    color = subtextColor,
                    fontSize = 12.5.sp,
                    maxLines = 1
                )
            }

            // Nhãn hiển thị nếu đang xem ngoại tuyến
            if (weather.isOffline) {
                Spacer(modifier = Modifier.height(6.dp))
                val offlineBg = if (isDark) Color(0xFFEF4444).copy(alpha = 0.25f) else Color(0xFFFEE2E2)
                val offlineBorder = if (isDark) Color(0xFFEF4444).copy(alpha = 0.5f) else Color(0xFFFCA5A5)
                val offlineTextColor = if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(offlineBg)
                        .border(1.dp, offlineBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "📡 Ngoại tuyến (Đã lưu lúc ${weather.lastUpdated})",
                        color = offlineTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val weatherIcon = WeatherCondition.getWeatherIcon(condition.type, weather.current.isDay)
            val iconTint = if (!isDark && condition.type == com.weather24h.app.data.model.WeatherType.CLOUDY) {
                Color(0xFF64748B)
            } else WeatherCondition.getWeatherIconTint(condition.type, weather.current.isDay)

            // Main Weather Icon & Temperature
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = weatherIcon,
                    contentDescription = condition.description,
                    tint = iconTint,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "${weather.current.temperature.roundToInt()}°",
                    color = textColor,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1.5).sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Weather Description & Day/Night Indicator Tag
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = condition.description,
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                val tagBg = if (isDark) {
                    if (weather.current.isDay) Color(0xFFFFB703).copy(alpha = 0.22f)
                    else Color(0xFF6366F1).copy(alpha = 0.28f)
                } else {
                    if (weather.current.isDay) Color(0xFFFEF3C7)
                    else Color(0xFFEEF2FF)
                }
                val tagBorder = if (isDark) {
                    if (weather.current.isDay) Color(0xFFFFB703).copy(alpha = 0.45f)
                    else Color(0xFF6366F1).copy(alpha = 0.45f)
                } else {
                    if (weather.current.isDay) Color(0xFFFDE68A)
                    else Color(0xFFC7D2FE)
                }
                val tagTextColor = if (isDark) {
                    if (weather.current.isDay) Color(0xFFFFD166)
                    else Color(0xFFBAE6FD)
                } else {
                    if (weather.current.isDay) Color(0xFFB45309)
                    else Color(0xFF4338CA)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(tagBg)
                        .border(1.dp, tagBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (weather.current.isDay) "Ban ngày" else "Ban đêm",
                        color = tagTextColor,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // High / Low & Feels like (Ghi rõ ràng: Cảm giác thật: , Cao: , Thấp: - đảm bảo 1 hàng không bị rớt dòng)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cảm giác thật: ${weather.current.feelsLike.roundToInt()}°",
                    color = subtextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "•",
                    color = subtextColor.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(
                    text = "Cao: ${weather.maxTemp24h.roundToInt()}°",
                    color = subtextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "•",
                    color = subtextColor.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(
                    text = "Thấp: ${weather.minTemp24h.roundToInt()}°",
                    color = subtextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Nguồn: ${weather.providerName}\nDữ liệu lúc ${weather.lastUpdated}",
                color = subtextColor,
                fontSize = 10.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
