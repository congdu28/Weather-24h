package com.weather24h.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.weather24h.app.BuildConfig
import com.weather24h.app.data.model.CityLocation
import com.weather24h.app.data.model.OutfitRecommendation
import com.weather24h.app.data.model.WeatherData
import com.weather24h.app.data.model.WeatherProvider
import com.weather24h.app.data.remote.GeminiStylistService
import com.weather24h.app.data.remote.GoogleWeatherApiService
import com.weather24h.app.data.remote.WeatherApiService
import com.weather24h.app.ui.widget.WeatherWidgetProvider
import com.weather24h.app.util.WeatherNotificationHelper

class WeatherRepository(
    private val context: Context,
    private val apiService: WeatherApiService,
    private val stylistService: GeminiStylistService
) {
    private val prefs = context.getSharedPreferences("weather24h_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val googleApiService = GoogleWeatherApiService()

    /**
     * Lấy dữ liệu thời tiết (Hỗ trợ đa nguồn: Google Weather làm nguồn chính + Open-Meteo dự phòng)
     * Khi có mạng: Tải mới, lưu cache, cập nhật Widget và kiểm tra cảnh báo.
     * Khi mất mạng: Tự động trả về dữ liệu cache gần nhất với cờ isOffline = true.
     */
    suspend fun getWeatherData(location: CityLocation): WeatherData {
        return try {
            val provider = getWeatherProvider()
            val freshWeather = if (provider == WeatherProvider.GOOGLE) {
                val effectiveKey = getEffectiveGoogleWeatherKey()
                if (effectiveKey.isNotBlank()) {
                    try {
                        googleApiService.getFullWeather(location, effectiveKey)
                    } catch (e: Exception) {
                        // Tự động chuyển dự phòng sang Open-Meteo khi Google API gặp sự cố hoặc vượt hạn mức
                        apiService.getFullWeather(location, getForecastModel()).copy(
                            providerName = "Open-Meteo (Dự phòng tự động)"
                        )
                    }
                } else {
                    // Chưa cấu hình Google Key, tự động chuyển Open-Meteo miễn phí
                    apiService.getFullWeather(location, getForecastModel()).copy(
                        providerName = "Open-Meteo (Chưa cấu hình Google Key)"
                    )
                }
            } else {
                apiService.getFullWeather(location, getForecastModel())
            }

            // Lưu vào bộ nhớ đệm ngoại tuyến
            saveCachedWeather(freshWeather)

            // Cập nhật Widget màn hình chính
            WeatherWidgetProvider.updateAllWidgets(context, freshWeather)

            // Kiểm tra và gửi cảnh báo nếu bật tính năng
            if (isSevereAlertsEnabled()) {
                WeatherNotificationHelper.checkAndSendSevereAlerts(context, freshWeather)
            }

            freshWeather
        } catch (e: Exception) {
            // Khi mất mạng hoặc lỗi kết nối, ưu tiên lấy dữ liệu đã cache
            val cached = getCachedWeather()
            if (cached != null) {
                cached.copy(isOffline = true)
            } else {
                throw e
            }
        }
    }


    suspend fun searchCities(query: String): List<CityLocation> {
        return apiService.searchCities(query)
    }

    suspend fun getAddressFromCoordinates(lat: Double, lon: Double): Pair<String, String> {
        return apiService.getAddressFromCoordinates(context, lat, lon, apiKey = getGeminiApiKey())
    }

    suspend fun getOutfitRecommendation(weather: WeatherData): OutfitRecommendation {
        val outfit = stylistService.getOutfitRecommendation(weather)
        // Cập nhật widget với tip phối đồ mới
        WeatherWidgetProvider.updateAllWidgets(context, weather, "👔 ${outfit.styleVibe}: ${outfit.top}")
        return outfit
    }

    // ==========================================
    // QUẢN LÝ BỘ NHỚ ĐỆM NGOẠI TUYẾN (OFFLINE CACHE)
    // ==========================================
    fun saveCachedWeather(weather: WeatherData) {
        try {
            val json = gson.toJson(weather)
            prefs.edit()
                .putString("cached_weather_json", json)
                .putString("last_cached_time", weather.lastUpdated)
                .apply()
        } catch (_: Exception) {}
    }

    fun getCachedWeather(): WeatherData? {
        return try {
            val json = prefs.getString("cached_weather_json", null) ?: return null
            gson.fromJson(json, WeatherData::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun clearOfflineCache() {
        prefs.edit()
            .remove("cached_weather_json")
            .remove("last_cached_time")
            .apply()
    }

    // ==========================================
    // THÀNH PHỐ MẶC ĐỊNH & KHỞI ĐỘNG
    // ==========================================
    fun isStartWithGps(): Boolean {
        return prefs.getBoolean("start_with_gps", false) // Mặc định mở theo thành phố mặc định
    }

    fun setStartWithGps(enabled: Boolean) {
        prefs.edit().putBoolean("start_with_gps", enabled).apply()
    }

    fun getDefaultCity(): CityLocation {
        val json = prefs.getString("default_city_json", null)
        return if (!json.isNullOrEmpty()) {
            try {
                gson.fromJson(json, CityLocation::class.java)
            } catch (_: Exception) {
                CityLocation.DEFAULT_LOCATION
            }
        } else {
            CityLocation.DEFAULT_LOCATION
        }
    }

    fun setDefaultCity(city: CityLocation) {
        val json = gson.toJson(city)
        prefs.edit().putString("default_city_json", json).apply()
    }

    fun getLastActiveCity(): CityLocation {
        val json = prefs.getString("last_active_city_json", null)
        return if (!json.isNullOrEmpty()) {
            try {
                gson.fromJson(json, CityLocation::class.java)
            } catch (e: Exception) {
                getDefaultCity()
            }
        } else {
            getDefaultCity()
        }
    }

    fun setLastActiveCity(city: CityLocation) {
        val json = gson.toJson(city)
        prefs.edit().putString("last_active_city_json", json).apply()
    }

    // ==========================================
    // DANH SÁCH THÀNH PHỐ YÊU THÍCH (FAVORITES)
    // ==========================================
    @Volatile
    private var cachedFavorites: MutableList<CityLocation>? = null

    fun getFavoriteCities(): List<CityLocation> {
        cachedFavorites?.let { return ArrayList(it) }
        val json = prefs.getString("favorite_cities_json", null)
        val list = if (json.isNullOrEmpty()) {
            val initial = listOf(
                CityLocation.DEFAULT_LOCATION,
                CityLocation(name = "Đà Nẵng", country = "Việt Nam", latitude = 16.0544, longitude = 108.2022),
                CityLocation(name = "TP. Hồ Chí Minh", country = "Việt Nam", latitude = 10.8231, longitude = 106.6297)
            )
            saveFavoriteCities(initial)
            initial
        } else {
            try {
                val type = object : TypeToken<List<CityLocation>>() {}.type
                gson.fromJson<List<CityLocation>>(json, type) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
        val mutable = list.toMutableList()
        cachedFavorites = mutable
        return ArrayList(mutable)
    }

    fun toggleFavoriteCity(city: CityLocation): Boolean {
        val current = (cachedFavorites ?: getFavoriteCities().toMutableList()).toMutableList()
        val existingIndex = current.indexOfFirst { it.name.equals(city.name, ignoreCase = true) }
        val isNowFavorite = if (existingIndex >= 0) {
            current.removeAt(existingIndex)
            false
        } else {
            current.add(city.copy(isCurrentLocation = false))
            true
        }
        cachedFavorites = current
        saveFavoriteCities(current)
        return isNowFavorite
    }

    fun isFavoriteCity(city: CityLocation): Boolean {
        val list = cachedFavorites ?: getFavoriteCities()
        return list.any { it.name.equals(city.name, ignoreCase = true) }
    }

    private fun saveFavoriteCities(list: List<CityLocation>) {
        cachedFavorites = ArrayList(list)
        val json = gson.toJson(list)
        prefs.edit().putString("favorite_cities_json", json).apply()
    }

    // ==========================================
    // CÀI ĐẶT THÔNG BÁO & CẢNH BÁO
    // ==========================================
    fun isMorningNotificationEnabled(): Boolean {
        return prefs.getBoolean("morning_notif_enabled", true)
    }

    fun setMorningNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("morning_notif_enabled", enabled).apply()
    }

    fun isSevereAlertsEnabled(): Boolean {
        return prefs.getBoolean("severe_alerts_enabled", true)
    }

    fun setSevereAlertsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("severe_alerts_enabled", enabled).apply()
    }

    fun getPeriodicNotifIntervalHours(): Int {
        // Mặc định là 2 giờ / lần theo yêu cầu
        return prefs.getInt("periodic_notif_hours", 2)
    }

    fun setPeriodicNotifIntervalHours(hours: Int) {
        prefs.edit().putInt("periodic_notif_hours", hours).apply()
    }

    // ==========================================
    // CÀI ĐẶT ĐỘ TỐI NỀN & AMBIENT LIGHT
    // ==========================================
    fun getBgDimmingLevel(): Float {
        // Mức độ làm tối nền: 0.0f (sáng gốc) đến 0.8f (tối sâu), mặc định 0.15f
        return prefs.getFloat("bg_dimming_level", 0.15f)
    }

    fun setBgDimmingLevel(level: Float) {
        prefs.edit().putFloat("bg_dimming_level", level.coerceIn(0.0f, 0.8f)).apply()
    }

    fun getAmbientGlowStyle(): com.weather24h.app.ui.theme.AmbientGlowStyle {
        val name = prefs.getString("ambient_glow_style", com.weather24h.app.ui.theme.AmbientGlowStyle.NATURAL.name)
        return try {
            com.weather24h.app.ui.theme.AmbientGlowStyle.valueOf(name ?: com.weather24h.app.ui.theme.AmbientGlowStyle.NATURAL.name)
        } catch (_: Exception) {
            com.weather24h.app.ui.theme.AmbientGlowStyle.NATURAL
        }
    }

    fun setAmbientGlowStyle(style: com.weather24h.app.ui.theme.AmbientGlowStyle) {
        prefs.edit().putString("ambient_glow_style", style.name).apply()
    }

    fun getAmbientGlowIntensity(): Float {
        // Cường độ phát sáng: 0.0f (tắt) đến 1.0f (rực rỡ), mặc định 0.6f
        return prefs.getFloat("ambient_glow_intensity", 0.6f)
    }

    fun setAmbientGlowIntensity(intensity: Float) {
        prefs.edit().putFloat("ambient_glow_intensity", intensity.coerceIn(0.0f, 1.0f)).apply()
    }

    // ==========================================
    // API KEY & CHU KỲ TỰ ĐỘNG
    // ==========================================
    fun saveGeminiApiKey(apiKey: String) = stylistService.saveApiKey(apiKey)
    fun clearCustomGeminiApiKey() = stylistService.clearCustomKey()
    fun getGeminiApiKey(): String? = stylistService.getApiKey()
    fun getCustomApiKey(): String? = stylistService.getCustomApiKey()
    fun isUsingCustomKey(): Boolean = stylistService.isUsingCustomKey()
    fun hasDefaultKey(): Boolean = stylistService.hasDefaultKey()

    fun getAutoRefreshInterval(): Int = prefs.getInt("auto_refresh_minutes", 60)
    fun setAutoRefreshInterval(minutes: Int) = prefs.edit().putInt("auto_refresh_minutes", minutes).apply()

    // ==========================================
    // CÀI ĐẶT THEME (SÁNG / TỐI / HỆ THỐNG)
    // ==========================================
    fun getThemeMode(): com.weather24h.app.ui.theme.AppThemeMode {
        val str = prefs.getString("app_theme_mode", com.weather24h.app.ui.theme.AppThemeMode.SYSTEM.name)
        return try {
            com.weather24h.app.ui.theme.AppThemeMode.valueOf(str ?: com.weather24h.app.ui.theme.AppThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            com.weather24h.app.ui.theme.AppThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: com.weather24h.app.ui.theme.AppThemeMode) {
        prefs.edit().putString("app_theme_mode", mode.name).apply()
    }

    // ==========================================
    // CÀI ĐẶT WIDGET (THEME SÁNG / TỐI & ĐỘ TRONG SUỐT)
    // ==========================================
    fun getWidgetThemeMode(): com.weather24h.app.ui.theme.AppThemeMode {
        val str = prefs.getString("widget_theme_mode", com.weather24h.app.ui.theme.AppThemeMode.SYSTEM.name)
        return try {
            com.weather24h.app.ui.theme.AppThemeMode.valueOf(str ?: com.weather24h.app.ui.theme.AppThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            com.weather24h.app.ui.theme.AppThemeMode.SYSTEM
        }
    }

    fun setWidgetThemeMode(mode: com.weather24h.app.ui.theme.AppThemeMode) {
        prefs.edit().putString("widget_theme_mode", mode.name).apply()
    }

    fun getWidgetOpacity(): Float {
        // Mặc định độ đục nền là 0.85 (85%), giá trị từ 0.0f (trong suốt) đến 1.0f (đặc hoàn toàn)
        return prefs.getFloat("widget_bg_opacity", 0.85f)
    }

    fun setWidgetOpacity(opacity: Float) {
        prefs.edit().putFloat("widget_bg_opacity", opacity.coerceIn(0.0f, 1.0f)).apply()
    }

    fun isWidgetAutoRefreshEnabled(): Boolean {
        return prefs.getBoolean("widget_auto_refresh_enabled", true)
    }

    fun setWidgetAutoRefreshEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("widget_auto_refresh_enabled", enabled).apply()
    }

    // ==========================================
    // CÀI ĐẶT MÔ HÌNH DỰ BÁO KHÍ TƯỢNG (FORECAST MODEL)
    // ==========================================
    fun getForecastModel(): com.weather24h.app.data.model.WeatherForecastModel {
        val str = prefs.getString("weather_forecast_model", com.weather24h.app.data.model.WeatherForecastModel.BEST_MATCH.name)
        return try {
            com.weather24h.app.data.model.WeatherForecastModel.valueOf(str ?: com.weather24h.app.data.model.WeatherForecastModel.BEST_MATCH.name)
        } catch (_: Exception) {
            com.weather24h.app.data.model.WeatherForecastModel.BEST_MATCH
        }
    }

    fun setForecastModel(model: com.weather24h.app.data.model.WeatherForecastModel) {
        prefs.edit().putString("weather_forecast_model", model.name).apply()
    }

    // ==========================================
    // CÀI ĐẶT ĐỘ MỜ KHỐI THỜI TIẾT MÀN HÌNH CHÍNH (CARD OPACITY)
    // ==========================================
    fun getCardOpacity(): Float {
        // Mặc định độ đục các thẻ thời tiết là 0.80f (80%), từ 0.20f (rất mờ/trong suốt) đến 1.0f (đặc hoàn toàn)
        return prefs.getFloat("card_bg_opacity", 0.80f)
    }

    fun setCardOpacity(opacity: Float) {
        prefs.edit().putFloat("card_bg_opacity", opacity.coerceIn(0.20f, 1.0f)).apply()
    }

    // ==========================================
    // QUẢN LÝ NGUỒN DỮ LIỆU THỜI TIẾT (PROVIDER)
    // ==========================================
    fun getWeatherProvider(): WeatherProvider {
        val id = prefs.getString("weather_data_provider", WeatherProvider.GOOGLE.id)
        return WeatherProvider.fromId(id)
    }

    fun setWeatherProvider(provider: WeatherProvider) {
        prefs.edit().putString("weather_data_provider", provider.id).apply()
    }

    fun getCustomGoogleWeatherKey(): String? = prefs.getString("custom_google_weather_key", null)

    fun saveGoogleWeatherKey(key: String) {
        prefs.edit().putString("custom_google_weather_key", key.trim()).apply()
    }

    fun clearCustomGoogleWeatherKey() {
        prefs.edit().remove("custom_google_weather_key").apply()
    }

    fun getEffectiveGoogleWeatherKey(): String {
        val custom = getCustomGoogleWeatherKey()
        if (!custom.isNullOrBlank()) return custom
        return BuildConfig.DEFAULT_GOOGLE_WEATHER_KEY
    }

    fun hasGoogleWeatherKey(): Boolean = getEffectiveGoogleWeatherKey().isNotBlank()
    fun isUsingCustomGoogleWeatherKey(): Boolean = !getCustomGoogleWeatherKey().isNullOrBlank()

    suspend fun testGoogleWeatherKey(key: String): Result<String> {
        return googleApiService.testApiKey(key)
    }
}

