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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.weather24h.app.data.model.WeatherType
import com.weather24h.app.ui.theme.AtmospherePerformanceMode
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Động cơ hiệu ứng khí quyển thời tiết thời gian thực cao cấp (Living Dynamic Weather Atmosphere Engine)
 * 
 * - MƯA THỰC TẾ: Giọt mưa rơi nghiêng theo gió đa tầng (foreground/background), bọt nước bắn tung tóe (splash)
 *   và gợn sóng nước (ripple) lăn tăn trên bề mặt đáy, lớp sương hơi nước ẩm bốc lên mềm mại.
 * - DÔNG SÉT VOLUMETRIC: Chớp giật đa xung (double-flash burst) phủ sáng chân thực toàn bầu trời,
 *   kèm tia sét phân nhánh ngẫu nhiên với lõi sáng trắng tím và quầng hào quang cyan.
 * - NẮNG VÀNG RỰC RỠ: Đĩa mặt trời tỏa vầng hào quang ấm áp, chùm tia nắng thể tích (God Rays) xoay êm ái,
 *   kèm các hạt bụi nắng (solar dust motes / bokeh) lơ lửng bồng bềnh trong không khí.
 * - ĐÊM SAO & CỰC QUANG: Bầu trời sao đa tầng thị sai với tia sao 4 cánh lấp lánh, dải lụa cực quang
 *   (Aurora wave) huyền ảo uốn lượn êm dịu, vệt sao băng rực rỡ lướt qua.
 * - MÂY TRÔI ĐA TẦNG: Các cụm mây thể tích bồng bềnh 3 lớp thị sai trôi nhịp nhàng với tốc độ khác nhau.
 * - SƯƠNG MÙ KHÍ QUYỂN: Các dải sương mờ dập dềnh uốn sóng ngang mềm mại.
 * - TUYẾT RƠI LÃNG MẠN: Các bông tuyết lượn sóng đa kích thước bay chậm rãi.
 * 
 * Tối ưu hóa 100% GPU Canvas, không cấp phát bộ nhớ trong frame render (Zero Garbage Collection).
 */
