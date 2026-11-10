import {
  AirQuality,
  CityLocation,
  CurrentWeather,
  DailyForecast,
  HourlyForecast,
  LifestyleItem,
  WeatherData
} from "../types/weather";

export const HOANG_SA_LOCATION: CityLocation = {
  name: "Quần đảo Hoàng Sa",
  country: "Việt Nam",
  admin1: "Huyện Hoàng Sa, TP. Đà Nẵng",
  latitude: 16.5375,
  longitude: 111.6083
};

export const TRUONG_SA_LOCATION: CityLocation = {
  name: "Quần đảo Trường Sa",
  country: "Việt Nam",
  admin1: "Huyện Trường Sa, Tỉnh Khánh Hòa",
  latitude: 8.6433,
  longitude: 111.9189
};

export const DEFAULT_LOCATION: CityLocation = {
  name: "Hà Nội",
  country: "Việt Nam",
  admin1: "Thủ đô",
  latitude: 21.0285,
  longitude: 105.8542
};

export const VIETNAM_MAJOR_CITIES: CityLocation[] = [
  { name: "Hà Nội", country: "Việt Nam", admin1: "Thủ đô", latitude: 21.0285, longitude: 105.8542 },
  { name: "TP. Hồ Chí Minh", country: "Việt Nam", admin1: "TP. Hồ Chí Minh", latitude: 10.8231, longitude: 106.6297 },
  { name: "Đà Nẵng", country: "Việt Nam", admin1: "Đà Nẵng", latitude: 16.0544, longitude: 108.2022 },
  { name: "Quần đảo Hoàng Sa", country: "Việt Nam", admin1: "Huyện Hoàng Sa, TP. Đà Nẵng", latitude: 16.5375, longitude: 111.6083 },
  { name: "Quần đảo Trường Sa", country: "Việt Nam", admin1: "Huyện Trường Sa, Tỉnh Khánh Hòa", latitude: 8.6433, longitude: 111.9189 },
  { name: "Hải Phòng", country: "Việt Nam", admin1: "Hải Phòng", latitude: 20.8449, longitude: 106.6881 },
  { name: "Cần Thơ", country: "Việt Nam", admin1: "Cần Thơ", latitude: 10.0452, longitude: 105.7469 },
  { name: "Huế", country: "Việt Nam", admin1: "Thừa Thiên Huế", latitude: 16.4637, longitude: 107.5909 },
  { name: "Nha Trang", country: "Việt Nam", admin1: "Khánh Hòa", latitude: 12.2388, longitude: 109.1967 },
  { name: "Đà Lạt", country: "Việt Nam", admin1: "Lâm Đồng", latitude: 11.9404, longitude: 108.4583 },
  { name: "Hạ Long", country: "Việt Nam", admin1: "Quảng Ninh", latitude: 20.9505, longitude: 107.0734 },
  { name: "Vũng Tàu", country: "Việt Nam", admin1: "Bà Rịa - Vũng Tàu", latitude: 10.3460, longitude: 107.0843 },
  { name: "Phú Quốc", country: "Việt Nam", admin1: "Kiên Giang", latitude: 10.2289, longitude: 103.9572 },
  { name: "Sa Pa", country: "Việt Nam", admin1: "Lào Cai", latitude: 22.3364, longitude: 103.8438 },
  { name: "Vinh", country: "Việt Nam", admin1: "Nghệ An", latitude: 18.6734, longitude: 105.6813 },
  { name: "Thanh Hóa", country: "Việt Nam", admin1: "Thanh Hóa", latitude: 19.8067, longitude: 105.7852 },
  { name: "Quy Nhơn", country: "Việt Nam", admin1: "Bình Định", latitude: 13.7820, longitude: 109.2194 },
  { name: "Buôn Ma Thuột", country: "Việt Nam", admin1: "Đắk Lắk", latitude: 12.6667, longitude: 108.0500 }
];

export function getWeatherCondition(code: number, isDay = true): {
  description: string;
  icon: string;
  gradient: string;
  type: "sunny" | "cloudy" | "rainy" | "thunder" | "fog" | "snow";
} {
  switch (code) {
    case 0:
      return {
        description: isDay ? "Trời quang, nắng đẹp" : "Đêm quang đãng",
        icon: isDay ? "Sun" : "Moon",
        gradient: isDay ? "from-amber-500/20 via-sky-600/30 to-blue-900/60" : "from-slate-900 via-indigo-950 to-slate-950",
        type: "sunny"
      };
    case 1:
      return {
        description: isDay ? "Trời hầu như không mây" : "Đêm ít mây",
        icon: isDay ? "SunMedium" : "MoonStar",
        gradient: isDay ? "from-blue-500/30 via-sky-700/40 to-slate-900/70" : "from-slate-900 via-blue-950 to-slate-950",
        type: "sunny"
      };
    case 2:
      return {
        description: isDay ? "Mây rải rác, hửng nắng" : "Đêm mây rải rác",
        icon: isDay ? "CloudSun" : "CloudMoon",
        gradient: "from-sky-700/30 via-slate-800/40 to-slate-950",
        type: "cloudy"
      };
    case 3:
      return {
        description: "Trời nhiều mây, u ám",
        icon: "Cloud",
        gradient: "from-slate-700/40 via-slate-800/60 to-slate-950",
        type: "cloudy"
      };
    case 45:
    case 48:
      return {
        description: "Có sương mù",
        icon: "CloudFog",
        gradient: "from-zinc-700/40 via-slate-800/50 to-slate-950",
        type: "fog"
      };
    case 51:
    case 53:
    case 55:
      return {
        description: "Mưa phùn nhẹ hạt",
        icon: "CloudDrizzle",
        gradient: "from-blue-900/40 via-slate-800/60 to-slate-950",
        type: "rainy"
      };
    case 61:
      return {
        description: "Mưa rào nhẹ",
        icon: "CloudRain",
        gradient: "from-cyan-900/40 via-blue-950/60 to-slate-950",
        type: "rainy"
      };
    case 63:
    case 65:
      return {
        description: "Mưa to diện rộng",
        icon: "CloudRain",
        gradient: "from-blue-950/70 via-slate-900 to-black",
        type: "rainy"
      };
    case 80:
      return {
        description: isDay ? "Nắng xen mưa rào thoáng qua" : "Mưa rào nhẹ thoáng qua",
        icon: isDay ? "CloudSunRain" : "CloudRain",
        gradient: "from-blue-700/30 via-slate-800/40 to-slate-950",
        type: "rainy"
      };
    case 81:
    case 82:
      return {
        description: "Mưa rào từng đợt",
        icon: "CloudRain",
        gradient: "from-indigo-950/60 via-slate-900 to-black",
        type: "rainy"
      };
    case 95:
    case 96:
    case 99:
      return {
        description: "Mưa dông, có sấm sét",
        icon: "CloudLightning",
        gradient: "from-purple-950/60 via-indigo-950 to-black",
        type: "thunder"
      };
    default:
      return {
        description: "Có mưa rải rác",
        icon: "CloudSunRain",
        gradient: "from-slate-800 via-slate-900 to-black",
        type: "rainy"
      };
  }
}

