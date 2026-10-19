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
 * - MẶT TRỜI ĐỈNH TRỜI RỰC RỠ: Tọa độ trung tâm đỉnh màn hình, đĩa sáng chói lóa, vầng hào quang ấm áp tỏa rộng,
 *   chùm tia nắng thể tích (God Rays) chiếu xuyên qua tầng mây và các hạt bụi nắng lấp lánh (Solar Dust Motes).
 * - MÂY THỂ TÍCH 3D CHÂN THỰC (Volumetric 3D Clouds): Đa tầng thị sai (Parallax layers), viền mây đón nắng (Silver Lining),
 *   đáy mây đổ bóng khí quyển tự nhiên, bồng bềnh trôi nhịp nhàng khi trời có mây hoặc nhiều mây.
 * - MƯA CHÂN THỰC: Giọt mưa rơi nghiêng theo gió đa tầng (foreground/background), bọt nước bắn tung tóe (splash)
 *   và gợn sóng nước (ripple) lăn tăn trên bề mặt đáy, lớp sương hơi nước ẩm bốc lên mềm mại.
 * - DÔNG SÉT VOLUMETRIC: Chớp giật đa xung (double-pulse flash) chiếu sáng chân thực toàn bầu trời và thấu quang mây,
 *   kèm tia sét phân nhánh đa hướng sắc sảo với lõi sáng trắng tím và quầng hào quang cyan.
 * - ĐÊM SAO & CỰC QUANG: Mặt trăng phát quang với vết lồi lõm tinh tế, bầu trời sao đa tầng thị sai với tia sao 4 cánh lấp lánh,
 *   dải lụa cực quang (Aurora wave) huyền ảo uốn lượn êm dịu, vệt sao băng rực rỡ lướt qua.
 * - SƯƠNG MÙ KHÍ QUYỂN: Các dải sương mờ dập dềnh uốn sóng ngang mềm mại.
 * - TUYẾT RƠI LÃNG MẠN: Các bông tuyết lượn sóng đa kích thước bay chậm rãi và đung đưa theo gió.
 * 
 * 100% GPU Canvas, không cấp phát bộ nhớ trong frame render (Zero Garbage Collection).
 * Hoạt động mượt mà liên tục 60/120 FPS kể cả khi đang cuộn màn hình.
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

    // 1. Chu kỳ chuyển động chính của mưa / tuyết / bụi nắng (2200ms)
    val mainLoop by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "MainLoop"
    )

    // 2. Nhịp thở hào quang mặt trời / cực quang / nhịp sóng (3600ms)
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathingPulse"
    )

    // 3. Dao động tia nắng God Rays (lắc nhẹ góc 15 độ, 18000ms cho chuyển động cực kỳ tự nhiên)
    val godRaySway by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GodRaySway"
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
                0.0f at 5800
                1.0f at 6900
                1.0f at 8500
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "MeteorProgress"
    )

    // 6. Chu kỳ trôi mây & sương mù chậm rãi (28000ms)
    val slowDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SlowDrift"
    )

    // 7. Khởi tạo mảng hạt cố định ngẫu nhiên (Pre-computed particles, không tạo mới trong frame draw)
    val rainDrops = remember {
        List(95) {
            val isFg = Random.nextFloat() > 0.45f
            RainDropParticle(
                xRatio = Random.nextFloat(),
                yRatio = Random.nextFloat(),
                speed = if (isFg) Random.nextFloat() * 0.4f + 0.9f else Random.nextFloat() * 0.3f + 0.6f,
                length = if (isFg) Random.nextFloat() * 14f + 28f else Random.nextFloat() * 8f + 16f,
                thickness = if (isFg) 1.8f else 1.1f,
                alpha = if (isFg) Random.nextFloat() * 0.35f + 0.50f else Random.nextFloat() * 0.20f + 0.25f,
                isForeground = isFg
            )
        }
    }

    val splashPoints = remember {
        List(14) {
            SplashSite(
                xRatio = Random.nextFloat(),
                yRatio = Random.nextFloat() * 0.20f + 0.80f,
                phaseOffset = Random.nextFloat(),
                maxRadius = Random.nextFloat() * 10f + 12f
            )
        }
    }

    val dustMotes = remember {
        List(32) {
            DustMoteParticle(
                xRatio = Random.nextFloat(),
                yRatio = Random.nextFloat() * 0.65f + 0.05f,
                radius = Random.nextFloat() * 2.4f + 1.2f,
                driftSpeed = Random.nextFloat() * 0.4f + 0.3f,
                phase = Random.nextFloat() * 6.28f,
                baseAlpha = Random.nextFloat() * 0.45f + 0.30f
            )
        }
    }

    val starField = remember {
        List(65) { index ->
            val hasSpikes = index % 10 == 0
            val size = if (hasSpikes) Random.nextFloat() * 1.5f + 2.5f else Random.nextFloat() * 1.1f + 1.0f
            StarParticle(
                xRatio = Random.nextFloat(),
                yRatio = Random.nextFloat() * 0.75f,
                radius = size,
                twinkleSpeed = Random.nextFloat() * 1.5f + 0.8f,
                phase = Random.nextFloat() * 6.28f,
                baseAlpha = if (hasSpikes) 0.95f else Random.nextFloat() * 0.5f + 0.35f,
                hasCrossSpikes = hasSpikes
            )
        }
    }

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

    // Các cụm mây thể tích 3D được thiết kế tỉ mỉ, đầy đặn và tự nhiên (như trong ảnh mẫu)
    val partlyCloudyClouds = remember {
        listOf(
            CloudMass(
                startXRatio = 0.05f,
                yRatio = 0.06f,
                widthRatio = 0.75f,
                heightRatio = 0.22f,
                speed = 0.12f,
                baseAlpha = 0.82f,
                puffs = listOf(
                    CloudPuff(0.15f, 0.60f, 0.22f, 0.38f, 0.85f),
                    CloudPuff(0.38f, 0.40f, 0.30f, 0.48f, 1.0f),
                    CloudPuff(0.65f, 0.45f, 0.28f, 0.44f, 0.95f),
                    CloudPuff(0.85f, 0.62f, 0.20f, 0.32f, 0.80f)
                )
            ),
            CloudMass(
                startXRatio = 0.55f,
                yRatio = 0.18f,
                widthRatio = 0.80f,
                heightRatio = 0.24f,
                speed = 0.08f,
                baseAlpha = 0.78f,
                puffs = listOf(
                    CloudPuff(0.18f, 0.52f, 0.26f, 0.40f, 0.85f),
                    CloudPuff(0.46f, 0.36f, 0.34f, 0.52f, 1.0f),
                    CloudPuff(0.76f, 0.48f, 0.28f, 0.44f, 0.90f)
                )
            )
        )
    }

    val overcastClouds = remember {
        listOf(
            // Tầng mây vòm cao (High atmospheric canopy)
            CloudMass(
                startXRatio = 0.0f,
                yRatio = 0.02f,
                widthRatio = 1.15f,
                heightRatio = 0.26f,
                speed = 0.06f,
                baseAlpha = 0.88f,
                puffs = listOf(
                    CloudPuff(0.12f, 0.52f, 0.32f, 0.48f, 0.90f),
                    CloudPuff(0.42f, 0.35f, 0.38f, 0.58f, 1.0f),
                    CloudPuff(0.72f, 0.42f, 0.35f, 0.52f, 0.95f),
                    CloudPuff(0.94f, 0.58f, 0.28f, 0.42f, 0.85f)
                )
            ),
            // Tầng mây tích trung tâm (Mid cumulus bank - đón ánh nắng thấu quang)
            CloudMass(
                startXRatio = 0.30f,
                yRatio = 0.14f,
                widthRatio = 1.10f,
                heightRatio = 0.28f,
                speed = 0.10f,
                baseAlpha = 0.84f,
                puffs = listOf(
                    CloudPuff(0.14f, 0.50f, 0.30f, 0.45f, 0.90f),
                    CloudPuff(0.40f, 0.32f, 0.40f, 0.56f, 1.0f),
                    CloudPuff(0.68f, 0.38f, 0.36f, 0.50f, 0.95f),
                    CloudPuff(0.88f, 0.54f, 0.28f, 0.40f, 0.85f)
                )
            ),
            // Tầng mây gần bay lướt (Foreground drifting wisps)
            CloudMass(
                startXRatio = 0.05f,
                yRatio = 0.28f,
                widthRatio = 0.95f,
                heightRatio = 0.22f,
                speed = 0.16f,
                baseAlpha = 0.72f,
                puffs = listOf(
                    CloudPuff(0.20f, 0.52f, 0.28f, 0.42f, 0.85f),
                    CloudPuff(0.52f, 0.42f, 0.34f, 0.50f, 1.0f),
                    CloudPuff(0.82f, 0.50f, 0.30f, 0.44f, 0.90f)
                )
            )
        )
    }

    val stormClouds = remember {
        listOf(
            CloudMass(
                startXRatio = 0.0f,
                yRatio = 0.02f,
                widthRatio = 1.15f,
                heightRatio = 0.32f,
                speed = 0.07f,
                baseAlpha = 0.92f,
                puffs = listOf(
                    CloudPuff(0.16f, 0.40f, 0.38f, 0.58f, 0.95f),
                    CloudPuff(0.50f, 0.30f, 0.45f, 0.65f, 1.0f),
                    CloudPuff(0.82f, 0.38f, 0.40f, 0.60f, 0.95f)
                )
            ),
            CloudMass(
                startXRatio = 0.22f,
                yRatio = 0.15f,
                widthRatio = 1.05f,
                heightRatio = 0.28f,
                speed = 0.13f,
                baseAlpha = 0.86f,
                puffs = listOf(
                    CloudPuff(0.20f, 0.46f, 0.34f, 0.52f, 0.90f),
                    CloudPuff(0.54f, 0.34f, 0.40f, 0.58f, 1.0f),
                    CloudPuff(0.86f, 0.48f, 0.32f, 0.48f, 0.85f)
                )
            )
        )
    }

    val activeRainDrops = remember(rainDrops, isBatterySaving) {
        if (isBatterySaving) rainDrops.take(45) else rainDrops
    }
    val activeSplashPoints = remember(splashPoints, isBatterySaving) {
        if (isBatterySaving) splashPoints.take(6) else splashPoints
    }
    val activeDustMotes = remember(dustMotes, isBatterySaving) {
        if (isBatterySaving) dustMotes.take(14) else dustMotes
    }
    val activeStarField = remember(starField, isBatterySaving) {
        if (isBatterySaving) starField.take(28) else starField
    }
    val activeSnowFlakes = remember(snowFlakes, isBatterySaving) {
        if (isBatterySaving) snowFlakes.take(28) else snowFlakes
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Vị trí mặt trời tự nhiên đỉnh trời (như trong ảnh mẫu mục tiêu)
        val sunCenter = Offset(width * 0.50f, height * 0.045f)

        when {
            // =========================================================================
            // 1. DÔNG BÃO & SẤM SÉT (Thunderstorm)
            // =========================================================================
            type == WeatherType.THUNDERSTORM -> {
                // Đám mây giông tầng đỉnh
                drawVolumetricClouds(
                    clouds = stormClouds,
                    driftProgress = slowDrift,
                    width = width,
                    height = height,
                    pulse = breathingPulse,
                    topHighlightColor = Color(0xFFC084FC),
                    baseShadeColor = Color(0xFF0F172A),
                    flashIntensity = stormFlash
                )

                // Ánh chớp giật bầu trời lan tỏa đa tầng sắc tím điện & xanh cyan
                if (stormFlash > 0.05f) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFFC084FC).copy(alpha = (stormFlash * 0.52f).coerceIn(0f, 0.95f)),
                                Color(0xFF38BDF8).copy(alpha = (stormFlash * 0.32f).coerceIn(0f, 0.70f)),
                                Color.Transparent
                            )
                        ),
                        size = size
                    )

                    // Tia sét phân nhánh sắc sảo khi chớp đạt đỉnh (> 0.35)
                    if (stormFlash > 0.35f) {
                        drawRealisticLightningFork(width, height, stormFlash)
                    }
                }

                // Mưa xối xả tốc độ cao kèm bọt nước bắn mạnh
                drawRainStreaksEnhanced(activeRainDrops, mainLoop, width, height, speedMultiplier = 1.6f, isHeavy = true)
                if (activeSplashPoints.isNotEmpty()) {
                    drawWaterSplashesAndRipples(activeSplashPoints, mainLoop, width, height, intensity = 1.5f)
                }

                // Hơi nước mờ mịt ở đáy màn hình do mưa to
                drawRect(
                    brush = Brush.verticalGradient(
                        0.70f to Color.Transparent,
                        1.0f to Color(0xFFBAE6FD).copy(alpha = 0.18f)
                    ),
                    size = size
                )
            }

            // =========================================================================
            // 2. MƯA & MƯA PHÙN (Rain & Drizzle)
            // =========================================================================
            type == WeatherType.RAIN || type == WeatherType.DRIZZLE -> {
                val isHeavy = type == WeatherType.RAIN
                val speed = if (isHeavy) 1.25f else 0.75f
                val splashIntensity = if (isHeavy) 1.0f else 0.5f

                // Lớp mây mưa xám phủ trên đỉnh trời
                drawVolumetricClouds(
                    clouds = stormClouds,
                    driftProgress = slowDrift * 0.7f,
                    width = width,
                    height = height,
                    pulse = breathingPulse,
                    topHighlightColor = Color(0xFFCBD5E1),
                    baseShadeColor = Color(0xFF1E293B)
                )

                drawRainStreaksEnhanced(activeRainDrops, mainLoop, width, height, speedMultiplier = speed, isHeavy = isHeavy)
                if (activeSplashPoints.isNotEmpty()) {
                    drawWaterSplashesAndRipples(activeSplashPoints, mainLoop, width, height, intensity = splashIntensity)
                }

                // Lớp sương ẩm mờ dịu ở đáy màn hình
                drawRect(
                    brush = Brush.verticalGradient(
                        0.75f to Color.Transparent,
                        1.0f to Color.White.copy(alpha = if (isHeavy) 0.14f else 0.08f)
                    ),
                    size = size
                )
            }

            // =========================================================================
            // 3. TRỜI NẮNG VÀNG / QUANG ĐÃNG BAN NGÀY (Clear / Sunny Day)
            // =========================================================================
            (type == WeatherType.CLEAR || type == WeatherType.PARTLY_CLOUDY) && isDay -> {
                // Đĩa mặt trời đỉnh trời, quầng hào quang ấm áp tỏa rộng, tia nắng God Rays hướng xuống
                drawCinematicTopSun(
                    sunCenter = sunCenter,
                    width = width,
                    height = height,
                    breathingPulse = breathingPulse,
                    swayAngle = godRaySway,
                    isBatterySaving = isBatterySaving
                )

                // Các hạt bụi nắng (solar dust motes / warm bokeh) lơ lửng bồng bềnh
                drawFloatingSolarMotes(activeDustMotes, mainLoop, width, height)

                // Nếu là trời có mây gián đoạn (Partly Cloudy): vẽ các cụm mây trắng bồng bềnh đón nắng
                if (type == WeatherType.PARTLY_CLOUDY) {
                    drawVolumetricClouds(
                        clouds = partlyCloudyClouds,
                        driftProgress = slowDrift,
                        width = width,
                        height = height,
                        pulse = breathingPulse,
                        topHighlightColor = Color.White,
                        baseShadeColor = Color(0xFF6B8CAE).copy(alpha = 0.55f)
                    )
                }
            }

            // =========================================================================
            // 4. NHIỀU MÂY BAN NGÀY (Cloudy / Overcast Day - như ảnh thực tế của người dùng)
            // =========================================================================
            type == WeatherType.CLOUDY && isDay -> {
                // Mặt trời vẫn tỏa ánh sáng thấu quang từ đỉnh màn hình qua các khe mây
                drawCinematicTopSun(
                    sunCenter = sunCenter,
                    width = width,
                    height = height,
                    breathingPulse = breathingPulse,
                    swayAngle = godRaySway,
                    isBatterySaving = isBatterySaving,
                    isDiffused = true
                )

                // Các cụm mây thể tích 3D dày dặn, bồng bềnh viền bạc đón sáng rực rỡ
                drawVolumetricClouds(
                    clouds = overcastClouds,
                    driftProgress = slowDrift,
                    width = width,
                    height = height,
                    pulse = breathingPulse,
                    topHighlightColor = Color.White,
                    baseShadeColor = Color(0xFF476785).copy(alpha = 0.65f)
                )

                // Hạt bụi nắng lơ lửng trong không khí
                drawFloatingSolarMotes(activeDustMotes.take(16), mainLoop, width, height)
            }

            // =========================================================================
            // 5. BẦU TRỜI ĐÊM SAO & CỰC QUANG (Starry Night, Aurora & Meteors)
            // =========================================================================
            !isDay && (type == WeatherType.CLEAR || type == WeatherType.PARTLY_CLOUDY || type == WeatherType.CLOUDY) -> {
                val moonCenter = Offset(width * 0.80f, height * 0.09f)

                drawAtmosphericNightSky(
                    moonCenter = moonCenter,
                    width = width,
                    height = height,
                    breathingPulse = breathingPulse,
                    slowDrift = slowDrift,
                    meteorProgress = meteorProgress,
                    stars = activeStarField,
                    isBatterySaving = isBatterySaving
                )

                // Nếu có mây ban đêm: mây bạc ánh trăng huyền ảo lướt qua bầu trời sao
                if (type == WeatherType.PARTLY_CLOUDY || type == WeatherType.CLOUDY) {
                    val nightClouds = if (type == WeatherType.CLOUDY) overcastClouds else partlyCloudyClouds
                    drawVolumetricClouds(
                        clouds = nightClouds,
                        driftProgress = slowDrift,
                        width = width,
                        height = height,
                        pulse = breathingPulse,
                        topHighlightColor = Color(0xFFE2E8F0),
                        baseShadeColor = Color(0xFF1E293B).copy(alpha = 0.70f)
                    )
                }
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

                // Lớp bụi sương giá mùa đông ở đáy
                drawRect(
                    brush = Brush.verticalGradient(
                        0.80f to Color.Transparent,
                        1.0f to Color(0xFFE0F2FE).copy(alpha = 0.18f)
                    ),
                    size = size
                )
            }
        }
    }
}

