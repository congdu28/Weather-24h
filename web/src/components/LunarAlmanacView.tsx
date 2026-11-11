"use client";

import React, { useState, useMemo } from "react";
import {
  getLunarDate,
  getMonthDays,
  convertLunarToSolar,
  LunarDate
} from "../utils/lunarCalendar";
import {
  getDayAlmanac,
  AlmanacActivity,
  ACTIVITY_META,
  DAY_LEVEL_INFO,
  DayLevel
} from "../utils/lunarAlmanac";
import {
  Calendar,
  Compass,
  Clock,
  Sparkles,
  ChevronLeft,
  ChevronRight,
  ArrowRightLeft,
  CheckCircle2,
  XCircle,
  Filter,
  Droplets,
  RotateCcw
} from "lucide-react";
import { DailyForecast } from "../types/weather";
import { getWeatherCondition } from "../services/weatherApi";
import WeatherIcon from "./WeatherIcon";

interface Props {
  isDarkTheme?: boolean;
  dailyForecast?: DailyForecast[];
}

export default function LunarAlmanacView({ isDarkTheme = true, dailyForecast }: Props) {
  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [activeSubTab, setActiveSubTab] = useState<"day" | "month" | "converter" | "good_days">("day");

  // Lunar date for selected date
  const lunarDate: LunarDate = useMemo(() => {
    return getLunarDate(
      selectedDate.getDate(),
      selectedDate.getMonth() + 1,
      selectedDate.getFullYear()
    );
  }, [selectedDate]);

  const almanac = useMemo(() => getDayAlmanac(lunarDate), [lunarDate]);
  const levelInfo = DAY_LEVEL_INFO[almanac.level];

  // Month navigation for Month View
  const [monthYear, setMonthYear] = useState({
    month: new Date().getMonth() + 1,
    year: new Date().getFullYear()
  });

  const monthDays = useMemo(() => {
    return getMonthDays(monthYear.year, monthYear.month);
  }, [monthYear]);

  // Map dự báo thời tiết theo ngày chuẩn "YYYY-MM-DD"
  const dailyForecastMap = useMemo(() => {
    const map = new Map<string, DailyForecast>();
    if (!dailyForecast) return map;
    dailyForecast.forEach((d) => {
      if (d.date) {
        map.set(d.date, d);
      }
    });
    return map;
  }, [dailyForecast]);

  // Dữ liệu ngày trong tháng được bổ sung thông tin Hoàng đạo, Hắc đạo, Rằm/Mùng 1 & thời tiết
  const enrichedMonthDays = useMemo(() => {
    return monthDays.map((item) => {
      const dateStr = `${item.solarYear}-${String(item.solarMonth).padStart(2, "0")}-${String(item.solarDay).padStart(2, "0")}`;
      const almanac = getDayAlmanac(item.lunarDate);
      const forecast = dailyForecastMap.get(dateStr);
      const isHoangDao = item.lunarDate.isAuspiciousDay;
      const isVeryGood = almanac.level === DayLevel.EXCELLENT || (isHoangDao && almanac.level === DayLevel.GOOD);
      const isBad = almanac.level === DayLevel.BAD || almanac.level === DayLevel.VERY_BAD;
      const isFullMoon = item.lunarDate.day === 15;
      const isNewMoon = item.lunarDate.day === 1;

      return {
        ...item,
        dateStr,
        almanac,
        forecast,
        isHoangDao,
        isVeryGood,
        isBad,
        isFullMoon,
        isNewMoon
      };
    });
  }, [monthDays, dailyForecastMap]);

  // Converter state
  const [convertMode, setConvertMode] = useState<"solar2lunar" | "lunar2solar">("solar2lunar");
  const [inputDay, setInputDay] = useState(new Date().getDate());
  const [inputMonth, setInputMonth] = useState(new Date().getMonth() + 1);
  const [inputYear, setInputYear] = useState(new Date().getFullYear());
  const [inputLeap, setInputLeap] = useState(false);
  const [convertResult, setConvertResult] = useState<string | null>(null);

  // Good Day Finder state
  const [selectedActivity, setSelectedActivity] = useState<AlmanacActivity>(AlmanacActivity.OPENING);
  const [goodDaysResults, setGoodDaysResults] = useState<
    Array<{ date: Date; lunar: LunarDate; score: number; level: DayLevel }>
  >([]);

  // Function to search for good days in next 30 days
  const handleFindGoodDays = () => {
    const results: Array<{ date: Date; lunar: LunarDate; score: number; level: DayLevel }> = [];
    const base = new Date();
    for (let i = 0; i < 30; i++) {
      const d = new Date(base.getFullYear(), base.getMonth(), base.getDate() + i);
      const l = getLunarDate(d.getDate(), d.getMonth() + 1, d.getFullYear());
      const alm = getDayAlmanac(l);
      if (alm.goodActivities.includes(selectedActivity) && alm.level !== DayLevel.VERY_BAD) {
        results.push({
          date: d,
          lunar: l,
          score: alm.score,
          level: alm.level
        });
      }
    }
    setGoodDaysResults(results);
  };

  const handleConvert = () => {
    if (convertMode === "solar2lunar") {
      const l = getLunarDate(inputDay, inputMonth, inputYear);
      setConvertResult(
        `Dương lịch ${inputDay}/${inputMonth}/${inputYear} ➔ Âm lịch: Ngày ${l.day} tháng ${l.month}${l.isLeapMonth ? " (Nhuận)" : ""} năm ${l.yearCanChi} (${l.dayCanChi})`
      );
    } else {
      const s = convertLunarToSolar(inputDay, inputMonth, inputYear, inputLeap);
      if (!s) {
        setConvertResult("Ngày Âm lịch không tồn tại hoặc không hợp lệ.");
      } else {
        setConvertResult(
          `Âm lịch ${inputDay}/${inputMonth}/${inputYear}${inputLeap ? " (Nhuận)" : ""} ➔ Dương lịch: Ngày ${s.day}/${s.month}/${s.year}`
        );
      }
    }
  };

  const shiftDay = (days: number) => {
    const next = new Date(selectedDate);
    next.setDate(next.getDate() + days);
    setSelectedDate(next);
  };

  return (
    <div className="space-y-6">
      {/* Sub Tabs */}
      <div
        className={`flex flex-wrap items-center gap-2 p-1.5 rounded-2xl border backdrop-blur-xl ${
          isDarkTheme ? "bg-white/10 border-white/15" : "bg-white/80 border-slate-200 shadow-sm"
        }`}
      >
        <button
          onClick={() => setActiveSubTab("day")}
          className={`flex-1 min-w-[120px] py-2 px-3 rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-1.5 ${
            activeSubTab === "day"
              ? "bg-amber-500 text-slate-950 shadow-lg shadow-amber-500/25"
              : isDarkTheme
              ? "text-slate-300 hover:text-white hover:bg-white/5"
              : "text-slate-600 hover:text-slate-900 hover:bg-slate-100"
          }`}
        >
          <Calendar className="w-4 h-4" />
          Lịch Ngày Chi Tiết
        </button>
        <button
          onClick={() => setActiveSubTab("month")}
          className={`flex-1 min-w-[120px] py-2 px-3 rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-1.5 ${
            activeSubTab === "month"
              ? "bg-amber-500 text-slate-950 shadow-lg shadow-amber-500/25"
              : isDarkTheme
              ? "text-slate-300 hover:text-white hover:bg-white/5"
              : "text-slate-600 hover:text-slate-900 hover:bg-slate-100"
          }`}
        >
          <Calendar className="w-4 h-4" />
          Lịch Tháng Âm - Dương
        </button>
        <button
          onClick={() => setActiveSubTab("converter")}
          className={`flex-1 min-w-[120px] py-2 px-3 rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-1.5 ${
            activeSubTab === "converter"
              ? "bg-amber-500 text-slate-950 shadow-lg shadow-amber-500/25"
              : isDarkTheme
              ? "text-slate-300 hover:text-white hover:bg-white/5"
              : "text-slate-600 hover:text-slate-900 hover:bg-slate-100"
          }`}
        >
          <ArrowRightLeft className="w-4 h-4" />
          Đổi Ngày Âm ↔ Dương
        </button>
        <button
          onClick={() => {
            setActiveSubTab("good_days");
            handleFindGoodDays();
          }}
          className={`flex-1 min-w-[120px] py-2 px-3 rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-1.5 ${
            activeSubTab === "good_days"
              ? "bg-amber-500 text-slate-950 shadow-lg shadow-amber-500/25"
              : isDarkTheme
              ? "text-slate-300 hover:text-white hover:bg-white/5"
              : "text-slate-600 hover:text-slate-900 hover:bg-slate-100"
          }`}
        >
          <Filter className="w-4 h-4" />
          Tìm Ngày Tốt
        </button>
      </div>

      {/* 1. LỊCH NGÀY CHI TIẾT */}
      {activeSubTab === "day" && (
        <div className="space-y-6">
          {/* Date Selector Navigation */}
          <div
            className={`flex items-center justify-between p-4 rounded-3xl backdrop-blur-xl border ${
              isDarkTheme
                ? "bg-slate-900/50 border-white/10 text-white"
                : "bg-white/85 border-slate-200/90 text-slate-900 shadow-md"
            }`}
          >
            <button
              onClick={() => shiftDay(-1)}
              className={`p-2 rounded-xl transition-all ${
                isDarkTheme ? "bg-white/10 hover:bg-white/15 text-white" : "bg-slate-100 hover:bg-slate-200 text-slate-800"
              }`}
            >
              <ChevronLeft className="w-5 h-5" />
            </button>

            <div className="text-center">
              <div className={`text-sm font-bold ${isDarkTheme ? "text-amber-300" : "text-amber-700"}`}>
                Thứ{" "}
                {selectedDate.getDay() === 0 ? "Chủ Nhật" : selectedDate.getDay() + 1}
                , ngày {selectedDate.getDate()} tháng {selectedDate.getMonth() + 1} năm{" "}
                {selectedDate.getFullYear()}
              </div>
              <div className={`text-xs mt-0.5 ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                {lunarDate.dayCanChi} ({lunarDate.formattedLunarMedium})
              </div>
            </div>

            <button
              onClick={() => shiftDay(1)}
              className={`p-2 rounded-xl transition-all ${
                isDarkTheme ? "bg-white/10 hover:bg-white/15 text-white" : "bg-slate-100 hover:bg-slate-200 text-slate-800"
              }`}
            >
              <ChevronRight className="w-5 h-5" />
            </button>
          </div>

          {/* Hero Almanac Card */}
          <div
            className={`p-6 sm:p-8 rounded-3xl backdrop-blur-xl border shadow-xl ${
              isDarkTheme
                ? "bg-gradient-to-br from-amber-950/40 via-slate-900/70 to-slate-950 border-amber-500/30 text-white"
                : "bg-gradient-to-br from-amber-50/95 via-white/95 to-amber-100/70 border-amber-200/90 text-slate-900 shadow-amber-500/5"
            }`}
          >
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-amber-500/20">
              <div>
                <span className="text-xs uppercase font-bold text-amber-500 tracking-wider">
                  {lunarDate.isAuspiciousDay ? "🌟 Ngày Hoàng Đạo" : "🌑 Ngày Hắc Đạo"}
                </span>
                <div className={`text-3xl sm:text-4xl font-extrabold mt-1 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                  Ngày {lunarDate.dayCanChi}
                </div>
                <div className={`text-sm mt-1 ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                  Tháng {lunarDate.monthCanChi} • Năm {lunarDate.yearCanChi}
                </div>
              </div>

              <div className="text-right">
                <div className={`text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-500 font-medium"}`}>Đánh giá chung</div>
                <div className={`text-xl font-black ${levelInfo.color} flex items-center gap-1 justify-end mt-0.5`}>
                  {"★".repeat(levelInfo.stars)} {levelInfo.label}
                </div>
                <div className={`text-xs mt-0.5 ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>Điểm ngày: {almanac.score}/100</div>
              </div>
            </div>

            {/* Badges Info */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 my-6">
              <div className={`p-3 rounded-2xl border ${isDarkTheme ? "bg-white/5 border-white/10" : "bg-white/80 border-slate-200 shadow-xs"}`}>
                <div className={`text-[11px] ${isDarkTheme ? "text-slate-400" : "text-slate-500 font-medium"}`}>12 Trực</div>
                <div className={`text-sm font-bold mt-0.5 ${isDarkTheme ? "text-amber-300" : "text-amber-700"}`}>{almanac.truc.fullName}</div>
              </div>
              <div className={`p-3 rounded-2xl border ${isDarkTheme ? "bg-white/5 border-white/10" : "bg-white/80 border-slate-200 shadow-xs"}`}>
                <div className={`text-[11px] ${isDarkTheme ? "text-slate-400" : "text-slate-500 font-medium"}`}>Nhị Thập Bát Tú</div>
                <div className={`text-sm font-bold mt-0.5 ${isDarkTheme ? "text-purple-300" : "text-purple-700"}`}>Sao {almanac.mansion.name}</div>
              </div>
              <div className={`p-3 rounded-2xl border ${isDarkTheme ? "bg-white/5 border-white/10" : "bg-white/80 border-slate-200 shadow-xs"}`}>
                <div className={`text-[11px] ${isDarkTheme ? "text-slate-400" : "text-slate-500 font-medium"}`}>Ngũ Hành Nạp Âm</div>
                <div className={`text-sm font-bold mt-0.5 ${isDarkTheme ? "text-teal-300" : "text-teal-700"}`}>{almanac.napAm}</div>
              </div>
              <div className={`p-3 rounded-2xl border ${isDarkTheme ? "bg-white/5 border-white/10" : "bg-white/80 border-slate-200 shadow-xs"}`}>
                <div className={`text-[11px] ${isDarkTheme ? "text-slate-400" : "text-slate-500 font-medium"}`}>Tiết Khí</div>
                <div className={`text-sm font-bold mt-0.5 ${isDarkTheme ? "text-emerald-300" : "text-emerald-700"}`}>{lunarDate.solarTerm}</div>
              </div>
            </div>

            {/* Good & Bad Activities */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
              <div className={`p-4 rounded-2xl border ${isDarkTheme ? "bg-emerald-950/20 border-emerald-500/30" : "bg-emerald-50 border-emerald-200"}`}>
                <div className={`flex items-center gap-2 text-xs font-bold mb-2 ${isDarkTheme ? "text-emerald-300" : "text-emerald-800"}`}>
                  <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                  Việc nên làm hôm nay:
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {almanac.goodActivities.length > 0 ? (
                    almanac.goodActivities.map((act) => (
                      <span
                        key={act}
                        className={`px-2.5 py-1 rounded-lg border text-xs font-semibold ${
                          isDarkTheme
                            ? "bg-emerald-500/15 text-emerald-200 border-emerald-500/30"
                            : "bg-white text-emerald-800 border-emerald-300 shadow-xs"
                        }`}
                      >
                        {ACTIVITY_META[act].emoji} {ACTIVITY_META[act].label}
                      </span>
                    ))
                  ) : (
                    <span className={`text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>Các việc thông thường, không có việc đại cát.</span>
                  )}
                </div>
              </div>

              <div className={`p-4 rounded-2xl border ${isDarkTheme ? "bg-rose-950/20 border-rose-500/30" : "bg-rose-50 border-rose-200"}`}>
                <div className={`flex items-center gap-2 text-xs font-bold mb-2 ${isDarkTheme ? "text-rose-300" : "text-rose-800"}`}>
                  <XCircle className="w-4 h-4 text-rose-500" />
                  Việc nên kiêng cữ / cẩn trọng:
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {almanac.badActivities.length > 0 ? (
                    almanac.badActivities.map((act) => (
                      <span
                        key={act}
                        className={`px-2.5 py-1 rounded-lg border text-xs font-semibold ${
                          isDarkTheme
                            ? "bg-rose-500/15 text-rose-200 border-rose-500/30"
                            : "bg-white text-rose-800 border-rose-300 shadow-xs"
                        }`}
                      >
                        {ACTIVITY_META[act].emoji} {ACTIVITY_META[act].label}
                      </span>
                    ))
                  ) : (
                    <span className={`text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>Không có việc đại kỵ trong ngày.</span>
                  )}
                </div>
              </div>
            </div>

            {/* Directions & Clash */}
            <div
              className={`mt-5 p-4 rounded-2xl border flex flex-wrap items-center justify-between gap-3 text-xs ${
                isDarkTheme ? "bg-white/5 border-white/10" : "bg-white/80 border-slate-200"
              }`}
            >
              <div className={`flex items-center gap-1.5 ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                <Compass className="w-4 h-4 text-amber-500" />
                <span>
                  Hỷ Thần: <strong className={isDarkTheme ? "text-white" : "text-slate-900"}>{almanac.hyThan}</strong> • Tài Thần:{" "}
                  <strong className={isDarkTheme ? "text-white" : "text-slate-900"}>{almanac.taiThan}</strong>
                  {almanac.hacThan ? ` • Hạc Thần kỵ: ${almanac.hacThan}` : " • Hạc Thần: Trên trời (không cản)"}
                </span>
              </div>
              <div className={isDarkTheme ? "text-slate-300" : "text-slate-700"}>
                Tuổi xung ngày: <strong className="text-rose-500">{almanac.clashAgesText}</strong>
              </div>
            </div>
          </div>

          {/* 12 Giờ trong ngày & Giờ Lý Thuần Phong */}
          <div
            className={`p-6 rounded-3xl backdrop-blur-xl border ${
              isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900 shadow-md"
            }`}
          >
            <div className="flex items-center justify-between flex-wrap gap-2 mb-4">
              <h3 className={`text-base sm:text-lg font-bold flex items-center gap-2 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                <Clock className="w-5 h-5 text-amber-500" />
                12 Canh Giờ Hoàng Đạo & Xuất Hành Lý Thuần Phong
              </h3>
              <div className="flex items-center gap-3 text-xs">
                <span className="flex items-center gap-1.5 font-semibold text-amber-600 dark:text-amber-400">
                  <span className="w-2.5 h-2.5 rounded-full bg-amber-400 ring-2 ring-amber-300/40"></span>
                  Giờ Hoàng Đạo
                </span>
                <span className="flex items-center gap-1.5 text-slate-500 dark:text-slate-400">
                  <span className="w-2.5 h-2.5 rounded-full bg-slate-300 dark:bg-slate-600"></span>
                  Giờ Hắc Đạo
                </span>
              </div>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3 sm:gap-3.5">
              {almanac.hours.map((h, i) => (
                <div
                  key={i}
                  className={`p-3.5 sm:p-4 rounded-2xl border transition-all ${
                    h.isHoangDao
                      ? isDarkTheme
                        ? h.isBest
                          ? "bg-gradient-to-br from-amber-500/25 via-amber-900/35 to-slate-900/90 border-amber-400/60 text-amber-100 shadow-md shadow-amber-500/15 ring-1 ring-amber-400/50"
                          : "bg-gradient-to-br from-amber-500/15 via-amber-950/25 to-slate-900/80 border-amber-400/40 text-amber-100 shadow-xs ring-1 ring-amber-400/25"
                        : h.isBest
                          ? "bg-gradient-to-br from-amber-100 via-amber-50 to-orange-50 border-amber-300/90 text-amber-950 shadow-sm ring-1 ring-amber-300"
                          : "bg-gradient-to-br from-amber-50/90 via-yellow-50/50 to-amber-100/40 border-amber-200/90 text-amber-950 shadow-2xs ring-1 ring-amber-200/60"
                      : isDarkTheme
                        ? "bg-white/5 border-white/10 text-slate-300 opacity-80 hover:opacity-100"
                        : "bg-slate-50/90 border-slate-200/80 text-slate-700 opacity-85 hover:opacity-100"
                  }`}
                >
                  <div className="flex items-center justify-between gap-1">
                    <span
                      className={`text-sm sm:text-base font-extrabold ${
                        h.isHoangDao
                          ? isDarkTheme ? "text-amber-200" : "text-amber-950"
                          : isDarkTheme ? "text-white" : "text-slate-900"
                      }`}
                    >
                      Giờ {h.chi}
                    </span>
                    <span
                      className={`text-xs font-semibold px-2 py-0.5 rounded-md ${
                        h.isHoangDao
                          ? isDarkTheme
                            ? "bg-amber-400/20 text-amber-200 border border-amber-400/30"
                            : "bg-amber-200/60 text-amber-900 border border-amber-300/60"
                          : isDarkTheme
                            ? "bg-white/10 text-slate-300"
                            : "bg-slate-200/70 text-slate-700"
                      }`}
                    >
                      {h.range}
                    </span>
                  </div>

                  <div className="text-xs sm:text-[13px] font-bold mt-2 flex items-center gap-1.5 flex-wrap">
                    {h.isHoangDao ? (
                      <span className="inline-flex items-center gap-1 text-amber-600 dark:text-amber-300 font-extrabold">
                        <Sparkles className="w-3.5 h-3.5 text-amber-500 fill-amber-400/30" />
                        {h.isBest ? "Hoàng đạo (Đại cát)" : "Hoàng đạo"}
                      </span>
                    ) : (
                      <span className="text-slate-500 dark:text-slate-400 font-medium">
                        Hắc đạo
                      </span>
                    )}
                    <span className="text-slate-300 dark:text-slate-600">•</span>
                    <span
                      className={
                        h.lyThuanPhong.isGood
                          ? isDarkTheme
                            ? "text-emerald-300 font-bold"
                            : "text-emerald-700 font-bold"
                          : isDarkTheme
                            ? "text-slate-300 font-medium"
                            : "text-slate-600 font-medium"
                      }
                    >
                      {h.lyThuanPhong.name}
                    </span>
                  </div>

                  <p
                    className={`text-xs leading-relaxed line-clamp-3 mt-1.5 ${
                      h.isHoangDao
                        ? isDarkTheme
                          ? "text-amber-100/75"
                          : "text-amber-900/80"
                        : isDarkTheme
                          ? "text-slate-400"
                          : "text-slate-600"
                    }`}
                  >
                    {h.lyThuanPhong.description}
                  </p>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* 2. LỊCH THÁNG ÂM - DƯƠNG */}
      {activeSubTab === "month" && (
        <div
          className={`p-4 sm:p-6 md:p-8 rounded-3xl backdrop-blur-xl border ${
            isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900 shadow-md"
          }`}
        >
          {/* Header Tháng & Nút chuyển & Chú thích */}
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4 mb-4 pb-4 border-b border-amber-500/20">
            <div className="flex items-center gap-3">
              <button
                onClick={() => {
                  if (monthYear.month === 1) setMonthYear({ month: 12, year: monthYear.year - 1 });
                  else setMonthYear({ month: monthYear.month - 1, year: monthYear.year });
                }}
                className={`p-2.5 rounded-xl transition-all ${
                  isDarkTheme ? "bg-white/10 hover:bg-white/15 text-white" : "bg-slate-100 hover:bg-slate-200 text-slate-800"
                }`}
                title="Tháng trước"
              >
                <ChevronLeft className="w-5 h-5" />
              </button>

              <h3 className={`text-lg sm:text-xl font-black ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                Tháng {monthYear.month} / {monthYear.year}
              </h3>

              <button
                onClick={() => {
                  if (monthYear.month === 12) setMonthYear({ month: 1, year: monthYear.year + 1 });
                  else setMonthYear({ month: monthYear.month + 1, year: monthYear.year });
                }}
                className={`p-2.5 rounded-xl transition-all ${
                  isDarkTheme ? "bg-white/10 hover:bg-white/15 text-white" : "bg-slate-100 hover:bg-slate-200 text-slate-800"
                }`}
                title="Tháng sau"
              >
                <ChevronRight className="w-5 h-5" />
              </button>

              {/* Nút về hôm nay nếu đang xem tháng khác */}
              {(monthYear.month !== new Date().getMonth() + 1 || monthYear.year !== new Date().getFullYear()) && (
                <button
                  onClick={() => {
                    setMonthYear({
                      month: new Date().getMonth() + 1,
                      year: new Date().getFullYear()
                    });
                  }}
                  className={`text-xs font-bold px-3 py-1.5 rounded-xl flex items-center gap-1.5 transition-all ${
                    isDarkTheme
                      ? "bg-amber-500/20 text-amber-300 hover:bg-amber-500/30 border border-amber-400/30"
                      : "bg-amber-100 text-amber-900 hover:bg-amber-200 border border-amber-300"
                  }`}
                >
                  <RotateCcw className="w-3.5 h-3.5" />
                  Về hôm nay
                </button>
              )}
            </div>

            {/* Chú thích nhanh các biểu tượng */}
            <div className="flex items-center gap-2 sm:gap-3 flex-wrap text-xs font-semibold">
              <span className="flex items-center gap-1 text-emerald-600 dark:text-emerald-400">
                <Sparkles className="w-3.5 h-3.5" />
                Hoàng Đạo (Tốt)
              </span>
              <span className="flex items-center gap-1 text-slate-500 dark:text-slate-400">
                <span className="w-2 h-2 rounded-full bg-slate-300 dark:bg-slate-600" />
                Hắc Đạo (Xấu)
              </span>
              <span className="flex items-center gap-1 text-amber-600 dark:text-amber-400">
                <span>🌕</span>
                Rằm & Mùng 1
              </span>
              {dailyForecast && dailyForecast.length > 0 && (
                <span className="flex items-center gap-1 text-sky-600 dark:text-sky-400">
                  <span>🌦️</span>
                  Kèm thời tiết
                </span>
              )}
            </div>
          </div>

          {/* Days of Week Header */}
          <div className="grid grid-cols-7 gap-1.5 sm:gap-2.5 text-center mb-2">
            {[
              { label: "CN", isSunday: true },
              { label: "T2" },
              { label: "T3" },
              { label: "T4" },
              { label: "T5" },
              { label: "T6" },
              { label: "T7", isSaturday: true }
            ].map((d, i) => (
              <div
                key={i}
                className={`py-2 rounded-xl text-xs sm:text-sm font-extrabold uppercase tracking-wider ${
                  d.isSunday
                    ? "text-rose-500 bg-rose-500/10"
                    : d.isSaturday
                    ? "text-sky-500 bg-sky-500/10"
                    : isDarkTheme
                    ? "text-slate-400 bg-white/5"
                    : "text-slate-600 bg-slate-100"
                }`}
              >
                {d.label}
              </div>
            ))}
          </div>

          {/* Grid of days */}
          <div className="grid grid-cols-7 gap-1.5 sm:gap-2.5">
            {/* Empty slots for first week offset */}
            {Array.from({ length: enrichedMonthDays[0]?.dayOfWeek ?? 0 }).map((_, i) => (
              <div
                key={`empty-${i}`}
                className="min-h-[105px] sm:min-h-[125px] md:min-h-[135px] rounded-2xl bg-black/[0.015] dark:bg-white/[0.02] border border-dashed border-slate-200/50 dark:border-white/5 opacity-40"
              />
            ))}

            {enrichedMonthDays.map((item, idx) => {
              const isSunday = item.dayOfWeek === 0;
              const isSaturday = item.dayOfWeek === 6;

              return (
                <button
                  key={idx}
                  onClick={() => {
                    setSelectedDate(new Date(item.solarYear, item.solarMonth - 1, item.solarDay));
                    setActiveSubTab("day");
                  }}
                  className={`min-h-[105px] sm:min-h-[125px] md:min-h-[135px] p-2 sm:p-2.5 rounded-2xl border flex flex-col justify-between text-left transition-all duration-200 group active:scale-[0.98] ${
                    item.isToday
                      ? isDarkTheme
                        ? "bg-gradient-to-br from-amber-500/25 via-amber-950/40 to-slate-900 border-2 border-amber-400 ring-2 ring-amber-400/40 shadow-lg shadow-amber-500/20 text-white"
                        : "bg-gradient-to-br from-amber-100/90 via-amber-50/70 to-white border-2 border-amber-500 ring-2 ring-amber-400/50 shadow-md text-slate-900"
                      : item.isHoangDao
                      ? isDarkTheme
                        ? "bg-gradient-to-br from-slate-900/80 via-slate-900/50 to-amber-950/20 border-amber-500/25 hover:border-amber-400/60 hover:bg-white/10 text-white shadow-xs"
                        : "bg-gradient-to-br from-amber-50/60 via-white to-yellow-50/40 border-amber-200/80 hover:border-amber-300 hover:bg-amber-50/90 text-slate-900 shadow-2xs"
                      : isDarkTheme
                      ? "bg-white/5 border-white/10 hover:border-white/20 hover:bg-white/10 text-slate-300"
                      : "bg-white border-slate-200/80 hover:border-slate-300 hover:bg-slate-50 text-slate-800 shadow-2xs"
                  }`}
                >
                  {/* Top Row: Solar Day + Good/Bad / Festival Badge */}
                  <div className="flex items-start justify-between w-full gap-1">
                    <div className="flex items-center gap-1">
                      <span
                        className={`text-base sm:text-xl font-black leading-none ${
                          item.isToday
                            ? "text-amber-600 dark:text-amber-300"
                            : isSunday
                            ? "text-rose-500"
                            : isSaturday
                            ? "text-sky-500"
                            : isDarkTheme
                            ? "text-white"
                            : "text-slate-900"
                        }`}
                      >
                        {item.solarDay}
                      </span>
                      {item.isToday && (
                        <span className="hidden sm:inline-block px-1.5 py-0.5 rounded text-[8.5px] font-black uppercase bg-amber-500 text-slate-950 leading-none">
                          Nay
                        </span>
                      )}
                    </div>

                    {/* Biểu tượng Ngày Tốt / Ngày Xấu / Rằm / Mùng 1 */}
                    <div className="shrink-0 flex items-center">
                      {item.isNewMoon ? (
                        <span
                          className="inline-flex items-center gap-0.5 text-[9px] sm:text-[10px] font-bold px-1.5 py-0.5 rounded-full bg-rose-500/15 text-rose-600 dark:text-rose-400 border border-rose-500/30"
                          title="Mùng 1 đầu tháng Âm lịch"
                        >
                          🌑 <span className="hidden md:inline">M.Một</span>
                        </span>
                      ) : item.isFullMoon ? (
                        <span
                          className="inline-flex items-center gap-0.5 text-[9px] sm:text-[10px] font-bold px-1.5 py-0.5 rounded-full bg-amber-500/20 text-amber-700 dark:text-amber-300 border border-amber-400/40"
                          title="Ngày Rằm trăng tròn"
                        >
                          🌕 <span className="hidden md:inline">Rằm</span>
                        </span>
                      ) : item.isVeryGood ? (
                        <span
                          className="inline-flex items-center gap-0.5 text-[9px] sm:text-[10px] font-extrabold px-1.5 py-0.5 rounded-md bg-emerald-500/15 text-emerald-700 dark:text-emerald-300 border border-emerald-500/30"
                          title="Hoàng Đạo (Đại Cát) - Rất tốt lành"
                        >
                          <Sparkles className="w-2.5 h-2.5 text-emerald-500 fill-emerald-400/30" />
                          <span className="hidden md:inline">Đại Cát</span>
                          <span className="md:hidden">Tốt</span>
                        </span>
                      ) : item.isHoangDao ? (
                        <span
                          className="inline-flex items-center gap-0.5 text-[9px] sm:text-[10px] font-bold px-1.5 py-0.5 rounded-md bg-amber-500/15 text-amber-700 dark:text-amber-300 border border-amber-400/30"
                          title="Ngày Hoàng Đạo - Cát nhật"
                        >
                          <Sparkles className="w-2.5 h-2.5 text-amber-500 fill-amber-400/30" />
                          <span className="hidden md:inline">H.Đạo</span>
                        </span>
                      ) : item.isBad ? (
                        <span
                          className="inline-flex items-center gap-0.5 text-[9px] sm:text-[10px] font-medium px-1.5 py-0.5 rounded-md bg-rose-500/10 text-rose-600 dark:text-rose-400 border border-rose-500/20"
                          title="Ngày Hắc Đạo / Kiêng việc lớn"
                        >
                          <span className="hidden md:inline">Hắc Đạo</span>
                          <span className="md:hidden">Xấu</span>
                        </span>
                      ) : (
                        <span className="text-[9px] text-slate-400 dark:text-slate-500 hidden md:inline">
                          Bình
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Middle Row: Biểu tượng Thời tiết dự báo (nếu có dữ liệu) HOẶC Tiết khí / Can chi */}
                  {item.forecast ? (
                    <div className="my-1 px-1.5 py-1 rounded-xl bg-black/[0.03] dark:bg-white/[0.05] border border-black/[0.04] dark:border-white/[0.06] flex items-center justify-between gap-1 w-full">
                      <div className="flex items-center gap-1 min-w-0">
                        <WeatherIcon
                          name={
                            item.forecast.conditionIcon ||
                            getWeatherCondition(item.forecast.weatherCode, true).icon
                          }
                          className="w-5 h-5 sm:w-6 sm:h-6 shrink-0"
                        />
                        <div className="flex flex-col leading-none">
                          <span className="text-[11px] sm:text-xs font-black text-slate-800 dark:text-slate-100">
                            {Math.round(item.forecast.tempMax)}°
                          </span>
                          <span className="text-[9px] sm:text-[10px] font-medium text-slate-500 dark:text-slate-400 mt-0.5">
                            {Math.round(item.forecast.tempMin)}°
                          </span>
                        </div>
                      </div>

                      {item.forecast.precipitationProbabilityMax >= 20 ? (
                        <div
                          className="flex items-center gap-0.5 text-[9px] sm:text-[10px] font-bold text-sky-600 dark:text-sky-400 shrink-0"
                          title={`Xác suất mưa: ${item.forecast.precipitationProbabilityMax}% - Lượng mưa: ${item.forecast.precipitationSum}mm`}
                        >
                          <Droplets className="w-2.5 h-2.5 text-sky-500 shrink-0" />
                          <span>{item.forecast.precipitationProbabilityMax}%</span>
                        </div>
                      ) : (
                        <span
                          className="text-[9px] text-slate-400 dark:text-slate-500 hidden lg:inline truncate max-w-[45px]"
                          title={item.forecast.conditionDescription}
                        >
                          {item.forecast.conditionDescription || "Khô ráo"}
                        </span>
                      )}
                    </div>
                  ) : (
                    <div className="my-1 py-1 px-1 flex items-center justify-between text-[10px] text-slate-400 dark:text-slate-500 w-full min-h-[28px]">
                      <span className="truncate max-w-[70px] sm:max-w-[90px]">
                        {item.lunarDate.solarTerm || item.lunarDate.dayCanChi}
                      </span>
                      {item.lunarDate.festival && (
                        <span className="text-rose-500 font-bold truncate max-w-[60px] text-[9px]">
                          {item.lunarDate.festival}
                        </span>
                      )}
                    </div>
                  )}

                  {/* Bottom Row: Lunar Date & Can Chi */}
                  <div className="flex items-center justify-between w-full pt-1 border-t border-black/[0.04] dark:border-white/[0.06] text-left">
                    <div className="flex items-center gap-1">
                      <span
                        className={`text-xs sm:text-sm font-extrabold ${
                          item.isToday
                            ? "text-amber-600 dark:text-amber-300"
                            : item.isFullMoon || item.isNewMoon
                            ? "text-rose-600 dark:text-rose-400"
                            : isDarkTheme
                            ? "text-amber-300"
                            : "text-amber-800"
                        }`}
                      >
                        {item.lunarDate.day}/{item.lunarDate.month}
                        {item.lunarDate.isLeapMonth ? "N" : ""}
                      </span>
                      <span className="text-[9.5px] text-slate-400 font-normal hidden sm:inline">ÂL</span>
                    </div>

                    <span className="text-[9px] sm:text-[10px] font-medium text-slate-500 dark:text-slate-400 truncate max-w-[60px] sm:max-w-[80px]">
                      {item.lunarDate.canDay} {item.lunarDate.chiDay}
                    </span>
                  </div>
                </button>
              );
            })}
          </div>
        </div>
      )}

      {/* 3. ĐỔI NGÀY ÂM - DƯƠNG */}
      {activeSubTab === "converter" && (
        <div
          className={`p-6 sm:p-8 rounded-3xl backdrop-blur-xl border max-w-xl mx-auto shadow-xl ${
            isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
          }`}
        >
          <h3 className={`text-base font-bold flex items-center gap-2 mb-4 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
            <ArrowRightLeft className="w-5 h-5 text-amber-500" />
            Công Cụ Chuyển Đổi Ngày Âm ↔ Dương
          </h3>

          <div
            className={`flex gap-2 p-1 rounded-xl mb-5 border ${
              isDarkTheme ? "bg-white/5 border-white/10" : "bg-slate-100 border-slate-200"
            }`}
          >
            <button
              onClick={() => {
                setConvertMode("solar2lunar");
                setConvertResult(null);
              }}
              className={`flex-1 py-2 rounded-lg text-xs font-bold transition-all ${
                convertMode === "solar2lunar"
                  ? "bg-amber-500 text-slate-950 shadow-sm"
                  : isDarkTheme
                  ? "text-slate-300 hover:text-white"
                  : "text-slate-600 hover:text-slate-900"
              }`}
            >
              Dương Lịch ➔ Âm Lịch
            </button>
            <button
              onClick={() => {
                setConvertMode("lunar2solar");
                setConvertResult(null);
              }}
              className={`flex-1 py-2 rounded-lg text-xs font-bold transition-all ${
                convertMode === "lunar2solar"
                  ? "bg-amber-500 text-slate-950 shadow-sm"
                  : isDarkTheme
                  ? "text-slate-300 hover:text-white"
                  : "text-slate-600 hover:text-slate-900"
              }`}
            >
              Âm Lịch ➔ Dương Lịch
            </button>
          </div>

          <div className="grid grid-cols-3 gap-3 mb-4">
            <div>
              <label className={`text-[11px] font-semibold ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>Ngày</label>
              <input
                type="number"
                min={1}
                max={31}
                value={inputDay}
                onChange={(e) => setInputDay(Number(e.target.value))}
                className={`w-full mt-1 p-2.5 rounded-xl border text-center font-bold ${
                  isDarkTheme ? "bg-white/10 border-white/20 text-white" : "bg-slate-50 border-slate-300 text-slate-900"
                }`}
              />
            </div>
            <div>
              <label className={`text-[11px] font-semibold ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>Tháng</label>
              <input
                type="number"
                min={1}
                max={12}
                value={inputMonth}
                onChange={(e) => setInputMonth(Number(e.target.value))}
                className={`w-full mt-1 p-2.5 rounded-xl border text-center font-bold ${
                  isDarkTheme ? "bg-white/10 border-white/20 text-white" : "bg-slate-50 border-slate-300 text-slate-900"
                }`}
              />
            </div>
            <div>
              <label className={`text-[11px] font-semibold ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>Năm</label>
              <input
                type="number"
                min={1900}
                max={2100}
                value={inputYear}
                onChange={(e) => setInputYear(Number(e.target.value))}
                className={`w-full mt-1 p-2.5 rounded-xl border text-center font-bold ${
                  isDarkTheme ? "bg-white/10 border-white/20 text-white" : "bg-slate-50 border-slate-300 text-slate-900"
                }`}
              />
            </div>
          </div>

          {convertMode === "lunar2solar" && (
            <label className={`flex items-center gap-2 mb-4 text-xs cursor-pointer ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
              <input
                type="checkbox"
                checked={inputLeap}
                onChange={(e) => setInputLeap(e.target.checked)}
                className="rounded text-amber-500 focus:ring-0"
              />
              <span>Tháng Nhuận</span>
            </label>
          )}

          <button
            onClick={handleConvert}
            className="w-full py-3 rounded-2xl bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold text-sm shadow-lg shadow-amber-500/25 transition-all"
          >
            Chuyển Đổi Ngay
          </button>

          {convertResult && (
            <div
              className={`mt-5 p-4 rounded-2xl border text-center font-semibold text-xs sm:text-sm ${
                isDarkTheme ? "bg-white/10 border-amber-400/40 text-amber-300" : "bg-amber-50 border-amber-300 text-amber-900"
              }`}
            >
              {convertResult}
            </div>
          )}
        </div>
      )}

      {/* 4. TÌM NGÀY TỐT */}
      {activeSubTab === "good_days" && (
        <div
          className={`p-6 rounded-3xl backdrop-blur-xl border shadow-xl ${
            isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
          }`}
        >
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 mb-6 pb-4 border-b border-amber-500/20">
            <div>
              <h3 className={`text-base font-bold flex items-center gap-2 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                <Sparkles className="w-5 h-5 text-amber-500" />
                Tra Cứu Ngày Tốt Trong 30 Ngày Tới
              </h3>
              <p className={`text-xs mt-0.5 ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                Lọc ngày Hoàng đạo, trực cát tinh theo phong tục Việt Nam
              </p>
            </div>

            {/* Select activity */}
            <div className="flex items-center gap-2 w-full sm:w-auto">
              <select
                value={selectedActivity}
                onChange={(e) => {
                  setSelectedActivity(e.target.value as AlmanacActivity);
                }}
                className={`w-full sm:w-auto p-2 rounded-xl border text-xs font-semibold focus:outline-none ${
                  isDarkTheme ? "bg-slate-900 border-white/20 text-white" : "bg-white border-slate-300 text-slate-900"
                }`}
              >
                {Object.keys(ACTIVITY_META).map((actKey) => (
                  <option key={actKey} value={actKey}>
                    {ACTIVITY_META[actKey as AlmanacActivity].emoji} {ACTIVITY_META[actKey as AlmanacActivity].label}
                  </option>
                ))}
              </select>
              <button
                onClick={handleFindGoodDays}
                className="px-3 py-2 rounded-xl bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold text-xs shrink-0 shadow-sm"
              >
                Tìm
              </button>
            </div>
          </div>

          {/* Results List */}
          <div className="space-y-3">
            {goodDaysResults.length > 0 ? (
              goodDaysResults.map((item, idx) => (
                <div
                  key={idx}
                  onClick={() => {
                    setSelectedDate(item.date);
                    setActiveSubTab("day");
                  }}
                  className={`flex items-center justify-between p-3.5 rounded-2xl border transition-colors cursor-pointer ${
                    isDarkTheme
                      ? "bg-white/5 border-white/10 hover:bg-white/10 text-white"
                      : "bg-slate-50 border-slate-200 hover:bg-slate-100 text-slate-900 shadow-2xs"
                  }`}
                >
                  <div>
                    <div className={`text-sm font-bold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                      Thứ {item.date.getDay() === 0 ? "Chủ Nhật" : item.date.getDay() + 1},{" "}
                      ngày {item.date.getDate()}/{item.date.getMonth() + 1}/{item.date.getFullYear()}
                    </div>
                    <div className={`text-xs mt-0.5 ${isDarkTheme ? "text-amber-300" : "text-amber-700"}`}>
                      {item.lunar.formattedLunarMedium} • {item.lunar.solarTerm}
                    </div>
                  </div>

                  <div className="text-right">
                    <span className="text-xs font-bold text-emerald-600 dark:text-emerald-300 px-2.5 py-1 rounded-full bg-emerald-500/15 border border-emerald-500/30">
                      {item.lunar.isAuspiciousDay ? "Hoàng Đạo" : "Hắc Đạo"}
                    </span>
                    <div className={`text-[11px] mt-1 ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                      Điểm: {item.score}/100
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className={`text-center py-10 text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                Không tìm thấy ngày tốt đặc biệt cho công việc này trong 30 ngày tới hoặc hãy bấm nút Tìm.
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