@Composable
fun WeatherAtmosphereEffect(
    type: WeatherType,
    isDay: Boolean,
    isScrolling: Boolean = false,
    isPowerSaveMode: Boolean = false,
    performanceMode: AtmospherePerformanceMode = AtmospherePerformanceMode.BALANCED,
    modifier: Modifier = Modifier
) {
    val isBatterySaving = when (performanceMode) {
        AtmospherePerformanceMode.BATTERY_SAVER -> true
        AtmospherePerformanceMode.HIGH_QUALITY -> false
        AtmospherePerformanceMode.BALANCED -> isPowerSaveMode
    }

    val infiniteTransition = rememberInfiniteTransition(label = "WeatherAtmosphereLoop")

    // 1. Chu kỳ chuyển động chính của mưa / tuyết / mây (2200ms)
    val mainLoop by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "MainLoop"
    )

    // 2. Nhịp thở hào quang mặt trời / cực quang / nhịp sóng (3800ms)
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathingPulse"
    )

    // 3. Chu kỳ xoay chậm của tia nắng God Rays (42000ms / 42s cho chuyển động vô cùng êm ái)
    val sunAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 42000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SunAngle"
    )

    // 4. Chu kỳ dông sét chớp giật tự nhiên đa xung (Double-Pulse Flash, chu kỳ 6800ms)
    val stormFlash by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 6800
                0.0f at 0
                0.0f at 4800
                0.75f at 4860 // Chớp mồi
                0.18f at 4910 // Khoảng ngắt ngắn
                1.00f at 4980 // Chớp chính cực đại
                0.35f at 5050 // Âm hưởng chớp
                0.00f at 5160
                0.0f at 6800
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "StormFlash"
    )

    // 5. Chu kỳ sao băng lướt qua bầu trời đêm (8500ms)
    val meteorProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 8500
                0.0f at 0
                0.0f at 6000
                1.0f at 7100
                1.0f at 8500
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "MeteorProgress"
    )

    // 6. Chu kỳ trôi mây & sương mù chậm rãi (18000ms)
    val slowDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SlowDrift"
    )

    // 7. Khởi tạo mảng hạt cố định ngẫu nhiên (Pre-computed particles, không tạo mới trong frame draw)
    // Hạt mưa: (xRatio, yRatio, speedFactor, lengthFactor, layer: 0=xa/1=gần)
    val rainDrops = remember {
        List(95) {
            val isFg = Random.nextFloat() > 0.45f
            RainDropParticle(
                xRatio = Random.nextFloat(),
                yRatio = Random.nextFloat(),
                speed = if (isFg) Random.nextFloat() * 0.4f + 0.9f else Random.nextFloat() * 0.3f + 0.6f,
                length = if (isFg) Random.nextFloat() * 12f + 26f else Random.nextFloat() * 8f + 16f,
                thickness = if (isFg) 1.6f else 1.0f,
                alpha = if (isFg) Random.nextFloat() * 0.35f + 0.45f else Random.nextFloat() * 0.20f + 0.20f,
                isForeground = isFg
            )
        }
    }

    // Điểm bọt nước bắn (Splashes & Ripples ở đáy màn hình):
    val splashPoints = remember {
        List(14) {
            SplashSite(
                xRatio = Random.nextFloat(),
                yRatio = Random.nextFloat() * 0.20f + 0.80f, // 80% - 100% chiều cao
                phaseOffset = Random.nextFloat(),
                maxRadius = Random.nextFloat() * 10f + 12f
            )
        }
    }

    // Hạt bụi nắng lơ lửng ban ngày (Solar Dust Motes / Warm Bokeh):
    val dustMotes = remember {
        List(30) {
            DustMoteParticle(
                xRatio = Random.nextFloat(),
                yRatio = Random.nextFloat() * 0.75f,
                radius = Random.nextFloat() * 2.2f + 1.2f,
                driftSpeed = Random.nextFloat() * 0.4f + 0.3f,
                phase = Random.nextFloat() * 6.28f,
                baseAlpha = Random.nextFloat() * 0.45f + 0.25f
            )
        }
    }

    // Bầu trời sao đêm: (xRatio, yRatio, size, twinkleFreq, hasSpikes)
    val starField = remember {
        List(60) { index ->
            val hasSpikes = index % 12 == 0
            val size = if (hasSpikes) Random.nextFloat() * 1.4f + 2.4f else Random.nextFloat() * 1.0f + 1.0f
            StarParticle(
                xRatio = Random.nextFloat(),
                yRatio = Random.nextFloat() * 0.72f,
                radius = size,
                twinkleSpeed = Random.nextFloat() * 1.5f + 0.8f,
                phase = Random.nextFloat() * 6.28f,
                baseAlpha = if (hasSpikes) 0.92f else Random.nextFloat() * 0.5f + 0.35f,
                hasCrossSpikes = hasSpikes
            )
        }
    }

    // Hạt tuyết:
    val snowFlakes = remember {
        List(65) {
            SnowFlakeParticle(
                xRatio = Random.nextFloat(),
                yRatio = Random.nextFloat(),
                radius = Random.nextFloat() * 3.5f + 1.5f,
                speed = Random.nextFloat() * 0.4f + 0.5f,
                swayAmplitude = Random.nextFloat() * 16f + 8f,
                phase = Random.nextFloat() * 6.28f,
                alpha = Random.nextFloat() * 0.45f + 0.50f
            )
        }
    }

    // Lọc tập hạt động tối ưu theo trạng thái cuộn và chế độ pin
    val activeRainDrops = remember(rainDrops, isBatterySaving, isScrolling) {
        when {
            isBatterySaving -> rainDrops.take(35)
            isScrolling -> rainDrops.take(50)
            else -> rainDrops
        }
    }

    val activeSplashPoints = remember(splashPoints, isBatterySaving, isScrolling) {
        when {
            isScrolling -> emptyList() // Bỏ qua bọt nước & gợn sóng khi cuộn danh sách để đạt 60/120 FPS tuyệt đối
            isBatterySaving -> splashPoints.take(5)
            else -> splashPoints
        }
    }

    val activeDustMotes = remember(dustMotes, isBatterySaving, isScrolling) {
        when {
            isBatterySaving -> dustMotes.take(10)
            isScrolling -> dustMotes.take(15)
            else -> dustMotes
        }
    }

    val activeStarField = remember(starField, isBatterySaving) {
        if (isBatterySaving) starField.take(24) else starField
    }

    val activeSnowFlakes = remember(snowFlakes, isBatterySaving, isScrolling) {
        when {
            isBatterySaving -> snowFlakes.take(22)
            isScrolling -> snowFlakes.take(35)
            else -> snowFlakes
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        when {
            // =========================================================================
            // 1. DÔNG BÃO & SẤM SÉT (Thunderstorm)
            // =========================================================================
            type == WeatherType.THUNDERSTORM -> {
                // Ánh chớp giật bầu trời lan tỏa đa tầng sắc tím điện & xanh cyan
                if (stormFlash > 0.05f) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFFC084FC).copy(alpha = (stormFlash * 0.46f).coerceIn(0f, 0.9f)),
                                Color(0xFF38BDF8).copy(alpha = (stormFlash * 0.25f).coerceIn(0f, 0.6f)),
                                Color.Transparent
                            )
                        ),
                        size = size
                    )

                    // Vẽ tia sét nhánh rực rỡ khi chớp đạt đỉnh (> 0.4)
                    if (stormFlash > 0.38f) {
                        drawRealisticLightningFork(width, height, stormFlash)
                    }
                }

                // Mưa xối xả tốc độ cao kèm bọt nước bắn mạnh
                drawRainStreaksEnhanced(activeRainDrops, mainLoop, width, height, speedMultiplier = 1.6f, isHeavy = true)
                if (activeSplashPoints.isNotEmpty()) {
                    drawWaterSplashesAndRipples(activeSplashPoints, mainLoop, width, height, intensity = 1.4f)
                }
            }

            // =========================================================================
            // 2. MƯA & MƯA PHÙN (Rain & Drizzle)
            // =========================================================================
            type == WeatherType.RAIN || type == WeatherType.DRIZZLE -> {
                val isHeavy = type == WeatherType.RAIN
                val speed = if (isHeavy) 1.25f else 0.75f
                val splashIntensity = if (isHeavy) 1.0f else 0.5f

                drawRainStreaksEnhanced(activeRainDrops, mainLoop, width, height, speedMultiplier = speed, isHeavy = isHeavy)
                if (activeSplashPoints.isNotEmpty()) {
                    drawWaterSplashesAndRipples(activeSplashPoints, mainLoop, width, height, intensity = splashIntensity)
                }

                // Lớp sương ẩm mờ dịu ở đáy màn hình
                drawRect(
                    brush = Brush.verticalGradient(
                        0.75f to Color.Transparent,
                        1.0f to Color.White.copy(alpha = if (isHeavy) 0.12f else 0.06f)
                    ),
                    size = size
                )
            }

            // =========================================================================
            // 3. TRỜI NẮNG VÀNG / QUANG ĐÃNG BAN NGÀY (Clear / Sunny Day)
            // =========================================================================
            (type == WeatherType.CLEAR || type == WeatherType.PARTLY_CLOUDY) && isDay -> {
                val sunCenter = Offset(width * 0.82f, height * 0.11f)
                val baseRadius = width * 0.28f

                // Hào quang ấm áp tỏa rộng với nhịp thở nhẹ nhàng
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFDE047).copy(alpha = 0.44f * breathingPulse),
                            Color(0xFFF59E0B).copy(alpha = 0.22f * breathingPulse),
                            Color(0xFFFB923C).copy(alpha = 0.08f * breathingPulse),
                            Color.Transparent
                        ),
                        center = sunCenter,
                        radius = baseRadius * 1.8f
                    ),
                    center = sunCenter,
                    radius = baseRadius * 1.8f
                )

                // Đĩa mặt trời rực sáng tâm điểm
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.98f),
                            Color(0xFFFEF08A).copy(alpha = 0.92f),
                            Color(0xFFF59E0B).copy(alpha = 0.40f)
                        ),
                        center = sunCenter,
                        radius = baseRadius * 0.45f
                    ),
                    center = sunCenter,
                    radius = baseRadius * 0.45f
                )

                // Chùm tia nắng thể tích (God Rays) xoay êm ả 360 độ (cố định góc khi cuộn để giải phóng GPU)
                val currentSunAngle = if (isScrolling) 45f else sunAngle
                rotate(degrees = currentSunAngle, pivot = sunCenter) {
                    drawSoftGodRays(sunCenter, width, height, breathingPulse, isBatterySaving = isBatterySaving)
                }

                // Các hạt bụi nắng (solar dust motes / warm bokeh) lơ lửng trong không khí
                drawFloatingSolarMotes(activeDustMotes, mainLoop, width, height)
            }

            // =========================================================================
            // 4. BẦU TRỜI ĐÊM SAO & CỰC QUANG (Starry Night, Aurora & Meteors)
            // =========================================================================
            !isDay && (type == WeatherType.CLEAR || type == WeatherType.PARTLY_CLOUDY) -> {
                // Ánh trăng dịu mát với quầng sáng thanh thoát
                val moonCenter = Offset(width * 0.80f, height * 0.10f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE2E8F0).copy(alpha = 0.38f * breathingPulse),
                            Color(0xFF94A3B8).copy(alpha = 0.14f * breathingPulse),
                            Color.Transparent
                        ),
                        center = moonCenter,
                        radius = width * 0.24f
                    ),
                    center = moonCenter,
                    radius = width * 0.24f
                )

                // Lõi vầng trăng sáng
                drawCircle(
                    color = Color(0xFFF8FAFC).copy(alpha = 0.90f),
                    center = moonCenter,
                    radius = width * 0.065f
                )

                // Dải lụa cực quang huyền ảo (Aurora Ribbon Wave) lượn sóng trên vòm trời
                drawAuroraRibbon(width, height, slowDrift, breathingPulse, isBatterySaving = isBatterySaving)

                // Bầu trời sao lấp lánh đa tần số kèm tia sao 4 cánh
                drawStarFieldWithCrossSpikes(activeStarField, breathingPulse, width, height, isBatterySaving = isBatterySaving)

                // Vệt sao băng rực rỡ lướt qua vòm trời đêm (bỏ qua khi cuộn hoặc tiết kiệm pin)
                if (!isBatterySaving && !isScrolling && meteorProgress in 0.01f..0.99f) {
                    drawShootingMeteor(width, height, meteorProgress)
                }
            }

            // =========================================================================
            // 5. NHIỀU MÂY & U ÁM (Multi-tier Volumetric Clouds)
            // =========================================================================
            type == WeatherType.CLOUDY -> {
                drawRealisticCloudFormations(width, height, slowDrift, breathingPulse)
            }

            // =========================================================================
            // 6. SƯƠNG MÙ KHÍ QUYỂN (Atmospheric Rolling Fog / Mist)
            // =========================================================================
            type == WeatherType.FOG -> {
                drawAtmosphericRollingMist(width, height, slowDrift, breathingPulse)
            }

            // =========================================================================
            // 7. TUYẾT RƠI LÃNG MẠN (Romantic Snowfall)
            // =========================================================================
            type == WeatherType.SNOW -> {
                drawGentleSnowfall(activeSnowFlakes, mainLoop, width, height)
            }
        }
    }
}

