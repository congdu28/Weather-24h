"use client";

import React from "react";
import {
  Sun,
  SunMedium,
  Moon,
  MoonStar,
  Cloud,
  CloudSun,
  CloudMoon,
  CloudFog,
  CloudDrizzle,
  CloudRain,
  CloudLightning,
  CloudSunRain,
  Zap,
  Snowflake
} from "lucide-react";

interface Props {
  name: string;
  className?: string;
}

export default function WeatherIcon({ name, className = "w-8 h-8" }: Props) {
  switch (name) {
    case "Sun":
      return <Sun className={`${className} text-amber-400 animate-[spin_20s_linear_infinite]`} />;
    case "SunMedium":
      return <SunMedium className={`${className} text-amber-300`} />;
    case "Moon":
      return <Moon className={`${className} text-indigo-200`} />;
    case "MoonStar":
      return <MoonStar className={`${className} text-indigo-300`} />;
    case "Cloud":
      return <Cloud className={`${className} text-slate-300`} />;
    case "CloudSun":
      return <CloudSun className={`${className} text-amber-300`} />;
    case "CloudMoon":
      return <CloudMoon className={`${className} text-indigo-300`} />;
    case "CloudFog":
      return <CloudFog className={`${className} text-zinc-300`} />;
    case "CloudDrizzle":
      return <CloudDrizzle className={`${className} text-sky-300`} />;
    case "CloudRain":
      return <CloudRain className={`${className} text-blue-400`} />;
    case "CloudLightning":
      return <CloudLightning className={`${className} text-purple-400`} />;
    case "CloudSunRain":
      return <CloudSunRain className={`${className} text-sky-400`} />;
    case "Zap":
      return <Zap className={`${className} text-yellow-400 animate-pulse`} />;
    case "Snowflake":
      return <Snowflake className={`${className} text-cyan-200`} />;
    default:
      return <CloudSun className={`${className} text-amber-300`} />;
  }
}
