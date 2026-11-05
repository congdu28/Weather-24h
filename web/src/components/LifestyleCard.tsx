"use client";

import React from "react";
import { LifestyleItem } from "../types/weather";
import { HeartHandshake, Car, Shirt, Activity, ShieldAlert } from "lucide-react";

interface Props {
  items: LifestyleItem[];
  isDarkTheme?: boolean;
}

export default function LifestyleCard({ items, isDarkTheme = true }: Props) {
  if (!items || items.length === 0) return null;

  const renderIcon = (id: string) => {
    switch (id) {
      case "car_wash":
        return <Car className="w-4 h-4 text-amber-500" />;
      case "laundry":
        return <Shirt className="w-4 h-4 text-blue-500" />;
      case "sport":
        return <Activity className="w-4 h-4 text-emerald-500" />;
      case "uv":
        return <ShieldAlert className="w-4 h-4 text-orange-500" />;
      default:
        return <Activity className="w-4 h-4 text-slate-400" />;
    }
  };

  return (
    <div
      className={`rounded-2xl sm:rounded-3xl p-4 sm:p-6 backdrop-blur-xl border transition-all shadow-xl ${
        isDarkTheme
          ? "bg-slate-900/50 border-white/10 text-white"
          : "bg-white/75 border-white/60 text-slate-900 shadow-sky-500/10"
      }`}
    >
      <div className="flex items-center justify-between pb-3 border-b border-white/10 dark:border-white/10 mb-4">
        <h3 className={`text-sm font-bold flex items-center gap-2 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
          <HeartHandshake className="w-4 h-4 text-rose-500" />
          Chỉ số sinh hoạt & Đời sống
        </h3>
        <span className={`text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-500 font-medium"}`}>
          Gợi ý sinh hoạt hôm nay
        </span>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        {items.map((item) => (
          <div
            key={item.id}
            className={`p-3.5 rounded-2xl border transition-colors flex flex-col justify-between ${
              isDarkTheme
                ? "bg-white/5 border-white/10 hover:bg-white/10"
                : "bg-slate-50/90 border-slate-200/80 hover:bg-slate-100 shadow-xs"
            }`}
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <div
                  className={`p-1.5 rounded-xl ${
                    isDarkTheme ? "bg-white/10" : "bg-white shadow-xs border border-slate-200/60"
                  }`}
                >
                  {renderIcon(item.id)}
                </div>
                <span className={`text-xs font-semibold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                  {item.title}
                </span>
              </div>
              <span
                className={`text-xs font-bold px-2 py-0.5 rounded-full border ${
                  isDarkTheme
                    ? "text-amber-300 bg-amber-400/10 border-amber-400/20"
                    : "text-amber-700 bg-amber-50 border-amber-300"
                }`}
              >
                {item.level}
              </span>
            </div>

            <p className={`text-[11px] mt-2.5 line-clamp-2 leading-relaxed ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
              {item.description}
            </p>
          </div>
        ))}
      </div>
    </div>
  );
}
