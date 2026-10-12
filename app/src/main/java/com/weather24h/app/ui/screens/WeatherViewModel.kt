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
import kotlinx.coroutines.Job
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

    private var currentCity: CityLocation = CityLocation.DEFAULT_LOCATION
    private var autoRefreshJob: Job? = null
    private var loadWeatherJob: Job? = null
    private var loadOutfitJob: Job? = null
    private var gpsJob: Job? = null

    init {
        loadWeather(currentCity)
        startAutoRefreshTimer(_autoRefreshMinutes.value)
    }

    /**
     * Tải dữ liệu thời tiết cho một thành phố cụ thể.
     * Tự động hủy mọi tác vụ cũ đang chạy để tránh race condition (bị nhảy về thành phố cũ).
     */
    fun loadWeather(city: CityLocation) {
        // Hủy ngay các tác vụ nền cũ (GPS hoặc lấy dữ liệu thành phố trước)
        gpsJob?.cancel()
        loadWeatherJob?.cancel()
        loadOutfitJob?.cancel()

        currentCity = city

        loadWeatherJob = viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val weather = repository.getWeatherData(city)
                if (!isActive) return@launch

                // Cập nhật giao diện thời tiết ngay lập tức (không chờ AI Stylist)
                _uiState.value = WeatherUiState.Success(
                    weather = weather,
                    outfit = null,
                    isOutfitLoading = true
                )

                // Tải gợi ý trang phục AI độc lập trong background
                loadOutfitForWeather(weather)
            } catch (e: Exception) {
                if (isActive) {
                    _uiState.value = WeatherUiState.Error("Không thể tải thời tiết cho ${city.name}. Vui lòng thử lại!")
                }
            }
        }
    }

    /**
     * Tải gợi ý AI Stylist độc lập, kiểm tra đúng thành phố trước khi gán dữ liệu.
     */
    private fun loadOutfitForWeather(weather: WeatherData) {
        loadOutfitJob?.cancel()
        loadOutfitJob = viewModelScope.launch {
            try {
                val outfit = repository.getOutfitRecommendation(weather)
                if (!isActive) return@launch

                val currentState = _uiState.value
                // Chỉ áp dụng outfit nếu người dùng vẫn đang xem đúng thành phố này
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
        // Hủy mọi request đang dở trước đó
        gpsJob?.cancel()
        loadWeatherJob?.cancel()
        loadOutfitJob?.cancel()

        gpsJob = viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            // Geocoding chi tiết tiếng Việt
            val address = repository.getAddressFromCoordinates(lat, lon)
            if (!isActive) return@launch

            val gpsCity = CityLocation(
                name = address.first,
                country = address.second,
                latitude = lat,
                longitude = lon,
                isCurrentLocation = true
            )
            loadWeather(gpsCity)
        }
    }
}
