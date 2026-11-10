"use client";

import React, { useMemo } from "react";
import { CurrentWeather, DailyForecast, HourlyForecast } from "../types/weather";
import {
  Sparkles,
  CloudRain,
  Sun,
  ShieldAlert,
  Droplets,
  Thermometer,
  CheckCircle2,
  Lightbulb
} from "lucide-react";

interface Props {
  current: CurrentWeather;
  todayForecast?: DailyForecast;
  hourly?: HourlyForecast[];
  isDarkTheme?: boolean;
}

interface InsightItem {
  id: string;
  type: "warning" | "caution" | "good" | "info";
  title: string;
  badge: string;
  content: string;
  icon: React.ElementType;
  iconColor: string;
  bgColor: string;
  borderColor: string;
}

export default function SmartInsightsCard({
  current,
  todayForecast,
  hourly = [],
  isDarkTheme = true
}: Props) {
  const insights: InsightItem[] = useMemo(() => {
    const list: InsightItem[] = [];

    // 1. Phân tích cửa sổ mưa 24h
    const rainHours = hourly.filter((h) => h.precipitationProbability >= 40 || h.precipitation >= 0.2);
    if (rainHours.length > 0) {
      const firstRain = rainHours[0];
      const maxRainProbItem = rainHours.reduce((max, cur) =>
        cur.precipitationProbability > max.precipitationProbability ? cur : max
      );
      const lastRain = rainHours[rainHours.length - 1];

      list.push({
        id: "rain_window",
        type: "caution",
        title: "Cửa sổ có mưa trong ngày",
        badge: `Cao nhất ${maxRainProbItem.precipitationProbability}%`,
        content: `Dự báo khả năng có mưa rào rải rác từ ${firstRain.hour} đến ${lastRain.hour} (mưa nhiều nhất lúc ${maxRainProbItem.hour} với xác suất ${maxRainProbItem.precipitationProbability}%). Hãy chủ động chuẩn bị ô hoặc áo mưa khi ra ngoài!`,
        icon: CloudRain,
        iconColor: "text-blue-500",
        bgColor: isDarkTheme ? "bg-blue-950/40" : "bg-blue-50/90",
        borderColor: isDarkTheme ? "border-blue-500/30" : "border-blue-200"
      });
    } else {
      list.push({
        id: "dry_day",
        type: "good",
        title: "Trời khô ráo, không mưa",
        badge: "Rất thuận lợi",
        content: "Toàn bộ 24 giờ tới không có mưa (xác suất mưa dưới 20%). Thời tiết khô ráo, cực kỳ thuận lợi cho các hoạt động di chuyển, giặt phơi và vui chơi ngoài trời.",
        icon: CheckCircle2,
        iconColor: "text-emerald-500",
        bgColor: isDarkTheme ? "bg-emerald-950/40" : "bg-emerald-50/90",
        borderColor: isDarkTheme ? "border-emerald-500/30" : "border-emerald-200"
      });
    }

    // 2. Chênh lệch nhiệt độ ngày & đêm (Biên độ nhiệt)
    if (todayForecast) {
      const tempDiff = todayForecast.tempMax - todayForecast.tempMin;
      if (tempDiff >= 8) {
        list.push({
          id: "temp_swing",
          type: "warning",
          title: "Chênh lệch nhiệt độ ngày & đêm lớn",
          badge: `Dao động ${tempDiff}°C`,
          content: `Nhiệt độ thay đổi mạnh từ ${todayForecast.tempMin}°C (đêm/sáng sớm) đến ${todayForecast.tempMax}°C (giữa trưa). Sự chênh lệch ${tempDiff}°C dễ gây cảm sốt và ho, nên mang thêm áo khoác nhẹ khi đi sớm về muộn.`,
          icon: Thermometer,
          iconColor: "text-amber-500",
          bgColor: isDarkTheme ? "bg-amber-950/40" : "bg-amber-50/90",
          borderColor: isDarkTheme ? "border-amber-500/30" : "border-amber-200"
        });
      } else {
        list.push({
          id: "temp_stable",
          type: "good",
          title: "Nhiệt độ ngày đêm điều hòa",
          badge: `Chênh lệch ${tempDiff}°C`,
          content: `Biên độ nhiệt dễ chịu (${todayForecast.tempMin}°C - ${todayForecast.tempMax}°C), nhiệt độ chuyển biến êm dịu, ít nguy cơ sốc nhiệt.`,
          icon: Thermometer,
          iconColor: "text-teal-500",
          bgColor: isDarkTheme ? "bg-teal-950/40" : "bg-teal-50/90",
          borderColor: isDarkTheme ? "border-teal-500/30" : "border-teal-200"
        });
      }
    }

    // 3. Phân tích chỉ số bức xạ tia cực tím UV
    const maxUv = todayForecast?.uvIndexMax ?? current.uvIndex;
    if (maxUv >= 7.5) {
      list.push({
        id: "uv_alert",
        type: "warning",
        title: "Tia cực tím UV đạt mức nguy hại",
        badge: `Đỉnh UV ${maxUv.toFixed(1)}`,
        content: `Bức xạ UV đạt mức cao nguy hại vào khoảng 10:30 - 14:30. Tiếp xúc trực tiếp có thể gây bỏng rát da và hại mắt. Bắt buộc bôi kem chống nắng SPF 50+, đeo kính râm và mặc áo chống tia UV.`,
        icon: ShieldAlert,
        iconColor: "text-rose-500",
        bgColor: isDarkTheme ? "bg-rose-950/40" : "bg-rose-50/90",
        borderColor: isDarkTheme ? "border-rose-500/30" : "border-rose-200"
      });
    } else if (maxUv >= 3.5) {
      list.push({
        id: "uv_moderate",
        type: "info",
        title: "Chỉ số UV ở mức trung bình",
        badge: `Đỉnh UV ${maxUv.toFixed(1)}`,
        content: `Mức bức xạ UV vừa phải, an toàn vào sáng sớm và chiều mát. Chỉ cần che chắn nhẹ nếu hoạt động ngoài trời lâu trong khoảng 11h - 14h.`,
        icon: Sun,
        iconColor: "text-amber-400",
        bgColor: isDarkTheme ? "bg-amber-950/30" : "bg-amber-50/70",
        borderColor: isDarkTheme ? "border-amber-500/20" : "border-amber-200/70"
      });
    }

    // 4. Phân tích độ ẩm không khí (Nồm ẩm vs Hanh khô)
    const hum = current.relativeHumidity;
    if (hum >= 85) {
      list.push({
        id: "humidity_high",
        type: "caution",
        title: "Độ ẩm cao • Nguy cơ nồm ẩm",
        badge: `Độ ẩm ${hum}%`,
        content: `Không khí rất ẩm (${hum}%), dễ đọng nước trên sàn nhà và gây bí mồ hôi. Nên đóng kín cửa kính và bật chế độ hút ẩm (Dry) trên máy lạnh để không khí khô ráo, ngăn ngừa ẩm mốc.`,
        icon: Droplets,
        iconColor: "text-cyan-400",
        bgColor: isDarkTheme ? "bg-cyan-950/40" : "bg-cyan-50/90",
        borderColor: isDarkTheme ? "border-cyan-500/30" : "border-cyan-200"
      });
    } else if (hum <= 45) {
      list.push({
        id: "humidity_low",
        type: "caution",
        title: "Thời tiết hanh khô",
        badge: `Độ ẩm ${hum}%`,
        content: `Độ ẩm không khí xuống thấp (${hum}%), cơ thể dễ bị mất nước, khô da và rát cổ họng. Nên bổ sung thêm 500ml nước, thoa kem dưỡng ẩm và sử dụng máy tạo sương.`,
        icon: Droplets,
        iconColor: "text-orange-400",
        bgColor: isDarkTheme ? "bg-orange-950/40" : "bg-orange-50/90",
        borderColor: isDarkTheme ? "border-orange-500/30" : "border-orange-200"
      });
    }

    // 5. Cảm nhận nhiệt độ thực tế (Sốc nhiệt / Gió lạnh)
    const feelDiff = current.apparentTemperature - current.temperature;
    if (feelDiff >= 3) {
      list.push({
        id: "feel_hot",
        type: "info",
        title: "Cảm giác nhiệt oi bức hơn thực tế",
        badge: `Cảm nhận ${current.apparentTemperature}°C`,
        content: `Do độ ẩm và khả năng bốc hơi, cơ thể cảm nhận nhiệt độ là ${current.apparentTemperature}°C (nóng hơn nhiệt kế ${feelDiff}°C). Uống nước đều đặn ngay cả khi chưa thấy khát.`,
        icon: Thermometer,
        iconColor: "text-red-400",
        bgColor: isDarkTheme ? "bg-red-950/30" : "bg-red-50/70",
        borderColor: isDarkTheme ? "border-red-500/20" : "border-red-200/70"
      });
    }

    return list;
  }, [current, todayForecast, hourly, isDarkTheme]);

  return (
    <div className="weather-card glass-specular p-4 sm:p-6 lg:p-7">
      {/* Header */}
      <div className="flex items-center justify-between pb-3 sm:pb-4 border-b border-black/5 dark:border-white/10">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-xl bg-amber-500/20 text-amber-500 border border-amber-500/30">
            <Sparkles className="w-4 h-4 sm:w-5 sm:h-5" />
          </div>
          <div>
            <h3 className={`text-base font-extrabold flex items-center gap-1.5 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
              Phân tích & Cảnh báo Thông minh
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-amber-500/15 text-amber-600 dark:text-amber-400 font-bold border border-amber-500/30">
                AI Weather Insights
              </span>
            </h3>
            <p className={`text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-500 font-medium"}`}>
              Đánh giá biến động nhiệt độ, cửa sổ mưa, độ ẩm và lưu ý sức khỏe
            </p>
          </div>
        </div>

        <div className="hidden sm:flex items-center gap-1 text-xs text-slate-400">
          <Lightbulb className="w-3.5 h-3.5 text-amber-400" />
          <span>Tự động cập nhật theo dữ liệu thực</span>
        </div>
      </div>

      {/* Insights Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5 sm:gap-4 mt-4">
        {insights.map((item) => {
          const IconComp = item.icon;
          return (
            <div
              key={item.id}
              className={`p-4 rounded-2xl border transition-all hover:scale-[1.01] ${item.bgColor} ${item.borderColor}`}
            >
              <div className="flex items-start justify-between gap-3 mb-2">
                <div className="flex items-center gap-2.5">
                  <div className={`p-2 rounded-xl bg-black/10 dark:bg-white/10 ${item.iconColor} shrink-0`}>
                    <IconComp className="w-4 h-4 sm:w-5 sm:h-5" />
                  </div>
                  <span className={`text-xs sm:text-sm font-bold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                    {item.title}
                  </span>
                </div>
                <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full shrink-0 ${
                  item.type === "warning"
                    ? "bg-rose-500/20 text-rose-500 border border-rose-500/30"
                    : item.type === "caution"
                    ? "bg-amber-500/20 text-amber-500 border border-amber-500/30"
                    : "bg-emerald-500/20 text-emerald-500 border border-emerald-500/30"
                }`}>
                  {item.badge}
                </span>
              </div>

              <p className={`text-xs leading-relaxed ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                {item.content}
              </p>
            </div>
          );
        })}
      </div>
    </div>
  );
}
