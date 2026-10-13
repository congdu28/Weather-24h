package com.weather24h.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather24h.app.data.model.AlertSeverity
import com.weather24h.app.data.model.SevereWeatherAlert
import com.weather24h.app.ui.theme.LocalCardOpacity
import com.weather24h.app.ui.theme.LocalContentTextColor
import com.weather24h.app.ui.theme.LocalIsDarkTheme
import com.weather24h.app.ui.theme.LocalSubtextColor

/**
 * Thanh cảnh báo thời tiết cực đoan / nguy hiểm thiết kế siêu gọn (Compact Alert Banner).
 * Chiều cao chuẩn mực 52dp khi thu gọn, tuyệt đối không xô lệch, không tràn dòng dọc.
 * Nhấn vào để mở rộng xem chi tiết và khuyến cáo an toàn.
 */
@Composable
fun SevereWeatherAlertCard(
    alerts: List<SevereWeatherAlert>,
    modifier: Modifier = Modifier
) {
    if (alerts.isEmpty()) return

    // Cho phép người dùng tắt tạm thời cảnh báo trong phiên này
    var isDismissed by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    if (isDismissed) return

    val topAlert = alerts.first()
    val cardOpacity = LocalCardOpacity.current
    val isDark = LocalIsDarkTheme.current
    val contentColor = LocalContentTextColor.current
    val subtextColor = LocalSubtextColor.current

    val primaryColor = when (topAlert.severity) {
        AlertSeverity.DANGER -> Color(0xFFEF4444)
        AlertSeverity.WARNING -> Color(0xFFF59E0B)
        AlertSeverity.INFO -> Color(0xFF38BDF8)
    }

    val bannerBg = when (topAlert.severity) {
        AlertSeverity.DANGER -> if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.30f * cardOpacity) else Color(0xFFFEE2E2)
        AlertSeverity.WARNING -> if (isDark) Color(0xFF78350F).copy(alpha = 0.30f * cardOpacity) else Color(0xFFFEF3C7)
        AlertSeverity.INFO -> if (isDark) Color(0xFF0C4A6E).copy(alpha = 0.30f * cardOpacity) else Color(0xFFE0F2FE)
    }

    val bannerBorder = primaryColor.copy(alpha = if (isDark) 0.55f else 0.65f)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bannerBg,
        border = BorderStroke(1.dp, bannerBorder),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Hàng tóm tắt siêu gọn (Header Row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge Icon tròn nhỏ gọn
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(primaryColor.copy(alpha = 0.20f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = topAlert.iconEmoji, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(9.dp))

                // Cột nội dung chính gồm 2 dòng chuẩn mực
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Dòng 1: Tiêu đề cảnh báo (Bold, 1 dòng duy nhất, không bị chèn ép)
                    Text(
                        text = topAlert.title,
                        color = primaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Dòng 2: Badge thời gian + Tóm tắt ngắn
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (topAlert.timeInfo.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = primaryColor.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = topAlert.timeInfo,
                                    color = if (isDark) Color.White.copy(alpha = 0.95f) else Color(0xFF1E293B),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .widthIn(max = 120.dp)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Text(
                            text = topAlert.headline,
                            color = contentColor.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Nút Mở rộng / Thu gọn chi tiết
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Thu gọn" else "Xem chi tiết",
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Nút Đóng cảnh báo tạm thời
                IconButton(
                    onClick = { isDismissed = true },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng cảnh báo",
                        tint = subtextColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Phần nội dung an toàn mở rộng (chỉ bung ra khi người dùng ấn xem chi tiết)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    HorizontalDivider(
                        color = primaryColor.copy(alpha = 0.25f),
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Hiển thị đầy đủ nội dung tóm tắt khi mở rộng
                    Text(
                        text = topAlert.headline,
                        color = contentColor.copy(alpha = 0.95f),
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Text(
                        text = "Khuyến cáo an toàn:",
                        color = primaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )

                    topAlert.safetyTips.forEach { tip ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.5.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                color = primaryColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 5.dp)
                            )
                            Text(
                                text = tip,
                                color = contentColor.copy(alpha = 0.88f),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    if (alerts.size > 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "+ Còn ${alerts.size - 1} cảnh báo khác trong ngày",
                            color = subtextColor,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
