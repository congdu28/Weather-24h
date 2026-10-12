package com.weather24h.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Thunderstorm
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
import com.weather24h.app.data.model.WeatherType
import com.weather24h.app.data.model.WeatherCondition
import com.weather24h.app.ui.theme.CardBorder
import com.weather24h.app.ui.theme.CardGlassBg
import kotlin.math.roundToInt

@Composable
fun HourlyForecastSection(
    hourlyList: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    if (hourlyList.isEmpty()) return

    val temps = hourlyList.map { it.temperature }
    val itemWidth = 68.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardGlassBg)
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            .padding(vertical = 18.dp)
    ) {
        Column {
            // Header Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DỰ BÁO 24 GIỜ TỚI",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
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
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            val condition = WeatherCondition.fromWmoCode(item.weatherCode)
                            val icon = getWeatherIcon(condition.type)

                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            if (item.precipitationProbability > 0) {
                                Text(
                                    text = "${item.precipitationProbability}%",
                                    color = Color(0xFF48CAE4),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Spacer(modifier = Modifier.height(15.dp))
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
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${item.windSpeed.roundToInt()} km/h",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getWeatherIcon(type: WeatherType): ImageVector {
    return when (type) {
        WeatherType.CLEAR -> Icons.Default.WbSunny
        WeatherType.PARTLY_CLOUDY, WeatherType.CLOUDY, WeatherType.FOG -> Icons.Default.Cloud
        WeatherType.DRIZZLE, WeatherType.RAIN -> Icons.Default.WaterDrop
        WeatherType.THUNDERSTORM -> Icons.Default.Thunderstorm
        WeatherType.SNOW -> Icons.Default.WaterDrop
    }
}
