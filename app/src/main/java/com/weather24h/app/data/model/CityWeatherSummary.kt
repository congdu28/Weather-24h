package com.weather24h.app.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Tóm tắt thời tiết nhanh cho danh sách thành phố yêu thích.
 * Lưu trữ nhiệt độ, biểu tượng và trạng thái để hiển thị tức thì trên danh sách.
 */
@Keep
data class CityWeatherSummary(
    @SerializedName("cityName") val cityName: String,
    @SerializedName("tempC") val tempC: Int,
    @SerializedName("conditionText") val conditionText: String,
    @SerializedName("weatherType") val weatherType: WeatherType = WeatherType.CLEAR,
    @SerializedName("isDay") val isDay: Boolean = true,
    @SerializedName("updatedAt") val updatedAt: Long = System.currentTimeMillis()
)