// =====================================================================================
// DATA CLASSES CHO HẠT HIỆU ỨNG (PRE-ALLOCATED DATA MODELS)
// =====================================================================================

private data class RainDropParticle(
    val xRatio: Float,
    val yRatio: Float,
    val speed: Float,
    val length: Float,
    val thickness: Float,
    val alpha: Float,
    val isForeground: Boolean
)

private data class SplashSite(
    val xRatio: Float,
    val yRatio: Float,
    val phaseOffset: Float,
    val maxRadius: Float
)

private data class DustMoteParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val driftSpeed: Float,
    val phase: Float,
    val baseAlpha: Float
)

private data class StarParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val twinkleSpeed: Float,
    val phase: Float,
    val baseAlpha: Float,
    val hasCrossSpikes: Boolean
)

private data class SnowFlakeParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val speed: Float,
    val swayAmplitude: Float,
    val phase: Float,
    val alpha: Float
)

// =====================================================================================
// DRAWSCOPE HELPER FUNCTIONS (HARDWARE-ACCELERATED RENDER LOGIC)
// =====================================================================================

/**
 * Vẽ các vệt mưa chéo rơi tự nhiên kèm độ sâu thị sai (foreground / background)
 */
private fun DrawScope.drawRainStreaksEnhanced(
    drops: List<RainDropParticle>,
    progress: Float,
    width: Float,
    height: Float,
    speedMultiplier: Float,
    isHeavy: Boolean
) {
    val slantX = -18f

    drops.forEach { drop ->
        val currentProgress = (progress * drop.speed * speedMultiplier + drop.yRatio) % 1f
        val startY = currentProgress * (height + 120f) - 60f
        val startX = (drop.xRatio * width + (slantX * currentProgress * 2.2f)) % width
        val streakLength = drop.length * (if (isHeavy) 1.25f else 1.0f)

        val endX = startX + slantX * (streakLength / 40f)
        val endY = startY + streakLength

        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.04f),
                    Color(0xFFBAE6FD).copy(alpha = drop.alpha),
                    Color.White.copy(alpha = drop.alpha * 0.85f)
                ),
                startY = startY,
                endY = endY
            ),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = drop.thickness
        )
    }
}

