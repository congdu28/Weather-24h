package com.weather24h.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather24h.app.data.model.AlertSeverity
import com.weather24h.app.data.model.SevereWeatherAlert
import com.weather24h.app.ui.theme.LocalCardOpacity

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

    // Bảng màu gradient theo mức độ nghiêm trọng
    val (primaryColor, gradientColors) = when (topAlert.severity) {
        AlertSeverity.DANGER -> Pair(
            Color(0xFFEF4444),
            listOf(Color(0xFFDC2626).copy(alpha = 0.28f * cardOpacity), Color(0xFF991B1B).copy(alpha = 0.35f * cardOpacity))
        )
        AlertSeverity.WARNING -> Pair(
            Color(0xFFF59E0B),
            listOf(Color(0xFFD97706).copy(alpha = 0.25f * cardOpacity), Color(0xFFB45309).copy(alpha = 0.32f * cardOpacity))
        )
        AlertSeverity.INFO -> Pair(
            Color(0xFF38BDF8),
            listOf(Color(0xFF0284C7).copy(alpha = 0.22f * cardOpacity), Color(0xFF0369A1).copy(alpha = 0.30f * cardOpacity))
        )
    }

    // Hiệu ứng nhịp đập (pulse) nhẹ nhàng viền ngoài
    val infiniteTransition = rememberInfiniteTransition(label = "AlertPulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BorderPulse"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.5f * cardOpacity),
        border = BorderStroke(1.2.dp, primaryColor.copy(alpha = borderAlpha)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { isExpanded = !isExpanded }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradientColors))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header: Emoji + Title + Dismiss Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(primaryColor.copy(alpha = 0.22f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = topAlert.iconEmoji, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = topAlert.title,
                                color = primaryColor,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = primaryColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = topAlert.timeInfo,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = topAlert.headline,
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Nút Đóng tạm thời
                    IconButton(
                        onClick = { isDismissed = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng cảnh báo",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Chi tiết các khuyến cáo an toàn khi mở rộng
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        HorizontalDivider(
                            color = primaryColor.copy(alpha = 0.3f),
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Text(
                            text = "Khuyến cáo an toàn:",
                            color = primaryColor,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )

                        topAlert.safetyTips.forEach { tip ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    color = primaryColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                                Text(
                                    text = tip,
                                    color = Color.White.copy(alpha = 0.88f),
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        if (alerts.size > 1) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "+ Còn ${alerts.size - 1} cảnh báo khác trong ngày",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }

                // Dòng bấm xem thêm lời khuyên
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "Thu gọn lời khuyên" else "Xem khuyến cáo phòng ngừa",
                        color = primaryColor.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = primaryColor.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
