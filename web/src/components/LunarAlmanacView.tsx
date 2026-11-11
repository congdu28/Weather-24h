"use client";

import React, { useState, useMemo, useEffect, useSyncExternalStore } from "react";
import { createPortal } from "react-dom";
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
  DayLevel,
  PersonalProfile,
  PersonalDayMatch,
  getPersonalProfile,
  matchPersonalDay,
  ActivityVerdict,
  DayAlmanac,
  UserBirthProfile,
  DetailedBirthFortune,
  getDetailedBirthFortune,
  elementLabel,
  BIRTH_HOUR_OPTIONS,
  FiveElement,
  FIVE_ELEMENT_META,
  DeepFortuneAnalysis
} from "../utils/lunarAlmanac";
import {
  analyzeFortuneWithGemini38FlashHigh,
  getCachedGeminiFortune,
  getFortuneProfileCacheKey
} from "../services/geminiFortuneService";
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
  Check,
  Heart,
  Shield,
  TrendingUp,
  Zap,
  Star,
  RefreshCw,
  BrainCircuit
} from "lucide-react";
import { DailyForecast } from "../types/weather";
import { getWeatherCondition } from "../services/weatherApi";
import WeatherIcon from "./WeatherIcon";
import { usePersistentState } from "../utils/usePersistentState";

interface Props {
  isDarkTheme?: boolean;
  dailyForecast?: DailyForecast[];
  geminiApiKey?: string;
}

const emptySubscribe = () => () => {};

