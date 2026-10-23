package com.weather24h.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.weather24h.app.MainActivity
import com.weather24h.app.R
import com.weather24h.app.data.model.AlertSeverity
import com.weather24h.app.data.model.AppUpdateInfo
import com.weather24h.app.data.model.OutfitRecommendation
import com.weather24h.app.data.model.SevereWeatherDetector
import com.weather24h.app.data.model.WeatherCondition
import com.weather24h.app.data.model.WeatherData
import kotlin.math.roundToInt

object WeatherNotificationHelper {
    private const val CHANNEL_MORNING_ID = "weather_morning_briefing"
    private const val CHANNEL_ALERT_ID = "weather_severe_alerts"
    const val CHANNEL_PERIODIC_ID = "weather_periodic_status"
    private const val CHANNEL_LUNAR_ID = "weather_lunar_festivals"
    private const val CHANNEL_UPDATE_ID = "weather_app_updates"
    const val EXTRA_OPEN_UPDATE_DIALOG = "extra_open_update_dialog"

    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            // Kênh 1: Bản tin thời tiết buổi sáng
            val morningChannel = NotificationChannel(
                CHANNEL_MORNING_ID,
                "Bản tin thời tiết buổi sáng",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gửi tóm tắt thời tiết và lời khuyên trang phục mỗi buổi sáng"
                enableLights(true)
            }

            // Kênh 2: Cảnh báo thời tiết đặc biệt (mưa lớn, sấm sét, UV cao)
            val alertChannel = NotificationChannel(
                CHANNEL_ALERT_ID,
                "Cảnh báo thời tiết đặc biệt",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Cảnh báo thời tiết nguy hiểm: Dông sét, mưa lớn, tia cực tím cao"
                enableVibration(true)
                enableLights(true)
            }

            // Kênh 3: Thông báo thời tiết hiện tại định kỳ
            val periodicChannel = NotificationChannel(
                CHANNEL_PERIODIC_ID,
                "Thời tiết hiện tại định kỳ",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Cập nhật nhiệt độ và tình trạng thời tiết lên thanh thông báo"
                setShowBadge(false)
            }

            // Kênh 4: Lễ hội & Sự kiện Âm lịch
            val lunarChannel = NotificationChannel(
                CHANNEL_LUNAR_ID,
                "Lễ hội & Sự kiện Âm lịch",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Nhắc nhở ngày Rằm (15 ÂL), Mùng Một và các ngày lễ truyền thống Việt Nam"
                enableLights(true)
            }

