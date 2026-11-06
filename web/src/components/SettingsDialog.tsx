"use client";

import React, { useState } from "react";
import Image from "next/image";
import {
  X,
  Palette,
  Sparkles,
  CloudSun,
  Shield,
  Trash2,
  Check,
  Key,
  Info,
  Sliders
} from "lucide-react";
import { CityLocation } from "../types/weather";

export interface SettingsState {
  theme: "dark" | "light" | "system";
  atmosphereEnabled: boolean;
  atmospherePerformance: "high" | "saver";
  showLunar: boolean;
  tempUnit: "c" | "f";
  windUnit: "kmh" | "ms";
  forecastModel: string;
  geminiApiKey: string;
}

interface Props {
  isOpen: boolean;
  onClose: () => void;
  settings: SettingsState;
  onUpdateSettings: (newSettings: SettingsState) => void;
  favoriteCities: CityLocation[];
  onRemoveFavorite: (city: CityLocation) => void;
  isDarkTheme?: boolean;
}

export default function SettingsDialog({
  isOpen,
  onClose,
  settings,
  onUpdateSettings,
  favoriteCities,
  onRemoveFavorite,
  isDarkTheme = true
}: Props) {
  const [activeTab, setActiveTab] = useState<"general" | "atmosphere" | "forecast" | "favorites" | "ai" | "about">("general");
  const [apiKeyInput, setApiKeyInput] = useState(settings.geminiApiKey || "");

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-md animate-in fade-in">
      <div
        className={`w-full max-w-2xl border rounded-3xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh] transition-colors ${
          isDarkTheme ? "bg-slate-900 border-white/15 text-white" : "bg-white border-slate-200 text-slate-900"
        }`}
      >
        {/* Header */}
        <div className={`flex items-center justify-between p-5 border-b ${isDarkTheme ? "border-white/10" : "border-slate-200"}`}>
          <div className="flex items-center gap-2.5">
            <div className={`p-2 rounded-xl ${isDarkTheme ? "bg-amber-500/20 text-amber-500" : "bg-amber-100 text-amber-700"}`}>
              <Sliders className="w-5 h-5" />
            </div>
            <div>
              <h2 className={`text-base font-bold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>Cài đặt Weather 24h</h2>
              <p className={`text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>Tùy biến giao diện, hiệu ứng & tính năng</p>
            </div>
          </div>

          <button
            onClick={onClose}
            className={`p-2 rounded-full transition-colors ${
              isDarkTheme ? "hover:bg-white/10 text-slate-400 hover:text-white" : "hover:bg-slate-100 text-slate-500 hover:text-slate-900"
            }`}
            title="Đóng cài đặt"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content Body: Sidebar tabs + Content area */}
        <div className="flex flex-col sm:flex-row flex-1 overflow-hidden">
          {/* Tabs Navigation */}
          <div className={`w-full sm:w-48 border-b sm:border-b-0 sm:border-r p-2 sm:p-3 flex sm:flex-col gap-1 overflow-x-auto sm:overflow-x-visible shrink-0 ${
            isDarkTheme ? "border-white/10 bg-white/[0.02]" : "border-slate-200 bg-slate-50/80"
          }`}>
            {[
              { id: "general", label: "Giao diện & Đơn vị", icon: Palette },
              { id: "atmosphere", label: "Hiệu ứng khí quyển", icon: Sparkles },
              { id: "forecast", label: "Mô hình dự báo", icon: CloudSun },
              { id: "favorites", label: "Địa điểm đã lưu", icon: Shield },
              { id: "ai", label: "Gemini AI Key", icon: Key },
              { id: "about", label: "Giới thiệu", icon: Info },
            ].map((tab) => {
              const IconComp = tab.icon;
              const isActive = activeTab === tab.id;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id as typeof activeTab)}
                  className={`flex items-center gap-2.5 px-3 py-2.5 rounded-xl text-xs font-semibold shrink-0 transition-all ${
                    isActive
                      ? "bg-amber-500 text-slate-950 font-bold shadow-xs"
                      : isDarkTheme
                        ? "text-slate-300 hover:bg-white/5 hover:text-white"
                        : "text-slate-700 hover:bg-slate-200/70 hover:text-slate-950 font-medium"
                  }`}
                >
                  <IconComp className={`w-4 h-4 shrink-0 ${isActive ? "text-slate-950" : isDarkTheme ? "text-slate-400" : "text-slate-600"}`} />
                  <span className="truncate">{tab.label}</span>
                </button>
              );
            })}
          </div>

          {/* Tab Panel Content */}
          <div className="flex-1 p-5 overflow-y-auto space-y-5 text-sm">
            {/* 1. GIAO DIỆN & ĐƠN VỊ */}
            {activeTab === "general" && (
              <div className="space-y-4">
                <div>
                  <label className={`text-xs font-bold uppercase tracking-wider ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                    Chủ đề hiển thị
                  </label>
                  <div className="grid grid-cols-3 gap-2 mt-2">
                    {[
                      { id: "light", label: "Sáng (Mặc định)" },
                      { id: "dark", label: "Tối" },
                      { id: "system", label: "Theo hệ thống" }
                    ].map((th) => {
                      const isSelected = settings.theme === th.id;
                      return (
                        <button
                          key={th.id}
                          onClick={() => onUpdateSettings({ ...settings, theme: th.id as SettingsState["theme"] })}
                          className={`p-2.5 rounded-xl border text-xs font-semibold transition-all ${
                            isSelected
                              ? isDarkTheme
                                ? "bg-amber-500/20 border-amber-400 text-amber-300 font-bold shadow-xs"
                                : "bg-amber-100 border-amber-500 text-amber-950 font-bold shadow-xs"
                              : isDarkTheme
                                ? "bg-white/5 border-white/10 text-slate-300 hover:bg-white/10 hover:text-white"
                                : "bg-slate-100/80 border-slate-300/80 text-slate-700 hover:bg-slate-200/80 hover:text-slate-950"
                          }`}
                        >
                          {th.label}
                        </button>
                      );
                    })}
                  </div>
                </div>

                <div className={`pt-3 border-t ${isDarkTheme ? "border-white/10" : "border-slate-200"}`}>
                  <label className={`text-xs font-bold uppercase tracking-wider ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                    Đơn vị nhiệt độ
                  </label>
                  <div className="grid grid-cols-2 gap-2 mt-2">
                    {[
                      { id: "c", label: "Độ C (°C)" },
                      { id: "f", label: "Độ F (°F)" }
                    ].map((u) => {
                      const isSelected = settings.tempUnit === u.id;
                      return (
                        <button
                          key={u.id}
                          onClick={() => onUpdateSettings({ ...settings, tempUnit: u.id as "c" | "f" })}
                          className={`p-2.5 rounded-xl border text-xs font-semibold transition-all ${
                            isSelected
                              ? isDarkTheme
                                ? "bg-amber-500/20 border-amber-400 text-amber-300 font-bold shadow-xs"
                                : "bg-amber-100 border-amber-500 text-amber-950 font-bold shadow-xs"
                              : isDarkTheme
                                ? "bg-white/5 border-white/10 text-slate-300 hover:bg-white/10 hover:text-white"
                                : "bg-slate-100/80 border-slate-300/80 text-slate-700 hover:bg-slate-200/80 hover:text-slate-950"
                          }`}
                        >
                          {u.label}
                        </button>
                      );
                    })}
                  </div>
                </div>

                <div className={`pt-3 border-t ${isDarkTheme ? "border-white/10" : "border-slate-200"}`}>
                  <label className="flex items-center justify-between cursor-pointer group">
                    <div>
                      <div className={`text-sm font-semibold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                        Hiển thị Lịch Âm Việt Nam
                      </div>
                      <div className={`text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>
                        Hiển thị ngày Âm lịch, Can Chi & Tiết khí trên màn hình chính
                      </div>
                    </div>
                    <input
                      type="checkbox"
                      checked={settings.showLunar}
                      onChange={(e) => onUpdateSettings({ ...settings, showLunar: e.target.checked })}
                      className="w-5 h-5 accent-amber-500 rounded cursor-pointer"
                    />
                  </label>
                </div>
              </div>
            )}

            {/* 2. HIỆU ỨNG KHÍ QUYỂN */}
            {activeTab === "atmosphere" && (
              <div className="space-y-4">
                <label className="flex items-center justify-between cursor-pointer group">
                  <div>
                    <div className={`text-sm font-semibold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                      Hiệu ứng thời tiết sống động
                    </div>
                    <div className={`text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>
                      Hạt mưa rơi, sao băng, tia nắng mặt trời, sấm sét toàn màn hình
                    </div>
                  </div>
                  <input
                    type="checkbox"
                    checked={settings.atmosphereEnabled}
                    onChange={(e) => onUpdateSettings({ ...settings, atmosphereEnabled: e.target.checked })}
                    className="w-5 h-5 accent-amber-500 rounded cursor-pointer"
                  />
                </label>

                <div className={`pt-3 border-t ${isDarkTheme ? "border-white/10" : "border-slate-200"}`}>
                  <label className={`text-xs font-bold uppercase tracking-wider ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                    Chế độ hiệu năng
                  </label>
                  <div className="grid grid-cols-2 gap-2 mt-2">
                    {[
                      { id: "high", label: "Mượt mà (60-120 FPS)" },
                      { id: "saver", label: "Tiết kiệm Pin / Máy yếu" }
                    ].map((p) => {
                      const isSelected = settings.atmospherePerformance === p.id;
                      return (
                        <button
                          key={p.id}
                          onClick={() => onUpdateSettings({ ...settings, atmospherePerformance: p.id as "high" | "saver" })}
                          className={`p-2.5 rounded-xl border text-xs font-semibold transition-all ${
                            isSelected
                              ? isDarkTheme
                                ? "bg-amber-500/20 border-amber-400 text-amber-300 font-bold shadow-xs"
                                : "bg-amber-100 border-amber-500 text-amber-950 font-bold shadow-xs"
                              : isDarkTheme
                                ? "bg-white/5 border-white/10 text-slate-300 hover:bg-white/10 hover:text-white"
                                : "bg-slate-100/80 border-slate-300/80 text-slate-700 hover:bg-slate-200/80 hover:text-slate-950"
                          }`}
                        >
                          {p.label}
                        </button>
                      );
                    })}
                  </div>
                </div>
              </div>
            )}

            {/* 3. MÔ HÌNH DỰ BÁO */}
            {activeTab === "forecast" && (
              <div className="space-y-3">
                <div className={`text-xs font-bold uppercase tracking-wider ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                  Chọn mô hình dự báo AI & Khí tượng
                </div>
                {[
                  { id: "best_match", title: "Tự động tối ưu (Best Match)", desc: "Tự động kết hợp mô hình tốt nhất theo tọa độ thực tế." },
                  { id: "jma_seamless", title: "JMA (Khí tượng Nhật Bản - Đề xuất cho VN)", desc: "Mô hình khí tượng chuyên biệt Châu Á, độ chính xác cao cho gió mùa nhiệt đới." },
                  { id: "ecmwf_ifs", title: "ECMWF (Châu Âu IFS)", desc: "Mô hình dự báo độ chính xác cao nhất thế giới." },
                  { id: "gfs_seamless", title: "GFS (Hoa Kỳ NOAA)", desc: "Mô hình khí quyển toàn cầu của Mỹ." },
                  { id: "icon_seamless", title: "DWD ICON (Cục Khí Tượng Đức)", desc: "Mô hình dự báo thời tiết chuẩn Châu Âu của Đức." }
                ].map((m) => {
                  const isSelected = settings.forecastModel === m.id;
                  return (
                    <button
                      key={m.id}
                      onClick={() => onUpdateSettings({ ...settings, forecastModel: m.id })}
                      className={`w-full text-left p-3 rounded-2xl border transition-all ${
                        isSelected
                          ? isDarkTheme
                            ? "bg-amber-500/20 border-amber-400"
                            : "bg-amber-50 border-amber-500 shadow-xs"
                          : isDarkTheme
                            ? "bg-white/5 border-white/10 hover:bg-white/10"
                            : "bg-slate-50 border-slate-200 hover:bg-slate-100"
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <span className={`text-xs font-bold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>{m.title}</span>
                        {isSelected && <Check className={`w-4 h-4 ${isDarkTheme ? "text-amber-400" : "text-amber-600 font-bold"}`} />}
                      </div>
                      <p className={`text-[11px] mt-0.5 ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>{m.desc}</p>
                    </button>
                  );
                })}
              </div>
            )}

            {/* 4. ĐỊA ĐIỂM ĐÃ LƯU */}
            {activeTab === "favorites" && (
              <div className="space-y-3">
                <div className={`text-xs font-bold uppercase tracking-wider ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                  Danh sách thành phố yêu thích
                </div>
                {favoriteCities.length === 0 ? (
                  <div className={`py-8 text-center text-xs ${isDarkTheme ? "text-slate-400" : "text-slate-500"}`}>
                    Chưa có thành phố nào được lưu. Hãy bấm biểu tượng ngôi sao ⭐ trên khối thời tiết để thêm nhanh!
                  </div>
                ) : (
                  favoriteCities.map((city, idx) => (
                    <div
                      key={idx}
                      className={`flex items-center justify-between p-3 rounded-xl border transition-colors ${
                        isDarkTheme ? "bg-white/5 border-white/10" : "bg-slate-50 border-slate-200"
                      }`}
                    >
                      <div>
                        <div className={`text-xs font-bold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>{city.name}</div>
                        <div className={`text-[11px] ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>{city.country}</div>
                      </div>
                      <button
                        onClick={() => onRemoveFavorite(city)}
                        className={`p-1.5 rounded-lg transition-colors ${
                          isDarkTheme ? "text-rose-400 hover:bg-rose-500/20" : "text-rose-600 hover:bg-rose-100"
                        }`}
                        title="Xóa khỏi yêu thích"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  ))
                )}
              </div>
            )}

            {/* 5. GEMINI AI KEY */}
            {activeTab === "ai" && (
              <div className="space-y-3">
                <div className={`text-xs font-bold uppercase tracking-wider ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                  Google Gemini API Key
                </div>
                <p className={`text-xs leading-relaxed ${isDarkTheme ? "text-slate-400" : "text-slate-600"}`}>
                  Weather 24h tích hợp sẵn trí tuệ nhân tạo Gemini AI Stylist. Nếu bạn có khóa API cá nhân, hãy nhập vào đây để tăng tốc độ phân tích và không bị giới hạn số lần gọi.
                </p>
                <input
                  type="password"
                  value={apiKeyInput}
                  onChange={(e) => setApiKeyInput(e.target.value)}
                  placeholder="AIzaSy..."
                  className={`w-full p-2.5 rounded-xl border text-xs font-mono transition-colors ${
                    isDarkTheme
                      ? "bg-white/10 border-white/20 text-white placeholder-slate-500"
                      : "bg-slate-50 border-slate-300 text-slate-900 placeholder-slate-400 focus:bg-white focus:border-amber-500"
                  }`}
                />
                <button
                  onClick={() => {
                    onUpdateSettings({ ...settings, geminiApiKey: apiKeyInput.trim() });
                    alert("Đã lưu khóa API Gemini thành công!");
                  }}
                  className="px-4 py-2 rounded-xl bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold text-xs transition-colors shadow-xs"
                >
                  Lưu khóa API
                </button>
              </div>
            )}

            {/* 6. GIỚI THIỆU */}
            {activeTab === "about" && (
              <div className={`space-y-3 text-xs leading-relaxed ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                <div
                  className={`p-4 rounded-2xl border text-center flex flex-col items-center justify-center ${
                    isDarkTheme ? "bg-white/5 border-white/10" : "bg-slate-50 border-slate-200"
                  }`}
                >
                  <div className="relative w-16 h-16 rounded-2xl overflow-hidden shadow-lg mb-2.5 border border-white/20">
                    <Image
                      src="/logo.png"
                      alt="Weather 24h"
                      width={64}
                      height={64}
                      className="w-full h-full object-cover"
                    />
                  </div>
                  <div className={`text-base font-bold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                    Weather 24h & Lịch Âm Việt Nam
                  </div>
                  <div className={`text-[11px] font-semibold mt-0.5 ${isDarkTheme ? "text-amber-400" : "text-amber-700 font-bold"}`}>
                    Phiên bản Web • Đồng bộ v1.0.62 APK
                  </div>
                </div>

                <div className={`space-y-2 pt-2 text-xs ${isDarkTheme ? "text-slate-300" : "text-slate-800"}`}>
                  <p>• <strong>Dữ liệu Khí tượng:</strong> Open-Meteo API & OpenStreetMap Geocoding.</p>
                  <p>• <strong>Dữ liệu Radar Mây Mưa:</strong> Windy & RainViewer Live Radar.</p>
                  <p>• <strong>Thuật toán Âm Lịch:</strong> Nghiên cứu thiên văn học chính thống của TS. Hồ Ngọc Đức (GMT+7).</p>
                  <p>• <strong>Công nghệ:</strong> Next.js 16, React 19, Tailwind CSS v4, PWA.</p>
                  <p>
                    • <strong>Bản Web:</strong>{" "}
                    <a
                      href="https://weather-24h.vercel.app"
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-amber-500 hover:underline font-semibold"
                    >
                      weather-24h.vercel.app
                    </a>
                  </p>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
