"use client";

import React, { useState, useEffect, useCallback, useMemo } from "react";
import Header from "../components/Header";
import AtmosphereBackground from "../components/AtmosphereBackground";
import WeatherAtmosphereEffect from "../components/WeatherAtmosphereEffect";
import HeroWeatherCard from "../components/HeroWeatherCard";
import FavoriteCitiesQuickBar from "../components/FavoriteCitiesQuickBar";
import HourlyForecastCard from "../components/HourlyForecastCard";
import WeatherRadarCard from "../components/WeatherRadarCard";
import VietnameseLunarCard from "../components/VietnameseLunarCard";
import DailyForecastCard from "../components/DailyForecastCard";
import AiStylistCard from "../components/AiStylistCard";
import AirQualityCard from "../components/AirQualityCard";
import LifestyleCard from "../components/LifestyleCard";
import WeatherMetricsGrid from "../components/WeatherMetricsGrid";
import LunarAlmanacView from "../components/LunarAlmanacView";
import AstronomyArcCard from "../components/AstronomyArcCard";
import SmartInsightsCard from "../components/SmartInsightsCard";
import CityCompareCard from "../components/CityCompareCard";
import SettingsDialog, { SettingsState } from "../components/SettingsDialog";
import {
  CityLocation,
  WeatherData,
  DEFAULT_APP_SETTINGS,
  DEFAULT_WIDGET_LAYOUT,
  DEFAULT_WIDGET_ORDER,
  DEFAULT_ATMOSPHERE_PARTICLES,
  ACCENT_THEMES
} from "../types/weather";
import { usePersistentState } from "../utils/usePersistentState";
import {
  DEFAULT_LOCATION,
  fetchFullWeather,
  VIETNAM_MAJOR_CITIES,
  reverseGeocode
} from "../services/weatherApi";
import { RefreshCw, AlertCircle } from "lucide-react";

