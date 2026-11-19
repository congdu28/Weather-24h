"use client";

import React, { useEffect, useMemo, useRef, useState } from "react";
import { Compass, Crown, Loader2, RefreshCw, ScrollText, Sparkles, UserCheck } from "lucide-react";
import { BIRTH_HOUR_OPTIONS, FIVE_ELEMENT_META, UserBirthProfile, elementLabel } from "../utils/lunarAlmanac";
import {
  TuViChartResult,
  TuViDeepAnalysis,
  TuViPalace,
  TuViPersonInput,
  generateTuViChart
} from "../utils/tuViChart";
import { analyzeTuViChartWithGemini, getCachedTuViChartAi } from "../services/geminiFortuneService";

interface TuViChartViewProps {
  isDarkTheme: boolean;
  userProfile: UserBirthProfile | null;
  geminiApiKey?: string;
}

const DEFAULT_INPUT: TuViPersonInput = {
  name: "",
  gender: "male",
  day: 28,
  month: 4,
  year: 2002,
  birthHour: 3, // Giờ Mão
  isLunar: false
};

const YEARS = Array.from({ length: 91 }, (_, i) => 2030 - i);
const MONTHS = Array.from({ length: 12 }, (_, i) => i + 1);
const DAYS = Array.from({ length: 31 }, (_, i) => i + 1);

// Vị trí lưới 4x4 truyền thống theo chiIndex (0=Tý .. 11=Hợi)
const GRID_POSITION_BY_CHI: Record<number, string> = {
  5: "md:col-start-1 md:row-start-1", // Tỵ
  6: "md:col-start-2 md:row-start-1", // Ngọ
  7: "md:col-start-3 md:row-start-1", // Mùi
  8: "md:col-start-4 md:row-start-1", // Thân
  4: "md:col-start-1 md:row-start-2", // Thìn
  9: "md:col-start-4 md:row-start-2", // Dậu
  3: "md:col-start-1 md:row-start-3", // Mão
  10: "md:col-start-4 md:row-start-3", // Tuất
  2: "md:col-start-1 md:row-start-4", // Dần
  1: "md:col-start-2 md:row-start-4", // Sửu
  0: "md:col-start-3 md:row-start-4", // Tý
  11: "md:col-start-4 md:row-start-4" // Hợi
};

// Thứ tự render để trên mobile xếp theo thứ tự lưới gọn gàng
const DISPLAY_CHI_ORDER = [5, 6, 7, 8, 4, 9, 3, 10, 2, 1, 0, 11];

type Phase = "idle" | "chart" | "stars" | "deep" | "done";
const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

