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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.weather24h.app.data.model.WeatherType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Hiệu ứng thời tiết trực quan động cao cấp (Cinematic Weather Atmosphere)
 * - Nắng rạng rỡ với vầng hào quang thở và tia nắng xoay chậm (God Rays)
 * - Mưa rơi tự nhiên với hạt nước chéo và tia bọt nước mặt đất
 * - Dông sét chân thực với chớp sáng toàn cảnh và tia sét phân nhánh
 * - Đêm sao lấp lánh đa tầng cùng vệt sao băng lướt nhẹ
 * - Mây bay đa tầng thị sai (parallax) bồng bềnh
 */
@Composable
fun WeatherAtmosphereEffect(
    type: WeatherType,
    isDay: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WeatherAtmosphere")

    // Chu kỳ chuyển động mưa/tuyết/mây liên tục (2400ms)
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AtmosphereLoop"
    )

    // Chu kỳ thở hào quang mặt trời / mặt trăng (3600ms)
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseEffect"
    )

    // Chu kỳ xoay chậm của vầng hào quang nắng (36000ms / 36s cho 1 vòng quay siêu êm ái)
    val sunAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 36000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SunAngle"
    )

    // Chu kỳ sấm sét chớp nháy thực tế
    val lightningFlash by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 6000
                0.0f at 0
                0.0f at 4200
                0.90f at 4270
                0.15f at 4340
                1.00f at 4410
                0.20f at 4490
                0.0f at 4600
                0.0f at 6000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "LightningEffect"
    )

    // Chu kỳ sao băng đêm thỉnh thoảng vụt qua (7000ms)
    val meteorProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7500
                0.0f at 0
                0.0f at 5200
                1.0f at 6200
                1.0f at 7500
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "MeteorProgress"
    )

    // Khởi tạo các mảng hạt ngẫu nhiên qua remember
    val rainParticles = remember {
        List(80) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 0.6f + 0.6f)
        }
    }

    val stars = remember {
        List(48) {
            Triple(Random.nextFloat(), Random.nextFloat() * 0.70f, Random.nextFloat() * 0.6f + 0.4f)
        }
    }

    val snowParticles = remember {
        List(55) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 3.5f + 1.8f)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        when {
            // 1. DÔNG BÃO & SẤM SÉT (Thunderstorm)
            type == WeatherType.THUNDERSTORM -> {
                // Ánh chớp giật bầu trời
                if (lightningFlash > 0.05f) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFFC084FC).copy(alpha = lightningFlash * 0.42f),
                                Color(0xFF67E8F9).copy(alpha = lightningFlash * 0.20f),
                                Color.Transparent
                            )
                        ),
                        size = size
                    )

                    // Vẽ tia sét nhánh khi chớp sáng cực đại
                    if (lightningFlash > 0.4f) {
                        drawLightningBolt(width, height, lightningFlash)
                    }
                }

                // Mưa rơi nhanh, hạt dày và giọt nước bắn
                drawRainStreaks(rainParticles, animationProgress, width, height, speedMultiplier = 1.7f, isHeavy = true)
            }

            // 2. MƯA / MƯA PHÙN (Rain / Drizzle)
            type == WeatherType.RAIN || type == WeatherType.DRIZZLE -> {
                val isHeavy = type == WeatherType.RAIN
                val speed = if (isHeavy) 1.25f else 0.75f
                drawRainStreaks(rainParticles, animationProgress, width, height, speedMultiplier = speed, isHeavy = isHeavy)

                // Lớp hơi ẩm đáy màn hình
                drawRect(
                    brush = Brush.verticalGradient(
                        0.72f to Color.Transparent,
                        1.0f to Color.White.copy(alpha = 0.09f)
                    ),
                    size = size
                )
            }

            // 3. TRỜI NẮNG / TRONG XANH BAN NGÀY (Clear / Sunny Day)
            (type == WeatherType.CLEAR || type == WeatherType.PARTLY_CLOUDY) && isDay -> {
                val sunCenter = Offset(width * 0.82f, height * 0.11f)
                val baseRadius = width * 0.28f

                // Vầng hào quang ấm áp tỏa ra
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFDE047).copy(alpha = 0.42f * pulseProgress),
                            Color(0xFFF59E0B).copy(alpha = 0.20f * pulseProgress),
                            Color(0xFFFB923C).copy(alpha = 0.06f * pulseProgress),
                            Color.Transparent
                        ),
                        center = sunCenter,
                        radius = baseRadius * 1.6f
                    ),
                    center = sunCenter,
                    radius = baseRadius * 1.6f
                )

                // Đĩa mặt trời rực rỡ tâm điểm
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.98f),
                            Color(0xFFFEF08A).copy(alpha = 0.90f),
                            Color(0xFFF59E0B).copy(alpha = 0.35f)
                        ),
                        center = sunCenter,
                        radius = baseRadius * 0.42f
                    ),
                    center = sunCenter,
                    radius = baseRadius * 0.42f
                )

                // Các tia nắng xoay chậm mượt mà (God Rays xoay nhẹ theo sunAngle)
                rotate(degrees = sunAngle, pivot = sunCenter) {
                    drawSunRays(sunCenter, width, height, pulseProgress)
                }
            }

            // 4. BAN ĐÊM TRỜI QUANG (Clear Night / Starlight & Meteors)
            !isDay && (type == WeatherType.CLEAR || type == WeatherType.PARTLY_CLOUDY) -> {
                // Ánh trăng dịu mát
                val moonCenter = Offset(width * 0.80f, height * 0.10f)
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
                    color = Color(0xFFF8FAFC).copy(alpha = 0.88f),
                    center = moonCenter,
                    radius = width * 0.062f
                )

                // Bầu trời sao lấp lánh đa tần số
                stars.forEachIndexed { index, (xRatio, yRatio, baseBrightness) ->
                    val starX = xRatio * width
                    val starY = yRatio * height
                    val starTwinkle = ((pulseProgress * (1f + (index % 3) * 0.2f) + index * 0.12f) % 1.0f) * 0.5f + 0.5f
                    val starAlpha = (baseBrightness * starTwinkle).coerceIn(0.18f, 0.96f)

                    drawCircle(
                        color = Color.White.copy(alpha = starAlpha),
                        center = Offset(starX, starY),
                        radius = (if (index % 6 == 0) 2.4f else 1.3f)
                    )
                }

                // Vệt sao băng lướt qua bầu trời đêm
                if (meteorProgress in 0.01f..0.99f) {
                    val mStartX = width * (0.85f - meteorProgress * 0.65f)
                    val mStartY = height * (0.05f + meteorProgress * 0.35f)
                    val mTailLength = 45f
                    val tailEndX = mStartX + mTailLength * 0.8f
                    val tailEndY = mStartY - mTailLength * 0.5f

                    drawLine(
                        brush = Brush.linearGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFFBAE6FD).copy(alpha = 0.4f),
                                Color.White.copy(alpha = 0.95f)
                            ),
                            start = Offset(tailEndX, tailEndY),
                            end = Offset(mStartX, mStartY)
                        ),
                        start = Offset(tailEndX, tailEndY),
                        end = Offset(mStartX, mStartY),
                        strokeWidth = 2.0f
                    )
                }
            }

            // 5. NHIỀU MÂY / ÂM U / SƯƠNG MÙ (Volumetric Parallax Clouds)
            type == WeatherType.CLOUDY || type == WeatherType.FOG -> {
                drawVolumetricCloudLayers(width, height, animationProgress, pulseProgress)
            }

            // 6. TUYẾT RƠI (Snow)
            type == WeatherType.SNOW -> {
                snowParticles.forEachIndexed { index, (xRatio, yRatio, flakeRadius) ->
                    val progressWithOffset = (animationProgress + index * 0.035f) % 1f
                    val currentY = (yRatio + progressWithOffset) % 1f * height
                    val sway = sin(progressWithOffset * 6.28 + index) * 14f
                    val currentX = (xRatio * width + sway.toFloat()) % width

                    drawCircle(
                        color = Color.White.copy(alpha = 0.78f),
                        center = Offset(currentX, currentY),
                        radius = flakeRadius
                    )
                }
            }
        }
    }
}

