package com.weather24h.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather24h.app.data.model.WeatherCondition
import com.weather24h.app.data.model.WeatherType
import com.weather24h.app.ui.components.*
import com.weather24h.app.ui.theme.getSkyGradient

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    onRequestGps: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val autoRefreshMinutes by viewModel.autoRefreshMinutes.collectAsState()

    val favoriteCities by viewModel.favoriteCities.collectAsState()
    val defaultCity by viewModel.defaultCity.collectAsState()
    val isStartWithGps by viewModel.isStartWithGps.collectAsState()
    val isMorningNotif by viewModel.isMorningNotif.collectAsState()
    val isSevereAlerts by viewModel.isSevereAlerts.collectAsState()

    var showSearchDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val weatherType = when (val state = uiState) {
        is WeatherUiState.Success -> WeatherCondition.fromWmoCode(state.weather.current.weatherCode).type
        else -> WeatherType.CLEAR
    }
    val isDay = when (val state = uiState) {
        is WeatherUiState.Success -> state.weather.current.isDay
        else -> true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getSkyGradient(weatherType, isDay))
    ) {
        // Living dynamic weather atmosphere effect (Sun rays, rain streaks, storm lightning, clouds, stars)
        WeatherAtmosphereEffect(
            type = weatherType,
            isDay = isDay
        )

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
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Weather 24h",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
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
                            tint = Color(0xFFF0ABFC),
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
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Đang tải dữ liệu Weather 24h...",
                                color = Color.White,
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
                                color = Color.White,
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

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Current Weather Hero Card (With Favorite Star ⭐)
                        item {
                            CurrentWeatherCard(
                                weather = weather,
                                onLocationClick = { showSearchDialog = true },
                                isFavorite = viewModel.isFavoriteCity(weather.location),
                                onToggleFavorite = { viewModel.toggleFavoriteCity(weather.location) }
                            )
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

                        // 4. 7-Day Forecast Section
                        item {
                            DailyForecastSection(dailyList = weather.daily7Days)
                        }

                        // 5. Detailed Weather Metrics Grid
                        item {
                            WeatherMetricsGrid(weather = weather)
                        }

                        // Footer Updated Time
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (weather.isOffline)
                                        "Dữ liệu ngoại tuyến • Lưu lúc ${weather.lastUpdated}"
                                    else
                                        "Cập nhật lúc ${weather.lastUpdated}",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp
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
                    onRequestGps()
                },
                favoriteCities = favoriteCities,
                onToggleFavorite = { city ->
                    viewModel.toggleFavoriteCity(city)
                }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                initialCustomKey = viewModel.getCustomApiKey(),
                isUsingCustomKey = viewModel.isUsingCustomKey(),
                hasDefaultKey = viewModel.hasDefaultKey(),
                currentAutoRefreshMinutes = autoRefreshMinutes,
                defaultCity = defaultCity,
                isStartWithGps = isStartWithGps,
                isMorningNotif = isMorningNotif,
                isSevereAlerts = isSevereAlerts,
                favoriteCities = favoriteCities,
                onDismiss = { showSettingsDialog = false },
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
