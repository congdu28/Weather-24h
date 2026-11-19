import { CAN, CHI } from "./lunarCalendar";
import {
  FiveElement,
  UserBirthProfile,
  elementLabel,
  elementOfNapAm,
  getDetailedBirthFortune,
  napAm
} from "./lunarAlmanac";

export interface TuViPersonInput {
  name: string;
  gender: "male" | "female";
  day: number;
  month: number;
  year: number;
  birthHour: number; // 0..11
  isLunar: boolean;
}

export type StarBrightness = "M" | "V" | "Đ" | "B" | "H";

export interface TuViStar {
  name: string;
  category: "chinh" | "cat" | "sat" | "tu_hoa";
  element: FiveElement;
  brightness?: StarBrightness;
  tuHoaTag?: "Hóa Lộc" | "Hóa Quyền" | "Hóa Khoa" | "Hóa Kỵ";
}

export interface TuViPalace {
  chiIndex: number; // 0..11 (Tý..Hợi)
  chiName: string;
  palaceCan: string;
  palaceName: string; // Mệnh, Phụ Mẫu, ...
  isMenh: boolean;
  isThan: boolean;
  daiVanRange: string; // VD: "22 - 31"
  daiVanStart: number;
  tuan: boolean;
  triet: boolean;
  trangSinh: string;
  chinhTinh: TuViStar[];
  catTinh: TuViStar[];
  satTinh: TuViStar[];
  score: number; // 0..100 đánh giá độ cát lợi của cung
  interpretation: string;
  keyHighlights: string[];
  advice: string;
}

export interface TuViChartResult {
  displayName: string;
  gender: "male" | "female";
  amDuongLabel: string; // Dương Nam / Âm Nam / Dương Nữ / Âm Nữ
  isThuan: boolean;
  solarDate: { day: number; month: number; year: number };
  lunarDate: { day: number; month: number; year: number; isLeapMonth: boolean };
  hourChiIndex: number;
  hourLabel: string;
  yearCanChi: string;
  napAmMenh: string;
  menhElement: FiveElement;
  cucNumber: 2 | 3 | 4 | 5 | 6;
  cucName: string;
  menhChiIndex: number;
  thanChiIndex: number;
  thanPalaceName: string;
  chuMenh: string;
  chuThan: string;
  tuanPair: [number, number];
  trietPair: [number, number];
  tuHoaSummary: Array<{ hoa: "Hóa Lộc" | "Hóa Quyền" | "Hóa Khoa" | "Hóa Kỵ"; star: string; palace: string }>;
  palaces: TuViPalace[]; // 12 cung theo thứ tự chiIndex 0..11
  overallTitle: string;
  overallSummary: string;
  strengths: string[];
  cautions: string[];
}

export interface TuViDeepAspect {
  id: string;
  title: string;
  badge: string;
  icon: string;
  summary: string;
  details: string[];
  advice: string;
}

export interface TuViDeepAnalysis {
  headline: string;
  executiveSummary: string;
  aspects: TuViDeepAspect[];
}

const PALACE_NAMES = [
  "Mệnh",
  "Phụ Mẫu",
  "Phúc Đức",
  "Điền Trạch",
  "Quan Lộc",
  "Nô Bộc",
  "Thiên Di",
  "Tật Ách",
  "Tài Bạch",
  "Tử Tức",
  "Phu Thê",
  "Huynh Đệ"
] as const;

const TRANG_SINH_12 = [
  "Tràng Sinh",
  "Mộc Dục",
  "Quan Đới",
  "Lâm Quan",
  "Đế Vượng",
  "Suy",
  "Bệnh",
  "Tử",
  "Mộ",
  "Tuyệt",
  "Thai",
  "Dưỡng"
] as const;

const mod12 = (n: number): number => ((n % 12) + 12) % 12;

// Ngũ hành của 14 Chính Tinh
const CHINH_TINH_ELEMENT: Record<string, FiveElement> = {
  "Tử Vi": FiveElement.THO,
  "Thiên Cơ": FiveElement.MOC,
  "Thái Dương": FiveElement.HOA,
  "Vũ Khúc": FiveElement.KIM,
  "Thiên Đồng": FiveElement.THUY,
  "Liêm Trinh": FiveElement.HOA,
  "Thiên Phủ": FiveElement.THO,
  "Thái Âm": FiveElement.THUY,
  "Tham Lang": FiveElement.THUY,
  "Cự Môn": FiveElement.THUY,
  "Thiên Tướng": FiveElement.THUY,
  "Thiên Lương": FiveElement.MOC,
  "Thất Sát": FiveElement.KIM,
  "Phá Quân": FiveElement.THUY
};

