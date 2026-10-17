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

        val VIETNAM_MAJOR_CITIES = listOf(
            // Miền Bắc
            CityLocation(name = "Hà Nội", country = "Việt Nam", admin1 = "Thủ đô", latitude = 21.0285, longitude = 105.8542),
            CityLocation(name = "Hải Phòng", country = "Việt Nam", admin1 = "Hải Phòng", latitude = 20.8449, longitude = 106.6881),
            CityLocation(name = "Hạ Long", country = "Việt Nam", admin1 = "Quảng Ninh", latitude = 20.9505, longitude = 107.0734),
            CityLocation(name = "Sa Pa", country = "Việt Nam", admin1 = "Lào Cai", latitude = 22.3364, longitude = 103.8438),
            CityLocation(name = "Ninh Bình", country = "Việt Nam", admin1 = "Ninh Bình", latitude = 20.2506, longitude = 105.9745),
            CityLocation(name = "Bắc Ninh", country = "Việt Nam", admin1 = "Bắc Ninh", latitude = 21.1861, longitude = 106.0763),
            CityLocation(name = "Thái Nguyên", country = "Việt Nam", admin1 = "Thái Nguyên", latitude = 21.5942, longitude = 105.8481),
            CityLocation(name = "Nam Định", country = "Việt Nam", admin1 = "Nam Định", latitude = 20.4344, longitude = 106.1685),
            // Miền Trung & Tây Nguyên
            CityLocation(name = "Đà Nẵng", country = "Việt Nam", admin1 = "Đà Nẵng", latitude = 16.0544, longitude = 108.2022),
            CityLocation(name = "Huế", country = "Việt Nam", admin1 = "Thừa Thiên Huế", latitude = 16.4637, longitude = 107.5909),
            CityLocation(name = "Nha Trang", country = "Việt Nam", admin1 = "Khánh Hòa", latitude = 12.2388, longitude = 109.1967),
            CityLocation(name = "Đà Lạt", country = "Việt Nam", admin1 = "Lâm Đồng", latitude = 11.9404, longitude = 108.4583),
            CityLocation(name = "Quy Nhơn", country = "Việt Nam", admin1 = "Bình Định", latitude = 13.7820, longitude = 109.2194),
            CityLocation(name = "Vinh", country = "Việt Nam", admin1 = "Nghệ An", latitude = 18.6734, longitude = 105.6813),
            CityLocation(name = "Thanh Hóa", country = "Việt Nam", admin1 = "Thanh Hóa", latitude = 19.8067, longitude = 105.7852),
            CityLocation(name = "Buôn Ma Thuột", country = "Việt Nam", admin1 = "Đắk Lắk", latitude = 12.6667, longitude = 108.0500),
            CityLocation(name = "Phan Thiết", country = "Việt Nam", admin1 = "Bình Thuận", latitude = 10.9289, longitude = 108.1021),
            // Miền Nam
            CityLocation(name = "TP. Hồ Chí Minh", country = "Việt Nam", admin1 = "TP. Hồ Chí Minh", latitude = 10.8231, longitude = 106.6297),
            CityLocation(name = "Cần Thơ", country = "Việt Nam", admin1 = "Cần Thơ", latitude = 10.0452, longitude = 105.7469),
            CityLocation(name = "Vũng Tàu", country = "Việt Nam", admin1 = "Bà Rịa - Vũng Tàu", latitude = 10.3460, longitude = 107.0843),
            CityLocation(name = "Phú Quốc", country = "Việt Nam", admin1 = "Kiên Giang", latitude = 10.2289, longitude = 103.9572),
            CityLocation(name = "Biên Hòa", country = "Việt Nam", admin1 = "Đồng Nai", latitude = 10.9574, longitude = 106.8427),
            CityLocation(name = "Thủ Dầu Một", country = "Việt Nam", admin1 = "Bình Dương", latitude = 10.9804, longitude = 106.6519)
        )

        val POPULAR_CITIES = VIETNAM_MAJOR_CITIES.take(8) + listOf(
            CityLocation(name = "Tokyo", country = "Nhật Bản", latitude = 35.6762, longitude = 139.6503),
            CityLocation(name = "Seoul", country = "Hàn Quốc", latitude = 37.5665, longitude = 126.9780),
            CityLocation(name = "Bangkok", country = "Thái Lan", latitude = 13.7563, longitude = 100.5018),
            CityLocation(name = "Paris", country = "Pháp", latitude = 48.8566, longitude = 2.3522)
        )

        fun normalizeVietnamese(text: String): String {
            val temp = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
            val pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
            return pattern.matcher(temp).replaceAll("")
                .replace("đ", "d")
                .replace("Đ", "D")
                .lowercase()
                .trim()
        }
    }
}
