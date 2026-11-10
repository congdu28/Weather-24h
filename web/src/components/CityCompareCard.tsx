"use client";

import React, { useState, useEffect } from "react";
import { CityLocation } from "../types/weather";
import { getWeatherCondition, VIETNAM_MAJOR_CITIES } from "../services/weatherApi";
import WeatherIcon from "./WeatherIcon";
import {
  Layers,
  ArrowRight,
  Plus,
  RefreshCw,
  Check
} from "lucide-react";

interface Props {
  currentCity: CityLocation;
  favoriteCities: CityLocation[];
  onSelectCity: (city: CityLocation) => void;
  onOpenSearch: () => void;
  isDarkTheme?: boolean;
}

interface CityWeatherSummary {
  city: CityLocation;
  temp: number;
  weatherCode: number;
  isDay: boolean;
  rainProb?: number;
  humidity?: number;
}

export default function CityCompareCard({
  currentCity,
  favoriteCities,
  onSelectCity,
  onOpenSearch,
  isDarkTheme = true
}: Props) {
  const [summaries, setSummaries] = useState<CityWeatherSummary[]>([]);
  const [loading, setLoading] = useState(false);

  // Danh sách các thành phố để so sánh: favorite cities + bổ sung các thành phố chính
  const compareList: CityLocation[] = React.useMemo(() => {
    const list = [...favoriteCities];
    VIETNAM_MAJOR_CITIES.forEach((major) => {
      const exists = list.some(
        (c) =>
          c.name.toLowerCase() === major.name.toLowerCase() ||
          (Math.abs(c.latitude - major.latitude) < 0.1 && Math.abs(c.longitude - major.longitude) < 0.1)
      );
      if (!exists && list.length < 6) {
        list.push(major);
      }
    });
    return list.slice(0, 6);
  }, [favoriteCities]);

  useEffect(() => {
    let ignore = false;
    async function loadSummaries() {
      setLoading(true);
      try {
        const promises = compareList.map(async (city) => {
          try {
            const url = `https://api.open-meteo.com/v1/forecast?latitude=${city.latitude}&longitude=${city.longitude}&current=temperature_2m,relative_humidity_2m,is_day,weather_code&hourly=precipitation_probability&forecast_days=1&timezone=Asia%2FBangkok`;
            const res = await fetch(url, { next: { revalidate: 300 } });
            if (!res.ok) throw new Error("Fetch failed");
            const data = await res.json();
            const cur = data.current;
            const curHourIdx = new Date().getHours();
            const rainProb = data.hourly?.precipitation_probability?.[curHourIdx] ?? 0;
            return {
              city,
              temp: Math.round(cur.temperature_2m),
              weatherCode: cur.weather_code,
              isDay: cur.is_day === 1,
              rainProb,
              humidity: cur.relative_humidity_2m
            };
          } catch {
            return {
              city,
              temp: 28,
              weatherCode: 0,
              isDay: true,
              rainProb: 10,
              humidity: 70
            };
          }
        });

        const results = await Promise.all(promises);
        if (!ignore) {
          setSummaries(results);
          setLoading(false);
        }
      } catch {
        if (!ignore) setLoading(false);
      }
    }

    loadSummaries();
    return () => {
      ignore = true;
    };
  }, [compareList]);

  return (
    <div className="weather-card glass-specular p-4 sm:p-6 lg:p-7">
      {/* Header */}
      <div className="flex items-center justify-between pb-3 sm:pb-4 border-b border-black/5 dark:border-white/10">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-xl bg-amber-500/20 text-amber-500 border border-amber-500/30">
            <Layers className="w-4 h-4 sm:w-5 sm:h-5" />
          </div>
          <div>
            <h3 className={`text-base font-extrabold flex items-center gap-1.5 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
              So sánh Thời tiết Đa Điểm
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-amber-500/15 text-amber-600 dark:text-amber-400 font-bold border border-amber-500/30">
                Multi-City
              </span>
            </h3>
            <p className={`text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-500 font-medium"}`}>
              Theo dõi song song thời tiết các thành phố yêu thích & trung tâm lớn
            </p>
          </div>
        </div>

        <button
          onClick={onOpenSearch}
          className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold border transition-colors ${
            isDarkTheme
              ? "bg-white/5 border-white/10 text-amber-300 hover:bg-white/10"
              : "bg-amber-50 border-amber-200 text-amber-800 hover:bg-amber-100"
          }`}
        >
          <Plus className="w-3.5 h-3.5" />
          <span>Thêm thành phố</span>
        </button>
      </div>

      {/* Grid of Cities */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3 mt-4">
        {loading && summaries.length === 0 ? (
          <div className="col-span-full py-8 text-center">
            <RefreshCw className="w-6 h-6 animate-spin mx-auto text-amber-500 mb-2" />
            <p className={`text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>Đang cập nhật thời tiết các điểm đến...</p>
          </div>
        ) : (
          summaries.map((item, idx) => {
            const isCurrent =
              item.city.name.toLowerCase() === currentCity.name.toLowerCase() ||
              (Math.abs(item.city.latitude - currentCity.latitude) < 0.05 &&
                Math.abs(item.city.longitude - currentCity.longitude) < 0.05);

            const cond = getWeatherCondition(item.weatherCode, item.isDay);

            return (
              <div
                key={idx}
                onClick={() => onSelectCity(item.city)}
                className={`p-3.5 rounded-2xl border transition-all cursor-pointer group flex flex-col justify-between ${
                  isCurrent
                    ? isDarkTheme
                      ? "bg-amber-500/15 border-amber-500/50 shadow-md shadow-amber-500/10 ring-1 ring-amber-500/40"
                      : "bg-amber-50/90 border-amber-300 shadow-md ring-1 ring-amber-400/50"
                    : isDarkTheme
                    ? "bg-white/5 border-white/10 hover:bg-white/10 hover:border-white/20"
                    : "bg-white/80 border-slate-200/80 hover:bg-white hover:border-slate-300 shadow-xs"
                }`}
              >
                <div>
                  <div className="flex items-center justify-between gap-1">
                    <span className={`text-xs font-bold truncate ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                      {item.city.name}
                    </span>
                    {isCurrent && (
                      <span className="p-0.5 rounded-full bg-amber-500 text-slate-950">
                        <Check className="w-3 h-3 stroke-[3]" />
                      </span>
                    )}
                  </div>
                  <span className={`text-[10px] block truncate ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                    {item.city.admin1 || item.city.country}
                  </span>
                </div>

                <div className="my-2.5 flex items-center justify-between">
                  <div className={`text-xl font-black ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                    {item.temp}°
                  </div>
                  <WeatherIcon name={cond.icon} className="w-7 h-7" />
                </div>

                <div className="pt-2 border-t border-black/5 dark:border-white/10 flex items-center justify-between text-[10px]">
                  <span className={`font-semibold ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                    Mưa: {item.rainProb}%
                  </span>
                  <ArrowRight className="w-3 h-3 opacity-0 group-hover:opacity-100 transition-opacity text-amber-500" />
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