/**
 * Hiệu chuẩn thông minh mã thời tiết cho từng ngày trong chuỗi dự báo 7 ngày.
 * Khắc phục triệt để nhược điểm của Open-Meteo khi chỉ lấy MAX(weather_code) của 24h,
 * khiến những ngày nắng ấm (UV cao 6-8, 25-32°C) hoặc chỉ có vết ẩm 0.1-0.3mm ban đêm
 * bị gán nhầm thành ngày "Mưa" (CloudRain) làm người dùng lầm tưởng tỉ lệ mưa rất cao.
 */
export function calibrateDailyWeatherCode(params: {
  rawCode: number;
  rainMm: number;
  rainProb: number;
  maxTemp: number;
  minTemp: number;
  uvMax: number;
  isToday?: boolean;
  currentWeatherCode?: number;
  hourlyList?: HourlyForecast[];
}): number {
  const { rawCode, rainMm, rainProb, maxTemp, uvMax, isToday, currentWeatherCode, hourlyList } = params;

  // 1. Đối với "Hôm nay" (Ngày 0):
  // Đồng bộ với trạng thái thời tiết thực tế ban ngày đã qua chuẩn hóa
  if (isToday && currentWeatherCode !== undefined) {
    const isCurrentDry = [0, 1, 2, 3].includes(currentWeatherCode);
    if (isCurrentDry && rainMm <= 0.5 && rainProb < 50) {
      return currentWeatherCode;
    }
  }

  // 2. Thống kê dữ liệu các giờ ban ngày
  let daytimeRainHours = 0;
  let daytimeThunderHours = 0;
  if (hourlyList && hourlyList.length > 0) {
    const dayHours = hourlyList.filter(h => h.isDay);
    for (const h of dayHours) {
      if (h.weatherCode >= 95 && (h.precipitation >= 0.4 || h.precipitationProbability >= 45)) {
        daytimeThunderHours++;
      }
      if ([51, 53, 55, 61, 63, 65, 80, 81, 82].includes(h.weatherCode) && (h.precipitation >= 0.25 || h.precipitationProbability >= 45)) {
        daytimeRainHours++;
      }
    }
  }

  const isRainCode = [51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82].includes(rawCode);
  const isThunderCode = [95, 96, 99].includes(rawCode);
  const isFogCode = [45, 48].includes(rawCode);

  // 3. Khử sương mù ảo vào ban ngày khi trời nắng/nhiệt độ cao
  if (isFogCode && (maxTemp >= 24 || uvMax >= 4 || rainProb < 20)) {
    return uvMax >= 6 ? 1 : 2;
  }

  // 4. Khử mã sấm sét ảo khi không có mưa dông thực tế vào ban ngày
  if (isThunderCode) {
    if (daytimeThunderHours === 0 && (rainMm < 1.5 || rainProb < 45)) {
      return rainProb >= 40 && rainMm >= 0.8 ? 80 : 2;
    }
    return rawCode;
  }

  // 5. TRƯỜNG HỢP CỐT LÕI: Lượng mưa không đáng kể (Vết mưa <= 0.5mm)
  // Khắc phục triệt để trường hợp 0.3mm mưa, UV cao 6 nhưng icon báo mưa
  if (rainMm <= 0.5) {
    if (rainProb < 50 || daytimeRainHours === 0) {
      if (uvMax >= 6 || maxTemp >= 30) {
        return 1; // Trời hầu như không mây (Nắng đẹp)
      } else if (uvMax >= 3 || maxTemp >= 23) {
        return 2; // Mây rải rác, hửng nắng
      } else {
        return 3; // Trời nhiều mây
      }
    }
    return uvMax >= 5 ? 80 : 51;
  }

  // 6. Lượng mưa nhỏ (0.5mm < rainMm < 1.5mm)
  if (rainMm < 1.5) {
    if (rainProb < 40 && uvMax >= 5) {
      return 2; // Mây rải rác, hửng nắng
    }
    if (uvMax >= 5) {
      return 80; // Nắng xen mưa rào thoáng qua (CloudSunRain)
    }
    return 51; // Mưa phùn nhẹ
  }

  // 7. Lượng mưa đo được (>= 1.5mm)
  if (isRainCode) {
    if (uvMax >= 6 && rainProb < 50) {
      return 80; // Nắng xen mưa rào (CloudSunRain)
    }
    return rawCode;
  }

  return rawCode;
}

