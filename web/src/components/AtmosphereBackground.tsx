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

  // Dynamic sky gradients based on weather and light/dark theme
  let gradient = cond.gradient;
  if (!isDarkTheme) {
    switch (cond.type) {
      case "sunny":
        gradient = isDay
          ? "from-sky-400 via-sky-200 to-amber-100"
          : "from-indigo-900 via-blue-900 to-slate-800";
        break;
      case "rainy":
        gradient = "from-slate-400 via-blue-200 to-sky-100";
        break;
      case "thunder":
        gradient = "from-slate-600 via-purple-300 to-blue-200";
        break;
      case "cloudy":
        gradient = "from-slate-300 via-sky-200 to-blue-100";
        break;
      case "fog":
        gradient = "from-zinc-300 via-slate-200 to-sky-100";
        break;
      default:
        gradient = "from-sky-300 via-blue-100 to-slate-100";
    }
  }

  return (
    <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden transition-all duration-1000">
      {/* Base sky background */}
      <div className={`absolute inset-0 bg-gradient-to-b ${gradient} transition-colors duration-1000`} />

      {/* Dynamic ambient sun/moon glow orbs */}
      {cond.type === "sunny" && isDay && (
        <div
          className={`absolute -top-32 -right-32 w-[500px] h-[500px] rounded-full blur-3xl animate-pulse ${
            isDarkTheme ? "bg-amber-400/20" : "bg-amber-400/40"
          }`}
        />
      )}

      {cond.type === "sunny" && !isDay && (
        <>
          <div className="absolute top-12 right-20 w-44 h-44 rounded-full bg-blue-100/15 blur-2xl" />
          <div className="absolute inset-0 opacity-40 bg-[radial-gradient(#fff_1px,transparent_1px)] [background-size:24px_24px]" />
        </>
      )}

      {cond.type === "rainy" && (
        <>
          <div
            className={`absolute -top-20 left-1/4 w-96 h-96 rounded-full blur-3xl ${
              isDarkTheme ? "bg-blue-500/15" : "bg-blue-400/25"
            }`}
          />
          <div
            className={`absolute top-1/2 right-10 w-96 h-96 rounded-full blur-3xl ${
              isDarkTheme ? "bg-cyan-700/15" : "bg-cyan-500/25"
            }`}
          />
        </>
      )}

      {cond.type === "thunder" && (
        <>
          <div className="absolute top-10 left-1/3 w-96 h-96 rounded-full bg-purple-600/20 blur-3xl" />
          <div className="absolute top-1/3 right-1/4 w-80 h-80 rounded-full bg-indigo-500/20 blur-3xl" />
        </>
      )}

      {/* Subtle overlay for contrast */}
      <div
        className={`absolute inset-0 transition-colors ${
          isDarkTheme ? "bg-slate-950/30" : "bg-white/20"
        }`}
      />
    </div>
  );
}
