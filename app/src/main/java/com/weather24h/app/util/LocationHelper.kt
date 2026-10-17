package com.weather24h.app.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Trình hỗ trợ định vị vị trí thời gian thực cho Widget, Worker và ứng dụng
 * Tự động ưu tiên FusedLocationProviderClient (Google Play Services) chính xác cao,
 * có dự phòng nhiều tầng qua lastLocation và LocationManager gốc của Android.
 */
object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context, timeoutMillis: Long = 7000L): Location? {
        if (!hasLocationPermission(context)) return null

        return withTimeoutOrNull(timeoutMillis) {
            suspendCancellableCoroutine { cont ->
                try {
                    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                    val hasFine = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    val cts = CancellationTokenSource()
                    cont.invokeOnCancellation { cts.cancel() }

                    val request = CurrentLocationRequest.Builder()
                        .setPriority(
                            if (hasFine) Priority.PRIORITY_HIGH_ACCURACY
                            else Priority.PRIORITY_BALANCED_POWER_ACCURACY
                        )
                        .setMaxUpdateAgeMillis(60_000L) // Sử dụng vị trí hợp lệ trong vòng 60 giây
                        .setDurationMillis(timeoutMillis)
                        .build()

                    fusedClient.getCurrentLocation(request, cts.token)
                        .addOnSuccessListener { loc ->
                            if (loc != null) {
                                if (cont.isActive) cont.resume(loc)
                            } else {
                                // Dự phòng 1: Thử lấy lastLocation từ FusedLocationClient
                                fusedClient.lastLocation
                                    .addOnSuccessListener { lastLoc ->
                                        if (lastLoc != null) {
                                            if (cont.isActive) cont.resume(lastLoc)
                                        } else {
                                            // Dự phòng 2: Thử lấy qua LocationManager gốc
                                            val systemLoc = getSystemLastKnownLocation(context)
                                            if (cont.isActive) cont.resume(systemLoc)
                                        }
                                    }
                                    .addOnFailureListener {
                                        val systemLoc = getSystemLastKnownLocation(context)
                                        if (cont.isActive) cont.resume(systemLoc)
                                    }
                            }
                        }
                        .addOnFailureListener {
                            fusedClient.lastLocation
                                .addOnSuccessListener { lastLoc ->
                                    if (lastLoc != null) {
                                        if (cont.isActive) cont.resume(lastLoc)
                                    } else {
                                        val systemLoc = getSystemLastKnownLocation(context)
                                        if (cont.isActive) cont.resume(systemLoc)
                                    }
                                }
                                .addOnFailureListener {
                                    val systemLoc = getSystemLastKnownLocation(context)
                                    if (cont.isActive) cont.resume(systemLoc)
                                }
                        }
                } catch (_: SecurityException) {
                    if (cont.isActive) cont.resume(null)
                } catch (_: Exception) {
                    if (cont.isActive) cont.resume(null)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getSystemLastKnownLocation(context: Context): Location? {
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
            val gps = try { lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (_: Exception) { null }
            val net = try { lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) { null }
            val passive = try { lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) } catch (_: Exception) { null }
            listOfNotNull(gps, net, passive).maxByOrNull { it.time }
        } catch (_: Exception) {
            null
        }
    }
}
