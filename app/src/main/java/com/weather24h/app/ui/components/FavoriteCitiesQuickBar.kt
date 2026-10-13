package com.weather24h.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather24h.app.data.model.CityLocation
import com.weather24h.app.data.model.CityWeatherSummary
import com.weather24h.app.data.model.icon
import com.weather24h.app.ui.theme.LocalCardOpacity
import com.weather24h.app.ui.theme.LocalIsDarkTheme

/**
 * Thanh chuyển đổi nhanh địa điểm yêu thích trên màn hình chính:
 * - Cho phép chuyển đổi 1 chạm giữa vị trí GPS và các thành phố yêu thích.
 * - Hiển thị nhiệt độ tức thời và trạng thái thời tiết ngắn gọn.
 * - Có nút (+) mở nhanh hộp thoại tìm kiếm và quản lý danh sách.
 */
@Composable
fun FavoriteCitiesQuickBar(
    currentCityName: String,
    isCurrentGps: Boolean,
    favoriteCities: List<CityLocation>,
    favoriteSummaries: Map<String, CityWeatherSummary> = emptyMap(),
    onSelectCity: (CityLocation) -> Unit,
    onSelectGps: () -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val cardOpacity = LocalCardOpacity.current

    val activeColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val inactiveBg = if (isDark) Color(0xFF0F172A).copy(alpha = 0.55f * cardOpacity) else Color.White.copy(alpha = 0.70f * cardOpacity)
    val inactiveBorder = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Chip 1: Vị trí GPS Hiện tại
        item {
            val isGpsActive = isCurrentGps
            val gpsBg by animateColorAsState(
                targetValue = if (isGpsActive) activeColor.copy(alpha = if (isDark) 0.28f else 0.20f) else inactiveBg,
                animationSpec = tween(200),
                label = "GpsBg"
            )
            val gpsBorder by animateColorAsState(
                targetValue = if (isGpsActive) activeColor else inactiveBorder,
                animationSpec = tween(200),
                label = "GpsBorder"
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = gpsBg,
                border = BorderStroke(if (isGpsActive) 1.5.dp else 1.dp, gpsBorder),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onSelectGps() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "GPS",
                        tint = if (isGpsActive) activeColor else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Vị trí GPS",
                        color = if (isGpsActive) activeColor else textPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isGpsActive) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // Các Chip Thành phố yêu thích
        items(favoriteCities) { city ->
            val isActive = !isCurrentGps && city.name.equals(currentCityName, ignoreCase = true)
            val summary = favoriteSummaries[city.name.lowercase()]

            val chipBg by animateColorAsState(
                targetValue = if (isActive) activeColor.copy(alpha = if (isDark) 0.28f else 0.20f) else inactiveBg,
                animationSpec = tween(200),
                label = "FavBg"
            )
            val chipBorder by animateColorAsState(
                targetValue = if (isActive) activeColor else inactiveBorder,
                animationSpec = tween(200),
                label = "FavBorder"
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = chipBg,
                border = BorderStroke(if (isActive) 1.5.dp else 1.dp, chipBorder),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onSelectCity(city) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (summary != null) {
                        Text(
                            text = summary.weatherType.icon,
                            fontSize = 13.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD166),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = city.name,
                        color = if (isActive) activeColor else textPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
                    )
                    if (summary != null) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${summary.tempC}°",
                            color = if (isActive) activeColor else (if (isDark) Color(0xFFBAE6FD) else Color(0xFF0369A1)),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        // Chip Thêm / Quản lý (+)
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = inactiveBg,
                border = BorderStroke(1.dp, inactiveBorder),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onOpenSearch() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Thêm thành phố",
                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Thêm",
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