function calculateLifestyles(current: CurrentWeather, daily: DailyForecast[]): LifestyleItem[] {
  const rainToday = daily[0]?.precipitationSum ?? current.precipitation;
  const rainProb = daily[0]?.precipitationProbabilityMax ?? 0;

  // 1. Rửa xe
  let carWashScore = 85;
  let carWashLevel = "Rất thích hợp";
  let carWashDesc = "Thời tiết khô ráo, rất lý tưởng để vệ sinh và rửa xe.";
  if (rainToday > 2 || rainProb > 50) {
    carWashScore = 20;
    carWashLevel = "Không nên rửa";
    carWashDesc = "Hôm nay có khả năng mưa cao, nên hoãn rửa xe.";
  } else if (rainProb > 25) {
    carWashScore = 55;
    carWashLevel = "Cân nhắc";
    carWashDesc = "Trời nhiều mây, có thể có mưa bất chợt.";
  }

  // 2. Giặt phơi
  let laundryScore = 90;
  let laundryLevel = "Nhanh khô";
  let laundryDesc = "Nắng ráo, độ ẩm thích hợp, đồ khô rất nhanh.";
  if (current.relativeHumidity > 85 || rainProb > 40) {
    laundryScore = 30;
    laundryLevel = "Khó khô";
    laundryDesc = "Độ ẩm cao và dễ có mưa, nên phơi nơi có mái che hoặc dùng máy sấy.";
  }

  // 3. Thể thao ngoài trời
  let sportScore = 80;
  let sportLevel = "Thích hợp";
  let sportDesc = "Thời tiết thuận lợi cho chạy bộ, đạp xe, thể thao.";
  if (current.temperature > 35) {
    sportScore = 40;
    sportLevel = "Nắng gắt";
    sportDesc = "Nhiệt độ cao, nên tập vào sáng sớm hoặc chiều muộn.";
  } else if (current.weatherCode >= 80 || rainToday > 5) {
    sportScore = 25;
    sportLevel = "Nên tập trong nhà";
    sportDesc = "Có mưa dông ngoài trời, an toàn hơn khi tập trong nhà.";
  }

  // 4. Chống nắng UV
  let uvScore = 50;
  let uvLevel = "Vừa phải";
  let uvDesc = "Cần đeo kính râm khi ra đường.";
  if (current.uvIndex >= 8) {
    uvScore = 95;
    uvLevel = "Rất nguy hại";
    uvDesc = "Chỉ số UV cực cao! Cần thoa kem chống nắng, mang áo khoác, nón rộng vành.";
  } else if (current.uvIndex >= 6) {
    uvScore = 75;
    uvLevel = "Cao";
    uvDesc = "Tia cực tím mạnh, hạn chế tiếp xúc trực tiếp giữa trưa.";
  } else if (current.uvIndex <= 2) {
    uvScore = 20;
    uvLevel = "An toàn";
    uvDesc = "Tia UV thấp, thoải mái hoạt động ngoài trời.";
  }

  return [
    { id: "car_wash", title: "Rửa xe", score: carWashScore, level: carWashLevel, icon: "Car", description: carWashDesc },
    { id: "laundry", title: "Giặt phơi", score: laundryScore, level: laundryLevel, icon: "Shirt", description: laundryDesc },
    { id: "sport", title: "Thể thao", score: sportScore, level: sportLevel, icon: "Activity", description: sportDesc },
    { id: "uv", title: "Chống nắng", score: uvScore, level: uvLevel, icon: "ShieldAlert", description: uvDesc }
  ];
}

/**
 * Đọc dữ liệu phản hồi mây mưa thực tế (Radar dBZ) từ RainViewer API.
 * Sử dụng Zoom 8 (độ phân giải cao ~600m/pixel - cấp độ Phường/Xã) và ảnh thô không làm nhòe (0_0.png).
 * Áp dụng bộ lọc Chroma sắc độ để loại bỏ viền đen/xám quét của trạm radar (chỉ tính mưa khi có màu phản hồi thực tế).
 */
