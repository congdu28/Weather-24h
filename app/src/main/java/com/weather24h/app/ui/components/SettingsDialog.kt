package com.weather24h.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    initialCustomKey: String?,
    isUsingCustomKey: Boolean,
    hasDefaultKey: Boolean,
    currentAutoRefreshMinutes: Int,
    onDismiss: () -> Unit,
    onSaveCustomKey: (String) -> Unit,
    onClearCustomKey: () -> Unit,
    onSelectAutoRefreshInterval: (Int) -> Unit
) {
    var apiKeyText by remember { mutableStateOf(initialCustomKey ?: "") }

    val refreshOptions = listOf(
        Pair(30, "30 phút"),
        Pair(60, "1 giờ"),
        Pair(120, "2 giờ"),
        Pair(180, "3 giờ"),
        Pair(300, "5 giờ"),
        Pair(0, "Tắt (Thủ công)")
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E293B),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(0xFFA855F7),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cài đặt ứng dụng",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION 1: AUTO REFRESH INTERVAL
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TỰ ĐỘNG CẬP NHẬT THỜI TIẾT",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tự động làm mới dữ liệu thời tiết và gợi ý trang phục:",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    refreshOptions.forEach { (minutes, label) ->
                        val isSelected = currentAutoRefreshMinutes == minutes
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) Color(0xFF0284C7).copy(alpha = 0.35f)
                                    else Color.White.copy(alpha = 0.08f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectAutoRefreshInterval(minutes) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.75f),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 2: GEMINI AI KEY
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFC084FC),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GOOGLE GEMINI AI STYLIST",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status banner
                val statusText = when {
                    isUsingCustomKey -> "Đang dùng Key riêng của bạn"
                    hasDefaultKey -> "Đang dùng Key mặc định của app"
                    else -> "Đang dùng Smart Stylist chuyên gia (Nội bộ)"
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFA855F7).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Trạng thái: $statusText",
                        color = Color(0xFFE9D5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Bạn có thể nhập API Key riêng (Google AI Studio) để sử dụng hoặc để trống để dùng key mặc định.",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = apiKeyText,
                    onValueChange = { apiKeyText = it },
                    placeholder = {
                        Text(
                            text = if (isUsingCustomKey) "Key riêng của bạn" else "Dán API Key riêng của bạn...",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 13.sp
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA855F7),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isUsingCustomKey) {
                        TextButton(
                            onClick = {
                                apiKeyText = ""
                                onClearCustomKey()
                            }
                        ) {
                            Text("Dùng lại Key mặc định", color = Color(0xFFFCA5A5), fontSize = 12.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Button(
                        onClick = {
                            onSaveCustomKey(apiKeyText)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Lưu Key riêng", color = Color.White, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Version & Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weather 24h v${com.weather24h.app.BuildConfig.VERSION_NAME}",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 12.sp
                    )
                    TextButton(onClick = onDismiss) {
                        Text("Xong", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