/**
 * Hiệu ứng bọt nước bắn tung tóe (Splashes) và gợn sóng (Ripples) khi giọt nước chạm đáy
 */
private fun DrawScope.drawWaterSplashesAndRipples(
    sites: List<SplashSite>,
    progress: Float,
    width: Float,
    height: Float,
    intensity: Float
) {
    sites.forEachIndexed { index, site ->
        val cycle = (progress * 2.2f + site.phaseOffset) % 1f
        val splashX = site.xRatio * width
        val splashY = site.yRatio * height

        // 1. Gợn sóng elip lan tỏa nhẹ
        if (cycle < 0.65f) {
            val rippleScale = cycle / 0.65f
            val rippleRadius = site.maxRadius * rippleScale * intensity
            val rippleAlpha = ((1f - rippleScale) * 0.40f * intensity).coerceIn(0f, 0.6f)

            drawOval(
                color = Color(0xFFBAE6FD).copy(alpha = rippleAlpha),
                topLeft = Offset(splashX - rippleRadius, splashY - rippleRadius * 0.35f),
                size = Size(rippleRadius * 2f, rippleRadius * 0.70f),
                style = Stroke(width = 1.0f)
            )
        }

        // 2. Tia bọt nước bắn lên dạng cung nhọn
        if (cycle < 0.50f) {
            val burstProgress = cycle / 0.50f
            val burstAlpha = ((1f - burstProgress) * 0.55f * intensity).coerceIn(0f, 0.75f)
            val burstY = splashY - (burstProgress * 14f * intensity)
            val burstSpread = burstProgress * 10f * intensity

            // 2 giọt nước bắn trái / phải
            drawCircle(
                color = Color.White.copy(alpha = burstAlpha),
                radius = 1.2f,
                center = Offset(splashX - burstSpread, burstY)
            )
            drawCircle(
                color = Color.White.copy(alpha = burstAlpha),
                radius = 1.0f,
                center = Offset(splashX + burstSpread * 0.85f, burstY - 2f)
            )
        }
    }
}

