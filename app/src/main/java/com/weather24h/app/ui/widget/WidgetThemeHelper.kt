package com.weather24h.app.ui.widget

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources

object WidgetThemeHelper {

    /**
     * Xác định chính xác theme Sáng/Tối cho Widget.
     * Khi cấu hình là "SYSTEM", ưu tiên đọc trực tiếp từ Resources.getSystem() của Android OS
     * để luôn phản ánh tức thì trạng thái Dark Mode thực tế của hệ thống mà không bị cache cũ
     * từ context của BroadcastReceiver.
     */
    fun resolveIsDark(context: Context): Boolean {
        return try {
            val prefs = context.getSharedPreferences("weather24h_prefs", Context.MODE_PRIVATE)
            val themeStr = prefs.getString("widget_theme_mode", "SYSTEM") ?: "SYSTEM"
            when (themeStr.uppercase()) {
                "LIGHT" -> false
                "DARK" -> true
                else -> {
                    val systemUiMode = Resources.getSystem().configuration.uiMode
                    val systemNight = (systemUiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                    if (systemNight) {
                        true
                    } else {
                        val appUiMode = context.resources.configuration.uiMode
                        (appUiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                    }
                }
            }
        } catch (_: Exception) {
            val systemUiMode = Resources.getSystem().configuration.uiMode
            (systemUiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
    }

    /**
     * Rút gọn tên địa điểm cho các Widget nhỏ (1x1, 2x1, 2x2):
     * Ví dụ: "Xã Hoằng Thanh, Hoằng Hóa" -> "Hoằng Thanh", "Thành phố Thanh Hóa" -> "Thanh Hóa",
     * "Thành phố Hồ Chí Minh" -> "TP. HCM", "Quận Cầu Giấy" -> "Cầu Giấy".
     */
    fun formatShortLocationName(rawName: String): String {
        val firstPart = rawName.substringBefore(",").substringBefore("-").trim()
        if (firstPart.isEmpty()) return "Vị trí"

        val lower = firstPart.lowercase()
        if (lower.contains("hồ chí minh") || lower == "tp.hcm" || lower == "tphcm") {
            return "TP. HCM"
        }
        if (lower.contains("bà rịa") && lower.contains("vũng tàu")) {
            return "Vũng Tàu"
        }
        if (lower.contains("thừa thiên") && lower.contains("huế")) {
            return "Huế"
        }

        val prefixes = listOf(
            "thành phố trực thuộc trung ương ",
            "thành phố ",
            "tp. ",
            "tp ",
            "thủ đô ",
            "thị xã ",
            "tx. ",
            "tx ",
            "quận ",
            "huyện đảo ",
            "huyện ",
            "phường ",
            "xã ",
            "thị trấn ",
            "tt. ",
            "tỉnh "
        )

        for (prefix in prefixes) {
            if (lower.startsWith(prefix)) {
                val stripped = firstPart.substring(prefix.length).trim()
                if (stripped.isNotEmpty() && stripped.all { it.isDigit() }) {
                    return when {
                        lower.startsWith("quận ") -> "Quận $stripped"
                        lower.startsWith("phường ") -> "P. $stripped"
                        else -> firstPart
                    }
                }
                if (stripped.length >= 2) {
                    return stripped
                }
            }
        }
        return firstPart
    }
}
