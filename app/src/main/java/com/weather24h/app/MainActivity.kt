package com.weather24h.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import android.content.Intent
import com.weather24h.app.ui.screens.WeatherScreen
import com.weather24h.app.ui.screens.WeatherViewModel
import com.weather24h.app.ui.theme.Weather24hTheme
import com.weather24h.app.ui.widget.WeatherWidgetProvider
import com.weather24h.app.util.WeatherNotificationHelper

class MainActivity : ComponentActivity() {
    private val viewModel: WeatherViewModel by viewModels()
    private var pendingGpsIsPullRefresh = false
    private var pendingGpsIsInitial = false
    private var locationCancellationTokenSource: CancellationTokenSource? = null
    private var gpsRequestGeneration = 0

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val isPullRefresh = pendingGpsIsPullRefresh
        val isInitial = pendingGpsIsInitial

        if (fineLocationGranted || coarseLocationGranted) {
            fetchGpsLocation(isPullRefresh = isPullRefresh, isInitial = isInitial)
        } else {
            Toast.makeText(this, "Không có quyền vị trí. Sử dụng thành phố mặc định.", Toast.LENGTH_SHORT).show()
            if (isInitial) {
                viewModel.loadInitialFallbackWeather()
                requestNotificationPermissionIfNeeded()
            } else {
                viewModel.cancelRefreshing()
            }
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            WeatherNotificationHelper.initChannels(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make system bars transparent
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Khởi tạo notification channels
        WeatherNotificationHelper.initChannels(this)
        WeatherWidgetProvider.updateAllWidgets(this)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val cardOpacity by viewModel.cardOpacity.collectAsState()
            Weather24hTheme(
                themeMode = themeMode,
                cardOpacity = cardOpacity
            ) {
                WeatherScreen(
                    viewModel = viewModel,
                    onRequestGps = { isPullRefresh -> checkLocationPermissions(isPullRefresh) }
                )
            }
        }

        // Kiểm tra nếu được mở từ Widget với yêu cầu cập nhật vị trí thời gian thực
        val forceGps = intent?.getBooleanExtra(WeatherWidgetProvider.EXTRA_FORCE_GPS_REFRESH, false) == true
        if (forceGps) {
            checkLocationPermissions(isPullRefresh = true, isInitial = false)
        } else if (viewModel.isStartWithGps.value) {
            checkLocationPermissions(isInitial = true)
        } else {
            requestNotificationPermissionIfNeeded()
        }

        // Kiểm tra nếu được mở từ thông báo cập nhật ứng dụng mới
        if (intent?.getBooleanExtra(WeatherNotificationHelper.EXTRA_OPEN_UPDATE_DIALOG, false) == true) {
            viewModel.openUpdateDialogFromNotification()
        }
        if (intent?.getBooleanExtra(com.weather24h.app.ui.widget.LunarMonthWidgetProvider.EXTRA_OPEN_LUNAR_ALMANAC, false) == true) {
            viewModel.triggerOpenLunarAlmanac()
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val forceGps = intent.getBooleanExtra(WeatherWidgetProvider.EXTRA_FORCE_GPS_REFRESH, false)
        if (forceGps) {
            checkLocationPermissions(isPullRefresh = true, isInitial = false)
        }
        val openUpdate = intent.getBooleanExtra(WeatherNotificationHelper.EXTRA_OPEN_UPDATE_DIALOG, false)
        if (openUpdate) {
            viewModel.openUpdateDialogFromNotification()
        }
        if (intent.getBooleanExtra(com.weather24h.app.ui.widget.LunarMonthWidgetProvider.EXTRA_OPEN_LUNAR_ALMANAC, false)) {
            viewModel.triggerOpenLunarAlmanac()
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        // Đồng bộ cập nhật tất cả Widget nếu Widget đang theo theme thiết bị
        WeatherWidgetProvider.updateAllWidgets(this)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun checkLocationPermissions(isPullRefresh: Boolean = false, isInitial: Boolean = false) {
        val hasFine = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            fetchGpsLocation(isPullRefresh = isPullRefresh, isInitial = isInitial)
        } else {
            pendingGpsIsPullRefresh = isPullRefresh
            pendingGpsIsInitial = isInitial
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchGpsLocation(isPullRefresh: Boolean = false, isInitial: Boolean = false) {
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(this)
            val requestGeneration = ++gpsRequestGeneration
            val hasFineLocation = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            locationCancellationTokenSource?.cancel()
            val cancellationSource = CancellationTokenSource()
            locationCancellationTokenSource = cancellationSource

            val request = CurrentLocationRequest.Builder()
                .setPriority(
                    if (hasFineLocation) Priority.PRIORITY_HIGH_ACCURACY
                    else Priority.PRIORITY_BALANCED_POWER_ACCURACY
                )
                .setMaxUpdateAgeMillis(30_000L)
                .setDurationMillis(15_000L)
                .build()

            fusedClient.getCurrentLocation(request, cancellationSource.token)
                .addOnSuccessListener { freshLocation ->
                    if (requestGeneration != gpsRequestGeneration) return@addOnSuccessListener
                    if (freshLocation != null) {
                        applyGpsLocation(freshLocation, isPullRefresh, isInitial)
                    } else {
                        useRecentLastLocation(isPullRefresh, isInitial, requestGeneration)
                    }
                }
                .addOnFailureListener {
                    if (requestGeneration == gpsRequestGeneration) {
                        useRecentLastLocation(isPullRefresh, isInitial, requestGeneration)
                    }
                }
        } catch (_: SecurityException) {
            handleGpsFailure(isInitial)
        }
    }

    @SuppressLint("MissingPermission")
    private fun useRecentLastLocation(
        isPullRefresh: Boolean,
        isInitial: Boolean,
        requestGeneration: Int
    ) {
        val fusedClient = LocationServices.getFusedLocationProviderClient(this)
        fusedClient.lastLocation
            .addOnSuccessListener { lastLocation ->
                if (requestGeneration != gpsRequestGeneration) return@addOnSuccessListener
                val ageMillis = lastLocation?.let { System.currentTimeMillis() - it.time } ?: Long.MAX_VALUE
                if (lastLocation != null && ageMillis in 0..LAST_LOCATION_MAX_AGE_MILLIS) {
                    applyGpsLocation(lastLocation, isPullRefresh, isInitial)
                } else {
                    handleGpsFailure(isInitial)
                }
            }
            .addOnFailureListener {
                if (requestGeneration == gpsRequestGeneration) handleGpsFailure(isInitial)
            }
    }

    private fun applyGpsLocation(location: Location, isPullRefresh: Boolean, isInitial: Boolean) {
        viewModel.setLocationFromGps(
            lat = location.latitude,
            lon = location.longitude,
            isPullRefresh = isPullRefresh
        )
        if (isInitial) requestNotificationPermissionIfNeeded()
    }

    private fun handleGpsFailure(isInitial: Boolean) {
        Toast.makeText(
            this,
            "Chưa xác định được vị trí hiện tại. Vui lòng bật định vị trên máy.",
            Toast.LENGTH_SHORT
        ).show()
        if (isInitial) {
            viewModel.loadInitialFallbackWeather()
            requestNotificationPermissionIfNeeded()
        } else {
            viewModel.cancelRefreshing()
        }
    }

    override fun onDestroy() {
        gpsRequestGeneration++
        locationCancellationTokenSource?.cancel()
        super.onDestroy()
    }

    private companion object {
        const val LAST_LOCATION_MAX_AGE_MILLIS = 30 * 60 * 1000L
    }
}
