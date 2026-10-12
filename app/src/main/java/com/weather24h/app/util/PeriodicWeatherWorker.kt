package com.weather24h.app.util

import android.content.Context
import androidx.work.*
import com.weather24h.app.data.remote.GeminiStylistService
import com.weather24h.app.data.remote.WeatherApiService
import com.weather24h.app.data.repository.WeatherRepository
import java.util.concurrent.TimeUnit

/**
 * Worker chạy ngầm định kỳ (1h, 2h, 3h, 6h) bằng AndroidX WorkManager
 * Tự động lấy thời tiết và cập nhật thông báo nhiệt độ lên thanh thông báo hệ thống
 */
class PeriodicWeatherWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val repository = WeatherRepository(
                context = context,
                apiService = WeatherApiService(),
                stylistService = GeminiStylistService(context)
            )

            val interval = repository.getPeriodicNotifIntervalHours()
            if (interval <= 0) {
                WeatherNotificationHelper.cancelCurrentWeatherNotification(context)
                return Result.success()
            }

            val city = repository.getDefaultCity()
            val weather = repository.getWeatherData(city)
            WeatherNotificationHelper.showCurrentWeatherNotification(context, weather)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "periodic_weather_notification"

        fun schedule(context: Context, intervalHours: Int) {
            val workManager = WorkManager.getInstance(context)
            if (intervalHours <= 0) {
                workManager.cancelUniqueWork(WORK_NAME)
                WeatherNotificationHelper.cancelCurrentWeatherNotification(context)
                return
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<PeriodicWeatherWorker>(
                intervalHours.toLong(), TimeUnit.HOURS
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
