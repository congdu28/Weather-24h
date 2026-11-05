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
  time: string;
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
