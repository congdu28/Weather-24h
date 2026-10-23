package com.weather24h.app.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.weather24h.app.ui.theme.LocalCardBorder
import com.weather24h.app.ui.theme.LocalCardGlassBg
import com.weather24h.app.ui.theme.LocalContentTextColor
import com.weather24h.app.ui.theme.LocalIsDarkTheme
import com.weather24h.app.ui.theme.LocalSubtextColor
import com.weather24h.app.util.LunarDate
import com.weather24h.app.util.VietnameseLunarCalendar
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VietnameseLunarCard(
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val contentColor = LocalContentTextColor.current
    val subtextColor = LocalSubtextColor.current

    // Quản lý ngày đang xem (mặc định hôm nay, có thể bấm trước/sau để tra cứu)
    var selectedLocalDate by remember { mutableStateOf(LocalDate.now()) }
    var isExpanded by remember { mutableStateOf(false) }
    var showFullMonthDialog by remember { mutableStateOf(false) }

    val lunar: LunarDate = remember(selectedLocalDate) {
        VietnameseLunarCalendar.getLunarDate(
            selectedLocalDate.dayOfMonth,
            selectedLocalDate.monthValue,
            selectedLocalDate.year
        )
    }

    val isToday = remember(selectedLocalDate) {
        selectedLocalDate == LocalDate.now()
    }

    val (moonEventText, moonEventDays) = remember(lunar.day) {
        VietnameseLunarCalendar.getNextMoonEventCountdown(lunar.day)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(LocalCardGlassBg.current)
            .border(0.5.dp, LocalCardBorder.current, RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Column {
            // 1. Tiêu đề khối & Nút chuyển ngày
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFFB703), Color(0xFFFB8500))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NightsStay,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ÂM LỊCH & TIẾT KHÍ",
                            color = contentColor.copy(alpha = 0.90f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "Việt Nam (GMT+7)",
                            color = subtextColor,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Cụm nút chuyển ngày (Trước / Hôm nay / Sau)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { selectedLocalDate = selectedLocalDate.minusDays(1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Ngày trước",
                            tint = contentColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (!isToday) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF38BDF8).copy(alpha = 0.20f),
                            border = BorderStroke(0.5.dp, Color(0xFF38BDF8).copy(alpha = 0.40f)),
                            modifier = Modifier.clickable { selectedLocalDate = LocalDate.now() }
                        ) {
                            Text(
                                text = "Hôm nay",
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { selectedLocalDate = selectedLocalDate.plusDays(1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Ngày sau",
                            tint = contentColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Banner sự kiện lễ hội hoặc đếm ngược ngày Rằm / Mùng Một
            val festival = lunar.festival
            if (festival != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = festival,
                        color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            } else if (moonEventDays in 1..7) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFB703).copy(alpha = 0.12f),
                    border = BorderStroke(0.5.dp, Color(0xFFFFB703).copy(alpha = 0.30f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🌕 Còn $moonEventDays $moonEventText",
                        color = if (isDark) Color(0xFFFFD166) else Color(0xFFB45309),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 3. Khối trung tâm hiển thị ngày Âm lịch & Can Chi
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Số ngày Âm lịch nổi bật
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "${lunar.day}",
                        color = contentColor,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 38.sp
                    )
                    Text(
                        text = "Tháng ${lunar.month}${if (lunar.isLeapMonth) " (N)" else ""}",
                        color = subtextColor,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Chi tiết Can Chi & Trực
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ngày ${lunar.dayCanChi}",
                        color = contentColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tháng ${lunar.monthCanChi} • Năm ${lunar.yearCanChi}",
                        color = subtextColor,
                        fontSize = 12.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Pill Hoàng đạo / Hắc đạo & Tiết khí
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val pillBg = if (lunar.isAuspiciousDay) Color(0xFF10B981).copy(alpha = 0.16f) else Color(0xFF64748B).copy(alpha = 0.16f)
                        val pillColor = if (lunar.isAuspiciousDay) {
                            if (isDark) Color(0xFF34D399) else Color(0xFF059669)
                        } else {
                            if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = pillBg,
                            border = BorderStroke(0.5.dp, pillColor.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = lunar.auspiciousDayType,
                                color = pillColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF38BDF8).copy(alpha = 0.12f),
                            border = BorderStroke(0.5.dp, Color(0xFF38BDF8).copy(alpha = 0.30f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = lunar.solarTerm,
                                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Giờ Hoàng Đạo trong ngày
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFFFFB703) else Color(0xFFD97706),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Giờ Hoàng Đạo:",
                        color = contentColor.copy(alpha = 0.90f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    lunar.auspiciousHours.forEach { hour ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.05f),
                            border = BorderStroke(0.5.dp, LocalCardBorder.current)
                        ) {
                            Text(
                                text = hour,
                                color = contentColor.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Nút mở rộng xem lịch tháng đầy đủ (Âm - Dương)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0xFFFFB703).copy(alpha = 0.12f) else Color(0xFFFFB703).copy(alpha = 0.18f),
                border = BorderStroke(0.5.dp, Color(0xFFFFB703).copy(alpha = 0.40f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showFullMonthDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 9.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFFFFD166) else Color(0xFFB45309),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🗓️ Mở rộng xem lịch tháng đầy đủ (Âm - Dương)",
                        color = if (isDark) Color(0xFFFFD166) else Color(0xFFB45309),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 6. Nút mở rộng xem thêm thông tin chi tiết (Dương lịch tương ứng, chu kỳ trăng)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = if (isExpanded) "Thu gọn chi tiết" else "Xem đối chiếu Dương lịch & Tiết khí",
                    color = subtextColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = subtextColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    HorizontalDivider(
                        color = LocalCardBorder.current,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        Text(text = "Dương lịch tương ứng:", color = subtextColor, fontSize = 12.sp)
                        Text(
                            text = "${selectedLocalDate.dayOfMonth}/${selectedLocalDate.monthValue}/${selectedLocalDate.year}",
                            color = contentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        Text(text = "Âm lịch đầy đủ:", color = subtextColor, fontSize = 12.sp)
                        Text(
                            text = lunar.formattedLunarFull,
                            color = contentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        Text(text = "Tiết khí mặt trời:", color = subtextColor, fontSize = 12.sp)
                        Text(
                            text = lunar.solarTerm,
                            color = contentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (showFullMonthDialog) {
        FullMonthLunarCalendarDialog(
            initialDate = selectedLocalDate,
            onSelectDate = { selectedLocalDate = it },
            onDismiss = { showFullMonthDialog = false }
        )
    }
}

/**
 * Hộp thoại Lịch tháng đối chiếu Âm - Dương đầy đủ
 */
@Composable
fun FullMonthLunarCalendarDialog(
    initialDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val contentColor = LocalContentTextColor.current
    val subtextColor = LocalSubtextColor.current
    val dialogBg = if (isDark) Color(0xFF0B132B) else Color(0xFFF8FAFC)
    val cardBg = if (isDark) Color(0xFF131A2A) else Color.White
    val cardBorder = if (isDark) LocalCardBorder.current else Color(0x150F172A)

    var currentYearMonth by remember { mutableStateOf(YearMonth.of(initialDate.year, initialDate.monthValue)) }
    var selectedDate by remember { mutableStateOf(initialDate) }

    val daysInMonth = remember(currentYearMonth) {
        VietnameseLunarCalendar.getMonthDays(currentYearMonth.year, currentYearMonth.monthValue)
    }

    val selectedLunar = remember(selectedDate) {
        VietnameseLunarCalendar.getLunarDate(selectedDate.dayOfMonth, selectedDate.monthValue, selectedDate.year)
    }

    // Thứ của ngày đầu tháng: 1 (T2) .. 7 (CN)
    val firstDayOfWeek = remember(currentYearMonth) {
        LocalDate.of(currentYearMonth.year, currentYearMonth.monthValue, 1).dayOfWeek.value
    }
    val emptySlotsBefore = firstDayOfWeek - 1

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
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Top App Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Đóng",
                                tint = contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "LỊCH THÁNG ÂM - DƯƠNG",
                                color = contentColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                maxLines = 1
                            )
                            Text(
                                text = "Đối chiếu lịch Việt Nam (GMT+7)",
                                color = subtextColor,
                                fontSize = 11.5.sp,
                                maxLines = 1
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

                // Month Navigator Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = cardBg,
                    border = BorderStroke(0.5.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                currentYearMonth = currentYearMonth.minusMonths(1)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Tháng trước",
                                tint = contentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Tháng ${currentYearMonth.monthValue} / ${currentYearMonth.year}",
                                color = contentColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
                                modifier = Modifier.clickable {
                                    val now = LocalDate.now()
                                    currentYearMonth = YearMonth.of(now.year, now.monthValue)
                                    selectedDate = now
                                }
                            ) {
                                Text(
                                    text = "Hiện tại",
                                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                currentYearMonth = currentYearMonth.plusMonths(1)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Tháng sau",
                                tint = contentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable content: Calendar Grid + Selected Day Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Weekday headers & Calendar Grid Card
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = cardBg,
                        border = BorderStroke(0.5.dp, cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            // 7 Weekdays Header (T2 .. CN)
                            val dayTitles = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                dayTitles.forEachIndexed { i, title ->
                                    val isWeekend = i >= 5
                                    Text(
                                        text = title,
                                        color = if (isWeekend) (if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)) else subtextColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = cardBorder,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            // Calendar Days Rows (chunked by 7)
                            val totalSlots = emptySlotsBefore + daysInMonth.size
                            val rowsCount = (totalSlots + 6) / 7

                            for (r in 0 until rowsCount) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    for (c in 0..6) {
                                        val slotIndex = r * 7 + c
                                        val dayIndex = slotIndex - emptySlotsBefore
                                        if (dayIndex in daysInMonth.indices) {
                                            val dayInfo = daysInMonth[dayIndex]
                                            val itemDate = LocalDate.of(currentYearMonth.year, currentYearMonth.monthValue, dayInfo.solarDay)
                                            val isSelected = itemDate == selectedDate
                                            val isTodayItem = dayInfo.isToday
                                            val lDay = dayInfo.lunarDate.day
                                            val isMoon15 = lDay == 15
                                            val isMoon1 = lDay == 1
                                            val isFest = dayInfo.lunarDate.festival != null

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(0.95f)
                                                    .padding(2.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(
                                                        when {
                                                            isSelected -> Color(0xFF38BDF8).copy(alpha = 0.22f)
                                                            isTodayItem -> Color(0xFFFFB703).copy(alpha = 0.16f)
                                                            else -> Color.Transparent
                                                        }
                                                    )
                                                    .border(
                                                        width = when {
                                                            isSelected -> 1.5.dp
                                                            isTodayItem -> 1.dp
                                                            else -> 0.dp
                                                        },
                                                        color = when {
                                                            isSelected -> Color(0xFF38BDF8)
                                                            isTodayItem -> Color(0xFFFFB703)
                                                            else -> Color.Transparent
                                                        },
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable {
                                                        selectedDate = itemDate
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    // Solar day
                                                    Text(
                                                        text = "${dayInfo.solarDay}",
                                                        color = if (isTodayItem) Color(0xFFFFB703) else contentColor,
                                                        fontSize = 13.5.sp,
                                                        fontWeight = if (isSelected || isTodayItem) FontWeight.Bold else FontWeight.Medium
                                                    )

                                                    // Lunar day
                                                    val lunarDisplay = when {
                                                        isMoon1 -> "1/${dayInfo.lunarDate.month}"
                                                        isMoon15 -> "15"
                                                        else -> "$lDay"
                                                    }
                                                    Text(
                                                        text = lunarDisplay,
                                                        color = when {
                                                            isMoon15 -> if (isDark) Color(0xFFFFD166) else Color(0xFFB45309)
                                                            isMoon1 -> if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626)
                                                            isFest -> if (isDark) Color(0xFFF87171) else Color(0xFFEF4444)
                                                            else -> subtextColor.copy(alpha = 0.85f)
                                                        },
                                                        fontSize = 10.sp,
                                                        fontWeight = if (isMoon1 || isMoon15 || isFest) FontWeight.Bold else FontWeight.Normal
                                                    )

                                                    // Dots for auspicious or festival
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                        modifier = Modifier.padding(top = 1.dp)
                                                    ) {
                                                        if (dayInfo.lunarDate.isAuspiciousDay) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(3.5.dp)
                                                                    .clip(CircleShape)
                                                                    .background(Color(0xFF10B981))
                                                            )
                                                        }
                                                        if (isFest) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(3.5.dp)
                                                                    .clip(CircleShape)
                                                                    .background(Color(0xFFEF4444))
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            // Empty cell
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }

                            // Chú thích nhỏ
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Hoàng đạo", color = subtextColor, fontSize = 10.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Lễ hội", color = subtextColor, fontSize = 10.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("15", color = Color(0xFFFFB703), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Rằm 🌕", color = subtextColor, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    // Selected Day Detail Card
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = cardBg,
                        border = BorderStroke(0.5.dp, cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header: Solar Day & Auspicious pill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Dương lịch: ${selectedDate.dayOfMonth}/${selectedDate.monthValue}/${selectedDate.year}",
                                    color = contentColor,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedLunar.isAuspiciousDay) Color(0xFF10B981).copy(alpha = 0.16f) else Color(0xFF64748B).copy(alpha = 0.16f),
                                    border = BorderStroke(0.5.dp, (if (selectedLunar.isAuspiciousDay) Color(0xFF10B981) else Color(0xFF64748B)).copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = selectedLunar.auspiciousDayType,
                                        color = if (selectedLunar.isAuspiciousDay) (if (isDark) Color(0xFF34D399) else Color(0xFF059669)) else (if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Âm lịch: ${selectedLunar.formattedLunarFull} (${selectedLunar.dayCanChi})",
                                color = if (isDark) Color(0xFFFFD166) else Color(0xFFB45309),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Năm ${selectedLunar.yearCanChi} • Tháng ${selectedLunar.monthCanChi} • Tiết khí: ${selectedLunar.solarTerm}",
                                color = subtextColor,
                                fontSize = 12.sp
                            )

                            if (selectedLunar.festival != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "✨ ${selectedLunar.festival}",
                                    color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Giờ hoàng đạo: ${selectedLunar.auspiciousHours.joinToString(", ")}",
                                color = subtextColor,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Button to select this day for main screen
                            Button(
                                onClick = {
                                    onSelectDate(selectedDate)
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF38BDF8),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Xem ngày này trên màn hình chính",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
