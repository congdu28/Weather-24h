package com.weather24h.app.ui.components

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.weather24h.app.BuildConfig
import com.weather24h.app.data.model.CityLocation
import com.weather24h.app.data.model.WeatherForecastModel
import com.weather24h.app.data.model.WeatherProvider

private enum class SettingsSubScreen {
    MAIN,
    THEME,
    NOTIFICATIONS,
    LOCATION_CITIES,
    FORECAST_MODEL,
    GEMINI_API,
    SYNC_REFRESH,
    OFFLINE_WIDGET,
    ABOUT
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    currentThemeMode: com.weather24h.app.ui.theme.AppThemeMode,
    currentWeatherProvider: WeatherProvider = WeatherProvider.WEATHER_API,
    customWeatherApiKey: String? = null,
    isUsingCustomWeatherApiKey: Boolean = false,
    hasEffectiveWeatherApiKey: Boolean = true,
    customGoogleWeatherKey: String? = null,
    isUsingCustomGoogleWeatherKey: Boolean = false,
    hasEffectiveGoogleWeatherKey: Boolean = true,
    initialCustomKey: String?,
    isUsingCustomKey: Boolean,
    hasDefaultKey: Boolean,
    currentAutoRefreshMinutes: Int,
    defaultCity: CityLocation,
    isStartWithGps: Boolean,
    isMorningNotif: Boolean,
    isSevereAlerts: Boolean,
    periodicNotifHours: Int,
    bgDimmingLevel: Float,
    ambientGlowStyle: com.weather24h.app.ui.theme.AmbientGlowStyle,
    ambientGlowIntensity: Float,
    cardOpacity: Float = 0.80f,
    widgetThemeMode: com.weather24h.app.ui.theme.AppThemeMode = com.weather24h.app.ui.theme.AppThemeMode.SYSTEM,
    widgetOpacity: Float = 0.85f,
    isWidgetAutoRefresh: Boolean = true,
    forecastModel: WeatherForecastModel = WeatherForecastModel.BEST_MATCH,
    favoriteCities: List<CityLocation>,
    onDismiss: () -> Unit,
    onSelectThemeMode: (com.weather24h.app.ui.theme.AppThemeMode) -> Unit,
    onSelectWeatherProvider: (WeatherProvider) -> Unit = {},
    onSaveWeatherApiKey: (String) -> Unit = {},
    onClearWeatherApiKey: () -> Unit = {},
    onTestWeatherApiKey: ((String, (Boolean, String) -> Unit) -> Unit)? = null,
    onSaveGoogleWeatherKey: (String) -> Unit = {},
    onClearGoogleWeatherKey: () -> Unit = {},
    onTestGoogleWeatherKey: ((String, (Boolean, String) -> Unit) -> Unit)? = null,
    onSetBgDimmingLevel: (Float) -> Unit,
    onSelectAmbientGlowStyle: (com.weather24h.app.ui.theme.AmbientGlowStyle) -> Unit,
    onSetAmbientGlowIntensity: (Float) -> Unit,
    onSetCardOpacity: (Float) -> Unit = {},
    onSelectWidgetThemeMode: (com.weather24h.app.ui.theme.AppThemeMode) -> Unit = {},
    onSetWidgetOpacity: (Float) -> Unit = {},
    onSetWidgetAutoRefresh: (Boolean) -> Unit = {},
    onSelectForecastModel: (WeatherForecastModel) -> Unit = {},
    onSelectPeriodicNotifHours: (Int) -> Unit,
    onSendCurrentWeatherNotif: () -> Unit,
    onSaveCustomKey: (String) -> Unit,
    onClearCustomKey: () -> Unit,
    onSelectAutoRefreshInterval: (Int) -> Unit,
    onSetDefaultCity: (CityLocation) -> Unit,
    onSetStartWithGps: (Boolean) -> Unit,
    onSetMorningNotif: (Boolean) -> Unit,
    onSetSevereAlerts: (Boolean) -> Unit,
    onSendTestNotif: (Boolean) -> Unit,
    onClearCache: () -> Unit,
    onRemoveFavorite: (CityLocation) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var currentSubScreen by remember { mutableStateOf(SettingsSubScreen.MAIN) }

    var apiKeyText by remember { mutableStateOf(initialCustomKey ?: "") }
    var isKeyVisible by remember { mutableStateOf(false) }
    var selectedCustomMode by remember { mutableStateOf(isUsingCustomKey) }

