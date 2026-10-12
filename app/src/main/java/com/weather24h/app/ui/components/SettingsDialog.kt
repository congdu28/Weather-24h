package com.weather24h.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.weather24h.app.BuildConfig

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
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var apiKeyText by remember { mutableStateOf(initialCustomKey ?: "") }
    var isKeyVisible by remember { mutableStateOf(false) }

    // Chế độ chọn: true = dùng key riêng, false = dùng key mặc định
    var selectedCustomMode by remember { mutableStateOf(isUsingCustomKey) }

    val refreshOptions = listOf(
        Pair(30, "30 phút"),
        Pair(60, "1 giờ (Chuẩn)"),
        Pair(120, "2 giờ"),
        Pair(180, "3 giờ"),
        Pair(300, "5 giờ"),
        Pair(0, "Tắt (Thủ công)")
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF0F172A),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Dialog: Title, Subtitle & Close Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Cài đặt & Cấu hình",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tùy chỉnh hệ thống & AI Stylist",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // PHẦN 1: GOOGLE GEMINI AI STYLIST
                // ==========================================
                SectionHeader(
                    icon = Icons.Default.AutoAwesome,
                    iconTint = Color(0xFFD8B4FE),
                    title = "TRỢ LÝ PHỐI ĐỒ AI (GEMINI)"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Card Lựa chọn A: Dùng Key mặc định của ứng dụng
                KeyModeSelectCard(
                    title = "Key mặc định của ứng dụng",
                    badge = "Khuyên dùng • Sẵn sàng",
                    badgeColor = Color(0xFF10B981),
                    description = "Model gemini-3.8-flash • Đã nhúng sẵn ngầm định, kết nối ổn định không cần cấu hình.",
                    isSelected = !selectedCustomMode,
                    onClick = {
                        selectedCustomMode = false
                        onClearCustomKey()
                        Toast.makeText(context, "Đã chuyển sang dùng Key mặc định của app", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Card Lựa chọn B: Dùng Key cá nhân từ Google AI Studio
                KeyModeSelectCard(
                    title = "Sử dụng API Key cá nhân của bạn",
                    badge = "Tùy chỉnh",
                    badgeColor = Color(0xFFA855F7),
                    description = "Nhập Gemini API Key riêng của bạn để quản lý hạn ngạch độc lập.",
                    isSelected = selectedCustomMode,
                    onClick = {
                        selectedCustomMode = true
                    }
                )

                // Vùng nhập API Key riêng (hiển thị khi chọn chế độ tùy chỉnh)
                AnimatedVisibility(visible = selectedCustomMode) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Nhập Google Gemini API Key:",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = apiKeyText,
                            onValueChange = { apiKeyText = it },
                            placeholder = {
                                Text(
                                    text = "Dán AI Key (bắt đầu bằng AIza... hoặc AQ...)",
                                    color = Color.White.copy(alpha = 0.35f),
                                    fontSize = 12.sp
                                )
                            },
                            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                        Icon(
                                            imageVector = if (isKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Ẩn/Hiện Key",
                                            tint = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFA855F7),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Nút dán từ Clipboard
                            TextButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()?.trim()
                                    if (!clipText.isNullOrEmpty()) {
                                        apiKeyText = clipText
                                        Toast.makeText(context, "Đã dán key từ bộ nhớ tạm", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Bộ nhớ tạm không có nội dung", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Dán từ Clipboard", color = Color(0xFF38BDF8), fontSize = 12.sp)
                            }

                            // Nút Lưu Key
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (apiKeyText.trim().isNotEmpty()) {
                                        onSaveCustomKey(apiKeyText.trim())
                                        Toast.makeText(context, "Đã lưu API Key riêng thành công!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Vui lòng nhập API Key hợp lệ", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Lưu & Kích hoạt", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Link hướng dẫn lấy Key
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Chưa có Key? Tạo miễn phí tại aistudio.google.com ↗",
                            color = Color(0xFF93C5FD),
                            fontSize = 11.5.sp,
                            modifier = Modifier
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                                    context.startActivity(intent)
                                }
                                .padding(vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // PHẦN 2: TỰ ĐỘNG CẬP NHẬT THỜI TIẾT
                // ==========================================
                SectionHeader(
                    icon = Icons.Default.Schedule,
                    iconTint = Color(0xFF38BDF8),
                    title = "CHU KỲ TỰ ĐỘNG CẬP NHẬT"
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tự động chạy ngầm cập nhật dự báo 24h & trang phục AI:",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    refreshOptions.forEach { (minutes, label) ->
                        val isSelected = currentAutoRefreshMinutes == minutes
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) Color(0xFF0284C7).copy(alpha = 0.35f)
                                    else Color.White.copy(alpha = 0.06f)
                                )
                                .border(
                                    1.2.dp,
                                    if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.12f),
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
                Divider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // PHẦN 3: ĐỊNH VỊ GPS & ĐỊA ĐIỂM
                // ==========================================
                SectionHeader(
                    icon = Icons.Default.LocationOn,
                    iconTint = Color(0xFF34D399),
                    title = "ĐỊNH VỊ GPS THÔNG MINH"
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tích hợp công nghệ Geocoding 2 lớp tiếng Việt (Android Geocoder + BigDataCloud) tự động hiển thị chính xác tên quận/huyện, phường/xã của bạn.",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // PHẦN 4: THÔNG TIN ỨNG DỤNG (APP INFO)
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Weather 24h",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Phiên bản v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Bản phát hành chuẩn",
                            color = Color(0xFF6EE7B7),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nút Xong đóng dialog
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Hoàn tất", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.7.sp
        )
    }
}

@Composable
private fun KeyModeSelectCard(
    title: String,
    badge: String,
    badgeColor: Color,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) Color(0xFF1E293B)
                else Color.White.copy(alpha = 0.04f)
            )
            .border(
                1.3.dp,
                if (isSelected) Color(0xFFA855F7) else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.2f))
                        .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = badge,
                        color = Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )
        }
    }
}
