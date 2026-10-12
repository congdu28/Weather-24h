package com.weather24h.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TemperatureChart(
    temperatures: List<Double>,
    itemWidth: Dp = 68.dp,
    modifier: Modifier = Modifier
) {
    if (temperatures.size < 2) return

    val totalWidth = itemWidth * temperatures.size

    Canvas(
        modifier = modifier
            .width(totalWidth)
            .height(70.dp)
    ) {
        val widthPx = size.width
        val heightPx = size.height
        val paddingY = 16.dp.toPx()
        val availableHeight = heightPx - (paddingY * 2)

        var minTemp = temperatures.minOrNull() ?: 20.0
        var maxTemp = temperatures.maxOrNull() ?: 30.0
        if (kotlin.math.abs(maxTemp - minTemp) < 1.0) {
            maxTemp += 1.0
            minTemp -= 1.0
        }

        val itemWidthPx = itemWidth.toPx()
        val points = temperatures.mapIndexed { index, temp ->
            val x = index * itemWidthPx + (itemWidthPx / 2f)
            val normalizedY = (temp - minTemp) / (maxTemp - minTemp)
            val y = heightPx - paddingY - (normalizedY * availableHeight).toFloat()
            Offset(x, y)
        }

        // 1. Path smooth cubic
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

        // 2. Fill gradient under curve
        val fillPath = Path().apply {
            addPath(path)
            lineTo(points.last().x, heightPx)
            lineTo(points.first().x, heightPx)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.02f)
                )
            )
        )

        // 3. Stroke line
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.9f),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // 4. Dot markers
        points.forEach { point ->
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = point
            )
            drawCircle(
                color = Color(0xFF2193B0),
                radius = 3.5.dp.toPx(),
                center = point,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}
