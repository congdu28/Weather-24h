package com.weather24h.app.data.repository

import com.weather24h.app.data.model.CityLocation
import com.weather24h.app.data.model.OutfitRecommendation
import com.weather24h.app.data.model.WeatherData
import com.weather24h.app.data.remote.GeminiStylistService
import com.weather24h.app.data.remote.WeatherApiService

class WeatherRepository(
    private val apiService: WeatherApiService,
    private val stylistService: GeminiStylistService
) {
    suspend fun getWeatherData(location: CityLocation): WeatherData {
        return apiService.getFullWeather(location)
    }

    suspend fun searchCities(query: String): List<CityLocation> {
        return apiService.searchCities(query)
    }

    suspend fun getOutfitRecommendation(weather: WeatherData): OutfitRecommendation {
        return stylistService.getOutfitRecommendation(weather)
    }

    fun saveGeminiApiKey(apiKey: String) {
        stylistService.saveApiKey(apiKey)
    }

    fun getGeminiApiKey(): String? {
        return stylistService.getApiKey()
    }
}
