package com.weather24h.app.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Bundle
import android.widget.RemoteViews
import com.google.gson.Gson
import com.weather24h.app.MainActivity
import com.weather24h.app.R
import com.weather24h.app.data.model.WeatherData
import com.weather24h.app.data.model.WidgetStyle
import com.weather24h.app.util.LunarAlmanac
import com.weather24h.app.util.VietnameseLunarCalendar
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Cấu hình hiển thị tùy chỉnh cho Widget Lịch Vạn Niên Tháng (4x3).
 * Người dùng có thể bật/tắt từng thành phần thông tin và chọn cỡ chữ.
 */
data class LunarMonthWidgetConfig(
    val showWeatherAndLocation: Boolean = true,
    val showHeaderCanChi: Boolean = true,
    val showHoangDaoDots: Boolean = true,
    val showHolidayDots: Boolean = true,
    val showFooterBox: Boolean = true,
    val showSolarTermAndDir: Boolean = true,
    val showGoodHours: Boolean = true,
    /** 0 = Tiêu chuẩn, 1 = Lớn (Mặc định), 2 = Rất lớn */
    val fontSizeLevel: Int = 1
)

/**
 * Widget Lịch Vạn Niên (Lịch Tháng Âm - Dương) ngoài màn hình chính (4x3, hỗ trợ co giãn linh hoạt).
 * - Cho phép chuyển nhanh Tháng trước / Tháng hiện tại / Tháng sau ngay trên Widget.
 * - Hiển thị song song ngày Dương lịch, ngày Âm lịch (1/M, Rằm 15), chấm xanh Ngày Hoàng Đạo, chấm đỏ Lễ/Tết.
 * - Thanh tóm tắt cuối Widget hiển thị trọn vẹn thông tin Âm lịch hôm nay & 6 Giờ Hoàng Đạo không bị cắt chữ.
 * - Chạm vào thân lịch mở trực tiếp màn hình Lịch Vạn Niên Toàn Màn Hình trong ứng dụng.
 */
class LunarMonthWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PREV_MONTH -> {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val currentOffset = prefs.getInt(KEY_MONTH_OFFSET, 0)
                prefs.edit().putInt(KEY_MONTH_OFFSET, (currentOffset - 1).coerceIn(-24, 24)).apply()
                updateAllLunarMonthWidgets(context)
                return
            }
            ACTION_NEXT_MONTH -> {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val currentOffset = prefs.getInt(KEY_MONTH_OFFSET, 0)
                prefs.edit().putInt(KEY_MONTH_OFFSET, (currentOffset + 1).coerceIn(-24, 24)).apply()
                updateAllLunarMonthWidgets(context)
                return
            }
            ACTION_RESET_TODAY -> {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putInt(KEY_MONTH_OFFSET, 0).apply()
                updateAllLunarMonthWidgets(context)
                return
            }
            Intent.ACTION_CONFIGURATION_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                updateAllLunarMonthWidgets(context)
                return
            }
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val weather = getSavedWeather(context)
        for (widgetId in appWidgetIds) {
            updateLunarMonthWidget(context, appWidgetManager, widgetId, weather)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val weather = getSavedWeather(context)
        updateLunarMonthWidget(context, appWidgetManager, appWidgetId, weather)
    }

    companion object {
        private const val PREFS_NAME = "weather24h_prefs"
        private const val KEY_MONTH_OFFSET = "lunar_widget_month_offset"

        private const val KEY_SHOW_WEATHER = "lunar_widget_show_weather"
        private const val KEY_SHOW_HEADER_CANCHI = "lunar_widget_show_header_canchi"
        private const val KEY_SHOW_HOANGDAO_DOTS = "lunar_widget_show_hoangdao_dots"
        private const val KEY_SHOW_HOLIDAY_DOTS = "lunar_widget_show_holiday_dots"
        private const val KEY_SHOW_FOOTER = "lunar_widget_show_footer"
        private const val KEY_SHOW_SOLARTERM_DIR = "lunar_widget_show_solarterm_dir"
        private const val KEY_SHOW_GOOD_HOURS = "lunar_widget_show_good_hours"
        private const val KEY_FONT_SIZE_LEVEL = "lunar_widget_font_size_level"

        const val ACTION_PREV_MONTH = "com.weather24h.app.ACTION_LUNAR_WIDGET_PREV_MONTH"
        const val ACTION_NEXT_MONTH = "com.weather24h.app.ACTION_LUNAR_WIDGET_NEXT_MONTH"
        const val ACTION_RESET_TODAY = "com.weather24h.app.ACTION_LUNAR_WIDGET_RESET_TODAY"
        const val EXTRA_OPEN_LUNAR_ALMANAC = "EXTRA_OPEN_LUNAR_ALMANAC"

        fun getConfig(context: Context): LunarMonthWidgetConfig {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return LunarMonthWidgetConfig(
                showWeatherAndLocation = prefs.getBoolean(KEY_SHOW_WEATHER, true),
                showHeaderCanChi = prefs.getBoolean(KEY_SHOW_HEADER_CANCHI, true),
                showHoangDaoDots = prefs.getBoolean(KEY_SHOW_HOANGDAO_DOTS, true),
                showHolidayDots = prefs.getBoolean(KEY_SHOW_HOLIDAY_DOTS, true),
                showFooterBox = prefs.getBoolean(KEY_SHOW_FOOTER, true),
                showSolarTermAndDir = prefs.getBoolean(KEY_SHOW_SOLARTERM_DIR, true),
                showGoodHours = prefs.getBoolean(KEY_SHOW_GOOD_HOURS, true),
                fontSizeLevel = prefs.getInt(KEY_FONT_SIZE_LEVEL, 1).coerceIn(0, 2)
            )
        }

        fun saveConfig(context: Context, config: LunarMonthWidgetConfig) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(KEY_SHOW_WEATHER, config.showWeatherAndLocation)
                .putBoolean(KEY_SHOW_HEADER_CANCHI, config.showHeaderCanChi)
                .putBoolean(KEY_SHOW_HOANGDAO_DOTS, config.showHoangDaoDots)
                .putBoolean(KEY_SHOW_HOLIDAY_DOTS, config.showHolidayDots)
                .putBoolean(KEY_SHOW_FOOTER, config.showFooterBox)
                .putBoolean(KEY_SHOW_SOLARTERM_DIR, config.showSolarTermAndDir)
                .putBoolean(KEY_SHOW_GOOD_HOURS, config.showGoodHours)
                .putInt(KEY_FONT_SIZE_LEVEL, config.fontSizeLevel.coerceIn(0, 2))
                .apply()
            updateAllLunarMonthWidgets(context)
        }

        fun updateAllLunarMonthWidgets(context: Context, weather: WeatherData? = null) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val componentName = ComponentName(context, LunarMonthWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (widgetIds.isEmpty()) return

            val data = weather ?: getSavedWeather(context)
            for (widgetId in widgetIds) {
                updateLunarMonthWidget(context, appWidgetManager, widgetId, data)
            }
        }

        fun updateLunarMonthWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int,
            weather: WeatherData?
        ) {
            try {
                val views = RemoteViews(context.packageName, R.layout.z_weather_widget_lunar_month)
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val monthOffset = prefs.getInt(KEY_MONTH_OFFSET, 0).coerceIn(-24, 24)
                val config = getConfig(context)

                val options = appWidgetManager.getAppWidgetOptions(widgetId)
                val minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 300).coerceIn(200, 600)
                val minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 240).coerceIn(150, 600)

                val density = context.resources.displayMetrics.density.coerceIn(1.75f, 3.5f)
                val widthPx = (minWidthDp * density).roundToInt().coerceIn(520, 1600)
                val heightPx = (minHeightDp * density).roundToInt().coerceIn(420, 1600)

                val isDark = WidgetThemeHelper.resolveIsDark(context)
                val bgOpacity = prefs.getFloat("widget_bg_opacity", 0.85f).coerceIn(0.0f, 1.0f)
                val styleStr = prefs.getString("widget_visual_style", "FROSTED_GLASS")
                val widgetStyle = try {
                    WidgetStyle.valueOf(styleStr ?: "FROSTED_GLASS")
                } catch (_: Exception) {
                    WidgetStyle.FROSTED_GLASS
                }

                val wardName = WidgetThemeHelper.resolveWardLocationName(context, weather)

                val bitmap = renderCalendarBitmap(
                    width = widthPx,
                    height = heightPx,
                    monthOffset = monthOffset,
                    isDark = isDark || widgetStyle == WidgetStyle.SOLID_DARK,
                    bgOpacity = when (widgetStyle) {
                        WidgetStyle.MINIMAL_CLEAN -> 0.15f
                        WidgetStyle.SOLID_DARK -> 0.98f
                        else -> bgOpacity.coerceAtLeast(0.45f)
                    },
                    weather = weather,
                    wardLocationName = wardName,
                    config = config
                )
                views.setImageViewBitmap(R.id.z_lunar_widget_canvas, bitmap)

                // PendingIntent cho nút < Tháng trước
                val prevIntent = Intent(context, LunarMonthWidgetProvider::class.java).apply {
                    action = ACTION_PREV_MONTH
                }
                val prevPending = PendingIntent.getBroadcast(
                    context,
                    701,
                    prevIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.z_lunar_widget_btn_prev, prevPending)

                // PendingIntent cho nút Hôm nay (Reset về tháng hiện tại)
                val todayIntent = Intent(context, LunarMonthWidgetProvider::class.java).apply {
                    action = ACTION_RESET_TODAY
                }
                val todayPending = PendingIntent.getBroadcast(
                    context,
                    702,
                    todayIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.z_lunar_widget_btn_today, todayPending)

                // PendingIntent cho nút Tháng sau >
                val nextIntent = Intent(context, LunarMonthWidgetProvider::class.java).apply {
                    action = ACTION_NEXT_MONTH
                }
                val nextPending = PendingIntent.getBroadcast(
                    context,
                    703,
                    nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.z_lunar_widget_btn_next, nextPending)

                // PendingIntent mở toàn màn hình Lịch Vạn Niên trong ứng dụng
                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(EXTRA_OPEN_LUNAR_ALMANAC, true)
                }
                val openAppPending = PendingIntent.getActivity(
                    context,
                    704,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.z_lunar_widget_body, openAppPending)

                appWidgetManager.updateAppWidget(widgetId, views)
            } catch (_: Exception) {
            }
        }

        private fun renderCalendarBitmap(
            width: Int,
            height: Int,
            monthOffset: Int,
            isDark: Boolean,
            bgOpacity: Float,
            weather: WeatherData?,
            wardLocationName: String,
            config: LunarMonthWidgetConfig
        ): Bitmap {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val scale = min(width / 310f, height / 245f)
            val fontMult = when (config.fontSizeLevel) {
                0 -> 0.92f
                2 -> 1.10f
                else -> 1.0f
            }

            val today = LocalDate.now()
            val targetMonth = YearMonth.from(today).plusMonths(monthOffset.toLong())
            val firstOfMonth = targetMonth.atDay(1)
            val midMonthInfo = VietnameseLunarCalendar.getLunarDate(targetMonth.atDay(15))
            val todayLunar = VietnameseLunarCalendar.getLunarDate(today)
            val todayAlmanac = LunarAlmanac.getDayAlmanac(today)

            // Bảng màu theo Theme Sáng / Tối
            val bgStartColor = if (isDark) 0xFF0F172A.toInt() else 0xFFF8FAFC.toInt()
            val bgEndColor = if (isDark) 0xFF1E293B.toInt() else 0xFFEEF2F6.toInt()
            val textPrimary = if (isDark) 0xFFFFFFFF.toInt() else 0xFF0F172A.toInt()
            val textSecondary = if (isDark) 0xFFCBD5E1.toInt() else 0xFF475569.toInt()
            val textMuted = if (isDark) 0xFF94A3B8.toInt() else 0xFF64748B.toInt()
            val goldAccent = if (isDark) 0xFFFFD166.toInt() else 0xFFB45309.toInt()
            val cyanAccent = if (isDark) 0xFF38BDF8.toInt() else 0xFF0284C7.toInt()
            val redHoliday = if (isDark) 0xFFFF6B6B.toInt() else 0xFFDC2626.toInt()
            val greenHoangDao = if (isDark) 0xFF34D399.toInt() else 0xFF059669.toInt()
            val dividerColor = if (isDark) 0x24FFFFFF else 0x1A0F172A

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // 1. Nền bo góc sang trọng
            val cornerRadius = 20f * scale
            val cardRect = RectF(2f, 2f, width - 2f, height - 2f)
            val alphaInt = (bgOpacity * 255).roundToInt().coerceIn(40, 255)
            paint.shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                applyAlpha(bgStartColor, alphaInt),
                applyAlpha(bgEndColor, alphaInt),
                Shader.TileMode.CLAMP
            )
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, paint)
            paint.shader = null

            // Viền mảnh tinh tế
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = max(1f, 1.2f * scale)
            paint.color = if (isDark) 0x38FFD166 else 0x240F172A
            canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, paint)
            paint.style = Paint.Style.FILL

            val padH = 10f * scale
            val hasSubHeader = config.showHeaderCanChi || config.showWeatherAndLocation
            val headerH = if (hasSubHeader) height * 0.165f else height * 0.125f
            val weekdayH = height * 0.088f
            val footerH = if (config.showFooterBox) {
                val extraLines = (if (config.showSolarTermAndDir) 1 else 0) + (if (config.showGoodHours) 1 else 0)
                when (extraLines) {
                    2 -> height * 0.225f
                    1 -> height * 0.185f
                    else -> height * 0.145f
                }
            } else {
                6f * scale
            }
            val gridTop = headerH + weekdayH
            val gridHeight = height - gridTop - footerH

            // 2. Header: Nút [<] | Tiêu đề Tháng Dương & Âm | Nút [>]
            val navBtnW = 28f * scale
            val navBtnH = 22f * scale
            val navCenterY = headerH * 0.50f

            // Nút Prev (<)
            val prevRect = RectF(padH, navCenterY - navBtnH / 2f, padH + navBtnW, navCenterY + navBtnH / 2f)
            paint.color = if (isDark) 0x26FFFFFF else 0x160F172A
            canvas.drawRoundRect(prevRect, 8f * scale, 8f * scale, paint)
            paint.color = textPrimary
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 15.5f * scale
            canvas.drawText("‹", prevRect.centerX(), prevRect.centerY() + 5.5f * scale, paint)

            // Nút Next (>)
            val nextRect = RectF(width - padH - navBtnW, navCenterY - navBtnH / 2f, width - padH, navCenterY + navBtnH / 2f)
            paint.color = if (isDark) 0x26FFFFFF else 0x160F172A
            canvas.drawRoundRect(nextRect, 8f * scale, 8f * scale, paint)
            paint.color = textPrimary
            canvas.drawText("›", nextRect.centerX(), nextRect.centerY() + 5.5f * scale, paint)

            // Tiêu đề trung tâm Header
            val centerX = width / 2f
            val headerAvailWidth = width - (padH + navBtnW) * 2f - 12f * scale
            paint.color = textPrimary
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 13.8f * scale * fontMult
            val monthTitle = if (monthOffset == 0) {
                if (config.showHeaderCanChi) {
                    "Tháng ${targetMonth.monthValue} / ${targetMonth.year} • Năm ${midMonthInfo.yearCanChi}"
                } else {
                    "Tháng ${targetMonth.monthValue} / ${targetMonth.year}"
                }
            } else {
                "Tháng ${targetMonth.monthValue}/${targetMonth.year} • [Chạm về Hôm nay]"
            }
            val titleY = if (hasSubHeader) headerH * 0.45f else headerH * 0.62f
            canvas.drawText(ellipsizeText(monthTitle, paint, headerAvailWidth), centerX, titleY, paint)

            // Dòng phụ Header: Tháng âm + Nhiệt độ & Phường/Xã hiện tại
            if (hasSubHeader) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 10.2f * scale * fontMult
                paint.color = goldAccent
                val subParts = mutableListOf<String>()
                if (config.showHeaderCanChi) {
                    subParts.add("ÂL: Tháng ${midMonthInfo.month} (${midMonthInfo.monthCanChi})")
                }
                if (config.showWeatherAndLocation && weather != null) {
                    subParts.add("${weather.current.temperature.roundToInt()}°C $wardLocationName")
                } else if (config.showWeatherAndLocation && wardLocationName.isNotEmpty() && wardLocationName != "Vị trí") {
                    subParts.add(wardLocationName)
                }
                val subHeader = subParts.joinToString("  •  ")
                canvas.drawText(ellipsizeText(subHeader, paint, headerAvailWidth), centerX, headerH * 0.82f, paint)
            }

            // Đường kẻ ngăn cách dưới Header
            paint.color = dividerColor
            canvas.drawLine(padH, headerH, width - padH, headerH, paint)

            // 3. Hàng Thứ trong tuần (T2 .. CN) - Chữ to rõ
            val colWidth = (width - padH * 2f) / 7f
            val weekdays = arrayOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11.0f * scale * fontMult
            val weekdayBaseline = headerH + weekdayH * 0.68f
            for (i in 0 until 7) {
                paint.color = when (i) {
                    5 -> cyanAccent
                    6 -> redHoliday
                    else -> textSecondary
                }
                val colCenterX = padH + colWidth * (i + 0.5f)
                canvas.drawText(weekdays[i], colCenterX, weekdayBaseline, paint)
            }

            // 4. Lưới ngày Âm - Dương (Số Dương & Âm lớn, rõ nét)
            val firstDayOffset = firstOfMonth.dayOfWeek.value - 1 // T2 = 0 .. CN = 6
            val daysInMonth = targetMonth.lengthOfMonth()
            val totalCellsUsed = firstDayOffset + daysInMonth
            val numRows = if (totalCellsUsed > 35) 6 else 5
            val rowHeight = gridHeight / numRows.toFloat()

            for (day in 1..daysInMonth) {
                val cellIndex = firstDayOffset + (day - 1)
                val row = cellIndex / 7
                val col = cellIndex % 7

                val cellLeft = padH + col * colWidth
                val cellTop = gridTop + row * rowHeight
                val cellRight = cellLeft + colWidth
                val cellBottom = cellTop + rowHeight
                val cellCenterX = (cellLeft + cellRight) / 2f

                val date = targetMonth.atDay(day)
                val isToday = date == today
                val info = VietnameseLunarCalendar.getLunarDate(date)

                // Nền nổi bật cho ngày Hôm nay
                if (isToday) {
                    val hlRect = RectF(
                        cellLeft + 2.0f * scale,
                        cellTop + 1.0f * scale,
                        cellRight - 2.0f * scale,
                        cellBottom - 1.0f * scale
                    )
                    paint.style = Paint.Style.FILL
                    paint.color = if (isDark) 0x3DFFD166 else 0x280284C7
                    canvas.drawRoundRect(hlRect, 7.5f * scale, 7.5f * scale, paint)

                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = max(1.2f, 1.5f * scale)
                    paint.color = if (isDark) goldAccent else cyanAccent
                    canvas.drawRoundRect(hlRect, 7.5f * scale, 7.5f * scale, paint)
                    paint.style = Paint.Style.FILL
                }

                // Chấm trạng thái góc trên phải ô (Đỏ = Lễ/Tết, Xanh = Hoàng Đạo)
                val dotRadius = 2.5f * scale
                val dotX = cellRight - 6.0f * scale
                val dotY = cellTop + 5.5f * scale
                if (config.showHolidayDots && info.festival != null) {
                    paint.color = redHoliday
                    canvas.drawCircle(dotX, dotY, dotRadius, paint)
                } else if (config.showHoangDaoDots && info.isAuspiciousDay) {
                    paint.color = greenHoangDao
                    canvas.drawCircle(dotX, dotY, dotRadius, paint)
                }

                // Số ngày Dương lịch (To và đậm hơn)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = (if (numRows == 6) 12.6f else 13.8f) * scale * fontMult
                paint.color = when {
                    isToday && isDark -> goldAccent
                    col == 6 || info.festival != null -> redHoliday
                    col == 5 -> cyanAccent
                    else -> textPrimary
                }
                val solarY = cellTop + rowHeight * 0.46f
                canvas.drawText(day.toString(), cellCenterX, solarY, paint)

                // Số ngày Âm lịch bên dưới (To hơn, dễ đọc hơn)
                val isSpecialLunar = info.day == 1 || info.day == 15
                val lunarText = if (info.day == 1) "1/${info.month}" else info.day.toString()
                paint.typeface = Typeface.create(
                    Typeface.DEFAULT,
                    if (isSpecialLunar || isToday) Typeface.BOLD else Typeface.NORMAL
                )
                paint.textSize = (if (numRows == 6) 8.8f else 9.5f) * scale * fontMult
                paint.color = when {
                    isSpecialLunar -> redHoliday
                    isToday -> goldAccent
                    else -> textSecondary
                }
                val lunarY = cellTop + rowHeight * 0.85f
                canvas.drawText(lunarText, cellCenterX, lunarY, paint)
            }

            // 5. Thanh Footer tóm tắt Âm lịch & Giờ Hoàng Đạo Hôm Nay (Không bị mất nội dung)
            if (config.showFooterBox) {
                val footerTop = height - footerH
                val footerRect = RectF(
                    padH * 0.7f,
                    footerTop + 1.5f * scale,
                    width - padH * 0.7f,
                    height - 4.5f * scale
                )
                paint.style = Paint.Style.FILL
                paint.color = if (isDark) 0x28FFD166 else 0x180284C7
                canvas.drawRoundRect(footerRect, 11f * scale, 11f * scale, paint)

                // Viền nhẹ cho khung Footer
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = max(1f, 0.9f * scale)
                paint.color = if (isDark) 0x40FFD166 else 0x260284C7
                canvas.drawRoundRect(footerRect, 11f * scale, 11f * scale, paint)
                paint.style = Paint.Style.FILL

                // Huy hiệu Ngày Âm Hôm Nay gọn gàng bên trái Footer
                val badgeW = 42f * scale
                val badgeRect = RectF(
                    footerRect.left + 4.5f * scale,
                    footerRect.top + 4f * scale,
                    footerRect.left + 4.5f * scale + badgeW,
                    footerRect.bottom - 4f * scale
                )
                paint.color = if (isDark) 0x36FFD166 else 0x240284C7
                canvas.drawRoundRect(badgeRect, 8f * scale, 8f * scale, paint)

                paint.textAlign = Paint.Align.CENTER
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 11.8f * scale * fontMult
                paint.color = goldAccent
                canvas.drawText(
                    "${todayLunar.day}/${todayLunar.month}",
                    badgeRect.centerX(),
                    badgeRect.centerY() - 1.5f * scale,
                    paint
                )
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 7.6f * scale * fontMult
                paint.color = textSecondary
                canvas.drawText(
                    "Âm lịch",
                    badgeRect.centerX(),
                    badgeRect.centerY() + 9.0f * scale,
                    paint
                )

                // Các dòng thông tin chi tiết Hôm nay bên phải Badge (Hiển thị trọn vẹn không cắt mất Giờ Hoàng Đạo)
                val infoLeft = badgeRect.right + 7f * scale
                val maxInfoWidth = footerRect.right - infoLeft - 5f * scale
                paint.textAlign = Paint.Align.LEFT

                val lines = mutableListOf<Triple<String, Int, Boolean>>() // (text, color, isBold)

                // Dòng 1: Ngày Can Chi + Sao Hoàng/Hắc đạo + Trực
                val line1 = "Ngày ${todayLunar.dayCanChi} • ${todayLunar.auspiciousDayType} • Trực ${todayAlmanac.truc.name}"
                lines.add(Triple(line1, textPrimary, true))

                // Dòng 2 (Tùy chọn): Lễ/Rằm + Tiết khí + Hướng tốt
                if (config.showSolarTermAndDir) {
                    val dirSummary = "Hỷ: ${todayAlmanac.hyThan.label} • Tài: ${todayAlmanac.taiThan.label}"
                    val line2 = if (todayLunar.festival != null) {
                        "🎉 ${todayLunar.festival} • Tiết ${todayLunar.solarTerm} • $dirSummary"
                    } else {
                        "Tiết ${todayLunar.solarTerm} • $dirSummary"
                    }
                    val line2Color = if (todayLunar.festival != null) redHoliday else goldAccent
                    lines.add(Triple(line2, line2Color, false))
                }

                // Dòng 3 (Tùy chọn): Đầy đủ 6 Giờ Hoàng Đạo trong ngày
                if (config.showGoodHours) {
                    val allSixGoodHours = todayLunar.auspiciousHours.joinToString(", ") { it.substringBefore(" (") }
                    val line3 = "⏰ Giờ Hoàng Đạo: $allSixGoodHours"
                    lines.add(Triple(line3, if (isDark) greenHoangDao else cyanAccent, true))
                }

                val lineCount = lines.size
                for ((idx, item) in lines.withIndex()) {
                    val (txt, col, bold) = item
                    paint.typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
                    paint.textSize = (if (idx == 0) 9.4f else 8.7f) * scale * fontMult
                    paint.color = col

                    // Tự động thu nhẹ cỡ chữ nếu dòng quá dài để không bị mất chữ cuối
                    var fittedSize = paint.textSize
                    while (fittedSize > 7.2f * scale && paint.measureText(txt) > maxInfoWidth) {
                        fittedSize -= 0.3f * scale
                        paint.textSize = fittedSize
                    }

                    val yRatio = when (lineCount) {
                        1 -> 0.58f
                        2 -> if (idx == 0) 0.40f else 0.78f
                        else -> when (idx) {
                            0 -> 0.30f
                            1 -> 0.58f
                            else -> 0.86f
                        }
                    }
                    val baselineY = footerRect.top + footerRect.height() * yRatio
                    canvas.drawText(ellipsizeText(txt, paint, maxInfoWidth), infoLeft, baselineY, paint)
                }
            }

            return bitmap
        }

        private fun ellipsizeText(text: String, paint: Paint, maxWidth: Float): String {
            if (maxWidth <= 0f || paint.measureText(text) <= maxWidth) return text
            var end = text.length
            while (end > 4 && paint.measureText(text.substring(0, end) + "…") > maxWidth) {
                end--
            }
            return text.substring(0, end) + "…"
        }

        private fun applyAlpha(color: Int, alpha: Int): Int {
            return (color and 0x00FFFFFF) or ((alpha.coerceIn(0, 255)) shl 24)
        }

        private fun getSavedWeather(context: Context): WeatherData? {
            return try {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val json = prefs.getString("cached_weather_json", null) ?: return null
                Gson().fromJson(json, WeatherData::class.java)
            } catch (_: Exception) {
                null
            }
        }
    }
}
