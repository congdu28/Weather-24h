"use client";

import React, { useEffect, useRef, useState } from "react";
import { Heart, Loader2, RefreshCw, Search, Sparkles, UserCheck, Users } from "lucide-react";
import { BIRTH_HOUR_OPTIONS, FIVE_ELEMENT_META, UserBirthProfile, elementLabel } from "../utils/lunarAlmanac";
import {
  CompatibilityStatus,
  DeepLoveAiAnalysis,
  LoveCompatibilityResult,
  LovePersonInput,
  evaluateLoveCompatibility
} from "../utils/loveCompatibility";
import {
  analyzeLoveCompatibilityWithGemini,
  getCachedLoveCompatibilityAi
} from "../services/geminiFortuneService";

interface LoveCompatibilityViewProps {
  isDarkTheme: boolean;
  userProfile: UserBirthProfile | null;
  geminiApiKey?: string;
}

const DEFAULT_PERSON_1: LovePersonInput = {
  name: "",
  gender: "male",
  day: 28,
  month: 4,
  year: 2002,
  birthHour: 3, // Giờ Mão
  isLunar: false
};

const DEFAULT_PERSON_2: LovePersonInput = {
  name: "",
  gender: "female",
  day: 15,
  month: 8,
  year: 2003,
  birthHour: 6, // Giờ Ngọ
  isLunar: false
};

const YEARS = Array.from({ length: 91 }, (_, i) => 2030 - i); // 2030 -> 1940
const MONTHS = Array.from({ length: 12 }, (_, i) => i + 1);
const DAYS = Array.from({ length: 31 }, (_, i) => i + 1);

type AnalysisPhase = "idle" | "pillars" | "dimensions" | "deep" | "done";

function statusBadgeClass(status: CompatibilityStatus, isDarkTheme: boolean): string {
  switch (status) {
    case "excellent":
      return isDarkTheme
        ? "bg-emerald-500/20 text-emerald-300 border-emerald-500/35"
        : "bg-emerald-100 text-emerald-800 border-emerald-300";
    case "good":
      return isDarkTheme
        ? "bg-sky-500/20 text-sky-300 border-sky-500/35"
        : "bg-sky-100 text-sky-800 border-sky-300";
    case "neutral":
      return isDarkTheme
        ? "bg-amber-500/20 text-amber-300 border-amber-500/35"
        : "bg-amber-100 text-amber-800 border-amber-300";
    case "caution":
      return isDarkTheme
        ? "bg-rose-500/20 text-rose-300 border-rose-500/35"
        : "bg-rose-100 text-rose-800 border-rose-300";
  }
}

function scoreBarColor(status: CompatibilityStatus): string {
  switch (status) {
    case "excellent":
      return "bg-emerald-500";
    case "good":
      return "bg-sky-500";
    case "neutral":
      return "bg-amber-500";
    case "caution":
      return "bg-rose-500";
  }
}

const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

