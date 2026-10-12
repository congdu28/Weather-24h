package com.weather24h.app.data.repository

import android.content.Context
import com.weather24h.app.data.model.CityLocation
import com.weather24h.app.data.model.OutfitRecommendation
import com.weather24h.app.data.model.WeatherData
import com.weather24h.app.data.remote.GeminiStylistService
import com.weather24h.app.data.remote.WeatherApiService

class WeatherRepository(
    private val context: Context,
    private val apiService: WeatherApiService,
    private val stylistService: GeminiStylistService
) {
    private val prefs = context.getSharedPreferences("weather24h_prefs", Context.MODE_PRIVATE)

    suspend fun getWeatherData(location: CityLocation): WeatherData {
        return apiService.getFullWeather(location)
    }

    suspend fun searchCities(query: String): List<CityLocation> {
        return apiService.searchCities(query)
    }

    suspend fun getAddressFromCoordinates(lat: Double, lon: Double): Pair<String, String> {
        return apiService.getAddressFromCoordinates(context, lat, lon)
    }

    suspend fun getOutfitRecommendation(weather: WeatherData): OutfitRecommendation {
        return stylistService.getOutfitRecommendation(weather)
    }

    fun saveGeminiApiKey(apiKey: String) {
        stylistService.saveApiKey(apiKey)
    }

    fun clearCustomGeminiApiKey() {
        stylistService.clearCustomKey()
    }

    fun getGeminiApiKey(): String? {
        return stylistService.getApiKey()
    }

    fun getCustomApiKey(): String? {
        return stylistService.getCustomApiKey()
    }

    fun isUsingCustomKey(): Boolean {
        return stylistService.isUsingCustomKey()
    }

    fun hasDefaultKey(): Boolean {
        return stylistService.hasDefaultKey()
    }

    // Auto-refresh interval (in minutes: 0, 30, 60, 120, 180, 300)
    fun getAutoRefreshInterval(): Int {
        return prefs.getInt("auto_refresh_minutes", 60) // Mặc định 60 phút
    }

    fun setAutoRefreshInterval(minutes: Int) {
        prefs.edit().putInt("auto_refresh_minutes", minutes).apply()
    }
}