// Bảng Miếu/Vượng/Đắc/Bình/Hãm chuẩn cho 14 Chính Tinh tại 12 địa chi (0=Tý .. 11=Hợi)
const BRIGHTNESS_TABLE: Record<string, StarBrightness[]> = {
  "Tử Vi":      ["B", "Đ", "M", "B", "V", "M", "M", "Đ", "M", "B", "V", "B"],
  "Thiên Cơ":   ["M", "H", "Đ", "M", "V", "B", "M", "H", "Đ", "M", "V", "B"],
  "Thái Dương": ["H", "H", "V", "M", "V", "V", "M", "Đ", "B", "H", "H", "H"],
  "Vũ Khúc":    ["V", "M", "Đ", "H", "M", "B", "V", "M", "Đ", "H", "M", "B"],
  "Thiên Đồng": ["V", "H", "Đ", "M", "B", "M", "H", "H", "V", "B", "B", "M"],
  "Liêm Trinh": ["V", "Đ", "M", "H", "V", "H", "V", "Đ", "M", "H", "V", "H"],
  "Thiên Phủ":  ["M", "M", "M", "B", "M", "Đ", "V", "M", "M", "B", "M", "Đ"],
  "Thái Âm":    ["M", "M", "H", "H", "H", "H", "H", "B", "Đ", "V", "M", "M"],
  "Tham Lang":  ["V", "M", "Đ", "H", "M", "H", "V", "M", "Đ", "H", "M", "H"],
  "Cự Môn":     ["V", "H", "M", "M", "H", "H", "V", "H", "M", "M", "H", "H"],
  "Thiên Tướng":["V", "Đ", "M", "H", "V", "Đ", "V", "Đ", "M", "H", "Đ", "Đ"],
  "Thiên Lương":["V", "Đ", "M", "M", "V", "H", "M", "Đ", "B", "H", "V", "H"],
  "Thất Sát":   ["M", "Đ", "M", "H", "B", "V", "M", "Đ", "M", "H", "B", "V"],
  "Phá Quân":   ["M", "V", "H", "H", "Đ", "H", "M", "V", "H", "H", "Đ", "H"]
};

// Chủ Mệnh theo Địa Chi năm sinh (0..11)
const CHU_MENH_BY_CHI = [
  "Tham Lang", "Cự Môn", "Lộc Tồn", "Văn Khúc", "Liêm Trinh", "Vũ Khúc",
  "Phá Quân", "Vũ Khúc", "Liêm Trinh", "Văn Khúc", "Lộc Tồn", "Cự Môn"
];

// Chủ Thân theo Địa Chi năm sinh (0..11)
const CHU_THAN_BY_CHI = [
  "Linh Tinh", "Thiên Tướng", "Thiên Lương", "Thiên Đồng", "Văn Xương", "Thiên Cơ",
  "Hỏa Tinh", "Thiên Tướng", "Thiên Lương", "Thiên Đồng", "Văn Xương", "Thiên Cơ"
];

// Tứ Hóa theo Thiên Can năm sinh (0..9 -> Lộc, Quyền, Khoa, Kỵ)
const TU_HOA_BY_CAN: Array<[string, string, string, string]> = [
  ["Liêm Trinh", "Phá Quân", "Vũ Khúc", "Thái Dương"],   // Giáp
  ["Thiên Cơ", "Thiên Lương", "Tử Vi", "Thái Âm"],       // Ất
  ["Thiên Đồng", "Thiên Cơ", "Văn Xương", "Liêm Trinh"], // Bính
  ["Thái Âm", "Thiên Đồng", "Thiên Cơ", "Cự Môn"],       // Đinh
  ["Tham Lang", "Thái Âm", "Hữu Bật", "Thiên Cơ"],       // Mậu
  ["Vũ Khúc", "Tham Lang", "Thiên Lương", "Văn Khúc"],   // Kỷ
  ["Thái Dương", "Vũ Khúc", "Thái Âm", "Thiên Đồng"],    // Canh
  ["Cự Môn", "Thái Dương", "Văn Khúc", "Văn Xương"],     // Tân
  ["Thiên Lương", "Tử Vi", "Tả Phụ", "Vũ Khúc"],         // Nhâm
  ["Phá Quân", "Cự Môn", "Thái Âm", "Tham Lang"]         // Quý
];

// Lộc Tồn theo Thiên Can năm sinh
const LOC_TON_BY_CAN = [2, 3, 5, 6, 5, 6, 8, 9, 11, 0];

