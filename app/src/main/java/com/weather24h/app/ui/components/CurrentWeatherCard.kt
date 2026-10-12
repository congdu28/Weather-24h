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
import androidx.compose.material3.Icon
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
    modifier: Modifier = Modifier
) {
    val condition = WeatherCondition.fromWmoCode(weather.current.weatherCode)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(CardGlassBg)
            .border(1.dp, CardBorder, RoundedCornerShape(28.dp))
            .padding(vertical = 24.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Location Row
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
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = weather.location.name,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select City",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }

            if (weather.location.country.isNotEmpty()) {
                Text(
                    text = weather.location.displayName,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Temperature
            Text(
                text = "${weather.current.temperature.roundToInt()}°",
                color = Color.White,
                fontSize = 86.sp,
                fontWeight = FontWeight.ExtraLight,
                lineHeight = 86.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Weather Condition Description
            Text(
                text = condition.descriptionVi,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // High / Low & Feels-like
            Text(
                text = "Cao nhất: ${weather.maxTemp24h.roundToInt()}°  •  Thấp nhất: ${weather.minTemp24h.roundToInt()}°  •  Cảm nhận: ${weather.current.feelsLike.roundToInt()}°",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp
            )
        }
    }
}
