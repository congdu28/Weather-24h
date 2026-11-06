"use client";

import React from "react";
import { CurrentWeather, DailyForecast } from "../types/weather";
import {
  ShieldAlert,
  Droplets,
  Wind,
  Gauge,
  Eye,
  Sunrise
} from "lucide-react";

interface Props {
  current: CurrentWeather;
  todayForecast?: DailyForecast;
  isDarkTheme?: boolean;
}

export default function WeatherMetricsGrid({
  current,
  todayForecast,
  isDarkTheme = true
}: Props) {
  // UV level text & color
  let uvLabel = "Thấp";
  let uvColor = "text-emerald-500";
  if (current.uvIndex >= 11) { uvLabel = "Cực kỳ nguy hiểm"; uvColor = "text-purple-500"; }
  else if (current.uvIndex >= 8) { uvLabel = "Rất cao"; uvColor = "text-red-500"; }
  else if (current.uvIndex >= 6) { uvLabel = "Cao"; uvColor = "text-amber-500"; }
  else if (current.uvIndex >= 3) { uvLabel = "Trung bình"; uvColor = "text-yellow-600 dark:text-yellow-400"; }

  // Wind compass text
  const directions = ["Bắc", "Đông Bắc", "Đông", "Đông Nam", "Nam", "Tây Nam", "Tây", "Tây Bắc"];
  const windDirIndex = Math.round(current.windDirection / 45) % 8;
  const windDirName = directions[windDirIndex];

  const cardStyle = `p-3.5 sm:p-5 rounded-2xl sm:rounded-3xl border transition-all flex flex-col justify-between ${
    isDarkTheme
      ? "bg-slate-900/85 border-white/10 text-white shadow-md"
      : "bg-white/90 border-slate-200/90 text-slate-900 shadow-md shadow-sky-500/5"
  }`;

  const labelStyle = `flex items-center justify-between text-xs ${
    isDarkTheme ? "text-slate-400" : "text-slate-500 font-medium"
  }`;

  const subTextStyle = `text-[11px] mt-1 ${
    isDarkTheme ? "text-slate-300" : "text-slate-600"
  }`;

  return (
    <div className="grid grid-cols-2 md:grid-cols-3 gap-3 sm:gap-4">
      {/* 1. UV Index */}
      <div className={cardStyle}>
        <div className={labelStyle}>
          <span className="flex items-center gap-1.5 font-medium">
            <ShieldAlert className="w-4 h-4 text-amber-500" />
            Tia UV
          </span>
          <span className={`font-bold ${uvColor}`}>{uvLabel}</span>
        </div>
        <div className="my-2">
          <span className={`text-2xl sm:text-3xl font-extrabold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
            {current.uvIndex}
          </span>
          <span className={`text-xs ml-1 ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>/ 11+</span>
        </div>
        <div className={subTextStyle}>
          {current.uvIndex >= 6 ? "Nên thoa kem chống nắng & đeo kính râm." : "Mức độ an toàn ngoài trời."}
        </div>
      </div>

      {/* 2. Humidity */}
      <div className={cardStyle}>
        <div className={labelStyle}>
          <span className="flex items-center gap-1.5 font-medium">
            <Droplets className="w-4 h-4 text-blue-500" />
            Độ ẩm không khí
          </span>
        </div>
        <div className="my-2">
          <span className={`text-2xl sm:text-3xl font-extrabold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
            {current.relativeHumidity}
          </span>
          <span className={`text-sm ml-0.5 ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>%</span>
        </div>
        <div className={subTextStyle}>
          {current.relativeHumidity > 80 ? "Độ ẩm cao, trời nồm ẩm hoặc sắp mưa." : "Độ ẩm dễ chịu, thoáng đãng."}
        </div>
      </div>

      {/* 3. Wind */}
      <div className={cardStyle}>
        <div className={labelStyle}>
          <span className="flex items-center gap-1.5 font-medium">
            <Wind className="w-4 h-4 text-teal-500" />
            Gió
          </span>
          <span className={isDarkTheme ? "text-slate-300 font-medium" : "text-slate-700 font-semibold"}>
            Hướng {windDirName}
          </span>
        </div>
        <div className="my-2">
          <span className={`text-2xl sm:text-3xl font-extrabold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
            {current.windSpeed}
          </span>
          <span className={`text-xs ml-1 ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>km/h</span>
        </div>
        <div className={subTextStyle}>
          {current.windSpeed > 25 ? "Gió giật mạnh, chú ý di chuyển." : "Gió nhẹ, thời tiết êm dịu."}
        </div>
      </div>

      {/* 4. Surface Pressure */}
      <div className={cardStyle}>
        <div className={labelStyle}>
          <span className="flex items-center gap-1.5 font-medium">
            <Gauge className="w-4 h-4 text-purple-500" />
            Áp suất khí quyển
          </span>
        </div>
        <div className="my-2">
          <span className={`text-2xl sm:text-3xl font-extrabold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
            {current.surfacePressure}
          </span>
          <span className={`text-xs ml-1 ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>hPa</span>
        </div>
        <div className={subTextStyle}>
          Áp suất khí quyển chuẩn mực bề mặt.
        </div>
      </div>

      {/* 5. Visibility */}
      <div className={cardStyle}>
        <div className={labelStyle}>
          <span className="flex items-center gap-1.5 font-medium">
            <Eye className="w-4 h-4 text-cyan-500" />
            Tầm nhìn xa
          </span>
        </div>
        <div className="my-2">
          <span className={`text-2xl sm:text-3xl font-extrabold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
            {current.visibility}
          </span>
          <span className={`text-xs ml-1 ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>km</span>
        </div>
        <div className={subTextStyle}>
          {current.visibility >= 10 ? "Tầm nhìn rất rõ ràng, không sương mù." : "Tầm nhìn hạn chế, đề phòng sương."}
        </div>
      </div>

      {/* 6. Sunrise & Sunset */}
      <div className={cardStyle}>
        <div className={labelStyle}>
          <span className="flex items-center gap-1.5 font-medium">
            <Sunrise className="w-4 h-4 text-amber-500" />
            Bình minh & Hoàng hôn
          </span>
        </div>
        <div className="my-2 flex items-center justify-between pr-2">
          <div>
            <div className={`text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>Mọc</div>
            <div className="text-base font-bold text-amber-600 dark:text-amber-300">
              {todayForecast?.sunrise || "06:00"}
            </div>
          </div>
          <div className={`h-7 w-px ${isDarkTheme ? "bg-white/15" : "bg-slate-300"}`} />
          <div>
            <div className={`text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>Lặn</div>
            <div className="text-base font-bold text-orange-600 dark:text-orange-400">
              {todayForecast?.sunset || "18:00"}
            </div>
          </div>
        </div>
        <div className={subTextStyle}>
          Giờ chiếu sáng tự nhiên trong ngày.
        </div>
      </div>
    </div>
  );
}
