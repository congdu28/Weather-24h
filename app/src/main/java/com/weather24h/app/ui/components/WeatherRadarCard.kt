package com.weather24h.app.ui.components

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.weather24h.app.data.model.WeatherData
import com.weather24h.app.ui.theme.LocalCardBorder
import com.weather24h.app.ui.theme.LocalCardGlassBg
import com.weather24h.app.ui.theme.LocalIsDarkTheme
import com.weather24h.app.ui.theme.LocalSubtextColor
import java.util.Locale

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WeatherRadarCard(
    weather: WeatherData,
    modifier: Modifier = Modifier
) {
    var showFullscreenDialog by remember { mutableStateOf(false) }
    // Nguồn Radar: "windy" (Mượt nhất dùng WebGL GPU) hoặc "rainviewer" (Trạm mặt đất 2D tương thích cao)
    var selectedEngine by remember { mutableStateOf("windy") }
    // Lớp hiển thị: "radar" (Mưa), "satellite" (Mây), "wind" (Gió - hỗ trợ trên Windy)
    var selectedLayer by remember { mutableStateOf("radar") }
    // Trạng thái sự cố WebGL trên thiết bị để hiển thị thông báo và tự động chuyển sang RainViewer
    var hasWebGLError by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var isPreviewLoading by remember(selectedEngine, selectedLayer, reloadKey) { mutableStateOf(true) }
    var isFullscreenLoading by remember(selectedEngine, selectedLayer, reloadKey) { mutableStateOf(true) }

    val isDark = LocalIsDarkTheme.current
    val subtextColor = LocalSubtextColor.current

    val lat = weather.location.latitude
    val lon = weather.location.longitude
    val locationFullName = remember(weather.location) {
        val base = weather.location.name.trim()
        val admin = weather.location.admin1?.trim()
        if (!admin.isNullOrBlank() && !base.contains(admin, ignoreCase = true)) {
            "$base, $admin"
        } else {
            base
        }
    }
    val shortPinLabel = remember(weather.location.name) {
        weather.location.name.substringBefore(",").trim().ifBlank { weather.location.name }
    }
    val accentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

    // Xử lý khi WebGL của Windy bị lỗi trên một số GPU/thiết bị
    val onRadarFailure: (String) -> Unit = { _ ->
        hasWebGLError = true
        if (selectedEngine == "windy") {
            selectedEngine = "rainviewer"
            if (selectedLayer == "wind") selectedLayer = "radar"
        }
    }

    // Hàm tạo URL bản đồ động theo Engine và Layer:
    // Nâng mức zoom mặc định lên 10 (preview) và 11 (fullscreen) + bật marker=true, detailLat/Lon
    // để hiển thị chính xác cấp Xã/Phường/Huyện (như xã Hoằng Thanh) thay vì bị gom chung vào TP. Thanh Hóa ở mức zoom xa.
    fun buildMapUrl(engine: String, layer: String, isFullscreen: Boolean): String {
        val latStr = String.format(Locale.US, "%.4f", lat)
        val lonStr = String.format(Locale.US, "%.4f", lon)
        return if (engine == "windy") {
            val zoom = if (isFullscreen) 11 else 10
            val windyOverlay = when (layer) {
                "satellite" -> "satellite"
                "wind" -> "wind"
                else -> "radar"
            }
            val product = if (layer == "wind") "ecmwf" else windyOverlay
            "https://embed.windy.com/embed2.html?lat=$latStr&lon=$lonStr&detailLat=$latStr&detailLon=$lonStr&zoom=$zoom&level=surface&overlay=$windyOverlay&product=$product&menu=&message=false&marker=true&calendar=now&pressure=&type=map&location=coordinates&metricRain=mm&metricTemp=%C2%B0C&metricWind=km%2Fh&radarRange=-1"
        } else {
            val zoom = if (isFullscreen) 11 else 10
            val rvLayer = if (layer == "satellite") "satellite" else "radar"
            "https://www.rainviewer.com/map.html?loc=$latStr,$lonStr,$zoom&oFa=0&oC=1&oU=0&oCS=1&oF=0&oAP=1&c=3&o=83&lm=1&layer=$rvLayer&sm=1&sn=1"
        }
    }

    // Khối thẻ xem nhanh Radar trên màn hình chính
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = LocalCardGlassBg.current,
        border = BorderStroke(0.5.dp, LocalCardBorder.current),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Title & Fullscreen action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (isDark) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color(0xFFE0F2FE),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🛰️", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "RADAR KHÍ TƯỢNG",
                            color = accentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "📍 $locationFullName (${String.format(Locale.US, "%.2f, %.2f", lat, lon)})",
                            color = subtextColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Nút làm mới radar
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F5F9),
                        border = BorderStroke(
                            0.5.dp,
                            if (isDark) Color.White.copy(alpha = 0.08f) else Color(0x150F172A)
                        ),
                        modifier = Modifier.clickable { reloadKey++ }
                    ) {
                        Box(
                            modifier = Modifier.padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Làm mới Radar",
                                tint = accentColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    // Nút Phóng to toàn màn hình
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.18f) else Color(0xFFE0F2FE),
                        border = BorderStroke(
                            0.5.dp,
                            if (isDark) Color(0xFF38BDF8).copy(alpha = 0.25f) else Color(0xFF0284C7).copy(alpha = 0.20f)
                        ),
                        modifier = Modifier.clickable { showFullscreenDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Toàn màn hình",
                                tint = accentColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Phóng to",
                                color = accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hàng 1: Bộ chọn Nguồn Radar (Windy vs RainViewer)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LayerPill(
                    title = "⚡ Windy (Mượt)",
                    isSelected = selectedEngine == "windy",
                    isDark = isDark,
                    onClick = {
                        hasWebGLError = false
                        selectedEngine = "windy"
                    },
                    modifier = Modifier.weight(1f)
                )
                LayerPill(
                    title = "📡 RainViewer (Ổn định)",
                    isSelected = selectedEngine == "rainviewer",
                    isDark = isDark,
                    onClick = {
                        selectedEngine = "rainviewer"
                        if (selectedLayer == "wind") selectedLayer = "radar"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Thông báo cảnh báo WebGL thông minh (nếu máy có GPU không hỗ trợ WebGL đầy đủ)
            AnimatedVisibility(
                visible = hasWebGLError,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF59E0B).copy(alpha = if (isDark) 0.18f else 0.12f),
                    border = BorderStroke(0.5.dp, Color(0xFFF59E0B).copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WebGL chưa sẵn sàng trên máy này. Đã tự động chuyển sang bản đồ RainViewer ổn định.",
                            color = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E),
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Thử lại",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                hasWebGLError = false
                                selectedEngine = "windy"
                                reloadKey++
                            }
                        )
                    }
                }
            }

            // Hàng 2: Bộ chọn Lớp (Mưa / Mây / Gió)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LayerPill(
                    title = "🌧️ Radar Mưa",
                    isSelected = selectedLayer == "radar",
                    isDark = isDark,
                    onClick = { selectedLayer = "radar" },
                    modifier = Modifier.weight(1f)
                )
                LayerPill(
                    title = "☁️ Vệ tinh Mây",
                    isSelected = selectedLayer == "satellite",
                    isDark = isDark,
                    onClick = { selectedLayer = "satellite" },
                    modifier = Modifier.weight(1f)
                )
                if (selectedEngine == "windy") {
                    LayerPill(
                        title = "💨 Gió động",
                        isSelected = selectedLayer == "wind",
                        isDark = isDark,
                        onClick = { selectedLayer = "wind" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bản đồ nhúng xem nhanh (Có lớp che cảm ứng chống giật lag cuộn trang)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF020617))
            ) {
                if (weather.isOffline) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Kết nối mạng để tải dữ liệu Radar trực tiếp",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    val previewUrl = buildMapUrl(selectedEngine, selectedLayer, isFullscreen = false)
                    key(selectedEngine, selectedLayer, lat, lon, reloadKey) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    configureRadarWebView(
                                        webView = this,
                                        url = previewUrl,
                                        lat = lat,
                                        lon = lon,
                                        locationLabel = shortPinLabel,
                                        onFallbackNeeded = onRadarFailure,
                                        onLoadingChanged = { isPreviewLoading = it }
                                    )
                                }
                            },
                            update = { webView ->
                                if (webView.tag != previewUrl) {
                                    webView.tag = previewUrl
                                    webView.loadUrl(previewUrl)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Ghim định vị chính xác tại tâm bản đồ Preview (giúp nhận diện rõ vị trí xã/phường đang chọn)
                    if (!isPreviewLoading) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(y = (-12).dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0F172A).copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.85f)),
                                shadowElevation = 4.dp
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = shortPinLabel,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF38BDF8))
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }
                    }

                    // Lớp phủ chờ tải màu tối (Dark loading placeholder chống nhấp nháy)
                    if (isPreviewLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF020617)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = accentColor,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Đang tải dữ liệu Radar tại $shortPinLabel...",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }

                    // TẤM LẮN CẢM ỨNG (Touch Shield Overlay):
                    // Chống bắt sự kiện cuộn dọc, giúp cuộn danh sách mượt mà 120Hz không hề bị đơ/khựng.
                    // Chạm vào bất kỳ vị trí nào sẽ lập tức mở toàn màn hình tương tác!
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showFullscreenDialog = true }
                    )

                    // Nút bấm gợi ý mở toàn màn hình ở góc dưới
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.72f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clickable { showFullscreenDialog = true }
                    ) {
                        Text(
                            text = "🔍 Chạm để tương tác toàn màn hình ↗",
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dải chú thích màu lượng mưa (Color Legend)
            RadarLegendBar(isDark = isDark)
        }
    }

    // Hộp thoại phóng to toàn màn hình Radar tương tác hoàn toàn (Interactive Fullscreen Map)
    if (showFullscreenDialog) {
        Dialog(
            onDismissRequest = { showFullscreenDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF0F172A)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Bar trong chế độ Fullscreen: ưu tiên hiển thị đầy đủ tên Xã/Phường/Tỉnh và nút Định tâm vị trí
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B))
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { showFullscreenDialog = false },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Đóng",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Radar • $locationFullName",
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "📍 Vĩ độ: ${String.format(Locale.US, "%.3f", lat)} • Kinh độ: ${String.format(Locale.US, "%.3f", lon)}",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 10.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Nút định tâm lại đúng vị trí xã/phường đang chọn
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF38BDF8).copy(alpha = 0.18f),
                            border = BorderStroke(0.8.dp, Color(0xFF38BDF8).copy(alpha = 0.45f)),
                            modifier = Modifier.clickable { reloadKey++ }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Về vị trí đã chọn",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = shortPinLabel,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Thanh công cụ chọn Lớp (Mưa / Mây / Gió) và Nguồn (Windy / RainViewer) trên cùng một thanh gọn gàng
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (selectedLayer == "radar") Color(0xFF38BDF8) else Color.White.copy(alpha = 0.10f),
                                modifier = Modifier.clickable { selectedLayer = "radar" }
                            ) {
                                Text(
                                    text = "🌧️ Mưa",
                                    color = if (selectedLayer == "radar") Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (selectedLayer == "satellite") Color(0xFF38BDF8) else Color.White.copy(alpha = 0.10f),
                                modifier = Modifier.clickable { selectedLayer = "satellite" }
                            ) {
                                Text(
                                    text = "☁️ Mây",
                                    color = if (selectedLayer == "satellite") Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            if (selectedEngine == "windy") {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (selectedLayer == "wind") Color(0xFF38BDF8) else Color.White.copy(alpha = 0.10f),
                                    modifier = Modifier.clickable { selectedLayer = "wind" }
                                ) {
                                    Text(
                                        text = "💨 Gió",
                                        color = if (selectedLayer == "wind") Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (selectedEngine == "windy") Color(0xFF38BDF8) else Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.clickable {
                                    hasWebGLError = false
                                    selectedEngine = "windy"
                                }
                            ) {
                                Text(
                                    text = "Windy",
                                    color = if (selectedEngine == "windy") Color.Black else Color.White,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (selectedEngine == "rainviewer") Color(0xFF38BDF8) else Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.clickable {
                                    selectedEngine = "rainviewer"
                                    if (selectedLayer == "wind") selectedLayer = "radar"
                                }
                            ) {
                                Text(
                                    text = "RainViewer",
                                    color = if (selectedEngine == "rainviewer") Color.Black else Color.White,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Bản đồ tương tác toàn màn hình (Cho phép xoay, kéo, zoom, xem loop)
                    Box(modifier = Modifier.weight(1f)) {
                        val fullUrl = buildMapUrl(selectedEngine, selectedLayer, isFullscreen = true)
                        key(selectedEngine, selectedLayer, lat, lon, reloadKey) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        configureRadarWebView(
                                            webView = this,
                                            url = fullUrl,
                                            lat = lat,
                                            lon = lon,
                                            locationLabel = shortPinLabel,
                                            onFallbackNeeded = onRadarFailure,
                                            onLoadingChanged = { isFullscreenLoading = it }
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Lớp phủ chờ tải toàn màn hình chống chớp trắng
                        if (isFullscreenLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color(0xFF38BDF8),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Đang tải bản đồ Radar tại $locationFullName...",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // Chú thích ở chân trang Fullscreen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B))
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        RadarLegendBar(isDark = true)
                    }
                }
            }
        }
    }
}

/**
 * Cấu hình WebView chuyên biệt cho Radar WebGL và bản đồ khí tượng:
 * - Kích hoạt tăng tốc phần cứng GPU để đạt 60-120fps.
 * - Ghi đè Geolocation & loại bỏ chấm xanh IP Geolocation sai lệch của Windy (tránh việc chọn xã Hoằng Thanh nhưng chấm xanh lại nhảy về TP. Thanh Hóa).
 * - Gắn ghim & nhãn tên vị trí chính xác lên bản đồ Leaflet tại đúng tọa độ (lat, lon).
 * - Tự động phát hiện lỗi WebGL/GPU để chuyển đổi dự phòng mượt mà sang RainViewer.
 */
private fun configureRadarWebView(
    webView: WebView,
    url: String,
    lat: Double,
    lon: Double,
    locationLabel: String,
    onFallbackNeeded: (String) -> Unit,
    onLoadingChanged: ((Boolean) -> Unit)? = null
) {
    val safeLabel = locationLabel
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\"", "&quot;")
        .replace("\n", " ")

    val locationInjectionScript = """
        (function() {
            try {
                var targetLat = $lat;
                var targetLon = $lon;
                var targetLabel = '$safeLabel';

                // 1. Ghi đè navigator.geolocation để trang nhúng luôn nhận đúng tọa độ địa điểm đang chọn
                if (navigator.geolocation) {
                    var mockPos = {
                        coords: {
                            latitude: targetLat,
                            longitude: targetLon,
                            accuracy: 15,
                            altitude: null,
                            altitudeAccuracy: null,
                            heading: null,
                            speed: null
                        },
                        timestamp: Date.now()
                    };
                    navigator.geolocation.getCurrentPosition = function(success) {
                        if (typeof success === 'function') success(mockPos);
                    };
                    navigator.geolocation.watchPosition = function(success) {
                        if (typeof success === 'function') success(mockPos);
                        return 1;
                    };
                }

                // 2. Ẩn chấm xanh IP Geolocation mặc định của Windy (vốn trỏ về trạm IP trung tâm tỉnh/thành phố)
                if (!document.getElementById('w24h-radar-style')) {
                    var style = document.createElement('style');
                    style.id = 'w24h-radar-style';
                    style.innerHTML = '.my-location, .leaflet-marker-icon.my-location { display: none !important; } ' +
                        '.w24h-pin-wrap { display: flex; flex-direction: column; align-items: center; transform: translate(-50%, -100%); pointer-events: none; } ' +
                        '.w24h-pin-badge { background: rgba(15, 23, 42, 0.92); color: #ffffff; border: 1.5px solid #38bdf8; border-radius: 8px; padding: 2px 7px; font-size: 11px; font-weight: 700; white-space: nowrap; box-shadow: 0 2px 6px rgba(0,0,0,0.45); font-family: sans-serif; } ' +
                        '.w24h-pin-dot { width: 11px; height: 11px; background: #38bdf8; border: 2px solid #ffffff; border-radius: 50%; margin-top: 2px; box-shadow: 0 0 8px #38bdf8; }';
                    document.head.appendChild(style);
                }

                // 3. Gắn ghim nhãn vị trí chính xác lên bản đồ Leaflet của Windy / RainViewer
                var attempts = 0;
                var attachPin = function() {
                    attempts++;
                    var L = window.L;
                    var mapObj = (window.W && window.W.map) ? window.W.map : (window.map || null);
                    if (L && mapObj && typeof mapObj.addLayer === 'function') {
                        if (window.__w24hMarker) {
                            try { mapObj.removeLayer(window.__w24hMarker); } catch (e) {}
                        }
                        var icon = L.divIcon({
                            className: 'w24h-custom-div-icon',
                            html: '<div class="w24h-pin-wrap"><div class="w24h-pin-badge">📍 ' + targetLabel + '</div><div class="w24h-pin-dot"></div></div>',
                            iconSize: [0, 0],
                            iconAnchor: [0, 0]
                        });
                        window.__w24hMarker = L.marker([targetLat, targetLon], { icon: icon, interactive: false }).addTo(mapObj);
                        return;
                    }
                    if (attempts < 20) {
                        setTimeout(attachPin, 450);
                    }
                };
                setTimeout(attachPin, 500);
            } catch (e) {}
        })();
    """.trimIndent()

    webView.apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        // Loại bỏ triệt để hiện tượng nháy trắng khi WebView khởi tạo hoặc tải trang
        setBackgroundColor(android.graphics.Color.parseColor("#020617"))
        // Kích hoạt phần cứng GPU
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        overScrollMode = View.OVER_SCROLL_NEVER

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            mediaPlaybackRequiresUserGesture = false
            setSupportMultipleWindows(false)
            javaScriptCanOpenWindowsAutomatically = false
            cacheMode = WebSettings.LOAD_DEFAULT
            useWideViewPort = true
            loadWithOverviewMode = true
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }

        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onLoadingChanged?.invoke(true)
                view?.evaluateJavascript(locationInjectionScript, null)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.evaluateJavascript(locationInjectionScript, null)
                onLoadingChanged?.invoke(false)
            }

            override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?
            ): Boolean {
                onFallbackNeeded("Tiến trình kết xuất đồ họa WebView gặp sự cố.")
                return true // Ngăn ngừa sập ứng dụng chính
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
            }
        }

        webChromeClient = object : WebChromeClient() {
            // Chặn đứng hoàn toàn hộp thoại window.alert() của Windy khi gặp sự cố WebGL
            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                result?.confirm() // Giải phóng ngay lập tức luồng JS để không bị đơ
                val msg = message.orEmpty()
                if (msg.contains("radar failed", ignoreCase = true) ||
                    msg.contains("webgl", ignoreCase = true) ||
                    msg.contains("hardware", ignoreCase = true) ||
                    msg.contains("graphics", ignoreCase = true)) {
                    onFallbackNeeded("Radar Windy không hỗ trợ WebGL trên thiết bị này.")
                }
                return true // Chặn không hiển thị Android modal alert dialog!
            }

            override fun onJsConfirm(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                result?.confirm()
                return true
            }

            override fun onJsPrompt(
                view: WebView?,
                url: String?,
                message: String?,
                defaultValue: String?,
                result: JsPromptResult?
            ): Boolean {
                result?.confirm()
                return true
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                val msg = consoleMessage?.message().orEmpty()
                if (msg.contains("webgl", ignoreCase = true) &&
                    (msg.contains("failed", ignoreCase = true) ||
                     msg.contains("context lost", ignoreCase = true) ||
                     msg.contains("not supported", ignoreCase = true) ||
                     msg.contains("error", ignoreCase = true))) {
                    onFallbackNeeded("WebGL error: $msg")
                }
                return true
            }
        }

        loadUrl(url)
    }
}

