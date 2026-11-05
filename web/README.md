# Weather 24h & Lịch Âm Việt Nam (Bản Web)

Phiên bản Web của ứng dụng **Weather 24h**, được xây dựng bằng **Next.js 16 (App Router) + React 19 + TypeScript + Tailwind CSS v4**.

[![Trải nghiệm trực tuyến](https://img.shields.io/badge/Tr%E1%BA%A3i%20nghi%E1%BB%87m-weather--24h.vercel.app-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://weather-24h.vercel.app/)
[![Next.js 16](https://img.shields.io/badge/Next.js-16-black?style=flat&logo=next.js&logoColor=white)](https://nextjs.org/)
[![React 19](https://img.shields.io/badge/React-19-61DAFB?style=flat&logo=react&logoColor=black)](https://react.dev/)
[![Tailwind CSS v4](https://img.shields.io/badge/Tailwind-CSS_v4-06B6D4?style=flat&logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)

🌐 **Địa chỉ truy cập chính thức**: [**https://weather-24h.vercel.app/**](https://weather-24h.vercel.app/)

## ✨ Tính năng chính

1. **Thời tiết thời gian thực & Dự báo**:
   - Tọa độ GPS tự động (`navigator.geolocation`) hoặc tìm kiếm mọi tỉnh thành/xã phường tại Việt Nam.
   - Nhiệt độ hiện tại, cảm nhận thực tế, cao nhất/thấp nhất trong ngày.
   - Dự báo 24 giờ tới (cuộn ngang, xác suất mưa, hướng & tốc độ gió).
   - Dự báo 7 ngày tới (thanh nhiệt độ trực quan, khả năng mưa, bình minh/hoàng hôn).
   - Chỉ số thời tiết chi tiết: Tia UV, Độ ẩm, Tốc độ gió, Áp suất khí quyển, Tầm nhìn xa.

2. **Chất lượng không khí (AQI) & Đời sống**:
   - Thang đo European AQI, nồng độ bụi mịn PM2.5, PM10 và lời khuyên sức khỏe.
   - Chỉ số sinh hoạt: Rửa xe, Giặt phơi quần áo, Thể thao ngoài trời, Chống nắng.

3. **AI Stylist (Tư vấn trang phục thông minh)**:
   - Gợi ý áo, quần/váy, giày dép, phụ kiện (ô dù, kính râm, áo khoác) tự động theo thời tiết thực tế.

4. **Lịch Âm Việt Nam & Lịch Vạn Niên toàn diện (100% Offline)**:
   - Thuật toán thiên văn học chính thống của TS. Hồ Ngọc Đức (Múi giờ GMT+7).
   - Can Chi Ngày, Tháng, Năm; 24 Tiết khí; Giờ Hoàng đạo & Hắc đạo.
   - 12 Trực (Kiến, Trừ, Mãn, Bình...), Nhị thập bát tú (28 Sao), Ngũ hành nạp âm.
   - Hướng xuất hành (Hỷ Thần, Tài Thần, Hạc Thần).
   - Giờ xuất hành Lý Thuần Phong (Đại An, Tốc Hỷ, Tiểu Cát, Lưu Niên...).
   - Ngày kỵ (Tam Nương, Nguyệt Kỵ, Sát Chủ, Thọ Tử, Vãng Vong) & Cát tinh (Thiên Đức, Nguyệt Đức, Thiên Hỷ).
   - **Công cụ Đổi ngày**: Dương lịch ↔ Âm lịch hai chiều chính xác.
   - **Tìm ngày tốt**: Tra cứu ngày đẹp theo việc (Khai trương, Cưới hỏi, Động thổ, Xuất hành, Mua xe...).
   - **Lịch tháng**: Lưới lịch tháng hiển thị song song ngày Dương và ngày Âm.

5. **Radar thời tiết trực tiếp**:
   - Nhúng bản đồ mây mưa, radar, gió theo thời gian thực tại tọa độ đã chọn.

6. **PWA (Progressive Web App)**:
   - Cài đặt lên màn hình chính điện thoại (iOS Safari, Android Chrome) hoặc máy tính như một ứng dụng độc lập.

---

## 🚀 Hướng dẫn chạy thử trên máy (Local Dev)

Trong thư mục `web/`:

```bash
# 1. Chạy máy chủ phát triển
npm run dev
```

Mở trình duyệt truy cập: [http://localhost:3000](http://localhost:3000)

```bash
# 2. Build bản chạy thực tế (Production)
npm run build
npm run start
```

---

## 🌐 Hướng dẫn Triển khai Miễn Phí (Deploy)

Bạn có thể đưa trang web lên mạng hoàn toàn miễn phí chỉ với vài bước:

1. **Deploy lên Vercel** (Khuyên dùng nhất cho Next.js):
   - Truy cập [vercel.com](https://vercel.com) và đăng nhập bằng GitHub.
   - Chọn kho lưu trữ `Weather-24h`.
   - Tại mục **Root Directory**, bấm Edit và chọn thư mục `web`.
   - Bấm **Deploy**. Trang web sẽ có link `.vercel.app` miễn phí, hỗ trợ HTTPS và cập nhật tự động mỗi khi push git.

2. **Deploy lên Cloudflare Pages** hoặc **Netlify**:
   - Chọn Root directory: `web`
   - Build command: `npm run build`
   - Output directory: `.next`
