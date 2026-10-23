package com.weather24h.app.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather24h.app.ui.theme.LocalCardBorder
import com.weather24h.app.ui.theme.LocalCardGlassBg
import com.weather24h.app.ui.theme.LocalContentTextColor
import com.weather24h.app.ui.theme.LocalIsDarkTheme
import com.weather24h.app.ui.theme.LocalSubtextColor
import com.weather24h.app.util.LunarDate
import com.weather24h.app.util.VietnameseLunarCalendar
import java.time.LocalDate

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
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Việt Nam (GMT+7)",
                            color = subtextColor,
                            fontSize = 11.sp
                        )
                    }
                }

                // Cụm nút chuyển ngày (Trước / Hôm nay / Sau)
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                    fontWeight = FontWeight.Medium
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

            // 5. Nút mở rộng xem thêm thông tin chi tiết (Dương lịch tương ứng, chu kỳ trăng)
            Spacer(modifier = Modifier.height(8.dp))
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
}
