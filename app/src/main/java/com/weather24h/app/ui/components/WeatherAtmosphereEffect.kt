package com.weather24h.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.weather24h.app.data.model.WeatherType
import kotlin.random.Random

/**
 * Hiệu ứng thời tiết trực quan động (Dynamic Weather Atmosphere)
 * Tái hiện sinh động ánh nắng rạng rỡ, mưa rơi lất phất/mưa rào, sấm sét chớp nháy,
 * mây bay lững lờ hoặc bầu trời sao lấp lánh ban đêm.
 */
@Composable
fun WeatherAtmosphereEffect(
    type: WeatherType,
    isDay: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WeatherAtmosphere")

    // Chu kỳ chuyển động liên tục
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AtmosphereLoop"
    )

    // Chu kỳ chớp sáng (sấm chớp hoặc tia nắng thở)
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseEffect"
    )

    // Chu kỳ chớp sấm sét ngắn ngẫu nhiên
    val lightningFlash by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000
                0.0f at 0
                0.0f at 3800
                0.85f at 3880
                0.1f at 3960
                0.95f at 4040
                0.0f at 4200
                0.0f at 5000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "LightningEffect"
    )

    // Khởi tạo vị trí hạt cố định qua remember để tránh khởi tạo lại mỗi frame
    val rainParticles = remember {
        List(60) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 0.5f + 0.5f)
        }
    }

    val stars = remember {
        List(40) {
            Triple(Random.nextFloat(), Random.nextFloat() * 0.65f, Random.nextFloat() * 0.7f + 0.3f)
        }
    }

    val snowParticles = remember {
        List(50) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 3f + 2f)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        when {
            // 1. DÔNG BÃO / SẤM SÉT (Thunderstorm)
            type == WeatherType.THUNDERSTORM -> {
                // Hiệu ứng chớp giật sấm sét chiếu sáng toàn bầu trời
                if (lightningFlash > 0.05f) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFFC084FC).copy(alpha = lightningFlash * 0.35f),
                                Color(0xFF67E8F9).copy(alpha = lightningFlash * 0.15f),
                                Color.Transparent
                            )
                        ),
                        size = size
                    )
                }
                // Hạt mưa rơi dày và nhanh
                drawRainStreaks(rainParticles, animationProgress, width, height, speedMultiplier = 1.6f, isHeavy = true)
            }

            // 2. MƯA / MƯA PHÙN (Rain / Drizzle)
            type == WeatherType.RAIN || type == WeatherType.DRIZZLE -> {
                val isHeavy = type == WeatherType.RAIN
                val speed = if (isHeavy) 1.2f else 0.7f
                drawRainStreaks(rainParticles, animationProgress, width, height, speedMultiplier = speed, isHeavy = isHeavy)

                // Lớp sương ẩm mờ ở đáy màn hình
                drawRect(
                    brush = Brush.verticalGradient(
                        0.7f to Color.Transparent,
                        1.0f to Color.White.copy(alpha = 0.08f)
                    ),
                    size = size
                )
            }

            // 3. TRỜI NẮNG / TRONG XANH BAN NGÀY (Clear / Sunny Day)
            (type == WeatherType.CLEAR || type == WeatherType.PARTLY_CLOUDY) && isDay -> {
                val sunCenter = Offset(width * 0.82f, height * 0.12f)
                val baseRadius = width * 0.28f

                // Vầng hào quang mặt trời tỏa sáng ấm áp
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFDE047).copy(alpha = 0.45f * pulseProgress),
                            Color(0xFFF59E0B).copy(alpha = 0.22f * pulseProgress),
                            Color(0xFFFB923C).copy(alpha = 0.08f * pulseProgress),
                            Color.Transparent
                        ),
                        center = sunCenter,
                        radius = baseRadius * 1.5f
                    ),
                    center = sunCenter,
                    radius = baseRadius * 1.5f
                )

                // Đĩa mặt trời rực rỡ ở tâm
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color(0xFFFEF08A).copy(alpha = 0.85f),
                            Color(0xFFF59E0B).copy(alpha = 0.3f)
                        ),
                        center = sunCenter,
                        radius = baseRadius * 0.45f
                    ),
                    center = sunCenter,
                    radius = baseRadius * 0.45f
                )

                // Các tia nắng tỏa chéo huyền ảo (God Rays)
                drawSunRays(sunCenter, width, height, pulseProgress)
            }

            // 4. BAN ĐÊM TRỜI QUANG (Clear Night / Starlight)
            !isDay && (type == WeatherType.CLEAR || type == WeatherType.PARTLY_CLOUDY) -> {
                // Ánh trăng dịu mát góc phải
                val moonCenter = Offset(width * 0.80f, height * 0.11f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE2E8F0).copy(alpha = 0.35f * pulseProgress),
                            Color(0xFF94A3B8).copy(alpha = 0.12f * pulseProgress),
                            Color.Transparent
                        ),
                        center = moonCenter,
                        radius = width * 0.22f
                    ),
                    center = moonCenter,
                    radius = width * 0.22f
                )

                drawCircle(
                    color = Color(0xFFF8FAFC).copy(alpha = 0.85f),
                    center = moonCenter,
                    radius = width * 0.065f
                )

                // Sao lấp lánh trên bầu trời
                stars.forEachIndexed { index, (xRatio, yRatio, baseBrightness) ->
                    val starX = xRatio * width
                    val starY = yRatio * height
                    val starTwinkle = ((pulseProgress + index * 0.15f) % 1.0f) * 0.5f + 0.5f
                    val starAlpha = (baseBrightness * starTwinkle).coerceIn(0.15f, 0.95f)

                    drawCircle(
                        color = Color.White.copy(alpha = starAlpha),
                        center = Offset(starX, starY),
                        radius = (if (index % 5 == 0) 2.2f else 1.2f)
                    )
                }
            }

            // 5. NHIỀU MÂY / ÂM U / SƯƠNG MÙ (Cloudy / Fog)
            type == WeatherType.CLOUDY || type == WeatherType.FOG -> {
                // Lớp mây trôi lững lờ mềm mại
                val cloudOffset = (animationProgress * width * 0.25f)
                drawCloudLayer(width, height, cloudOffset, 0.15f * pulseProgress)
            }

            // 6. TUYẾT RƠI (Snow)
            type == WeatherType.SNOW -> {
                snowParticles.forEachIndexed { index, (xRatio, yRatio, flakeRadius) ->
                    val progressWithOffset = (animationProgress + index * 0.04f) % 1f
                    val currentY = (yRatio + progressWithOffset) % 1f * height
                    val sway = kotlin.math.sin(progressWithOffset * 6.28 + index) * 12f
                    val currentX = (xRatio * width + sway.toFloat()) % width

                    drawCircle(
                        color = Color.White.copy(alpha = 0.75f),
                        center = Offset(currentX, currentY),
                        radius = flakeRadius
                    )
                }
            }
        }
    }
}

