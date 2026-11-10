"use client";

import React, { useMemo } from "react";
import { CurrentWeather, DailyForecast } from "../types/weather";
import { getTodayLunar, LunarDate } from "../utils/lunarCalendar";
import {
  Sunrise,
  Sunset,
  Sun,
  Moon,
  Camera,
  Compass,
  Sparkles
} from "lucide-react";

interface Props {
  current: CurrentWeather;
  todayForecast?: DailyForecast;
  isDarkTheme?: boolean;
}

export default function AstronomyArcCard({
  current,
  todayForecast,
  isDarkTheme = true
}: Props) {
  const lunarDate: LunarDate = useMemo(() => getTodayLunar(), []);

  // 1. Phân tích Mặt Trời & Bình minh / Hoàng hôn
  const solarData = useMemo(() => {
    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();

    let riseMinutes = 5 * 60 + 50; // 05:50 mặc định
    let setMinutes = 17 * 60 + 35; // 17:35 mặc định
    let riseStr = "05:50";
    let setStr = "17:35";

    if (todayForecast?.sunrise && todayForecast?.sunset) {
      const riseDate = new Date(todayForecast.sunrise);
      const setDate = new Date(todayForecast.sunset);
      if (!isNaN(riseDate.getTime()) && !isNaN(setDate.getTime())) {
        riseMinutes = riseDate.getHours() * 60 + riseDate.getMinutes();
        setMinutes = setDate.getHours() * 60 + setDate.getMinutes();
        riseStr = `${riseDate.getHours().toString().padStart(2, "0")}:${riseDate.getMinutes().toString().padStart(2, "0")}`;
        setStr = `${setDate.getHours().toString().padStart(2, "0")}:${setDate.getMinutes().toString().padStart(2, "0")}`;
      }
    }

    const dayLengthMinutes = Math.max(60, setMinutes - riseMinutes);
    const dayLengthHours = Math.floor(dayLengthMinutes / 60);
    const dayLengthRemainMins = dayLengthMinutes % 60;

    const isDay = current ? current.isDay : (currentMinutes >= riseMinutes && currentMinutes <= setMinutes);

    let progress = 0;
    let statusText = "";
    let timeRemainingText = "";

    if (isDay) {
      progress = (currentMinutes - riseMinutes) / dayLengthMinutes;
      const minsToSunset = setMinutes - currentMinutes;
      const hLeft = Math.floor(minsToSunset / 60);
      const mLeft = minsToSunset % 60;
      statusText = "Mặt trời đang chiếu sáng";
      timeRemainingText = `Còn ${hLeft}h ${mLeft}m nữa đến hoàng hôn`;
    } else {
      if (currentMinutes < riseMinutes) {
        const minsToRise = riseMinutes - currentMinutes;
        const hLeft = Math.floor(minsToRise / 60);
        const mLeft = minsToRise % 60;
        statusText = "Đêm tĩnh lặng • Chờ bình minh";
        timeRemainingText = `Bình minh sau ${hLeft}h ${mLeft}m`;
        progress = 0;
      } else {
        const minsToMidnight = 1440 - currentMinutes;
        const totalMinsToRise = minsToMidnight + riseMinutes;
        const hLeft = Math.floor(totalMinsToRise / 60);
        const mLeft = totalMinsToRise % 60;
        statusText = "Mặt trời đã lặn";
        timeRemainingText = `Bình minh tiếp theo sau ${hLeft}h ${mLeft}m`;
        progress = 1;
      }
    }

    // Tọa độ biểu đồ vòng cung SVG (Width: 320, Height: 150)
    // Cung elip từ (30, 130) qua đỉnh (160, 25) tới (290, 130)
    const arcRadiusX = 130;
    const arcRadiusY = 100;
    const centerX = 160;
    const centerY = 130;

    // Angle theta: PI (180deg - Sunrise) đến 0 (0deg - Sunset)
    const theta = Math.PI - progress * Math.PI;
    const sunX = centerX + arcRadiusX * Math.cos(theta);
    const sunY = centerY - arcRadiusY * Math.sin(theta);

    // Kiểm tra Giờ Vàng (Golden Hour) & Giờ Xanh (Blue Hour)
    const isMorningGolden = currentMinutes >= riseMinutes && currentMinutes <= riseMinutes + 60;
    const isEveningGolden = currentMinutes >= setMinutes - 60 && currentMinutes <= setMinutes;
    const isGoldenHour = isMorningGolden || isEveningGolden;

    const isMorningBlue = currentMinutes >= riseMinutes - 35 && currentMinutes < riseMinutes;
    const isEveningBlue = currentMinutes > setMinutes && currentMinutes <= setMinutes + 35;
    const isBlueHour = isMorningBlue || isEveningBlue;

    return {
      riseStr,
      setStr,
      dayLengthHours,
      dayLengthRemainMins,
      isDay,
      progress,
      statusText,
      timeRemainingText,
      sunX,
      sunY,
      isGoldenHour,
      isBlueHour
    };
  }, [current, todayForecast]);

  // 2. Phân tích Tuần Trăng & Âm Lịch Thiên Văn
  const moonData = useMemo(() => {
    const d = lunarDate.day;
    let phaseName = "Trăng Non (Sóc)";
    let phaseDesc = "Mặt trăng ở giữa Trái Đất và Mặt Trời, mặt tối quay về Trái Đất";
    let illumination = 0;

    if (d === 1) {
      phaseName = "Trăng Non (Sóc)";
      phaseDesc = "Mặt trăng vô hình hoặc lưỡi liềm siêu mỏng";
      illumination = 1;
    } else if (d >= 2 && d <= 6) {
      phaseName = "Trăng Lưỡi Liềm Đầu Tháng (Waxing Crescent)";
      phaseDesc = "Ánh trăng dần xuất hiện ở đường chân trời phía Tây sau hoàng hôn";
      illumination = Math.round((d / 15) * 50);
    } else if (d === 7 || d === 8) {
      phaseName = "Bán Nguyệt Thượng Huyền (First Quarter)";
      phaseDesc = "Nửa vầng trăng sáng bên phải, mọc lúc chính ngọ và lặn lúc nửa đêm";
      illumination = 50;
    } else if (d >= 9 && d <= 14) {
      phaseName = "Trăng Khuyết Đầu Tháng (Waxing Gibbous)";
      phaseDesc = "Mặt trăng ngày càng tròn đầy, sáng rực rỡ suốt buổi tối";
      illumination = Math.round(50 + ((d - 8) / 7) * 48);
    } else if (d === 15 || d === 16) {
      phaseName = "Trăng Tròn (Vọng / Ngày Rằm)";
      phaseDesc = "Mặt trăng đối diện trọn vẹn với mặt trời, tỏa sáng viên mãn suốt đêm";
      illumination = 100;
    } else if (d >= 17 && d <= 21) {
      phaseName = "Trăng Khuyết Cuối Tháng (Waning Gibbous)";
      phaseDesc = "Độ sáng bắt đầu giảm dần, mọc muộn hơn sau khi trời tối hẳn";
      illumination = Math.round(100 - ((d - 16) / 6) * 48);
    } else if (d === 22 || d === 23) {
      phaseName = "Bán Nguyệt Hạ Huyền (Last Quarter)";
      phaseDesc = "Nửa vầng trăng sáng bên trái, mọc lúc nửa đêm và lặn lúc giữa trưa";
      illumination = 50;
    } else {
      phaseName = "Trăng Lưỡi Liềm Cuối Tháng (Waning Crescent)";
      phaseDesc = "Vầng trăng mảnh mọc vào rạng đông trước khi mặt trời mọc";
      illumination = Math.max(2, Math.round(50 - ((d - 23) / 7) * 48));
    }

    return {
      day: d,
      phaseName,
      phaseDesc,
      illumination,
      isFullMoon: d === 15 || d === 16,
      isNewMoon: d === 1
    };
  }, [lunarDate]);

  return (
    <div className="weather-card glass-specular p-4 sm:p-6 lg:p-7">
      {/* Header */}
      <div className="flex items-center justify-between pb-3 sm:pb-4 border-b border-black/5 dark:border-white/10">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-xl bg-amber-500/20 text-amber-500 border border-amber-500/30">
            <Compass className="w-4 h-4 sm:w-5 sm:h-5" />
          </div>
          <div>
            <h3 className={`text-base font-extrabold flex items-center gap-1.5 ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
              Quỹ đạo Mặt Trời & Tuần Trăng
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-amber-500/15 text-amber-600 dark:text-amber-400 font-bold border border-amber-500/30">
                Thiên Văn Trực Quan
              </span>
            </h3>
            <p className={`text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-500 font-medium"}`}>
              Theo dõi quỹ đạo nhật nguyệt, giờ vàng nhiếp ảnh & độ sáng trăng
            </p>
          </div>
        </div>

        {solarData.isGoldenHour && (
          <div className="hidden sm:flex items-center gap-1.5 px-3 py-1 rounded-full bg-amber-500/20 border border-amber-500/40 text-amber-500 text-xs font-bold animate-pulse">
            <Camera className="w-3.5 h-3.5" />
            <span>Giờ Vàng (Golden Hour)</span>
          </div>
        )}
      </div>

      {/* Main Grid: Solar Arc (Left) + Moon Phase (Right) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5 sm:gap-6 mt-4">
        {/* 1. SOLAR TRAJECTORY ARC (7 Cols) */}
        <div className="lg:col-span-7 flex flex-col justify-between p-4 sm:p-5 rounded-2xl bg-black/5 dark:bg-white/5 border border-black/5 dark:border-white/10">
          <div className="flex items-center justify-between">
            <span className={`text-xs font-bold flex items-center gap-1.5 ${isDarkTheme ? "text-amber-300" : "text-amber-700"}`}>
              <Sun className="w-4 h-4 text-amber-500 animate-spin-slow" />
              Độ Cao Mặt Trời & Độ Dài Ban Ngày
            </span>
            <span className={`text-[11px] font-semibold px-2.5 py-0.5 rounded-full ${
              solarData.isDay
                ? isDarkTheme ? "bg-amber-500/20 text-amber-300 border border-amber-500/30" : "bg-amber-100 text-amber-800"
                : isDarkTheme ? "bg-indigo-500/20 text-indigo-300 border border-indigo-500/30" : "bg-indigo-100 text-indigo-800"
            }`}>
              {solarData.statusText}
            </span>
          </div>

          {/* SVG Sun Arc */}
          <div className="relative w-full max-w-[340px] mx-auto my-2">
            <svg viewBox="0 0 320 150" className="w-full h-auto overflow-visible">
              <defs>
                <linearGradient id="solarPathGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                  <stop offset="0%" stopColor="#f59e0b" stopOpacity="0.4" />
                  <stop offset="50%" stopColor="#fbbf24" stopOpacity="0.9" />
                  <stop offset="100%" stopColor="#f97316" stopOpacity="0.4" />
                </linearGradient>
                <filter id="sunGlow" x="-30%" y="-30%" width="160%" height="160%">
                  <feGaussianBlur stdDeviation="5" result="blur" />
                  <feMerge>
                    <feMergeNode in="blur" />
                    <feMergeNode in="SourceGraphic" />
                  </feMerge>
                </filter>
              </defs>

              {/* Đường chân trời (Horizon) */}
              <line
                x1="15"
                y1="130"
                x2="305"
                y2="130"
                stroke={isDarkTheme ? "rgba(255,255,255,0.15)" : "rgba(0,0,0,0.12)"}
                strokeWidth="1.5"
                strokeDasharray="4 4"
              />

              {/* Vòng cung quỹ đạo mặt trời */}
              <path
                d="M 30,130 A 130,100 0 0,1 290,130"
                fill="none"
                stroke={isDarkTheme ? "rgba(255,255,255,0.15)" : "rgba(0,0,0,0.1)"}
                strokeWidth="2.5"
                strokeDasharray="6 5"
              />

              {/* Phần cung đã di chuyển (đã trôi qua) */}
              {solarData.isDay && (
                <path
                  d={`M 30,130 A 130,100 0 0,1 ${solarData.sunX},${solarData.sunY}`}
                  fill="none"
                  stroke="url(#solarPathGrad)"
                  strokeWidth="3.5"
                  strokeLinecap="round"
                />
              )}

              {/* Biểu tượng Bình minh & Hoàng hôn ở hai đầu */}
              <g transform="translate(18, 136)">
                <text
                  x="0"
                  y="0"
                  textAnchor="middle"
                  className="text-[10px] font-bold fill-amber-500"
                >
                  Đông (Bình minh)
                </text>
              </g>

              <g transform="translate(302, 136)">
                <text
                  x="0"
                  y="0"
                  textAnchor="middle"
                  className="text-[10px] font-bold fill-orange-500"
                >
                  Tây (Hoàng hôn)
                </text>
              </g>

              {/* Chấm Mặt Trời phát sáng */}
              {solarData.isDay ? (
                <g transform={`translate(${solarData.sunX}, ${solarData.sunY})`} filter="url(#sunGlow)">
                  <circle r="12" fill="#fbbf24" fillOpacity="0.3" className="animate-ping" />
                  <circle r="9" fill="#f59e0b" stroke="#ffffff" strokeWidth="2" />
                  <circle r="5" fill="#fef08a" />
                </g>
              ) : (
                <g transform="translate(160, 130)">
                  <circle r="7" fill={isDarkTheme ? "#6366f1" : "#818cf8"} stroke="#ffffff" strokeWidth="1.5" />
                  <text
                    x="0"
                    y="-14"
                    textAnchor="middle"
                    className="text-[10px] font-bold fill-indigo-400"
                  >
                    Đêm
                  </text>
                </g>
              )}
            </svg>
          </div>

          {/* Bottom Info: Sunrise vs Sunset */}
          <div className="flex items-center justify-between pt-2 border-t border-black/5 dark:border-white/10 text-xs">
            <div className="flex items-center gap-2">
              <div className="p-1.5 rounded-lg bg-amber-500/20 text-amber-500">
                <Sunrise className="w-4 h-4" />
              </div>
              <div>
                <div className={`text-[10px] ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>Bình minh</div>
                <div className={`font-extrabold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>{solarData.riseStr}</div>
              </div>
            </div>

            <div className="text-center">
              <div className={`text-[10px] ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>Thời gian chiếu sáng</div>
              <div className={`font-bold ${isDarkTheme ? "text-amber-300" : "text-amber-800"}`}>
                {solarData.dayLengthHours}h {solarData.dayLengthRemainMins}m
              </div>
              <div className={`text-[9px] mt-0.5 ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>
                {solarData.timeRemainingText}
              </div>
            </div>

            <div className="flex items-center gap-2 text-right">
              <div>
                <div className={`text-[10px] ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>Hoàng hôn</div>
                <div className={`font-extrabold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>{solarData.setStr}</div>
              </div>
              <div className="p-1.5 rounded-lg bg-orange-500/20 text-orange-500">
                <Sunset className="w-4 h-4" />
              </div>
            </div>
          </div>
        </div>

        {/* 2. MOON PHASE & NIGHT SKY (5 Cols) */}
        <div className="lg:col-span-5 flex flex-col justify-between p-4 sm:p-5 rounded-2xl bg-black/5 dark:bg-white/5 border border-black/5 dark:border-white/10">
          <div className="flex items-center justify-between">
            <span className={`text-xs font-bold flex items-center gap-1.5 ${isDarkTheme ? "text-indigo-300" : "text-indigo-700"}`}>
              <Moon className="w-4 h-4 text-indigo-400" />
              Tuần Trăng Thiên Văn
            </span>
            <span className="text-[11px] font-bold text-indigo-400 bg-indigo-500/15 px-2 py-0.5 rounded-full border border-indigo-500/30">
              Độ sáng {moonData.illumination}%
            </span>
          </div>

          {/* Visual Moon Rendering */}
          <div className="flex items-center gap-4 my-3">
            {/* Glowing Moon Graphic */}
            <div className="relative w-20 h-20 sm:w-22 sm:h-22 rounded-full shrink-0 flex items-center justify-center">
              {/* Outer Moonlight Aura */}
              <div
                className="absolute inset-0 rounded-full blur-xl pointer-events-none"
                style={{
                  backgroundColor: moonData.isFullMoon ? "rgba(254, 240, 138, 0.45)" : "rgba(165, 180, 252, 0.35)"
                }}
              />

              {/* Moon Sphere */}
              <div
                className="relative w-16 h-16 sm:w-18 sm:h-18 rounded-full overflow-hidden shadow-2xl border border-white/30"
                style={{
                  background: "radial-gradient(circle at 35% 35%, #f8fafc 0%, #cbd5e1 50%, #475569 100%)"
                }}
              >
                {/* Moon craters texture */}
                <div className="absolute top-2 left-4 w-3.5 h-3.5 rounded-full bg-slate-500/25 blur-[0.5px]" />
                <div className="absolute top-7 left-3 w-5 h-5 rounded-full bg-slate-600/30 blur-[0.5px]" />
                <div className="absolute bottom-3 right-4 w-4 h-4 rounded-full bg-slate-500/30 blur-[0.5px]" />
                <div className="absolute top-5 right-3 w-2.5 h-2.5 rounded-full bg-slate-400/30 blur-[0.5px]" />

                {/* Dark Shadow Overlay simulating phase */}
                {moonData.illumination < 95 && (
                  <div
                    className="absolute inset-0 bg-slate-950/80 transition-all pointer-events-none"
                    style={{
                      clipPath: moonData.day <= 15
                        ? `inset(0 ${moonData.illumination}% 0 0)`
                        : `inset(0 0 0 ${100 - moonData.illumination}%)`
                    }}
                  />
                )}
              </div>
            </div>

            {/* Moon Phase Details */}
            <div className="flex-1 min-w-0">
              <div className={`text-sm font-black truncate ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                {moonData.phaseName}
              </div>
              <div className="text-[11px] font-semibold text-amber-500 dark:text-amber-400 mt-0.5">
                Ngày {moonData.day} Âm lịch • {lunarDate.day === 15 ? "Đêm Trăng Rằm viên mãn" : `Tuổi trăng: ${moonData.day}/30 ngày`}
              </div>
              <p className={`text-[11px] mt-1 line-clamp-2 leading-relaxed ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>
                {moonData.phaseDesc}
              </p>
            </div>
          </div>

          {/* Quick Badges: Golden Hour & Blue Hour times */}
          <div className="pt-2 border-t border-black/5 dark:border-white/10 grid grid-cols-2 gap-2 text-xs">
            <div className={`p-2 rounded-xl flex items-center gap-2 border ${
              isDarkTheme ? "bg-white/5 border-white/10" : "bg-amber-50/80 border-amber-200/80"
            }`}>
              <Camera className="w-3.5 h-3.5 text-amber-500 shrink-0" />
              <div className="truncate">
                <div className={`text-[10px] font-medium ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>Giờ Vàng (Ảnh đẹp)</div>
                <div className={`font-bold ${isDarkTheme ? "text-amber-300" : "text-amber-800"}`}>
                  {solarData.setStr.split(":")[0] ? `${Number(solarData.setStr.split(":")[0]) - 1}:${solarData.setStr.split(":")[1]} - ${solarData.setStr}` : "16:30 - 17:30"}
                </div>
              </div>
            </div>

            <div className={`p-2 rounded-xl flex items-center gap-2 border ${
              isDarkTheme ? "bg-white/5 border-white/10" : "bg-sky-50/80 border-sky-200/80"
            }`}>
              <Sparkles className="w-3.5 h-3.5 text-sky-400 shrink-0" />
              <div className="truncate">
                <div className={`text-[10px] font-medium ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>Giờ Xanh (Blue Hour)</div>
                <div className={`font-bold ${isDarkTheme ? "text-sky-300" : "text-sky-800"}`}>
                  {solarData.setStr} - {solarData.setStr.split(":")[0] ? `${solarData.setStr.split(":")[0]}:${(Number(solarData.setStr.split(":")[1]) + 30) % 60}` : "17:35 - 18:05"}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
