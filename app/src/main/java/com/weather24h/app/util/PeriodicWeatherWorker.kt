package com.weather24h.app.util

import android.content.Context
import androidx.work.*
import com.weather24h.app.BuildConfig
import com.weather24h.app.data.remote.GeminiStylistService
import com.weather24h.app.data.remote.WeatherApiService
import com.weather24h.app.data.repository.WeatherRepository
import com.weather24h.app.ui.widget.LunarMonthWidgetProvider
import com.weather24h.app.ui.widget.WeatherWidgetProvider
import java.util.concurrent.TimeUnit

/**
 * Worker chạy ngầm định kỳ bằng AndroidX WorkManager
 * - Tự động đồng bộ thời tiết mới nhất cho Tiện ích Widget & Bộ nhớ đệm ngoại tuyến
 * - Tự động cập nhật thông báo nhiệt độ lên thanh thông báo hệ thống nếu người dùng bật
 * - Luôn tự động kiểm tra bản cập nhật ứng dụng mới từ GitHub để gửi thông báo
 */
class PeriodicWeatherWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // 1. Luôn kiểm tra bản cập nhật app mới ngầm từ GitHub để bắn thông báo cho người dùng
            try {
                val updateManager = AppUpdateManager(context)
                val updateResult = updateManager.checkForUpdates(BuildConfig.VERSION_NAME)
                updateResult.onSuccess { info ->
                    if (info.isUpdateAvailable) {
                        WeatherNotificationHelper.sendAppUpdateNotification(context, info)
                    }
                }
            } catch (_: Exception) {}

            val repository = WeatherRepository(
                context = context,
                apiService = WeatherApiService(),
                stylistService = GeminiStylistService(context)
            )

            val isWidgetAutoRefresh = repository.isWidgetAutoRefreshEnabled()
            val notifInterval = repository.getPeriodicNotifIntervalHours()
            val isLunarFestivalNotif = repository.isLunarFestivalNotifEnabled()

            // Nếu người dùng tắt cả cập nhật Widget lẫn thông báo thời tiết và nhắc lễ
            if (!isWidgetAutoRefresh && notifInterval <= 0 && !isLunarFestivalNotif) {
                WeatherNotificationHelper.cancelCurrentWeatherNotification(context)
                return Result.success()
            }

            // Ưu tiên lấy vị trí thời gian thực hiện tại nếu máy có quyền vị trí
            val currentLocation = LocationHelper.getCurrentLocation(context, timeoutMillis = 6000L)
            val targetCity = if (currentLocation != null) {
                try {
                    val address = repository.getAddressFromCoordinates(
                        currentLocation.latitude,
                        currentLocation.longitude
                    )
                    com.weather24h.app.data.model.CityLocation(
                        name = address.first,
                        country = address.second,
                        latitude = currentLocation.latitude,
                        longitude = currentLocation.longitude,
                        isCurrentLocation = true
                    ).also {
                        repository.setLastActiveCity(it)
                    }
                } catch (_: Exception) {
                    com.weather24h.app.data.model.CityLocation(
                        name = "Vị trí hiện tại",
                        country = "Việt Nam",
                        latitude = currentLocation.latitude,
                        longitude = currentLocation.longitude,
                        isCurrentLocation = true
                    ).also {
                        repository.setLastActiveCity(it)
                    }
                }
            } else {
                repository.getLastActiveCity()
            }

            val weather = repository.getWeatherData(targetCity)
            val finalWeather = weather.copy(location = targetCity)
            repository.saveCachedWeather(finalWeather)

            // Luôn đồng bộ Widget màn hình chính với dữ liệu mới nhất
            WeatherWidgetProvider.updateAllWidgets(context, finalWeather)

            // Cập nhật thông báo hệ thống nếu người dùng bật thông báo định kỳ
            if (notifInterval > 0) {
                WeatherNotificationHelper.showCurrentWeatherNotification(context, finalWeather, force = false)
            } else {
                WeatherNotificationHelper.cancelCurrentWeatherNotification(context)
            }

            // Kiểm tra và gửi thông báo lễ hội / Rằm / Mùng Một nếu có
            if (isLunarFestivalNotif) {
                WeatherNotificationHelper.checkAndSendLunarFestivalNotification(context)
            }

            Result.success()
        } catch (e: Exception) {
            // Tránh Result.retry() gây spam thông báo liên tục mỗi vài phút khi gặp lỗi mạng tạm thời
            Result.success()
        }
    }

    companion object {
        private const val WORK_NAME = "periodic_weather_worker"

        fun schedule(context: Context, intervalHours: Int? = null) {
            val workManager = WorkManager.getInstance(context)
            val repository = WeatherRepository(
                context = context,
                apiService = WeatherApiService(),
                stylistService = GeminiStylistService(context)
            )

            val notifHours = intervalHours ?: repository.getPeriodicNotifIntervalHours()
            val widgetHours = repository.getWidgetAutoRefreshHours()

            // Tự động làm mới:
            // - theo chu kỳ nhỏ nhất giữa Widget tự làm mới (1h, 2h, 3h, 6h) và Thông báo định kỳ
            // - 3h/lần nếu chỉ chạy nền kiểm tra bản cập nhật app và ngày lễ
            val activeIntervals = listOf(notifHours, widgetHours).filter { it > 0 }
            val effectiveHours = activeIntervals.minOrNull()?.toLong() ?: 3L

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<PeriodicWeatherWorker>(
                effectiveHours, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }
}
