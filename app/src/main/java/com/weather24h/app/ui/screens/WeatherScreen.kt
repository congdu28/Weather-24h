package com.weather24h.app.ui.screens

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import com.weather24h.app.R
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.weather24h.app.data.model.SevereWeatherDetector
import com.weather24h.app.data.model.WeatherCondition
import com.weather24h.app.data.model.WeatherType
import com.weather24h.app.ui.components.*
import com.weather24h.app.ui.theme.AtmospherePerformanceMode
import com.weather24h.app.ui.theme.getSkyGradient
import com.weather24h.app.ui.theme.rememberAnimatedSkyGradient

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    onRequestGps: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val updateState by viewModel.updateUiState.collectAsState()
    val newUpdateInfo by viewModel.newUpdateAvailableInfo.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val autoRefreshMinutes by viewModel.autoRefreshMinutes.collectAsState()

    val favoriteCities by viewModel.favoriteCities.collectAsState()
    val favoriteSummaries by viewModel.favoriteSummaries.collectAsState()
    val defaultCity by viewModel.defaultCity.collectAsState()
    val isStartWithGps by viewModel.isStartWithGps.collectAsState()
    val isMorningNotif by viewModel.isMorningNotif.collectAsState()
    val isSevereAlerts by viewModel.isSevereAlerts.collectAsState()
    val periodicNotifHours by viewModel.periodicNotifHours.collectAsState()
    val bgDimmingLevel by viewModel.bgDimmingLevel.collectAsState()
    val cardOpacity by viewModel.cardOpacity.collectAsState()
    val ambientGlowStyle by viewModel.ambientGlowStyle.collectAsState()
    val ambientGlowIntensity by viewModel.ambientGlowIntensity.collectAsState()
    val widgetThemeMode by viewModel.widgetThemeMode.collectAsState()
    val widgetOpacity by viewModel.widgetOpacity.collectAsState()
    val widgetStyle by viewModel.widgetStyle.collectAsState()
    val isWidgetAutoRefresh by viewModel.isWidgetAutoRefresh.collectAsState()
    val forecastModel by viewModel.forecastModel.collectAsState()
    val weatherProvider by viewModel.weatherProvider.collectAsState()
    val customWeatherApiKey by viewModel.customWeatherApiKey.collectAsState()
    val customGoogleWeatherKey by viewModel.customGoogleWeatherKey.collectAsState()

    val atmospherePerformanceMode by viewModel.atmospherePerformanceMode.collectAsState()

    // Quản lý trạng thái cuộn danh sách để tối ưu hiệu năng render hạt (60/120 FPS không giật lag)
    val listState = rememberLazyListState()
    val isScrolling by remember { derivedStateOf { listState.isScrollInProgress } }

    // Tự động nhận diện chế độ Tiết kiệm pin của hệ điều hành Android
    val powerManager = remember(context) { context.getSystemService(Context.POWER_SERVICE) as? PowerManager }
    var isPowerSaveMode by remember { mutableStateOf(powerManager?.isPowerSaveMode == true) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                isPowerSaveMode = powerManager?.isPowerSaveMode == true
            }
        }
        val filter = IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        context.registerReceiver(receiver, filter)
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    var showSearchDialog by remember { mutableStateOf(false) }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var lastBackPressTime by remember { mutableStateOf(0L) }

    // Xử lý chuẩn hóa nút trở về (Back): Nếu đang ở màn hình chính, nhấn 2 lần để thoát
    BackHandler(enabled = !showSearchDialog && !showSettingsDialog) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000L) {
            (context as? Activity)?.finish()
        } else {
            lastBackPressTime = currentTime
            Toast.makeText(context, "Nhấn lần nữa để thoát Weather 24h", Toast.LENGTH_SHORT).show()
        }
    }

    val weatherType = when (val state = uiState) {
        is WeatherUiState.Success -> state.weather.current.condition.type
        else -> WeatherType.CLEAR
    }
    val isDay = when (val state = uiState) {
        is WeatherUiState.Success -> state.weather.current.isDay
        else -> true
    }
    val themeMode by viewModel.themeMode.collectAsState()
    val isDarkTheme = com.weather24h.app.ui.theme.LocalIsDarkTheme.current

    val animatedSkyGradient = rememberAnimatedSkyGradient(weatherType, isDay, isDarkTheme)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedSkyGradient)
    ) {
        // Living dynamic ambient light & glow overlay
        AmbientGlowOverlay(
            style = ambientGlowStyle,
            intensity = ambientGlowIntensity,
            weatherType = weatherType,
            isDay = isDay,
            isScrolling = isScrolling,
            isPowerSaveMode = isPowerSaveMode || atmospherePerformanceMode == AtmospherePerformanceMode.BATTERY_SAVER
        )

        // Living dynamic weather atmosphere effect (Sun rays, rain streaks, storm lightning, clouds, stars)
        WeatherAtmosphereEffect(
            type = weatherType,
            isDay = isDay,
            isScrolling = isScrolling,
            isPowerSaveMode = isPowerSaveMode,
            performanceMode = atmospherePerformanceMode
        )

        // Lớp phủ làm tối nền người dùng tùy chỉnh (Background Dimming Level)
        if (bgDimmingLevel > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = bgDimmingLevel))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Weather 24h Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Nút cập nhật thời tiết mới nhất (hiệu ứng xoay tròn mượt mà)
                    val infiniteTransition = rememberInfiniteTransition(label = "RefreshInfinite")
                    val refreshRotation by if (isRefreshing) {
                        infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(900, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "Spinning"
                        )
                    } else {
                        animateFloatAsState(
                            targetValue = 0f,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "SpinReset"
                        )
                    }

                    IconButton(
                        onClick = {
                            if (viewModel.isCurrentCityGps()) {
                                onRequestGps(true)
                            } else {
                                viewModel.refreshWeather(isPullRefresh = true)
                            }
                        },
                        enabled = !isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Cập nhật thời tiết mới nhất",
                            tint = Color.White,
                            modifier = Modifier
                                .size(22.dp)
                                .graphicsLayer(rotationZ = refreshRotation)
                        )
                    }

                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cài đặt",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Thông báo nhỏ khi có bản cập nhật mới (Floating Mini Update Banner)
            AnimatedVisibility(
                visible = newUpdateInfo != null,
                enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut()
            ) {
                newUpdateInfo?.let { info ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = (if (isDarkTheme) Color(0xFF0F172A) else Color(0xFF0284C7)).copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, (if (isDarkTheme) Color(0xFF38BDF8) else Color.White).copy(alpha = 0.45f)),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openUpdateDialogFromBanner() }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "🚀", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Có bản cập nhật mới: v${info.latestVersion}",
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Chạm để nâng cấp trải nghiệm",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isDarkTheme) Color(0xFF38BDF8) else Color.White,
                                    modifier = Modifier.clickable { viewModel.openUpdateDialogFromBanner() }
                                ) {
                                    Text(
                                        text = "Cập nhật",
                                        color = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFF0284C7),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = { viewModel.dismissUpdateBanner() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Đóng thông báo",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Main Content Area
            when (val state = uiState) {
                is WeatherUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = if (isDarkTheme) Color.White else Color(0xFF0284C7))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Đang tải dữ liệu Weather 24h...",
                                color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                                fontSize = 15.sp
                            )
                        }
                    }
                }
                is WeatherUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = state.message,
                                color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.refreshWeather() }) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Thử lại")
                            }
                        }
                    }
                }
                is WeatherUiState.Success -> {
                    val weather = state.weather
                    val severeAlerts = remember(weather) { SevereWeatherDetector.detectAlerts(weather) }

                    // Cơ chế Pull-To-Refresh mượt mà và trực quan
                    var pullOffset by remember { mutableFloatStateOf(0f) }
                    val density = LocalDensity.current
                    val pullThreshold = with(density) { 72.dp.toPx() }
                    val maxPull = with(density) { 120.dp.toPx() }

                    val nestedScrollConnection = remember {
                        object : NestedScrollConnection {
                            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                                if (source == NestedScrollSource.Drag && pullOffset > 0f && available.y < 0f) {
                                    val consumed = available.y.coerceAtLeast(-pullOffset)
                                    pullOffset += consumed
                                    return Offset(0f, consumed)
                                }
                                return Offset.Zero
                            }

                            override fun onPostScroll(
                                consumed: Offset,
                                available: Offset,
                                source: NestedScrollSource
                            ): Offset {
                                if (source == NestedScrollSource.Drag && available.y > 0f) {
                                    pullOffset = (pullOffset + available.y * 0.45f).coerceAtMost(maxPull)
                                    return Offset(0f, available.y)
                                }
                                return Offset.Zero
                            }

                            override suspend fun onPreFling(available: Velocity): Velocity {
                                if (pullOffset >= pullThreshold && !isRefreshing) {
                                    if (viewModel.isCurrentCityGps()) {
                                        onRequestGps(true)
                                    } else {
                                        viewModel.refreshWeather(isPullRefresh = true)
                                    }
                                }
                                pullOffset = 0f
                                return Velocity.Zero
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(nestedScrollConnection)
                    ) {
                        PullRefreshBadge(
                            pullOffset = pullOffset,
                            pullThreshold = pullThreshold,
                            isRefreshing = isRefreshing,
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )

                        val listState = rememberLazyListState()

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 0. Thanh chuyển đổi nhanh Địa điểm Yêu thích & GPS
                            if (favoriteCities.isNotEmpty()) {
                                item(key = "favorite_quick_bar", contentType = "quickbar") {
                                    FavoriteCitiesQuickBar(
                                        currentCityName = weather.location.name,
                                        isCurrentGps = weather.location.isCurrentLocation,
                                        favoriteCities = favoriteCities,
                                        favoriteSummaries = favoriteSummaries,
                                        onSelectCity = { city -> viewModel.loadWeather(city) },
                                        onSelectGps = { onRequestGps(false) },
                                        onOpenSearch = { showSearchDialog = true }
                                    )
                                }
                            }

                            // 1. Current Weather Hero Card (With Favorite Star ⭐)
                            item(key = "current_weather_hero", contentType = "hero") {
                                val isFav = remember(favoriteCities, weather.location.name) {
                                    favoriteCities.any { it.name.equals(weather.location.name, ignoreCase = true) }
                                }
                                CurrentWeatherCard(
                                    weather = weather,
                                    onLocationClick = { showSearchDialog = true },
                                    isFavorite = isFav,
                                    onToggleFavorite = { viewModel.toggleFavoriteCity(weather.location) }
                                )
                            }

                            // 1b. Cảnh báo thời tiết nguy hiểm / cực đoan (Nếu có)
                            if (severeAlerts.isNotEmpty()) {
                                item(key = "severe_weather_alerts", contentType = "alert") {
                                    SevereWeatherAlertCard(alerts = severeAlerts)
                                }
                            }

                            // 2. Hourly 24h Forecast Section
                            item(key = "hourly_forecast_section", contentType = "hourly") {
                                HourlyForecastSection(hourlyList = weather.hourly24h)
                            }

                            // 3. AI Smart Stylist Card (Renders shimmer skeleton when loading)
                            item(key = "outfit_stylist_card", contentType = "stylist") {
                                OutfitStylistCard(
                                    outfit = state.outfit,
                                    isLoading = state.isOutfitLoading,
                                    onRefresh = { viewModel.refreshOutfit() },
                                    onConfigureApiKey = { showSettingsDialog = true }
                                )
                            }

                            // 4. Bản đồ Radar Mưa & Mây động trực tiếp (RainViewer / Satellite)
                            item(key = "weather_radar_card", contentType = "radar") {
                                WeatherRadarCard(weather = weather)
                            }

                            // 5. Chất lượng không khí & Bụi mịn PM2.5 / PM10 (AQI chuyên sâu)
                            item(key = "air_quality_card", contentType = "aqi") {
                                AirQualityCard(weather = weather)
                            }

                            // 6. 7-Day Forecast Section
                            item(key = "daily_forecast_section", contentType = "daily") {
                                DailyForecastSection(dailyList = weather.daily7Days)
                            }

                            // 7. Chỉ số Sức khỏe & Hoạt động Ngoài trời (Phơi đồ, Rửa xe, Nồm ẩm, v.v.)
                            item(key = "lifestyle_index_card", contentType = "lifestyle") {
                                LifestyleIndexCard(weather = weather)
                            }

                            // 8. Detailed Weather Metrics Grid
                            item(key = "weather_metrics_grid", contentType = "metrics") {
                                WeatherMetricsGrid(weather = weather)
                            }

                            // Footer Updated Time & Nguồn dữ liệu
                            item(key = "weather_footer", contentType = "footer") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Nguồn: ${weather.providerName}${if (weather.isOffline) " • Dữ liệu ngoại tuyến" else ""} • Cập nhật lúc ${weather.lastUpdated}",
                                        color = if (isDarkTheme) Color.White.copy(alpha = 0.55f) else Color(0xFF475569),
                                        fontSize = 11.5.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }

                        }
                    }
                }
            }
        }

        // Dialogs
        if (showSearchDialog) {
            val currentCityName = (uiState as? WeatherUiState.Success)?.weather?.location?.name ?: ""
            CitySearchDialog(
                onDismiss = { showSearchDialog = false },
                onSearch = { viewModel.searchCities(it) },
                searchResults = searchResults,
                isSearching = isSearching,
                onSelectCity = { city ->
                    viewModel.loadWeather(city)
                },
                onUseGps = {
                    onRequestGps(false)
                },
                favoriteCities = favoriteCities,
                currentCityName = currentCityName,
                favoriteSummaries = favoriteSummaries,
                onToggleFavorite = { city ->
                    viewModel.toggleFavoriteCity(city)
                }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                currentThemeMode = themeMode,
                currentWeatherProvider = weatherProvider,
                customWeatherApiKey = customWeatherApiKey,
                isUsingCustomWeatherApiKey = viewModel.isUsingCustomWeatherApiKey(),
                hasEffectiveWeatherApiKey = viewModel.hasWeatherApiKey(),
                customGoogleWeatherKey = customGoogleWeatherKey,
                isUsingCustomGoogleWeatherKey = viewModel.isUsingCustomGoogleWeatherKey(),
                hasEffectiveGoogleWeatherKey = viewModel.hasGoogleWeatherKey(),
                initialCustomKey = viewModel.getCustomApiKey(),
                isUsingCustomKey = viewModel.isUsingCustomKey(),
                hasDefaultKey = viewModel.hasDefaultKey(),
                currentAutoRefreshMinutes = autoRefreshMinutes,
                defaultCity = defaultCity,
                isStartWithGps = isStartWithGps,
                isMorningNotif = isMorningNotif,
                isSevereAlerts = isSevereAlerts,
                periodicNotifHours = periodicNotifHours,
                bgDimmingLevel = bgDimmingLevel,
                cardOpacity = cardOpacity,
                ambientGlowStyle = ambientGlowStyle,
                ambientGlowIntensity = ambientGlowIntensity,
                atmospherePerformanceMode = atmospherePerformanceMode,
                widgetThemeMode = widgetThemeMode,
                widgetOpacity = widgetOpacity,
                widgetStyle = widgetStyle,
                isWidgetAutoRefresh = isWidgetAutoRefresh,
                forecastModel = forecastModel,
                favoriteCities = favoriteCities,
                onDismiss = { showSettingsDialog = false },
                onSelectThemeMode = { viewModel.setThemeMode(it) },
                onSelectWeatherProvider = { viewModel.setWeatherProvider(it) },
                onSaveWeatherApiKey = { viewModel.saveWeatherApiKey(it) },
                onClearWeatherApiKey = { viewModel.clearWeatherApiKey() },
                onTestWeatherApiKey = { key, onResult ->
                    viewModel.testWeatherApiKey(key, onResult)
                },
                onSaveGoogleWeatherKey = { viewModel.saveGoogleWeatherKey(it) },
                onClearGoogleWeatherKey = { viewModel.clearGoogleWeatherKey() },
                onTestGoogleWeatherKey = { key, onResult ->
                    viewModel.testGoogleWeatherKey(key, onResult)
                },
                onSetBgDimmingLevel = { viewModel.setBgDimmingLevel(it) },
                onSetCardOpacity = { viewModel.setCardOpacity(it) },
                onSelectAmbientGlowStyle = { viewModel.setAmbientGlowStyle(it) },
                onSetAmbientGlowIntensity = { viewModel.setAmbientGlowIntensity(it) },
                onSelectAtmospherePerformanceMode = { viewModel.setAtmospherePerformanceMode(it) },

                onSelectWidgetThemeMode = { viewModel.setWidgetThemeMode(it) },
                onSetWidgetOpacity = { viewModel.setWidgetOpacity(it) },
                onSelectWidgetStyle = { viewModel.setWidgetStyle(it) },
                onSetWidgetAutoRefresh = { viewModel.setWidgetAutoRefresh(it) },
                onSelectForecastModel = { viewModel.setForecastModel(it) },
                onSelectPeriodicNotifHours = { viewModel.setPeriodicNotifHours(it) },
                onSendCurrentWeatherNotif = { viewModel.sendCurrentWeatherNotification() },
                onSaveCustomKey = { key ->
                    viewModel.saveCustomGeminiApiKey(key)
                },
                onClearCustomKey = {
                    viewModel.clearCustomGeminiApiKey()
                },
                onSelectAutoRefreshInterval = { minutes ->
                    viewModel.setAutoRefreshInterval(minutes)
                },
                onSetDefaultCity = { city ->
                    viewModel.setDefaultCity(city)
                },
                onSetStartWithGps = { enabled ->
                    viewModel.setStartWithGps(enabled)
                },
                onSetMorningNotif = { enabled ->
                    viewModel.setMorningNotification(enabled)
                },
                onSetSevereAlerts = { enabled ->
                    viewModel.setSevereAlerts(enabled)
                },
                onSendTestNotif = { isAlert ->
                    viewModel.sendTestNotification(isAlert)
                },
                onClearCache = {
                    viewModel.clearOfflineCache()
                },
                onRemoveFavorite = { city ->
                    viewModel.toggleFavoriteCity(city)
                },
                onCheckForUpdates = {
                    viewModel.checkForUpdates(silent = false)
                }
            )
        }

        // Hộp thoại tự động cập nhật ứng dụng (In-App Self-Update)
        AppUpdateDialog(
            state = updateState,
            onDismiss = { viewModel.dismissUpdateDialog() },
            onStartDownload = { info -> viewModel.startDownloadUpdate(info) },
            onInstall = { file -> viewModel.installDownloadedApk(file) },
            onRetry = { viewModel.checkForUpdates(silent = false) }
        )
    }
}

