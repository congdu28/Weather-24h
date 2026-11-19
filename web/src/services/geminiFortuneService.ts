import {
  DetailedBirthFortune,
  DeepFortuneAnalysis,
  elementLabel
} from "../utils/lunarAlmanac";
import { DeepLoveAiAnalysis, LoveCompatibilityResult } from "../utils/loveCompatibility";

const CACHE_KEY_PREFIX = "weather24h_gemini38_fortune_v1_";
const LOVE_CACHE_KEY_PREFIX = "weather24h_gemini_love_v1_";

function resolveGeminiApiKey(customApiKey?: string): string {
  let apiKey = (customApiKey || "").trim();
  if (!apiKey && typeof window !== "undefined") {
    try {
      const savedSettings = localStorage.getItem("weather24h_settings");
      if (savedSettings) {
        const parsed = JSON.parse(savedSettings);
        if (parsed?.geminiApiKey && typeof parsed.geminiApiKey === "string") {
          apiKey = parsed.geminiApiKey.trim();
        }
      }
    } catch {
      // ignore
    }
  }
  if (!apiKey && typeof process !== "undefined" && process.env?.NEXT_PUBLIC_GEMINI_API_KEY) {
    apiKey = process.env.NEXT_PUBLIC_GEMINI_API_KEY.trim();
  }
  return apiKey;
}

export function getFortuneProfileCacheKey(fortune: DetailedBirthFortune): string {
  const { profile, solarDate, birthHourFortune } = fortune;
  return `${CACHE_KEY_PREFIX}${profile.gender}_${solarDate.year}_${solarDate.month}_${solarDate.day}_h${birthHourFortune.hourChiIndex}`;
}

export function getCachedGeminiFortune(fortune: DetailedBirthFortune): DeepFortuneAnalysis | null {
  if (typeof window === "undefined") return null;
  try {
    const key = getFortuneProfileCacheKey(fortune);
    const raw = localStorage.getItem(key);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as DeepFortuneAnalysis;
    if (parsed && Array.isArray(parsed.aspects) && parsed.aspects.length >= 6) {
      return parsed;
    }
  } catch {
    // ignore storage errors
  }
  return null;
}

const GEMINI_MODEL_FALLBACKS: Array<{ id: string; useThinkingHigh: boolean }> = [
  { id: "gemini-3.8-flash", useThinkingHigh: true },
  { id: "gemini-3.5-flash-lite", useThinkingHigh: false },
  { id: "gemini-3.1-flash-lite", useThinkingHigh: false }
];

function parseFortuneJson(rawText: string) {
  const cleaned = rawText
    .replace(/^```(?:json)?\s*/i, "")
    .replace(/\s*```$/i, "")
    .trim();
  return JSON.parse(cleaned);
}

/**
 * Phân tích chuyên sâu lá số Tứ Trụ Bát Tự & Tử Vi bằng Gemini API.
 * Tự động chuyển đổi qua danh sách model dự phòng nếu model chính bị giới hạn (429) hoặc quá tải,
 * đồng thời tích hợp sẵn hệ thống suy luận Bát Tự 8 phương diện để luôn trả về kết quả tức thì.
 */
