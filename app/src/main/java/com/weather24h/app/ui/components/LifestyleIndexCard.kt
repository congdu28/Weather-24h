package com.weather24h.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.weather24h.app.data.model.LifestyleCalculator
import com.weather24h.app.data.model.LifestyleItem
import com.weather24h.app.data.model.WeatherData
import com.weather24h.app.ui.theme.LocalCardOpacity
import com.weather24h.app.ui.theme.LocalIsDarkTheme
import com.weather24h.app.ui.theme.LocalContentTextColor
import com.weather24h.app.ui.theme.LocalSubtextColor
import com.weather24h.app.ui.theme.LocalCardBorder
import com.weather24h.app.ui.theme.LocalCardGlassBg

@Composable
fun LifestyleIndexCard(
    weather: WeatherData,
    modifier: Modifier = Modifier
) {
    val items = remember(weather) { LifestyleCalculator.calculate(weather) }
    var selectedItem by remember { mutableStateOf<LifestyleItem?>(null) }
    val cardOpacity = LocalCardOpacity.current
    val isDark = LocalIsDarkTheme.current
    val textColor = LocalContentTextColor.current
    val subtextColor = LocalSubtextColor.current
    val accentColor = if (isDark) Color(0xFF86EFAC) else Color(0xFF166534)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = LocalCardGlassBg.current,
        border = BorderStroke(1.dp, LocalCardBorder.current),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(accentColor.copy(alpha = 0.12f * cardOpacity), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🧘", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CHỈ SỐ SỨC KHỎE & ĐỜI SỐNG",
                        color = accentColor,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Gợi ý sinh hoạt & hoạt động ngoài trời",
                        color = subtextColor,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid 2 cột x 3 dòng
            val chunked = items.chunked(2)
            chunked.forEachIndexed { index, rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { item ->
                        LifestyleGridTile(
                            item = item,
                            onClick = { selectedItem = item },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                if (index < chunked.size - 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }

    // Modal chi tiết khi chạm vào từng mục
    selectedItem?.let { item ->
        Dialog(onDismissRequest = { selectedItem = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, LocalCardBorder.current),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(text = item.iconEmoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    color = textColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = lifestyleBadgeColor(item.badgeColor, isDark).copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = item.status,
                                        color = lifestyleBadgeColor(item.badgeColor, isDark),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { selectedItem = null }, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Đóng",
                                tint = subtextColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Phân tích & Lời khuyên:",
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.detailedAdvice,
                        color = subtextColor,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { selectedItem = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Đã hiểu",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LifestyleGridTile(
    item: LifestyleItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val badgeColor = lifestyleBadgeColor(item.badgeColor, isDark)
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = (if (isDark) Color.White else Color(0xFF0F172A))
            .copy(alpha = 0.05f * LocalCardOpacity.current),
        border = BorderStroke(1.dp, LocalCardBorder.current),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Icon + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = item.iconEmoji, fontSize = 20.sp)

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f).padding(start = 6.dp)
                ) {
                    Text(
                        text = item.status,
                        color = badgeColor,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = item.name,
                color = LocalContentTextColor.current,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Short tip
            Text(
                text = item.shortTip,
                color = LocalSubtextColor.current,
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
                maxLines = 3
            )
        }
    }
}

private fun lifestyleBadgeColor(color: Long, isDark: Boolean): Color = when (color) {
    0xFFEF4444L -> if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C)
    0xFFF59E0BL -> if (isDark) Color(0xFFFCD34D) else Color(0xFF92400E)
    0xFF22C55EL -> if (isDark) Color(0xFF86EFAC) else Color(0xFF166534)
    0xFFA855F7L -> if (isDark) Color(0xFFD8B4FE) else Color(0xFF6B21A8)
    else -> if (isDark) Color(0xFF7DD3FC) else Color(0xFF075985)
}
