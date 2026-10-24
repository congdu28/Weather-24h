# ============================================================================
# Weather 24h - ProGuard & R8 Hardened Anti-Reverse-Engineering Rules
# ============================================================================

# 1. Tối ưu hóa sâu & Gom gói chống dịch ngược (Anti-Decompilation)
-repackageclasses 'o'
-allowaccessmodification
-overloadaggressively
-renamesourcefileattribute SourceFile

# Chỉ giữ các thuộc tính bắt buộc cho Gson generic & Kotlin reflection cơ bản
# (Đã loại bỏ SourceFile, LineNumberTable để ẩn toàn bộ tên file .kt gốc)
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Loại bỏ hoàn toàn Log debug và chuỗi tên biến Kotlin Intrinsics khỏi bản Release APK
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNullParameter(java.lang.Object, java.lang.String);
    public static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
    public static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
}

# 2. Gson & Data Models
# Chỉ giữ tên trường (<fields>) và hàm khởi tạo (<init>) của các model được serialize JSON vào SharedPreferences.
# Toàn bộ các hàm logic nghiệp vụ (getCondition, detectAlerts, calculate...) sẽ được R8 làm rối tên (obfuscate).
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers class * extends com.google.gson.reflect.TypeToken { *; }
-dontwarn sun.misc.**

-keep @androidx.annotation.Keep class * { *; }

-keep class com.weather24h.app.data.model.WeatherData,
            com.weather24h.app.data.model.CurrentWeather,
            com.weather24h.app.data.model.HourlyForecast,
            com.weather24h.app.data.model.DailyForecast,
            com.weather24h.app.data.model.AirQuality,
            com.weather24h.app.data.model.CityLocation,
            com.weather24h.app.data.model.CityWeatherSummary,
            com.weather24h.app.data.model.WeatherCondition {
    <fields>;
    <init>(...);
}

-keepclassmembers enum com.weather24h.app.data.model.** {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 3. WorkManager Workers (Chỉ giữ hàm khởi tạo để hệ thống gọi, làm rối nội dung bên trong)
-keep class com.weather24h.app.util.PeriodicWeatherWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# 4. Android Entry Points (Activity, AppWidgetProvider, FileProvider)
# Chỉ giữ constructor mặc định; toàn bộ hàm nội bộ được R8 làm rối và inline
-keep class com.weather24h.app.MainActivity {
    public <init>();
}
-keep class com.weather24h.app.ui.widget.** extends android.appwidget.AppWidgetProvider {
    public <init>();
}
-keep class androidx.core.content.FileProvider { *; }

# 5. Kotlin Coroutines & Networking
-dontwarn kotlinx.coroutines.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn com.google.android.gms.**
-dontwarn androidx.compose.**

# 6. AndroidX Lifecycle & ViewModel
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    public <init>(android.app.Application);
}