// Thiên Khôi & Thiên Việt theo Thiên Can năm sinh
const KHOI_VIET_BY_CAN: Array<[number, number]> = [
  [1, 7],   // Giáp
  [0, 8],   // Ất
  [11, 9],  // Bính
  [11, 9],  // Đinh
  [1, 7],   // Mậu
  [0, 8],   // Kỷ
  [1, 7],   // Canh
  [6, 2],   // Tân
  [3, 5],   // Nhâm
  [3, 5]    // Quý
];

// Triệt theo Thiên Can năm sinh
const TRIET_BY_CAN: Array<[number, number]> = [
  [8, 9], [6, 7], [4, 5], [2, 3], [0, 1],
  [8, 9], [6, 7], [4, 5], [2, 3], [0, 1]
];

function getCucInfo(menhElement: FiveElement): { num: 2 | 3 | 4 | 5 | 6; name: string; trangSinhStart: number } {
  switch (menhElement) {
    case FiveElement.THUY:
      return { num: 2, name: "Thủy Nhị Cục", trangSinhStart: 8 }; // Thân
    case FiveElement.MOC:
      return { num: 3, name: "Mộc Tam Cục", trangSinhStart: 11 }; // Hợi
    case FiveElement.KIM:
      return { num: 4, name: "Kim Tứ Cục", trangSinhStart: 5 }; // Tỵ
    case FiveElement.THO:
      return { num: 5, name: "Thổ Ngũ Cục", trangSinhStart: 8 }; // Thân
    case FiveElement.HOA:
      return { num: 6, name: "Hỏa Lục Cục", trangSinhStart: 2 }; // Dần
  }
}

function getTuViPosition(lunarDay: number, cuc: number): number {
  let x = 0;
  while ((lunarDay + x) % cuc !== 0) {
    x++;
  }
  const q = (lunarDay + x) / cuc;
  const base = mod12(2 + (q - 1)); // Khởi Dần (2)
  return x % 2 === 0 ? mod12(base + x) : mod12(base - x);
}

const PALACE_MEANINGS: Record<string, string> = {
  "Mệnh": "Thể hiện tính cách cốt lõi, khí chất, tài năng bẩm sinh và xu hướng vận mệnh cả đời.",
  "Phụ Mẫu": "Phản ánh nhân duyên, sự nâng đỡ từ cha mẹ và người lớn tuổi trong gia đình.",
  "Phúc Đức": "Phản ánh phúc phần tổ tiên, đời sống tinh thần, sự an yên nội tâm và hậu vận.",
  "Điền Trạch": "Thể hiện duyên với đất đai, nhà cửa, nơi an cư lạc nghiệp và tích lũy bất động sản.",
  "Quan Lộc": "Đại diện cho công danh, sự nghiệp, năng lực chuyên môn và con đường thăng tiến.",
  "Nô Bộc": "Phản ánh mối quan hệ bạn bè, đồng nghiệp, cộng sự và quý nhân xung quanh.",
  "Thiên Di": "Thể hiện vận xuất hành, cơ hội khi đi xa, khả năng thích nghi với môi trường xã hội.",
  "Tật Ách": "Lưu ý về thể chất, điểm cần giữ gìn sức khỏe và cách phòng tránh tai ương.",
  "Tài Bạch": "Đại diện cho năng lực kiếm tiền, quản lý tài chính và dòng chảy tài lộc.",
  "Tử Tức": "Phản ánh đường con cái, nhân duyên nuôi dạy con và niềm vui từ thế hệ sau.",
  "Phu Thê": "Đại diện cho tình duyên, hôn nhân, mẫu người bạn đời và sự hòa hợp gia đạo.",
  "Huynh Đệ": "Thể hiện tình cảm anh chị em ruột thịt và người thân thiết ngang hàng."
};

const CHINH_TINH_SUMMARY: Record<string, string> = {
  "Tử Vi": "Đế tinh chủ quyền uy, bản lĩnh lãnh đạo và quý nhân phù trợ",
  "Thiên Cơ": "Mưu lược, thông minh, nhạy bén và giỏi xoay chuyển tình thế",
  "Thái Dương": "Quang minh lỗi lạc, nhiệt huyết, hợp danh tiếng và công danh",
  "Vũ Khúc": "Tài tinh thực tế, quyết đoán, mạnh về quản lý tài chính và hành động",
  "Thiên Đồng": "Phúc tinh ôn hòa, giàu cảm xúc, thích sự bình yên và sáng tạo",
  "Liêm Trinh": "Chính trực, cá tính mạnh, sắc sảo và có sức hút riêng",
  "Thiên Phủ": "Kho tàng vững chãi, điềm đạm, giỏi giữ gìn tài sản và ổn định",
  "Thái Âm": "Phú tinh tinh tế, sâu sắc, vượng về điền sản và tài lộc bền bỉ",
  "Tham Lang": "Đa tài, giao tiếp khéo léo, giàu tham vọng và sức sống",
  "Cự Môn": "Tư duy phản biện sắc bén, hợp nghề nói, giảng dạy và phân tích",
  "Thiên Tướng": "Ấn tinh nghĩa khí, ngay thẳng, giỏi điều phối và trợ lực",
  "Thiên Lương": "Ấm tinh hiền hậu, có tâm hướng thiện, hay gặp may mắn hóa giải",
  "Thất Sát": "Tướng tinh quả cảm, độc lập, bứt phá mạnh trong thử thách",
  "Phá Quân": "Tiên phong đổi mới, dám nghĩ dám làm, thích mở lối đi riêng"
};

