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
            val componentName = ComponentName(context, WeatherWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (widgetIds.isEmpty()) return

            val data = weather ?: getSavedWeather(context)
            for (widgetId in widgetIds) {
                updateWidget(context, appWidgetManager, widgetId, data, aiTip)
            }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int,
            weather: WeatherData?,
            aiTip: String? = null
        ) {
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
                views.setTextViewText(R.id.widget_city_name, weather.location.name)
                views.setTextViewText(R.id.widget_temp, "${weather.current.temperature.roundToInt()}°")
                views.setTextViewText(
                    R.id.widget_weather_desc,
                    "${condition.description} • Cảm giác ${weather.current.feelsLike.roundToInt()}°C"
                )
                views.setTextViewText(
                    R.id.widget_range,
                    "Thấp: ${weather.minTemp24h.roundToInt()}° • Cao: ${weather.maxTemp24h.roundToInt()}°"
                )
                views.setTextViewText(R.id.widget_updated, "Cập nhật ${weather.lastUpdated}")

                val tip = aiTip ?: "👔 Phong cách: Thời tiết ${condition.description.lowercase()}"
                views.setTextViewText(R.id.widget_ai_tip, tip)
            } else {
                views.setTextViewText(R.id.widget_city_name, "Weather 24h")
                views.setTextViewText(R.id.widget_temp, "--°")
                views.setTextViewText(R.id.widget_weather_desc, "Chạm để cập nhật thời tiết")
                views.setTextViewText(R.id.widget_range, "Dự báo thời tiết 24h & 7 ngày")
                views.setTextViewText(R.id.widget_ai_tip, "👔 Trợ lý AI Stylist sẵn sàng")
            }

            appWidgetManager.updateAppWidget(widgetId, views)
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