export async function checkRainViewerRadar(
  lat: number,
  lon: number
): Promise<{ isRainObserved: boolean } | null> {
  if (typeof window === "undefined" || typeof document === "undefined") {
    return null;
  }
  try {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 2000);

    const metaRes = await fetch("https://api.rainviewer.com/public/weather-maps.json", {
      signal: controller.signal
    });
    const meta = await metaRes.json();
    clearTimeout(timeout);

    if (!meta?.radar?.past?.length || !meta.host) return null;
    const latest = meta.radar.past[meta.radar.past.length - 1];

    // Zoom 8 cho độ phân giải siêu nét (~600m mỗi điểm ảnh), tương ứng bán kính cấp Phường/Xã
    const zoom = 8;
    const n = Math.pow(2, zoom);
    const exactX = ((lon + 180) / 360) * n;
    const latRad = (lat * Math.PI) / 180;
    const exactY = ((1 - Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI) / 2) * n;
    const tileX = Math.floor(exactX);
    const tileY = Math.floor(exactY);
    const pixelX = Math.floor((exactX % 1) * 256);
    const pixelY = Math.floor((exactY % 1) * 256);

    // 0_0.png: Lấy dữ liệu radar gốc, không kích hoạt Gaussian blur làm nhòe mây xa sang trung tâm
    const tileUrl = `${meta.host}${latest.path}/256/${zoom}/${tileX}/${tileY}/2/0_0.png`;

    return new Promise((resolve) => {
      const img = new Image();
      img.crossOrigin = "anonymous";
      const timer = setTimeout(() => {
        img.src = "";
        resolve(null);
      }, 2000);

      img.onload = () => {
        clearTimeout(timer);
        try {
          const canvas = document.createElement("canvas");
          canvas.width = 256;
          canvas.height = 256;
          const ctx = canvas.getContext("2d");
          if (!ctx) {
            resolve(null);
            return;
          }
          ctx.drawImage(img, 0, 0);

          let rainPixels = 0;
          // Quét phạm vi 3x3 pixels (~1.5km x 1.5km) đúng chuẩn cấp Phường
          const radius = 1;
          for (let dx = -radius; dx <= radius; dx++) {
            for (let dy = -radius; dy <= radius; dy++) {
              const px = Math.min(255, Math.max(0, pixelX + dx));
              const py = Math.min(255, Math.max(0, pixelY + dy));
              const data = ctx.getImageData(px, py, 1, 1).data;
              const r = data[0];
              const g = data[1];
              const b = data[2];
              const a = data[3];

              // Bộ lọc màu sắc thực (Chroma filter):
              // Viền bóng đài radar là màu đen/xám (r ≈ g ≈ b, chroma = 0..15).
              // Chỉ hạt mưa thật trên radar mới có sắc màu đậm (xanh, vàng, cam, đỏ: chroma >= 25).
              const chroma = Math.max(r, g, b) - Math.min(r, g, b);
              if (a > 40 && chroma >= 25) {
                rainPixels++;
              }
            }
          }
          // Có từ 2 pixel phản hồi sắc màu trở lên mới xác nhận có mưa thực địa
          resolve({ isRainObserved: rainPixels >= 2 });
        } catch {
          resolve(null);
        }
      };

      img.onerror = () => {
        clearTimeout(timer);
        resolve(null);
      };

      img.src = tileUrl;
    });
  } catch {
    return null;
  }
}

