package com.weather24h.app.data.model

enum class WeatherForecastModel(
    val apiValue: String,
    val title: String,
    val provider: String,
    val description: String,
    val badge: String,
    val badgeColor: Long
) {
    BEST_MATCH(
        apiValue = "best_match",
        title = "Tự động tối ưu (Best Match)",
        provider = "Open-Meteo",
        description = "Tự động chọn mô hình có độ phân giải tối ưu theo từng tọa độ.",
        badge = "Khuyên dùng",
        badgeColor = 0xFF10B981
    ),
    ECMWF(
        apiValue = "ecmwf_ifs025",
        title = "ECMWF IFS 0,25°",
        provider = "Châu Âu (ECMWF)",
        description = "Mô hình toàn cầu tiêu chuẩn độ chính xác cao hàng đầu Châu Âu.",
        badge = "Châu Âu",
        badgeColor = 0xFF38BDF8
    ),
    GFS(
        apiValue = "gfs_seamless",
        title = "NOAA GFS",
        provider = "Hoa Kỳ (NOAA)",
        description = "Mô hình khí quyển toàn cầu tiêu chuẩn của Mỹ, cập nhật 4 lần/ngày.",
        badge = "Hoa Kỳ",
        badgeColor = 0xFF818CF8
    ),
    ICON(
        apiValue = "icon_seamless",
        title = "DWD ICON",
        provider = "Đức (DWD)",
        description = "Mô hình khí tượng toán học toàn cầu của Cục Khí tượng Đức.",
        badge = "Đức",
        badgeColor = 0xFFF59E0B
    ),
    JMA(
        apiValue = "jma_seamless",
        title = "JMA GSM",
        provider = "Nhật Bản (JMA)",
        description = "Mô hình khí quyển toàn cầu của Cục Khí tượng Nhật Bản.",
        badge = "Nhật Bản",
        badgeColor = 0xFFEC4899
    ),
    METEO_FRANCE(
        apiValue = "meteofrance_seamless",
        title = "Météo-France",
        provider = "Pháp (Météo-France)",
        description = "Mô hình ARPEGE của Pháp, phân tích độ ẩm và áp suất khí quyển.",
        badge = "Pháp",
        badgeColor = 0xFFA855F7
    );

    val displayName: String get() = title
    val agency: String get() = provider
}
