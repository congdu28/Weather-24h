package com.weather24h.app.ui.components

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.weather24h.app.data.model.DailyForecast
import com.weather24h.app.data.model.WeatherType
import com.weather24h.app.ui.theme.LocalCardBorder
import com.weather24h.app.ui.theme.LocalCardGlassBg
import com.weather24h.app.ui.theme.LocalContentTextColor
import com.weather24h.app.ui.theme.LocalIsDarkTheme
import com.weather24h.app.ui.theme.LocalSubtextColor
import com.weather24h.app.util.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private const val PREF_FILE = "weather24h_prefs"
private const val PREF_BIRTH_YEAR = "lunar_user_birth_year"

enum class LunarDialogTab(val label: String, val icon: ImageVector) {
    DETAIL("Chi tiết", Icons.Default.AutoAwesome),
    MONTH("Lịch tháng", Icons.Default.CalendarMonth),
    GOOD_DAYS("Tìm ngày tốt", Icons.Default.EventAvailable),
    EVENTS("Ngày lễ", Icons.Default.Celebration),
    CONVERT("Đổi ngày", Icons.Default.SwapHoriz)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VietnameseLunarCard(
    modifier: Modifier = Modifier,
    dailyForecasts: List<DailyForecast> = emptyList()
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE) }
    var birthYear by remember { mutableIntStateOf(prefs.getInt(PREF_BIRTH_YEAR, 0)) }
    val profile = remember(birthYear) { if (birthYear in 1900..2100) LunarAlmanac.getPersonalProfile(birthYear) else null }

    val isDark = LocalIsDarkTheme.current
    val contentColor = LocalContentTextColor.current
    val subtextColor = LocalSubtextColor.current

    var selectedLocalDate by remember { mutableStateOf(LocalDate.now()) }
    var activeDialogTab by remember { mutableStateOf<LunarDialogTab?>(null) }

    val almanac = remember(selectedLocalDate) { LunarAlmanac.getDayAlmanac(selectedLocalDate) }
    val lunar = almanac.lunar
    val personalMatch = remember(profile, almanac) { profile?.let { LunarAlmanac.matchPersonalDay(it, almanac) } }

    val isToday = remember(selectedLocalDate) { selectedLocalDate == LocalDate.now() }
    val (moonEventText, moonEventDays) = remember(lunar.day, lunar.monthLength) {
        VietnameseLunarCalendar.getNextMoonEventCountdown(lunar)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(LocalCardGlassBg.current)
            .border(0.5.dp, LocalCardBorder.current, RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Column {
            // 1. Header & Chuyển ngày
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFFFFB703), Color(0xFFFB8500)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NightsStay,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "LỊCH VẠN NIÊN & TIẾT KHÍ",
                            color = contentColor.copy(alpha = 0.90f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "Dương lịch: ${formatWeekdayVi(selectedLocalDate.dayOfWeek.value)}, ${selectedLocalDate.dayOfMonth}/${selectedLocalDate.monthValue}/${selectedLocalDate.year}",
                            color = subtextColor,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { selectedLocalDate = selectedLocalDate.minusDays(1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, "Ngày trước", tint = contentColor.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                    }
                    if (!isToday) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF38BDF8).copy(alpha = 0.20f),
                            border = BorderStroke(0.5.dp, Color(0xFF38BDF8).copy(alpha = 0.40f)),
                            modifier = Modifier.clickable { selectedLocalDate = LocalDate.now() }
                        ) {
                            Text(
                                text = "Hôm nay",
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = { selectedLocalDate = selectedLocalDate.plusDays(1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, "Ngày sau", tint = contentColor.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Banner lễ hội hoặc đếm ngược Rằm / Mùng Một
            val festival = lunar.festival
            if (festival != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth().clickable { activeDialogTab = LunarDialogTab.EVENTS }
                ) {
                    Text(
                        text = festival,
                        color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            } else if (moonEventDays in 1..7) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFB703).copy(alpha = 0.12f),
                    border = BorderStroke(0.5.dp, Color(0xFFFFB703).copy(alpha = 0.30f)),
                    modifier = Modifier.fillMaxWidth().clickable { activeDialogTab = LunarDialogTab.EVENTS }
                ) {
                    Text(
                        text = "🌕 Còn $moonEventDays $moonEventText",
                        color = if (isDark) Color(0xFFFFD166) else Color(0xFFB45309),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 3. Khối trung tâm (Bấm vào mở ngay màn hình chi tiết toàn màn hình)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { activeDialogTab = LunarDialogTab.DETAIL }
                    .padding(vertical = 2.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "${lunar.day}",
                        color = contentColor,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 38.sp
                    )
                    Text(
                        text = "Tháng ${lunar.month}${if (lunar.isLeapMonth) " (N)" else ""}${if (lunar.isFullMonth) " Đ" else " T"}",
                        color = subtextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = LunarAlmanac.starsText(almanac.level),
                        color = levelColor(almanac.level, isDark),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Ngày ${lunar.dayCanChi}",
                            color = contentColor,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${almanac.napAm}",
                            color = subtextColor,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tháng ${lunar.monthCanChi} • Năm ${lunar.yearCanChi}",
                        color = subtextColor,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SmallPill(
                            text = lunar.auspiciousDayType,
                            color = if (lunar.isAuspiciousDay) (if (isDark) Color(0xFF34D399) else Color(0xFF059669))
                            else (if (isDark) Color(0xFF94A3B8) else Color(0xFF475569))
                        )
                        SmallPill(
                            text = "${almanac.truc.fullName} • Sao ${almanac.mansion.name}",
                            color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                        )
                        SmallPill(
                            text = "☀️ ${lunar.solarTerm}",
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                        )
                        personalMatch?.let { pm ->
                            val c = when (pm.verdict) {
                                ActivityVerdict.GOOD -> if (isDark) Color(0xFF34D399) else Color(0xFF059669)
                                ActivityVerdict.BAD -> if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
                                ActivityVerdict.NEUTRAL -> subtextColor
                            }
                            SmallPill(text = "${pm.label} (${profile?.canChi})", color = c)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Tóm tắt nhanh: Nên làm / Kiêng / Hướng tốt
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f))
                    .clickable { activeDialogTab = LunarDialogTab.DETAIL }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val goodShort = almanac.goodActivities.take(4).joinToString(", ") { it.label }
                    .ifEmpty { almanac.truc.goodText }
                val badShort = (almanac.badDays.map { "Phạm ${it.name}" } + almanac.badActivities.take(3).map { it.label })
                    .joinToString(", ").ifEmpty { almanac.truc.badText }

                Text(
                    text = "✅ Nên: $goodShort",
                    color = if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857),
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "❌ Kỵ: $badShort",
                    color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C),
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "🧭 Hỷ Thần: ${almanac.hyThan.label} • Tài Thần: ${almanac.taiThan.label} • Kỵ tuổi: ${VietnameseLunarCalendar.CHI[almanac.clashChi]}",
                    color = contentColor.copy(alpha = 0.85f),
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Giờ Hoàng Đạo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f))
                    .padding(horizontal = 12.dp, vertical = 9.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFFFFB703) else Color(0xFFD97706),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Giờ Hoàng Đạo (★ = hợp cả Lý Thuần Phong):",
                        color = contentColor.copy(alpha = 0.90f),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                val nowHour = remember { LocalTime.now().hour }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    almanac.hours.filter { it.isHoangDao }.forEach { h ->
                        val isNow = isToday && h.containsHour(nowHour)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isNow) Color(0xFFFFB703).copy(alpha = 0.25f)
                            else if (isDark) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.05f),
                            border = BorderStroke(
                                if (isNow) 1.dp else 0.5.dp,
                                if (isNow) Color(0xFFFFB703) else LocalCardBorder.current
                            )
                        ) {
                            Text(
                                text = "${h.chi} (${h.range})${if (h.isBest) " ★" else ""}",
                                color = contentColor.copy(alpha = 0.88f),
                                fontSize = 11.sp,
                                fontWeight = if (h.isBest || isNow) FontWeight.SemiBold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6. Các nút truy cập nhanh Toàn màn hình
            val btnGradient = Brush.horizontalGradient(
                if (isDark) listOf(Color(0xFFFFB703).copy(alpha = 0.24f), Color(0xFFFB8500).copy(alpha = 0.34f))
                else listOf(Color(0xFFFFB703).copy(alpha = 0.28f), Color(0xFFF59E0B).copy(alpha = 0.38f))
            )
            val btnBorderColor = if (isDark) Color(0xFFFFB703).copy(alpha = 0.55f) else Color(0xFFD97706).copy(alpha = 0.50f)
            val btnContentColor = if (isDark) Color(0xFFFFD166) else Color(0xFF92400E)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(btnGradient)
                    .border(1.dp, btnBorderColor, RoundedCornerShape(14.dp))
                    .clickable { activeDialogTab = LunarDialogTab.DETAIL }
                    .padding(vertical = 10.dp, horizontal = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, null, tint = btnContentColor, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Xem chi tiết ngày & Lịch Vạn Niên",
                        color = btnContentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickActionChip(Modifier.weight(1f), Icons.Default.CalendarMonth, "Lịch tháng") {
                    activeDialogTab = LunarDialogTab.MONTH
                }
                QuickActionChip(Modifier.weight(1f), Icons.Default.EventAvailable, "Ngày tốt") {
                    activeDialogTab = LunarDialogTab.GOOD_DAYS
                }
                QuickActionChip(Modifier.weight(1f), Icons.Default.Celebration, "Ngày lễ") {
                    activeDialogTab = LunarDialogTab.EVENTS
                }
                QuickActionChip(Modifier.weight(1f), Icons.Default.SwapHoriz, "Đổi ngày") {
                    activeDialogTab = LunarDialogTab.CONVERT
                }
            }
        }
    }

    if (activeDialogTab != null) {
        LunarAlmanacFullScreenDialog(
            initialDate = selectedLocalDate,
            initialTab = activeDialogTab!!,
            dailyForecasts = dailyForecasts,
            birthYear = birthYear,
            onBirthYearChange = { newYear ->
                birthYear = newYear
                prefs.edit().putInt(PREF_BIRTH_YEAR, newYear).apply()
            },
            onSelectDate = { selectedLocalDate = it },
            onDismiss = { activeDialogTab = null }
        )
    }
}

@Composable
private fun SmallPill(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.35f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
        )
    }
}