            // Kênh 5: Cập nhật ứng dụng phiên bản mới
            val updateChannel = NotificationChannel(
                CHANNEL_UPDATE_ID,
                "Bản cập nhật ứng dụng",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo khi có bản cập nhật mới của ứng dụng Weather 24h"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            manager.createNotificationChannel(morningChannel)
            manager.createNotificationChannel(alertChannel)
            manager.createNotificationChannel(periodicChannel)
            manager.createNotificationChannel(lunarChannel)
            manager.createNotificationChannel(updateChannel)
        }
    }

    /**
     * Gửi bản tin thời tiết buổi sáng kèm gợi ý trang phục AI
     */
    fun showMorningNotification(context: Context, weather: WeatherData, outfit: OutfitRecommendation?) {
        initChannels(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val condition = weather.current.condition
        val temp = weather.current.temperature.roundToInt()
        val cityName = weather.location.name

        val outfitTip = outfit?.summary ?: "Trang phục phù hợp: ${condition.description.lowercase()} mát mẻ."
        val appLogo = getAppLogoBitmap(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_MORNING_ID)
            .setSmallIcon(R.drawable.ic_stat_weather)
            .apply {
                if (appLogo != null) setLargeIcon(appLogo)
            }
            .setContentTitle("🌤️ Thời tiết hôm nay tại $cityName ($temp°C)")
            .setContentText("${condition.description} • $outfitTip")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${condition.description}. Nhiệt độ $temp°C (Cảm giác như ${weather.current.feelsLike.roundToInt()}°C).\n\n👔 Lời khuyên mặc đồ:\n$outfitTip")
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(1001, notification)
    }

    /**
     * Tự động kiểm tra và gửi cảnh báo khẩn nếu có sấm sét, mưa lớn hoặc UV nguy hại
     * (Kèm cơ chế cooldown 4 tiếng cho mỗi loại cảnh báo để tránh spam khi cập nhật thời tiết thường xuyên)
     */
    fun checkAndSendSevereAlerts(context: Context, weather: WeatherData) {
        val prefs = context.getSharedPreferences("weather24h_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("severe_alerts_enabled", true)) return

        val detectedAlerts = SevereWeatherDetector.detectAlerts(weather)
        val severeAlert = detectedAlerts.firstOrNull { it.severity == AlertSeverity.DANGER || it.severity == AlertSeverity.WARNING }
            ?: return

        val now = System.currentTimeMillis()
        val lastAlertTime = prefs.getLong("last_severe_alert_${severeAlert.id}", 0L)
        // Cooldown tối thiểu 4 giờ cho cùng một loại cảnh báo
        if (now - lastAlertTime < 4 * 3600 * 1000L) {
            return
        }

        initChannels(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // Lưu thời điểm gửi cảnh báo
        prefs.edit().putLong("last_severe_alert_${severeAlert.id}", now).apply()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val tipsSummary = severeAlert.safetyTips.joinToString("\n• ", prefix = "• ")
        val appLogo = getAppLogoBitmap(context)
        val alert = NotificationCompat.Builder(context, CHANNEL_ALERT_ID)
            .setSmallIcon(R.drawable.ic_stat_weather)
            .apply {
                if (appLogo != null) setLargeIcon(appLogo)
            }
            .setContentTitle("${severeAlert.iconEmoji} ${severeAlert.title} - ${weather.location.name}")
            .setContentText("${severeAlert.timeInfo}: ${severeAlert.headline}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${severeAlert.headline} (${severeAlert.timeInfo})\n\n🛡️ Khuyến cáo an toàn:\n$tipsSummary")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(2001, alert)
    }

    /**
     * Gửi thông báo thử nghiệm trực tiếp từ Cài đặt
     */
    fun sendTestNotification(context: Context, isAlert: Boolean = false) {
        initChannels(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (isAlert) CHANNEL_ALERT_ID else CHANNEL_MORNING_ID
        val title = if (isAlert) "⚡ [Thử nghiệm] Cảnh báo Dông sét & Mưa lớn" else "🌤️ [Thử nghiệm] Bản tin thời tiết Weather 24h"
        val message = if (isAlert)
            "Hệ thống cảnh báo thời tiết nguy hiểm của Weather 24h đang hoạt động hoàn hảo!"
        else
            "Hà Nội 28°C • Trời nắng đẹp. Gợi ý Gemini: Áo sơ mi mỏng nhẹ + Kính râm thời trang."

        val appLogo = getAppLogoBitmap(context)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_weather)
            .apply {
                if (appLogo != null) setLargeIcon(appLogo)
            }
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(if (isAlert) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(9999, notification)
    }

    /**
     * Gửi thông báo thời tiết hiện tại lên thanh thông báo theo chu kỳ đã chọn (mặc định 2h/lần).
     * @param force Nếu là true (khi bấm thử nghiệm hoặc đổi cài đặt), bỏ qua kiểm tra chu kỳ thời gian.
     */
    fun showCurrentWeatherNotification(
        context: Context,
        weather: WeatherData,
        force: Boolean = false
    ) {
        val prefs = context.getSharedPreferences("weather24h_prefs", Context.MODE_PRIVATE)
        val notifIntervalHours = prefs.getInt("periodic_notif_hours", 2)

        // Nếu tắt thông báo định kỳ (0h) và không phải bấm gửi thủ công (force)
        if (notifIntervalHours <= 0 && !force) {
            cancelCurrentWeatherNotification(context)
            return
        }

        val now = System.currentTimeMillis()
        val lastSent = prefs.getLong("last_periodic_notif_sent_time", 0L)
        // Chu kỳ tối thiểu tính bằng ms (trừ 5 phút dung sai cho scheduler hệ điều hành)
        val minIntervalMs = if (notifIntervalHours <= 1) 55 * 60 * 1000L else (notifIntervalHours * 60 - 5) * 60 * 1000L

        if (!force && (now - lastSent < minIntervalMs)) {
            // Chưa đủ chu kỳ thời gian người dùng cài đặt, bỏ qua để tránh spam liên tục
            return
        }

        initChannels(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // Lưu lại mốc thời gian vừa gửi thành công
        prefs.edit().putLong("last_periodic_notif_sent_time", now).apply()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            3001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val condition = weather.current.condition
        val temp = weather.current.temperature.roundToInt()
        val feelsLike = weather.current.feelsLike.roundToInt()
        val cityName = weather.location.name
        val humidity = weather.current.humidity
        val wind = weather.current.windSpeed

        val title = "$cityName • $temp°C • ${condition.description}"
        val content = "Cảm giác $feelsLike°C | Độ ẩm $humidity% | Gió $wind km/h"
        val expandedText = "${condition.description} tại $cityName\n• Nhiệt độ: $temp°C (Cảm giác như $feelsLike°C)\n• Độ ẩm: $humidity% • Tốc độ gió: $wind km/h • Chỉ số UV: ${weather.current.uvIndex}\n• Cập nhật: ${weather.lastUpdated}"

        val appLogo = getAppLogoBitmap(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_PERIODIC_ID)
            .setSmallIcon(R.drawable.ic_stat_weather)
            .apply {
                if (appLogo != null) setLargeIcon(appLogo)
            }
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true) // Giữ cập nhật êm ái, không rung/chuông làm phiền
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(3001, notification)
    }

    /**
     * Tự động kiểm tra và gửi thông báo nếu hôm nay hoặc ngày mai là ngày lễ Âm lịch hoặc Rằm/Mùng Một.
     * Cơ chế deduplication lưu `last_lunar_festival_notif_date` (yyyy-MM-dd) đảm bảo chỉ gửi tối đa 1 lần/ngày.
     */
    fun checkAndSendLunarFestivalNotification(context: Context, force: Boolean = false) {
        val prefs = context.getSharedPreferences("weather24h_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("lunar_festival_notif_enabled", true) && !force) return

        val today = java.time.LocalDate.now()
        val todayStr = today.toString()
        val lastSentDate = prefs.getString("last_lunar_festival_notif_date", "")

        if (!force && lastSentDate == todayStr) {
            // Hôm nay đã gửi thông báo sự kiện âm lịch rồi, tránh làm phiền
            return
        }

        val event = VietnameseLunarCalendar.getApproachingFestivalEvent(today) ?: return

        initChannels(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1004,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, content) = event
        val appLogo = getAppLogoBitmap(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_LUNAR_ID)
            .setSmallIcon(R.drawable.ic_stat_weather)
            .apply {
                if (appLogo != null) setLargeIcon(appLogo)
            }
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$content\n\n🗓️ Tra cứu lịch Âm, giờ hoàng đạo và 24 tiết khí trong ứng dụng Weather 24h.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(1004, notification)
        prefs.edit().putString("last_lunar_festival_notif_date", todayStr).apply()
    }

    /**
     * Bắn thông báo thử nghiệm Âm lịch & Lễ hội trực tiếp từ Cài đặt
     */
    fun sendTestLunarFestivalNotification(context: Context) {
        initChannels(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            9998,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val appLogo = getAppLogoBitmap(context)
        val todayLunar = VietnameseLunarCalendar.getLunarDate(
            java.time.LocalDate.now().dayOfMonth,
            java.time.LocalDate.now().monthValue,
            java.time.LocalDate.now().year
        )

        val testTitle = "🌕 [Thử nghiệm] Nhắc nhở Âm lịch & Sự kiện"
        val testContent = "Hôm nay: ${todayLunar.formattedLunarFull} (${todayLunar.dayCanChi}) • ${todayLunar.solarTerm}"
        val testExpanded = "$testContent\n\n✨ Tính năng thông báo ngày lễ truyền thống, ngày Rằm (15 ÂL) và Mùng Một của Weather 24h đang hoạt động hoàn hảo!"

        val notification = NotificationCompat.Builder(context, CHANNEL_LUNAR_ID)
            .setSmallIcon(R.drawable.ic_stat_weather)
            .apply {
                if (appLogo != null) setLargeIcon(appLogo)
            }
            .setContentTitle(testTitle)
            .setContentText(testContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(testExpanded))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(9998, notification)
    }

    /**
     * Bắn thông báo khi phát hiện bản cập nhật ứng dụng mới từ GitHub
     * Kèm deduplication: tránh spam người dùng liên tục, chỉ nhắc lại sau 24h nếu chưa cập nhật
     */
    fun sendAppUpdateNotification(context: Context, info: AppUpdateInfo, force: Boolean = false) {
        val prefs = context.getSharedPreferences("weather24h_prefs", Context.MODE_PRIVATE)
        val lastNotifiedVersion = prefs.getString("last_notified_update_version", "") ?: ""
        val lastNotifiedTime = prefs.getLong("last_notified_update_time", 0L)
        val now = System.currentTimeMillis()

        val isNewVersion = info.latestVersionName != lastNotifiedVersion
        val is24HoursPassed = (now - lastNotifiedTime) > 24 * 60 * 60 * 1000L

        if (!force && !isNewVersion && !is24HoursPassed) {
            return
        }

        initChannels(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_UPDATE_DIALOG, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val appLogo = getAppLogoBitmap(context)
        val title = "🚀 Đã có bản cập nhật mới v${info.latestVersionName}!"
        val content = "Tải ngay để trải nghiệm các tính năng mới và cải tiến hiệu năng."
        val bigText = buildString {
            append("Phiên bản: v${info.latestVersionName}")
            if (info.formattedApkSize.isNotBlank()) append(" (${info.formattedApkSize})")
            append("\n\n✨ Tóm tắt nâng cấp:\n")
            append(info.releaseNotes.ifBlank { "• Tối ưu hóa hiệu năng và trải nghiệm giao diện người dùng." })
            append("\n\n👉 Chạm để mở ứng dụng và cập nhật ngay!")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_UPDATE_ID)
            .setSmallIcon(R.drawable.ic_stat_weather)
            .apply {
                if (appLogo != null) setLargeIcon(appLogo)
            }
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_stat_weather,
                "Cập nhật ngay",
                pendingIntent
            )
            .build()

        manager.notify(2002, notification)
        prefs.edit()
            .putString("last_notified_update_version", info.latestVersionName)
            .putLong("last_notified_update_time", now)
            .apply()
    }

    private fun getAppLogoBitmap(context: Context) = try {
        BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
    } catch (_: Exception) {
        null
    }

    fun cancelCurrentWeatherNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        manager.cancel(3001)
    }
}