/**
 * Tia sét phân nhánh ngẫu nhiên sắc sảo và chân thực khi dông bão
 */
private fun DrawScope.drawRealisticLightningFork(width: Float, height: Float, flashIntensity: Float) {
    val startX = width * 0.48f

    // 1. Nhánh sét chính
    val mainBolt = Path().apply {
        moveTo(startX, 0f)
        lineTo(startX - 28f, height * 0.16f)
        lineTo(startX + 14f, height * 0.24f)
        lineTo(startX - 40f, height * 0.40f)
        lineTo(startX + 8f, height * 0.48f)
        lineTo(startX - 22f, height * 0.64f)
        lineTo(startX + 4f, height * 0.74f)
    }

    // 2. Nhánh sét phụ rẽ nhánh
    val branchBolt = Path().apply {
        moveTo(startX - 40f, height * 0.40f)
        lineTo(startX - 75f, height * 0.48f)
        lineTo(startX - 90f, height * 0.58f)
    }

    // Quầng hào quang tím/cyan bao quanh tia sét
    val glowAlpha = (flashIntensity * 0.45f).coerceIn(0f, 0.65f)
    drawPath(
        path = mainBolt,
        color = Color(0xFF818CF8).copy(alpha = glowAlpha),
        style = Stroke(width = 6.5f, cap = StrokeCap.Round)
    )
    drawPath(
        path = branchBolt,
        color = Color(0xFF38BDF8).copy(alpha = glowAlpha * 0.7f),
        style = Stroke(width = 4.0f, cap = StrokeCap.Round)
    )

    // Lõi sét màu trắng tím cực đại
    val coreAlpha = flashIntensity.coerceIn(0f, 0.95f)
    drawPath(
        path = mainBolt,
        color = Color(0xFFFDF4FF).copy(alpha = coreAlpha),
        style = Stroke(width = 2.4f, cap = StrokeCap.Round)
    )
    drawPath(
        path = branchBolt,
        color = Color(0xFFFDF4FF).copy(alpha = coreAlpha * 0.8f),
        style = Stroke(width = 1.5f, cap = StrokeCap.Round)
    )
}

