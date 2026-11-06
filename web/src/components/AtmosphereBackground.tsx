"use client";

import React, { useMemo } from "react";
import { getWeatherCondition } from "../services/weatherApi";

interface Props {
  weatherCode: number;
  isDay: boolean;
  isDarkTheme?: boolean;
}

export default function AtmosphereBackground({
  weatherCode,
  isDay,
  isDarkTheme = true
}: Props) {
  const cond = useMemo(() => getWeatherCondition(weatherCode, isDay), [weatherCode, isDay]);

  // Vibrant, rich sky gradients matching real-world meteorology
  let gradient = cond.gradient;
  if (!isDarkTheme) {
    switch (cond.type) {
      case "sunny":
        gradient = isDay
          ? "from-sky-400 via-sky-300 to-amber-100"
          : "from-slate-900 via-indigo-950 to-blue-950";
        break;
      case "cloudy":
        gradient = isDay
          ? "from-sky-600 via-slate-300 to-slate-200"
          : "from-slate-950 via-slate-900 to-blue-950";
        break;
      case "rainy":
        gradient = isDay
          ? "from-slate-600 via-blue-500/70 to-slate-200"
          : "from-slate-950 via-blue-950 to-slate-900";
        break;
      case "thunder":
        gradient = isDay
          ? "from-slate-800 via-purple-700/80 to-indigo-950"
          : "from-purple-950 via-slate-950 to-black";
        break;
      case "fog":
        gradient = isDay
          ? "from-slate-400 via-zinc-300 to-sky-200"
          : "from-zinc-900 via-slate-900 to-zinc-950";
        break;
      default:
        gradient = isDay
          ? "from-sky-400 via-blue-200 to-slate-100"
          : "from-slate-950 via-indigo-950 to-slate-900";
    }
  } else {
    // Dark theme rich atmospheric depths
    switch (cond.type) {
      case "sunny":
        gradient = isDay
          ? "from-sky-950 via-slate-900 to-amber-950/40"
          : "from-slate-950 via-indigo-950 to-blue-950";
        break;
      case "cloudy":
        gradient = isDay
          ? "from-slate-900 via-slate-800 to-sky-950/60"
          : "from-slate-950 via-slate-900 to-black";
        break;
      case "rainy":
        gradient = "from-slate-950 via-blue-950 to-slate-900";
        break;
      case "thunder":
        gradient = "from-purple-950 via-slate-950 to-black";
        break;
      case "fog":
        gradient = "from-zinc-900 via-slate-900 to-zinc-950";
        break;
      default:
        gradient = "from-slate-950 via-slate-900 to-slate-950";
    }
  }

  return (
    <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden transform-gpu">
      {/* 1. Base vibrant sky background (100% GPU cached layer) */}
      <div className={`absolute inset-0 bg-gradient-to-b ${gradient}`} />

      {/* 2. Static radial solar/lunar ambient light (No GPU-costly blur-3xl animations) */}
      {cond.type === "sunny" && isDay && (
        <div
          className="absolute -top-20 right-1/4 w-[500px] h-[500px] rounded-full pointer-events-none"
          style={{
            background: isDarkTheme
              ? "radial-gradient(circle, rgba(251, 191, 36, 0.22) 0%, rgba(245, 158, 11, 0.08) 50%, transparent 75%)"
              : "radial-gradient(circle, rgba(251, 191, 36, 0.45) 0%, rgba(245, 158, 11, 0.15) 50%, transparent 75%)"
          }}
        />
      )}

      {cond.type === "sunny" && !isDay && (
        <div
          className="absolute top-10 right-20 w-64 h-64 rounded-full pointer-events-none"
          style={{
            background: "radial-gradient(circle, rgba(224, 242, 254, 0.20) 0%, rgba(186, 230, 253, 0.05) 60%, transparent 75%)"
          }}
        />
      )}

      {cond.type === "cloudy" && (
        <div
          className="absolute top-0 left-10 w-[600px] h-[350px] rounded-full pointer-events-none"
          style={{
            background: isDarkTheme
              ? "radial-gradient(ellipse, rgba(51, 65, 85, 0.25) 0%, transparent 70%)"
              : "radial-gradient(ellipse, rgba(255, 255, 255, 0.4) 0%, transparent 70%)"
          }}
        />
      )}

      {cond.type === "rainy" && (
        <div
          className="absolute top-0 left-1/4 w-[600px] h-[450px] rounded-full pointer-events-none"
          style={{
            background: isDarkTheme
              ? "radial-gradient(ellipse, rgba(30, 58, 138, 0.25) 0%, transparent 70%)"
              : "radial-gradient(ellipse, rgba(59, 130, 246, 0.20) 0%, transparent 70%)"
          }}
        />
      )}

      {/* 3. Smooth bottom vignette gradient */}
      <div
        className={`absolute inset-0 pointer-events-none ${
          isDarkTheme ? "bg-gradient-to-t from-slate-950/80 via-transparent to-transparent" : "bg-gradient-to-t from-slate-900/10 via-transparent to-transparent"
        }`}
      />
    </div>
  );
}
