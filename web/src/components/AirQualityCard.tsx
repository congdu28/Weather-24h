"use client";

import React from "react";
import { AirQuality } from "../types/weather";
import { Wind, ShieldCheck, AlertTriangle } from "lucide-react";

interface Props {
  airQuality?: AirQuality;
  isDarkTheme?: boolean;
}

export default function AirQualityCard({ airQuality, isDarkTheme = true }: Props) {
  if (!airQuality) return null;

  return (
    <div
      className={`rounded-3xl p-5 sm:p-6 backdrop-blur-xl border transition-all shadow-xl ${
        isDarkTheme
          ? "bg-slate-900/50 border-white/10 text-white"
          : "bg-white/85 border-slate-200/90 text-slate-900 shadow-sky-500/5"
      }`}
    >
      <div className="flex items-center justify-between pb-3 border-b border-white/10 dark:border-white/10">
        <h3 className={`text-sm font-bold flex items-center gap-2 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
          <Wind className="w-4 h-4 text-cyan-500" />
          Chất lượng không khí (AQI)
        </h3>
        <span className={`text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-500 font-medium"}`}>
          {airQuality.aqiScale}
        </span>
      </div>

      <div className="mt-4 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        {/* AQI Score & Category */}
        <div className="flex items-center gap-4">
          <div
            className={`w-16 h-16 rounded-2xl border flex flex-col items-center justify-center shadow-inner ${
              isDarkTheme
                ? "bg-white/10 border-white/15"
                : "bg-cyan-50 border-cyan-200 shadow-xs"
            }`}
          >
            <span className={`text-2xl font-black ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
              {airQuality.aqiIndex}
            </span>
            <span className={`text-[10px] uppercase font-semibold ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
              AQI
            </span>
          </div>

          <div>
            <div className={`text-base font-bold ${airQuality.color} flex items-center gap-1.5`}>
              {airQuality.aqiIndex <= 40 ? (
                <ShieldCheck className="w-4 h-4 text-emerald-500" />
              ) : (
                <AlertTriangle className="w-4 h-4 text-amber-500" />
              )}
              {airQuality.category}
            </div>
            <p className={`text-xs mt-1 max-w-sm leading-relaxed ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
              {airQuality.advice}
            </p>
          </div>
        </div>

        {/* PM2.5 and PM10 Metrics */}
        <div className="flex items-center gap-3 w-full sm:w-auto">
          <div
            className={`flex-1 sm:flex-none p-3 rounded-2xl border text-center min-w-[90px] ${
              isDarkTheme ? "bg-white/5 border-white/10" : "bg-slate-50 border-slate-200"
            }`}
          >
            <div className={`text-[11px] ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>Bụi PM2.5</div>
            <div className={`text-sm font-bold mt-0.5 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
              {airQuality.pm25} µg/m³
            </div>
          </div>
          <div
            className={`flex-1 sm:flex-none p-3 rounded-2xl border text-center min-w-[90px] ${
              isDarkTheme ? "bg-white/5 border-white/10" : "bg-slate-50 border-slate-200"
            }`}
          >
            <div className={`text-[11px] ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>Bụi PM10</div>
            <div className={`text-sm font-bold mt-0.5 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
              {airQuality.pm10} µg/m³
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