@Composable
private fun QuickActionChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val contentColor = LocalContentTextColor.current
    Surface(
        shape = RoundedCornerShape(11.dp),
        color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f),
        border = BorderStroke(0.5.dp, LocalCardBorder.current),
        modifier = modifier.clip(RoundedCornerShape(11.dp)).clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp)
        ) {
            Icon(icon, null, tint = contentColor.copy(alpha = 0.85f), modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, color = contentColor.copy(alpha = 0.9f), fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

// ==========================================================================================
//  HỘP THOẠI TOÀN MÀN HÌNH – LỊCH VẠN NIÊN (5 TAB)
// ==========================================================================================

@Composable
fun FullMonthLunarCalendarDialog(
    initialDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE) }
    var birthYear by remember { mutableIntStateOf(prefs.getInt(PREF_BIRTH_YEAR, 0)) }
    LunarAlmanacFullScreenDialog(
        initialDate = initialDate,
        initialTab = LunarDialogTab.MONTH,
        dailyForecasts = emptyList(),
        birthYear = birthYear,
        onBirthYearChange = {
            birthYear = it
            prefs.edit().putInt(PREF_BIRTH_YEAR, it).apply()
        },
        onSelectDate = onSelectDate,
        onDismiss = onDismiss
    )
}

