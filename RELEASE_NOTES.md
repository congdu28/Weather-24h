### 🌟 Những thay đổi nổi bật trong bản cập nhật v1.0.63

#### 🎯 1. Chuẩn hóa quan trắc thời tiết & Loại bỏ triệt để báo mưa ảo
* **Khắc phục lỗi báo mưa sai lệch**: Giải quyết triệt để tình trạng ngoài trời đang nắng ráo, chỉ số tia cực tím UV cao và mây mỏng nhưng ứng dụng vẫn báo *"Mưa phùn"* hoặc *"Mưa rào"* do sai số ô lưới diện rộng (13km - 25km) của mô hình số trị toàn cầu.
* **Thuật toán quang học mặt đất (Physical Ground-truth)**: Tự động đối chiếu cường độ tia cực tím (UV ≥ 2.0) và tỷ lệ mây phủ (`cloud_cover`). Khi phát hiện ánh nắng mặt trời trực tiếp với lượng mưa vi lượng (≤ 0.25mm), ứng dụng sẽ tự động chuẩn hóa về: **Trời quang đãng, nắng đẹp** hoặc **Nắng xen mây**, đồng bộ hiển thị icon mặt trời rực rỡ.
* **Đối chiếu Radar thời gian thực (RainViewer)**: Thu hẹp bán kính kiểm tra cấp Phường/Xã (~1.5km) với ảnh radar thô không nhòe (`0_0.png`) và bộ lọc sắc độ màu thực (Chroma filter), loại bỏ các vệt quét giả của trạm đài.

#### 🇯🇵 2. Bổ sung & Tối ưu mô hình khí tượng JMA (Nhật Bản)
* Bổ sung mô hình dự báo **JMA (Japan Meteorological Agency)** chuyên biệt cho khí hậu nhiệt đới gió mùa Đông Nam Á, dự báo cực kỳ chính xác lượng mưa thực địa tại các tỉnh thành Việt Nam.

#### 🇻🇳 3. Khẳng định chủ quyền Biển Đảo Việt Nam
* Tích hợp đầy đủ dữ liệu thời tiết và định danh cho **Quần đảo Hoàng Sa (TP. Đà Nẵng)** và **Quần đảo Trường Sa (Tỉnh Khánh Hòa)**.
* Chuẩn hóa danh mục địa danh tiếng Việt theo cấp Xã/Phường, Quận/Huyện, Tỉnh/Thành phố.

#### ⚡ 4. Đồng bộ nền tảng & Tối ưu hiệu năng
* Đồng bộ dữ liệu và thuật toán chuẩn hóa với nền tảng Web [weather-24h.vercel.app](https://weather-24h.vercel.app).
* Tối ưu hiển thị dải dự báo 24 giờ và dải dự báo 7 ngày, giúp ứng dụng hoạt động mượt mà, phản hồi tức thì và tiết kiệm pin.
