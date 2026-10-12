package com.weather24h.app.data.remote

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.weather24h.app.data.model.OutfitRecommendation
import com.weather24h.app.data.model.WeatherCondition
import com.weather24h.app.data.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GeminiStylistService(context: Context) {
    private val prefs = context.getSharedPreferences("weather24h_prefs", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString("gemini_api_key", apiKey.trim()).apply()
    }

    fun getApiKey(): String? {
        val key = prefs.getString("gemini_api_key", null)
        return if (!key.isNullOrBlank()) key.trim() else null
    }

    suspend fun getOutfitRecommendation(weather: WeatherData): OutfitRecommendation = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!apiKey.isNullOrEmpty()) {
            try {
                val aiResult = fetchFromGemini(apiKey, weather)
                if (aiResult != null) return@withContext aiResult
            } catch (_: Exception) {}
        }

        // Tự động dùng thuật toán chuyên gia nếu không có key hoặc lỗi mạng
        getRuleBasedRecommendation(weather)
    }

    private fun fetchFromGemini(apiKey: String, weather: WeatherData): OutfitRecommendation? {
        val condition = WeatherCondition.fromWmoCode(weather.current.weatherCode).descriptionVi
        val maxRain = weather.hourly24h.maxOfOrNull { it.precipitationProbability } ?: 0

        val prompt = """
        Bạn là một chuyên gia thời trang (AI Fashion Stylist). Dựa trên thông số thời tiết tại ${weather.location.displayName}:
        - Nhiệt độ: ${weather.current.temperature}°C (Cảm nhận: ${weather.current.feelsLike}°C)
        - Dao động: ${weather.minTemp24h}°C - ${weather.maxTemp24h}°C
        - Trạng thái: $condition
        - Độ ẩm: ${weather.current.humidity}%
        - Chỉ số tia cực tím UV: ${weather.current.uvIndex}
        - Xác suất mưa cao nhất trong 24h: $maxRain%
        - Gió: ${weather.current.windSpeed} km/h

        Hãy trả lời bằng JSON duy nhất với các trường:
        {
          "summary": "1-2 câu tóm tắt ngắn phong cách hôm nay",
          "top": "Áo khuyên mặc",
          "bottom": "Quần/váy khuyên mặc",
          "footwear": "Giày dép khuyên dùng",
          "accessories": ["phụ kiện 1", "phụ kiện 2", "ví dụ: dù, kính râm, kem chống nắng..."],
          "warningAlert": "Cảnh báo thời tiết quan trọng nếu có (hoặc null)",
          "activityTip": "Lời khuyên vận động hoặc sức khỏe",
          "styleVibe": "Tên phong cách (ví dụ: Năng động dạo phố)"
        }
        """.trimIndent()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        val payload = """
        {
          "contents": [{
            "parts": [{"text": ${gson.toJson(prompt)}}]
          }],
          "generationConfig": {
            "responseMimeType": "application/json"
          }
        }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return null

        val json = gson.fromJson(body, JsonObject::class.java)
        val candidates = json.getAsJsonArray("candidates") ?: return null
        if (candidates.size() == 0) return null

        val content = candidates.get(0).asJsonObject.getAsJsonObject("content")
        val parts = content.getAsJsonArray("parts")
        val rawText = parts.get(0).asJsonObject.get("text").asString

        val data = gson.fromJson(rawText, JsonObject::class.java)

        val accList = mutableListOf<String>()
        data.getAsJsonArray("accessories")?.forEach { accList.add(it.asString) }

        return OutfitRecommendation(
            summary = data.get("summary")?.asString ?: "Trang phục thoải mái, hợp thời tiết.",
            top = data.get("top")?.asString ?: "Áo thun thoáng mát",
            bottom = data.get("bottom")?.asString ?: "Quần dài hoặc short",
            footwear = data.get("footwear")?.asString ?: "Sneaker năng động",
            accessories = if (accList.isNotEmpty()) accList else listOf("Bình nước"),
            warningAlert = data.get("warningAlert")?.let { if (it.isJsonNull) null else it.asString },
            activityTip = data.get("activityTip")?.asString ?: "Uống đủ nước trong ngày.",
            styleVibe = data.get("styleVibe")?.asString ?: "Thời trang ứng dụng",
            isAIGenerated = true
        )
    }

    private fun getRuleBasedRecommendation(weather: WeatherData): OutfitRecommendation {
        val temp = weather.current.temperature
        val maxRain = weather.hourly24h.maxOfOrNull { it.precipitationProbability } ?: 0
        val uv = weather.current.uvIndex
        val isWindy = weather.current.windSpeed > 22

        val summary: String
        val top: String
        val bottom: String
        var footwear: String
        val accessories = mutableListOf<String>()
        var warningAlert: String? = null
        val activityTip: String
        val styleVibe: String

        when {
            temp < 16.0 -> {
                styleVibe = "Ấm áp & Layering"
                summary = "Trời rét lạnh, cần chú ý giữ ấm các vùng cổ, ngực và bàn tay."
                top = "Áo len dệt kim dày, áo giữ nhiệt hoặc áo khoác phao chần bông"
                bottom = "Quần dạ hoặc quần jeans lót nỉ"
                footwear = "Boots cổ cao hoặc sneaker dày dặn kèm tất len"
                accessories.add("Khăn quàng cổ")
                accessories.add("Găng tay")
                activityTip = "Tránh ở ngoài gió lạnh quá lâu, uống nước ấm thường xuyên."
            }
            temp < 23.0 -> {
                styleVibe = "Smart Casual mùa thu"
                summary = "Thời tiết se lạnh cực kỳ dễ chịu, hoàn hảo cho phong cách layer thanh lịch."
                top = "Áo hoodie mỏng, áo cardigan hoặc blazer kết hợp áo thun"
                bottom = "Quần tây ống suông hoặc quần chinos thoải mái"
                footwear = "Sneaker da hoặc loafer cổ điển"
                accessories.add("Áo khoác gió nhẹ")
                activityTip = "Thời tiết vàng để dạo phố, làm việc quán cà phê và tập thể dục."
            }
            temp < 29.0 -> {
                styleVibe = "Năng động & Trẻ trung"
                summary = "Nhiệt độ mát mẻ, lý tưởng cho mọi trang phục dạo phố và công sở."
                top = "Áo thun cotton mềm mại, áo polo hoặc sơ mi đũi thoáng khí"
                bottom = "Quần jeans nhẹ hoặc chân váy xòe"
                footwear = "Sneaker thoải mái hoặc sandal thời trang"
                activityTip = "Rất thuận lợi cho các hoạt động ngoài trời cả ngày."
            }
            temp < 34.0 -> {
                styleVibe = "Mát mẻ & Tối giản"
                summary = "Trời oi bức, ưu tiên các chất liệu thấm hút mồ hôi và màu sáng mát."
                top = "Áo thun oversize mỏng mát, áo tank-top hoặc sơ mi đũi ngắn tay"
                bottom = "Quần short linen hoặc quần vải thô thoáng khí"
                footwear = "Dép quai hậu thông thoáng hoặc giày lười vải"
                activityTip = "Nên hạn chế vận động mạnh vào giữa trưa, mang theo nước."
            }
            else -> {
                styleVibe = "Chống nắng tối đa"
                summary = "Nắng nóng gay gắt! Hãy bảo vệ làn da với trang phục chống nắng đạt chuẩn."
                top = "Áo thun làm mát (Airism), áo khoác chống nắng UPF 50+"
                bottom = "Quần vải nhẹ co giãn, màu sáng tản nhiệt"
                footwear = "Dép quai ngang thông thoáng hoặc giày vải mỏng"
                warningAlert = "Cảnh báo nhiệt độ cao nguy cơ say nắng. Hạn chế ra đường từ 11h - 15h."
                activityTip = "Uống nhiều nước có bổ sung khoáng/điện giải."
            }
        }

        if (maxRain >= 40) {
            warningAlert = "Hôm nay có khả năng mưa ($maxRain%). Nhớ chuẩn bị đồ chống ướt!"
            accessories.add("Dù gấp chống thấm")
            accessories.add("Áo mưa bỏ cốp")
            if (maxRain >= 65) {
                footwear = "Giày chống nước, giày đế bám tốt chống trơn"
            }
        }

        if (uv >= 6.0) {
            accessories.add("Kính râm chống tia UV")
            accessories.add("Kem chống nắng SPF 50+")
            accessories.add("Mũ rộng vành")
            if (warningAlert == null) {
                warningAlert = "Chỉ số UV cao ($uv). Nhớ bôi kem chống nắng trước khi ra ngoài."
            }
        }

        if (isWindy) {
            accessories.add("Áo khoác cản gió")
        }

        return OutfitRecommendation(
            summary = summary,
            top = top,
            bottom = bottom,
            footwear = footwear,
            accessories = accessories.distinct(),
            warningAlert = warningAlert,
            activityTip = activityTip,
            styleVibe = styleVibe,
            isAIGenerated = false
        )
    }
}