@Composable
fun LunarAlmanacFullScreenDialog(
    initialDate: LocalDate,
    initialTab: LunarDialogTab = LunarDialogTab.DETAIL,
    dailyForecasts: List<DailyForecast> = emptyList(),
    birthYear: Int = -1,
    onBirthYearChange: ((Int) -> Unit)? = null,
    onSelectDate: (LocalDate) -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE) }
    var internalBirthYear by remember {
        mutableIntStateOf(if (birthYear >= 0) birthYear else prefs.getInt(PREF_BIRTH_YEAR, 0))
    }
    val effectiveBirthYear = if (birthYear >= 0) birthYear else internalBirthYear
    val updateBirthYear: (Int) -> Unit = { newYear ->
        internalBirthYear = newYear
        prefs.edit().putInt(PREF_BIRTH_YEAR, newYear).apply()
        onBirthYearChange?.invoke(newYear)
    }

    val isDark = LocalIsDarkTheme.current
    val contentColor = LocalContentTextColor.current
    val subtextColor = LocalSubtextColor.current
    val dialogBg = if (isDark) Color(0xFF0B132B) else Color(0xFFF8FAFC)
    val cardBg = if (isDark) Color(0xFF131A2A) else Color.White
    val cardBorder = if (isDark) LocalCardBorder.current else Color(0x180F172A)

    var currentTab by remember { mutableStateOf(initialTab) }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var showBirthYearDialog by remember { mutableStateOf(false) }
    var showWidgetConfigDialog by remember { mutableStateOf(false) }

    val profile = remember(effectiveBirthYear) {
        if (effectiveBirthYear in 1900..2100) LunarAlmanac.getPersonalProfile(effectiveBirthYear) else null
    }
    val forecastMap = remember(dailyForecasts) { dailyForecasts.associateBy { it.dateString.take(10) } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler(enabled = true) { onDismiss() }

        Surface(color = dialogBg, modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Top Bar (Hàng 1: Nút quay lại + Tiêu đề đầy đủ + Nút Sửa Widget)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowBack, "Đóng", tint = contentColor, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "LỊCH VẠN NIÊN VIỆT NAM",
                            color = contentColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF38BDF8).copy(alpha = 0.16f),
                        border = BorderStroke(0.5.dp, Color(0xFF38BDF8).copy(alpha = 0.45f)),
                        modifier = Modifier.clickable { showWidgetConfigDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                null,
                                tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Sửa Widget",
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                softWrap = false
                            )
                        }
                    }
                }

                // Hàng 2: Thanh thông tin Tuổi / Bản mệnh (hiển thị trọn vẹn không bị che khuất)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFB703).copy(alpha = if (isDark) 0.13f else 0.12f),
                    border = BorderStroke(0.5.dp, Color(0xFFFFB703).copy(alpha = 0.42f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { showBirthYearDialog = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 11.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                null,
                                tint = if (isDark) Color(0xFFFFD166) else Color(0xFFB45309),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = profile?.let {
                                    "Tuổi ${it.canChi} (${it.birthYear}) • ${it.element.emoji} ${it.napAm} (${it.yinYangLabel})"
                                } ?: "Chạm để chọn Năm sinh — Xem xung hợp Can Chi, Ngũ Hành",
                                color = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E),
                                fontSize = 11.8.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFB703).copy(alpha = 0.22f)
                        ) {
                            Text(
                                text = if (profile != null) "Đổi tuổi (${profile.birthYear})" else "Chọn tuổi ›",
                                color = if (isDark) Color(0xFFFFD166) else Color(0xFFB45309),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Thanh chuyển 5 Tab
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LunarDialogTab.values().forEach { tab ->
                        val active = currentTab == tab
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (active) Color(0xFF38BDF8) else cardBg,
                            border = BorderStroke(0.5.dp, if (active) Color(0xFF38BDF8) else cardBorder),
                            modifier = Modifier.clickable { currentTab = tab }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                            ) {
                                Icon(
                                    tab.icon,
                                    null,
                                    tint = if (active) Color.White else subtextColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = tab.label,
                                    color = if (active) Color.White else contentColor,
                                    fontSize = 12.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Nội dung từng Tab
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    when (currentTab) {
                        LunarDialogTab.DETAIL -> DayDetailTabContent(
                            selectedDate = selectedDate,
                            onDateChange = {
                                selectedDate = it
                                onSelectDate(it)
                            },
                            profile = profile,
                            forecastMap = forecastMap,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            onOpenGoodDays = { currentTab = LunarDialogTab.GOOD_DAYS }
                        )
                        LunarDialogTab.MONTH -> MonthCalendarTabContent(
                            selectedDate = selectedDate,
                            onSelectDate = {
                                selectedDate = it
                                onSelectDate(it)
                            },
                            onOpenDetail = {
                                selectedDate = it
                                onSelectDate(it)
                                currentTab = LunarDialogTab.DETAIL
                            },
                            cardBg = cardBg,
                            cardBorder = cardBorder
                        )
                        LunarDialogTab.GOOD_DAYS -> GoodDayFinderTabContent(
                            profile = profile,
                            forecastMap = forecastMap,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            onSelectDate = {
                                selectedDate = it
                                onSelectDate(it)
                                currentTab = LunarDialogTab.DETAIL
                            }
                        )
                        LunarDialogTab.EVENTS -> UpcomingEventsTabContent(
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            onSelectDate = {
                                selectedDate = it
                                onSelectDate(it)
                                currentTab = LunarDialogTab.DETAIL
                            }
                        )
                        LunarDialogTab.CONVERT -> DateConverterTabContent(
                            initialDate = selectedDate,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            onOpenDetail = {
                                selectedDate = it
                                onSelectDate(it)
                                currentTab = LunarDialogTab.DETAIL
                            }
                        )
                    }
                }
            }
        }
    }

    if (showBirthYearDialog) {
        BirthYearPickerDialog(
            currentYear = effectiveBirthYear,
            onSave = {
                updateBirthYear(it)
                showBirthYearDialog = false
            },
            onDismiss = { showBirthYearDialog = false }
        )
    }

    if (showWidgetConfigDialog) {
        LunarMonthWidgetConfigDialog(
            onDismiss = { showWidgetConfigDialog = false }
        )
    }
}

