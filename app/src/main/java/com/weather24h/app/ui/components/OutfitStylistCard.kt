package com.weather24h.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather24h.app.data.model.OutfitRecommendation

@Composable
fun OutfitStylistCard(
    outfit: OutfitRecommendation,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onConfigureApiKey: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF6B21A8).copy(alpha = 0.45f),
                        Color(0xFF1E1B4B).copy(alpha = 0.65f)
                    )
                )
            )
            .border(
                1.4.dp,
                Color(0xFFA855F7).copy(alpha = 0.4f),
                RoundedCornerShape(26.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            // Header Row: AI Badge & Buttons
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
                        text = if (outfit.isAIGenerated) "GEMINI AI STYLIST" else "SMART STYLIST",
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
                            tint = Color.White.copy(alpha = 0.7f),
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
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Làm mới gợi ý",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Style Vibe
            Text(
                text = outfit.styleVibe,
                color = Color(0xFFF0ABFC),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Summary
            Text(
                text = outfit.summary,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Clothes breakdown box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .padding(14.dp)
            ) {
                OutfitItemRow(
                    icon = Icons.Default.Checkroom,
                    label = "Áo khuyên mặc",
                    value = outfit.top
                )
                Divider(
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                OutfitItemRow(
                    icon = Icons.Default.Layers,
                    label = "Quần / Váy",
                    value = outfit.bottom
                )
                Divider(
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                OutfitItemRow(
                    icon = Icons.Default.RollerSkating,
                    label = "Giày dép",
                    value = outfit.footwear
                )
            }

            // Accessories
            if (outfit.accessories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "PHỤ KIỆN NÊN MANG THEO",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    outfit.accessories.take(3).forEach { acc ->
                        val isProtection = acc.contains("dù", ignoreCase = true) ||
                                acc.contains("mưa", ignoreCase = true) ||
                                acc.contains("kính", ignoreCase = true) ||
                                acc.contains("nắng", ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isProtection) Color(0xFF8B5CF6).copy(alpha = 0.3f)
                                    else Color.White.copy(alpha = 0.12f)
                                )
                                .border(
                                    1.dp,
                                    if (isProtection) Color(0xFFA855F7).copy(alpha = 0.6f)
                                    else Color.White.copy(alpha = 0.2f),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = acc,
                                color = Color.White,
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
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF59E0B).copy(alpha = 0.18f))
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = outfit.warningAlert,
                        color = Color(0xFFFDE68A),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun OutfitItemRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFC084FC),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
