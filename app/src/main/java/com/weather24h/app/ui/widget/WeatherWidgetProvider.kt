package com.weather24h.app.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.widget.RemoteViews
import android.widget.Toast
import com.google.gson.Gson
import com.weather24h.app.MainActivity
import com.weather24h.app.R
import com.weather24h.app.data.model.CityLocation
import com.weather24h.app.data.model.WeatherCondition
import com.weather24h.app.data.model.WeatherData
import com.weather24h.app.data.model.WeatherType
import com.weather24h.app.data.remote.GeminiStylistService
import com.weather24h.app.data.remote.WeatherApiService
import com.weather24h.app.data.repository.WeatherRepository
import com.weather24h.app.util.LocationHelper
import com.weather24h.app.util.WeatherNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class WeatherWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_REFRESH_WIDGET) {
            val pendingResult = goAsync()
            refreshWidgetWeatherWithLocation(context, isSilent = false, pendingResult = pendingResult)
            return
        }
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            updateAllWidgets(context)
            return
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val weather = getSavedWeather(context)
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId, weather)
        }
        // Tự động kiểm tra và làm mới vị trí thời gian thực ngầm nếu tới chu kỳ
        refreshWidgetWeatherWithLocation(context, isSilent = true)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val weather = getSavedWeather(context)
        updateWidget(context, appWidgetManager, appWidgetId, weather)
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.weather24h.app.ACTION_REFRESH_WIDGET"
        const val EXTRA_FORCE_GPS_REFRESH = "EXTRA_FORCE_GPS_REFRESH"
        private var lastAutoRefreshTime = 0L
        private var lastManualRefreshTime = 0L

        /**
         * Tự động xác định vị trí GPS thời gian thực và cập nhật thời tiết cho tất cả Widget
         * Nếu có vị trí mới (ví dụ vừa di chuyển từ tỉnh này sang tỉnh khác), widget sẽ lấy thời tiết vị trí mới đó.
         */
        fun refreshWidgetWeatherWithLocation(
            context: Context,
            isSilent: Boolean = false,
            pendingResult: android.content.BroadcastReceiver.PendingResult? = null
        ) {
            val appContext = context.applicationContext
            val now = System.currentTimeMillis()
            if (isSilent && now - lastAutoRefreshTime < 60_000L) {
                pendingResult?.finish()
                return
            }
            if (!isSilent && now - lastManualRefreshTime < 3_000L) {
                // Chặn spam làm mới liên tục để bảo vệ pin và dữ liệu
                pendingResult?.finish()
                return
            }
            if (isSilent) {
                lastAutoRefreshTime = now
            } else {
                lastManualRefreshTime = now
            }

            // Phản hồi giao diện tức thì nếu người dùng bấm nút làm mới trực tiếp trên Widget
            if (!isSilent) {
                try {
                    val appWidgetManager = AppWidgetManager.getInstance(appContext)

                    val detailedViews = RemoteViews(appContext.packageName, R.layout.weather_widget)
                    detailedViews.setTextViewText(R.id.widget_updated, "Đang định vị & làm mới...")
                    val detailedComp = ComponentName(appContext, WeatherWidgetProvider::class.java)
                    val detailedIds = appWidgetManager.getAppWidgetIds(detailedComp)
                    if (detailedIds.isNotEmpty()) {
                        appWidgetManager.partiallyUpdateAppWidget(detailedIds, detailedViews)
                    }

                    val compactViews = RemoteViews(appContext.packageName, R.layout.weather_widget_compact)
                    compactViews.setTextViewText(R.id.compact_widget_desc, "Đang lấy vị trí mới...")
                    val compactComp = ComponentName(appContext, CompactWeatherWidgetProvider::class.java)
                    val compactIds = appWidgetManager.getAppWidgetIds(compactComp)
                    if (compactIds.isNotEmpty()) {
                        appWidgetManager.partiallyUpdateAppWidget(compactIds, compactViews)
                    }

                    val forecastViews = RemoteViews(appContext.packageName, R.layout.weather_widget_forecast)
                    forecastViews.setTextViewText(R.id.forecast_widget_desc, "Đang lấy vị trí mới...")
                    val forecastComp = ComponentName(appContext, Forecast5DayWidgetProvider::class.java)
                    val forecastIds = appWidgetManager.getAppWidgetIds(forecastComp)
                    if (forecastIds.isNotEmpty()) {
                        appWidgetManager.partiallyUpdateAppWidget(forecastIds, forecastViews)
                    }

                    val miniViews = RemoteViews(appContext.packageName, R.layout.weather_widget_mini)
                    miniViews.setTextViewText(R.id.mini_widget_desc, "Đang tải...")
                    val miniComp = ComponentName(appContext, MiniWeatherWidgetProvider::class.java)
                    val miniIds = appWidgetManager.getAppWidgetIds(miniComp)
                    if (miniIds.isNotEmpty()) {
                        appWidgetManager.partiallyUpdateAppWidget(miniIds, miniViews)
                    }
                } catch (_: Exception) {}
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val repository = WeatherRepository(
                        context = appContext,
                        apiService = WeatherApiService(),
                        stylistService = GeminiStylistService(appContext)
                    )

                    // 1. Lấy vị trí GPS thực tế hiện tại của người dùng
                    val currentLocation = LocationHelper.getCurrentLocation(appContext, timeoutMillis = 7000L)

                    val targetCity = if (currentLocation != null) {
                        try {
                            val address = repository.getAddressFromCoordinates(
                                currentLocation.latitude,
                                currentLocation.longitude
                            )
                            CityLocation(
                                name = address.first,
                                country = address.second,
                                latitude = currentLocation.latitude,
                                longitude = currentLocation.longitude,
                                isCurrentLocation = true
                            ).also {
                                repository.setLastActiveCity(it)
                            }
                        } catch (_: Exception) {
                            CityLocation(
                                name = "Vị trí hiện tại",
                                country = "Việt Nam",
                                latitude = currentLocation.latitude,
                                longitude = currentLocation.longitude,
                                isCurrentLocation = true
                            ).also {
                                repository.setLastActiveCity(it)
                            }
                        }
                    } else {
                        repository.getLastActiveCity()
                    }

                    val fresh = repository.getWeatherData(targetCity)
                    val finalWeather = fresh.copy(location = targetCity)
                    repository.saveCachedWeather(finalWeather)

                    withContext(Dispatchers.Main) {
                        updateAllWidgets(appContext, finalWeather)
                        if (!isSilent) {
                            Toast.makeText(
                                appContext,
                                "Đã cập nhật: ${targetCity.name} (${finalWeather.current.temperature.roundToInt()}°C)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    // Đồng bộ bản tin thông báo nhiệt độ hệ thống nếu có bật
                    if (repository.getPeriodicNotifIntervalHours() > 0) {
                        WeatherNotificationHelper.showCurrentWeatherNotification(appContext, finalWeather)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        updateAllWidgets(appContext)
                        if (!isSilent) {
                            Toast.makeText(
                                appContext,
                                "Không thể kết nối mạng để làm mới thời tiết",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } finally {
                    pendingResult?.finish()
                }
            }
        }

        fun updateAllWidgets(context: Context, weather: WeatherData? = null, aiTip: String? = null) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return

            // 1. Cập nhật Widget Chi tiết theo giờ (4x2 / 6 khung giờ)
            val componentName = ComponentName(context, WeatherWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
            val data = weather ?: getSavedWeather(context)
            for (widgetId in widgetIds) {
                updateWidget(context, appWidgetManager, widgetId, data, aiTip)
            }

            // 2. Đồng thời cập nhật Widget Tối giản (2x1 / 3x1)
            CompactWeatherWidgetProvider.updateAllCompactWidgets(context, data)

            // 3. Đồng thời cập nhật Widget Dự báo 5 ngày (4x1 / 4x2)
            Forecast5DayWidgetProvider.updateAllForecastWidgets(context, data)

            // 4. Đồng thời cập nhật Widget Mini (1x1)
            MiniWeatherWidgetProvider.updateAllMiniWidgets(context, data)

            // 5. Đồng thời cập nhật Widget Lịch Vạn Niên Tháng (4x3)
            LunarMonthWidgetProvider.updateAllLunarMonthWidgets(context, data)
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int,
            weather: WeatherData?,
            aiTip: String? = null
        ) {
            try {
                val views = RemoteViews(context.packageName, R.layout.weather_widget)

                // Cài đặt Theme & Độ trong suốt nền
                val prefs = context.getSharedPreferences("weather24h_prefs", Context.MODE_PRIVATE)
                val isDark = WidgetThemeHelper.resolveIsDark(context)
                val bgOpacity = prefs.getFloat("widget_bg_opacity", 0.85f).coerceIn(0.0f, 1.0f)
                val styleStr = prefs.getString("widget_visual_style", "FROSTED_GLASS")
                val widgetStyle = try {
                    com.weather24h.app.data.model.WidgetStyle.valueOf(styleStr ?: "FROSTED_GLASS")
                } catch (_: Exception) {
                    com.weather24h.app.data.model.WidgetStyle.FROSTED_GLASS
                }

                // Áp dụng hình nền & độ trong suốt theo phong cách thị giác đã chọn
                val bgDrawableRes = if (isDark || widgetStyle == com.weather24h.app.data.model.WidgetStyle.SOLID_DARK) {
                    R.drawable.widget_bg_dark
                } else {
                    R.drawable.widget_bg_light
                }
                views.setImageViewResource(R.id.widget_bg_layer, bgDrawableRes)

                val alphaInt = when (widgetStyle) {
                    com.weather24h.app.data.model.WidgetStyle.MINIMAL_CLEAN -> 0
                    com.weather24h.app.data.model.WidgetStyle.SOLID_DARK -> 255
                    else -> (bgOpacity * 255).toInt().coerceIn(0, 255)
                }
                views.setInt(R.id.widget_bg_layer, "setImageAlpha", alphaInt)

                // Áp dụng màu thích ứng thời tiết nếu chọn ADAPTIVE_COLOR
                if (widgetStyle == com.weather24h.app.data.model.WidgetStyle.ADAPTIVE_COLOR && weather != null) {
                    val adaptiveColor = when (weather.current.condition.type) {
                        WeatherType.THUNDERSTORM -> 0xFF311042.toInt()
                        WeatherType.RAIN, WeatherType.DRIZZLE -> 0xFF0C4A6E.toInt()
                        WeatherType.CLEAR -> if (weather.current.isDay) 0xFF0369A1.toInt() else 0xFF020617.toInt()
                        WeatherType.PARTLY_CLOUDY, WeatherType.CLOUDY -> 0xFF1E293B.toInt()
                        WeatherType.SNOW -> 0xFF0F172A.toInt()
                        WeatherType.FOG -> 0xFF334155.toInt()
                    }
                    views.setInt(R.id.widget_bg_layer, "setColorFilter", adaptiveColor)
                } else {
                    views.setInt(R.id.widget_bg_layer, "setColorFilter", 0)
                }

                // Bảng màu chữ tương ứng với theme sáng / tối
                val colorPrimary = if (isDark || widgetStyle != com.weather24h.app.data.model.WidgetStyle.FROSTED_GLASS) 0xFFFFFFFF.toInt() else 0xFF0F172A.toInt()
                val colorSecondary = if (isDark || widgetStyle != com.weather24h.app.data.model.WidgetStyle.FROSTED_GLASS) 0xFFCBD5E1.toInt() else 0xFF475569.toInt()
                val colorAccent = if (isDark || widgetStyle != com.weather24h.app.data.model.WidgetStyle.FROSTED_GLASS) 0xFF38BDF8.toInt() else 0xFF0284C7.toInt()
                val colorDim = if (isDark) 0xFF94A3B8.toInt() else 0xFF64748B.toInt()
                val colorAiTip = if (isDark) 0xFFF0ABFC.toInt() else 0xFF7E22CE.toInt()
                val chipBgRes = if (isDark) R.drawable.widget_hour_chip_dark else R.drawable.widget_hour_chip_light
                val btnRefreshBgRes = if (isDark) R.drawable.widget_refresh_btn_bg_dark else R.drawable.widget_refresh_btn_bg_light

                // Cập nhật màu sắc cho các view chính
                views.setTextColor(R.id.widget_city_name, colorPrimary)
                views.setTextColor(R.id.widget_temp, colorPrimary)
                views.setTextColor(R.id.widget_weather_desc, colorSecondary)
                views.setTextColor(R.id.widget_range, colorAccent)
                views.setTextColor(R.id.widget_ai_tip, colorAiTip)
                views.setTextColor(R.id.widget_updated, colorDim)

                // Nút bấm Làm mới trực tiếp trên Widget
                views.setInt(R.id.widget_btn_refresh, "setBackgroundResource", btnRefreshBgRes)
                views.setInt(R.id.widget_btn_refresh, "setColorFilter", colorAccent)

                val refreshIntent = Intent(context, WeatherWidgetProvider::class.java).apply {
                    action = ACTION_REFRESH_WIDGET
                }
                val refreshPendingIntent = PendingIntent.getBroadcast(
                    context,
                    2001,
                    refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)
                views.setOnClickPendingIntent(R.id.widget_updated, refreshPendingIntent)

                // Click vào toàn bộ khối widget mở ứng dụng và tự động cập nhật vị trí GPS mới
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(EXTRA_FORCE_GPS_REFRESH, true)
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                // Mảng ID của 6 khung giờ
                val hourContainers = intArrayOf(
                    R.id.widget_h1_container, R.id.widget_h2_container, R.id.widget_h3_container,
                    R.id.widget_h4_container, R.id.widget_h5_container, R.id.widget_h6_container
                )
                val hourTimeIds = intArrayOf(
                    R.id.widget_h1_time, R.id.widget_h2_time, R.id.widget_h3_time,
                    R.id.widget_h4_time, R.id.widget_h5_time, R.id.widget_h6_time
                )
                val hourIconIds = intArrayOf(
                    R.id.widget_h1_icon, R.id.widget_h2_icon, R.id.widget_h3_icon,
                    R.id.widget_h4_icon, R.id.widget_h5_icon, R.id.widget_h6_icon
                )
                val hourTempIds = intArrayOf(
                    R.id.widget_h1_temp, R.id.widget_h2_temp, R.id.widget_h3_temp,
                    R.id.widget_h4_temp, R.id.widget_h5_temp, R.id.widget_h6_temp
                )
                val hourRainIds = intArrayOf(
                    R.id.widget_h1_rain, R.id.widget_h2_rain, R.id.widget_h3_rain,
                    R.id.widget_h4_rain, R.id.widget_h5_rain, R.id.widget_h6_rain
                )

                // Áp dụng style nền chip và màu chữ cho cả 6 khung giờ
                for (i in 0 until 6) {
                    views.setInt(hourContainers[i], "setBackgroundResource", chipBgRes)
                    views.setTextColor(hourTimeIds[i], colorDim)
                    views.setTextColor(hourTempIds[i], colorPrimary)
                    views.setTextColor(hourRainIds[i], colorAccent)
                }

                // Xử lý Responsive theo kích thước ô Widget thực tế từ Launcher
                val options = appWidgetManager.getAppWidgetOptions(widgetId)
                val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 300
                val minHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 120

                // Ẩn dòng AI Tip & Cập nhật nếu chiều cao bị kéo nhỏ (< 95dp)
                views.setViewVisibility(
                    R.id.widget_bottom_row,
                    if (minHeight < 95) android.view.View.GONE else android.view.View.VISIBLE
                )

                // Tính toán số lượng chip giờ hiển thị linh hoạt theo kích thước chiều rộng màn hình:
                // >= 330dp: Đủ rộng để hiển thị trọn vẹn 6 chip (~55dp/chip)
                // 280..329dp: Hiển thị 5 chip cân đối (~56-65dp/chip)
                // < 280dp: Hiển thị 4 chip thoáng đãng (~65-70dp/chip)
                val numVisibleChips = when {
                    minWidth >= 330 -> 6
                    minWidth >= 280 -> 5
                    else -> 4
                }
                views.setViewVisibility(
                    R.id.widget_h5_container,
                    if (numVisibleChips >= 5) android.view.View.VISIBLE else android.view.View.GONE
                )
                views.setViewVisibility(
                    R.id.widget_h6_container,
                    if (numVisibleChips >= 6) android.view.View.VISIBLE else android.view.View.GONE
                )

                if (weather != null) {
                    val condition = weather.current.condition
                    val emoji = getWeatherEmoji(condition, weather.current.isDay)

                    views.setTextViewText(R.id.widget_city_name, weather.location.name)
                    views.setTextViewText(R.id.widget_temp, "${weather.current.temperature.roundToInt()}°")
                    views.setTextViewText(R.id.widget_weather_icon, emoji)
                    views.setTextViewText(
                        R.id.widget_weather_desc,
                        "${condition.description} • Cảm giác ${weather.current.feelsLike.roundToInt()}°C"
                    )
                    val lunar = com.weather24h.app.util.VietnameseLunarCalendar.getTodayLunar()
                    views.setTextViewText(
                        R.id.widget_range,
                        "Cao: ${weather.maxTemp24h.roundToInt()}° • Thấp: ${weather.minTemp24h.roundToInt()}° • ${lunar.formattedLunarShort}"
                    )
                    views.setTextViewText(R.id.widget_updated, "Cập nhật ${weather.lastUpdated} 🔄")

                    // Cập nhật 6 mốc giờ tới
                    val hourlyList = weather.hourly24h.take(6)
                    for (i in 0 until 6) {
                        if (i < hourlyList.size) {
                            val h = hourlyList[i]
                            val timeLabel = h.hourLabel
                            views.setTextViewText(hourTimeIds[i], timeLabel)
                            views.setTextViewText(hourIconIds[i], getWeatherEmoji(h.condition, h.isDay))
                            views.setTextViewText(hourTempIds[i], "${h.temperature.roundToInt()}°")
                            val isDry = (h.precipitationProbability in 0..19) && ((h.precipitationMm ?: 0.0) < 0.1)
                            val rainLabel = if (isDry) "—" else h.rainProbabilityText
                            views.setTextViewText(hourRainIds[i], rainLabel)
                        }
                    }

                    val tip = aiTip ?: "👔 AI Stylist: ${condition.description}"
                    views.setTextViewText(R.id.widget_ai_tip, tip)
                } else {
                    views.setTextViewText(R.id.widget_city_name, "Weather 24h")
                    views.setTextViewText(R.id.widget_temp, "--°")
                    views.setTextViewText(R.id.widget_weather_icon, "☀️")
                    views.setTextViewText(R.id.widget_weather_desc, "Chạm để cập nhật thời tiết")
                    views.setTextViewText(R.id.widget_range, "Dự báo thời tiết 24h & 7 ngày")
                    views.setTextViewText(R.id.widget_ai_tip, "👔 AI Stylist sẵn sàng")
                    views.setTextViewText(R.id.widget_updated, "Chạm để cập nhật 🔄")
                }

                appWidgetManager.updateAppWidget(widgetId, views)
            } catch (e: Exception) {
            }
        }

        private fun getWeatherEmoji(condition: WeatherCondition, isDay: Boolean = true): String {
            return when (condition.type) {
                WeatherType.CLEAR -> if (isDay) "☀️" else "🌙"
                WeatherType.PARTLY_CLOUDY -> if (isDay) "🌤️" else "☁️"
                WeatherType.CLOUDY -> "☁️"
                WeatherType.FOG -> "🌫️"
                WeatherType.DRIZZLE -> "🌦️"
                WeatherType.RAIN -> "🌧️"
                WeatherType.THUNDERSTORM -> "⛈️"
                WeatherType.SNOW -> "❄️"
            }
        }

        private fun getSavedWeather(context: Context): WeatherData? {
            return try {
                val prefs = context.getSharedPreferences("weather24h_prefs", Context.MODE_PRIVATE)
                if (prefs.getInt("cached_weather_schema", 0) != 3) return null
                val json = prefs.getString("cached_weather_json", null) ?: return null
                Gson().fromJson(json, WeatherData::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}
