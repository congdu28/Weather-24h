package com.weather24h.app.ui.components

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Refresh
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
import com.weather24h.app.ui.theme.LocalCardOpacity
import com.weather24h.app.ui.theme.LocalContentTextColor
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
    // Nguồn Radar: "windy" (Mượt nhất dùng WebGL GPU) hoặc "rainviewer" (Trạm mặt đất)
    var selectedEngine by remember { mutableStateOf("windy") }
    // Lớp hiển thị: "radar" (Mưa), "satellite" (Mây), "wind" (Gió - hỗ trợ trên Windy)
    var selectedLayer by remember { mutableStateOf("radar") }

    val isDark = LocalIsDarkTheme.current
    val cardOpacity = LocalCardOpacity.current
    val subtextColor = LocalSubtextColor.current

    val lat = weather.location.latitude
    val lon = weather.location.longitude
    val accentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

    // Hàm tạo URL bản đồ động theo Engine và Layer
    fun buildMapUrl(engine: String, layer: String, isFullscreen: Boolean): String {
        return if (engine == "windy") {
            val zoom = if (isFullscreen) 8 else 7
            val windyOverlay = when (layer) {
                "satellite" -> "satellite"
                "wind" -> "wind"
                else -> "radar"
            }
            val product = if (layer == "wind") "ecmwf" else windyOverlay
            "https://embed.windy.com/embed.html?type=map&location=coordinates&metricRain=mm&metricTemp=%C2%B0C&metricWind=km%2Fh&zoom=$zoom&overlay=$windyOverlay&product=$product&level=surface&lat=$lat&lon=$lon"
        } else {
            val zoom = if (isFullscreen) 9 else 8
            val rvLayer = if (layer == "satellite") "satellite" else "radar"
            "https://www.rainviewer.com/map.html?loc=$lat,$lon,$zoom&oFa=0&oC=1&oU=0&oCS=1&oF=0&oAP=1&c=3&o=83&lm=1&layer=$rvLayer&sm=1&sn=1"
        }
    }

    // Khối thẻ xem nhanh Radar trên màn hình chính
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.65f * cardOpacity) else Color.White.copy(alpha = 0.85f * cardOpacity),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f * cardOpacity) else Color(0xFF38BDF8).copy(alpha = 0.35f * cardOpacity)),
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
                            text = "BẢN ĐỒ RADAR KHÍ TƯỢNG",
                            color = accentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (selectedEngine == "windy") "Windy WebGL • Siêu mượt & Sắc nét" else "RainViewer • Trạm Radar thực tế",
                            color = subtextColor,
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Nút Phóng to toàn màn hình
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.18f) else Color(0xFFE0F2FE),
                    border = BorderStroke(
                        1.dp,
                        if (isDark) Color(0xFF38BDF8).copy(alpha = 0.45f) else Color(0xFF0284C7).copy(alpha = 0.4f)
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
                            maxLines = 1
                        )
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
                    title = "⚡ Windy (Mượt nhất)",
                    isSelected = selectedEngine == "windy",
                    isDark = isDark,
                    onClick = { selectedEngine = "windy" },
                    modifier = Modifier.weight(1f)
                )
                LayerPill(
                    title = "📡 RainViewer",
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
                    key(selectedEngine, selectedLayer, lat, lon) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    // Bật tăng tốc phần cứng GPU để không tụt FPS
                                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                                    isVerticalScrollBarEnabled = false
                                    isHorizontalScrollBarEnabled = false
                                    overScrollMode = View.OVER_SCROLL_NEVER

                                    settings.apply {
                                        javaScriptEnabled = true
                                        domStorageEnabled = true
                                        cacheMode = WebSettings.LOAD_DEFAULT
                                        useWideViewPort = true
                                        loadWithOverviewMode = true
                                    }
                                    webViewClient = WebViewClient()
                                    webChromeClient = WebChromeClient()
                                    loadUrl(previewUrl)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
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
                    // Top Bar trong chế độ Fullscreen
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
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = "Radar • ${weather.location.name}",
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Vĩ độ: ${String.format(Locale.US, "%.2f", lat)} • Kinh độ: ${String.format(Locale.US, "%.2f", lon)}",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Bộ chọn Nguồn & Lớp thu nhỏ trong thanh công cụ Fullscreen
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                shape = RoundedCornerShape(7.dp),
                                color = if (selectedEngine == "windy") Color(0xFF38BDF8) else Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.clickable { selectedEngine = "windy" }
                            ) {
                                Text(
                                    text = "Windy",
                                    color = if (selectedEngine == "windy") Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(7.dp),
                                color = if (selectedEngine == "rainviewer") Color(0xFF38BDF8) else Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.clickable {
                                    selectedEngine = "rainviewer"
                                    if (selectedLayer == "wind") selectedLayer = "radar"
                                }
                            ) {
                                Text(
                                    text = "RainViewer",
                                    color = if (selectedEngine == "rainviewer") Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Thanh công cụ chọn lớp khi ở toàn màn hình
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedLayer == "radar") Color(0xFF38BDF8) else Color.White.copy(alpha = 0.10f),
                            modifier = Modifier.clickable { selectedLayer = "radar" }
                        ) {
                            Text(
                                text = "🌧️ Radar Mưa",
                                color = if (selectedLayer == "radar") Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedLayer == "satellite") Color(0xFF38BDF8) else Color.White.copy(alpha = 0.10f),
                            modifier = Modifier.clickable { selectedLayer = "satellite" }
                        ) {
                            Text(
                                text = "☁️ Vệ tinh Mây",
                                color = if (selectedLayer == "satellite") Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        if (selectedEngine == "windy") {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (selectedLayer == "wind") Color(0xFF38BDF8) else Color.White.copy(alpha = 0.10f),
                                modifier = Modifier.clickable { selectedLayer = "wind" }
                            ) {
                                Text(
                                    text = "💨 Gió động",
                                    color = if (selectedLayer == "wind") Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Bản đồ tương tác toàn màn hình (Cho phép xoay, kéo, zoom, xem loop)
                    Box(modifier = Modifier.weight(1f)) {
                        val fullUrl = buildMapUrl(selectedEngine, selectedLayer, isFullscreen = true)
                        key(selectedEngine, selectedLayer, lat, lon) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            cacheMode = WebSettings.LOAD_DEFAULT
                                            useWideViewPort = true
                                            loadWithOverviewMode = true
                                        }
                                        webViewClient = WebViewClient()
                                        webChromeClient = WebChromeClient()
                                        loadUrl(fullUrl)
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
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
            1.dp,
            if (isSelected) activeColor else (if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFCBD5E1))
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Text(
            text = title,
            color = if (isSelected) activeColor else (if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF475569)),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
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
                color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF64748B),
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
