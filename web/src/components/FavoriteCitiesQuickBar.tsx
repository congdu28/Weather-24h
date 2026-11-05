"use client";

import React from "react";
import { CityLocation } from "../types/weather";
import { Crosshair, Plus, Star } from "lucide-react";

interface Props {
  currentCityName: string;
  favoriteCities: CityLocation[];
  onSelectCity: (city: CityLocation) => void;
  onSelectGps: () => void;
  onOpenSearch: () => void;
  isDarkTheme?: boolean;
}

export default function FavoriteCitiesQuickBar({
  currentCityName,
  favoriteCities,
  onSelectCity,
  onSelectGps,
  onOpenSearch,
  isDarkTheme = true
}: Props) {
  if (favoriteCities.length === 0) return null;

  return (
    <div className="w-full flex items-center gap-2 overflow-x-auto pb-1 pt-1 scrollbar-none select-none">
      {/* Nút GPS */}
      <button
        onClick={onSelectGps}
        className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full border text-xs font-semibold shrink-0 transition-all active:scale-95 ${
          isDarkTheme
            ? "bg-white/10 hover:bg-white/20 border-white/15 text-slate-200"
            : "bg-white/90 hover:bg-white border-slate-300 text-slate-800 shadow-sm"
        }`}
      >
        <Crosshair className="w-3.5 h-3.5 text-blue-500" />
        <span>Vị trí GPS</span>
      </button>

      {/* Danh sách thành phố yêu thích */}
      {favoriteCities.map((city, idx) => {
        const isSelected = city.name.toLowerCase() === currentCityName.toLowerCase();

        return (
          <button
            key={`${city.latitude}-${city.longitude}-${idx}`}
            onClick={() => onSelectCity(city)}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full border text-xs font-semibold shrink-0 transition-all active:scale-95 ${
              isSelected
                ? "bg-amber-500 text-slate-950 border-amber-400 shadow-md shadow-amber-500/20 font-bold"
                : isDarkTheme
                ? "bg-white/10 hover:bg-white/15 text-slate-200 border-white/15"
                : "bg-white/90 hover:bg-white text-slate-700 border-slate-300 shadow-xs"
            }`}
          >
            <Star className={`w-3 h-3 ${isSelected ? "fill-slate-950 text-slate-950" : "fill-amber-400 text-amber-500"}`} />
            <span>{city.name}</span>
          </button>
        );
      })}

      {/* Nút thêm thành phố */}
      <button
        onClick={onOpenSearch}
        className={`flex items-center gap-1 px-2.5 py-1.5 rounded-full border text-xs shrink-0 transition-all ${
          isDarkTheme
            ? "bg-white/5 hover:bg-white/15 border-white/10 text-slate-400 hover:text-white"
            : "bg-white/80 hover:bg-white border-slate-300 text-slate-600 hover:text-slate-900 shadow-xs"
        }`}
        title="Thêm thành phố"
      >
        <Plus className="w-3.5 h-3.5" />
      </button>
    </div>
  );
}