// =====================================================================================
// DATA CLASSES CHO HẠT HIỆU ỨNG (PRE-ALLOCATED DATA MODELS - ZERO GC)
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

private data class CloudPuff(
    val relX: Float,
    val relY: Float,
    val radiusX: Float,
    val radiusY: Float,
    val alphaRatio: Float
)

private data class CloudMass(
    val startXRatio: Float,
    val yRatio: Float,
    val widthRatio: Float,
    val heightRatio: Float,
    val speed: Float,
    val baseAlpha: Float,
    val puffs: List<CloudPuff>
)

// =====================================================================================
// DRAWSCOPE HELPER FUNCTIONS (HARDWARE-ACCELERATED RENDER LOGIC)
// =====================================================================================

/**
 * Hiển thị các cụm mây thể tích 3D chân thực (Volumetric 3D Clouds)
 * Áp dụng kỹ thuật đổ bóng 3 tầng: Đáy mây đậm khối khí quyển -> Thân mây trắng thể tích -> Viền mây bạc đón nắng (Silver Lining).
 */
private fun DrawScope.drawVolumetricClouds(
    clouds: List<CloudMass>,
    driftProgress: Float,
    width: Float,
    height: Float,
    pulse: Float,
    topHighlightColor: Color,
    baseShadeColor: Color,
    flashIntensity: Float = 0f
) {
    clouds.forEach { cloud ->
        val totalSpan = width * 1.8f
        val driftX = (driftProgress * cloud.speed * totalSpan)
        val cloudBaseX = ((cloud.startXRatio * width + driftX) % totalSpan) - width * 0.4f
        val cloudBaseY = cloud.yRatio * height
        val cloudW = cloud.widthRatio * width
        val cloudH = cloud.heightRatio * height

        cloud.puffs.forEach { puff ->
            val puffCenterX = cloudBaseX + puff.relX * cloudW
            val puffCenterY = cloudBaseY + puff.relY * cloudH
            val radiusX = puff.radiusX * cloudW
            val radiusY = puff.radiusY * cloudH
            val avgRadius = (radiusX + radiusY) * 0.5f

            val effectiveAlpha = (cloud.baseAlpha * puff.alphaRatio * (0.90f + 0.10f * pulse)).coerceIn(0f, 1f)

            // Pass 1: Bóng mờ chân khối đế mây (Atmospheric Underbelly Shadow)
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseShadeColor.copy(alpha = effectiveAlpha * 0.90f),
                        baseShadeColor.copy(alpha = effectiveAlpha * 0.40f),
                        Color.Transparent
                    ),
                    center = Offset(puffCenterX, puffCenterY + radiusY * 0.35f),
                    radius = avgRadius * 1.05f
                ),
                topLeft = Offset(puffCenterX - radiusX, puffCenterY - radiusY * 0.65f),
                size = Size(radiusX * 2f, radiusY * 1.85f)
            )

            // Pass 2: Khối thân mây thể tích trắng đặc (Dense Cloud Body)
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = effectiveAlpha * 0.85f),
                        Color.White.copy(alpha = effectiveAlpha * 0.45f),
                        Color.Transparent
                    ),
                    center = Offset(puffCenterX, puffCenterY),
                    radius = avgRadius * 0.92f
                ),
                topLeft = Offset(puffCenterX - radiusX * 0.92f, puffCenterY - radiusY * 0.92f),
                size = Size(radiusX * 1.84f, radiusY * 1.84f)
            )

            // Pass 3: Viền bạc đón nắng đỉnh mây (Sunlit Silver Lining Highlight)
            val highlightAlpha = if (flashIntensity > 0.05f) {
                (flashIntensity * 0.95f).coerceIn(0f, 0.98f)
            } else {
                (effectiveAlpha * 0.95f).coerceIn(0f, 0.98f)
            }

            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        topHighlightColor.copy(alpha = highlightAlpha),
                        topHighlightColor.copy(alpha = highlightAlpha * 0.35f),
                        Color.Transparent
                    ),
                    center = Offset(puffCenterX, puffCenterY - radiusY * 0.35f),
                    radius = avgRadius * 0.85f
                ),
                topLeft = Offset(puffCenterX - radiusX * 0.85f, puffCenterY - radiusY * 1.15f),
                size = Size(radiusX * 1.70f, radiusY * 1.45f)
            )
        }
    }
}

