"use client";

import React, { useState, useEffect, useCallback } from "react";
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
import SettingsDialog, { SettingsState } from "../components/SettingsDialog";
import { CityLocation, WeatherData } from "../types/weather";
import { usePersistentState } from "../utils/usePersistentState";
import {
  DEFAULT_LOCATION,
  fetchFullWeather,
  VIETNAM_MAJOR_CITIES,
  reverseGeocode
} from "../services/weatherApi";
import { RefreshCw, AlertCircle } from "lucide-react";

const DEFAULT_SETTINGS: SettingsState = {
  theme: "dark",
  atmosphereEnabled: true,
  atmospherePerformance: "high",
  showLunar: true,
  tempUnit: "c",
  windUnit: "kmh",
  forecastModel: "best_match",
  geminiApiKey: ""
};

export default function Home() {
  const [location, setLocation] = usePersistentState<CityLocation>(
    "weather24h_last_loc",
    DEFAULT_LOCATION
  );

  const [settings, setSettings] = usePersistentState<SettingsState>(
    "weather24h_settings",
    DEFAULT_SETTINGS
  );

  const [favoriteCities, setFavoriteCities] = usePersistentState<CityLocation[]>(
    "weather24h_favs",
    [VIETNAM_MAJOR_CITIES[0], VIETNAM_MAJOR_CITIES[1], VIETNAM_MAJOR_CITIES[2]]
  );

  const [isDarkTheme, setIsDarkTheme] = useState<boolean>(true);
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

  const loadWeather = useCallback(async (loc: CityLocation) => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await fetchFullWeather(loc);
      setWeatherData(data);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Không thể tải dữ liệu thời tiết. Vui lòng thử lại!";
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    let ignore = false;
    fetchFullWeather(location)
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
  }, [location]);

  const handleSelectLocation = (loc: CityLocation) => {
    setLocation(loc);
    localStorage.setItem("weather24h_last_loc", JSON.stringify(loc));
    loadWeather(loc);
  };

  const handleRefresh = () => {
    loadWeather(location);
  };

  const handleUpdateSettings = (newSettings: SettingsState) => {
    setSettings(newSettings);
    localStorage.setItem("weather24h_settings", JSON.stringify(newSettings));
  };

  const isFavorite = favoriteCities.some(
    (c) => c.name.toLowerCase() === location.name.toLowerCase()
  );

  const toggleFavorite = () => {
    let updated: CityLocation[];
    if (isFavorite) {
      updated = favoriteCities.filter((c) => c.name.toLowerCase() !== location.name.toLowerCase());
    } else {
      updated = [...favoriteCities, location];
    }
    setFavoriteCities(updated);
    localStorage.setItem("weather24h_favs", JSON.stringify(updated));
  };

  const removeFavorite = (city: CityLocation) => {
    const updated = favoriteCities.filter((c) => c.name.toLowerCase() !== city.name.toLowerCase());
    setFavoriteCities(updated);
    localStorage.setItem("weather24h_favs", JSON.stringify(updated));
  };

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
      />

      {/* Main Container (z-10) */}
      <main className="relative z-10 max-w-6xl mx-auto px-4 pt-4 space-y-5">
        {/* Quick Bar: Favorite Cities & GPS */}
        {activeTab === "weather" && (
          <FavoriteCitiesQuickBar
            currentCityName={location.name}
            favoriteCities={favoriteCities}
            onSelectCity={handleSelectLocation}
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

        {/* TAB 1: THỜI TIẾT (BỐ CỤC CHUẨN APK WEATHER 24H) */}
        {activeTab === "weather" && weatherData && (
          <div className="space-y-6 animate-in fade-in duration-300">
            {/* 1. SEAMLESS HERO DISPLAY (DẸP TRÁI, PHẢI HIỂN THỊ ĐẦY ĐỦ THÔNG SỐ NHANH) */}
            <HeroWeatherCard
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

            {/* 2. Dự báo 24 giờ tới (Hourly 24h) */}
            <HourlyForecastCard hourly={weatherData.hourly} isDarkTheme={isDarkTheme} />

            {/* 3. Bản đồ Radar Mưa & Mây động trực tiếp */}
            <WeatherRadarCard location={weatherData.location} isDarkTheme={isDarkTheme} />

            {/* 4. Khối Âm lịch Việt Nam & Lịch Vạn Niên chuyên sâu (nếu bật trong cài đặt) */}
            {settings.showLunar && (
              <VietnameseLunarCard
                onOpenDetailedAlmanac={() => setActiveTab("lunar")}
                isDarkTheme={isDarkTheme}
              />
            )}

            {/* 5. Dự báo 7 ngày tới (Daily 7-Day Forecast - xếp trên AI Stylist) */}
            <DailyForecastCard
              daily={weatherData.daily}
              showLunar={settings.showLunar}
              isDarkTheme={isDarkTheme}
            />

            {/* 6. AI Smart Stylist (Tư vấn trang phục thông minh) */}
            <AiStylistCard
              current={weatherData.current}
              todayForecast={weatherData.daily[0]}
              isDarkTheme={isDarkTheme}
            />

            {/* 7. Chất lượng không khí (Air Quality AQI, PM2.5, PM10) */}
            <AirQualityCard airQuality={weatherData.airQuality} isDarkTheme={isDarkTheme} />

            {/* 8. Chỉ số Sinh hoạt & Đời sống (Rửa xe, Giặt phơi, Thể thao, UV) */}
            <LifestyleCard items={weatherData.lifestyles} isDarkTheme={isDarkTheme} />

            {/* 9. Lưới chỉ số thời tiết chi tiết (Weather Metrics Grid) */}
            <WeatherMetricsGrid
              current={weatherData.current}
              todayForecast={weatherData.daily[0]}
              isDarkTheme={isDarkTheme}
            />
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
          <span>Weather 24h & Lịch Âm Việt Nam (v1.0.61)</span>
          <span>•</span>
          <span>Dữ liệu: Open-Meteo & OpenStreetMap</span>
          <span>•</span>
          <span>Thuật toán Âm lịch: TS. Hồ Ngọc Đức</span>
        </div>
        <p className="text-[11px]">
          Giao diện đồng bộ bản Android APK. Hỗ trợ PWA cài đặt trực tiếp lên điện thoại & PC.
        </p>
      </footer>
    </div>
  );
}
