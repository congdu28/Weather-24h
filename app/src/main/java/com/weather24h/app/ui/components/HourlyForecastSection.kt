package com.weather24h.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.weather24h.app.ui.theme.LocalIsDarkTheme
import com.weather24h.app.ui.theme.LocalSubtextColor
import kotlin.math.roundToInt

@Composable
fun HourlyForecastSection(
    hourlyList: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    if (hourlyList.isEmpty()) return

    val isDark = LocalIsDarkTheme.current
    val contentColor = LocalContentTextColor.current
    val subtextColor = LocalSubtextColor.current
    val temps = remember(hourlyList) { hourlyList.map { it.temperature } }
    val itemWidth = 68.dp

    // Trạng thái chế độ xem biểu đồ (Tổng hợp / Nhiệt độ / Lượng mưa)
    var displayMode by remember { mutableStateOf(ChartDisplayMode.COMBINED) }
    // Trạng thái giờ được chọn để xem chi tiết
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // Thống kê cực trị trong 24 giờ
    val minTemp = temps.minOrNull()?.roundToInt() ?: 20
    val maxTemp = temps.maxOrNull()?.roundToInt() ?: 30
    val maxRainHour = hourlyList.maxByOrNull { it.precipitationProbability }
    val maxRainProb = maxRainHour?.precipitationProbability ?: 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(LocalCardGlassBg.current)
            .border(0.5.dp, LocalCardBorder.current, RoundedCornerShape(24.dp))
            .padding(vertical = 16.dp)
    ) {
        Column {
            // Header Title & Thống kê tóm tắt
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
                        tint = contentColor.copy(alpha = 0.85f),
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

                // Nhãn nhiệt độ cao/thấp trong 24h
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.08f)
                ) {
                    Text(
                        text = "$minTemp° - $maxTemp°C",
                        color = contentColor.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Thanh chuyển đổi chế độ biểu đồ (Mode Pills)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartDisplayMode.values().forEach { mode ->
                    val isSelected = displayMode == mode
                    val pillBg = if (isSelected) {
                        if (isDark) Color(0xFF0284C7).copy(alpha = 0.35f) else Color(0xFF0284C7).copy(alpha = 0.18f)
                    } else {
                        Color.Transparent
                    }
                    val pillBorder = if (isSelected) Color(0xFF0284C7).copy(alpha = 0.5f) else subtextColor.copy(alpha = 0.15f)
                    val pillTextColor = if (isSelected) {
                        if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                    } else {
                        subtextColor
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(pillBg)
                            .border(if (isSelected) 1.dp else 0.5.dp, pillBorder, RoundedCornerShape(20.dp))
                            .clickable { displayMode = mode }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = mode.iconEmoji, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = mode.label,
                                color = pillTextColor,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Hộp chi tiết khi người dùng nhấn chọn một khung giờ (Interactive Detail Banner)
            AnimatedVisibility(
                visible = selectedIndex != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                selectedIndex?.let { idx ->
                    val selectedItem = hourlyList.getOrNull(idx)
                    if (selectedItem != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = (if (isDark) Color(0xFF0C4A6E) else Color(0xFFE0F2FE)).copy(alpha = 0.55f),
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp,
                                Color(0xFF0284C7).copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${selectedItem.hourLabel} • ${selectedItem.condition.description}",
                                            color = contentColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${selectedItem.temperature.roundToInt()}°C",
                                            color = Color(0xFF0284C7),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val rainNote = if (selectedItem.precipitationProbability in 40..70 && selectedItem.condition.type in listOf(com.weather24h.app.data.model.WeatherType.CLEAR, com.weather24h.app.data.model.WeatherType.PARTLY_CLOUDY)) {
                                        " • Khả năng mưa rào rải rác"
                                    } else ""
                                    Text(
                                        text = "Xác suất mưa: ${selectedItem.rainProbabilityText}" +
                                                (if (selectedItem.precipitationMm != null && selectedItem.precipitationMm > 0) " (${selectedItem.precipitationMm} mm)" else "") +
                                                rainNote +
                                                " • Gió: ${selectedItem.windSpeed.roundToInt()} km/h" +
                                                " • Độ ẩm: ${selectedItem.humidity}%",
                                        color = contentColor.copy(alpha = 0.85f),
                                        fontSize = 11.sp
                                    )
                                }

                                IconButton(
                                    onClick = { selectedIndex = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Đóng",
                                        tint = subtextColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Gợi ý nhỏ nếu chưa chọn giờ
            if (selectedIndex == null) {
                Text(
                    text = if (maxRainProb >= 50) "Khả năng mưa cao nhất: $maxRainProb% lúc ${maxRainHour?.hourLabel ?: ""}. Nhấn vào cột giờ để xem chi tiết."
                           else "Dự báo thời tiết 24 giờ tới. Nhấn vào từng giờ để xem chi tiết.",
                    color = subtextColor,
                    fontSize = 10.5.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nội dung cuộn ngang chứa biểu đồ và các mốc giờ
            Column(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
            ) {
                // Row 1: Giờ, Icon, Xác suất mưa
                Row {
                    hourlyList.forEachIndexed { index, item ->
                        val isSelected = selectedIndex == index
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(itemWidth)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedIndex = if (selectedIndex == index) null else index
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = item.hourLabel,
                                color = if (isSelected) Color(0xFF0284C7) else contentColor.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontWeight = if (index == 0 || isSelected) FontWeight.Bold else FontWeight.Normal
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            val condition = item.condition
                            val icon = WeatherCondition.getWeatherIcon(condition.type, item.isDay)
                            val iconTint = WeatherCondition.getWeatherIconTint(condition.type, item.isDay)

                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Hiển thị % mưa hoặc mm lượng mưa
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
                                        text = item.rainProbabilityText,
                                        color = Color(0xFF0284C7),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = item.rainProbabilityText,
                                        color = subtextColor.copy(alpha = 0.70f),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Row 2: Biểu đồ trực quan tương tác (Temperature / Precipitation / Combined)
                TemperatureChart(
                    temperatures = temps,
                    itemWidth = itemWidth,
                    hourlyList = hourlyList,
                    displayMode = displayMode,
                    selectedIndex = selectedIndex
                )

                // Row 3: Nhiệt độ số và tốc độ gió / lượng mưa
                Row {
                    hourlyList.forEachIndexed { index, item ->
                        val isSelected = selectedIndex == index
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(itemWidth)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedIndex = if (selectedIndex == index) null else index
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "${item.temperature.roundToInt()}°",
                                color = if (isSelected) Color(0xFF0284C7) else contentColor,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                            )

                            // Tùy theo chế độ: hiện mm mưa hoặc tốc độ gió
                            if (displayMode == ChartDisplayMode.PRECIPITATION && item.precipitationMm != null && item.precipitationMm > 0) {
                                Text(
                                    text = "${item.precipitationMm} mm",
                                    color = Color(0xFF0284C7),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "${item.windSpeed.roundToInt()} km/h",
                                    color = subtextColor,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getWeatherIcon(type: WeatherType): ImageVector = WeatherCondition.getWeatherIcon(type, true)
