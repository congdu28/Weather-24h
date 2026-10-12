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
                views.setImageViewResource(R.id.compact_widget_bg_layer, bgDrawableRes)
                val alphaInt = (bgOpacity * 255).toInt().coerceIn(0, 255)
                views.setInt(R.id.compact_widget_bg_layer, "setImageAlpha", alphaInt)

                // Bảng màu chữ tương ứng với theme sáng / tối
                val colorPrimary = if (isDark) 0xFFFFFFFF.toInt() else 0xFF0F172A.toInt()
                val colorSecondary = if (isDark) 0xFFCBD5E1.toInt() else 0xFF475569.toInt()
                val colorAccent = if (isDark) 0xFF38BDF8.toInt() else 0xFF0284C7.toInt()

                views.setTextColor(R.id.compact_widget_temp, colorPrimary)
                views.setTextColor(R.id.compact_widget_city, colorPrimary)
                views.setTextColor(R.id.compact_widget_desc, colorSecondary)
                views.setTextColor(R.id.compact_widget_sub, colorAccent)

                // Nút bấm Làm mới trực tiếp trên Widget Tối giản
                val btnRefreshBgRes = if (isDark) R.drawable.widget_refresh_btn_bg_dark else R.drawable.widget_refresh_btn_bg_light
                views.setInt(R.id.compact_widget_btn_refresh, "setBackgroundResource", btnRefreshBgRes)
                views.setInt(R.id.compact_widget_btn_refresh, "setColorFilter", colorAccent)

                val refreshIntent = Intent(context, WeatherWidgetProvider::class.java).apply {
                    action = WeatherWidgetProvider.ACTION_REFRESH_WIDGET
                }
                val refreshPendingIntent = PendingIntent.getBroadcast(
                    context,
                    2002,
                    refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.compact_widget_btn_refresh, refreshPendingIntent)

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
                        "${condition.description} • Cảm giác ${weather.current.feelsLike.roundToInt()}°C"
                    )
                    views.setTextViewText(
                        R.id.compact_widget_sub,
                        "Cao: ${weather.maxTemp24h.roundToInt()}° • Thấp: ${weather.minTemp24h.roundToInt()}° • Ẩm: ${weather.current.humidity}%"
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