@Composable
private fun PullRefreshBadge(
    pullOffset: Float,
    pullThreshold: Float,
    isRefreshing: Boolean,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val showPullIndicator = pullOffset > 10f || isRefreshing
    val pullProgress = (pullOffset / pullThreshold).coerceIn(0f, 1f)

    val indicatorAlpha by animateFloatAsState(
        targetValue = if (showPullIndicator) 1f else 0f,
        animationSpec = tween(200),
        label = "PullAlpha"
    )

    if (indicatorAlpha > 0.01f) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDarkTheme) Color(0xFF1E293B).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f),
            border = BorderStroke(
                0.5.dp,
                if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color(0xFF0F172A).copy(alpha = 0.08f)
            ),
            shadowElevation = 6.dp,
            modifier = modifier
                .zIndex(10f)
                .padding(top = 8.dp)
                .graphicsLayer(alpha = indicatorAlpha)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(15.dp),
                        color = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đang cập nhật thời tiết...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkTheme) Color.White else Color(0xFF0F172A)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        modifier = Modifier
                            .size(15.dp)
                            .graphicsLayer(rotationZ = pullProgress * 180f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (pullOffset >= pullThreshold) "Thả tay để cập nhật" else "Vuốt xuống để cập nhật",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkTheme) Color.White else Color(0xFF0F172A)
                    )
                }
            }
        }
    }
}
