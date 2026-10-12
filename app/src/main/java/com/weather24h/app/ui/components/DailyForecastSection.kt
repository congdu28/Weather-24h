package com.weather24h.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather24h.app.data.model.DailyForecast
import com.weather24h.app.data.model.WeatherCondition
import com.weather24h.app.ui.theme.CardBorder
import com.weather24h.app.ui.theme.CardGlassBg
import kotlin.math.roundToInt

@Composable
fun DailyForecastSection(
    dailyList: List<DailyForecast>,
    modifier: Modifier = Modifier
) {
    if (dailyList.isEmpty()) return

    val globalMin = dailyList.minOfOrNull { it.minTemp } ?: 20.0
    val globalMax = dailyList.maxOfOrNull { it.maxTemp } ?: 35.0
    val tempSpan = if (globalMax - globalMin <= 0.0) 1.0 else (globalMax - globalMin)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardGlassBg)
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Column {
            // Header Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DỰ BÁO 7 NGÀY TỚI",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7 Days
            dailyList.forEachIndexed { index, day ->
                val condition = WeatherCondition.fromWmoCode(day.weatherCode)
                val icon = getWeatherIcon(condition.type)

                val leftRatio = ((day.minTemp - globalMin) / tempSpan).toFloat().coerceIn(0f, 1f)
                val rightRatio = ((day.maxTemp - globalMin) / tempSpan).toFloat().coerceIn(0f, 1f)
                val widthRatio = (rightRatio - leftRatio).coerceAtLeast(0.12f)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    // Day of Week
                    Text(
                        text = day.dayOfWeek,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.width(72.dp)
                    )

                    // Weather Icon & Rain %
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.width(52.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        if (day.precipitationProbability > 20) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${day.precipitationProbability}%",
                                color = Color(0xFF48CAE4),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Min Temp
                    Text(
                        text = "${day.minTemp.roundToInt()}°",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(30.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Temperature Range Bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = widthRatio)
                                .align(Alignment.CenterStart)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF48CAE4),
                                            Color(0xFFFFD166),
                                            Color(0xFFEF476F)
                                        )
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Max Temp
                    Text(
                        text = "${day.maxTemp.roundToInt()}°",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.width(30.dp)
                    )
                }

                if (index < dailyList.size - 1) {
                    Divider(
                        color = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
