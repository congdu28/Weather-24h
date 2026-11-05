"use client";

import React, { useMemo } from "react";
import { CurrentWeather, DailyForecast, CityLocation, AirQuality } from "../types/weather";
import { getWeatherCondition } from "../services/weatherApi";
import { getTodayLunar } from "../utils/lunarCalendar";
import { getDayAlmanac, DAY_LEVEL_INFO } from "../utils/lunarAlmanac";
import WeatherIcon from "./WeatherIcon";
import {
  MapPin,
  Crosshair,
  Star,
  ChevronDown,
  ChevronRight,
  ShieldCheck,
  Droplets,
  Wind,
  ShieldAlert,
  Eye,
  Sunrise,
  Sunset,
  Gauge
} from "lucide-react";

interface Props {
  current: CurrentWeather;
  todayForecast?: DailyForecast;
  location: CityLocation;
  airQuality?: AirQuality;
  isFavorite: boolean;
  onToggleFavorite: () => void;
  onLocationClick: () => void;
  onLunarClick: () => void;
  showLunar?: boolean;
  isDarkTheme?: boolean;
}

export default function HeroWeatherCard({
  current,
  todayForecast,
  location,
  airQuality,
  isFavorite,
  onToggleFavorite,
  onLocationClick,
  onLunarClick,
  showLunar = true,
  isDarkTheme = true
}: Props) {
  const cond = getWeatherCondition(current.weatherCode, current.isDay);
  const maxTemp = todayForecast?.tempMax ?? current.temperature + 2;
  const minTemp = todayForecast?.tempMin ?? current.temperature - 3;

  const todayLunar = useMemo(() => getTodayLunar(), []);
  const todayAlmanac = useMemo(() => getDayAlmanac(todayLunar), [todayLunar]);
  const levelInfo = DAY_LEVEL_INFO[todayAlmanac.level];

  // Sub location string
  const subText = location.admin1 && location.admin1 !== location.name
    ? `${location.admin1}, ${location.country}`
    : location.country || "";

  // UV label & color
  let uvLabel = "Thấp";
  let uvColor = "text-emerald-400";
  if (current.uvIndex >= 11) { uvLabel = "Cực kỳ nguy hại"; uvColor = "text-purple-400"; }
  else if (current.uvIndex >= 8) { uvLabel = "Rất cao"; uvColor = "text-red-400"; }
  else if (current.uvIndex >= 6) { uvLabel = "Cao"; uvColor = "text-amber-400"; }
  else if (current.uvIndex >= 3) { uvLabel = "Vừa phải"; uvColor = "text-yellow-400"; }

  // Wind direction compass
  const directions = ["Bắc", "Đông Bắc", "Đông", "Đông Nam", "Nam", "Tây Nam", "Tây", "Tây Bắc"];
  const windDirIndex = Math.round(current.windDirection / 45) % 8;
  const windDirName = directions[windDirIndex];

  return (
    <div
      className={`w-full rounded-3xl p-6 sm:p-8 backdrop-blur-2xl border transition-all shadow-2xl ${
        isDarkTheme
          ? "bg-slate-900/40 border-white/15 text-white"
          : "bg-white/70 border-white/80 text-slate-900 shadow-sky-500/10"
      }`}
    >
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
        {/* ============================================================== */}
        {/* CỘT BÊN TRÁI: ĐỊA ĐIỂM, LỊCH ÂM, NHIỆT ĐỘ KHỔNG LỒ & MÔ TẢ    */}
        {/* ============================================================== */}
        <div className="lg:col-span-7 flex flex-col items-start text-left space-y-4">
          {/* 1. Tên Địa Điểm & Nút Yêu Thích ⭐ */}
          <div className="flex items-center gap-2 flex-wrap">
            <button
              onClick={onLocationClick}
              className={`group flex items-center gap-2 px-4 py-2.5 rounded-2xl border backdrop-blur-md transition-all active:scale-95 ${
                isDarkTheme
                  ? "bg-white/10 hover:bg-white/15 border-white/15 text-white"
                  : "bg-white/90 hover:bg-white border-slate-200/80 text-slate-900 shadow-sm"
              }`}
              title="Nhấn để tìm kiếm hoặc đổi địa điểm"
            >
              {location.isCurrentLocation ? (
                <Crosshair className="w-5 h-5 text-amber-500 shrink-0" />
              ) : (
                <MapPin className="w-5 h-5 text-amber-500 shrink-0" />
              )}

              <span className="text-2xl sm:text-3xl font-black tracking-tight" suppressHydrationWarning>
                {location.name}
              </span>

              <ChevronDown className="w-4 h-4 opacity-70 group-hover:opacity-100 transition-opacity" />
            </button>

            {/* Nút Yêu Thích ⭐ */}
            <button
              onClick={onToggleFavorite}
              className={`p-2.5 rounded-2xl border backdrop-blur-md transition-all active:scale-90 ${
                isDarkTheme
                  ? "bg-white/10 hover:bg-white/15 border-white/15 text-white"
                  : "bg-white/90 hover:bg-white border-slate-200/80 text-slate-900 shadow-sm"
              }`}
              title={isFavorite ? "Bỏ yêu thích" : "Lưu vào yêu thích"}
            >
              <Star
                className={`w-5 h-5 transition-transform ${
                  isFavorite
                    ? "text-amber-400 fill-amber-400 scale-110"
                    : isDarkTheme ? "text-white/70 hover:text-white" : "text-slate-400 hover:text-amber-500"
                }`}
              />
            </button>
          </div>

          {/* Tên Phụ / Xã / Huyện / Tỉnh */}
          {subText && (
            <div
              suppressHydrationWarning
              className={`text-xs sm:text-sm font-medium pl-1 ${
                isDarkTheme ? "text-slate-300" : "text-slate-600"
              }`}
            >
              {subText}
            </div>
          )}

          {/* 2. Thẻ Lịch Âm Mini (Chạm để mở Lịch Vạn Niên chi tiết) */}
          {showLunar && (
            <button
              onClick={onLunarClick}
              className={`w-full sm:w-auto inline-flex items-center justify-between gap-3 px-4 py-2.5 rounded-2xl border backdrop-blur-md transition-all active:scale-[0.98] group ${
                isDarkTheme
                  ? "bg-black/30 hover:bg-black/40 border-amber-400/40 text-white"
                  : "bg-amber-50/90 hover:bg-amber-100/90 border-amber-300 text-slate-900 shadow-sm"
              }`}
            >
              <div className="text-left">
                <div className="text-xs sm:text-sm font-bold flex items-center gap-1.5 flex-wrap">
                  <span>🗓️ {todayLunar.formattedLunarShort}</span>
                  <span className={isDarkTheme ? "text-amber-300" : "text-amber-700 font-extrabold"}>
                    ({todayLunar.dayCanChi})
                  </span>
                  <span>•</span>
                  <span className={`text-xs ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                    Tiết {todayLunar.solarTerm}
                  </span>
                </div>
                <div className={`text-[11px] font-medium flex items-center gap-1.5 mt-0.5 flex-wrap ${
                  isDarkTheme ? "text-amber-300/90" : "text-amber-800"
                }`}>
                  <span>{todayLunar.auspiciousDayType.split(" (")[0]} ({todayLunar.isAuspiciousDay ? "Hoàng đạo" : "Hắc đạo"})</span>
                  <span>•</span>
                  <span>{todayAlmanac.truc.fullName}</span>
                  <span>•</span>
                  <span>{"★".repeat(levelInfo.stars)} {levelInfo.label}</span>
                </div>
              </div>
              <ChevronRight className="w-4 h-4 text-amber-500 group-hover:translate-x-1 transition-transform shrink-0" />
            </button>
          )}

          {/* 3. Nhiệt độ khổng lồ + Icon thời tiết + Mô tả */}
          <div className="pt-2 flex items-center gap-5">
            <div className="flex items-baseline">
              <span className="text-7xl sm:text-8xl md:text-9xl font-light tracking-tighter leading-none">
                {current.temperature}°
              </span>
            </div>

            <div className="space-y-1">
              <div
                className={`p-3 rounded-2xl border w-fit ${
                  isDarkTheme ? "bg-white/10 border-white/15" : "bg-white/90 border-slate-200 shadow-sm"
                }`}
              >
                <WeatherIcon name={cond.icon} className="w-12 h-12 sm:w-14 sm:h-14" />
              </div>
              <div className={`text-lg sm:text-xl font-extrabold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                {cond.description}
              </div>
              <div className={`text-sm sm:text-base font-semibold ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                Nhiệt độ ngày: <strong className={isDarkTheme ? "text-white" : "text-slate-900"}>{maxTemp}° / {minTemp}°</strong>
              </div>
              <div className={`text-xs sm:text-sm font-medium ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                Cảm giác như: {current.apparentTemperature}°C
              </div>
            </div>
          </div>
        </div>

        {/* ============================================================== */}
        {/* CỘT BÊN PHẢI: LƯỚI THÔNG SỐ CHI TIẾT THỜI TIẾT TỨC THỜI       */}
        {/* ============================================================== */}
        <div className="lg:col-span-5 w-full">
          <div
            className={`p-4 sm:p-5 rounded-2xl border space-y-3.5 ${
              isDarkTheme
                ? "bg-white/5 border-white/10"
                : "bg-white/80 border-slate-200/80 shadow-md"
            }`}
          >
            <div
              className={`flex items-center justify-between pb-2 border-b text-xs font-bold uppercase tracking-wider ${
                isDarkTheme ? "border-white/10 text-white/80" : "border-slate-200 text-slate-700"
              }`}
            >
              <span>Chi tiết thời tiết hiện tại</span>
              <span>Cập nhật lúc {current.time.split("T")[1] || "vừa xong"}</span>
            </div>

            {/* Quick Metrics Grid */}
            <div className="grid grid-cols-2 gap-3 text-xs">
              {/* 1. AQI */}
              {airQuality && (
                <div
                  className={`p-3 rounded-xl border flex flex-col justify-between ${
                    isDarkTheme ? "bg-white/5 border-white/10" : "bg-sky-50/80 border-sky-200"
                  }`}
                >
                  <div className={`flex items-center justify-between ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>
                    <span className="flex items-center gap-1 font-medium">
                      <ShieldCheck className="w-3.5 h-3.5 text-emerald-500" />
                      Không khí AQI
                    </span>
                    <span className={`font-bold ${airQuality.color}`}>{airQuality.category}</span>
                  </div>
                  <div className={`text-xl font-bold mt-1 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                    {airQuality.aqiIndex}{" "}
                    <span className={`text-[10px] font-normal ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                      AQI
                    </span>
                  </div>
                  <div className={`text-[10px] mt-0.5 ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                    PM2.5: {airQuality.pm25} µg/m³
                  </div>
                </div>
              )}

              {/* 2. Độ ẩm */}
              <div
                className={`p-3 rounded-xl border flex flex-col justify-between ${
                  isDarkTheme ? "bg-white/5 border-white/10" : "bg-blue-50/80 border-blue-200"
                }`}
              >
                <div className={`flex items-center justify-between ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>
                  <span className="flex items-center gap-1 font-medium">
                    <Droplets className="w-3.5 h-3.5 text-blue-500" />
                    Độ ẩm
                  </span>
                </div>
                <div className={`text-xl font-bold mt-1 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                  {current.relativeHumidity}%
                </div>
                <div className={`text-[10px] mt-0.5 ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                  {current.relativeHumidity > 80 ? "Ẩm ướt / Nồm" : "Thoáng đãng"}
                </div>
              </div>

              {/* 3. Gió */}
              <div
                className={`p-3 rounded-xl border flex flex-col justify-between ${
                  isDarkTheme ? "bg-white/5 border-white/10" : "bg-teal-50/80 border-teal-200"
                }`}
              >
                <div className={`flex items-center justify-between ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>
                  <span className="flex items-center gap-1 font-medium">
                    <Wind className="w-3.5 h-3.5 text-teal-500" />
                    Gió ({windDirName})
                  </span>
                </div>
                <div className={`text-xl font-bold mt-1 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                  {current.windSpeed}{" "}
                  <span className={`text-[10px] font-normal ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                    km/h
                  </span>
                </div>
                <div className={`text-[10px] mt-0.5 ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                  {current.windSpeed > 20 ? "Gió vừa" : "Gió nhẹ"}
                </div>
              </div>

              {/* 4. Tia UV */}
              <div
                className={`p-3 rounded-xl border flex flex-col justify-between ${
                  isDarkTheme ? "bg-white/5 border-white/10" : "bg-amber-50/80 border-amber-200"
                }`}
              >
                <div className={`flex items-center justify-between ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>
                  <span className="flex items-center gap-1 font-medium">
                    <ShieldAlert className="w-3.5 h-3.5 text-amber-500" />
                    Tia UV
                  </span>
                  <span className={`font-bold ${uvColor}`}>{uvLabel}</span>
                </div>
                <div className={`text-xl font-bold mt-1 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                  {current.uvIndex}{" "}
                  <span className={`text-[10px] font-normal ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                    / 11+
                  </span>
                </div>
                <div className={`text-[10px] mt-0.5 ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                  {current.uvIndex >= 6 ? "Cần chống nắng" : "An toàn ngoài trời"}
                </div>
              </div>

              {/* 5. Tầm nhìn xa */}
              <div
                className={`p-3 rounded-xl border flex flex-col justify-between ${
                  isDarkTheme ? "bg-white/5 border-white/10" : "bg-slate-50 border-slate-200"
                }`}
              >
                <div className={`flex items-center justify-between ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>
                  <span className="flex items-center gap-1 font-medium">
                    <Eye className="w-3.5 h-3.5 text-cyan-500" />
                    Tầm nhìn xa
                  </span>
                </div>
                <div className={`text-xl font-bold mt-1 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                  {current.visibility}{" "}
                  <span className={`text-[10px] font-normal ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                    km
                  </span>
                </div>
                <div className={`text-[10px] mt-0.5 ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                  {current.visibility >= 10 ? "Tầm nhìn tốt" : "Sương mờ nhẹ"}
                </div>
              </div>

              {/* 6. Áp suất khí quyển */}
              <div
                className={`p-3 rounded-xl border flex flex-col justify-between ${
                  isDarkTheme ? "bg-white/5 border-white/10" : "bg-slate-50 border-slate-200"
                }`}
              >
                <div className={`flex items-center justify-between ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>
                  <span className="flex items-center gap-1 font-medium">
                    <Gauge className="w-3.5 h-3.5 text-purple-500" />
                    Áp suất
                  </span>
                </div>
                <div className={`text-xl font-bold mt-1 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                  {current.surfacePressure}{" "}
                  <span className={`text-[10px] font-normal ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                    hPa
                  </span>
                </div>
                <div className={`text-[10px] mt-0.5 ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                  Khí áp ổn định
                </div>
              </div>
            </div>

            {/* Mặt trời mọc & Lặn */}
            {todayForecast && (
              <div
                className={`p-3 rounded-xl border flex items-center justify-around text-xs ${
                  isDarkTheme ? "bg-white/5 border-white/10" : "bg-slate-50 border-slate-200"
                }`}
              >
                <div className="flex items-center gap-2">
                  <Sunrise className="w-4 h-4 text-amber-500" />
                  <div>
                    <div className={`text-[10px] ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>Mọc</div>
                    <div className={`font-bold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>{todayForecast.sunrise}</div>
                  </div>
                </div>
                <div className={`h-6 w-px ${isDarkTheme ? "bg-white/20" : "bg-slate-300"}`} />
                <div className="flex items-center gap-2">
                  <Sunset className="w-4 h-4 text-orange-500" />
                  <div>
                    <div className={`text-[10px] ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>Lặn</div>
                    <div className={`font-bold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>{todayForecast.sunset}</div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
