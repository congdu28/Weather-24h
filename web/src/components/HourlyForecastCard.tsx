"use client";

import React, { useState, useMemo } from "react";
import { HourlyForecast } from "../types/weather";
import { getWeatherCondition } from "../services/weatherApi";
import WeatherIcon from "./WeatherIcon";
import {
  Clock,
  Droplets,
  Wind,
  Thermometer,
  TrendingUp,
  TrendingDown
} from "lucide-react";

interface Props {
  hourly: HourlyForecast[];
  isDarkTheme?: boolean;
}

export default function HourlyForecastCard({ hourly, isDarkTheme = true }: Props) {
  const [viewMode, setViewMode] = useState<"temp" | "rain" | "wind">("temp");

  // Tìm min/max temp để vẽ biểu đồ thanh xu hướng tương đối
  const { minTemp, maxTemp, range } = useMemo(() => {
    let min = 999;
    let max = -999;
    if (hourly && hourly.length > 0) {
      hourly.forEach((h) => {
        if (h.temperature < min) min = h.temperature;
        if (h.temperature > max) max = h.temperature;
      });
    }
    return {
      minTemp: min === 999 ? 20 : min,
      maxTemp: max === -999 ? 30 : max,
      range: Math.max(1, max - min)
    };
  }, [hourly]);

  if (!hourly || hourly.length === 0) return null;

  return (
    <div className="weather-card glass-specular p-4 sm:p-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4 pb-3 border-b border-black/5 dark:border-white/10">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-xl bg-blue-500/20 text-blue-500 border border-blue-500/30">
            <Clock className="w-4 h-4 sm:w-5 sm:h-5" />
          </div>
          <div>
            <h3 className={`text-base font-extrabold flex items-center gap-2 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
              Dự báo 24 giờ tới
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-blue-500/15 text-blue-600 dark:text-blue-400 font-bold border border-blue-500/30">
                Xu hướng động
              </span>
            </h3>
            <div className="flex items-center gap-3 text-xs mt-0.5">
              <span className={`flex items-center gap-1 ${isDarkTheme ? "text-rose-300" : "text-rose-600 font-semibold"}`}>
                <TrendingUp className="w-3.5 h-3.5" /> Cao nhất: {maxTemp}°
              </span>
              <span className={`flex items-center gap-1 ${isDarkTheme ? "text-sky-300" : "text-sky-600 font-semibold"}`}>
                <TrendingDown className="w-3.5 h-3.5" /> Thấp nhất: {minTemp}°
              </span>
            </div>
          </div>
        </div>

        {/* View Mode Filter Tabs */}
        <div className={`flex items-center p-1 rounded-xl border self-start sm:self-auto shrink-0 ${
          isDarkTheme ? "bg-black/20 border-white/10" : "bg-slate-100 border-slate-200"
        }`}>
          {[
            { id: "temp", label: "Nhiệt độ", icon: Thermometer },
            { id: "rain", label: "Lượng mưa", icon: Droplets },
            { id: "wind", label: "Gió & Ẩm", icon: Wind }
          ].map((tab) => {
            const IconComp = tab.icon;
            const isSelected = viewMode === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setViewMode(tab.id as typeof viewMode)}
                className={`flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg text-xs font-bold transition-all ${
                  isSelected
                    ? isDarkTheme
                      ? "bg-blue-500 text-slate-950 shadow-xs"
                      : "bg-white text-blue-900 shadow-xs"
                    : isDarkTheme
                    ? "text-slate-400 hover:text-white"
                    : "text-slate-600 hover:text-slate-950"
                }`}
              >
                <IconComp className="w-3.5 h-3.5" />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Horizontal Scroll Area */}
      <div className="flex items-stretch gap-3 overflow-x-auto pb-3 pt-3 px-1 scrollbar-thin">
        {hourly.map((h, i) => {
          const cond = getWeatherCondition(h.weatherCode, h.isDay);
          const isCurrent = i === 0;
          const isMax = range >= 2 && h.temperature === maxTemp;
          const isMin = range >= 2 && h.temperature === minTemp;

          // Tính độ cao tương đối cho thanh mini biểu đồ (chiều cao từ 8px đến 42px)
          const barHeight = Math.round(8 + ((h.temperature - minTemp) / range) * 34);

          return (
            <div
              key={i}
              className={`flex flex-col items-center justify-between p-3.5 rounded-2xl min-w-[92px] sm:min-w-[102px] transition-all border shrink-0 relative group hover:scale-[1.02] ${
                isCurrent
                  ? isDarkTheme
                    ? "bg-blue-600/30 border-blue-400/60 shadow-lg shadow-blue-500/20 ring-1 ring-blue-400/40"
                    : "bg-blue-100/95 border-blue-300 shadow-md text-blue-950 font-bold ring-1 ring-blue-400/50"
                  : isDarkTheme
                  ? "bg-white/5 border-white/10 hover:bg-white/10 hover:border-white/20"
                  : "bg-white/80 border-slate-200/80 hover:bg-white shadow-xs"
              }`}
            >
              {/* Badge Top Indicator: MAX / MIN / Hiện tại (đặt an toàn bên trong thẻ, không bị che khuất) */}
              <div className="h-5 flex items-center justify-center mb-1 w-full">
                {isCurrent ? (
                  <span className="px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-600 dark:text-amber-300 border border-amber-500/30 text-[9px] font-black tracking-wider uppercase leading-none shadow-2xs">
                    Hiện tại
                  </span>
                ) : isMax ? (
                  <span className="px-2 py-0.5 rounded-full bg-rose-500 text-white text-[9px] font-black tracking-wider uppercase leading-none shadow-xs">
                    Max
                  </span>
                ) : isMin ? (
                  <span className="px-2 py-0.5 rounded-full bg-sky-500 text-white text-[9px] font-black tracking-wider uppercase leading-none shadow-xs">
                    Min
                  </span>
                ) : (
                  <span className="h-2" />
                )}
              </div>

              {/* Giờ */}
              <span
                className={`text-xs sm:text-sm font-bold ${
                  isCurrent
                    ? isDarkTheme ? "text-amber-300" : "text-blue-700 font-black"
                    : isDarkTheme ? "text-slate-300" : "text-slate-700"
                }`}
              >
                {isCurrent ? "Bây giờ" : h.hour}
              </span>

              {/* Icon thời tiết */}
              <div className="my-2.5 transition-transform group-hover:scale-110">
                <WeatherIcon name={cond.icon} className="w-8 h-8 sm:w-9 sm:h-9" />
              </div>

              {/* Dynamic View Content based on View Mode */}
              {viewMode === "temp" && (
                <div className="flex flex-col items-center w-full">
                  <div className={`text-lg sm:text-xl font-black ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                    {h.temperature}°
                  </div>

                  {/* Thanh biểu đồ nhiệt độ mini */}
                  <div className="w-8 h-11 flex items-end justify-center my-1.5">
                    <div
                      className="w-2.5 rounded-full transition-all duration-300"
                      style={{
                        height: `${barHeight}px`,
                        background: h.temperature >= 32
                          ? "linear-gradient(to top, #f97316, #ef4444)"
                          : h.temperature >= 26
                          ? "linear-gradient(to top, #fbbf24, #f59e0b)"
                          : "linear-gradient(to top, #0ea5e9, #38bdf8)"
                      }}
                    />
                  </div>

                  {/* Khả năng mưa nhỏ phía dưới */}
                  <div className={`flex items-center gap-1 text-[11px] font-semibold ${
                    h.precipitationProbability >= 40
                      ? "text-blue-400 font-bold"
                      : isDarkTheme ? "text-slate-400" : "text-slate-500"
                  }`}>
                    <Droplets className="w-3 h-3 text-blue-500" />
                    <span>{h.precipitationProbability}%</span>
                  </div>
                </div>
              )}

              {viewMode === "rain" && (
                <div className="flex flex-col items-center w-full">
                  <div className={`text-lg sm:text-xl font-black text-blue-500`}>
                    {h.precipitationProbability}%
                  </div>

                  {/* Cột nước mưa */}
                  <div className="w-8 h-11 flex items-end justify-center my-1.5">
                    <div
                      className="w-3 rounded-t-md bg-gradient-to-t from-blue-600 to-cyan-400 transition-all duration-300"
                      style={{
                        height: `${Math.max(6, Math.min(42, (h.precipitationProbability / 100) * 42))}px`
                      }}
                    />
                  </div>

                  <span className={`text-[11px] font-semibold ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                    {h.precipitation > 0 ? `${h.precipitation.toFixed(1)}mm` : "Không mưa"}
                  </span>
                </div>
              )}

              {viewMode === "wind" && (
                <div className="flex flex-col items-center w-full">
                  <div className={`text-base sm:text-lg font-black ${isDarkTheme ? "text-teal-300" : "text-teal-700"}`}>
                    {h.windSpeed} <span className="text-[10px] font-normal">km/h</span>
                  </div>

                  <div className="my-2 p-1.5 rounded-full bg-teal-500/15 text-teal-400">
                    <Wind className="w-4 h-4" />
                  </div>

                  <div className={`text-[11px] font-semibold flex items-center gap-0.5 ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                    <span>Ẩm: {h.relativeHumidity}%</span>
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
