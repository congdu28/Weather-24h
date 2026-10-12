package com.weather24h.app.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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

            // 1. Cập nhật Widget Chi tiết theo giờ (4x2)
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

                // Click vào widget mở ứng dụng
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

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

                    // Cập nhật 4 mốc giờ tới
                    val hourlyList = weather.hourly24h.take(4)
                    if (hourlyList.size >= 1) {
                        val h = hourlyList[0]
                        views.setTextViewText(R.id.widget_h1_time, h.hourLabel)
                        views.setTextViewText(R.id.widget_h1_icon, getWeatherEmoji(h.weatherCode, h.isDay))
                        views.setTextViewText(R.id.widget_h1_temp, "${h.temperature.roundToInt()}°")
                        views.setTextViewText(R.id.widget_h1_rain, "${h.precipitationProbability}%")
                    }
                    if (hourlyList.size >= 2) {
                        val h = hourlyList[1]
                        views.setTextViewText(R.id.widget_h2_time, h.hourLabel)
                        views.setTextViewText(R.id.widget_h2_icon, getWeatherEmoji(h.weatherCode, h.isDay))
                        views.setTextViewText(R.id.widget_h2_temp, "${h.temperature.roundToInt()}°")
                        views.setTextViewText(R.id.widget_h2_rain, "${h.precipitationProbability}%")
                    }
                    if (hourlyList.size >= 3) {
                        val h = hourlyList[2]
                        views.setTextViewText(R.id.widget_h3_time, h.hourLabel)
                        views.setTextViewText(R.id.widget_h3_icon, getWeatherEmoji(h.weatherCode, h.isDay))
                        views.setTextViewText(R.id.widget_h3_temp, "${h.temperature.roundToInt()}°")
                        views.setTextViewText(R.id.widget_h3_rain, "${h.precipitationProbability}%")
                    }
                    if (hourlyList.size >= 4) {
                        val h = hourlyList[3]
                        views.setTextViewText(R.id.widget_h4_time, h.hourLabel)
                        views.setTextViewText(R.id.widget_h4_icon, getWeatherEmoji(h.weatherCode, h.isDay))
                        views.setTextViewText(R.id.widget_h4_temp, "${h.temperature.roundToInt()}°")
                        views.setTextViewText(R.id.widget_h4_rain, "${h.precipitationProbability}%")
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
            } catch (_: Exception) {
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
            } catch (_: Exception) {
                null
            }
        }
    }
}
