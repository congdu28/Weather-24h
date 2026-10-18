package com.weather24h.app.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.weather24h.app.data.model.CityLocation
import com.weather24h.app.data.model.CityWeatherSummary
import com.weather24h.app.data.model.OutfitRecommendation
import com.weather24h.app.data.model.WeatherData
import com.weather24h.app.data.model.WeatherProvider
import com.weather24h.app.data.model.WidgetStyle
import com.weather24h.app.data.remote.GeminiStylistService
import com.weather24h.app.data.remote.WeatherApiService
import com.weather24h.app.data.repository.WeatherRepository
import com.weather24h.app.ui.theme.AmbientGlowStyle
import com.weather24h.app.ui.theme.AtmospherePerformanceMode
import com.weather24h.app.util.PeriodicWeatherWorker
import com.weather24h.app.util.WeatherNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(
        val weather: WeatherData,
        val outfit: OutfitRecommendation?,
        val isOutfitLoading: Boolean = false
    ) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = WeatherApiService()
    private val stylistService = GeminiStylistService(application)
    private val repository = WeatherRepository(
        context = application,
        apiService = apiService,
        stylistService = stylistService
    )

    private val _uiState = MutableStateFlow<WeatherUiState>(
        repository.getCachedWeather()?.let {
            WeatherUiState.Success(
                weather = it,
                outfit = null,
                isOutfitLoading = false
            )
        } ?: WeatherUiState.Loading
    )
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    // Trạng thái đang làm mới mượt mà (Pull-to-refresh, khởi động ngầm & Nút cập nhật)
    private val _isRefreshing = MutableStateFlow(repository.getCachedWeather() != null)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _searchResults = MutableStateFlow<List<CityLocation>>(emptyList())
    val searchResults: StateFlow<List<CityLocation>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _autoRefreshMinutes = MutableStateFlow(repository.getAutoRefreshInterval())
    val autoRefreshMinutes: StateFlow<Int> = _autoRefreshMinutes.asStateFlow()

    // Danh sách thành phố yêu thích
    private val _favoriteCities = MutableStateFlow<List<CityLocation>>(repository.getFavoriteCities())
    val favoriteCities: StateFlow<List<CityLocation>> = _favoriteCities.asStateFlow()

    // Tóm tắt thời tiết của các thành phố yêu thích (Nhiệt độ, icon, tình trạng)
    private val _favoriteSummaries = MutableStateFlow<Map<String, CityWeatherSummary>>(repository.getFavoriteSummaries())
    val favoriteSummaries: StateFlow<Map<String, CityWeatherSummary>> = _favoriteSummaries.asStateFlow()

    // Cài đặt thành phố mặc định & khởi động
    private val _defaultCity = MutableStateFlow(repository.getDefaultCity())
    val defaultCity: StateFlow<CityLocation> = _defaultCity.asStateFlow()

    private val _isStartWithGps = MutableStateFlow(repository.isStartWithGps())
    val isStartWithGps: StateFlow<Boolean> = _isStartWithGps.asStateFlow()

    // Cài đặt thông báo
    private val _isMorningNotif = MutableStateFlow(repository.isMorningNotificationEnabled())
    val isMorningNotif: StateFlow<Boolean> = _isMorningNotif.asStateFlow()

    private val _isSevereAlerts = MutableStateFlow(repository.isSevereAlertsEnabled())
    val isSevereAlerts: StateFlow<Boolean> = _isSevereAlerts.asStateFlow()

    private val _periodicNotifHours = MutableStateFlow(repository.getPeriodicNotifIntervalHours())
    val periodicNotifHours: StateFlow<Int> = _periodicNotifHours.asStateFlow()

    // Cài đặt Theme giao diện (Sáng / Tối / Theo thiết bị)
    private val _themeMode = MutableStateFlow(repository.getThemeMode())
    val themeMode: StateFlow<com.weather24h.app.ui.theme.AppThemeMode> = _themeMode.asStateFlow()

    // Cài đặt độ tối nền (0.0f đến 0.8f) & Ambient Glow
    private val _bgDimmingLevel = MutableStateFlow(repository.getBgDimmingLevel())
    val bgDimmingLevel: StateFlow<Float> = _bgDimmingLevel.asStateFlow()

    // Cài đặt độ mờ / trong suốt khối thời tiết màn hình chính (0.20f đến 1.0f)
    private val _cardOpacity = MutableStateFlow(repository.getCardOpacity())
    val cardOpacity: StateFlow<Float> = _cardOpacity.asStateFlow()

    private val _ambientGlowStyle = MutableStateFlow(repository.getAmbientGlowStyle())
    val ambientGlowStyle: StateFlow<AmbientGlowStyle> = _ambientGlowStyle.asStateFlow()

    private val _ambientGlowIntensity = MutableStateFlow(repository.getAmbientGlowIntensity())
    val ambientGlowIntensity: StateFlow<Float> = _ambientGlowIntensity.asStateFlow()

    private val _atmospherePerformanceMode = MutableStateFlow(repository.getAtmospherePerformanceMode())
    val atmospherePerformanceMode: StateFlow<AtmospherePerformanceMode> = _atmospherePerformanceMode.asStateFlow()

    // Cài đặt Widget (Theme sáng/tối & Độ trong suốt)
    private val _widgetThemeMode = MutableStateFlow(repository.getWidgetThemeMode())
    val widgetThemeMode: StateFlow<com.weather24h.app.ui.theme.AppThemeMode> = _widgetThemeMode.asStateFlow()

    private val _widgetOpacity = MutableStateFlow(repository.getWidgetOpacity())
    val widgetOpacity: StateFlow<Float> = _widgetOpacity.asStateFlow()

    private val _widgetStyle = MutableStateFlow(repository.getWidgetStyle())
    val widgetStyle: StateFlow<WidgetStyle> = _widgetStyle.asStateFlow()

    // Cài đặt Tự động làm mới Widget (1h/lần)
    private val _isWidgetAutoRefresh = MutableStateFlow(repository.isWidgetAutoRefreshEnabled())
    val isWidgetAutoRefresh: StateFlow<Boolean> = _isWidgetAutoRefresh.asStateFlow()

    fun setWidgetAutoRefresh(enabled: Boolean) {
        _isWidgetAutoRefresh.value = enabled
        repository.setWidgetAutoRefreshEnabled(enabled)
        PeriodicWeatherWorker.schedule(getApplication(), _periodicNotifHours.value)
    }

    // Cài đặt mô hình dự báo thời tiết (ECMWF, GFS, ICON, JMA, v.v.)
    private val _forecastModel = MutableStateFlow(repository.getForecastModel())
    val forecastModel: StateFlow<com.weather24h.app.data.model.WeatherForecastModel> = _forecastModel.asStateFlow()

    fun isCurrentCityGps(): Boolean = currentCity.isCurrentLocation

    fun setCardOpacity(opacity: Float) {
        val clamped = opacity.coerceIn(0.20f, 1.0f)
        _cardOpacity.value = clamped
        repository.setCardOpacity(clamped)
    }

    fun setThemeMode(mode: com.weather24h.app.ui.theme.AppThemeMode) {
        _themeMode.value = mode
        repository.setThemeMode(mode)
    }

    fun setForecastModel(model: com.weather24h.app.data.model.WeatherForecastModel) {
        _forecastModel.value = model
        repository.setForecastModel(model)
        refreshWeather()
    }

    fun setWidgetThemeMode(mode: com.weather24h.app.ui.theme.AppThemeMode) {
        _widgetThemeMode.value = mode
        repository.setWidgetThemeMode(mode)
        com.weather24h.app.ui.widget.WeatherWidgetProvider.updateAllWidgets(getApplication())
    }

    fun setWidgetOpacity(opacity: Float) {
        val clamped = opacity.coerceIn(0.0f, 1.0f)
        _widgetOpacity.value = clamped
        repository.setWidgetOpacity(clamped)
        com.weather24h.app.ui.widget.WeatherWidgetProvider.updateAllWidgets(getApplication())
    }

    fun setWidgetStyle(style: WidgetStyle) {
        _widgetStyle.value = style
        repository.setWidgetStyle(style)
        com.weather24h.app.ui.widget.WeatherWidgetProvider.updateAllWidgets(getApplication())
    }

    fun setBgDimmingLevel(level: Float) {
        val clamped = level.coerceIn(0.0f, 0.8f)
        _bgDimmingLevel.value = clamped
        repository.setBgDimmingLevel(clamped)
    }

    fun setAmbientGlowStyle(style: AmbientGlowStyle) {
        _ambientGlowStyle.value = style
        repository.setAmbientGlowStyle(style)
    }

    fun setAmbientGlowIntensity(intensity: Float) {
        val clamped = intensity.coerceIn(0.0f, 1.0f)
        _ambientGlowIntensity.value = clamped
        repository.setAmbientGlowIntensity(clamped)
    }

    fun setAtmospherePerformanceMode(mode: AtmospherePerformanceMode) {
        _atmospherePerformanceMode.value = mode
        repository.setAtmospherePerformanceMode(mode)
    }

    // Nguồn dữ liệu thời tiết (WeatherAPI vs Google Weather vs Open-Meteo)
    private val _weatherProvider = MutableStateFlow(repository.getWeatherProvider())
    val weatherProvider: StateFlow<WeatherProvider> = _weatherProvider.asStateFlow()

    private val _customWeatherApiKey = MutableStateFlow(repository.getCustomWeatherApiKey())
    val customWeatherApiKey: StateFlow<String?> = _customWeatherApiKey.asStateFlow()

    private val _customGoogleWeatherKey = MutableStateFlow(repository.getCustomGoogleWeatherKey())
    val customGoogleWeatherKey: StateFlow<String?> = _customGoogleWeatherKey.asStateFlow()

    fun setWeatherProvider(provider: WeatherProvider) {
        _weatherProvider.value = provider
        repository.setWeatherProvider(provider)
        refreshWeather()
    }

    // --- WeatherAPI Key Actions ---
    fun saveWeatherApiKey(key: String) {
        repository.saveWeatherApiKey(key)
        _customWeatherApiKey.value = repository.getCustomWeatherApiKey()
        if (_weatherProvider.value == WeatherProvider.WEATHER_API) {
            refreshWeather()
        }
    }

    fun clearWeatherApiKey() {
        repository.clearCustomWeatherApiKey()
        _customWeatherApiKey.value = null
        if (_weatherProvider.value == WeatherProvider.WEATHER_API) {
            refreshWeather()
        }
    }

    fun getEffectiveWeatherApiKey(): String = repository.getEffectiveWeatherApiKey()
    fun hasWeatherApiKey(): Boolean = repository.hasWeatherApiKey()
    fun isUsingCustomWeatherApiKey(): Boolean = repository.isUsingCustomWeatherApiKey()

    fun testWeatherApiKey(key: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.testWeatherApiKey(key)
            result.onSuccess { msg ->
                onResult(true, msg)
            }.onFailure { err ->
                onResult(false, err.message ?: "Kiểm tra kết nối thất bại")
            }
        }
    }

    // --- Google Weather Key Actions ---
    fun saveGoogleWeatherKey(key: String) {
        repository.saveGoogleWeatherKey(key)
        _customGoogleWeatherKey.value = repository.getCustomGoogleWeatherKey()
        if (_weatherProvider.value == WeatherProvider.GOOGLE) {
            refreshWeather()
        }
    }

    fun clearGoogleWeatherKey() {
        repository.clearCustomGoogleWeatherKey()
        _customGoogleWeatherKey.value = null
        if (_weatherProvider.value == WeatherProvider.GOOGLE) {
            refreshWeather()
        }
    }

    fun getEffectiveGoogleWeatherKey(): String = repository.getEffectiveGoogleWeatherKey()
    fun hasGoogleWeatherKey(): Boolean = repository.hasGoogleWeatherKey()
    fun isUsingCustomGoogleWeatherKey(): Boolean = repository.isUsingCustomGoogleWeatherKey()

    fun testGoogleWeatherKey(key: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.testGoogleWeatherKey(key)
            result.onSuccess { msg ->
                onResult(true, msg)
            }.onFailure { err ->
                onResult(false, err.message ?: "Kiểm tra kết nối thất bại")
            }
        }
    }


    private var currentCity: CityLocation = repository.getLastActiveCity()
    private var autoRefreshJob: Job? = null
    private var loadWeatherJob: Job? = null
    private var loadOutfitJob: Job? = null
    private var gpsJob: Job? = null

    init {
        // Khởi tạo các notification channel
        WeatherNotificationHelper.initChannels(application)

        // Lên lịch đồng bộ định kỳ Widget (1h/lần) & Thông báo (nếu bật)
        PeriodicWeatherWorker.schedule(application, _periodicNotifHours.value)

        // Tải trang phục cho thời tiết đã lưu từ phiên trước (nếu có)
        val cached = repository.getCachedWeather()
        if (cached != null) {
            loadOutfitForWeather(cached)
        }

        // Khi ưu tiên GPS, MainActivity sẽ tải đúng vị trí sau khi quyền được xử lý.
        // Chỉ tải thành phố đã lưu ngay nếu người dùng đã tắt khởi động bằng GPS.
        if (!_isStartWithGps.value) {
            loadWeather(currentCity, isBackgroundRefresh = true)
        }
        startAutoRefreshTimer(_autoRefreshMinutes.value)
        refreshFavoriteSummaries()
    }

    /** Dùng thành phố đã lưu nếu lần định vị đầu tiên bị từ chối hoặc thất bại. */
    fun loadInitialFallbackWeather() {
        if (_uiState.value !is WeatherUiState.Success && loadWeatherJob?.isActive != true) {
            loadWeather(currentCity)
        } else if (_uiState.value is WeatherUiState.Success) {
            refreshWeather(isPullRefresh = true)
        }
    }

    /**
     * Tải dữ liệu thời tiết cho một thành phố cụ thể.
     * Tự động hủy mọi tác vụ cũ đang chạy để tránh race condition.
     */
    fun loadWeather(city: CityLocation, isBackgroundRefresh: Boolean = false) {
        gpsJob?.cancel()
        loadWeatherJob?.cancel()
        loadOutfitJob?.cancel()

        val isSameCity = city.name.equals(currentCity.name, ignoreCase = true)
        currentCity = city
        repository.setLastActiveCity(city)

        // Kiểm tra xem đã có dữ liệu cache (từ bộ nhớ hoặc pre-fetch) của thành phố này chưa
        val cachedWeather = repository.getCachedWeatherForCity(city)

        loadWeatherJob = viewModelScope.launch {
            if (cachedWeather != null) {
                // CHUYỂN TAB TỨC THÌ 0S: Hiển thị ngay lập tức dữ liệu cache mà không cần xoay vòng loading
                _uiState.value = WeatherUiState.Success(
                    weather = cachedWeather,
                    outfit = if (isSameCity) (_uiState.value as? WeatherUiState.Success)?.outfit else null,
                    isOutfitLoading = true
                )
                _isRefreshing.value = true
            } else {
                val hasExistingData = _uiState.value is WeatherUiState.Success
                if (!hasExistingData || (!isSameCity && !isBackgroundRefresh)) {
                    _uiState.value = WeatherUiState.Loading
                } else {
                    _isRefreshing.value = true
                }
            }
            try {
                val weather = repository.getWeatherData(city)
                if (!isActive) return@launch

                _uiState.value = WeatherUiState.Success(
                    weather = weather,
                    outfit = if (isSameCity) (_uiState.value as? WeatherUiState.Success)?.outfit else null,
                    isOutfitLoading = true
                )
                _favoriteSummaries.value = repository.getFavoriteSummaries()

                // Cập nhật thông báo thời tiết trên status bar nếu đang bật định kỳ
                if (_periodicNotifHours.value > 0) {
                    WeatherNotificationHelper.showCurrentWeatherNotification(getApplication(), weather)
                }

                loadOutfitForWeather(weather)
            } catch (e: Exception) {
                if (isActive) {
                    if (_uiState.value !is WeatherUiState.Success) {
                        _uiState.value = WeatherUiState.Error("Không thể tải thời tiết cho ${city.name}. Vui lòng thử lại!")
                    }
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun loadOutfitForWeather(weather: WeatherData) {
        loadOutfitJob?.cancel()
        loadOutfitJob = viewModelScope.launch {
            try {
                val outfit = repository.getOutfitRecommendation(weather)
                if (!isActive) return@launch

                val currentState = _uiState.value
                if (currentState is WeatherUiState.Success && currentState.weather.location.name == weather.location.name) {
                    _uiState.value = currentState.copy(outfit = outfit, isOutfitLoading = false)
                }
            } catch (_: Exception) {
                if (isActive) {
                    val currentState = _uiState.value
                    if (currentState is WeatherUiState.Success) {
                        _uiState.value = currentState.copy(isOutfitLoading = false)
                    }
                }
            }
        }
    }

    fun refreshWeather(isPullRefresh: Boolean = false) {
        if (!isPullRefresh || _uiState.value !is WeatherUiState.Success) {
            loadWeather(currentCity)
            return
        }

        // Tải làm mới ngầm (không reset UI về loading spinner, giữ trải nghiệm mượt mà)
        loadWeatherJob?.cancel()
        loadWeatherJob = viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val weather = repository.getWeatherData(currentCity)
                if (!isActive) return@launch

                val currentState = _uiState.value
                if (currentState is WeatherUiState.Success) {
                    _uiState.value = currentState.copy(weather = weather)
                }

                if (_periodicNotifHours.value > 0) {
                    WeatherNotificationHelper.showCurrentWeatherNotification(getApplication(), weather)
                }

                loadOutfitForWeather(weather)
            } catch (_: Exception) {
                // Giữ lại dữ liệu hiện tại khi cập nhật nền thất bại
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refreshOutfit() {
        val currentState = _uiState.value
        if (currentState is WeatherUiState.Success) {
            _uiState.value = currentState.copy(isOutfitLoading = true)
            loadOutfitForWeather(currentState.weather)
        }
    }

    fun searchCities(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            val results = repository.searchCities(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    // ==========================================
    // QUẢN LÝ THÀNH PHỐ YÊU THÍCH & MẶC ĐỊNH
    // ==========================================
    fun toggleFavoriteCity(city: CityLocation) {
        repository.toggleFavoriteCity(city)
        _favoriteCities.value = repository.getFavoriteCities()
        _favoriteSummaries.value = repository.getFavoriteSummaries()
        refreshFavoriteSummaries()
    }

    fun removeFavoriteCity(city: CityLocation) {
        repository.removeFavoriteCity(city)
        _favoriteCities.value = repository.getFavoriteCities()
        _favoriteSummaries.value = repository.getFavoriteSummaries()
    }

    fun getCurrentSelectedCity(): CityLocation = currentCity

    fun refreshFavoriteSummaries() {
        val favs = repository.getFavoriteCities()
        if (favs.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val currentMap = repository.getFavoriteSummaries().toMutableMap()
            var hasChanges = false
            for (city in favs) {
                val key = city.name.lowercase()
                val existing = currentMap[key]
                val now = System.currentTimeMillis()
                // Cập nhật nếu chưa có thông tin hoặc dữ liệu đã cũ hơn 20 phút
                if (existing == null || now - existing.updatedAt > 20 * 60 * 1000L) {
                    try {
                        val weather = apiService.getFullWeather(city, repository.getForecastModel())
                        // Lưu toàn bộ dữ liệu thời tiết để chuyển tab tức thì 0 giây
                        repository.saveCachedWeatherForCity(weather)

                        currentMap[key] = CityWeatherSummary(
                            cityName = city.name,
                            tempC = weather.current.temperature.toInt(),
                            conditionText = weather.current.condition.description,
                            weatherType = weather.current.condition.type,
                            isDay = weather.current.isDay,
                            updatedAt = now
                        )
                        hasChanges = true
                    } catch (_: Exception) {}
                }
            }
            if (hasChanges) {
                repository.saveFavoriteSummaries(currentMap)
                _favoriteSummaries.value = currentMap
            }
        }
    }

    fun isFavoriteCity(city: CityLocation): Boolean {
        return repository.isFavoriteCity(city)
    }

    fun setDefaultCity(city: CityLocation) {
        repository.setDefaultCity(city)
        _defaultCity.value = city
    }

    fun setStartWithGps(enabled: Boolean) {
        repository.setStartWithGps(enabled)
        _isStartWithGps.value = enabled
    }

    // ==========================================
    // CÀI ĐẶT THÔNG BÁO & NGOẠI TUYẾN
    // ==========================================
    fun setMorningNotification(enabled: Boolean) {
        repository.setMorningNotificationEnabled(enabled)
        _isMorningNotif.value = enabled
    }

    fun setSevereAlerts(enabled: Boolean) {
        repository.setSevereAlertsEnabled(enabled)
        _isSevereAlerts.value = enabled
    }

    fun setPeriodicNotifHours(hours: Int) {
        _periodicNotifHours.value = hours
        repository.setPeriodicNotifIntervalHours(hours)
        PeriodicWeatherWorker.schedule(getApplication(), hours)

        if (hours > 0) {
            val currentState = _uiState.value
            if (currentState is WeatherUiState.Success) {
                WeatherNotificationHelper.showCurrentWeatherNotification(getApplication(), currentState.weather, force = true)
            }
        } else {
            WeatherNotificationHelper.cancelCurrentWeatherNotification(getApplication())
        }
    }

    fun sendCurrentWeatherNotification() {
        val currentState = _uiState.value
        if (currentState is WeatherUiState.Success) {
            WeatherNotificationHelper.showCurrentWeatherNotification(getApplication(), currentState.weather, force = true)
        } else {
            val cached = repository.getCachedWeather()
            if (cached != null) {
                WeatherNotificationHelper.showCurrentWeatherNotification(getApplication(), cached, force = true)
            }
        }
    }

    fun sendTestNotification(isAlert: Boolean = false) {
        WeatherNotificationHelper.sendTestNotification(getApplication(), isAlert)
    }

    fun clearOfflineCache() {
        repository.clearOfflineCache()
    }

    // ==========================================
    // API KEY & INTERVAL
    // ==========================================
    fun saveCustomGeminiApiKey(key: String) {
        repository.saveGeminiApiKey(key)
        refreshOutfit()
    }

    fun clearCustomGeminiApiKey() {
        repository.clearCustomGeminiApiKey()
        refreshOutfit()
    }

    fun getCustomApiKey(): String? = repository.getCustomApiKey()
    fun isUsingCustomKey(): Boolean = repository.isUsingCustomKey()
    fun hasDefaultKey(): Boolean = repository.hasDefaultKey()

    fun setAutoRefreshInterval(minutes: Int) {
        _autoRefreshMinutes.value = minutes
        repository.setAutoRefreshInterval(minutes)
        startAutoRefreshTimer(minutes)
    }

    private fun startAutoRefreshTimer(minutes: Int) {
        autoRefreshJob?.cancel()
        if (minutes > 0) {
            autoRefreshJob = viewModelScope.launch {
                while (isActive) {
                    delay(minutes * 60 * 1000L)
                    refreshWeather()
                }
            }
        }
    }

    fun cancelRefreshing() {
        _isRefreshing.value = false
    }

    fun setLocationFromGps(lat: Double, lon: Double, isPullRefresh: Boolean = false) {
        gpsJob?.cancel()
        loadWeatherJob?.cancel()
        loadOutfitJob?.cancel()

        gpsJob = viewModelScope.launch {
            val hasExistingData = _uiState.value is WeatherUiState.Success
            if (hasExistingData) {
                _isRefreshing.value = true
            } else {
                _uiState.value = WeatherUiState.Loading
            }

            try {
                val provisionalCity = CityLocation(
                    name = "Vị trí của bạn",
                    country = "Việt Nam",
                    latitude = lat,
                    longitude = lon,
                    isCurrentLocation = true
                )
                currentCity = provisionalCity
                repository.setLastActiveCity(provisionalCity)

                // Tải song song thời tiết & phân giải tên địa điểm để giảm tối đa thời gian chờ
                val weatherDeferred = async {
                    try {
                        repository.getWeatherData(provisionalCity)
                    } catch (e: Exception) {
                        null
                    }
                }

                val addressDeferred = async {
                    try {
                        repository.getAddressFromCoordinates(lat, lon)
                    } catch (_: Exception) {
                        Pair("Vị trí của bạn", "Việt Nam")
                    }
                }

                val weather = weatherDeferred.await()
                val address = addressDeferred.await()

                if (!isActive) return@launch

                if (weather != null) {
                    val realCity = provisionalCity.copy(name = address.first, country = address.second)
                    currentCity = realCity
                    repository.setLastActiveCity(realCity)
                    val finalWeather = weather.copy(location = realCity)

                    val currentState = _uiState.value
                    if (hasExistingData && currentState is WeatherUiState.Success) {
                        _uiState.value = currentState.copy(weather = finalWeather)
                    } else {
                        _uiState.value = WeatherUiState.Success(
                            weather = finalWeather,
                            outfit = null,
                            isOutfitLoading = true
                        )
                    }

                    if (_periodicNotifHours.value > 0) {
                        WeatherNotificationHelper.showCurrentWeatherNotification(getApplication(), finalWeather)
                    }

                    loadOutfitForWeather(finalWeather)
                } else if (!hasExistingData) {
                    _uiState.value = WeatherUiState.Error("Không thể tải thời tiết tại vị trí GPS này.")
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
