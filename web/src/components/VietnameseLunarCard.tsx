"use client";

import React, { useMemo } from "react";
import { getTodayLunar, LunarDate } from "../utils/lunarCalendar";
import { getDayAlmanac, getPersonalProfile } from "../utils/lunarAlmanac";
import { usePersistentState } from "../utils/usePersistentState";
import {
  Calendar,
  Compass,
  Clock,
  Sparkles,
  ChevronRight,
  Moon
} from "lucide-react";

interface Props {
  onOpenDetailedAlmanac: () => void;
  isDarkTheme?: boolean;
}

export default function VietnameseLunarCard({ onOpenDetailedAlmanac, isDarkTheme = true }: Props) {
  const todayLunar: LunarDate = useMemo(() => getTodayLunar(), []);
  const almanac = useMemo(() => getDayAlmanac(todayLunar), [todayLunar]);
  const [birthYear] = usePersistentState<number>("weather24h_birth_year", 0);
  const profile = useMemo(() => {
    return birthYear >= 1900 && birthYear <= 2100 ? getPersonalProfile(birthYear) : null;
  }, [birthYear]);

  return (
    <div
      className={`weather-card glass-specular p-4 sm:p-6 lg:p-7 ${
        isDarkTheme
          ? "bg-gradient-to-br from-amber-950/40 via-slate-900/80 to-slate-950/90 border-amber-500/25 text-white"
          : "bg-gradient-to-br from-amber-50/95 via-white/95 to-amber-100/80 border-amber-200/90 text-slate-900 shadow-amber-500/5"
      }`}
    >
      {/* Decorative Gold Glow */}
      <div className="absolute -top-12 -right-12 w-48 h-48 rounded-full bg-amber-500/15 blur-2xl pointer-events-none" />

      {/* Top Header */}
      <div className="flex items-center justify-between pb-3 sm:pb-4 border-b border-amber-500/20">
        <div className="flex items-center gap-2">
          <div className="p-1.5 sm:p-2 rounded-xl bg-amber-500/20 text-amber-500 border border-amber-500/30">
            <Calendar className="w-4 h-4 sm:w-5 sm:h-5" />
          </div>
          <div>
            <h2 className={`text-sm sm:text-base font-bold flex items-center gap-1.5 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
              Lịch Âm Việt Nam
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-amber-400/20 text-amber-600 dark:text-amber-300 border border-amber-400/30 font-semibold">
                GMT+7
              </span>
            </h2>
            <div className="flex items-center gap-2 flex-wrap">
              <p className={`text-[11px] sm:text-xs font-medium ${isDarkTheme ? "text-amber-200/70" : "text-amber-800"}`}>
                Lịch Vạn Niên & Thiên Văn Học Hồ Ngọc Đức
              </p>
              {profile && (
                <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-700 dark:text-amber-300 border border-amber-400/30">
                  Tuổi {profile.canChi} ({profile.birthYear})
                </span>
              )}
            </div>
          </div>
        </div>

        {/* Auspicious Day Badge */}
        <div
          className={`px-2.5 sm:px-3 py-1 rounded-full text-[11px] sm:text-xs font-semibold flex items-center gap-1.5 border shadow-sm ${
            todayLunar.isAuspiciousDay
              ? isDarkTheme
                ? "bg-amber-500/20 text-amber-300 border-amber-400/40"
                : "bg-amber-100 text-amber-900 border-amber-300 font-bold"
              : isDarkTheme
              ? "bg-slate-700/40 text-slate-300 border-slate-600/40"
              : "bg-slate-100 text-slate-700 border-slate-300"
          }`}
        >
          {todayLunar.isAuspiciousDay ? (
            <>
              <Sparkles className="w-3.5 h-3.5 text-amber-500" />
              <span>Hoàng Đạo</span>
            </>
          ) : (
            <>
              <Moon className="w-3.5 h-3.5 text-slate-500" />
              <span>Hắc Đạo</span>
            </>
          )}
        </div>
      </div>

      {/* Main Lunar Highlights */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 my-5 items-center">
        {/* Left: Big Lunar Date */}
        <div
          className={`sm:col-span-1 text-center sm:text-left p-4 sm:p-0 rounded-2xl border sm:border-0 ${
            isDarkTheme ? "bg-white/5 border-white/10" : "bg-amber-50/80 border-amber-200/80"
          }`}
        >
          <div className="text-4xl sm:text-5xl font-black tracking-tight text-amber-500 dark:text-amber-300">
            {todayLunar.day}
            <span className={`text-xl font-medium ml-1 ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
              /{todayLunar.month}
              {todayLunar.isLeapMonth ? " (N)" : ""} ÂL
            </span>
          </div>
          <div className={`text-sm font-bold mt-1 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
            Ngày {todayLunar.dayCanChi}
          </div>
          <div className={`text-xs mt-0.5 ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
            Tháng {todayLunar.monthCanChi} • Năm {todayLunar.yearCanChi}
          </div>
        </div>

        {/* Center: Trực, Sao & Tiết Khí */}
        <div
          className={`sm:col-span-2 space-y-2 p-4 rounded-2xl border ${
            isDarkTheme ? "bg-white/5 border-white/10" : "bg-white/80 border-amber-200/80 shadow-sm"
          }`}
        >
          <div className="flex flex-wrap items-center gap-2 text-xs">
            <span className="px-2.5 py-1 rounded-lg bg-amber-400/20 text-amber-700 dark:text-amber-200 border border-amber-400/30 font-semibold">
              🌿 {todayLunar.solarTerm}
            </span>
            <span className="px-2.5 py-1 rounded-lg bg-blue-500/20 text-blue-700 dark:text-blue-200 border border-blue-400/30 font-semibold">
              ⭐ {almanac.truc.fullName}
            </span>
            <span className="px-2.5 py-1 rounded-lg bg-purple-500/20 text-purple-700 dark:text-purple-200 border border-purple-400/30 font-semibold">
              ✨ Sao {almanac.mansion.name} ({almanac.mansion.isGood ? "Tốt" : "Xấu"})
            </span>
            <span className="px-2.5 py-1 rounded-lg bg-teal-500/20 text-teal-700 dark:text-teal-200 border border-teal-400/30 font-semibold">
              🔮 {almanac.napAm}
            </span>
          </div>

          <p className={`text-xs line-clamp-2 leading-relaxed pt-1 ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
            {almanac.summary}
          </p>
        </div>
      </div>

      {/* Auspicious Hours Strip */}
      <div
        className={`rounded-2xl p-3 border ${
          isDarkTheme ? "bg-white/5 border-white/10" : "bg-amber-50/80 border-amber-200/80"
        }`}
      >
        <div className="flex items-center gap-1.5 text-xs font-semibold text-amber-600 dark:text-amber-300 mb-2">
          <Clock className="w-3.5 h-3.5" />
          Giờ Hoàng Đạo trong ngày:
        </div>
        <div className="flex flex-wrap gap-1.5">
          {todayLunar.auspiciousHours.map((h, i) => (
            <span
              key={i}
              className={`text-[11px] px-2.5 py-1 rounded-lg border font-medium ${
                isDarkTheme
                  ? "bg-amber-500/10 text-amber-200 border-amber-500/20"
                  : "bg-white text-amber-900 border-amber-300 shadow-xs"
              }`}
            >
              {h}
            </span>
          ))}
        </div>
      </div>

      {/* Bottom CTA to open full Lịch Vạn Niên */}
      <div className="mt-4 pt-3 border-t border-amber-500/20 flex flex-col sm:flex-row items-center justify-between gap-3">
        <div className={`flex items-center gap-2 text-xs ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
          <Compass className="w-4 h-4 text-amber-500" />
          <span>
            Hỷ Thần: <strong className={isDarkTheme ? "text-white" : "text-slate-900"}>{almanac.hyThan}</strong> • Tài Thần:{" "}
            <strong className={isDarkTheme ? "text-white" : "text-slate-900"}>{almanac.taiThan}</strong>
          </span>
        </div>

        <button
          onClick={onOpenDetailedAlmanac}
          className="w-full sm:w-auto px-4 py-2 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-600 hover:to-amber-700 text-slate-950 font-bold text-xs flex items-center justify-center gap-1.5 shadow-lg shadow-amber-500/25 transition-all transform hover:scale-[1.02] active:scale-[0.98]"
        >
          <span>Xem Lịch Vạn Niên & Đổi ngày</span>
          <ChevronRight className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}
