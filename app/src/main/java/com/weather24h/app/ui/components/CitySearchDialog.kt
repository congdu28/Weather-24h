package com.weather24h.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.weather24h.app.data.model.CityLocation
import com.weather24h.app.data.model.CityWeatherSummary
import com.weather24h.app.ui.theme.LocalIsDarkTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CitySearchDialog(
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit,
    searchResults: List<CityLocation>,
    isSearching: Boolean,
    onSelectCity: (CityLocation) -> Unit,
    onUseGps: () -> Unit,
    favoriteCities: List<CityLocation> = emptyList(),
    currentCityName: String = "",
    favoriteSummaries: Map<String, CityWeatherSummary> = emptyMap(),
    onToggleFavorite: (CityLocation) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }

    val isDark = LocalIsDarkTheme.current
    val contentColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val dialogBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFE2E8F0)
    val chipBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F5F9)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = dialogBg,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.86f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Chọn địa điểm",
                            color = contentColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tìm kiếm hoặc chọn từ danh sách yêu thích",
                            color = subtextColor,
                            fontSize = 11.5.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = subtextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Box
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        onSearch(it)
                    },
                    placeholder = {
                        Text(
                            text = "Nhập tên quận, thành phố...",
                            color = subtextColor.copy(alpha = 0.6f),
                            fontSize = 13.5.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = subtextColor)
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = {
                                query = ""
                                onSearch("")
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Xóa", tint = subtextColor)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = contentColor,
                        unfocusedTextColor = contentColor,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f),
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Use GPS Location Button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0284C7).copy(alpha = if (isDark) 0.22f else 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            onUseGps()
                            onDismiss()
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFF0284C7).copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sử dụng vị trí hiện tại (GPS)",
                                color = contentColor,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tự động cập nhật thời tiết theo tọa độ thực",
                                color = if (isDark) Color(0xFFBAE6FD) else Color(0xFF0369A1),
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "📍 Định vị",
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isSearching) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = Color(0xFF38BDF8),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Đang tìm kiếm địa điểm...",
                                color = subtextColor,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else if (query.isNotEmpty()) {
                    // Search results
                    if (searchResults.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Không tìm thấy địa điểm phù hợp",
                                color = subtextColor,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults) { city ->
                                val isFav = favoriteCities.any { it.name.equals(city.name, ignoreCase = true) }
                                CityItemRow(
                                    city = city,
                                    isFavorite = isFav,
                                    cardBg = cardBg,
                                    contentColor = contentColor,
                                    subtextColor = subtextColor,
                                    onToggleFavorite = { onToggleFavorite(city) },
                                    onClick = {
                                        onSelectCity(city)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Danh sách Thành phố yêu thích (Dạng danh sách thẻ đẹp mắt) & Thành phố phổ biến
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 1. MỤC DANH SÁCH ĐỊA ĐIỂM YÊU THÍCH (Favorite Locations List Section)
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color(0xFFFFD166).copy(alpha = 0.22f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD166),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ĐỊA ĐIỂM YÊU THÍCH",
                                        color = contentColor,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    if (favoriteCities.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0)
                                        ) {
                                            Text(
                                                text = "${favoriteCities.size}",
                                                color = contentColor.copy(alpha = 0.85f),
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "Chạm để mở ngay",
                                    color = subtextColor.copy(alpha = 0.75f),
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        if (favoriteCities.isEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = cardBg,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.StarBorder,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD166).copy(alpha = 0.65f),
                                            modifier = Modifier.size(30.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Chưa có địa điểm yêu thích",
                                            color = contentColor,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "Chạm vào biểu tượng ngôi sao ⭐ trên thẻ thời tiết chính để lưu các thành phố bạn quan tâm vào danh sách này.",
                                            color = subtextColor,
                                            fontSize = 11.5.sp,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            items(favoriteCities) { city ->
                                val isCurrent = city.name.equals(currentCityName, ignoreCase = true)
                                val summary = favoriteSummaries[city.name.lowercase()]
                                FavoriteCityCardItem(
                                    city = city,
                                    isCurrent = isCurrent,
                                    summary = summary,
                                    isDark = isDark,
                                    contentColor = contentColor,
                                    subtextColor = subtextColor,
                                    cardBg = cardBg,
                                    onSelect = {
                                        onSelectCity(city)
                                        onDismiss()
                                    },
                                    onRemove = { onToggleFavorite(city) }
                                )
                            }
                        }

                        // 2. Thành phố phổ biến gợi ý
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "THÀNH PHỐ PHỔ BIẾN",
                                    color = subtextColor,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CityLocation.POPULAR_CITIES.forEach { city ->
                                    val isFav = favoriteCities.any { it.name.equals(city.name, ignoreCase = true) }
                                    val isCurrent = city.name.equals(currentCityName, ignoreCase = true)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = when {
                                            isCurrent -> Color(0xFF0284C7).copy(alpha = if (isDark) 0.22f else 0.15f)
                                            isFav -> Color(0xFFFFD166).copy(alpha = if (isDark) 0.15f else 0.12f)
                                            else -> chipBg
                                        },
                                        border = BorderStroke(
                                            1.dp,
                                            when {
                                                isCurrent -> Color(0xFF38BDF8).copy(alpha = 0.5f)
                                                isFav -> Color(0xFFFFD166).copy(alpha = 0.4f)
                                                else -> Color.Transparent
                                            }
                                        ),
                                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
                                        ) {
                                            Text(
                                                text = city.name,
                                                color = contentColor,
                                                fontSize = 12.5.sp,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                modifier = Modifier
                                                    .clickable {
                                                        onSelectCity(city)
                                                        onDismiss()
                                                    }
                                                    .padding(vertical = 5.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            IconButton(
                                                onClick = { onToggleFavorite(city) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                                    contentDescription = "Yêu thích",
                                                    tint = if (isFav) Color(0xFFFFD166) else subtextColor.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(14.dp)
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
    }
}

/**
 * Thẻ danh sách Thành phố yêu thích được thiết kế sang trọng:
 * - Hiển thị biểu tượng thời tiết / ngôi sao vàng óng ánh.
 * - Hiển thị tên thành phố, vị trí và nhiệt độ tóm tắt trực tiếp.
 * - Đánh dấu badge "Đang xem" nếu là thành phố đang mở trên màn hình chính.
 * - Nút xóa nhanh khỏi danh sách với phản hồi mượt mà.
 */
@Composable
private fun FavoriteCityCardItem(
    city: CityLocation,
    isCurrent: Boolean,
    summary: CityWeatherSummary?,
    isDark: Boolean,
    contentColor: Color,
    subtextColor: Color,
    cardBg: Color,
    onSelect: () -> Unit,
    onRemove: () -> Unit
) {
    val activeBorder = Color(0xFF38BDF8)
    val normalBorder = if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
    val surfaceBg = if (isCurrent) {
        Color(0xFF0284C7).copy(alpha = if (isDark) 0.18f else 0.10f)
    } else {
        cardBg
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = surfaceBg,
        border = BorderStroke(if (isCurrent) 1.5.dp else 1.dp, if (isCurrent) activeBorder else normalBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Phần thân bấm chọn thành phố
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onSelect)
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Biểu tượng thời tiết hoặc Ngôi sao vàng
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (isCurrent) Color(0xFF38BDF8).copy(alpha = 0.22f)
                            else Color(0xFFFFD166).copy(alpha = if (isDark) 0.16f else 0.14f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (summary != null) {
                        Text(
                            text = summary.weatherType.icon,
                            fontSize = 19.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD166),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(11.dp))

                // Cột thông tin thành phố
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = city.name,
                            color = contentColor,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isCurrent) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF38BDF8).copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = "Đang xem",
                                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    val subtitle = if (summary != null && summary.conditionText.isNotBlank()) {
                        "${summary.conditionText} • ${city.displayName}"
                    } else {
                        city.displayName.ifEmpty { "Việt Nam" }
                    }
                    Text(
                        text = subtitle,
                        color = subtextColor,
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Nhiệt độ hiện tại nếu đã lưu tóm tắt
                if (summary != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${summary.tempC}°",
                        color = contentColor,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Nút xóa nhanh khỏi danh sách yêu thích
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Xóa khỏi yêu thích",
                    tint = subtextColor.copy(alpha = 0.75f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun CityItemRow(
    city: CityLocation,
    isFavorite: Boolean = false,
    cardBg: Color,
    contentColor: Color,
    subtextColor: Color,
    onToggleFavorite: () -> Unit = {},
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
                    .padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = city.name,
                        color = contentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (city.displayName.isNotEmpty()) {
                        Text(
                            text = city.displayName,
                            color = subtextColor,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (isFavorite) "Bỏ yêu thích" else "Thêm yêu thích",
                    tint = if (isFavorite) Color(0xFFFFD166) else subtextColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