@Composable
private fun LayerPill(
    title: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) {
            activeColor.copy(alpha = if (isDark) 0.22f else 0.15f)
        } else {
            if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFF1F5F9)
        },
        border = BorderStroke(
            if (isSelected) 1.dp else 0.5.dp,
            if (isSelected) activeColor.copy(alpha = 0.45f) else (if (isDark) Color.White.copy(alpha = 0.08f) else Color(0x150F172A))
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Text(
            text = title,
            color = if (isSelected) activeColor else (if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF334155)),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun RadarLegendBar(isDark: Boolean = true) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Lượng mưa:",
                color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF334155),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LegendPill(color = Color(0xFF22C55E), label = "Mưa nhỏ", isDark = isDark)
                LegendPill(color = Color(0xFFEAB308), label = "Vừa", isDark = isDark)
                LegendPill(color = Color(0xFFF97316), label = "To", isDark = isDark)
                LegendPill(color = Color(0xFFA855F7), label = "Dông sét", isDark = isDark)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF22C55E),
                            Color(0xFFEAB308),
                            Color(0xFFF97316),
                            Color(0xFFA855F7)
                        )
                    )
                )
        )
    }
}

@Composable
private fun LegendPill(color: Color, label: String, isDark: Boolean = true) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF334155),
            fontSize = 9.5.sp,
            maxLines = 1
        )
    }
}