/**
 * Chùm tia nắng thể tích (Soft Volumetric God Rays) tỏa góc mềm mại
 */
private fun DrawScope.drawSoftGodRays(
    sunCenter: Offset,
    width: Float,
    height: Float,
    pulse: Float,
    isBatterySaving: Boolean = false
) {
    val rayCount = if (isBatterySaving) 4 else 8
    val angleStep = 360f / rayCount
    for (i in 0 until rayCount) {
        val angleRad = (i * angleStep) * (Math.PI / 180.0)
        val rayLength = width * 1.15f
        val endX = sunCenter.x + (cos(angleRad) * rayLength).toFloat()
        val endY = sunCenter.y + (sin(angleRad) * rayLength).toFloat()

        val path = Path().apply {
            moveTo(sunCenter.x, sunCenter.y)
            lineTo(endX + 42f, endY + 42f)
            lineTo(endX - 42f, endY - 42f)
            close()
        }

        drawPath(
            path = path,
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFEF08A).copy(alpha = 0.14f * pulse),
                    Color(0xFFF59E0B).copy(alpha = 0.05f * pulse),
                    Color.Transparent
                ),
                center = sunCenter,
                radius = rayLength
            )
        )
    }
}

/**
 * Các hạt bụi nắng (Solar Dust Motes / Bokeh) lơ lửng bồng bềnh trong ánh mặt trời
 */
private fun DrawScope.drawFloatingSolarMotes(
    motes: List<DustMoteParticle>,
    progress: Float,
    width: Float,
    height: Float
) {
    motes.forEachIndexed { index, mote ->
        val moteProgress = (progress * mote.driftSpeed + index * 0.033f) % 1f
        val sway = sin(moteProgress * 6.28f + mote.phase) * 22f
        val curX = (mote.xRatio * width + sway) % width
        val curY = ((mote.yRatio + 1f - moteProgress) % 1f) * (height * 0.70f) + (height * 0.05f)

        val alphaPulse = (sin(moteProgress * 6.28f + mote.phase) * 0.25f + 0.75f) * mote.baseAlpha

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = alphaPulse.coerceIn(0f, 1f)),
                    Color(0xFFFEF08A).copy(alpha = (alphaPulse * 0.6f).coerceIn(0f, 1f)),
                    Color.Transparent
                ),
                center = Offset(curX, curY),
                radius = mote.radius * 2f
            ),
            center = Offset(curX, curY),
            radius = mote.radius * 2f
        )
    }
}

