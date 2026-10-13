# Weather 24h

Ứng dụng thời tiết Android viết bằng Kotlin và Jetpack Compose. Màn hình chính hiển thị thời tiết hiện tại, dự báo theo giờ và theo ngày, chất lượng không khí, các chỉ số thời tiết, cảnh báo, gợi ý trang phục và widget màn hình chính.

## Logic hiện tại

1. `MainActivity` khởi tạo giao diện, xin quyền thông báo và chỉ lấy GPS lúc mở app nếu người dùng bật tùy chọn khởi động bằng GPS.
2. `WeatherViewModel` đọc thành phố dùng gần nhất (hoặc thành phố mặc định), tải thời tiết và phát trạng thái `Loading` / `Success` / `Error` qua `StateFlow`. Đổi thành phố hoặc làm mới sẽ hủy tác vụ tải cũ. Kéo để làm mới giữ dữ liệu cũ trên màn hình trong lúc tải.
3. Từ v1.0.26, `WeatherRepository` mặc định **Open-Meteo → Best Match** theo tọa độ, lấy dữ liệu hiện tại và 7 ngày dự báo, không cần API key. Khi nâng cấp, cấu hình này được áp dụng một lần; lựa chọn thủ công sau đó được giữ nguyên. WeatherAPI.com vẫn là nguồn tùy chọn: cung cấp hiện tại và 3 ngày đầu, Open-Meteo bổ sung các ngày còn lại.
4. Sau khi tải, repository lưu dữ liệu bằng `SharedPreferences`, cập nhật widget và kiểm tra cảnh báo nếu đã bật. Nếu cả hai nguồn lỗi, app chỉ dùng cache của cùng vị trí và đánh dấu `isOffline`; nếu không có cache phù hợp, app báo lỗi. Dữ liệu mẫu cũ không còn được dùng.
5. Gợi ý trang phục được tải sau thời tiết: ưu tiên Gemini khi có API key và phản hồi hợp lệ, sau đó dùng bộ quy tắc nội bộ. App cũng có tìm kiếm thành phố, GPS, danh sách yêu thích, giao diện và cài đặt widget.
6. `PeriodicWeatherWorker` đồng bộ thời tiết cho widget và thông báo bằng WorkManager khi các tùy chọn tương ứng được bật. Thời gian tự làm mới khi đang mở app được quản lý riêng trong `WeatherViewModel`.
7. Thẻ sức khỏe dùng `LocalCardGlassBg` và `LocalCardOpacity` chung, không đặt mức trong suốt tối thiểu riêng. Giờ cập nhật nằm giữa thẻ thời tiết hiện tại; nguồn hiển thị bằng chữ nhỏ ở cuối trang. UV hiện tại và tầm nhìn được đọc từ phản hồi Open-Meteo khi có dữ liệu.

## Quy tắc hiển thị mưa

- Trạng thái hiện tại giữ mô tả của nguồn; mã WeatherAPI chỉ mưa/dông quanh khu vực được ghi rõ, không khẳng định đang mưa tại vị trí người dùng.
- Xác suất mưa và lượng mưa là hai chỉ số riêng. App không đổi biểu tượng trời quang thành mưa dựa trên xác suất, không tự suy ra phần trăm từ mm. Giá trị xác suất thiếu dùng `-1` và hiển thị `—`.
- Gợi ý mưa lớn dựa trên mã cường độ hoặc lượng mưa theo giờ từ 5 mm; đây là quy tắc gợi ý tại tọa độ, không phải cảnh báo diện rộng của cơ quan khí tượng.
- Workflow chạy `testDebugUnitTest` trước khi tạo APK. Test tái hiện trường hợp mã `1063`, xác suất cao với mưa nhẹ, dữ liệu thiếu và độ ẩm 77%.

## Cấu trúc chính

| Đường dẫn | Vai trò |
| --- | --- |
| `app/src/main/java/com/weather24h/app/MainActivity.kt` | Điểm vào, quyền vị trí và thông báo |
| `ui/screens/WeatherViewModel.kt` | Trạng thái màn hình, tải và làm mới dữ liệu |
| `ui/screens/WeatherScreen.kt` | Giao diện chính |
| `data/repository/WeatherRepository.kt` | Chọn nguồn, dự phòng, cache và cài đặt |
| `data/remote/` | WeatherAPI.com, Open-Meteo, Gemini; mã Google Weather cũ chưa được gọi từ giao diện |
| `data/model/` | Mô hình dữ liệu và quy tắc cảnh báo |
| `ui/widget/` | Widget thường và widget nhỏ |
| `util/PeriodicWeatherWorker.kt` | Đồng bộ nền |

## Chạy dự án

- Mở thư mục bằng Android Studio với JDK 17 và Android SDK 34. App hỗ trợ Android 8.0 trở lên (`minSdk 26`).
- Đồng bộ Gradle rồi chạy module `app` trên máy ảo hoặc thiết bị Android.
- Build APK trên Windows: `./gradlew.bat assembleDebug`. APK nằm trong `app/build/outputs/apk/debug/`.
- Mặc định Open-Meteo không cần key. Nếu chọn WeatherAPI.com, cấu hình `WEATHER_API_KEY` trong `local.properties`, biến môi trường hoặc Gradle property, hoặc nhập key cá nhân trong cài đặt app. `GEMINI_API_KEY` dành cho gợi ý trang phục AI. Không đưa key cá nhân vào commit.

## Phát hành

Workflow `.github/workflows/build-apk.yml` build APK release khi đẩy lên `main`, đẩy tag `v*` hoặc chạy thủ công. Bản build được tải ở **Actions → Artifacts**; với tag hoặc chạy thủ công, workflow còn tạo GitHub Release.

## Điểm cần lưu ý khi phát triển tiếp

- Key WeatherAPI.com trước đây từng nằm trong mã nguồn; cần thay key đó ở WeatherAPI.com trước khi phát hành rộng rãi. Key đưa vào `BuildConfig` vẫn có thể bị trích xuất từ APK; nếu cần giữ key bí mật, phải gọi WeatherAPI.com qua backend riêng.
- Keystore phát hành và mật khẩu hiện còn trong dự án. Cần thay và quản lý riêng trước khi phân phối công khai.
