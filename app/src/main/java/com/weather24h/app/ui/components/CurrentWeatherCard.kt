package com.weather24h.app.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    val condition = WeatherCondition.fromWmoCode(weather.current.weatherCode)

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

                // Nút Đánh dấu yêu thích ⭐
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Yêu thích",
                        tint = if (isFavorite) Color(0xFFFFD166) else textColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(22.dp)
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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.25f))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "📡 Ngoại tuyến (Đã lưu lúc ${weather.lastUpdated})",
                        color = Color(0xFFFCA5A5),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val weatherIcon = WeatherCondition.getWeatherIcon(condition.type, weather.current.isDay)
            val iconTint = WeatherCondition.getWeatherIconTint(condition.type, weather.current.isDay)

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = condition.description,
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (weather.current.isDay) Color(0xFFFFB703).copy(alpha = 0.2f)
                            else Color(0xFF6366F1).copy(alpha = 0.25f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (weather.current.isDay) "Ban ngày" else "Ban đêm",
                        color = if (weather.current.isDay) Color(0xFFFFD166) else Color(0xFFBAE6FD),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // High / Low & Feels like
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cảm giác ${weather.current.feelsLike.roundToInt()}°C",
                    color = subtextColor,
                    fontSize = 14.sp
                )
                Text(
                    text = "•",
                    color = subtextColor.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
                Text(
                    text = "C:${weather.maxTemp24h.roundToInt()}° T:${weather.minTemp24h.roundToInt()}°",
                    color = subtextColor,
                    fontSize = 14.sp
                )
            }
        }
    }
}
