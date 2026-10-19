# Weather 24h

Ứng dụng thời tiết Android viết bằng Kotlin và Jetpack Compose. Màn hình chính hiển thị thời tiết hiện tại, dự báo theo giờ và theo ngày, chất lượng không khí, các chỉ số thời tiết, cảnh báo, gợi ý trang phục và widget màn hình chính.

## Logic hiện tại

1. `MainActivity` khởi tạo giao diện; bản cài mới mặc định xin vị trí hiện tại và ưu tiên một phép đo GPS mới, chính xác cao. App chỉ dùng tọa độ gần nhất khi phép đo mới thất bại và dữ liệu cũ chưa quá 30 phút. Nếu người dùng từ chối quyền, app mới tải thành phố dự phòng đã lưu.
2. `WeatherViewModel` đọc thành phố dùng gần nhất (hoặc thành phố mặc định), tải thời tiết và phát trạng thái `Loading` / `Success` / `Error` qua `StateFlow`. Đổi thành phố hoặc làm mới sẽ hủy tác vụ tải cũ. Kéo để làm mới giữ dữ liệu cũ trên màn hình trong lúc tải.
3. Từ v1.0.26, `WeatherRepository` mặc định **Open-Meteo → Best Match** theo tọa độ, lấy dữ liệu hiện tại và 7 ngày dự báo, không cần API key. Khi nâng cấp, cấu hình này được áp dụng một lần; lựa chọn thủ công sau đó được giữ nguyên. WeatherAPI.com vẫn là nguồn tùy chọn: cung cấp hiện tại và 3 ngày đầu, Open-Meteo bổ sung các ngày còn lại.
4. Sau khi tải, repository lưu dữ liệu bằng `SharedPreferences`, cập nhật widget và kiểm tra cảnh báo nếu đã bật. Nếu cả hai nguồn lỗi, app chỉ dùng cache của cùng vị trí và đánh dấu `isOffline`; nếu không có cache phù hợp, app báo lỗi. Dữ liệu mẫu cũ không còn được dùng.
5. Gợi ý trang phục được tải sau thời tiết: ưu tiên Gemini khi có API key và phản hồi hợp lệ, sau đó dùng bộ quy tắc nội bộ. App cũng có tìm kiếm thành phố, GPS, danh sách yêu thích, giao diện và cài đặt widget.
6. `PeriodicWeatherWorker` đồng bộ thời tiết cho widget và thông báo bằng WorkManager khi các tùy chọn tương ứng được bật. Thời gian tự làm mới khi đang mở app được quản lý riêng trong `WeatherViewModel`.
7. Thẻ sức khỏe dùng `LocalCardGlassBg` và `LocalCardOpacity` chung, không đặt mức trong suốt tối thiểu riêng. Giờ cập nhật nằm giữa thẻ thời tiết hiện tại; nguồn hiển thị bằng chữ nhỏ ở cuối trang. UV hiện tại và tầm nhìn được đọc từ phản hồi Open-Meteo khi có dữ liệu.
8. Từ v1.0.28, app nâng cấp động cơ nền động thời tiết thời gian thực (Living Dynamic Weather Atmosphere): bọt nước bắn và gợn sóng khi mưa, dông sét đa xung phân nhánh, chùm tia nắng God Rays và hạt bụi nắng lơ lửng, cực quang và sao đêm 4 cánh; bổ sung biểu đồ kép tương tác 24h (Nhiệt độ + Cột lượng mưa + Tương tác chọn giờ); mở rộng tùy biến Widget với 4 phong cách thị giác (Kính mờ, Tối giản, Đổi màu theo thời tiết, Đen OLED) cùng khung xem trước trực tiếp (Live Widget Preview) trong Cài đặt.
9. Từ v1.0.29, app giải quyết triệt để sự cố WebGL trên bản đồ Windy: cấu hình WebChromeClient chặn hoàn toàn JavaScript `alert()` làm gián đoạn người dùng; tự động chuyển đổi dự phòng thông minh (auto-fallback) sang bản đồ trạm radar 2D RainViewer; tối ưu tăng tốc phần cứng `hardwareAccelerated="true"` và nhúng Windy v2; đồng thời nâng cấp toàn diện mục Địa điểm yêu thích thành danh sách thẻ chuyên nghiệp với nhiệt độ thời gian thực, huy hiệu "Đang xem" và thanh chọn nhanh 1 chạm (FavoriteCitiesQuickBar) trên màn hình chính.
10. Từ v1.0.30, app tối ưu hóa tốc độ mở ứng dụng tức thì 0 giây với cơ chế khởi động từ bộ nhớ đệm (Stale-While-Revalidate): hiển thị ngay thời tiết đã lưu từ phiên trước và thông báo nổi "Đang cập nhật thời tiết mới nhất..." trong lúc GPS/API tải ngầm; tinh chỉnh toàn diện responsive chống tràn chữ ở thẻ Radar Khí Tượng và hộp thoại "Chọn địa điểm" (không bị vỡ chữ "Đang xem", "Định vị", thanh tìm kiếm vừa vặn trên mọi kích thước màn hình).
11. Từ v1.0.31, app cập nhật toàn diện bộ nhận diện thương hiệu với biểu tượng logo chính thức mới: đồng bộ icon launcher đa mật độ điểm ảnh (mipmap mdpi đến xxxhdpi), adaptive icon chuẩn Android 8+, biểu tượng thương hiệu trên thanh TopBar và mục Giới thiệu trong Cài đặt; thiết kế biểu tượng thông báo thanh trạng thái monochrome `ic_stat_weather` cùng `largeIcon` màu sắc nhận diện thương hiệu chuẩn xác; chuẩn hóa toàn diện độ tương phản màu sắc và độ dày phông chữ (Typography Hierarchy) trên cả 2 giao diện Sáng & Tối (khắc phục chữ mờ nhạt ở xác suất mưa 0%, dấu chấm phân cách mờ, căn chỉnh kích thước nhãn thời gian và cấp độ thang đo AQI sắc nét).
12. Từ v1.0.32, app nâng cấp gói tối ưu hóa trải nghiệm toàn diện 4 trong 1:
    - **Hiệu năng & Nền động**: Chuyển cảnh màu nền bầu trời mượt mà (`rememberAnimatedSkyGradient` 850ms); bổ sung tùy chọn 3 chế độ hiệu năng nền động trong Cài đặt (`AtmospherePerformanceMode`: Tự động tối ưu, Chất lượng cao nhất, Tiết kiệm pin tối đa).
    - **Mở rộng & Tùy biến Widget**: Bổ sung 2 loại widget mới gồm Widget Dự báo 5 ngày (4x1 / 4x2) và Mini Widget khí tượng (1x1); nâng cấp bộ xem trước trực quan 4 loại widget trong Cài đặt với đồng bộ làm mới dữ liệu tức thì.
    - **Bản đồ Radar Khí Tượng**: Bộ chuyển đổi nhanh trực tiếp giữa Windy (WebGL) và RainViewer (Trạm 2D) ngay trên thẻ; xóa bỏ hoàn toàn hiện tượng nháy trắng khi khởi tạo WebView với nền tối sâu `#020617` và lớp phủ placeholder mượt mà; tự động xử lý sự cố tiến trình render WebView và WebGL để chuyển đổi dự phòng an toàn.
    - **Tải ngầm địa điểm yêu thích (Instant Switch 0s)**: Bộ nhớ đệm đa luồng `ConcurrentHashMap` lưu riêng từng thành phố; pre-fetch toàn bộ dữ liệu thời tiết khi đồng bộ danh sách yêu thích giúp chuyển đổi thành phố 0 giây tức thì; tích hợp danh mục 23 tỉnh/thành phố và điểm du lịch trọng điểm Việt Nam cùng thuật toán tìm kiếm không dấu thông minh (`normalizeVietnamese`).
