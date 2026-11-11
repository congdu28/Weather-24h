"use client";

import React, { useState, useMemo } from "react";
import {
  getLunarDate,
  getMonthDays,
  convertLunarToSolar,
  LunarDate,
  CAN,
  CHI
} from "../utils/lunarCalendar";
import {
  getDayAlmanac,
  AlmanacActivity,
  ACTIVITY_META,
  DAY_LEVEL_INFO,
  DayLevel,
  PersonalProfile,
  PersonalDayMatch,
  getPersonalProfile,
  matchPersonalDay,
  ZODIAC_TRAITS,
  ActivityVerdict,
  DayAlmanac
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
  RotateCcw,
  User,
  CalendarDays,
  Award,
  X,
  Check
} from "lucide-react";
import { DailyForecast } from "../types/weather";
import { getWeatherCondition } from "../services/weatherApi";
import WeatherIcon from "./WeatherIcon";
import { usePersistentState } from "../utils/usePersistentState";

interface Props {
  isDarkTheme?: boolean;
  dailyForecast?: DailyForecast[];
}

export default function LunarAlmanacView({ isDarkTheme = true, dailyForecast }: Props) {
  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [activeSubTab, setActiveSubTab] = useState<"day" | "month" | "good_days" | "fortune" | "converter">("day");

  // Năm sinh gia chủ lưu bền vững trong localStorage
  const [birthYear, setBirthYear] = usePersistentState<number>("weather24h_birth_year", 0);
  const [showBirthYearModal, setShowBirthYearModal] = useState<boolean>(false);
  const [tempYear, setTempYear] = useState<number>(1990);
  const [filterPersonalMatch, setFilterPersonalMatch] = useState<boolean>(true);

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

  // Hồ sơ bản mệnh phong thủy gia chủ
  const profile: PersonalProfile | null = useMemo(() => {
    return birthYear >= 1900 && birthYear <= 2100 ? getPersonalProfile(birthYear) : null;
  }, [birthYear]);

  // Đối chiếu phong thủy ngày được chọn với bản mệnh gia chủ
  const personalMatch: PersonalDayMatch | null = useMemo(() => {
    return profile ? matchPersonalDay(profile, almanac) : null;
  }, [profile, almanac]);

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

  // Dữ liệu ngày trong tháng được bổ sung thông tin Hoàng đạo, Hắc đạo, Rằm/Mùng 1, thời tiết & xung hợp tuổi
  const enrichedMonthDays = useMemo(() => {
    return monthDays.map((item) => {
      const dateStr = `${item.solarYear}-${String(item.solarMonth).padStart(2, "0")}-${String(item.solarDay).padStart(2, "0")}`;
      const dayAlm = getDayAlmanac(item.lunarDate);
      const forecast = dailyForecastMap.get(dateStr);
      const isHoangDao = item.lunarDate.isAuspiciousDay;
      const isVeryGood = dayAlm.level === DayLevel.EXCELLENT || (isHoangDao && dayAlm.level === DayLevel.GOOD);
      const isBad = dayAlm.level === DayLevel.BAD || dayAlm.level === DayLevel.VERY_BAD;
      const isFullMoon = item.lunarDate.day === 15;
      const isNewMoon = item.lunarDate.day === 1;
      const dayPersonal = profile ? matchPersonalDay(profile, dayAlm) : null;

      return {
        ...item,
        dateStr,
        almanac: dayAlm,
        forecast,
        isHoangDao,
        isVeryGood,
        isBad,
        isFullMoon,
        isNewMoon,
        personal: dayPersonal
      };
    });
  }, [monthDays, dailyForecastMap, profile]);

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
    Array<{
      date: Date;
      lunar: LunarDate;
      almanac: DayAlmanac;
      score: number;
      level: DayLevel;
      personalMatch?: PersonalDayMatch;
    }>
  >([]);

  // Function to search for good days in next 45 days, combined with personal profile
  const handleFindGoodDays = () => {
    const results: Array<{
      date: Date;
      lunar: LunarDate;
      almanac: DayAlmanac;
      score: number;
      level: DayLevel;
      personalMatch?: PersonalDayMatch;
    }> = [];
    const base = new Date();
    for (let i = 0; i < 45; i++) {
      const d = new Date(base.getFullYear(), base.getMonth(), base.getDate() + i);
      const l = getLunarDate(d.getDate(), d.getMonth() + 1, d.getFullYear());
      const alm = getDayAlmanac(l);
      const personal = profile ? matchPersonalDay(profile, alm) : undefined;

      // Nếu bật lọc hợp tuổi và ngày này xung tuổi -> loại bỏ
      if (filterPersonalMatch && personal && personal.verdict === ActivityVerdict.BAD) {
        continue;
      }

      if (alm.goodActivities.includes(selectedActivity) && alm.level !== DayLevel.VERY_BAD) {
        results.push({
          date: d,
          lunar: l,
          almanac: alm,
          score: alm.score,
          level: alm.level,
          personalMatch: personal
        });
      }
    }

    // Sắp xếp ưu tiên: Điểm ngày + điểm hòa hợp tuổi gia chủ
    results.sort((a, b) => {
      const pA = a.personalMatch ? a.personalMatch.points * 5 : 0;
      const pB = b.personalMatch ? b.personalMatch.points * 5 : 0;
      return (b.score + pB) - (a.score + pA);
    });

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
      {/* Banner Bản Mệnh Gia Chủ & Nhập Năm Sinh */}
      <div
        className={`p-4 sm:p-5 rounded-3xl border flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3.5 backdrop-blur-xl transition-all ${
          profile
            ? isDarkTheme
              ? "bg-gradient-to-r from-amber-500/15 via-slate-900 to-amber-950/25 border-amber-500/35 text-white shadow-lg"
              : "bg-gradient-to-r from-amber-50 via-white to-orange-50/80 border-amber-200 text-slate-900 shadow-sm"
            : isDarkTheme
            ? "bg-slate-900/60 border-amber-500/25 text-white shadow-md"
            : "bg-amber-50/70 border-amber-200/90 text-slate-900 shadow-sm"
        }`}
      >
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-2xl bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center text-slate-950 font-black text-lg shadow-md shrink-0">
            {profile ? "👤" : "🔮"}
          </div>
          <div>
            {profile ? (
              <>
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="text-sm sm:text-base font-black">
                    Tuổi {profile.canChi} ({profile.birthYear})
                  </span>
                  <span className="text-xs font-bold px-2.5 py-0.5 rounded-full bg-amber-500/20 text-amber-700 dark:text-amber-300 border border-amber-400/40">
                    {profile.element} • {profile.napAm}
                  </span>
                  <span className="text-xs font-medium text-slate-500 dark:text-slate-400">
                    {profile.yinYangLabel}
                  </span>
                </div>
                <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                  Cung phi: <strong>{profile.cungMenh.menhNam}</strong> (Nam) / <strong>{profile.cungMenh.menhNu}</strong> (Nữ) • Tam hợp: <strong>{profile.tamHop.join(", ")}</strong> • Kỵ xung: <strong className="text-rose-500">{profile.tuHanhXung[0]}</strong>
                </p>
              </>
            ) : (
              <>
                <div className="text-sm font-black flex items-center gap-2">
                  <span>Chưa thiết lập năm sinh gia chủ</span>
                  <span className="text-[10px] px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-700 dark:text-amber-300 font-bold">
                    Cá nhân hóa
                  </span>
                </div>
                <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                  Nhập năm sinh để đối chiếu ngày xung/hợp, nhận lời khuyên việc nên làm & mở khóa bói tử vi phong thủy trọn đời!
                </p>
              </>
            )}
          </div>
        </div>

        <button
          onClick={() => {
            if (profile) setTempYear(profile.birthYear);
            setShowBirthYearModal(true);
          }}
          className={`px-4 py-2 rounded-xl text-xs font-bold transition-all shadow-sm shrink-0 self-end sm:self-center ${
            profile
              ? "bg-amber-500 hover:bg-amber-600 text-slate-950"
              : "bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-600 hover:to-amber-700 text-slate-950"
          }`}
        >
          {profile ? `Đổi tuổi (${profile.birthYear})` : "Nhập năm sinh ngay ›"}
        </button>
      </div>

      {/* Sub Tabs */}
      <div
        className={`flex flex-wrap items-center gap-1.5 p-1.5 rounded-2xl border backdrop-blur-xl ${
          isDarkTheme ? "bg-white/10 border-white/15" : "bg-white/80 border-slate-200 shadow-sm"
        }`}
      >
        <button
          onClick={() => setActiveSubTab("day")}
          className={`flex-1 min-w-[110px] py-2 px-3 rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-1.5 ${
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
          className={`flex-1 min-w-[110px] py-2 px-3 rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-1.5 ${
            activeSubTab === "month"
              ? "bg-amber-500 text-slate-950 shadow-lg shadow-amber-500/25"
              : isDarkTheme
              ? "text-slate-300 hover:text-white hover:bg-white/5"
              : "text-slate-600 hover:text-slate-900 hover:bg-slate-100"
          }`}
        >
          <CalendarDays className="w-4 h-4" />
          Lịch Tháng Âm - Dương
        </button>
        <button
          onClick={() => {
            setActiveSubTab("good_days");
            handleFindGoodDays();
          }}
          className={`flex-1 min-w-[110px] py-2 px-3 rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-1.5 ${
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
        <button
          onClick={() => setActiveSubTab("fortune")}
          className={`flex-1 min-w-[125px] py-2 px-3 rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-1.5 ${
            activeSubTab === "fortune"
              ? "bg-amber-500 text-slate-950 shadow-lg shadow-amber-500/25"
              : isDarkTheme
              ? "text-slate-300 hover:text-white hover:bg-white/5"
              : "text-slate-600 hover:text-slate-900 hover:bg-slate-100"
          }`}
        >
          <Sparkles className="w-4 h-4 text-amber-500" />
          Bói Tử Vi & Vận Mệnh
        </button>
        <button
          onClick={() => setActiveSubTab("converter")}
          className={`flex-1 min-w-[110px] py-2 px-3 rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-1.5 ${
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

            {/* CÁ NHÂN HÓA THEO TUỔI GIA CHỦ */}
            {profile && personalMatch ? (
              <div
                className={`mt-5 p-5 rounded-3xl border transition-all ${
                  personalMatch.verdict === ActivityVerdict.BAD
                    ? isDarkTheme
                      ? "bg-rose-950/20 border-rose-500/35 text-white"
                      : "bg-rose-50/70 border-rose-200 text-slate-900"
                    : personalMatch.points >= 3
                    ? isDarkTheme
                      ? "bg-emerald-950/25 border-emerald-500/40 text-white"
                      : "bg-emerald-50/70 border-emerald-200 text-slate-900"
                    : isDarkTheme
                    ? "bg-white/5 border-white/10 text-white"
                    : "bg-amber-50/50 border-amber-200/70 text-slate-900"
                }`}
              >
                <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 pb-3 border-b border-black/5 dark:border-white/10">
                  <div className="flex items-center gap-2.5">
                    <div className="w-10 h-10 rounded-2xl bg-amber-500/20 text-amber-500 flex items-center justify-center font-bold text-sm shrink-0">
                      👤
                    </div>
                    <div>
                      <div className="text-sm font-black flex items-center gap-2">
                        Đối Chiếu Phong Thủy Tuổi: {profile.canChi} ({profile.birthYear})
                        <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-700 dark:text-amber-300 border border-amber-400/30">
                          {profile.element} • {profile.napAm}
                        </span>
                      </div>
                      <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                        {profile.yinYangLabel} • Cung {profile.cungMenh.menhNam} (Nam) / {profile.cungMenh.menhNu} (Nữ)
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2.5">
                    <span
                      className={`text-xs font-black px-3 py-1 rounded-full border ${
                        personalMatch.badgeColor === "rose"
                          ? "bg-rose-500/20 text-rose-600 dark:text-rose-300 border-rose-500/40"
                          : personalMatch.badgeColor === "emerald"
                          ? "bg-emerald-500/20 text-emerald-700 dark:text-emerald-300 border-emerald-500/40"
                          : personalMatch.badgeColor === "orange"
                          ? "bg-orange-500/20 text-orange-700 dark:text-orange-300 border-orange-500/40"
                          : "bg-slate-200/70 text-slate-700 dark:bg-white/10 dark:text-slate-300 border-slate-300/40"
                      }`}
                    >
                      {personalMatch.badgeLabel}
                    </span>
                    <span className="text-xs font-extrabold text-amber-600 dark:text-amber-400">
                      Hợp tuổi: {personalMatch.scorePercent}%
                    </span>
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-3.5">
                  <div className="space-y-2">
                    <div className="text-xs font-bold text-slate-700 dark:text-slate-200 uppercase tracking-wider">
                      Tương quan Can Chi & Ngũ Hành:
                    </div>
                    <ul className="space-y-1.5 text-xs text-slate-600 dark:text-slate-300">
                      {personalMatch.notes.map((note, idx) => (
                        <li key={idx} className="leading-relaxed">
                          {note}
                        </li>
                      ))}
                    </ul>
                  </div>

                  <div className="space-y-2.5">
                    <div className="text-xs font-bold text-slate-700 dark:text-slate-200 uppercase tracking-wider">
                      Lời khuyên hành sự cho gia chủ:
                    </div>
                    <div className="p-3 rounded-2xl bg-black/[0.02] dark:bg-white/[0.04] border border-black/[0.04] dark:border-white/[0.06] text-xs space-y-1.5">
                      <p className="leading-relaxed">
                        <strong className="text-emerald-600 dark:text-emerald-400">Cát sự:</strong> {personalMatch.luckyAdvice}
                      </p>
                      <p className="leading-relaxed">
                        <strong className="text-rose-500">Cần phòng:</strong> {personalMatch.cautionAdvice}
                      </p>
                      <p className="leading-relaxed text-slate-500 dark:text-slate-400 pt-1 border-t border-black/5 dark:border-white/5">
                        🎨 <strong>Màu sắc may mắn:</strong> {profile.luckyColors}
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            ) : (
              <div
                className={`mt-5 p-4 rounded-3xl border flex flex-col sm:flex-row items-center justify-between gap-3 text-xs ${
                  isDarkTheme ? "bg-white/5 border-white/10" : "bg-amber-50/60 border-amber-200/70"
                }`}
              >
                <div className="flex items-center gap-2.5">
                  <Sparkles className="w-5 h-5 text-amber-500 shrink-0" />
                  <span>
                    Chưa nhập năm sinh để xem đối chiếu phong thủy riêng! Nhập năm sinh để biết hôm nay có hợp tuổi bạn không.
                  </span>
                </div>
                <button
                  onClick={() => setShowBirthYearModal(true)}
                  className="px-3.5 py-1.5 rounded-xl font-bold bg-amber-500 hover:bg-amber-600 text-slate-950 shrink-0 shadow-xs"
                >
                  Nhập năm sinh
                </button>
              </div>
            )}
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
                <span className="w-2 h-2 rounded-full bg-slate-400 dark:bg-slate-500" />
                Hắc Đạo
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
                      ) : (
                        <span
                          className="inline-flex items-center gap-1 text-[9px] sm:text-[10px] font-medium px-1.5 py-0.5 rounded-md bg-slate-100/90 dark:bg-white/5 text-slate-500 dark:text-slate-400 border border-slate-200/80 dark:border-white/10"
                          title="Ngày Hắc Đạo"
                        >
                          <span className="w-1.5 h-1.5 rounded-full bg-slate-400 dark:bg-slate-500 shrink-0" />
                          <span className="hidden md:inline">Hắc Đạo</span>
                          <span className="md:hidden">Hắc</span>
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

                  {/* Bottom Row: Lunar Date & Can Chi / Tuổi hợp xung */}
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

                    <div className="flex items-center gap-1 min-w-0">
                      {item.personal && item.personal.verdict === ActivityVerdict.BAD ? (
                        <span
                          className="text-[8.5px] sm:text-[9px] font-bold text-rose-500 bg-rose-500/10 px-1 py-0.5 rounded shrink-0"
                          title={`Xung khắc tuổi ${profile?.canChi}: ${item.personal.notes[0]}`}
                        >
                          Xung
                        </span>
                      ) : item.personal && item.personal.points >= 2 ? (
                        <span
                          className="text-[8.5px] sm:text-[9px] font-bold text-emerald-600 dark:text-emerald-400 bg-emerald-500/10 px-1 py-0.5 rounded shrink-0"
                          title={`Hợp tuổi ${profile?.canChi}: ${item.personal.notes[0]}`}
                        >
                          Hợp
                        </span>
                      ) : null}
                      <span className="text-[9px] sm:text-[10px] font-medium text-slate-500 dark:text-slate-400 truncate max-w-[55px] sm:max-w-[75px]">
                        {item.lunarDate.canDay} {item.lunarDate.chiDay}
                      </span>
                    </div>
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
          className={`p-6 sm:p-8 rounded-3xl backdrop-blur-xl border shadow-xl ${
            isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
          }`}
        >
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-6 pb-4 border-b border-amber-500/20">
            <div>
              <h3 className={`text-base sm:text-lg font-bold flex items-center gap-2 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                <Sparkles className="w-5 h-5 text-amber-500" />
                Tra Cứu Ngày Tốt Trong 45 Ngày Tới
              </h3>
              <p className={`text-xs mt-0.5 ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                {profile
                  ? `Lọc ngày Hoàng Đạo, cát tinh và đối chiếu hòa hợp với tuổi ${profile.canChi} (${profile.birthYear})`
                  : "Lọc ngày Hoàng Đạo, trực cát tinh theo phong tục truyền thống Việt Nam"}
              </p>
            </div>

            {/* Select activity & Options */}
            <div className="flex flex-wrap items-center gap-2.5 w-full sm:w-auto">
              <select
                value={selectedActivity}
                onChange={(e) => {
                  setSelectedActivity(e.target.value as AlmanacActivity);
                }}
                className={`w-full sm:w-auto p-2.5 rounded-xl border text-xs font-bold focus:outline-none ${
                  isDarkTheme ? "bg-slate-900 border-white/20 text-white" : "bg-white border-slate-300 text-slate-900"
                }`}
              >
                {Object.keys(ACTIVITY_META).map((actKey) => (
                  <option key={actKey} value={actKey}>
                    {ACTIVITY_META[actKey as AlmanacActivity].emoji} {ACTIVITY_META[actKey as AlmanacActivity].label}
                  </option>
                ))}
              </select>

              {profile && (
                <label className="flex items-center gap-1.5 cursor-pointer text-xs font-semibold px-2.5 py-1.5 rounded-xl bg-black/[0.03] dark:bg-white/[0.05] border border-black/5 dark:border-white/10">
                  <input
                    type="checkbox"
                    checked={filterPersonalMatch}
                    onChange={(e) => setFilterPersonalMatch(e.target.checked)}
                    className="w-3.5 h-3.5 rounded text-amber-500 focus:ring-0"
                  />
                  <span>Hợp tuổi {profile.canChi}</span>
                </label>
              )}

              <button
                onClick={handleFindGoodDays}
                className="px-4 py-2 rounded-xl bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold text-xs shrink-0 shadow-sm"
              >
                Tìm Kiếm
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
                  className={`flex flex-col sm:flex-row sm:items-center justify-between p-4 rounded-2xl border transition-all cursor-pointer gap-2.5 hover:-translate-y-0.5 ${
                    isDarkTheme
                      ? "bg-white/5 border-white/10 hover:bg-white/10 text-white"
                      : "bg-slate-50 border-slate-200 hover:bg-slate-100 text-slate-900 shadow-2xs"
                  }`}
                >
                  <div>
                    <div className={`text-sm sm:text-base font-extrabold flex items-center gap-2 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                      Thứ {item.date.getDay() === 0 ? "Chủ Nhật" : item.date.getDay() + 1},{" "}
                      ngày {item.date.getDate()}/{item.date.getMonth() + 1}/{item.date.getFullYear()}
                      {item.lunar.day === 1 || item.lunar.day === 15 ? (
                        <span className="text-xs px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-600 dark:text-amber-300 font-bold border border-amber-400/30">
                          {item.lunar.day === 1 ? "🌑 Mùng 1" : "🌕 Ngày Rằm"}
                        </span>
                      ) : null}
                    </div>
                    <div className={`text-xs mt-1 flex items-center gap-2 flex-wrap ${isDarkTheme ? "text-amber-300" : "text-amber-700 font-medium"}`}>
                      <span>{item.lunar.formattedLunarMedium}</span>
                      <span>•</span>
                      <span>{item.lunar.solarTerm}</span>
                      <span>•</span>
                      <span>{item.almanac.truc.fullName}</span>
                    </div>
                    {item.personalMatch && (
                      <p className="text-[11px] text-slate-500 dark:text-slate-400 mt-1">
                        👤 {item.personalMatch.notes[0]}
                      </p>
                    )}
                  </div>

                  <div className="flex items-center sm:flex-col sm:items-end justify-between gap-1.5 shrink-0">
                    <div className="flex items-center gap-2">
                      {item.personalMatch && (
                        <span
                          className={`text-xs font-bold px-2.5 py-0.5 rounded-full border ${
                            item.personalMatch.badgeColor === "emerald"
                              ? "bg-emerald-500/15 text-emerald-700 dark:text-emerald-300 border-emerald-500/30"
                              : item.personalMatch.badgeColor === "rose"
                              ? "bg-rose-500/15 text-rose-600 dark:text-rose-300 border-rose-500/30"
                              : "bg-slate-200/70 text-slate-700 dark:bg-white/10 dark:text-slate-300 border-slate-300/40"
                          }`}
                        >
                          {item.personalMatch.badgeLabel}
                        </span>
                      )}
                      <span className="text-xs font-bold text-amber-600 dark:text-amber-300 px-2.5 py-0.5 rounded-full bg-amber-500/15 border border-amber-400/30">
                        {item.lunar.isAuspiciousDay ? "Hoàng Đạo" : "Hắc Đạo"}
                      </span>
                    </div>
                    <div className={`text-xs font-semibold ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                      Điểm ngày: <strong>{item.score}/100</strong>
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className={`text-center py-10 text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                Không tìm thấy ngày tốt đặc biệt cho công việc này trong 45 ngày tới hoặc hãy bấm nút Tìm Kiếm.
              </div>
            )}
          </div>
        </div>
      )}

      {/* 5. BÓI TỬ VI & VẬN MỆNH TRỌN ĐỜI */}
      {activeSubTab === "fortune" && (
        <div className="space-y-6">
          {!profile ? (
            <div
              className={`p-8 sm:p-12 rounded-3xl backdrop-blur-xl border text-center shadow-xl ${
                isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
              }`}
            >
              <div className="w-16 h-16 rounded-3xl bg-amber-500/20 text-amber-500 flex items-center justify-center mx-auto mb-4 font-bold text-3xl">
                🔮
              </div>
              <h3 className="text-xl sm:text-2xl font-black mb-2">Bói Tử Vi & Vận Mệnh Bản Mệnh</h3>
              <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 max-w-md mx-auto mb-6 leading-relaxed">
                Khám phá bí mật tử vi theo năm sinh: Ngũ Hành Nạp Âm, tướng tinh con giáp, Bát Trạch phong thủy hướng nhà, màu sắc may mắn và bộ tuổi Tam Hợp/Lục Hợp trọn đời.
              </p>
              <button
                onClick={() => setShowBirthYearModal(true)}
                className="px-6 py-3 rounded-2xl font-black text-sm bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-600 hover:to-amber-700 text-slate-950 shadow-lg shadow-amber-500/25 transition-all inline-flex items-center gap-2"
              >
                <User className="w-4 h-4" />
                Chọn Năm Sinh Để Xem Lá Số ›
              </button>

              <div className="mt-8 pt-6 border-t border-black/5 dark:border-white/10">
                <span className="text-xs font-bold text-slate-400 block mb-3">Hoặc chọn nhanh năm sinh phổ biến:</span>
                <div className="flex items-center justify-center gap-2 flex-wrap max-w-xl mx-auto">
                  {[1988, 1989, 1990, 1991, 1992, 1993, 1994, 1995, 1996, 1997, 1998, 1999, 2000, 2001, 2002, 2003].map((y) => {
                    const p = getPersonalProfile(y);
                    return (
                      <button
                        key={y}
                        onClick={() => setBirthYear(y)}
                        className={`text-xs px-3 py-1.5 rounded-xl font-bold transition-all ${
                          isDarkTheme
                            ? "bg-white/5 hover:bg-white/10 text-slate-200 border border-white/10"
                            : "bg-slate-100 hover:bg-slate-200 text-slate-800 border border-slate-200"
                        }`}
                      >
                        {y} ({p.canChi})
                      </button>
                    );
                  })}
                </div>
              </div>
            </div>
          ) : (
            <div className="space-y-6">
              {/* Header Profile Summary */}
              <div
                className={`p-6 sm:p-8 rounded-3xl backdrop-blur-xl border shadow-xl relative overflow-hidden ${
                  isDarkTheme
                    ? "bg-gradient-to-br from-amber-500/15 via-slate-900/90 to-amber-950/20 border-amber-500/30 text-white"
                    : "bg-gradient-to-br from-amber-50 via-white to-orange-50/80 border-amber-200 text-slate-900"
                }`}
              >
                <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                  <div className="flex items-center gap-4">
                    <div className="w-16 h-16 rounded-3xl bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center text-slate-950 font-black text-2xl shadow-lg shrink-0">
                      {profile.zodiacAnimal.includes("Chuột") ? "🐀" :
                       profile.zodiacAnimal.includes("Trâu") ? "🐂" :
                       profile.zodiacAnimal.includes("Hổ") ? "🐅" :
                       profile.zodiacAnimal.includes("Mèo") ? "🐈" :
                       profile.zodiacAnimal.includes("Rồng") ? "🐉" :
                       profile.zodiacAnimal.includes("Rắn") ? "🐍" :
                       profile.zodiacAnimal.includes("Ngựa") ? "🐎" :
                       profile.zodiacAnimal.includes("Dê") ? "🐐" :
                       profile.zodiacAnimal.includes("Khỉ") ? "🐒" :
                       profile.zodiacAnimal.includes("Gà") ? "🐓" :
                       profile.zodiacAnimal.includes("Chó") ? "🐕" : "🐖"}
                    </div>
                    <div>
                      <div className="flex items-center gap-2 flex-wrap">
                        <h2 className="text-xl sm:text-2xl font-black">
                          Tuổi {profile.canChi} ({profile.birthYear})
                        </h2>
                        <span className="text-xs font-bold px-3 py-1 rounded-full bg-amber-500/20 text-amber-700 dark:text-amber-300 border border-amber-400/40">
                          {profile.element} • {profile.napAm}
                        </span>
                      </div>
                      <p className="text-xs sm:text-sm text-slate-600 dark:text-slate-300 mt-1">
                        Cầm tinh con <strong>{profile.zodiacAnimal}</strong> • Thiên Can: <strong>{CAN[profile.canIndex]}</strong> ({profile.canElement}) • Địa Chi: <strong>{CHI[profile.chiIndex]}</strong> ({profile.chiElement})
                      </p>
                    </div>
                  </div>

                  <button
                    onClick={() => setShowBirthYearModal(true)}
                    className="px-4 py-2 rounded-xl text-xs font-bold bg-amber-500 hover:bg-amber-600 text-slate-950 transition-all shadow-sm shrink-0"
                  >
                    Đổi năm sinh
                  </button>
                </div>
              </div>

              {/* Grid 4 cards */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* 1. Tính Cách & Vận Mệnh Con Giáp */}
                <div
                  className={`p-6 rounded-3xl backdrop-blur-xl border shadow-md ${
                    isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
                  }`}
                >
                  <h3 className="text-sm font-extrabold flex items-center gap-2 mb-3 text-amber-500">
                    <Award className="w-4 h-4" />
                    Tính Cách & Tướng Tinh Con Giáp ({ZODIAC_TRAITS[profile.chiIndex].title})
                  </h3>
                  <div className="space-y-3 text-xs leading-relaxed text-slate-600 dark:text-slate-300">
                    <div>
                      <strong className="text-slate-900 dark:text-white block mb-0.5">Đặc tính cốt lõi:</strong>
                      {ZODIAC_TRAITS[profile.chiIndex].personality}
                    </div>
                    <div>
                      <strong className="text-slate-900 dark:text-white block mb-0.5">Lĩnh vực sự nghiệp đại cát:</strong>
                      {ZODIAC_TRAITS[profile.chiIndex].career}
                    </div>
                  </div>
                </div>

                {/* 2. Bát Trạch Cung Phi & Hướng Phong Thủy */}
                <div
                  className={`p-6 rounded-3xl backdrop-blur-xl border shadow-md ${
                    isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
                  }`}
                >
                  <h3 className="text-sm font-extrabold flex items-center gap-2 mb-3 text-emerald-500">
                    <Compass className="w-4 h-4" />
                    Bát Trạch Cung Phi & Hướng Nhà Cát Lợi
                  </h3>
                  <div className="space-y-3 text-xs leading-relaxed text-slate-600 dark:text-slate-300">
                    <div className="p-3 rounded-2xl bg-black/[0.02] dark:bg-white/[0.04] border border-black/[0.04] dark:border-white/[0.06]">
                      <span className="font-bold text-slate-900 dark:text-white">Nam mạng:</span> Cung <strong>{profile.cungMenh.menhNam}</strong> ({profile.cungMenh.nhomNam})
                      <div className="text-[11px] text-emerald-600 dark:text-emerald-400 mt-1">
                        Hướng tốt (Sinh Khí, Thiên Y, Diên Niên, Phục Vị): <strong>{profile.cungMenh.huongTotNam.join(", ")}</strong>
                      </div>
                    </div>

                    <div className="p-3 rounded-2xl bg-black/[0.02] dark:bg-white/[0.04] border border-black/[0.04] dark:border-white/[0.06]">
                      <span className="font-bold text-slate-900 dark:text-white">Nữ mạng:</span> Cung <strong>{profile.cungMenh.menhNu}</strong> ({profile.cungMenh.nhomNu})
                      <div className="text-[11px] text-emerald-600 dark:text-emerald-400 mt-1">
                        Hướng tốt (Sinh Khí, Thiên Y, Diên Niên, Phục Vị): <strong>{profile.cungMenh.huongTotNu.join(", ")}</strong>
                      </div>
                    </div>
                  </div>
                </div>

                {/* 3. Tam Hợp, Lục Hợp & Tứ Hành Xung */}
                <div
                  className={`p-6 rounded-3xl backdrop-blur-xl border shadow-md ${
                    isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
                  }`}
                >
                  <h3 className="text-sm font-extrabold flex items-center gap-2 mb-3 text-indigo-500">
                    <CheckCircle2 className="w-4 h-4" />
                    Tuổi Hợp & Tuổi Kỵ Trong Làm Ăn / Kết Hôn
                  </h3>
                  <div className="space-y-2.5 text-xs text-slate-600 dark:text-slate-300">
                    <div>
                      <span className="font-bold text-emerald-600 dark:text-emerald-400">Tam Hợp:</span> Tuổi{" "}
                      <strong>{profile.tamHop.join(", ")}</strong> (hợp tác làm ăn phát đạt, gia đạo hòa hợp).
                    </div>
                    <div>
                      <span className="font-bold text-emerald-600 dark:text-emerald-400">Lục Hợp (Nhị Hợp):</span> Tuổi{" "}
                      <strong>{profile.lucHop}</strong> (quý nhân tương trợ, trăm sự hanh thông).
                    </div>
                    <div className="pt-2 border-t border-black/5 dark:border-white/5">
                      <span className="font-bold text-rose-500">Tứ Hành Xung:</span> Kỵ tuổi{" "}
                      <strong>{profile.tuHanhXung.join(", ")}</strong> (dễ bất đồng quan điểm, nên nhường nhịn).
                    </div>
                  </div>
                </div>

                {/* 4. Màu Sắc & Con Số May Mắn */}
                <div
                  className={`p-6 rounded-3xl backdrop-blur-xl border shadow-md ${
                    isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
                  }`}
                >
                  <h3 className="text-sm font-extrabold flex items-center gap-2 mb-3 text-amber-500">
                    <Sparkles className="w-4 h-4" />
                    Màu Sắc May Mắn & Số Kích Tài Lộc
                  </h3>
                  <div className="space-y-2.5 text-xs text-slate-600 dark:text-slate-300">
                    <div>
                      <strong className="text-emerald-600 dark:text-emerald-400 block mb-0.5">Màu hợp phong thủy:</strong>
                      {profile.luckyColors}
                    </div>
                    <div>
                      <strong className="text-rose-500 block mb-0.5">Màu tương khắc nên hạn chế:</strong>
                      {profile.unluckyColors}
                    </div>
                    <div className="pt-2 border-t border-black/5 dark:border-white/5">
                      <strong className="text-amber-600 dark:text-amber-400 block mb-1">Con số may mắn kích hoạt tài khí:</strong>
                      <div className="flex items-center gap-2">
                        {profile.luckyNumbers.map((num) => (
                          <span
                            key={num}
                            className="w-7 h-7 rounded-xl bg-amber-500/20 text-amber-700 dark:text-amber-300 font-black flex items-center justify-center border border-amber-400/40 text-xs"
                          >
                            {num}
                          </span>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Modal Chọn Năm Sinh Gia Chủ */}
      {showBirthYearModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-in fade-in duration-200">
          <div
            className={`w-full max-w-lg p-6 rounded-3xl border shadow-2xl overflow-hidden transition-all ${
              isDarkTheme ? "bg-slate-900 border-white/20 text-white" : "bg-white border-slate-200 text-slate-900"
            }`}
          >
            <div className="flex items-center justify-between pb-3 mb-4 border-b border-black/5 dark:border-white/10">
              <h3 className="text-base font-black flex items-center gap-2">
                <User className="w-5 h-5 text-amber-500" />
                Thiết Lập Năm Sinh & Tử Vi Gia Chủ
              </h3>
              <button
                onClick={() => setShowBirthYearModal(false)}
                className={`p-1.5 rounded-xl transition-all ${
                  isDarkTheme ? "hover:bg-white/10 text-slate-400" : "hover:bg-slate-100 text-slate-500"
                }`}
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <p className="text-xs text-slate-500 dark:text-slate-400 mb-4 leading-relaxed">
              Nhập hoặc chọn năm sinh Dương lịch của bạn để Weather 24h đối chiếu lá số Tử Vi, Bát Trạch phong thủy, tuổi Tam Hợp/Lục Xung và cá nhân hóa lịch ngày tốt.
            </p>

            <div className="space-y-4">
              <div>
                <label className="block text-xs font-bold mb-1.5 text-slate-700 dark:text-slate-200">
                  Chọn năm sinh (1930 - 2025):
                </label>
                <select
                  value={tempYear}
                  onChange={(e) => setTempYear(Number(e.target.value))}
                  className={`w-full p-3 rounded-2xl border text-sm font-bold focus:outline-none focus:ring-2 focus:ring-amber-500 ${
                    isDarkTheme ? "bg-slate-800 border-white/20 text-white" : "bg-slate-50 border-slate-300 text-slate-900"
                  }`}
                >
                  {Array.from({ length: 96 }).map((_, i) => {
                    const y = 2025 - i;
                    const p = getPersonalProfile(y);
                    return (
                      <option key={y} value={y}>
                        {y} — Tuổi {p.canChi} ({p.zodiacAnimal.split(" ")[0]}) • Mệnh {p.element} ({p.napAm})
                      </option>
                    );
                  })}
                </select>
              </div>

              {/* Quick Decade Shortcuts */}
              <div className="flex items-center gap-1.5 flex-wrap">
                <span className="text-[11px] font-semibold text-slate-400 mr-1">Nhanh:</span>
                {[1980, 1985, 1990, 1995, 2000, 2005].map((y) => (
                  <button
                    key={y}
                    onClick={() => setTempYear(y)}
                    className={`text-xs px-2.5 py-1 rounded-xl font-bold transition-all ${
                      tempYear === y
                        ? "bg-amber-500 text-slate-950 shadow-xs"
                        : isDarkTheme
                        ? "bg-white/10 hover:bg-white/15 text-slate-300"
                        : "bg-slate-100 hover:bg-slate-200 text-slate-700"
                    }`}
                  >
                    {y}
                  </button>
                ))}
              </div>

              {/* Preview Bản Mệnh */}
              {(() => {
                const previewProfile = getPersonalProfile(tempYear);
                return (
                  <div
                    className={`p-4 rounded-2xl border ${
                      isDarkTheme
                        ? "bg-gradient-to-br from-amber-500/10 via-slate-800/60 to-slate-900 border-amber-500/30"
                        : "bg-amber-50/80 border-amber-200"
                    }`}
                  >
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-sm font-black text-amber-600 dark:text-amber-400">
                        Tuổi {previewProfile.canChi} ({previewProfile.birthYear})
                      </span>
                      <span className="text-xs font-bold px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-700 dark:text-amber-300 border border-amber-400/30">
                        {previewProfile.element} • {previewProfile.napAm}
                      </span>
                    </div>
                    <div className="grid grid-cols-2 gap-2 text-xs text-slate-600 dark:text-slate-300">
                      <div>• Con giáp: <strong>{previewProfile.zodiacAnimal}</strong></div>
                      <div>• {previewProfile.yinYangLabel}</div>
                      <div>• Tam hợp: <strong className="text-emerald-600 dark:text-emerald-400">{previewProfile.tamHop.join(", ")}</strong></div>
                      <div>• Tứ hành xung: <strong className="text-rose-500">{previewProfile.tuHanhXung.join(", ")}</strong></div>
                      <div className="col-span-2 pt-1 border-t border-black/5 dark:border-white/5">
                        • Cung phi: <strong>{previewProfile.cungMenh.menhNam}</strong> (Nam) / <strong>{previewProfile.cungMenh.menhNu}</strong> (Nữ)
                      </div>
                    </div>
                  </div>
                );
              })()}

              <div className="flex items-center justify-between gap-3 pt-2">
                {birthYear > 0 ? (
                  <button
                    onClick={() => {
                      setBirthYear(0);
                      setShowBirthYearModal(false);
                    }}
                    className="px-4 py-2.5 rounded-xl text-xs font-bold text-rose-500 hover:bg-rose-500/10 transition-all"
                  >
                    Xóa năm sinh
                  </button>
                ) : (
                  <div />
                )}

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => setShowBirthYearModal(false)}
                    className={`px-4 py-2.5 rounded-xl text-xs font-bold transition-all ${
                      isDarkTheme ? "bg-white/10 hover:bg-white/15 text-slate-300" : "bg-slate-100 hover:bg-slate-200 text-slate-700"
                    }`}
                  >
                    Hủy
                  </button>
                  <button
                    onClick={() => {
                      setBirthYear(tempYear);
                      setShowBirthYearModal(false);
                    }}
                    className="px-5 py-2.5 rounded-xl text-xs font-black bg-amber-500 hover:bg-amber-600 text-slate-950 transition-all shadow-md flex items-center gap-1.5"
                  >
                    <Check className="w-4 h-4" />
                    Lưu & Sử dụng
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
