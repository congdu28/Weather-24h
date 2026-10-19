# ============================================================================
# Weather 24h - ProGuard & R8 Optimization Rules
# ============================================================================

# 1. Attributes & Annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes SourceFile,LineNumberTable

# 2. Gson & Data Models (QUAN TRỌNG: Bảo vệ toàn bộ Model dữ liệu và Cache)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers class * extends com.google.gson.reflect.TypeToken { *; }
-dontwarn sun.misc.**

# Giữ nguyên toàn bộ cấu trúc và trường của các Model để tránh lỗi giải mã JSON
-keep @androidx.annotation.Keep class * { *; }
-keep class com.weather24h.app.data.model.** { *; }
-keepclassmembers class com.weather24h.app.data.model.** { *; }
-keepclassmembers enum com.weather24h.app.data.model.** { *; }

# 3. WorkManager Workers
-keep class com.weather24h.app.util.PeriodicWeatherWorker { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# 4. AppWidget Providers & Receivers
-keep class com.weather24h.app.ui.widget.** extends android.appwidget.AppWidgetProvider { *; }
-keep class com.weather24h.app.MainActivity { *; }
-keep class com.weather24h.app.util.** { *; }
-keep class androidx.core.content.FileProvider { *; }

# 5. Kotlin Coroutines & Reflection
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# 6. OkHttp & Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# 7. Google Play Services Location
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.**

# 8. AndroidX Lifecycle & ViewModel
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel {
    public <init>(android.app.Application);
}

# 9. Jetpack Compose & UI
-dontwarn androidx.compose.**
