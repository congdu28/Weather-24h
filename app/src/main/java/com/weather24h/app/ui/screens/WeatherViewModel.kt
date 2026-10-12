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

    init {
        loadWeather(currentCity)
        startAutoRefreshTimer(_autoRefreshMinutes.value)
    }

    fun loadWeather(city: CityLocation) {
        currentCity = city
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val weather = repository.getWeatherData(city)
                _uiState.value = WeatherUiState.Success(weather = weather, outfit = null, isOutfitLoading = true)

                // Load outfit recommendation
                val outfit = repository.getOutfitRecommendation(weather)
                _uiState.value = WeatherUiState.Success(weather = weather, outfit = outfit, isOutfitLoading = false)
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error("Không thể tải dữ liệu thời tiết. Vui lòng thử lại!")
            }
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            try {
                val weather = repository.getWeatherData(currentCity)
                val currentOutfit = (_uiState.value as? WeatherUiState.Success)?.outfit
                _uiState.value = WeatherUiState.Success(weather = weather, outfit = currentOutfit, isOutfitLoading = false)
            } catch (_: Exception) {}
        }
    }

    fun refreshOutfit() {
        val currentState = _uiState.value
        if (currentState is WeatherUiState.Success) {
            viewModelScope.launch {
                _uiState.value = currentState.copy(isOutfitLoading = true)
                val newOutfit = repository.getOutfitRecommendation(currentState.weather)
                _uiState.value = currentState.copy(outfit = newOutfit, isOutfitLoading = false)
            }
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
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            // Xác định chính xác quận/huyện, thành phố bằng reverse geocoding tiếng Việt
            val address = repository.getAddressFromCoordinates(lat, lon)
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
