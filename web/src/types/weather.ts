export interface CityLocation {
  name: string;
  country: string;
  admin1?: string | null;
  latitude: number;
  longitude: number;
  isCurrentLocation?: boolean;
}

export interface CurrentWeather {
  temperature: number;
  apparentTemperature: number;
  relativeHumidity: number;
  weatherCode: number;
  isDay: boolean;
  windSpeed: number;
  windDirection: number;
  surfacePressure: number;
  uvIndex: number;
  visibility: number;
  precipitation: number;
  cloudCover?: number;
  time: string;
  radarStatus?: {
    isRainObserved: boolean;
    calibrated: boolean;
    note?: string;
  };
}

export interface HourlyForecast {
  time: string;
  hour: string;
  temperature: number;
  weatherCode: number;
  precipitationProbability: number;
  precipitation: number;
  windSpeed: number;
  relativeHumidity: number;
  isDay: boolean;
}

export interface DailyForecast {
  date: string;
  dayOfWeek: string;
  weatherCode: number;
  tempMax: number;
  tempMin: number;
  precipitationProbabilityMax: number;
  precipitationSum: number;
  uvIndexMax: number;
  sunrise: string;
  sunset: string;
  conditionDescription?: string;
  conditionIcon?: string;
}

export interface AirQuality {
  pm25: number;
  pm10: number;
  aqiIndex: number;
  aqiScale: string;
  category: string;
  color: string;
  advice: string;
}

export interface LifestyleItem {
  id: string;
  title: string;
  score: number; // 0 - 100
  level: string; // Tốt, Thích hợp, Cẩn trọng, Kém...
  icon: string;
  description: string;
}

export interface WeatherData {
  location: CityLocation;
  current: CurrentWeather;
  hourly: HourlyForecast[];
  daily: DailyForecast[];
  airQuality?: AirQuality;
  lifestyles: LifestyleItem[];
  updatedAt: string;
}

// === TÙY BIẾN GIAO DIỆN & TÍNH NĂNG WEATHER 24H ===
export type GlassPreset = "ultra" | "frosted" | "balanced" | "solid";
export type DepthPreset = "flat" | "soft" | "deep" | "glow";
export type BlurPreset = "none" | "low" | "medium" | "heavy";
export type RadiusPreset = "compact" | "smooth" | "curvy";
export type AccentPreset = "amber" | "ocean" | "violet" | "emerald" | "rose" | "adaptive";

export type WidgetId =
  | "hero"
  | "astronomy"
  | "insights"
  | "cityCompare"
  | "hourly"
  | "radar"
  | "lunar"
  | "daily"
  | "aiStylist"
  | "airQuality"
  | "lifestyle"
  | "metrics";

export const DEFAULT_WIDGET_ORDER: WidgetId[] = [
  "hero",
  "hourly",
  "radar",
  "lunar",
  "daily",
  "astronomy",
  "insights",
  "cityCompare",
  "aiStylist",
  "airQuality",
  "lifestyle",
  "metrics"
];

export interface WidgetLayoutState {
  hero: boolean;
  astronomy: boolean; // Quỹ đạo Mặt trời & Tuần trăng
  insights: boolean; // Cảnh báo & Lời khuyên thông minh
  cityCompare: boolean; // So sánh thời tiết các thành phố yêu thích
  hourly: boolean; // Dự báo 24 giờ tới
  radar: boolean; // Bản đồ radar mây mưa
  lunar: boolean; // Âm lịch & Lịch Vạn Niên
  daily: boolean; // Dự báo 7 ngày tới
  aiStylist: boolean; // AI Stylist
  airQuality: boolean; // Chất lượng không khí
  lifestyle: boolean; // Chỉ số sinh hoạt đời sống
  metrics: boolean; // Lưới thông số chi tiết
}

export interface AtmosphereParticlesState {
  rain: boolean;
  lightning: boolean;
  shootingStars: boolean;
  sunBeams: boolean;
  leaves: boolean;
  clouds: boolean;
  density: "low" | "medium" | "high";
}