function evaluatePalace(
  palaceName: string,
  chinhTinh: TuViStar[],
  catTinh: TuViStar[],
  satTinh: TuViStar[],
  tuan: boolean,
  triet: boolean
): { score: number; interpretation: string; keyHighlights: string[]; advice: string } {
  let score = 68;
  const highlights: string[] = [];

  if (chinhTinh.length === 0) {
    highlights.push("Vô Chính Diệu: chịu ảnh hưởng từ cung đối chiếu (Tam Phương Tứ Chính), tính linh hoạt cao.");
    if (tuan || triet) {
      score += 6;
      highlights.push("Có Tuần/Triệt đóng: giúp Vô Chính Diệu ổn định và bớt dao động hơn.");
    } else {
      score -= 4;
    }
  } else {
    chinhTinh.forEach((s) => {
      if (s.brightness === "M") score += 9;
      else if (s.brightness === "V") score += 7;
      else if (s.brightness === "Đ") score += 4;
      else if (s.brightness === "H") score -= 6;
      if (s.tuHoaTag === "Hóa Lộc" || s.tuHoaTag === "Hóa Quyền" || s.tuHoaTag === "Hóa Khoa") {
        score += 7;
      } else if (s.tuHoaTag === "Hóa Kỵ") {
        score -= 7;
      }
      const desc = CHINH_TINH_SUMMARY[s.name] || "";
      highlights.push(`${s.name} (${s.brightness || "B"}${s.tuHoaTag ? ` • ${s.tuHoaTag}` : ""}): ${desc}.`);
    });
  }

  catTinh.forEach((c) => {
    score += 3;
    if (c.tuHoaTag) score += 4;
  });

  satTinh.forEach((st) => {
    score -= 4;
  });

  if (catTinh.length > 0) {
    highlights.push(`Cát tinh hội tụ: ${catTinh.map((c) => c.name + (c.tuHoaTag ? ` (${c.tuHoaTag})` : "")).join(", ")}.`);
  }
  if (satTinh.length > 0) {
    highlights.push(`Sát/Bại tinh cần lưu ý: ${satTinh.map((s) => s.name).join(", ")}.`);
  }
  if (tuan || triet) {
    highlights.push(
      `Gặp ${[tuan ? "Tuần Không" : "", triet ? "Triệt Lộ" : ""].filter(Boolean).join(" & ")}: giai đoạn đầu thường phải tự lực hoặc chậm mà chắc, về sau càng vững.`
    );
  }

  score = Math.max(38, Math.min(98, score));
  const baseMeaning = PALACE_MEANINGS[palaceName] || "";
  const chinhNames = chinhTinh.map((s) => s.name).join(" - ");
  const interpretation = chinhTinh.length
    ? `${baseMeaning} Cung ${palaceName} có ${chinhNames} tọa thủ (${score >= 78 ? "cách cục sáng sủa, nhiều trợ lực" : score >= 62 ? "cách cục cân bằng, ổn định" : "có thử thách rèn giũa bản lĩnh"}).`
    : `${baseMeaning} Cung ${palaceName} ở thế Vô Chính Diệu nên cần nhìn thêm cung xung chiếu và tam hợp để luận đủ độ sáng.`;

  const advice =
    satTinh.length >= 2
      ? `Cung ${palaceName} có sát tinh chiếu, nên giữ tâm thế điềm tĩnh, tránh nóng vội và chú trọng chữ Tín.`
      : score >= 78
      ? `Cung ${palaceName} vượng cát khí — hãy chủ động nắm bắt cơ hội và phát huy thế mạnh sẵn có.`
      : `Cung ${palaceName} ở mức trung hòa — tích lũy đều đặn và giữ kỷ luật sẽ tạo kết quả bền vững.`;

  return { score, interpretation, keyHighlights: highlights, advice };
}

