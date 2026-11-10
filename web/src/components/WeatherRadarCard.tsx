"use client";

import React, { useState } from "react";
import { Radar, ExternalLink, MapPin } from "lucide-react";
import { CityLocation } from "../types/weather";

interface Props {
  location: CityLocation;
  isDarkTheme?: boolean;
}

export default function WeatherRadarCard({ location, isDarkTheme = true }: Props) {
  const [layer, setLayer] = useState<"radar" | "clouds" | "wind">("radar");
  const [isInteractive, setIsInteractive] = useState(false);

  // Windy embed URL with responsive parameter and overlay
  const windyOverlay = layer === "radar" ? "radar" : layer === "clouds" ? "clouds" : "wind";
  const embedUrl = `https://embed.windy.com/embed2.html?lat=${location.latitude}&lon=${location.longitude}&detailLat=${location.latitude}&detailLon=${location.longitude}&width=100%25&height=480&zoom=7&level=surface&overlay=${windyOverlay}&product=radar&menu=&message=true&marker=true&calendar=now&pressure=&type=map&location=coordinates&detail=&metricWind=km%2Fh&metricTemp=%C2%B0C&radarRange=-1`;

  return (
    <div
      className={`weather-card glass-specular p-4 sm:p-6 ${
        isDarkTheme ? "text-white" : "text-slate-900"
      }`}
    >
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 sm:pb-4 border-b border-black/5 dark:border-white/10">
        <div>
          <h3 className={`text-base font-extrabold flex items-center gap-2 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
            <Radar className="w-4 h-4 text-emerald-500" />
            Radar Thời Tiết Trực Tiếp
          </h3>
          <p className={`text-xs sm:text-sm mt-0.5 flex items-center gap-1 font-medium ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
            <MapPin className="w-3.5 h-3.5 text-amber-500 shrink-0" />
            Theo dõi mây mưa thời gian thực tại {location.name}
          </p>
        </div>

        {/* Layer Selector */}
        <div
          className={`flex items-center gap-1 p-1 rounded-xl border self-start sm:self-auto ${
            isDarkTheme ? "bg-white/10 border-white/10" : "bg-slate-100 border-slate-200"
          }`}
        >
          <button
            onClick={() => setLayer("radar")}
            className={`px-2.5 sm:px-3 py-1 rounded-lg text-xs font-semibold transition-all ${
              layer === "radar"
                ? "bg-emerald-600 text-white shadow-md shadow-emerald-600/30"
                : isDarkTheme ? "text-slate-300 hover:text-white" : "text-slate-600 hover:text-slate-900"
            }`}
          >
            Radar Mưa
          </button>
          <button
            onClick={() => setLayer("clouds")}
            className={`px-2.5 sm:px-3 py-1 rounded-lg text-xs font-semibold transition-all ${
              layer === "clouds"
                ? "bg-blue-600 text-white shadow-md shadow-blue-600/30"
                : isDarkTheme ? "text-slate-300 hover:text-white" : "text-slate-600 hover:text-slate-900"
            }`}
          >
            Lớp Mây
          </button>
          <button
            onClick={() => setLayer("wind")}
            className={`px-2.5 sm:px-3 py-1 rounded-lg text-xs font-semibold transition-all ${
              layer === "wind"
                ? "bg-teal-600 text-white shadow-md shadow-teal-600/30"
                : isDarkTheme ? "text-slate-300 hover:text-white" : "text-slate-600 hover:text-slate-900"
            }`}
          >
            Gió
          </button>
        </div>
      </div>

      {/* Radar Map Container (with scroll protection) */}
      <div className="relative mt-3 sm:mt-4 w-full h-[320px] sm:h-[400px] md:h-[460px] rounded-2xl overflow-hidden border border-white/15 shadow-inner bg-slate-950">
        <iframe
          src={embedUrl}
          title="Windy Weather Radar"
          className={`w-full h-full border-0 transition-opacity ${isInteractive ? "pointer-events-auto" : "pointer-events-none"}`}
          loading="lazy"
          allow="geolocation"
        />

        {/* Scroll overlay: Prevents iframe from intercepting mouse wheel / touch scroll */}
        {!isInteractive && (
          <div
            onClick={() => setIsInteractive(true)}
            className="absolute inset-0 bg-transparent cursor-pointer flex items-center justify-center group"
          >
            <span className="opacity-0 group-hover:opacity-100 transition-opacity px-4 py-2 rounded-xl bg-slate-900/85 text-white text-xs font-semibold border border-white/20 shadow-lg">
              👆 Nhấp để tương tác bản đồ
            </span>
          </div>
        )}

        {isInteractive && (
          <button
            onClick={() => setIsInteractive(false)}
            className="absolute top-3 left-3 px-3 py-1.5 rounded-xl bg-slate-900/90 text-white text-xs font-semibold border border-white/20 shadow-lg z-10 hover:bg-slate-800 transition-all"
          >
            🔒 Khóa bản đồ để cuộn trang
          </button>
        )}

        {/* Open in full Windy badge */}
        <a
          href={`https://www.windy.com/?${location.latitude},${location.longitude},8`}
          target="_blank"
          rel="noopener noreferrer"
          className="absolute bottom-3 right-3 px-3 py-1.5 rounded-xl bg-slate-900/90 hover:bg-slate-800 text-white text-xs font-medium border border-white/20 shadow-lg flex items-center gap-1.5 transition-all"
        >
          <span>Mở toàn màn hình</span>
          <ExternalLink className="w-3.5 h-3.5 text-slate-300" />
        </a>
      </div>
    </div>
  );
}
