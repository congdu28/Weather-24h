package com.weather24h.app.util

import android.content.Context
import com.weather24h.app.BuildConfig

/**
 * Lớp bảo vệ khóa cấu hình & kiểm tra tính toàn vẹn gói ứng dụng (Anti-Tamper / Anti-Clone).
 * Giải mã khóa từ chuỗi nhị phân đã làm rối lúc biên dịch, ngăn chặn trích xuất chuỗi tĩnh (strings/JADX)
 * và vô hiệu hóa khóa mặc định nếu APK bị đổi ApplicationId / đóng gói lại.
 */
object SecurityVault {
    private const val MASK_SEED = 0x5A3C9E71

    private val expectedPkg: String
        get() = charArrayOf(
            'c', 'o', 'm', '.', 'w', 'e', 'a', 't', 'h', 'e', 'r', '2', '4', 'h', '.', 'a', 'p', 'p'
        ).concatToString()

    fun isPackageGenuine(context: Context?): Boolean {
        if (context == null) return true
        return context.packageName == expectedPkg
    }

    fun getDefaultGeminiKey(context: Context? = null): String {
        if (!isPackageGenuine(context)) return ""
        return decodeHexXor(BuildConfig.DEFAULT_GEMINI_API_KEY_ENC, MASK_SEED)
    }

    fun getDefaultWeatherApiKey(context: Context? = null): String {
        if (!isPackageGenuine(context)) return ""
        return decodeHexXor(BuildConfig.DEFAULT_WEATHER_API_KEY_ENC, MASK_SEED xor 0x13572468)
    }

    fun getDefaultGoogleWeatherKey(context: Context? = null): String {
        if (!isPackageGenuine(context)) return ""
        return decodeHexXor(BuildConfig.DEFAULT_GOOGLE_WEATHER_KEY_ENC, MASK_SEED xor 0x24681357)
    }

    private fun decodeHexXor(encodedHex: String, seed: Int): String {
        val clean = encodedHex.trim()
        if (clean.isEmpty() || clean.length % 2 != 0) return ""
        return try {
            val len = clean.length / 2
            val out = ByteArray(len)
            for (i in 0 until len) {
                val hi = Character.digit(clean[i * 2], 16)
                val lo = Character.digit(clean[i * 2 + 1], 16)
                if (hi < 0 || lo < 0) return ""
                val b = (hi shl 4) or lo
                val k = ((seed ushr ((i % 4) * 8)) xor (i * 31 + 0x5A)) and 0xFF
                out[i] = (b xor k).toByte()
            }
            String(out, Charsets.UTF_8).trim()
        } catch (_: Exception) {
            ""
        }
    }
}