export const LoveCompatibilityView: React.FC<LoveCompatibilityViewProps> = ({
  isDarkTheme,
  userProfile,
  geminiApiKey
}) => {
  const [person1, setPerson1] = useState<LovePersonInput>(DEFAULT_PERSON_1);
  const [person2, setPerson2] = useState<LovePersonInput>(DEFAULT_PERSON_2);
  const [result, setResult] = useState<LoveCompatibilityResult | null>(null);
  const [errorMsg, setErrorMsg] = useState<string>("");

  // State điều khiển chạy theo từng nhịp (progressive reveal)
  const [phase, setPhase] = useState<AnalysisPhase>("idle");
  const [statusText, setStatusText] = useState<string>("");
  const [showOverview, setShowOverview] = useState<boolean>(false);
  const [visibleDimCount, setVisibleDimCount] = useState<number>(0);
  const [deepAnalysis, setDeepAnalysis] = useState<{ analysis: DeepLoveAiAnalysis; source: "gemini-live" | "builtin" } | null>(null);
  const [visibleChapterCount, setVisibleChapterCount] = useState<number>(0);
  const runIdRef = useRef<number>(0);

  // Tự đồng bộ sẵn Người thứ 1 theo hồ sơ người dùng nếu có
  useEffect(() => {
    if (userProfile && userProfile.year >= 1900) {
      setPerson1((prev) => ({
        ...prev,
        gender: userProfile.gender,
        day: userProfile.day,
        month: userProfile.month,
        year: userProfile.year,
        birthHour: typeof userProfile.birthHour === "number" ? userProfile.birthHour : 6,
        isLunar: Boolean(userProfile.isLunar)
      }));
    }
  }, [userProfile]);

  const runProgressiveAnalysis = async (forceRefreshDeep = false) => {
    setErrorMsg("");
    let evaluated: LoveCompatibilityResult;
    try {
      evaluated = evaluateLoveCompatibility(person1, person2);
    } catch {
      setErrorMsg("Ngày tháng năm sinh không hợp lệ (ví dụ ngày 30/2 hoặc ngày âm lịch không tồn tại). Vui lòng kiểm tra lại.");
      return;
    }

    const currentRun = ++runIdRef.current;
    const isCancelled = () => runIdRef.current !== currentRun;

    // Reset hiển thị để chạy theo từng nhịp mượt mà
    setResult(evaluated);
    setShowOverview(false);
    setVisibleDimCount(0);
    setDeepAnalysis(null);
    setVisibleChapterCount(0);

    // Khởi chạy gọi AI / bộ phân tích chuyên sâu song song từ đầu
    const cached = !forceRefreshDeep ? getCachedLoveCompatibilityAi(evaluated) : null;
    const deepPromise = cached
      ? Promise.resolve({ analysis: cached, source: "gemini-live" as const })
      : analyzeLoveCompatibilityWithGemini(evaluated, geminiApiKey, forceRefreshDeep);

    // Nhịp 1: Lập Tứ Trụ & Cung Phi
    setPhase("pillars");
    setStatusText("Nhịp 1/3 • Đang lập Tứ Trụ Giờ - Ngày - Tháng - Năm & Cung Phi Bát Trạch...");
    await wait(320);
    if (isCancelled()) return;
    setShowOverview(true);

    // Nhịp 2: Đối chiếu & chấm điểm 6 phương diện tuần tự
    setPhase("dimensions");
    setStatusText("Nhịp 2/3 • Đang đối chiếu Ngũ Hành Nạp Âm, Can Chi, Nhật Chủ & Hỷ Dụng Thần...");
    for (let i = 1; i <= evaluated.dimensions.length; i++) {
      await wait(170);
      if (isCancelled()) return;
      setVisibleDimCount(i);
    }

    // Nhịp 3: Chờ kết quả luận giải chuyên sâu và xuất từng chương theo nhịp
    setPhase("deep");
    setStatusText("Nhịp 3/3 • Đang luận giải chuyên sâu Duyên phận, Hôn nhân, Tài lộc & Phong thủy...");
    const deepRes = await deepPromise;
    if (isCancelled()) return;
    setDeepAnalysis(deepRes);

    for (let j = 1; j <= deepRes.analysis.aspects.length; j++) {
      await wait(200);
      if (isCancelled()) return;
      setVisibleChapterCount(j);
    }

    setPhase("done");
    setStatusText("");
  };

  const applyMyProfileToPerson1 = () => {
    if (!userProfile) return;
    setPerson1((prev) => ({
      ...prev,
      gender: userProfile.gender,
      day: userProfile.day,
      month: userProfile.month,
      year: userProfile.year,
      birthHour: typeof userProfile.birthHour === "number" ? userProfile.birthHour : 6,
      isLunar: Boolean(userProfile.isLunar)
    }));
  };

  const isRunning = phase !== "idle" && phase !== "done";

  const cardBg = isDarkTheme
    ? "bg-slate-900/55 border-white/10 text-white"
    : "bg-white/90 border-slate-200/90 text-slate-900 shadow-md";

  const subCardBg = isDarkTheme
    ? "bg-white/5 border-white/10 text-slate-200"
    : "bg-slate-50/90 border-slate-200 text-slate-800";

  const inputClass = isDarkTheme
    ? "bg-slate-950/70 border-white/15 text-white focus:border-rose-400"
    : "bg-white border-slate-300 text-slate-900 focus:border-rose-500";

  const renderPersonForm = (
    title: string,
    accentClass: string,
    data: LovePersonInput,
    onChange: (updater: (prev: LovePersonInput) => LovePersonInput) => void,
    extraAction?: React.ReactNode
  ) => (
    <div className={`p-5 rounded-3xl border backdrop-blur-xl ${cardBg}`}>
      <div className="flex items-center justify-between gap-2 mb-4 flex-wrap">
        <div className="flex items-center gap-2.5">
          <span className={`w-8 h-8 rounded-xl flex items-center justify-center text-sm font-bold ${accentClass}`}>
            {data.gender === "male" ? "👨" : "👩"}
          </span>
          <div>
            <h3 className="text-sm font-black">{title}</h3>
            <p className={`text-[11px] ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
              Nhập đầy đủ giờ, ngày, tháng, năm sinh
            </p>
          </div>
        </div>
        {extraAction}
      </div>

      <div className="space-y-3.5">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div>
            <label className={`block text-[11px] font-bold mb-1 ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
              Tên / Biệt danh (tùy chọn)
            </label>
            <input
              type="text"
              value={data.name}
              onChange={(e) => onChange((prev) => ({ ...prev, name: e.target.value }))}
              placeholder={data.gender === "male" ? "Ví dụ: Anh Minh" : "Ví dụ: Bé Linh"}
              className={`w-full px-3 py-2 rounded-xl border text-xs font-semibold outline-none transition-colors ${inputClass}`}
            />
          </div>

          <div>
            <label className={`block text-[11px] font-bold mb-1 ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
              Giới tính
            </label>
            <div className="grid grid-cols-2 gap-2">
              {(["male", "female"] as const).map((g) => (
                <button
                  key={g}
                  type="button"
                  onClick={() => onChange((prev) => ({ ...prev, gender: g }))}
                  className={`py-2 px-3 rounded-xl text-xs font-bold border transition-all ${
                    data.gender === g
                      ? "bg-rose-500 text-white border-rose-500 shadow-sm"
                      : isDarkTheme
                      ? "bg-white/5 border-white/10 text-slate-300 hover:bg-white/10"
                      : "bg-slate-100 border-slate-200 text-slate-700 hover:bg-slate-200"
                  }`}
                >
                  {g === "male" ? "♂️ Nam" : "♀️ Nữ"}
                </button>
              ))}
            </div>
          </div>
        </div>

        <div className="flex items-center justify-between gap-2 flex-wrap">
          <span className={`text-[11px] font-bold ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
            Loại lịch ngày sinh
          </span>
          <div className="inline-flex rounded-xl p-0.5 border border-slate-500/20 bg-slate-500/10">
            {[
              { label: "Dương lịch", value: false },
              { label: "Âm lịch", value: true }
            ].map((opt) => (
              <button
                key={String(opt.value)}
                type="button"
                onClick={() => onChange((prev) => ({ ...prev, isLunar: opt.value }))}
                className={`px-3 py-1 rounded-lg text-[11px] font-bold transition-all ${
                  data.isLunar === opt.value
                    ? "bg-amber-500 text-slate-950 shadow-xs"
                    : isDarkTheme
                    ? "text-slate-300 hover:text-white"
                    : "text-slate-600 hover:text-slate-900"
                }`}
              >
                {opt.label}
              </button>
            ))}
          </div>
        </div>

        <div className="grid grid-cols-3 gap-2.5">
          <div>
            <label className={`block text-[11px] font-bold mb-1 ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
              Ngày
            </label>
            <select
              value={data.day}
              onChange={(e) => onChange((prev) => ({ ...prev, day: Number(e.target.value) }))}
              className={`w-full px-2.5 py-2 rounded-xl border text-xs font-bold outline-none ${inputClass}`}
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
              value={data.month}
              onChange={(e) => onChange((prev) => ({ ...prev, month: Number(e.target.value) }))}
              className={`w-full px-2.5 py-2 rounded-xl border text-xs font-bold outline-none ${inputClass}`}
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
              value={data.year}
              onChange={(e) => onChange((prev) => ({ ...prev, year: Number(e.target.value) }))}
              className={`w-full px-2.5 py-2 rounded-xl border text-xs font-bold outline-none ${inputClass}`}
            >
              {YEARS.map((y) => (
                <option key={y} value={y}>
                  {y}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div>
          <label className={`block text-[11px] font-bold mb-1 ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
            Giờ sinh (12 Địa Chi)
          </label>
          <select
            value={data.birthHour}
            onChange={(e) => onChange((prev) => ({ ...prev, birthHour: Number(e.target.value) }))}
            className={`w-full px-3 py-2 rounded-xl border text-xs font-bold outline-none ${inputClass}`}
          >
            <option value={-1}>Chưa rõ giờ sinh (lấy giờ Ngọ mặc định)</option>
            {BIRTH_HOUR_OPTIONS.map((opt) => (
              <option key={opt.chiIndex} value={opt.chiIndex}>
                {opt.emoji} Giờ {opt.chi} ({opt.range}) — {opt.periodLabel}
              </option>
            ))}
          </select>
        </div>
      </div>
    </div>
  );

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Header giới thiệu */}
      <div className={`p-5 rounded-3xl border backdrop-blur-xl ${cardBg}`}>
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-rose-500/20 text-rose-500 flex items-center justify-center shrink-0">
              <Heart className="w-6 h-6 fill-rose-500/30" />
            </div>
            <div>
              <h2 className="text-base sm:text-lg font-black">Bói Tình Duyên & Xem Tuổi Hợp Đôi</h2>
              <p className={`text-xs ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                Đối chiếu Tứ Trụ Bát Tự (Giờ • Ngày • Tháng • Năm), Hỷ Dụng Thần, Ngũ Hành Nạp Âm, Cung Phi Bát Trạch, Hoàng Đạo & Thần Số Học
              </p>
            </div>
          </div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-[11px] font-bold bg-rose-500/15 text-rose-500 border border-rose-500/25 self-start sm:self-center">
            <Users className="w-3.5 h-3.5" />
            Luận giải 2 người
          </div>
        </div>
      </div>

      {/* Form nhập 2 người */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        {renderPersonForm(
          "Thông tin Người thứ 1",
          "bg-sky-500/20 text-sky-500",
          person1,
          setPerson1,
          userProfile ? (
            <button
              type="button"
              onClick={applyMyProfileToPerson1}
              className={`px-2.5 py-1 rounded-xl text-[11px] font-bold border flex items-center gap-1 transition-all ${
                isDarkTheme
                  ? "bg-white/10 border-white/15 text-amber-300 hover:bg-white/15"
                  : "bg-amber-50 border-amber-200 text-amber-800 hover:bg-amber-100"
              }`}
            >
              <UserCheck className="w-3.5 h-3.5" />
              Lấy từ hồ sơ của tôi
            </button>
          ) : undefined
        )}

        {renderPersonForm(
          "Thông tin Người thứ 2",
          "bg-rose-500/20 text-rose-500",
          person2,
          setPerson2
        )}
      </div>

      {errorMsg && (
        <div className="p-3.5 rounded-2xl border border-rose-500/35 bg-rose-500/15 text-rose-500 text-xs font-bold text-center">
          {errorMsg}
        </div>
      )}

      {/* Nút Tìm kiếm & Luận chi tiết */}
      <div className="flex justify-center">
        <button
          type="button"
          disabled={isRunning}
          onClick={() => runProgressiveAnalysis(false)}
          className="px-6 py-3 rounded-2xl bg-gradient-to-r from-rose-500 to-amber-500 hover:from-rose-600 hover:to-amber-600 disabled:opacity-70 text-white font-black text-sm shadow-lg shadow-rose-500/25 flex items-center gap-2 transition-all active:scale-[0.98]"
        >
          {isRunning ? <Loader2 className="w-4 h-4 animate-spin" /> : <Search className="w-4 h-4" />}
          {isRunning ? "Đang Tìm Kiếm & Luận Giải..." : "Tìm Kiếm & Luận Giải Tình Duyên Chi Tiết"}
        </button>
      </div>

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

      {/* KẾT QUẢ LUẬN GIẢI THEO TỪNG NHỊP */}
      {result && (
        <div className="space-y-6">
          {/* NHỊP 1: Tổng quan điểm số & 2 lá số chi tiết */}
          {showOverview && (
            <div className={`p-6 rounded-3xl border backdrop-blur-xl animate-in fade-in slide-in-from-bottom-2 duration-300 ${cardBg}`}>
              <div className="flex flex-col md:flex-row items-center justify-between gap-6">
                <div className="flex-1 space-y-2 text-center md:text-left">
                  <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-black bg-rose-500/15 text-rose-500 border border-rose-500/25">
                    <Heart className="w-3.5 h-3.5 fill-rose-500" />
                    {result.verdictBadge}
                  </div>
                  <h3 className="text-xl sm:text-2xl font-black">{result.verdictTitle}</h3>
                  <p className={`text-xs sm:text-sm leading-relaxed ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                    {result.summary}
                  </p>
                </div>

                <div className="shrink-0 flex flex-col items-center justify-center w-32 h-32 rounded-3xl border-2 border-rose-500/30 bg-rose-500/10">
                  <span className="text-3xl font-black text-rose-500">{result.totalScore}</span>
                  <span className={`text-[11px] font-bold ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                    / 100 điểm
                  </span>
                </div>
              </div>

              {/* Tóm tắt thông tin chi tiết 2 người */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-6">
                {[result.person1, result.person2].map((p, idx) => {
                  const hyDung = p.fourPillars.hyDungThan.map((e) => elementLabel(e)).join(", ") || "Cân bằng";
                  return (
                    <div key={idx} className={`p-4 rounded-2xl border ${subCardBg}`}>
                      <div className="flex items-center justify-between gap-2 mb-2">
                        <span className="font-black text-sm">
                          {p.profile.gender === "male" ? "♂️" : "♀️"} {p.displayName} — {p.personalProfile.canChi} ({p.personalProfile.zodiacAnimal})
                        </span>
                        <span
                          className={`text-[11px] font-bold px-2 py-0.5 rounded-full ${
                            isDarkTheme ? "bg-white/10 text-amber-300" : "bg-amber-100 text-amber-800"
                          }`}
                        >
                          {FIVE_ELEMENT_META[p.personalProfile.element].emoji} {elementLabel(p.personalProfile.element)}
                        </span>
                      </div>
                      <div className={`text-[11px] space-y-1 ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                        <p>
                          • Dương lịch: <strong>{p.solarDate.day}/{p.solarDate.month}/{p.solarDate.year}</strong> • Âm lịch:{" "}
                          <strong>{p.lunarDate.day}/{p.lunarDate.month}/{p.lunarDate.year}</strong>
                        </p>
                        <p>
                          • Mệnh: <strong>{p.personalProfile.napAm}</strong> • Cung Phi: <strong>{p.cungPhi}</strong> ({p.cungPhiGroup})
                        </p>
                        <p>
                          • Tứ Trụ: Năm <strong>{p.fourPillars.yearPillar.canChi}</strong> • Tháng <strong>{p.fourPillars.monthPillar.canChi}</strong> • Ngày{" "}
                          <strong>{p.fourPillars.dayPillar.canChi}</strong> • Giờ{" "}
                          <strong>{p.fourPillars.hourPillar ? p.fourPillars.hourPillar.canChi : p.birthHourFortune.hourCanChi}</strong>
                        </p>
                        <p>
                          • Nhật Chủ: <strong>{p.fourPillars.nhatChuCan}</strong> ({p.fourPillars.nhatChuStrength}) • Hỷ Dụng Thần:{" "}
                          <strong>{hyDung}</strong>
                        </p>
                        <p>
                          • Hoàng Đạo: <strong>{p.westernZodiac.symbol} {p.westernZodiac.name}</strong> • Thần Số Học:{" "}
                          <strong>Số {p.numerology.lifePathNumber}</strong>
                        </p>
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* Bù trừ Ngũ Hành & Hỷ Dụng Thần */}
              <div
                className={`mt-4 p-4 rounded-2xl border ${
                  isDarkTheme ? "bg-emerald-500/10 border-emerald-500/25 text-emerald-200" : "bg-emerald-50 border-emerald-200 text-emerald-900"
                }`}
              >
                <div className="text-xs font-black mb-1.5">🌿 Bù trừ Ngũ Hành & Hỷ Dụng Thần trong Tứ Trụ</div>
                <ul className="text-xs space-y-1">
                  {result.complementaryElements.map((item, i) => (
                    <li key={i}>• {item}</li>
                  ))}
                </ul>
              </div>
            </div>
          )}

          {/* NHỊP 2: 6 Phương Diện Chấm Điểm Chi Tiết (xuất hiện từng nhịp) */}
          {visibleDimCount > 0 && (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {result.dimensions.slice(0, visibleDimCount).map((dim) => (
                <div
                  key={dim.id}
                  className={`p-5 rounded-3xl border backdrop-blur-xl flex flex-col justify-between animate-in fade-in slide-in-from-bottom-2 duration-300 ${cardBg}`}
                >
                  <div>
                    <div className="flex items-center justify-between gap-2 mb-2.5">
                      <h4 className="text-sm font-black flex items-center gap-2">
                        <span>{dim.icon}</span>
                        {dim.title}
                      </h4>
                      <span
                        className={`px-2.5 py-0.5 rounded-full text-[11px] font-bold border shrink-0 ${statusBadgeClass(
                          dim.status,
                          isDarkTheme
                        )}`}
                      >
                        {dim.badge} • {dim.score}/{dim.maxScore}
                      </span>
                    </div>

                    <div className={`w-full h-1.5 rounded-full mb-3 overflow-hidden ${isDarkTheme ? "bg-white/10" : "bg-slate-200"}`}>
                      <div
                        className={`h-full rounded-full transition-all duration-500 ${scoreBarColor(dim.status)}`}
                        style={{ width: `${Math.round((dim.score / dim.maxScore) * 100)}%` }}
                      />
                    </div>

                    <p className={`text-xs leading-relaxed mb-3 font-medium ${isDarkTheme ? "text-slate-200" : "text-slate-700"}`}>
                      {dim.summary}
                    </p>

                    <ul className={`text-[11px] space-y-1 mb-3 ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                      {dim.details.map((d, i) => (
                        <li key={i}>• {d}</li>
                      ))}
                    </ul>
                  </div>

                  <div className={`p-3 rounded-2xl text-[11px] border ${subCardBg}`}>
                    <strong>💡 Lời khuyên:</strong> {dim.advice}
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* NHỊP 3: Luận Giải Chuyên Sâu 6 Chương (tự động chạy sau Nhịp 2) */}
          {deepAnalysis && visibleChapterCount > 0 && (
            <div className={`p-6 rounded-3xl border backdrop-blur-xl animate-in fade-in duration-300 ${cardBg}`}>
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
                <div>
                  <div className="flex items-center gap-2 flex-wrap">
                    <h3 className="text-base font-black flex items-center gap-2">
                      <Sparkles className="w-5 h-5 text-amber-500" />
                      Luận Giải Chuyên Sâu Hôn Nhân & Vận Duyên
                    </h3>
                    <span
                      className={`text-[11px] font-bold px-2.5 py-0.5 rounded-full ${
                        deepAnalysis.source === "gemini-live"
                          ? "bg-emerald-500/20 text-emerald-500"
                          : "bg-amber-500/20 text-amber-500"
                      }`}
                    >
                      {deepAnalysis.source === "gemini-live" ? "Gemini AI" : "Hệ thống Bát Tự chuyên sâu"}
                    </span>
                  </div>
                  <p className={`text-xs mt-1 ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                    {deepAnalysis.analysis.executiveSummary}
                  </p>
                </div>

                <button
                  type="button"
                  disabled={isRunning}
                  onClick={() => runProgressiveAnalysis(true)}
                  className={`px-3 py-1.5 rounded-xl text-[11px] font-bold border flex items-center gap-1.5 shrink-0 self-start sm:self-center transition-all ${
                    isDarkTheme
                      ? "bg-white/5 border-white/15 text-slate-200 hover:bg-white/10"
                      : "bg-slate-100 border-slate-200 text-slate-700 hover:bg-slate-200"
                  }`}
                >
                  <RefreshCw className={`w-3.5 h-3.5 ${isRunning ? "animate-spin" : ""}`} />
                  Phân tích lại
                </button>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {deepAnalysis.analysis.aspects.slice(0, visibleChapterCount).map((asp) => (
                  <div
                    key={asp.id}
                    className={`p-4 rounded-2xl border animate-in fade-in slide-in-from-bottom-2 duration-300 ${subCardBg}`}
                  >
                    <div className="flex items-center justify-between gap-2 mb-2">
                      <h5 className="text-xs sm:text-sm font-black flex items-center gap-1.5">
                        <span>{asp.icon}</span>
                        {asp.title}
                      </h5>
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-rose-500/15 text-rose-500 shrink-0">
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

          {/* Bí quyết phong thủy tổng hợp (xuất hiện khi hoàn tất tất cả nhịp) */}
          {phase === "done" && (
            <div className={`p-5 rounded-3xl border backdrop-blur-xl animate-in fade-in duration-300 ${cardBg}`}>
              <h3 className="text-sm font-black mb-2.5">🧭 Bí Quyết Phong Thủy & Giữ Lửa Hôn Nhân</h3>
              <ul className={`text-xs space-y-1.5 ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                {result.fengShuiAdvice.map((tip, i) => (
                  <li key={i}>• {tip}</li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