13. Từ v1.0.33, app nâng cấp toàn diện nền thời tiết động khí quyển sống động và chuẩn hóa cài đặt mặc định:
    - **Nền động hoạt động liên tục khi cuộn**: Giữ nguyên toàn bộ chuyển động nền động (vòng xoay tia nắng God Rays, vệt mưa, bọt nước tung tóe, gợn sóng đáy, hạt bụi nắng, tuyết rơi và sao băng) hoạt động liên tục 100% mượt mà 60/120 FPS khi người dùng vuốt/cuộn màn hình, không bị khựng đơ hay cắt giảm hạt đột ngột.
    - **Chuẩn hóa cài đặt mặc định mới cài**: Giao diện ứng dụng tự động theo hệ thống (`AppThemeMode.SYSTEM`); độ tối nền đặt sáng gốc 0% (`bgDimmingLevel = 0.0f`); độ mờ khối thời tiết màn hình chính kính mờ trong suốt 25% (`cardOpacity = 0.25f`) giúp nhìn thấu nền trời sống động; ánh sáng Ambient Light & Gradient tự nhiên cường độ 100% (`ambientGlowIntensity = 1.0f`).
    - **Nâng cấp nền thời tiết thực sống động & trực quan toàn app**:
        - *Trời nắng & có mây*: Đĩa mặt trời rực rỡ với vầng quang 3 tầng, chùm tia thể tích xoay liên tục, hiệu ứng lóa ống kính điện ảnh (cinematic lens flare rings) và hạt bụi nắng ấm áp bồng bềnh; cụm mây tích thể tích 3D viền bạc đón sáng mặt trời trôi bồng bềnh cho ngày có mây.
        - *Đêm sao & trăng sáng*: Vầng trăng phát quang với chi tiết bề mặt tinh tế (Lunar Maria), dải lụa cực quang Aurora huyền ảo, bầu trời sao đa tầng thị sai lấp lánh tia sao 4 cánh và vệt sao băng rực rỡ lướt qua.
        - *Dông bão & sấm sét*: Mây giông tích điện tầng đỉnh, chớp giật đa xung thấu quang mây, tia sét phân nhánh đa hướng sắc nét với lõi trắng tím và hào quang cyan/tím, mưa xối xả tốc độ cao kèm bọt nước tung tóe và hơi nước bốc lên.
        - *Mưa & Mưa phùn*: Lớp mây mưa xám phủ đỉnh trời, vệt mưa nghiêng theo gió đa tầng thị sai, bọt nước bắn và sóng lăn tăn ở đáy.
        - *Nhiều mây u ám & Sương mù*: 4 tầng mây thể tích cuồn cuộn trôi nhịp nhàng; dải sương mù dập dềnh uốn sóng ngang mềm mại.
        - *Tuyết rơi lãng mạn*: Bông tuyết 3 tầng kích thước đung đưa lượn sóng theo gió cùng lớp sương giá mùa đông.
        - *Hệ màu bầu trời chuyển tiếp*: Nâng cấp bảng màu gradient sâu thẳm, rực rỡ và chân thực cho từng trạng thái khí quyển cả ngày và đêm.