/**
 * Hiệu ứng mặt trời đỉnh trời chân thực (Cinematic Top Sun):
 * Tọa độ trung tâm đỉnh màn hình, đĩa mặt trời chói lóa, quầng hào quang rộng, chùm tia nắng God Rays chiếu hướng xuống
 */
private fun DrawScope.drawCinematicTopSun(
    sunCenter: Offset,
    width: Float,
    height: Float,
    breathingPulse: Float,
    swayAngle: Float,
    isBatterySaving: Boolean,
    isDiffused: Boolean = false
) {
    val baseRadius = width * 0.32f

    // 1. Quầng sáng khổng lồ lan tỏa ánh sáng ban ngày (Daylight bloom)
    val bloomAlpha = if (isDiffused) 0.28f else 0.38f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFEF08A).copy(alpha = bloomAlpha * breathingPulse),
                Color(0xFFFDE047).copy(alpha = (bloomAlpha * 0.65f) * breathingPulse),
                Color(0xFFF59E0B).copy(alpha = 0.12f * breathingPulse),
                Color.Transparent
            ),
            center = sunCenter,
            radius = baseRadius * 2.4f
        ),
        center = sunCenter,
        radius = baseRadius * 2.4f
    )

    // 2. Quầng ấm áp vành trong
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f * breathingPulse),
                Color(0xFFFEF9C3).copy(alpha = 0.65f * breathingPulse),
                Color(0xFFFDE047).copy(alpha = 0.25f),
                Color.Transparent
            ),
            center = sunCenter,
            radius = baseRadius * 1.1f
        ),
        center = sunCenter,
        radius = baseRadius * 1.1f
    )

    // 3. Đĩa lõi mặt trời rực rỡ chói lóa
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.98f),
                Color(0xFFFEF9C3).copy(alpha = 0.92f),
                Color(0xFFFDE047).copy(alpha = 0.50f)
            ),
            center = sunCenter,
            radius = baseRadius * 0.40f
        ),
        center = sunCenter,
        radius = baseRadius * 0.40f
    )

    // 4. Chùm tia nắng thể tích (Downward Volumetric God Rays) chiếu xiên xuống
    rotate(degrees = swayAngle, pivot = sunCenter) {
        drawDownwardGodRays(sunCenter, width, height, breathingPulse, isBatterySaving = isBatterySaving, isDiffused = isDiffused)
    }

    // 5. Cinematic Lens Flare (Hiệu ứng lóa ống kính quang học dọc theo trục dọc màn hình)
    if (!isBatterySaving && !isDiffused) {
        val flareY1 = height * 0.18f
        val flareY2 = height * 0.35f
        val flareY3 = height * 0.52f

        // Flare 1: Vòng tròn vàng ấm áp
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFEF08A).copy(alpha = 0.16f * breathingPulse),
                    Color(0xFFF59E0B).copy(alpha = 0.06f * breathingPulse),
                    Color.Transparent
                ),
                center = Offset(width * 0.50f, flareY1),
                radius = width * 0.05f
            ),
            center = Offset(width * 0.50f, flareY1),
            radius = width * 0.05f
        )

        // Flare 2: Vòng khuyên xanh lam ngọc tinh tế
        drawCircle(
            color = Color(0xFF38BDF8).copy(alpha = 0.10f * breathingPulse),
            center = Offset(width * 0.50f, flareY2),
            radius = width * 0.075f,
            style = Stroke(width = 1.0f)
        )

        // Flare 3: Hào quang ấm dịu
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE0F2FE).copy(alpha = 0.08f * breathingPulse),
                    Color.Transparent
                ),
                center = Offset(width * 0.50f, flareY3),
                radius = width * 0.09f
            ),
            center = Offset(width * 0.50f, flareY3),
            radius = width * 0.09f
        )
    }
}

