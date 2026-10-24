package com.weather24h.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.google.gson.JsonParser
import com.weather24h.app.BuildConfig
import com.weather24h.app.data.model.AppUpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class AppUpdateManager(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    companion object {
        /**
         * So sánh 2 chuỗi phiên bản dạng "1.0.40" vs "1.0.41" hoặc "v1.0.40"
         * Trả về true nếu latest > current
         */
        fun isNewerVersion(latestVersion: String, currentVersion: String): Boolean {
            val cleanLatest = latestVersion.trim().removePrefix("v").removePrefix("V")
            val cleanCurrent = currentVersion.trim().removePrefix("v").removePrefix("V")

            val latestParts = cleanLatest.split('.').map { it.toIntOrNull() ?: 0 }
            val currentParts = cleanCurrent.split('.').map { it.toIntOrNull() ?: 0 }

            val maxLen = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
            return false
        }

        /**
         * Lọc sạch toàn bộ đường dẫn máy chủ, markdown links, mã hash khỏi mô tả bản cập nhật,
         * chỉ giữ lại các nội dung tính năng/thay đổi thân thiện với người dùng.
         */
        fun sanitizeReleaseNotes(rawBody: String?): String {
            if (rawBody.isNullOrBlank()) {
                return "• Tối ưu hóa hiệu năng và độ ổn định toàn hệ thống.\n• Cải tiến giao diện trực quan và trải nghiệm người dùng.\n• Sửa các lỗi phát sinh từ phiên bản trước."
            }

            val lines = rawBody.lines()
            val cleanedLines = mutableListOf<String>()
            val scmKeyword = SecurityVault.getScmProviderKeyword()

            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isBlank()) continue

                if (trimmed.startsWith("##") ||
                    trimmed.startsWith("http://") ||
                    trimmed.startsWith("https://") ||
                    trimmed.contains("Full Changelog", ignoreCase = true) ||
                    trimmed.contains("What's Changed", ignoreCase = true)
                ) {
                    continue
                }

                var clean = trimmed.replace(Regex("""https?://\S+"""), "").trim()
                clean = clean.replace(Regex("""\[(.*?)\]\(.*?\)"""), "$1").trim()
                clean = clean.replace(Regex("""\s+by\s+@[a-zA-Z0-9_\-]+.*$"""), "").trim()
                clean = clean.replace(Regex("""\s+in\s+#[0-9]+.*$"""), "").trim()
                clean = clean.replace(Regex("""\s+in\s*$"""), "").trim()
                clean = clean.replace("**", "").replace("__", "").trim()

                if (clean.contains(scmKeyword, ignoreCase = true) ||
                    clean.contains("compare/", ignoreCase = true) ||
                    clean.contains("commit/", ignoreCase = true) ||
                    clean.contains("pull/", ignoreCase = true)
                ) {
                    continue
                }

                if (clean.startsWith("* ") || clean.startsWith("- ") || clean.startsWith("• ")) {
                    clean = clean.substring(2).trim()
                } else if (clean.startsWith("•")) {
                    clean = clean.removePrefix("•").trim()
                }

                clean = clean.replace(
                    Regex("""^(?:\[(?:feat|fix|perf|refactor|chore|style|test|build|ci)\]|(?:feat|fix|perf|refactor|chore|style|test|build|ci)(?:\([a-zA-Z0-9_\- ]+\))?:)\s*""", RegexOption.IGNORE_CASE),
                    ""
                ).trim()

                if (clean.isNotEmpty() && clean[0].isLowerCase()) {
                    clean = clean.replaceFirstChar { it.uppercaseChar() }
                }

                if (clean.isNotEmpty()) {
                    clean = "• $clean"
                }

                if (clean.length > 2 && clean != "•") {
                    cleanedLines.add(clean)
                }
            }

            return if (cleanedLines.isEmpty()) {
                "• Tối ưu hóa hiệu năng và độ ổn định toàn hệ thống.\n• Cải tiến giao diện trực quan và trải nghiệm người dùng.\n• Sửa các lỗi phát sinh từ phiên bản trước."
            } else {
                cleanedLines.joinToString("\n")
            }
        }

        fun isFallbackReleaseNotes(notes: String): Boolean {
            if (notes.isBlank()) return true
            val lower = notes.lowercase()
            return (lower.contains("tối ưu hóa") && lower.contains("trải nghiệm người dùng")) ||
                    (lower.contains("tinh chỉnh hiển thị") && lower.contains("ổn định")) ||
                    (lower.contains("tối ưu hóa hiệu năng") && lower.contains("ổn định toàn hệ thống"))
        }
    }

    /**
     * Kiểm tra phiên bản mới nhất ngầm qua kênh bảo mật
     */
    suspend fun checkForUpdates(
        currentVersion: String = BuildConfig.VERSION_NAME
    ): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val baseEndpoint = SecurityVault.getUpdateBaseEndpoint(context)
            if (baseEndpoint.isBlank()) {
                return@withContext Result.failure(Exception("Không thể kết nối máy chủ cập nhật"))
            }
            val latestUrl = "$baseEndpoint/releases/latest"
            val request = Request.Builder()
                .url(latestUrl)
                .header("Accept", SecurityVault.getUpdateAcceptHeader())
                .header("Cache-Control", "no-cache")
                .header("Pragma", "no-cache")
                .header("User-Agent", "Weather24h-Android/${BuildConfig.VERSION_NAME}")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Máy chủ cập nhật phản hồi mã lỗi: ${response.code}")
                )
            }

            val bodyString = response.body?.string()
                ?: return@withContext Result.failure(Exception("Phản hồi rỗng từ máy chủ"))

            var jsonObject = JsonParser.parseString(bodyString).asJsonObject
            var rawTag = jsonObject.get("tag_name")?.asString ?: ""
            var latestVersion = rawTag.removePrefix("v").removePrefix("V")
            var apkUrl = ""
            var apkSize = 0L

            val extractApk: (com.google.gson.JsonObject) -> Pair<String, Long> = { obj ->
                var url = ""
                var size = 0L
                val assetsArray = obj.getAsJsonArray("assets")
                if (assetsArray != null) {
                    for (element in assetsArray) {
                        val asset = element.asJsonObject
                        val name = asset.get("name")?.asString ?: ""
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            url = asset.get("browser_download_url")?.asString ?: ""
                            size = asset.get("size")?.asLong ?: 0L
                            break
                        }
                    }
                }
                Pair(url, size)
            }

            val primaryApk = extractApk(jsonObject)
            apkUrl = primaryApk.first
            apkSize = primaryApk.second

            // Nếu bản release mới nhất đang trong quá trình đóng gói APK trên CI (chưa có asset .apk),
            // kiểm tra danh sách 5 bản phát hành gần nhất để tìm bản mới nhất đã có sẵn file APK
            if (apkUrl.isBlank()) {
                try {
                    val listReq = Request.Builder()
                        .url("$baseEndpoint/releases?per_page=5")
                        .header("Accept", SecurityVault.getUpdateAcceptHeader())
                        .header("Cache-Control", "no-cache")
                        .header("User-Agent", "Weather24h-Android/${BuildConfig.VERSION_NAME}")
                        .build()
                    val listResp = client.newCall(listReq).execute()
                    if (listResp.isSuccessful) {
                        val listBody = listResp.body?.string()
                        if (!listBody.isNullOrBlank()) {
                            val releasesArr = JsonParser.parseString(listBody).asJsonArray
                            for (relElem in releasesArr) {
                                val relObj = relElem.asJsonObject
                                val candidateApk = extractApk(relObj)
                                if (candidateApk.first.isNotBlank()) {
                                    jsonObject = relObj
                                    rawTag = relObj.get("tag_name")?.asString ?: ""
                                    latestVersion = rawTag.removePrefix("v").removePrefix("V")
                                    apkUrl = candidateApk.first
                                    apkSize = candidateApk.second
                                    break
                                }
                            }
                        }
                    }
                } catch (_: Exception) {}
            }

            val rawTitle = jsonObject.get("name")?.asString ?: "Bản cập nhật v$latestVersion"
            val releaseTitle = rawTitle.replace(Regex("""https?://\S+"""), "").trim().ifBlank { "Bản cập nhật v$latestVersion" }
            var releaseNotes = sanitizeReleaseNotes(jsonObject.get("body")?.asString)

            if (isFallbackReleaseNotes(releaseNotes) && rawTag.isNotBlank()) {
                val compareNotes = fetchCompareReleaseNotes(baseEndpoint, currentVersion, rawTag)
                if (!compareNotes.isNullOrBlank() && !isFallbackReleaseNotes(compareNotes)) {
                    releaseNotes = compareNotes
                } else {
                    val commitNotes = fetchCommitReleaseNotes(baseEndpoint, rawTag)
                    if (!commitNotes.isNullOrBlank() && !isFallbackReleaseNotes(commitNotes)) {
                        releaseNotes = commitNotes
                    }
                }
            }

            val updateAvailable = isNewerVersion(latestVersion, currentVersion) && apkUrl.isNotBlank()

            Result.success(
                AppUpdateInfo(
                    latestVersionName = latestVersion,
                    releaseTitle = releaseTitle,
                    releaseNotes = releaseNotes,
                    downloadUrl = apkUrl,
                    apkSize = apkSize,
                    isUpdateAvailable = updateAvailable
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Tải file APK ngầm và ghi ra bộ nhớ đệm cache của ứng dụng
     */
    suspend fun downloadApk(
        downloadUrl: String,
        onProgress: (progress: Float, downloadedBytes: Long, totalBytes: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", "Weather24h-Android/${BuildConfig.VERSION_NAME}")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Không thể tải file APK: Mã lỗi ${response.code}")
                )
            }

            val responseBody = response.body
                ?: return@withContext Result.failure(Exception("Dữ liệu tệp APK rỗng"))

            val totalBytes = responseBody.contentLength()
            val updateDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val tempFile = File(updateDir, "Weather24h-update.apk.tmp")
            val finalFile = File(updateDir, "Weather24h-update.apk")

            if (tempFile.exists()) tempFile.delete()
            if (finalFile.exists()) finalFile.delete()

            var downloadedBytes = 0L
            val buffer = ByteArray(8 * 1024)

            responseBody.byteStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        val progress = if (totalBytes > 0) {
                            downloadedBytes.toFloat() / totalBytes
                        } else {
                            0f
                        }
                        onProgress(progress, downloadedBytes, totalBytes)
                    }
                    output.flush()
                }
            }

            if (!tempFile.renameTo(finalFile)) {
                tempFile.copyTo(finalFile, overwrite = true)
                tempFile.delete()
            }

            Result.success(finalFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Kích hoạt trình cài đặt gói của Android để cập nhật đè ứng dụng
     */
    fun installApk(context: Context, apkFile: File): Result<Unit> {
        return try {
            if (!apkFile.exists() || apkFile.length() <= 0) {
                return Result.failure(Exception("Tệp cài đặt APK không tồn tại hoặc bị hỏng"))
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val permissionIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(permissionIntent)
                    return Result.failure(
                        Exception("Vui lòng cấp quyền 'Cài đặt ứng dụng không rõ nguồn gốc' cho Weather 24h rồi nhấn cập nhật lại.")
                    )
                }
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchCompareReleaseNotes(baseEndpoint: String, currentVersion: String, latestTag: String): String? {
        return try {
            val cleanCurrent = currentVersion.trim().removePrefix("v").removePrefix("V")
            val cleanLatest = latestTag.trim().removePrefix("v").removePrefix("V")
            val compareUrl = "$baseEndpoint/compare/v${cleanCurrent}...v${cleanLatest}"
            val request = Request.Builder()
                .url(compareUrl)
                .header("Accept", SecurityVault.getUpdateAcceptHeader())
                .header("User-Agent", "Weather24h-Android/${BuildConfig.VERSION_NAME}")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return null

            val body = response.body?.string() ?: return null
            val jsonObject = JsonParser.parseString(body).asJsonObject
            val commitsArray = jsonObject.getAsJsonArray("commits") ?: return null

            val commitMessages = mutableListOf<String>()
            for (element in commitsArray) {
                val commitObj = element.asJsonObject.getAsJsonObject("commit") ?: continue
                val msg = commitObj.get("message")?.asString ?: continue
                val firstLine = msg.lines().firstOrNull()?.trim() ?: ""
                if (firstLine.isNotBlank()) {
                    commitMessages.add(firstLine)
                }
            }

            if (commitMessages.isNotEmpty()) {
                sanitizeReleaseNotes(commitMessages.joinToString("\n"))
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchCommitReleaseNotes(baseEndpoint: String, rawTag: String): String? {
        return try {
            val commitUrl = "$baseEndpoint/commits/$rawTag"
            val request = Request.Builder()
                .url(commitUrl)
                .header("Accept", SecurityVault.getUpdateAcceptHeader())
                .header("User-Agent", "Weather24h-Android/${BuildConfig.VERSION_NAME}")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return null

            val body = response.body?.string() ?: return null
            val jsonObject = JsonParser.parseString(body).asJsonObject
            val commitObj = jsonObject.getAsJsonObject("commit") ?: return null
            val message = commitObj.get("message")?.asString ?: return null
            sanitizeReleaseNotes(message)
        } catch (_: Exception) {
            null
        }
    }
}