export default function Home() {
  const [location, setLocation] = usePersistentState<CityLocation>(
    "weather24h_last_loc",
    DEFAULT_LOCATION
  );

  const [rawSettings, setSettings] = usePersistentState<SettingsState>(
    "weather24h_settings",
    DEFAULT_APP_SETTINGS
  );

  // Merge settings với fallback an toàn tuyệt đối
  const settings: SettingsState = useMemo(() => {
    const baseOrder = rawSettings?.widgetOrder || DEFAULT_WIDGET_ORDER;
    const validOrder = baseOrder.filter((id) => DEFAULT_WIDGET_ORDER.includes(id));
    DEFAULT_WIDGET_ORDER.forEach((id) => {
      if (!validOrder.includes(id)) validOrder.push(id);
    });

    return {
      ...DEFAULT_APP_SETTINGS,
      ...rawSettings,
      widgets: {
        ...DEFAULT_WIDGET_LAYOUT,
        ...(rawSettings?.widgets || {})
      },
      widgetOrder: validOrder,
      atmosphereParticles: {
        ...DEFAULT_ATMOSPHERE_PARTICLES,
        ...(rawSettings?.atmosphereParticles || {})
      }
    };
  }, [rawSettings]);

  const [favoriteCities, setFavoriteCities] = usePersistentState<CityLocation[]>(
    "weather24h_favs",
    [VIETNAM_MAJOR_CITIES[0], VIETNAM_MAJOR_CITIES[1], VIETNAM_MAJOR_CITIES[2]]
  );

  const [isDarkTheme, setIsDarkTheme] = useState<boolean>(false);
  const [weatherData, setWeatherData] = useState<WeatherData | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<"weather" | "lunar" | "radar">("weather");
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);

  // Sync theme (Light / Dark / System)
  useEffect(() => {
    const applyTheme = () => {
      let dark = true;
      if (settings.theme === "light") {
        dark = false;
      } else if (settings.theme === "dark") {
        dark = true;
      } else if (settings.theme === "system") {
        dark = window.matchMedia("(prefers-color-scheme: dark)").matches;
      }

      setIsDarkTheme(dark);
      if (dark) {
        document.documentElement.classList.add("dark");
        document.documentElement.classList.remove("light");
      } else {
        document.documentElement.classList.add("light");
        document.documentElement.classList.remove("dark");
      }
    };

    applyTheme();

    if (settings.theme === "system") {
      const media = window.matchMedia("(prefers-color-scheme: dark)");
      const handler = () => applyTheme();
      media.addEventListener("change", handler);
      return () => media.removeEventListener("change", handler);
    }
  }, [settings.theme]);

  // Cập nhật động hệ thống CSS Variables: Card Opacity, Blur, Radius, Shadow Depth, Accent Color
  useEffect(() => {
    const root = document.documentElement;
    const isDark = isDarkTheme;

    // 1. Màu điểm nhấn (Accent Theme)
    let accentHex = "#f59e0b";
    let accentRgb = "245, 158, 11";
    if (settings.accentColor === "adaptive" && weatherData) {
      const code = weatherData.current.weatherCode;
      if (code >= 95) {
        accentHex = "#8b5cf6"; accentRgb = "139, 92, 246"; // Dông sét: Tím điện
      } else if (code >= 51 && code <= 82) {
        accentHex = "#0ea5e9"; accentRgb = "14, 165, 233"; // Mưa: Xanh đại dương
      } else if (code === 0 || code === 1) {
        accentHex = "#f59e0b"; accentRgb = "245, 158, 11"; // Nắng: Vàng hổ phách
      } else {
        accentHex = "#10b981"; accentRgb = "16, 185, 129"; // Mây/Sương mù: Lục bảo
      }
    } else {
      const themeObj = ACCENT_THEMES[settings.accentColor] || ACCENT_THEMES.amber;
      accentHex = themeObj.hex;
      accentRgb = themeObj.rgb;
    }

    root.style.setProperty("--accent-color", accentHex);
    root.style.setProperty("--accent-rgb", accentRgb);
    root.style.setProperty("--accent-light", `rgba(${accentRgb}, 0.15)`);
    root.style.setProperty("--accent-border", `rgba(${accentRgb}, 0.35)`);
    root.style.setProperty("--accent-glow", `rgba(${accentRgb}, 0.25)`);

    // 2. Độ đậm nhạt & Trong suốt các khối (Card Opacity)
    const opacityVal = (settings.customOpacity || 88) / 100;
    if (isDark) {
      root.style.setProperty("--card-bg", `rgba(15, 23, 42, ${opacityVal})`);
    } else {
      root.style.setProperty("--card-bg", `rgba(255, 255, 255, ${opacityVal})`);
    }

    // 3. Độ mờ kính (Backdrop Blur)
    let blurPx = "16px";
    if (settings.blurLevel === "none") blurPx = "0px";
    else if (settings.blurLevel === "low") blurPx = "8px";
    else if (settings.blurLevel === "heavy") blurPx = "24px";
    root.style.setProperty("--card-blur", blurPx);

    // 4. Độ bo tròn góc (Border Radius)
    let radiusPx = "24px";
    if (settings.borderRadius === "compact") radiusPx = "14px";
    else if (settings.borderRadius === "curvy") radiusPx = "32px";
    root.style.setProperty("--card-radius", radiusPx);

    // 5. Chiều sâu & Đổ bóng (Depth & 3D Shadows)
    if (settings.depthLevel === "flat") {
      root.style.setProperty("--card-shadow", "none");
      root.style.setProperty("--card-hover-shadow", isDark ? "0 4px 12px rgba(0,0,0,0.4)" : "0 4px 12px rgba(0,0,0,0.06)");
      root.style.setProperty("--card-border", isDark ? "rgba(255, 255, 255, 0.08)" : "rgba(226, 232, 240, 0.8)");
      root.style.setProperty("--card-hover-y", "0px");
    } else if (settings.depthLevel === "soft") {
      root.style.setProperty(
        "--card-shadow",
        isDark ? "0 10px 30px -10px rgba(0, 0, 0, 0.45)" : "0 10px 25px -5px rgba(14, 165, 233, 0.07), 0 4px 6px -4px rgba(0, 0, 0, 0.02)"
      );
      root.style.setProperty(
        "--card-hover-shadow",
        isDark ? "0 20px 40px -12px rgba(0, 0, 0, 0.65)" : "0 20px 35px -8px rgba(14, 165, 233, 0.14)"
      );
      root.style.setProperty("--card-border", isDark ? "rgba(255, 255, 255, 0.12)" : "rgba(226, 232, 240, 0.9)");
      root.style.setProperty("--card-hover-y", "-2px");
    } else if (settings.depthLevel === "deep") {
      root.style.setProperty(
        "--card-shadow",
        isDark ? "0 22px 50px -12px rgba(0, 0, 0, 0.75), 0 8px 16px -4px rgba(0, 0, 0, 0.3)" : "0 20px 35px -10px rgba(15, 23, 42, 0.15), 0 8px 12px -4px rgba(0, 0, 0, 0.05)"
      );
      root.style.setProperty(
        "--card-hover-shadow",
        isDark ? "0 30px 65px -15px rgba(0, 0, 0, 0.9)" : "0 28px 50px -12px rgba(15, 23, 42, 0.22)"
      );
      root.style.setProperty("--card-border", isDark ? "rgba(255, 255, 255, 0.18)" : "rgba(203, 213, 225, 0.95)");
      root.style.setProperty("--card-hover-y", "-4px");
    } else if (settings.depthLevel === "glow") {
      root.style.setProperty(
        "--card-shadow",
        `0 12px 35px -5px rgba(${accentRgb}, ${isDark ? 0.35 : 0.2}), 0 0 20px 2px rgba(${accentRgb}, ${isDark ? 0.18 : 0.1})`
      );
      root.style.setProperty(
        "--card-hover-shadow",
        `0 18px 45px -5px rgba(${accentRgb}, ${isDark ? 0.5 : 0.32}), 0 0 28px 4px rgba(${accentRgb}, ${isDark ? 0.28 : 0.16})`
      );
      root.style.setProperty("--card-border", `rgba(${accentRgb}, ${isDark ? 0.45 : 0.35})`);
      root.style.setProperty("--card-hover-y", "-3px");
    }
  }, [settings, isDarkTheme, weatherData]);

  const loadWeather = useCallback(async (loc: CityLocation) => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await fetchFullWeather(loc, settings.forecastModel);
      setWeatherData(data);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Không thể tải dữ liệu thời tiết. Vui lòng thử lại!";
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  }, [settings.forecastModel]);

  useEffect(() => {
    let ignore = false;
    fetchFullWeather(location, settings.forecastModel)
      .then((data) => {
        if (!ignore) {
          setWeatherData(data);
          setIsLoading(false);
        }
      })
      .catch((err: unknown) => {
        if (!ignore) {
          const msg = err instanceof Error ? err.message : "Không thể tải dữ liệu thời tiết. Vui lòng thử lại!";
          setError(msg);
          setIsLoading(false);
        }
      });
    return () => {
      ignore = true;
    };
  }, [location, settings.forecastModel]);

  const handleSelectLocation = (loc: CityLocation) => {
    setLocation(loc);
    localStorage.setItem("weather24h_last_loc", JSON.stringify(loc));
    loadWeather(loc);
  };

  const handleRefresh = () => {
    loadWeather(location);
  };

  const handleUpdateSettings = (newSettings: SettingsState) => {
    const prevModel = settings.forecastModel;
    setSettings(newSettings);
    localStorage.setItem("weather24h_settings", JSON.stringify(newSettings));
    if (newSettings.forecastModel !== prevModel) {
      setIsLoading(true);
      fetchFullWeather(location, newSettings.forecastModel)
        .then((data) => {
          setWeatherData(data);
          setIsLoading(false);
        })
        .catch(() => setIsLoading(false));
    }
  };

  const isCityFavorite = useCallback(
    (city: CityLocation) => {
      return favoriteCities.some(
        (c) =>
          c.name.trim().toLowerCase() === city.name.trim().toLowerCase() ||
          (Math.abs(c.latitude - city.latitude) < 0.02 && Math.abs(c.longitude - city.longitude) < 0.02)
      );
    },
    [favoriteCities]
  );

  const isFavorite = isCityFavorite(location);

  const removeFavorite = useCallback(
    (target: CityLocation) => {
      setFavoriteCities((prev) =>
        prev.filter(
          (c) =>
            c.name.trim().toLowerCase() !== target.name.trim().toLowerCase() &&
            !(Math.abs(c.latitude - target.latitude) < 0.02 && Math.abs(c.longitude - target.longitude) < 0.02)
        )
      );
    },
    [setFavoriteCities]
  );

  const toggleFavoriteCity = useCallback(
    (city: CityLocation) => {
      if (isCityFavorite(city)) {
        removeFavorite(city);
      } else {
        setFavoriteCities((prev) => [...prev, city]);
      }
    },
    [isCityFavorite, removeFavorite, setFavoriteCities]
  );

  const toggleFavorite = useCallback(() => {
    toggleFavoriteCity(location);
  }, [toggleFavoriteCity, location]);

  const handleGpsLocation = () => {
    if (!navigator.geolocation) {
      alert("Trình duyệt không hỗ trợ GPS.");
      return;
    }
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        const lat = pos.coords.latitude;
        const lon = pos.coords.longitude;
        handleSelectLocation({
          name: "Đang định vị...",
          country: "Việt Nam",
          admin1: "GPS Tọa độ",
          latitude: lat,
          longitude: lon,
          isCurrentLocation: true
        });

        try {
          const exact = await reverseGeocode(lat, lon);
          handleSelectLocation(exact);
        } catch {
          handleSelectLocation({
            name: "Vị trí của bạn",
            country: "Việt Nam",
            admin1: "GPS Tọa độ",
            latitude: lat,
            longitude: lon,
            isCurrentLocation: true
          });
        }
      },
      (err) => {
        alert("Không thể lấy vị trí hiện tại: " + err.message);
      }
    );
  };

  const widgets = settings.widgets;

  return (
    <div
      className={`min-h-screen font-sans pb-16 relative transition-colors duration-500 ${
        isDarkTheme ? "text-slate-100" : "text-slate-900"
      }`}
    >
      {/* 1. Dynamic Weather Atmosphere Background (z-0) */}
      {weatherData && (
        <>
          <AtmosphereBackground
            weatherCode={weatherData.current.weatherCode}
            isDay={weatherData.current.isDay}
            isDarkTheme={isDarkTheme}
          />
          <WeatherAtmosphereEffect
            weatherCode={weatherData.current.weatherCode}
            isDay={weatherData.current.isDay}
            enabled={settings.atmosphereEnabled}
            isDarkTheme={isDarkTheme}
            windSpeed={weatherData.current.windSpeed}
            particles={settings.atmosphereParticles}
            performanceMode={settings.atmospherePerformance}
          />
        </>
      )}

      {/* 2. Top Header with Settings button */}
      <Header
        currentLocation={location}
        onSelectLocation={handleSelectLocation}
        onRefresh={handleRefresh}
        isLoading={isLoading}
        activeTab={activeTab}
        onTabChange={setActiveTab}
        onOpenSettings={() => setIsSettingsOpen(true)}
        isSearchOpen={isSearchOpen}
        onSetSearchOpen={setIsSearchOpen}
        isDarkTheme={isDarkTheme}
        favoriteCities={favoriteCities}
        onToggleFavoriteCity={toggleFavoriteCity}
      />

      {/* Main Container (z-10) */}
      <main className="relative z-10 max-w-6xl mx-auto px-3 sm:px-4 md:px-6 pt-3 sm:pt-4 space-y-4 sm:space-y-6">
        {/* Quick Bar: Favorite Cities & GPS */}
        {activeTab === "weather" && (
          <FavoriteCitiesQuickBar
            currentCityName={location.name}
            favoriteCities={favoriteCities}
            onSelectCity={handleSelectLocation}
            onRemoveCity={removeFavorite}
            onSelectGps={handleGpsLocation}
            onOpenSearch={() => setIsSearchOpen(true)}
            isDarkTheme={isDarkTheme}
          />
        )}

        {error && (
          <div className="p-4 rounded-2xl bg-rose-900/60 border border-rose-500/50 flex items-center justify-between gap-3 text-rose-200 text-xs sm:text-sm">
            <div className="flex items-center gap-2">
              <AlertCircle className="w-5 h-5 text-rose-400 shrink-0" />
              <span>{error}</span>
            </div>
            <button
              onClick={handleRefresh}
              className="px-3 py-1.5 rounded-xl bg-rose-700 hover:bg-rose-600 text-white font-semibold text-xs shrink-0"
            >
              Thử lại
            </button>
          </div>
        )}

        {isLoading && !weatherData && (
          <div className="py-24 text-center">
            <RefreshCw className="w-8 h-8 animate-spin mx-auto text-amber-500 mb-3" />
            <p className="text-sm font-medium opacity-80">Đang tải dữ liệu Weather 24h & Âm lịch...</p>
          </div>
        )}

        {/* TAB 1: THỜI TIẾT CHÍNH (ĐỒNG BỘ APK & THEO THỨ TỰ TÙY BIẾN CỦA NGƯỜI DÙNG) */}
        {activeTab === "weather" && weatherData && (
          <div className="space-y-5 sm:space-y-6 animate-in fade-in duration-300">
            {settings.widgetOrder.map((widgetId) => {
              if (!widgets[widgetId]) return null;
              switch (widgetId) {
                case "hero":
                  return (
                    <HeroWeatherCard
                      key="hero"
                      current={weatherData.current}
                      todayForecast={weatherData.daily[0]}
                      location={weatherData.location}
                      airQuality={weatherData.airQuality}
                      isFavorite={isFavorite}
                      onToggleFavorite={toggleFavorite}
                      onLocationClick={() => setIsSearchOpen(true)}
                      onLunarClick={() => setActiveTab("lunar")}
                      showLunar={settings.showLunar}
                      isDarkTheme={isDarkTheme}
                    />
                  );
                case "astronomy":
                  return (
                    <AstronomyArcCard
                      key="astronomy"
                      current={weatherData.current}
                      todayForecast={weatherData.daily[0]}
                      isDarkTheme={isDarkTheme}
                    />
                  );
                case "insights":
                  return (
                    <SmartInsightsCard
                      key="insights"
                      current={weatherData.current}
                      todayForecast={weatherData.daily[0]}
                      hourly={weatherData.hourly}
                      isDarkTheme={isDarkTheme}
                    />
                  );
                case "cityCompare":
                  return (
                    <CityCompareCard
                      key="cityCompare"
                      currentCity={location}
                      favoriteCities={favoriteCities}
                      onSelectCity={handleSelectLocation}
                      onOpenSearch={() => setIsSearchOpen(true)}
                      isDarkTheme={isDarkTheme}
                    />
                  );
                case "hourly":
                  return (
                    <HourlyForecastCard
                      key="hourly"
                      hourly={weatherData.hourly}
                      isDarkTheme={isDarkTheme}
                    />
                  );
                case "radar":
                  return (
                    <WeatherRadarCard
                      key="radar"
                      location={weatherData.location}
                      isDarkTheme={isDarkTheme}
                    />
                  );
                case "lunar":
                  return settings.showLunar ? (
                    <VietnameseLunarCard
                      key="lunar"
                      onOpenDetailedAlmanac={() => setActiveTab("lunar")}
                      isDarkTheme={isDarkTheme}
                    />
                  ) : null;
                case "daily":
                  return (
                    <DailyForecastCard
                      key="daily"
                      daily={weatherData.daily}
                      showLunar={settings.showLunar}
                      isDarkTheme={isDarkTheme}
                    />
                  );
                case "aiStylist":
                  return (
                    <AiStylistCard
                      key="aiStylist"
                      current={weatherData.current}
                      todayForecast={weatherData.daily[0]}
                      isDarkTheme={isDarkTheme}
                    />
                  );
                case "airQuality":
                  return (
                    <AirQualityCard
                      key="airQuality"
                      airQuality={weatherData.airQuality}
                      isDarkTheme={isDarkTheme}
                    />
                  );
                case "lifestyle":
                  return (
                    <LifestyleCard
                      key="lifestyle"
                      items={weatherData.lifestyles}
                      isDarkTheme={isDarkTheme}
                    />
                  );
                case "metrics":
                  return (
                    <WeatherMetricsGrid
                      key="metrics"
                      current={weatherData.current}
                      todayForecast={weatherData.daily[0]}
                      isDarkTheme={isDarkTheme}
                    />
                  );
                default:
                  return null;
              }
            })}
          </div>
        )}

        {/* TAB 2: LỊCH VẠN NIÊN & ÂM LỊCH */}
        {activeTab === "lunar" && (
          <div className="animate-in fade-in duration-300">
            <LunarAlmanacView isDarkTheme={isDarkTheme} />
          </div>
        )}

        {/* TAB 3: RADAR THỜI TIẾT */}
        {activeTab === "radar" && weatherData && (
          <div className="animate-in fade-in duration-300">
            <WeatherRadarCard location={weatherData.location} isDarkTheme={isDarkTheme} />
          </div>
        )}
      </main>

      {/* Settings Dialog */}
      <SettingsDialog
        isOpen={isSettingsOpen}
        onClose={() => setIsSettingsOpen(false)}
        settings={settings}
        onUpdateSettings={handleUpdateSettings}
        favoriteCities={favoriteCities}
        onRemoveFavorite={removeFavorite}
        isDarkTheme={isDarkTheme}
      />

      {/* Footer */}
      <footer className="relative z-10 mt-16 border-t border-slate-500/20 pt-8 pb-12 text-center text-xs opacity-70 max-w-6xl mx-auto px-4">
        <div className="flex flex-wrap items-center justify-center gap-4 mb-2 font-medium">
          <span>Weather 24h & Lịch Âm Việt Nam (v1.0.63)</span>
          <span>•</span>
          <span>Dữ liệu: Open-Meteo & OpenStreetMap</span>
          <span>•</span>
          <span>Thuật toán Âm lịch: TS. Hồ Ngọc Đức</span>
        </div>
        <p className="text-[11px]">
          Giao diện đồng bộ bản Android APK v1.0.63. Hỗ trợ PWA cài đặt trực tiếp lên điện thoại & PC.
        </p>
      </footer>
    </div>
  );
}
