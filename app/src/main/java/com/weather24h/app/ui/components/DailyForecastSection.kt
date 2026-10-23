package com.weather24h.app.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.weather24h.app.data.model.DailyForecast
import com.weather24h.app.data.model.WeatherCondition
import com.weather24h.app.ui.theme.LocalCardBorder
import com.weather24h.app.ui.theme.LocalCardGlassBg
import com.weather24h.app.ui.theme.LocalContentTextColor
import com.weather24h.app.ui.theme.LocalIsDarkTheme
import com.weather24h.app.ui.theme.LocalSubtextColor
import kotlin.math.roundToInt

@Composable
fun DailyForecastSection(
    dailyList: List<DailyForecast>,
    showLunarCalendar: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (dailyList.isEmpty()) return

    var selectedDay by remember { mutableStateOf<DailyForecast?>(null) }
    val isDark = LocalIsDarkTheme.current
    val contentColor = LocalContentTextColor.current
    val subtextColor = LocalSubtextColor.current

    val globalMin = remember(dailyList) { dailyList.minOfOrNull { it.minTemp } ?: 20.0 }
    val globalMax = remember(dailyList) { dailyList.maxOfOrNull { it.maxTemp } ?: 35.0 }
    val tempSpan = remember(globalMin, globalMax) { if (globalMax - globalMin <= 0.0) 1.0 else (globalMax - globalMin) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(LocalCardGlassBg.current)
            .border(0.5.dp, LocalCardBorder.current, RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Column {
            // Header Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = contentColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DỰ BÁO ${dailyList.size} NGÀY TỚI",
                        color = contentColor.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Nhấn để xem chi tiết",
                    color = subtextColor,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7 Days List
            dailyList.forEachIndexed { index, day ->
                val rawCondition = WeatherCondition.fromWmoCode(day.weatherCode)
                val baseCondition = if ((rawCondition.type == com.weather24h.app.data.model.WeatherType.DRIZZLE || rawCondition.type == com.weather24h.app.data.model.WeatherType.RAIN) &&
                    day.precipitationProbability in 0..35 && day.maxTemp >= 28.0
                ) {
                    WeatherCondition("Nắng xen mây", com.weather24h.app.data.model.WeatherType.PARTLY_CLOUDY)
                } else {
                    rawCondition
                }
                val condition = if (day.precipitationProbability >= 60 && baseCondition.type == com.weather24h.app.data.model.WeatherType.CLEAR) {
                    WeatherCondition("Nắng gián đoạn, có lúc mưa rào", com.weather24h.app.data.model.WeatherType.DRIZZLE)
                } else if (day.precipitationProbability >= 40 && baseCondition.type == com.weather24h.app.data.model.WeatherType.CLEAR) {
                    WeatherCondition("Nắng xen mây, có thể có mưa", com.weather24h.app.data.model.WeatherType.PARTLY_CLOUDY)
                } else {
                    baseCondition
                }
                val icon = WeatherCondition.getWeatherIcon(condition.type, isDay = true)
                val iconTint = WeatherCondition.getWeatherIconTint(condition.type, isDay = true, isDark = isDark)

                val leftRatio = ((day.minTemp - globalMin) / tempSpan).toFloat().coerceIn(0f, 1f)
                val rightRatio = ((day.maxTemp - globalMin) / tempSpan).toFloat().coerceIn(0f, 1f)
                val widthRatio = (rightRatio - leftRatio).coerceAtLeast(0.12f)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedDay = day }
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    // Day of Week + Lunar Date
                    Column(
                        modifier = Modifier.width(72.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = day.dayOfWeek,
                            color = contentColor,
                            fontSize = 13.5.sp,
                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Medium
                        )
                        if (showLunarCalendar) {
                            val lunar = com.weather24h.app.util.VietnameseLunarCalendar.getLunarDateFromIso(day.dateString)
                            val isFullMoon = lunar.day == 15
                            val isNewMoon = lunar.day == 1
                            val lunarColor = when {
                                isFullMoon -> if (isDark) Color(0xFFFFD166) else Color(0xFFD97706)
                                isNewMoon -> if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                                else -> subtextColor.copy(alpha = 0.85f)
                            }
                            Text(
                                text = "${if (isFullMoon) "🌕 " else if (isNewMoon) "🌑 " else ""}${lunar.formattedLunarShort}",
                                color = lunarColor,
                                fontSize = 10.sp,
                                fontWeight = if (isFullMoon || isNewMoon) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    // Weather Icon & Rain %
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.width(56.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = condition.description,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        if (day.precipitationProbability > 0) {
                            Text(
                                text = day.rainProbabilityText,
                                color = Color(0xFF0284C7),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = day.rainProbabilityText,
                                color = subtextColor.copy(alpha = 0.70f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Min Temp
                    Text(
                        text = "${day.minTemp.roundToInt()}°",
                        color = subtextColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
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
                            .background(if (isDark) Color.Black.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.08f))
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
                        color = contentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.width(30.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Chevron indicator
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Chi tiết",
                        tint = subtextColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (index < dailyList.size - 1) {
                    Divider(
                        color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }

    // Modal hiển thị chi tiết dự báo của ngày được chọn
    selectedDay?.let { day ->
        DayForecastDetailDialog(
            day = day,
            onDismiss = { selectedDay = null }
        )
    }
}

@Composable
fun DayForecastDetailDialog(
    day: DailyForecast,
    onDismiss: () -> Unit
) {
    val rawCondition = WeatherCondition.fromWmoCode(day.weatherCode)
    val baseCondition = if ((rawCondition.type == com.weather24h.app.data.model.WeatherType.DRIZZLE || rawCondition.type == com.weather24h.app.data.model.WeatherType.RAIN) &&
        day.precipitationProbability in 0..35 && day.maxTemp >= 28.0
    ) {
        WeatherCondition("Nắng xen mây", com.weather24h.app.data.model.WeatherType.PARTLY_CLOUDY)
    } else {
        rawCondition
    }
    val condition = if (day.precipitationProbability >= 60 && baseCondition.type == com.weather24h.app.data.model.WeatherType.CLEAR) {
        WeatherCondition("Nắng gián đoạn, có lúc mưa rào", com.weather24h.app.data.model.WeatherType.DRIZZLE)
    } else if (day.precipitationProbability >= 40 && baseCondition.type == com.weather24h.app.data.model.WeatherType.CLEAR) {
        WeatherCondition("Nắng xen mây, có thể có mưa", com.weather24h.app.data.model.WeatherType.PARTLY_CLOUDY)
    } else {
        baseCondition
    }
    val isDark = LocalIsDarkTheme.current
    val icon = WeatherCondition.getWeatherIcon(condition.type, isDay = true)
    val iconTint = WeatherCondition.getWeatherIconTint(condition.type, isDay = true, isDark = isDark)
    val contentColor = LocalContentTextColor.current
    val subtextColor = LocalSubtextColor.current
    val cardBg = if (isDark) Color(0xFF131A2A) else Color.White
    val cardBorderColor = if (isDark) LocalCardBorder.current else Color(0x100F172A)
    val dialogBg = if (isDark) Color(0xFF0B132B) else Color(0xFFF1F5F9)
    val buttonBg = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler(enabled = true) {
            onDismiss()
        }

        Surface(
            color = dialogBg,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Top App Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(buttonBg)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Dự báo • ${day.dayOfWeek}",
                                color = contentColor,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ngày ${day.dateString}",
                                color = subtextColor,
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = "Nguồn: ${day.sourceName ?: "Chưa xác định"}",
                                color = subtextColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = subtextColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Thẻ tổng quan thời tiết ngày đó
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = cardBg,
                        border = BorderStroke(0.5.dp, cardBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = condition.description,
                                tint = iconTint,
                                modifier = Modifier.size(64.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = condition.description,
                                color = contentColor,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Cao nhất",
                                        tint = Color(0xFFEF476F),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Cao nhất ${day.maxTemp.roundToInt()}°C",
                                        color = contentColor,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Thấp nhất",
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Thấp nhất ${day.minTemp.roundToInt()}°C",
                                        color = subtextColor,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Khối Âm lịch Việt Nam của ngày được chọn
                    val lunar = com.weather24h.app.util.VietnameseLunarCalendar.getLunarDateFromIso(day.dateString)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = cardBg,
                        border = BorderStroke(0.5.dp, cardBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Tiêu đề & Huy hiệu Hoàng Đạo / Hắc Đạo
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NightsStay,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFFFFB703) else Color(0xFFD97706),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ÂM LỊCH VIỆT NAM",
                                        color = contentColor.copy(alpha = 0.90f),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (lunar.isAuspiciousDay) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF64748B).copy(alpha = 0.15f),
                                    border = BorderStroke(0.5.dp, (if (lunar.isAuspiciousDay) Color(0xFF10B981) else Color(0xFF64748B)).copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = lunar.auspiciousDayType,
                                        color = if (lunar.isAuspiciousDay) (if (isDark) Color(0xFF34D399) else Color(0xFF059669)) else (if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "${lunar.formattedLunarFull} (${lunar.dayCanChi})",
                                color = contentColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Năm ${lunar.yearCanChi} • Tháng ${lunar.monthCanChi} • Tiết khí: ${lunar.solarTerm}",
                                color = subtextColor,
                                fontSize = 12.sp
                            )

                            if (lunar.festival != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "✨ ${lunar.festival}",
                                    color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Giờ hoàng đạo: ${lunar.auspiciousHours.joinToString(", ")}",
                                color = subtextColor,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // 2. Dự báo nhiệt độ theo từng giờ của ngày được chọn
                    if (day.hourly24h.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = cardBg,
                            border = BorderStroke(0.5.dp, cardBorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "NHIỆT ĐỘ TỪNG GIỜ TRONG NGÀY",
                                        color = subtextColor,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                val itemWidth = 64.dp
                                val temps = day.hourly24h.map { it.temperature }

                                Column(
                                    modifier = Modifier
                                        .horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 10.dp)
                                ) {
                                    // Row 1: Giờ, Icon, Mưa %
                                    Row {
                                        day.hourly24h.forEach { item ->
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.width(itemWidth)
                                            ) {
                                                Text(
                                                    text = item.hourLabel,
                                                    color = contentColor.copy(alpha = 0.85f),
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Medium
                                                )

                                                Spacer(modifier = Modifier.height(6.dp))

                                                val c = item.condition
                                                val ic = WeatherCondition.getWeatherIcon(c.type, item.isDay)
                                                val icTint = WeatherCondition.getWeatherIconTint(c.type, item.isDay, isDark = isDark)

                                                Icon(
                                                    imageVector = ic,
                                                    contentDescription = null,
                                                    tint = icTint,
                                                    modifier = Modifier.size(22.dp)
                                                )

                                                Spacer(modifier = Modifier.height(4.dp))

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center,
                                                    modifier = Modifier.height(16.dp)
                                                ) {
                                                    if (item.precipitationProbability > 0) {
                                                        Text(
                                                            text = "☔ ${item.precipitationProbability}%",
                                                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    } else {
                                                        Text(
                                                            text = "—",
                                                            color = subtextColor.copy(alpha = 0.45f),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Normal
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Row 2: Biểu đồ đường cong nhiệt độ Bezier (Màu Đỏ Cam)
                                    TemperatureChart(
                                        temperatures = temps,
                                        itemWidth = itemWidth,
                                        hourlyList = day.hourly24h
                                    )

                                    // Row 3: Nhiệt độ số (Đỏ cam) và độ ẩm / tốc độ gió
                                    Row {
                                        day.hourly24h.forEach { item ->
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.width(itemWidth)
                                            ) {
                                                Text(
                                                    text = "${item.temperature.roundToInt()}°",
                                                    color = Color(0xFFFF6B00),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Text(
                                                    text = "💧${item.humidity}%",
                                                    color = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488),
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Lưới thông số quan trọng trong ngày
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = cardBg,
                        border = BorderStroke(0.5.dp, cardBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "CÁC CHỈ SỐ DỰ KIẾN TRONG NGÀY",
                                color = subtextColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )

                            // Hàng 1: Xác suất mưa & Chỉ số UV cực đại
                            Row(modifier = Modifier.fillMaxWidth()) {
                                DayDetailMetricItem(
                                    icon = Icons.Default.WaterDrop,
                                    iconTint = Color(0xFF0284C7),
                                    title = "XÁC SUẤT MƯA",
                                    value = day.rainProbabilityText,
                                    desc = when { day.precipitationProbability < 0 -> "Nguồn chưa cung cấp"; day.precipitationProbability >= 40 -> "Có khả năng mưa"; else -> "Ít khả năng mưa" },
                                    contentColor = contentColor,
                                    subtextColor = subtextColor,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                DayDetailMetricItem(
                                    icon = Icons.Default.WbSunny,
                                    iconTint = getUvColor(day.uvIndexMax),
                                    title = "CHỈ SỐ UV TỐI ĐA",
                                    value = String.format("%.1f", day.uvIndexMax),
                                    desc = getUvAdvice(day.uvIndexMax),
                                    contentColor = contentColor,
                                    subtextColor = subtextColor,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Divider(color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f))

                            // Hàng 2: Mặt trời mọc & Mặt trời lặn
                            Row(modifier = Modifier.fillMaxWidth()) {
                                DayDetailMetricItem(
                                    icon = Icons.Default.WbTwilight,
                                    iconTint = Color(0xFFFBBF24),
                                    title = "BÌNH MINH",
                                    value = day.sunrise.ifEmpty { "05:45" },
                                    desc = "Bình minh ngày mới",
                                    contentColor = contentColor,
                                    subtextColor = subtextColor,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                DayDetailMetricItem(
                                    icon = Icons.Default.NightsStay,
                                    iconTint = Color(0xFFC084FC),
                                    title = "HOÀNG HÔN",
                                    value = day.sunset.ifEmpty { "18:15" },
                                    desc = "Bắt đầu chuyển tối",
                                    contentColor = contentColor,
                                    subtextColor = subtextColor,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // 3. Gợi ý trang phục & chuẩn bị cho ngày này
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = cardBg,
                        border = BorderStroke(0.5.dp, cardBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFC084FC),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "GỢI Ý TRANG PHỤC & LƯU Ý",
                                    color = contentColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Mẹo 1: Quần áo
                            val clothingTip = when {
                                day.maxTemp >= 33.0 -> "Trời khá nóng bức (${day.maxTemp.roundToInt()}°C). Hãy ưu tiên áo thun cộc tay, quần short thoáng khí và uống nhiều nước."
                                day.minTemp <= 19.0 -> "Trời se lạnh về đêm và sáng sớm (${day.minTemp.roundToInt()}°C). Nên chuẩn bị áo khoác nhẹ hoặc khăn mỏng."
                                else -> "Thời tiết dễ chịu (${day.minTemp.roundToInt()}° - ${day.maxTemp.roundToInt()}°C). Thích hợp với mọi trang phục công sở hay dạo phố thường ngày."
                            }
                            DailyTipRow(icon = Icons.Default.CheckCircle, text = clothingTip, tint = Color(0xFF10B981), textColor = contentColor)

                            Spacer(modifier = Modifier.height(8.dp))

                            // Mẹo 2: Mưa / Ô dù
                            val rainTip = if (day.precipitationProbability < 0) {
                                "Nguồn chưa cung cấp xác suất mưa. Kiểm tra trạng thái và lượng mưa dự báo trước khi ra ngoài."
                            } else if (day.precipitationProbability >= 40) {
                                "Xác suất mưa ${day.precipitationProbability}%. Đừng quên bỏ sẵn ô (dù) hoặc áo mưa vào cốp xe để không bị ướt bất chợt."
                            } else {
                                "Xác suất mưa thấp (${day.precipitationProbability}%). Thời tiết thuận lợi cho các hoạt động ngoài trời, dã ngoại hoặc thể thao."
                            }
                            DailyTipRow(icon = Icons.Default.Umbrella, text = rainTip, tint = Color(0xFF0284C7), textColor = contentColor)

                            Spacer(modifier = Modifier.height(8.dp))

                            // Mẹo 3: Chống nắng / UV
                            val uvTip = if (day.uvIndexMax >= 6.0) {
                                "Chỉ số UV đạt ${String.format("%.1f", day.uvIndexMax)} (Mức cao). Hãy bôi kem chống nắng và đeo kính râm khi ra đường trong khung giờ 10h - 15h."
                            } else {
                                "Chỉ số UV ở mức ${String.format("%.1f", day.uvIndexMax)} an toàn cho da dưới ánh nắng tự nhiên."
                            }
                            DailyTipRow(icon = Icons.Default.Shield, text = uvTip, tint = Color(0xFFFBBF24), textColor = contentColor)
                        }
                    }

                    // Nút Đóng
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text("Đóng chi tiết", color = contentColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayDetailMetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    desc: String,
    contentColor: Color,
    subtextColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = title, color = subtextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = value, color = contentColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, color = subtextColor, fontSize = 11.sp)
    }
}

@Composable
private fun DailyTipRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = textColor,
            fontSize = 12.5.sp,
            lineHeight = 18.sp
        )
    }
}
