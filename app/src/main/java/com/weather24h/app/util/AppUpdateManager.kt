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
        const val GITHUB_LATEST_RELEASE_API =
            "https://api.github.com/repos/congdu28/Weather-24h/releases/latest"

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
         * Lọc sạch toàn bộ đường dẫn git, repo, markdown links, commit SHA khỏi mô tả bản cập nhật,
         * chỉ giữ lại các nội dung tính năng/thay đổi thân thiện với người dùng để bảo mật repo.
         */
        fun sanitizeReleaseNotes(rawBody: String?): String {
            if (rawBody.isNullOrBlank()) {
                return "• Tối ưu hóa hiệu năng và độ ổn định toàn hệ thống.\n• Cải tiến giao diện trực quan và trải nghiệm người dùng.\n• Sửa các lỗi phát sinh từ phiên bản trước."
            }

            val lines = rawBody.lines()
            val cleanedLines = mutableListOf<String>()

            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isBlank()) continue

                // Bỏ qua các tiêu đề markdown hoặc các dòng chỉ chứa link changelog thuần túy
                if (trimmed.startsWith("##") ||
                    trimmed.startsWith("http://") ||
                    trimmed.startsWith("https://") ||
                    trimmed.contains("Full Changelog", ignoreCase = true) ||
                    trimmed.contains("What's Changed", ignoreCase = true)
                ) {
                    continue
                }

                // Loại bỏ các cú pháp URL http/https trong dòng
                var clean = trimmed.replace(Regex("""https?://\S+"""), "").trim()

                // Loại bỏ cú pháp Markdown link [Text](url) -> Text
                clean = clean.replace(Regex("""\[(.*?)\]\(.*?\)"""), "$1").trim()

                // Loại bỏ phần đuôi " by @user ..." hoặc " in #..." hoặc " in ..." nếu có
                clean = clean.replace(Regex("""\s+by\s+@[a-zA-Z0-9_\-]+.*$"""), "").trim()
                clean = clean.replace(Regex("""\s+in\s+#[0-9]+.*$"""), "").trim()
                clean = clean.replace(Regex("""\s+in\s*$"""), "").trim()

                // Loại bỏ markdown in đậm / in nghiêng
                clean = clean.replace("**", "").replace("__", "").trim()

                // Bỏ qua nếu dòng sau khi lọc chỉ còn từ khóa git/github hoặc quá ngắn
                if (clean.contains("github", ignoreCase = true) ||
                    clean.contains("compare/", ignoreCase = true) ||
                    clean.contains("commit/", ignoreCase = true) ||
                    clean.contains("pull/", ignoreCase = true)
                ) {
                    continue
                }

                // Loại bỏ tiền tố bullet cũ nếu có để xử lý nội dung chính
                if (clean.startsWith("* ") || clean.startsWith("- ") || clean.startsWith("• ")) {
                    clean = clean.substring(2).trim()
                } else if (clean.startsWith("•")) {
                    clean = clean.removePrefix("•").trim()
                }

                // Loại bỏ tiền tố commit kỹ thuật: feat(scope):, fix:, chore:, perf:, [fix], etc.
                clean = clean.replace(
                    Regex("""^(?:\[(?:feat|fix|perf|refactor|chore|style|test|build|ci)\]|(?:feat|fix|perf|refactor|chore|style|test|build|ci)(?:\([a-zA-Z0-9_\- ]+\))?:)\s*""", RegexOption.IGNORE_CASE),
                    ""
                ).trim()

                // Viết hoa chữ cái đầu tiên cho chuẩn mực ứng dụng thương mại
                if (clean.isNotEmpty() && clean[0].isLowerCase()) {
                    clean = clean.replaceFirstChar { it.uppercaseChar() }
                }

                // Chuẩn hóa dấu đầu dòng
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
            return notes.contains("Tối ưu hóa hiệu năng và độ ổn định toàn hệ thống") &&
                    notes.contains("Cải tiến giao diện trực quan và trải nghiệm người dùng")
        }
    }

    /**
     * Kiểm tra phiên bản mới nhất từ backend GitHub Release ngầm
     */
    suspend fun checkForUpdates(
        currentVersion: String = BuildConfig.VERSION_NAME
    ): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(GITHUB_LATEST_RELEASE_API)
                .header("Accept", "application/vnd.github.v3+json")
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

            val jsonObject = JsonParser.parseString(bodyString).asJsonObject
            val rawTag = jsonObject.get("tag_name")?.asString ?: ""
            val latestVersion = rawTag.removePrefix("v").removePrefix("V")
            val rawTitle = jsonObject.get("name")?.asString ?: "Bản cập nhật v$latestVersion"
            val releaseTitle = rawTitle.replace(Regex("""https?://\S+"""), "").trim().ifBlank { "Bản cập nhật v$latestVersion" }
            var releaseNotes = sanitizeReleaseNotes(jsonObject.get("body")?.asString)

            // Nếu releaseNotes rỗng hoặc chỉ có link compare khiến rơi vào fallback, tự động lấy thông tin từ commit thật của tag
            if (isFallbackReleaseNotes(releaseNotes) && rawTag.isNotBlank()) {
                val commitNotes = fetchCommitReleaseNotes(rawTag)
                if (!commitNotes.isNullOrBlank() && !isFallbackReleaseNotes(commitNotes)) {
                    releaseNotes = commitNotes
                }
            }

            // Tìm asset APK trong danh sách assets
            var apkUrl = ""
            var apkSize = 0L

            val assetsArray = jsonObject.getAsJsonArray("assets")
            if (assetsArray != null) {
                for (element in assetsArray) {
                    val asset = element.asJsonObject
                    val name = asset.get("name")?.asString ?: ""
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.get("browser_download_url")?.asString ?: ""
                        apkSize = asset.get("size")?.asLong ?: 0L
                        break
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

            // Kiểm tra quyền cài đặt từ nguồn ngoài trên Android 8.0+
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

    /**
     * Fallback đọc commit message trực tiếp từ tag của phiên bản mới nhất
     * khi release body không chứa nội dung mô tả chi tiết.
     */
    private fun fetchCommitReleaseNotes(rawTag: String): String? {
        return try {
            val commitUrl = "https://api.github.com/repos/congdu28/Weather-24h/commits/$rawTag"
            val request = Request.Builder()
                .url(commitUrl)
                .header("Accept", "application/vnd.github.v3+json")
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