export async function analyzeFortuneWithGemini38FlashHigh(
  fortune: DetailedBirthFortune,
  customApiKey?: string,
  forceRefresh = false
): Promise<{ analysis: DeepFortuneAnalysis; source: "gemini-3.8-flash-live" | "gemini-3.8-flash-builtin" }> {
  if (!forceRefresh) {
    const cached = getCachedGeminiFortune(fortune);
    if (cached) {
      return { analysis: cached, source: "gemini-3.8-flash-live" };
    }
  }

  // Tìm API key từ tham số truyền vào, từ cài đặt người dùng trong localStorage, hoặc biến môi trường
  let apiKey = (customApiKey || "").trim();
  if (!apiKey && typeof window !== "undefined") {
    try {
      const savedSettings = localStorage.getItem("weather24h_settings");
      if (savedSettings) {
        const parsed = JSON.parse(savedSettings);
        if (parsed?.geminiApiKey && typeof parsed.geminiApiKey === "string") {
          apiKey = parsed.geminiApiKey.trim();
        }
      }
    } catch {
      // ignore
    }
  }
  if (!apiKey && typeof process !== "undefined" && process.env?.NEXT_PUBLIC_GEMINI_API_KEY) {
    apiKey = process.env.NEXT_PUBLIC_GEMINI_API_KEY.trim();
  }

  const {
    profile,
    solarDate,
    lunarDate,
    personalProfile,
    cungPhi,
    cungPhiGroup,
    westernZodiac,
    numerology,
    saoHan2026,
    fourPillars,
    birthHourFortune
  } = fortune;

  const genderText = profile.gender === "male" ? "Nam mạng" : "Nữ mạng";
  const hourText = birthHourFortune.hasBirthHour
    ? `Giờ ${birthHourFortune.hourChi} (${birthHourFortune.hourRange}) - Trụ Giờ: ${birthHourFortune.hourCanChi} (${birthHourFortune.hourNapAm})`
    : `Chưa rõ giờ sinh (Mặc định tham chiếu Giờ ${birthHourFortune.hourChi} - ${birthHourFortune.hourCanChi})`;

  const dungThanText = fourPillars.hyDungThan.map(elementLabel).join(", ");
  const lackingText =
    fourPillars.lackingElements.length > 0
      ? fourPillars.lackingElements.map(elementLabel).join(", ")
      : "Đủ cả 5 hành Kim - Mộc - Thủy - Hỏa - Thổ";

  if (apiKey && apiKey.startsWith("AIza")) {
    try {
      const prompt = `Bạn là chuyên gia Tử Vi, Tứ Trụ Bát Tự, Phong Thủy Bát Trạch và Thần Số Học hàng đầu Việt Nam.
Hãy phân tích chi tiết tất cả các mặt cuộc đời cho gia chủ với thông tin lá số sau (viết bằng tiếng Việt gần gũi, dễ hiểu, rõ ràng, tránh dùng các thuật ngữ Hán Việt quá học thuật gây khó hiểu):

- Giới tính: ${genderText}
- Ngày sinh Dương lịch: ${solarDate.day}/${solarDate.month}/${solarDate.year}
- Ngày sinh Âm lịch: Ngày ${lunarDate.day} tháng ${lunarDate.month}${lunarDate.isLeapMonth ? " (Nhuận)" : ""} năm ${lunarDate.yearCanChi} (${lunarDate.year})
- Giờ sinh: ${hourText}
- 4 Trụ Sinh (Năm - Tháng - Ngày - Giờ):
  + Năm sinh: ${fourPillars.yearPillar.canChi} (${fourPillars.yearPillar.napAm})
  + Tháng sinh: ${fourPillars.monthPillar.canChi} (${fourPillars.monthPillar.napAm})
  + Ngày sinh: ${fourPillars.dayPillar.canChi} (${fourPillars.dayPillar.napAm}) - Hành ngày sinh: ${elementLabel(fourPillars.nhatChuElement)}
  + Giờ sinh: ${birthHourFortune.hourCanChi} (${birthHourFortune.hourNapAm})
- Ngũ hành khuyết: ${lackingText} | Ngũ hành cần bổ sung để may mắn: ${dungThanText}
- Mệnh Nạp Âm năm sinh: ${personalProfile.napAm} (Mệnh ${elementLabel(personalProfile.element)})
- Cung Phi Bát Trạch: Cung ${cungPhi} (${cungPhiGroup})
- Con số chủ đạo Pythagoras: Số ${numerology.lifePathNumber} (${numerology.title})
- Cung Hoàng Đạo: ${westernZodiac.name} (${westernZodiac.element})
- Sao Hạn năm 2026 (Bính Ngọ): ${saoHan2026.lunarAge} tuổi mụ, Sao ${saoHan2026.sao} (${saoHan2026.saoType}), Hạn ${saoHan2026.han}.

Hãy trả về kết quả ĐÚNG ĐỊNH DẠNG JSON theo schema sau:
{
  "overallHeadline": "${genderText} tuổi ${personalProfile.canChi} (${personalProfile.birthYear} • ${personalProfile.napAm})",
  "overallScore": 90,
  "executiveSummary": "Đoạn tổng kết toàn cục lá số dựa trên Giờ, Ngày, Tháng, Năm sinh và Giới tính (khoảng 3-4 câu dễ hiểu, thiết thực)",
  "aspects": [
    {
      "id": "battu_nguhanh",
      "title": "1. Bản Mệnh 4 Trụ Sinh & Ngũ Hành Cần Bổ Sung",
      "badge": "Nên bổ sung hành ${dungThanText}",
      "icon": "☯️",
      "summary": "Luận giải tổng quan 4 cột mốc Năm - Tháng - Ngày - Giờ sinh và cách cân bằng ngũ hành",
      "details": ["Chi tiết 1...", "Chi tiết 2...", "Chi tiết 3...", "Chi tiết 4..."],
      "actionableAdvice": "Lời khuyên ứng dụng cụ thể"
    },
    {
      "id": "tinhcach_tamly",
      "title": "2. Tính Cách, Nội Tâm & Điểm Mạnh Bẩm Sinh",
      "badge": "Mệnh ${elementLabel(personalProfile.element)} • Số ${numerology.lifePathNumber}",
      "icon": "🧠",
      "summary": "...",
      "details": ["...", "...", "..."],
      "actionableAdvice": "..."
    },
    {
      "id": "sunghiep_congdanh",
      "title": "3. Công Việc, Sự Nghiệp & Ngành Nghề Phù Hợp",
      "badge": "Phát triển 30 - 50 tuổi",
      "icon": "🚀",
      "summary": "...",
      "details": ["...", "...", "..."],
      "actionableAdvice": "..."
    },
    {
      "id": "tailoc_dientrach",
      "title": "4. Tiền Bạc, Nhà Đất & Cách Quản Lý Tài Chính",
      "badge": "Tài lộc & Nhà đất",
      "icon": "💰",
      "summary": "...",
      "details": ["...", "...", "..."],
      "actionableAdvice": "..."
    },
    {
      "id": "tinhduyen_giadao",
      "title": "5. Tình Duyên, Hôn Nhân & Đường Con Cái",
      "badge": "Gia đạo & Con cái",
      "icon": "❤️",
      "summary": "...",
      "details": ["...", "...", "..."],
      "actionableAdvice": "..."
    },
    {
      "id": "suckhoe_tangphu",
      "title": "6. Sức Khỏe & Chế Độ Sinh Hoạt Cần Lưu Ý",
      "badge": "Chăm sóc sức khỏe",
      "icon": "🌿",
      "summary": "...",
      "details": ["...", "...", "..."],
      "actionableAdvice": "..."
    },
    {
      "id": "daivan_luunien2026",
      "title": "7. Dự Báo Vận Trình Năm 2026 (Bính Ngọ)",
      "badge": "Sao ${saoHan2026.sao} • Hạn ${saoHan2026.han}",
      "icon": "🔮",
      "summary": "...",
      "details": ["...", "...", "..."],
      "actionableAdvice": "..."
    },
    {
      "id": "phongthuy_caivan",
      "title": "8. Cẩm Nang Phong Thủy May Mắn Hàng Ngày",
      "badge": "Cung ${cungPhi}",
      "icon": "🧭",
      "summary": "...",
      "details": ["...", "...", "..."],
      "actionableAdvice": "..."
    }
  ]
}`;

      for (const candidate of GEMINI_MODEL_FALLBACKS) {
        try {
          const generationConfig: Record<string, unknown> = {
            temperature: 0.65,
            responseMimeType: "application/json"
          };
          if (candidate.useThinkingHigh) {
            generationConfig.thinkingConfig = {
              thinkingLevel: "HIGH"
            };
          }

          const response = await fetch(
            `https://generativelanguage.googleapis.com/v1beta/models/${candidate.id}:generateContent?key=${encodeURIComponent(apiKey)}`,
            {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({
                contents: [{ role: "user", parts: [{ text: prompt }] }],
                generationConfig
              })
            }
          );

          if (!response.ok) {
            // API key không hợp lệ thì dừng luôn, không thử thêm model khác
            if (response.status === 401 || response.status === 403) {
              break;
            }
            // Với 429 (hết quota/rate limit), 404/400 (model không hỗ trợ), 5xx -> chuyển sang model dự phòng tiếp theo
            continue;
          }

          const data = await response.json();
          const rawText =
            data?.candidates?.[0]?.content?.parts
              ?.map((p: { text?: string }) => p.text || "")
              .join("")
              .trim() || "";

          if (!rawText) continue;

          const parsed = parseFortuneJson(rawText);
          if (parsed && Array.isArray(parsed.aspects) && parsed.aspects.length >= 6) {
            const finalResult: DeepFortuneAnalysis = {
              modelVersion: "AI",
              thinkingMode: "Luận giải chi tiết bằng AI",
              overallHeadline: parsed.overallHeadline || fortune.deepAnalysis.overallHeadline,
              overallScore: Number(parsed.overallScore) || fortune.deepAnalysis.overallScore,
              executiveSummary: parsed.executiveSummary || fortune.deepAnalysis.executiveSummary,
              aspects: parsed.aspects
            };

            if (typeof window !== "undefined") {
              try {
                localStorage.setItem(getFortuneProfileCacheKey(fortune), JSON.stringify(finalResult));
              } catch {
                // ignore quota errors
              }
            }

            return { analysis: finalResult, source: "gemini-3.8-flash-live" };
          }
        } catch {
          // Lỗi parse hoặc lỗi mạng ở model hiện tại -> thử model dự phòng tiếp theo
          continue;
        }
      }
    } catch {
      // Nếu lỗi mạng hoặc chưa cấu hình key ngoài, dùng ngay hệ thống Built-in Engine
    }
  }

  // Mô phỏng độ trễ suy luận (High Thinking) mượt mà khi người dùng bấm nút phân tích chuyên sâu
  await new Promise((resolve) => setTimeout(resolve, 650));

  return {
    analysis: fortune.deepAnalysis,
    source: "gemini-3.8-flash-builtin"
  };
}

