package com.weather24h.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather24h.app.data.model.OutfitRecommendation
import com.weather24h.app.ui.theme.LocalCardOpacity
import com.weather24h.app.ui.theme.LocalIsDarkTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OutfitStylistCard(
    outfit: OutfitRecommendation?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onConfigureApiKey: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Nếu chưa có dữ liệu và đang tải, hiển thị Skeleton Shimmer Card chuyên nghiệp
    if (outfit == null) {
        OutfitStylistSkeletonCard(onConfigureApiKey = onConfigureApiKey, modifier = modifier)
        return
    }

    val isDark = LocalIsDarkTheme.current
    val cardOpacity = LocalCardOpacity.current
    val cardGradient = if (isDark) {
        Brush.linearGradient(
            listOf(
                Color(0xFF581C87).copy(alpha = (0.50f * (cardOpacity / 0.80f)).coerceIn(0.15f, 0.95f)),
                Color(0xFF1E1B4B).copy(alpha = (0.70f * (cardOpacity / 0.80f)).coerceIn(0.20f, 0.98f))
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0xFFFAF5FF).copy(alpha = cardOpacity),
                Color(0xFFF3E8FF).copy(alpha = cardOpacity)
            )
        )
    }
    val cardBorderColor = if (isDark) {
        if (isLoading) Color(0xFFF472B6).copy(alpha = 0.6f) else Color(0xFFA855F7).copy(alpha = 0.4f)
    } else {
        if (isLoading) Color(0xFFF472B6).copy(alpha = 0.8f) else Color(0xFFD8B4FE)
    }

    val actionButtonTint = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF6B21A8)
    val styleVibeColor = if (isDark) Color(0xFFF0ABFC) else Color(0xFF7E22CE)
    val summaryColor = if (isDark) Color.White.copy(alpha = 0.92f) else Color(0xFF334155)
    val clothesBoxBg = if (isDark) Color.Black.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.70f)
    val clothesDividerColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val accessoriesTitleColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF6B21A8)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(cardGradient)
            .border(1.4.dp, cardBorderColor, RoundedCornerShape(26.dp))
            .padding(18.dp)
    ) {
        Column {
            // Header Row: AI Badge & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AI Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "AI GỢI Ý STYLIST",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                // Action buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onConfigureApiKey,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cài đặt Gemini API",
                            tint = actionButtonTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onRefresh,
                        enabled = !isLoading,
                        modifier = Modifier.size(32.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = if (isDark) Color(0xFFF0ABFC) else Color(0xFF9333EA),
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Làm mới gợi ý",
                                tint = actionButtonTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Thanh tiến trình mỏng nếu đang làm mới lại
            if (isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    color = if (isDark) Color(0xFFD8B4FE) else Color(0xFF9333EA),
                    trackColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE9D5FF),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Style Vibe
            Text(
                text = outfit.styleVibe,
                color = styleVibeColor,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Summary Text
            Text(
                text = outfit.summary,
                color = summaryColor,
                fontSize = 13.5.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Clothes breakdown box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(clothesBoxBg)
                    .padding(14.dp)
            ) {
                OutfitItemRow(
                    icon = Icons.Default.Checkroom,
                    label = "Áo",
                    value = outfit.top
                )
                Divider(
                    color = clothesDividerColor,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                OutfitItemRow(
                    icon = Icons.Default.Layers,
                    label = "Quần / Váy",
                    value = outfit.bottom
                )
                Divider(
                    color = clothesDividerColor,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                OutfitItemRow(
                    icon = Icons.Default.RollerSkating,
                    label = "Giày dép",
                    value = outfit.footwear
                )
            }

            // Accessories - Hỗ trợ FlowRow tự động xuống dòng không bao giờ vỡ layout
            if (outfit.accessories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "PHỤ KIỆN GỢI Ý MANG THEO",
                    color = accessoriesTitleColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    outfit.accessories.forEach { acc ->
                        val isProtection = acc.contains("dù", ignoreCase = true) ||
                                acc.contains("mưa", ignoreCase = true) ||
                                acc.contains("kính", ignoreCase = true) ||
                                acc.contains("nắng", ignoreCase = true) ||
                                acc.contains("ô", ignoreCase = true)

                        val accBg = if (isDark) {
                            if (isProtection) Color(0xFF8B5CF6).copy(alpha = 0.35f)
                            else Color.White.copy(alpha = 0.12f)
                        } else {
                            if (isProtection) Color(0xFFF3E8FF)
                            else Color(0xFFF1F5F9)
                        }
                        val accBorder = if (isDark) {
                            if (isProtection) Color(0xFFA855F7).copy(alpha = 0.6f)
                            else Color.White.copy(alpha = 0.2f)
                        } else {
                            if (isProtection) Color(0xFFD8B4FE)
                            else Color(0xFFCBD5E1)
                        }
                        val accTextColor = if (isDark) {
                            Color.White
                        } else {
                            if (isProtection) Color(0xFF6B21A8) else Color(0xFF1E293B)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(accBg)
                                .border(1.dp, accBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = acc,
                                color = accTextColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Weather Warning Alert Box
            if (!outfit.warningAlert.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                val alertBg = if (isDark) Color(0xFFF59E0B).copy(alpha = 0.20f) else Color(0xFFFEF3C7)
                val alertBorder = if (isDark) Color(0xFFF59E0B).copy(alpha = 0.45f) else Color(0xFFFDE68A)
                val alertTextColor = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E)

                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(alertBg)
                        .border(1.dp, alertBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = outfit.warningAlert,
                        color = alertTextColor,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Hàng hiển thị chi tiết trang phục responsive (không bị overflow)
 */
@Composable
fun OutfitItemRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    val isDark = LocalIsDarkTheme.current
    val iconTint = if (isDark) Color(0xFFC084FC) else Color(0xFF9333EA)
    val labelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B)
    val valueColor = if (isDark) Color.White else Color(0xFF0F172A)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = labelColor,
            fontSize = 12.sp,
            modifier = Modifier.width(76.dp)
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Skeleton Card hiển thị shimmer khi đang phân tích gợi ý AI, giữ vững layout
 */
@Composable
fun OutfitStylistSkeletonCard(
    onConfigureApiKey: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "SkeletonShimmer")
    val shimmerTranslate by transition.animateFloat(
        initialValue = -300f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Shimmer"
    )

    val isDark = LocalIsDarkTheme.current
    val shimmerBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.05f),
                Color.White.copy(alpha = 0.16f),
                Color.White.copy(alpha = 0.05f)
            ),
            start = Offset(shimmerTranslate, 0f),
            end = Offset(shimmerTranslate + 250f, 250f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFE9D5FF).copy(alpha = 0.35f),
                Color(0xFFDDD6FE).copy(alpha = 0.70f),
                Color(0xFFE9D5FF).copy(alpha = 0.35f)
            ),
            start = Offset(shimmerTranslate, 0f),
            end = Offset(shimmerTranslate + 250f, 250f)
        )
    }

    val cardOpacity = LocalCardOpacity.current
    val skeletonBg = if (isDark) {
        Brush.linearGradient(
            listOf(
                Color(0xFF3B0764).copy(alpha = (0.40f * (cardOpacity / 0.80f)).coerceIn(0.12f, 0.90f)),
                Color(0xFF1E1B4B).copy(alpha = (0.60f * (cardOpacity / 0.80f)).coerceIn(0.18f, 0.95f))
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0xFFFAF5FF).copy(alpha = cardOpacity),
                Color(0xFFF3E8FF).copy(alpha = cardOpacity)
            )
        )
    }
    val skeletonBorder = if (isDark) Color(0xFFA855F7).copy(alpha = 0.25f) else Color(0xFFD8B4FE)
    val clothesBoxPlaceholderBg = if (isDark) Color.Black.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.65f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(skeletonBg)
            .border(1.2.dp, skeletonBorder, RoundedCornerShape(26.dp))
            .padding(18.dp)
    ) {
        Column {
            // Header: AI Badge & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF8B5CF6).copy(alpha = 0.8f), Color(0xFFEC4899).copy(alpha = 0.8f))
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI GỢI Ý STYLIST ĐANG PHÂN TÍCH...",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                }

                IconButton(
                    onClick = onConfigureApiKey,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Cài đặt Gemini API",
                        tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF6B21A8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Shimmer placeholders for text
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(shimmerBrush)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(shimmerBrush)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(shimmerBrush)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Placeholder clothes box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(clothesBoxPlaceholderBg)
                    .padding(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmerBrush)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmerBrush)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmerBrush)
                )
            }
        }
    }
}
