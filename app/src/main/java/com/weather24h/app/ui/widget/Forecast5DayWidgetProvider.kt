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
import com.weather24h.app.data.model.WidgetStyle
import kotlin.math.roundToInt

class Forecast5DayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val weather = getSavedWeather(context)
        for (widgetId in appWidgetIds) {
            updateForecastWidget(context, appWidgetManager, widgetId, weather)
        }
        WeatherWidgetProvider.refreshWidgetWeatherWithLocation(context, isSilent = true)
    }

    companion object {
        fun updateAllForecastWidgets(context: Context, weather: WeatherData? = null) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val componentName = ComponentName(context, Forecast5DayWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (widgetIds.isEmpty()) return

            val data = weather ?: getSavedWeather(context)
            for (widgetId in widgetIds) {
                updateForecastWidget(context, appWidgetManager, widgetId, data)
            }
        }

        fun updateForecastWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int,
            weather: WeatherData?
        ) {
            try {
                val views = RemoteViews(context.packageName, R.layout.weather_widget_forecast)

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
                val styleStr = prefs.getString("widget_visual_style", "FROSTED_GLASS")
                val widgetStyle = try {
                    WidgetStyle.valueOf(styleStr ?: "FROSTED_GLASS")
                } catch (_: Exception) {
                    WidgetStyle.FROSTED_GLASS
                }

                // 1. Áp dụng hình nền & độ trong suốt
                val bgDrawableRes = if (isDark || widgetStyle == WidgetStyle.SOLID_DARK) {
                    R.drawable.widget_bg_dark
                } else {
                    R.drawable.widget_bg_light
                }
                views.setImageViewResource(R.id.forecast_widget_bg_layer, bgDrawableRes)

                val alphaInt = when (widgetStyle) {
                    WidgetStyle.MINIMAL_CLEAN -> 0
                    WidgetStyle.SOLID_DARK -> 255
                    else -> (bgOpacity * 255).toInt().coerceIn(0, 255)
                }
                views.setInt(R.id.forecast_widget_bg_layer, "setImageAlpha", alphaInt)

                // 2. Màu thích ứng thời tiết nếu chọn ADAPTIVE_COLOR
                if (widgetStyle == WidgetStyle.ADAPTIVE_COLOR && weather != null) {
                    val adaptiveColor = when (weather.current.condition.type) {
                        WeatherType.THUNDERSTORM -> 0xFF311042.toInt()
                        WeatherType.RAIN, WeatherType.DRIZZLE -> 0xFF0C4A6E.toInt()
                        WeatherType.CLEAR -> if (weather.current.isDay) 0xFF0369A1.toInt() else 0xFF020617.toInt()
                        WeatherType.PARTLY_CLOUDY, WeatherType.CLOUDY -> 0xFF1E293B.toInt()
                        WeatherType.SNOW -> 0xFF0F172A.toInt()
                        WeatherType.FOG -> 0xFF334155.toInt()
                    }
                    views.setInt(R.id.forecast_widget_bg_layer, "setColorFilter", adaptiveColor)
                } else {
                    views.setInt(R.id.forecast_widget_bg_layer, "setColorFilter", 0)
                }

                // 3. Bảng màu chữ tương ứng với theme
                val colorPrimary = if (isDark || widgetStyle != WidgetStyle.FROSTED_GLASS) 0xFFFFFFFF.toInt() else 0xFF0F172A.toInt()
                val colorSecondary = if (isDark || widgetStyle != WidgetStyle.FROSTED_GLASS) 0xFFCBD5E1.toInt() else 0xFF475569.toInt()
                val colorAccent = if (isDark || widgetStyle != WidgetStyle.FROSTED_GLASS) 0xFF38BDF8.toInt() else 0xFF0284C7.toInt()
                val colorDim = if (isDark) 0xFF94A3B8.toInt() else 0xFF64748B.toInt()
                val chipBgRes = if (isDark) R.drawable.widget_hour_chip_dark else R.drawable.widget_hour_chip_light

                views.setTextColor(R.id.forecast_widget_city, colorPrimary)
                views.setTextColor(R.id.forecast_widget_temp, colorPrimary)
                views.setTextColor(R.id.forecast_widget_desc, colorAccent)

                // Nút bấm Làm mới trực tiếp
                val btnRefreshBgRes = if (isDark) R.drawable.widget_refresh_btn_bg_dark else R.drawable.widget_refresh_btn_bg_light
                views.setInt(R.id.forecast_widget_btn_refresh, "setBackgroundResource", btnRefreshBgRes)
                views.setInt(R.id.forecast_widget_btn_refresh, "setColorFilter", colorAccent)

                val refreshIntent = Intent(context, WeatherWidgetProvider::class.java).apply {
                    action = WeatherWidgetProvider.ACTION_REFRESH_WIDGET
                }
                val refreshPendingIntent = PendingIntent.getBroadcast(
                    context,
                    2003,
                    refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.forecast_widget_btn_refresh, refreshPendingIntent)

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(WeatherWidgetProvider.EXTRA_FORCE_GPS_REFRESH, true)
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    102,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.forecast_widget_root, pendingIntent)

                if (weather != null) {
                    val condition = weather.current.condition
                    val emoji = getWeatherEmoji(condition, weather.current.isDay)

                    views.setTextViewText(R.id.forecast_widget_city, weather.location.name)
                    views.setTextViewText(R.id.forecast_widget_icon, emoji)
                    views.setTextViewText(R.id.forecast_widget_temp, "${weather.current.temperature.roundToInt()}°")
                    views.setTextViewText(
                        R.id.forecast_widget_desc,
                        "C: ${weather.maxTemp24h.roundToInt()}° • T: ${weather.minTemp24h.roundToInt()}°"
                    )

                    // Cập nhật 5 ngày dự báo
                    val days = weather.daily7Days.take(5)
                    val chipLayouts = listOf(
                        Triple(R.id.forecast_chip_0, Triple(R.id.day0_label, R.id.day0_icon, R.id.day0_temp_max), Pair(R.id.day0_temp_min, R.id.day0_rain)),
                        Triple(R.id.forecast_chip_1, Triple(R.id.day1_label, R.id.day1_icon, R.id.day1_temp_max), Pair(R.id.day1_temp_min, R.id.day1_rain)),
                        Triple(R.id.forecast_chip_2, Triple(R.id.day2_label, R.id.day2_icon, R.id.day2_temp_max), Pair(R.id.day2_temp_min, R.id.day2_rain)),
                        Triple(R.id.forecast_chip_3, Triple(R.id.day3_label, R.id.day3_icon, R.id.day3_temp_max), Pair(R.id.day3_temp_min, R.id.day3_rain)),
                        Triple(R.id.forecast_chip_4, Triple(R.id.day4_label, R.id.day4_icon, R.id.day4_temp_max), Pair(R.id.day4_temp_min, R.id.day4_rain))
                    )

                    for (i in 0 until 5) {
                        val (chipId, textIds, subIds) = chipLayouts[i]
                        val (labelId, iconId, maxTempId) = textIds
                        val (minTempId, rainId) = subIds

                        views.setInt(chipId, "setBackgroundResource", chipBgRes)
                        views.setTextColor(labelId, colorPrimary)
                        views.setTextColor(maxTempId, colorPrimary)
                        views.setTextColor(minTempId, colorDim)
                        views.setTextColor(rainId, colorAccent)

                        if (i < days.size) {
                            val day = days[i]
                            val dayCond = WeatherCondition.fromWmoCode(day.weatherCode)
                            val dayEmoji = getWeatherEmoji(dayCond, isDay = true)
                            val dayName = formatShortDay(day.dayOfWeek, i)

                            views.setTextViewText(labelId, dayName)
                            views.setTextViewText(iconId, dayEmoji)
                            views.setTextViewText(maxTempId, "${day.maxTemp.roundToInt()}°")
                            views.setTextViewText(minTempId, "${day.minTemp.roundToInt()}°")
                            views.setTextViewText(rainId, if (day.precipitationProbability > 0) "${day.precipitationProbability}%" else "—")
                        }
                    }
                } else {
                    views.setTextViewText(R.id.forecast_widget_city, "Weather 24h")
                    views.setTextViewText(R.id.forecast_widget_icon, "☀️")
                    views.setTextViewText(R.id.forecast_widget_temp, "--°")
                    views.setTextViewText(R.id.forecast_widget_desc, "Chạm để cập nhật")
                }

                appWidgetManager.updateAppWidget(widgetId, views)
            } catch (e: Exception) {}
        }

        private fun formatShortDay(dayOfWeek: String, index: Int): String {
            if (index == 0) return "H.nay"
            val cleaned = dayOfWeek.trim()
            return when {
                cleaned.contains("Hai", ignoreCase = true) || cleaned.contains("2") -> "T2"
                cleaned.contains("Ba", ignoreCase = true) || cleaned.contains("3") -> "T3"
                cleaned.contains("Tư", ignoreCase = true) || cleaned.contains("4") -> "T4"
                cleaned.contains("Năm", ignoreCase = true) || cleaned.contains("5") -> "T5"
                cleaned.contains("Sáu", ignoreCase = true) || cleaned.contains("6") -> "T6"
                cleaned.contains("Bảy", ignoreCase = true) || cleaned.contains("7") -> "T7"
                cleaned.contains("Nhật", ignoreCase = true) || cleaned.contains("CN", ignoreCase = true) -> "CN"
                else -> cleaned.take(4)
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
