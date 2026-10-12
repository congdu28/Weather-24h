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

class CompactWeatherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val weather = getSavedWeather(context)
        for (widgetId in appWidgetIds) {
            updateCompactWidget(context, appWidgetManager, widgetId, weather)
        }
    }

    companion object {
        fun updateAllCompactWidgets(context: Context, weather: WeatherData? = null) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val componentName = ComponentName(context, CompactWeatherWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (widgetIds.isEmpty()) return

            val data = weather ?: getSavedWeather(context)
            for (widgetId in widgetIds) {
                updateCompactWidget(context, appWidgetManager, widgetId, data)
            }
        }

        fun updateCompactWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int,
            weather: WeatherData?
        ) {
            try {
                val views = RemoteViews(context.packageName, R.layout.weather_widget_compact)

                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    101,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.compact_widget_root, pendingIntent)

                if (weather != null) {
                    val condition = WeatherCondition.fromWmoCode(weather.current.weatherCode)
                    val emoji = getWeatherEmoji(weather.current.weatherCode, weather.current.isDay)

                    views.setTextViewText(R.id.compact_widget_icon, emoji)
                    views.setTextViewText(R.id.compact_widget_temp, "${weather.current.temperature.roundToInt()}°")
                    views.setTextViewText(R.id.compact_widget_city, weather.location.name)
                    views.setTextViewText(
                        R.id.compact_widget_desc,
                        "${condition.description} • Cảm giác ${weather.current.feelsLike.roundToInt()}°"
                    )
                    views.setTextViewText(
                        R.id.compact_widget_sub,
                        "C:${weather.maxTemp24h.roundToInt()}° T:${weather.minTemp24h.roundToInt()}° • Ẩm: ${weather.current.humidity}%"
                    )
                } else {
                    views.setTextViewText(R.id.compact_widget_icon, "☀️")
                    views.setTextViewText(R.id.compact_widget_temp, "--°")
                    views.setTextViewText(R.id.compact_widget_city, "Weather 24h")
                    views.setTextViewText(R.id.compact_widget_desc, "Chạm để cập nhật")
                    views.setTextViewText(R.id.compact_widget_sub, "Dự báo thời tiết 24h")
                }

                appWidgetManager.updateAppWidget(widgetId, views)
            } catch (e: Exception) {}
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
