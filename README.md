# Weather 24h

Ứng dụng dự báo thời tiết trực quan, hiện đại và tinh tế dành cho Android, được phát triển hoàn toàn bằng **Kotlin** và **Jetpack Compose**.

[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=flat&logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9%2B-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack-Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Release](https://img.shields.io/github/v/release/congdu28/Weather-24h?color=0284C7&label=Release)](https://github.com/congdu28/Weather-24h/releases/latest)

---

## ✨ Tính năng nổi bật

- 🌤️ **Khí quyển sống động (Living Dynamic Atmosphere)**: Hoạt ảnh nền thời tiết theo thời gian thực (tia nắng God Rays, mây thể tích 3D, vệt mưa, bọt nước đáy, dông sét đa xung, sao băng và cực quang).
- 📈 **Dự báo 24 giờ trực quan**: Biểu đồ nhiệt độ đỏ cam, lượng mưa xanh biển và độ ẩm xanh ngọc được phân tách màu sắc riêng biệt, loại bỏ hoàn toàn nhầm lẫn.
- 📅 **Dự báo 7 ngày chi tiết**: Theo dõi biên độ nhiệt cao/thấp, xác suất mưa và chi tiết từng ngày với modal toàn màn hình.
- 🛰️ **Bản đồ Radar trực tiếp**: Quan sát hướng di chuyển của mây và lượng mưa tức thì qua trạm vệ tinh RainViewer 2D.
- 📱 **Bộ Widget màn hình chính đa dạng**:
  - **4x2 Chi tiết**: Hiển thị thời tiết hiện tại, chuỗi 6 giờ tới và gợi ý trang phục.
  - **4x1 Dự báo 5 ngày**: Thống kê xu hướng thời tiết cả tuần nhỏ gọn.
  - **2x1 Tối giản & 1x1 Mini**: Tinh gọn, hiển thị nhiệt độ và trạng thái tức thời.
  - *Tự động đổi màu Sáng/Tối theo hệ điều hành và tự động cập nhật vị trí GPS khi người dùng di chuyển.*
- 👔 **AI Smart Stylist**: Tự động gợi ý trang phục phù hợp với nhiệt độ và thời tiết thực tế.
- 🚀 **Tự động cập nhật trực tiếp (In-App OTA)**: Tự động phát hiện và tải bản phát hành mới ngay trong ứng dụng.
- ⚡ **Khởi động 0 giây & Hoạt động Ngoại tuyến**: Lưu cache đa tầng thông minh, mở app là thấy ngay thời tiết của phiên trước.

---

## 🛠️ Công nghệ & Kiến trúc

| Thành phần | Công nghệ sử dụng |
| :--- | :--- |
| **Giao diện (UI)** | Jetpack Compose, Material 3, Day/Night Theme |
| **Kiến trúc** | MVVM, Single Source of Truth, Kotlin Coroutines & StateFlow |
| **Nền tảng & Tác vụ ngầm** | WorkManager (`PeriodicWeatherWorker`), AppWidgetProvider |
| **Nguồn dữ liệu** | Open-Meteo (Mặc định không cần API key), WeatherAPI.com, RainViewer, Gemini AI |
| **Tối ưu hóa & Bảo mật** | R8 / ProGuard Code & Resource Shrinking, HTTPS-only, Safe Fallback Parsing |

---

## 📲 Tải về & Cài đặt

1. Truy cập mục [**Releases**](https://github.com/congdu28/Weather-24h/releases/latest) để tải tệp `Weather24h-v*.apk` mới nhất.
2. Mở file APK trên điện thoại Android (Android 8.0 trở lên) để tiến hành cài đặt.
3. Cấp quyền Vị trí (GPS) để ứng dụng tự động cập nhật thời tiết chính xác tại nơi bạn sinh sống.

---

## 💻 Hướng dẫn Biên dịch (Build from Source)

- **Yêu cầu**: Android Studio (Hedgehog hoặc mới hơn), JDK 17, Android SDK 34 (`minSdk 26`).
- **Build APK Debug**:
  ```bash
  ./gradlew.bat assembleDebug
  ```
  File APK tạo tại: `app/build/outputs/apk/debug/app-debug.apk`
- **Build APK Release**:
  ```bash
  ./gradlew.bat assembleRelease
  ```
- **Chạy kiểm thử Unit Tests**:
  ```bash
  ./gradlew.bat testDebugUnitTest
  ```