export default function LunarAlmanacView({ isDarkTheme = true, dailyForecast, geminiApiKey }: Props) {
  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [activeSubTab, setActiveSubTab] = useState<"day" | "month" | "good_days" | "fortune" | "converter">("day");

  // Hồ sơ ngày sinh & giới tính gia chủ lưu bền vững trong localStorage
  const [userProfile, setUserProfile] = usePersistentState<UserBirthProfile | null>("weather24h_user_profile", null);
  const [birthYear, setBirthYear] = usePersistentState<number>("weather24h_birth_year", 0);
  const [showBirthProfileModal, setShowBirthProfileModal] = useState<boolean>(false);
  const [filterPersonalMatch, setFilterPersonalMatch] = useState<boolean>(true);
  const [showFortunePromptBanner, setShowFortunePromptBanner] = useState<boolean>(true);

  // Client mounted state for createPortal (SSR safe via useSyncExternalStore)
  const mounted = useSyncExternalStore(emptySubscribe, () => true, () => false);

  // Khóa scroll trang nền khi mở modal thiết lập sinh & tử vi
  useEffect(() => {
    if (showBirthProfileModal) {
      document.body.style.overflow = "hidden";
      return () => {
        document.body.style.overflow = "unset";
      };
    }
  }, [showBirthProfileModal]);

  // State tạm thời cho Modal thiết lập ngày, tháng, năm, giờ sinh & giới tính
  const [tempGender, setTempGender] = useState<"male" | "female">("male");
  const [tempDay, setTempDay] = useState<number>(15);
  const [tempMonth, setTempMonth] = useState<number>(6);
  const [tempYear, setTempYear] = useState<number>(1990);
  const [tempIsLunar, setTempIsLunar] = useState<boolean>(false);
  const [tempBirthHour, setTempBirthHour] = useState<number>(6); // Mặc định Giờ Ngọ (11:00 - 12:59), -1 là chưa rõ giờ

  // Phân mục bên trong Tab 5 (Bói Tử Vi & Vận Mệnh)
  const [fortuneCategory, setFortuneCategory] = useState<"deep_ai" | "battrach" | "lifestages" | "numerology" | "zodiac" | "year2026">("deep_ai");

  // State cho phân tích chuyên sâu bằng Gemini 3.8 Flash High
  const [aiResultsByKey, setAiResultsByKey] = useState<
    Record<string, { analysis: DeepFortuneAnalysis; source: "gemini-3.8-flash-live" | "gemini-3.8-flash-builtin" }>
  >({});
  const [isAnalyzingAi, setIsAnalyzingAi] = useState<boolean>(false);

  // Hồ sơ hợp nhất đảm bảo tương thích ngược nếu trước đó đã lưu birthYear
  const effectiveProfile: UserBirthProfile | null = useMemo(() => {
    if (userProfile && userProfile.year >= 1900 && userProfile.year <= 2100) {
      return userProfile;
    }
    if (birthYear >= 1900 && birthYear <= 2100) {
      return { day: 15, month: 6, year: birthYear, gender: "male", isLunar: false };
    }
    return null;
  }, [userProfile, birthYear]);

  // Kiểm tra xem người dùng đã nhập đầy đủ thông tin (bao gồm cả Giờ sinh) chưa
  const isProfileCompleteWithHour = useMemo(() => {
    return Boolean(
      userProfile &&
        userProfile.year >= 1900 &&
        typeof userProfile.birthHour === "number" &&
        userProfile.birthHour >= 0 &&
        userProfile.birthHour <= 11
    );
  }, [userProfile]);

  // Chi tiết luận giải tử vi vận mệnh theo giới tính & giờ, ngày, tháng, năm sinh
  const detailedFortune: DetailedBirthFortune | null = useMemo(() => {
    if (!effectiveProfile) return null;
    try {
      return getDetailedBirthFortune(effectiveProfile);
    } catch {
      return null;
    }
  }, [effectiveProfile]);

  // Kết quả luận giải chi tiết bằng AI (ưu tiên kết quả vừa chạy hoặc cache, fallback có sẵn)
  const activeDeepAnalysis = useMemo(() => {
    if (!detailedFortune) {
      return null;
    }
    const key = getFortuneProfileCacheKey(detailedFortune);
    const inMemory = aiResultsByKey[key];
    if (inMemory) {
      return inMemory.analysis;
    }
    const cached = getCachedGeminiFortune(detailedFortune);
    if (cached) {
      return cached;
    }
    return detailedFortune.deepAnalysis;
  }, [detailedFortune, aiResultsByKey]);

  const handleTriggerGeminiDeepAnalysis = async (forceRefresh = true) => {
    if (!detailedFortune || isAnalyzingAi) return;
    setIsAnalyzingAi(true);
    try {
      const key = getFortuneProfileCacheKey(detailedFortune);
      const res = await analyzeFortuneWithGemini38FlashHigh(detailedFortune, geminiApiKey, forceRefresh);
      setAiResultsByKey((prev) => ({
        ...prev,
        [key]: res
      }));
    } finally {
      setIsAnalyzingAi(false);
    }
  };

  const openProfileModal = () => {
    if (userProfile) {
      setTempGender(userProfile.gender);
      setTempDay(userProfile.day);
      setTempMonth(userProfile.month);
      setTempYear(userProfile.year);
      setTempIsLunar(userProfile.isLunar || false);
      setTempBirthHour(
        typeof userProfile.birthHour === "number" && userProfile.birthHour >= -1 && userProfile.birthHour <= 11
          ? userProfile.birthHour
          : 6
      );
    } else if (birthYear >= 1900 && birthYear <= 2100) {
      setTempGender("male");
      setTempDay(15);
      setTempMonth(6);
      setTempYear(birthYear);
      setTempIsLunar(false);
      setTempBirthHour(6);
    } else {
      setTempGender("male");
      setTempDay(15);
      setTempMonth(6);
      setTempYear(1990);
      setTempIsLunar(false);
      setTempBirthHour(6);
    }
    setShowBirthProfileModal(true);
  };

  // Số ngày tối đa hợp lệ trong tháng được chọn của Modal (tự động điều chỉnh tháng 28/29/30/31 ngày hoặc 30 ngày Âm lịch)
  const maxModalDays = useMemo(() => {
    if (tempIsLunar) return 30;
    return new Date(tempYear, tempMonth, 0).getDate();
  }, [tempYear, tempMonth, tempIsLunar]);

  const effectiveTempDay = Math.min(tempDay, maxModalDays);

  const handleSaveProfile = () => {
    const p: UserBirthProfile = {
      day: effectiveTempDay,
      month: tempMonth,
      year: tempYear,
      gender: tempGender,
      isLunar: tempIsLunar,
      birthHour: tempBirthHour >= 0 ? tempBirthHour : undefined
    };
    setUserProfile(p);
    setBirthYear(tempYear);
    setShowBirthProfileModal(false);
  };

  const handleQuickSelectBirthHour = (hourChiIdx: number) => {
    const base = effectiveProfile || { day: 15, month: 6, year: 1990, gender: "male" as const, isLunar: false };
    const updated: UserBirthProfile = {
      ...base,
      birthHour: hourChiIdx
    };
    setUserProfile(updated);
    setBirthYear(updated.year);
  };

  const handleClearProfile = () => {
    setUserProfile(null);
    setBirthYear(0);
    setShowBirthProfileModal(false);
  };

  // Preview trực tiếp khi người dùng thao tác trong modal
  const modalPreviewFortune = useMemo(() => {
    try {
      return getDetailedBirthFortune({
        day: effectiveTempDay,
        month: tempMonth,
        year: tempYear,
        gender: tempGender,
        isLunar: tempIsLunar,
        birthHour: tempBirthHour >= 0 ? tempBirthHour : undefined
      });
    } catch {
      return null;
    }
  }, [effectiveTempDay, tempMonth, tempYear, tempGender, tempIsLunar, tempBirthHour]);

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

  // Hồ sơ bản mệnh phong thủy gia chủ (ưu tiên năm Âm lịch từ detailedFortune cho người sinh tháng 1-2 trước Tết)
  const profile: PersonalProfile | null = useMemo(() => {
    if (detailedFortune) return detailedFortune.personalProfile;
    const y = effectiveProfile ? effectiveProfile.year : birthYear;
    return y >= 1900 && y <= 2100 ? getPersonalProfile(y) : null;
  }, [detailedFortune, effectiveProfile, birthYear]);

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
            {detailedFortune ? (
              <>
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="text-sm sm:text-base font-black">
                    {detailedFortune.profile.gender === "male" ? "👨 Nam mạng" : "👩 Nữ mạng"} • Tuổi {detailedFortune.personalProfile.canChi} ({detailedFortune.personalProfile.birthYear})
                  </span>
                  <span className={`text-xs font-bold px-2.5 py-0.5 rounded-full border ${
                    isDarkTheme
                      ? "bg-amber-500/25 text-amber-200 border-amber-400/50"
                      : "bg-amber-500/20 text-amber-800 border-amber-400/40"
                  }`}>
                    Mệnh {elementLabel(detailedFortune.personalProfile.element)} • {detailedFortune.personalProfile.napAm}
                  </span>
                  {detailedFortune.birthHourFortune.hasBirthHour && (
                    <span className={`text-xs font-bold px-2.5 py-0.5 rounded-full border ${
                      isDarkTheme
                        ? "bg-purple-500/25 text-purple-200 border-purple-400/50"
                        : "bg-purple-500/15 text-purple-800 border-purple-400/40"
                    }`}>
                      ⏰ Giờ {detailedFortune.birthHourFortune.hourChi} ({detailedFortune.birthHourFortune.hourCanChi})
                    </span>
                  )}
                  <span className={`text-xs font-bold px-2 py-0.5 rounded-full border ${
                    isDarkTheme
                      ? "bg-indigo-500/25 text-indigo-200 border-indigo-400/50"
                      : "bg-indigo-500/15 text-indigo-700 border-indigo-400/30"
                  }`}>
                    {detailedFortune.westernZodiac.symbol} {detailedFortune.westernZodiac.name}
                  </span>
                  <span className={`text-xs font-bold px-2 py-0.5 rounded-full border ${
                    isDarkTheme
                      ? "bg-emerald-500/25 text-emerald-200 border-emerald-400/50"
                      : "bg-emerald-500/15 text-emerald-700 border-emerald-400/30"
                  }`}>
                    Số {detailedFortune.numerology.lifePathNumber}
                  </span>
                </div>
                <p className={`text-xs mt-1 font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                  Cung phi: <strong className={isDarkTheme ? "text-emerald-300 font-bold" : "text-emerald-700 font-bold"}>{detailedFortune.cungPhi}</strong> ({detailedFortune.cungPhiGroup}) • Tam hợp: <strong className={isDarkTheme ? "text-emerald-300 font-bold" : "text-emerald-700 font-bold"}>{detailedFortune.personalProfile.tamHop.join(", ")}</strong> • Kỵ xung: <strong className="text-rose-400 font-bold">{detailedFortune.personalProfile.tuHanhXung[0]}</strong>
                </p>
              </>
            ) : profile ? (
              <>
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="text-sm sm:text-base font-black">
                    Tuổi {profile.canChi} ({profile.birthYear})
                  </span>
                  <span className={`text-xs font-bold px-2.5 py-0.5 rounded-full border ${
                    isDarkTheme
                      ? "bg-amber-500/25 text-amber-200 border-amber-400/50"
                      : "bg-amber-500/20 text-amber-800 border-amber-400/40"
                  }`}>
                    Mệnh {elementLabel(profile.element)} • {profile.napAm}
                  </span>
                  <span className={`text-xs font-semibold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                    {profile.yinYangLabel}
                  </span>
                </div>
                <p className={`text-xs mt-1 font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                  Cung phi: <strong className={isDarkTheme ? "text-emerald-300 font-bold" : "text-emerald-700 font-bold"}>{profile.cungMenh.menhNam}</strong> (Nam) / <strong className={isDarkTheme ? "text-emerald-300 font-bold" : "text-emerald-700 font-bold"}>{profile.cungMenh.menhNu}</strong> (Nữ) • Tam hợp: <strong className={isDarkTheme ? "text-emerald-300 font-bold" : "text-emerald-700 font-bold"}>{profile.tamHop.join(", ")}</strong> • Kỵ xung: <strong className="text-rose-400 font-bold">{profile.tuHanhXung[0]}</strong>
                </p>
              </>
            ) : (
              <>
                <div className="text-sm font-black flex items-center gap-2">
                  <span>Chưa thiết lập giờ, ngày, tháng, năm sinh & giới tính</span>
                  <span className={`text-[10px] px-2 py-0.5 rounded-full font-bold ${
                    isDarkTheme ? "bg-amber-500/30 text-amber-200 border border-amber-400/40" : "bg-amber-500/20 text-amber-800"
                  }`}>
                    Luận giải bằng AI
                  </span>
                </div>
                <p className={`text-xs mt-1 font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                  Nhập giới tính, giờ sinh, ngày tháng năm sinh để nhận luận giải chi tiết bằng AI về vận mệnh, tử vi trọn đời và phong thủy!
                </p>
              </>
            )}
          </div>
        </div>

        <button
          onClick={openProfileModal}
          className={`px-4 py-2 rounded-xl text-xs font-bold transition-all shadow-sm shrink-0 self-end sm:self-center ${
            profile
              ? "bg-amber-500 hover:bg-amber-600 text-slate-950"
              : "bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-600 hover:to-amber-700 text-slate-950"
          }`}
        >
          {detailedFortune ? "Đổi giờ & ngày sinh" : profile ? `Đổi tuổi (${profile.birthYear})` : "Nhập giờ & ngày sinh ›"}
        </button>
      </div>

      {/* Sub Tabs */}
      <div
        className={`flex flex-wrap items-center gap-1.5 p-1.5 rounded-2xl border backdrop-blur-xl ${
          isDarkTheme ? "bg-slate-900/80 border-white/15" : "bg-white/80 border-slate-200 shadow-sm"
        }`}
      >
        <button
          onClick={() => setActiveSubTab("day")}
          className={`flex-1 min-w-[110px] py-2 px-3 rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-1.5 ${
            activeSubTab === "day"
              ? "bg-amber-500 text-slate-950 shadow-lg shadow-amber-500/25 font-black"
              : isDarkTheme
              ? "text-slate-200 hover:text-white bg-slate-800/80 hover:bg-slate-700/80 border border-white/10 font-bold"
              : "text-slate-700 hover:text-slate-950 bg-slate-100/90 hover:bg-slate-200 border border-slate-200/90 font-bold"
          }`}
        >
          <Calendar className="w-4 h-4" />
          Lịch Ngày Chi Tiết
        </button>
        <button
          onClick={() => setActiveSubTab("month")}
          className={`flex-1 min-w-[110px] py-2 px-3 rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-1.5 ${
            activeSubTab === "month"
              ? "bg-amber-500 text-slate-950 shadow-lg shadow-amber-500/25 font-black"
              : isDarkTheme
              ? "text-slate-200 hover:text-white bg-slate-800/80 hover:bg-slate-700/80 border border-white/10 font-bold"
              : "text-slate-700 hover:text-slate-950 bg-slate-100/90 hover:bg-slate-200 border border-slate-200/90 font-bold"
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
              ? "bg-amber-500 text-slate-950 shadow-lg shadow-amber-500/25 font-black"
              : isDarkTheme
              ? "text-slate-200 hover:text-white bg-slate-800/80 hover:bg-slate-700/80 border border-white/10 font-bold"
              : "text-slate-700 hover:text-slate-950 bg-slate-100/90 hover:bg-slate-200 border border-slate-200/90 font-bold"
          }`}
        >
          <Filter className="w-4 h-4" />
          Tìm Ngày Tốt
        </button>
        <button
          onClick={() => {
            setActiveSubTab("fortune");
            setShowFortunePromptBanner(true);
          }}
          className={`flex-1 min-w-[125px] py-2 px-3 rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-1.5 ${
            activeSubTab === "fortune"
              ? "bg-amber-500 text-slate-950 shadow-lg shadow-amber-500/25 font-black"
              : isDarkTheme
              ? "text-slate-200 hover:text-white bg-slate-800/80 hover:bg-slate-700/80 border border-white/10 font-bold"
              : "text-slate-700 hover:text-slate-950 bg-slate-100/90 hover:bg-slate-200 border border-slate-200/90 font-bold"
          }`}
        >
          <Sparkles className="w-4 h-4 text-amber-500" />
          Bói Tử Vi & Vận Mệnh
        </button>
        <button
          onClick={() => setActiveSubTab("converter")}
          className={`flex-1 min-w-[110px] py-2 px-3 rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-1.5 ${
            activeSubTab === "converter"
              ? "bg-amber-500 text-slate-950 shadow-lg shadow-amber-500/25 font-black"
              : isDarkTheme
              ? "text-slate-200 hover:text-white bg-slate-800/80 hover:bg-slate-700/80 border border-white/10 font-bold"
              : "text-slate-700 hover:text-slate-950 bg-slate-100/90 hover:bg-slate-200 border border-slate-200/90 font-bold"
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
              <div className={`text-sm font-black ${isDarkTheme ? "text-amber-300" : "text-amber-700"}`}>
                {selectedDate.getDay() === 0 ? "Chủ Nhật" : `Thứ ${selectedDate.getDay() + 1}`}
                , ngày {selectedDate.getDate()} tháng {selectedDate.getMonth() + 1} năm{" "}
                {selectedDate.getFullYear()}
              </div>
              <div className={`text-xs mt-0.5 font-bold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                Ngày {lunarDate.day}/{lunarDate.month}{lunarDate.isLeapMonth ? " (Nhuận)" : ""} ÂL • Ngày {lunarDate.dayCanChi}, Tháng {lunarDate.monthCanChi}, Năm {lunarDate.yearCanChi}
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
                <div className={`text-sm mt-1 font-semibold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                  Tháng {lunarDate.monthCanChi} • Năm {lunarDate.yearCanChi}
                </div>
              </div>

              <div className="text-right">
                <div className={`text-xs font-bold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>Đánh giá chung</div>
                <div className={`text-xl font-black ${levelInfo.color} flex items-center gap-1 justify-end mt-0.5`}>
                  {"★".repeat(levelInfo.stars)} {levelInfo.label}
                </div>
                <div className={`text-xs mt-0.5 font-bold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>Điểm ngày: {almanac.score}/100</div>
              </div>
            </div>

            {/* Badges Info */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 my-6">
              <div className={`p-3.5 rounded-2xl border ${isDarkTheme ? "bg-slate-900/80 border-white/15 shadow-sm" : "bg-white/90 border-slate-200 shadow-xs"}`}>
                <div className={`text-[11px] font-bold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>12 Trực</div>
                <div className={`text-sm font-black mt-0.5 ${isDarkTheme ? "text-amber-300" : "text-amber-800"}`}>{almanac.truc.fullName}</div>
              </div>
              <div className={`p-3.5 rounded-2xl border ${isDarkTheme ? "bg-slate-900/80 border-white/15 shadow-sm" : "bg-white/90 border-slate-200 shadow-xs"}`}>
                <div className={`text-[11px] font-bold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>Nhị Thập Bát Tú</div>
                <div className={`text-sm font-black mt-0.5 ${isDarkTheme ? "text-purple-300" : "text-purple-800"}`}>Sao {almanac.mansion.name}</div>
              </div>
              <div className={`p-3.5 rounded-2xl border ${isDarkTheme ? "bg-slate-900/80 border-white/15 shadow-sm" : "bg-white/90 border-slate-200 shadow-xs"}`}>
                <div className={`text-[11px] font-bold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>Ngũ Hành Nạp Âm</div>
                <div className={`text-sm font-black mt-0.5 ${isDarkTheme ? "text-teal-300" : "text-teal-800"}`}>{almanac.napAm}</div>
              </div>
              <div className={`p-3.5 rounded-2xl border ${isDarkTheme ? "bg-slate-900/80 border-white/15 shadow-sm" : "bg-white/90 border-slate-200 shadow-xs"}`}>
                <div className={`text-[11px] font-bold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>Tiết Khí</div>
                <div className={`text-sm font-black mt-0.5 ${isDarkTheme ? "text-emerald-300" : "text-emerald-800"}`}>{lunarDate.solarTerm}</div>
              </div>
            </div>

            {/* Good & Bad Activities */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
              <div className={`p-4 rounded-2xl border ${isDarkTheme ? "bg-emerald-950/20 border-emerald-500/30" : "bg-emerald-50 border-emerald-200"}`}>
                <div className={`flex items-center gap-2 text-xs font-black mb-2.5 ${isDarkTheme ? "text-emerald-300" : "text-emerald-800"}`}>
                  <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                  Việc nên làm hôm nay:
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {almanac.goodActivities.length > 0 ? (
                    almanac.goodActivities.map((act) => (
                      <span
                        key={act}
                        className={`px-2.5 py-1 rounded-lg border text-xs font-bold ${
                          isDarkTheme
                            ? "bg-emerald-500/20 text-emerald-100 border-emerald-400/40"
                            : "bg-white text-emerald-900 border-emerald-300 shadow-xs"
                        }`}
                      >
                        {ACTIVITY_META[act].emoji} {ACTIVITY_META[act].label}
                      </span>
                    ))
                  ) : (
                    <span className={`text-xs font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>Các việc thông thường, không có việc đại cát.</span>
                  )}
                </div>
              </div>

              <div className={`p-4 rounded-2xl border ${isDarkTheme ? "bg-rose-950/20 border-rose-500/30" : "bg-rose-50 border-rose-200"}`}>
                <div className={`flex items-center gap-2 text-xs font-black mb-2.5 ${isDarkTheme ? "text-rose-300" : "text-rose-800"}`}>
                  <XCircle className="w-4 h-4 text-rose-500" />
                  Việc nên kiêng cữ / cẩn trọng:
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {almanac.badActivities.length > 0 ? (
                    almanac.badActivities.map((act) => (
                      <span
                        key={act}
                        className={`px-2.5 py-1 rounded-lg border text-xs font-bold ${
                          isDarkTheme
                            ? "bg-rose-500/20 text-rose-100 border-rose-400/40"
                            : "bg-white text-rose-900 border-rose-300 shadow-xs"
                        }`}
                      >
                        {ACTIVITY_META[act].emoji} {ACTIVITY_META[act].label}
                      </span>
                    ))
                  ) : (
                    <span className={`text-xs font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>Không có việc đại kỵ trong ngày.</span>
                  )}
                </div>
              </div>
            </div>

            {/* Directions & Clash */}
            <div
              className={`mt-5 p-4 rounded-2xl border flex flex-wrap items-center justify-between gap-3 text-xs ${
                isDarkTheme ? "bg-slate-900/80 border-white/15 text-slate-100" : "bg-white/85 border-slate-200 text-slate-800"
              }`}
            >
              <div className={`flex items-center gap-1.5 ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                <Compass className="w-4 h-4 text-amber-500 shrink-0" />
                <span>
                  Hỷ Thần: <strong className={isDarkTheme ? "text-amber-300 font-black" : "text-slate-950 font-black"}>{almanac.hyThan}</strong> • Tài Thần:{" "}
                  <strong className={isDarkTheme ? "text-amber-300 font-black" : "text-slate-950 font-black"}>{almanac.taiThan}</strong>
                  {almanac.hacThan ? ` • Hạc Thần kỵ: ${almanac.hacThan}` : " • Hạc Thần: Trên trời (không cản)"}
                </span>
              </div>
              <div className={isDarkTheme ? "text-slate-200" : "text-slate-700"}>
                Tuổi xung ngày: <strong className="text-rose-400 font-bold">{almanac.clashAgesText}</strong>
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
                    ? "bg-slate-900/70 border-white/15 text-white"
                    : "bg-amber-50/50 border-amber-200/70 text-slate-900"
                }`}
              >
                <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 pb-3 border-b border-black/5 dark:border-white/10">
                  <div className="flex items-center gap-2.5">
                    <div className="w-10 h-10 rounded-2xl bg-amber-500/20 text-amber-500 flex items-center justify-center font-bold text-sm shrink-0">
                      👤
                    </div>
                    <div>
                      <div className="text-sm font-black flex items-center gap-2 flex-wrap">
                        Đối Chiếu Phong Thủy: {detailedFortune ? `${detailedFortune.profile.gender === "male" ? "👨 Nam mạng" : "👩 Nữ mạng"} • ` : ""}{profile.canChi} ({profile.birthYear})
                        <span className={`text-xs font-bold px-2 py-0.5 rounded-full border ${
                          isDarkTheme
                            ? "bg-amber-500/25 text-amber-200 border-amber-400/50"
                            : "bg-amber-500/20 text-amber-800 border-amber-400/30"
                        }`}>
                          Mệnh {elementLabel(profile.element)} • {profile.napAm}
                        </span>
                      </div>
                      <p className={`text-xs mt-0.5 font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                        {profile.yinYangLabel} • {detailedFortune ? (
                          <>Cung Phi Bát Trạch: <strong className={isDarkTheme ? "text-emerald-300 font-bold" : "text-emerald-700 font-bold"}>{detailedFortune.cungPhi}</strong> ({detailedFortune.cungPhiGroup})</>
                        ) : (
                          <>Cung {profile.cungMenh.menhNam} (Nam) / {profile.cungMenh.menhNu} (Nữ)</>
                        )}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2.5">
                    <span
                      className={`text-xs font-black px-3 py-1 rounded-full border ${
                        personalMatch.badgeColor === "rose"
                          ? "bg-rose-500/20 text-rose-300 border-rose-500/40"
                          : personalMatch.badgeColor === "emerald"
                          ? "bg-emerald-500/20 text-emerald-300 border-emerald-500/40"
                          : personalMatch.badgeColor === "orange"
                          ? "bg-orange-500/20 text-orange-300 border-orange-500/40"
                          : "bg-slate-200/70 text-slate-700 dark:bg-white/10 dark:text-slate-200 border-slate-300/40"
                      }`}
                    >
                      {personalMatch.badgeLabel}
                    </span>
                    <span className={`text-xs font-black ${isDarkTheme ? "text-amber-300" : "text-amber-800"}`}>
                      Hợp tuổi: {personalMatch.scorePercent}%
                    </span>
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-3.5">
                  <div className="space-y-2">
                    <div className={`text-xs font-black uppercase tracking-wider ${isDarkTheme ? "text-amber-300" : "text-amber-800"}`}>
                      Tương quan Can Chi & Ngũ Hành:
                    </div>
                    <ul className="space-y-2 text-xs">
                      {personalMatch.notes.map((note, idx) => (
                        <li
                          key={idx}
                          className={`leading-relaxed p-3 rounded-2xl border font-medium flex items-start gap-2.5 ${
                            isDarkTheme
                              ? "bg-slate-900/90 border-white/15 text-slate-100 shadow-xs"
                              : "bg-white/90 border-slate-200 text-slate-800 shadow-xs"
                          }`}
                        >
                          <span className="text-amber-400 font-black shrink-0 text-sm mt-[-1px]">
                            {note.startsWith("▲") ? "▲" : note.startsWith("■") ? "■" : "•"}
                          </span>
                          <span className="flex-1">
                            {note.replace(/^[▲■•]\s*/, "")}
                          </span>
                        </li>
                      ))}
                    </ul>
                  </div>

                  <div className="space-y-2.5">
                    <div className={`text-xs font-black uppercase tracking-wider ${isDarkTheme ? "text-amber-300" : "text-amber-800"}`}>
                      Lời khuyên hành sự cho gia chủ:
                    </div>
                    <div className={`p-4 rounded-2xl border text-xs space-y-2.5 ${
                      isDarkTheme
                        ? "bg-slate-900/90 border-white/15 text-slate-100 shadow-md"
                        : "bg-white/90 border-slate-200 text-slate-800 shadow-xs"
                    }`}>
                      <div className="leading-relaxed">
                        <strong className="text-emerald-400 font-bold block mb-0.5">🌟 Cát sự:</strong>
                        <span className={isDarkTheme ? "text-slate-100" : "text-slate-800"}>{personalMatch.luckyAdvice}</span>
                      </div>
                      <div className="leading-relaxed pt-2 border-t border-black/5 dark:border-white/10">
                        <strong className="text-rose-400 font-bold block mb-0.5">⚠️ Cần phòng:</strong>
                        <span className={isDarkTheme ? "text-slate-100" : "text-slate-800"}>{personalMatch.cautionAdvice}</span>
                      </div>
                      <div className={`leading-relaxed pt-2 border-t border-black/5 dark:border-white/10 font-medium ${
                        isDarkTheme ? "text-amber-200" : "text-amber-900"
                      }`}>
                        🎨 <strong>Màu sắc may mắn:</strong> {profile.luckyColors}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            ) : (
              <div
                className={`mt-5 p-4 rounded-3xl border flex flex-col sm:flex-row items-center justify-between gap-3 text-xs ${
                  isDarkTheme ? "bg-slate-900/80 border-white/15 text-slate-100" : "bg-amber-50/60 border-amber-200/70 text-slate-800"
                }`}
              >
                <div className="flex items-center gap-2.5">
                  <Sparkles className="w-5 h-5 text-amber-500 shrink-0" />
                  <span className={isDarkTheme ? "text-slate-200 font-medium" : "text-slate-700 font-medium"}>
                    Chưa thiết lập hồ sơ sinh để xem đối chiếu phong thủy riêng! Nhập ngày sinh & giới tính để nhận luận giải chuẩn xác.
                  </span>
                </div>
                <button
                  onClick={openProfileModal}
                  className="px-3.5 py-1.5 rounded-xl font-bold bg-amber-500 hover:bg-amber-600 text-slate-950 shrink-0 shadow-xs"
                >
                  Nhập ngày sinh & giới tính
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
                <span className="flex items-center gap-1.5 font-bold text-amber-400">
                  <span className="w-2.5 h-2.5 rounded-full bg-amber-400 ring-2 ring-amber-300/40"></span>
                  Giờ Hoàng Đạo
                </span>
                <span className={`flex items-center gap-1.5 font-bold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                  <span className="w-2.5 h-2.5 rounded-full bg-slate-400 dark:bg-slate-500"></span>
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
                        ? "bg-slate-900/70 border-white/10 text-slate-200 hover:bg-slate-800/80"
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
                            ? "bg-white/10 text-slate-200 border border-white/10"
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
                      <span className={isDarkTheme ? "text-slate-300 font-medium" : "text-slate-600 font-medium"}>
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
                            ? "text-slate-200 font-medium"
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
                          ? "text-amber-100/90 font-medium"
                          : "text-amber-900/80"
                        : isDarkTheme
                          ? "text-slate-200 font-medium"
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
              <span className="flex items-center gap-1 text-slate-600 dark:text-slate-300">
                <span className="w-2 h-2 rounded-full bg-slate-400 dark:bg-slate-400" />
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
                    ? "text-slate-200 bg-white/10 font-black"
                    : "text-slate-700 bg-slate-100 font-black"
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
                        ? "bg-gradient-to-br from-slate-900/80 via-slate-900/50 to-amber-950/20 border-amber-500/25 hover:border-amber-400/60 hover:bg-slate-800/80 text-white shadow-xs"
                        : "bg-gradient-to-br from-amber-50/60 via-white to-yellow-50/40 border-amber-200/80 hover:border-amber-300 hover:bg-amber-50/90 text-slate-900 shadow-2xs"
                      : isDarkTheme
                      ? "bg-slate-900/70 border-white/10 hover:border-white/20 hover:bg-slate-800/80 text-slate-200"
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
                          className="inline-flex items-center gap-1 text-[9px] sm:text-[10px] font-semibold px-1.5 py-0.5 rounded-md bg-slate-100/90 dark:bg-white/10 text-slate-600 dark:text-slate-200 border border-slate-200/80 dark:border-white/15"
                          title="Ngày Hắc Đạo"
                        >
                          <span className="w-1.5 h-1.5 rounded-full bg-slate-400 dark:bg-slate-400 shrink-0" />
                          <span className="hidden md:inline">Hắc Đạo</span>
                          <span className="md:hidden">Hắc</span>
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Middle Row: Biểu tượng Thời tiết dự báo (nếu có dữ liệu) HOẶC Tiết khí / Can chi */}
                  {item.forecast ? (
                    <div className="my-1 px-1.5 py-1 rounded-xl bg-black/[0.03] dark:bg-white/[0.07] border border-black/[0.04] dark:border-white/[0.1] flex items-center justify-between gap-1 w-full">
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
                          <span className="text-[9px] sm:text-[10px] font-semibold text-slate-600 dark:text-slate-300 mt-0.5">
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
                          className="text-[9px] text-slate-500 dark:text-slate-200 hidden lg:inline truncate max-w-[45px] font-medium"
                          title={item.forecast.conditionDescription}
                        >
                          {item.forecast.conditionDescription || "Khô ráo"}
                        </span>
                      )}
                    </div>
                  ) : (
                    <div className="my-1 py-1 px-1 flex items-center justify-between text-[10px] text-slate-700 dark:text-slate-200 w-full min-h-[28px] font-semibold">
                      <span className="truncate max-w-[70px] sm:max-w-[90px]">
                        {item.lunarDate.solarTerm || item.lunarDate.dayCanChi}
                      </span>
                      {item.lunarDate.festival && (
                        <span className="text-rose-500 font-black truncate max-w-[60px] text-[9px]">
                          {item.lunarDate.festival}
                        </span>
                      )}
                    </div>
                  )}

                  {/* Bottom Row: Lunar Date & Can Chi / Tuổi hợp xung */}
                  <div className="flex items-center justify-between w-full pt-1 border-t border-black/[0.04] dark:border-white/[0.08] text-left">
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
                      <span className={`text-[9.5px] font-semibold hidden sm:inline ${isDarkTheme ? "text-slate-300" : "text-slate-500"}`}>ÂL</span>
                    </div>

                    <div className="flex items-center gap-1 min-w-0">
                      {item.personal && item.personal.verdict === ActivityVerdict.BAD ? (
                        <span
                          className="text-[8.5px] sm:text-[9px] font-bold text-rose-500 bg-rose-500/10 px-1 py-0.5 rounded shrink-0"
                          title={`Đối chiếu tuổi ${profile?.canChi} (${item.personal.badgeLabel}): ${item.personal.notes[0]}`}
                        >
                          {item.personal.badgeLabel.startsWith("Xung") ? "Xung" : "Kỵ"}
                        </span>
                      ) : item.personal && item.personal.points >= 2 ? (
                        <span
                          className="text-[8.5px] sm:text-[9px] font-bold text-emerald-600 dark:text-emerald-400 bg-emerald-500/10 px-1 py-0.5 rounded shrink-0"
                          title={`Hợp tuổi ${profile?.canChi}: ${item.personal.notes[0]}`}
                        >
                          Hợp
                        </span>
                      ) : null}
                      <span className="text-[9px] sm:text-[10px] font-bold text-slate-700 dark:text-slate-200 truncate max-w-[55px] sm:max-w-[75px]">
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
              <label className={`text-[11px] font-bold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>Ngày</label>
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
              <label className={`text-[11px] font-bold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>Tháng</label>
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
              <label className={`text-[11px] font-bold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>Năm</label>
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
                      ? "bg-slate-900/70 border-white/15 hover:bg-slate-800/80 text-white"
                      : "bg-slate-50 border-slate-200 hover:bg-slate-100 text-slate-900 shadow-2xs"
                  }`}
                >
                  <div>
                    <div className={`text-sm sm:text-base font-black flex items-center gap-2 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                      {item.date.getDay() === 0 ? "Chủ Nhật" : `Thứ ${item.date.getDay() + 1}`},{" "}
                      ngày {item.date.getDate()}/{item.date.getMonth() + 1}/{item.date.getFullYear()}
                      {item.lunar.day === 1 || item.lunar.day === 15 ? (
                        <span className="text-xs px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-600 dark:text-amber-300 font-bold border border-amber-400/30">
                          {item.lunar.day === 1 ? "🌑 Mùng 1" : "🌕 Ngày Rằm"}
                        </span>
                      ) : null}
                    </div>
                    <div className={`text-xs mt-1 flex items-center gap-2 flex-wrap ${isDarkTheme ? "text-amber-300 font-semibold" : "text-amber-700 font-semibold"}`}>
                      <span>{item.lunar.formattedLunarMedium}</span>
                      <span>•</span>
                      <span>{item.lunar.solarTerm}</span>
                      <span>•</span>
                      <span>{item.almanac.truc.fullName}</span>
                    </div>
                    {item.personalMatch && (
                      <p className={`text-[11px] mt-1 font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
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
                              ? "bg-emerald-500/20 text-emerald-300 border-emerald-500/40"
                              : item.personalMatch.badgeColor === "rose"
                              ? "bg-rose-500/20 text-rose-300 border-rose-500/40"
                              : "bg-slate-200/70 text-slate-700 dark:bg-white/10 dark:text-slate-200 border-slate-300/40"
                          }`}
                        >
                          {item.personalMatch.badgeLabel}
                        </span>
                      )}
                      <span className="text-xs font-bold text-amber-600 dark:text-amber-300 px-2.5 py-0.5 rounded-full bg-amber-500/15 border border-amber-400/30">
                        {item.lunar.isAuspiciousDay ? "Hoàng Đạo" : "Hắc Đạo"}
                      </span>
                    </div>
                    <div className={`text-xs font-bold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                      Điểm ngày: <strong>{item.score}/100</strong>
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className={`text-center py-10 text-xs font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-600"}`}>
                Không tìm thấy ngày tốt đặc biệt cho công việc này trong 45 ngày tới hoặc hãy bấm nút Tìm Kiếm.
              </div>
            )}
          </div>
        </div>
      )}

      {/* 5. BÓI TỬ VI & VẬN MỆNH CHI TIẾT THEO GIỚI TÍNH, GIỜ, NGÀY, THÁNG, NĂM SINH */}
      {activeSubTab === "fortune" && (
        <div className="space-y-6">
          {/* Gợi ý người dùng nhập đầy đủ thông tin (Giới tính + Giờ + Ngày + Tháng + Năm sinh) khi bấm vào tab xem bói */}
          {(!isProfileCompleteWithHour && showFortunePromptBanner) && (
            <div
              className={`p-5 sm:p-6 rounded-3xl border backdrop-blur-xl shadow-lg transition-all ${
                isDarkTheme
                  ? "bg-gradient-to-r from-amber-500/20 via-purple-950/35 to-slate-900 border-amber-400/45 text-white"
                  : "bg-gradient-to-r from-amber-50 via-orange-50/80 to-indigo-50/70 border-amber-300 text-slate-900"
              }`}
            >
              <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
                <div className="flex items-start gap-3.5">
                  <div className="w-11 h-11 rounded-2xl bg-amber-500 text-slate-950 flex items-center justify-center font-black text-xl shrink-0 shadow-md">
                    ✨
                  </div>
                  <div>
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="text-xs font-black uppercase px-2.5 py-0.5 rounded-full bg-amber-500 text-slate-950">
                        Gợi Ý Nhập Đủ Thông Tin Sinh
                      </span>
                      <span className={`text-xs font-bold px-2.5 py-0.5 rounded-full border ${
                        isDarkTheme ? "bg-purple-500/25 text-purple-200 border-purple-400/40" : "bg-purple-100 text-purple-800 border-purple-300"
                      }`}>
                        Luận giải chi tiết bằng AI
                      </span>
                    </div>
                    <h4 className="text-sm sm:text-base font-black mt-1.5">
                      {!userProfile
                        ? "Hãy nhập đầy đủ Giới tính, Giờ sinh, Ngày, Tháng, Năm sinh để luận giải chi tiết tất cả các mặt!"
                        : "Bạn chưa chọn Giờ Sinh! Hãy bổ sung Giờ Sinh để xem đầy đủ cả 4 cột mốc cuộc đời (Năm - Tháng - Ngày - Giờ)."}
                    </h4>
                    <p className={`text-xs mt-1 leading-relaxed font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                      Khi có đủ <strong>Giờ sinh, Ngày, Tháng, Năm sinh và Giới tính</strong>, hệ thống sẽ tính chính xác <strong>ngũ hành đang thiếu, màu sắc may mắn, đường con cái và vận mệnh hậu vận (từ 49 tuổi trở đi)</strong>.
                    </p>
                  </div>
                </div>

                <div className="flex items-center gap-2 shrink-0 self-end md:self-center">
                  <button
                    onClick={openProfileModal}
                    className="px-4 py-2.5 rounded-2xl text-xs font-black bg-amber-500 hover:bg-amber-600 text-slate-950 shadow-md transition-all flex items-center gap-1.5"
                  >
                    <Clock className="w-4 h-4" />
                    Nhập Đầy Đủ Giờ & Ngày Sinh ›
                  </button>
                  {detailedFortune && (
                    <button
                      onClick={() => setShowFortunePromptBanner(false)}
                      className={`p-2 rounded-xl text-xs transition-all ${
                        isDarkTheme ? "hover:bg-white/10 text-slate-300" : "hover:bg-slate-200 text-slate-600"
                      }`}
                      title="Ẩn gợi ý"
                    >
                      <X className="w-4 h-4" />
                    </button>
                  )}
                </div>
              </div>

              {/* Thanh chọn nhanh 12 Canh Giờ Sinh ngay tại chỗ nếu đã có năm/ngày sinh */}
              {detailedFortune && (
                <div className="mt-4 pt-3.5 border-t border-amber-500/20">
                  <span className={`text-[11px] font-bold block mb-2 ${isDarkTheme ? "text-amber-200" : "text-amber-900"}`}>
                    ⚡ Chọn nhanh Giờ Sinh của bạn để xem luận giải theo giờ sinh ngay lập tức:
                  </span>
                  <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 gap-1.5">
                    {BIRTH_HOUR_OPTIONS.map((h) => (
                      <button
                        key={h.chiIndex}
                        onClick={() => handleQuickSelectBirthHour(h.chiIndex)}
                        className={`px-2.5 py-1.5 rounded-xl text-left text-[11px] font-bold border transition-all flex items-center justify-between ${
                          isDarkTheme
                            ? "bg-slate-900/80 hover:bg-amber-500/25 text-slate-100 border-white/15 hover:border-amber-400/50"
                            : "bg-white/90 hover:bg-amber-100 text-slate-800 border-slate-200 hover:border-amber-400"
                        }`}
                      >
                        <span>{h.emoji} Giờ {h.chi}</span>
                        <span className={`text-[10px] ${isDarkTheme ? "text-slate-300" : "text-slate-500"}`}>{h.range.replace(":00", "h").replace(":59", "h")}</span>
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {!detailedFortune ? (
            <div
              className={`p-8 sm:p-12 rounded-3xl backdrop-blur-xl border text-center shadow-xl ${
                isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
              }`}
            >
              <div className="w-16 h-16 rounded-3xl bg-amber-500/20 text-amber-500 flex items-center justify-center mx-auto mb-4 font-bold text-3xl">
                🔮
              </div>
              <h3 className="text-xl sm:text-2xl font-black mb-2">Luận Giải Tử Vi & Vận Mệnh Chi Tiết Bằng AI</h3>
              <p className={`text-xs sm:text-sm max-w-lg mx-auto mb-6 leading-relaxed font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                Khám phá vận mệnh toàn diện theo <strong>Giới tính</strong> và <strong>Giờ, Ngày, Tháng, Năm sinh</strong>:
                <strong> Luận giải chi tiết bằng AI</strong> về tính cách, sự nghiệp, tài lộc, tình duyên, hướng nhà hợp tuổi,
                Tử Vi 3 giai đoạn cuộc đời, Thần Số Học, Cung Hoàng Đạo và Vận Hạn năm 2026.
              </p>
              <button
                onClick={openProfileModal}
                className="px-6 py-3 rounded-2xl font-black text-sm bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-600 hover:to-amber-700 text-slate-950 shadow-lg shadow-amber-500/25 transition-all inline-flex items-center gap-2"
              >
                <User className="w-4 h-4" />
                Nhập Giờ, Ngày, Tháng, Năm Sinh & Giới Tính ›
              </button>

              <div className="mt-8 pt-6 border-t border-black/5 dark:border-white/10">
                <span className={`text-xs font-bold block mb-3 ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>Hoặc chọn nhanh năm sinh phổ biến (sau đó có thể chọn thêm giờ/ngày sinh):</span>
                <div className="flex items-center justify-center gap-2 flex-wrap max-w-xl mx-auto">
                  {[1988, 1989, 1990, 1991, 1992, 1993, 1994, 1995, 1996, 1997, 1998, 1999, 2000, 2001, 2002, 2003].map((y) => {
                    const p = getPersonalProfile(y);
                    return (
                      <button
                        key={y}
                        onClick={() => {
                          setUserProfile({ day: 15, month: 6, year: y, gender: "male", isLunar: false, birthHour: 6 });
                          setBirthYear(y);
                        }}
                        className={`text-xs px-3 py-1.5 rounded-xl font-bold transition-all ${
                          isDarkTheme
                            ? "bg-slate-800/80 hover:bg-slate-700/80 text-slate-200 border border-white/15"
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
              {/* Header Profile Summary Banner */}
              <div
                className={`p-6 sm:p-8 rounded-3xl backdrop-blur-xl border shadow-xl relative overflow-hidden ${
                  isDarkTheme
                    ? "bg-gradient-to-br from-amber-500/15 via-slate-900/90 to-amber-950/25 border-amber-500/30 text-white"
                    : "bg-gradient-to-br from-amber-50 via-white to-orange-50/80 border-amber-200 text-slate-900"
                }`}
              >
                <div className="flex flex-col lg:flex-row items-start lg:items-center justify-between gap-5">
                  <div className="flex items-center gap-4">
                    <div className="w-16 h-16 sm:w-20 sm:h-20 rounded-3xl bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center text-slate-950 font-black text-3xl shadow-lg shrink-0">
                      {detailedFortune.personalProfile.zodiacAnimal.includes("Chuột") ? "🐀" :
                       detailedFortune.personalProfile.zodiacAnimal.includes("Trâu") ? "🐂" :
                       detailedFortune.personalProfile.zodiacAnimal.includes("Hổ") ? "🐅" :
                       detailedFortune.personalProfile.zodiacAnimal.includes("Mèo") ? "🐈" :
                       detailedFortune.personalProfile.zodiacAnimal.includes("Rồng") ? "🐉" :
                       detailedFortune.personalProfile.zodiacAnimal.includes("Rắn") ? "🐍" :
                       detailedFortune.personalProfile.zodiacAnimal.includes("Ngựa") ? "🐎" :
                       detailedFortune.personalProfile.zodiacAnimal.includes("Dê") ? "🐐" :
                       detailedFortune.personalProfile.zodiacAnimal.includes("Khỉ") ? "🐒" :
                       detailedFortune.personalProfile.zodiacAnimal.includes("Gà") ? "🐓" :
                       detailedFortune.personalProfile.zodiacAnimal.includes("Chó") ? "🐕" : "🐖"}
                    </div>
                    <div>
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="text-xs font-black px-3 py-1 rounded-full bg-amber-500 text-slate-950">
                          {detailedFortune.profile.gender === "male" ? "👨 Nam Mạng" : "👩 Nữ Mạng"}
                        </span>
                        <h2 className="text-xl sm:text-2xl font-black">
                          Tuổi {detailedFortune.personalProfile.canChi} ({detailedFortune.personalProfile.birthYear})
                        </h2>
                        <span className={`text-xs font-bold px-2.5 py-0.5 rounded-full border ${
                          isDarkTheme ? "bg-purple-500/25 text-purple-200 border-purple-400/40" : "bg-purple-100 text-purple-800 border-purple-300"
                        }`}>
                          ⏰ {detailedFortune.birthHourFortune.hasBirthHour
                            ? `Giờ ${detailedFortune.birthHourFortune.hourChi} (${detailedFortune.birthHourFortune.hourRange})`
                            : "Chưa chọn giờ sinh"}
                        </span>
                      </div>
                      <p className={`text-xs sm:text-sm mt-1 font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                        Sinh ngày <strong>{detailedFortune.solarDate.day}/{detailedFortune.solarDate.month}/{detailedFortune.solarDate.year}</strong> (Dương lịch) • Âm lịch: ngày <strong>{detailedFortune.lunarDate.day}/{detailedFortune.lunarDate.month}</strong> năm <strong>{detailedFortune.lunarDate.yearCanChi} ({detailedFortune.lunarDate.year})</strong>
                      </p>
                      <div className={`text-[11px] mt-0.5 font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                        4 cột mốc sinh: Năm <strong>{detailedFortune.lunarDate.yearCanChi}</strong> • Tháng <strong>{detailedFortune.lunarDate.monthCanChi}</strong> • Ngày <strong>{detailedFortune.lunarDate.dayCanChi}</strong> • Giờ <strong>{detailedFortune.birthHourFortune.hasBirthHour ? detailedFortune.birthHourFortune.hourCanChi : `${detailedFortune.birthHourFortune.hourCanChi} (tham khảo)`}</strong>
                      </div>
                    </div>
                  </div>

                  <button
                    onClick={openProfileModal}
                    className="px-4 py-2.5 rounded-2xl text-xs font-bold bg-amber-500 hover:bg-amber-600 text-slate-950 transition-all shadow-md shrink-0 self-end lg:self-center flex items-center gap-1.5"
                  >
                    <User className="w-4 h-4" />
                    Sửa giờ & ngày sinh
                  </button>
                </div>

                {/* 4 Badged Cards */}
                <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mt-6 pt-6 border-t border-amber-500/20">
                  <div className={`p-3.5 rounded-2xl border ${isDarkTheme ? "bg-slate-900/80 border-white/15" : "bg-white/80 border-slate-200"}`}>
                    <span className={`text-[11px] font-bold block uppercase ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>Ngũ Hành Nạp Âm</span>
                    <div className="text-sm font-black text-amber-500 dark:text-amber-300 mt-0.5">
                      Mệnh {elementLabel(detailedFortune.personalProfile.element)}
                    </div>
                    <div className={`text-xs mt-0.5 truncate font-semibold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                      {detailedFortune.personalProfile.napAm}
                    </div>
                  </div>

                  <div className={`p-3.5 rounded-2xl border ${isDarkTheme ? "bg-slate-900/80 border-white/15" : "bg-white/80 border-slate-200"}`}>
                    <span className={`text-[11px] font-bold block uppercase ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>Cung Phi ({detailedFortune.profile.gender === "male" ? "Nam" : "Nữ"})</span>
                    <div className="text-sm font-black text-emerald-600 dark:text-emerald-300 mt-0.5">
                      Cung {detailedFortune.cungPhi}
                    </div>
                    <div className={`text-xs mt-0.5 font-semibold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                      {detailedFortune.cungPhiGroup}
                    </div>
                  </div>

                  <div className={`p-3.5 rounded-2xl border ${isDarkTheme ? "bg-slate-900/80 border-white/15" : "bg-white/80 border-slate-200"}`}>
                    <span className={`text-[11px] font-bold block uppercase ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>Cung Hoàng Đạo</span>
                    <div className="text-sm font-black text-indigo-600 dark:text-indigo-300 mt-0.5 flex items-center gap-1">
                      <span>{detailedFortune.westernZodiac.symbol}</span>
                      <span>{detailedFortune.westernZodiac.name}</span>
                    </div>
                    <div className={`text-xs mt-0.5 font-semibold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                      Nguyên tố: {detailedFortune.westernZodiac.element}
                    </div>
                  </div>

                  <div className={`p-3.5 rounded-2xl border ${isDarkTheme ? "bg-slate-900/80 border-white/15" : "bg-white/80 border-slate-200"}`}>
                    <span className={`text-[11px] font-bold block uppercase ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>Thần Số Học</span>
                    <div className="text-sm font-black text-rose-500 dark:text-rose-400 mt-0.5">
                      Số Chủ Đạo {detailedFortune.numerology.lifePathNumber}
                    </div>
                    <div className={`text-xs mt-0.5 truncate font-semibold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                      {detailedFortune.numerology.title.split(" & ")[0]}
                    </div>
                  </div>
                </div>
              </div>

              {/* Fortune Category Selector Tabs */}
              <div
                className={`flex flex-wrap items-center gap-1.5 p-1.5 rounded-2xl border backdrop-blur-xl ${
                  isDarkTheme ? "bg-slate-900/80 border-white/15" : "bg-white/85 border-slate-200 shadow-sm"
                }`}
              >
                {[
                  { id: "deep_ai" as const, label: "Luận Giải Chi Tiết Bằng AI", icon: "✨", tag: "Toàn Diện 8 Mặt" },
                  { id: "battrach" as const, label: "Hướng Hợp Phong Thủy", icon: "🧭", tag: "Theo Giới Tính" },
                  { id: "lifestages" as const, label: "Tử Vi Trọn Đời", icon: "📜", tag: "3 Giai Đoạn & Tình Duyên" },
                  { id: "numerology" as const, label: "Thần Số Học", icon: "🔢", tag: "Số Chủ Đạo" },
                  { id: "zodiac" as const, label: "Cung Hoàng Đạo", icon: "♈", tag: "Phương Tây" },
                  { id: "year2026" as const, label: "Vận Hạn 2026", icon: "🔮", tag: "Năm Bính Ngọ" }
                ].map((cat) => (
                  <button
                    key={cat.id}
                    onClick={() => setFortuneCategory(cat.id)}
                    className={`flex-1 min-w-[140px] py-2.5 px-3 rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-1.5 ${
                      fortuneCategory === cat.id
                        ? "bg-amber-500 text-slate-950 shadow-md shadow-amber-500/20 font-black"
                        : isDarkTheme
                        ? "text-slate-200 hover:text-white bg-slate-800/80 hover:bg-slate-700/80 border border-white/10 font-bold"
                        : "text-slate-700 hover:text-slate-950 bg-slate-100/90 hover:bg-slate-200 border border-slate-200 font-bold"
                    }`}
                  >
                    <span>{cat.icon}</span>
                    <span>{cat.label}</span>
                  </button>
                ))}
              </div>

              {/* 0. LUẬN GIẢI CHI TIẾT BẰNG AI THEO GIỜ, NGÀY, THÁNG, NĂM SINH */}
              {fortuneCategory === "deep_ai" && activeDeepAnalysis && (
                <div className="space-y-6">
                  {/* Khối 1: Bản Mệnh Theo 4 Cột Mốc Sinh (Năm - Tháng - Ngày - Giờ) & Cân Bằng Ngũ Hành */}
                  <div
                    className={`p-6 sm:p-7 rounded-3xl backdrop-blur-xl border shadow-xl ${
                      isDarkTheme ? "bg-slate-900/65 border-white/15 text-white" : "bg-white/90 border-slate-200 text-slate-900"
                    }`}
                  >
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-5 pb-4 border-b border-amber-500/20">
                      <div>
                        <div className="flex items-center gap-2 flex-wrap">
                          <span className="text-xs font-black uppercase px-2.5 py-0.5 rounded-full bg-amber-500 text-slate-950">
                            4 Cột Mốc Sinh (Năm • Tháng • Ngày • Giờ)
                          </span>
                          <span className={`text-xs font-bold px-2.5 py-0.5 rounded-full border ${
                            isDarkTheme ? "bg-emerald-500/20 text-emerald-300 border-emerald-400/40" : "bg-emerald-50 text-emerald-800 border-emerald-300"
                          }`}>
                            Hành ngày sinh: {elementLabel(detailedFortune.fourPillars.nhatChuElement)} ({detailedFortune.fourPillars.nhatChuCan}) • {
                              detailedFortune.fourPillars.nhatChuStrength === "Thân Vượng"
                                ? "Nội lực mạnh mẽ"
                                : detailedFortune.fourPillars.nhatChuStrength === "Thân Nhược"
                                ? "Cần quý nhân trợ lực"
                                : "Khí lực cân bằng"
                            }
                          </span>
                        </div>
                        <h3 className="text-base sm:text-lg font-black mt-1.5">
                          Bản Mệnh Từng Giai Đoạn Theo Giờ, Ngày, Tháng, Năm Sinh
                        </h3>
                      </div>

                      <button
                        onClick={openProfileModal}
                        className={`px-3.5 py-2 rounded-xl text-xs font-bold border transition-all self-start sm:self-center flex items-center gap-1.5 ${
                          isDarkTheme
                            ? "bg-white/10 hover:bg-white/15 text-amber-300 border-amber-400/40"
                            : "bg-amber-50 hover:bg-amber-100 text-amber-900 border-amber-300"
                        }`}
                      >
                        <Clock className="w-3.5 h-3.5" />
                        {detailedFortune.birthHourFortune.hasBirthHour
                          ? `Đổi giờ sinh (Giờ ${detailedFortune.birthHourFortune.hourChi})`
                          : "Chọn Giờ Sinh"}
                      </button>
                    </div>

                    {/* 4 Cột: Năm - Tháng - Ngày - Giờ */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3.5">
                      {[
                        detailedFortune.fourPillars.yearPillar,
                        detailedFortune.fourPillars.monthPillar,
                        detailedFortune.fourPillars.dayPillar,
                        detailedFortune.fourPillars.hourPillar
                      ].map((pillar, idx) => {
                        if (!pillar) {
                          return (
                            <div
                              key="missing-hour"
                              onClick={openProfileModal}
                              className={`p-4 rounded-2xl border border-dashed cursor-pointer transition-all flex flex-col justify-between ${
                                isDarkTheme
                                  ? "bg-amber-500/10 border-amber-400/50 hover:bg-amber-500/15 text-amber-100"
                                  : "bg-amber-50/70 border-amber-400 hover:bg-amber-100/70 text-amber-950"
                              }`}
                            >
                              <div>
                                <div className="text-xs font-black uppercase text-amber-500">
                                  Trụ Giờ (Giờ sinh)
                                </div>
                                <div className={`text-[11px] font-medium mt-0.5 ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                                  Từ 49 tuổi • Con cái & Hậu vận
                                </div>
                                <div className="my-4 py-3 px-3 rounded-xl bg-amber-500/20 border border-amber-400/40 text-center">
                                  <span className="text-sm font-black block">⏰ Chưa chọn Giờ Sinh</span>
                                  <span className="text-[11px] mt-0.5 block">Bấm để chọn khung giờ sinh</span>
                                </div>
                              </div>
                              <p className={`text-[11px] leading-relaxed ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                                Đang tạm tính theo Giờ {detailedFortune.birthHourFortune.hourChi} ({detailedFortune.birthHourFortune.hourCanChi}).
                              </p>
                            </div>
                          );
                        }

                        const isDayPillar = idx === 2;
                        return (
                          <div
                            key={pillar.pillarName}
                            className={`p-4 rounded-2xl border flex flex-col justify-between ${
                              isDayPillar
                                ? isDarkTheme
                                  ? "bg-gradient-to-br from-amber-500/20 via-slate-900 to-slate-900 border-amber-400/60 ring-1 ring-amber-400/30"
                                  : "bg-gradient-to-br from-amber-50 via-white to-orange-50/60 border-amber-400 ring-1 ring-amber-300"
                                : isDarkTheme
                                ? "bg-slate-900/85 border-white/15"
                                : "bg-slate-50/90 border-slate-200"
                            }`}
                          >
                            <div>
                              <div className="flex items-center justify-between gap-1">
                                <span className={`text-xs font-black uppercase ${isDayPillar ? "text-amber-500 dark:text-amber-300" : isDarkTheme ? "text-slate-200" : "text-slate-800"}`}>
                                  {pillar.pillarName}
                                </span>
                                {isDayPillar && (
                                  <span className="text-[10px] font-black px-2 py-0.5 rounded-md bg-amber-500 text-slate-950">
                                    Trụ Chính
                                  </span>
                                )}
                              </div>
                              <div className={`text-[11px] font-medium mt-0.5 ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                                {pillar.subTitle}
                              </div>

                              <div className="my-3 py-2.5 px-3 rounded-xl bg-black/[0.04] dark:bg-white/[0.06] border border-black/5 dark:border-white/10">
                                <div className="text-lg font-black text-amber-600 dark:text-amber-300">
                                  {pillar.canChi}
                                </div>
                                <div className="flex items-center gap-2 mt-1 text-[11px] font-bold flex-wrap">
                                  <span>Can {pillar.can} ({elementLabel(pillar.canElement)})</span>
                                  <span>•</span>
                                  <span>Chi {pillar.chi} ({elementLabel(pillar.chiElement)})</span>
                                </div>
                                <div className={`text-[11px] mt-1 font-semibold ${isDarkTheme ? "text-emerald-300" : "text-emerald-700"}`}>
                                  Mệnh: {pillar.napAm} (Hành {elementLabel(pillar.napAmElement)})
                                </div>
                              </div>
                            </div>

                            <p className={`text-[11px] leading-relaxed ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                              {pillar.meaning}
                            </p>
                          </div>
                        );
                      })}
                    </div>

                    {/* Biểu đồ Cân Bằng Ngũ Hành & Hành Nên Bổ Sung */}
                    <div className="mt-5 pt-5 border-t border-black/5 dark:border-white/10 grid grid-cols-1 lg:grid-cols-12 gap-5 items-center">
                      <div className="lg:col-span-7 space-y-2.5">
                        <div className="text-xs font-extrabold uppercase tracking-wider text-amber-500">
                          Số Lượng Ngũ Hành Trong Giờ, Ngày, Tháng, Năm Sinh
                        </div>
                        <div className="grid grid-cols-5 gap-2">
                          {([FiveElement.KIM, FiveElement.MOC, FiveElement.THUY, FiveElement.HOA, FiveElement.THO] as const).map((el) => {
                            const count = detailedFortune.fourPillars.elementCounts[el] || 0;
                            const isDungThan = detailedFortune.fourPillars.hyDungThan.includes(el);
                            return (
                              <div
                                key={el}
                                className={`p-2.5 rounded-2xl border text-center ${
                                  isDungThan
                                    ? isDarkTheme
                                      ? "bg-emerald-500/20 border-emerald-400/50 text-white"
                                      : "bg-emerald-50 border-emerald-300 text-slate-900"
                                    : count === 0
                                    ? isDarkTheme
                                      ? "bg-rose-500/15 border-rose-400/40 text-rose-200"
                                    : "bg-rose-50 border-rose-200 text-rose-900"
                                    : isDarkTheme
                                    ? "bg-slate-900/80 border-white/15 text-slate-100"
                                    : "bg-slate-50 border-slate-200 text-slate-800"
                                }`}
                              >
                                <div className="text-base">{FIVE_ELEMENT_META[el].emoji}</div>
                                <div className="text-xs font-black mt-0.5">{FIVE_ELEMENT_META[el].label}</div>
                                <div className="text-sm font-black mt-0.5">{count}</div>
                                <div className="text-[10px] font-bold mt-0.5">
                                  {isDungThan ? "🌟 Nên bổ sung" : count === 0 ? "Đang thiếu" : "Đầy đủ"}
                                </div>
                              </div>
                            );
                          })}
                        </div>
                      </div>

                      <div
                        className={`lg:col-span-5 p-4 rounded-2xl border text-xs space-y-1.5 ${
                          isDarkTheme ? "bg-emerald-950/25 border-emerald-500/35 text-slate-100" : "bg-emerald-50/80 border-emerald-200 text-slate-800"
                        }`}
                      >
                        <div className="font-black text-sm text-emerald-500 dark:text-emerald-300 flex items-center gap-1.5">
                          <Sparkles className="w-4 h-4" />
                          Ngũ Hành Mang Lại May Mắn: Hành {detailedFortune.fourPillars.hyDungThan.map(elementLabel).join(" & ")}
                        </div>
                        <p className="leading-relaxed">
                          {detailedFortune.fourPillars.dungThanAdvice}
                        </p>
                      </div>
                    </div>
                  </div>

                  {/* Khối 2: Luận Giải Chi Tiết Theo Giờ Sinh */}
                  <div
                    className={`p-6 rounded-3xl backdrop-blur-xl border shadow-md ${
                      isDarkTheme ? "bg-slate-900/60 border-white/15 text-white" : "bg-white/90 border-slate-200 text-slate-900"
                    }`}
                  >
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-3">
                      <h3 className="text-sm sm:text-base font-black flex items-center gap-2 text-amber-500">
                        <Clock className="w-5 h-5 shrink-0" />
                        <span>
                          Ý Nghĩa Giờ Sinh ({detailedFortune.birthHourFortune.hasBirthHour
                            ? `Giờ ${detailedFortune.birthHourFortune.hourChi} ${detailedFortune.birthHourFortune.hourRange}`
                            : `Tạm tính Giờ ${detailedFortune.birthHourFortune.hourChi}`}) — {detailedFortune.birthHourFortune.destinyTitle}
                        </span>
                      </h3>
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className={`text-xs font-bold px-2.5 py-0.5 rounded-full border ${
                          detailedFortune.birthHourFortune.isHoangDaoHour
                            ? "bg-amber-500/20 text-amber-300 border-amber-400/40"
                            : isDarkTheme ? "bg-white/10 text-slate-200 border-white/15" : "bg-slate-100 text-slate-700 border-slate-200"
                        }`}>
                          {detailedFortune.birthHourFortune.isHoangDaoHour ? "🌟 Sinh vào Giờ Hoàng Đạo (Thuận lợi)" : "💪 Sinh vào Giờ Tự Lập (Bản lĩnh kiên cường)"}
                        </span>
                      </div>
                    </div>

                    <p className={`text-xs leading-relaxed mb-4 font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                      {detailedFortune.birthHourFortune.coreOverview}
                    </p>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-3.5 text-xs">
                      <div className={`p-3.5 rounded-2xl border ${isDarkTheme ? "bg-slate-900/80 border-white/15 text-slate-200" : "bg-slate-50 border-slate-200 text-slate-700"}`}>
                        <strong className="text-amber-500 dark:text-amber-300 block mb-1">✨ Tính cách theo giờ sinh:</strong>
                        {detailedFortune.birthHourFortune.personalityTrait}
                      </div>
                      <div className={`p-3.5 rounded-2xl border ${isDarkTheme ? "bg-slate-900/80 border-white/15 text-slate-200" : "bg-slate-50 border-slate-200 text-slate-700"}`}>
                        <strong className="text-emerald-500 dark:text-emerald-300 block mb-1">💼 Công việc & Tiền bạc:</strong>
                        {detailedFortune.birthHourFortune.careerWealth}
                      </div>
                      <div className={`p-3.5 rounded-2xl border ${isDarkTheme ? "bg-slate-900/80 border-white/15 text-slate-200" : "bg-slate-50 border-slate-200 text-slate-700"}`}>
                        <strong className="text-indigo-500 dark:text-indigo-300 block mb-1">👨‍👩‍👧‍👦 Con cái & Tuổi già:</strong>
                        {detailedFortune.birthHourFortune.childrenAndLateLife}
                      </div>
                    </div>
                  </div>

                  {/* Khối 3: Luận Giải Chi Tiết Bằng AI (8 Phương Diện Cuộc Đời) */}
                  <div
                    className={`p-6 sm:p-8 rounded-3xl backdrop-blur-xl border shadow-xl ${
                      isDarkTheme
                        ? "bg-gradient-to-br from-indigo-950/35 via-slate-900/90 to-purple-950/30 border-indigo-500/35 text-white"
                        : "bg-gradient-to-br from-indigo-50/70 via-white to-amber-50/60 border-indigo-200 text-slate-900"
                    }`}
                  >
                    <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 pb-5 border-b border-indigo-500/20">
                      <div>
                        <div className="flex items-center gap-2 flex-wrap">
                          <span className="inline-flex items-center gap-1.5 text-xs font-black px-3 py-1 rounded-full bg-gradient-to-r from-amber-500 to-orange-500 text-slate-950 shadow-xs">
                            <BrainCircuit className="w-3.5 h-3.5" />
                            Luận Giải Chi Tiết Bằng AI
                          </span>
                          <span className="text-xs font-black px-2.5 py-0.5 rounded-full bg-amber-500/20 text-amber-500 dark:text-amber-300 border border-amber-400/40">
                            Điểm Hòa Hợp Bản Mệnh: {activeDeepAnalysis.overallScore}/100
                          </span>
                        </div>
                        <h3 className="text-lg sm:text-xl font-black mt-2">
                          {activeDeepAnalysis.overallHeadline}
                        </h3>
                      </div>

                      <button
                        onClick={() => handleTriggerGeminiDeepAnalysis(true)}
                        disabled={isAnalyzingAi}
                        className="px-4 py-2.5 rounded-2xl text-xs font-black bg-gradient-to-r from-amber-500 to-orange-500 hover:from-amber-600 hover:to-orange-600 disabled:opacity-60 text-slate-950 shadow-md transition-all flex items-center gap-2 shrink-0 self-start lg:self-center"
                      >
                        <RefreshCw className={`w-4 h-4 ${isAnalyzingAi ? "animate-spin" : ""}`} />
                        <span>
                          {isAnalyzingAi
                            ? "AI đang phân tích chi tiết..."
                            : "Luận Giải Chi Tiết Lại Bằng AI"}
                        </span>
                      </button>
                    </div>

                    {/* Executive Summary */}
                    <div
                      className={`mt-5 p-4 rounded-2xl border text-xs sm:text-sm leading-relaxed font-medium ${
                        isDarkTheme ? "bg-slate-900/80 border-white/15 text-slate-100" : "bg-white/90 border-slate-200 text-slate-800"
                      }`}
                    >
                      <strong className="text-amber-500 dark:text-amber-300 block mb-1">
                        📌 Tóm Tắt Tổng Quan Bản Mệnh:
                      </strong>
                      {activeDeepAnalysis.executiveSummary}
                    </div>

                    {/* Grid 8 Phương Diện Cuộc Đời */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-6">
                      {activeDeepAnalysis.aspects.map((aspect) => (
                        <div
                          key={aspect.id}
                          className={`p-5 rounded-2xl border flex flex-col justify-between space-y-3 transition-all ${
                            isDarkTheme
                              ? "bg-slate-900/85 border-white/15 hover:border-amber-400/40 text-slate-100"
                              : "bg-white/95 border-slate-200 hover:border-amber-300 text-slate-900 shadow-xs"
                          }`}
                        >
                          <div className="space-y-2.5">
                            <div className="flex items-start justify-between gap-2 pb-2 border-b border-black/5 dark:border-white/10">
                              <h4 className="text-sm font-black flex items-center gap-2 text-amber-500 dark:text-amber-300">
                                <span className="text-base">{aspect.icon}</span>
                                <span>{aspect.title}</span>
                              </h4>
                              <span
                                className={`text-[10px] font-bold px-2 py-0.5 rounded-full shrink-0 border ${
                                  isDarkTheme
                                    ? "bg-white/10 text-slate-200 border-white/15"
                                    : "bg-slate-100 text-slate-700 border-slate-200"
                                }`}
                              >
                                {aspect.badge}
                              </span>
                            </div>

                            <p className={`text-xs leading-relaxed font-semibold ${isDarkTheme ? "text-slate-100" : "text-slate-800"}`}>
                              {aspect.summary}
                            </p>

                            <ul className="space-y-1.5 text-xs">
                              {aspect.details.map((line, i) => (
                                <li
                                  key={i}
                                  className={`leading-relaxed ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}
                                >
                                  {line}
                                </li>
                              ))}
                            </ul>
                          </div>

                          <div
                            className={`p-3 rounded-xl border text-xs font-bold ${
                              isDarkTheme
                                ? "bg-emerald-500/15 border-emerald-500/30 text-emerald-300"
                                : "bg-emerald-50 border-emerald-200 text-emerald-800"
                            }`}
                          >
                            💡 Bí quyết cải vận: {aspect.actionableAdvice}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}

              {/* 1. BÁT TRẠCH PHONG THỦY THEO GIỚI TÍNH */}
              {fortuneCategory === "battrach" && (
                <div className="space-y-6">
                  {/* Cung Phi Info Box */}
                  <div
                    className={`p-6 rounded-3xl backdrop-blur-xl border shadow-md ${
                      isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
                    }`}
                  >
                    <div className="flex items-center gap-3 mb-2">
                      <Compass className="w-5 h-5 text-emerald-500" />
                      <h3 className="text-base font-black">
                        Bát Trạch Cung Phi Cho {detailedFortune.profile.gender === "male" ? "Nam Mạng" : "Nữ Mạng"} Tuổi {detailedFortune.personalProfile.canChi} ({detailedFortune.personalProfile.birthYear})
                      </h3>
                    </div>
                    <p className={`text-xs leading-relaxed font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                      Theo Dịch học Bát Trạch phong thủy, cùng sinh năm Âm lịch {detailedFortune.personalProfile.birthYear} ({detailedFortune.personalProfile.canChi}) nhưng <strong>Nam mạng</strong> và <strong>Nữ mạng</strong> mang Cung Phi và Cung Mệnh hoàn toàn khác biệt.
                      Bạn là <strong>{detailedFortune.profile.gender === "male" ? "Nam mạng" : "Nữ mạng"}</strong> nên thuộc cung <strong>{detailedFortune.cungPhi}</strong>, nhóm <strong>{detailedFortune.cungPhiGroup}</strong>.
                      Dưới đây là 4 hướng Đại Cát mang lại sinh khí tài lộc và 4 hướng Đại Hung cần tránh:
                    </p>
                  </div>

                  {/* 8 Directions Grid */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {/* 4 Hướng Cát */}
                    <div
                      className={`p-6 rounded-3xl backdrop-blur-xl border shadow-md ${
                        isDarkTheme ? "bg-emerald-950/20 border-emerald-500/30 text-white" : "bg-emerald-50/80 border-emerald-200 text-slate-900"
                      }`}
                    >
                      <h4 className="text-sm font-extrabold flex items-center gap-2 mb-4 text-emerald-600 dark:text-emerald-400">
                        <CheckCircle2 className="w-4 h-4" />
                        4 Hướng Đại Cát (Vượng Khí & Chiêu Tài Lộc)
                      </h4>

                      <div className="space-y-3.5">
                        {detailedFortune.batTrachDirections.filter(d => d.isGood).map((dir, idx) => (
                          <div
                            key={idx}
                            className={`p-3.5 rounded-2xl border text-xs space-y-1 ${
                              isDarkTheme
                                ? "bg-slate-900/80 border-white/15 text-slate-100 shadow-sm"
                                : "bg-white/90 border-slate-200 text-slate-800 shadow-xs"
                            }`}
                          >
                            <div className="flex items-center justify-between">
                              <span className="font-black text-emerald-600 dark:text-emerald-300 text-sm">
                                {dir.type}: {dir.direction}
                              </span>
                              <span className={`text-[11px] font-bold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                                {dir.starName}
                              </span>
                            </div>
                            <p className={`leading-relaxed ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                              {dir.meaning}
                            </p>
                            <div className="text-[11px] text-emerald-600 dark:text-emerald-400 pt-1 font-bold">
                              💡 Ứng dụng: {dir.advice}
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>

                    {/* 4 Hướng Hung */}
                    <div
                      className={`p-6 rounded-3xl backdrop-blur-xl border shadow-md ${
                        isDarkTheme ? "bg-rose-950/20 border-rose-500/30 text-white" : "bg-rose-50/80 border-rose-200 text-slate-900"
                      }`}
                    >
                      <h4 className="text-sm font-extrabold flex items-center gap-2 mb-4 text-rose-600 dark:text-rose-400">
                        <XCircle className="w-4 h-4" />
                        4 Hướng Đại Hung (Nên Tránh & Hóa Giải)
                      </h4>

                      <div className="space-y-3.5">
                        {detailedFortune.batTrachDirections.filter(d => !d.isGood).map((dir, idx) => (
                          <div
                            key={idx}
                            className={`p-3.5 rounded-2xl border text-xs space-y-1 ${
                              isDarkTheme
                                ? "bg-slate-900/80 border-white/15 text-slate-100 shadow-sm"
                                : "bg-white/90 border-slate-200 text-slate-800 shadow-xs"
                            }`}
                          >
                            <div className="flex items-center justify-between">
                              <span className="font-black text-rose-600 dark:text-rose-300 text-sm">
                                {dir.type}: {dir.direction}
                              </span>
                              <span className={`text-[11px] font-bold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                                {dir.starName}
                              </span>
                            </div>
                            <p className={`leading-relaxed ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                              {dir.meaning}
                            </p>
                            <div className="text-[11px] text-rose-500 dark:text-rose-400 pt-1 font-bold">
                              ⚠️ Hóa giải: {dir.advice}
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>

                  {/* Ứng dụng thực chiến phong thủy */}
                  <div
                    className={`p-6 rounded-3xl backdrop-blur-xl border shadow-md ${
                      isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
                    }`}
                  >
                    <h4 className="text-sm font-extrabold flex items-center gap-2 mb-3 text-amber-500">
                      <Sparkles className="w-4 h-4" />
                      Gợi Ý Bố Trí Không Gian Sống Thực Chiến Cho {detailedFortune.profile.gender === "male" ? "Nam" : "Nữ"} Mạng
                    </h4>
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 text-xs">
                      <div className={`p-3 rounded-2xl border ${isDarkTheme ? "bg-slate-900/80 border-white/15 text-slate-200" : "bg-white/90 border-slate-200 text-slate-700"}`}>
                        <strong className="text-slate-900 dark:text-white block mb-1">🖥️ Bàn làm việc:</strong>
                        Quay mặt về hướng <strong>{detailedFortune.batTrachDirections[0].direction} (Sinh Khí)</strong> để thu hút năng lượng thăng tiến và ký kết hợp đồng thuận lợi.
                      </div>
                      <div className={`p-3 rounded-2xl border ${isDarkTheme ? "bg-slate-900/80 border-white/15 text-slate-200" : "bg-white/90 border-slate-200 text-slate-700"}`}>
                        <strong className="text-slate-900 dark:text-white block mb-1">🛏️ Đầu giường ngủ:</strong>
                        Kê tựa về hướng <strong>{detailedFortune.batTrachDirections[1].direction} (Thiên Y)</strong> hoặc <strong>{detailedFortune.batTrachDirections[2].direction} (Diên Niên)</strong> giúp ngủ ngon, phục hồi sinh lực.
                      </div>
                      <div className={`p-3 rounded-2xl border ${isDarkTheme ? "bg-slate-900/80 border-white/15 text-slate-200" : "bg-white/90 border-slate-200 text-slate-700"}`}>
                        <strong className="text-slate-900 dark:text-white block mb-1">🚪 Cửa chính & Ban thờ:</strong>
                        Mở cửa hoặc đặt ban thờ hướng <strong>{detailedFortune.batTrachDirections[3].direction} (Phục Vị)</strong> hoặc Sinh Khí để gia đạo luôn an yên, thi cử đỗ đạt.
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* 2. TỬ VI TRỌN ĐỜI: 3 THỜI KỲ & TÌNH DUYÊN */}
              {fortuneCategory === "lifestages" && (
                <div className="space-y-6">
                  {/* Tính cách theo Giới tính */}
                  <div
                    className={`p-6 rounded-3xl backdrop-blur-xl border shadow-md ${
                      isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
                    }`}
                  >
                    <h3 className="text-sm font-extrabold flex items-center gap-2 mb-3 text-amber-500">
                      <Award className="w-4 h-4" />
                      Tướng Tinh & Đặc Trưng Tính Cách: {detailedFortune.personalityByGender.title}
                    </h3>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
                      <div className={`p-3.5 rounded-2xl border ${isDarkTheme ? "bg-slate-900/80 border-white/15 text-slate-200" : "bg-white/90 border-slate-200 text-slate-700 shadow-xs"}`}>
                        <strong className="text-slate-900 dark:text-white block mb-1">Bản lĩnh cốt lõi:</strong>
                        {detailedFortune.personalityByGender.coreTraits}
                      </div>
                      <div className={`p-3.5 rounded-2xl border ${isDarkTheme ? "bg-slate-900/80 border-white/15 text-slate-200" : "bg-white/90 border-slate-200 text-slate-700 shadow-xs"}`}>
                        <strong className="text-slate-900 dark:text-white block mb-1">Thế mạnh vượt trội:</strong>
                        {detailedFortune.personalityByGender.strengths}
                      </div>
                      <div className={`p-3.5 rounded-2xl border ${isDarkTheme ? "bg-slate-900/80 border-white/15 text-slate-200" : "bg-white/90 border-slate-200 text-slate-700 shadow-xs"}`}>
                        <strong className="text-slate-900 dark:text-white block mb-1">Phong cách giao thiệp:</strong>
                        {detailedFortune.personalityByGender.socialStyle}
                      </div>
                    </div>
                  </div>

                  {/* 3 Thời kỳ cuộc đời */}
                  <div
                    className={`p-6 rounded-3xl backdrop-blur-xl border shadow-md ${
                      isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
                    }`}
                  >
                    <h3 className="text-sm font-extrabold flex items-center gap-2 mb-4 text-indigo-500">
                      <TrendingUp className="w-4 h-4" />
                      Vận Trình Cuộc Đời Qua 3 Giai Đoạn (Tiền Vận — Trung Vận — Hậu Vận)
                    </h3>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                      {detailedFortune.lifeStages.map((stage, idx) => (
                        <div
                          key={idx}
                          className={`p-4 rounded-2xl border text-xs space-y-2.5 ${
                            idx === 1
                              ? isDarkTheme
                                ? "bg-amber-500/15 border-amber-500/35 text-white"
                                : "bg-amber-50/70 border-amber-200"
                              : isDarkTheme
                                ? "bg-slate-900/80 border-white/15 text-slate-100 shadow-sm"
                                : "bg-white/90 border-slate-200 text-slate-800 shadow-xs"
                          }`}
                        >
                          <div className="flex items-center justify-between pb-1.5 border-b border-black/5 dark:border-white/10">
                            <span className="font-black text-sm text-amber-600 dark:text-amber-400">
                              {stage.stage} ({stage.ageRange})
                            </span>
                            <span className={`text-[11px] font-bold ${isDarkTheme ? "text-slate-200" : "text-slate-500"}`}>
                              {idx === 0 ? "🌅 Khởi sắc" : idx === 1 ? "☀️ Đỉnh cao" : "🌕 Viên mãn"}
                            </span>
                          </div>

                          <div className="font-bold text-slate-900 dark:text-white">
                            {stage.title}
                          </div>

                          <p className={`leading-relaxed ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                            {stage.overview}
                          </p>

                          <div className="pt-1.5 border-t border-black/5 dark:border-white/10 space-y-1 text-[11px]">
                            <div>
                              <strong className="text-emerald-600 dark:text-emerald-400">Công danh & Tiền tài:</strong>{" "}
                              <span className={isDarkTheme ? "text-slate-200" : "text-slate-700"}>{stage.careerFinance}</span>
                            </div>
                            <div>
                              <strong className="text-rose-500 dark:text-rose-400">Gia đạo & Tình duyên:</strong>{" "}
                              <span className={isDarkTheme ? "text-slate-200" : "text-slate-700"}>{stage.familyLove}</span>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Tình duyên & Hôn nhân */}
                  <div
                    className={`p-6 rounded-3xl backdrop-blur-xl border shadow-md ${
                      isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
                    }`}
                  >
                    <h3 className="text-sm font-extrabold flex items-center gap-2 mb-3 text-rose-500">
                      <Heart className="w-4 h-4" />
                      Hôn Nhân & Tình Duyên Của {detailedFortune.profile.gender === "male" ? "Nam" : "Nữ"} Mạng Tuổi {detailedFortune.personalProfile.canChi}
                    </h3>
                    <p className={`text-xs leading-relaxed mb-4 font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                      {detailedFortune.loveMarriage.overview}
                    </p>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
                      <div className="p-3.5 rounded-2xl bg-emerald-500/10 border border-emerald-500/20">
                        <strong className="text-emerald-600 dark:text-emerald-400 block mb-1">
                          💍 Tuổi Đại Hợp Kết Hôn & Làm Ăn:
                        </strong>
                        <p className={isDarkTheme ? "text-slate-200" : "text-slate-700"}>
                          Tuổi <strong>{detailedFortune.loveMarriage.bestAges.join(", ")}</strong> (Tam Hợp & Lục Hợp mang lại hòa hợp gia đạo, phú quý sinh tài).
                        </p>
                      </div>

                      <div className="p-3.5 rounded-2xl bg-rose-500/10 border border-rose-500/20">
                        <strong className="text-rose-500 block mb-1">
                          ⚠️ Tuổi Cần Nhường Nhịn Trong Hôn Nhân:
                        </strong>
                        <p className={isDarkTheme ? "text-slate-200" : "text-slate-700"}>
                          Tuổi <strong>{detailedFortune.loveMarriage.challengingAges.join(", ")}</strong> (Tứ Hành Xung, nên học cách lắng nghe và bao dung để hóa giải xung khắc).
                        </p>
                      </div>
                    </div>

                    <div className={`mt-3 p-3 rounded-2xl border text-xs ${isDarkTheme ? "bg-slate-900/80 border-white/15 text-slate-200" : "bg-white/90 border-slate-200 text-slate-700"}`}>
                      💡 <strong>Lời khuyên giữ lửa hôn nhân:</strong> {detailedFortune.loveMarriage.advice}
                    </div>
                  </div>
                </div>
              )}

              {/* 3. THẦN SỐ HỌC PYTHAGORAS */}
              {fortuneCategory === "numerology" && (
                <div className="space-y-6">
                  {/* Hero Life Path Card */}
                  <div
                    className={`p-6 sm:p-8 rounded-3xl backdrop-blur-xl border shadow-xl relative overflow-hidden ${
                      isDarkTheme
                        ? "bg-gradient-to-br from-indigo-950/40 via-slate-900/80 to-purple-950/30 border-indigo-500/30 text-white"
                        : "bg-gradient-to-br from-indigo-50 via-white to-purple-50/70 border-indigo-200 text-slate-900"
                    }`}
                  >
                    <div className="flex flex-col sm:flex-row items-start sm:items-center gap-5">
                      <div className="w-16 h-16 sm:w-20 sm:h-20 rounded-3xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white font-black text-3xl shadow-lg shadow-indigo-500/25 shrink-0">
                        {detailedFortune.numerology.lifePathNumber}
                      </div>
                      <div>
                        <div className="flex items-center gap-2 flex-wrap">
                          <span className="text-xs uppercase font-extrabold px-3 py-1 rounded-full bg-indigo-500/20 text-indigo-600 dark:text-indigo-400 border border-indigo-400/30">
                            Con Số Chủ Đạo Pythagoras
                          </span>
                          <h3 className="text-xl sm:text-2xl font-black">
                            Số {detailedFortune.numerology.lifePathNumber}: {detailedFortune.numerology.title}
                          </h3>
                        </div>
                        <div className="flex items-center gap-2 flex-wrap mt-2">
                          {detailedFortune.numerology.keywords.map((kw, i) => (
                            <span key={i} className="text-xs px-2.5 py-0.5 rounded-lg bg-black/5 dark:bg-white/10 font-bold">
                              #{kw}
                            </span>
                          ))}
                        </div>
                      </div>
                    </div>

                    <p className={`text-xs sm:text-sm mt-5 leading-relaxed pt-5 border-t border-indigo-500/20 font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                      {detailedFortune.numerology.description}
                    </p>
                  </div>

                  {/* Strengths & Challenges */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div
                      className={`p-6 rounded-3xl backdrop-blur-xl border shadow-md ${
                        isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
                      }`}
                    >
                      <h4 className="text-sm font-extrabold flex items-center gap-2 mb-3 text-emerald-500">
                        <CheckCircle2 className="w-4 h-4" />
                        Điểm Mạnh Bẩm Sinh & Năng Khiếu
                      </h4>
                      <ul className="space-y-2 text-xs">
                        {detailedFortune.numerology.strengths.map((s, idx) => (
                          <li key={idx} className="flex items-start gap-2">
                            <span className="text-emerald-500 font-bold mt-0.5">✓</span>
                            <span className={`leading-relaxed ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>{s}</span>
                          </li>
                        ))}
                      </ul>
                    </div>

                    <div
                      className={`p-6 rounded-3xl backdrop-blur-xl border shadow-md ${
                        isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
                      }`}
                    >
                      <h4 className="text-sm font-extrabold flex items-center gap-2 mb-3 text-rose-500">
                        <Shield className="w-4 h-4" />
                        Bài Học Cuộc Đời & Điểm Cần Hoàn Thiện
                      </h4>
                      <ul className="space-y-2 text-xs">
                        {detailedFortune.numerology.challenges.map((c, idx) => (
                          <li key={idx} className="flex items-start gap-2">
                            <span className="text-rose-500 font-bold mt-0.5">!</span>
                            <span className={`leading-relaxed ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>{c}</span>
                          </li>
                        ))}
                      </ul>
                    </div>
                  </div>

                  {/* Nghề nghiệp phù hợp */}
                  <div
                    className={`p-6 rounded-3xl backdrop-blur-xl border shadow-md ${
                      isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
                    }`}
                  >
                    <h4 className="text-sm font-extrabold flex items-center gap-2 mb-3 text-amber-500">
                      <Zap className="w-4 h-4" />
                      Lĩnh Vực Nghề Nghiệp Thăng Hoa Nhất
                    </h4>
                    <div className="flex items-center gap-2 flex-wrap">
                      {detailedFortune.numerology.idealCareers.map((c, i) => (
                        <span
                          key={i}
                          className={`px-3.5 py-1.5 rounded-xl text-xs font-bold border ${
                            isDarkTheme
                              ? "bg-amber-500/25 text-amber-200 border-amber-400/40"
                              : "bg-amber-500/15 text-amber-800 border-amber-400/30"
                          }`}
                        >
                          💼 {c}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>
              )}

              {/* 4. CUNG HOÀNG ĐẠO PHƯƠNG TÂY KẾT HỢP ĐÔNG TÂY */}
              {fortuneCategory === "zodiac" && (
                <div className="space-y-6">
                  {/* Hero Zodiac Card */}
                  <div
                    className={`p-6 sm:p-8 rounded-3xl backdrop-blur-xl border shadow-xl relative overflow-hidden ${
                      isDarkTheme
                        ? "bg-gradient-to-br from-amber-950/30 via-slate-900/80 to-slate-950 border-amber-500/30 text-white"
                        : "bg-gradient-to-br from-amber-50 via-white to-orange-50 border-amber-200 text-slate-900"
                    }`}
                  >
                    <div className="flex flex-col sm:flex-row items-start sm:items-center gap-5">
                      <div className="w-16 h-16 sm:w-20 sm:h-20 rounded-3xl bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center text-slate-950 font-black text-4xl shadow-lg shrink-0">
                        {detailedFortune.westernZodiac.symbol}
                      </div>
                      <div>
                        <div className="flex items-center gap-2 flex-wrap">
                          <span className="text-xs uppercase font-extrabold px-3 py-1 rounded-full bg-amber-500/20 text-amber-600 dark:text-amber-400 border border-amber-400/30">
                            Cung Hoàng Đạo Phương Tây
                          </span>
                          <h3 className="text-xl sm:text-2xl font-black">
                            {detailedFortune.westernZodiac.name} ({detailedFortune.westernZodiac.latinName})
                          </h3>
                        </div>
                        <div className={`text-xs mt-1 flex items-center gap-3 flex-wrap font-semibold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                          <span>📅 {detailedFortune.westernZodiac.dateRange}</span>
                          <span>•</span>
                          <span>Nguyên tố: <strong className={isDarkTheme ? "text-indigo-300 font-bold" : "text-indigo-700 font-bold"}>{detailedFortune.westernZodiac.element}</strong></span>
                          <span>•</span>
                          <span>Sao bảo hộ: <strong className={isDarkTheme ? "text-amber-300 font-bold" : "text-amber-700 font-bold"}>{detailedFortune.westernZodiac.rulingPlanet}</strong></span>
                        </div>
                      </div>
                    </div>

                    <p className={`text-xs sm:text-sm mt-5 leading-relaxed pt-5 border-t border-amber-500/20 font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                      {detailedFortune.westernZodiac.traits}
                    </p>
                  </div>

                  {/* Nét giao thoa Đông - Tây */}
                  <div
                    className={`p-6 rounded-3xl backdrop-blur-xl border shadow-md ${
                      isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
                    }`}
                  >
                    <h4 className="text-sm font-extrabold flex items-center gap-2 mb-3 text-indigo-500">
                      <Sparkles className="w-4 h-4" />
                      Nét Giao Thoa Độc Đáo: Con Giáp {detailedFortune.personalProfile.zodiacAnimal.split(" ")[0]} & Cung {detailedFortune.westernZodiac.name}
                    </h4>
                    <p className={`text-xs leading-relaxed font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                      Sự kết hợp giữa tướng tinh con giáp <strong>{detailedFortune.personalProfile.zodiacAnimal}</strong> của nền văn minh Á Đông và phẩm chất của cung <strong>{detailedFortune.westernZodiac.name} (Nguyên tố {detailedFortune.westernZodiac.element})</strong> tạo nên một cá tính đa tầng: vừa có sự nhạy bén, sâu sắc, trọng tình cảm của người phương Đông, vừa mang tư duy phản biện, tự tin và độc lập của người phương Tây.
                    </p>

                    <div className="mt-4 pt-4 border-t border-black/5 dark:border-white/10">
                      <span className={`text-xs font-bold block mb-2 ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>Thế mạnh kết hợp nổi bật:</span>
                      <div className="flex items-center gap-2 flex-wrap">
                        {detailedFortune.westernZodiac.strengths.map((st, i) => (
                          <span
                            key={i}
                            className={`px-3 py-1 rounded-full text-xs font-bold border ${
                              isDarkTheme
                                ? "bg-indigo-500/25 text-indigo-200 border-indigo-400/40"
                                : "bg-indigo-500/15 text-indigo-800 border-indigo-400/30"
                            }`}
                          >
                            ✨ {st}
                          </span>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* 5. VẬN HẠN NĂM 2026 (BÍNH NGỌ) */}
              {fortuneCategory === "year2026" && (
                <div className="space-y-6">
                  {/* Hero 2026 Card */}
                  <div
                    className={`p-6 sm:p-8 rounded-3xl backdrop-blur-xl border shadow-xl relative overflow-hidden ${
                      isDarkTheme
                        ? "bg-gradient-to-br from-amber-500/20 via-slate-900/90 to-red-950/20 border-amber-500/30 text-white"
                        : "bg-gradient-to-br from-amber-50 via-white to-red-50/70 border-amber-200 text-slate-900"
                    }`}
                  >
                    <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-4 border-b border-amber-500/20">
                      <div>
                        <span className="text-xs font-bold uppercase tracking-wider text-amber-500">
                          Dự Báo Vận Trình Năm 2026 (Bính Ngọ)
                        </span>
                        <h3 className="text-xl sm:text-2xl font-black mt-0.5">
                          {detailedFortune.profile.gender === "male" ? "Nam mạng" : "Nữ mạng"} • {detailedFortune.personalProfile.canChi}
                        </h3>
                        <div className={`text-xs mt-0.5 font-bold ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                          Năm 2026 tính theo tuổi mụ là: <strong className={isDarkTheme ? "text-amber-300" : "text-amber-700"}>{detailedFortune.saoHan2026.lunarAge} tuổi</strong>
                        </div>
                      </div>

                      <div className="flex items-center gap-2">
                        <span className={`text-xs font-black px-3 py-1.5 rounded-full border ${
                          detailedFortune.saoHan2026.saoType === "Cát tinh"
                            ? "bg-emerald-500/20 text-emerald-300 border-emerald-500/40"
                            : detailedFortune.saoHan2026.saoType === "Hung tinh"
                            ? "bg-rose-500/20 text-rose-300 border-rose-500/40"
                            : "bg-amber-500/20 text-amber-300 border-amber-400/40"
                        }`}>
                          Sao: {detailedFortune.saoHan2026.sao} ({detailedFortune.saoHan2026.saoType})
                        </span>
                      </div>
                    </div>

                    <p className={`text-xs sm:text-sm mt-4 leading-relaxed font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                      {detailedFortune.saoHan2026.yearOverview}
                    </p>
                  </div>

                  {/* 2 Cards: Sao Chiếu & Hạn Niên */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div
                      className={`p-6 rounded-3xl backdrop-blur-xl border shadow-md ${
                        isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
                      }`}
                    >
                      <h4 className="text-sm font-extrabold flex items-center gap-2 mb-2 text-amber-500">
                        <Star className="w-4 h-4" />
                        Sao Chiếu Mệnh Cửu Diệu: Sao {detailedFortune.saoHan2026.sao}
                      </h4>
                      <p className={`text-xs leading-relaxed ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                        {detailedFortune.saoHan2026.saoDescription}
                      </p>
                    </div>

                    <div
                      className={`p-6 rounded-3xl backdrop-blur-xl border shadow-md ${
                        isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
                      }`}
                    >
                      <h4 className="text-sm font-extrabold flex items-center gap-2 mb-2 text-rose-500">
                        <Shield className="w-4 h-4" />
                        Niên Hạn: Hạn {detailedFortune.saoHan2026.han}
                      </h4>
                      <p className={`text-xs leading-relaxed ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                        {detailedFortune.saoHan2026.hanDescription}
                      </p>
                    </div>
                  </div>

                  {/* Tháng tốt & Tháng cẩn trọng */}
                  <div
                    className={`p-6 rounded-3xl backdrop-blur-xl border shadow-md ${
                      isDarkTheme ? "bg-slate-900/50 border-white/10 text-white" : "bg-white/85 border-slate-200 text-slate-900"
                    }`}
                  >
                    <h4 className="text-sm font-extrabold flex items-center gap-2 mb-3 text-emerald-500">
                      <Calendar className="w-4 h-4" />
                      Lưu Ý Tháng May Mắn & Tháng Cần Phòng Ngừa Trong Năm 2026
                    </h4>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
                      <div className="p-3.5 rounded-2xl bg-emerald-500/10 border border-emerald-500/20">
                        <strong className="text-emerald-600 dark:text-emerald-400 block mb-1">
                          🌟 Các tháng Đại Cát vượng tài, dễ thành công:
                        </strong>
                        <div className="flex items-center gap-2 flex-wrap mt-1.5">
                          {detailedFortune.saoHan2026.favorableMonths.map(m => (
                            <span key={m} className={`px-2.5 py-1 rounded-lg font-bold ${
                              isDarkTheme ? "bg-emerald-500/25 text-emerald-200 border border-emerald-500/30" : "bg-emerald-500/20 text-emerald-800"
                            }`}>
                              Tháng {m} ÂL
                            </span>
                          ))}
                        </div>
                      </div>

                      <div className="p-3.5 rounded-2xl bg-rose-500/10 border border-rose-500/20">
                        <strong className="text-rose-500 block mb-1">
                          ⚠️ Các tháng kỵ nên thận trọng tiền bạc, sức khỏe:
                        </strong>
                        <div className="flex items-center gap-2 flex-wrap mt-1.5">
                          {detailedFortune.saoHan2026.cautionMonths.map(m => (
                            <span key={m} className={`px-2.5 py-1 rounded-lg font-bold ${
                              isDarkTheme ? "bg-rose-500/25 text-rose-200 border border-rose-500/30" : "bg-rose-500/20 text-rose-800"
                            }`}>
                              Tháng {m} ÂL
                            </span>
                          ))}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* Modal Thiết Lập Hồ Sơ Sinh & Giới Tính Gia Chủ */}
      {showBirthProfileModal && mounted && createPortal(
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/75 backdrop-blur-md animate-in fade-in duration-200">
          <div
            className={`w-full max-w-xl max-h-[90vh] overflow-y-auto p-6 rounded-3xl border shadow-2xl transition-all ${
              isDarkTheme ? "bg-slate-900 border-white/20 text-white" : "bg-white border-slate-200 text-slate-900"
            }`}
          >
            <div className="flex items-center justify-between pb-3 mb-4 border-b border-black/5 dark:border-white/10">
              <h3 className="text-base font-black flex items-center gap-2">
                <User className="w-5 h-5 text-amber-500" />
                Thiết Lập Hồ Sơ Sinh & Tử Vi Cá Nhân
              </h3>
              <button
                onClick={() => setShowBirthProfileModal(false)}
                className={`p-1.5 rounded-xl transition-all ${
                  isDarkTheme ? "hover:bg-white/10 text-slate-300 hover:text-white" : "hover:bg-slate-100 text-slate-500 hover:text-slate-900"
                }`}
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <p className={`text-xs mb-4 leading-relaxed font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
              Nhập <strong>Giới tính</strong>, <strong>Giờ sinh</strong> và <strong>Ngày, Tháng, Năm sinh</strong> để nhận <strong>Luận giải chi tiết bằng AI</strong> về vận mệnh, ngũ hành may mắn, hướng hợp tuổi, Thần Số Học, Cung Hoàng Đạo và Tử Vi trọn đời.
            </p>

            <div className="space-y-4">
              {/* Chọn Giới Tính */}
              <div>
                <label className={`block text-xs font-bold mb-1.5 ${isDarkTheme ? "text-slate-100" : "text-slate-800"}`}>
                  Giới tính (Dùng để tính Cung mệnh & hướng phong thủy):
                </label>
                <div className="grid grid-cols-2 gap-2.5">
                  <button
                    type="button"
                    onClick={() => setTempGender("male")}
                    className={`py-2.5 px-4 rounded-2xl font-bold text-xs transition-all flex items-center justify-center gap-2 border ${
                      tempGender === "male"
                        ? "bg-amber-500 text-slate-950 border-amber-400 shadow-md shadow-amber-500/20"
                        : isDarkTheme
                        ? "bg-slate-800 text-slate-200 border-white/15 hover:bg-slate-700"
                        : "bg-slate-100 text-slate-700 border-slate-200 hover:bg-slate-200"
                    }`}
                  >
                    <span>👨</span>
                    <span>Nam Mạng</span>
                  </button>
                  <button
                    type="button"
                    onClick={() => setTempGender("female")}
                    className={`py-2.5 px-4 rounded-2xl font-bold text-xs transition-all flex items-center justify-center gap-2 border ${
                      tempGender === "female"
                        ? "bg-amber-500 text-slate-950 border-amber-400 shadow-md shadow-amber-500/20"
                        : isDarkTheme
                        ? "bg-slate-800 text-slate-200 border-white/15 hover:bg-slate-700"
                        : "bg-slate-100 text-slate-700 border-slate-200 hover:bg-slate-200"
                    }`}
                  >
                    <span>👩</span>
                    <span>Nữ Mạng</span>
                  </button>
                </div>
              </div>

              {/* Loại Lịch */}
              <div>
                <label className={`block text-xs font-bold mb-1.5 ${isDarkTheme ? "text-slate-100" : "text-slate-800"}`}>
                  Loại ngày sinh:
                </label>
                <div className="grid grid-cols-2 gap-2.5">
                  <button
                    type="button"
                    onClick={() => setTempIsLunar(false)}
                    className={`py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 border ${
                      !tempIsLunar
                        ? "bg-amber-500/25 text-amber-700 dark:text-amber-300 border-amber-400/60"
                        : isDarkTheme
                        ? "bg-slate-800 text-slate-200 border-white/15 hover:bg-slate-700"
                        : "bg-slate-100 text-slate-700 border-slate-200 hover:bg-slate-200"
                    }`}
                  >
                    <span>☀️ Dương Lịch (Chuẩn)</span>
                  </button>
                  <button
                    type="button"
                    onClick={() => setTempIsLunar(true)}
                    className={`py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-1.5 border ${
                      tempIsLunar
                        ? "bg-amber-500/25 text-amber-700 dark:text-amber-300 border-amber-400/60"
                        : isDarkTheme
                        ? "bg-slate-800 text-slate-200 border-white/15 hover:bg-slate-700"
                        : "bg-slate-100 text-slate-700 border-slate-200 hover:bg-slate-200"
                    }`}
                  >
                    <span>🌙 Âm Lịch</span>
                  </button>
                </div>
              </div>

              {/* Grid 3 Selects: Ngày, Tháng, Năm */}
              <div className="grid grid-cols-3 gap-2.5">
                <div>
                  <label className={`block text-xs font-bold mb-1 ${isDarkTheme ? "text-slate-100" : "text-slate-800"}`}>
                    Ngày sinh:
                  </label>
                  <select
                    value={effectiveTempDay}
                    onChange={(e) => setTempDay(Number(e.target.value))}
                    className={`w-full p-2.5 rounded-xl border text-xs font-bold focus:outline-none focus:ring-2 focus:ring-amber-500 ${
                      isDarkTheme ? "bg-slate-800 border-white/20 text-white" : "bg-slate-50 border-slate-300 text-slate-900"
                    }`}
                  >
                    {Array.from({ length: maxModalDays }).map((_, i) => (
                      <option key={i + 1} value={i + 1}>
                        Ngày {i + 1}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className={`block text-xs font-bold mb-1 ${isDarkTheme ? "text-slate-100" : "text-slate-800"}`}>
                    Tháng sinh:
                  </label>
                  <select
                    value={tempMonth}
                    onChange={(e) => setTempMonth(Number(e.target.value))}
                    className={`w-full p-2.5 rounded-xl border text-xs font-bold focus:outline-none focus:ring-2 focus:ring-amber-500 ${
                      isDarkTheme ? "bg-slate-800 border-white/20 text-white" : "bg-slate-50 border-slate-300 text-slate-900"
                    }`}
                  >
                    {Array.from({ length: 12 }).map((_, i) => (
                      <option key={i + 1} value={i + 1}>
                        Tháng {i + 1}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className={`block text-xs font-bold mb-1 ${isDarkTheme ? "text-slate-100" : "text-slate-800"}`}>
                    Năm sinh:
                  </label>
                  <select
                    value={tempYear}
                    onChange={(e) => setTempYear(Number(e.target.value))}
                    className={`w-full p-2.5 rounded-xl border text-xs font-bold focus:outline-none focus:ring-2 focus:ring-amber-500 ${
                      isDarkTheme ? "bg-slate-800 border-white/20 text-white" : "bg-slate-50 border-slate-300 text-slate-900"
                    }`}
                  >
                    {Array.from({ length: 97 }).map((_, i) => {
                      const y = 2026 - i;
                      const p = getPersonalProfile(y);
                      return (
                        <option key={y} value={y}>
                          {y} ({p.canChi})
                        </option>
                      );
                    })}
                  </select>
                </div>
              </div>

              {/* Ô Nhập Giờ Sinh (12 Canh Giờ Âm Lịch) */}
              <div>
                <div className="flex items-center justify-between mb-1">
                  <label className={`block text-xs font-bold ${isDarkTheme ? "text-slate-100" : "text-slate-800"}`}>
                    ⏰ Giờ sinh (Xem tính cách, đường con cái & hậu vận):
                  </label>
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                    isDarkTheme ? "bg-purple-500/20 text-purple-300" : "bg-purple-100 text-purple-800"
                  }`}>
                    12 Khung Giờ Trong Ngày
                  </span>
                </div>
                <select
                  value={tempBirthHour}
                  onChange={(e) => setTempBirthHour(Number(e.target.value))}
                  className={`w-full p-2.5 rounded-xl border text-xs font-bold focus:outline-none focus:ring-2 focus:ring-amber-500 ${
                    isDarkTheme ? "bg-slate-800 border-white/20 text-white" : "bg-slate-50 border-slate-300 text-slate-900"
                  }`}
                >
                  {BIRTH_HOUR_OPTIONS.map((h) => (
                    <option key={h.chiIndex} value={h.chiIndex}>
                      {h.emoji} Giờ {h.chi} ({h.range}) — {h.periodLabel}
                    </option>
                  ))}
                  <option value={-1}>❓ Chưa rõ giờ sinh (Chỉ tính theo Năm - Tháng - Ngày sinh)</option>
                </select>
              </div>

              {/* Quick Decade Shortcuts */}
              <div className="flex items-center gap-1.5 flex-wrap">
                <span className={`text-[11px] font-bold mr-1 ${isDarkTheme ? "text-slate-200" : "text-slate-600"}`}>Năm nhanh:</span>
                {[1985, 1988, 1990, 1992, 1995, 1998, 2000, 2003].map((y) => (
                  <button
                    key={y}
                    type="button"
                    onClick={() => setTempYear(y)}
                    className={`text-xs px-2.5 py-1 rounded-xl font-bold transition-all border ${
                      tempYear === y
                        ? "bg-amber-500 text-slate-950 border-amber-400 shadow-xs"
                        : isDarkTheme
                        ? "bg-white/10 hover:bg-white/15 text-slate-200 border-white/10"
                        : "bg-slate-100 hover:bg-slate-200 text-slate-700 border-slate-200"
                    }`}
                  >
                    {y}
                  </button>
                ))}
              </div>

              {/* Live Preview Card */}
              {modalPreviewFortune && (
                <div
                  className={`p-4 rounded-2xl border ${
                    isDarkTheme
                      ? "bg-gradient-to-br from-amber-500/15 via-slate-800/80 to-slate-900 border-amber-500/40"
                      : "bg-amber-50/90 border-amber-300 shadow-xs"
                  }`}
                >
                  <div className="flex items-center justify-between mb-2.5 flex-wrap gap-1.5">
                    <span className="text-sm font-black text-amber-500 dark:text-amber-300 flex items-center gap-1.5">
                      <span>{tempGender === "male" ? "👨 Nam mạng" : "👩 Nữ mạng"}</span>
                      <span>•</span>
                      <span>
                        Tuổi {modalPreviewFortune.personalProfile.canChi} ({modalPreviewFortune.personalProfile.birthYear}
                        {modalPreviewFortune.personalProfile.birthYear !== modalPreviewFortune.solarDate.year
                          ? ` ÂL • DL ${modalPreviewFortune.solarDate.year}`
                          : ""}
                        )
                      </span>
                    </span>
                    <span className={`text-xs font-bold px-2.5 py-0.5 rounded-full border ${
                      isDarkTheme
                        ? "bg-amber-500/25 text-amber-200 border-amber-400/40"
                        : "bg-amber-500/20 text-amber-800 border-amber-400/30"
                    }`}>
                      Mệnh {elementLabel(modalPreviewFortune.personalProfile.element)} • {modalPreviewFortune.personalProfile.napAm}
                    </span>
                  </div>
                  <div className={`grid grid-cols-2 gap-2 text-xs font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                    <div>• Cung phi {tempGender === "male" ? "Nam" : "Nữ"}: <strong className={isDarkTheme ? "text-emerald-300 font-bold" : "text-emerald-700 font-bold"}>{modalPreviewFortune.cungPhi} ({modalPreviewFortune.cungPhiGroup})</strong></div>
                    <div>• Cung Hoàng Đạo: <strong className={isDarkTheme ? "text-indigo-300 font-bold" : "text-indigo-700 font-bold"}>{modalPreviewFortune.westernZodiac.symbol} {modalPreviewFortune.westernZodiac.name}</strong></div>
                    <div>• Thần số học: <strong className={isDarkTheme ? "text-sky-300 font-bold" : "text-sky-700 font-bold"}>Số {modalPreviewFortune.numerology.lifePathNumber}</strong> ({modalPreviewFortune.numerology.title.split(" & ")[0]})</div>
                    <div>• Giờ sinh: <strong className={isDarkTheme ? "text-purple-300 font-bold" : "text-purple-700 font-bold"}>{tempBirthHour >= 0 ? `Giờ ${modalPreviewFortune.birthHourFortune.hourChi} (${modalPreviewFortune.birthHourFortune.hourCanChi})` : "Chưa rõ giờ"}</strong></div>
                    <div className={`col-span-2 pt-1.5 border-t border-black/10 dark:border-white/10 text-[11px] ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                      • 4 cột mốc sinh: <strong className="text-slate-900 dark:text-white font-bold">Năm {modalPreviewFortune.fourPillars.yearPillar.canChi} • Tháng {modalPreviewFortune.fourPillars.monthPillar.canChi} • Ngày {modalPreviewFortune.fourPillars.dayPillar.canChi} {tempBirthHour >= 0 ? `• Giờ ${modalPreviewFortune.birthHourFortune.hourCanChi}` : ""}</strong> (Hành bổ trợ: <strong className={isDarkTheme ? "text-emerald-300" : "text-emerald-700"}>{modalPreviewFortune.fourPillars.hyDungThan.map(elementLabel).join(" & ")}</strong>)
                    </div>
                  </div>
                </div>
              )}

              {/* Action Buttons */}
              <div className="flex items-center justify-between gap-3 pt-2">
                {userProfile || birthYear > 0 ? (
                  <button
                    type="button"
                    onClick={handleClearProfile}
                    className="px-4 py-2.5 rounded-xl text-xs font-bold text-rose-500 hover:bg-rose-500/15 transition-all"
                  >
                    Xóa hồ sơ
                  </button>
                ) : (
                  <div />
                )}

                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={() => setShowBirthProfileModal(false)}
                    className={`px-4 py-2.5 rounded-xl text-xs font-bold transition-all ${
                      isDarkTheme ? "bg-white/10 hover:bg-white/15 text-slate-200" : "bg-slate-100 hover:bg-slate-200 text-slate-700"
                    }`}
                  >
                    Hủy
                  </button>
                  <button
                    type="button"
                    onClick={handleSaveProfile}
                    className="px-5 py-2.5 rounded-xl text-xs font-black bg-amber-500 hover:bg-amber-600 text-slate-950 transition-all shadow-md flex items-center gap-1.5"
                  >
                    <Check className="w-4 h-4" />
                    Lưu Hồ Sơ & Mở Lá Số
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>,
        document.body
      )}
    </div>
  );
}