export async function fetchFullWeather(
  location: CityLocation,
  modelId?: string
): Promise<WeatherData> {
  const modelParam = modelId && modelId !== "best_match" ? `&models=${encodeURIComponent(modelId)}` : "";
  const weatherUrl = `https://api.open-meteo.com/v1/forecast?latitude=${location.latitude}&longitude=${location.longitude}${modelParam}&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m,uv_index,visibility,cloud_cover&hourly=temperature_2m,relative_humidity_2m,precipitation_probability,precipitation,weather_code,wind_speed_10m,is_day&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_probability_max,precipitation_sum&timezone=auto&forecast_days=7`;

  const aqiUrl = `https://air-quality-api.open-meteo.com/v1/air-quality?latitude=${location.latitude}&longitude=${location.longitude}&current=pm10,pm2_5,european_aqi`;

  const [weatherRes, aqiRes, radarCheck] = await Promise.all([
    fetch(weatherUrl).then(r => r.json()),
    fetch(aqiUrl).then(r => r.json()).catch(() => null),
    checkRainViewerRadar(location.latitude, location.longitude).catch(() => null)
  ]);

  if (!weatherRes || !weatherRes.current) {
    throw new Error("Không thể tải dữ liệu thời tiết.");
  }

  const cur = weatherRes.current;
  let finalWeatherCode = cur.weather_code;
  const isDay = cur.is_day === 1;
  const uv = cur.uv_index ?? 0;
  const cloud = cur.cloud_cover ?? 50;
  const precip = cur.precipitation ?? 0;

  // Nhóm mã thời tiết mưa theo chuẩn WMO
  const isRainCode = [51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82, 95, 96, 99].includes(cur.weather_code);

  // Điều kiện quang học mặt đất: Nếu có nắng rõ (UV >= 2.0 và mây <= 60%) hoặc mây rất ít (<= 40%),
  // thì lượng mưa mô phỏng <= 0.3mm của ô lưới 25km chỉ là mưa rìa ô lưới hoặc bốc hơi tầng cao (virga).
  const isPhysicallySunny = (uv >= 2.0 && cloud <= 60) || (uv >= 1.5 && cloud <= 45);

  let radarStatus: CurrentWeather["radarStatus"] = undefined;

  if (radarCheck !== null) {
    if (radarCheck.isRainObserved) {
      if (isPhysicallySunny) {
        // Mặc dù radar có vết mưa ở phạm vi lân cận, nhưng tại tọa độ mặt đất trời đang nắng rực rỡ
        finalWeatherCode = cloud <= 20 ? 0 : cloud <= 50 ? 1 : 2;
        radarStatus = {
          isRainObserved: false,
          calibrated: true,
          note: `Trời nắng ráo (UV ${uv.toFixed(1)}) & Mây ${cloud}% (Mưa cục bộ ở xa)`
        };
      } else {
        radarStatus = {
          isRainObserved: true,
          calibrated: false,
          note: "Radar thời gian thực ghi nhận có mây mưa"
        };
        if (!isRainCode && cur.weather_code <= 2) {
          finalWeatherCode = 61; // Mưa rào nhẹ
          radarStatus = {
            isRainObserved: true,
            calibrated: true,
            note: "Hiệu chỉnh theo Radar: Ghi nhận có mưa thực tế"
          };
        }
      }
    } else {
      // Radar xác nhận 0 dBZ (Không có mưa tại cấp độ Phường)
      if (isRainCode) {
        // Sai lệch giữa mô hình số trị diện rộng và thực tế mặt đất!
        if (cloud <= 20) {
          finalWeatherCode = 0; // Trời quang, nắng đẹp
        } else if (cloud <= 50) {
          finalWeatherCode = 1; // Hầu như không mây / Đêm ít mây
        } else {
          finalWeatherCode = 2; // Mây rải rác
        }
        radarStatus = {
          isRainObserved: false,
          calibrated: true,
          note: `Radar đối chiếu 0 dBZ (Không mưa) & Nắng ráo (UV ${uv > 0 ? uv.toFixed(1) : "An toàn"})`
        };
      } else {
        radarStatus = {
          isRainObserved: false,
          calibrated: false,
          note: "Radar xác nhận không mưa (0 dBZ)"
        };
      }
    }
  } else {
    // Dự phòng khi chưa tải được ảnh Radar (hiệu chỉnh theo trường quang học và mây)
    if (isRainCode && (isPhysicallySunny || (precip <= 0.2 && cloud <= 55))) {
      finalWeatherCode = cloud <= 25 ? 0 : cloud <= 50 ? 1 : 2;
      radarStatus = {
        isRainObserved: false,
        calibrated: true,
        note: `Chuẩn hóa quang học (UV ${uv.toFixed(1)}, Mây ${cloud}%)`
      };
    }
  }

  const current: CurrentWeather = {
    temperature: Math.round(cur.temperature_2m),
    apparentTemperature: Math.round(cur.apparent_temperature),
    relativeHumidity: cur.relative_humidity_2m,
    weatherCode: finalWeatherCode,
    isDay,
    windSpeed: Math.round(cur.wind_speed_10m),
    windDirection: cur.wind_direction_10m,
    surfacePressure: Math.round(cur.surface_pressure),
    uvIndex: uv,
    visibility: Math.round((cur.visibility ?? 10000) / 1000),
    precipitation: precip,
    cloudCover: cloud,
    time: cur.time,
    radarStatus
  };

  // Hourly next 24h
  const hourlyData = weatherRes.hourly;
  const hourly: HourlyForecast[] = [];
  const times: string[] = hourlyData.time || [];
  for (let i = 0; i < times.length && hourly.length < 24; i++) {
    const itemDate = new Date(times[i]);
    if (itemDate.getTime() >= Date.now() - 3600000) {
      let hCode = hourlyData.weather_code[i];
      const hPrecip = hourlyData.precipitation[i] ?? 0;
      const hProb = hourlyData.precipitation_probability[i] ?? 0;

      // Khử mã mưa ảo cho giờ khô ráo (lượng mưa < 0.15mm và xác suất mưa rất thấp < 30%)
      if ([51, 53, 55, 61, 80].includes(hCode) && hPrecip < 0.15 && hProb < 30) {
        hCode = 2; // Mây rải rác
      }

      hourly.push({
        time: times[i],
        hour: `${itemDate.getHours().toString().padStart(2, "0")}:00`,
        temperature: Math.round(hourlyData.temperature_2m[i]),
        weatherCode: hCode,
        precipitationProbability: hProb,
        precipitation: hPrecip,
        windSpeed: Math.round(hourlyData.wind_speed_10m[i]),
        relativeHumidity: hourlyData.relative_humidity_2m[i],
        isDay: hourlyData.is_day ? hourlyData.is_day[i] === 1 : (itemDate.getHours() >= 6 && itemDate.getHours() < 18)
      });
    }
  }

  // Đồng bộ slot giờ hiện tại ("Bây giờ") với trạng thái đã hiệu chuẩn
  if (hourly.length > 0) {
    hourly[0].weatherCode = finalWeatherCode;
  }

  // Daily 7 days - Hiệu chuẩn thông minh trạng thái từng ngày
  const dailyData = weatherRes.daily;
  const daily: DailyForecast[] = [];
  const dTimes: string[] = dailyData.time || [];
  const daysOfWeek = ["Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy"];

  for (let i = 0; i < dTimes.length; i++) {
    const d = new Date(dTimes[i]);
    const rawCode = dailyData.weather_code[i];
    const rainSum = Math.round((dailyData.precipitation_sum[i] ?? 0) * 10) / 10;
    const rainProb = dailyData.precipitation_probability_max[i] ?? 0;
    const maxTemp = Math.round(dailyData.temperature_2m_max[i]);
    const minTemp = Math.round(dailyData.temperature_2m_min[i]);
    const uvMax = dailyData.uv_index_max ? Math.round(dailyData.uv_index_max[i]) : 0;

    // Trích xuất các giờ thuộc ngày này để phân tích chuyên sâu
    const dayHours: HourlyForecast[] = [];
    if (hourlyData.time) {
      for (let h = 0; h < hourlyData.time.length; h++) {
        if (hourlyData.time[h].startsWith(dTimes[i])) {
          const itemDate = new Date(hourlyData.time[h]);
          dayHours.push({
            time: hourlyData.time[h],
            hour: `${itemDate.getHours().toString().padStart(2, "0")}:00`,
            temperature: Math.round(hourlyData.temperature_2m[h]),
            weatherCode: hourlyData.weather_code[h],
            precipitationProbability: hourlyData.precipitation_probability[h] ?? 0,
            precipitation: hourlyData.precipitation[h] ?? 0,
            windSpeed: Math.round(hourlyData.wind_speed_10m[h]),
            relativeHumidity: hourlyData.relative_humidity_2m[h],
            isDay: hourlyData.is_day ? hourlyData.is_day[h] === 1 : (itemDate.getHours() >= 6 && itemDate.getHours() < 18)
          });
        }
      }
    }

    const calibratedCode = calibrateDailyWeatherCode({
      rawCode,
      rainMm: rainSum,
      rainProb,
      maxTemp,
      minTemp,
      uvMax,
      isToday: i === 0,
      currentWeatherCode: finalWeatherCode,
      hourlyList: dayHours
    });

    daily.push({
      date: dTimes[i],
      dayOfWeek: i === 0 ? "Hôm nay" : daysOfWeek[d.getDay()],
      weatherCode: calibratedCode,
      tempMax: maxTemp,
      tempMin: minTemp,
      precipitationProbabilityMax: rainProb,
      precipitationSum: rainSum,
      uvIndexMax: uvMax,
      sunrise: dailyData.sunrise ? dailyData.sunrise[i].split("T")[1] : "06:00",
      sunset: dailyData.sunset ? dailyData.sunset[i].split("T")[1] : "18:00"
    });
  }

  // Air quality
  let airQuality: AirQuality | undefined = undefined;
  if (aqiRes && aqiRes.current) {
    const aqiVal = aqiRes.current.european_aqi ?? 0;
    let cat = "Tốt";
    let col = "text-emerald-400";
    let adv = "Không khí trong lành, rất thích hợp cho mọi hoạt động ngoài trời.";
    if (aqiVal > 80) {
      cat = "Rất kém";
      col = "text-red-500";
      adv = "Ô nhiễm nghiêm trọng, nên hạn chế tối đa ra ngoài và đóng cửa sổ.";
    } else if (aqiVal > 60) {
      cat = "Kém";
      col = "text-orange-400";
      adv = "Không khí ô nhiễm, người nhạy cảm nên đeo khẩu trang lọc bụi mịn N95.";
    } else if (aqiVal > 40) {
      cat = "Trung bình";
      col = "text-amber-400";
      adv = "Chất lượng không khí chấp nhận được, có thể mở cửa sổ thông thoáng.";
    }

    airQuality = {
      pm25: Math.round(aqiRes.current.pm2_5 * 10) / 10,
      pm10: Math.round(aqiRes.current.pm10 * 10) / 10,
      aqiIndex: aqiVal,
      aqiScale: "European AQI",
      category: cat,
      color: col,
      advice: adv
    };
  }

  const lifestyles = calculateLifestyles(current, daily);

  return {
    location,
    current,
    hourly,
    daily,
    airQuality,
    lifestyles,
    updatedAt: new Date().toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" })
  };
}

