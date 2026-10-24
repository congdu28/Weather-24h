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

class CompactWeatherWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            updateAllCompactWidgets(context)
            return
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val weather = getSavedWeather(context)
        for (widgetId in appWidgetIds) {
            updateCompactWidget(context, appWidgetManager, widgetId, weather)
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
        updateCompactWidget(context, appWidgetManager, appWidgetId, weather)
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
                views.setImageViewResource(R.id.compact_widget_bg_layer, bgDrawableRes)

                val alphaInt = when (widgetStyle) {
                    WidgetStyle.MINIMAL_CLEAN -> 0
                    WidgetStyle.SOLID_DARK -> 255
                    else -> (bgOpacity * 255).toInt().coerceIn(0, 255)
                }
                views.setInt(R.id.compact_widget_bg_layer, "setImageAlpha", alphaInt)

                if (widgetStyle == WidgetStyle.ADAPTIVE_COLOR && weather != null) {
                    val adaptiveColor = when (weather.current.condition.type) {
                        WeatherType.THUNDERSTORM -> 0xFF311042.toInt()
                        WeatherType.RAIN, WeatherType.DRIZZLE -> 0xFF0C4A6E.toInt()
                        WeatherType.CLEAR -> if (weather.current.isDay) 0xFF0369A1.toInt() else 0xFF020617.toInt()
                        WeatherType.PARTLY_CLOUDY, WeatherType.CLOUDY -> 0xFF1E293B.toInt()
                        WeatherType.SNOW -> 0xFF0F172A.toInt()
                        WeatherType.FOG -> 0xFF334155.toInt()
                    }
                    views.setInt(R.id.compact_widget_bg_layer, "setColorFilter", adaptiveColor)
                } else {
                    views.setInt(R.id.compact_widget_bg_layer, "setColorFilter", 0)
                }

                // Bảng màu chữ tương ứng với theme sáng / tối
                val colorPrimary = if (isDark || widgetStyle != WidgetStyle.FROSTED_GLASS) 0xFFFFFFFF.toInt() else 0xFF0F172A.toInt()
                val colorAccent = if (isDark || widgetStyle != WidgetStyle.FROSTED_GLASS) 0xFF38BDF8.toInt() else 0xFF0284C7.toInt()

                views.setTextColor(R.id.compact_widget_temp, colorPrimary)
                views.setTextColor(R.id.compact_widget_city, colorPrimary)
                views.setTextColor(R.id.compact_widget_desc, colorAccent)

                views.setTextColor(R.id.compact_2x2_city, colorPrimary)
                views.setTextColor(R.id.compact_2x2_temp, colorPrimary)
                views.setTextColor(R.id.compact_2x2_range, colorAccent)

                // Nút bấm Làm mới trực tiếp trên cả 2 chế độ 2x1 và 2x2
                val btnRefreshBgRes = if (isDark) R.drawable.widget_refresh_btn_bg_dark else R.drawable.widget_refresh_btn_bg_light
                views.setInt(R.id.compact_widget_btn_refresh, "setBackgroundResource", btnRefreshBgRes)
                views.setInt(R.id.compact_widget_btn_refresh, "setColorFilter", colorAccent)
                views.setInt(R.id.compact_2x2_btn_refresh, "setBackgroundResource", btnRefreshBgRes)
                views.setInt(R.id.compact_2x2_btn_refresh, "setColorFilter", colorAccent)

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
                views.setOnClickPendingIntent(R.id.compact_2x2_btn_refresh, refreshPendingIntent)

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(WeatherWidgetProvider.EXTRA_FORCE_GPS_REFRESH, true)
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    101,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.compact_widget_root, pendingIntent)

                // Đọc kích thước thực tế từ Launcher để chọn bố cục 2x1 hoặc 2x2 chuẩn xác
                val options = appWidgetManager.getAppWidgetOptions(widgetId)
                val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 150
                val minHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 65
                val density = context.resources.displayMetrics.density

                val is2x2Mode = minHeight >= 105
                val isNarrow = minWidth < 165

                val padH = ((if (is2x2Mode) 14 else if (isNarrow) 10 else 13) * density).toInt()
                val padV = ((if (is2x2Mode) 12 else 7) * density).toInt()
                views.setViewPadding(R.id.compact_widget_content, padH, padV, padH, padV)

                views.setViewVisibility(R.id.compact_layout_2x1, if (is2x2Mode) View.GONE else View.VISIBLE)
                views.setViewVisibility(R.id.compact_layout_2x2, if (is2x2Mode) View.VISIBLE else View.GONE)

                // Trong chế độ 2x1: điều chỉnh cỡ chữ & nút refresh theo bề ngang
                views.setViewVisibility(
                    R.id.compact_widget_btn_refresh,
                    if (minWidth < 135) View.GONE else View.VISIBLE
                )
                views.setTextViewTextSize(
                    R.id.compact_widget_icon,
                    TypedValue.COMPLEX_UNIT_SP,
                    if (isNarrow) 21f else 25f
                )
                views.setTextViewTextSize(
                    R.id.compact_widget_temp,
                    TypedValue.COMPLEX_UNIT_SP,
                    if (isNarrow) 23f else 28f
                )
                views.setTextViewTextSize(
                    R.id.compact_widget_city,
                    TypedValue.COMPLEX_UNIT_SP,
                    if (isNarrow) 13f else 15f
                )
                views.setTextViewTextSize(
                    R.id.compact_widget_desc,
                    TypedValue.COMPLEX_UNIT_SP,
                    if (isNarrow) 10.5f else 11.5f
                )

                // Trong chế độ 2x2: điều chỉnh cỡ chữ thoáng đẹp
                views.setTextViewTextSize(
                    R.id.compact_2x2_city,
                    TypedValue.COMPLEX_UNIT_SP,
                    if (isNarrow) 14.5f else 16f
                )
                views.setTextViewTextSize(
                    R.id.compact_2x2_icon,
                    TypedValue.COMPLEX_UNIT_SP,
                    if (isNarrow) 28f else 34f
                )
                views.setTextViewTextSize(
                    R.id.compact_2x2_temp,
                    TypedValue.COMPLEX_UNIT_SP,
                    if (isNarrow) 34f else 42f
                )
                views.setTextViewTextSize(
                    R.id.compact_2x2_range,
                    TypedValue.COMPLEX_UNIT_SP,
                    if (isNarrow) 11.5f else 13f
                )

                if (weather != null) {
                    val condition = weather.current.condition
                    val emoji = getWeatherEmoji(condition, weather.current.isDay)
                    val curT = "${weather.current.temperature.roundToInt()}°"
                    val maxT = weather.maxTemp24h.roundToInt()
                    val minT = weather.minTemp24h.roundToInt()
                    val shortCity = WidgetThemeHelper.formatShortLocationName(weather.location.name)

                    // Đổ dữ liệu bố cục 2x1
                    views.setTextViewText(R.id.compact_widget_icon, emoji)
                    views.setTextViewText(R.id.compact_widget_temp, curT)
                    views.setTextViewText(R.id.compact_widget_city, shortCity)
                    views.setTextViewText(
                        R.id.compact_widget_desc,
                        if (isNarrow) "$maxT° / $minT°" else "Cao $maxT° • Thấp $minT°"
                    )

                    // Đổ dữ liệu bố cục 2x2
                    views.setTextViewText(R.id.compact_2x2_city, shortCity)
                    views.setTextViewText(R.id.compact_2x2_icon, emoji)
                    views.setTextViewText(R.id.compact_2x2_temp, curT)
                    views.setTextViewText(
                        R.id.compact_2x2_range,
                        "Cao $maxT°  •  Thấp $minT°"
                    )
                } else {
                    views.setTextViewText(R.id.compact_widget_icon, "☀️")
                    views.setTextViewText(R.id.compact_widget_temp, "--°")
                    views.setTextViewText(R.id.compact_widget_city, "Weather 24h")
                    views.setTextViewText(R.id.compact_widget_desc, "--° / --°")

                    views.setTextViewText(R.id.compact_2x2_city, "Weather 24h")
                    views.setTextViewText(R.id.compact_2x2_icon, "☀️")
                    views.setTextViewText(R.id.compact_2x2_temp, "--°")
                    views.setTextViewText(R.id.compact_2x2_range, "Chạm để cập nhật")
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
