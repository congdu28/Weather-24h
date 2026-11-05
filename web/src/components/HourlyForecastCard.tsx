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
          : "bg-white/85 border-slate-200/90 text-slate-900 shadow-sky-500/5"
      }`}
    >
      <div className="flex items-center justify-between mb-4 pb-2 border-b border-white/10 dark:border-white/10">
        <h3 className={`text-sm font-bold flex items-center gap-2 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
          <Clock className="w-4 h-4 text-blue-500" />
          Dự báo 24 giờ tới
        </h3>
        <span className={`text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-500 font-medium"}`}>
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
              className={`flex flex-col items-center justify-between p-3.5 rounded-2xl min-w-[85px] sm:min-w-[95px] transition-all border shrink-0 ${
                isCurrent
                  ? isDarkTheme
                    ? "bg-blue-600/30 border-blue-400/50 shadow-lg shadow-blue-500/20"
                    : "bg-blue-50 border-blue-300 shadow-md text-blue-900"
                  : isDarkTheme
                  ? "bg-white/5 border-white/10 hover:bg-white/10"
                  : "bg-slate-50/90 border-slate-200/80 hover:bg-slate-100 shadow-sm"
              }`}
            >
              <span
                className={`text-xs font-semibold ${
                  isCurrent
                    ? isDarkTheme ? "text-amber-300" : "text-blue-700 font-bold"
                    : isDarkTheme ? "text-slate-300" : "text-slate-600"
                }`}
              >
                {isCurrent ? "Bây giờ" : h.hour}
              </span>

              <div className="my-2.5">
                <WeatherIcon name={cond.icon} className="w-8 h-8" />
              </div>

              <div className={`text-base font-extrabold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                {h.temperature}°
              </div>

              {/* Rain Chance */}
              <div
                className={`mt-2 flex items-center gap-1 text-[11px] font-medium ${
                  isDarkTheme ? "text-blue-300" : "text-blue-600"
                }`}
              >
                <Droplets className="w-3 h-3 text-blue-500" />
                <span>{h.precipitationProbability}%</span>
              </div>

              {/* Wind Speed */}
              <div
                className={`mt-1 flex items-center gap-0.5 text-[10px] ${
                  isDarkTheme ? "text-slate-400" : "text-slate-500"
                }`}
              >
                <Wind className="w-2.5 h-2.5" />
                <span>{h.windSpeed} km/h</span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