export function generateTuViChart(input: TuViPersonInput): TuViChartResult {
  const safeHour = input.birthHour >= 0 && input.birthHour <= 11 ? input.birthHour : 6;
  const profile: UserBirthProfile = {
    day: input.day,
    month: input.month,
    year: input.year,
    gender: input.gender,
    birthHour: safeHour,
    isLunar: input.isLunar
  };
  const fortune = getDetailedBirthFortune(profile);
  const { solarDate, lunarDate, personalProfile } = fortune;

  const yearCanIndex = personalProfile.canIndex;
  const yearChiIndex = personalProfile.chiIndex;
  const isYangYear = yearCanIndex % 2 === 0;
  const isMale = input.gender === "male";
  const amDuongLabel = `${isYangYear ? "Dương" : "Âm"} ${isMale ? "Nam" : "Nữ"}`;
  const isThuan = (isYangYear && isMale) || (!isYangYear && !isMale);

  // 1. Cung Mệnh & Cung Thân
  const monthPos = mod12(2 + (lunarDate.month - 1));
  const menhChiIndex = mod12(monthPos - safeHour);
  const thanChiIndex = mod12(monthPos + safeHour);

  // 2. Can của 12 cung theo Ngũ Hổ Độn
  const danCan = ((yearCanIndex % 5) * 2 + 2) % 10;
  const palaceCans = Array.from({ length: 12 }, (_, chi) => (danCan + mod12(chi - 2)) % 10);

  // 3. Cục theo Nạp Âm Cung Mệnh
  const menhPalaceNapAm = napAm(palaceCans[menhChiIndex], menhChiIndex);
  const cucElement = elementOfNapAm(menhPalaceNapAm);
  const cucInfo = getCucInfo(cucElement);

  // 4. An 14 Chính Tinh
  const tuViPos = getTuViPosition(lunarDate.day, cucInfo.num);
  const thienPhuPos = mod12(4 - tuViPos);

  const chinhMap: Record<number, TuViStar[]> = Object.fromEntries(Array.from({ length: 12 }, (_, i) => [i, []]));
  const catMap: Record<number, TuViStar[]> = Object.fromEntries(Array.from({ length: 12 }, (_, i) => [i, []]));
  const satMap: Record<number, TuViStar[]> = Object.fromEntries(Array.from({ length: 12 }, (_, i) => [i, []]));

  const tuHoaStars = TU_HOA_BY_CAN[yearCanIndex];
  const tuHoaLabels: Array<"Hóa Lộc" | "Hóa Quyền" | "Hóa Khoa" | "Hóa Kỵ"> = [
    "Hóa Lộc",
    "Hóa Quyền",
    "Hóa Khoa",
    "Hóa Kỵ"
  ];
  const findTuHoa = (starName: string) => {
    const idx = tuHoaStars.indexOf(starName);
    return idx >= 0 ? tuHoaLabels[idx] : undefined;
  };

  const placeChinh = (name: string, pos: number) => {
    const tuHoaTag = findTuHoa(name);
    chinhMap[pos].push({
      name,
      category: "chinh",
      element: CHINH_TINH_ELEMENT[name] || FiveElement.THO,
      brightness: BRIGHTNESS_TABLE[name]?.[pos] || "B",
      tuHoaTag
    });
  };

  placeChinh("Tử Vi", tuViPos);
  placeChinh("Thiên Cơ", mod12(tuViPos - 1));
  placeChinh("Thái Dương", mod12(tuViPos - 3));
  placeChinh("Vũ Khúc", mod12(tuViPos - 4));
  placeChinh("Thiên Đồng", mod12(tuViPos - 5));
  placeChinh("Liêm Trinh", mod12(tuViPos - 8));

  placeChinh("Thiên Phủ", thienPhuPos);
  placeChinh("Thái Âm", mod12(thienPhuPos + 1));
  placeChinh("Tham Lang", mod12(thienPhuPos + 2));
  placeChinh("Cự Môn", mod12(thienPhuPos + 3));
  placeChinh("Thiên Tướng", mod12(thienPhuPos + 4));
  placeChinh("Thiên Lương", mod12(thienPhuPos + 5));
  placeChinh("Thất Sát", mod12(thienPhuPos + 6));
  placeChinh("Phá Quân", mod12(thienPhuPos + 10));

  const placeCat = (name: string, pos: number, element: FiveElement) => {
    catMap[pos].push({ name, category: "cat", element, tuHoaTag: findTuHoa(name) });
  };
  const placeSat = (name: string, pos: number, element: FiveElement) => {
    satMap[pos].push({ name, category: "sat", element, tuHoaTag: findTuHoa(name) });
  };

  // 5. Phụ tinh cốt lõi
  const locTonPos = LOC_TON_BY_CAN[yearCanIndex];
  placeCat("Lộc Tồn", locTonPos, FiveElement.THO);
  placeSat("Kình Dương", mod12(locTonPos + 1), FiveElement.KIM);
  placeSat("Đà La", mod12(locTonPos - 1), FiveElement.KIM);

  placeCat("Tả Phụ", mod12(4 + lunarDate.month - 1), FiveElement.THO);
  placeCat("Hữu Bật", mod12(10 - (lunarDate.month - 1)), FiveElement.THUY);

  placeCat("Văn Xương", mod12(10 - safeHour), FiveElement.KIM);
  placeCat("Văn Khúc", mod12(4 + safeHour), FiveElement.THUY);

  const [khoiPos, vietPos] = KHOI_VIET_BY_CAN[yearCanIndex];
  placeCat("Thiên Khôi", khoiPos, FiveElement.HOA);
  placeCat("Thiên Việt", vietPos, FiveElement.HOA);

  placeSat("Địa Kiếp", mod12(11 + safeHour), FiveElement.HOA);
  placeSat("Địa Không", mod12(11 - safeHour), FiveElement.HOA);

  // Hỏa Tinh - Linh Tinh
  let hoaBase = 1;
  let linhBase = 3;
  if ([8, 0, 4].includes(yearChiIndex)) {
    hoaBase = 2; linhBase = 10;
  } else if ([5, 9, 1].includes(yearChiIndex)) {
    hoaBase = 3; linhBase = 10;
  } else if ([11, 3, 7].includes(yearChiIndex)) {
    hoaBase = 9; linhBase = 10;
  }
  placeSat("Hỏa Tinh", mod12(hoaBase + safeHour), FiveElement.HOA);
  placeSat("Linh Tinh", mod12(linhBase + safeHour), FiveElement.HOA);

  // Đào Hoa, Hồng Loan, Thiên Hỷ, Thiên Mã
  const daoHoaPos = [2, 6, 10].includes(yearChiIndex)
    ? 3
    : [8, 0, 4].includes(yearChiIndex)
    ? 9
    : [5, 9, 1].includes(yearChiIndex)
    ? 6
    : 0;
  const hongLoanPos = mod12(3 - yearChiIndex);
  const thienHyPos = mod12(hongLoanPos + 6);
  const thienMaPos = [2, 6, 10].includes(yearChiIndex)
    ? 8
    : [8, 0, 4].includes(yearChiIndex)
    ? 2
    : [5, 9, 1].includes(yearChiIndex)
    ? 11
    : 5;

  placeCat("Đào Hoa", daoHoaPos, FiveElement.MOC);
  placeCat("Hồng Loan", hongLoanPos, FiveElement.THUY);
  placeCat("Thiên Hỷ", thienHyPos, FiveElement.THUY);
  placeCat("Thiên Mã", thienMaPos, FiveElement.HOA);

  // 6. Tuần & Triệt
  const trietPair = TRIET_BY_CAN[yearCanIndex];
  const tuanStart = mod12(yearChiIndex - yearCanIndex - 2);
  const tuanPair: [number, number] = [tuanStart, mod12(tuanStart + 1)];

  // 7. Vòng Tràng Sinh & Tên 12 Cung & Đại Vận
  const palaceNamesByChi: string[] = Array(12).fill("");
  const daiVanByChi: number[] = Array(12).fill(0);
  const trangSinhByChi: string[] = Array(12).fill("");

  for (let offset = 0; offset < 12; offset++) {
    const chi = mod12(menhChiIndex + offset);
    palaceNamesByChi[chi] = PALACE_NAMES[offset];

    const dvChi = isThuan ? mod12(menhChiIndex + offset) : mod12(menhChiIndex - offset);
    daiVanByChi[dvChi] = cucInfo.num + offset * 10;

    const tsChi = isThuan ? mod12(cucInfo.trangSinhStart + offset) : mod12(cucInfo.trangSinhStart - offset);
    trangSinhByChi[tsChi] = TRANG_SINH_12[offset];
  }

  // 8. Tổng hợp danh sách 12 cung
  const palaces: TuViPalace[] = Array.from({ length: 12 }, (_, chiIndex) => {
    const palaceName = palaceNamesByChi[chiIndex];
    const isTuan = tuanPair[0] === chiIndex || tuanPair[1] === chiIndex;
    const isTriet = trietPair[0] === chiIndex || trietPair[1] === chiIndex;
    const chinhTinh = chinhMap[chiIndex];
    const catTinh = catMap[chiIndex];
    const satTinh = satMap[chiIndex];
    const evalInfo = evaluatePalace(palaceName, chinhTinh, catTinh, satTinh, isTuan, isTriet);
    const dvStart = daiVanByChi[chiIndex];

    return {
      chiIndex,
      chiName: CHI[chiIndex],
      palaceCan: CAN[palaceCans[chiIndex]],
      palaceName,
      isMenh: chiIndex === menhChiIndex,
      isThan: chiIndex === thanChiIndex,
      daiVanStart: dvStart,
      daiVanRange: `${dvStart} - ${dvStart + 9}`,
      tuan: isTuan,
      triet: isTriet,
      trangSinh: trangSinhByChi[chiIndex],
      chinhTinh,
      catTinh,
      satTinh,
      score: evalInfo.score,
      interpretation: evalInfo.interpretation,
      keyHighlights: evalInfo.keyHighlights,
      advice: evalInfo.advice
    };
  });

  // Map Tứ Hóa về đúng tên cung
  const tuHoaSummary = tuHoaStars.map((star, idx) => {
    const foundPalace = palaces.find(
      (p) => p.chinhTinh.some((s) => s.name === star) || p.catTinh.some((s) => s.name === star)
    );
    return {
      hoa: tuHoaLabels[idx],
      star,
      palace: foundPalace ? `${foundPalace.palaceName} (${foundPalace.chiName})` : "—"
    };
  });

  const menhPalace = palaces[menhChiIndex];
  const quanPalace = palaces.find((p) => p.palaceName === "Quan Lộc")!;
  const taiPalace = palaces.find((p) => p.palaceName === "Tài Bạch")!;
  const phuThePalace = palaces.find((p) => p.palaceName === "Phu Thê")!;

  const menhChinhNames = menhPalace.chinhTinh.map((s) => s.name).join(" - ") || "Vô Chính Diệu";
  const displayName = input.name.trim() || (isMale ? "Đương số Nam" : "Đương số Nữ");

  const strengths: string[] = [
    `Bản mệnh ${personalProfile.napAm} (${elementLabel(personalProfile.element)}), ${cucInfo.name} — ${amDuongLabel} đi ${isThuan ? "thuận lý" : "nghịch lý"}.`,
    `Cung Mệnh an tại ${menhPalace.chiName} (${menhChinhNames}), đạt độ cát lợi ${menhPalace.score}/100.`,
    `Tam hợp Mệnh - Tài Bạch (${taiPalace.chiName}) - Quan Lộc (${quanPalace.chiName}) tạo trục phát triển công danh & tài chính.`
  ];

  const cautions: string[] = [
    `Lưu ý hai cung gặp Tuần (${CHI[tuanPair[0]]}-${CHI[tuanPair[1]]}) và Triệt (${CHI[trietPair[0]]}-${CHI[trietPair[1]]}): cần kiên trì ở giai đoạn khởi đầu.`,
    `Hóa Kỵ đóng tại cung ${tuHoaSummary[3]?.palace || "—"} (${tuHoaSummary[3]?.star}): đây là nơi dễ phát sinh trăn trở hoặc thử thách cần khéo léo tiết chế.`
  ];

  return {
    displayName,
    gender: input.gender,
    amDuongLabel,
    isThuan,
    solarDate: { day: solarDate.day, month: solarDate.month, year: solarDate.year },
    lunarDate: { day: lunarDate.day, month: lunarDate.month, year: lunarDate.year, isLeapMonth: lunarDate.isLeapMonth },
    hourChiIndex: safeHour,
    hourLabel: fortune.birthHourFortune.hourChi,
    yearCanChi: personalProfile.canChi,
    napAmMenh: personalProfile.napAm,
    menhElement: personalProfile.element,
    cucNumber: cucInfo.num,
    cucName: cucInfo.name,
    menhChiIndex,
    thanChiIndex,
    thanPalaceName: palaces[thanChiIndex].palaceName,
    chuMenh: CHU_MENH_BY_CHI[yearChiIndex],
    chuThan: CHU_THAN_BY_CHI[yearChiIndex],
    tuanPair,
    trietPair,
    tuHoaSummary,
    palaces,
    overallTitle: `Lá Số Tử Vi: ${displayName} (${personalProfile.canChi}) • Mệnh ${menhChinhNames}`,
    overallSummary: `${displayName} sinh ngày ${solarDate.day}/${solarDate.month}/${solarDate.year} Dương lịch (tức ${lunarDate.day}/${lunarDate.month}/${lunarDate.year} Âm lịch, giờ ${fortune.birthHourFortune.hourChi}). Mệnh ${personalProfile.napAm}, ${cucInfo.name}, Thân cư ${palaces[thanChiIndex].palaceName}. Cung Phu Thê tại ${phuThePalace.chiName} (${phuThePalace.chinhTinh.map((s) => s.name).join(", ") || "Vô Chính Diệu"}).`,
    strengths,
    cautions
  };
}

