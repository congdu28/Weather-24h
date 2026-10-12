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
        provider = "Open-Meteo Hybrid Multi-Model",
        description = "Tự động ghép nối các mô hình có độ sai số thấp nhất theo từng tọa độ địa lý.",
        badge = "Khuyên dùng",
        badgeColor = 0xFF10B981
    ),
    ECMWF(
        apiValue = "ecmwf_ifs025",
        title = "ECMWF IFS (Châu Âu)",
        provider = "Trung tâm Dự báo Thời tiết Hạn vừa Châu Âu",
        description = "Mô hình được đánh giá có độ chính xác cao nhất thế giới hiện nay, chuẩn vàng quốc tế.",
        badge = "Chuẩn vàng thế giới",
        badgeColor = 0xFF38BDF8
    ),
    GFS(
        apiValue = "gfs_seamless",
        title = "NOAA GFS (Hoa Kỳ)",
        provider = "Cơ quan Khí quyển & Đại dương Mỹ (NOAA)",
        description = "Mô hình dự báo toàn cầu tiêu chuẩn của Mỹ, dữ liệu siêu máy tính cập nhật 4 lần/ngày.",
        badge = "Uy tín Hoa Kỳ",
        badgeColor = 0xFF818CF8
    ),
    ICON(
        apiValue = "icon_seamless",
        title = "DWD ICON (Đức)",
        provider = "Cục Khí tượng Quốc gia Đức (DWD)",
        description = "Mô hình toán phi thủy tĩnh độ phân giải cao, dự báo mưa và biến động gió cực kỳ chi tiết.",
        badge = "Độ chính xác cao",
        badgeColor = 0xFFF59E0B
    ),
    JMA(
        apiValue = "jma_seamless",
        title = "JMA GSM (Nhật Bản)",
        provider = "Cục Khí tượng Nhật Bản (JMA)",
        description = "Tối ưu xuất sắc cho khí hậu nhiệt đới, gió mùa và diễn biến áp thấp/bão tại Đông Nam Á & Châu Á.",
        badge = "Chuyên gia Châu Á",
        badgeColor = 0xFFEC4899
    ),
    METEO_FRANCE(
        apiValue = "meteofrance_seamless",
        title = "Météo-France (Pháp)",
        provider = "Cơ quan Khí tượng Pháp (Météo-France)",
        description = "Mô hình toàn cầu ARPEGE nổi tiếng của Pháp về phân tích độ ẩm và áp suất khí quyển.",
        badge = "Châu Âu ARPEGE",
        badgeColor = 0xFFA855F7
    );

    val displayName: String get() = title
    val agency: String get() = provider
}