// ============================================================================
// CHỦ QUYỀN BIỂN ĐẢO VIỆT NAM (HOÀNG SA & TRƯỜNG SA) VÀ BẢO VỆ ĐỊA LÝ QUỐC GIA
// ============================================================================
export function isHoangSaArea(lat: number, lon: number): boolean {
  return lat >= 15.0 && lat <= 17.8 && lon >= 110.0 && lon <= 113.8;
}

export function isTruongSaArea(lat: number, lon: number): boolean {
  return lat >= 6.5 && lat <= 12.8 && lon >= 111.0 && lon <= 118.5;
}

export function isEastSeaArea(lat: number, lon: number): boolean {
  return lat >= 6.0 && lat <= 21.5 && lon >= 108.5 && lon <= 119.5;
}

export function normalizeVietnamese(text: string): string {
  return text
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d")
    .replace(/Đ/g, "D")
    .toLowerCase()
    .trim();
}

export function compactVietnameseAdministrativeName(value?: string | null): string | null {
  if (!value) return null;
  const trimmed = value.trim();
  if (!trimmed) return null;
  return (
    trimmed
      .replace(/^(phường|xã|thị trấn|quận|huyện|thị xã|thành phố|tỉnh)\s+/i, "")
      .trim() || trimmed
  );
}

export function formatHierarchyLocation(
  ward?: string | null,
  district?: string | null,
  province?: string | null,
  country = "Việt Nam"
): { name: string; admin1: string; country: string } {
  const cleanWard = compactVietnameseAdministrativeName(ward);
  const cleanDistrict = compactVietnameseAdministrativeName(district);
  const cleanProvince = compactVietnameseAdministrativeName(province);
  const cleanCountry = country.trim() || "Việt Nam";

  let primaryName = "Vị trí của bạn";
  if (ward && district && cleanWard?.toLowerCase() !== cleanDistrict?.toLowerCase()) {
    primaryName = `${ward}, ${district}`;
  } else if (cleanWard && cleanDistrict && cleanWard.toLowerCase() !== cleanDistrict.toLowerCase()) {
    primaryName = `${cleanWard}, ${cleanDistrict}`;
  } else if (ward && province && cleanWard?.toLowerCase() !== cleanProvince?.toLowerCase()) {
    primaryName = `${ward}, ${province}`;
  } else if (district && province && cleanDistrict?.toLowerCase() !== cleanProvince?.toLowerCase()) {
    primaryName = `${district}, ${province}`;
  } else if (ward) {
    primaryName = ward;
  } else if (district) {
    primaryName = district;
  } else if (province) {
    primaryName = province;
  }

  let secondaryLocation = cleanCountry;
  if (province && !primaryName.toLowerCase().includes(cleanProvince?.toLowerCase() || "")) {
    secondaryLocation = `${province}, ${cleanCountry}`;
  } else if (district && !primaryName.toLowerCase().includes(cleanDistrict?.toLowerCase() || "")) {
    secondaryLocation = `${district}, ${cleanCountry}`;
  }

  return {
    name: primaryName,
    admin1: secondaryLocation,
    country: cleanCountry
  };
}

/**
 * Tra cứu địa chỉ chi tiết theo tọa độ GPS: Cấp Xã/Phường -> Quận/Huyện -> Tỉnh/TP
 */