export const TuViChartView: React.FC<TuViChartViewProps> = ({
  isDarkTheme,
  userProfile,
  geminiApiKey
}) => {
  const [input, setInput] = useState<TuViPersonInput>(DEFAULT_INPUT);
  const [chart, setChart] = useState<TuViChartResult | null>(null);
  const [selectedChi, setSelectedChi] = useState<number>(0);
  const [errorMsg, setErrorMsg] = useState<string>("");

  const [phase, setPhase] = useState<Phase>("idle");
  const [statusText, setStatusText] = useState<string>("");
  const [showStars, setShowStars] = useState<boolean>(false);
  const [deepAnalysis, setDeepAnalysis] = useState<{ analysis: TuViDeepAnalysis; source: "gemini-live" | "builtin" } | null>(null);
  const [visibleAspectCount, setVisibleAspectCount] = useState<number>(0);
  const runIdRef = useRef<number>(0);

  useEffect(() => {
    if (userProfile && userProfile.year >= 1900) {
      setInput((prev) => ({
        ...prev,
        gender: userProfile.gender,
        day: userProfile.day,
        month: userProfile.month,
        year: userProfile.year,
        birthHour: typeof userProfile.birthHour === "number" && userProfile.birthHour >= 0 ? userProfile.birthHour : 6,
        isLunar: Boolean(userProfile.isLunar)
      }));
    }
  }, [userProfile]);

  const applyMyProfile = () => {
    if (!userProfile) return;
    setInput((prev) => ({
      ...prev,
      gender: userProfile.gender,
      day: userProfile.day,
      month: userProfile.month,
      year: userProfile.year,
      birthHour: typeof userProfile.birthHour === "number" && userProfile.birthHour >= 0 ? userProfile.birthHour : 6,
      isLunar: Boolean(userProfile.isLunar)
    }));
  };

  const runCastChart = async (forceRefreshDeep = false) => {
    setErrorMsg("");
    let generated: TuViChartResult;
    try {
      generated = generateTuViChart(input);
    } catch {
      setErrorMsg("Ngày tháng năm sinh không hợp lệ (ví dụ ngày 30/2 hoặc ngày âm lịch không tồn tại). Vui lòng kiểm tra lại.");
      return;
    }

    const currentRun = ++runIdRef.current;
    const isCancelled = () => runIdRef.current !== currentRun;

    setChart(generated);
    setSelectedChi(generated.menhChiIndex);
    setShowStars(false);
    setDeepAnalysis(null);
    setVisibleAspectCount(0);

    const cached = !forceRefreshDeep ? getCachedTuViChartAi(generated) : null;
    const deepPromise = cached
      ? Promise.resolve({ analysis: cached, source: "gemini-live" as const })
      : analyzeTuViChartWithGemini(generated, geminiApiKey, forceRefreshDeep);

    // Nhịp 1: An Thiên Bàn & 12 Cung
    setPhase("chart");
    setStatusText("Nhịp 1/3 • Đang lập Thiên Bàn, an Cung Mệnh - Thân & định Cục...");
    await wait(340);
    if (isCancelled()) return;

    // Nhịp 2: An 14 Chính Tinh, Phụ Tinh, Tứ Hóa, Tuần/Triệt
    setPhase("stars");
    setStatusText("Nhịp 2/3 • Đang an 14 Chính Tinh, Tứ Hóa, Tuần/Triệt & Vòng Đại Vận...");
    setShowStars(true);
    await wait(480);
    if (isCancelled()) return;

    // Nhịp 3: Luận giải chi tiết toàn bộ lá số theo từng chương
    setPhase("deep");
    setStatusText("Nhịp 3/3 • Đang luận giải chi tiết Mệnh-Thân, Quan Lộc, Tài Bạch, Phu Thê & Đại Vận...");
    const deepRes = await deepPromise;
    if (isCancelled()) return;
    setDeepAnalysis(deepRes);

    for (let i = 1; i <= deepRes.analysis.aspects.length; i++) {
      await wait(200);
      if (isCancelled()) return;
      setVisibleAspectCount(i);
    }

    setPhase("done");
    setStatusText("");
  };

  const selectedPalace: TuViPalace | null = useMemo(() => {
    if (!chart) return null;
    return chart.palaces[selectedChi] || chart.palaces[chart.menhChiIndex];
  }, [chart, selectedChi]);

  const isRunning = phase !== "idle" && phase !== "done";

  const cardBg = isDarkTheme
    ? "bg-slate-900/55 border-white/10 text-white"
    : "bg-white/90 border-slate-200/90 text-slate-900 shadow-md";

  const subCardBg = isDarkTheme
    ? "bg-white/5 border-white/10 text-slate-200"
    : "bg-slate-50/90 border-slate-200 text-slate-800";

  const inputClass = isDarkTheme
    ? "bg-slate-950/70 border-white/15 text-white focus:border-amber-400"
    : "bg-white border-slate-300 text-slate-900 focus:border-amber-500";

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Header & Form nhập thông tin lập lá số */}
      <div className={`p-5 sm:p-6 rounded-3xl border backdrop-blur-xl ${cardBg}`}>
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-5">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-amber-500/20 text-amber-500 flex items-center justify-center shrink-0">
              <Crown className="w-6 h-6" />
            </div>
            <div>
              <h2 className="text-base sm:text-lg font-black">Lập Lá Số Tử Vi Đẩu Số (12 Cung)</h2>
              <p className={`text-xs ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                An sao chuẩn 14 Chính Tinh, Tứ Hóa, Tuần/Triệt, Vòng Đại Vận và bấm vào từng cung để xem luận giải chi tiết
              </p>
            </div>
          </div>

          {userProfile && (
            <button
              type="button"
              onClick={applyMyProfile}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold border flex items-center gap-1.5 self-start sm:self-center transition-all ${
                isDarkTheme
                  ? "bg-white/10 border-white/15 text-amber-300 hover:bg-white/15"
                  : "bg-amber-50 border-amber-200 text-amber-800 hover:bg-amber-100"
              }`}
            >
              <UserCheck className="w-4 h-4" />
              Lấy từ hồ sơ của tôi
            </button>
          )}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-12 gap-3.5 items-end">
          {/* Họ tên */}
          <div className="md:col-span-3">
            <label className={`block text-[11px] font-bold mb-1 ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
              Họ tên (tùy chọn)
            </label>
            <input
              type="text"
              value={input.name}
              onChange={(e) => setInput((prev) => ({ ...prev, name: e.target.value }))}
              placeholder="Ví dụ: Nguyễn Văn An"
              className={`w-full px-3 py-2 rounded-xl border text-xs font-semibold outline-none ${inputClass}`}
            />
          </div>

          {/* Giới tính */}
          <div className="md:col-span-2">
            <label className={`block text-[11px] font-bold mb-1 ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
              Giới tính
            </label>
            <div className="grid grid-cols-2 gap-1.5">
              {(["male", "female"] as const).map((g) => (
                <button
                  key={g}
                  type="button"
                  onClick={() => setInput((prev) => ({ ...prev, gender: g }))}
                  className={`py-2 rounded-xl text-xs font-bold border transition-all ${
                    input.gender === g
                      ? "bg-amber-500 text-slate-950 border-amber-500 shadow-xs"
                      : isDarkTheme
                      ? "bg-white/5 border-white/10 text-slate-300 hover:bg-white/10"
                      : "bg-slate-100 border-slate-200 text-slate-700 hover:bg-slate-200"
                  }`}
                >
                  {g === "male" ? "Nam" : "Nữ"}
                </button>
              ))}
            </div>
          </div>

          {/* Loại lịch */}
          <div className="md:col-span-2">
            <label className={`block text-[11px] font-bold mb-1 ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
              Loại lịch
            </label>
            <div className="grid grid-cols-2 gap-1.5">
              {[
                { label: "Dương", value: false },
                { label: "Âm", value: true }
              ].map((opt) => (
                <button
                  key={String(opt.value)}
                  type="button"
                  onClick={() => setInput((prev) => ({ ...prev, isLunar: opt.value }))}
                  className={`py-2 rounded-xl text-xs font-bold border transition-all ${
                    input.isLunar === opt.value
                      ? "bg-amber-500 text-slate-950 border-amber-500 shadow-xs"
                      : isDarkTheme
                      ? "bg-white/5 border-white/10 text-slate-300 hover:bg-white/10"
                      : "bg-slate-100 border-slate-200 text-slate-700 hover:bg-slate-200"
                  }`}
                >
                  {opt.label}
                </button>
              ))}
            </div>
          </div>

          {/* Ngày / Tháng / Năm */}
          <div className="md:col-span-5 grid grid-cols-3 gap-2">
            <div>
              <label className={`block text-[11px] font-bold mb-1 ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                Ngày
              </label>
              <select
                value={input.day}
                onChange={(e) => setInput((prev) => ({ ...prev, day: Number(e.target.value) }))}
                className={`w-full px-2 py-2 rounded-xl border text-xs font-bold outline-none ${inputClass}`}
              >
                {DAYS.map((d) => (
                  <option key={d} value={d}>
                    Ngày {d}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className={`block text-[11px] font-bold mb-1 ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                Tháng
              </label>
              <select
                value={input.month}
                onChange={(e) => setInput((prev) => ({ ...prev, month: Number(e.target.value) }))}
                className={`w-full px-2 py-2 rounded-xl border text-xs font-bold outline-none ${inputClass}`}
              >
                {MONTHS.map((m) => (
                  <option key={m} value={m}>
                    Tháng {m}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className={`block text-[11px] font-bold mb-1 ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                Năm
              </label>
              <select
                value={input.year}
                onChange={(e) => setInput((prev) => ({ ...prev, year: Number(e.target.value) }))}
                className={`w-full px-2 py-2 rounded-xl border text-xs font-bold outline-none ${inputClass}`}
              >
                {YEARS.map((y) => (
                  <option key={y} value={y}>
                    {y}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Giờ sinh */}
          <div className="md:col-span-8">
            <label className={`block text-[11px] font-bold mb-1 ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
              Giờ sinh (12 Địa Chi — quyết định vị trí Cung Mệnh & Chính Tinh)
            </label>
            <select
              value={input.birthHour}
              onChange={(e) => setInput((prev) => ({ ...prev, birthHour: Number(e.target.value) }))}
              className={`w-full px-3 py-2 rounded-xl border text-xs font-bold outline-none ${inputClass}`}
            >
              {BIRTH_HOUR_OPTIONS.map((opt) => (
                <option key={opt.chiIndex} value={opt.chiIndex}>
                  {opt.emoji} Giờ {opt.chi} ({opt.range}) — {opt.periodLabel}
                </option>
              ))}
            </select>
          </div>

          {/* Nút Lập Lá Số */}
          <div className="md:col-span-4">
            <button
              type="button"
              disabled={isRunning}
              onClick={() => runCastChart(false)}
              className="w-full py-2.5 px-4 rounded-xl bg-gradient-to-r from-amber-500 to-orange-500 hover:from-amber-600 hover:to-orange-600 disabled:opacity-70 text-slate-950 font-black text-xs sm:text-sm shadow-lg shadow-amber-500/25 flex items-center justify-center gap-2 transition-all active:scale-[0.98]"
            >
              {isRunning ? <Loader2 className="w-4 h-4 animate-spin" /> : <ScrollText className="w-4 h-4" />}
              {isRunning ? "Đang Lập Lá Số..." : "Lập Lá Số & Luận Giải"}
            </button>
          </div>
        </div>
      </div>

      {errorMsg && (
        <div className="p-3.5 rounded-2xl border border-rose-500/35 bg-rose-500/15 text-rose-500 text-xs font-bold text-center">
          {errorMsg}
        </div>
      )}

      {/* Thanh trạng thái chạy theo từng nhịp */}
      {isRunning && statusText && (
        <div
          className={`p-3.5 rounded-2xl border flex items-center justify-center gap-2.5 text-xs font-bold animate-in fade-in duration-200 ${
            isDarkTheme
              ? "bg-amber-500/10 border-amber-500/30 text-amber-300"
              : "bg-amber-50 border-amber-200 text-amber-800"
          }`}
        >
          <Loader2 className="w-4 h-4 animate-spin shrink-0" />
          <span>{statusText}</span>
        </div>
      )}

      {/* LÁ SỐ TỬ VI 12 CUNG */}
      {chart && (
        <div className="space-y-6 animate-in fade-in duration-300">
          {/* Grid 4x4 truyền thống trên desktop, xếp gọn trên mobile */}
          <div className={`p-3 sm:p-4 rounded-3xl border backdrop-blur-xl ${cardBg}`}>
            <div className="flex items-center justify-between gap-2 mb-3 px-1 flex-wrap">
              <div className="text-xs font-black flex items-center gap-2">
                <Compass className="w-4 h-4 text-amber-500" />
                Bản Đồ Lá Số Tử Vi 12 Cung (Bấm vào từng cung để xem chi tiết)
              </div>
              <div className="flex items-center gap-2 text-[10px] font-bold">
                <span className="px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-500">Chính Tinh</span>
                <span className="px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-500">Cát Tinh</span>
                <span className="px-2 py-0.5 rounded-full bg-rose-500/20 text-rose-500">Sát Tinh</span>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-2.5">
              {/* THIÊN BÀN TRUNG TÂM */}
              <div
                className={`md:col-start-2 md:col-span-2 md:row-start-2 md:row-span-2 p-4 sm:p-5 rounded-2xl border flex flex-col justify-between ${
                  isDarkTheme
                    ? "bg-gradient-to-br from-amber-500/10 via-slate-900/80 to-rose-500/10 border-amber-500/30"
                    : "bg-gradient-to-br from-amber-50 via-white to-orange-50 border-amber-300"
                }`}
              >
                <div className="text-center border-b pb-3 border-amber-500/20">
                  <div className="text-[11px] font-bold uppercase tracking-wider text-amber-500">
                    Thiên Bàn Tử Vi Đẩu Số
                  </div>
                  <h3 className="text-base sm:text-lg font-black mt-0.5">{chart.displayName}</h3>
                  <div className="flex items-center justify-center gap-2 flex-wrap mt-1.5">
                    <span className="text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-amber-500/20 text-amber-500">
                      {chart.amDuongLabel} • {chart.isThuan ? "Thuận Lý" : "Nghịch Lý"}
                    </span>
                    <span className="text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-sky-500/20 text-sky-500">
                      {chart.cucName}
                    </span>
                  </div>
                </div>

                <div className={`grid grid-cols-2 gap-x-3 gap-y-1.5 text-[11px] py-3 ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                  <div>
                    <span className="opacity-70">Dương lịch:</span>{" "}
                    <strong>
                      {chart.solarDate.day}/{chart.solarDate.month}/{chart.solarDate.year}
                    </strong>
                  </div>
                  <div>
                    <span className="opacity-70">Âm lịch:</span>{" "}
                    <strong>
                      {chart.lunarDate.day}/{chart.lunarDate.month}/{chart.lunarDate.year}
                    </strong>
                  </div>
                  <div>
                    <span className="opacity-70">Năm sinh:</span> <strong>{chart.yearCanChi}</strong>
                  </div>
                  <div>
                    <span className="opacity-70">Giờ sinh:</span> <strong>Giờ {chart.hourLabel}</strong>
                  </div>
                  <div className="col-span-2">
                    <span className="opacity-70">Bản Mệnh:</span>{" "}
                    <strong>
                      {FIVE_ELEMENT_META[chart.menhElement].emoji} {chart.napAmMenh} ({elementLabel(chart.menhElement)})
                    </strong>
                  </div>
                  <div>
                    <span className="opacity-70">Chủ Mệnh:</span> <strong>{chart.chuMenh}</strong>
                  </div>
                  <div>
                    <span className="opacity-70">Chủ Thân:</span> <strong>{chart.chuThan}</strong>
                  </div>
                  <div className="col-span-2">
                    <span className="opacity-70">Thân cư:</span>{" "}
                    <strong className="text-rose-500">{chart.thanPalaceName}</strong>
                  </div>
                </div>

                <div className={`pt-2.5 border-t border-amber-500/20 text-[10px] grid grid-cols-2 gap-1 ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                  {chart.tuHoaSummary.map((th) => (
                    <div key={th.hoa} className="truncate">
                      <strong className={th.hoa === "Hóa Kỵ" ? "text-rose-500" : "text-emerald-500"}>{th.hoa}:</strong> {th.star}
                    </div>
                  ))}
                </div>
              </div>

              {/* 12 CUNG */}
              {DISPLAY_CHI_ORDER.map((chiIdx) => {
                const palace = chart.palaces[chiIdx];
                const isSelected = selectedChi === chiIdx;
                return (
                  <button
                    key={chiIdx}
                    type="button"
                    onClick={() => setSelectedChi(chiIdx)}
                    className={`${GRID_POSITION_BY_CHI[chiIdx]} text-left p-2.5 rounded-2xl border transition-all flex flex-col justify-between min-h-[148px] ${
                      isSelected
                        ? isDarkTheme
                          ? "bg-amber-500/15 border-amber-400 ring-2 ring-amber-400/50"
                          : "bg-amber-50 border-amber-500 ring-2 ring-amber-400/50"
                        : palace.isMenh
                        ? isDarkTheme
                          ? "bg-rose-500/10 border-rose-400/40 hover:border-amber-400"
                          : "bg-rose-50/70 border-rose-300 hover:border-amber-500"
                        : isDarkTheme
                        ? "bg-white/5 border-white/10 hover:bg-white/10"
                        : "bg-slate-50/80 border-slate-200 hover:bg-white"
                    }`}
                  >
                    {/* Header cung */}
                    <div>
                      <div className="flex items-center justify-between gap-1 border-b pb-1 mb-1.5 border-slate-500/20">
                        <span className="text-[10px] font-bold opacity-75">
                          {palace.palaceCan} {palace.chiName}
                        </span>
                        <span className="text-[11px] font-black uppercase tracking-tight text-amber-500">
                          {palace.palaceName}
                        </span>
                        <span className="text-[10px] font-bold opacity-75">{palace.daiVanStart}</span>
                      </div>

                      {/* Badges Mệnh / Thân / Tuần / Triệt */}
                      {(palace.isMenh || palace.isThan || palace.tuan || palace.triet) && (
                        <div className="flex flex-wrap gap-1 mb-1.5">
                          {palace.isMenh && (
                            <span className="px-1.5 py-0.2 rounded bg-rose-500 text-white text-[9px] font-black">
                              MỆNH
                            </span>
                          )}
                          {palace.isThan && (
                            <span className="px-1.5 py-0.2 rounded bg-indigo-500 text-white text-[9px] font-black">
                              THÂN
                            </span>
                          )}
                          {palace.tuan && (
                            <span className="px-1.5 py-0.2 rounded bg-slate-600 text-white text-[9px] font-bold">
                              TUẦN
                            </span>
                          )}
                          {palace.triet && (
                            <span className="px-1.5 py-0.2 rounded bg-slate-800 text-amber-300 text-[9px] font-bold">
                              TRIỆT
                            </span>
                          )}
                        </div>
                      )}

                      {/* Chính Tinh & Phụ Tinh */}
                      {showStars ? (
                        <div className="space-y-1 animate-in fade-in duration-300">
                          {palace.chinhTinh.length === 0 ? (
                            <div className="text-[10px] italic opacity-60">Vô Chính Diệu</div>
                          ) : (
                            <div className="space-y-0.5">
                              {palace.chinhTinh.map((s) => (
                                <div key={s.name} className="text-[11px] font-black text-amber-500 leading-tight flex items-center justify-between gap-1">
                                  <span>
                                    {s.name} <span className="text-[9px] opacity-80">({s.brightness})</span>
                                  </span>
                                  {s.tuHoaTag && (
                                    <span
                                      className={`text-[9px] px-1 rounded font-bold ${
                                        s.tuHoaTag === "Hóa Kỵ"
                                          ? "bg-rose-500/20 text-rose-500"
                                          : "bg-emerald-500/20 text-emerald-500"
                                      }`}
                                    >
                                      {s.tuHoaTag.replace("Hóa ", "")}
                                    </span>
                                  )}
                                </div>
                              ))}
                            </div>
                          )}

                          {/* Cát & Sát tinh gọn */}
                          <div className="flex flex-wrap gap-x-1.5 gap-y-0.5 text-[10px] leading-tight pt-0.5">
                            {palace.catTinh.map((c) => (
                              <span key={c.name} className="text-emerald-500 font-semibold">
                                {c.name}
                                {c.tuHoaTag ? `(${c.tuHoaTag.replace("Hóa ", "")})` : ""}
                              </span>
                            ))}
                            {palace.satTinh.map((st) => (
                              <span key={st.name} className="text-rose-500 font-semibold">
                                {st.name}
                              </span>
                            ))}
                          </div>
                        </div>
                      ) : (
                        <div className="text-[10px] opacity-50 italic py-2">Đang an sao...</div>
                      )}
                    </div>

                    {/* Footer cung */}
                    <div className="flex items-center justify-between text-[9px] opacity-70 pt-1.5 mt-1 border-t border-slate-500/15">
                      <span>{palace.trangSinh}</span>
                      <span>ĐV {palace.daiVanRange}</span>
                    </div>
                  </button>
                );
              })}
            </div>
          </div>

          {/* CHI TIẾT CUNG ĐANG CHỌN */}
          {showStars && selectedPalace && (
            <div className={`p-5 sm:p-6 rounded-3xl border backdrop-blur-xl animate-in fade-in duration-300 ${cardBg}`}>
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-3">
                <div>
                  <div className="flex items-center gap-2 flex-wrap">
                    <h3 className="text-base sm:text-lg font-black">
                      Chi Tiết Cung {selectedPalace.palaceName} ({selectedPalace.palaceCan} {selectedPalace.chiName})
                    </h3>
                    {selectedPalace.isMenh && (
                      <span className="px-2 py-0.5 rounded-full text-[11px] font-black bg-rose-500 text-white">Cung Mệnh</span>
                    )}
                    {selectedPalace.isThan && (
                      <span className="px-2 py-0.5 rounded-full text-[11px] font-black bg-indigo-500 text-white">Cung Thân</span>
                    )}
                  </div>
                  <p className={`text-xs mt-0.5 ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                    Đại Vận: <strong>{selectedPalace.daiVanRange} tuổi</strong> • Vòng Tràng Sinh: <strong>{selectedPalace.trangSinh}</strong>
                  </p>
                </div>
                <div className="px-3 py-1.5 rounded-2xl bg-amber-500/15 text-amber-500 border border-amber-500/30 text-xs font-black self-start sm:self-center">
                  Độ cát lợi: {selectedPalace.score}/100
                </div>
              </div>

              <p className={`text-xs sm:text-sm leading-relaxed mb-3 font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                {selectedPalace.interpretation}
              </p>

              <div className={`p-4 rounded-2xl border mb-3 ${subCardBg}`}>
                <div className="text-xs font-black mb-1.5">✨ Giải mã các sao trong cung {selectedPalace.palaceName}</div>
                <ul className="text-xs space-y-1">
                  {selectedPalace.keyHighlights.map((h, i) => (
                    <li key={i}>• {h}</li>
                  ))}
                </ul>
              </div>

              <div className={`text-xs font-semibold ${isDarkTheme ? "text-amber-300" : "text-amber-800"}`}>
                💡 <strong>Lời khuyên:</strong> {selectedPalace.advice}
              </div>
            </div>
          )}

          {/* NHỊP 3: LUẬN GIẢI CHUYÊN SÂU TOÀN LÁ SỐ */}
          {deepAnalysis && visibleAspectCount > 0 && (
            <div className={`p-5 sm:p-6 rounded-3xl border backdrop-blur-xl animate-in fade-in duration-300 ${cardBg}`}>
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
                <div>
                  <div className="flex items-center gap-2 flex-wrap">
                    <h3 className="text-base font-black flex items-center gap-2">
                      <Sparkles className="w-5 h-5 text-amber-500" />
                      {deepAnalysis.analysis.headline}
                    </h3>
                    <span
                      className={`text-[11px] font-bold px-2.5 py-0.5 rounded-full ${
                        deepAnalysis.source === "gemini-live"
                          ? "bg-emerald-500/20 text-emerald-500"
                          : "bg-amber-500/20 text-amber-500"
                      }`}
                    >
                      {deepAnalysis.source === "gemini-live" ? "Gemini AI" : "Hệ thống Tử Vi chuyên sâu"}
                    </span>
                  </div>
                  <p className={`text-xs mt-1 leading-relaxed ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                    {deepAnalysis.analysis.executiveSummary}
                  </p>
                </div>

                <button
                  type="button"
                  disabled={isRunning}
                  onClick={() => runCastChart(true)}
                  className={`px-3 py-1.5 rounded-xl text-[11px] font-bold border flex items-center gap-1.5 shrink-0 self-start sm:self-center transition-all ${
                    isDarkTheme
                      ? "bg-white/5 border-white/15 text-slate-200 hover:bg-white/10"
                      : "bg-slate-100 border-slate-200 text-slate-700 hover:bg-slate-200"
                  }`}
                >
                  <RefreshCw className={`w-3.5 h-3.5 ${isRunning ? "animate-spin" : ""}`} />
                  Luận lại lá số
                </button>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {deepAnalysis.analysis.aspects.slice(0, visibleAspectCount).map((asp) => (
                  <div
                    key={asp.id}
                    className={`p-4 rounded-2xl border animate-in fade-in slide-in-from-bottom-2 duration-300 ${subCardBg}`}
                  >
                    <div className="flex items-center justify-between gap-2 mb-2">
                      <h5 className="text-xs sm:text-sm font-black flex items-center gap-1.5">
                        <span>{asp.icon}</span>
                        {asp.title}
                      </h5>
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-amber-500/15 text-amber-500 shrink-0">
                        {asp.badge}
                      </span>
                    </div>
                    <p className={`text-xs mb-2 font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                      {asp.summary}
                    </p>
                    <ul className={`text-[11px] space-y-1 mb-2.5 ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                      {asp.details.map((d, idx) => (
                        <li key={idx}>• {d}</li>
                      ))}
                    </ul>
                    <div className={`text-[11px] pt-2 border-t ${isDarkTheme ? "border-white/10 text-amber-300" : "border-slate-200 text-amber-800"}`}>
                      <strong>Gợi ý:</strong> {asp.advice}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
