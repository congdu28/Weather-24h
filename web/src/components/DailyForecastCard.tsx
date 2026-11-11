"use client";

import React, { useState, useEffect, useSyncExternalStore } from "react";
import { createPortal } from "react-dom";
import { DailyForecast } from "../types/weather";
import { getWeatherCondition } from "../services/weatherApi";
import { getLunarDate } from "../utils/lunarCalendar";
import WeatherIcon from "./WeatherIcon";
import { CalendarDays, Droplets, Sun, Sunset, X, ShieldAlert } from "lucide-react";

interface Props {
  daily: DailyForecast[];
  showLunar?: boolean;
  isDarkTheme?: boolean;
}

const emptySubscribe = () => () => {};

export default function DailyForecastCard({
  daily,
  showLunar = true,
  isDarkTheme = true
}: Props) {
  const [selectedDay, setSelectedDay] = useState<DailyForecast | null>(null);
  const mounted = useSyncExternalStore(emptySubscribe, () => true, () => false);

  // Khóa scroll khi mở modal chi tiết ngày
  useEffect(() => {
    if (selectedDay) {
      document.body.style.overflow = "hidden";
      return () => {
        document.body.style.overflow = "unset";
      };
    }
  }, [selectedDay]);

  if (!daily || daily.length === 0) return null;

  const allMins = daily.map(d => d.tempMin);
  const allMaxs = daily.map(d => d.tempMax);
  const globalMin = Math.min(...allMins);
  const globalMax = Math.max(...allMaxs);
  const range = Math.max(1, globalMax - globalMin);

  return (
    <>
      <div
        className={`weather-card glass-specular p-4 sm:p-6 ${
          isDarkTheme ? "text-white" : "text-slate-900"
        }`}
      >
        <div className="flex items-center justify-between mb-4 pb-2 border-b border-black/5 dark:border-white/10">
          <h3 className={`text-base font-extrabold flex items-center gap-2 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
            <CalendarDays className="w-4 h-4 text-emerald-500" />
            Dự báo 7 ngày tới
          </h3>
          <span className={`text-xs ${isDarkTheme ? "text-slate-300 font-medium" : "text-slate-600 font-semibold"}`}>
            Chạm để xem chi tiết ngày
          </span>
        </div>

        <div className="space-y-2.5">
          {daily.map((day, i) => {
            const cond = getWeatherCondition(day.weatherCode, true);
            const iconName = day.conditionIcon || cond.icon;
            const leftPercent = Math.max(0, Math.min(100, ((day.tempMin - globalMin) / range) * 100));
            const widthPercent = Math.max(15, Math.min(100 - leftPercent, ((day.tempMax - day.tempMin) / range) * 100));

            // Parse Lunar date for this day
            const dParts = day.date.split("-").map(Number);
            const lunar = getLunarDate(dParts[2], dParts[1], dParts[0]);
            const isFullMoon = lunar.day === 15;
            const isNewMoon = lunar.day === 1;

            return (
              <div
                key={i}
                onClick={() => setSelectedDay(day)}
                className={`flex items-center justify-between p-2.5 sm:p-3 rounded-2xl border transition-all cursor-pointer gap-2 sm:gap-3 group active:scale-[0.99] ${
                  isDarkTheme
                    ? "bg-white/5 border-white/10 hover:bg-white/10"
                    : "bg-slate-50/90 border-slate-200/80 hover:bg-slate-100 shadow-sm"
                }`}
              >
                {/* Day Name & Lunar Date */}
                <div className="w-20 sm:w-28 shrink-0">
                  <div
                    className={`text-xs sm:text-sm font-semibold transition-colors ${
                      isDarkTheme ? "text-white group-hover:text-amber-300" : "text-slate-900 group-hover:text-amber-600"
                    }`}
                  >
                    {day.dayOfWeek}
                  </div>
                  {showLunar && (
                    <div
                      className={`text-[10px] sm:text-[11px] font-medium truncate ${
                        isDarkTheme ? "text-amber-300/80" : "text-amber-700 font-semibold"
                      }`}
                    >
                      {isFullMoon ? "🌕 " : isNewMoon ? "🌑 " : ""}
                      {lunar.formattedLunarShort}
                    </div>
                  )}
                </div>

                {/* Weather Icon & Rain */}
                <div className="flex items-center gap-2 shrink-0 w-24 sm:w-28">
                  <WeatherIcon name={iconName} className="w-6 h-6" />
                  {day.precipitationProbabilityMax >= 20 ? (
                    <div
                      className={`flex items-center gap-0.5 text-[11px] sm:text-xs font-semibold ${
                        isDarkTheme ? "text-blue-300" : "text-blue-600"
                      }`}
                      title={`Lượng mưa dự báo: ${day.precipitationSum} mm`}
                    >
                      <Droplets className="w-3 h-3 text-blue-500 shrink-0" />
                      <span>{day.precipitationProbabilityMax}%</span>
                    </div>
                  ) : day.precipitationProbabilityMax > 0 ? (
                    <span
                      className={`text-[11px] sm:text-xs flex items-center gap-0.5 ${
                        isDarkTheme ? "text-slate-400" : "text-slate-500 font-medium"
                      }`}
                      title={`Xác suất mưa thấp (${day.precipitationProbabilityMax}%), lượng mưa ${day.precipitationSum} mm`}
                    >
                      <Droplets className="w-2.5 h-2.5 opacity-50 shrink-0" />
                      <span>{day.precipitationProbabilityMax}%</span>
                    </span>
                  ) : (
                    <span className={`text-[11px] sm:text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                      Khô ráo
                    </span>
                  )}
                </div>

                {/* Temperature Bar */}
                <div className="flex-1 flex items-center gap-2">
                  <span
                    className={`text-xs font-medium w-7 text-right ${
                      isDarkTheme ? "text-slate-300" : "text-slate-600 font-semibold"
                    }`}
                  >
                    {day.tempMin}°
                  </span>

                  <div
                    className={`flex-1 h-2 rounded-full relative overflow-hidden hidden sm:block ${
                      isDarkTheme ? "bg-slate-800/80" : "bg-slate-200"
                    }`}
                  >
                    <div
                      className="absolute top-0 bottom-0 rounded-full bg-gradient-to-r from-cyan-400 to-amber-400"
                      style={{
                        left: `${leftPercent}%`,
                        width: `${widthPercent}%`
                      }}
                    />
                  </div>

                  <span
                    className={`text-xs font-bold w-7 ${
                      isDarkTheme ? "text-white" : "text-slate-900"
                    }`}
                  >
                    {day.tempMax}°
                  </span>
                </div>

                {/* Sun times (desktop) */}
                <div
                  className={`hidden md:flex items-center gap-2 text-[11px] shrink-0 ${
                    isDarkTheme ? "text-slate-400" : "text-slate-500"
                  }`}
                >
                  <span className="flex items-center gap-0.5">
                    <Sun className="w-3 h-3 text-amber-500" /> {day.sunrise}
                  </span>
                  <span>•</span>
                  <span className="flex items-center gap-0.5">
                    <Sunset className="w-3 h-3 text-orange-500" /> {day.sunset}
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Selected Day Details Modal */}
      {selectedDay && mounted && createPortal(
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/75 backdrop-blur-md animate-in fade-in">
          <div
            className={`w-full max-w-md border rounded-3xl p-6 shadow-2xl space-y-4 ${
              isDarkTheme ? "bg-slate-900 border-white/20 text-white" : "bg-white border-slate-200 text-slate-900"
            }`}
          >
            <div className="flex items-center justify-between pb-3 border-b border-white/10 dark:border-white/10">
              <div>
                <h3 className={`text-base font-bold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                  Dự báo ngày {selectedDay.dayOfWeek}
                </h3>
                <p className={`text-xs ${isDarkTheme ? "text-slate-300 font-medium" : "text-slate-500"}`}>
                  Ngày {selectedDay.date.split("-").reverse().join("/")}
                </p>
              </div>
              <button
                onClick={() => setSelectedDay(null)}
                className={`p-1.5 rounded-full transition-colors ${
                  isDarkTheme ? "hover:bg-white/10 text-slate-300 hover:text-white" : "hover:bg-slate-100 text-slate-500 hover:text-slate-900"
                }`}
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Weather summary */}
            <div
              className={`p-4 rounded-2xl border flex items-center justify-between ${
                isDarkTheme ? "bg-white/5 border-white/10" : "bg-slate-50 border-slate-200"
              }`}
            >
              <div>
                <div className={`text-2xl font-black ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                  {selectedDay.tempMax}° / {selectedDay.tempMin}°C
                </div>
                <div
                  className={`text-xs font-semibold mt-1 ${
                    isDarkTheme ? "text-amber-300" : "text-amber-700 font-bold"
                  }`}
                >
                  {selectedDay.conditionDescription || getWeatherCondition(selectedDay.weatherCode, true).description}
                </div>
              </div>
              <WeatherIcon
                name={selectedDay.conditionIcon || getWeatherCondition(selectedDay.weatherCode, true).icon}
                className="w-12 h-12"
              />
            </div>

            {/* Metrics Grid */}
            <div className="grid grid-cols-2 gap-3 text-xs">
              <div
                className={`p-3 rounded-xl border ${
                  isDarkTheme ? "bg-white/5 border-white/10" : "bg-blue-50/70 border-blue-100"
                }`}
              >
                <div className={`flex items-center gap-1 ${isDarkTheme ? "text-slate-200 font-bold" : "text-slate-700 font-bold"}`}>
                  <Droplets className="w-3.5 h-3.5 text-blue-500" />
                  Xác suất mưa
                </div>
                <div className={`text-sm font-bold mt-1 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                  {selectedDay.precipitationProbabilityMax}% ({selectedDay.precipitationSum} mm{
                    selectedDay.precipitationSum <= 0.5 && selectedDay.precipitationSum > 0
                      ? " • Vết mưa không đáng kể"
                      : selectedDay.precipitationSum === 0
                      ? " • Khô ráo"
                      : ""
                  })
                </div>
              </div>

              <div
                className={`p-3 rounded-xl border ${
                  isDarkTheme ? "bg-white/5 border-white/10" : "bg-amber-50/70 border-amber-100"
                }`}
              >
                <div className={`flex items-center gap-1 ${isDarkTheme ? "text-slate-200 font-bold" : "text-slate-700 font-bold"}`}>
                  <ShieldAlert className="w-3.5 h-3.5 text-amber-500" />
                  Tia cực tím UV Max
                </div>
                <div className={`text-sm font-bold mt-1 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                  {selectedDay.uvIndexMax} / 11+
                </div>
              </div>

              <div
                className={`p-3 rounded-xl border ${
                  isDarkTheme ? "bg-white/5 border-white/10" : "bg-orange-50/70 border-orange-100"
                }`}
              >
                <div className={`flex items-center gap-1 ${isDarkTheme ? "text-slate-200 font-bold" : "text-slate-700 font-bold"}`}>
                  <Sun className="w-3.5 h-3.5 text-amber-500" />
                  Mặt trời mọc
                </div>
                <div className={`text-sm font-bold mt-1 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                  {selectedDay.sunrise}
                </div>
              </div>

              <div
                className={`p-3 rounded-xl border ${
                  isDarkTheme ? "bg-white/5 border-white/10" : "bg-purple-50/70 border-purple-100"
                }`}
              >
                <div className={`flex items-center gap-1 ${isDarkTheme ? "text-slate-200 font-bold" : "text-slate-700 font-bold"}`}>
                  <Sunset className="w-3.5 h-3.5 text-orange-500" />
                  Mặt trời lặn
                </div>
                <div className={`text-sm font-bold mt-1 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                  {selectedDay.sunset}
                </div>
              </div>
            </div>

            <button
              onClick={() => setSelectedDay(null)}
              className={`w-full py-2.5 rounded-xl font-semibold text-xs transition-colors ${
                isDarkTheme ? "bg-white/10 hover:bg-white/20 text-white" : "bg-slate-900 hover:bg-slate-800 text-white"
              }`}
            >
              Đóng
            </button>
          </div>
        </div>,
        document.body
      )}
    </>
  );
}
