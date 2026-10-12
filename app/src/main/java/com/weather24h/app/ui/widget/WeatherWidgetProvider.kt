package com.weather24h.app.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.widget.RemoteViews
import com.google.gson.Gson
import com.weather24h.app.MainActivity
import com.weather24h.app.R
import com.weather24h.app.data.model.WeatherCondition
import com.weather24h.app.data.model.WeatherData
import com.weather24h.app.data.model.WeatherType
import kotlin.math.roundToInt

class WeatherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val weather = getSavedWeather(context)
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId, weather)
        }
    }

    companion object {
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
                val themeStr = prefs.getString("widget_theme_mode", "SYSTEM") ?: "SYSTEM"
                val isDark = when (themeStr) {
                    "LIGHT" -> false
                    "DARK" -> true
                    else -> {
                        val nightFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                        nightFlags == Configuration.UI_MODE_NIGHT_YES
                    }
                }
                val bgOpacity = prefs.getFloat("widget_bg_opacity", 0.85f).coerceIn(0.0f, 1.0f)

                // Áp dụng hình nền & độ trong suốt (chuẩn RemoteViews ImageView API)
                val bgDrawableRes = if (isDark) R.drawable.widget_bg_dark else R.drawable.widget_bg_light
                views.setImageViewResource(R.id.widget_bg_layer, bgDrawableRes)
                val alphaInt = (bgOpacity * 255).toInt().coerceIn(0, 255)
                views.setInt(R.id.widget_bg_layer, "setImageAlpha", alphaInt)

                // Bảng màu chữ tương ứng với theme sáng / tối
                val colorPrimary = if (isDark) 0xFFFFFFFF.toInt() else 0xFF0F172A.toInt()
                val colorSecondary = if (isDark) 0xFFCBD5E1.toInt() else 0xFF475569.toInt()
                val colorAccent = if (isDark) 0xFF38BDF8.toInt() else 0xFF0284C7.toInt()
                val colorDim = if (isDark) 0xFF94A3B8.toInt() else 0xFF64748B.toInt()
                val colorAiTip = if (isDark) 0xFFF0ABFC.toInt() else 0xFF7E22CE.toInt()
                val chipBgRes = if (isDark) R.drawable.widget_hour_chip_dark else R.drawable.widget_hour_chip_light

                // Cập nhật màu sắc cho các view chính
                views.setTextColor(R.id.widget_city_name, colorPrimary)
                views.setTextColor(R.id.widget_temp, colorPrimary)
                views.setTextColor(R.id.widget_weather_desc, colorSecondary)
                views.setTextColor(R.id.widget_range, colorAccent)
                views.setTextColor(R.id.widget_ai_tip, colorAiTip)
                views.setTextColor(R.id.widget_updated, colorDim)

                // Click vào widget mở ứng dụng
                val intent = Intent(context, MainActivity::class.java)
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

                if (weather != null) {
                    val condition = WeatherCondition.fromWmoCode(weather.current.weatherCode)
                    val emoji = getWeatherEmoji(weather.current.weatherCode, weather.current.isDay)

                    views.setTextViewText(R.id.widget_city_name, weather.location.name)
                    views.setTextViewText(R.id.widget_temp, "${weather.current.temperature.roundToInt()}°")
                    views.setTextViewText(R.id.widget_weather_icon, emoji)
                    views.setTextViewText(
                        R.id.widget_weather_desc,
                        "${condition.description} • Cảm giác ${weather.current.feelsLike.roundToInt()}°C"
                    )
                    views.setTextViewText(
                        R.id.widget_range,
                        "T:${weather.minTemp24h.roundToInt()}° • C:${weather.maxTemp24h.roundToInt()}°"
                    )
                    views.setTextViewText(R.id.widget_updated, "Cập nhật ${weather.lastUpdated}")

                    // Cập nhật 6 mốc giờ tới
                    val hourlyList = weather.hourly24h.take(6)
                    for (i in 0 until 6) {
                        if (i < hourlyList.size) {
                            val h = hourlyList[i]
                            views.setTextViewText(hourTimeIds[i], h.hourLabel)
                            views.setTextViewText(hourIconIds[i], getWeatherEmoji(h.weatherCode, h.isDay))
                            views.setTextViewText(hourTempIds[i], "${h.temperature.roundToInt()}°")
                            views.setTextViewText(hourRainIds[i], "${h.precipitationProbability}%")
                        }
                    }

                    val tip = aiTip ?: "👔 Phong cách: Thời tiết ${condition.description.lowercase()}"
                    views.setTextViewText(R.id.widget_ai_tip, tip)
                } else {
                    views.setTextViewText(R.id.widget_city_name, "Weather 24h")
                    views.setTextViewText(R.id.widget_temp, "--°")
                    views.setTextViewText(R.id.widget_weather_icon, "☀️")
                    views.setTextViewText(R.id.widget_weather_desc, "Chạm để cập nhật thời tiết")
                    views.setTextViewText(R.id.widget_range, "Dự báo thời tiết 24h & 7 ngày")
                    views.setTextViewText(R.id.widget_ai_tip, "👔 Trợ lý AI Stylist sẵn sàng")
                }

                appWidgetManager.updateAppWidget(widgetId, views)
            } catch (e: Exception) {
            }
        }

        private fun getWeatherEmoji(code: Int, isDay: Boolean = true): String {
            val condition = WeatherCondition.fromWmoCode(code)
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
                val json = prefs.getString("cached_weather_json", null) ?: return null
                Gson().fromJson(json, WeatherData::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}