14. Từ v1.0.34, app nâng cấp cơ chế tự động định vị GPS thời gian thực cho Widget màn hình chính (Real-time GPS Location Auto-Detection):
    - **Cập nhật theo vị trí thực tế hiện tại**: Khắc phục triệt để vấn đề widget lấy lại thành phố cũ của phiên trước. Khi người dùng di chuyển (ví dụ 1 tiếng trước ở Thanh Hóa, hiện tại đã đến Hà Nội), bấm nút làm mới trên bất kỳ Widget nào sẽ kích hoạt `LocationHelper` lấy tọa độ GPS thực tế của thiết bị, phân giải ngược địa chỉ (reverse geocoding) sang thành phố mới, tải thời tiết mới nhất và đồng bộ ngay lập tức cho toàn bộ các Widget (4x2, 2x1, 4x1, 1x1).
    - **Chạm Widget mở ứng dụng tự động đồng bộ vị trí mới**: Chạm vào thân bất kỳ Widget nào để mở app sẽ gửi `EXTRA_FORCE_GPS_REFRESH`, kích hoạt `MainActivity` (cả cold-start lẫn warm-start qua `onNewIntent` với `launchMode="singleTask"`) kiểm tra và định vị ngay lập tức vị trí GPS hiện tại, cập nhật màn hình chính sang thành phố mới mà không cần thao tác thủ công.
    - **Đồng bộ ngầm thông minh**: `PeriodicWeatherWorker` và chu kỳ `onUpdate` của Widget tự động kiểm tra vị trí GPS ngầm để phát hiện khi người dùng di chuyển sang tỉnh/thành phố khác, tự động chuyển vùng thời tiết và cập nhật thông báo/widget một cách liền mạch và tiết kiệm pin.
    - **Dự phòng an toàn đa tầng**: Tự động ưu tiên `FusedLocationProviderClient` (High Accuracy / Balanced), có dự phòng nhiều tầng qua `lastLocation` và `LocationManager` gốc của hệ thống Android; nếu máy tắt GPS hoặc chưa cấp quyền vị trí, hệ thống tự động dự phòng về thành phố gần nhất mà không gây gián đoạn hay lỗi crash.
