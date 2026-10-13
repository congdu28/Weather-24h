package com.weather24h.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.weather24h.app.data.model.HourlyForecast
import com.weather24h.app.ui.theme.LocalIsDarkTheme

enum class ChartDisplayMode(val label: String, val iconEmoji: String) {
    COMBINED("Tổng hợp", "📊"),
    TEMPERATURE("Nhiệt độ", "🌡️"),
    PRECIPITATION("Lượng mưa", "🌧️")
}

/**
 * Biểu đồ thời tiết trực quan đa chế độ (Visual Weather Chart):
 * - Hỗ trợ vẽ đường cong nhiệt độ Bezier mượt mà với dải gradient bóng đổ
 * - Cột lượng mưa (precipitation bars) và xác suất mưa (%) theo giờ
 * - Chế độ kết hợp (Combined) đối chiếu trực tiếp giữa nhiệt độ và mưa
 * - Đánh dấu cột giờ đang được chọn (highlight indicator)
 */
@Composable
fun TemperatureChart(
    temperatures: List<Double>,
    itemWidth: Dp = 68.dp,
    modifier: Modifier = Modifier,
    hourlyList: List<HourlyForecast> = emptyList(),
    displayMode: ChartDisplayMode = ChartDisplayMode.COMBINED,
    selectedIndex: Int? = null
) {
    if (temperatures.size < 2 && hourlyList.size < 2) return

    val isDark = LocalIsDarkTheme.current
    val effectiveTemps = if (hourlyList.isNotEmpty()) hourlyList.map { it.temperature } else temperatures
    val totalWidth = itemWidth * effectiveTemps.size

    val chartHeight = when (displayMode) {
        ChartDisplayMode.COMBINED -> 94.dp
        ChartDisplayMode.TEMPERATURE -> 72.dp
        ChartDisplayMode.PRECIPITATION -> 72.dp
    }

    Canvas(
        modifier = modifier
            .width(totalWidth)
            .height(chartHeight)
    ) {
        val widthPx = size.width
        val heightPx = size.height
        val itemWidthPx = itemWidth.toPx()

        // 1. Vẽ vùng đánh dấu cột giờ đang chọn (Selection Highlight Column)
        if (selectedIndex != null && selectedIndex in effectiveTemps.indices) {
            val selCenterX = selectedIndex * itemWidthPx + (itemWidthPx / 2f)
            val highlightWidth = itemWidthPx - 8.dp.toPx()
            drawRoundRect(
                color = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.12f) else Color(0xFF0284C7).copy(alpha = 0.08f),
                topLeft = Offset(selCenterX - highlightWidth / 2f, 2.dp.toPx()),
                size = Size(highlightWidth, heightPx - 4.dp.toPx()),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
        }

        // =========================================================================
        // PHẦN A: VẼ CỘT LƯỢNG MƯA & XÁC SUẤT MƯA (Precipitation Bars)
        // =========================================================================
        if (displayMode == ChartDisplayMode.PRECIPITATION || displayMode == ChartDisplayMode.COMBINED) {
            val rainBarBaseY = heightPx - 4.dp.toPx()
            val maxBarHeight = if (displayMode == ChartDisplayMode.COMBINED) heightPx * 0.38f else heightPx * 0.70f
            val barWidth = 14.dp.toPx()

            effectiveTemps.indices.forEach { index ->
                val hourItem = hourlyList.getOrNull(index)
                val prob = hourItem?.precipitationProbability ?: 0
                val rainMm = hourItem?.precipitationMm ?: 0.0

                if (prob > 0 || rainMm > 0.0) {
                    val barCenterX = index * itemWidthPx + (itemWidthPx / 2f)
                    val normalizedHeight = when {
                        rainMm > 0.0 -> (rainMm / 10.0).coerceIn(0.18, 1.0).toFloat()
                        else -> (prob / 100f).coerceIn(0.15f, 1.0f)
                    }
                    val barHeight = maxBarHeight * normalizedHeight
                    val barTopY = rainBarBaseY - barHeight

                    // Vẽ cột mưa bo tròn đỉnh
                    val barBrush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF38BDF8).copy(alpha = if (index == selectedIndex) 0.95f else 0.80f),
                            Color(0xFF0284C7).copy(alpha = if (index == selectedIndex) 0.75f else 0.50f)
                        ),
                        startY = barTopY,
                        endY = rainBarBaseY
                    )

                    drawRoundRect(
                        brush = barBrush,
                        topLeft = Offset(barCenterX - barWidth / 2f, barTopY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Điểm sáng đỉnh cột
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = 1.8.dp.toPx(),
                        center = Offset(barCenterX, barTopY + 2.dp.toPx())
                    )
                }
            }
        }

        // =========================================================================
        // PHẦN B: VẼ ĐƯỜNG CONG NHIỆT ĐỘ BEZIER (Temperature Spline Curve)
        // =========================================================================
        if (displayMode == ChartDisplayMode.TEMPERATURE || displayMode == ChartDisplayMode.COMBINED) {
            val topPaddingY = 14.dp.toPx()
            val bottomPaddingY = if (displayMode == ChartDisplayMode.COMBINED) heightPx * 0.38f else 16.dp.toPx()
            val availableTempHeight = heightPx - topPaddingY - bottomPaddingY

            var minTemp = effectiveTemps.minOrNull() ?: 20.0
            var maxTemp = effectiveTemps.maxOrNull() ?: 30.0
            if (kotlin.math.abs(maxTemp - minTemp) < 1.0) {
                maxTemp += 1.0
                minTemp -= 1.0
            }

            val points = effectiveTemps.mapIndexed { index, temp ->
                val x = index * itemWidthPx + (itemWidthPx / 2f)
                val normalizedY = (temp - minTemp) / (maxTemp - minTemp)
                val y = heightPx - bottomPaddingY - (normalizedY * availableTempHeight).toFloat()
                Offset(x, y)
            }

            // 1. Đường cong cubic bezier mượt mà
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val cx1 = p0.x + (p1.x - p0.x) / 2f
                    val cy1 = p0.y
                    val cx2 = p0.x + (p1.x - p0.x) / 2f
                    val cy2 = p1.y
                    cubicTo(cx1, cy1, cx2, cy2, p1.x, p1.y)
                }
            }

            // 2. Vùng đổ bóng gradient dưới đường cong
            val fillPath = Path().apply {
                addPath(path)
                lineTo(points.last().x, heightPx - bottomPaddingY)
                lineTo(points.first().x, heightPx - bottomPaddingY)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isDark) Color(0xFF38BDF8).copy(alpha = 0.30f) else Color(0xFF0284C7).copy(alpha = 0.22f),
                        if (isDark) Color(0xFF38BDF8).copy(alpha = 0.02f) else Color(0xFF0284C7).copy(alpha = 0.01f)
                    ),
                    startY = topPaddingY,
                    endY = heightPx - bottomPaddingY
                )
            )

            // 3. Nét vẽ đường cong nhiệt độ chính
            drawPath(
                path = path,
                color = if (isDark) Color.White.copy(alpha = 0.95f) else Color(0xFF0284C7),
                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
            )

            // 4. Các điểm chấm nhiệt độ & điểm cao nhất/thấp nhất
            val minTempIdx = effectiveTemps.indexOf(effectiveTemps.minOrNull())
            val maxTempIdx = effectiveTemps.indexOf(effectiveTemps.maxOrNull())

            points.forEachIndexed { index, point ->
                val isSelected = index == selectedIndex
                val isPeak = index == minTempIdx || index == maxTempIdx

                // Vòng hào quang cho điểm đang chọn hoặc điểm cực trị
                if (isSelected || isPeak) {
                    drawCircle(
                        color = (if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)).copy(alpha = 0.35f),
                        radius = (if (isSelected) 7.5.dp else 5.5.dp).toPx(),
                        center = point
                    )
                }

                // Chấm tròn nhiệt độ chính
                drawCircle(
                    color = if (isDark) Color.White else Color(0xFF0284C7),
                    radius = (if (isSelected) 4.2.dp else 3.2.dp).toPx(),
                    center = point
                )

                // Lõi sáng trắng ở giữa
                drawCircle(
                    color = if (isDark) Color(0xFF0F172A) else Color.White,
                    radius = (if (isSelected) 2.2.dp else 1.5.dp).toPx(),
                    center = point
                )
            }
        }
    }
}