/**
 * Vẽ các vệt mưa chéo rơi liên tục
 */
private fun DrawScope.drawRainStreaks(
    particles: List<Triple<Float, Float, Float>>,
    progress: Float,
    width: Float,
    height: Float,
    speedMultiplier: Float,
    isHeavy: Boolean
) {
    val slantX = -18f // Độ nghiêng gió chéo

    particles.forEachIndexed { index, (xRatio, yRatio, lengthMultiplier) ->
        val speed = ((index % 4) + 2) * 0.25f * speedMultiplier
        val currentProgress = (progress * speed + yRatio) % 1f
        val startY = currentProgress * (height + 100f) - 50f
        val startX = (xRatio * width + (slantX * currentProgress * 2)) % width
        val streakLength = (if (isHeavy) 32f else 20f) * lengthMultiplier

        val endX = startX + slantX * (streakLength / 40f)
        val endY = startY + streakLength

        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.05f),
                    Color(0xFFBAE6FD).copy(alpha = if (isHeavy) 0.55f else 0.35f),
                    Color.White.copy(alpha = 0.15f)
                ),
                startY = startY,
                endY = endY
            ),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = if (isHeavy) 1.8f else 1.1f
        )
    }
}

/**
 * Vẽ tia nắng mặt trời chéo tự nhiên
 */
private fun DrawScope.drawSunRays(
    sunCenter: Offset,
    width: Float,
    height: Float,
    pulse: Float
) {
    val rayPath = Path().apply {
        moveTo(sunCenter.x, sunCenter.y)
        lineTo(width * 0.2f, height * 0.75f)
        lineTo(width * 0.45f, height * 0.85f)
        close()
    }
    drawPath(
        path = rayPath,
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFEF08A).copy(alpha = 0.16f * pulse),
                Color(0xFFF59E0B).copy(alpha = 0.06f * pulse),
                Color.Transparent
            ),
            center = sunCenter,
            radius = width * 1.2f
        )
    )

    val rayPath2 = Path().apply {
        moveTo(sunCenter.x, sunCenter.y)
        lineTo(0f, height * 0.35f)
        lineTo(width * 0.15f, height * 0.55f)
        close()
    }
    drawPath(
        path = rayPath2,
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFDE047).copy(alpha = 0.12f * pulse),
                Color.Transparent
            ),
            center = sunCenter,
            radius = width * 1.0f
        )
    )
}

/**
 * Vẽ các đám mây mờ mềm trôi chậm
 */
private fun DrawScope.drawCloudLayer(
    width: Float,
    height: Float,
    offset: Float,
    alpha: Float
) {
    val cloudColor = Color.White.copy(alpha = alpha.coerceIn(0.08f, 0.22f))
    val baseY = height * 0.08f

    drawOval(
        color = cloudColor,
        topLeft = Offset((width * 0.1f - offset) % width - 100f, baseY),
        size = Size(width * 0.7f, height * 0.16f)
    )

    drawOval(
        color = cloudColor.copy(alpha = alpha * 0.7f),
        topLeft = Offset((width * 0.5f - offset * 0.6f) % width - 150f, baseY + 40f),
        size = Size(width * 0.8f, height * 0.2f)
    )
}
