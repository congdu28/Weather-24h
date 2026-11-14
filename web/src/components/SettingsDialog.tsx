"use client";

import React, { useState, useEffect, useSyncExternalStore } from "react";
import { createPortal } from "react-dom";
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
  Sliders,
  LayoutGrid,
  ChevronUp,
  ChevronDown,
  RotateCcw
} from "lucide-react";
import {
  CityLocation,
  AppSettings as SettingsState,
  GlassPreset,
  DepthPreset,
  BlurPreset,
  RadiusPreset,
  AccentPreset,
  WidgetLayoutState,
  WidgetId,
  DEFAULT_WIDGET_ORDER,
  AtmosphereParticlesState,
  ACCENT_THEMES
} from "../types/weather";

export type { SettingsState };

interface Props {
  isOpen: boolean;
  onClose: () => void;
  settings: SettingsState;
  onUpdateSettings: (newSettings: SettingsState) => void;
  favoriteCities: CityLocation[];
  onRemoveFavorite: (city: CityLocation) => void;
  isDarkTheme?: boolean;
}

const emptySubscribe = () => () => {};

export default function SettingsDialog({
  isOpen,
  onClose,
  settings,
  onUpdateSettings,
  favoriteCities,
  onRemoveFavorite,
  isDarkTheme = true
}: Props) {
  const [activeTab, setActiveTab] = useState<
    "general" | "layout" | "atmosphere" | "forecast" | "favorites" | "ai" | "about"
  >("general");
  const [apiKeyInput, setApiKeyInput] = useState(settings.geminiApiKey || "");

  // Helpers cập nhật state an toàn
  const update = (partial: Partial<SettingsState>) => {
    onUpdateSettings({ ...settings, ...partial });
  };

  const updateWidget = (key: keyof WidgetLayoutState, val: boolean) => {
    onUpdateSettings({
      ...settings,
      widgets: {
        ...settings.widgets,
        [key]: val
      }
    });
  };

  const updateParticle = (key: keyof AtmosphereParticlesState, val: unknown) => {
    onUpdateSettings({
      ...settings,
      atmosphereParticles: {
        ...settings.atmosphereParticles,
        [key]: val
      }
    });
  };

  const WIDGET_DEF_MAP: Record<WidgetId, { label: string; desc: string }> = {
    hero: { label: "Khối thời tiết chính (Hero)", desc: "Nhiệt độ lớn, vị trí, icon động & tóm tắt" },
    astronomy: { label: "Quỹ đạo Mặt Trời & Tuần Trăng", desc: "Vòng cung mặt trời, giờ vàng & hình trăng" },
    insights: { label: "Phân tích & Cảnh báo thông minh", desc: "Độ chênh nhiệt, cửa sổ mưa, chỉ số UV" },
    cityCompare: { label: "So sánh thời tiết đa thành phố", desc: "Xem nhanh thời tiết các thành phố yêu thích" },
    hourly: { label: "Dự báo 24 giờ tới (Hourly)", desc: "Biểu đồ xu hướng nhiệt độ, lượng mưa, gió" },
    radar: { label: "Bản đồ Radar Mây Mưa trực tiếp", desc: "Theo dõi mưa thực tế từ vệ tinh & radar" },
    lunar: { label: "Lịch Âm & Lịch Vạn Niên", desc: "Giờ hoàng đạo, can chi, việc nên làm" },
    daily: { label: "Dự báo 7 ngày tới (Daily)", desc: "Chi tiết nhiệt độ tuần & dự báo mở rộng" },
    aiStylist: { label: "AI Smart Stylist", desc: "Tư vấn phối đồ thông minh theo thời tiết" },
    airQuality: { label: "Chất lượng không khí (AQI)", desc: "Chỉ số ô nhiễm bụi mịn PM2.5, PM10" },
    lifestyle: { label: "Chỉ số Sinh hoạt & Đời sống", desc: "Rửa xe, giặt phơi, hoạt động thể thao" },
    metrics: { label: "Lưới thông số khí tượng chi tiết", desc: "Áp suất, tầm nhìn, độ ẩm, gió giật" }
  };

  const currentOrder: WidgetId[] = React.useMemo(() => {
    const base = settings.widgetOrder || DEFAULT_WIDGET_ORDER;
    const valid = base.filter((id) => DEFAULT_WIDGET_ORDER.includes(id));
    DEFAULT_WIDGET_ORDER.forEach((id) => {
      if (!valid.includes(id)) valid.push(id);
    });
    return valid;
  }, [settings.widgetOrder]);

  const moveWidget = (index: number, direction: "up" | "down") => {
    const targetIndex = direction === "up" ? index - 1 : index + 1;
    if (targetIndex < 0 || targetIndex >= currentOrder.length) return;
    const newOrder = [...currentOrder];
    const temp = newOrder[index];
    newOrder[index] = newOrder[targetIndex];
    newOrder[targetIndex] = temp;
    update({ widgetOrder: newOrder });
  };

  const resetWidgetOrder = () => {
    update({ widgetOrder: DEFAULT_WIDGET_ORDER });
  };

  const currentAccent = ACCENT_THEMES[settings.accentColor] || ACCENT_THEMES.amber;

  const mounted = useSyncExternalStore(emptySubscribe, () => true, () => false);

  // Khóa scroll khi mở modal Cài đặt
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = "hidden";
      return () => {
        document.body.style.overflow = "unset";
      };
    }
  }, [isOpen]);

  if (!isOpen || !mounted) return null;

  return createPortal(
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-3 sm:p-4 bg-black/75 backdrop-blur-md animate-in fade-in">
      <div
        className={`w-full max-w-3xl border rounded-3xl shadow-2xl overflow-hidden flex flex-col max-h-[92vh] transition-colors ${
          isDarkTheme ? "bg-slate-900/95 border-white/15 text-white" : "bg-white/95 border-slate-200 text-slate-900"
        }`}
      >
        {/* Header */}
        <div className={`flex items-center justify-between p-4 sm:p-5 border-b ${isDarkTheme ? "border-white/10" : "border-slate-200"}`}>
          <div className="flex items-center gap-3">
            <div className={`p-2.5 rounded-2xl ${isDarkTheme ? "bg-amber-500/20 text-amber-400" : "bg-amber-100 text-amber-700"}`}>
              <Sliders className="w-5 h-5" />
            </div>
            <div>
              <h2 className={`text-base font-extrabold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                Cài đặt Weather 24h
              </h2>
              <p className={`text-xs ${isDarkTheme ? "text-slate-300 font-medium" : "text-slate-600 font-medium"}`}>
                Tùy biến chiều sâu, độ đậm nhạt, hiệu ứng & tính năng mở rộng
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className={`p-2 rounded-full transition-colors ${
              isDarkTheme ? "hover:bg-white/10 text-slate-300 hover:text-white" : "hover:bg-slate-100 text-slate-500 hover:text-slate-900"
            }`}
            title="Đóng cài đặt"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content Body: Sidebar tabs + Content area */}
        <div className="flex flex-col sm:flex-row flex-1 overflow-hidden">
          {/* Tabs Navigation */}
          <div className={`w-full sm:w-52 border-b sm:border-b-0 sm:border-r p-2 sm:p-3 flex sm:flex-col gap-1 overflow-x-auto sm:overflow-x-visible shrink-0 ${
            isDarkTheme ? "border-white/10 bg-white/[0.02]" : "border-slate-200 bg-slate-50/80"
          }`}>
            {[
              { id: "general", label: "Giao diện & Chiều sâu", icon: Palette },
              { id: "layout", label: "Bố cục & Khối hiển thị", icon: LayoutGrid },
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
                  <IconComp className={`w-4 h-4 shrink-0 ${isActive ? "text-slate-950" : isDarkTheme ? "text-slate-300" : "text-slate-600"}`} />
                  <span className="truncate">{tab.label}</span>
                </button>
              );
            })}
          </div>

          {/* Tab Panel Content */}
          <div className="flex-1 p-4 sm:p-6 overflow-y-auto space-y-6 text-sm">
            {/* 1. GIAO DIỆN & CHIỀU SÂU THẨM MỸ */}
            {activeTab === "general" && (
              <div className="space-y-5 animate-in fade-in duration-200">
                {/* Live Preview Box */}
                <div>
                  <label className={`text-xs font-bold uppercase tracking-wider block mb-2 ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                    Xem trước khối hiển thị (Live Preview)
                  </label>
                  <div
                    className="p-4 rounded-2xl border transition-all duration-300 flex items-center justify-between"
                    style={{
                      backgroundColor: isDarkTheme
                        ? `rgba(15, 23, 42, ${settings.customOpacity / 100})`
                        : `rgba(255, 255, 255, ${settings.customOpacity / 100})`,
                      boxShadow: settings.depthLevel === "flat"
                        ? "none"
                        : settings.depthLevel === "soft"
                        ? "0 10px 25px -5px rgba(0, 0, 0, 0.2)"
                        : settings.depthLevel === "deep"
                        ? "0 20px 40px -10px rgba(0, 0, 0, 0.4)"
                        : `0 12px 35px -5px rgba(${currentAccent.rgb}, 0.35)`,
                      borderColor: settings.depthLevel === "glow"
                        ? `rgba(${currentAccent.rgb}, 0.5)`
                        : isDarkTheme ? "rgba(255, 255, 255, 0.15)" : "rgba(226, 232, 240, 0.9)",
                      borderRadius: settings.borderRadius === "compact" ? "14px" : settings.borderRadius === "smooth" ? "22px" : "30px"
                    }}
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-black">Hà Nội • Mẫu Thẻ Thời Tiết</span>
                        <span
                          className="text-[10px] px-2 py-0.5 rounded-full font-bold"
                          style={{
                            backgroundColor: `rgba(${currentAccent.rgb}, 0.15)`,
                            color: currentAccent.hex
                          }}
                        >
                          Đậm nhạt: {settings.customOpacity}%
                        </span>
                      </div>
                      <p className={`text-xs mt-1 ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                        28°C • Trời quang, nắng đẹp • Gió 14 km/h
                      </p>
                    </div>
                    <div
                      className="p-2.5 rounded-xl text-white font-bold text-xs shadow-xs"
                      style={{ backgroundColor: currentAccent.hex }}
                    >
                      Weather 24h
                    </div>
                  </div>
                </div>

                {/* Theme Mode */}
                <div className={`pt-4 border-t ${isDarkTheme ? "border-white/10" : "border-slate-200"}`}>
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
                          onClick={() => update({ theme: th.id as SettingsState["theme"] })}
                          className={`p-2.5 rounded-xl border text-xs font-semibold transition-all ${
                            isSelected
                              ? isDarkTheme
                                ? "bg-amber-500/20 border-amber-400 text-amber-300 font-bold shadow-xs"
                                : "bg-amber-100 border-amber-500 text-amber-950 font-bold shadow-xs"
                              : isDarkTheme
                                ? "bg-white/5 border-white/10 text-slate-300 hover:bg-white/10"
                                : "bg-slate-100/80 border-slate-300/80 text-slate-700 hover:bg-slate-200/80"
                          }`}
                        >
                          {th.label}
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Accent Color Palette */}
                <div className={`pt-4 border-t ${isDarkTheme ? "border-white/10" : "border-slate-200"}`}>
                  <label className={`text-xs font-bold uppercase tracking-wider ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                    Màu sắc điểm nhấn (Accent Theme)
                  </label>
                  <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 mt-2">
                    {(Object.keys(ACCENT_THEMES) as AccentPreset[]).map((key) => {
                      const item = ACCENT_THEMES[key];
                      const isSelected = settings.accentColor === key;
                      return (
                        <button
                          key={key}
                          onClick={() => update({ accentColor: key })}
                          className={`flex items-center gap-2.5 p-2.5 rounded-xl border text-xs font-bold transition-all text-left ${
                            isSelected
                              ? isDarkTheme
                                ? "bg-white/15 border-white/40 ring-1 ring-white/30"
                                : "bg-slate-100 border-slate-400 ring-1 ring-slate-400"
                              : isDarkTheme
                                ? "bg-white/5 border-white/10 hover:bg-white/10"
                                : "bg-slate-50 border-slate-200 hover:bg-slate-100"
                          }`}
                        >
                          <span
                            className="w-3.5 h-3.5 rounded-full shrink-0 shadow-xs"
                            style={{ backgroundColor: item.hex }}
                          />
                          <span className="truncate">{item.name}</span>
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Opacity & Glass Sliders */}
                <div className={`pt-4 border-t ${isDarkTheme ? "border-white/10" : "border-slate-200"}`}>
                  <div className="flex items-center justify-between">
                    <div>
                      <label className={`text-xs font-bold uppercase tracking-wider ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                        Độ đậm nhạt & Trong suốt các khối
                      </label>
                      <p className={`text-[11px] ${isDarkTheme ? "text-slate-300" : "text-slate-500"}`}>
                        Kính siêu trong nhìn thấy nền động thời tiết hoặc khối đặc chống chói
                      </p>
                    </div>
                    <span className="text-sm font-black text-amber-500 font-mono px-2 py-0.5 rounded-lg bg-amber-500/10">
                      {settings.customOpacity}%
                    </span>
                  </div>

                  {/* Range Slider */}
                  <div className="mt-3">
                    <input
                      type="range"
                      min={50}
                      max={100}
                      step={2}
                      value={settings.customOpacity}
                      onChange={(e) => update({ customOpacity: Number(e.target.value) })}
                      className="w-full cursor-pointer"
                    />
                  </div>

                  {/* Preset Buttons */}
                  <div className="grid grid-cols-4 gap-2 mt-3">
                    {[
                      { label: "Siêu trong", val: 65, preset: "ultra" },
                      { label: "Kính mờ", val: 80, preset: "frosted" },
                      { label: "Cân bằng", val: 90, preset: "balanced" },
                      { label: "Khối đặc", val: 98, preset: "solid" }
                    ].map((item) => (
                      <button
                        key={item.val}
                        onClick={() => update({ customOpacity: item.val, glassStyle: item.preset as GlassPreset })}
                        className={`py-2 px-1 rounded-xl text-center text-xs font-bold border transition-all ${
                          settings.customOpacity === item.val
                            ? "bg-amber-500 text-slate-950 font-black shadow-xs"
                            : isDarkTheme
                            ? "bg-white/5 border-white/10 hover:bg-white/10 text-slate-300"
                            : "bg-slate-100 border-slate-200 hover:bg-slate-200 text-slate-700"
                        }`}
                      >
                        {item.label}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Depth & Elevation (Chiều Sâu & Đổ Bóng) */}
                <div className={`pt-4 border-t ${isDarkTheme ? "border-white/10" : "border-slate-200"}`}>
                  <label className={`text-xs font-bold uppercase tracking-wider ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                    Chiều sâu & Đổ bóng (Depth & 3D Elevation)
                  </label>
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 mt-2">
                    {[
                      { id: "flat", label: "Phẳng (Tối giản)", desc: "Không bóng đổ" },
                      { id: "soft", label: "Nổi mềm mại", desc: "Bóng nhẹ tự nhiên" },
                      { id: "deep", label: "Nổi sâu 3D", desc: "Bóng đa tầng" },
                      { id: "glow", label: "Hào quang sáng", desc: "Phát sáng viền" }
                    ].map((item) => {
                      const isSelected = settings.depthLevel === item.id;
                      return (
                        <button
                          key={item.id}
                          onClick={() => update({ depthLevel: item.id as DepthPreset })}
                          className={`p-3 rounded-2xl border text-left transition-all ${
                            isSelected
                              ? isDarkTheme
                                ? "bg-amber-500/20 border-amber-400 text-amber-300 font-bold shadow-xs"
                                : "bg-amber-100 border-amber-500 text-amber-950 font-bold shadow-xs"
                              : isDarkTheme
                                ? "bg-white/5 border-white/10 text-slate-300 hover:bg-white/10"
                                : "bg-slate-50 border-slate-200 text-slate-700 hover:bg-slate-100"
                          }`}
                        >
                          <div className="text-xs font-bold">{item.label}</div>
                          <div className={`text-[10px] mt-0.5 ${isDarkTheme ? "text-slate-300" : "text-slate-500"}`}>
                            {item.desc}
                          </div>
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Backdrop Blur & Corner Radius */}
                <div className={`pt-4 border-t grid grid-cols-1 sm:grid-cols-2 gap-4 ${isDarkTheme ? "border-white/10" : "border-slate-200"}`}>
                  {/* Blur Level */}
                  <div>
                    <label className={`text-xs font-bold uppercase tracking-wider ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                      Độ mờ kính (Backdrop Blur)
                    </label>
                    <div className="grid grid-cols-2 gap-2 mt-2">
                      {[
                        { id: "none", label: "Tắt mờ (0px)" },
                        { id: "low", label: "Mờ nhẹ (8px)" },
                        { id: "medium", label: "Chuẩn kính (16px)" },
                        { id: "heavy", label: "Siêu mờ (24px)" }
                      ].map((b) => (
                        <button
                          key={b.id}
                          onClick={() => update({ blurLevel: b.id as BlurPreset })}
                          className={`p-2 rounded-xl border text-xs font-semibold transition-all ${
                            settings.blurLevel === b.id
                              ? "bg-amber-500 text-slate-950 font-bold"
                              : isDarkTheme
                              ? "bg-white/5 border-white/10 hover:bg-white/10 text-slate-300"
                              : "bg-slate-100 border-slate-200 hover:bg-slate-200 text-slate-700"
                          }`}
                        >
                          {b.label}
                        </button>
                      ))}
                    </div>
                  </div>

                  {/* Border Radius */}
                  <div>
                    <label className={`text-xs font-bold uppercase tracking-wider ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                      Kiểu bo góc khối (Corner Radius)
                    </label>
                    <div className="grid grid-cols-3 gap-2 mt-2">
                      {[
                        { id: "compact", label: "Góc 14px" },
                        { id: "smooth", label: "Góc 22px" },
                        { id: "curvy", label: "Góc 28px" }
                      ].map((r) => (
                        <button
                          key={r.id}
                          onClick={() => update({ borderRadius: r.id as RadiusPreset })}
                          className={`p-2 rounded-xl border text-xs font-semibold transition-all ${
                            settings.borderRadius === r.id
                              ? "bg-amber-500 text-slate-950 font-bold"
                              : isDarkTheme
                              ? "bg-white/5 border-white/10 hover:bg-white/10 text-slate-300"
                              : "bg-slate-100 border-slate-200 hover:bg-slate-200 text-slate-700"
                          }`}
                        >
                          {r.label}
                        </button>
                      ))}
                    </div>
                  </div>
                </div>

                {/* Units & Lunar */}
                <div className={`pt-4 border-t grid grid-cols-1 sm:grid-cols-2 gap-4 ${isDarkTheme ? "border-white/10" : "border-slate-200"}`}>
                  <div>
                    <label className={`text-xs font-bold uppercase tracking-wider ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                      Đơn vị nhiệt độ
                    </label>
                    <div className="grid grid-cols-2 gap-2 mt-2">
                      {[
                        { id: "c", label: "Độ C (°C)" },
                        { id: "f", label: "Độ F (°F)" }
                      ].map((u) => (
                        <button
                          key={u.id}
                          onClick={() => update({ tempUnit: u.id as "c" | "f" })}
                          className={`p-2 rounded-xl border text-xs font-bold transition-all ${
                            settings.tempUnit === u.id
                              ? "bg-amber-500 text-slate-950 font-black"
                              : isDarkTheme
                              ? "bg-white/5 border-white/10 text-slate-300"
                              : "bg-slate-100 border-slate-200 text-slate-700"
                          }`}
                        >
                          {u.label}
                        </button>
                      ))}
                    </div>
                  </div>

                  <div>
                    <label className={`text-xs font-bold uppercase tracking-wider ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                      Đơn vị tốc độ gió
                    </label>
                    <div className="grid grid-cols-3 gap-2 mt-2">
                      {[
                        { id: "kmh", label: "km/h" },
                        { id: "ms", label: "m/s" },
                        { id: "mph", label: "mph" }
                      ].map((w) => (
                        <button
                          key={w.id}
                          onClick={() => update({ windUnit: w.id as "kmh" | "ms" | "mph" })}
                          className={`p-2 rounded-xl border text-xs font-bold transition-all ${
                            settings.windUnit === w.id
                              ? "bg-amber-500 text-slate-950 font-black"
                              : isDarkTheme
                              ? "bg-white/5 border-white/10 text-slate-300"
                              : "bg-slate-100 border-slate-200 text-slate-700"
                          }`}
                        >
                          {w.label}
                        </button>
                      ))}
                    </div>
                  </div>
                </div>

                <div className={`pt-4 border-t ${isDarkTheme ? "border-white/10" : "border-slate-200"}`}>
                  <label className="flex items-center justify-between cursor-pointer group">
                    <div>
                      <div className={`text-sm font-bold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                        Hiển thị Lịch Âm Việt Nam
                      </div>
                      <div className={`text-xs ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                        Hiển thị ngày Âm lịch, Can Chi & Tiết khí chính xác theo múi giờ GMT+7
                      </div>
                    </div>
                    <input
                      type="checkbox"
                      checked={settings.showLunar}
                      onChange={(e) => update({ showLunar: e.target.checked })}
                      className="w-5 h-5 accent-amber-500 rounded cursor-pointer"
                    />
                  </label>
                </div>
              </div>
            )}

            {/* 2. BỐ CỤC & THỨ TỰ KHỐI HIỂN THỊ (REORDER & VISIBILITY) */}
            {activeTab === "layout" && (
              <div className="space-y-4 animate-in fade-in duration-200">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2.5">
                  <div>
                    <h3 className={`text-xs font-bold uppercase tracking-wider ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                      Tùy biến vị trí trên dưới & hiển thị
                    </h3>
                    <p className={`text-xs ${isDarkTheme ? "text-slate-300" : "text-slate-500"}`}>
                      Nhấn nút mũi tên ⬆️ / ⬇️ để thay đổi thứ tự ưu tiên các khối trên trang chủ
                    </p>
                  </div>
                  <div className="flex items-center gap-2 self-start sm:self-auto shrink-0">
                    <button
                      onClick={resetWidgetOrder}
                      className={`flex items-center gap-1.5 px-2.5 py-1.5 rounded-xl text-xs font-semibold border transition-colors ${
                        isDarkTheme
                          ? "bg-white/5 border-white/10 text-slate-300 hover:bg-white/10"
                          : "bg-slate-100 border-slate-200 text-slate-700 hover:bg-slate-200"
                      }`}
                      title="Đặt lại thứ tự ban đầu"
                    >
                      <RotateCcw className="w-3.5 h-3.5" />
                      <span>Thứ tự mặc định</span>
                    </button>
                    <button
                      onClick={() => {
                        const allEnabled = Object.keys(settings.widgets).reduce((acc, k) => {
                          acc[k as keyof WidgetLayoutState] = true;
                          return acc;
                        }, {} as WidgetLayoutState);
                        update({ widgets: allEnabled });
                      }}
                      className="px-3 py-1.5 rounded-xl text-xs font-bold bg-amber-500 text-slate-950 hover:bg-amber-400 transition-colors shadow-xs"
                    >
                      Bật tất cả
                    </button>
                  </div>
                </div>

                {/* Ordered Widgets List */}
                <div className="space-y-2 pt-1">
                  {currentOrder.map((widgetId, idx) => {
                    const meta = WIDGET_DEF_MAP[widgetId] || {
                      label: widgetId,
                      desc: "Khối chức năng"
                    };
                    const isChecked = settings.widgets[widgetId] ?? true;
                    const isFirst = idx === 0;
                    const isLast = idx === currentOrder.length - 1;

                    return (
                      <div
                        key={widgetId}
                        className={`flex items-center justify-between p-3 sm:p-3.5 rounded-2xl border transition-all ${
                          isChecked
                            ? isDarkTheme
                              ? "bg-white/5 border-white/15"
                              : "bg-slate-50 border-slate-300 shadow-xs"
                            : isDarkTheme
                            ? "bg-white/[0.02] border-white/5 opacity-50"
                            : "bg-slate-100/50 border-slate-200 opacity-50"
                        }`}
                      >
                        {/* Left: Position number + Name + Desc */}
                        <div className="flex items-center gap-3 min-w-0 mr-3">
                          <span
                            className="w-7 h-7 rounded-xl flex items-center justify-center text-xs font-black shrink-0 shadow-xs"
                            style={{
                              backgroundColor: `rgba(${currentAccent.rgb}, ${isDarkTheme ? 0.25 : 0.15})`,
                              color: currentAccent.hex
                            }}
                          >
                            #{idx + 1}
                          </span>
                          <div className="min-w-0">
                            <div className={`text-xs font-bold truncate ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                              {meta.label}
                            </div>
                            <div className={`text-[11px] truncate ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                              {meta.desc}
                            </div>
                          </div>
                        </div>

                        {/* Right: Up / Down Reorder buttons + Enable/Disable checkbox */}
                        <div className="flex items-center gap-1.5 shrink-0">
                          {/* Move Up */}
                          <button
                            type="button"
                            onClick={() => moveWidget(idx, "up")}
                            disabled={isFirst}
                            className={`p-1.5 rounded-xl border transition-colors ${
                              isFirst
                                ? "opacity-25 cursor-not-allowed border-transparent text-slate-500"
                                : isDarkTheme
                                ? "bg-white/5 border-white/10 hover:bg-white/15 text-slate-200"
                                : "bg-white border-slate-200 hover:bg-slate-100 text-slate-700 shadow-xs"
                            }`}
                            title="Di chuyển lên trên"
                          >
                            <ChevronUp className="w-4 h-4" />
                          </button>

                          {/* Move Down */}
                          <button
                            type="button"
                            onClick={() => moveWidget(idx, "down")}
                            disabled={isLast}
                            className={`p-1.5 rounded-xl border transition-colors ${
                              isLast
                                ? "opacity-25 cursor-not-allowed border-transparent text-slate-500"
                                : isDarkTheme
                                ? "bg-white/5 border-white/10 hover:bg-white/15 text-slate-200"
                                : "bg-white border-slate-200 hover:bg-slate-100 text-slate-700 shadow-xs"
                            }`}
                            title="Di chuyển xuống dưới"
                          >
                            <ChevronDown className="w-4 h-4" />
                          </button>

                          {/* Visibility Checkbox */}
                          <label className="ml-2 cursor-pointer p-1" title="Bật/Tắt khối này">
                            <input
                              type="checkbox"
                              checked={isChecked}
                              onChange={(e) => updateWidget(widgetId, e.target.checked)}
                              className="w-5 h-5 accent-amber-500 rounded cursor-pointer block"
                            />
                          </label>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {/* 3. HIỆU ỨNG KHÍ QUYỂN (ATMOSPHERE EFFECTS) */}
            {activeTab === "atmosphere" && (
              <div className="space-y-5 animate-in fade-in duration-200">
                {/* Main toggle */}
                <label className="flex items-center justify-between cursor-pointer group">
                  <div>
                    <div className={`text-sm font-bold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                      Hiệu ứng thời tiết sống động
                    </div>
                    <div className={`text-xs ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                      Mưa rơi, sao băng đêm, tia nắng mặt trời, sấm sét chớp sáng nền
                    </div>
                  </div>
                  <input
                    type="checkbox"
                    checked={settings.atmosphereEnabled}
                    onChange={(e) => update({ atmosphereEnabled: e.target.checked })}
                    className="w-5 h-5 accent-amber-500 rounded cursor-pointer"
                  />
                </label>

                {/* Performance mode */}
                <div className={`pt-3 border-t ${isDarkTheme ? "border-white/10" : "border-slate-200"}`}>
                  <label className={`text-xs font-bold uppercase tracking-wider ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                    Chế độ hiệu năng
                  </label>
                  <div className="grid grid-cols-2 gap-2 mt-2">
                    {[
                      { id: "high", label: "Mượt mà (60-120 FPS)", desc: "Hình ảnh điện ảnh sống động" },
                      { id: "saver", label: "Tiết kiệm Pin / Máy yếu", desc: "Tối ưu phần cứng tối đa" }
                    ].map((p) => {
                      const isSelected = settings.atmospherePerformance === p.id;
                      return (
                        <button
                          key={p.id}
                          onClick={() => update({ atmospherePerformance: p.id as "high" | "saver" })}
                          className={`p-3 rounded-2xl border text-left text-xs font-bold transition-all ${
                            isSelected
                              ? isDarkTheme
                                ? "bg-amber-500/20 border-amber-400 text-amber-300 font-bold shadow-xs"
                                : "bg-amber-100 border-amber-500 text-amber-950 font-bold shadow-xs"
                              : isDarkTheme
                                ? "bg-white/5 border-white/10 text-slate-300 hover:bg-white/10"
                                : "bg-slate-100/80 border-slate-300/80 text-slate-700 hover:bg-slate-200/80"
                          }`}
                        >
                          <div>{p.label}</div>
                          <div className={`text-[10px] font-normal mt-0.5 ${isDarkTheme ? "text-slate-300" : "text-slate-500"}`}>
                            {p.desc}
                          </div>
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Particle density */}
                <div className={`pt-3 border-t ${isDarkTheme ? "border-white/10" : "border-slate-200"}`}>
                  <label className={`text-xs font-bold uppercase tracking-wider ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                    Mật độ hạt hiệu ứng
                  </label>
                  <div className="grid grid-cols-3 gap-2 mt-2">
                    {[
                      { id: "low", label: "Thưa thớt (Nhẹ)" },
                      { id: "medium", label: "Vừa phải (Chuẩn)" },
                      { id: "high", label: "Dày đặc (Sinh động)" }
                    ].map((d) => (
                      <button
                        key={d.id}
                        onClick={() => updateParticle("density", d.id)}
                        className={`p-2.5 rounded-xl border text-xs font-bold transition-all ${
                          settings.atmosphereParticles?.density === d.id
                            ? "bg-amber-500 text-slate-950 font-black shadow-xs"
                            : isDarkTheme
                            ? "bg-white/5 border-white/10 text-slate-300"
                            : "bg-slate-100 border-slate-200 text-slate-700"
                        }`}
                      >
                        {d.label}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Granular particle toggles */}
                <div className={`pt-3 border-t ${isDarkTheme ? "border-white/10" : "border-slate-200"}`}>
                  <label className={`text-xs font-bold uppercase tracking-wider block mb-2 ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                    Tùy chọn từng hiệu ứng chi tiết
                  </label>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                    {[
                      { id: "rain", label: "🌧️ Hạt mưa rơi & giọt bắn" },
                      { id: "lightning", label: "⚡ Sấm sét chớp sáng nền" },
                      { id: "shootingStars", label: "🌠 Sao băng đêm & tinh tú" },
                      { id: "sunBeams", label: "☀️ Tia nắng & bụi ánh sáng mặt trời" },
                      { id: "leaves", label: "🍃 Lá bay cuốn theo chiều gió" },
                      { id: "clouds", label: "☁️ Mây trôi khí quyển bề mặt" }
                    ].map((item) => {
                      const key = item.id as keyof AtmosphereParticlesState;
                      const enabled = (settings.atmosphereParticles as unknown as Record<string, boolean>)?.[key] ?? true;
                      return (
                        <label
                          key={item.id}
                          className={`flex items-center justify-between p-3 rounded-xl border cursor-pointer transition-all ${
                            enabled
                              ? isDarkTheme ? "bg-white/5 border-white/15" : "bg-slate-50 border-slate-300"
                              : isDarkTheme ? "bg-white/[0.02] border-white/5 opacity-50" : "bg-slate-100 opacity-50"
                          }`}
                        >
                          <span className={`text-xs font-semibold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>
                            {item.label}
                          </span>
                          <input
                            type="checkbox"
                            checked={enabled}
                            onChange={(e) => updateParticle(key, e.target.checked)}
                            className="w-4 h-4 accent-amber-500 rounded cursor-pointer"
                          />
                        </label>
                      );
                    })}
                  </div>
                </div>
              </div>
            )}

            {/* 4. MÔ HÌNH DỰ BÁO */}
            {activeTab === "forecast" && (
              <div className="space-y-3 animate-in fade-in duration-200">
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
                      onClick={() => update({ forecastModel: m.id })}
                      className={`w-full text-left p-3.5 rounded-2xl border transition-all ${
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
                      <p className={`text-[11px] mt-0.5 ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>{m.desc}</p>
                    </button>
                  );
                })}
              </div>
            )}

            {/* 5. ĐỊA ĐIỂM ĐÃ LƯU */}
            {activeTab === "favorites" && (
              <div className="space-y-3 animate-in fade-in duration-200">
                <div className={`text-xs font-bold uppercase tracking-wider ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                  Danh sách thành phố yêu thích
                </div>
                {favoriteCities.length === 0 ? (
                  <div className={`py-8 text-center text-xs ${isDarkTheme ? "text-slate-300" : "text-slate-500"}`}>
                    Chưa có thành phố nào được lưu. Hãy bấm biểu tượng ngôi sao ⭐ trên khối thời tiết để thêm nhanh!
                  </div>
                ) : (
                  favoriteCities.map((city, idx) => (
                    <div
                      key={idx}
                      className={`flex items-center justify-between p-3.5 rounded-2xl border transition-colors ${
                        isDarkTheme ? "bg-white/5 border-white/10" : "bg-slate-50 border-slate-200"
                      }`}
                    >
                      <div>
                        <div className={`text-xs font-bold ${isDarkTheme ? "text-white" : "text-slate-900"}`}>{city.name}</div>
                        <div className={`text-[11px] ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>{city.country}</div>
                      </div>
                      <button
                        onClick={() => onRemoveFavorite(city)}
                        className={`p-2 rounded-xl transition-colors ${
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

            {/* 6. GEMINI AI KEY */}
            {activeTab === "ai" && (
              <div className="space-y-3 animate-in fade-in duration-200">
                <div className={`text-xs font-bold uppercase tracking-wider ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                  Google Gemini API Key
                </div>
                <p className={`text-xs leading-relaxed ${isDarkTheme ? "text-slate-300" : "text-slate-600"}`}>
                  Weather 24h tích hợp sẵn trí tuệ nhân tạo Gemini AI Stylist. Nếu bạn có khóa API cá nhân, hãy nhập vào đây để tăng tốc độ phân tích và không bị giới hạn số lần gọi.
                </p>
                <input
                  type="password"
                  value={apiKeyInput}
                  onChange={(e) => setApiKeyInput(e.target.value)}
                  placeholder="AIzaSy..."
                  className={`w-full p-3 rounded-2xl border text-xs font-mono transition-colors ${
                    isDarkTheme
                      ? "bg-white/10 border-white/20 text-white placeholder-slate-500"
                      : "bg-slate-50 border-slate-300 text-slate-900 placeholder-slate-400 focus:bg-white focus:border-amber-500"
                  }`}
                />
                <button
                  onClick={() => {
                    update({ geminiApiKey: apiKeyInput.trim() });
                    alert("Đã lưu khóa API Gemini thành công!");
                  }}
                  className="px-5 py-2.5 rounded-xl bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold text-xs transition-colors shadow-xs"
                >
                  Lưu khóa API
                </button>
              </div>
            )}

            {/* 7. GIỚI THIỆU */}
            {activeTab === "about" && (
              <div className={`space-y-4 text-xs leading-relaxed animate-in fade-in duration-200 ${isDarkTheme ? "text-slate-300" : "text-slate-700"}`}>
                <div
                  className={`p-5 rounded-3xl border text-center flex flex-col items-center justify-center ${
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
                    Phiên bản Web • Đồng bộ v1.0.63 APK
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

                <div className={`pt-3 border-t text-[11px] font-semibold tracking-wide text-center ${
                  isDarkTheme ? "border-white/10 text-slate-400" : "border-slate-200 text-slate-500"
                }`}>
                  © 2027 Designed by CD
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>,
    document.body
  );
}
