"use client";

import React from "react";
import { HourlyForecast } from "../types/weather";
import { getWeatherCondition } from "../services/weatherApi";
import WeatherIcon from "./WeatherIcon";
import { Clock, Droplets, Wind } from "lucide-react";

interface Props {
  hourly: HourlyForecast[];
  isDarkTheme?: boolean;
}

export default function HourlyForecastCard({ hourly, isDarkTheme = true }: Props) {
  if (!hourly || hourly.length === 0) return null;

  return (
    <div
      className={`rounded-3xl p-5 sm:p-6 backdrop-blur-xl border transition-all shadow-xl ${
        isDarkTheme
          ? "bg-slate-900/50 border-white/10 text-white"
          : "bg-white/75 border-white/60 text-slate-900 shadow-sky-500/10"
      }`}
    >
      <div className="flex items-center justify-between mb-4 pb-2 border-b border-black/5 dark:border-white/10">
        <h3 className={`text-base font-extrabold flex items-center gap-2 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
          <Clock className="w-4 h-4 text-blue-500" />
          Dự báo 24 giờ tới
        </h3>
        <span className={`text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-600 font-semibold"}`}>
          Cuộn ngang xem thêm
        </span>
      </div>

      {/* Horizontal Scroll Area */}
      <div className="flex items-center gap-3 overflow-x-auto pb-3 pt-1 scrollbar-thin">
        {hourly.map((h, i) => {
          const cond = getWeatherCondition(h.weatherCode, h.isDay);
          const isCurrent = i === 0;

          return (
            <div
              key={i}
              className={`flex flex-col items-center justify-between p-3.5 rounded-2xl min-w-[88px] sm:min-w-[98px] transition-all border shrink-0 ${
                isCurrent
                  ? isDarkTheme
                    ? "bg-blue-600/30 border-blue-400/50 shadow-lg shadow-blue-500/20"
                    : "bg-blue-100/90 border-blue-300 shadow-md text-blue-950 font-bold"
                  : isDarkTheme
                  ? "bg-white/5 border-white/10 hover:bg-white/10"
                  : "bg-white/80 border-slate-200/80 hover:bg-white shadow-sm"
              }`}
            >
              <span
                className={`text-xs sm:text-sm font-bold ${
                  isCurrent
                    ? isDarkTheme ? "text-amber-300" : "text-blue-700 font-black"
                    : isDarkTheme ? "text-slate-300" : "text-slate-700"
                }`}
              >
                {isCurrent ? "Bây giờ" : h.hour}
              </span>

              <div className="my-2.5">
                <WeatherIcon name={cond.icon} className="w-8 h-8 sm:w-9 sm:h-9" />
              </div>

              <div className={`text-lg sm:text-xl font-black ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                {h.temperature}°
              </div>

              {/* Rain Chance */}
              <div
                className={`mt-2 flex items-center gap-1 text-xs font-semibold ${
                  isDarkTheme ? "text-blue-300" : "text-blue-700"
                }`}
              >
                <Droplets className="w-3.5 h-3.5 text-blue-500" />
                <span>{h.precipitationProbability}%</span>
              </div>

              {/* Wind Speed */}
              <div
                className={`mt-1 flex items-center gap-0.5 text-[11px] font-medium ${
                  isDarkTheme ? "text-slate-400" : "text-slate-600"
                }`}
              >
                <Wind className="w-3 h-3 text-teal-500" />
                <span>{h.windSpeed} km/h</span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
