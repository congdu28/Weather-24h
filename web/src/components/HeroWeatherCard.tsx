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
  Gauge,
  Thermometer
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
      className={`weather-card glass-specular w-full p-4 sm:p-6 lg:p-8 relative overflow-hidden ${
        isDarkTheme ? "text-white" : "text-slate-900"
      }`}
    >
      {/* Hiệu ứng hào quang thời tiết đa tầng (Weather Aura Light) */}
      <div
        className="absolute -top-16 -left-16 w-80 h-80 rounded-full blur-3xl pointer-events-none opacity-25 sm:opacity-35 transition-all duration-1000 animate-pulse"
        style={{
          backgroundColor:
            cond.type === "sunny"
              ? current.isDay ? "#f59e0b" : "#6366f1"
              : cond.type === "rainy"
              ? "#0ea5e9"
              : cond.type === "thunder"
              ? "#8b5cf6"
              : "#10b981"
        }}
      />
      <div
        className="absolute -bottom-20 -right-20 w-80 h-80 rounded-full blur-3xl pointer-events-none opacity-15 sm:opacity-25 transition-all duration-1000"
        style={{
          backgroundColor:
            cond.type === "sunny"
              ? "#f97316"
              : cond.type === "rainy"
              ? "#0284c7"
              : cond.type === "thunder"
              ? "#7c3aed"
              : "#0d9488"
        }}
      />

      <div className="relative z-1 grid grid-cols-1 lg:grid-cols-12 gap-5 sm:gap-8 items-center">
        {/* ============================================================== */}
        {/* CỘT BÊN TRÁI: ĐỊA ĐIỂM, LỊCH ÂM, NHIỆT ĐỘ KHỔNG LỒ & MÔ TẢ    */}
        {/* ============================================================== */}
        <div className="lg:col-span-7 flex flex-col items-start text-left space-y-3 sm:space-y-4">
          {/* 1. Tên Địa Điểm & Nút Yêu Thích ⭐ */}
          <div className="flex items-center gap-2 flex-wrap">
            <button
              onClick={onLocationClick}
              className={`group flex items-center gap-2 px-3 sm:px-4 py-2 sm:py-2.5 rounded-xl sm:rounded-2xl border transition-all active:scale-95 ${
                isDarkTheme
                  ? "bg-white/10 hover:bg-white/15 border-white/15 text-white"
                  : "bg-white hover:bg-slate-50 border-slate-200 text-slate-900 shadow-xs"
              }`}
              title="Nhấn để tìm kiếm hoặc đổi địa điểm"
            >
              {location.isCurrentLocation ? (
                <Crosshair className="w-4 h-4 sm:w-5 sm:h-5 text-amber-500 shrink-0" />
              ) : (
                <MapPin className="w-4 h-4 sm:w-5 sm:h-5 text-amber-500 shrink-0" />
              )}

              <span className="text-xl sm:text-2xl lg:text-3xl font-black tracking-tight" suppressHydrationWarning>
                {location.name}
              </span>

              <ChevronDown className="w-4 h-4 opacity-70 group-hover:opacity-100 transition-opacity" />
            </button>

            {/* Nút Yêu Thích ⭐ */}
            <button
              onClick={onToggleFavorite}
              className={`p-2 sm:p-2.5 rounded-xl sm:rounded-2xl border transition-all active:scale-90 ${
                isDarkTheme
                  ? "bg-white/10 hover:bg-white/15 border-white/15 text-white"
                  : "bg-white hover:bg-slate-50 border-slate-200 text-slate-900 shadow-xs"
              }`}
              title={isFavorite ? "Bỏ yêu thích" : "Lưu vào yêu thích"}
            >
              <Star
                className={`w-4 h-4 sm:w-5 sm:h-5 transition-transform ${
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
              className={`text-xs sm:text-sm font-semibold pl-1 ${
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
              className={`w-full sm:w-auto inline-flex items-center justify-between gap-2.5 sm:gap-3 px-3 sm:px-4 py-2 sm:py-2.5 rounded-xl sm:rounded-2xl border transition-all active:scale-[0.98] group ${
                isDarkTheme
                  ? "bg-black/30 hover:bg-black/40 border-amber-400/40 text-white"
                  : "bg-amber-50/90 hover:bg-amber-100/90 border-amber-300 text-slate-900 shadow-xs"
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
                <div className={`text-[10px] sm:text-[11px] font-medium flex items-center gap-1.5 mt-0.5 flex-wrap ${
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

          {/* 3. Nhiệt độ khổng lồ + Icon thời tiết động + Mô tả & Cảm giác như */}
          <div className="pt-2 flex flex-row items-center gap-3 sm:gap-6 flex-wrap sm:flex-nowrap">
            <div className="flex items-baseline shrink-0">
              <span className="text-6xl sm:text-7xl md:text-8xl lg:text-9xl font-light tracking-tighter leading-none select-none">
                {current.temperature}°
              </span>
            </div>

            <div className="space-y-2.5 min-w-0 flex-1">
              {/* Icon thời tiết có hiệu ứng quầng sáng động (Dynamic Glow) */}
              <div className="flex items-center gap-3">
                <div className="relative group/icon shrink-0">
                  <div
                    className={`absolute -inset-1 rounded-2xl blur-md opacity-45 group-hover/icon:opacity-80 transition-all duration-500 ${
                      cond.type === "sunny"
                        ? "bg-amber-400"
                        : cond.type === "rainy"
                        ? "bg-sky-400"
                        : cond.type === "thunder"
                        ? "bg-purple-500"
                        : "bg-teal-400"
                    }`}
                  />
                  <div
                    className={`relative p-2.5 sm:p-3 rounded-2xl border transition-all duration-300 group-hover/icon:scale-105 ${
                      isDarkTheme
                        ? "bg-slate-900/80 border-white/20 shadow-lg"
                        : "bg-white/95 border-slate-200/90 shadow-md"
                    }`}
                  >
                    <WeatherIcon name={cond.icon} className="w-9 h-9 sm:w-12 sm:h-12" />
                  </div>
                </div>
                <div className={`text-base sm:text-xl lg:text-2xl font-black tracking-tight ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                  {cond.description}
                </div>
              </div>

              {/* Phần "Cảm giác như" nổi bật & Biên độ ngày */}
              <div className="flex items-center gap-2 flex-wrap pt-0.5">
                <div
                  className={`inline-flex items-center gap-2 px-3 py-1.5 sm:px-3.5 sm:py-2 rounded-xl sm:rounded-2xl border transition-all shadow-xs ${
                    isDarkTheme
                      ? "bg-amber-500/15 border-amber-400/40 text-amber-200 shadow-amber-950/20"
                      : "bg-gradient-to-r from-amber-50 to-orange-50/90 border-amber-300/80 text-amber-950 shadow-amber-100/50"
                  }`}
                >
                  <div className="p-1 rounded-lg bg-amber-500/20 text-amber-500 shrink-0">
                    <Thermometer className="w-4 h-4 sm:w-5 sm:h-5 text-amber-500 animate-pulse" />
                  </div>
                  <div className="flex items-baseline gap-1.5">
                    <span className="text-xs sm:text-sm font-medium opacity-85">Cảm giác như:</span>
                    <span className="text-base sm:text-lg lg:text-xl font-black text-amber-600 dark:text-amber-300 tracking-tight">
                      {current.apparentTemperature}°C
                    </span>
                  </div>
                </div>

                <div
                  className={`inline-flex items-center gap-1.5 px-3 py-1.5 sm:px-3.5 sm:py-2 rounded-xl sm:rounded-2xl border text-xs sm:text-sm font-semibold ${
                    isDarkTheme
                      ? "bg-white/5 border-white/10 text-slate-300"
                      : "bg-white/90 border-slate-200 text-slate-700 shadow-2xs"
                  }`}
                >
                  <span className="opacity-75">Biên độ:</span>
                  <span className="text-rose-500 font-extrabold">{maxTemp}°</span>
                  <span className="opacity-40">/</span>
                  <span className="text-sky-500 font-extrabold">{minTemp}°</span>
                </div>
              </div>

              {/* Huy hiệu đối chiếu & chuẩn hóa Radar thực tế */}
              {current.radarStatus && (
                <div
                  className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-semibold border transition-all mt-1 ${
                    current.radarStatus.calibrated
                      ? isDarkTheme
                        ? "bg-emerald-500/15 border-emerald-400/40 text-emerald-300"
                        : "bg-emerald-50 border-emerald-400 text-emerald-800 shadow-xs"
                      : current.radarStatus.isRainObserved
                        ? isDarkTheme
                          ? "bg-sky-500/15 border-sky-400/40 text-sky-300"
                          : "bg-sky-50 border-sky-400 text-sky-800 shadow-xs"
                        : isDarkTheme
                          ? "bg-white/5 border-white/10 text-slate-300"
                          : "bg-slate-100 border-slate-300 text-slate-700"
                  }`}
                >
                  <span
                    className={`w-2 h-2 rounded-full shrink-0 ${
                      current.radarStatus.calibrated
                        ? "bg-emerald-400 animate-pulse"
                        : current.radarStatus.isRainObserved
                          ? "bg-sky-400 animate-pulse"
                          : "bg-slate-400"
                    }`}
                  />
                  <span className="truncate max-w-[280px] sm:max-w-none">
                    {current.radarStatus.note ||
                      (current.radarStatus.calibrated
                        ? "Đã hiệu chỉnh theo Radar thực tế (0 dBZ)"
                        : "Khớp quan trắc Radar thực tế")}
                  </span>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* ============================================================== */}
        {/* CỘT BÊN PHẢI: LƯỚI THÔNG SỐ CHI TIẾT THỜI TIẾT TỨC THỜI       */}
        {/* ============================================================== */}
        <div className="lg:col-span-5 w-full">
          <div
            className={`p-3.5 sm:p-5 rounded-2xl border space-y-3 ${
              isDarkTheme
                ? "bg-white/5 border-white/10"
                : "bg-white/85 border-slate-200/80 shadow-md"
            }`}
          >
            <div
              className={`flex items-center justify-between pb-2 border-b text-[11px] sm:text-xs font-bold uppercase tracking-wider ${
                isDarkTheme ? "border-white/10 text-white/80" : "border-slate-200 text-slate-700"
              }`}
            >
              <span className="flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
                Chi tiết thời tiết hiện tại
              </span>
              <span>Cập nhật lúc {current.time.split("T")[1] || "vừa xong"}</span>
            </div>

            {/* Quick Metrics Grid with Hover Dynamics */}
            <div className="grid grid-cols-2 gap-2 sm:gap-3 text-xs">
              {/* 1. AQI */}
              {airQuality && (
                <div
                  className={`p-2.5 sm:p-3 rounded-xl border flex flex-col justify-between transition-all duration-200 hover:scale-[1.02] hover:shadow-xs cursor-default ${
                    isDarkTheme ? "bg-white/5 hover:bg-white/8 border-white/10" : "bg-sky-50/80 hover:bg-sky-100/80 border-sky-200"
                  }`}
                >
                  <div className={`flex items-center justify-between ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>
                    <span className="flex items-center gap-1.5 font-medium text-[11px] sm:text-xs">
                      <div className="p-1 rounded-md bg-emerald-500/15 text-emerald-500 shrink-0">
                        <ShieldCheck className="w-3.5 h-3.5" />
                      </div>
                      Không khí
                    </span>
                    <span className={`font-bold text-[11px] sm:text-xs px-1.5 py-0.5 rounded-full ${
                      isDarkTheme ? "bg-emerald-500/20 text-emerald-300" : "bg-white/90 text-emerald-700 shadow-2xs"
                    }`}>{airQuality.category}</span>
                  </div>
                  <div className={`text-lg sm:text-xl font-bold mt-1 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                    {airQuality.aqiIndex}{" "}
                    <span className={`text-[10px] font-normal ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                      AQI
                    </span>
                  </div>
                  <div className={`text-[10px] mt-0.5 truncate ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                    PM2.5: {airQuality.pm25} µg/m³
                  </div>
                </div>
              )}

              {/* 2. Độ ẩm */}
              <div
                className={`p-2.5 sm:p-3 rounded-xl border flex flex-col justify-between transition-all duration-200 hover:scale-[1.02] hover:shadow-xs cursor-default ${
                  isDarkTheme ? "bg-white/5 hover:bg-white/8 border-white/10" : "bg-blue-50/80 hover:bg-blue-100/80 border-blue-200"
                }`}
              >
                <div className={`flex items-center justify-between ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>
                  <span className="flex items-center gap-1.5 font-medium text-[11px] sm:text-xs">
                    <div className="p-1 rounded-md bg-blue-500/15 text-blue-500 shrink-0">
                      <Droplets className="w-3.5 h-3.5" />
                    </div>
                    Độ ẩm
                  </span>
                </div>
                <div className={`text-lg sm:text-xl font-bold mt-1 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                  {current.relativeHumidity}%
                </div>
                <div className={`text-[10px] mt-0.5 ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                  {current.relativeHumidity > 80 ? "Ẩm ướt / Nồm" : "Thoáng đãng"}
                </div>
              </div>

              {/* 3. Gió */}
              <div
                className={`p-2.5 sm:p-3 rounded-xl border flex flex-col justify-between transition-all duration-200 hover:scale-[1.02] hover:shadow-xs cursor-default ${
                  isDarkTheme ? "bg-white/5 hover:bg-white/8 border-white/10" : "bg-teal-50/80 hover:bg-teal-100/80 border-teal-200"
                }`}
              >
                <div className={`flex items-center justify-between ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>
                  <span className="flex items-center gap-1.5 font-medium text-[11px] sm:text-xs truncate">
                    <div className="p-1 rounded-md bg-teal-500/15 text-teal-500 shrink-0">
                      <Wind className="w-3.5 h-3.5" />
                    </div>
                    Gió ({windDirName})
                  </span>
                </div>
                <div className={`text-lg sm:text-xl font-bold mt-1 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
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
                className={`p-2.5 sm:p-3 rounded-xl border flex flex-col justify-between transition-all duration-200 hover:scale-[1.02] hover:shadow-xs cursor-default ${
                  isDarkTheme ? "bg-white/5 hover:bg-white/8 border-white/10" : "bg-amber-50/80 hover:bg-amber-100/80 border-amber-200"
                }`}
              >
                <div className={`flex items-center justify-between ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>
                  <span className="flex items-center gap-1.5 font-medium text-[11px] sm:text-xs">
                    <div className="p-1 rounded-md bg-amber-500/15 text-amber-500 shrink-0">
                      <ShieldAlert className="w-3.5 h-3.5" />
                    </div>
                    Tia UV
                  </span>
                  <span className={`font-bold text-[11px] sm:text-xs px-1.5 py-0.5 rounded-full ${
                    isDarkTheme ? "bg-amber-500/20 text-amber-300" : "bg-white/90 shadow-2xs"
                  } ${uvColor}`}>{uvLabel}</span>
                </div>
                <div className={`text-lg sm:text-xl font-bold mt-1 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                  {current.uvIndex}{" "}
                  <span className={`text-[10px] font-normal ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                    / 11+
                  </span>
                </div>
                <div className={`text-[10px] mt-0.5 ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                  {current.uvIndex >= 6 ? "Cần chống nắng" : "An toàn"}
                </div>
              </div>

              {/* 5. Tầm nhìn xa */}
              <div
                className={`p-2.5 sm:p-3 rounded-xl border flex flex-col justify-between transition-all duration-200 hover:scale-[1.02] hover:shadow-xs cursor-default ${
                  isDarkTheme ? "bg-white/5 hover:bg-white/8 border-white/10" : "bg-cyan-50/80 hover:bg-cyan-100/80 border-cyan-200"
                }`}
              >
                <div className={`flex items-center justify-between ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>
                  <span className="flex items-center gap-1.5 font-medium text-[11px] sm:text-xs">
                    <div className="p-1 rounded-md bg-cyan-500/15 text-cyan-500 shrink-0">
                      <Eye className="w-3.5 h-3.5" />
                    </div>
                    Tầm nhìn
                  </span>
                </div>
                <div className={`text-lg sm:text-xl font-bold mt-1 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                  {current.visibility}{" "}
                  <span className={`text-[10px] font-normal ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                    km
                  </span>
                </div>
                <div className={`text-[10px] mt-0.5 ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                  {current.visibility >= 10 ? "Tầm nhìn tốt" : "Sương mù"}
                </div>
              </div>

              {/* 6. Áp suất khí quyển */}
              <div
                className={`p-2.5 sm:p-3 rounded-xl border flex flex-col justify-between transition-all duration-200 hover:scale-[1.02] hover:shadow-xs cursor-default ${
                  isDarkTheme ? "bg-white/5 hover:bg-white/8 border-white/10" : "bg-purple-50/80 hover:bg-purple-100/80 border-purple-200"
                }`}
              >
                <div className={`flex items-center justify-between ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>
                  <span className="flex items-center gap-1.5 font-medium text-[11px] sm:text-xs">
                    <div className="p-1 rounded-md bg-purple-500/15 text-purple-500 shrink-0">
                      <Gauge className="w-3.5 h-3.5" />
                    </div>
                    Áp suất
                  </span>
                </div>
                <div className={`text-lg sm:text-xl font-bold mt-1 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
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
                className={`p-3 rounded-xl border flex items-center justify-around text-xs transition-all duration-200 hover:shadow-xs ${
                  isDarkTheme ? "bg-white/5 hover:bg-white/8 border-white/10" : "bg-gradient-to-r from-amber-50/60 via-white/80 to-orange-50/60 border-slate-200/90 shadow-2xs"
                }`}
              >
                <div className="flex items-center gap-2.5">
                  <div className="p-1.5 rounded-lg bg-amber-500/15 text-amber-500 shrink-0">
                    <Sunrise className="w-4 h-4" />
                  </div>
                  <div>
                    <div className={`text-[10px] font-medium ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>Mặt trời mọc</div>
                    <div className={`font-extrabold text-sm ${isDarkTheme ? "text-white" : "text-slate-900"}`}>{todayForecast.sunrise}</div>
                  </div>
                </div>
                <div className={`h-8 w-px ${isDarkTheme ? "bg-white/20" : "bg-slate-200"}`} />
                <div className="flex items-center gap-2.5">
                  <div className="p-1.5 rounded-lg bg-orange-500/15 text-orange-500 shrink-0">
                    <Sunset className="w-4 h-4" />
                  </div>
                  <div>
                    <div className={`text-[10px] font-medium ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>Mặt trời lặn</div>
                    <div className={`font-extrabold text-sm ${isDarkTheme ? "text-white" : "text-slate-900"}`}>{todayForecast.sunset}</div>
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
