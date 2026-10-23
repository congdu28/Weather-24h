package com.weather24h.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.weather24h.app.BuildConfig
import com.weather24h.app.data.model.CityLocation
import com.weather24h.app.data.model.CityWeatherSummary
import com.weather24h.app.data.model.OutfitRecommendation
import com.weather24h.app.data.model.WeatherData
import com.weather24h.app.data.model.WeatherProvider
import com.weather24h.app.data.model.WidgetStyle
import com.weather24h.app.data.remote.GeminiStylistService
import com.weather24h.app.data.remote.GoogleWeatherApiService
import com.weather24h.app.data.remote.WeatherApiComService
import com.weather24h.app.data.remote.WeatherApiService
import com.weather24h.app.ui.widget.WeatherWidgetProvider
import com.weather24h.app.util.WeatherNotificationHelper
import kotlinx.coroutines.CancellationException

class WeatherRepository(
    private val context: Context,
    private val apiService: WeatherApiService,
    private val stylistService: GeminiStylistService
) {
    private val prefs = context.getSharedPreferences("weather24h_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val googleApiService = GoogleWeatherApiService()
    private val weatherApiComService = WeatherApiComService()

    init {
        // Áp dụng mặc định mới một lần khi nâng cấp; giữ lựa chọn thủ công ở các lần mở sau.
        if (!prefs.getBoolean("open_meteo_default_applied_v26", false)) {
            prefs.edit()
                .putString("weather_data_provider", WeatherProvider.OPEN_METEO.id)
                .putString("weather_forecast_model", com.weather24h.app.data.model.WeatherForecastModel.BEST_MATCH.name)
                .putBoolean("open_meteo_default_applied_v26", true)
                .apply()
        }
    }

    /** Tải nguồn đã chọn, sau đó dùng Open-Meteo hoặc cache cùng vị trí khi cần. */
    suspend fun getWeatherData(location: CityLocation): WeatherData {
        return try {
            val provider = getWeatherProvider()
            val freshWeather = when (provider) {
                WeatherProvider.WEATHER_API -> {
                    val effectiveKey = getEffectiveWeatherApiKey()
                    if (effectiveKey.isBlank()) {
                        apiService.getFullWeather(location, getForecastModel()).copy(
                            providerName = "Open-Meteo (chưa cấu hình WeatherAPI.com)"
                        )
                    } else try {
                        weatherApiComService.getFullWeather(location, effectiveKey) {
                            try {
                                apiService.getFullWeather(location, getForecastModel())
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                null
                            }
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // Tự động chuyển dự phòng sang Open-Meteo khi WeatherAPI gặp sự cố mạng
                        apiService.getFullWeather(location, getForecastModel()).copy(
                            providerName = "Open-Meteo (Dự phòng tự động)"
                        )
                    }
                }
                WeatherProvider.GOOGLE -> {
                    val effectiveKey = getEffectiveGoogleWeatherKey()
                    if (effectiveKey.isNotBlank()) {
                        try {
                            googleApiService.getFullWeather(location, effectiveKey)
                        } catch (e: CancellationException) {
                            throw e
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
                }
                WeatherProvider.OPEN_METEO -> {
                    apiService.getFullWeather(location, getForecastModel())
                }
            }

            // Lưu vào bộ nhớ đệm ngoại tuyến
            saveCachedWeather(freshWeather)
            saveFavoriteSummary(location, freshWeather)

            // Cập nhật Widget màn hình chính
            WeatherWidgetProvider.updateAllWidgets(context, freshWeather)

            // Kiểm tra và gửi cảnh báo nếu bật tính năng
            if (isSevereAlertsEnabled()) {
                WeatherNotificationHelper.checkAndSendSevereAlerts(context, freshWeather)
            }

            freshWeather
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Khi mất mạng hoặc lỗi kết nối, ưu tiên lấy dữ liệu đã cache
            val cached = getCachedWeather(location)
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
    // QUẢN LÝ BỘ NHỚ ĐỆM NGOẠI TUYẾN (OFFLINE CACHE & PRE-FETCH)
    // ==========================================
    private val memoryWeatherCache = java.util.concurrent.ConcurrentHashMap<String, WeatherData>()

    private fun getCityKey(location: CityLocation): String {
        return "${location.name.lowercase().trim()}_${String.format(java.util.Locale.US, "%.2f", location.latitude)}_${String.format(java.util.Locale.US, "%.2f", location.longitude)}"
    }

    fun saveCachedWeatherForCity(weather: WeatherData) {
        val key = getCityKey(weather.location)
        memoryWeatherCache[key] = weather
        try {
            val json = gson.toJson(weather)
            prefs.edit()
                .putString("cache_city_$key", json)
                .putString("cache_city_by_name_${weather.location.name.lowercase().trim()}", json)
                .apply()
        } catch (_: Exception) {}
    }

    fun getCachedWeatherForCity(location: CityLocation): WeatherData? {
        val key = getCityKey(location)
        val inMem = memoryWeatherCache[key]
        if (inMem != null) return inMem

        val byName = memoryWeatherCache.values.firstOrNull {
            it.location.name.equals(location.name, ignoreCase = true)
        }
        if (byName != null) return byName

        // Kiểm tra Shared Preferences theo key tọa độ
        val jsonByKey = prefs.getString("cache_city_$key", null)
        if (!jsonByKey.isNullOrEmpty()) {
            try {
                val fromPrefs = gson.fromJson(jsonByKey, WeatherData::class.java)
                if (fromPrefs != null) {
                    memoryWeatherCache[key] = fromPrefs
                    return fromPrefs
                }
            } catch (_: Exception) {}
        }

        // Kiểm tra Shared Preferences theo tên thành phố
        val jsonByName = prefs.getString("cache_city_by_name_${location.name.lowercase().trim()}", null)
        if (!jsonByName.isNullOrEmpty()) {
            try {
                val fromPrefs = gson.fromJson(jsonByName, WeatherData::class.java)
                if (fromPrefs != null) {
                    memoryWeatherCache[key] = fromPrefs
                    return fromPrefs
                }
            } catch (_: Exception) {}
        }

        val cachedDefault = getCachedWeather(location)
        if (cachedDefault != null) {
            memoryWeatherCache[key] = cachedDefault
            return cachedDefault
        }

        return null
    }

    fun saveCachedWeather(weather: WeatherData) {
        try {
            saveCachedWeatherForCity(weather)
            val json = gson.toJson(weather)
            prefs.edit()
                .putString("cached_weather_json", json)
                .putString("last_cached_time", weather.lastUpdated)
                .putInt("cached_weather_schema", 3)
                .apply()
        } catch (_: Exception) {}
    }

    fun getCachedWeather(location: CityLocation? = null): WeatherData? {
        return try {
            if (prefs.getInt("cached_weather_schema", 0) != 3) return null
            val json = prefs.getString("cached_weather_json", null) ?: return null
            val cached = gson.fromJson(json, WeatherData::class.java) ?: return null
            if (location != null && (
                    kotlin.math.abs(cached.location.latitude - location.latitude) > 0.01 ||
                    kotlin.math.abs(cached.location.longitude - location.longitude) > 0.01
                )) null else cached
        } catch (_: Exception) {
            null
        }
    }

    fun clearOfflineCache() {
        memoryWeatherCache.clear()
        prefs.edit()
            .remove("cached_weather_json")
            .remove("last_cached_time")
            .remove("cached_weather_schema")
            .apply()
    }

    // ==========================================
    // THÀNH PHỐ MẶC ĐỊNH & KHỞI ĐỘNG
    // ==========================================
    fun isStartWithGps(): Boolean {
        // Bản cài mới ưu tiên vị trí hiện tại; lựa chọn tắt của người dùng vẫn được giữ lại.
        return prefs.getBoolean("start_with_gps", true)
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
            val parsed = parseFavoriteCitiesSafely(json)
            if (parsed.isEmpty()) {
                // Nếu chuỗi JSON không rỗng nhưng rỗng sau khi parse (ví dụ "[]" hoặc lỗi format),
                // cố gắng lấy danh sách mặc định nhưng KHÔNG ghi đè vào prefs để tránh xóa nhầm dữ liệu người dùng
                listOf(
                    CityLocation.DEFAULT_LOCATION,
                    CityLocation(name = "Đà Nẵng", country = "Việt Nam", latitude = 16.0544, longitude = 108.2022),
                    CityLocation(name = "TP. Hồ Chí Minh", country = "Việt Nam", latitude = 10.8231, longitude = 106.6297)
                )
            } else {
                parsed
            }
        }
        val mutable = list.toMutableList()
        cachedFavorites = mutable
        return ArrayList(mutable)
    }

    private fun parseFavoriteCitiesSafely(json: String): List<CityLocation> {
        if (json.isBlank()) return emptyList()

        // Tầng 1: Sử dụng mảng Array<CityLocation>::class.java
        // (Trong JVM, Array luôn giữ nguyên reified type ở runtime, miễn nhiễm 100% với R8/ProGuard generic erasure)
        try {
            val array = gson.fromJson(json, Array<CityLocation>::class.java)
            if (array != null && array.isNotEmpty()) {
                val validList = array.filter { it.name.isNotBlank() }
                if (validList.isNotEmpty()) {
                    return validList
                }
            }
        } catch (_: Exception) {}

        // Tầng 2: Thử qua TypeToken (với keep rules đã kích hoạt)
        try {
            val type = object : TypeToken<List<CityLocation>>() {}.type
            val parsedList = gson.fromJson<List<CityLocation>>(json, type)
            if (!parsedList.isNullOrEmpty()) {
                val validList = parsedList.filter { it.name.isNotBlank() }
                if (validList.isNotEmpty()) {
                    return validList
                }
            }
        } catch (_: Exception) {}

        // Tầng 3: Giải mã JSON cấp thấp qua JsonParser (phòng trường hợp cấu trúc từ bản rất cũ)
        try {
            val element = JsonParser.parseString(json)
            if (element.isJsonArray) {
                val jsonArray = element.asJsonArray
                val manualList = mutableListOf<CityLocation>()
                for (item in jsonArray) {
                    if (item.isJsonObject) {
                        val obj = item.asJsonObject
                        val name = obj.get("name")?.asString?.trim() ?: ""
                        if (name.isNotBlank()) {
                            val country = obj.get("country")?.asString ?: ""
                            val admin1 = obj.get("admin1")?.asString
                            val lat = obj.get("latitude")?.asDouble ?: 0.0
                            val lon = obj.get("longitude")?.asDouble ?: 0.0
                            val isCurrent = obj.get("isCurrentLocation")?.asBoolean ?: false
                            manualList.add(
                                CityLocation(
                                    name = name,
                                    country = country,
                                    admin1 = admin1,
                                    latitude = lat,
                                    longitude = lon,
                                    isCurrentLocation = isCurrent
                                )
                            )
                        }
                    }
                }
                if (manualList.isNotEmpty()) {
                    return manualList
                }
            }
        } catch (_: Exception) {}

        return emptyList()
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

    fun removeFavoriteCity(city: CityLocation) {
        val current = (cachedFavorites ?: getFavoriteCities().toMutableList()).toMutableList()
        val index = current.indexOfFirst { it.name.equals(city.name, ignoreCase = true) }
        if (index >= 0) {
            current.removeAt(index)
            cachedFavorites = current
            saveFavoriteCities(current)
        }
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

    fun getFavoriteSummaries(): Map<String, CityWeatherSummary> {
        val json = prefs.getString("favorite_summaries_json", null) ?: return emptyMap()
        try {
            val type = object : TypeToken<Map<String, CityWeatherSummary>>() {}.type
            val result = gson.fromJson<Map<String, CityWeatherSummary>>(json, type)
            if (result != null) return result
        } catch (_: Exception) {}

        try {
            val element = JsonParser.parseString(json)
            if (element.isJsonObject) {
                val obj = element.asJsonObject
                val map = mutableMapOf<String, CityWeatherSummary>()
                for ((key, value) in obj.entrySet()) {
                    if (value.isJsonObject) {
                        val summary = gson.fromJson(value, CityWeatherSummary::class.java)
                        if (summary != null) {
                            map[key] = summary
                        }
                    }
                }
                return map
            }
        } catch (_: Exception) {}

        return emptyMap()
    }

    fun saveFavoriteSummary(city: CityLocation, weather: WeatherData) {
        try {
            val currentMap = getFavoriteSummaries().toMutableMap()
            currentMap[city.name.lowercase()] = CityWeatherSummary(
                cityName = city.name,
                tempC = weather.current.temperature.toInt(),
                conditionText = weather.current.condition.description,
                weatherType = weather.current.condition.type,
                isDay = weather.current.isDay,
                updatedAt = System.currentTimeMillis()
            )
            val json = gson.toJson(currentMap)
            prefs.edit().putString("favorite_summaries_json", json).apply()
        } catch (_: Exception) {}
    }

    fun saveFavoriteSummaries(summaries: Map<String, CityWeatherSummary>) {
        try {
            val json = gson.toJson(summaries)
            prefs.edit().putString("favorite_summaries_json", json).apply()
        } catch (_: Exception) {}
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
        // Mức độ làm tối nền: 0.0f (sáng gốc) đến 0.8f (tối sâu), mặc định 0.0f
        return prefs.getFloat("bg_dimming_level", 0.0f)
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
        // Cường độ phát sáng: 0.0f (tắt) đến 1.0f (rực rỡ), mặc định 1.0f
        return prefs.getFloat("ambient_glow_intensity", 1.0f)
    }

    fun setAmbientGlowIntensity(intensity: Float) {
        prefs.edit().putFloat("ambient_glow_intensity", intensity.coerceIn(0.0f, 1.0f)).apply()
    }

    // ==========================================
    // CÀI ĐẶT HIỆU NĂNG NỀN ĐỘNG THỜI TIẾT
    // ==========================================
    fun getAtmospherePerformanceMode(): com.weather24h.app.ui.theme.AtmospherePerformanceMode {
        val name = prefs.getString("atmosphere_performance_mode", com.weather24h.app.ui.theme.AtmospherePerformanceMode.BALANCED.name)
        return try {
            com.weather24h.app.ui.theme.AtmospherePerformanceMode.valueOf(name ?: com.weather24h.app.ui.theme.AtmospherePerformanceMode.BALANCED.name)
        } catch (_: Exception) {
            com.weather24h.app.ui.theme.AtmospherePerformanceMode.BALANCED
        }
    }

    fun setAtmospherePerformanceMode(mode: com.weather24h.app.ui.theme.AtmospherePerformanceMode) {
        prefs.edit().putString("atmosphere_performance_mode", mode.name).apply()
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

    fun getWidgetStyle(): WidgetStyle {
        val str = prefs.getString("widget_visual_style", WidgetStyle.FROSTED_GLASS.name)
        return try {
            WidgetStyle.valueOf(str ?: WidgetStyle.FROSTED_GLASS.name)
        } catch (_: Exception) {
            WidgetStyle.FROSTED_GLASS
        }
    }

    fun setWidgetStyle(style: WidgetStyle) {
        prefs.edit().putString("widget_visual_style", style.name).apply()
    }

    // ==========================================
    // CÀI ĐẶT ÂM LỊCH VIỆT NAM (VIETNAMESE LUNAR CALENDAR)
    // ==========================================
    fun isLunarCalendarEnabled(): Boolean {
        return prefs.getBoolean("vietnamese_lunar_calendar_enabled", true)
    }

    fun setLunarCalendarEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("vietnamese_lunar_calendar_enabled", enabled).apply()
    }

    fun isLunarFestivalNotifEnabled(): Boolean {
        return prefs.getBoolean("lunar_festival_notif_enabled", true)
    }

    fun setLunarFestivalNotifEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("lunar_festival_notif_enabled", enabled).apply()
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
        // Mặc định độ đục các thẻ thời tiết là 0.60f (60%), từ 0.20f đến 1.0f
        val saved = prefs.getFloat("card_bg_opacity", 0.60f)
        // Nếu trước đó đang ở mức mặc định cũ 0.25f (25%), tự động nâng cấp lên 0.60f (60%) theo chuẩn mới
        if (kotlin.math.abs(saved - 0.25f) < 0.01f) {
            setCardOpacity(0.60f)
            return 0.60f
        }
        return saved
    }

    fun setCardOpacity(opacity: Float) {
        prefs.edit().putFloat("card_bg_opacity", opacity.coerceIn(0.20f, 1.0f)).apply()
    }

    // ==========================================
    // QUẢN LÝ NGUỒN DỮ LIỆU THỜI TIẾT (PROVIDER)
    // ==========================================
    fun getWeatherProvider(): WeatherProvider {
        val id = prefs.getString("weather_data_provider", WeatherProvider.OPEN_METEO.id)
        // Người dùng từng chọn Google sẽ tự chuyển về nguồn chính hiện tại.
        return WeatherProvider.fromId(id).let {
            if (it == WeatherProvider.GOOGLE) WeatherProvider.OPEN_METEO else it
        }
    }

    fun setWeatherProvider(provider: WeatherProvider) {
        prefs.edit().putString("weather_data_provider", provider.id).apply()
    }

    // --- WeatherAPI.com (Nguồn trạm quan trắc thực tế) ---
    fun getCustomWeatherApiKey(): String? = prefs.getString("custom_weather_api_key", null)

    fun saveWeatherApiKey(key: String) {
        prefs.edit().putString("custom_weather_api_key", key.trim()).apply()
    }

    fun clearCustomWeatherApiKey() {
        prefs.edit().remove("custom_weather_api_key").apply()
    }

    fun getEffectiveWeatherApiKey(): String {
        val custom = getCustomWeatherApiKey()
        if (!custom.isNullOrBlank()) return custom
        return BuildConfig.DEFAULT_WEATHER_API_KEY
    }

    fun hasWeatherApiKey(): Boolean = getEffectiveWeatherApiKey().isNotBlank()
    fun isUsingCustomWeatherApiKey(): Boolean = !getCustomWeatherApiKey().isNullOrBlank()

    suspend fun testWeatherApiKey(key: String): Result<String> {
        return weatherApiComService.testApiKey(key)
    }

    // --- Google Weather API (Google DeepMind WeatherNext) ---
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