/**
 * Vẽ các vệt mưa chéo rơi liên tục kèm bọt bắn nhẹ
 */
private fun DrawScope.drawRainStreaks(
    particles: List<Triple<Float, Float, Float>>,
    progress: Float,
    width: Float,
    height: Float,
    speedMultiplier: Float,
    isHeavy: Boolean
) {
    val slantX = -18f

    particles.forEachIndexed { index, (xRatio, yRatio, lengthMultiplier) ->
        val speed = ((index % 4) + 2) * 0.25f * speedMultiplier
        val currentProgress = (progress * speed + yRatio) % 1f
        val startY = currentProgress * (height + 100f) - 50f
        val startX = (xRatio * width + (slantX * currentProgress * 2)) % width
        val streakLength = (if (isHeavy) 34f else 22f) * lengthMultiplier

        val endX = startX + slantX * (streakLength / 40f)
        val endY = startY + streakLength

        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.05f),
                    Color(0xFFBAE6FD).copy(alpha = if (isHeavy) 0.55f else 0.35f),
                    Color.White.copy(alpha = 0.20f)
                ),
                startY = startY,
                endY = endY
            ),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = if (isHeavy) 1.8f else 1.2f
        )
    }
}

/**
 * Vẽ tia sét phân nhánh khi có dông bão
 */
