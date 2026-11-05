"use client";

import React, { useState, useEffect } from "react";
import Image from "next/image";
import {
  MapPin,
  Search,
  Crosshair,
  RefreshCw,
  X,
  Calendar,
  CloudSun,
  Radar,
  Settings
} from "lucide-react";
import { CityLocation } from "../types/weather";
import {
  searchCities,
  reverseGeocode,
  VIETNAM_MAJOR_CITIES
} from "../services/weatherApi";

interface Props {
  currentLocation: CityLocation;
  onSelectLocation: (loc: CityLocation) => void;
  onRefresh: () => void;
  isLoading: boolean;
  activeTab: "weather" | "lunar" | "radar";
  onTabChange: (tab: "weather" | "lunar" | "radar") => void;
  onOpenSettings: () => void;
  isSearchOpen?: boolean;
  onSetSearchOpen?: (open: boolean) => void;
  isDarkTheme?: boolean;
}

export default function Header({
  currentLocation,
  onSelectLocation,
  onRefresh,
  isLoading,
  activeTab,
  onTabChange,
  onOpenSettings,
  isSearchOpen: controlledSearchOpen,
  onSetSearchOpen,
  isDarkTheme = true
}: Props) {
  const [internalSearchOpen, setInternalSearchOpen] = useState(false);
  const isSearchOpen = controlledSearchOpen !== undefined ? controlledSearchOpen : internalSearchOpen;
  const setIsSearchOpen = (open: boolean) => {
    if (onSetSearchOpen) onSetSearchOpen(open);
    else setInternalSearchOpen(open);
  };

  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState<CityLocation[]>(VIETNAM_MAJOR_CITIES.slice(0, 8));
  const [isSearching, setIsSearching] = useState(false);

  useEffect(() => {
    const timer = setTimeout(async () => {
      if (searchQuery.trim()) {
        setIsSearching(true);
        const res = await searchCities(searchQuery);
        setSearchResults(res);
        setIsSearching(false);
      } else {
        setSearchResults(VIETNAM_MAJOR_CITIES.slice(0, 8));
      }
    }, 350);
    return () => clearTimeout(timer);
  }, [searchQuery]);

  const handleLocateMe = () => {
    if (!navigator.geolocation) {
      alert("Trình duyệt của bạn không hỗ trợ định vị GPS.");
      return;
    }
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        const lat = pos.coords.latitude;
        const lon = pos.coords.longitude;
        onSelectLocation({
          name: "Đang xác định vị trí...",
          country: "Việt Nam",
          admin1: "GPS Tọa độ",
          latitude: lat,
          longitude: lon,
          isCurrentLocation: true
        });
        setIsSearchOpen(false);

        try {
          const exact = await reverseGeocode(lat, lon);
          onSelectLocation(exact);
        } catch {
          onSelectLocation({
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
    <>
      <header
        className={`sticky top-0 z-40 w-full backdrop-blur-xl border-b px-4 py-3 transition-colors ${
          isDarkTheme
            ? "bg-slate-950/70 border-white/10 text-white"
            : "bg-white/80 border-slate-200/80 text-slate-900 shadow-sm"
        }`}
      >
        <div className="max-w-6xl mx-auto flex items-center justify-between gap-3">
          {/* Logo / App Name đồng bộ tuyệt đối với APK Android */}
          <div className="flex items-center gap-2.5">
            <div className="relative w-10 h-10 rounded-2xl overflow-hidden shadow-lg shadow-blue-500/20 border border-white/20 bg-slate-900 shrink-0">
              <Image
                src="/logo.png"
                alt="Weather 24h"
                width={40}
                height={40}
                className="w-full h-full object-cover"
                priority
              />
            </div>
            <div>
              <h1 className="text-lg font-bold tracking-tight flex items-center gap-1.5">
                Weather 24h
                <span className="text-[10px] uppercase font-semibold px-1.5 py-0.5 rounded-full bg-amber-500/20 text-amber-500 border border-amber-500/30">
                  Lịch Âm
                </span>
              </h1>
              <p className={`text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                Dự báo chuẩn & Lịch Vạn Niên
              </p>
            </div>
          </div>

          {/* Navigation Tabs (Desktop & Tablet) */}
          <nav
            className={`hidden md:flex items-center gap-1 p-1 rounded-xl border ${
              isDarkTheme ? "bg-white/5 border-white/10" : "bg-slate-100 border-slate-200"
            }`}
          >
            <button
              onClick={() => onTabChange("weather")}
              className={`px-3.5 py-1.5 rounded-lg text-xs font-medium transition-all flex items-center gap-1.5 ${
                activeTab === "weather"
                  ? "bg-blue-600 text-white shadow-md shadow-blue-600/30"
                  : isDarkTheme ? "text-slate-300 hover:text-white hover:bg-white/5" : "text-slate-600 hover:text-slate-900 hover:bg-white"
              }`}
            >
              <CloudSun className="w-4 h-4" />
              Thời Tiết
            </button>
            <button
              onClick={() => onTabChange("lunar")}
              className={`px-3.5 py-1.5 rounded-lg text-xs font-medium transition-all flex items-center gap-1.5 ${
                activeTab === "lunar"
                  ? "bg-amber-600 text-white shadow-md shadow-amber-600/30"
                  : isDarkTheme ? "text-slate-300 hover:text-white hover:bg-white/5" : "text-slate-600 hover:text-slate-900 hover:bg-white"
              }`}
            >
              <Calendar className="w-4 h-4" />
              Lịch Vạn Niên
            </button>
            <button
              onClick={() => onTabChange("radar")}
              className={`px-3.5 py-1.5 rounded-lg text-xs font-medium transition-all flex items-center gap-1.5 ${
                activeTab === "radar"
                  ? "bg-emerald-600 text-white shadow-md shadow-emerald-600/30"
                  : isDarkTheme ? "text-slate-300 hover:text-white hover:bg-white/5" : "text-slate-600 hover:text-slate-900 hover:bg-white"
              }`}
            >
              <Radar className="w-4 h-4" />
              Radar Mưa
            </button>
          </nav>

          {/* Location Selector & Actions */}
          <div className="flex items-center gap-2">
            <button
              onClick={() => setIsSearchOpen(true)}
              className={`flex items-center gap-2 px-3 py-1.5 rounded-xl border text-xs font-medium max-w-[150px] sm:max-w-xs truncate transition-all ${
                isDarkTheme
                  ? "bg-white/10 hover:bg-white/15 border-white/15 text-slate-200"
                  : "bg-white hover:bg-slate-50 border-slate-300 text-slate-800 shadow-sm"
              }`}
            >
              <MapPin className="w-3.5 h-3.5 text-amber-500 shrink-0" />
              <span className="truncate">
                {currentLocation.name}
              </span>
              <Search className="w-3.5 h-3.5 opacity-60 ml-auto shrink-0" />
            </button>

            <button
              onClick={handleLocateMe}
              title="Định vị vị trí của tôi"
              className={`p-2 rounded-xl border transition-all ${
                isDarkTheme
                  ? "bg-white/10 hover:bg-white/15 border-white/15 text-slate-200 hover:text-amber-300"
                  : "bg-white hover:bg-slate-50 border-slate-300 text-slate-700 shadow-sm hover:text-amber-600"
              }`}
            >
              <Crosshair className="w-4 h-4" />
            </button>

            <button
              onClick={onRefresh}
              disabled={isLoading}
              title="Làm mới thời tiết"
              className={`p-2 rounded-xl border transition-all ${
                isDarkTheme
                  ? "bg-white/10 hover:bg-white/15 border-white/15 text-slate-200"
                  : "bg-white hover:bg-slate-50 border-slate-300 text-slate-700 shadow-sm"
              } ${isLoading ? "animate-spin text-blue-500" : ""}`}
            >
              <RefreshCw className="w-4 h-4" />
            </button>

            {/* Nút Cài đặt (Settings ⚙️) */}
            <button
              onClick={onOpenSettings}
              title="Cài đặt giao diện & tùy chọn"
              className={`p-2 rounded-xl border transition-all ${
                isDarkTheme
                  ? "bg-white/10 hover:bg-white/15 border-white/15 text-slate-200 hover:text-amber-300"
                  : "bg-white hover:bg-slate-50 border-slate-300 text-slate-700 shadow-sm hover:text-amber-600"
              }`}
            >
              <Settings className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Mobile Navigation Bar */}
        <div
          className={`flex md:hidden items-center justify-around gap-1 mt-2.5 pt-2 border-t ${
            isDarkTheme ? "border-white/5" : "border-slate-200"
          }`}
        >
          <button
            onClick={() => onTabChange("weather")}
            className={`flex-1 py-1.5 rounded-lg text-xs font-medium flex items-center justify-center gap-1.5 transition-all ${
              activeTab === "weather"
                ? "bg-blue-600 text-white font-semibold shadow-sm"
                : isDarkTheme ? "text-slate-400 hover:text-white" : "text-slate-600 hover:text-slate-900"
            }`}
          >
            <CloudSun className="w-3.5 h-3.5" />
            Thời Tiết
          </button>
          <button
            onClick={() => onTabChange("lunar")}
            className={`flex-1 py-1.5 rounded-lg text-xs font-medium flex items-center justify-center gap-1.5 transition-all ${
              activeTab === "lunar"
                ? "bg-amber-600 text-white font-semibold shadow-sm"
                : isDarkTheme ? "text-slate-400 hover:text-white" : "text-slate-600 hover:text-slate-900"
            }`}
          >
            <Calendar className="w-3.5 h-3.5" />
            Lịch Vạn Niên
          </button>
          <button
            onClick={() => onTabChange("radar")}
            className={`flex-1 py-1.5 rounded-lg text-xs font-medium flex items-center justify-center gap-1.5 transition-all ${
              activeTab === "radar"
                ? "bg-emerald-600 text-white font-semibold shadow-sm"
                : isDarkTheme ? "text-slate-400 hover:text-white" : "text-slate-600 hover:text-slate-900"
            }`}
          >
            <Radar className="w-3.5 h-3.5" />
            Radar Mưa
          </button>
        </div>
      </header>

      {/* City Search Modal */}
      {isSearchOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-md animate-in fade-in">
          <div
            className={`w-full max-w-lg border rounded-3xl shadow-2xl p-5 overflow-hidden flex flex-col max-h-[85vh] ${
              isDarkTheme
                ? "bg-slate-900 border-white/15 text-white"
                : "bg-white border-slate-200 text-slate-900"
            }`}
          >
            <div className="flex items-center justify-between pb-3 border-b border-slate-500/20">
              <h2 className="text-base font-bold flex items-center gap-2">
                <Search className="w-4 h-4 text-amber-500" />
                Tìm kiếm Tỉnh / Thành phố
              </h2>
              <button
                onClick={() => setIsSearchOpen(false)}
                className="p-1.5 rounded-full hover:bg-slate-500/10 text-slate-400 hover:text-slate-600 transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Input Bar */}
            <div className="relative mt-4">
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Nhập tên tỉnh, thành phố, xã/phường (ví dụ: Đà Nẵng, Cần Thơ)..."
                autoFocus
                className={`w-full pl-10 pr-4 py-2.5 rounded-xl border focus:outline-none focus:ring-2 focus:ring-amber-500 text-sm ${
                  isDarkTheme
                    ? "bg-white/5 border-white/15 text-white placeholder-slate-400"
                    : "bg-slate-50 border-slate-300 text-slate-900 placeholder-slate-500"
                }`}
              />
              <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
            </div>

            {/* GPS quick pick button */}
            <button
              onClick={handleLocateMe}
              className="mt-3 flex items-center gap-2 px-3 py-2 rounded-xl bg-blue-600/15 hover:bg-blue-600/25 text-blue-600 dark:text-blue-300 border border-blue-500/30 text-xs font-semibold transition-all"
            >
              <Crosshair className="w-4 h-4" />
              Sử dụng vị trí hiện tại (GPS tự động)
            </button>

            {/* Results list */}
            <div className="mt-4 overflow-y-auto space-y-1.5 flex-1 pr-1">
              <div className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider mb-2">
                {searchQuery ? "Kết quả tìm kiếm" : "Thành phố phổ biến"}
              </div>

              {isSearching && (
                <div className="py-8 text-center text-xs text-slate-400">
                  <RefreshCw className="w-5 h-5 animate-spin mx-auto mb-2 text-amber-500" />
                  Đang tìm kiếm dữ liệu...
                </div>
              )}

              {!isSearching && searchResults.length === 0 && (
                <div className="py-8 text-center text-xs text-slate-400">
                  Không tìm thấy địa điểm phù hợp. Hãy thử tên khác!
                </div>
              )}

              {!isSearching &&
                searchResults.map((city, idx) => {
                  const isSovereign =
                    city.name.includes("Hoàng Sa") ||
                    city.name.includes("Trường Sa") ||
                    (city.admin1 && (city.admin1.includes("Hoàng Sa") || city.admin1.includes("Trường Sa")));

                  return (
                    <button
                      key={`${city.latitude}-${city.longitude}-${idx}`}
                      onClick={() => {
                        onSelectLocation(city);
                        setIsSearchOpen(false);
                      }}
                      className={`w-full flex items-center justify-between p-3 rounded-2xl border transition-all text-left group ${
                        isDarkTheme
                          ? "hover:bg-white/10 border-transparent hover:border-white/10"
                          : "hover:bg-slate-100 border-transparent hover:border-slate-200"
                      } ${
                        isSovereign
                          ? isDarkTheme
                            ? "bg-amber-500/10 border-amber-500/30"
                            : "bg-amber-50 border-amber-300 shadow-sm"
                          : ""
                      }`}
                    >
                      <div>
                        <div className="text-sm font-semibold flex items-center gap-1.5 group-hover:text-amber-500 transition-colors">
                          {city.name}
                          {isSovereign && (
                            <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-600 dark:text-amber-400 border border-amber-500/30">
                              ⭐ Việt Nam
                            </span>
                          )}
                        </div>
                        <div className={`text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                          {city.admin1 ? `${city.admin1}, ` : ""}
                          {city.country}
                        </div>
                      </div>
                      <MapPin className="w-4 h-4 text-slate-400 group-hover:text-amber-500 transition-colors shrink-0" />
                    </button>
                  );
                })}
            </div>
          </div>
        </div>
      )}
    </>
  );
}
