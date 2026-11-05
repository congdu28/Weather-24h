"use client";

import React from "react";
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
  const cond = getWeatherCondition(weatherCode, isDay);

  // Vibrant, rich sky gradients matching real-world meteorology
  let gradient = cond.gradient;
  if (!isDarkTheme) {
    switch (cond.type) {
      case "sunny":
        gradient = isDay
          ? "from-sky-500 via-sky-300 to-amber-100"
          : "from-slate-900 via-indigo-950 to-blue-950";
        break;
      case "cloudy":
        gradient = isDay
          ? "from-sky-700/80 via-slate-400 to-slate-200"
          : "from-slate-950 via-slate-900 to-blue-950";
        break;
      case "rainy":
        gradient = isDay
          ? "from-slate-700 via-blue-600/70 to-slate-300"
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
          ? "from-sky-500 via-blue-300 to-slate-200"
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
    <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden transition-all duration-1000">
      {/* 1. Base vibrant sky background */}
      <div className={`absolute inset-0 bg-gradient-to-b ${gradient} transition-colors duration-1000`} />

      {/* 2. Dynamic ambient sun/moon glow orbs */}
      {cond.type === "sunny" && isDay && (
        <>
          <div
            className={`absolute -top-24 right-1/4 w-[600px] h-[600px] rounded-full blur-3xl animate-pulse ${
              isDarkTheme ? "bg-amber-400/25" : "bg-amber-400/50"
            }`}
          />
          <div
            className={`absolute top-0 right-10 w-[400px] h-[400px] rounded-full blur-2xl ${
              isDarkTheme ? "bg-yellow-500/20" : "bg-amber-300/40"
            }`}
          />
        </>
      )}

      {cond.type === "sunny" && !isDay && (
        <>
          <div className="absolute top-12 right-20 w-56 h-56 rounded-full bg-blue-200/20 blur-3xl animate-pulse" />
          <div className="absolute inset-0 opacity-40 bg-[radial-gradient(#fff_1px,transparent_1px)] [background-size:28px_28px]" />
        </>
      )}

      {cond.type === "cloudy" && (
        <>
          <div
            className={`absolute -top-20 left-10 w-[700px] h-[400px] rounded-full blur-3xl ${
              isDarkTheme ? "bg-slate-700/30" : "bg-white/40"
            }`}
          />
          <div
            className={`absolute top-1/4 right-0 w-[600px] h-[350px] rounded-full blur-3xl ${
              isDarkTheme ? "bg-sky-900/30" : "bg-sky-200/50"
            }`}
          />
        </>
      )}

      {cond.type === "rainy" && (
        <>
          <div
            className={`absolute -top-20 left-1/4 w-[500px] h-[500px] rounded-full blur-3xl ${
              isDarkTheme ? "bg-blue-600/25" : "bg-blue-500/35"
            }`}
          />
          <div
            className={`absolute top-1/2 right-10 w-[600px] h-[600px] rounded-full blur-3xl ${
              isDarkTheme ? "bg-cyan-800/25" : "bg-cyan-600/35"
            }`}
          />
        </>
      )}

      {cond.type === "thunder" && (
        <>
          <div className="absolute top-10 left-1/3 w-[600px] h-[600px] rounded-full bg-purple-600/30 blur-3xl animate-pulse" />
          <div className="absolute top-1/3 right-1/4 w-[500px] h-[500px] rounded-full bg-indigo-600/30 blur-3xl" />
        </>
      )}

      {/* 3. Smooth bottom vignette gradient */}
      <div
        className={`absolute inset-0 transition-colors pointer-events-none ${
          isDarkTheme ? "bg-gradient-to-t from-slate-950/80 via-transparent to-transparent" : "bg-gradient-to-t from-slate-900/10 via-transparent to-transparent"
        }`}
      />
    </div>
  );
}