15. Từ v1.0.35, app khắc phục triệt để spam thông báo và tối ưu cuộn 120 FPS:
    - **Chống spam thông báo**: Giãn cách chu kỳ tối thiểu (mặc định 2h, trừ dung sai hệ thống), cooldown 4h cho cảnh báo khẩn cấp, `setOnlyAlertOnce(true)` để cập nhật êm dịu không rung chuông, `PeriodicWeatherWorker` không dùng `Result.retry()` khi gặp lỗi mạng tạm thời.
    - **Mượt mà 120 FPS**: Gán `key` và `contentType` cho các mục `LazyColumn` trong `WeatherScreen`, bổ sung huy hiệu kéo làm mới tuỳ chỉnh `PullRefreshBadge`.
16. Từ v1.0.36, app tinh chỉnh độ chính xác hiển thị thời tiết:
    - Hài hòa xác suất mưa cao với mô tả thời tiết (tự động chuyển thành "Nắng gián đoạn, có lúc mưa rào" hoặc "Nắng xen mây, có thể có mưa").
    - Khắc phục cảnh báo sai lệch ban đêm: cảnh báo tia UV và nắng nóng gay gắt chỉ kích hoạt khi trời sáng (`current.isDay == true`).
    - Cố định cụm thông tin nhiệt độ cảm giác thật / cao / thấp trên 1 dòng duy nhất chống vỡ chữ trên màn hình nhỏ.
17. Từ v1.0.37, app nâng cấp thẩm mỹ viền thẻ siêu mảnh và tối ưu CI Build:
    - Giảm độ dày đường viền thẻ từ `1.dp` xuống `0.5.dp` trên toàn bộ các thành phần giao diện, giảm alpha của `LocalCardBorder` hòa quyện nhẹ nhàng với nền kính mờ.
    - Rút gọn mô tả phần giới thiệu ứng dụng trong Cài đặt.
18. Từ v1.0.38, app tăng cường toàn diện bảo mật và tối ưu hóa phân phối diện rộng:
    - **Kích hoạt R8 / ProGuard & Resource Shrinking**: Bật `isMinifyEnabled = true` và `isShrinkResources = true` cho bản phát hành Release. Giảm mạnh 30% - 50% dung lượng file APK, làm rối mã nguồn chống dịch ngược và loại bỏ tài nguyên dư thừa.
    - **Bộ quy tắc ProGuard chuẩn hóa**: Bảo vệ an toàn tuyệt đối các model dữ liệu Gson (`com.weather24h.app.data.model.**`), WorkManager workers, AppWidget providers, OkHttp, Coroutines và AndroidX Lifecycle.
    - **Network Security Config (HTTPS-only)**: Chặn hoàn toàn lưu lượng HTTP không mã hóa (`cleartextTrafficPermitted="false"`), bảo vệ an toàn kết nối dữ liệu trước các cuộc tấn công MitM trên mạng WiFi công cộng.
    - **Bảo mật WebView Radar**: Khóa `allowFileAccess = false`, `allowContentAccess = false` và `mixedContentMode = MIXED_CONTENT_NEVER_ALLOW`, ngăn chặn nguy cơ rò rỉ tệp cục bộ và mã độc chéo.
    - **Chống rò rỉ dữ liệu qua ADB Backup**: Đặt `android:allowBackup="false"` bảo vệ an toàn thông tin vị trí và khóa API cá nhân của người dùng.
    - **Bảo vệ khóa API & chống cạn kiệt Quota**: Loại bỏ khóa nhúng tĩnh, ưu tiên thuật toán Stylist nội bộ chạy ngoại tuyến 100% không tốn chi phí và tốc độ 0s; bổ sung throttle chống spam intent trên Widget.
19. Từ v1.0.39, app bổ sung tính năng "Chia sẻ thời tiết" (Weather Share Sheet) và lan tỏa ứng dụng:
    - **Chia sẻ dự báo thời tiết 1 chạm**: Nút Chia sẻ mới được bố trí tiện lợi ngay trên thanh TopBar và thẻ Thời tiết chính `CurrentWeatherCard`. Tự động tạo bản tin tóm tắt thời tiết đầy đủ (nhiệt độ, cảm giác thật, biên độ nhiệt, xác suất mưa, độ ẩm, chất lượng không khí AQI và gợi ý trang phục) mở trực tiếp Android Share Sheet để gửi qua Zalo, Messenger, Telegram, Facebook hoặc SMS.
    - **Chia sẻ ứng dụng cho bạn bè**: Bổ sung tùy chọn chia sẻ link ứng dụng kèm lời giới thiệu tính năng trong danh mục Cài đặt chính và màn hình Giới thiệu (About), giúp người dùng dễ dàng gửi link tải bản APK mới nhất cho người thân và bạn bè.
    - **Tiện ích tập trung `WeatherShareHelper`**: Quản lý logic soạn thảo thông điệp và kích hoạt `Intent.ACTION_SEND` an toàn, chuyên nghiệp.