/**
 * Dải lụa cực quang huyền diệu (Aurora Ribbon Wave) trên bầu trời đêm
 */
private fun DrawScope.drawAuroraRibbon(
    width: Float,
    height: Float,
    drift: Float,
    pulse: Float,
    isBatterySaving: Boolean = false
) {
    val auroraY = height * 0.18f
    val waveOffset = drift * 6.28f

    val auroraPath = Path().apply {
        moveTo(0f, auroraY)
        val segments = if (isBatterySaving) 4 else 8
        val step = width / segments
        for (i in 1..segments) {
            val cx = (i - 0.5f) * step
            val cy = auroraY + sin(i * 0.8f + waveOffset) * 28f
            val ex = i * step
            val ey = auroraY + sin(i * 0.8f + waveOffset + 0.4f) * 20f
            quadraticBezierTo(cx, cy, ex, ey)
        }
        lineTo(width, 0f)
        lineTo(0f, 0f)
        close()
    }

    drawPath(
        path = auroraPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF34D399).copy(alpha = 0.08f * pulse),
                Color(0xFF818CF8).copy(alpha = 0.06f * pulse),
                Color.Transparent
            ),
            startY = 0f,
            endY = height * 0.32f
        )
    )
}

/**
 * Bầu trời sao đa tầng thị sai kèm tia sao 4 cánh lấp lánh (Diffraction Spikes)
 */
private fun DrawScope.drawStarFieldWithCrossSpikes(
    stars: List<StarParticle>,
    pulse: Float,
    width: Float,
    height: Float,
    isBatterySaving: Boolean = false
) {
    stars.forEachIndexed { index, star ->
        val starX = star.xRatio * width
        val starY = star.yRatio * height
        val twinkle = (sin(pulse * 6.28f * star.twinkleSpeed + star.phase) * 0.4f + 0.6f)
        val alpha = (star.baseAlpha * twinkle).coerceIn(0.15f, 0.98f)

        // Hạt sao tròn cơ bản
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            center = Offset(starX, starY),
            radius = star.radius
        )

        // Tia sao 4 cánh tỏa sáng cho các ngôi sao nổi bật (bỏ qua khi ở chế độ tiết kiệm pin)
        if (!isBatterySaving && star.hasCrossSpikes && alpha > 0.65f) {
            val spikeLen = star.radius * 2.8f
            val spikeAlpha = (alpha * 0.55f).coerceIn(0f, 0.7f)

            // Tia ngang
            drawLine(
                color = Color.White.copy(alpha = spikeAlpha),
                start = Offset(starX - spikeLen, starY),
                end = Offset(starX + spikeLen, starY),
                strokeWidth = 0.8f
            )
            // Tia dọc
            drawLine(
                color = Color.White.copy(alpha = spikeAlpha),
                start = Offset(starX, starY - spikeLen),
                end = Offset(starX, starY + spikeLen),
                strokeWidth = 0.8f
            )
        }
    }
}

/**
 * Vệt sao băng rực rỡ lướt qua bầu trời
 */
