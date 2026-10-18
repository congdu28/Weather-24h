package com.weather24h.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.weather24h.app.MainActivity
import com.weather24h.app.data.model.AlertSeverity
import com.weather24h.app.data.model.OutfitRecommendation
import com.weather24h.app.data.model.SevereWeatherDetector
import com.weather24h.app.data.model.WeatherCondition
import com.weather24h.app.data.model.WeatherData
import kotlin.math.roundToInt

object WeatherNotificationHelper {
    private const val CHANNEL_MORNING_ID = "weather_morning_briefing"
    private const val CHANNEL_ALERT_ID = "weather_severe_alerts"
    const val CHANNEL_PERIODIC_ID = "weather_periodic_status"

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

            manager.createNotificationChannel(morningChannel)
            manager.createNotificationChannel(alertChannel)
            manager.createNotificationChannel(periodicChannel)
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

        val condition = WeatherCondition.fromWmoCode(weather.current.weatherCode)
        val temp = weather.current.temperature.roundToInt()
        val cityName = weather.location.name

        val outfitTip = outfit?.summary ?: "Trang phục phù hợp: ${condition.description.lowercase()} mát mẻ."

        val notification = NotificationCompat.Builder(context, CHANNEL_MORNING_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
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
        val alert = NotificationCompat.Builder(context, CHANNEL_ALERT_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
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

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
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

        val condition = WeatherCondition.fromWmoCode(weather.current.weatherCode)
        val temp = weather.current.temperature.roundToInt()
        val feelsLike = weather.current.feelsLike.roundToInt()
        val cityName = weather.location.name
        val humidity = weather.current.humidity
        val wind = weather.current.windSpeed

        val title = "$cityName • $temp°C • ${condition.description}"
        val content = "Cảm giác $feelsLike°C | Độ ẩm $humidity% | Gió $wind km/h"
        val expandedText = "${condition.description} tại $cityName\n• Nhiệt độ: $temp°C (Cảm giác như $feelsLike°C)\n• Độ ẩm: $humidity% • Tốc độ gió: $wind km/h • Chỉ số UV: ${weather.current.uvIndex}\n• Cập nhật: ${weather.lastUpdated}"

        val notification = NotificationCompat.Builder(context, CHANNEL_PERIODIC_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
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

    fun cancelCurrentWeatherNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        manager.cancel(3001)
    }
}