20. Từ v1.0.40, app khắc phục triệt để lỗi biên dịch trên CI và củng cố độ ổn định:
    - **Sửa lỗi tham chiếu thuộc tính AirQuality**: Chuẩn hóa gọi `aqiIndex` và `statusText` trong `WeatherShareHelper`, bảo đảm `./gradlew testDebugUnitTest` và `assembleRelease` chạy thành công 100%.
    - **Kiểm thử đơn vị tự động**: Bổ sung bộ test `WeatherShareHelperTest` xác thực độ chính xác của chuỗi chia sẻ dự báo thời tiết và chia sẻ ứng dụng.
    - **Mô đun hóa & ProGuard**: Tách hàm tạo văn bản chia sẻ (`buildForecastShareText`, `buildAppShareText`) giúp kiểm thử độc lập và cập nhật bộ quy tắc ProGuard R8 bảo vệ toàn diện các lớp tiện ích `util`.
21. Từ v1.0.41, app tích hợp tính năng tự động cập nhật trực tiếp trong ứng dụng (In-App Self-Update) và bảo vệ an toàn danh tính repo:
    - **Tự động cập nhật trực tiếp (In-App Self-Update / OTA)**: Cho phép người dùng kiểm tra phiên bản mới và tải APK ngầm trực tiếp trong app qua `AppUpdateManager` mà không cần mở trình duyệt web hay truy cập GitHub. Tích hợp thanh tiến trình tải trực quan và kích hoạt trình cài đặt hệ thống Android (`FileProvider` và `PackageInstaller`) nâng cấp đè ứng dụng mà không làm mất dữ liệu.
    - **Gỡ bỏ URL GitHub khỏi tính năng chia sẻ**: Xóa bỏ toàn bộ liên kết repo khỏi nội dung chia sẻ thời tiết và chia sẻ ứng dụng, bảo vệ an toàn tuyệt đối mã nguồn và quyền riêng tư cá nhân.
    - **Kiểm thử đơn vị tự động `AppUpdateManagerTest`**: Kiểm thử toàn diện thuật toán so sánh phiên bản semver (`isNewerVersion`).
22. Từ v1.0.42, app tinh chỉnh giao diện hòa quyện nền và nâng cấp hoạt ảnh thời tiết chân thực:
    - **Gỡ bỏ hoàn toàn tính năng chia sẻ**: Xóa bỏ các nút chia sẻ thời tiết trên TopBar, thẻ thời tiết chính và mục chia sẻ trong Cài đặt / Giới thiệu theo nhu cầu người dùng, giữ giao diện gọn gàng, tinh tế và riêng tư.
    - **Khung thời tiết chính hòa quyện nền trời (Seamless Borderless Hero)**: Loại bỏ khung hộp trắng đục cồng kềnh, chuyển sang bố cục không viền trong suốt hòa quyện tự nhiên với bầu trời khí quyển; nhiệt độ 86sp thanh thoát phong cách tối giản; phông chữ trắng sắc nét với bóng đổ mềm mại đọc rõ trên mọi nền trời; tích hợp chip viên thuốc bán trong suốt hiển thị chất lượng không khí (`🍃 AQI 22 • Tốt`).
    - **Nâng cấp hoạt ảnh nền động chân thực & sống động (Living Dynamic Atmosphere)**:
        - *Mặt trời đỉnh trời rực rỡ*: Đĩa mặt trời tỏa ánh hào quang ấm áp lan tỏa toàn màn hình, chùm tia nắng thể tích (God Rays) chiếu xiên xuống qua các kẽ mây cả khi trời nắng lẫn khi nhiều mây ban ngày (như ảnh thực tế).
        - *Mây thể tích 3D chân thực (Volumetric 3D Clouds)*: Đa tầng mây trôi thị sai bồng bềnh với kỹ thuật đổ bóng 3 lớp: đáy mây đậm khối khí quyển, thân mây trắng đặc và viền mây bạc đón nắng (Silver Lining).
        - *Hệ màu bầu trời tươi tắn & sâu thẳm*: Nâng cấp gradient bầu trời xanh thiên thanh rực rỡ, không còn bị mờ nhạt hay xám chì đơn điệu.

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
