package com.weather24h.app.data.model

import com.google.gson.annotations.SerializedName

data class CityLocation(
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String = "",
    @SerializedName("admin1") val admin1: String? = null,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    val isCurrentLocation: Boolean = false
) {
    val displayName: String
        get() {
            return if (!admin1.isNullOrEmpty() && admin1 != name) {
                "$name, $admin1, $country"
            } else if (country.isNotEmpty()) {
                "$name, $country"
            } else {
                name
            }
        }

    companion object {
        val DEFAULT_LOCATION = CityLocation(
            name = "Hà Nội",
            country = "Việt Nam",
            latitude = 21.0285,
            longitude = 105.8542
        )

        val POPULAR_CITIES = listOf(
            CityLocation(name = "Hà Nội", country = "Việt Nam", latitude = 21.0285, longitude = 105.8542),
            CityLocation(name = "TP. Hồ Chí Minh", country = "Việt Nam", latitude = 10.8231, longitude = 106.6297),
            CityLocation(name = "Đà Nẵng", country = "Việt Nam", latitude = 16.0544, longitude = 108.2022),
            CityLocation(name = "Đà Lạt", country = "Việt Nam", latitude = 11.9404, longitude = 108.4583),
            CityLocation(name = "Hải Phòng", country = "Việt Nam", latitude = 20.8449, longitude = 106.6881),
            CityLocation(name = "Cần Thơ", country = "Việt Nam", latitude = 10.0452, longitude = 105.7469),
            CityLocation(name = "Tokyo", country = "Nhật Bản", latitude = 35.6762, longitude = 139.6503),
            CityLocation(name = "Seoul", country = "Hàn Quốc", latitude = 37.5665, longitude = 126.9780),
            CityLocation(name = "Bangkok", country = "Thái Lan", latitude = 13.7563, longitude = 100.5018),
            CityLocation(name = "Paris", country = "Pháp", latitude = 48.8566, longitude = 2.3522)
        )
    }
}