private fun DrawScope.drawLightningBolt(width: Float, height: Float, flashAlpha: Float) {
    val boltPath = Path().apply {
        val startX = width * 0.45f
        moveTo(startX, 0f)
        lineTo(startX - 25f, height * 0.18f)
        lineTo(startX + 10f, height * 0.26f)
        lineTo(startX - 35f, height * 0.42f)
        lineTo(startX + 5f, height * 0.50f)
        lineTo(startX - 20f, height * 0.68f)
    }

    drawPath(
        path = boltPath,
        color = Color(0xFFF3E8FF).copy(alpha = flashAlpha.coerceIn(0f, 0.9f)),
        style = Stroke(width = 2.8f)
    )

    drawPath(
        path = boltPath,
        color = Color(0xFF67E8F9).copy(alpha = flashAlpha * 0.4f),
        style = Stroke(width = 6.0f)
    )
}

/**
 * Vẽ tia nắng mặt trời tỏa góc
 */
private fun DrawScope.drawSunRays(
    sunCenter: Offset,
    width: Float,
    height: Float,
    pulse: Float
) {
    for (i in 0 until 6) {
        val angleRad = (i * 60f) * (Math.PI / 180.0)
        val rayLength = width * 0.9f
        val endX = sunCenter.x + (cos(angleRad) * rayLength).toFloat()
        val endY = sunCenter.y + (sin(angleRad) * rayLength).toFloat()

        val path = Path().apply {
            moveTo(sunCenter.x, sunCenter.y)
            lineTo(endX + 35f, endY + 35f)
            lineTo(endX - 35f, endY - 35f)
            close()
        }

        drawPath(
            path = path,
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFEF08A).copy(alpha = 0.12f * pulse),
                    Color(0xFFF59E0B).copy(alpha = 0.04f * pulse),
                    Color.Transparent
                ),
                center = sunCenter,
                radius = rayLength
            )
        )
    }
}

/**
 * Vẽ mây bồng bềnh 3 lớp thị sai (Volumetric Parallax Clouds)
 */
private fun DrawScope.drawVolumetricCloudLayers(
    width: Float,
    height: Float,
    progress: Float,
    pulse: Float
) {
    val baseY = height * 0.08f

    // Lớp mây sau (chạy chậm)
    val offset1 = progress * width * 0.12f
    drawOval(
        color = Color.White.copy(alpha = 0.10f * pulse + 0.05f),
        topLeft = Offset((width * 0.6f - offset1) % (width * 1.4f) - width * 0.2f, baseY - 20f),
        size = Size(width * 0.85f, height * 0.20f)
    )

    // Lớp mây giữa (chạy vừa)
    val offset2 = progress * width * 0.22f
    drawOval(
        color = Color.White.copy(alpha = 0.14f * pulse + 0.06f),
        topLeft = Offset((width * 0.15f - offset2) % (width * 1.3f) - width * 0.15f, baseY + 25f),
        size = Size(width * 0.75f, height * 0.17f)
    )

    // Lớp mây trước (chạy nhanh hơn)
    val offset3 = progress * width * 0.32f
    drawOval(
        color = Color.White.copy(alpha = 0.08f * pulse + 0.04f),
        topLeft = Offset((width * 0.85f - offset3) % (width * 1.5f) - width * 0.2f, baseY + 60f),
        size = Size(width * 0.90f, height * 0.22f)
    )
}
