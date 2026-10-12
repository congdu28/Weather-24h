package com.weather24h.app.ui.components

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.weather24h.app.BuildConfig
import com.weather24h.app.data.model.CityLocation

private enum class SettingsSubScreen {
    MAIN,
    NOTIFICATIONS,
    LOCATION_CITIES,
    GEMINI_API,
    SYNC_REFRESH,
    OFFLINE_WIDGET,
    ABOUT
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    initialCustomKey: String?,
    isUsingCustomKey: Boolean,
    hasDefaultKey: Boolean,
    currentAutoRefreshMinutes: Int,
    defaultCity: CityLocation,
    isStartWithGps: Boolean,
    isMorningNotif: Boolean,
    isSevereAlerts: Boolean,
    favoriteCities: List<CityLocation>,
    onDismiss: () -> Unit,
    onSaveCustomKey: (String) -> Unit,
    onClearCustomKey: () -> Unit,
    onSelectAutoRefreshInterval: (Int) -> Unit,
    onSetDefaultCity: (CityLocation) -> Unit,
    onSetStartWithGps: (Boolean) -> Unit,
    onSetMorningNotif: (Boolean) -> Unit,
    onSetSevereAlerts: (Boolean) -> Unit,
    onSendTestNotif: (Boolean) -> Unit,
    onClearCache: () -> Unit,
    onRemoveFavorite: (CityLocation) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var currentSubScreen by remember { mutableStateOf(SettingsSubScreen.MAIN) }

    var apiKeyText by remember { mutableStateOf(initialCustomKey ?: "") }
    var isKeyVisible by remember { mutableStateOf(false) }
    var selectedCustomMode by remember { mutableStateOf(isUsingCustomKey) }

