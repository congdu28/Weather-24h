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

                // Áp dụng hình nền & độ trong suốt theo phong cách thị giác đã chọn
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

                // Áp dụng màu thích ứng thời tiết nếu chọn ADAPTIVE_COLOR
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
                val colorSecondary = if (isDark || widgetStyle != WidgetStyle.FROSTED_GLASS) 0xFFCBD5E1.toInt() else 0xFF475569.toInt()
                val colorAccent = if (isDark || widgetStyle != WidgetStyle.FROSTED_GLASS) 0xFF38BDF8.toInt() else 0xFF0284C7.toInt()
                val colorDim = if (isDark) 0xFF94A3B8.toInt() else 0xFF64748B.toInt()
                val chipBgRes = if (isDark) R.drawable.widget_hour_chip_dark else R.drawable.widget_hour_chip_light

                views.setTextColor(R.id.compact_widget_temp, colorPrimary)
                views.setTextColor(R.id.compact_widget_city, colorPrimary)
                views.setTextColor(R.id.compact_widget_desc, colorSecondary)
                views.setTextColor(R.id.compact_widget_sub, colorAccent)
                views.setTextColor(R.id.compact_2x2_footer, colorSecondary)

                // Nút bấm Làm mới trực tiếp trên Widget
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

                // Đọc kích thước thực tế từ Launcher để tối ưu Responsive cho 2x1, 2x2 và 3x1
                val options = appWidgetManager.getAppWidgetOptions(widgetId)
                val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 150
                val minHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 65
                val density = context.resources.displayMetrics.density

                val is2x2Mode = minHeight >= 108
                val isShort2x1 = !is2x2Mode && minHeight < 58
                val isNarrow = minWidth < 165

                val padH = ((if (isNarrow) 9 else 12) * density).toInt()
                val padV = ((if (is2x2Mode) 9 else if (isShort2x1) 5 else 7) * density).toInt()
                views.setViewPadding(R.id.compact_widget_content, padH, padV, padH, padV)

                // Bật/tắt phần mở rộng 2x2
                views.setViewVisibility(
                    R.id.compact_2x2_extra_container,
                    if (is2x2Mode) View.VISIBLE else View.GONE
                )
                views.setViewVisibility(
                    R.id.compact_2x2_footer,
                    if (is2x2Mode && minHeight >= 135) View.VISIBLE else View.GONE
                )

                // Ẩn nút refresh nếu chiều ngang quá hẹp (< 132dp) để tên xã/thành phố không bị cắt chữ
                views.setViewVisibility(
                    R.id.compact_widget_btn_refresh,
                    if (minWidth < 132) View.GONE else View.VISIBLE
                )

                // Căn chỉnh cỡ chữ thông minh theo chiều rộng & chiều cao
                views.setTextViewTextSize(
                    R.id.compact_widget_icon,
                    TypedValue.COMPLEX_UNIT_SP,
                    if (isNarrow) 20f else 24f
                )
                views.setTextViewTextSize(
                    R.id.compact_widget_temp,
                    TypedValue.COMPLEX_UNIT_SP,
                    if (isNarrow) 21.5f else 25.5f
                )
                views.setTextViewTextSize(
                    R.id.compact_widget_city,
                    TypedValue.COMPLEX_UNIT_SP,
                    if (isNarrow) 12.5f else 14f
                )
                views.setTextViewTextSize(
                    R.id.compact_widget_desc,
                    TypedValue.COMPLEX_UNIT_SP,
                    if (isNarrow) 9.8f else 10.5f
                )
                views.setTextViewTextSize(
                    R.id.compact_widget_sub,
                    TypedValue.COMPLEX_UNIT_SP,
                    if (isNarrow) 9f else 9.8f
                )
                views.setViewVisibility(
                    R.id.compact_widget_sub,
                    if (isShort2x1) View.GONE else View.VISIBLE
                )

                // Cấu hình 4 chip dự báo giờ trong chế độ 2x2
                val hourContainers = intArrayOf(
                    R.id.compact_h1_container,
                    R.id.compact_h2_container,
                    R.id.compact_h3_container,
                    R.id.compact_h4_container
                )
                val hourTimeIds = intArrayOf(
                    R.id.compact_h1_time,
                    R.id.compact_h2_time,
                    R.id.compact_h3_time,
                    R.id.compact_h4_time
                )
                val hourIconIds = intArrayOf(
                    R.id.compact_h1_icon,
                    R.id.compact_h2_icon,
                    R.id.compact_h3_icon,
                    R.id.compact_h4_icon
                )
                val hourTempIds = intArrayOf(
                    R.id.compact_h1_temp,
                    R.id.compact_h2_temp,
                    R.id.compact_h3_temp,
                    R.id.compact_h4_temp
                )
                val hourRainIds = intArrayOf(
                    R.id.compact_h1_rain,
                    R.id.compact_h2_rain,
                    R.id.compact_h3_rain,
                    R.id.compact_h4_rain
                )

                for (i in 0 until 4) {
                    views.setInt(hourContainers[i], "setBackgroundResource", chipBgRes)
                    views.setTextColor(hourTimeIds[i], colorDim)
                    views.setTextColor(hourTempIds[i], colorPrimary)
                    views.setTextColor(hourRainIds[i], colorAccent)
                }
                // Nếu 2x2 nhưng bề ngang hẹp (< 155dp), hiển thị 3 khung giờ cho thoáng đẹp
                views.setViewVisibility(
                    R.id.compact_h4_container,
                    if (minWidth >= 155) View.VISIBLE else View.GONE
                )

                if (weather != null) {
                    val condition = weather.current.condition
                    val emoji = getWeatherEmoji(condition, weather.current.isDay)
                    val maxT = weather.maxTemp24h.roundToInt()
                    val minT = weather.minTemp24h.roundToInt()
                    val feelsT = weather.current.feelsLike.roundToInt()
                    val lunar = com.weather24h.app.util.VietnameseLunarCalendar.getTodayLunar()

                    views.setTextViewText(R.id.compact_widget_icon, emoji)
                    views.setTextViewText(R.id.compact_widget_temp, "${weather.current.temperature.roundToInt()}°")
                    views.setTextViewText(R.id.compact_widget_city, weather.location.name)

                    if (isShort2x1) {
                        views.setTextViewText(
                            R.id.compact_widget_desc,
                            "${condition.description} • $maxT°/$minT°"
                        )
                    } else if (isNarrow) {
                        views.setTextViewText(R.id.compact_widget_desc, condition.description)
                        views.setTextViewText(
                            R.id.compact_widget_sub,
                            "C:$maxT° T:$minT° • ${lunar.formattedLunarShort}"
                        )
                    } else {
                        views.setTextViewText(
                            R.id.compact_widget_desc,
                            "${condition.description} • Cảm giác $feelsT°C"
                        )
                        views.setTextViewText(
                            R.id.compact_widget_sub,
                            "Cao: $maxT° • Thấp: $minT° • ${lunar.formattedLunarShort}"
                        )
                    }

                    // Đổ dữ liệu 4 khung giờ cho chế độ 2x2
                    val hourlyList = weather.hourly24h.take(4)
                    for (i in 0 until 4) {
                        if (i < hourlyList.size) {
                            val h = hourlyList[i]
                            views.setTextViewText(hourTimeIds[i], h.hourLabel)
                            views.setTextViewText(hourIconIds[i], getWeatherEmoji(h.condition, h.isDay))
                            views.setTextViewText(hourTempIds[i], "${h.temperature.roundToInt()}°")
                            val isDry = (h.precipitationProbability in 0..19) && ((h.precipitationMm ?: 0.0) < 0.1)
                            views.setTextViewText(hourRainIds[i], if (isDry) "—" else h.rainProbabilityText)
                        }
                    }

                    views.setTextViewText(
                        R.id.compact_2x2_footer,
                        "💧 ${weather.current.humidity}% • 💨 ${weather.current.windSpeed.roundToInt()} km/h • Cảm giác $feelsT°"
                    )
                } else {
                    views.setTextViewText(R.id.compact_widget_icon, "☀️")
                    views.setTextViewText(R.id.compact_widget_temp, "--°")
                    views.setTextViewText(R.id.compact_widget_city, "Weather 24h")
                    views.setTextViewText(R.id.compact_widget_desc, "Chạm để cập nhật")
                    views.setTextViewText(R.id.compact_widget_sub, "Dự báo thời tiết 24h")
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
