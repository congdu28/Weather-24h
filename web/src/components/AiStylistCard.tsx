"use client";

import React, { useMemo } from "react";
import { Sparkles, Shirt, Umbrella, Glasses, Footprints, AlertCircle } from "lucide-react";
import { CurrentWeather, DailyForecast } from "../types/weather";

interface Props {
  current: CurrentWeather;
  todayForecast?: DailyForecast;
  isDarkTheme?: boolean;
}

export default function AiStylistCard({ current, todayForecast, isDarkTheme = true }: Props) {
  const outfit = useMemo(() => {
    const temp = current.temperature;
    const rainChance = todayForecast?.precipitationProbabilityMax ?? 0;
    const isRaining = current.weatherCode >= 51 || rainChance > 40;
    const isHot = temp >= 30;
    const isCool = temp >= 20 && temp < 25;
    const isCold = temp < 20;
    const isVeryCold = temp < 15;

    let top = "Áo thun cotton thoáng mát, thấm hút mồ hôi";
    let bottom = "Quần shorts hoặc quần dài vải đũi nhẹ";
    let shoes = "Giày thể thao êm chân hoặc sandals";
    let accessory = "Mũ lưỡi trai, kính râm chống tia cực tím";
    let tip = "Thời tiết ấm áp, trang phục năng động và thoáng khí là lựa chọn lý tưởng.";

    if (isVeryCold) {
      top = "Áo len dày hoặc áo nỉ kèm áo khoác phao / dạ ấm";
      bottom = "Quần jean dày hoặc quần nỉ giữ nhiệt";
      shoes = "Giày thể thao cổ cao, boots hoặc giày bít mũi kèm tất dày";
      accessory = "Khăn quàng cổ, găng tay giữ ấm";
      tip = "Trời rất lạnh, hãy mặc nhiều lớp để giữ ấm ngực và cổ họng!";
    } else if (isCold) {
      top = "Áo thun dài tay hoặc sơ mi kết hợp áo khoác nhẹ / hoodie";
      bottom = "Quần jeans hoặc quần kaki dài";
      shoes = "Giày sneaker hoặc giày da";
      accessory = "Áo khoác gió có mũ";
      tip = "Tiết trời se lạnh, một chiếc áo khoác mỏng hoặc cardigan là phù hợp nhất.";
    } else if (isCool) {
      top = "Áo thun polo hoặc sơ mi dài tay xắn gấu";
      bottom = "Quần chinos hoặc quần tây thoải mái";
      shoes = "Giày lười hoặc sneaker gọn gàng";
      accessory = "Đồng hồ hoặc áo khoác mỏng mang theo";
      tip = "Khí trời mát mẻ dễ chịu, thích hợp diện phong cách lịch sự nhẹ nhàng.";
    } else if (isHot) {
      top = "Áo thun ba lỗ, áo phông oversized hoặc sơ mi cộc tay chất liệu lanh";
      bottom = "Quần ngố, quần đùi hoặc váy nhẹ";
      shoes = "Dép quai hậu, sandals thoáng khí";
      accessory = "Kính râm phân cực, kem chống nắng SPF 50+, mũ rộng vành";
      tip = "Nắng nóng gay gắt, hãy chọn quần áo sáng màu và uống đủ nước nhé!";
    }

    if (isRaining) {
      accessory = "Mang theo ô gấp hoặc áo mưa nhỏ gọn trong cốp xe";
      shoes = "Giày chống thấm nước hoặc dép có độ bám tốt";
      tip += " Dự báo có mưa, đừng quên mang theo dù/áo mưa khi ra ngoài!";
    }

    return { top, bottom, shoes, accessory, tip, isRaining };
  }, [current, todayForecast]);

  return (
    <div
      className={`rounded-2xl sm:rounded-3xl p-4 sm:p-6 border transition-all ${
        isDarkTheme
          ? "bg-gradient-to-br from-indigo-950/40 via-purple-950/40 to-slate-900/80 border-purple-500/25 text-white shadow-xl"
          : "bg-gradient-to-br from-purple-50/90 via-indigo-50/70 to-white/95 border-purple-200/90 text-slate-900 shadow-xl shadow-purple-500/5"
      }`}
    >
      <div className="flex items-center justify-between pb-3 border-b border-purple-500/20">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-xl bg-purple-500/20 text-purple-500 border border-purple-500/30">
            <Sparkles className="w-4 h-4" />
          </div>
          <div>
            <h3 className={`text-sm font-bold flex items-center gap-1.5 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
              AI Stylist – Tư Vấn Trang Phục
            </h3>
            <p className={`text-xs ${isDarkTheme ? "text-purple-200/70" : "text-purple-700"}`}>
              Gợi ý theo nhiệt độ {current.temperature}°C & thời tiết hôm nay
            </p>
          </div>
        </div>

        {outfit.isRaining && (
          <span className="px-2.5 py-1 rounded-full text-[11px] font-semibold bg-blue-500/20 text-blue-600 dark:text-blue-300 border border-blue-400/30 flex items-center gap-1">
            <Umbrella className="w-3.5 h-3.5" />
            Nên mang dù
          </span>
        )}
      </div>

      {/* Outfit Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 my-4">
        <div
          className={`p-3 rounded-2xl border flex items-center gap-3 ${
            isDarkTheme ? "bg-white/5 border-white/10" : "bg-white/80 border-slate-200 shadow-xs"
          }`}
        >
          <div className={`p-2 rounded-xl ${isDarkTheme ? "bg-white/10 text-amber-300" : "bg-amber-100 text-amber-700"}`}>
            <Shirt className="w-4 h-4" />
          </div>
          <div>
            <div className={`text-[11px] font-medium ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
              Áo khuyên dùng
            </div>
            <div className={`text-xs font-semibold mt-0.5 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
              {outfit.top}
            </div>
          </div>
        </div>

        <div
          className={`p-3 rounded-2xl border flex items-center gap-3 ${
            isDarkTheme ? "bg-white/5 border-white/10" : "bg-white/80 border-slate-200 shadow-xs"
          }`}
        >
          <div className={`p-2 rounded-xl ${isDarkTheme ? "bg-white/10 text-blue-300" : "bg-blue-100 text-blue-700"}`}>
            <Shirt className="w-4 h-4" />
          </div>
          <div>
            <div className={`text-[11px] font-medium ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
              Quần / Váy
            </div>
            <div className={`text-xs font-semibold mt-0.5 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
              {outfit.bottom}
            </div>
          </div>
        </div>

        <div
          className={`p-3 rounded-2xl border flex items-center gap-3 ${
            isDarkTheme ? "bg-white/5 border-white/10" : "bg-white/80 border-slate-200 shadow-xs"
          }`}
        >
          <div className={`p-2 rounded-xl ${isDarkTheme ? "bg-white/10 text-emerald-300" : "bg-emerald-100 text-emerald-700"}`}>
            <Footprints className="w-4 h-4" />
          </div>
          <div>
            <div className={`text-[11px] font-medium ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
              Giày dép
            </div>
            <div className={`text-xs font-semibold mt-0.5 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
              {outfit.shoes}
            </div>
          </div>
        </div>

        <div
          className={`p-3 rounded-2xl border flex items-center gap-3 ${
            isDarkTheme ? "bg-white/5 border-white/10" : "bg-white/80 border-slate-200 shadow-xs"
          }`}
        >
          <div className={`p-2 rounded-xl ${isDarkTheme ? "bg-white/10 text-rose-300" : "bg-rose-100 text-rose-700"}`}>
            <Glasses className="w-4 h-4" />
          </div>
          <div>
            <div className={`text-[11px] font-medium ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
              Phụ kiện & vật dụng
            </div>
            <div className={`text-xs font-semibold mt-0.5 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
              {outfit.accessory}
            </div>
          </div>
        </div>
      </div>

      {/* Stylist Tip Box */}
      <div
        className={`p-3 rounded-2xl border flex items-start gap-2.5 ${
          isDarkTheme ? "bg-purple-500/10 border-purple-500/20" : "bg-purple-100/70 border-purple-200 text-purple-950"
        }`}
      >
        <AlertCircle className="w-4 h-4 text-purple-500 shrink-0 mt-0.5" />
        <p className={`text-xs leading-relaxed font-medium ${isDarkTheme ? "text-purple-200" : "text-purple-900"}`}>
          {outfit.tip}
        </p>
      </div>
    </div>
  );
}
