# ☀️ Weather 24h - Ứng dụng Thời tiết Android Native (Kotlin + Jetpack Compose)

**Weather 24h** là ứng dụng thời tiết thuần Android (Native 100%) được xây dựng bằng **Kotlin** và bộ giao diện hiện đại **Jetpack Compose (Material 3)**. Ứng dụng nổi bật với hiệu ứng **Glassmorphism**, biểu đồ nhiệt độ 24 giờ uốn lượn mượt mà và tính năng **AI Stylist gợi ý mặc đồ** theo thời tiết tích hợp Google Gemini AI.

---

## 🌟 Tính năng chính

1. **⏱️ Dự báo 24 giờ (Hourly Forecast & Temperature Curve)**:
   - Danh sách cuộn ngang 24 giờ liên tục với mốc giờ, icon thời tiết, tỷ lệ mưa (%) và tốc độ gió.
   - **Biểu đồ nhiệt độ Bezier (TemperatureChart)**: Vẽ đường cong nhiệt độ tăng giảm trong ngày mượt mà trên Canvas ở tần số quét cao (60/120fps).

2. **📅 Dự báo 7 ngày tới (7-Day Forecast)**:
   - Dự báo từng ngày trong tuần kèm icon và xác suất mưa.
   - **Thanh đo nhiệt độ Min - Max trực quan**: Thể hiện dải nhiệt độ trong ngày bằng thanh dải màu gradient từ xanh mát đến đỏ ấm.

3. **📊 Bộ 8 chỉ số thời tiết chi tiết (Metrics Grid)**:
   - **Chỉ số UV (Tia cực tím)** kèm cấp độ cảnh báo (An toàn, Cần bôi kem, Cực độ).
   - **Độ ẩm không khí (%)** & cảm giác độ ẩm.
   - **Tốc độ & Hướng gió** (km/h, hướng Bắc, Đông Nam, Tây Bắc...).
   - **Nhiệt độ cảm nhận thực tế (Feels Like)**.
   - **Chất lượng không khí (AQI: PM2.5 & PM10)**.
   - **Giờ mặt trời mọc & lặn**.
   - **Áp suất khí quyển (hPa)** & **Tầm nhìn xa (km)**.

4. **👗 AI Stylist - Gợi ý mặc đồ thông minh (Google Gemini AI)**:
   - Tự động phân tích các thông số thời tiết để gợi ý: **Áo**, **Quần/Váy**, **Giày dép**.
   - **Gợi ý phụ kiện thiết yếu**: *Dù gấp, áo mưa, kính râm UV, kem chống nắng SPF 50+, áo khoác gió...*
   - Cảnh báo thời tiết xấu (trời sắp mưa, nắng gắt trưa).
   - Tích hợp **Google Gemini 1.5 Flash API** + **Bộ Rule Engine chuyên gia dự phòng** (hoạt động tốt ngay cả khi offline hoặc chưa nhập API Key).

5. **🎨 Giao diện & Định vị**:
   - **Dynamic Sky Gradient**: Nền trời đổi màu sống động theo thời tiết và thời gian (Sáng sớm, Nắng trưa, Hoàng hôn, Đêm sao, Mưa dông).
   - **Định vị GPS tự động**: Hỗ trợ định vị vị trí hiện tại qua GPS hoặc tìm kiếm mọi thành phố trên thế giới.

---

## 🏗️ Cấu trúc dự án Kotlin (Clean Architecture & MVVM)

