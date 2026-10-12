package com.weather24h.app.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.weather24h.app.ui.theme.CardBorder
import com.weather24h.app.ui.theme.CardGlassBg
import kotlin.math.roundToInt

@Composable
fun DailyForecastSection(
    dailyList: List<DailyForecast>,
    modifier: Modifier = Modifier
) {
    if (dailyList.isEmpty()) return

    var selectedDay by remember { mutableStateOf<DailyForecast?>(null) }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
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

                Text(
                    text = "Nhấn để xem chi tiết",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7 Days List
            dailyList.forEachIndexed { index, day ->
                val condition = WeatherCondition.fromWmoCode(day.weatherCode)
                val icon = WeatherCondition.getWeatherIcon(condition.type, isDay = true)
                val iconTint = WeatherCondition.getWeatherIconTint(condition.type, isDay = true)

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
                    // Day of Week
                    Text(
                        text = day.dayOfWeek,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.width(70.dp)
                    )

                    // Weather Icon & Rain %
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.width(52.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = condition.description,
                            tint = iconTint,
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

                    Spacer(modifier = Modifier.width(4.dp))

                    // Chevron indicator
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Chi tiết",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
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
    val condition = WeatherCondition.fromWmoCode(day.weatherCode)
    val icon = WeatherCondition.getWeatherIcon(condition.type, isDay = true)
    val iconTint = WeatherCondition.getWeatherIconTint(condition.type, isDay = true)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler(enabled = true) {
            onDismiss()
        }

        Surface(
            color = Color(0xFF0B132B),
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
                                .background(Color(0xFF1E293B))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Dự báo • ${day.dayOfWeek}",
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ngày ${day.dateString}",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 12.5.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = Color.White.copy(alpha = 0.7f),
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
                        color = Color(0xFF131A2A),
                        border = BorderStroke(1.dp, CardBorder),
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
                                color = Color.White,
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
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Thấp nhất",
                                        tint = Color(0xFF48CAE4),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Thấp nhất ${day.minTemp.roundToInt()}°C",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // 2. Lưới thông số quan trọng trong ngày
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF131A2A),
                        border = BorderStroke(1.dp, CardBorder),
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
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )

                            // Hàng 1: Xác suất mưa & Chỉ số UV cực đại
                            Row(modifier = Modifier.fillMaxWidth()) {
                                DayDetailMetricItem(
                                    icon = Icons.Default.WaterDrop,
                                    iconTint = Color(0xFF38BDF8),
                                    title = "XÁC SUẤT MƯA",
                                    value = "${day.precipitationProbability}%",
                                    desc = if (day.precipitationProbability >= 40) "Nguy cơ mưa cao" else "Ít khả năng mưa",
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                DayDetailMetricItem(
                                    icon = Icons.Default.WbSunny,
                                    iconTint = getUvColor(day.uvIndexMax),
                                    title = "CHỈ SỐ UV TỐI ĐA",
                                    value = String.format("%.1f", day.uvIndexMax),
                                    desc = getUvAdvice(day.uvIndexMax),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Divider(color = Color.White.copy(alpha = 0.08f))

                            // Hàng 2: Mặt trời mọc & Mặt trời lặn
                            Row(modifier = Modifier.fillMaxWidth()) {
                                DayDetailMetricItem(
                                    icon = Icons.Default.WbTwilight,
                                    iconTint = Color(0xFFFBBF24),
                                    title = "BÌNH MINH",
                                    value = day.sunrise.ifEmpty { "05:45" },
                                    desc = "Bình minh ngày mới",
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                DayDetailMetricItem(
                                    icon = Icons.Default.NightsStay,
                                    iconTint = Color(0xFFC084FC),
                                    title = "HOÀNG HÔN",
                                    value = day.sunset.ifEmpty { "18:15" },
                                    desc = "Bắt đầu chuyển tối",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // 3. Gợi ý trang phục & chuẩn bị cho ngày này
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF131A2A),
                        border = BorderStroke(1.dp, CardBorder),
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
                                    color = Color.White,
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
                            DailyTipRow(icon = Icons.Default.CheckCircle, text = clothingTip, tint = Color(0xFF34D399))

                            Spacer(modifier = Modifier.height(8.dp))

                            // Mẹo 2: Mưa / Ô dù
                            val rainTip = if (day.precipitationProbability >= 40) {
                                "Xác suất mưa ${day.precipitationProbability}%. Đừng quên bỏ sẵn ô (dù) hoặc áo mưa vào cốp xe để không bị ướt bất chợt."
                            } else {
                                "Xác suất mưa thấp (${day.precipitationProbability}%). Thời tiết thuận lợi cho các hoạt động ngoài trời, dã ngoại hoặc thể thao."
                            }
                            DailyTipRow(icon = Icons.Default.Umbrella, text = rainTip, tint = Color(0xFF38BDF8))

                            Spacer(modifier = Modifier.height(8.dp))

                            // Mẹo 3: Chống nắng / UV
                            val uvTip = if (day.uvIndexMax >= 6.0) {
                                "Chỉ số UV đạt ${String.format("%.1f", day.uvIndexMax)} (Mức cao). Hãy bôi kem chống nắng và đeo kính râm khi ra đường trong khung giờ 10h - 15h."
                            } else {
                                "Chỉ số UV ở mức ${String.format("%.1f", day.uvIndexMax)} an toàn cho da dưới ánh nắng tự nhiên."
                            }
                            DailyTipRow(icon = Icons.Default.Shield, text = uvTip, tint = Color(0xFFFBBF24))
                        }
                    }

                    // Nút Đóng
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text("Đóng chi tiết", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = title, color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}

@Composable
private fun DailyTipRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color
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
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.5.sp,
            lineHeight = 18.sp
        )
    }
}
