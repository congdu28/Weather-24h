package com.weather24h.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.android.gms.location.LocationServices
import com.weather24h.app.ui.screens.WeatherScreen
import com.weather24h.app.ui.screens.WeatherViewModel
import com.weather24h.app.ui.theme.Weather24hTheme

class MainActivity : ComponentActivity() {
    private val viewModel: WeatherViewModel by viewModels()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            fetchGpsLocation()
        } else {
            Toast.makeText(this, "Không có quyền truy cập vị trí. Sử dụng thành phố mặc định.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make system bars transparent
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            Weather24hTheme {
                WeatherScreen(
                    viewModel = viewModel,
                    onRequestGps = { checkLocationPermissions() }
                )
            }
        }
    }

    private fun checkLocationPermissions() {
        val hasFine = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            fetchGpsLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun fetchGpsLocation() {
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(this)
            fusedClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.setLocationFromGps(location.latitude, location.longitude)
                } else {
                    Toast.makeText(this, "Chưa xác định được tọa độ GPS. Vui lòng bật định vị trên máy.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Lỗi khi lấy vị trí GPS.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