```
Weather 24h/
├── .github/
│   └── workflows/
│       └── build-apk.yml               # Tự động build và release APK lên GitHub Releases
├── .gitignore                          # Cấu hình ignore cho Android
├── build.gradle.kts                    # Cấu hình root Gradle
├── settings.gradle.kts                 # Khai báo repository và module :app
├── gradle.properties                   # Cấu hình bộ nhớ JVM và AndroidX
├── gradlew & gradlew.bat               # Gradle wrapper executable
├── gradle/wrapper/                     # Gradle wrapper binary & config
└── app/
    ├── build.gradle.kts                # Cấu hình module Android, Compose & Dependencies
    ├── proguard-rules.pro              # Quy tắc bảo vệ code khi build release
    └── src/main/
        ├── AndroidManifest.xml         # Khai báo quyền Internet & GPS
        ├── res/                        # Resources (strings, themes)
        └── java/com/weather24h/app/
            ├── MainActivity.kt         # Khởi chạy Compose & yêu cầu quyền GPS
            ├── data/
            │   ├── model/
            │   │   ├── CityLocation.kt           # Tọa độ, địa danh, danh sách gợi ý
            │   │   ├── WeatherCondition.kt       # Ánh xạ mã WMO sang tiếng Việt & kiểu thời tiết
            │   │   ├── WeatherData.kt            # Current, Hourly, Daily, AQI
            │   │   └── OutfitRecommendation.kt   # Mô hình gợi ý trang phục AI
            │   ├── remote/
            │   │   ├── WeatherApiService.kt      # Open-Meteo REST API (không cần API key)
            │   │   └── GeminiStylistService.kt   # Gemini 1.5 Flash REST API + Rule Engine
            │   └── repository/
            │       └── WeatherRepository.kt      # Quản lý kho dữ liệu thời tiết
            └── ui/
                ├── theme/                        # Màu sắc, Gradient bầu trời, Typography
                ├── components/
                │   ├── CurrentWeatherCard.kt     # Thẻ hiển thị nhiệt độ chính
                │   ├── TemperatureChart.kt       # Biểu đồ vẽ đường cong nhiệt độ Canvas
                │   ├── HourlyForecastSection.kt  # Khung cuộn 24h kết hợp biểu đồ
                │   ├── DailyForecastSection.kt   # Khung dự báo 7 ngày với thanh nhiệt độ
                │   ├── WeatherMetricsGrid.kt     # Lưới 8 chỉ số chi tiết
                │   ├── OutfitStylistCard.kt      # Thẻ gợi ý mặc đồ AI
                │   ├── CitySearchDialog.kt       # Hộp thoại tìm kiếm thành phố
                │   └── GeminiApiKeyDialog.kt     # Hộp thoại cài đặt API Key Gemini
                └── screens/
                    ├── WeatherViewModel.kt       # StateFlow, MVVM xử lý logic
                    └── WeatherScreen.kt          # Màn hình chính lắp ghép toàn bộ UI
```

---

## 🚀 Đẩy code lên Git & Tự động Build Release APK

Dự án đã được tích hợp sẵn file `.github/workflows/build-apk.yml`. Khi bạn đẩy code lên GitHub, GitHub Actions sẽ **tự động build file APK** cho bạn hoàn toàn miễn phí trên Cloud mà không cần cấu hình Android SDK nặng máy!

### 1. Khởi tạo Git & Đẩy code lên GitHub
Mở Terminal/PowerShell tại thư mục `Weather 24h`:

```powershell
# 1. Khởi tạo kho Git (nếu chưa có)
git init

# 2. Thêm toàn bộ mã nguồn vào commit
git add .
git commit -m "feat: initial commit Weather 24h Android Kotlin Jetpack Compose"

# 3. Liên kết với repository GitHub của bạn (thay URL bên dưới bằng repo của bạn)
git remote add origin https://github.com/<YOUR_USERNAME>/<YOUR_REPO_NAME>.git

# 4. Đẩy mã nguồn lên branch main
git branch -M main
git push -u origin main
```

### 2. Tự động Release ra file APK cài đặt
* **Cách 1: Tải trực tiếp từ GitHub Actions**:
  - Khi bạn vừa `git push`, tab **Actions** trên GitHub sẽ tự động chạy workflow `Build & Release Android APK`.
  - Khi quá trình hoàn tất (khoảng 2-3 phút), bạn chỉ cần vào run đó và tải file artifact **`Weather24h-Release-APK`** về máy.
* **Cách 2: Tạo Release Tag chính thức**:
  - Đánh tag phiên bản và push lên GitHub:
    ```powershell
    git tag v1.0.0
    git push origin v1.0.0
    ```
  - GitHub Actions sẽ tự động tạo một trang **Release** chính thức tại mục *Releases* của repo, đính kèm sẵn file **`Weather24h-v1.0.0.apk`** để bất kỳ ai cũng có thể tải về cài đặt trực tiếp lên điện thoại Android!

### 3. Build APK trực tiếp trên máy tính (Local)
Nếu máy bạn có cài đặt JDK 17:
```powershell
./gradlew assembleRelease
```
File APK sẽ xuất hiện tại:
`app/build/outputs/apk/release/app-release.apk`
