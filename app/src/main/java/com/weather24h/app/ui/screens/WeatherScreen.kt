package com.weather24h.app.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.weather24h.app.ui.theme.getSkyGradient

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    onRequestGps: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val autoRefreshMinutes by viewModel.autoRefreshMinutes.collectAsState()

    val favoriteCities by viewModel.favoriteCities.collectAsState()
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
    val isWidgetAutoRefresh by viewModel.isWidgetAutoRefresh.collectAsState()
    val forecastModel by viewModel.forecastModel.collectAsState()
    val weatherProvider by viewModel.weatherProvider.collectAsState()
    val customWeatherApiKey by viewModel.customWeatherApiKey.collectAsState()
    val customGoogleWeatherKey by viewModel.customGoogleWeatherKey.collectAsState()

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getSkyGradient(weatherType, isDay, isDarkTheme))
    ) {
        // Living dynamic ambient light & glow overlay
        AmbientGlowOverlay(
            style = ambientGlowStyle,
            intensity = ambientGlowIntensity,
            weatherType = weatherType,
            isDay = isDay
        )

        // Living dynamic weather atmosphere effect (Sun rays, rain streaks, storm lightning, clouds, stars)
        WeatherAtmosphereEffect(
            type = weatherType,
            isDay = isDay
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Weather 24h",
                        color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

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
                            tint = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                            modifier = Modifier
                                .size(22.dp)
                                .graphicsLayer(rotationZ = refreshRotation)
                        )
                    }

                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cài đặt",
                            tint = if (isDarkTheme) Color(0xFFF0ABFC) else Color(0xFF9333EA),
                            modifier = Modifier.size(22.dp)
                        )
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
                        // Floating badge indicator khi kéo hoặc đang cập nhật
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
                                    1.dp,
                                    if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color(0xFF0F172A).copy(alpha = 0.15f)
                                ),
                                shadowElevation = 6.dp,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
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

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 1. Current Weather Hero Card (With Favorite Star ⭐)
                            item {
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
                                item {
                                    SevereWeatherAlertCard(alerts = severeAlerts)
                                }
                            }

                            // 2. Hourly 24h Forecast Section
                            item {
                                HourlyForecastSection(hourlyList = weather.hourly24h)
                            }

                            // 3. AI Smart Outfit Stylist Card (Renders shimmer skeleton when loading)
                            item {
                                OutfitStylistCard(
                                    outfit = state.outfit,
                                    isLoading = state.isOutfitLoading,
                                    onRefresh = { viewModel.refreshOutfit() },
                                    onConfigureApiKey = { showSettingsDialog = true }
                                )
                            }

                            // 4. Bản đồ Radar Mưa & Mây động trực tiếp (RainViewer / Satellite)
                            item {
                                WeatherRadarCard(weather = weather)
                            }

                            // 5. Chất lượng không khí & Bụi mịn PM2.5 / PM10 (AQI chuyên sâu)
                            item {
                                AirQualityCard(weather = weather)
                            }

                            // 6. 7-Day Forecast Section
                            item {
                                DailyForecastSection(dailyList = weather.daily7Days)
                            }

                            // 6. Chỉ số Sức khỏe & Hoạt động Ngoài trời (Phơi đồ, Rửa xe, Nồm ẩm, v.v.)
                            item {
                                LifestyleIndexCard(weather = weather)
                            }

                            // 7. Detailed Weather Metrics Grid
                            item {
                                WeatherMetricsGrid(weather = weather)
                            }

                            item {
                                Text(
                                    text = "Nguồn: ${weather.providerName}${if (weather.isOffline) " • Dữ liệu ngoại tuyến" else ""}",
                                    color = com.weather24h.app.ui.theme.LocalSubtextColor.current,
                                    fontSize = 9.sp,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                            }

                        }
                    }
                }
            }
        }

        // Dialogs
        if (showSearchDialog) {
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
                widgetThemeMode = widgetThemeMode,
                widgetOpacity = widgetOpacity,
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

                onSelectWidgetThemeMode = { viewModel.setWidgetThemeMode(it) },
                onSetWidgetOpacity = { viewModel.setWidgetOpacity(it) },
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
                }
            )
        }
    }
}