// ==========================================================================================
//  TAB 1: CHI TIẾT NGÀY TOÀN MÀN HÌNH
// ==========================================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayDetailTabContent(
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    profile: PersonalProfile?,
    forecastMap: Map<String, DailyForecast>,
    cardBg: Color,
    cardBorder: Color,
    onOpenGoodDays: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val contentColor = LocalContentTextColor.current
    val subtextColor = LocalSubtextColor.current

    val almanac = remember(selectedDate) { LunarAlmanac.getDayAlmanac(selectedDate) }
    val lunar = almanac.lunar
    val personalMatch = remember(profile, almanac) { profile?.let { LunarAlmanac.matchPersonalDay(it, almanac) } }
    val forecast = forecastMap[selectedDate.toString()]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Thanh điều hướng ngày & Tổng quan Âm - Dương
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = cardBg,
            border = BorderStroke(0.5.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { onDateChange(selectedDate.minusDays(1)) }) {
                        Icon(Icons.Default.ChevronLeft, "Hôm trước", tint = contentColor)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${formatWeekdayVi(selectedDate.dayOfWeek.value)}, ${selectedDate.dayOfMonth}/${selectedDate.monthValue}/${selectedDate.year}",
                            color = contentColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (selectedDate != LocalDate.now()) {
                            Text(
                                text = "Chạm để về Hôm nay",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                modifier = Modifier.clickable { onDateChange(LocalDate.now()) }
                            )
                        } else {
                            Text(text = "Hôm nay", color = Color(0xFFFFB703), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    IconButton(onClick = { onDateChange(selectedDate.plusDays(1)) }) {
                        Icon(Icons.Default.ChevronRight, "Hôm sau", tint = contentColor)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Tờ lịch Âm lớn
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFFFB703).copy(alpha = 0.22f), Color(0xFFFB8500).copy(alpha = 0.10f))
                                )
                            )
                            .border(1.dp, Color(0xFFFFB703).copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "ÂM LỊCH",
                            color = if (isDark) Color(0xFFFFD166) else Color(0xFFB45309),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${lunar.day}",
                            color = contentColor,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${lunar.monthName} (${if (lunar.isFullMonth) "Đủ" else "Thiếu"})",
                            color = subtextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "Năm ${lunar.yearCanChi} (${LunarAlmanac.napAm((lunar.year + 6) % 10, (lunar.year + 8) % 12)})",
                            color = subtextColor,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Tháng ${lunar.monthCanChi}",
                            color = subtextColor,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Ngày ${lunar.dayCanChi} (${almanac.yinYangLabel})",
                            color = contentColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Nạp âm: ${almanac.napAmElement.emoji} ${almanac.napAm}",
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tiết khí: ☀️ ${lunar.solarTerm}",
                            color = subtextColor,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Đánh giá tổng quan (Điểm + Sao + Tóm tắt)
                val lvlColor = levelColor(almanac.level, isDark)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = lvlColor.copy(alpha = 0.12f),
                    border = BorderStroke(0.5.dp, lvlColor.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Đánh giá ngày: ${almanac.level.label} (${almanac.score}/100)",
                                color = lvlColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = LunarAlmanac.starsText(almanac.level),
                                color = lvlColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = almanac.summary,
                            color = contentColor.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                // Các sự kiện / ngày lễ trong ngày
                if (lunar.events.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    lunar.events.forEach { ev ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(text = ev.displayName, color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626), fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            if (ev.description.isNotBlank()) {
                                Text(text = " — ${ev.description}", color = subtextColor, fontSize = 11.5.sp)
                            }
                        }
                    }
                }

                // Thời tiết dự báo của ngày (nếu nằm trong 7 ngày tới)
                if (forecast != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    WeatherPairingBanner(forecast = forecast, almanac = almanac, isDark = isDark)
                }
            }
        }

        // 2. Nên làm / Không nên làm
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = cardBg,
            border = BorderStroke(0.5.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(Icons.Default.CheckCircle, "VIỆC NÊN LÀM & KIÊNG KỴ TRONG NGÀY", Color(0xFF10B981))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "✅ Việc nên làm",
                    color = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (almanac.goodActivities.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        almanac.goodActivities.forEach { act ->
                            SmallPill("${act.emoji} ${act.label}", if (isDark) Color(0xFF34D399) else Color(0xFF059669))
                        }
                    }
                } else {
                    Text("Hôm nay ít cát tinh, chỉ nên làm các việc thường nhật, cúng lễ cầu an.", color = subtextColor, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "❌ Việc nên tránh / kiêng kỵ",
                    color = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (almanac.badActivities.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        almanac.badActivities.forEach { act ->
                            SmallPill("${act.emoji} ${act.label}", if (isDark) Color(0xFFF87171) else Color(0xFFDC2626))
                        }
                    }
                } else {
                    Text("Không kỵ việc lớn nào đáng kể.", color = subtextColor, fontSize = 12.sp)
                }

                // Cát tinh & Ngày kỵ
                if (almanac.goodStars.isNotEmpty() || almanac.badDays.isNotEmpty()) {
                    HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 10.dp))
                    almanac.goodStars.forEach { s ->
                        Text("🌟 Cát tinh ${s.name}: ${s.description}", color = contentColor, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                    }
                    almanac.badDays.forEach { s ->
                        Text("⚠️ Ngày kỵ ${s.name}: ${s.description}", color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C), fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }

        // 3. Thập nhị Trực & Nhị thập bát tú
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = cardBg,
            border = BorderStroke(0.5.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(Icons.Default.Stars, "THẬP NHỊ TRỰC & NHỊ THẬP BÁT TÚ", Color(0xFFFFB703))

                Text(
                    text = "📌 ${almanac.truc.fullName} • ${lunar.auspiciousDayType}",
                    color = contentColor,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("• Nên: ${almanac.truc.goodText}", color = subtextColor, fontSize = 12.sp)
                Text("• Kỵ: ${almanac.truc.badText}", color = subtextColor, fontSize = 12.sp)

                HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "✨ Sao ${almanac.mansion.name} (${almanac.mansion.fullName}) — ${if (almanac.mansion.isGood) "Cát tinh (Sao tốt)" else "Hung tinh (Sao xấu)"}",
                    color = contentColor,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("• Tốt cho: ${almanac.mansion.goodText}", color = subtextColor, fontSize = 12.sp)
                Text("• Kiêng: ${almanac.mansion.badText}", color = subtextColor, fontSize = 12.sp)
            }
        }

        // 4. La bàn hướng xuất hành
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = cardBg,
            border = BorderStroke(0.5.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(Icons.Default.Explore, "HƯỚNG XUẤT HÀNH TỐT TRONG NGÀY", Color(0xFF38BDF8))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AuspiciousCompass(
                        hyThan = almanac.hyThan,
                        taiThan = almanac.taiThan,
                        hacThan = almanac.hacThan,
                        isDark = isDark,
                        modifier = Modifier.size(135.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DirectionLegendRow(
                            color = Color(0xFFEC4899),
                            title = "Hỷ Thần (cầu may mắn, cưới hỏi, hỷ sự)",
                            direction = almanac.hyThan.label
                        )
                        DirectionLegendRow(
                            color = Color(0xFFFFB703),
                            title = "Tài Thần (cầu tài lộc, kinh doanh, ký kết)",
                            direction = almanac.taiThan.label
                        )
                        DirectionLegendRow(
                            color = Color(0xFF94A3B8),
                            title = "Hạc Thần (hung thần nên tránh)",
                            direction = almanac.hacThan?.label ?: "Trên trời (không kỵ hướng nào)"
                        )
                    }
                }
            }
        }

        // 5. 12 Canh giờ trong ngày (Hoàng đạo & Lý Thuần Phong)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = cardBg,
            border = BorderStroke(0.5.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(Icons.Default.Schedule, "CHI TIẾT 12 CANH GIỜ & GIỜ XUẤT HÀNH", Color(0xFFFFB703))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Giờ có dấu ★ vừa là Giờ Hoàng Đạo vừa đạt cát thời Lý Thuần Phong (${almanac.bestHours.joinToString(", ") { "${it.chi} ${it.range}" }}).",
                    color = subtextColor,
                    fontSize = 11.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                val isToday = selectedDate == LocalDate.now()
                val nowHour = remember { LocalTime.now().hour }

                almanac.hours.forEach { h ->
                    val isNow = isToday && h.containsHour(nowHour)
                    val ltpGood = h.lyThuanPhong.isGood
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when {
                                    isNow -> Color(0xFFFFB703).copy(alpha = 0.16f)
                                    h.isBest -> Color(0xFF10B981).copy(alpha = 0.08f)
                                    else -> Color.Transparent
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Giờ ${h.chi} (${h.range})${if (h.isBest) " ★" else ""}",
                                color = contentColor,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                SmallPill(
                                    text = if (h.isHoangDao) "Hoàng đạo" else "Hắc đạo",
                                    color = if (h.isHoangDao) Color(0xFF10B981) else Color(0xFF64748B)
                                )
                                SmallPill(
                                    text = h.lyThuanPhong.name,
                                    color = if (ltpGood) Color(0xFF38BDF8) else Color(0xFFF97316)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = h.lyThuanPhong.description,
                            color = subtextColor,
                            fontSize = 11.sp,
                            lineHeight = 14.5.sp
                        )
                    }
                }
            }
        }

        // 6. Tuổi xung khắc & Cá nhân hoá
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = cardBg,
            border = BorderStroke(0.5.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionHeader(Icons.Default.Groups, "TUỔI HỢP – XUNG VỚI NGÀY ${lunar.dayCanChi.uppercase()}", Color(0xFFF59E0B))
                Text(
                    text = "• Tuổi hợp (Tam hợp / Lục hợp): ${almanac.harmonyChi.joinToString(", ") { VietnameseLunarCalendar.CHI[it] }}",
                    color = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "• Xung khắc mạnh: ${almanac.clashAgesText}",
                    color = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "• Tương hại: Tuổi ${VietnameseLunarCalendar.CHI[almanac.harmChi]}${if (almanac.punishChi.isNotEmpty()) " • Tương hình: Tuổi ${almanac.punishChi.joinToString(", ") { VietnameseLunarCalendar.CHI[it] }}" else ""}",
                    color = subtextColor,
                    fontSize = 12.sp
                )

                if (profile != null && personalMatch != null) {
                    HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "👤 Đối chiếu: ${profile.canChi} (${profile.birthYear}) • ${profile.yinYangLabel}",
                            color = contentColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        val matchColor = when {
                            personalMatch.verdict == ActivityVerdict.BAD -> Color(0xFFEF4444)
                            personalMatch.points >= 6 -> Color(0xFF10B981)
                            personalMatch.points < 0 -> Color(0xFFF97316)
                            else -> Color(0xFF38BDF8)
                        }
                        val matchBadge = when {
                            personalMatch.verdict == ActivityVerdict.BAD -> "Xung tuổi (${personalMatch.points})"
                            personalMatch.points >= 6 -> "Rất hợp (+${personalMatch.points})"
                            personalMatch.points > 0 -> "Hợp (+${personalMatch.points})"
                            personalMatch.points < 0 -> "Hơi khắc (${personalMatch.points})"
                            else -> "Bình hòa"
                        }
                        SmallPill(text = matchBadge, color = matchColor)
                    }
                    Text(
                        text = "• Bản mệnh: ${profile.element.emoji} ${profile.napAm} (Can ${profile.canElement.label}, Chi ${profile.chiElement.label})",
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    personalMatch.notes.forEach { n ->
                        Text("• $n", color = subtextColor, fontSize = 12.sp, lineHeight = 16.5.sp)
                    }
                    Text("• Màu sắc hợp mệnh: ${profile.luckyColors}", color = subtextColor, fontSize = 11.5.sp)
                }
            }
        }

        // Gợi ý mở Tìm ngày tốt
        Button(
            onClick = onOpenGoodDays,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.EventAvailable, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tìm ngày tốt cho Cưới hỏi, Khai trương, Xuất hành...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Text(
            text = "Lưu ý: Thông tin ngày tốt xấu, trực, tú và hướng xuất hành được tổng hợp theo sách lịch vạn niên truyền thống, mang tính chất tham khảo văn hóa.",
            color = subtextColor.copy(alpha = 0.7f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
    }
}

@Composable
private fun WeatherPairingBanner(forecast: DailyForecast, almanac: DayAlmanac, isDark: Boolean) {
    val cond = forecast.condition
    val rainProb = forecast.precipitationProbability
    val niceWeather = (cond.type == WeatherType.CLEAR || cond.type == WeatherType.PARTLY_CLOUDY || cond.type == WeatherType.CLOUDY) && rainProb < 45
    val advice = when {
        almanac.level.stars >= 4 && niceWeather -> "Ngày tốt + thời tiết đẹp, rất thuận lợi cho việc ngoài trời & xuất hành!"
        almanac.level.stars >= 4 && !niceWeather -> "Ngày tốt về lịch âm nhưng khả năng mưa ${rainProb}%, nên chuẩn bị phương án che chắn."
        almanac.level.stars <= 2 && niceWeather -> "Trời đẹp nhưng ngày lịch âm kém thuận, chỉ nên dạo chơi hoặc làm việc nhẹ."
        else -> "Kết hợp xem thêm dự báo mưa (${rainProb}%) khi lên kế hoạch trong ngày."
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF38BDF8).copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "🌦️ Thời tiết dự báo: ${cond.description} • ${forecast.minTemp.roundToInt()}°–${forecast.maxTemp.roundToInt()}°C (Mưa ${rainProb}%)",
                color = if (isDark) Color(0xFF7DD3FC) else Color(0xFF0369A1),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = advice, color = LocalContentTextColor.current.copy(alpha = 0.88f), fontSize = 11.5.sp)
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(17.dp))
        Spacer(modifier = Modifier.width(7.dp))
        Text(title, color = LocalContentTextColor.current, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
    }
}

