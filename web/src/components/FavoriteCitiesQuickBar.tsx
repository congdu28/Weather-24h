"use client";

import React from "react";
import { CityLocation } from "../types/weather";
import { Crosshair, Plus, Star, X } from "lucide-react";

interface Props {
  currentCityName: string;
  favoriteCities: CityLocation[];
  onSelectCity: (city: CityLocation) => void;
  onRemoveCity: (city: CityLocation) => void;
  onSelectGps: () => void;
  onOpenSearch: () => void;
  isDarkTheme?: boolean;
}

export default function FavoriteCitiesQuickBar({
  currentCityName,
  favoriteCities,
  onSelectCity,
  onRemoveCity,
  onSelectGps,
  onOpenSearch,
  isDarkTheme = true
}: Props) {
  return (
    <div className="w-full flex items-center gap-2.5 overflow-x-auto pb-1.5 pt-1 scrollbar-none select-none">
      {/* Nút GPS */}
      <button
        onClick={onSelectGps}
        className={`flex items-center gap-2 px-3.5 py-2 rounded-full border text-xs sm:text-sm font-semibold shrink-0 transition-all active:scale-95 shadow-xs ${
          isDarkTheme
            ? "bg-white/10 hover:bg-white/20 border-white/15 text-slate-200"
            : "bg-white hover:bg-slate-50 border-slate-300 text-slate-800 shadow-sm"
        }`}
      >
        <Crosshair className="w-4 h-4 text-sky-500" />
        <span>Vị trí GPS</span>
      </button>

      {/* Danh sách thành phố yêu thích */}
      {favoriteCities.map((city, idx) => {
        const isSelected = city.name.trim().toLowerCase() === currentCityName.trim().toLowerCase();

        return (
          <div
            key={`${city.latitude}-${city.longitude}-${idx}`}
            className={`group inline-flex items-center gap-1.5 pl-3.5 pr-1.5 py-1.5 rounded-full border text-xs sm:text-sm font-semibold shrink-0 transition-all ${
              isSelected
                ? "bg-amber-500 text-slate-950 border-amber-400 shadow-md shadow-amber-500/25 font-bold"
                : isDarkTheme
                ? "bg-white/10 hover:bg-white/15 text-slate-200 border-white/15"
                : "bg-white hover:bg-slate-50 text-slate-700 border-slate-300 shadow-xs"
            }`}
          >
            <button
              onClick={() => onSelectCity(city)}
              className="flex items-center gap-1.5 cursor-pointer text-left focus:outline-hidden"
              title={`Xem thời tiết tại ${city.name}`}
            >
              <Star
                className={`w-3.5 h-3.5 shrink-0 ${
                  isSelected ? "fill-slate-950 text-slate-950" : "fill-amber-400 text-amber-500"
                }`}
              />
              <span className="truncate max-w-[140px] sm:max-w-[200px]">{city.name}</span>
            </button>

            {/* Nút Xóa nhanh khỏi danh sách yêu thích */}
            <button
              onClick={(e) => {
                e.stopPropagation();
                onRemoveCity(city);
              }}
              className={`p-1 rounded-full transition-colors ${
                isSelected
                  ? "hover:bg-slate-900/20 text-slate-950/70 hover:text-slate-950"
                  : isDarkTheme
                  ? "hover:bg-white/20 text-slate-400 hover:text-rose-400"
                  : "hover:bg-slate-200 text-slate-400 hover:text-rose-600"
              }`}
              title={`Xóa ${city.name} khỏi danh sách yêu thích`}
            >
              <X className="w-3.5 h-3.5" />
            </button>
          </div>
        );
      })}

      {/* Nút thêm thành phố */}
      <button
        onClick={onOpenSearch}
        className={`flex items-center gap-1.5 px-3 py-2 rounded-full border text-xs sm:text-sm font-medium shrink-0 transition-all ${
          isDarkTheme
            ? "bg-white/5 hover:bg-white/15 border-white/10 text-slate-400 hover:text-white"
            : "bg-white hover:bg-slate-50 border-slate-300 text-slate-600 hover:text-slate-900 shadow-xs"
        }`}
        title="Thêm thành phố vào danh sách"
      >
        <Plus className="w-4 h-4 text-amber-500" />
        <span className="hidden sm:inline">Thêm</span>
      </button>
    </div>
  );
}
