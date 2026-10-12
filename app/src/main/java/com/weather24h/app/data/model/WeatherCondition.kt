package com.weather24h.app.data.model

enum class WeatherType {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    THUNDERSTORM
}

data class WeatherCondition(
    val descriptionVi: String,
    val type: WeatherType
) {
    companion object {
        fun fromWmoCode(code: Int): WeatherCondition {
            return when (code) {
                0 -> WeatherCondition("Trời quang đãng", WeatherType.CLEAR)
                1 -> WeatherCondition("Ít mây, trời trong", WeatherType.CLEAR)
                2 -> WeatherCondition("Mây rải rác", WeatherType.PARTLY_CLOUDY)
                3 -> WeatherCondition("Nhiều mây, âm u", WeatherType.CLOUDY)
                45, 48 -> WeatherCondition("Sương mù dày", WeatherType.FOG)
                51, 53, 55 -> WeatherCondition("Mưa phùn nhẹ", WeatherType.DRIZZLE)
                61 -> WeatherCondition("Mưa rào nhẹ", WeatherType.RAIN)
                63, 65 -> WeatherCondition("Mưa rào vừa đến to", WeatherType.RAIN)
                71, 73, 75, 77 -> WeatherCondition("Tuyết rơi", WeatherType.SNOW)
                80, 81, 82 -> WeatherCondition("Mưa rào từng đợt", WeatherType.RAIN)
                95, 96, 99 -> WeatherCondition("Dông bão, có sấm sét", WeatherType.THUNDERSTORM)
                else -> WeatherCondition("Thời tiết ôn hòa", WeatherType.PARTLY_CLOUDY)
            }
        }
    }
}