    val refreshOptions = listOf(
        Pair(30, "30 phút"),
        Pair(60, "1 giờ (Chuẩn)"),
        Pair(120, "2 giờ"),
        Pair(180, "3 giờ"),
        Pair(300, "5 giờ"),
        Pair(0, "Tắt (Thủ công)")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color(0xFF090D16),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Top App Bar chuẩn phong cách ảnh mẫu (Nút Back tròn + Tiêu đề)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .clickable {
                                if (currentSubScreen == SettingsSubScreen.MAIN) {
                                    onDismiss()
                                } else {
                                    currentSubScreen = SettingsSubScreen.MAIN
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = when (currentSubScreen) {
                            SettingsSubScreen.MAIN -> "Cài đặt"
                            SettingsSubScreen.NOTIFICATIONS -> "Thông báo thời tiết"
                            SettingsSubScreen.LOCATION_CITIES -> "Cài đặt vị trí"
                            SettingsSubScreen.GEMINI_API -> "Quản lý API Key Gemini"
                            SettingsSubScreen.SYNC_REFRESH -> "Chu kỳ làm mới"
                            SettingsSubScreen.OFFLINE_WIDGET -> "Ngoại tuyến & Widget"
                            SettingsSubScreen.ABOUT -> "Về ứng dụng"
                        },
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Nội dung các màn hình
                AnimatedContent(
                    targetState = currentSubScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "SettingsNav",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { screen ->
                    when (screen) {
                        // 1. MÀN HÌNH CHÍNH (THEO PHONG CÁCH INSET GROUPED CARDS TRONG ẢNH MẪU)
                        SettingsSubScreen.MAIN -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // NHÓM 1: THÔNG BÁO THỜI TIẾT
                                InsetGroupCard {
                                    SettingsRowItem(
                                        icon = Icons.Default.Notifications,
                                        iconTint = Color(0xFF38BDF8),
                                        title = "Thông báo thời tiết",
                                        subtitle = if (isMorningNotif || isSevereAlerts) "Đang bật" else "Tắt",
                                        onClick = { currentSubScreen = SettingsSubScreen.NOTIFICATIONS }
                                    )
                                }

                                // NHÓM 2: CÀI ĐẶT VỊ TRÍ
                                InsetGroupCard {
                                    SettingsRowItem(
                                        icon = Icons.Default.LocationOn,
                                        iconTint = Color(0xFF34D399),
                                        title = "Cài đặt vị trí",
                                        subtitle = if (isStartWithGps) "Vị trí GPS hiện tại" else defaultCity.name,
                                        onClick = { currentSubScreen = SettingsSubScreen.LOCATION_CITIES }
                                    )
                                }

                                // NHÓM 3: AI STYLIST & CHẾ ĐỘ DỊCH VỤ
                                InsetGroupCard {
                                    SettingsRowItem(
                                        icon = Icons.Default.AutoAwesome,
                                        iconTint = Color(0xFFC084FC),
                                        title = "Quản lý API Key Gemini",
                                        subtitle = if (isUsingCustomKey) "Key cá nhân của bạn" else "Key mặc định của app",
                                        onClick = { currentSubScreen = SettingsSubScreen.GEMINI_API }
                                    )
                                    ItemDivider()
                                    SettingsRowItem(
                                        icon = Icons.Default.Schedule,
                                        iconTint = Color(0xFFFBBF24),
                                        title = "Chu kỳ làm mới",
                                        subtitle = "${currentAutoRefreshMinutes.takeIf { it > 0 }?.let { "$it phút" } ?: "Thủ công"}",
                                        onClick = { currentSubScreen = SettingsSubScreen.SYNC_REFRESH }
                                    )
                                }

                                // NHÓM 4: NGOẠI TUYẾN & TIỆN ÍCH WIDGET
                                InsetGroupCard {
                                    SettingsRowItem(
                                        icon = Icons.Default.OfflineBolt,
                                        iconTint = Color(0xFFF472B6),
                                        title = "Dữ liệu ngoại tuyến & Widget",
                                        subtitle = "Đã bật bộ nhớ đệm tự động",
                                        onClick = { currentSubScreen = SettingsSubScreen.OFFLINE_WIDGET }
                                    )
                                    ItemDivider()
                                    SettingsRowItem(
                                        icon = Icons.Default.Info,
                                        iconTint = Color(0xFF94A3B8),
                                        title = "Về ứng dụng",
                                        subtitle = "Phiên bản v${BuildConfig.VERSION_NAME}",
                                        onClick = { currentSubScreen = SettingsSubScreen.ABOUT }
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }

                        // 2. SUB-SCREEN: THÔNG BÁO THỜI TIẾT
                        SettingsSubScreen.NOTIFICATIONS -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                InsetGroupCard {
                                    ToggleRowItem(
                                        title = "Bản tin thời tiết buổi sáng",
                                        subtitle = "Gửi tóm tắt thời tiết & gợi ý trang phục mỗi sáng (7h00)",
                                        isChecked = isMorningNotif,
                                        onCheckedChange = onSetMorningNotif
                                    )
                                    ItemDivider()
                                    ToggleRowItem(
                                        title = "Cảnh báo thời tiết nguy hiểm",
                                        subtitle = "Cảnh báo khẩn khi có Dông sét, mưa lớn hoặc tia UV nguy hại",
                                        isChecked = isSevereAlerts,
                                        onCheckedChange = onSetSevereAlerts
                                    )
                                }

                                // Nút Gửi thử nghiệm
                                InsetGroupCard {
                                    Button(
                                        onClick = {
                                            onSendTestNotif(false)
                                            Toast.makeText(context, "Đã gửi thông báo buổi sáng mẫu!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Gửi thử thông báo buổi sáng", color = Color.White, fontSize = 13.sp)
                                    }

                                    Button(
                                        onClick = {
                                            onSendTestNotif(true)
                                            Toast.makeText(context, "Đã gửi cảnh báo dông sét mẫu!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Gửi thử cảnh báo Dông bão", color = Color.White, fontSize = 13.sp)
                                    }
                                }
                            }
                        }

                        // 3. SUB-SCREEN: CÀI ĐẶT VỊ TRÍ & THÀNH PHỐ
                        SettingsSubScreen.LOCATION_CITIES -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                InsetGroupCard {
                                    ToggleRowItem(
                                        title = "Luôn khởi động bằng vị trí GPS",
                                        subtitle = "Tự động nhận diện quận/huyện hiện tại bằng GPS khi mở app",
                                        isChecked = isStartWithGps,
                                        onCheckedChange = onSetStartWithGps
                                    )
                                }

                                InsetGroupCard {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "THÀNH PHỐ MẶC ĐỊNH KHI VÀO APP",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.7.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf(
                                                CityLocation.DEFAULT_LOCATION,
                                                CityLocation(name = "Đà Nẵng", country = "Việt Nam", latitude = 16.0544, longitude = 108.2022),
                                                CityLocation(name = "TP. Hồ Chí Minh", country = "Việt Nam", latitude = 10.8231, longitude = 106.6297),
                                                CityLocation(name = "Đà Lạt", country = "Việt Nam", latitude = 11.9404, longitude = 108.4583)
                                            ).forEach { city ->
                                                val isSelected = defaultCity.name == city.name
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = {
                                                        onSetDefaultCity(city)
                                                        Toast.makeText(context, "Đã chọn ${city.name} làm mặc định", Toast.LENGTH_SHORT).show()
                                                    },
                                                    label = { Text(city.name) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = Color(0xFF0284C7),
                                                        selectedLabelColor = Color.White
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                // Danh sách thành phố yêu thích
                                InsetGroupCard {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "DANH SÁCH YÊU THÍCH (${favoriteCities.size})",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.7.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))

                                        if (favoriteCities.isEmpty()) {
                                            Text(
                                                text = "Chưa có thành phố yêu thích nào. Hãy nhấn biểu tượng ngôi sao ⭐ trên màn hình chính để thêm!",
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 12.sp
                                            )
                                        } else {
                                            favoriteCities.forEach { city ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD166), modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(city.name, color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                                                    }
                                                    IconButton(onClick = { onRemoveFavorite(city) }, modifier = Modifier.size(28.dp)) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 4. SUB-SCREEN: QUẢN LÝ API KEY GEMINI
                        SettingsSubScreen.GEMINI_API -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                InsetGroupCard {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        KeyModeSelectCard(
                                            title = "Key mặc định của ứng dụng",
                                            badge = "Khuyên dùng • Sẵn sàng",
                                            badgeColor = Color(0xFF10B981),
                                            description = "Model gemini-3.8-flash • Đã nhúng sẵn ngầm định, kết nối ổn định không cần cấu hình.",
                                            isSelected = !selectedCustomMode,
                                            onClick = {
                                                selectedCustomMode = false
                                                onClearCustomKey()
                                                Toast.makeText(context, "Đã kích hoạt Key mặc định của app", Toast.LENGTH_SHORT).show()
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        KeyModeSelectCard(
                                            title = "Sử dụng API Key cá nhân của bạn",
                                            badge = "Tùy chỉnh riêng",
                                            badgeColor = Color(0xFFA855F7),
                                            description = "Nhập Gemini API Key riêng từ Google AI Studio để quản lý quota độc lập.",
                                            isSelected = selectedCustomMode,
                                            onClick = { selectedCustomMode = true }
                                        )

                                        if (selectedCustomMode) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            OutlinedTextField(
                                                value = apiKeyText,
                                                onValueChange = { apiKeyText = it },
                                                placeholder = { Text("Dán key cá nhân...", color = Color.White.copy(alpha = 0.35f), fontSize = 12.sp) },
                                                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                                trailingIcon = {
                                                    IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                                        Icon(
                                                            imageVector = if (isKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                            contentDescription = null,
                                                            tint = Color.White.copy(alpha = 0.6f),
                                                            modifier = Modifier.size(18.dp)
                                                        )
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

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                        val text = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()?.trim()
                                                        if (!text.isNullOrEmpty()) {
                                                            apiKeyText = text
                                                            Toast.makeText(context, "Đã dán key từ Clipboard", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Dán từ Clipboard", fontSize = 11.5.sp, maxLines = 1)
                                                }

                                                Button(
                                                    onClick = {
                                                        focusManager.clearFocus()
                                                        if (apiKeyText.trim().isNotEmpty()) {
                                                            onSaveCustomKey(apiKeyText.trim())
                                                            Toast.makeText(context, "Đã lưu và kích hoạt Key thành công!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Lưu Key", color = Color.White, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 5. SUB-SCREEN: CHU KỲ LÀM MỚI
                        SettingsSubScreen.SYNC_REFRESH -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                InsetGroupCard {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "TỰ ĐỘNG LÀM MỚI DỮ LIỆU THỜI TIẾT",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.7.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))

                                        refreshOptions.forEach { (minutes, label) ->
                                            val isSelected = currentAutoRefreshMinutes == minutes
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable { onSelectAutoRefreshInterval(minutes) }
                                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(label, color = if (isSelected) Color(0xFF38BDF8) else Color.White, fontSize = 14.sp)
                                                if (isSelected) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                                                }
                                            }
                                            ItemDivider()
                                        }
                                    }
                                }
                            }
                        }

                        // 6. SUB-SCREEN: NGOẠI TUYẾN & WIDGET
                        SettingsSubScreen.OFFLINE_WIDGET -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                InsetGroupCard {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Widgets,
                                                contentDescription = null,
                                                tint = Color(0xFFC084FC),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "TIỆN ÍCH WIDGET MÀN HÌNH CHÍNH",
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Cách thêm Widget ra màn hình điện thoại:\n• Bước 1: Nhấn giữ vào vùng trống trên màn hình chính của bạn.\n• Bước 2: Chọn mục 'Tiện ích' (Widgets).\n• Bước 3: Tìm ứng dụng 'Weather 24h' và kéo thả Widget ra vị trí bạn thích!",
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontSize = 12.5.sp,
                                            lineHeight = 19.sp
                                        )
                                    }
                                }

                                InsetGroupCard {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Storage,
                                                contentDescription = null,
                                                tint = Color(0xFF38BDF8),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "BỘ NHỚ ĐỆM NGOẠI TUYẾN (CACHE)",
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Dữ liệu thời tiết và gợi ý phong cách Gemini được tự động lưu vào máy. Khi bạn mất kết nối mạng hoặc ở vùng sóng yếu, ứng dụng vẫn hoạt động tức thì với dữ liệu đã lưu.",
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontSize = 12.5.sp,
                                            lineHeight = 18.sp
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Button(
                                            onClick = {
                                                onClearCache()
                                                Toast.makeText(context, "Đã xóa toàn bộ bộ nhớ đệm ngoại tuyến!", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.2f)),
                                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Xóa bộ nhớ đệm ngoại tuyến", color = Color(0xFFFCA5A5), fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }

                        // 7. SUB-SCREEN: VỀ ỨNG DỤNG
                        SettingsSubScreen.ABOUT -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                InsetGroupCard {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(RoundedCornerShape(22.dp))
                                                .background(Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF7C3AED)))),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Cloud, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("Weather 24h", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                        Text("Phiên bản v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Thông tin tác giả: CD
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = Color(0xFF38BDF8),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Tác giả: CD",
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.5.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = "Ứng dụng thời tiết cao cấp được phát triển 100% bằng Native Android Kotlin & Jetpack Compose, tích hợp trí tuệ nhân tạo Google Gemini 3.8 Flash và API Open-Meteo.",
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontSize = 12.5.sp,
                                            lineHeight = 18.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Khối Card Inset bo góc kiểu iOS / HyperOS theo ảnh mẫu
@Composable
private fun InsetGroupCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF131A2A),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

// Hàng tùy chọn có icon, title, subtitle và chevron phải '>'
@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.wrapContentWidth()
        ) {
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 12.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.35f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ToggleRowItem(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = Color.White.copy(alpha = 0.55f), fontSize = 11.5.sp, lineHeight = 16.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0284C7)
            )
        )
    }
}

@Composable
private fun ItemDivider() {
    Divider(
        color = Color.White.copy(alpha = 0.06f),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
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
            .background(if (isSelected) Color(0xFF1E293B) else Color.White.copy(alpha = 0.04f))
            .border(1.2.dp, if (isSelected) Color(0xFFA855F7) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
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
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.2f))
                        .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                ) {
                    Text(badge, color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(description, color = Color.White.copy(alpha = 0.65f), fontSize = 11.5.sp, lineHeight = 16.sp)
        }
    }
}