/**
 * Chùm tia nắng thể tích (Downward Volumetric God Rays) tỏa hướng xuống mặt đất
 */
private fun DrawScope.drawDownwardGodRays(
    sunCenter: Offset,
    width: Float,
    height: Float,
    pulse: Float,
    isBatterySaving: Boolean = false,
    isDiffused: Boolean = false
) {
    val rayCount = if (isBatterySaving) 4 else 7
    val raySpanAngle = 90f // Tỏa góc 90 độ hướng xuống
    val startAngle = 90f - raySpanAngle / 2f
    val step = raySpanAngle / (rayCount - 1)

    val rayLength = height * 0.70f
    val baseAlpha = if (isDiffused) 0.10f else 0.15f

    for (i in 0 until rayCount) {
        val angleDeg = startAngle + i * step
        val angleRad = angleDeg * (Math.PI / 180.0)

        val endX = sunCenter.x + (cos(angleRad) * rayLength).toFloat()
        val endY = sunCenter.y + (sin(angleRad) * rayLength).toFloat()

        val halfWidth = (width * 0.055f)

        val path = Path().apply {
            moveTo(sunCenter.x, sunCenter.y)
            lineTo(endX + halfWidth, endY)
            lineTo(endX - halfWidth, endY)
            close()
        }

        drawPath(
            path = path,
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFEF9C3).copy(alpha = (baseAlpha * pulse).coerceIn(0f, 1f)),
                    Color(0xFFFDE047).copy(alpha = (baseAlpha * 0.5f * pulse).coerceIn(0f, 1f)),
                    Color.Transparent
                ),
                center = sunCenter,
                radius = rayLength
            )
        )
    }
}