    var weatherApiKeyText by remember(customWeatherApiKey) { mutableStateOf(customWeatherApiKey ?: "") }
    var isWeatherApiKeyVisible by remember { mutableStateOf(false) }
    var selectedWeatherApiCustomMode by remember(isUsingCustomWeatherApiKey) { mutableStateOf(isUsingCustomWeatherApiKey) }
    var isTestingWeatherApiKey by remember { mutableStateOf(false) }
    var weatherApiTestStatusMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    var googleWeatherKeyText by remember(customGoogleWeatherKey) { mutableStateOf(customGoogleWeatherKey ?: "") }
    var isGoogleKeyVisible by remember { mutableStateOf(false) }
    var selectedGoogleCustomMode by remember(isUsingCustomGoogleWeatherKey) { mutableStateOf(isUsingCustomGoogleWeatherKey) }
    var isTestingGoogleKey by remember { mutableStateOf(false) }
    var testStatusMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }


    // Xử lý động Theme sáng / tối cho toàn bộ hộp thoại Cài đặt
    val isDark = when (currentThemeMode) {
        com.weather24h.app.ui.theme.AppThemeMode.LIGHT -> false
        com.weather24h.app.ui.theme.AppThemeMode.DARK -> true
        com.weather24h.app.ui.theme.AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val dialogBg = if (isDark) Color(0xFF090D16) else Color(0xFFF8FAFC)
    val topButtonBg = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val topButtonIcon = if (isDark) Color.White else Color(0xFF0F172A)
    val titleColor = if (isDark) Color.White else Color(0xFF0F172A)
    val cardBg = if (isDark) Color(0xFF131A2A) else Color.White
    val cardBorder = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFE2E8F0)
    val contentColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val sectionHeaderColor = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF334155)
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)
    val chipBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val chipBorder = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f)

    val refreshOptions = listOf(
        Pair(30, "30 phút"),
        Pair(60, "1 giờ (Chuẩn)"),
        Pair(120, "2 giờ"),
        Pair(180, "3 giờ"),
        Pair(300, "5 giờ"),
        Pair(0, "Tắt (Thủ công)")
    )

    Dialog(
        onDismissRequest = {
            if (currentSubScreen != SettingsSubScreen.MAIN) {
                currentSubScreen = SettingsSubScreen.MAIN
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler(enabled = true) {
            if (currentSubScreen != SettingsSubScreen.MAIN) {
                currentSubScreen = SettingsSubScreen.MAIN
            } else {
                onDismiss()
            }
        }

        Surface(
            color = dialogBg,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Top App Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(topButtonBg)
                            .clickable {
                                if (currentSubScreen == SettingsSubScreen.MAIN) {
                                    onDismiss()
                                } else {
                                    currentSubScreen = SettingsSubScreen.MAIN
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = topButtonIcon,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = when (currentSubScreen) {
                            SettingsSubScreen.MAIN -> "Cài đặt"
                            SettingsSubScreen.THEME -> "Giao diện & Theme"
                            SettingsSubScreen.NOTIFICATIONS -> "Thông báo thời tiết"
                            SettingsSubScreen.LOCATION_CITIES -> "Cài đặt vị trí"
                            SettingsSubScreen.FORECAST_MODEL -> "Nguồn dữ liệu & Mô hình"
                            SettingsSubScreen.GEMINI_API -> "Quản lý API Key Gemini"
                            SettingsSubScreen.SYNC_REFRESH -> "Chu kỳ làm mới"
                            SettingsSubScreen.OFFLINE_WIDGET -> "Ngoại tuyến & Widget"
                            SettingsSubScreen.ABOUT -> "Về ứng dụng"
                        },
                        color = titleColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Nội dung các màn hình cài đặt
                AnimatedContent(
                    targetState = currentSubScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "SettingsNav",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { screen ->
                    when (screen) {
                        // 1. MÀN HÌNH CHÍNH
                        SettingsSubScreen.MAIN -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // NHÓM 0: GIAO DIỆN & THEME
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    SettingsRowItem(
                                        icon = Icons.Default.Palette,
                                        iconTint = Color(0xFFA855F7),
                                        title = "Giao diện & Theme",
                                        subtitle = "${currentThemeMode.title} • Độ mờ khối: ${(cardOpacity * 100).toInt()}% • Ambient: ${ambientGlowStyle.title}",
                                        titleColor = contentColor,
                                        subtitleColor = subtextColor,
                                        onClick = { currentSubScreen = SettingsSubScreen.THEME }
                                    )
                                }

                                // NHÓM 1: THÔNG BÁO THỜI TIẾT
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    val notifStatus = if (periodicNotifHours > 0) "${periodicNotifHours}h/lần" else "Tắt định kỳ"
                                    SettingsRowItem(
                                        icon = Icons.Default.Notifications,
                                        iconTint = Color(0xFF38BDF8),
                                        title = "Thông báo thời tiết",
                                        subtitle = "$notifStatus • ${if (isMorningNotif || isSevereAlerts) "Cảnh báo bật" else "Cảnh báo tắt"}",
                                        titleColor = contentColor,
                                        subtitleColor = subtextColor,
                                        onClick = { currentSubScreen = SettingsSubScreen.NOTIFICATIONS }
                                    )
                                }

                                // NHÓM 2: CÀI ĐẶT VỊ TRÍ
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    SettingsRowItem(
                                        icon = Icons.Default.LocationOn,
                                        iconTint = Color(0xFF34D399),
                                        title = "Cài đặt vị trí",
                                        subtitle = if (isStartWithGps) "Vị trí GPS hiện tại" else defaultCity.name,
                                        titleColor = contentColor,
                                        subtitleColor = subtextColor,
                                        onClick = { currentSubScreen = SettingsSubScreen.LOCATION_CITIES }
                                    )
                                }

                                // NHÓM 2.5: NGUỒN DỮ LIỆU & MÔ HÌNH THỜI TIẾT (GOOGLE WEATHER / WEATHERNEXT AI / OPEN-METEO)
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    val providerSubtitle = when (currentWeatherProvider) {
                                        WeatherProvider.WEATHER_API -> "WeatherAPI (Trạm quan trắc) • Chuẩn nhất"
                                        WeatherProvider.GOOGLE -> "Google Weather (WeatherNext AI)"
                                        WeatherProvider.OPEN_METEO -> "Open-Meteo • ${forecastModel.title}"
                                    }
                                    SettingsRowItem(
                                        icon = Icons.Default.Public,
                                        iconTint = Color(0xFF0EA5E9),
                                        title = "Nguồn dữ liệu & Mô hình",
                                        subtitle = providerSubtitle,
                                        titleColor = contentColor,
                                        subtitleColor = subtextColor,
                                        onClick = { currentSubScreen = SettingsSubScreen.FORECAST_MODEL }
                                    )
                                }


                                // NHÓM 3: TIỆN ÍCH WIDGET (Độc lập)
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    val widgetThemeLabel = when (widgetThemeMode) {
                                        com.weather24h.app.ui.theme.AppThemeMode.LIGHT -> "Sáng"
                                        com.weather24h.app.ui.theme.AppThemeMode.DARK -> "Tối"
                                        com.weather24h.app.ui.theme.AppThemeMode.SYSTEM -> "Theo hệ điều hành"
                                    }
                                    SettingsRowItem(
                                        icon = Icons.Default.Widgets,
                                        iconTint = Color(0xFFF472B6),
                                        title = "Tiện ích Widget",
                                        subtitle = "Theme: $widgetThemeLabel • Nền: ${(widgetOpacity * 100).toInt()}% • Tự động: ${if (isWidgetAutoRefresh) "1h/lần" else "Tắt"}",
                                        titleColor = contentColor,
                                        subtitleColor = subtextColor,
                                        onClick = { currentSubScreen = SettingsSubScreen.OFFLINE_WIDGET }
                                    )
                                }

                                // NHÓM 4: AI STYLIST & CHẾ ĐỘ DỊCH VỤ
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    SettingsRowItem(
                                        icon = Icons.Default.AutoAwesome,
                                        iconTint = Color(0xFFC084FC),
                                        title = "Quản lý API Key Gemini",
                                        subtitle = if (isUsingCustomKey) "Key cá nhân của bạn" else "Key mặc định của app",
                                        titleColor = contentColor,
                                        subtitleColor = subtextColor,
                                        onClick = { currentSubScreen = SettingsSubScreen.GEMINI_API }
                                    )
                                    ItemDivider(dividerColor = dividerColor)
                                    SettingsRowItem(
                                        icon = Icons.Default.Schedule,
                                        iconTint = Color(0xFFFBBF24),
                                        title = "Chu kỳ làm mới",
                                        subtitle = "${currentAutoRefreshMinutes.takeIf { it > 0 }?.let { "$it phút/lần" } ?: "Thủ công"}",
                                        titleColor = contentColor,
                                        subtitleColor = subtextColor,
                                        onClick = { currentSubScreen = SettingsSubScreen.SYNC_REFRESH }
                                    )
                                }

                                // NHÓM 5: VỀ ỨNG DỤNG (Độc lập)
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    SettingsRowItem(
                                        icon = Icons.Default.Info,
                                        iconTint = Color(0xFF94A3B8),
                                        title = "Về ứng dụng",
                                        subtitle = "Phiên bản v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                                        titleColor = contentColor,
                                        subtitleColor = subtextColor,
                                        onClick = { currentSubScreen = SettingsSubScreen.ABOUT }
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }

                        // 1.5. SUB-SCREEN: GIAO DIỆN & THEME
                        SettingsSubScreen.THEME -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // 1. Chế độ hiển thị ứng dụng
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "CHẾ ĐỘ HIỂN THỊ ỨNG DỤNG",
                                            color = sectionHeaderColor,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.7.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))

                                        listOf(
                                            Triple(com.weather24h.app.ui.theme.AppThemeMode.SYSTEM, "Theo thiết bị (Tự động)", "Tự động đổi theme Sáng/Tối theo cài đặt của hệ điều hành"),
                                            Triple(com.weather24h.app.ui.theme.AppThemeMode.DARK, "Chế độ Tối (Khuyên dùng)", "Màu nền sẫm sâu, bảo vệ mắt, tiết kiệm pin OLED và nổi bật icon thời tiết"),
                                            Triple(com.weather24h.app.ui.theme.AppThemeMode.LIGHT, "Chế độ Sáng", "Màu sắc tươi tắn, thanh khiết, chữ đậm rõ nét dưới ánh sáng ngoài trời")
                                        ).forEach { (mode, title, desc) ->
                                            val isSelected = currentThemeMode == mode
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        onSelectThemeMode(mode)
                                                        Toast.makeText(context, "Đã chọn: $title", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = title,
                                                        color = if (isSelected) Color(0xFF38BDF8) else contentColor,
                                                        fontSize = 14.5.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                    )
                                                    Spacer(modifier = Modifier.height(3.dp))
                                                    Text(
                                                        text = desc,
                                                        color = subtextColor,
                                                        fontSize = 11.5.sp,
                                                        lineHeight = 16.sp
                                                    )
                                                }
                                                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                                    if (isSelected) {
                                                        Icon(
                                                            Icons.Default.CheckCircle,
                                                            contentDescription = null,
                                                            tint = Color(0xFF38BDF8),
                                                            modifier = Modifier.size(22.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            ItemDivider(dividerColor = dividerColor)
                                        }
                                    }
                                }

                                // 2. Độ sáng / tối của nền (Background Dimming)
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "ĐỘ SÁNG / TỐI CỦA NỀN",
                                                color = sectionHeaderColor,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.7.sp,
                                                modifier = Modifier.weight(1f, fill = false),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            val dimPercent = (bgDimmingLevel * 100).toInt()
                                            Text(
                                                text = if (dimPercent == 0) "Sáng gốc (0%)" else "Làm tối $dimPercent%",
                                                color = Color(0xFF38BDF8),
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Kéo để làm tối nền giúp chữ và icon nổi bật, dịu mắt hơn khi dùng ban đêm.",
                                            color = subtextColor,
                                            fontSize = 11.5.sp,
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Slider(
                                            value = bgDimmingLevel,
                                            onValueChange = onSetBgDimmingLevel,
                                            valueRange = 0.0f..0.8f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color(0xFF38BDF8),
                                                activeTrackColor = Color(0xFF38BDF8),
                                                inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f)
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // 5 nút preset dàn đều cân đối, responsive mọi màn hình
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf(
                                                Pair(0.0f, "0%"),
                                                Pair(0.2f, "20%"),
                                                Pair(0.4f, "40%"),
                                                Pair(0.6f, "60%"),
                                                Pair(0.8f, "80%")
                                            ).forEach { (preset, label) ->
                                                val isPresSelected = kotlin.math.abs(bgDimmingLevel - preset) < 0.05f
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isPresSelected) Color(0xFF38BDF8).copy(alpha = 0.22f) else chipBg,
                                                    border = BorderStroke(1.dp, if (isPresSelected) Color(0xFF38BDF8) else chipBorder),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { onSetBgDimmingLevel(preset) }
                                                ) {
                                                    Text(
                                                        text = label,
                                                        color = if (isPresSelected) Color(0xFF38BDF8) else contentColor.copy(alpha = 0.85f),
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 7.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // 2.5. Độ mờ & trong suốt các khối thời tiết màn hình chính
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "ĐỘ MỜ KHỐI THỜI TIẾT",
                                                color = sectionHeaderColor,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.7.sp,
                                                modifier = Modifier.weight(1f, fill = false),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            val opacityPercent = (cardOpacity * 100).toInt()
                                            Text(
                                                text = "$opacityPercent%",
                                                color = Color(0xFF38BDF8),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Điều chỉnh độ trong suốt của các thẻ thời tiết trên màn hình chính để nhìn xuyên thấu bầu trời.",
                                            color = subtextColor,
                                            fontSize = 11.5.sp,
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Slider(
                                            value = cardOpacity,
                                            onValueChange = onSetCardOpacity,
                                            valueRange = 0.20f..1.0f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color(0xFF38BDF8),
                                                activeTrackColor = Color(0xFF38BDF8),
                                                inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f)
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // 4 nút preset tiện lợi: 40%, 60%, 80%, 100%
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf(
                                                Pair(0.40f, "40% Mờ"),
                                                Pair(0.60f, "60%"),
                                                Pair(0.80f, "80% Chuẩn"),
                                                Pair(1.00f, "100% Đặc")
                                            ).forEach { (preset, label) ->
                                                val isPresSelected = kotlin.math.abs(cardOpacity - preset) < 0.05f
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isPresSelected) Color(0xFF38BDF8).copy(alpha = 0.22f) else chipBg,
                                                    border = BorderStroke(1.dp, if (isPresSelected) Color(0xFF38BDF8) else chipBorder),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { onSetCardOpacity(preset) }
                                                ) {
                                                    Text(
                                                        text = label,
                                                        color = if (isPresSelected) Color(0xFF38BDF8) else contentColor.copy(alpha = 0.85f),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 7.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // 3. Tùy chỉnh màu gradient & Ambient Light
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "ÁNH SÁNG AMBIENT LIGHT & GRADIENT",
                                                color = sectionHeaderColor,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.7.sp,
                                                modifier = Modifier.weight(1f, fill = false),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            val glowPercent = (ambientGlowIntensity * 100).toInt()
                                            Text(
                                                text = if (glowPercent == 0) "Tắt (0%)" else "Cường độ $glowPercent%",
                                                color = Color(0xFFA855F7),
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Hiệu ứng ánh sáng hào quang phát sáng phía sau các khối thẻ thời tiết.",
                                            color = subtextColor,
                                            fontSize = 11.5.sp,
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))

                                        Slider(
                                            value = ambientGlowIntensity,
                                            onValueChange = onSetAmbientGlowIntensity,
                                            valueRange = 0.0f..1.0f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color(0xFFA855F7),
                                                activeTrackColor = Color(0xFFA855F7),
                                                inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f)
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "PHONG CÁCH ÁNH SÁNG",
                                            color = sectionHeaderColor,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        com.weather24h.app.ui.theme.AmbientGlowStyle.values().forEach { style ->
                                            val isStyleSelected = ambientGlowStyle == style
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        onSelectAmbientGlowStyle(style)
                                                        Toast.makeText(context, "Đã chọn: ${style.title}", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    // Preview gradient dots
                                                    Row(
                                                        modifier = Modifier.padding(end = 12.dp),
                                                        horizontalArrangement = Arrangement.spacedBy((-6).dp)
                                                    ) {
                                                        listOf(style.primaryColor, style.secondaryColor, style.accentColor).forEach { color ->
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(16.dp)
                                                                    .clip(CircleShape)
                                                                    .background(color)
                                                                    .border(1.dp, if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.15f), CircleShape)
                                                            )
                                                        }
                                                    }
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = style.title,
                                                            color = if (isStyleSelected) Color(0xFFA855F7) else contentColor,
                                                            fontSize = 13.5.sp,
                                                            fontWeight = if (isStyleSelected) FontWeight.Bold else FontWeight.Medium
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = style.description,
                                                            color = subtextColor,
                                                            fontSize = 11.sp,
                                                            lineHeight = 15.sp
                                                        )
                                                    }
                                                }
                                                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                                    if (isStyleSelected) {
                                                        Icon(
                                                            Icons.Default.CheckCircle,
                                                            contentDescription = null,
                                                            tint = Color(0xFFA855F7),
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            ItemDivider(dividerColor = dividerColor)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        // 2. SUB-SCREEN: THÔNG BÁO THỜI TIẾT
                        SettingsSubScreen.NOTIFICATIONS -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // 1. Cập nhật thời tiết định kỳ lên thanh thông báo
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "THÔNG BÁO THỜI TIẾT HIỆN TẠI (ĐỊNH KỲ)",
                                            color = sectionHeaderColor,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.7.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Tự động gửi và cập nhật nhiệt độ thời tiết lên thanh thông báo hệ thống theo chu kỳ đã chọn.",
                                            color = subtextColor,
                                            fontSize = 11.5.sp,
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))

                                        listOf(
                                            Pair(1, "Mỗi 1 giờ / lần"),
                                            Pair(2, "Mỗi 2 giờ / lần (Mặc định)"),
                                            Pair(3, "Mỗi 3 giờ / lần"),
                                            Pair(6, "Mỗi 6 giờ / lần"),
                                            Pair(0, "Tắt thông báo định kỳ")
                                        ).forEach { (hours, label) ->
                                            val isSelected = periodicNotifHours == hours
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        onSelectPeriodicNotifHours(hours)
                                                        Toast.makeText(context, "Đã đặt: $label", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .padding(vertical = 11.dp, horizontal = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = if (isSelected) Color(0xFF38BDF8) else contentColor,
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                                    if (isSelected) {
                                                        Icon(
                                                            Icons.Default.CheckCircle,
                                                            contentDescription = null,
                                                            tint = Color(0xFF38BDF8),
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            ItemDivider(dividerColor = dividerColor)
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = {
                                                onSendCurrentWeatherNotif()
                                                Toast.makeText(context, "Đã gửi thông báo thời tiết hiện tại lên thanh thông báo!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Gửi thông báo thời tiết ngay bây giờ", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }

                                // 2. Bản tin sáng & Cảnh báo nguy hiểm
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "BẢN TIN SÁNG & CẢNH BÁO NGUY HIỂM",
                                            color = sectionHeaderColor,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.7.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        ToggleRowItem(
                                            title = "Bản tin thời tiết buổi sáng",
                                            subtitle = "Gửi tóm tắt thời tiết & gợi ý trang phục mỗi sáng (7h00)",
                                            isChecked = isMorningNotif,
                                            titleColor = contentColor,
                                            subtitleColor = subtextColor,
                                            onCheckedChange = onSetMorningNotif
                                        )
                                        ItemDivider(dividerColor = dividerColor)
                                        ToggleRowItem(
                                            title = "Cảnh báo thời tiết nguy hiểm",
                                            subtitle = "Cảnh báo khẩn khi có Dông sét, mưa lớn hoặc tia UV nguy hại",
                                            isChecked = isSevereAlerts,
                                            titleColor = contentColor,
                                            subtitleColor = subtextColor,
                                            onCheckedChange = onSetSevereAlerts
                                        )
                                    }
                                }

                                // 3. Nút Thử nghiệm
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    Button(
                                        onClick = {
                                            onSendTestNotif(false)
                                            Toast.makeText(context, "Đã gửi thông báo buổi sáng mẫu!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = chipBg),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Gửi thử thông báo buổi sáng", color = contentColor, fontSize = 13.sp)
                                    }

                                    Button(
                                        onClick = {
                                            onSendTestNotif(true)
                                            Toast.makeText(context, "Đã gửi cảnh báo dông sét mẫu!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = chipBg),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Gửi thử cảnh báo Dông bão", color = contentColor, fontSize = 13.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        // 3. SUB-SCREEN: CÀI ĐẶT VỊ TRÍ & THÀNH PHỐ
                        SettingsSubScreen.LOCATION_CITIES -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    ToggleRowItem(
                                        title = "Luôn khởi động bằng vị trí GPS",
                                        subtitle = "Tự động nhận diện quận/huyện hiện tại bằng GPS khi mở app",
                                        isChecked = isStartWithGps,
                                        titleColor = contentColor,
                                        subtitleColor = subtextColor,
                                        onCheckedChange = onSetStartWithGps
                                    )
                                }

                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "THÀNH PHỐ MẶC ĐỊNH KHI VÀO APP",
                                            color = sectionHeaderColor,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.7.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf(
                                                CityLocation.DEFAULT_LOCATION,
                                                CityLocation(name = "Đà Nẵng", country = "Việt Nam", latitude = 16.0544, longitude = 108.2022),
                                                CityLocation(name = "TP. Hồ Chí Minh", country = "Việt Nam", latitude = 10.8231, longitude = 106.6297),
                                                CityLocation(name = "Đà Lạt", country = "Việt Nam", latitude = 11.9404, longitude = 108.4583)
                                            ).forEach { city ->
                                                val isSelected = defaultCity.name == city.name
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = {
                                                        onSetDefaultCity(city)
                                                        Toast.makeText(context, "Đã chọn ${city.name} làm mặc định", Toast.LENGTH_SHORT).show()
                                                    },
                                                    label = { Text(city.name) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = Color(0xFF0284C7),
                                                        selectedLabelColor = Color.White,
                                                        containerColor = chipBg,
                                                        labelColor = contentColor
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                // Danh sách thành phố yêu thích
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "DANH SÁCH YÊU THÍCH (${favoriteCities.size})",
                                            color = sectionHeaderColor,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.7.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))

                                        if (favoriteCities.isEmpty()) {
                                            Text(
                                                text = "Chưa có thành phố yêu thích nào. Hãy nhấn biểu tượng ngôi sao ⭐ trên màn hình chính để thêm!",
                                                color = subtextColor,
                                                fontSize = 12.sp
                                            )
                                        } else {
                                            favoriteCities.forEach { city ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD166), modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(city.name, color = contentColor, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                                                    }
                                                    IconButton(onClick = { onRemoveFavorite(city) }, modifier = Modifier.size(28.dp)) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 3.5. SUB-SCREEN: NGUỒN DỮ LIỆU & MÔ HÌNH DỰ BÁO THỜI TIẾT
                        SettingsSubScreen.FORECAST_MODEL -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF0EA5E9).copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Public,
                                                    contentDescription = null,
                                                    tint = Color(0xFF0EA5E9),
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "CÔNG NGHỆ DỮ LIỆU THỜI TIẾT",
                                                    color = sectionHeaderColor,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.7.sp
                                                )
                                                Text(
                                                    text = "Trạm quan trắc thực tế & AI khí tượng",
                                                    color = contentColor,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Weather 24h hỗ trợ 3 nguồn dữ liệu: WeatherAPI (đo đạc trực tiếp từ trạm khí tượng mặt đất & radar tại Việt Nam, độ chính xác cao nhất không cần thẻ), Google Weather (AI WeatherNext của Google DeepMind), và mạng lưới khí tượng toàn cầu Open-Meteo.",
                                            color = subtextColor,
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }

                                // 1. CHỌN NHÀ CUNG CẤP DỮ LIỆU CHÍNH (WEATHERAPI VS GOOGLE WEATHER VS OPEN-METEO)
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "CHỌN NGUỒN DỮ LIỆU CHÍNH",
                                            color = sectionHeaderColor,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.7.sp
                                        )

                                        KeyModeSelectCard(
                                            title = "WeatherAPI (Trạm quan trắc thực tế)",
                                            badge = "Khuyên dùng • Mặc định",
                                            badgeColor = Color(0xFF10B981),
                                            description = "Dữ liệu đo đạc thực tế từ trạm khí tượng mặt đất và hệ thống radar tại Việt Nam. Không bị sai lệch, nhiệt độ cảm nhận (Feels like) và dự báo mưa chính xác 99%. Đã tích hợp sẵn, không cần thẻ.",
                                            isSelected = currentWeatherProvider == WeatherProvider.WEATHER_API,
                                            isDark = isDark,
                                            contentColor = contentColor,
                                            subtextColor = subtextColor,
                                            onClick = {
                                                onSelectWeatherProvider(WeatherProvider.WEATHER_API)
                                                Toast.makeText(context, "Đã chọn: WeatherAPI (Trạm quan trắc thực tế)", Toast.LENGTH_SHORT).show()
                                            }
                                        )

                                        KeyModeSelectCard(
                                            title = "Google Weather (WeatherNext AI)",
                                            badge = "Google AI",
                                            badgeColor = Color(0xFF38BDF8),
                                            description = "Mô hình AI dự báo thời tiết tiên tiến của Google DeepMind. Cùng một nguồn dữ liệu với Google Search và Google Pixel Weather (Cần cấu hình API Key Google Cloud).",
                                            isSelected = currentWeatherProvider == WeatherProvider.GOOGLE,
                                            isDark = isDark,
                                            contentColor = contentColor,
                                            subtextColor = subtextColor,
                                            onClick = {
                                                onSelectWeatherProvider(WeatherProvider.GOOGLE)
                                                Toast.makeText(context, "Đã chọn: Google Weather (WeatherNext AI)", Toast.LENGTH_SHORT).show()
                                            }
                                        )

                                        KeyModeSelectCard(
                                            title = "Open-Meteo (Đa mô hình khí tượng)",
                                            badge = "Đa mô hình • Miễn phí",
                                            badgeColor = Color(0xFFA855F7),
                                            description = "Mạng lưới khí tượng mở toàn cầu, miễn phí 100% không yêu cầu API Key. Cho phép tự do chọn siêu máy tính (ECMWF Châu Âu, GFS Mỹ, ICON Đức, JMA Nhật Bản).",
                                            isSelected = currentWeatherProvider == WeatherProvider.OPEN_METEO,
                                            isDark = isDark,
                                            contentColor = contentColor,
                                            subtextColor = subtextColor,
                                            onClick = {
                                                onSelectWeatherProvider(WeatherProvider.OPEN_METEO)
                                                Toast.makeText(context, "Đã chọn: Open-Meteo", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }

                                // 2. NẾU CHỌN WEATHERAPI -> CẤU HÌNH API KEY
                                if (currentWeatherProvider == WeatherProvider.WEATHER_API) {
                                    InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = "CẤU HÌNH WEATHERAPI.COM",
                                                color = sectionHeaderColor,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.7.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Ứng dụng đã nhúng sẵn Key miễn phí trọn đời. Bạn cũng có thể dùng Key cá nhân (1.000.000 lượt/tháng, chỉ cần email, KHÔNG CẦN THẺ).",
                                                color = subtextColor,
                                                fontSize = 11.5.sp,
                                                lineHeight = 16.sp
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))

                                            KeyModeSelectCard(
                                                title = "Key nhúng sẵn của ứng dụng",
                                                badge = "Sẵn sàng • Miễn phí trọn đời",
                                                badgeColor = Color(0xFF10B981),
                                                description = "Đã tích hợp sẵn ngầm định trong bản dựng. Tự động kết nối trạm quan trắc thực tế tại Việt Nam với độ chính xác cao nhất.",
                                                isSelected = !selectedWeatherApiCustomMode,
                                                isDark = isDark,
                                                contentColor = contentColor,
                                                subtextColor = subtextColor,
                                                onClick = {
                                                    selectedWeatherApiCustomMode = false
                                                    onClearWeatherApiKey()
                                                    weatherApiTestStatusMessage = null
                                                    Toast.makeText(context, "Đã kích hoạt Key nhúng sẵn của app", Toast.LENGTH_SHORT).show()
                                                }
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            KeyModeSelectCard(
                                                title = "Sử dụng API Key WeatherAPI.com cá nhân",
                                                badge = "1.000.000 lượt/tháng",
                                                badgeColor = Color(0xFFA855F7),
                                                description = "Đăng ký miễn phí tại WeatherAPI.com chỉ bằng email trong 30 giây, TUYỆT ĐỐI KHÔNG CẦN THẺ NGÂN HÀNG.",
                                                isSelected = selectedWeatherApiCustomMode,
                                                isDark = isDark,
                                                contentColor = contentColor,
                                                subtextColor = subtextColor,
                                                onClick = { selectedWeatherApiCustomMode = true }
                                            )

                                            if (selectedWeatherApiCustomMode) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                OutlinedTextField(
                                                    value = weatherApiKeyText,
                                                    onValueChange = {
                                                         weatherApiKeyText = it
                                                         weatherApiTestStatusMessage = null
                                                    },
                                                    placeholder = { Text("Dán WeatherAPI Key...", color = subtextColor.copy(alpha = 0.5f), fontSize = 12.sp) },
                                                    visualTransformation = if (isWeatherApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                                    trailingIcon = {
                                                        IconButton(onClick = { isWeatherApiKeyVisible = !isWeatherApiKeyVisible }) {
                                                            Icon(
                                                                imageVector = if (isWeatherApiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                                contentDescription = null,
                                                                tint = subtextColor,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = Color(0xFF38BDF8),
                                                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f),
                                                        focusedTextColor = contentColor,
                                                        unfocusedTextColor = contentColor
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                            val text = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()?.trim()
                                                            if (!text.isNullOrEmpty()) {
                                                                weatherApiKeyText = text
                                                                weatherApiTestStatusMessage = null
                                                                Toast.makeText(context, "Đã dán key từ Clipboard", Toast.LENGTH_SHORT).show()
                                                            }
                                                        },
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Dán key", fontSize = 11.5.sp, maxLines = 1)
                                                    }

                                                    Button(
                                                        onClick = {
                                                            focusManager.clearFocus()
                                                            val keyToTest = weatherApiKeyText.trim()
                                                            if (keyToTest.isEmpty()) {
                                                                Toast.makeText(context, "Vui lòng nhập API Key để kiểm tra", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                isTestingWeatherApiKey = true
                                                                weatherApiTestStatusMessage = null
                                                                onTestWeatherApiKey?.invoke(keyToTest) { isSuccess, message ->
                                                                    isTestingWeatherApiKey = false
                                                                    weatherApiTestStatusMessage = Pair(isSuccess, message)
                                                                }
                                                            }
                                                        },
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                                        enabled = !isTestingWeatherApiKey && weatherApiKeyText.trim().isNotEmpty(),
                                                        modifier = Modifier.weight(1.2f)
                                                    ) {
                                                        if (isTestingWeatherApiKey) {
                                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                                        } else {
                                                            Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(14.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("Kiểm tra", fontSize = 11.5.sp, maxLines = 1)
                                                        }
                                                    }

                                                    Button(
                                                        onClick = {
                                                            focusManager.clearFocus()
                                                            if (weatherApiKeyText.trim().isNotEmpty()) {
                                                                onSaveWeatherApiKey(weatherApiKeyText.trim())
                                                                Toast.makeText(context, "Đã lưu WeatherAPI Key!", Toast.LENGTH_SHORT).show()
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                        shape = RoundedCornerShape(10.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("Lưu Key", color = Color.White, fontSize = 11.5.sp, maxLines = 1)
                                                    }
                                                }

                                                // Banner kết quả kiểm tra
                                                weatherApiTestStatusMessage?.let { (isSuccess, msg) ->
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = if (isSuccess) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                                                        border = BorderStroke(1.dp, if (isSuccess) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.4f)),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                                                contentDescription = null,
                                                                tint = if (isSuccess) Color(0xFF34D399) else Color(0xFFF87171),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = msg,
                                                                color = if (isSuccess) (if (isDark) Color(0xFF34D399) else Color(0xFF047857)) else (if (isDark) Color(0xFFF87171) else Color(0xFFB91C1C)),
                                                                fontSize = 11.5.sp,
                                                                lineHeight = 15.sp
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Nút mở hướng dẫn lấy Key WeatherAPI
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.weatherapi.com/signup.aspx"))
                                                        context.startActivity(intent)
                                                    }
                                                    .padding(10.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "Lấy Key miễn phí tại WeatherAPI (Không cần thẻ)",
                                                            color = Color(0xFF38BDF8),
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = subtextColor.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                // 2. NẾU CHỌN GOOGLE WEATHER -> CẤU HÌNH API KEY
                                if (currentWeatherProvider == WeatherProvider.GOOGLE) {
                                    InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = "CẤU HÌNH GOOGLE WEATHER API KEY",
                                                color = sectionHeaderColor,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.7.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Bạn có thể sử dụng Key mặc định của app hoặc kết nối API Key riêng từ Google Cloud Console.",
                                                color = subtextColor,
                                                fontSize = 11.5.sp,
                                                lineHeight = 16.sp
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))

                                            KeyModeSelectCard(
                                                title = "Key mặc định của ứng dụng",
                                                badge = "Sẵn sàng • Tự động dự phòng",
                                                badgeColor = Color(0xFF10B981),
                                                description = "Đã tích hợp sẵn ngầm định. Tự động chuyển Open-Meteo an toàn nếu mất mạng hoặc hết hạn ngạch.",
                                                isSelected = !selectedGoogleCustomMode,
                                                isDark = isDark,
                                                contentColor = contentColor,
                                                subtextColor = subtextColor,
                                                onClick = {
                                                    selectedGoogleCustomMode = false
                                                    onClearGoogleWeatherKey()
                                                    testStatusMessage = null
                                                    Toast.makeText(context, "Đã kích hoạt Key mặc định của app", Toast.LENGTH_SHORT).show()
                                                }
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            KeyModeSelectCard(
                                                title = "Sử dụng API Key Google Cloud cá nhân",
                                                badge = "Hạn ngạch riêng",
                                                badgeColor = Color(0xFFA855F7),
                                                description = "Nhập API Key từ Google Cloud / Maps Platform (đã bật Weather API) để quản lý độc lập.",
                                                isSelected = selectedGoogleCustomMode,
                                                isDark = isDark,
                                                contentColor = contentColor,
                                                subtextColor = subtextColor,
                                                onClick = { selectedGoogleCustomMode = true }
                                            )

                                            if (selectedGoogleCustomMode) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                OutlinedTextField(
                                                    value = googleWeatherKeyText,
                                                    onValueChange = {
                                                        googleWeatherKeyText = it
                                                        testStatusMessage = null
                                                    },
                                                    placeholder = { Text("Dán Google Weather API Key...", color = subtextColor.copy(alpha = 0.5f), fontSize = 12.sp) },
                                                    visualTransformation = if (isGoogleKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                                    trailingIcon = {
                                                        IconButton(onClick = { isGoogleKeyVisible = !isGoogleKeyVisible }) {
                                                            Icon(
                                                                imageVector = if (isGoogleKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                                contentDescription = null,
                                                                tint = subtextColor,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = Color(0xFF38BDF8),
                                                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f),
                                                        focusedTextColor = contentColor,
                                                        unfocusedTextColor = contentColor
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                // Action Buttons: Dán, Test, Lưu
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                            val text = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()?.trim()
                                                            if (!text.isNullOrEmpty()) {
                                                                googleWeatherKeyText = text
                                                                testStatusMessage = null
                                                                Toast.makeText(context, "Đã dán key từ Clipboard", Toast.LENGTH_SHORT).show()
                                                            }
                                                        },
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Dán key", fontSize = 11.5.sp, maxLines = 1)
                                                    }

                                                    Button(
                                                        onClick = {
                                                            focusManager.clearFocus()
                                                            val keyToTest = googleWeatherKeyText.trim()
                                                            if (keyToTest.isEmpty()) {
                                                                Toast.makeText(context, "Vui lòng nhập API Key để kiểm tra", Toast.LENGTH_SHORT).show()
                                                                return@Button
                                                            }
                                                            isTestingGoogleKey = true
                                                            testStatusMessage = null
                                                            onTestGoogleWeatherKey?.invoke(keyToTest) { isSuccess, message ->
                                                                isTestingGoogleKey = false
                                                                testStatusMessage = Pair(isSuccess, message)
                                                            }
                                                        },
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                                        enabled = !isTestingGoogleKey && googleWeatherKeyText.trim().isNotEmpty(),
                                                        modifier = Modifier.weight(1.2f)
                                                    ) {
                                                        if (isTestingGoogleKey) {
                                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                                        } else {
                                                            Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(14.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("Kiểm tra", fontSize = 11.5.sp, maxLines = 1)
                                                        }
                                                    }

                                                    Button(
                                                        onClick = {
                                                            focusManager.clearFocus()
                                                            if (googleWeatherKeyText.trim().isNotEmpty()) {
                                                                onSaveGoogleWeatherKey(googleWeatherKeyText.trim())
                                                                Toast.makeText(context, "Đã lưu Google Weather API Key!", Toast.LENGTH_SHORT).show()
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                        shape = RoundedCornerShape(10.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("Lưu Key", color = Color.White, fontSize = 11.5.sp, maxLines = 1)
                                                    }
                                                }

                                                // Banner kết quả kiểm tra
                                                testStatusMessage?.let { (isSuccess, msg) ->
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = if (isSuccess) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                                                        border = BorderStroke(1.dp, if (isSuccess) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.4f)),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                                                contentDescription = null,
                                                                tint = if (isSuccess) Color(0xFF34D399) else Color(0xFFF87171),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = msg,
                                                                color = if (isSuccess) (if (isDark) Color(0xFF34D399) else Color(0xFF047857)) else (if (isDark) Color(0xFFF87171) else Color(0xFFB91C1C)),
                                                                fontSize = 11.5.sp,
                                                                lineHeight = 15.sp
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                // Nút mở hướng dẫn lấy Key
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://console.cloud.google.com/google/maps-apis/overview"))
                                                            context.startActivity(intent)
                                                        }
                                                        .padding(10.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = "Cách lấy Key từ Google Cloud Console",
                                                                color = Color(0xFF38BDF8),
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }
                                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = subtextColor.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // 3. NẾU CHỌN OPEN-METEO -> DANH SÁCH MÔ HÌNH KHÍ TƯỢNG TOÀN CẦU
                                if (currentWeatherProvider == WeatherProvider.OPEN_METEO) {
                                    InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(
                                                text = "CHỌN SIÊU MÁY TÍNH / MÔ HÌNH DỰ BÁO",
                                                color = sectionHeaderColor,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.7.sp
                                            )

                                            WeatherForecastModel.values().forEach { model ->
                                                val isSelected = model == forecastModel
                                                ForecastModelSelectCard(
                                                    model = model,
                                                    isSelected = isSelected,
                                                    isDark = isDark,
                                                    contentColor = contentColor,
                                                    subtextColor = subtextColor,
                                                    onClick = {
                                                        onSelectForecastModel(model)
                                                        Toast.makeText(
                                                            context,
                                                            "Đã chuyển sang mô hình: ${model.title}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // 4. CARD BẢO VỆ ZERO-DOWNTIME
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Security,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "CƠ CHẾ DỰ PHÒNG TỰ ĐỘNG (ZERO-DOWNTIME)",
                                                color = contentColor,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Bất cứ khi nào nguồn Google Weather API gặp sự cố đường truyền hoặc hết hạn mức yêu cầu, hệ thống sẽ tự động chuyển tức thì sang Open-Meteo để lấy dự báo thời tiết. Bạn sẽ không bao giờ gặp tình trạng lỗi hay gián đoạn trải nghiệm.",
                                            color = subtextColor,
                                            fontSize = 11.5.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }

                        // 4. SUB-SCREEN: QUẢN LÝ API KEY GEMINI
                        SettingsSubScreen.GEMINI_API -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        KeyModeSelectCard(
                                            title = "Key mặc định của ứng dụng",
                                            badge = "Khuyên dùng • Sẵn sàng",
                                            badgeColor = Color(0xFF10B981),
                                            description = "Model gemini-3.8-flash • Đã nhúng sẵn ngầm định, kết nối ổn định không cần cấu hình.",
                                            isSelected = !selectedCustomMode,
                                            isDark = isDark,
                                            contentColor = contentColor,
                                            subtextColor = subtextColor,
                                            onClick = {
                                                selectedCustomMode = false
                                                onClearCustomKey()
                                                Toast.makeText(context, "Đã kích hoạt Key mặc định của app", Toast.LENGTH_SHORT).show()
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        KeyModeSelectCard(
                                            title = "Sử dụng API Key cá nhân của bạn",
                                            badge = "Tùy chỉnh riêng",
                                            badgeColor = Color(0xFFA855F7),
                                            description = "Nhập Gemini API Key riêng từ Google AI Studio để quản lý quota độc lập.",
                                            isSelected = selectedCustomMode,
                                            isDark = isDark,
                                            contentColor = contentColor,
                                            subtextColor = subtextColor,
                                            onClick = { selectedCustomMode = true }
                                        )

                                        if (selectedCustomMode) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            OutlinedTextField(
                                                value = apiKeyText,
                                                onValueChange = { apiKeyText = it },
                                                placeholder = { Text("Dán key cá nhân...", color = subtextColor.copy(alpha = 0.5f), fontSize = 12.sp) },
                                                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                                trailingIcon = {
                                                    IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                                        Icon(
                                                            imageVector = if (isKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                            contentDescription = null,
                                                            tint = subtextColor,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFFA855F7),
                                                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f),
                                                    focusedTextColor = contentColor,
                                                    unfocusedTextColor = contentColor,
                                                    focusedContainerColor = chipBg,
                                                    unfocusedContainerColor = chipBg
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                        val text = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()?.trim()
                                                        if (!text.isNullOrEmpty()) {
                                                            apiKeyText = text
                                                            Toast.makeText(context, "Đã dán key từ Clipboard", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Dán từ Clipboard", fontSize = 11.5.sp, maxLines = 1)
                                                }

                                                Button(
                                                    onClick = {
                                                        focusManager.clearFocus()
                                                        if (apiKeyText.trim().isNotEmpty()) {
                                                            onSaveCustomKey(apiKeyText.trim())
                                                            Toast.makeText(context, "Đã lưu và kích hoạt Key thành công!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Lưu Key", color = Color.White, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 5. SUB-SCREEN: CHU KỲ LÀM MỚI
                        SettingsSubScreen.SYNC_REFRESH -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "TỰ ĐỘNG LÀM MỚI DỮ LIỆU THỜI TIẾT",
                                            color = sectionHeaderColor,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.7.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))

                                        refreshOptions.forEach { (minutes, label) ->
                                            val isSelected = currentAutoRefreshMinutes == minutes
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable { onSelectAutoRefreshInterval(minutes) }
                                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(label, color = if (isSelected) Color(0xFF38BDF8) else contentColor, fontSize = 14.sp)
                                                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                                    if (isSelected) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                            }
                                            ItemDivider(dividerColor = dividerColor)
                                        }
                                    }
                                }
                            }
                        }

                        // 6. SUB-SCREEN: NGOẠI TUYẾN & TIỆN ÍCH WIDGET
                        SettingsSubScreen.OFFLINE_WIDGET -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // 1. TÙY CHỈNH THEME & ĐỘ TRONG SUỐT CHO WIDGET
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = null,
                                                tint = Color(0xFFF472B6),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "TÙY CHỈNH TIỆN ÍCH WIDGET",
                                                color = contentColor,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // A. Giao diện theme của Widget (Sáng / Tối / Theo hệ thống)
                                        Text(
                                            text = "GIAO DIỆN MÀU WIDGET (THEME)",
                                            color = sectionHeaderColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf(
                                                Triple(com.weather24h.app.ui.theme.AppThemeMode.SYSTEM, "Tự động", "Theo hệ thống"),
                                                Triple(com.weather24h.app.ui.theme.AppThemeMode.DARK, "Theme Tối", "Màu sẫm OLED"),
                                                Triple(com.weather24h.app.ui.theme.AppThemeMode.LIGHT, "Theme Sáng", "Màu thanh thoát")
                                            ).forEach { (mode, label, _) ->
                                                val isModeSel = widgetThemeMode == mode
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = if (isModeSel) Color(0xFFF472B6).copy(alpha = 0.22f) else chipBg,
                                                    border = BorderStroke(1.dp, if (isModeSel) Color(0xFFF472B6) else chipBorder),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable {
                                                            onSelectWidgetThemeMode(mode)
                                                            Toast.makeText(context, "Widget: $label", Toast.LENGTH_SHORT).show()
                                                        }
                                                ) {
                                                    Text(
                                                        text = label,
                                                        color = if (isModeSel) Color(0xFFF472B6) else contentColor,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isModeSel) FontWeight.Bold else FontWeight.Medium,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 8.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // B. Độ trong suốt của nền Widget
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "ĐỘ TRONG SUỐT NỀN WIDGET",
                                                color = sectionHeaderColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                            val opacityPct = (widgetOpacity * 100).toInt()
                                            Text(
                                                text = when (opacityPct) {
                                                    0 -> "0% (Trong suốt hoàn toàn)"
                                                    100 -> "100% (Đặc hoàn toàn)"
                                                    else -> "$opacityPct% (Kính mờ)"
                                                },
                                                color = Color(0xFFF472B6),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Điều chỉnh độ trong suốt của nền Widget màn hình chính. 0% giúp widget hòa vào hình nền điện thoại.",
                                            color = subtextColor,
                                            fontSize = 11.5.sp,
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Slider(
                                            value = widgetOpacity,
                                            onValueChange = onSetWidgetOpacity,
                                            valueRange = 0.0f..1.0f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color(0xFFF472B6),
                                                activeTrackColor = Color(0xFFF472B6),
                                                inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f)
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Presets độ trong suốt
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf(
                                                Pair(0.0f, "0%"),
                                                Pair(0.30f, "30%"),
                                                Pair(0.60f, "60%"),
                                                Pair(0.85f, "85%"),
                                                Pair(1.0f, "100%")
                                            ).forEach { (preset, label) ->
                                                val isPresSelected = kotlin.math.abs(widgetOpacity - preset) < 0.05f
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isPresSelected) Color(0xFFF472B6).copy(alpha = 0.22f) else chipBg,
                                                    border = BorderStroke(1.dp, if (isPresSelected) Color(0xFFF472B6) else chipBorder),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { onSetWidgetOpacity(preset) }
                                                ) {
                                                    Text(
                                                        text = label,
                                                        color = if (isPresSelected) Color(0xFFF472B6) else contentColor.copy(alpha = 0.85f),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))
                                        HorizontalDivider(color = chipBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                                        Spacer(modifier = Modifier.height(12.dp))

                                        // C. Tự động làm mới Widget ngầm 1h/lần
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "TỰ ĐỘNG LÀM MỚI (1H/LẦN)",
                                                    color = contentColor,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Tự động chạy ngầm đồng bộ thời tiết mới nhất lên Widget mỗi 1 giờ ngay cả khi app đang đóng.",
                                                    color = subtextColor,
                                                    fontSize = 11.5.sp,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Switch(
                                                checked = isWidgetAutoRefresh,
                                                onCheckedChange = onSetWidgetAutoRefresh,
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color.White,
                                                    checkedTrackColor = Color(0xFFF472B6)
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // D. Nút làm mới trực tiếp trên Widget
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = chipBg,
                                            border = BorderStroke(1.dp, chipBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🔄",
                                                    fontSize = 16.sp,
                                                    modifier = Modifier.padding(end = 8.dp)
                                                )
                                                Column {
                                                    Text(
                                                        text = "Làm mới trực tiếp trên Widget",
                                                        color = contentColor,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = "Nhấn vào icon 🔄 trên góc phải Widget ngoài màn hình chính để cập nhật tức thì.",
                                                        color = subtextColor,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // 2. THÔNG TIN CÁC KIỂU WIDGET & HƯỚNG DẪN
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Widgets,
                                                contentDescription = null,
                                                tint = Color(0xFFC084FC),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "CÁC KIỂU WIDGET MÀN HÌNH CHÍNH",
                                                color = contentColor,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Kiểu 1: Dự báo chi tiết theo giờ (4x2)
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = chipBg,
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    text = "1. Widget Dự báo chi tiết theo giờ (4x2)",
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Hiển thị đầy đủ nhiệt độ, thời tiết hiện tại, thanh dự báo 6 mốc giờ tới kèm xác suất mưa (%) và lời khuyên phối đồ AI Stylist.",
                                                    color = subtextColor,
                                                    fontSize = 11.5.sp,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }

                                        // Kiểu 2: Widget Tối giản & Đồng hồ (2x1 / 3x1)
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = chipBg,
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    text = "2. Widget Tối giản & Đồng hồ (2x1 / 3x1)",
                                                    color = Color(0xFF34D399),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Thiết kế siêu gọn, thanh lịch: biểu tượng thời tiết ngày/đêm lớn, nhiệt độ to rõ ràng, độ ẩm và phạm vi Cao/Thấp trong ngày.",
                                                    color = subtextColor,
                                                    fontSize = 11.5.sp,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }

                                        Text(
                                            text = "Cách thêm Widget ra màn hình điện thoại:\n• Bước 1: Nhấn giữ vào vùng trống trên màn hình chính của bạn.\n• Bước 2: Chọn mục 'Tiện ích' (Widgets).\n• Bước 3: Tìm ứng dụng 'Weather 24h' và kéo thả Widget bạn thích ra màn hình!",
                                            color = subtextColor,
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }

                                // 3. BỘ NHỚ ĐỆM NGOẠI TUYẾN
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Storage,
                                                contentDescription = null,
                                                tint = Color(0xFF38BDF8),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "BỘ NHỚ ĐỆM NGOẠI TUYẾN (CACHE)",
                                                color = contentColor,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Dữ liệu thời tiết và gợi ý phong cách Gemini được tự động lưu vào máy. Khi bạn mất kết nối mạng hoặc ở vùng sóng yếu, ứng dụng vẫn hoạt động tức thì với dữ liệu đã lưu.",
                                            color = subtextColor,
                                            fontSize = 12.5.sp,
                                            lineHeight = 18.sp
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Button(
                                            onClick = {
                                                onClearCache()
                                                Toast.makeText(context, "Đã xóa toàn bộ bộ nhớ đệm ngoại tuyến!", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.2f)),
                                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Xóa bộ nhớ đệm ngoại tuyến", color = Color(0xFFFCA5A5), fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }

                        // 7. SUB-SCREEN: VỀ ỨNG DỤNG
                        SettingsSubScreen.ABOUT -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                InsetGroupCard(cardBg = cardBg, cardBorder = cardBorder) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(RoundedCornerShape(22.dp))
                                                .background(Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF7C3AED)))),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Cloud, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("Weather 24h", color = contentColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                        Text("Phiên bản v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})", color = subtextColor, fontSize = 12.sp)

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Thông tin tác giả: CD
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF38BDF8).copy(alpha = if (isDark) 0.15f else 0.10f),
                                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = Color(0xFF38BDF8),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Tác giả: CD",
                                                    color = Color(0xFF0284C7),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.5.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = "Ứng dụng thời tiết cao cấp được phát triển 100% bằng Native Android Kotlin & Jetpack Compose, tích hợp trí tuệ nhân tạo Google Gemini 3.8 Flash và API Open-Meteo.",
                                            color = subtextColor,
                                            fontSize = 12.5.sp,
                                            lineHeight = 18.sp,
                                            textAlign = TextAlign.Center
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

// Khối Card Inset bo góc kiểu iOS / HyperOS theo theme sáng/tối
@Composable
private fun InsetGroupCard(
    cardBg: Color,
    cardBorder: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

// Hàng tùy chọn chuẩn 2 dòng: dòng trên là Tiêu đề, dòng dưới là Tính năng/Cài đặt hiện tại
@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String? = null,
    titleColor: Color,
    subtitleColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = titleColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        color = subtitleColor,
                        fontSize = 12.5.sp,
                        maxLines = 2,
                        lineHeight = 16.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = subtitleColor.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ToggleRowItem(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    titleColor: Color,
    subtitleColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = titleColor, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = subtitleColor, fontSize = 11.5.sp, lineHeight = 16.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0284C7)
            )
        )
    }
}

@Composable
private fun ItemDivider(dividerColor: Color) {
    Divider(
        color = dividerColor,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun KeyModeSelectCard(
    title: String,
    badge: String,
    badgeColor: Color,
    description: String,
    isSelected: Boolean,
    isDark: Boolean,
    contentColor: Color,
    subtextColor: Color,
    onClick: () -> Unit
) {
    val bg = if (isSelected) {
        if (isDark) Color(0xFF1E293B) else Color(0xFFF3E8FF)
    } else {
        if (isDark) Color.White.copy(alpha = 0.04f) else Color(0xFFF8FAFC)
    }
    val border = if (isSelected) Color(0xFFA855F7) else if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE2E8F0)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.2.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = contentColor,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = if (isDark) 0.2f else 0.15f))
                        .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                ) {
                    Text(badge, color = if (isDark) Color.White else badgeColor, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(description, color = subtextColor, fontSize = 11.5.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun ForecastModelSelectCard(
    model: WeatherForecastModel,
    isSelected: Boolean,
    isDark: Boolean,
    contentColor: Color,
    subtextColor: Color,
    onClick: () -> Unit
) {
    val bg = if (isSelected) {
        if (isDark) Color(0xFF0C243B) else Color(0xFFE0F2FE)
    } else {
        if (isDark) Color.White.copy(alpha = 0.04f) else Color(0xFFF8FAFC)
    }
    val border = if (isSelected) Color(0xFF0284C7) else if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE2E8F0)
    val badgeColor = when (model) {
        WeatherForecastModel.BEST_MATCH -> Color(0xFF10B981)
        WeatherForecastModel.ECMWF -> Color(0xFF3B82F6)
        WeatherForecastModel.GFS -> Color(0xFFF59E0B)
        WeatherForecastModel.ICON -> Color(0xFF8B5CF6)
        WeatherForecastModel.JMA -> Color(0xFFEC4899)
        WeatherForecastModel.METEO_FRANCE -> Color(0xFF06B6D4)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(if (isSelected) 1.5.dp else 1.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = model.title,
                        color = contentColor,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = if (isDark) 0.22f else 0.15f))
                        .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = model.badge,
                        color = if (isDark) Color.White else badgeColor,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Cơ quan: ${model.provider}",
                color = Color(0xFF0284C7),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = model.description,
                color = subtextColor,
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )
        }
    }
}