@Composable
private fun DirectionLegendRow(color: Color, title: String, direction: String) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(6.dp))
            Text(direction, color = LocalContentTextColor.current, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Text(title, color = LocalSubtextColor.current, fontSize = 11.sp)
    }
}

@Composable
private fun AuspiciousCompass(
    hyThan: CompassDirection,
    taiThan: CompassDirection,
    hacThan: CompassDirection?,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val ringColor = if (isDark) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.15f)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val r = size.minDimension / 2f - 10.dp.toPx()

            drawCircle(color = ringColor, radius = r, center = c, style = Stroke(width = 1.5.dp.toPx()))
            drawCircle(color = ringColor.copy(alpha = 0.08f), radius = r * 0.65f, center = c, style = Stroke(width = 1.dp.toPx()))

            // Vạch 8 hướng
            for (i in 0 until 8) {
                val rad = Math.toRadians((i * 45 - 90).toDouble())
                val p1 = Offset((c.x + (r - 5.dp.toPx()) * cos(rad)).toFloat(), (c.y + (r - 5.dp.toPx()) * sin(rad)).toFloat())
                val p2 = Offset((c.x + r * cos(rad)).toFloat(), (c.y + r * sin(rad)).toFloat())
                drawLine(ringColor, p1, p2, strokeWidth = 1.5.dp.toPx())
            }

            fun drawPointer(dir: CompassDirection, color: Color, lengthFactor: Float) {
                val rad = Math.toRadians((dir.angle - 90f).toDouble())
                val end = Offset((c.x + r * lengthFactor * cos(rad)).toFloat(), (c.y + r * lengthFactor * sin(rad)).toFloat())
                drawLine(color, c, end, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                drawCircle(color, radius = 5.dp.toPx(), center = end)
            }

            hacThan?.let { drawPointer(it, Color(0xFF94A3B8), 0.65f) }
            drawPointer(taiThan, Color(0xFFFFB703), 0.82f)
            drawPointer(hyThan, Color(0xFFEC4899), 0.74f)
            drawCircle(color = if (isDark) Color.White else Color.Black, radius = 3.5.dp.toPx(), center = c)
        }
        // Nhãn B - N - Đ - T
        val labelColor = LocalSubtextColor.current
        Text("B", color = Color(0xFFEF4444), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopCenter))
        Text("N", color = labelColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomCenter))
        Text("Đ", color = labelColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterEnd))
        Text("T", color = labelColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterStart))
    }
}

// ==========================================================================================
//  TAB 2: LỊCH THÁNG ÂM - DƯƠNG
// ==========================================================================================