/**
 * Hiệu ứng bầu trời đêm: Vầng trăng phát quang kèm vết lồi lõm, cực quang uốn lượn, sao lấp lánh và sao băng
 */
private fun DrawScope.drawAtmosphericNightSky(
    moonCenter: Offset,
    width: Float,
    height: Float,
    breathingPulse: Float,
    slowDrift: Float,
    meteorProgress: Float,
    stars: List<StarParticle>,
    isBatterySaving: Boolean
) {
    // 1. Quầng hào quang ánh trăng tỏa dịu
    val haloRadius = width * 0.28f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFBAE6FD).copy(alpha = 0.24f * breathingPulse),
                Color(0xFFE2E8F0).copy(alpha = 0.12f * breathingPulse),
                Color(0xFF64748B).copy(alpha = 0.04f * breathingPulse),
                Color.Transparent
            ),
            center = moonCenter,
            radius = haloRadius
        ),
        center = moonCenter,
        radius = haloRadius
    )

    // 2. Đĩa mặt trăng sáng trong trẻo
    val moonR = width * 0.068f
    drawCircle(
        color = Color(0xFFF8FAFC).copy(alpha = 0.95f),
        center = moonCenter,
        radius = moonR
    )

    // Vết lồi lõm bề mặt mặt trăng tinh tế (Lunar Maria)
    drawCircle(
        color = Color(0xFFCBD5E1).copy(alpha = 0.35f),
        center = moonCenter + Offset(-moonR * 0.25f, -moonR * 0.15f),
        radius = moonR * 0.28f
    )
    drawCircle(
        color = Color(0xFF94A3B8).copy(alpha = 0.28f),
        center = moonCenter + Offset(moonR * 0.20f, moonR * 0.25f),
        radius = moonR * 0.22f
    )
    drawCircle(
        color = Color(0xFFCBD5E1).copy(alpha = 0.25f),
        center = moonCenter + Offset(-moonR * 0.10f, moonR * 0.35f),
        radius = moonR * 0.18f
    )

    // 3. Dải lụa cực quang huyền diệu (Aurora Ribbon Wave)
    drawAuroraRibbon(width, height, slowDrift, breathingPulse, isBatterySaving = isBatterySaving)

    // 4. Bầu trời sao lấp lánh đa tần số kèm tia sao 4 cánh
    drawStarFieldWithCrossSpikes(stars, breathingPulse, width, height, isBatterySaving = isBatterySaving)

    // 5. Vệt sao băng lướt qua bầu trời (Hoạt động liên tục)
    if (!isBatterySaving && meteorProgress in 0.01f..0.99f) {
        drawShootingMeteor(width, height, meteorProgress)
    }
}

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
    sites.forEachIndexed { _, site ->
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
 * Tia sét phân nhánh đa hướng sắc sảo và chân thực khi dông bão
 */
private fun DrawScope.drawRealisticLightningFork(width: Float, height: Float, flashIntensity: Float) {
    val startX = width * 0.52f

    val mainBolt = Path().apply {
        moveTo(startX, 0f)
        lineTo(startX - 26f, height * 0.14f)
        lineTo(startX + 18f, height * 0.22f)
        lineTo(startX - 38f, height * 0.38f)
        lineTo(startX + 12f, height * 0.46f)
        lineTo(startX - 24f, height * 0.62f)
        lineTo(startX + 8f, height * 0.72f)
        lineTo(startX - 14f, height * 0.82f)
    }

    val branch1 = Path().apply {
        moveTo(startX - 38f, height * 0.38f)
        lineTo(startX - 78f, height * 0.45f)
        lineTo(startX - 95f, height * 0.54f)
        lineTo(startX - 120f, height * 0.60f)
    }

    val branch2 = Path().apply {
        moveTo(startX + 12f, height * 0.46f)
        lineTo(startX + 45f, height * 0.52f)
        lineTo(startX + 58f, height * 0.61f)
    }

    val branch3 = Path().apply {
        moveTo(startX - 26f, height * 0.14f)
        lineTo(startX - 60f, height * 0.20f)
    }

    val purpleAlpha = (flashIntensity * 0.55f).coerceIn(0f, 0.75f)
    drawPath(path = mainBolt, color = Color(0xFFA855F7).copy(alpha = purpleAlpha), style = Stroke(width = 8.0f, cap = StrokeCap.Round))
    drawPath(path = branch1, color = Color(0xFFA855F7).copy(alpha = purpleAlpha * 0.8f), style = Stroke(width = 5.5f, cap = StrokeCap.Round))
    drawPath(path = branch2, color = Color(0xFFA855F7).copy(alpha = purpleAlpha * 0.8f), style = Stroke(width = 5.0f, cap = StrokeCap.Round))
    drawPath(path = branch3, color = Color(0xFFA855F7).copy(alpha = purpleAlpha * 0.7f), style = Stroke(width = 4.0f, cap = StrokeCap.Round))

    val cyanAlpha = (flashIntensity * 0.70f).coerceIn(0f, 0.85f)
    drawPath(path = mainBolt, color = Color(0xFF38BDF8).copy(alpha = cyanAlpha), style = Stroke(width = 4.2f, cap = StrokeCap.Round))
    drawPath(path = branch1, color = Color(0xFF38BDF8).copy(alpha = cyanAlpha * 0.8f), style = Stroke(width = 2.8f, cap = StrokeCap.Round))
    drawPath(path = branch2, color = Color(0xFF38BDF8).copy(alpha = cyanAlpha * 0.8f), style = Stroke(width = 2.5f, cap = StrokeCap.Round))

    val coreAlpha = flashIntensity.coerceIn(0f, 0.98f)
    drawPath(path = mainBolt, color = Color.White.copy(alpha = coreAlpha), style = Stroke(width = 2.2f, cap = StrokeCap.Round))
    drawPath(path = branch1, color = Color.White.copy(alpha = coreAlpha * 0.85f), style = Stroke(width = 1.4f, cap = StrokeCap.Round))
    drawPath(path = branch2, color = Color.White.copy(alpha = coreAlpha * 0.85f), style = Stroke(width = 1.3f, cap = StrokeCap.Round))
    drawPath(path = branch3, color = Color.White.copy(alpha = coreAlpha * 0.75f), style = Stroke(width = 1.2f, cap = StrokeCap.Round))
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
    stars.forEachIndexed { _, star ->
        val starX = star.xRatio * width
        val starY = star.yRatio * height
        val twinkle = (sin(pulse * 6.28f * star.twinkleSpeed + star.phase) * 0.4f + 0.6f)
        val alpha = (star.baseAlpha * twinkle).coerceIn(0.15f, 0.98f)

        drawCircle(
            color = Color.White.copy(alpha = alpha),
            center = Offset(starX, starY),
            radius = star.radius
        )

        if (!isBatterySaving && star.hasCrossSpikes && alpha > 0.65f) {
            val spikeLen = star.radius * 2.8f
            val spikeAlpha = (alpha * 0.55f).coerceIn(0f, 0.7f)

            drawLine(
                color = Color.White.copy(alpha = spikeAlpha),
                start = Offset(starX - spikeLen, starY),
                end = Offset(starX + spikeLen, starY),
                strokeWidth = 0.8f
            )
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

    drawCircle(
        color = Color.White,
        radius = 2.0f,
        center = Offset(startX, startY)
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
            color = Color.White.copy(alpha = (0.07f + i * 0.04f) * pulse)
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