export function getLoveCompatibilityCacheKey(result: LoveCompatibilityResult): string {
  const p1 = result.person1;
  const p2 = result.person2;
  return `${LOVE_CACHE_KEY_PREFIX}${p1.profile.gender}_${p1.solarDate.year}_${p1.solarDate.month}_${p1.solarDate.day}_h${p1.birthHourFortune.hourChiIndex}__${p2.profile.gender}_${p2.solarDate.year}_${p2.solarDate.month}_${p2.solarDate.day}_h${p2.birthHourFortune.hourChiIndex}`;
}

export function getCachedLoveCompatibilityAi(result: LoveCompatibilityResult): DeepLoveAiAnalysis | null {
  if (typeof window === "undefined") return null;
  try {
    const raw = localStorage.getItem(getLoveCompatibilityCacheKey(result));
    if (!raw) return null;
    const parsed = JSON.parse(raw) as DeepLoveAiAnalysis;
    if (parsed && Array.isArray(parsed.aspects) && parsed.aspects.length >= 4) {
      return parsed;
    }
  } catch {
    // ignore
  }
  return null;
}

function buildBuiltinLoveFallback(result: LoveCompatibilityResult): DeepLoveAiAnalysis {
  return {
    headline: `${result.verdictTitle}: ${result.person1.displayName} & ${result.person2.displayName}`,
    compatibilityScore: result.totalScore,
    executiveSummary: result.summary,
    aspects: result.dimensions.map((d) => ({
      id: d.id,
      title: d.title,
      badge: `${d.badge} (${d.score}/${d.maxScore})`,
      icon: d.icon,
      summary: d.summary,
      details: d.details,
      advice: d.advice
    }))
  };
}

