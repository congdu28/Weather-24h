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
                    // Đọc từ hệ thống Android OS
                    val systemUiMode = Resources.getSystem().configuration.uiMode
                    val systemNight = (systemUiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                    if (systemNight) {
                        true
                    } else {
                        // Fallback đọc thêm từ context nếu cần
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
}
