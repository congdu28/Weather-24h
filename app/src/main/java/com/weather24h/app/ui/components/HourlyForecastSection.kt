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
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartDisplayMode.values().forEach { mode ->
                    val isSelected = displayMode == mode
                    val (modeColor, modeDarkColor) = when (mode) {
                        ChartDisplayMode.TEMPERATURE -> Color(0xFFFF6B00) to Color(0xFFFF7A00)
                        ChartDisplayMode.PRECIPITATION -> Color(0xFF0284C7) to Color(0xFF38BDF8)
                        ChartDisplayMode.HUMIDITY -> Color(0xFF0D9488) to Color(0xFF2DD4BF)
                        ChartDisplayMode.COMBINED -> Color(0xFF0284C7) to Color(0xFF38BDF8)
                    }

                    val pillBg = if (isSelected) {
                        (if (isDark) modeDarkColor else modeColor).copy(alpha = if (isDark) 0.28f else 0.15f)
                    } else {
                        Color.Transparent
                    }
                    val pillBorder = if (isSelected) {
                        (if (isDark) modeDarkColor else modeColor).copy(alpha = 0.60f)
                    } else {
                        if (isDark) subtextColor.copy(alpha = 0.20f) else Color(0xFF94A3B8).copy(alpha = 0.45f)
                    }
                    val pillTextColor = if (isSelected) {
                        if (isDark) modeDarkColor else modeColor
                    } else {
                        if (isDark) subtextColor else Color(0xFF1E293B)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(pillBg)
                            .border(if (isSelected) 1.dp else 0.6.dp, pillBorder, RoundedCornerShape(20.dp))
                            .clickable { displayMode = mode }
                            .padding(horizontal = 11.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = mode.iconEmoji, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = mode.label,
                                color = pillTextColor,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // Thanh chú thích trực quan (Visual Legend) giúp người dùng không bao giờ nhầm lẫn các chỉ số
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (displayMode) {
                    ChartDisplayMode.COMBINED -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF6B00))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Nhiệt độ (°C)",
                                color = Color(0xFFFF6B00),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF0284C7))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Cột mưa (mm)",
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💧", fontSize = 9.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Độ ẩm (%)",
                                color = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    ChartDisplayMode.TEMPERATURE -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF6B00))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Đường biểu đồ nhiệt độ (°C) - Dao động từ $minTemp° đến $maxTemp°C",
                                color = Color(0xFFFF6B00),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    ChartDisplayMode.PRECIPITATION -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF0284C7))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Cột lượng mưa (mm) & Khả năng mưa (☔ %)",
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    ChartDisplayMode.HUMIDITY -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0D9488))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Đường biểu đồ độ ẩm không khí (%) theo từng giờ",
                                color = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
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
                            shape = RoundedCornerShape(14.dp),
                            color = (if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)).copy(alpha = 0.92f),
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp,
                                LocalCardBorder.current
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${selectedItem.hourLabel} • ${selectedItem.condition.description}",
                                        color = contentColor,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    IconButton(
                                        onClick = { selectedIndex = null },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Đóng",
                                            tint = subtextColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // 3 Thẻ chỉ số hiển thị màu sắc và icon riêng biệt: Nhiệt độ - Mưa - Độ ẩm
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 1. Thẻ Nhiệt độ (Đỏ Cam)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFFF6B00).copy(alpha = if (isDark) 0.18f else 0.10f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            0.5.dp,
                                            Color(0xFFFF6B00).copy(alpha = 0.35f)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "🌡️ Nhiệt độ",
                                                color = Color(0xFFFF6B00),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${selectedItem.temperature.roundToInt()}°C",
                                                color = Color(0xFFFF6B00),
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }

                                    // 2. Thẻ Lượng mưa / Xác suất mưa (Xanh Biển)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF0284C7).copy(alpha = if (isDark) 0.18f else 0.10f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            0.5.dp,
                                            Color(0xFF0284C7).copy(alpha = 0.35f)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "🌧️ Mưa",
                                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            val rainDetail = if (selectedItem.precipitationMm != null && selectedItem.precipitationMm > 0) {
                                                "${selectedItem.rainProbabilityText} (${selectedItem.precipitationMm}mm)"
                                            } else {
                                                selectedItem.rainProbabilityText
                                            }
                                            Text(
                                                text = rainDetail,
                                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }

                                    // 3. Thẻ Độ ẩm (Xanh Ngọc Teal)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF0D9488).copy(alpha = if (isDark) 0.18f else 0.10f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            0.5.dp,
                                            Color(0xFF0D9488).copy(alpha = 0.35f)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "💧 Độ ẩm",
                                                color = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${selectedItem.humidity}%",
                                                color = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488),
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "💨 Gió: ${selectedItem.windSpeed.roundToInt()} km/h",
                                        color = subtextColor,
                                        fontSize = 10.5.sp
                                    )
                                    if (selectedItem.precipitationProbability in 40..70 && selectedItem.condition.type in listOf(WeatherType.CLEAR, WeatherType.PARTLY_CLOUDY)) {
                                        Text(
                                            text = "⚠️ Có thể mưa rào bất chợt",
                                            color = Color(0xFFF59E0B),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Gợi ý nhỏ nếu chưa chọn giờ
            if (selectedIndex == null) {
                Text(
                    text = if (maxRainProb >= 50) "Khả năng mưa cao nhất: $maxRainProb% lúc ${maxRainHour?.hourLabel ?: ""}. Nhấn vào từng giờ để xem chi tiết."
                           else "Dự báo thời tiết 24 giờ tới. Nhấn vào từng giờ để xem chi tiết nhiệt độ, mưa và độ ẩm.",
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
                // Row 1: Giờ, Icon thời tiết, Chỉ số theo từng chế độ
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
                                color = if (isSelected) Color(0xFFFF6B00) else contentColor.copy(alpha = 0.9f),
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

                            // Hiển thị chỉ số phía trên biểu đồ tùy theo chế độ
                            when (displayMode) {
                                ChartDisplayMode.COMBINED, ChartDisplayMode.PRECIPITATION -> {
                                    // Khả năng có mưa (Màu xanh dương, biểu tượng ô/dù ☔ tránh nhầm độ ẩm)
                                    if (item.precipitationProbability > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = (if (isDark) Color(0xFF0284C7) else Color(0xFF38BDF8)).copy(alpha = 0.16f),
                                            modifier = Modifier.height(16.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            ) {
                                                Text(
                                                    text = "☔ ${item.precipitationProbability}%",
                                                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "—",
                                            color = subtextColor.copy(alpha = 0.40f),
                                            fontSize = 10.5.sp,
                                            modifier = Modifier.height(16.dp)
                                        )
                                    }
                                }
                                ChartDisplayMode.TEMPERATURE -> {
                                    // Nhiệt độ phía trên
                                    Text(
                                        text = "${item.temperature.roundToInt()}°",
                                        color = Color(0xFFFF6B00),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.height(16.dp)
                                    )
                                }
                                ChartDisplayMode.HUMIDITY -> {
                                    // Độ ẩm phía trên (Màu xanh ngọc Teal)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF0D9488).copy(alpha = 0.16f),
                                        modifier = Modifier.height(16.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            Text(
                                                text = "💧 ${item.humidity}%",
                                                color = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Row 2: Biểu đồ trực quan tương tác (Temperature / Precipitation / Humidity / Combined)
                TemperatureChart(
                    temperatures = temps,
                    itemWidth = itemWidth,
                    hourlyList = hourlyList,
                    displayMode = displayMode,
                    selectedIndex = selectedIndex
                )

                // Row 3: Các chỉ số chi tiết phía dưới biểu đồ
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
                            when (displayMode) {
                                ChartDisplayMode.COMBINED -> {
                                    // 1. Nhiệt độ màu đỏ cam rực rỡ
                                    Text(
                                        text = "${item.temperature.roundToInt()}°",
                                        color = Color(0xFFFF6B00),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    // 2. Độ ẩm màu xanh ngọc teal với icon giọt nước
                                    Text(
                                        text = "💧${item.humidity}%",
                                        color = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                ChartDisplayMode.TEMPERATURE -> {
                                    Text(
                                        text = "${item.temperature.roundToInt()}°C",
                                        color = Color(0xFFFF6B00),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${item.windSpeed.roundToInt()} km/h",
                                        color = subtextColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                ChartDisplayMode.PRECIPITATION -> {
                                    val rainMmText = if (item.precipitationMm != null && item.precipitationMm > 0) "${item.precipitationMm} mm" else "0 mm"
                                    Text(
                                        text = rainMmText,
                                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${item.temperature.roundToInt()}°",
                                        color = Color(0xFFFF6B00),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                ChartDisplayMode.HUMIDITY -> {
                                    Text(
                                        text = "${item.humidity}%",
                                        color = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val comfortText = when {
                                        item.humidity < 40 -> "Khô"
                                        item.humidity <= 70 -> "Dễ chịu"
                                        else -> "Ẩm ướt"
                                    }
                                    Text(
                                        text = comfortText,
                                        color = subtextColor,
                                        fontSize = 10.sp,
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
}

fun getWeatherIcon(type: WeatherType): ImageVector = WeatherCondition.getWeatherIcon(type, true)