@Composable
private fun MonthCalendarTabContent(
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    onOpenDetail: (LocalDate) -> Unit,
    cardBg: Color,
    cardBorder: Color
) {
    val isDark = LocalIsDarkTheme.current
    val contentColor = LocalContentTextColor.current
    val subtextColor = LocalSubtextColor.current

    var currentYearMonth by remember { mutableStateOf(YearMonth.of(selectedDate.year, selectedDate.monthValue)) }
    val daysInMonth = remember(currentYearMonth) {
        VietnameseLunarCalendar.getMonthDays(currentYearMonth.year, currentYearMonth.monthValue)
    }
    val selectedAlmanac = remember(selectedDate) { LunarAlmanac.getDayAlmanac(selectedDate) }
    val selectedLunar = selectedAlmanac.lunar
    val emptySlotsBefore = remember(currentYearMonth) {
        LocalDate.of(currentYearMonth.year, currentYearMonth.monthValue, 1).dayOfWeek.value - 1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bộ chuyển tháng
        Surface(shape = RoundedCornerShape(18.dp), color = cardBg, border = BorderStroke(0.5.dp, cardBorder), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }) {
                    Icon(Icons.Default.ChevronLeft, "Tháng trước", tint = contentColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Tháng ${currentYearMonth.monthValue} / ${currentYearMonth.year}",
                        color = contentColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
                        modifier = Modifier.clickable {
                            val now = LocalDate.now()
                            currentYearMonth = YearMonth.of(now.year, now.monthValue)
                            onSelectDate(now)
                        }
                    ) {
                        Text(
                            text = "Hiện tại",
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                IconButton(onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }) {
                    Icon(Icons.Default.ChevronRight, "Tháng sau", tint = contentColor)
                }
            }
        }

        // Lưới lịch tháng
        Surface(shape = RoundedCornerShape(20.dp), color = cardBg, border = BorderStroke(0.5.dp, cardBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                val dayTitles = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    dayTitles.forEachIndexed { i, title ->
                        Text(
                            text = title,
                            color = if (i >= 5) (if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)) else subtextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                HorizontalDivider(color = cardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                val totalSlots = emptySlotsBefore + daysInMonth.size
                val rowsCount = (totalSlots + 6) / 7
                for (r in 0 until rowsCount) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        for (c in 0..6) {
                            val dayIndex = r * 7 + c - emptySlotsBefore
                            if (dayIndex in daysInMonth.indices) {
                                val dayInfo = daysInMonth[dayIndex]
                                val itemDate = LocalDate.of(currentYearMonth.year, currentYearMonth.monthValue, dayInfo.solarDay)
                                val isSelected = itemDate == selectedDate
                                val isTodayItem = dayInfo.isToday
                                val lDay = dayInfo.lunarDate.day
                                val isFest = dayInfo.lunarDate.festival != null

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.95f)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                isSelected -> Color(0xFF38BDF8).copy(alpha = 0.22f)
                                                isTodayItem -> Color(0xFFFFB703).copy(alpha = 0.16f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else if (isTodayItem) 1.dp else 0.dp,
                                            color = if (isSelected) Color(0xFF38BDF8) else if (isTodayItem) Color(0xFFFFB703) else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { onSelectDate(itemDate) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${dayInfo.solarDay}",
                                            color = if (isTodayItem) Color(0xFFFFB703) else contentColor,
                                            fontSize = 13.5.sp,
                                            fontWeight = if (isSelected || isTodayItem) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Text(
                                            text = if (lDay == 1) "1/${dayInfo.lunarDate.month}" else "$lDay",
                                            color = when {
                                                lDay == 15 -> if (isDark) Color(0xFFFFD166) else Color(0xFFB45309)
                                                lDay == 1 || isFest -> if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626)
                                                else -> subtextColor.copy(alpha = 0.85f)
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = if (lDay == 1 || lDay == 15 || isFest) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            if (dayInfo.lunarDate.isAuspiciousDay) {
                                                Box(modifier = Modifier.size(3.5.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                            }
                                            if (isFest) {
                                                Box(modifier = Modifier.size(3.5.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                            }
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Tóm tắt ngày đang chọn + nút mở chi tiết
        Surface(shape = RoundedCornerShape(20.dp), color = cardBg, border = BorderStroke(0.5.dp, cardBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedDate.dayOfMonth}/${selectedDate.monthValue}/${selectedDate.year} • ${selectedLunar.formattedLunarShort}",
                        color = contentColor,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SmallPill(
                        text = "${selectedAlmanac.level.label} ${LunarAlmanac.starsText(selectedAlmanac.level)}",
                        color = levelColor(selectedAlmanac.level, isDark)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${selectedLunar.fullCanChi} • ${selectedAlmanac.truc.fullName} • Sao ${selectedAlmanac.mansion.name}",
                    color = subtextColor,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = selectedAlmanac.summary, color = contentColor.copy(alpha = 0.9f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onOpenDetail(selectedDate) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Xem chi tiết đầy đủ ngày này", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ==========================================================================================
//  TAB 3: TÌM NGÀY TỐT THEO VIỆC + KẾT HỢP THỜI TIẾT
// ==========================================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoodDayFinderTabContent(
    profile: PersonalProfile?,
    forecastMap: Map<String, DailyForecast>,
    cardBg: Color,
    cardBorder: Color,
    onSelectDate: (LocalDate) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val contentColor = LocalContentTextColor.current
    val subtextColor = LocalSubtextColor.current

    var selectedActivity by remember { mutableStateOf(AlmanacActivity.TRAVEL) }
    var rangeDays by remember { mutableIntStateOf(30) }

    val results = remember(selectedActivity, rangeDays, profile) {
        LunarAlmanac.findGoodDays(
            activity = selectedActivity,
            from = LocalDate.now(),
            days = rangeDays,
            profile = profile
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(shape = RoundedCornerShape(18.dp), color = cardBg, border = BorderStroke(0.5.dp, cardBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Chọn công việc cần xem ngày:", color = contentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AlmanacActivity.values().forEach { act ->
                        val active = act == selectedActivity
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) Color(0xFFFFB703).copy(alpha = 0.25f) else Color.Transparent,
                            border = BorderStroke(if (active) 1.dp else 0.5.dp, if (active) Color(0xFFFFB703) else cardBorder),
                            modifier = Modifier.clickable { selectedActivity = act }
                        ) {
                            Text(
                                text = "${act.emoji} ${act.label}",
                                color = if (active) (if (isDark) Color(0xFFFFD166) else Color(0xFFB45309)) else contentColor,
                                fontSize = 11.5.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Khoảng thời gian tìm kiếm:", color = contentColor, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(14 to "2 tuần", 30 to "1 tháng", 60 to "2 tháng", 90 to "3 tháng").forEach { (d, label) ->
                        val active = rangeDays == d
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) Color(0xFF38BDF8) else Color.Transparent,
                            border = BorderStroke(0.5.dp, if (active) Color(0xFF38BDF8) else cardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { rangeDays = d }
                        ) {
                            Text(
                                text = label,
                                color = if (active) Color.White else subtextColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "Tìm thấy ${results.size} ngày tốt cho \"${selectedActivity.label}\"${if (profile != null) " (đã lọc ngày xung tuổi ${profile.canChi})" else ""}:",
            color = subtextColor,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        results.forEach { item ->
            val a = item.almanac
            val f = forecastMap[a.date.toString()]
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardBg,
                border = BorderStroke(0.5.dp, cardBorder),
                modifier = Modifier.fillMaxWidth().clickable { onSelectDate(a.date) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${formatWeekdayVi(a.date.dayOfWeek.value)}, ${a.date.dayOfMonth}/${a.date.monthValue}/${a.date.year} (${a.lunar.formattedLunarShort})",
                            color = contentColor,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SmallPill("${a.level.label} ${LunarAlmanac.starsText(a.level)}", levelColor(a.level, isDark))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SmallPill("Ngày ${a.lunar.dayCanChi}", if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309))
                        SmallPill("${a.truc.fullName} • Sao ${a.mansion.name}", if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7))
                        SmallPill(
                            text = a.lunar.auspiciousDayType,
                            color = if (a.lunar.isAuspiciousDay) (if (isDark) Color(0xFF34D399) else Color(0xFF059669))
                            else (if (isDark) Color(0xFF94A3B8) else Color(0xFF475569))
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🧭 Hỷ Thần: ${a.hyThan.label} • Tài Thần: ${a.taiThan.label}",
                        color = contentColor.copy(alpha = 0.90f),
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val bestHourStr = a.bestHours.take(4).joinToString(", ") { "${it.chi} (${it.range})" }
                        .ifEmpty { a.lunar.auspiciousHours.take(3).joinToString(", ") }
                    Text(
                        text = "⏰ Giờ đẹp: $bestHourStr",
                        color = subtextColor,
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (f != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🌦️ Dự báo: ${f.condition.description}, ${f.minTemp.roundToInt()}°–${f.maxTemp.roundToInt()}°C, mưa ${f.precipitationProbability}%",
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ==========================================================================================
//  TAB 4: DANH SÁCH NGÀY LỄ & SỰ KIỆN SẮP TỚI
// ==========================================================================================

@Composable
private fun UpcomingEventsTabContent(
    cardBg: Color,
    cardBorder: Color,
    onSelectDate: (LocalDate) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val contentColor = LocalContentTextColor.current
    val subtextColor = LocalSubtextColor.current

    var showMoonPhases by remember { mutableStateOf(false) }
    val events = remember(showMoonPhases) {
        VietnameseLunarCalendar.getUpcomingEvents(
            from = LocalDate.now(),
            days = 365,
            includeMoonPhases = showMoonPhases,
            includeMinor = true
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Text("Sự kiện & lễ Tết trong 12 tháng tới (${events.size}):", color = subtextColor, fontSize = 12.sp)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (showMoonPhases) Color(0xFFFFB703).copy(alpha = 0.2f) else Color.Transparent,
                border = BorderStroke(0.5.dp, if (showMoonPhases) Color(0xFFFFB703) else cardBorder),
                modifier = Modifier.clickable { showMoonPhases = !showMoonPhases }
            ) {
                Text(
                    text = if (showMoonPhases) "Ẩn Rằm/Mùng 1" else "+ Hiện Rằm/Mùng 1",
                    color = if (showMoonPhases) Color(0xFFFFB703) else subtextColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        events.forEach { item ->
            Surface(
                shape = RoundedCornerShape(15.dp),
                color = cardBg,
                border = BorderStroke(0.5.dp, cardBorder),
                modifier = Modifier.fillMaxWidth().clickable { onSelectDate(item.date) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(13.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.event.displayName,
                            color = contentColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${formatWeekdayVi(item.date.dayOfWeek.value)}, ${item.date.dayOfMonth}/${item.date.monthValue}/${item.date.year} • ${item.lunar.formattedLunarShort} (${item.lunar.dayCanChi})",
                            color = subtextColor,
                            fontSize = 11.5.sp
                        )
                        if (item.event.description.isNotBlank()) {
                            Text(text = item.event.description, color = subtextColor.copy(alpha = 0.85f), fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    SmallPill(
                        text = when (item.daysLeft) {
                            0L -> "Hôm nay"
                            1L -> "Ngày mai"
                            else -> "Còn ${item.daysLeft} ngày"
                        },
                        color = if (item.daysLeft <= 7) Color(0xFFEF4444) else if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                    )
                }
            }
        }
    }
}

// ==========================================================================================
//  TAB 5: ĐỔI NGÀY DƯƠNG ↔ ÂM HAI CHIỀU
// ==========================================================================================

@Composable
private fun DateConverterTabContent(
    initialDate: LocalDate,
    cardBg: Color,
    cardBorder: Color,
    onOpenDetail: (LocalDate) -> Unit
) {
    val contentColor = LocalContentTextColor.current
    val subtextColor = LocalSubtextColor.current

    var solarToLunarMode by remember { mutableStateOf(true) }
    var dInput by remember { mutableStateOf(initialDate.dayOfMonth.toString()) }
    var mInput by remember { mutableStateOf(initialDate.monthValue.toString()) }
    var yInput by remember { mutableStateOf(initialDate.year.toString()) }
    var isLeapInput by remember { mutableStateOf(false) }

    val resolvedSolarDate: LocalDate? = remember(solarToLunarMode, dInput, mInput, yInput, isLeapInput) {
        val d = dInput.toIntOrNull() ?: return@remember null
        val m = mInput.toIntOrNull() ?: return@remember null
        val y = yInput.toIntOrNull() ?: return@remember null
        if (y !in 1900..2100) return@remember null
        try {
            if (solarToLunarMode) LocalDate.of(y, m, d)
            else VietnameseLunarCalendar.convertLunarToSolar(d, m, y, isLeapInput)
        } catch (_: Exception) {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(shape = RoundedCornerShape(18.dp), color = cardBg, border = BorderStroke(0.5.dp, cardBorder), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(true to "Dương lịch → Âm lịch", false to "Âm lịch → Dương lịch").forEach { (mode, label) ->
                        val active = solarToLunarMode == mode
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) Color(0xFF38BDF8) else Color.Transparent,
                            border = BorderStroke(0.5.dp, if (active) Color(0xFF38BDF8) else cardBorder),
                            modifier = Modifier.weight(1f).clickable { solarToLunarMode = mode }
                        ) {
                            Text(
                                text = label,
                                color = if (active) Color.White else contentColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dInput,
                        onValueChange = { if (it.length <= 2) dInput = it.filter(Char::isDigit) },
                        label = { Text("Ngày") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = mInput,
                        onValueChange = { if (it.length <= 2) mInput = it.filter(Char::isDigit) },
                        label = { Text("Tháng") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = yInput,
                        onValueChange = { if (it.length <= 4) yInput = it.filter(Char::isDigit) },
                        label = { Text("Năm (1900-2100)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )
                }

                if (!solarToLunarMode) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                        Checkbox(checked = isLeapInput, onCheckedChange = { isLeapInput = it })
                        Text("Tháng nhuận Âm lịch", color = contentColor, fontSize = 12.5.sp)
                    }
                }
            }
        }

        if (resolvedSolarDate != null) {
            val almanac = remember(resolvedSolarDate) { LunarAlmanac.getDayAlmanac(resolvedSolarDate) }
            val lunar = almanac.lunar
            Surface(shape = RoundedCornerShape(18.dp), color = cardBg, border = BorderStroke(0.5.dp, cardBorder), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("KẾT QUẢ QUY ĐỔI", color = Color(0xFFFFB703), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "☀️ Dương lịch: ${formatWeekdayVi(resolvedSolarDate.dayOfWeek.value)}, ngày ${resolvedSolarDate.dayOfMonth}/${resolvedSolarDate.monthValue}/${resolvedSolarDate.year}",
                        color = contentColor,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "🌙 Âm lịch: ${lunar.formattedLunarFull} (tháng ${if (lunar.isFullMonth) "Đủ 30 ngày" else "Thiếu 29 ngày"})",
                        color = contentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = "${lunar.fullCanChi} • Nạp âm: ${almanac.napAm}", color = subtextColor, fontSize = 12.sp)
                    Text(text = "${almanac.truc.fullName} • Sao ${almanac.mansion.name} • Tiết ${lunar.solarTerm}", color = subtextColor, fontSize = 12.sp)
                    Text(text = almanac.summary, color = contentColor.copy(alpha = 0.9f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { onOpenDetail(resolvedSolarDate) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mở chi tiết ngày này", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Surface(shape = RoundedCornerShape(16.dp), color = cardBg, border = BorderStroke(0.5.dp, cardBorder), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Ngày vừa nhập không hợp lệ hoặc tháng Âm lịch không có ngày 30 / không nhuận trong năm đó.",
                    color = Color(0xFFF87171),
                    fontSize = 12.5.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

// ==========================================================================================
//  HỘP THOẠI NHẬP NĂM SINH
// ==========================================================================================

@Composable
private fun BirthYearPickerDialog(
    currentYear: Int,
    onSave: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(if (currentYear in 1900..2100) currentYear.toString() else "1995") }
    val year = text.toIntOrNull()
    val preview = remember(year) { if (year != null && year in 1900..2100) LunarAlmanac.getPersonalProfile(year) else null }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cá nhân hoá theo năm sinh Âm lịch", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Nhập năm sinh để tự động cảnh báo ngày xung tuổi và lọc khi tìm ngày tốt:", fontSize = 12.5.sp)
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= 4) text = it.filter(Char::isDigit) },
                    label = { Text("Năm sinh (VD: 1996)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (preview != null) {
                    Text(
                        text = "Tuổi: ${preview.canChi} (con ${preview.zodiacAnimal}) • ${preview.yinYangLabel}\n" +
                               "Mệnh Nạp âm: ${preview.element.emoji} ${preview.napAm}\n" +
                               "Ngũ hành Can Chi: Thiên can ${preview.canElement.label} – Địa chi ${preview.chiElement.label}\n" +
                               "Màu hợp: ${preview.luckyColors}",
                        fontSize = 12.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 17.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = preview != null,
                onClick = { if (year != null) onSave(year) }
            ) { Text("Lưu") }
        },
        dismissButton = {
            Row {
                if (currentYear > 0) {
                    TextButton(onClick = { onSave(0) }) { Text("Xóa", color = Color(0xFFEF4444)) }
                }
                TextButton(onClick = onDismiss) { Text("Đóng") }
            }
        }
    )
}

@Composable
fun LunarMonthWidgetConfigDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("weather24h_prefs", Context.MODE_PRIVATE) }
    val isDark = LocalIsDarkTheme.current
    val contentColor = LocalContentTextColor.current
    val subtextColor = LocalSubtextColor.current

    var config by remember {
        mutableStateOf(com.weather24h.app.ui.widget.LunarMonthWidgetProvider.getConfig(context))
    }
    var autoRefreshHours by remember {
        val hasHours = prefs.contains("widget_auto_refresh_hours")
        val initial = if (hasHours) {
            prefs.getInt("widget_auto_refresh_hours", 1)
        } else {
            if (prefs.getBoolean("widget_auto_refresh_enabled", true)) 1 else 0
        }
        mutableIntStateOf(initial)
    }

    val applyConfig: (com.weather24h.app.ui.widget.LunarMonthWidgetConfig) -> Unit = { newCfg ->
        config = newCfg
        com.weather24h.app.ui.widget.LunarMonthWidgetProvider.saveConfig(context, newCfg)
    }

    val applyRefreshInterval: (Int) -> Unit = { hours ->
        autoRefreshHours = hours
        prefs.edit()
            .putInt("widget_auto_refresh_hours", hours)
            .putBoolean("widget_auto_refresh_enabled", hours > 0)
            .apply()
        com.weather24h.app.util.PeriodicWeatherWorker.schedule(context)
        com.weather24h.app.ui.widget.WeatherWidgetProvider.updateAllWidgets(context)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Chỉnh sửa Widget Lịch Tháng",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Chu kỳ tự động cập nhật Widget (1h, 2h, 3h, 6h / lần)
                Text(
                    text = "1. Chu kỳ tự động cập nhật Widget:",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    listOf(
                        0 to "Tắt",
                        1 to "1h",
                        2 to "2h",
                        3 to "3h",
                        6 to "6h"
                    ).forEach { (h, label) ->
                        val sel = autoRefreshHours == h
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (sel) Color(0xFF38BDF8) else Color.Gray.copy(alpha = 0.14f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { applyRefreshInterval(h) }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (sel) Color.White else contentColor,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                // 2. Cỡ chữ & số trên Widget Lịch Tháng
                Text(
                    text = "2. Cỡ chữ & số trên Widget Lịch Tháng:",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        0 to "Tiêu chuẩn",
                        1 to "Lớn (Đề xuất)",
                        2 to "Rất lớn"
                    ).forEach { (lv, label) ->
                        val sel = config.fontSizeLevel == lv
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (sel) Color(0xFFFFB703) else Color.Gray.copy(alpha = 0.14f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { applyConfig(config.copy(fontSizeLevel = lv)) }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (sel) Color(0xFF0F172A) else contentColor,
                                    fontSize = 11.sp,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                // 3. Tích chọn thông tin hiển thị trên Widget Lịch Tháng
                Text(
                    text = "3. Tích chọn thông tin hiển thị trên Widget:",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )

                WidgetCheckOptionRow(
                    label = "Nhiệt độ & tên Phường/Xã trên tiêu đề",
                    checked = config.showWeatherAndLocation,
                    onCheckedChange = { applyConfig(config.copy(showWeatherAndLocation = it)) }
                )
                WidgetCheckOptionRow(
                    label = "Can Chi Năm & Tháng Âm lịch trên tiêu đề",
                    checked = config.showHeaderCanChi,
                    onCheckedChange = { applyConfig(config.copy(showHeaderCanChi = it)) }
                )
                WidgetCheckOptionRow(
                    label = "Chấm xanh Ngày Hoàng Đạo trên ô lịch",
                    checked = config.showHoangDaoDots,
                    onCheckedChange = { applyConfig(config.copy(showHoangDaoDots = it)) }
                )
                WidgetCheckOptionRow(
                    label = "Chấm đỏ Ngày Lễ / Tết trên ô lịch",
                    checked = config.showHolidayDots,
                    onCheckedChange = { applyConfig(config.copy(showHolidayDots = it)) }
                )
                WidgetCheckOptionRow(
                    label = "Khung thông tin Âm lịch hôm nay (phía dưới)",
                    checked = config.showFooterBox,
                    onCheckedChange = { applyConfig(config.copy(showFooterBox = it)) }
                )
                if (config.showFooterBox) {
                    WidgetCheckOptionRow(
                        label = "Dòng Lễ/Rằm, Tiết khí & Hướng tốt",
                        checked = config.showSolarTermAndDir,
                        onCheckedChange = { applyConfig(config.copy(showSolarTermAndDir = it)) }
                    )
                    WidgetCheckOptionRow(
                        label = "Dòng 6 Giờ Hoàng Đạo trong ngày",
                        checked = config.showGoodHours,
                        onCheckedChange = { applyConfig(config.copy(showGoodHours = it)) }
                    )
                }

                Text(
                    text = "💡 Mọi thay đổi được áp dụng tức thì lên Widget Lịch Tháng ngoài màn hình chính.",
                    fontSize = 11.sp,
                    color = subtextColor
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    com.weather24h.app.ui.widget.WeatherWidgetProvider.updateAllWidgets(context)
                    onDismiss()
                }
            ) {
                Text("Hoàn tất", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun WidgetCheckOptionRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val contentColor = LocalContentTextColor.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = contentColor,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatWeekdayVi(dayOfWeek: Int): String = when (dayOfWeek) {
    1 -> "Thứ Hai"
    2 -> "Thứ Ba"
    3 -> "Thứ Tư"
    4 -> "Thứ Năm"
    5 -> "Thứ Sáu"
    6 -> "Thứ Bảy"
    else -> "Chủ Nhật"
}

private fun levelColor(level: DayLevel, isDark: Boolean): Color = when (level) {
    DayLevel.EXCELLENT -> if (isDark) Color(0xFF34D399) else Color(0xFF059669)
    DayLevel.GOOD -> if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    DayLevel.NORMAL -> if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
    DayLevel.BAD -> if (isDark) Color(0xFFFB923C) else Color(0xFFEA580C)
    DayLevel.VERY_BAD -> if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
}
