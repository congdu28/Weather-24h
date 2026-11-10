"use client";

import React, { useId } from "react";

interface Props {
  name: string;
  className?: string;
}

export default function WeatherIcon({ name, className = "w-8 h-8" }: Props) {
  const id = useId().replace(/:/g, "");

  switch (name) {
    // -------------------------------------------------------------
    // 1. TRỜI NẮNG QUANG / NẮNG ĐẸP (Sun / SunMedium)
    // -------------------------------------------------------------
    case "Sun":
    case "SunMedium":
      return (
        <svg
          viewBox="0 0 64 64"
          className={`${className} overflow-visible`}
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <defs>
            <radialGradient id={`${id}-sun-disc`} cx="38%" cy="32%" r="65%">
              <stop offset="0%" stopColor="#FEF08A" />
              <stop offset="45%" stopColor="#FBBF24" />
              <stop offset="100%" stopColor="#EA580C" />
            </radialGradient>
            <linearGradient id={`${id}-sun-ray`} x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#FDE047" />
              <stop offset="100%" stopColor="#F59E0B" />
            </linearGradient>
            <radialGradient id={`${id}-sun-glow`} cx="50%" cy="50%" r="50%">
              <stop offset="0%" stopColor="#FDE047" stopOpacity="0.45" />
              <stop offset="70%" stopColor="#F59E0B" stopOpacity="0.2" />
              <stop offset="100%" stopColor="#F59E0B" stopOpacity="0" />
            </radialGradient>
          </defs>

          {/* Quầng sáng vầng thái dương */}
          <circle cx="32" cy="32" r="28" fill={`url(#${id}-sun-glow)`} />

          {/* Các tia nắng quay nhẹ nhàng xung quanh tâm */}
          <g className="animate-weather-spin">
            {/* 8 tia nắng bo tròn */}
            <rect x="30" y="4" width="4" height="9" rx="2" fill={`url(#${id}-sun-ray)`} />
            <rect x="30" y="51" width="4" height="9" rx="2" fill={`url(#${id}-sun-ray)`} />
            <rect x="4" y="30" width="9" height="4" rx="2" fill={`url(#${id}-sun-ray)`} />
            <rect x="51" y="30" width="9" height="4" rx="2" fill={`url(#${id}-sun-ray)`} />
            {/* 4 tia chéo */}
            <rect x="13" y="13" width="4" height="8" rx="2" fill={`url(#${id}-sun-ray)`} transform="rotate(-45 15 17)" />
            <rect x="47" y="47" width="4" height="8" rx="2" fill={`url(#${id}-sun-ray)`} transform="rotate(-45 49 51)" />
            <rect x="47" y="13" width="4" height="8" rx="2" fill={`url(#${id}-sun-ray)`} transform="rotate(45 49 17)" />
            <rect x="13" y="47" width="4" height="8" rx="2" fill={`url(#${id}-sun-ray)`} transform="rotate(45 15 51)" />
          </g>

          {/* Quả cầu mặt trời chính 3D bóng bẩy */}
          <circle cx="32" cy="32" r="14.5" fill={`url(#${id}-sun-disc)`} />
          {/* Điểm phản quang ánh sáng */}
          <ellipse cx="27" cy="26" rx="4.5" ry="2.5" fill="#FFFFFF" opacity="0.55" transform="rotate(-25 27 26)" />
        </svg>
      );

    // -------------------------------------------------------------
    // 2. ĐÊM QUANG ĐÃNG / TRĂNG SAO (Moon / MoonStar)
    // -------------------------------------------------------------
    case "Moon":
    case "MoonStar":
      return (
        <svg
          viewBox="0 0 64 64"
          className={`${className} overflow-visible`}
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <defs>
            <linearGradient id={`${id}-moon`} x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#FFFFFF" />
              <stop offset="35%" stopColor="#C7D2FE" />
              <stop offset="75%" stopColor="#818CF8" />
              <stop offset="100%" stopColor="#4F46E5" />
            </linearGradient>
            <radialGradient id={`${id}-moon-glow`} cx="40%" cy="40%" r="60%">
              <stop offset="0%" stopColor="#818CF8" stopOpacity="0.4" />
              <stop offset="100%" stopColor="#4F46E5" stopOpacity="0" />
            </radialGradient>
          </defs>

          {/* Vầng sáng huyền ảo trăng đêm */}
          <circle cx="28" cy="32" r="26" fill={`url(#${id}-moon-glow)`} />

          {/* Mặt trăng lưỡi liềm mượt mà */}
          <path
            d="M38 12 C24.5 14 15 25.5 15 38.5 C15 51.5 25.5 61 38.5 61 C45 61 50.5 58.5 54.5 54 C38.5 53 30.5 39 33.5 26 C35 19.5 38 15 38 12 Z"
            fill={`url(#${id}-moon)`}
            className="animate-weather-float"
          />

          {/* Những ngôi sao lấp lánh */}
          <g className="animate-weather-twinkle">
            <path d="M48 14 L50 20 L56 22 L50 24 L48 30 L46 24 L40 22 L46 20 Z" fill="#FDE047" opacity="0.9" />
          </g>
          <g className="animate-weather-twinkle" style={{ animationDelay: "1.2s" }}>
            <path d="M52 38 L53.5 42 L57.5 43.5 L53.5 45 L52 49 L50.5 45 L46.5 43.5 L50.5 42 Z" fill="#38BDF8" opacity="0.85" />
          </g>
          <circle cx="22" cy="18" r="1.5" fill="#E0E7FF" opacity="0.75" />
        </svg>
      );

    // -------------------------------------------------------------
    // 3. NGÀY CÓ MÂY & NẮNG (CloudSun)
    // -------------------------------------------------------------
    case "CloudSun":
      return (
        <svg
          viewBox="0 0 64 64"
          className={`${className} overflow-visible`}
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <defs>
            <radialGradient id={`${id}-cs-sun`} cx="38%" cy="32%" r="65%">
              <stop offset="0%" stopColor="#FEF08A" />
              <stop offset="50%" stopColor="#FBBF24" />
              <stop offset="100%" stopColor="#EA580C" />
            </radialGradient>
            <linearGradient id={`${id}-cs-cloud`} x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="#FFFFFF" />
              <stop offset="60%" stopColor="#F1F5F9" />
              <stop offset="100%" stopColor="#CBD5E1" />
            </linearGradient>
            <linearGradient id={`${id}-cs-ray`} x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#FDE047" />
              <stop offset="100%" stopColor="#F59E0B" />
            </linearGradient>
          </defs>

          {/* Mặt trời nhô lên góc trên bên phải */}
          <g transform="translate(42, 22)">
            <g className="animate-weather-spin" style={{ transformOrigin: "0px 0px" }}>
              <rect x="-1.5" y="-18" width="3" height="6" rx="1.5" fill={`url(#${id}-cs-ray)`} />
              <rect x="-1.5" y="12" width="3" height="6" rx="1.5" fill={`url(#${id}-cs-ray)`} />
              <rect x="-18" y="-1.5" width="6" height="3" rx="1.5" fill={`url(#${id}-cs-ray)`} />
              <rect x="12" y="-1.5" width="6" height="3" rx="1.5" fill={`url(#${id}-cs-ray)`} />
              <rect x="-13" y="-13" width="3" height="5" rx="1.5" fill={`url(#${id}-cs-ray)`} transform="rotate(-45 -11.5 -10.5)" />
              <rect x="8" y="8" width="3" height="5" rx="1.5" fill={`url(#${id}-cs-ray)`} transform="rotate(-45 9.5 10.5)" />
              <rect x="8" y="-13" width="3" height="5" rx="1.5" fill={`url(#${id}-cs-ray)`} transform="rotate(45 9.5 -10.5)" />
            </g>
            <circle cx="0" cy="0" r="11" fill={`url(#${id}-cs-sun)`} />
          </g>

          {/* Đám mây trắng bềnh bồng đằng trước */}
          <g className="animate-weather-float">
            {/* Đổ bóng đáy mây */}
            <path
              d="M20 52 C14.5 52 10 47.5 10 42 C10 36.8 14 32.5 19 32.1 C20.5 25.5 26.5 20.5 33.5 20.5 C41 20.5 47.2 25.8 48.2 33 C52.5 33.5 56 37.2 56 42 C56 47.5 51.5 52 46 52 Z"
              fill="#94A3B8"
              opacity="0.25"
              transform="translate(0, 1.5)"
            />
            {/* Thân đám mây chính */}
            <path
              d="M20 50 C14.5 50 10 45.5 10 40 C10 34.8 14 30.5 19 30.1 C20.5 23.5 26.5 18.5 33.5 18.5 C41 18.5 47.2 23.8 48.2 31 C52.5 31.5 56 35.2 56 40 C56 45.5 51.5 50 46 50 Z"
              fill={`url(#${id}-cs-cloud)`}
            />
            {/* Vệt sáng trên mây */}
            <ellipse cx="32" cy="24" rx="7" ry="3.5" fill="#FFFFFF" opacity="0.6" />
          </g>
        </svg>
      );

    // -------------------------------------------------------------
    // 4. ĐÊM CÓ MÂY & TRĂNG (CloudMoon)
    // -------------------------------------------------------------
    case "CloudMoon":
      return (
        <svg
          viewBox="0 0 64 64"
          className={`${className} overflow-visible`}
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <defs>
            <linearGradient id={`${id}-cm-moon`} x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#FFFFFF" />
              <stop offset="60%" stopColor="#A5B4FC" />
              <stop offset="100%" stopColor="#6366F1" />
            </linearGradient>
            <linearGradient id={`${id}-cm-cloud`} x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="#F8FAFC" />
              <stop offset="60%" stopColor="#CBD5E1" />
              <stop offset="100%" stopColor="#64748B" />
            </linearGradient>
          </defs>

          {/* Trăng lấp ló góc trên bên phải */}
          <path
            d="M48 10 C39 11.5 32 19 32 28 C32 31.5 33 34.5 35 37 C27 34 26 23 30 16 C31 14 33 11 34 10 Z"
            fill={`url(#${id}-cm-moon)`}
            transform="translate(8, -2)"
          />

          {/* Đám mây đêm */}
          <g className="animate-weather-float">
            <path
              d="M20 50 C14.5 50 10 45.5 10 40 C10 34.8 14 30.5 19 30.1 C20.5 23.5 26.5 18.5 33.5 18.5 C41 18.5 47.2 23.8 48.2 31 C52.5 31.5 56 35.2 56 40 C56 45.5 51.5 50 46 50 Z"
              fill={`url(#${id}-cm-cloud)`}
            />
          </g>
        </svg>
      );

    // -------------------------------------------------------------
    // 5. TRỜI NHIỀU MÂY / ÂM U (Cloud)
    // -------------------------------------------------------------
    case "Cloud":
      return (
        <svg
          viewBox="0 0 64 64"
          className={`${className} overflow-visible`}
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <defs>
            <linearGradient id={`${id}-cloud-back`} x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="#CBD5E1" />
              <stop offset="100%" stopColor="#64748B" />
            </linearGradient>
            <linearGradient id={`${id}-cloud-front`} x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="#FFFFFF" />
              <stop offset="60%" stopColor="#E2E8F0" />
              <stop offset="100%" stopColor="#94A3B8" />
            </linearGradient>
          </defs>

          {/* Đám mây tầng sau (tạo chiều sâu khối 3D) */}
          <path
            d="M32 40 C27 40 23 36 23 31 C23 26.5 26.5 22.8 31 22.2 C32.5 16.5 37.5 12 43.5 12 C50.5 12 56 17 56.8 23.5 C60.5 24 63 27 63 31 C63 36 59 40 54 40 Z"
            fill={`url(#${id}-cloud-back)`}
            opacity="0.8"
          />

          {/* Đám mây tầng trước */}
          <g className="animate-weather-float">
            <path
              d="M18 53 C13 53 9 49 9 44 C9 39.2 12.8 35.3 17.5 34.9 C18.8 28.5 24.5 23.5 31.5 23.5 C39 23.5 45.2 28.8 46.2 36 C50.5 36.5 54 40.2 54 45 C54 50.5 49.5 53 44 53 Z"
              fill={`url(#${id}-cloud-front)`}
            />
          </g>
        </svg>
      );

    // -------------------------------------------------------------
    // 6. MƯA RÀO / MƯA VỪA / MƯA TO (CloudRain / CloudDrizzle)
    // -------------------------------------------------------------
    case "CloudRain":
    case "CloudDrizzle":
      return (
        <svg
          viewBox="0 0 64 64"
          className={`${className} overflow-visible`}
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <defs>
            <linearGradient id={`${id}-rain-cloud`} x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="#E2E8F0" />
              <stop offset="60%" stopColor="#94A3B8" />
              <stop offset="100%" stopColor="#475569" />
            </linearGradient>
            <linearGradient id={`${id}-rain-drop`} x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="#38BDF8" />
              <stop offset="100%" stopColor="#0284C7" />
            </linearGradient>
          </defs>

          {/* Đám mây mưa màu xám xanh */}
          <path
            d="M19 42 C14 42 10 38 10 33 C10 28.5 13.5 24.8 18 24.3 C19.5 18 25.5 13 32.5 13 C40 13 46.2 18.2 47.2 25.5 C51.5 26 55 29.8 55 34.5 C55 39.5 50.5 42 45 42 Z"
            fill={`url(#${id}-rain-cloud)`}
          />

          {/* 3 Hạt mưa giọt nước rơi sinh động */}
          <g className="animate-weather-rain-1">
            <path
              d="M21 46 C19.8 48.5 19 50.2 19 52 C19 54.2 20.8 56 23 56 C25.2 56 27 54.2 27 52 C27 50.2 26.2 48.5 25 46 C24.2 44.5 21.8 44.5 21 46 Z"
              fill={`url(#${id}-rain-drop)`}
            />
          </g>
          <g className="animate-weather-rain-2">
            <path
              d="M31 48 C29.8 50.5 29 52.2 29 54 C29 56.2 30.8 58 33 58 C35.2 58 37 56.2 37 54 C37 52.2 36.2 50.5 35 48 C34.2 46.5 31.8 46.5 31 48 Z"
              fill={`url(#${id}-rain-drop)`}
            />
          </g>
          <g className="animate-weather-rain-3">
            <path
              d="M41 46 C39.8 48.5 39 50.2 39 52 C39 54.2 40.8 56 43 56 C45.2 56 47 54.2 47 52 C47 50.2 46.2 48.5 45 46 C44.2 44.5 41.8 44.5 41 46 Z"
              fill={`url(#${id}-rain-drop)`}
            />
          </g>
        </svg>
      );

    // -------------------------------------------------------------
    // 7. NẮNG KÈM MƯA RÀO (CloudSunRain)
    // -------------------------------------------------------------
    case "CloudSunRain":
      return (
        <svg
          viewBox="0 0 64 64"
          className={`${className} overflow-visible`}
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <defs>
            <radialGradient id={`${id}-csr-sun`} cx="38%" cy="32%" r="65%">
              <stop offset="0%" stopColor="#FEF08A" />
              <stop offset="60%" stopColor="#F59E0B" />
              <stop offset="100%" stopColor="#D97706" />
            </radialGradient>
            <linearGradient id={`${id}-csr-cloud`} x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="#FFFFFF" />
              <stop offset="60%" stopColor="#E2E8F0" />
              <stop offset="100%" stopColor="#94A3B8" />
            </linearGradient>
            <linearGradient id={`${id}-csr-drop`} x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="#38BDF8" />
              <stop offset="100%" stopColor="#0284C7" />
            </linearGradient>
          </defs>

          {/* Mặt trời góc phải */}
          <circle cx="43" cy="21" r="10" fill={`url(#${id}-csr-sun)`} />

          {/* Đám mây */}
          <path
            d="M19 43 C14 43 10 39 10 34 C10 29.5 13.5 25.8 18 25.3 C19.5 19 25.5 14 32.5 14 C40 14 46.2 19.2 47.2 26.5 C51.5 27 55 30.8 55 35.5 C55 40.5 50.5 43 45 43 Z"
            fill={`url(#${id}-csr-cloud)`}
          />

          {/* Hạt mưa */}
          <g className="animate-weather-rain-1">
            <path
              d="M24 47 C22.8 49.5 22 51.2 22 53 C22 55.2 23.8 57 26 57 C28.2 57 30 55.2 30 53 C30 51.2 29.2 49.5 28 47 C27.2 45.5 24.8 45.5 24 47 Z"
              fill={`url(#${id}-csr-drop)`}
            />
          </g>
          <g className="animate-weather-rain-2">
            <path
              d="M36 47 C34.8 49.5 34 51.2 34 53 C34 55.2 35.8 57 38 57 C40.2 57 42 55.2 42 53 C42 51.2 41.2 49.5 40 47 C39.2 45.5 36.8 45.5 36 47 Z"
              fill={`url(#${id}-csr-drop)`}
            />
          </g>
        </svg>
      );

    // -------------------------------------------------------------
    // 8. DÔNG SẤM SÉT (CloudLightning / Zap)
    // -------------------------------------------------------------
    case "CloudLightning":
    case "Zap":
      return (
        <svg
          viewBox="0 0 64 64"
          className={`${className} overflow-visible`}
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <defs>
            <linearGradient id={`${id}-storm-cloud`} x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="#64748B" />
              <stop offset="50%" stopColor="#334155" />
              <stop offset="100%" stopColor="#1E293B" />
            </linearGradient>
            <linearGradient id={`${id}-lightning`} x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#FEF08A" />
              <stop offset="50%" stopColor="#FACC15" />
              <stop offset="100%" stopColor="#F59E0B" />
            </linearGradient>
          </defs>

          {/* Đám mây giông bão đen sẫm */}
          <path
            d="M19 40 C14 40 10 36 10 31 C10 26.5 13.5 22.8 18 22.3 C19.5 16 25.5 11 32.5 11 C40 11 46.2 16.2 47.2 23.5 C51.5 24 55 27.8 55 32.5 C55 37.5 50.5 40 45 40 Z"
            fill={`url(#${id}-storm-cloud)`}
          />

          {/* Tia sét vàng rực rỡ phát sáng chớp nháy */}
          <g className="animate-weather-flash">
            <path
              d="M33 30 L23 44 H33 L29 60 L44 42 H34 L38 30 Z"
              fill={`url(#${id}-lightning)`}
              stroke="#FFFFFF"
              strokeWidth="0.8"
            />
          </g>
        </svg>
      );

    // -------------------------------------------------------------
    // 9. SƯƠNG MÙ (CloudFog)
    // -------------------------------------------------------------
    case "CloudFog":
      return (
        <svg
          viewBox="0 0 64 64"
          className={`${className} overflow-visible`}
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <defs>
            <linearGradient id={`${id}-fog-cloud`} x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="#FFFFFF" />
              <stop offset="100%" stopColor="#CBD5E1" />
            </linearGradient>
            <linearGradient id={`${id}-fog-line`} x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#CBD5E1" stopOpacity="0.2" />
              <stop offset="20%" stopColor="#94A3B8" />
              <stop offset="80%" stopColor="#94A3B8" />
              <stop offset="100%" stopColor="#CBD5E1" stopOpacity="0.2" />
            </linearGradient>
          </defs>

          {/* Mây tầng trên */}
          <path
            d="M20 36 C16 36 12 33 12 28.5 C12 24.5 15 21.5 19 21 C20.5 15.5 25.5 11 32 11 C38.5 11 44 15.5 45 22 C48.5 22.5 52 25.5 52 29.5 C52 33.5 48.5 36 44 36 Z"
            fill={`url(#${id}-fog-cloud)`}
            opacity="0.8"
          />

          {/* 3 Lớp sóng sương mù ngang bồng bềnh */}
          <rect x="10" y="42" width="44" height="3.5" rx="1.75" fill={`url(#${id}-fog-line)`} />
          <rect x="14" y="49" width="36" height="3.5" rx="1.75" fill={`url(#${id}-fog-line)`} />
          <rect x="18" y="56" width="28" height="3" rx="1.5" fill={`url(#${id}-fog-line)`} opacity="0.75" />
        </svg>
      );

    // -------------------------------------------------------------
    // 10. TUYẾT RƠI (Snowflake)
    // -------------------------------------------------------------
    case "Snowflake":
      return (
        <svg
          viewBox="0 0 64 64"
          className={`${className} overflow-visible`}
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <defs>
            <linearGradient id={`${id}-snow`} x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#E0F2FE" />
              <stop offset="50%" stopColor="#7DD3FC" />
              <stop offset="100%" stopColor="#0284C7" />
            </linearGradient>
          </defs>

          {/* Bông tuyết pha lê 6 cánh xoay nhẹ */}
          <g className="animate-weather-spin" style={{ animationDuration: "35s" }}>
            <line x1="32" y1="8" x2="32" y2="56" stroke={`url(#${id}-snow)`} strokeWidth="3" strokeLinecap="round" />
            <line x1="8" y1="32" x2="56" y2="32" stroke={`url(#${id}-snow)`} strokeWidth="3" strokeLinecap="round" />
            <line x1="15" y1="15" x2="49" y2="49" stroke={`url(#${id}-snow)`} strokeWidth="3" strokeLinecap="round" />
            <line x1="49" y1="15" x2="15" y2="49" stroke={`url(#${id}-snow)`} strokeWidth="3" strokeLinecap="round" />
            {/* Nhánh hoa tuyết */}
            <circle cx="32" cy="32" r="4.5" fill="#38BDF8" />
            <circle cx="32" cy="18" r="2" fill="#E0F2FE" />
            <circle cx="32" cy="46" r="2" fill="#E0F2FE" />
            <circle cx="18" cy="32" r="2" fill="#E0F2FE" />
            <circle cx="46" cy="32" r="2" fill="#E0F2FE" />
          </g>
        </svg>
      );

    // -------------------------------------------------------------
    // DEFAULT: CloudSun
    // -------------------------------------------------------------
    default:
      return (
        <svg
          viewBox="0 0 64 64"
          className={`${className} overflow-visible`}
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <circle cx="32" cy="32" r="14" fill="#FBBF24" />
        </svg>
      );
  }
}
