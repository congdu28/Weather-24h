package com.weather24h.app.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.weather24h.app.data.model.CityLocation
import com.weather24h.app.data.model.OutfitRecommendation
import com.weather24h.app.data.model.WeatherData
import com.weather24h.app.data.remote.GeminiStylistService
import com.weather24h.app.data.remote.WeatherApiService
import com.weather24h.app.data.repository.WeatherRepository
import com.weather24h.app.ui.theme.AmbientGlowStyle
import com.weather24h.app.util.PeriodicWeatherWorker
import com.weather24h.app.util.WeatherNotificationHelper
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
    private val repository = WeatherRepository(
        context = application,
        apiService = WeatherApiService(),
        stylistService = GeminiStylistService(application)
    )

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<CityLocation>>(emptyList())
    val searchResults: StateFlow<List<CityLocation>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _autoRefreshMinutes = MutableStateFlow(repository.getAutoRefreshInterval())
    val autoRefreshMinutes: StateFlow<Int> = _autoRefreshMinutes.asStateFlow()

    // Danh sách thành phố yêu thích
    private val _favoriteCities = MutableStateFlow<List<CityLocation>>(repository.getFavoriteCities())
    val favoriteCities: StateFlow<List<CityLocation>> = _favoriteCities.asStateFlow()

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

    private val _ambientGlowStyle = MutableStateFlow(repository.getAmbientGlowStyle())
    val ambientGlowStyle: StateFlow<AmbientGlowStyle> = _ambientGlowStyle.asStateFlow()

    private val _ambientGlowIntensity = MutableStateFlow(repository.getAmbientGlowIntensity())
    val ambientGlowIntensity: StateFlow<Float> = _ambientGlowIntensity.asStateFlow()

    fun setThemeMode(mode: com.weather24h.app.ui.theme.AppThemeMode) {
        _themeMode.value = mode
        repository.setThemeMode(mode)
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

    private var currentCity: CityLocation = repository.getDefaultCity()
    private var autoRefreshJob: Job? = null
    private var loadWeatherJob: Job? = null
    private var loadOutfitJob: Job? = null
    private var gpsJob: Job? = null

    init {
        // Khởi tạo các notification channel
        WeatherNotificationHelper.initChannels(application)

        // Lên lịch thông báo định kỳ (Mặc định 2h/lần)
        PeriodicWeatherWorker.schedule(application, _periodicNotifHours.value)

        // Load thành phố mặc định ban đầu
        loadWeather(currentCity)
        startAutoRefreshTimer(_autoRefreshMinutes.value)
    }

    /**
     * Tải dữ liệu thời tiết cho một thành phố cụ thể.
     * Tự động hủy mọi tác vụ cũ đang chạy để tránh race condition.
     */
    fun loadWeather(city: CityLocation) {
        gpsJob?.cancel()
        loadWeatherJob?.cancel()
        loadOutfitJob?.cancel()

        currentCity = city

        loadWeatherJob = viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val weather = repository.getWeatherData(city)
                if (!isActive) return@launch

                _uiState.value = WeatherUiState.Success(
                    weather = weather,
                    outfit = null,
                    isOutfitLoading = true
                )

                // Cập nhật thông báo thời tiết trên status bar nếu đang bật định kỳ
                if (_periodicNotifHours.value > 0) {
                    WeatherNotificationHelper.showCurrentWeatherNotification(getApplication(), weather)
                }

                loadOutfitForWeather(weather)
            } catch (e: Exception) {
                if (isActive) {
                    _uiState.value = WeatherUiState.Error("Không thể tải thời tiết cho ${city.name}. Vui lòng thử lại!")
                }
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

    fun refreshWeather() {
        loadWeather(currentCity)
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
                WeatherNotificationHelper.showCurrentWeatherNotification(getApplication(), currentState.weather)
            }
        } else {
            WeatherNotificationHelper.cancelCurrentWeatherNotification(getApplication())
        }
    }

    fun sendCurrentWeatherNotification() {
        val currentState = _uiState.value
        if (currentState is WeatherUiState.Success) {
            WeatherNotificationHelper.showCurrentWeatherNotification(getApplication(), currentState.weather)
        } else {
            val cached = repository.getCachedWeather()
            if (cached != null) {
                WeatherNotificationHelper.showCurrentWeatherNotification(getApplication(), cached)
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

    fun setLocationFromGps(lat: Double, lon: Double) {
        gpsJob?.cancel()
        loadWeatherJob?.cancel()
        loadOutfitJob?.cancel()

        gpsJob = viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            val provisionalCity = CityLocation(
                name = "Vị trí của bạn",
                country = "Việt Nam",
                latitude = lat,
                longitude = lon,
                isCurrentLocation = true
            )
            currentCity = provisionalCity

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
                val finalWeather = weather.copy(location = realCity)

                _uiState.value = WeatherUiState.Success(
                    weather = finalWeather,
                    outfit = null,
                    isOutfitLoading = true
                )

                loadOutfitForWeather(finalWeather)
            } else {
                _uiState.value = WeatherUiState.Error("Không thể tải thời tiết tại vị trí GPS này.")
            }
        }
    }
}
