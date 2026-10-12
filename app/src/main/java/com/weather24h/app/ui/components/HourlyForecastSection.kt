package com.weather24h.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WaterDrop
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
import com.weather24h.app.data.model.HourlyForecast
import com.weather24h.app.data.model.WeatherCondition
import com.weather24h.app.data.model.WeatherType
import com.weather24h.app.ui.theme.LocalCardBorder
import com.weather24h.app.ui.theme.LocalCardGlassBg
import com.weather24h.app.ui.theme.LocalContentTextColor
import com.weather24h.app.ui.theme.LocalSubtextColor
import kotlin.math.roundToInt

@Composable
fun HourlyForecastSection(
    hourlyList: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    if (hourlyList.isEmpty()) return

    val contentColor = LocalContentTextColor.current
    val subtextColor = LocalSubtextColor.current
    val temps = hourlyList.map { it.temperature }
    val itemWidth = 68.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(LocalCardGlassBg.current)
            .border(1.dp, LocalCardBorder.current, RoundedCornerShape(24.dp))
            .padding(vertical = 18.dp)
    ) {
        Column {
            // Header Title & Chú thích rõ ràng cho người dùng về % (Xác suất mưa)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = contentColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DỰ BÁO 24 GIỜ TỚI",
                        color = contentColor.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Xác suất mưa",
                        color = subtextColor,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Horizontal Scroll Content
            Column(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
            ) {
                // Row 1: Time, Icon, Rain %
                Row {
                    hourlyList.forEachIndexed { index, item ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(itemWidth)
                        ) {
                            Text(
                                text = item.hourLabel,
                                color = contentColor.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            val condition = WeatherCondition.fromWmoCode(item.weatherCode, item.precipitationProbability, item.isDay)
                            val icon = WeatherCondition.getWeatherIcon(condition.type, item.isDay)
                            val iconTint = WeatherCondition.getWeatherIconTint(condition.type, item.isDay)

                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Hiển thị đầy đủ % xác suất mưa cho TẤT CẢ các khung giờ
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.height(16.dp)
                            ) {
                                if (item.precipitationProbability > 0) {
                                    Icon(
                                        imageVector = Icons.Default.WaterDrop,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(9.dp)
                                    )
                                    Spacer(modifier = Modifier.width(1.5.dp))
                                    Text(
                                        text = "${item.precipitationProbability}%",
                                        color = Color(0xFF0284C7),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "0%",
                                        color = subtextColor.copy(alpha = 0.45f),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Row 2: Visual Bezier Curve
                TemperatureChart(
                    temperatures = temps,
                    itemWidth = itemWidth
                )

                // Row 3: Temperature number & wind speed
                Row {
                    hourlyList.forEach { item ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(itemWidth)
                        ) {
                            Text(
                                text = "${item.temperature.roundToInt()}°",
                                color = contentColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${item.windSpeed.roundToInt()} km/h",
                                color = subtextColor,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getWeatherIcon(type: WeatherType): ImageVector = WeatherCondition.getWeatherIcon(type, true)