export async function reverseGeocode(latitude: number, longitude: number): Promise<CityLocation> {
  // 1. Kiểm tra chủ quyền Hoàng Sa & Trường Sa
  if (isHoangSaArea(latitude, longitude)) {
    return { ...HOANG_SA_LOCATION, isCurrentLocation: true };
  }
  if (isTruongSaArea(latitude, longitude)) {
    return { ...TRUONG_SA_LOCATION, isCurrentLocation: true };
  }

  // 2. OpenStreetMap Nominatim với tiếng Việt đầy đủ
  try {
    const osmUrl = `https://nominatim.openstreetmap.org/reverse?lat=${latitude}&lon=${longitude}&format=json&addressdetails=1&accept-language=vi`;
    const res = await fetch(osmUrl, {
      headers: {
        "User-Agent": "Weather24h-Web/1.0 (vi-VN)"
      }
    });
    if (res.ok) {
      const data = await res.json();
      if (data && data.address) {
        const addr = data.address;
        const ward = addr.suburb || addr.quarter || addr.neighbourhood || addr.village || addr.town || addr.hamlet;
        const district = addr.city_district || addr.district || addr.county || addr.municipality;
        const province = addr.state || addr.province || addr.city;
        const country = addr.country || "Việt Nam";

        if (ward || district || province) {
          const hierarchy = formatHierarchyLocation(ward, district, province, country);
          return {
            name: hierarchy.name,
            admin1: hierarchy.admin1,
            country: hierarchy.country,
            latitude,
            longitude,
            isCurrentLocation: true
          };
        }
      }
    }
  } catch (e) {
    console.warn("Lỗi Nominatim reverse geocode:", e);
  }

  // 3. Dự phòng BigDataCloud
  try {
    const bdcUrl = `https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=${latitude}&longitude=${longitude}&localityLanguage=vi`;
    const res = await fetch(bdcUrl);
    if (res.ok) {
      const data = await res.json();
      const city = data.city || "";
      const locality = data.locality || "";
      const principalSubdivision = data.principalSubdivision || "";
      const countryName = data.countryName || "Việt Nam";

      let adminWard: string | null = null;
      let adminDistrict: string | null = null;
      if (data.localityInfo && Array.isArray(data.localityInfo.administrative)) {
        for (const item of data.localityInfo.administrative) {
          const nameStr = item.name || "";
          const descStr = item.description || "";
          if (
            descStr.includes("phường") ||
            descStr.includes("xã") ||
            nameStr.startsWith("Phường") ||
            nameStr.startsWith("Xã")
          ) {
            if (!adminWard) adminWard = nameStr;
          } else if (
            descStr.includes("quận") ||
            descStr.includes("huyện") ||
            descStr.includes("thị xã")
          ) {
            if (!adminDistrict) adminDistrict = nameStr;
          }
        }
      }

      const finalWard = adminWard || (locality.includes("phường") || locality.includes("xã") ? locality : null);
      const finalDistrict = adminDistrict || (city && !city.includes("Hồ Chí Minh") && !city.includes("Hà Nội") ? city : null);

      const hierarchy = formatHierarchyLocation(finalWard, finalDistrict, principalSubdivision, countryName);
      return {
        name: hierarchy.name,
        admin1: hierarchy.admin1,
        country: hierarchy.country,
        latitude,
        longitude,
        isCurrentLocation: true
      };
    }
  } catch (e) {
    console.warn("Lỗi BigDataCloud reverse geocode:", e);
  }

  return {
    name: "Vị trí GPS",
    admin1: "Việt Nam",
    country: "Việt Nam",
    latitude,
    longitude,
    isCurrentLocation: true
  };
}

interface GeocodingItem {
  name: string;
  country?: string;
  admin1?: string;
  admin2?: string;
  latitude: number;
  longitude: number;
}

interface NominatimSearchResult {
  lat: string;
  lon: string;
  name?: string;
  display_name?: string;
  address?: {
    suburb?: string;
    quarter?: string;
    village?: string;
    town?: string;
    hamlet?: string;
    county?: string;
    district?: string;
    city_district?: string;
    municipality?: string;
    state?: string;
    province?: string;
    city?: string;
    country?: string;
  };
}

