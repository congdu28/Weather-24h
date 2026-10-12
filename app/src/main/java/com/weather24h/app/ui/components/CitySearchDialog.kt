package com.weather24h.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.weather24h.app.data.model.CityLocation
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
                .fillMaxHeight(0.82f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chọn địa điểm",
                        color = contentColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onDismiss) {
                        Text("Đóng", color = subtextColor)
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
                            fontSize = 14.sp
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
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = subtextColor)
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

                Spacer(modifier = Modifier.height(16.dp))

                // Use GPS Location Button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0284C7).copy(alpha = if (isDark) 0.25f else 0.12f),
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
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Sử dụng vị trí hiện tại (GPS)",
                                color = contentColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tự động nhận diện quận/huyện tiếng Việt",
                                color = if (isDark) Color(0xFFBAE6FD) else Color(0xFF0369A1),
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isSearching) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF38BDF8))
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
                    // Danh sách Thành phố yêu thích & Thành phố phổ biến
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 1. Thành phố yêu thích (Nếu có)
                        if (favoriteCities.isNotEmpty()) {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD166),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "THÀNH PHỐ YÊU THÍCH (${favoriteCities.size})",
                                        color = contentColor.copy(alpha = 0.85f),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.7.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    favoriteCities.forEach { city ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFFFFD166).copy(alpha = if (isDark) 0.15f else 0.12f),
                                            modifier = Modifier.clip(RoundedCornerShape(12.dp))
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .clickable {
                                                            onSelectCity(city)
                                                            onDismiss()
                                                        }
                                                        .padding(vertical = 6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFFD166),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(5.dp))
                                                    Text(
                                                        text = city.name,
                                                        color = contentColor,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(2.dp))
                                                IconButton(
                                                    onClick = { onToggleFavorite(city) },
                                                    modifier = Modifier.size(26.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Xóa khỏi yêu thích",
                                                        tint = subtextColor,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Thành phố phổ biến gợi ý
                        item {
                            Text(
                                text = "THÀNH PHỐ PHỔ BIẾN",
                                color = subtextColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.7.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CityLocation.POPULAR_CITIES.forEach { city ->
                                    val isFav = favoriteCities.any { it.name.equals(city.name, ignoreCase = true) }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isFav) Color(0xFFFFD166).copy(alpha = if (isDark) 0.14f else 0.12f) else chipBg,
                                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
                                        ) {
                                            Text(
                                                text = city.name,
                                                color = contentColor,
                                                fontSize = 13.sp,
                                                modifier = Modifier
                                                    .clickable {
                                                        onSelectCity(city)
                                                        onDismiss()
                                                    }
                                                    .padding(vertical = 6.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            IconButton(
                                                onClick = { onToggleFavorite(city) },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                                    contentDescription = "Yêu thích",
                                                    tint = if (isFav) Color(0xFFFFD166) else subtextColor,
                                                    modifier = Modifier.size(15.dp)
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