export function buildBuiltinTuViDeepAnalysis(chart: TuViChartResult): TuViDeepAnalysis {
  const menh = chart.palaces[chart.menhChiIndex];
  const than = chart.palaces[chart.thanChiIndex];
  const quan = chart.palaces.find((p) => p.palaceName === "Quan Lộc")!;
  const tai = chart.palaces.find((p) => p.palaceName === "Tài Bạch")!;
  const phuThe = chart.palaces.find((p) => p.palaceName === "Phu Thê")!;
  const phucDuc = chart.palaces.find((p) => p.palaceName === "Phúc Đức")!;

  const currentAge = Math.max(1, 2026 - chart.lunarDate.year + 1);
  const activeDaiVan =
    chart.palaces.find((p) => currentAge >= p.daiVanStart && currentAge <= p.daiVanStart + 9) || menh;

  const formatStars = (p: TuViPalace) =>
    p.chinhTinh.map((s) => `${s.name} (${s.brightness || "B"}${s.tuHoaTag ? `, ${s.tuHoaTag}` : ""})`).join(", ") ||
    "Vô Chính Diệu";

  return {
    headline: `Luận Giải Tổng Hợp Lá Số ${chart.displayName} (${chart.yearCanChi})`,
    executiveSummary: chart.overallSummary,
    aspects: [
      {
        id: "menh_than",
        title: "1. Cục Diện Mệnh - Thân & Khí Chất Cốt Lõi",
        badge: `${chart.cucName} • ${menh.score}/100`,
        icon: "👑",
        summary: `Cung Mệnh tại ${menh.chiName} có ${formatStars(menh)}; Thân cư ${than.palaceName} (${than.chiName}) cho thấy hậu vận gắn liền với lĩnh vực ${than.palaceName}.`,
        details: [...menh.keyHighlights, `Chủ Mệnh: ${chart.chuMenh} • Chủ Thân: ${chart.chuThan}.`],
        advice: menh.advice
      },
      {
        id: "quan_loc",
        title: "2. Công Danh, Sự Nghiệp (Cung Quan Lộc)",
        badge: `Quan Lộc (${quan.chiName}) • ${quan.score}/100`,
        icon: "💼",
        summary: `Cung Quan Lộc tại ${quan.chiName} có ${formatStars(quan)}. ${quan.interpretation}`,
        details: quan.keyHighlights,
        advice: quan.advice
      },
      {
        id: "tai_bach",
        title: "3. Tiền Tài & Quản Lý Tài Chính (Cung Tài Bạch)",
        badge: `Tài Bạch (${tai.chiName}) • ${tai.score}/100`,
        icon: "🪙",
        summary: `Cung Tài Bạch tại ${tai.chiName} có ${formatStars(tai)}. ${tai.interpretation}`,
        details: tai.keyHighlights,
        advice: tai.advice
      },
      {
        id: "phu_the",
        title: "4. Tình Duyên & Hôn Nhân (Cung Phu Thê)",
        badge: `Phu Thê (${phuThe.chiName}) • ${phuThe.score}/100`,
        icon: "💕",
        summary: `Cung Phu Thê tại ${phuThe.chiName} có ${formatStars(phuThe)}. ${phuThe.interpretation}`,
        details: phuThe.keyHighlights,
        advice: phuThe.advice
      },
      {
        id: "phuc_duc",
        title: "5. Phúc Đức, Gia Đạo & Nền Tảng Tinh Thần",
        badge: `Phúc Đức (${phucDuc.chiName}) • ${phucDuc.score}/100`,
        icon: "🪷",
        summary: `Cung Phúc Đức tại ${phucDuc.chiName} là gốc rễ nâng đỡ toàn bộ lá số, có ${formatStars(phucDuc)}.`,
        details: phucDuc.keyHighlights,
        advice: phucDuc.advice
      },
      {
        id: "dai_van",
        title: `6. Đại Vận Hiện Tại (${activeDaiVan.daiVanRange} tuổi) & Tứ Hóa`,
        badge: `Đại vận ${activeDaiVan.palaceName}`,
        icon: "⏳",
        summary: `Ở tuổi ${currentAge} âm lịch (năm 2026), đương số đang đi qua Đại vận ${activeDaiVan.daiVanRange} tuổi tại cung ${activeDaiVan.palaceName} (${activeDaiVan.chiName}) có ${formatStars(activeDaiVan)}.`,
        details: [
          ...chart.tuHoaSummary.map((th) => `${th.hoa} nhập cung ${th.palace} (sao ${th.star}).`),
          ...activeDaiVan.keyHighlights.slice(0, 2)
        ],
        advice: activeDaiVan.advice
      }
    ]
  };
}