export async function searchCities(query: string): Promise<CityLocation[]> {
  const trimmed = query.trim();
  if (!trimmed) return VIETNAM_MAJOR_CITIES.slice(0, 10);

  const normQuery = normalizeVietnamese(trimmed);

  // ==========================================================================
  // NGUYÊN TẮC BẢO VỆ CHỦ QUYỀN BIỂN ĐẢO QUỐC GIA:
  // Khi tìm kiếm các từ khóa liên quan đến Biển Đông / Hoàng Sa / Trường Sa
  // hoặc tên gọi sai phạm do Trung Quốc đơn phương đặt (như "Tam Sa", "Sansha", "Xisha", "Nansha"):
  // => LẬP TỨC TRẢ VỀ DUY NHẤT 2 QUẦN ĐẢO HOÀNG SA VÀ TRƯỜNG SA THUỘC CHỦ QUYỀN VIỆT NAM.
  // => LOẠI BỎ TRIỆT ĐỂ MỌI KẾT QUẢ SAI PHẠM TRUNG QUỐC RA KHỎI BẢN ĐỒ.
  // ==========================================================================
  const isHoangSaTrigger =
    normQuery.includes("tam sa") ||
    normQuery.includes("sansha") ||
    normQuery.includes("xisha") ||
    normQuery.includes("hoang sa") ||
    normQuery.includes("paracel") ||
    normQuery.includes("tay sa");

  const isTruongSaTrigger =
    normQuery.includes("truong sa") ||
    normQuery.includes("spratly") ||
    normQuery.includes("nansha") ||
    normQuery.includes("nam sa");

  if (isHoangSaTrigger || isTruongSaTrigger) {
    if (isTruongSaTrigger && !isHoangSaTrigger) {
      return [TRUONG_SA_LOCATION, HOANG_SA_LOCATION];
    }
    return [HOANG_SA_LOCATION, TRUONG_SA_LOCATION];
  }

  // Khớp danh sách thành phố sẵn có trong nước
  const localMatches = VIETNAM_MAJOR_CITIES.filter((c) => {
    const nName = normalizeVietnamese(c.name);
    const nAdmin = c.admin1 ? normalizeVietnamese(c.admin1) : "";
    return nName.includes(normQuery) || nAdmin.includes(normQuery);
  });

  const rawResults: CityLocation[] = [];

  // Chạy song song Open-Meteo và OpenStreetMap Nominatim để có xã/phường/tỉnh chi tiết
  try {
    const encoded = encodeURIComponent(trimmed);

    const [openMeteoRes, nominatimRes] = await Promise.allSettled([
      fetch(
        `https://geocoding-api.open-meteo.com/v1/search?name=${encoded}&count=8&language=vi&format=json`
      ).then((r) => (r.ok ? r.json() : null)),
      fetch(
        `https://nominatim.openstreetmap.org/search?q=${encoded}&format=json&addressdetails=1&limit=6&accept-language=vi`,
        { headers: { "User-Agent": "Weather24h-Web/1.0 (vi-VN)" } }
      ).then((r) => (r.ok ? r.json() : null))
    ]);

    // Parse Nominatim (rất mạnh ở cấp Xã/Phường/Quận/Huyện)
    if (nominatimRes.status === "fulfilled" && Array.isArray(nominatimRes.value)) {
      for (const item of nominatimRes.value as NominatimSearchResult[]) {
        const lat = parseFloat(item.lat);
        const lon = parseFloat(item.lon);
        if (isNaN(lat) || isNaN(lon)) continue;

        const addr = item.address;
        const ward =
          addr?.suburb ||
          addr?.quarter ||
          addr?.village ||
          addr?.town ||
          addr?.hamlet ||
          item.name;
        const district =
          addr?.county ||
          addr?.district ||
          addr?.city_district ||
          addr?.municipality;
        const province = addr?.state || addr?.province || addr?.city;
        const country = addr?.country || "Việt Nam";

        const formatted = formatHierarchyLocation(ward, district, province, country);
        rawResults.push({
          name: formatted.name,
          country: formatted.country,
          admin1: formatted.admin1,
          latitude: lat,
          longitude: lon
        });
      }
    }

    // Parse Open-Meteo
    if (
      openMeteoRes.status === "fulfilled" &&
      openMeteoRes.value &&
      Array.isArray(openMeteoRes.value.results)
    ) {
      for (const item of openMeteoRes.value.results as GeocodingItem[]) {
        rawResults.push({
          name: item.name,
          country: item.country || "Việt Nam",
          admin1: item.admin1 || item.admin2 || null,
          latitude: item.latitude,
          longitude: item.longitude
        });
      }
    }
  } catch (e) {
    console.error("Lỗi tìm kiếm địa điểm:", e);
  }

  // ==========================================================================
  // BỘ LỌC CHỦ QUYỀN TOÀN DIỆN CHO TẤT CẢ KẾT QUẢ API BÊN NGOÀI
  // ==========================================================================
  const sanitizedResults: CityLocation[] = [];
  const ILLEGAL_PATTERNS = /(sansha|tam\s*sa|xisha|nansha|zhongsha|chidao)/i;

  for (const item of rawResults) {
    const fullText = `${item.name} ${item.admin1 || ""} ${item.country || ""}`;
    const normFull = normalizeVietnamese(fullText);

    // 1. Loại bỏ hoàn toàn mọi kết quả chứa tên phi pháp Sansha / Tam Sa của TQ
    if (ILLEGAL_PATTERNS.test(normFull)) {
      continue;
    }

    // 2. Nếu tọa độ thuộc quần đảo Hoàng Sa
    if (isHoangSaArea(item.latitude, item.longitude)) {
      sanitizedResults.push(HOANG_SA_LOCATION);
      continue;
    }

    // 3. Nếu tọa độ thuộc quần đảo Trường Sa
    if (isTruongSaArea(item.latitude, item.longitude)) {
      sanitizedResults.push(TRUONG_SA_LOCATION);
      continue;
    }

    // 4. Nếu tọa độ thuộc vùng Biển Đông nhưng lại ghi nhận là lãnh thổ Trung Quốc -> Loại bỏ ngay
    if (
      isEastSeaArea(item.latitude, item.longitude) &&
      (normFull.includes("trung quoc") || normFull.includes("china") || normFull.includes("hai nam") || normFull.includes("hainan"))
    ) {
      continue;
    }

    sanitizedResults.push(item);
  }

  // Kết hợp danh sách local và API, loại bỏ trùng lặp
  const combined = [...localMatches, ...sanitizedResults];
  const unique = combined.filter(
    (v, i, a) =>
      a.findIndex(
        (t) =>
          (t.name.toLowerCase() === v.name.toLowerCase() &&
            Math.abs(t.latitude - v.latitude) < 0.05 &&
            Math.abs(t.longitude - v.longitude) < 0.05) ||
          (Math.abs(t.latitude - v.latitude) < 0.01 &&
            Math.abs(t.longitude - v.longitude) < 0.01)
      ) === i
  );

  return unique.length > 0 ? unique : localMatches;
}
