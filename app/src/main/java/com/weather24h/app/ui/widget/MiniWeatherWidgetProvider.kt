package com.weather24h.app.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.google.gson.Gson
import com.weather24h.app.MainActivity
import com.weather24h.app.R
import com.weather24h.app.data.model.WeatherCondition
import com.weather24h.app.data.model.WeatherData
import com.weather24h.app.data.model.WeatherType
import com.weather24h.app.data.model.WidgetStyle
import kotlin.math.roundToInt

class MiniWeatherWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            updateAllMiniWidgets(context)
            return
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val weather = getSavedWeather(context)
        for (widgetId in appWidgetIds) {
            updateMiniWidget(context, appWidgetManager, widgetId, weather)
        }
        WeatherWidgetProvider.refreshWidgetWeatherWithLocation(context, isSilent = true)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val weather = getSavedWeather(context)
        updateMiniWidget(context, appWidgetManager, appWidgetId, weather)
    }

    companion object {
        fun updateAllMiniWidgets(context: Context, weather: WeatherData? = null) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val componentName = ComponentName(context, MiniWeatherWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (widgetIds.isEmpty()) return

            val data = weather ?: getSavedWeather(context)
            for (widgetId in widgetIds) {
                updateMiniWidget(context, appWidgetManager, widgetId, data)
            }
        }

        fun updateMiniWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int,
            weather: WeatherData?
        ) {
            try {
                val views = RemoteViews(context.packageName, R.layout.weather_widget_mini)

                val prefs = context.getSharedPreferences("weather24h_prefs", Context.MODE_PRIVATE)
                val isDark = WidgetThemeHelper.resolveIsDark(context)
                val bgOpacity = prefs.getFloat("widget_bg_opacity", 0.85f).coerceIn(0.0f, 1.0f)
                val styleStr = prefs.getString("widget_visual_style", "FROSTED_GLASS")
                val widgetStyle = try {
                    WidgetStyle.valueOf(styleStr ?: "FROSTED_GLASS")
                } catch (_: Exception) {
                    WidgetStyle.FROSTED_GLASS
                }

                val bgDrawableRes = if (isDark || widgetStyle == WidgetStyle.SOLID_DARK) {
                    R.drawable.widget_bg_dark
                } else {
                    R.drawable.widget_bg_light
                }
                views.setImageViewResource(R.id.mini_widget_bg_layer, bgDrawableRes)

                val alphaInt = when (widgetStyle) {
                    WidgetStyle.MINIMAL_CLEAN -> 0
                    WidgetStyle.SOLID_DARK -> 255
                    else -> (bgOpacity * 255).toInt().coerceIn(0, 255)
                }
                views.setInt(R.id.mini_widget_bg_layer, "setImageAlpha", alphaInt)

                if (widgetStyle == WidgetStyle.ADAPTIVE_COLOR && weather != null) {
                    val adaptiveColor = when (weather.current.condition.type) {
                        WeatherType.THUNDERSTORM -> 0xFF311042.toInt()
                        WeatherType.RAIN, WeatherType.DRIZZLE -> 0xFF0C4A6E.toInt()
                        WeatherType.CLEAR -> if (weather.current.isDay) 0xFF0369A1.toInt() else 0xFF020617.toInt()
                        WeatherType.PARTLY_CLOUDY, WeatherType.CLOUDY -> 0xFF1E293B.toInt()
                        WeatherType.SNOW -> 0xFF0F172A.toInt()
                        WeatherType.FOG -> 0xFF334155.toInt()
                    }
                    views.setInt(R.id.mini_widget_bg_layer, "setColorFilter", adaptiveColor)
                } else {
                    views.setInt(R.id.mini_widget_bg_layer, "setColorFilter", 0)
                }

                val colorPrimary = if (isDark || widgetStyle != WidgetStyle.FROSTED_GLASS) 0xFFFFFFFF.toInt() else 0xFF0F172A.toInt()
                val colorSecondary = if (isDark || widgetStyle != WidgetStyle.FROSTED_GLASS) 0xFFCBD5E1.toInt() else 0xFF475569.toInt()
                val colorAccent = if (isDark || widgetStyle != WidgetStyle.FROSTED_GLASS) 0xFF38BDF8.toInt() else 0xFF0284C7.toInt()

                views.setTextColor(R.id.mini_widget_temp, colorPrimary)
                views.setTextColor(R.id.mini_widget_city, colorPrimary)
                views.setTextColor(R.id.mini_widget_desc, colorAccent)
                views.setTextColor(R.id.mini_widget_sub, colorSecondary)

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(WeatherWidgetProvider.EXTRA_FORCE_GPS_REFRESH, true)
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    103,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.mini_widget_root, pendingIntent)

                // Responsive theo kích thước thực tế của ô Widget (1x1, 2x1, 2x2)
                val options = appWidgetManager.getAppWidgetOptions(widgetId)
                val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 70
                val minHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 70
                val density = context.resources.displayMetrics.density

                val is2x2 = minWidth >= 125 && minHeight >= 110
                val isWide2x1 = minWidth >= 125 && minHeight < 110
                val isUltraCompact1x1 = minHeight < 64 || minWidth < 62

                val padH = ((if (is2x2) 10 else if (isWide2x1) 8 else 5) * density).toInt()
                val padV = ((if (is2x2) 8 else if (isUltraCompact1x1) 3 else 5) * density).toInt()
                views.setViewPadding(R.id.mini_widget_content, padH, padV, padH, padV)

                when {
                    is2x2 -> {
                        views.setTextViewTextSize(R.id.mini_widget_icon, TypedValue.COMPLEX_UNIT_SP, 28f)
                        views.setTextViewTextSize(R.id.mini_widget_temp, TypedValue.COMPLEX_UNIT_SP, 26f)
                        views.setTextViewTextSize(R.id.mini_widget_city, TypedValue.COMPLEX_UNIT_SP, 13.5f)
                        views.setTextViewTextSize(R.id.mini_widget_desc, TypedValue.COMPLEX_UNIT_SP, 10.5f)
                        views.setTextViewTextSize(R.id.mini_widget_sub, TypedValue.COMPLEX_UNIT_SP, 9.5f)
                        views.setViewVisibility(R.id.mini_widget_desc, View.VISIBLE)
                        views.setViewVisibility(R.id.mini_widget_sub, View.VISIBLE)
                    }
                    isWide2x1 -> {
                        views.setTextViewTextSize(R.id.mini_widget_icon, TypedValue.COMPLEX_UNIT_SP, 20f)
                        views.setTextViewTextSize(R.id.mini_widget_temp, TypedValue.COMPLEX_UNIT_SP, 19.5f)
                        views.setTextViewTextSize(R.id.mini_widget_city, TypedValue.COMPLEX_UNIT_SP, 11.5f)
                        views.setTextViewTextSize(R.id.mini_widget_desc, TypedValue.COMPLEX_UNIT_SP, 9.5f)
                        views.setViewVisibility(R.id.mini_widget_desc, View.VISIBLE)
                        views.setViewVisibility(R.id.mini_widget_sub, View.GONE)
                    }
                    isUltraCompact1x1 -> {
                        views.setTextViewTextSize(R.id.mini_widget_icon, TypedValue.COMPLEX_UNIT_SP, 16.5f)
                        views.setTextViewTextSize(R.id.mini_widget_temp, TypedValue.COMPLEX_UNIT_SP, 16f)
                        views.setTextViewTextSize(R.id.mini_widget_city, TypedValue.COMPLEX_UNIT_SP, 9.5f)
                        views.setTextViewTextSize(R.id.mini_widget_desc, TypedValue.COMPLEX_UNIT_SP, 8f)
                        views.setViewVisibility(R.id.mini_widget_desc, if (minHeight < 54) View.GONE else View.VISIBLE)
                        views.setViewVisibility(R.id.mini_widget_sub, View.GONE)
                    }
                    else -> {
                        views.setTextViewTextSize(R.id.mini_widget_icon, TypedValue.COMPLEX_UNIT_SP, 19.5f)
                        views.setTextViewTextSize(R.id.mini_widget_temp, TypedValue.COMPLEX_UNIT_SP, 18f)
                        views.setTextViewTextSize(R.id.mini_widget_city, TypedValue.COMPLEX_UNIT_SP, 10.5f)
                        views.setTextViewTextSize(R.id.mini_widget_desc, TypedValue.COMPLEX_UNIT_SP, 8.8f)
                        views.setViewVisibility(R.id.mini_widget_desc, View.VISIBLE)
                        views.setViewVisibility(R.id.mini_widget_sub, View.GONE)
                    }
                }

                if (weather != null) {
                    val condition = weather.current.condition
                    val emoji = getWeatherEmoji(condition, weather.current.isDay)
                    val maxT = weather.maxTemp24h.roundToInt()
                    val minT = weather.minTemp24h.roundToInt()

                    views.setTextViewText(R.id.mini_widget_icon, emoji)
                    views.setTextViewText(R.id.mini_widget_temp, "${weather.current.temperature.roundToInt()}°")
                    views.setTextViewText(R.id.mini_widget_city, weather.location.name)

                    val descText = when {
                        is2x2 || isWide2x1 -> "${condition.description} • $maxT°/$minT°"
                        else -> "$maxT° / $minT°"
                    }
                    views.setTextViewText(R.id.mini_widget_desc, descText)
                    views.setTextViewText(
                        R.id.mini_widget_sub,
                        "Cảm giác ${weather.current.feelsLike.roundToInt()}° • 💧${weather.current.humidity}%"
                    )
                } else {
                    views.setTextViewText(R.id.mini_widget_icon, "☀️")
                    views.setTextViewText(R.id.mini_widget_temp, "--°")
                    views.setTextViewText(R.id.mini_widget_city, "Weather")
                    views.setTextViewText(R.id.mini_widget_desc, "24h")
                    views.setTextViewText(R.id.mini_widget_sub, "Chạm để cập nhật")
                }

                appWidgetManager.updateAppWidget(widgetId, views)
            } catch (_: Exception) {}
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
            } catch (_: Exception) {
                null
            }
        }
    }
}
