package com.weather24h.app.data.model

import java.util.Locale

data class AppUpdateInfo(
    val latestVersionName: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val apkSize: Long,
    val isUpdateAvailable: Boolean
) {
    val formattedApkSize: String
        get() {
            if (apkSize <= 0) return ""
            val mb = apkSize / (1024.0 * 1024.0)
            return String.format(Locale.US, "%.2f MB", mb)
        }
}
