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
    PRECIPITATION("Lượng mưa", "🌧️"),
    HUMIDITY("Độ ẩm", "💧")
}

/**
 * Biểu đồ thời tiết trực quan đa chế độ (Visual Weather Chart):
 * - Hỗ trợ vẽ đường cong nhiệt độ Bezier mượt mà với dải gradient ĐỎ CAM rực rỡ (#FF6B00 / #FF7A00)
 * - Cột lượng mưa (precipitation bars) và xác suất mưa (%) màu XANH DƯƠNG khí tượng (#0284C7 / #38BDF8)
 * - Đường cong độ ẩm không khí màu XANH NGỌC TEAL (#0D9488 / #14B8A6)
 * - Chế độ kết hợp (Combined) đối chiếu trực tiếp giữa nhiệt độ và mưa, hoàn toàn không gây nhầm lẫn
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
        ChartDisplayMode.HUMIDITY -> 72.dp
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
                color = if (isDark) Color(0xFFFF6B00).copy(alpha = 0.12f) else Color(0xFFFF6B00).copy(alpha = 0.08f),
                topLeft = Offset(selCenterX - highlightWidth / 2f, 2.dp.toPx()),
                size = Size(highlightWidth, heightPx - 4.dp.toPx()),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
        }

        // =========================================================================
        // PHẦN A: VẼ CỘT LƯỢNG MƯA & XÁC SUẤT MƯA (Precipitation Bars - Xanh Biển)
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

                    // Vẽ cột mưa bo tròn đỉnh với màu xanh dương khí tượng
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

                    // Điểm sáng đỉnh cột mưa
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = 1.8.dp.toPx(),
                        center = Offset(barCenterX, barTopY + 2.dp.toPx())
                    )
                }
            }
        }

        // =========================================================================
        // PHẦN B: VẼ ĐƯỜNG CONG NHIỆT ĐỘ BEZIER (MÀU ĐỎ CAM RỰC RỠ)
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

            // 2. Vùng đổ bóng gradient đỏ cam ấm áp dưới đường cong nhiệt độ
            val fillPath = Path().apply {
                addPath(path)
                lineTo(points.last().x, heightPx - bottomPaddingY)
                lineTo(points.first().x, heightPx - bottomPaddingY)
                close()
            }

            val tempColorPrimary = if (isDark) Color(0xFFFF7A00) else Color(0xFFFF6B00)
            val tempColorSecondary = if (isDark) Color(0xFFF97316) else Color(0xFFEA580C)

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        tempColorPrimary.copy(alpha = if (isDark) 0.32f else 0.25f),
                        tempColorPrimary.copy(alpha = 0.01f)
                    ),
                    startY = topPaddingY,
                    endY = heightPx - bottomPaddingY
                )
            )

            // 3. Nét vẽ đường cong nhiệt độ chính (MÀU ĐỎ CAM)
            drawPath(
                path = path,
                color = tempColorPrimary,
                style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
            )

            // 4. Các điểm chấm nhiệt độ & điểm cao nhất/thấp nhất
            val minTempIdx = effectiveTemps.indexOf(effectiveTemps.minOrNull())
            val maxTempIdx = effectiveTemps.indexOf(effectiveTemps.maxOrNull())

            points.forEachIndexed { index, point ->
                val isSelected = index == selectedIndex
                val isPeak = index == minTempIdx || index == maxTempIdx

                // Vòng hào quang đỏ cam cho điểm đang chọn hoặc điểm cực trị
                if (isSelected || isPeak) {
                    drawCircle(
                        color = tempColorPrimary.copy(alpha = 0.38f),
                        radius = (if (isSelected) 8.dp else 5.8.dp).toPx(),
                        center = point
                    )
                }

                // Chấm tròn nhiệt độ chính
                drawCircle(
                    color = tempColorPrimary,
                    radius = (if (isSelected) 4.4.dp else 3.4.dp).toPx(),
                    center = point
                )

                // Lõi sáng trắng ở giữa giúp tương phản hoàn hảo
                drawCircle(
                    color = Color.White,
                    radius = (if (isSelected) 2.2.dp else 1.6.dp).toPx(),
                    center = point
                )
            }
        }

        // =========================================================================
        // PHẦN C: VẼ ĐƯỜNG CONG ĐỘ ẨM KHÔNG KHÍ (MÀU XANH NGỌC TEAL #0D9488)
        // =========================================================================
        if (displayMode == ChartDisplayMode.HUMIDITY) {
            val topPaddingY = 16.dp.toPx()
            val bottomPaddingY = 16.dp.toPx()
            val availableHumHeight = heightPx - topPaddingY - bottomPaddingY

            val humValues = effectiveTemps.indices.map { idx ->
                hourlyList.getOrNull(idx)?.humidity?.toDouble() ?: 60.0
            }

            var minHum = humValues.minOrNull() ?: 40.0
            var maxHum = humValues.maxOrNull() ?: 90.0
            if (kotlin.math.abs(maxHum - minHum) < 5.0) {
                maxHum = (maxHum + 5.0).coerceAtMost(100.0)
                minHum = (minHum - 5.0).coerceAtLeast(0.0)
            }

            val humPoints = humValues.mapIndexed { index, hum ->
                val x = index * itemWidthPx + (itemWidthPx / 2f)
                val normalizedY = (hum - minHum) / (maxHum - minHum)
                val y = heightPx - bottomPaddingY - (normalizedY * availableHumHeight).toFloat()
                Offset(x, y)
            }

            val humPath = Path().apply {
                moveTo(humPoints.first().x, humPoints.first().y)
                for (i in 0 until humPoints.size - 1) {
                    val p0 = humPoints[i]
                    val p1 = humPoints[i + 1]
                    val cx1 = p0.x + (p1.x - p0.x) / 2f
                    val cy1 = p0.y
                    val cx2 = p0.x + (p1.x - p0.x) / 2f
                    val cy2 = p1.y
                    cubicTo(cx1, cy1, cx2, cy2, p1.x, p1.y)
                }
            }

            val humFillPath = Path().apply {
                addPath(humPath)
                lineTo(humPoints.last().x, heightPx - bottomPaddingY)
                lineTo(humPoints.first().x, heightPx - bottomPaddingY)
                close()
            }

            val humColorPrimary = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488)

            drawPath(
                path = humFillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        humColorPrimary.copy(alpha = if (isDark) 0.30f else 0.22f),
                        humColorPrimary.copy(alpha = 0.01f)
                    ),
                    startY = topPaddingY,
                    endY = heightPx - bottomPaddingY
                )
            )

            drawPath(
                path = humPath,
                color = humColorPrimary,
                style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
            )

            humPoints.forEachIndexed { index, point ->
                val isSelected = index == selectedIndex
                if (isSelected) {
                    drawCircle(
                        color = humColorPrimary.copy(alpha = 0.38f),
                        radius = 8.dp.toPx(),
                        center = point
                    )
                }
                drawCircle(
                    color = humColorPrimary,
                    radius = (if (isSelected) 4.4.dp else 3.2.dp).toPx(),
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = (if (isSelected) 2.2.dp else 1.6.dp).toPx(),
                    center = point
                )
            }
        }
    }
}