export async function analyzeLoveCompatibilityWithGemini(
  result: LoveCompatibilityResult,
  customApiKey?: string,
  forceRefresh = false
): Promise<{ analysis: DeepLoveAiAnalysis; source: "gemini-live" | "builtin" }> {
  if (!forceRefresh) {
    const cached = getCachedLoveCompatibilityAi(result);
    if (cached) {
      return { analysis: cached, source: "gemini-live" };
    }
  }

  const apiKey = resolveGeminiApiKey(customApiKey);
  if (apiKey) {
    const { person1: p1, person2: p2 } = result;
    const formatPerson = (p: LoveCompatibilityResult["person1"]) =>
      `- ${p.displayName} (${p.profile.gender === "male" ? "Nam" : "Nữ"}):
  + Dương lịch: ${p.solarDate.day}/${p.solarDate.month}/${p.solarDate.year} | Âm lịch: ${p.lunarDate.day}/${p.lunarDate.month}/${p.lunarDate.year} (${p.personalProfile.canChi})
  + Mệnh Nạp Âm: ${p.personalProfile.napAm} (${elementLabel(p.personalProfile.element)}) | Cung Phi: ${p.cungPhi} (${p.cungPhiGroup})
  + Tứ Trụ: Năm ${p.fourPillars.yearPillar.canChi} • Tháng ${p.fourPillars.monthPillar.canChi} • Ngày ${p.fourPillars.dayPillar.canChi} • Giờ ${p.fourPillars.hourPillar ? p.fourPillars.hourPillar.canChi : p.birthHourFortune.hourCanChi}${!p.birthHourFortune.hasBirthHour ? " (Chưa rõ giờ)" : ""}
  + Cung Hoàng Đạo: ${p.westernZodiac.name} (${p.westernZodiac.element}) | Thần Số Học: Số ${p.numerology.lifePathNumber}`;

    const prompt = `Bạn là chuyên gia Tử Vi Đẩu Số, Tứ Trụ Bát Tự, Phong Thủy Bát Trạch và Tâm lý Hôn nhân gia đình Việt Nam.
Hãy luận giải chuyên sâu tình duyên và mức độ hòa hợp của cặp đôi sau (giọng văn tinh tế, khách quan, sâu sắc, mang tính xây dựng bằng tiếng Việt):

${formatPerson(p1)}

${formatPerson(p2)}

Điểm tổng hợp nền tảng: ${result.totalScore}/100 (${result.verdictTitle}).
Bù trừ Ngũ Hành: ${result.complementaryElements.join(" ")}

Yêu cầu trả về đúng định dạng JSON thuần (không bọc markdown), cấu trúc:
{
  "headline": "Tiêu đề ngắn gọn, sâu sắc cho mối duyên này",
  "compatibilityScore": ${result.totalScore},
  "executiveSummary": "Đoạn tổng quan 3-4 câu phân tích cốt lõi mối quan hệ giữa ${p1.displayName} và ${p2.displayName}",
  "aspects": [
    {
      "id": "duyen_phan",
      "title": "1. Sợi Duyên Phận & Sức Hút Ban Đầu",
      "badge": "Nhãn ngắn 2-4 từ",
      "icon": "💕",
      "summary": "Tóm tắt 1-2 câu",
      "details": ["Ý chi tiết 1", "Ý chi tiết 2"],
      "advice": "Lời khuyên thực tế"
    },
    {
      "id": "tinh_cach_giao_tiep",
      "title": "2. Tính Cách, Giao Tiếp & Xử Lý Bất Đồng",
      "badge": "Nhãn ngắn",
      "icon": "🗣️",
      "summary": "...",
      "details": ["...", "..."],
      "advice": "..."
    },
    {
      "id": "hon_nhan_giadao",
      "title": "3. Hôn Nhân, Gia Đạo & Cung Phu Thê",
      "badge": "Nhãn ngắn",
      "icon": "🏡",
      "summary": "...",
      "details": ["...", "..."],
      "advice": "..."
    },
    {
      "id": "tai_loc_su_nghiep",
      "title": "4. Tài Lộc, Sự Nghiệp & Bù Trừ Ngũ Hành",
      "badge": "Nhãn ngắn",
      "icon": "🪙",
      "summary": "...",
      "details": ["...", "..."],
      "advice": "..."
    },
    {
      "id": "con_cai_hau_van",
      "title": "5. Đường Con Cái & Hậu Vận Về Sau",
      "badge": "Nhãn ngắn",
      "icon": "🌱",
      "summary": "...",
      "details": ["...", "..."],
      "advice": "..."
    },
    {
      "id": "hoa_giai_phong_thuy",
      "title": "6. Phong Thủy Hóa Giải & Bí Quyết Giữ Lửa",
      "badge": "Nhãn ngắn",
      "icon": "🧭",
      "summary": "...",
      "details": ["...", "..."],
      "advice": "..."
    }
  ]
}`;

    for (const candidate of GEMINI_MODEL_FALLBACKS) {
      try {
        const generationConfig: Record<string, unknown> = {
          temperature: 0.65,
          responseMimeType: "application/json"
        };
        if (candidate.useThinkingHigh) {
          generationConfig.thinkingConfig = {
            thinkingLevel: "HIGH"
          };
        }

        const response = await fetch(
          `https://generativelanguage.googleapis.com/v1beta/models/${candidate.id}:generateContent?key=${encodeURIComponent(apiKey)}`,
          {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              contents: [{ role: "user", parts: [{ text: prompt }] }],
              generationConfig
            })
          }
        );

        if (!response.ok) {
          if (response.status === 401 || response.status === 403) break;
          continue;
        }

        const data = await response.json();
        const rawText =
          data?.candidates?.[0]?.content?.parts
            ?.map((p: { text?: string }) => p.text || "")
            .join("")
            .trim() || "";

        if (!rawText) continue;

        const parsed = parseFortuneJson(rawText) as DeepLoveAiAnalysis;
        if (parsed && Array.isArray(parsed.aspects) && parsed.aspects.length >= 4) {
          const finalResult: DeepLoveAiAnalysis = {
            headline: parsed.headline || `${result.verdictTitle}`,
            compatibilityScore: Number(parsed.compatibilityScore) || result.totalScore,
            executiveSummary: parsed.executiveSummary || result.summary,
            aspects: parsed.aspects
          };
          if (typeof window !== "undefined") {
            try {
              localStorage.setItem(getLoveCompatibilityCacheKey(result), JSON.stringify(finalResult));
            } catch {
              // ignore
            }
          }
          return { analysis: finalResult, source: "gemini-live" };
        }
      } catch {
        continue;
      }
    }
  }

  await new Promise((resolve) => setTimeout(resolve, 450));
  return {
    analysis: buildBuiltinLoveFallback(result),
    source: "builtin"
  };
}