export interface AppSettings {
  theme: "dark" | "light" | "system";
  accentColor: AccentPreset;
  glassStyle: GlassPreset;
  customOpacity: number; // 50 to 100 (%)
  depthLevel: DepthPreset;
  blurLevel: BlurPreset;
  borderRadius: RadiusPreset;
  widgets: WidgetLayoutState;
  widgetOrder: WidgetId[];
  atmosphereEnabled: boolean;
  atmospherePerformance: "high" | "saver";
  atmosphereParticles: AtmosphereParticlesState;
  showLunar: boolean;
  tempUnit: "c" | "f";
  windUnit: "kmh" | "ms" | "mph";
  forecastModel: string;
  geminiApiKey: string;
}

export const DEFAULT_WIDGET_LAYOUT: WidgetLayoutState = {
  hero: true,
  astronomy: true,
  insights: true,
  cityCompare: true,
  hourly: true,
  radar: true,
  lunar: true,
  daily: true,
  aiStylist: true,
  airQuality: true,
  lifestyle: true,
  metrics: true
};

export const DEFAULT_ATMOSPHERE_PARTICLES: AtmosphereParticlesState = {
  rain: true,
  lightning: true,
  shootingStars: true,
  sunBeams: true,
  leaves: true,
  clouds: true,
  density: "medium"
};

export const DEFAULT_APP_SETTINGS: AppSettings = {
  theme: "light",
  accentColor: "amber",
  glassStyle: "ultra",
  customOpacity: 65,
  depthLevel: "glow",
  blurLevel: "medium",
  borderRadius: "smooth",
  widgets: DEFAULT_WIDGET_LAYOUT,
  widgetOrder: DEFAULT_WIDGET_ORDER,
  atmosphereEnabled: true,
  atmospherePerformance: "high",
  atmosphereParticles: DEFAULT_ATMOSPHERE_PARTICLES,
  showLunar: true,
  tempUnit: "c",
  windUnit: "kmh",
  forecastModel: "best_match",
  geminiApiKey: ""
};

export interface AccentThemeInfo {
  id: AccentPreset;
  name: string;
  hex: string;
  rgb: string;
  badgeBg: string;
  badgeText: string;
}

export const ACCENT_THEMES: Record<AccentPreset, AccentThemeInfo> = {
  amber: {
    id: "amber",
    name: "Hoàng Kim (Gold)",
    hex: "#f59e0b",
    rgb: "245, 158, 11",
    badgeBg: "bg-amber-500/20",
    badgeText: "text-amber-500"
  },
  ocean: {
    id: "ocean",
    name: "Biển Xanh (Sky Ocean)",
    hex: "#0ea5e9",
    rgb: "14, 165, 233",
    badgeBg: "bg-sky-500/20",
    badgeText: "text-sky-500"
  },
  violet: {
    id: "violet",
    name: "Cực Quang (Aurora)",
    hex: "#8b5cf6",
    rgb: "139, 92, 246",
    badgeBg: "bg-violet-500/20",
    badgeText: "text-violet-500"
  },
  emerald: {
    id: "emerald",
    name: "Lục Bảo (Emerald)",
    hex: "#10b981",
    rgb: "16, 185, 129",
    badgeBg: "bg-emerald-500/20",
    badgeText: "text-emerald-500"
  },
  rose: {
    id: "rose",
    name: "Hoàng Hôn (Sunset Rose)",
    hex: "#f43f5e",
    rgb: "244, 63, 94",
    badgeBg: "bg-rose-500/20",
    badgeText: "text-rose-500"
  },
  adaptive: {
    id: "adaptive",
    name: "Tự động theo thời tiết",
    hex: "#f59e0b",
    rgb: "245, 158, 11",
    badgeBg: "bg-blue-500/20",
    badgeText: "text-blue-400"
  }
};