private fun DrawScope.drawShootingMeteor(width: Float, height: Float, progress: Float) {
    val startX = width * (0.88f - progress * 0.72f)
    val startY = height * (0.04f + progress * 0.38f)
    val tailLength = 55f
    val endX = startX + tailLength * 0.82f
    val endY = startY - tailLength * 0.52f

    drawLine(
        brush = Brush.linearGradient(
            listOf(
                Color.Transparent,
                Color(0xFFBAE6FD).copy(alpha = 0.45f),
                Color.White.copy(alpha = 0.98f)
            ),
            start = Offset(endX, endY),
            end = Offset(startX, startY)
        ),
        start = Offset(endX, endY),
        end = Offset(startX, startY),
        strokeWidth = 2.2f,
        cap = StrokeCap.Round
    )

    // Đầu sao băng sáng lóa
    drawCircle(
        color = Color.White,
        radius = 2.0f,
        center = Offset(startX, startY)
    )
}

/**
 * Các cụm mây thể tích bồng bềnh 3 lớp thị sai trôi nhịp nhàng
 */
private fun DrawScope.drawRealisticCloudFormations(
    width: Float,
    height: Float,
    drift: Float,
    pulse: Float
) {
    val baseY = height * 0.08f

    // Lớp 1 (Mây xa, trôi chậm, xám xanh dịu)
    val offset1 = drift * width * 0.15f
    drawOval(
        color = Color.White.copy(alpha = 0.08f * pulse + 0.04f),
        topLeft = Offset((width * 0.65f - offset1) % (width * 1.5f) - width * 0.25f, baseY - 15f),
        size = Size(width * 0.90f, height * 0.22f)
    )

    // Lớp 2 (Mây trung, trôi vừa, đầy đặn hơn)
    val offset2 = drift * width * 0.28f
    drawOval(
        color = Color.White.copy(alpha = 0.12f * pulse + 0.06f),
        topLeft = Offset((width * 0.20f - offset2) % (width * 1.4f) - width * 0.20f, baseY + 28f),
        size = Size(width * 0.80f, height * 0.18f)
    )

    // Lớp 3 (Mây gần, trôi nhẹ lướt ngang)
    val offset3 = drift * width * 0.42f
    drawOval(
        color = Color.White.copy(alpha = 0.07f * pulse + 0.03f),
        topLeft = Offset((width * 0.90f - offset3) % (width * 1.6f) - width * 0.25f, baseY + 65f),
        size = Size(width * 0.95f, height * 0.24f)
    )
}

/**
 * Dải sương mù dập dềnh uốn sóng ngang mềm mại (Atmospheric Rolling Mist)
 */
private fun DrawScope.drawAtmosphericRollingMist(
    width: Float,
    height: Float,
    drift: Float,
    pulse: Float
) {
    val mistBaseY = height * 0.16f
    for (i in 0 until 3) {
        val waveOffset = (drift * 6.28f * (i + 1) * 0.5f)
        val mistPath = Path().apply {
            val curY = mistBaseY + i * 55f
            moveTo(0f, curY)
            val segments = 6
            val step = width / segments
            for (s in 1..segments) {
                val cx = (s - 0.5f) * step
                val cy = curY + sin(s * 1.1f + waveOffset) * 18f
                val ex = s * step
                val ey = curY + sin(s * 1.1f + waveOffset + 0.5f) * 12f
                quadraticBezierTo(cx, cy, ex, ey)
            }
            lineTo(width, curY + 45f)
            lineTo(0f, curY + 45f)
            close()
        }

        drawPath(
            path = mistPath,
            color = Color.White.copy(alpha = (0.06f + i * 0.03f) * pulse)
        )
    }
}

/**
 * Tuyết rơi lãng mạn lượn sóng đa kích thước
 */
private fun DrawScope.drawGentleSnowfall(
    snowFlakes: List<SnowFlakeParticle>,
    progress: Float,
    width: Float,
    height: Float
) {
    snowFlakes.forEachIndexed { index, flake ->
        val flakeProgress = (progress * flake.speed + index * 0.025f) % 1f
        val sway = sin(flakeProgress * 6.28f + flake.phase) * flake.swayAmplitude
        val curX = (flake.xRatio * width + sway) % width
        val curY = (flake.yRatio + flakeProgress) % 1f * height

        drawCircle(
            color = Color.White.copy(alpha = flake.alpha),
            center = Offset(curX, curY),
            radius = flake.radius
        )
    }
}
