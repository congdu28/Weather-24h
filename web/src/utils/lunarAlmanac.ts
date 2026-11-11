import {
  CAN,
  CHI,
  HOUR_RANGES,
  LunarDate,
  auspiciousHourIndices,
  jdFromDate,
  getLunarDate,
  convertLunarToSolar
} from "./lunarCalendar";

export enum AlmanacActivity {
  WEDDING = "WEDDING",
  OPENING = "OPENING",
  CONSTRUCTION = "CONSTRUCTION",
  MOVING = "MOVING",
  TRAVEL = "TRAVEL",
  CONTRACT = "CONTRACT",
  WEALTH = "WEALTH",
  BUY = "BUY",
  STUDY = "STUDY",
  WORSHIP = "WORSHIP",
  MEDICAL = "MEDICAL",
  REPAIR = "REPAIR",
  BURIAL = "BURIAL"
}

export const ACTIVITY_META: Record<AlmanacActivity, { label: string; emoji: string }> = {
  [AlmanacActivity.WEDDING]: { label: "Cưới hỏi", emoji: "💍" },
  [AlmanacActivity.OPENING]: { label: "Khai trương", emoji: "🏪" },
  [AlmanacActivity.CONSTRUCTION]: { label: "Động thổ, xây dựng", emoji: "🏗️" },
  [AlmanacActivity.MOVING]: { label: "Nhập trạch, chuyển nhà", emoji: "🏠" },
  [AlmanacActivity.TRAVEL]: { label: "Xuất hành, đi xa", emoji: "🧳" },
  [AlmanacActivity.CONTRACT]: { label: "Ký kết, giao dịch", emoji: "🤝" },
  [AlmanacActivity.WEALTH]: { label: "Cầu tài, cầu lộc", emoji: "💰" },
  [AlmanacActivity.BUY]: { label: "Mua xe, tài sản lớn", emoji: "🚗" },
  [AlmanacActivity.STUDY]: { label: "Nhập học, thi cử", emoji: "🎓" },
  [AlmanacActivity.WORSHIP]: { label: "Cúng lễ, cầu phúc", emoji: "🙏" },
  [AlmanacActivity.MEDICAL]: { label: "Khám chữa bệnh", emoji: "💊" },
  [AlmanacActivity.REPAIR]: { label: "Sửa nhà, tu tạo", emoji: "🔨" },
  [AlmanacActivity.BURIAL]: { label: "An táng, cải táng", emoji: "🕯️" }
};

export enum ActivityVerdict {
  GOOD = "GOOD",
  NEUTRAL = "NEUTRAL",
  BAD = "BAD"
}

export enum DayLevel {
  EXCELLENT = "EXCELLENT",
  GOOD = "GOOD",
  NORMAL = "NORMAL",
  BAD = "BAD",
  VERY_BAD = "VERY_BAD"
}

export const DAY_LEVEL_INFO: Record<DayLevel, { label: string; stars: number; color: string }> = {
  [DayLevel.EXCELLENT]: { label: "Rất tốt", stars: 5, color: "text-emerald-400" },
  [DayLevel.GOOD]: { label: "Tốt", stars: 4, color: "text-green-400" },
  [DayLevel.NORMAL]: { label: "Bình thường", stars: 3, color: "text-yellow-400" },
  [DayLevel.BAD]: { label: "Xấu", stars: 2, color: "text-orange-400" },
  [DayLevel.VERY_BAD]: { label: "Rất xấu", stars: 1, color: "text-red-400" }
};

export enum FiveElement {
  KIM = "KIM",
  THUY = "THUY",
  MOC = "MOC",
  HOA = "HOA",
  THO = "THO"
}

export const FIVE_ELEMENT_META: Record<FiveElement, { label: string; emoji: string }> = {
  [FiveElement.KIM]: { label: "Kim", emoji: "⚪" },
  [FiveElement.THUY]: { label: "Thủy", emoji: "💧" },
  [FiveElement.MOC]: { label: "Mộc", emoji: "🌳" },
  [FiveElement.HOA]: { label: "Hỏa", emoji: "🔥" },
  [FiveElement.THO]: { label: "Thổ", emoji: "🟤" }
};

export interface TrucInfo {
  name: string;
  fullName: string;
  score: number;
  goodText: string;
  badText: string;
  good: AlmanacActivity[];
  bad: AlmanacActivity[];
}

export interface MansionInfo {
  name: string;
  fullName: string;
  isGood: boolean;
  goodText: string;
  badText: string;
  good: AlmanacActivity[];
  bad: AlmanacActivity[];
}

export interface SpecialDay {
  name: string;
  isGood: boolean;
  description: string;
  affects: AlmanacActivity[];
  weight: number;
}

export interface LyThuanPhongInfo {
  name: string;
  isGood: boolean;
  description: string;
}

export interface HourInfo {
  chiIndex: number;
  chi: string;
  range: string;
  startHour: number;
  isHoangDao: boolean;
  lyThuanPhong: LyThuanPhongInfo;
  isBest: boolean;
}

export interface DayAlmanac {
  lunar: LunarDate;
  truc: TrucInfo;
  mansion: MansionInfo;
  napAm: string;
  napAmElement: FiveElement;
  goodStars: SpecialDay[];
  badDays: SpecialDay[];
  score: number;
  level: DayLevel;
  activityPoints: Record<AlmanacActivity, number>;
  hyThan: string;
  taiThan: string;
  hacThan: string | null;
  hours: HourInfo[];
  clashChi: number;
  clashAgesText: string;
  summary: string;
  goodActivities: AlmanacActivity[];
  badActivities: AlmanacActivity[];
}

export interface PersonalProfile {
  birthYear: number;
  canIndex: number;
  chiIndex: number;
  canChi: string;
  zodiacAnimal: string;
  napAm: string;
  element: FiveElement;
  isYang: boolean;
  yinYangLabel: string;
  canElement: FiveElement;
  chiElement: FiveElement;
  luckyColors: string;
  unluckyColors: string;
  luckyNumbers: number[];
  cungMenh: {
    menhNam: string;
    menhNu: string;
    nhomNam: "Đông Tứ Mệnh" | "Tây Tứ Mệnh";
    nhomNu: "Đông Tứ Mệnh" | "Tây Tứ Mệnh";
    huongTotNam: string[];
    huongTotNu: string[];
  };
  tamHop: string[];
  lucHop: string;
  tuHanhXung: string[];
}

export interface PersonalDayMatch {
  verdict: ActivityVerdict;
  points: number;
  scorePercent: number;
  badgeLabel: string;
  badgeColor: string;
  notes: string[];
  luckyAdvice: string;
  cautionAdvice: string;
}

export interface GoodDayResult {
  date: Date;
  lunar: LunarDate;
  almanac: DayAlmanac;
  score: number;
  level: DayLevel;
  activityScore: number;
  personalMatch?: PersonalDayMatch;
}

export const ZODIAC_ANIMALS = [
  "Chuột (Tý)", "Trâu (Sửu)", "Hổ (Dần)", "Mèo (Mão)",
  "Rồng (Thìn)", "Rắn (Tỵ)", "Ngựa (Ngọ)", "Dê (Mùi)",
  "Khỉ (Thân)", "Gà (Dậu)", "Chó (Tuất)", "Lợn (Hợi)"
];

export const ZODIAC_TRAITS: Record<number, { title: string; personality: string; career: string }> = {
  0: { title: "Túc Trí Đa Mưu", personality: "Nhanh nhẹn, thông minh, thích ứng cao, biết tích lũy và nhìn xa trông rộng.", career: "Kinh doanh, tài chính, quản lý, nghiên cứu chiến lược." },
  1: { title: "Cần Kiệm Trì Trọng", personality: "Chăm chỉ, kiên định, thực tế, luôn tạo dựng sự nghiệp bằng sự bền bỉ vững chắc.", career: "Bất động sản, xây dựng, kỹ thuật, sản xuất, nông nghiệp công nghệ." },
  2: { title: "Dũng Cảm Quyết Đoán", personality: "Uy phong, nhiệt huyết, dám nghĩ dám làm, mang tố chất của nhà tiên phong lãnh đạo.", career: "Quản trị doanh nghiệp, quân sự, ngoại giao, chính trị, khởi nghiệp." },
  3: { title: "Ôn Hòa Tinh Tế", personality: "Khéo léo, chu đáo, mắt thẩm mỹ cao, giỏi đối nhân xử thế và gìn giữ hòa khí.", career: "Nghệ thuật, thiết kế, truyền thông, sư phạm, tâm lý, dịch vụ cao cấp." },
  4: { title: "Hùng Tâm Tráng Khí", personality: "Khí phách, tham vọng, nhiều hoài bão lớn, luôn khao khát vươn tầm đỉnh cao.", career: "Lãnh đạo, chính khách, doanh nhân đầu tư, công nghệ đột phá." },
  5: { title: "Thâm Trầm Cơ Trí", personality: "Sâu sắc, nhạy bén, trực giác chuẩn xác, phong thái điềm tĩnh và khó bị đoán định.", career: "Nghiên cứu khoa học, điều tra, tư vấn pháp lý, triết học, đầu tư chứng khoán." },
  6: { title: "Khoáng Đạt Tự Do", personality: "Năng động, cởi mở, dồi dào năng lượng, thích xê dịch và theo đuổi đam mê.", career: "Du lịch, xuất nhập khẩu, truyền thông, thể thao, biểu diễn, quan hệ công chúng." },
  7: { title: "Nhân Hậu Lương Thiện", personality: "Thân thiện, giàu lòng trắc ẩn, yêu chuộng hòa bình, đời sống nội tâm phong phú.", career: "Y tế, từ thiện, giáo dục, văn học nghệ thuật, nhân sự." },
  8: { title: "Thông Tuệ Biến Hóa", personality: "Linh hoạt, hóm hỉnh, đa tài, giỏi xử lý tình huống khó và giao tiếp thuyết phục.", career: "Công nghệ thông tin, ngoại giao, marketing, diễn thuyết, giải trí." },
  9: { title: "Nguyên Tắc Chu Toàn", personality: "Chính xác, kỷ luật, yêu cầu cao về sự hoàn hảo, trung thực và có trách nhiệm lớn.", career: "Kiểm toán, tài chính, luật sư, quân đội, quản lý chất lượng." },
  10: { title: "Trung Nghĩa Trực Trực", personality: "Trung thành, khẳng khái, giàu tinh thần bảo vệ lẽ phải, đáng tin cậy tuyệt đối.", career: "An ninh, pháp lý, công tác xã hội, quản lý nhân sự, bảo hiểm." },
  11: { title: "Phúc Hậu An Nhiên", personality: "Hòa nhã, rộng rãi, hào phóng, có phúc lộc dồi dào và tính cách lạc quan yêu đời.", career: "Ẩm thực, khách sạn, kinh doanh bán lẻ, từ thiện, công nghệ giải trí." }
};

export function isClash(a: number, b: number): boolean {
  return (a - b + 12) % 12 === 6;
}

export function isHarm(a: number, b: number): boolean {
  return (a + b) % 12 === 7;
}

export function isSixHarmony(a: number, b: number): boolean {
  return (a + b) % 12 === 1;
}

export function isTripleHarmony(a: number, b: number): boolean {
  return a !== b && a % 4 === b % 4;
}

const PUNISH_GROUPS = [
  new Set([0, 3]),
  new Set([2, 5, 8]),
  new Set([1, 10, 7])
];
const SELF_PUNISH = new Set([4, 6, 9, 11]);

export function isPunish(a: number, b: number): boolean {
  if (a === b) return SELF_PUNISH.has(a);
  return PUNISH_GROUPS.some(set => set.has(a) && set.has(b));
}

const BREAK_PAIRS = new Set([
  "0,9", "9,0",
  "3,6", "6,3",
  "1,4", "4,1",
  "7,10", "10,7"
]);

export function isBreak(a: number, b: number): boolean {
  return BREAK_PAIRS.has(`${a},${b}`);
}

export function elementOfCan(canIdx: number): FiveElement {
  switch (canIdx) {
    case 0:
    case 1:
      return FiveElement.MOC;
    case 2:
    case 3:
      return FiveElement.HOA;
    case 4:
    case 5:
      return FiveElement.THO;
    case 6:
    case 7:
      return FiveElement.KIM;
    default:
      return FiveElement.THUY;
  }
}

export function elementOfChi(chiIdx: number): FiveElement {
  switch (chiIdx) {
    case 2:
    case 3:
      return FiveElement.MOC;
    case 5:
    case 6:
      return FiveElement.HOA;
    case 8:
    case 9:
      return FiveElement.KIM;
    case 11:
    case 0:
      return FiveElement.THUY;
    default:
      return FiveElement.THO;
  }
}

export function isCanHarmony(a: number, b: number): boolean {
  return (a + 5) % 10 === b || (b + 5) % 10 === a;
}

export function isCanClash(a: number, b: number): boolean {
  return a % 2 === b % 2 && (((a + 6) % 10 === b) || ((b + 6) % 10 === a)) && (a % 5 !== 2);
}

export function elementGenerates(source: FiveElement, target: FiveElement): boolean {
  switch (source) {
    case FiveElement.KIM: return target === FiveElement.THUY;
    case FiveElement.THUY: return target === FiveElement.MOC;
    case FiveElement.MOC: return target === FiveElement.HOA;
    case FiveElement.HOA: return target === FiveElement.THO;
    case FiveElement.THO: return target === FiveElement.KIM;
  }
}

export function elementOvercomes(source: FiveElement, target: FiveElement): boolean {
  switch (source) {
    case FiveElement.KIM: return target === FiveElement.MOC;
    case FiveElement.MOC: return target === FiveElement.THO;
    case FiveElement.THO: return target === FiveElement.THUY;
    case FiveElement.THUY: return target === FiveElement.HOA;
    case FiveElement.HOA: return target === FiveElement.KIM;
  }
}

const CUNG_NAMES = [
  "", "Khảm (Thủy)", "Khôn (Thổ)", "Chấn (Mộc)", "Tốn (Mộc)",
  "Trung Cung", "Càn (Kim)", "Đoài (Kim)", "Cấn (Thổ)", "Ly (Hỏa)"
];

export function calculateCungPhi(year: number): {
  menhNam: string;
  menhNu: string;
  nhomNam: "Đông Tứ Mệnh" | "Tây Tứ Mệnh";
  nhomNu: "Đông Tứ Mệnh" | "Tây Tứ Mệnh";
  huongTotNam: string[];
  huongTotNu: string[];
} {
  const last2 = year % 100;
  let sum = Math.floor(last2 / 10) + (last2 % 10);
  while (sum >= 10) {
    sum = Math.floor(sum / 10) + (sum % 10);
  }

  let cungNamIdx: number;
  let cungNuIdx: number;

  if (year < 2000) {
    cungNamIdx = (10 - sum) % 9 || 9;
    cungNuIdx = (sum + 5) % 9 || 9;
  } else {
    cungNamIdx = (9 - sum) % 9 || 9;
    cungNuIdx = (sum + 6) % 9 || 9;
  }

  if (cungNamIdx === 5) cungNamIdx = 2;
  if (cungNuIdx === 5) cungNuIdx = 8;

  const isDongTu = (cung: number) => [1, 9, 3, 4].includes(cung);
  const huongDongTu = ["Đông (Chính Đông)", "Đông Nam", "Nam (Chính Nam)", "Bắc (Chính Bắc)"];
  const huongTayTu = ["Tây (Chính Tây)", "Tây Bắc", "Tây Nam", "Đông Bắc"];

  return {
    menhNam: CUNG_NAMES[cungNamIdx] || "Càn (Kim)",
    menhNu: CUNG_NAMES[cungNuIdx] || "Ly (Hỏa)",
    nhomNam: isDongTu(cungNamIdx) ? "Đông Tứ Mệnh" : "Tây Tứ Mệnh",
    nhomNu: isDongTu(cungNuIdx) ? "Đông Tứ Mệnh" : "Tây Tứ Mệnh",
    huongTotNam: isDongTu(cungNamIdx) ? huongDongTu : huongTayTu,
    huongTotNu: isDongTu(cungNuIdx) ? huongDongTu : huongTayTu
  };
}

const TAM_HOP_MAP: Record<number, string[]> = {
  0: ["Thân", "Thìn"],
  1: ["Tỵ", "Dậu"],
  2: ["Ngọ", "Tuất"],
  3: ["Hợi", "Mùi"],
  4: ["Thân", "Tý"],
  5: ["Dậu", "Sửu"],
  6: ["Dần", "Tuất"],
  7: ["Hợi", "Mão"],
  8: ["Tý", "Thìn"],
  9: ["Tỵ", "Sửu"],
  10: ["Dần", "Ngọ"],
  11: ["Mão", "Mùi"]
};

const LUC_HOP_MAP: Record<number, string> = {
  0: "Sửu", 1: "Tý", 2: "Hợi", 3: "Tuất", 4: "Dậu", 5: "Thân",
  6: "Mùi", 7: "Ngọ", 8: "Tỵ", 9: "Thìn", 10: "Mão", 11: "Dần"
};

const TU_HANH_XUNG_MAP: Record<number, string[]> = {
  0: ["Ngọ", "Mão", "Dậu"],
  6: ["Tý", "Mão", "Dậu"],
  3: ["Dậu", "Tý", "Ngọ"],
  9: ["Mão", "Tý", "Ngọ"],

  2: ["Thân", "Tỵ", "Hợi"],
  8: ["Dần", "Tỵ", "Hợi"],
  5: ["Hợi", "Dần", "Thân"],
  11: ["Tỵ", "Dần", "Thân"],

  1: ["Mùi", "Thìn", "Tuất"],
  7: ["Sửu", "Thìn", "Tuất"],
  4: ["Tuất", "Sửu", "Mùi"],
  10: ["Thìn", "Sửu", "Mùi"]
};

export function getPersonalProfile(birthYear: number): PersonalProfile {
  const canIdx = ((birthYear + 6) % 10 + 10) % 10;
  const chiIdx = ((birthYear + 8) % 12 + 12) % 12;
  const nAm = napAm(canIdx, chiIdx);
  const elem = elementOfNapAm(nAm);
  const isYang = canIdx % 2 === 0;

  const luckyColorsMap: Record<FiveElement, string> = {
    [FiveElement.KIM]: "Trắng, xám, bạc, ánh kim (bản mệnh) • Vàng, nâu đất (Thổ sinh Kim)",
    [FiveElement.THUY]: "Đen, xanh nước biển (bản mệnh) • Trắng, xám, ánh kim (Kim sinh Thủy)",
    [FiveElement.MOC]: "Xanh lá cây (bản mệnh) • Đen, xanh nước biển (Thủy sinh Mộc)",
    [FiveElement.HOA]: "Đỏ, hồng, tím, cam (bản mệnh) • Xanh lá cây (Mộc sinh Hỏa)",
    [FiveElement.THO]: "Vàng, nâu đất (bản mệnh) • Đỏ, hồng, tím, cam (Hỏa sinh Thổ)"
  };

  const unluckyColorsMap: Record<FiveElement, string> = {
    [FiveElement.KIM]: "Đỏ, hồng, tím (Hỏa khắc Kim)",
    [FiveElement.THUY]: "Vàng, nâu đất (Thổ khắc Thủy)",
    [FiveElement.MOC]: "Trắng, xám, bạc (Kim khắc Mộc)",
    [FiveElement.HOA]: "Đen, xanh dương đậm (Thủy khắc Hỏa)",
    [FiveElement.THO]: "Xanh lá cây (Mộc khắc Thổ)"
  };

  const luckyNumbersMap: Record<FiveElement, number[]> = {
    [FiveElement.KIM]: [6, 7, 0, 2, 5, 8],
    [FiveElement.THUY]: [1, 6, 7],
    [FiveElement.MOC]: [3, 4, 1],
    [FiveElement.HOA]: [9, 3, 4],
    [FiveElement.THO]: [0, 2, 5, 8, 9]
  };

  return {
    birthYear,
    canIndex: canIdx,
    chiIndex: chiIdx,
    canChi: `${CAN[canIdx]} ${CHI[chiIdx]}`,
    zodiacAnimal: ZODIAC_ANIMALS[chiIdx],
    napAm: nAm,
    element: elem,
    isYang,
    yinYangLabel: isYang ? `Dương (${CAN[canIdx]} ${elementOfCan(canIdx)})` : `Âm (${CAN[canIdx]} ${elementOfCan(canIdx)})`,
    canElement: elementOfCan(canIdx),
    chiElement: elementOfChi(chiIdx),
    luckyColors: luckyColorsMap[elem],
    unluckyColors: unluckyColorsMap[elem],
    luckyNumbers: luckyNumbersMap[elem],
    cungMenh: calculateCungPhi(birthYear),
    tamHop: TAM_HOP_MAP[chiIdx] || [],
    lucHop: LUC_HOP_MAP[chiIdx] || "",
    tuHanhXung: TU_HANH_XUNG_MAP[chiIdx] || []
  };
}

export function matchPersonalDay(profile: PersonalProfile, almanac: DayAlmanac): PersonalDayMatch {
  const jd = almanac.lunar.julianDay > 0 ? almanac.lunar.julianDay : jdFromDate(almanac.lunar.solarDay, almanac.lunar.solarMonth, almanac.lunar.solarYear);
  const dayCan = (jd + 9) % 10;
  const dayChi = (jd + 1) % 12;

  const notes: string[] = [];
  let points = 0;
  const userCan = profile.canIndex;
  const userChi = profile.chiIndex;
  const userAnimal = `tuổi ${CHI[userChi]} (${profile.canChi})`;

  // 1. Địa Chi
  if (isClash(userChi, dayChi)) {
    const canClash = elementOvercomes(elementOfCan(dayCan), elementOfCan(userCan)) ||
                     elementOvercomes(elementOfCan(userCan), elementOfCan(dayCan));
    if (canClash) {
      points -= 6;
      notes.push(`⚠️ Thiên khắc địa xung: Ngày ${CAN[dayCan]} ${CHI[dayChi]} khắc trực diện ${userAnimal} – đại kỵ khởi sự việc trọng đại.`);
    } else {
      points -= 4;
      notes.push(`⚠️ Địa chi lục xung: Ngày ${CHI[dayChi]} xung trực diện với tuổi ${CHI[userChi]}.`);
    }
  }

  if (isSixHarmony(userChi, dayChi)) {
    points += 3;
    notes.push(`✅ Địa chi lục hợp: Ngày ${CHI[dayChi]} nhị hợp với tuổi ${CHI[userChi]} (cát khí hanh thông).`);
  }

  if (isTripleHarmony(userChi, dayChi)) {
    points += 2;
    notes.push(`✅ Địa chi tam hợp: Ngày ${CHI[dayChi]} tam hợp với tuổi ${CHI[userChi]} (quý nhân trợ giúp).`);
  }

  if (isHarm(userChi, dayChi)) {
    points -= 2;
    notes.push(`⚠️ Địa chi tương hại: Ngày ${CHI[dayChi]} lục hại với tuổi ${CHI[userChi]}.`);
  }

  if (isPunish(userChi, dayChi)) {
    points -= 2;
    notes.push(`⚠️ Địa chi tương hình: Ngày ${CHI[dayChi]} phạm hình với tuổi ${CHI[userChi]}.`);
  }

  if (isBreak(userChi, dayChi)) {
    points -= 1;
    notes.push(`⚠️ Địa chi tương phá: Ngày ${CHI[dayChi]} lục phá với tuổi ${CHI[userChi]}.`);
  }

  // 2. Thiên Can
  const dayCanEl = elementOfCan(dayCan);
  const userCanEl = elementOfCan(userCan);

  if (isCanHarmony(dayCan, userCan)) {
    points += 2;
    notes.push(`✅ Thiên can ngũ hợp: Can ngày ${CAN[dayCan]} tương hợp với can tuổi ${CAN[userCan]}.`);
  } else if (isCanClash(dayCan, userCan)) {
    points -= 1;
    notes.push(`⚠️ Thiên can tương xung: Can ngày ${CAN[dayCan]} xung can tuổi ${CAN[userCan]}.`);
  } else if (elementGenerates(dayCanEl, userCanEl)) {
    points += 1;
    notes.push(`✅ Thiên can tương sinh: Can ngày (${dayCanEl}) sinh dưỡng can tuổi (${userCanEl}).`);
  } else if (elementOvercomes(dayCanEl, userCanEl) && !isClash(userChi, dayChi)) {
    points -= 1;
    notes.push(`⚠️ Thiên can khắc: Can ngày ${CAN[dayCan]} khắc can tuổi ${CAN[userCan]}.`);
  }

  // 3. Ngũ Hành Nạp Âm
  const dayEl = almanac.napAmElement;
  const userEl = profile.element;

  if (elementGenerates(dayEl, userEl)) {
    points += 3;
    notes.push(`✅ Ngũ hành nạp âm: Ngày ${almanac.napAm} (${dayEl}) tương sinh bản mệnh ${profile.napAm} (${userEl}) – vượng khí đại cát.`);
  } else if (dayEl === userEl) {
    points += 1;
    notes.push(`✅ Ngũ hành nạp âm: Ngày ${almanac.napAm} tỷ hòa cùng hành ${userEl} với bản mệnh – thuận hòa vững bền.`);
  } else if (elementOvercomes(userEl, dayEl)) {
    points += 1;
    notes.push(`✅ Ngũ hành nạp âm: Bản mệnh ${userEl} chế khắc được hành ngày (${dayEl}) – nắm thế chủ động, đắc tài lộc.`);
  } else if (elementOvercomes(dayEl, userEl)) {
    points -= 3;
    notes.push(`⚠️ Ngũ hành nạp âm: Ngày ${almanac.napAm} (${dayEl}) khắc bản mệnh ${profile.napAm} (${userEl}) – hao tổn, nên thận trọng.`);
  } else {
    notes.push(`ℹ️ Ngũ hành nạp âm: Bản mệnh ${userEl} sinh xuất cho hành ngày (${dayEl}) – hao khí nhẹ, nên giữ sức khỏe.`);
  }

  // 4. Âm Dương
  const isYangDay = ((jd + 9) % 10) % 2 === 0;
  if (profile.isYang !== isYangDay) {
    notes.push(`☯️ Âm Dương giao hòa: Tuổi ${profile.isYang ? "Dương" : "Âm"} gặp ngày ${isYangDay ? "Dương" : "Âm"} – cân bằng cát lợi.`);
  }

  let verdict = ActivityVerdict.NEUTRAL;
  if (isClash(userChi, dayChi) || points <= -3) {
    verdict = ActivityVerdict.BAD;
  } else if (points >= 2) {
    verdict = ActivityVerdict.GOOD;
  }

  const scorePercent = Math.max(10, Math.min(99, Math.round(50 + points * 6 + (almanac.score - 50) * 0.25)));

  let badgeLabel = "Bình hòa";
  let badgeColor = "slate";
  let luckyAdvice = "Vận khí ngày bình ổn. Mọi việc cứ tuần tự tiến hành, cẩn trọng kiểm tra giấy tờ sẽ gặt hái kết quả tốt.";
  let cautionAdvice = "Giữ tâm thái bình hòa, tránh nóng nảy trong giao tiếp và cẩn trọng khi điều khiển phương tiện.";

  if (verdict === ActivityVerdict.BAD) {
    badgeLabel = `Xung tuổi (${points > 0 ? `+${points}` : points})`;
    badgeColor = "rose";
    luckyAdvice = "Nên làm việc thiện, an dưỡng tinh thần, hoàn thành các công việc thường nhật dang dở.";
    cautionAdvice = "Ngày xung tuổi gia chủ. Tránh xuất tiền lớn, ký hợp đồng kinh tế, tránh tranh chấp cãi cọ hay đi xa đêm khuya.";
  } else if (points >= 5) {
    badgeLabel = `Rất hợp (+${points})`;
    badgeColor = "emerald";
    luckyAdvice = "Thời cơ đại cát hanh thông! Rất thuận lợi để xuất hành, gặp gỡ đối tác, ký kết, mở rộng kinh doanh hay cầu tài.";
    cautionAdvice = "Tránh tự phụ chủ quan, hãy tận dụng cát khí để giải quyết dứt điểm các kế hoạch quan trọng.";
  } else if (points > 0) {
    badgeLabel = `Hợp tuổi (+${points})`;
    badgeColor = "emerald";
    luckyAdvice = "Ngày sinh vượng cát tường. Tiến hành công việc làm ăn, trao đổi hợp tác hoặc đi lại đều gặp may mắn.";
    cautionAdvice = "Hạn chế nhận lời hứa hẹn quá sức và chú ý giờ xuất hành để tối ưu tài lộc.";
  } else if (points < 0) {
    badgeLabel = `Hơi khắc (${points})`;
    badgeColor = "orange";
    luckyAdvice = "Tập trung giải quyết các việc nội bộ, củng cố gia đạo và chuẩn bị kế hoạch.";
    cautionAdvice = "Hạn chế quyết định vội vàng, thận trọng lời ăn tiếng nói nơi công sở.";
  }

  return {
    verdict,
    points,
    scorePercent,
    badgeLabel,
    badgeColor,
    notes,
    luckyAdvice,
    cautionAdvice
  };
}

const TRUC_RAW = [
  { name: "Kiến", score: 0, goodText: "Xuất hành, cưới hỏi, cầu tài, nhập học", badText: "Động thổ, đào giếng, an táng", g: [AlmanacActivity.TRAVEL, AlmanacActivity.WEDDING, AlmanacActivity.WEALTH, AlmanacActivity.STUDY, AlmanacActivity.CONTRACT], b: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.BURIAL, AlmanacActivity.REPAIR] },
  { name: "Trừ", score: 1, goodText: "Cúng giải hạn, chữa bệnh, dọn dẹp", badText: "Cưới hỏi, xuất hành, khai trương, ký kết", g: [AlmanacActivity.MEDICAL, AlmanacActivity.WORSHIP, AlmanacActivity.REPAIR], b: [AlmanacActivity.WEDDING, AlmanacActivity.TRAVEL, AlmanacActivity.OPENING, AlmanacActivity.CONTRACT, AlmanacActivity.BUY] },
  { name: "Mãn", score: 1, goodText: "Cầu tài, cầu phúc, cúng tế, khai trương", badText: "Uống thuốc, chữa bệnh, an táng, kiện tụng", g: [AlmanacActivity.WEALTH, AlmanacActivity.WORSHIP, AlmanacActivity.OPENING, AlmanacActivity.TRAVEL, AlmanacActivity.BUY], b: [AlmanacActivity.MEDICAL, AlmanacActivity.BURIAL] },
  { name: "Bình", score: 0, goodText: "Việc thông thường, sửa sang nhà cửa, cúng lễ", badText: "Đào mương, kiện tụng, tranh chấp", g: [AlmanacActivity.REPAIR, AlmanacActivity.WEDDING, AlmanacActivity.WORSHIP], b: [] },
  { name: "Định", score: 2, goodText: "Cưới hỏi, nhập trạch, khai trương, ký kết, xây dựng", badText: "Kiện tụng, đi xa, chữa bệnh", g: [AlmanacActivity.WEDDING, AlmanacActivity.MOVING, AlmanacActivity.OPENING, AlmanacActivity.CONTRACT, AlmanacActivity.WEALTH, AlmanacActivity.BUY, AlmanacActivity.CONSTRUCTION], b: [AlmanacActivity.TRAVEL, AlmanacActivity.MEDICAL] },
  { name: "Chấp", score: 1, goodText: "Lập khế ước, giao dịch, động thổ, tu sửa", badText: "Chuyển nhà, đi xa, khai trương", g: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.REPAIR, AlmanacActivity.CONTRACT, AlmanacActivity.MEDICAL], b: [AlmanacActivity.MOVING, AlmanacActivity.TRAVEL, AlmanacActivity.OPENING, AlmanacActivity.BUY] },
  { name: "Phá", score: -2, goodText: "Phá dỡ nhà cũ, chữa bệnh, trừ tà", badText: "Cưới hỏi, khai trương, ký kết, chuyển nhà", g: [AlmanacActivity.MEDICAL, AlmanacActivity.REPAIR], b: [AlmanacActivity.WEDDING, AlmanacActivity.OPENING, AlmanacActivity.CONTRACT, AlmanacActivity.MOVING, AlmanacActivity.TRAVEL, AlmanacActivity.CONSTRUCTION, AlmanacActivity.BUY] },
  { name: "Nguy", score: -2, goodText: "Cúng lễ, tĩnh dưỡng", badText: "Xuất hành, cưới hỏi, khai trương, xây dựng", g: [AlmanacActivity.WORSHIP], b: [AlmanacActivity.TRAVEL, AlmanacActivity.WEDDING, AlmanacActivity.OPENING, AlmanacActivity.CONSTRUCTION, AlmanacActivity.MOVING, AlmanacActivity.BUY] },
  { name: "Thành", score: 2, goodText: "Khai trương, cưới hỏi, nhập học, xây dựng, nhập trạch", badText: "Kiện tụng, tranh chấp", g: [AlmanacActivity.WEDDING, AlmanacActivity.OPENING, AlmanacActivity.STUDY, AlmanacActivity.CONSTRUCTION, AlmanacActivity.MOVING, AlmanacActivity.CONTRACT, AlmanacActivity.TRAVEL, AlmanacActivity.WEALTH, AlmanacActivity.BUY, AlmanacActivity.WORSHIP], b: [] },
  { name: "Thu", score: 0, goodText: "Thu hoạch, thu nợ, mua hàng, cầu tài", badText: "Khai trương, an táng, xuất hành đi xa", g: [AlmanacActivity.WEALTH, AlmanacActivity.BUY], b: [AlmanacActivity.OPENING, AlmanacActivity.BURIAL, AlmanacActivity.TRAVEL] },
  { name: "Khai", score: 2, goodText: "Khai trương, nhập học, xuất hành, cưới hỏi, động thổ", badText: "An táng, chôn cất", g: [AlmanacActivity.OPENING, AlmanacActivity.STUDY, AlmanacActivity.TRAVEL, AlmanacActivity.WEDDING, AlmanacActivity.CONSTRUCTION, AlmanacActivity.MOVING, AlmanacActivity.CONTRACT, AlmanacActivity.WEALTH], b: [AlmanacActivity.BURIAL] },
  { name: "Bế", score: -1, goodText: "Đắp đê, lấp hố, xây tường, an táng", badText: "Khai trương, xuất hành, cưới hỏi, chữa bệnh", g: [AlmanacActivity.BURIAL, AlmanacActivity.REPAIR], b: [AlmanacActivity.OPENING, AlmanacActivity.TRAVEL, AlmanacActivity.WEDDING, AlmanacActivity.MEDICAL, AlmanacActivity.STUDY, AlmanacActivity.MOVING] }
];

export const TRUC_LIST: TrucInfo[] = TRUC_RAW.map(t => ({
  ...t,
  fullName: `Trực ${t.name}`,
  good: t.g,
  bad: t.b
}));

const MANSIONS_DATA = [
  { name: "Giác", full: "Giác Mộc Giao", good: true, gText: "Cưới hỏi, xây dựng, thi cử, khai trương", bText: "An táng, sửa mộ", g: [AlmanacActivity.WEDDING, AlmanacActivity.CONSTRUCTION, AlmanacActivity.STUDY, AlmanacActivity.OPENING, AlmanacActivity.TRAVEL], b: [AlmanacActivity.BURIAL] },
  { name: "Cang", full: "Cang Kim Long", good: false, gText: "Cắt may, trồng trọt", bText: "Cưới hỏi, xây dựng, an táng", g: [], b: [AlmanacActivity.WEDDING, AlmanacActivity.CONSTRUCTION, AlmanacActivity.BURIAL, AlmanacActivity.OPENING, AlmanacActivity.MOVING] },
  { name: "Đê", full: "Đê Thổ Lạc", good: false, gText: "Không có việc lớn hợp", bText: "Khởi công, cưới hỏi, an táng", g: [], b: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.WEDDING, AlmanacActivity.TRAVEL, AlmanacActivity.BURIAL, AlmanacActivity.OPENING] },
  { name: "Phòng", full: "Phòng Nhật Thố", good: true, gText: "Khởi công, cưới hỏi, xuất hành, nhập trạch", bText: "Không kỵ việc lớn", g: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.WEDDING, AlmanacActivity.TRAVEL, AlmanacActivity.BUY, AlmanacActivity.BURIAL, AlmanacActivity.MOVING, AlmanacActivity.OPENING], b: [] },
  { name: "Tâm", full: "Tâm Nguyệt Hồ", good: false, gText: "Không có việc lớn hợp", bText: "Khởi công, cưới hỏi, an táng", g: [], b: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.WEDDING, AlmanacActivity.BURIAL, AlmanacActivity.OPENING, AlmanacActivity.CONTRACT] },
  { name: "Vĩ", full: "Vĩ Hỏa Hổ", good: true, gText: "Khởi công, cưới hỏi, xây dựng, khai trương", bText: "Đóng giường, may áo", g: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.WEDDING, AlmanacActivity.BURIAL, AlmanacActivity.OPENING, AlmanacActivity.REPAIR], b: [] },
  { name: "Cơ", full: "Cơ Thủy Báo", good: true, gText: "Khởi tạo, xây dựng, khai trương, cưới hỏi", bText: "Đóng giường, đi thuyền", g: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.BURIAL, AlmanacActivity.OPENING, AlmanacActivity.WEDDING, AlmanacActivity.REPAIR], b: [] },
  { name: "Đẩu", full: "Đẩu Mộc Giải", good: true, gText: "Khởi tạo, xây dựng, cưới hỏi, khai trương", bText: "Không kỵ việc lớn", g: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.WEDDING, AlmanacActivity.OPENING, AlmanacActivity.BURIAL, AlmanacActivity.REPAIR, AlmanacActivity.TRAVEL], b: [] },
  { name: "Ngưu", full: "Ngưu Kim Ngưu", good: false, gText: "Đi thuyền, cắt may", bText: "Khởi công, cưới hỏi, khai trương", g: [], b: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.WEDDING, AlmanacActivity.OPENING, AlmanacActivity.TRAVEL, AlmanacActivity.MOVING] },
  { name: "Nữ", full: "Nữ Thổ Bức", good: false, gText: "Kết màn, may áo", bText: "Cưới hỏi, khởi công, an táng", g: [], b: [AlmanacActivity.WEDDING, AlmanacActivity.CONSTRUCTION, AlmanacActivity.BURIAL, AlmanacActivity.OPENING] },
  { name: "Hư", full: "Hư Nhật Thử", good: false, gText: "Không có việc lớn hợp", bText: "Khởi công, cưới hỏi, khai trương", g: [], b: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.WEDDING, AlmanacActivity.OPENING, AlmanacActivity.MOVING] },
  { name: "Nguy", full: "Nguy Nguyệt Yến", good: false, gText: "Lót giường, chặt cỏ", bText: "Dựng nhà, cưới hỏi, an táng", g: [], b: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.WEDDING, AlmanacActivity.BURIAL, AlmanacActivity.TRAVEL] },
  { name: "Thất", full: "Thất Hỏa Trư", good: true, gText: "Khởi công, cưới hỏi, xây dựng, khai trương", bText: "Không kỵ việc lớn", g: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.WEDDING, AlmanacActivity.BURIAL, AlmanacActivity.OPENING, AlmanacActivity.MOVING, AlmanacActivity.REPAIR], b: [] },
  { name: "Bích", full: "Bích Thủy Du", good: true, gText: "Khởi tạo, cưới hỏi, xây dựng, khai trương", bText: "Không kỵ việc lớn", g: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.WEDDING, AlmanacActivity.OPENING, AlmanacActivity.BURIAL, AlmanacActivity.TRAVEL, AlmanacActivity.MOVING], b: [] },
  { name: "Khuê", full: "Khuê Mộc Lang", good: false, gText: "Nhập học, cắt may", bText: "Khai trương, an táng, động thổ", g: [AlmanacActivity.STUDY], b: [AlmanacActivity.OPENING, AlmanacActivity.BURIAL, AlmanacActivity.CONSTRUCTION] },
  { name: "Lâu", full: "Lâu Kim Cẩu", good: true, gText: "Khởi công, cưới hỏi, khai trương, nhập trạch", bText: "Đóng giường", g: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.WEDDING, AlmanacActivity.OPENING, AlmanacActivity.MOVING, AlmanacActivity.CONTRACT], b: [] },
  { name: "Vị", full: "Vị Thổ Trĩ", good: true, gText: "Khởi công, cưới hỏi, xây dựng, khai trương", bText: "Đi thuyền", g: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.WEDDING, AlmanacActivity.BURIAL, AlmanacActivity.OPENING], b: [] },
  { name: "Mão", full: "Mão Nhật Kê", good: false, gText: "Xây cất nhỏ", bText: "Khởi công, cưới hỏi, khai trương", g: [], b: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.WEDDING, AlmanacActivity.BURIAL, AlmanacActivity.OPENING, AlmanacActivity.MOVING] },
  { name: "Tất", full: "Tất Nguyệt Ô", good: true, gText: "Khởi công, cưới hỏi, khai trương, mua sắm", bText: "Đi thuyền", g: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.WEDDING, AlmanacActivity.BURIAL, AlmanacActivity.OPENING, AlmanacActivity.BUY], b: [] },
  { name: "Chủy", full: "Chủy Hỏa Hầu", good: false, gText: "Không có việc lớn hợp", bText: "Khởi công, an táng, cưới hỏi", g: [], b: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.BURIAL, AlmanacActivity.WEDDING, AlmanacActivity.CONTRACT] },
  { name: "Sâm", full: "Sâm Thủy Viên", good: true, gText: "Khởi công, xây dựng, nhập học, xuất hành", bText: "Cưới hỏi, an táng", g: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.STUDY, AlmanacActivity.TRAVEL, AlmanacActivity.WEALTH, AlmanacActivity.OPENING], b: [AlmanacActivity.WEDDING, AlmanacActivity.BURIAL] },
  { name: "Tỉnh", full: "Tỉnh Mộc Hãn", good: true, gText: "Khởi công, xây dựng, nhập học, xuất hành", bText: "An táng, may áo", g: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.STUDY, AlmanacActivity.TRAVEL, AlmanacActivity.REPAIR], b: [AlmanacActivity.BURIAL] },
  { name: "Quỷ", full: "Quỷ Kim Dương", good: false, gText: "An táng, chặt cỏ", bText: "Khởi công, cưới hỏi, khai trương", g: [AlmanacActivity.BURIAL], b: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.WEDDING, AlmanacActivity.OPENING, AlmanacActivity.MOVING] },
  { name: "Liễu", full: "Liễu Thổ Chương", good: false, gText: "Không có việc lớn hợp", bText: "Khởi công, cưới hỏi, khai trương", g: [], b: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.WEDDING, AlmanacActivity.BURIAL, AlmanacActivity.OPENING] },
  { name: "Tinh", full: "Tinh Nhật Mã", good: false, gText: "Xây phòng mới", bText: "Cưới hỏi, khai trương", g: [AlmanacActivity.CONSTRUCTION], b: [AlmanacActivity.WEDDING, AlmanacActivity.OPENING] },
  { name: "Trương", full: "Trương Nguyệt Lộc", good: true, gText: "Khởi công, cưới hỏi, khai trương, nhập học", bText: "Sửa thuyền", g: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.WEDDING, AlmanacActivity.OPENING, AlmanacActivity.STUDY, AlmanacActivity.BURIAL, AlmanacActivity.MOVING], b: [] },
  { name: "Dực", full: "Dực Hỏa Xà", good: false, gText: "Cắt áo", bText: "Cưới hỏi, xây dựng, khai trương", g: [], b: [AlmanacActivity.WEDDING, AlmanacActivity.CONSTRUCTION, AlmanacActivity.BURIAL, AlmanacActivity.OPENING] },
  { name: "Chẩn", full: "Chẩn Thủy Dẫn", good: true, gText: "Khởi công, cưới hỏi, nhập học, xuất hành", bText: "Đi thuyền", g: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.WEDDING, AlmanacActivity.STUDY, AlmanacActivity.TRAVEL, AlmanacActivity.BUY, AlmanacActivity.BURIAL], b: [] }
];

export const MANSIONS: MansionInfo[] = MANSIONS_DATA.map(m => ({
  name: m.name,
  fullName: m.full,
  isGood: m.good,
  goodText: m.gText,
  badText: m.bText,
  good: m.g,
  bad: m.b
}));

const NAP_AM = [
  "Hải Trung Kim", "Lô Trung Hỏa", "Đại Lâm Mộc", "Lộ Bàng Thổ", "Kiếm Phong Kim",
  "Sơn Đầu Hỏa", "Giản Hạ Thủy", "Thành Đầu Thổ", "Bạch Lạp Kim", "Dương Liễu Mộc",
  "Tuyền Trung Thủy", "Ốc Thượng Thổ", "Tích Lịch Hỏa", "Tùng Bách Mộc", "Trường Lưu Thủy",
  "Sa Trung Kim", "Sơn Hạ Hỏa", "Bình Địa Mộc", "Bích Thượng Thổ", "Kim Bạch Kim",
  "Phú Đăng Hỏa", "Thiên Hà Thủy", "Đại Trạch Thổ", "Thoa Xuyến Kim", "Tang Đố Mộc",
  "Đại Khê Thủy", "Sa Trung Thổ", "Thiên Thượng Hỏa", "Thạch Lựu Mộc", "Đại Hải Thủy"
];

function sexagenaryIndex(canIdx: number, chiIdx: number): number {
  return ((6 * canIdx - 5 * chiIdx) % 60 + 60) % 60;
}

export function napAm(canIdx: number, chiIdx: number): string {
  return NAP_AM[Math.floor(sexagenaryIndex(canIdx, chiIdx) / 2)];
}

export function elementOfNapAm(nAm: string): FiveElement {
  if (nAm.endsWith("Kim")) return FiveElement.KIM;
  if (nAm.endsWith("Thủy")) return FiveElement.THUY;
  if (nAm.endsWith("Mộc")) return FiveElement.MOC;
  if (nAm.endsWith("Hỏa")) return FiveElement.HOA;
  return FiveElement.THO;
}

export const LY_THUAN_PHONG: LyThuanPhongInfo[] = [
  { name: "Đại An", isGood: true, description: "Mọi việc tốt lành, cầu tài Tây Nam, nhà cửa yên ổn, xuất hành bình an." },
  { name: "Lưu Niên", isGood: false, description: "Việc khó thành, cầu tài mờ mịt, kiện cáo nên hoãn, người đi chưa có tin về." },
  { name: "Tốc Hỷ", isGood: true, description: "Tin vui sắp tới, cầu tài hướng Nam, gặp gỡ đối tác nhiều may mắn." },
  { name: "Xích Khẩu", isGood: false, description: "Dễ cãi cọ, thị phi, đề phòng hao tài; người đi nên hoãn lại." },
  { name: "Tiểu Cát", isGood: true, description: "Rất tốt lành, buôn bán có lời, có tin mừng, mọi việc hòa hợp." },
  { name: "Không Vong", isGood: false, description: "Cầu tài không lợi, hay bị trái ý, ra đi dễ gặp trở ngại." }
];

export function lyThuanPhong(lunarDay: number, lunarMonth: number, hourChiIdx: number): LyThuanPhongInfo {
  const khac = (hourChiIdx % 6) + 1;
  const idx = ((lunarDay + lunarMonth + khac - 3) % 6 + 6) % 6;
  return LY_THUAN_PHONG[idx];
}

function hyThanDirection(canIdx: number): string {
  switch (canIdx % 5) {
    case 0: return "Đông Bắc";
    case 1: return "Tây Bắc";
    case 2: return "Tây Nam";
    case 3: return "Chính Nam";
    default: return "Đông Nam";
  }
}

function taiThanDirection(canIdx: number): string {
  switch (canIdx) {
    case 0: case 1: return "Đông Nam";
    case 2: case 3: return "Chính Đông";
    case 4: return "Chính Bắc";
    case 5: return "Chính Nam";
    case 6: case 7: return "Tây Nam";
    case 8: return "Chính Tây";
    default: return "Tây Bắc";
  }
}

function hacThanDirection(canIdx: number, chiIdx: number): string | null {
  const sIdx = sexagenaryIndex(canIdx, chiIdx);
  if (sIdx >= 45 && sIdx <= 50) return "Đông Bắc";
  if (sIdx >= 51 && sIdx <= 55) return "Chính Đông";
  if ((sIdx >= 56 && sIdx <= 59) || sIdx === 0 || sIdx === 1) return "Đông Nam";
  if (sIdx >= 2 && sIdx <= 6) return "Chính Nam";
  if (sIdx >= 7 && sIdx <= 12) return "Tây Nam";
  if (sIdx >= 13 && sIdx <= 17) return "Chính Tây";
  if (sIdx >= 18 && sIdx <= 23) return "Tây Bắc";
  if (sIdx >= 24 && sIdx <= 28) return "Chính Bắc";
  return null;
}

const THIEN_DUC = ["Đinh", "Thân", "Nhâm", "Tân", "Hợi", "Giáp", "Quý", "Dần", "Bính", "Ất", "Tỵ", "Canh"];
const NGUYET_DUC = ["Bính", "Giáp", "Nhâm", "Canh"];
const SAT_CHU = [5, 0, 7, 3, 8, 10, 11, 1, 6, 9, 2, 4];
const THO_TU = [10, 4, 11, 5, 0, 6, 1, 7, 2, 8, 3, 9];
const VANG_VONG = [2, 5, 8, 11, 3, 6, 9, 0, 4, 7, 10, 1];
const TAM_NUONG = new Set([3, 7, 13, 18, 22, 27]);
const NGUYET_KY = new Set([5, 14, 23]);
const ALL_ACTIVITIES = Object.values(AlmanacActivity);

function specialDays(lunar: LunarDate, canIdx: number, chiIdx: number): [SpecialDay[], SpecialDay[]] {
  const mIdx = Math.max(0, Math.min(11, lunar.month - 1));
  const good: SpecialDay[] = [];
  const bad: SpecialDay[] = [];

  const td = THIEN_DUC[mIdx];
  if (td === CAN[canIdx] || td === CHI[chiIdx]) {
    good.push({ name: "Thiên Đức", isGood: true, description: "Cát tinh lớn, hóa giải hung sát, tốt cho mọi việc.", affects: ALL_ACTIVITIES, weight: 6 });
  }
  if (NGUYET_DUC[mIdx % 4] === CAN[canIdx]) {
    good.push({ name: "Nguyệt Đức", isGood: true, description: "Cát tinh, trăm việc đều tốt, đặc biệt cầu phúc, xây dựng.", affects: ALL_ACTIVITIES, weight: 6 });
  }
  if ((mIdx + 10) % 12 === chiIdx) {
    good.push({ name: "Thiên Hỷ", isGood: true, description: "Sao vui mừng, tốt cho cưới hỏi, khai trương, giao dịch.", affects: [AlmanacActivity.WEDDING, AlmanacActivity.OPENING, AlmanacActivity.CONTRACT], weight: 4 });
  }

  if (TAM_NUONG.has(lunar.day)) {
    bad.push({ name: "Tam Nương", isGood: false, description: "Ngày Tam Nương: kỵ cưới hỏi, khai trương, xuất hành, động thổ.", affects: [AlmanacActivity.WEDDING, AlmanacActivity.OPENING, AlmanacActivity.TRAVEL, AlmanacActivity.CONSTRUCTION, AlmanacActivity.MOVING, AlmanacActivity.CONTRACT], weight: 10 });
  }
  if (NGUYET_KY.has(lunar.day)) {
    bad.push({ name: "Nguyệt Kỵ", isGood: false, description: "Mùng 5, 14, 23: kỵ xuất hành, khai trương, cưới hỏi.", affects: [AlmanacActivity.TRAVEL, AlmanacActivity.OPENING, AlmanacActivity.WEDDING, AlmanacActivity.MOVING, AlmanacActivity.CONTRACT, AlmanacActivity.WEALTH], weight: 8 });
  }
  if (SAT_CHU[mIdx] === chiIdx) {
    bad.push({ name: "Sát Chủ", isGood: false, description: "Kỵ xây dựng, nhập trạch, cưới hỏi, an táng, khai trương.", affects: [AlmanacActivity.CONSTRUCTION, AlmanacActivity.MOVING, AlmanacActivity.WEDDING, AlmanacActivity.BURIAL, AlmanacActivity.OPENING], weight: 10 });
  }
  if (THO_TU[mIdx] === chiIdx) {
    bad.push({ name: "Thọ Tử", isGood: false, description: "Ngày Thọ Tử: trăm sự đều kỵ, không nên khởi sự việc lớn.", affects: ALL_ACTIVITIES, weight: 12 });
  }
  if (VANG_VONG[mIdx] === chiIdx) {
    bad.push({ name: "Vãng Vong", isGood: false, description: "Kỵ xuất hành, cưới hỏi, nhập trạch, khai trương, cầu tài.", affects: [AlmanacActivity.TRAVEL, AlmanacActivity.WEDDING, AlmanacActivity.MOVING, AlmanacActivity.OPENING, AlmanacActivity.WEALTH], weight: 8 });
  }

  return [good, bad];
}

export function getDayAlmanac(lunar: LunarDate): DayAlmanac {
  const jd = lunar.julianDay > 0 ? lunar.julianDay : jdFromDate(lunar.solarDay, lunar.solarMonth, lunar.solarYear);
  const canIdx = (jd + 9) % 10;
  const chiIdx = (jd + 1) % 12;

  const monthChi = (lunar.month + 1) % 12;
  const truc = TRUC_LIST[(chiIdx - monthChi + 12) % 12];
  const mansion = MANSIONS[(jd + 11) % 28];
  const nAm = napAm(canIdx, chiIdx);
  const napAmElem = elementOfNapAm(nAm);

  const [goodStars, badDays] = specialDays(lunar, canIdx, chiIdx);

  let score = 50;
  score += lunar.isAuspiciousDay ? 12 : -8;
  score += truc.score * 7;
  score += mansion.isGood ? 8 : -8;
  goodStars.forEach(s => { score += s.weight; });
  badDays.forEach(b => { score -= b.weight; });
  score = Math.max(0, Math.min(100, score));

  let level = DayLevel.NORMAL;
  if (score >= 75) level = DayLevel.EXCELLENT;
  else if (score >= 62) level = DayLevel.GOOD;
  else if (score >= 45) level = DayLevel.NORMAL;
  else if (score >= 30) level = DayLevel.BAD;
  else level = DayLevel.VERY_BAD;

  const activityPoints: Record<AlmanacActivity, number> = {} as Record<AlmanacActivity, number>;
  ALL_ACTIVITIES.forEach(act => {
    let p = 0;
    if (truc.good.includes(act)) p += 2;
    if (truc.bad.includes(act)) p -= 2;
    if (mansion.good.includes(act)) p += 1;
    if (mansion.bad.includes(act)) p -= 1;
    p += lunar.isAuspiciousDay ? 1 : -1;
    goodStars.forEach(s => { if (s.affects.includes(act)) p += 1; });
    badDays.forEach(b => { if (b.affects.includes(act)) p -= 3; });
    activityPoints[act] = p;
  });

  const hdHours = new Set(auspiciousHourIndices(chiIdx));
  const hours: HourInfo[] = [];
  for (let h = 0; h < 12; h++) {
    const isHd = hdHours.has(h);
    const ltp = lyThuanPhong(lunar.day, lunar.month, h);
    hours.push({
      chiIndex: h,
      chi: CHI[h],
      range: HOUR_RANGES[h],
      startHour: h === 0 ? 23 : h * 2 - 1,
      isHoangDao: isHd,
      lyThuanPhong: ltp,
      isBest: isHd && ltp.isGood
    });
  }

  const clashChi = (chiIdx + 6) % 12;
  const c1 = `${CAN[(canIdx + 4) % 10]} ${CHI[clashChi]}`;
  const c2 = `${CAN[(canIdx + 6) % 10]} ${CHI[clashChi]}`;
  const clashAgesText = `Tuổi ${CHI[clashChi]} (${c1}, ${c2})`;

  const goodActivities = ALL_ACTIVITIES.filter(a => activityPoints[a] >= 2)
    .sort((a, b) => activityPoints[b] - activityPoints[a]);

  const badActivities = ALL_ACTIVITIES.filter(a => activityPoints[a] <= -1)
    .sort((a, b) => activityPoints[a] - activityPoints[b]);

  const summary = `${lunar.isAuspiciousDay ? "Ngày Hoàng đạo" : "Ngày Hắc đạo"} (${lunar.auspiciousDayType.split(" (")[0]}), ${truc.fullName}, sao ${mansion.name} ${mansion.isGood ? "tốt" : "xấu"}. ${badDays.length ? `Phạm ${badDays.map(b => b.name).join(", ")}. ` : ""}${level === DayLevel.EXCELLENT || level === DayLevel.GOOD ? "Ngày thuận lợi để làm việc lớn." : "Nên cẩn trọng khi khởi sự việc trọng đại."}`;

  return {
    lunar,
    truc,
    mansion,
    napAm: nAm,
    napAmElement: napAmElem,
    goodStars,
    badDays,
    score,
    level,
    activityPoints,
    hyThan: hyThanDirection(canIdx),
    taiThan: taiThanDirection(canIdx),
    hacThan: hacThanDirection(canIdx, chiIdx),
    hours,
    clashChi,
    clashAgesText,
    summary,
    goodActivities,
    badActivities
  };
}

// ==========================================
// TÍNH NĂNG BÓI CHI TIẾT THEO GIỚI TÍNH, NGÀY THÁNG NĂM SINH
// ==========================================

export interface UserBirthProfile {
  day: number;
  month: number;
  year: number;
  gender: "male" | "female";
  isLunar?: boolean;
}

export interface ZodiacSignInfo {
  name: string;
  latinName: string;
  symbol: string;
  dateRange: string;
  element: "Lửa" | "Đất" | "Khí" | "Nước";
  rulingPlanet: string;
  traits: string;
  strengths: string[];
}

export interface NumerologyInfo {
  lifePathNumber: number;
  title: string;
  keywords: string[];
  description: string;
  strengths: string[];
  challenges: string[];
  idealCareers: string[];
}

export interface BatTrachDirection {
  direction: string;
  type: "Sinh Khí" | "Thiên Y" | "Diên Niên" | "Phục Vị" | "Tuyệt Mệnh" | "Ngũ Quỷ" | "Lục Sát" | "Họa Hại";
  isGood: boolean;
  starName: string;
  starElement: string;
  meaning: string;
  advice: string;
}

export interface LifeStageFortune {
  stage: "Tiền vận" | "Trung vận" | "Hậu vận";
  ageRange: string;
  title: string;
  overview: string;
  careerFinance: string;
  familyLove: string;
}

export interface SaoHan2026Info {
  lunarAge: number;
  sao: string;
  saoType: "Cát tinh" | "Hung tinh" | "Trung tinh";
  saoDescription: string;
  han: string;
  hanDescription: string;
  yearOverview: string;
  favorableMonths: number[];
  cautionMonths: number[];
}

export interface DetailedBirthFortune {
  profile: UserBirthProfile;
  solarDate: { day: number; month: number; year: number };
  lunarDate: LunarDate;
  personalProfile: PersonalProfile;
  cungPhi: string;
  cungPhiGroup: "Đông Tứ Mệnh" | "Tây Tứ Mệnh";
  cungPhiElement: string;
  batTrachDirections: BatTrachDirection[];
  westernZodiac: ZodiacSignInfo;
  numerology: NumerologyInfo;
  personalityByGender: {
    title: string;
    coreTraits: string;
    strengths: string;
    socialStyle: string;
  };
  loveMarriage: {
    overview: string;
    bestAges: string[];
    challengingAges: string[];
    advice: string;
  };
  lifeStages: LifeStageFortune[];
  saoHan2026: SaoHan2026Info;
}

export function getWesternZodiac(day: number, month: number): ZodiacSignInfo {
  if ((month === 3 && day >= 21) || (month === 4 && day <= 19)) {
    return {
      name: "Bạch Dương",
      latinName: "Aries",
      symbol: "♈",
      dateRange: "21/03 - 19/04",
      element: "Lửa",
      rulingPlanet: "Sao Hỏa (Mars)",
      traits: "Tiên phong, quả cảm, dồi dào sinh lực và luôn khao khát dẫn đầu trong mọi lĩnh vực.",
      strengths: ["Quyết đoán nhanh", "Nhiệt huyết bùng nổ", "Tự tin mở lối", "Dám nghĩ dám làm"]
    };
  }
  if ((month === 4 && day >= 20) || (month === 5 && day <= 20)) {
    return {
      name: "Kim Ngưu",
      latinName: "Taurus",
      symbol: "♉",
      dateRange: "20/04 - 20/05",
      element: "Đất",
      rulingPlanet: "Sao Kim (Venus)",
      traits: "Kiên định, thực tế, bền bỉ, có khả năng quản lý tài chính và yêu cái đẹp đích thực.",
      strengths: ["Đáng tin cậy", "Kiên nhẫn phi thường", "Quản lý tài chính giỏi", "Vững vàng trước bão giông"]
    };
  }
  if ((month === 5 && day >= 21) || (month === 6 && day <= 21)) {
    return {
      name: "Song Tử",
      latinName: "Gemini",
      symbol: "♊",
      dateRange: "21/05 - 21/06",
      element: "Khí",
      rulingPlanet: "Sao Thủy (Mercury)",
      traits: "Linh hoạt, hóm hỉnh, giao tiếp cuốn hút, tư duy đa chiều và thích nghi cực nhanh.",
      strengths: ["Hoạt ngôn thuyết phục", "Trí tuệ nhanh nhạy", "Đa tài đa nghệ", "Sáng tạo đổi mới"]
    };
  }
  if ((month === 6 && day >= 22) || (month === 7 && day <= 22)) {
    return {
      name: "Cự Giải",
      latinName: "Cancer",
      symbol: "♋",
      dateRange: "22/06 - 22/07",
      element: "Nước",
      rulingPlanet: "Mặt Trăng (Moon)",
      traits: "Giàu tình cảm, trực giác tinh tế, chu đáo và luôn đặt tổ ấm gia đình làm điểm tựa.",
      strengths: ["Thấu cảm sâu sắc", "Bảo bọc chở che", "Trung thành tuyệt đối", "Trực giác chuẩn xác"]
    };
  }
  if ((month === 7 && day >= 23) || (month === 8 && day <= 22)) {
    return {
      name: "Sư Tử",
      latinName: "Leo",
      symbol: "♌",
      dateRange: "23/07 - 22/08",
      element: "Lửa",
      rulingPlanet: "Mặt Trời (Sun)",
      traits: "Bản lĩnh lãnh đạo, phong thái uy quyền, quảng đại, hào phóng và luôn tỏa sáng rực rỡ.",
      strengths: ["Khí chất vương giả", "Hào hiệp trượng nghĩa", "Truyền cảm hứng lớn", "Lạc quan tích cực"]
    };
  }
  if ((month === 8 && day >= 23) || (month === 9 && day <= 22)) {
    return {
      name: "Xử Nữ",
      latinName: "Virgo",
      symbol: "♍",
      dateRange: "23/08 - 22/09",
      element: "Đất",
      rulingPlanet: "Sao Thủy (Mercury)",
      traits: "Tỉ mỉ, kỷ luật, chu toàn, khả năng phân tích logic và tiêu chuẩn hoàn thiện rất cao.",
      strengths: ["Chính xác tuyệt đối", "Tư duy phân tích sắc sảo", "Tận tâm chu đáo", "Tổ chức khoa học"]
    };
  }
  if ((month === 9 && day >= 23) || (month === 10 && day <= 23)) {
    return {
      name: "Thiên Bình",
      latinName: "Libra",
      symbol: "♎",
      dateRange: "23/09 - 23/10",
      element: "Khí",
      rulingPlanet: "Sao Kim (Venus)",
      traits: "Công bằng, thanh lịch, giao tiếp hòa nhã, có mắt thẩm mỹ tinh tế và yêu hòa bình.",
      strengths: ["Ngoại giao xuất sắc", "Cân bằng dĩ hòa vi quý", "Thẩm mỹ tinh tế", "Thu hút lòng người"]
    };
  }
  if ((month === 10 && day >= 24) || (month === 11 && day <= 21)) {
    return {
      name: "Bọ Cạp",
      latinName: "Scorpio",
      symbol: "♏",
      dateRange: "24/10 - 21/11",
      element: "Nước",
      rulingPlanet: "Sao Diêm Vương (Pluto)",
      traits: "Sâu sắc, kiên cường, nội tâm mạnh mẽ, trực giác thấu thị và ý chí thép vượt khó khăn.",
      strengths: ["Ý chí bất khuất", "Trực giác nhạy bén", "Bí ẩn cuốn hút", "Tập trung cao độ"]
    };
  }
  if ((month === 11 && day >= 22) || (month === 12 && day <= 21)) {
    return {
      name: "Nhân Mã",
      latinName: "Sagittarius",
      symbol: "♐",
      dateRange: "22/11 - 21/12",
      element: "Lửa",
      rulingPlanet: "Sao Mộc (Jupiter)",
      traits: "Lạc quan, tự do, phóng khoáng, ham học hỏi và luôn hướng về những chân trời mới mẻ.",
      strengths: ["Tầm nhìn chiến lược", "Trung thực thẳng thắn", "Năng lượng tích cực", "Dám bước ra khỏi vùng an toàn"]
    };
  }
  if ((month === 12 && day >= 22) || (month === 1 && day <= 19)) {
    return {
      name: "Ma Kết",
      latinName: "Capricorn",
      symbol: "♑",
      dateRange: "22/12 - 19/01",
      element: "Đất",
      rulingPlanet: "Sao Thổ (Saturn)",
      traits: "Kỷ luật thép, tham vọng lớn, trách nhiệm cao và bền bỉ leo lên đỉnh vinh quang sự nghiệp.",
      strengths: ["Kiên định mục tiêu", "Chiến lược dài hạn", "Tinh thần trách nhiệm cao", "Bản lĩnh lãnh đạo vững chãi"]
    };
  }
  if ((month === 1 && day >= 20) || (month === 2 && day <= 18)) {
    return {
      name: "Bảo Bình",
      latinName: "Aquarius",
      symbol: "♒",
      dateRange: "20/01 - 18/02",
      element: "Khí",
      rulingPlanet: "Sao Thiên Vương (Uranus)",
      traits: "Độc lập, sáng tạo đột phá, tư duy vượt thời đại, nhân đạo và ghét sự gò bó sáo mòn.",
      strengths: ["Tư duy đổi mới", "Tầm nhìn nhân văn", "Độc lập tự chủ", "Khả năng phát minh sáng tạo"]
    };
  }
  return {
    name: "Song Ngư",
    latinName: "Pisces",
    symbol: "♓",
    dateRange: "19/02 - 20/03",
    element: "Nước",
    rulingPlanet: "Sao Hải Vương (Neptune)",
    traits: "Nhân hậu, giàu lòng trắc ẩn, tâm hồn nghệ sĩ lãng mạn và trực giác tâm linh thấu hiểu.",
    strengths: ["Thấu cảm tâm hồn", "Khả năng nghệ thuật", "Lương thiện bao dung", "Trực giác tiên đoán"]
  };
}

const NUMEROLOGY_DATA: Record<number, Omit<NumerologyInfo, "lifePathNumber">> = {
  2: {
    title: "Sứ Giả Hòa Bình & Kết Nối Tình Thân",
    keywords: ["Lắng nghe", "Hòa hợp", "Thấu cảm", "Ngoại giao"],
    description: "Bạn mang năng lượng của sự nhạy cảm, hòa giải và gắn kết. Bạn có năng khiếu ngoại giao bẩm sinh, luôn biết cách xoa dịu mâu thuẫn và tạo ra sự cân bằng trong các mối quan hệ.",
    strengths: ["Khả năng lắng nghe chân thành", "Trực giác tình cảm tốt", "Trung thành và đáng tin cậy", "Khéo léo dung hòa tập thể"],
    challenges: ["Dễ bị tổn thương bởi lời chỉ trích", "Hay do dự không dám đưa ra quyết định quyết liệt", "Đôi khi quá hy sinh quyền lợi cá nhân"],
    idealCareers: ["Ngoại giao, hòa giải viên", "Tâm lý học, tư vấn trị liệu", "Nhân sự (HR), quan hệ công chúng", "Nghệ thuật, giáo dục"]
  },
  3: {
    title: "Người Sáng Tạo & Truyền Cảm Hứng Lạc Quan",
    keywords: ["Sáng tạo", "Hoạt ngôn", "Năng lượng", "Niềm vui"],
    description: "Bạn là linh hồn của các cuộc hội ngộ, sở hữu nguồn năng lượng tươi vui, khiếu hài hước và tài năng ngôn ngữ phong phú. Cuộc đời bạn là hành trình lan tỏa cảm hứng và làm đẹp cuộc sống.",
    strengths: ["Tài ăn nói và biểu đạt lôi cuốn", "Tư duy sáng tạo không giới hạn", "Lạc quan truyền năng lượng tích cực", "Dễ kết nối và được quý mến"],
    challenges: ["Dễ phân tán mục tiêu, nhanh chán", "Cần rèn luyện tính kiên trì kỷ luật", "Đôi khi nói nhiều hơn lắng nghe"],
    idealCareers: ["Truyền thông, marketing, sáng tạo nội dung", "MC, diễn giả, nghệ thuật giải trí", "Thiết kế, hội họa, viết lách", "Kinh doanh và bán hàng"]
  },
  4: {
    title: "Người Kiến Tạo Kỷ Luật & Thực Tiễn Vững Chãi",
    keywords: ["Kỷ luật", "Thực tế", "Tổ chức", "Bền bỉ"],
    description: "Bạn là điểm tựa vững chãi của gia đình và tổ chức. Với tinh thần trách nhiệm cao độ và sự tỉ mỉ, bạn luôn biến những ý tưởng mông lung thành quy trình chuẩn mực và kết quả bền vững.",
    strengths: ["Kỷ luật sắt và sự kiên trì phi thường", "Khả năng tổ chức, quản lý chi tiết sắc sảo", "Trung thực, giữ chữ tín hàng đầu", "Xây dựng nền móng dài hạn"],
    challenges: ["Đôi khi cứng nhắc, ngại thay đổi", "Dễ rơi vào lo âu về tài chính và an toàn", "Cần học cách thả lỏng và linh hoạt"],
    idealCareers: ["Quản lý dự án, kỹ sư, kiến trúc sư", "Tài chính, kế toán, kiểm toán", "Pháp lý, quản trị hệ thống", "Sản xuất, bất động sản"]
  },
  5: {
    title: "Nhà Thám Hiểm Tự Do & Đột Phá Tiên Phong",
    keywords: ["Tự do", "Khám phá", "Thích nghi", "Đổi mới"],
    description: "Bạn sinh ra để phá vỡ những giới hạn khuôn mẫu. Với lòng dũng cảm và niềm đam mê phiêu lưu, bạn không ngừng tìm kiếm những chân trời mới và tạo nên những cuộc cách mạng đổi mới tích cực.",
    strengths: ["Khả năng thích ứng siêu việt", "Tư duy cởi mở, không ngại thử thách", "Năng lượng dồi dào, cuốn hút tự nhiên", "Dám nghĩ khác và làm khác"],
    challenges: ["Khó gắn bó lâu dài với công việc nhàm chán", "Dễ bốc đồng và thiếu kiên nhẫn", "Cần học cách kiểm soát sự tùy hứng"],
    idealCareers: ["Du lịch, hàng không, ngoại thương", "Khởi nghiệp công nghệ, đổi mới sáng tạo", "Nhiếp ảnh gia, nhà báo du ký", "Tư vấn chiến lược, tiếp thị số"]
  },
  6: {
    title: "Người Chăm Sóc & Yêu Thương Gia Đình",
    keywords: ["Trách nhiệm", "Yêu thương", "Bảo bọc", "Mỹ cảm"],
    description: "Trái tim bạn luôn đong đầy tình yêu thương vô điều kiện. Bạn coi sóc tổ ấm, chu toàn cho những người thân yêu và sẵn sàng cống hiến vì hạnh phúc của cộng đồng.",
    strengths: ["Tình cảm bao la, đức hy sinh cao quý", "Trách nhiệm gia đình và xã hội tuyệt vời", "Mắt thẩm mỹ và khả năng trang trí tinh tế", "Chữa lành và gắn kết các thành viên"],
    challenges: ["Dễ ôm đồm gánh vác việc của người khác", "Hay lo lắng thái quá và kỳ vọng cao", "Cần biết yêu thương và chăm sóc bản thân"],
    idealCareers: ["Y tế, bác sĩ, điều dưỡng", "Giáo dục mầm non, tâm lý học đường", "Thiết kế nội thất, ẩm thực, nhà hàng", "Tổ chức phi lợi nhuận, công tác xã hội"]
  },
  7: {
    title: "Nhà Thông Thái & Chiêm Nghiệm Tri Thức",
    keywords: ["Trí tuệ", "Triết lý", "Nghiên cứu", "Nội tâm"],
    description: "Bạn là một linh hồn tri thức khao khát tìm hiểu bản chất sâu xa của vạn vật. Bạn không tin vào bề nổi mà luôn phân tích, chiêm nghiệm và tìm kiếm chân lý cuộc đời.",
    strengths: ["Tư duy phản biện và trực giác triết học", "Khả năng nghiên cứu chuyên sâu độc lập", "Độc lập tự chủ cao", "Cái nhìn sâu sắc qua vẻ ngoài hào nhoáng"],
    challenges: ["Có xu hướng thu mình, sống khép kín", "Dễ hoài nghi và xa cách đám đông", "Cần mở lòng kết nối tình cảm với mọi người"],
    idealCareers: ["Nghiên cứu khoa học, học giả, triết gia", "Phân tích dữ liệu, chuyên gia công nghệ cao", "Tư vấn chiến lược cấp cao", "Y học chuyên sâu, tâm linh huyền học"]
  },
  8: {
    title: "Bản Lĩnh Điều Hành & Quản Trị Tài Chính",
    keywords: ["Quyền lực", "Tài chính", "Điều hành", "Thành đạt"],
    description: "Bạn sở hữu tư chất của một nhà lãnh đạo tầm vóc, có trực giác kinh doanh sắc sảo và khả năng kiến tạo thịnh vượng vật chất. Bạn sinh ra để làm chủ những đại nghiệp lớn lao.",
    strengths: ["Tầm nhìn kinh doanh và quản trị tài chính sắc bén", "Bản lĩnh vững vàng trước áp lực cao", "Khả năng chỉ huy và dẫn dắt đội ngũ lớn", "Khát vọng vươn tới đỉnh cao thành công"],
    challenges: ["Dễ đặt nặng giá trị vật chất hơn cảm xúc", "Có thể trở nên độc đoán nếu thiếu lắng nghe", "Cần cân bằng giữa công việc và đời sống tinh thần"],
    idealCareers: ["Doanh nhân, CEO, giám đốc điều hành", "Đầu tư tài chính, ngân hàng, chứng khoán", "Bất động sản quy mô lớn", "Chính trị gia, quản lý cấp cao"]
  },
  9: {
    title: "Trực Giác Nhân Đạo & Phụng Sự Cộng Đồng",
    keywords: ["Nhân đạo", "Bao dung", "Lý tưởng", "Cống hiến"],
    description: "Bạn mang tâm thế của người phụng sự nhân loại. Với trái tim vị tha và tầm nhìn đại cục, bạn luôn khát khao để lại những giá trị trường tồn và nâng đỡ những người yếu thế.",
    strengths: ["Lòng nhân ái bao la và tinh thần đại nghĩa", "Khả năng truyền cảm hứng nhân văn mạnh mẽ", "Uy tín và sự kính trọng tự nhiên từ mọi người", "Tầm nhìn bao quát vượt lên lợi ích vị kỷ"],
    challenges: ["Dễ bị thất vọng khi đời thực không như lý tưởng", "Khó tha thứ cho sự bất công hoặc lừa dối", "Cần thực tế hơn trong quản lý tài chính cá nhân"],
    idealCareers: ["Lãnh đạo các tổ chức cộng đồng, từ thiện", "Giáo dục, đào tạo phát triển con người", "Văn hóa nghệ thuật mang tính nhân văn", "Luật sư nhân quyền, tư vấn hướng nghiệp"]
  },
  10: {
    title: "Người Tiên Phong Độc Lập & Dẫn Đầu Xu Thế",
    keywords: ["Tiên phong", "Tự chủ", "Quyết đoán", "Bản lĩnh"],
    description: "Số 10 (con số của sự trọn vẹn và khởi nguyên) đại diện cho nhà lãnh đạo tự thân. Bạn có tinh thần độc lập mạnh mẽ, ý chí kiên định và khả năng xoay chuyển cục diện để tự tay mở ra con đường thành công.",
    strengths: ["Tính tự lập và lòng tự trọng rất cao", "Quyết đoán, không nao núng trước thử thách", "Khả năng tái tạo và thích nghi ngoạn mục", "Sức hút cá nhân và tài dẫn dắt bẩm sinh"],
    challenges: ["Cái tôi lớn, đôi khi khó chấp nhận lời khuyên", "Dễ cảm thấy cô đơn trên hành trình dẫn đầu", "Cần rèn luyện sự lắng nghe và thấu cảm"],
    idealCareers: ["Nhà sáng lập, chủ doanh nghiệp độc lập", "Giám đốc sáng tạo, trưởng bộ phận đổi mới", "Chỉ huy, thể thao chuyên nghiệp", "Nhà hoạch định chính sách"]
  },
  11: {
    title: "Bậc Thầy Trực Giác & Soi Sáng Tâm Linh",
    keywords: ["Trực giác", "Tâm linh", "Khai sáng", "Lý tưởng"],
    description: "Số 11 là Master Number mang tần số rung động tâm linh phi thường. Bạn sở hữu giác quan thứ sáu nhạy bén kỳ lạ và có sứ mệnh nâng tầm nhận thức, soi rọi con đường cho người khác.",
    strengths: ["Trực giác tâm linh thấu thị vượt trội", "Khả năng truyền cảm hứng sâu sắc tới tâm hồn", "Tư tưởng tiến bộ và lý tưởng cao đẹp", "Sự nhạy cảm tinh tế với năng lượng xung quanh"],
    challenges: ["Áp lực tinh thần nội tâm rất lớn", "Dễ bị quá tải cảm xúc nếu không biết thiền định", "Cần giữ đôi chân vững chắc trên mặt đất"],
    idealCareers: ["Nhà khai vấn tâm lý (Life Coach), trị liệu", "Lãnh đạo tinh thần, nhà nghiên cứu tâm linh", "Nghệ sĩ triết học, nhà thơ, triết gia", "Tổ chức từ thiện quốc tế"]
  },
  22: {
    title: "Bậc Thầy Kiến Tạo Ước Mơ Vĩ Đại (Master Builder)",
    keywords: ["Kiến tạo", "Tầm vóc", "Hiện thực hóa", "Vĩ đại"],
    description: "Số 22/4 là Master Number quyền năng nhất trong Thần số học. Bạn vừa có tầm nhìn vĩ mô siêu phàm của số 11 vừa có tính thực tiễn kỷ luật sắt của số 4, có thể biến những giấc mơ khổng lồ thành hiện thực.",
    strengths: ["Khả năng hiện thực hóa các dự án quy mô lớn", "Tầm nhìn chiến lược vượt thời đại", "Năng lực lãnh đạo và tổ chức xuất chúng", "Đóng góp những di sản vĩ đại cho xã hội"],
    challenges: ["Gánh nặng trách nhiệm quá to lớn", "Dễ rơi vào áp lực cầu toàn cực độ", "Cần học cách chia sẻ quyền hạn và tin tưởng người khác"],
    idealCareers: ["Kiến trúc sư các đại công trình quốc gia", "Nhà sáng lập các tập đoàn đa quốc gia", "Chính khách kiến thiết đất nước", "Nhà từ thiện quy mô toàn cầu"]
  }
};

export function calculateLifePathNumber(day: number, month: number, year: number): NumerologyInfo {
  function sumDigits(n: number): number {
    return String(n).split("").reduce((acc, c) => acc + (parseInt(c, 10) || 0), 0);
  }
  function reduce(n: number, isFinal: boolean = false): number {
    if (n === 11 || n === 22) return n;
    let s = sumDigits(n);
    while (s > 9) {
      if (s === 10 && isFinal) return 10;
      if (s === 11 || s === 22) return s;
      s = sumDigits(s);
    }
    return s;
  }
  const rd = reduce(day);
  const rm = reduce(month);
  const ry = reduce(year);
  const total = rd + rm + ry;
  const lifePath = reduce(total, true);

  const fallback = NUMEROLOGY_DATA[10];
  const data = NUMEROLOGY_DATA[lifePath] || fallback;

  return {
    lifePathNumber: lifePath,
    ...data
  };
}

const BAT_TRACH_MAP: Record<string, {
  sinhKhi: string; thienY: string; dienNien: string; phucVi: string;
  hoaHai: string; lucSat: string; nguQuy: string; tuyetMenh: string;
}> = {
  "Khảm": {
    sinhKhi: "Đông Nam", thienY: "Chính Đông", dienNien: "Chính Nam", phucVi: "Chính Bắc",
    hoaHai: "Chính Tây", lucSat: "Tây Bắc", nguQuy: "Đông Bắc", tuyetMenh: "Tây Nam"
  },
  "Khôn": {
    sinhKhi: "Đông Bắc", thienY: "Chính Tây", dienNien: "Tây Bắc", phucVi: "Tây Nam",
    hoaHai: "Chính Đông", lucSat: "Chính Nam", nguQuy: "Đông Nam", tuyetMenh: "Chính Bắc"
  },
  "Chấn": {
    sinhKhi: "Chính Nam", thienY: "Chính Bắc", dienNien: "Đông Nam", phucVi: "Chính Đông",
    hoaHai: "Tây Nam", lucSat: "Đông Bắc", nguQuy: "Tây Bắc", tuyetMenh: "Chính Tây"
  },
  "Tốn": {
    sinhKhi: "Chính Bắc", thienY: "Chính Nam", dienNien: "Chính Đông", phucVi: "Đông Nam",
    hoaHai: "Tây Bắc", lucSat: "Chính Tây", nguQuy: "Tây Nam", tuyetMenh: "Đông Bắc"
  },
  "Càn": {
    sinhKhi: "Chính Tây", thienY: "Đông Bắc", dienNien: "Tây Nam", phucVi: "Tây Bắc",
    hoaHai: "Đông Nam", lucSat: "Chính Bắc", nguQuy: "Chính Đông", tuyetMenh: "Chính Nam"
  },
  "Đoài": {
    sinhKhi: "Tây Bắc", thienY: "Tây Nam", dienNien: "Đông Bắc", phucVi: "Chính Tây",
    hoaHai: "Chính Bắc", lucSat: "Đông Nam", nguQuy: "Chính Nam", tuyetMenh: "Chính Đông"
  },
  "Cấn": {
    sinhKhi: "Tây Nam", thienY: "Tây Bắc", dienNien: "Chính Tây", phucVi: "Đông Bắc",
    hoaHai: "Chính Nam", lucSat: "Chính Đông", nguQuy: "Chính Bắc", tuyetMenh: "Đông Nam"
  },
  "Ly": {
    sinhKhi: "Chính Đông", thienY: "Đông Nam", dienNien: "Chính Bắc", phucVi: "Chính Nam",
    hoaHai: "Đông Bắc", lucSat: "Tây Nam", nguQuy: "Chính Tây", tuyetMenh: "Tây Bắc"
  }
};

export function getBatTrachDetails(cungFullName: string): BatTrachDirection[] {
  let key = "Càn";
  for (const k of Object.keys(BAT_TRACH_MAP)) {
    if (cungFullName.includes(k)) {
      key = k;
      break;
    }
  }
  const m = BAT_TRACH_MAP[key] || BAT_TRACH_MAP["Càn"];

  return [
    {
      direction: m.sinhKhi,
      type: "Sinh Khí",
      isGood: true,
      starName: "Sao Tham Lang (Mộc)",
      starElement: "Mộc",
      meaning: "Đại cát đệ nhất. Thu hút tài lộc dồi dào, thăng quan tiến chức, danh tiếng rạng rỡ.",
      advice: "Rất tốt để đặt hướng cửa chính nhà ở, bàn làm việc của người đứng đầu, phòng khách để chiêu tài tụ lộc."
    },
    {
      direction: m.thienY,
      type: "Thiên Y",
      isGood: true,
      starName: "Sao Cự Môn (Thổ)",
      starElement: "Thổ",
      meaning: "Cát tinh chủ về sức khỏe dồi dào, tiêu trừ bệnh tật, có quý nhân tương trợ, trường thọ.",
      advice: "Thích hợp đặt đầu giường ngủ, phòng nghỉ ngơi dưỡng sức hoặc phòng khám chữa bệnh."
    },
    {
      direction: m.dienNien,
      type: "Diên Niên",
      isGood: true,
      starName: "Sao Vũ Khúc (Kim)",
      starElement: "Kim",
      meaning: "Cát tinh chủ về tình duyên gia đạo, vợ chồng con cái hòa thuận, quan hệ xã giao tốt đẹp.",
      advice: "Tốt cho việc đặt phòng khách, cửa chính, phòng ngủ vợ chồng để giữ gìn hòa khí ấm êm."
    },
    {
      direction: m.phucVi,
      type: "Phục Vị",
      isGood: true,
      starName: "Sao Tả Phụ (Thủy)",
      starElement: "Thủy",
      meaning: "Cát tinh chủ về sự an định, củng cố sức mạnh tinh thần, thi cử tiến bộ, may mắn trong học tập.",
      advice: "Rất tốt để đặt bàn thờ tổ tiên trang nghiêm, bàn học tập, nơi thiền định tĩnh tâm."
    },
    {
      direction: m.hoaHai,
      type: "Họa Hại",
      isGood: false,
      starName: "Sao Lộc Tồn (Thổ)",
      starElement: "Thổ",
      meaning: "Hung tinh nhẹ. Dễ gặp chuyện trái ý, bất hòa, thị phi vặt vãnh làm tiêu hao tinh thần.",
      advice: "Tránh mở cửa chính vào hướng này. Có thể bố trí phòng kho, nhà vệ sinh để trấn hung."
    },
    {
      direction: m.lucSat,
      type: "Lục Sát",
      isGood: false,
      starName: "Sao Văn Khúc (Thủy)",
      starElement: "Thủy",
      meaning: "Hung tinh chủ về xáo trộn tình cảm, tranh chấp kiện tụng, dễ hao tổn tiền của và bất an gia đạo.",
      advice: "Tránh đặt phòng ngủ hoặc phòng làm việc theo hướng này để giữ tình cảm gia đình vững bền."
    },
    {
      direction: m.nguQuy,
      type: "Ngũ Quỷ",
      isGood: false,
      starName: "Sao Liêm Trinh (Hỏa)",
      starElement: "Hỏa",
      meaning: "Hung tinh chủ về thị phi hỏa hoạn, mất việc làm, tiểu nhân quấy phá, bệnh tật liên miên.",
      advice: "Tuyệt đối không đặt phòng bếp hoặc phòng ngủ theo hướng này. Dùng hướng bếp quay về hướng tốt để trấn áp."
    },
    {
      direction: m.tuyetMenh,
      type: "Tuyệt Mệnh",
      isGood: false,
      starName: "Sao Phá Quân (Kim)",
      starElement: "Kim",
      meaning: "Đại hung tinh nguy hiểm nhất. Chủ về hao tán tài sản lớn, tổn thọ, gia đạo bất an, mệt mỏi.",
      advice: "Kỵ nhất cửa chính, phòng ngủ, phòng làm việc. Nên bố trí nhà vệ sinh hoặc nhà kho để 'lấy hung trấn hung'."
    }
  ];
}

const PERSONALITY_BY_GENDER_DATA: Record<number, Record<"male" | "female", {
  title: string; coreTraits: string; strengths: string; socialStyle: string;
}>> = {
  0: {
    male: {
      title: "Túc Trí Đa Mưu • Tiên Phong Khởi Nghiệp",
      coreTraits: "Nhanh nhẹn, thông minh, khả năng thích ứng tuyệt vời, giỏi quan sát và nắm bắt thời cơ kinh doanh.",
      strengths: "Nhìn xa trông rộng, biết tích lũy của cải, tư duy nhạy bén trước biến động.",
      socialStyle: "Khéo léo, rộng rãi với bạn bè, giỏi mở rộng quan hệ đối tác tin cậy."
    },
    female: {
      title: "Nhu Mì Linh Hoạt • Vượng Phu Ích Tử",
      coreTraits: "Đảm đang, chu đáo, tinh tế, tay hòm chìa khóa giỏi quản lý tài chính gia đình vững vàng.",
      strengths: "Tháo vát, kiên nhẫn, trực giác nhạy bén, luôn là hậu phương vững chắc.",
      socialStyle: "Duyên dáng, hòa nhã, được mọi người trong gia đình và xã hội yêu mến."
    }
  },
  1: {
    male: {
      title: "Cần Kiệm Trì Trọng • Trụ Cột Vững Vàng",
      coreTraits: "Chăm chỉ, thực tế, kiên định như núi, luôn xây dựng sự nghiệp bằng sự bền bỉ vững chắc.",
      strengths: "Đáng tin cậy tuyệt đối, không ngại gian khó, chữ tín luôn đặt lên hàng đầu.",
      socialStyle: "Chân thành, ít nói nhưng làm nhiều, luôn giữ đúng lời hứa."
    },
    female: {
      title: "Hiền Thục Đảm Đang • Điềm Đạm Nhẫn Nại",
      coreTraits: "Cần mẫn, giàu đức hy sinh, biết vun vén tổ ấm và chăm lo gia đình chu toàn.",
      strengths: "Bền chí, chịu thương chịu khó, quản lý chi tiêu tài tình.",
      socialStyle: "Mộc mạc, thật thà, sống chan hòa và luôn sẵn lòng giúp đỡ người khác."
    }
  },
  2: {
    male: {
      title: "Uy Phong Hùng Dũng • Khí Phách Lãnh Đạo",
      coreTraits: "Nhiệt huyết, dũng cảm, quyết đoán, mang tố chất của nhà tiên phong dám đương đầu thử thách.",
      strengths: "Ý chí kiên cường, dám nghĩ dám làm, luôn bảo vệ lẽ phải và người thân yêu.",
      socialStyle: "Hào phóng, khẳng khái, có sức hút tự nhiên với đám đông và cấp dưới."
    },
    female: {
      title: "Bản Lĩnh Độc Lập • Năng Động Hiện Đại",
      coreTraits: "Mạnh mẽ, tự tin, thông minh, không thích dựa dẫm và luôn có chính kiến độc lập.",
      strengths: "Nhiệt tình, sáng tạo, khả năng giải quyết khủng hoảng xuất sắc.",
      socialStyle: "Cởi mở, thẳng thắn, được bạn bè nể phục và mến mộ vì lòng trượng nghĩa."
    }
  },
  3: {
    male: {
      title: "Ôn Hòa Nho Nhã • Trí Tuệ Tinh Tế",
      coreTraits: "Điềm tĩnh, lịch thiệp, mắt thẩm mỹ cao, giỏi đối nhân xử thế và luôn giữ gìn hòa khí.",
      strengths: "Khéo léo ngoại giao, tư duy cẩn trọng, tránh xa rủi ro không cần thiết.",
      socialStyle: "Lịch lãm, nhã nhặn, giao thiệp rộng rãi và luôn có quý nhân giúp đỡ."
    },
    female: {
      title: "Dịu Dàng Đoan Trang • Tinh Tế Khéo Léo",
      coreTraits: "Thanh nhã, chu đáo, giàu lòng trắc ẩn, yêu cái đẹp và có đời sống nội tâm phong phú.",
      strengths: "Thấu hiểu lòng người, khéo léo vun vén hạnh phúc, mắt thẩm mỹ cao.",
      socialStyle: "Duyên dáng, nhẹ nhàng, ai gặp cũng có thiện cảm và quý mến."
    }
  },
  4: {
    male: {
      title: "Hùng Tâm Tráng Khí • Tầm Vóc Vĩ Đại",
      coreTraits: "Khí phách ngút trời, tham vọng lớn, tự tin và luôn khao khát vươn tầm đỉnh cao sự nghiệp.",
      strengths: "Tầm nhìn chiến lược, năng lượng dồi dào, khả năng truyền cảm hứng lớn.",
      socialStyle: "Rộng lượng, nghĩa hiệp, phong thái tự tin thu hút mọi ánh nhìn."
    },
    female: {
      title: "Quý Phái Tự Tin • Tài Sắc Vẹn Toàn",
      coreTraits: "Thông tuệ, kiêu hãnh, năng động, luôn nỗ lực để đạt được thành công tự thân.",
      strengths: "Năng lực lãnh đạo xuất sắc, ý chí tiến thủ, phong thái thanh lịch quý phái.",
      socialStyle: "Cuốn hút, chân thành, quảng giao và luôn tỏa sáng nơi đông người."
    }
  },
  5: {
    male: {
      title: "Thâm Trầm Cơ Trí • Trực Giác Sắc Bén",
      coreTraits: "Sâu sắc, điềm đạm, trực giác chuẩn xác, phong thái bình thản nhưng mưu lược bên trong.",
      strengths: "Tư duy phân tích thấu đáo, bí ẩn cuốn hút, kiên nhẫn chờ thời cơ vàng.",
      socialStyle: "Kín đáo, chọn lọc bạn bè, đã kết thân là hết lòng trung thành."
    },
    female: {
      title: "Quyến Rũ Tinh Tế • Trí Tuệ Sâu Sắc",
      coreTraits: "Sắc sảo, trực giác nhạy cảm phi thường, phong cách thời thượng và giàu sức hút.",
      strengths: "Khéo léo xử lý tình huống khó, biết cách giữ lửa gia đình, độc lập tự chủ.",
      socialStyle: "Bí ẩn, tinh tế, ứng xử thông minh và luôn giữ chừng mực."
    }
  },
  6: {
    male: {
      title: "Khoáng Đạt Tự Do • Tiên Phong Hành Động",
      coreTraits: "Năng động, cởi mở, dồi dào nhiệt huyết, thích xê dịch và đam mê chinh phục mục tiêu mới.",
      strengths: "Dám dấn thân, không ngại đổi mới, tinh thần lạc quan truyền cảm hứng.",
      socialStyle: "Hào hiệp, vui vẻ, nhiều bạn bè bè bạn khắp bốn phương."
    },
    female: {
      title: "Tươi Vui Rạng Rỡ • Độc Lập Nhiệt Thành",
      coreTraits: "Nhanh nhẹn, tháo vát, yêu tự do, tích cực lan tỏa nụ cười và năng lượng sống.",
      strengths: "Học hỏi nhanh, làm việc năng suất cao, không bao giờ chịu khuất phục khó khăn.",
      socialStyle: "Cởi mở, chân thành, hoạt bát và luôn mang lại tiếng cười."
    }
  },
  7: {
    male: {
      title: "Nhân Hậu Lương Thiện • Trầm Tĩnh Vững Vàng",
      coreTraits: "Thân thiện, giàu lòng trắc ẩn, yêu chuộng hòa bình và luôn hướng về gia đình.",
      strengths: "Chu đáo, tỉ mỉ, kiên nhẫn, có năng khiếu nghệ thuật và thẩm mỹ cao.",
      socialStyle: "Hiền hòa, nhường nhịn, luôn biết nghĩ cho người khác."
    },
    female: {
      title: "Dịu Hiền Đôn Hậu • Khéo Léo Vun Vén",
      coreTraits: "Nhân từ, bao dung, giàu tình mẫu tử, luôn tạo ra bầu không khí ấm áp trong tổ ấm.",
      strengths: "Chăm sóc gia đình tuyệt vời, đức hy sinh cao, khéo léo may vá bếp núc.",
      socialStyle: "Nhã nhặn, kín đáo, được họ hàng và bạn bè hết lòng yêu mến."
    }
  },
  8: {
    male: {
      title: "Thông Tuệ Biến Hóa • Đa Tài Ứng Biến",
      coreTraits: "Thông minh, hóm hỉnh, đa tài, xử lý tình huống linh hoạt và giỏi nắm bắt cơ hội.",
      strengths: "Đầu óc kinh doanh nhạy bén, khả năng đàm phán thuyết phục xuất sắc.",
      socialStyle: "Vui vẻ, hóm hỉnh, dễ gần và luôn làm chủ bầu không khí giao tiếp."
    },
    female: {
      title: "Linh Hoạt Sắc Sảo • Tháo Vát Tự Lập",
      coreTraits: "Nhanh trí, hoạt ngôn, đảm việc nước giỏi việc nhà, thích nghi tốt với mọi hoàn cảnh.",
      strengths: "Tư duy sáng tạo, tay hòm chìa khóa đắc lực, tự chủ kinh tế vững vàng.",
      socialStyle: "Duyên dáng, sôi nổi, luôn mang đến sự mới mẻ và cuốn hút."
    }
  },
  9: {
    male: {
      title: "Nguyên Tắc Chu Toàn • Trách Nhiệm Kiên Định",
      coreTraits: "Chính xác, kỷ luật cao, cầu toàn, trung thực và luôn giữ uy tín nghề nghiệp.",
      strengths: "Quản lý công việc khoa học, tỉ mỉ, dám đứng ra chịu trách nhiệm lớn.",
      socialStyle: "Nghiêm túc, thẳng thắn, chân thành và ghét sự dối trá."
    },
    female: {
      title: "Tháo Vát Chăm Chỉ • Tề Gia Nội Trợ",
      coreTraits: "Cần mẫn, chu toàn, gọn gàng ngăn nắp, biết vun vén chi tiêu và chăm lo con cái thành đạt.",
      strengths: "Giỏi việc nhà lẫn việc xã hội, quản lý tài chính chặt chẽ, chu đáo.",
      socialStyle: "Nhiệt tình, chu đáo, hiếu nghĩa vẹn toàn với hai bên gia đình."
    }
  },
  10: {
    male: {
      title: "Trung Nghĩa Trực Trực • Chính Trực Đáng Tin",
      coreTraits: "Trung thành, thẳng thắn, khẳng khái, giàu tinh thần bảo vệ lẽ phải và trách nhiệm cao.",
      strengths: "Đáng tin cậy nhất trong 12 con giáp, kiên định trước cám dỗ, sống trọng tình nghĩa.",
      socialStyle: "Chân thành, ghét thị phi giả tạo, là người bạn tri kỷ suốt đời."
    },
    female: {
      title: "Hiền Lương Trung Hậu • Đảm Đang Thủy Chung",
      coreTraits: "Thủy chung, hiền lành, yêu chuộng công bằng, luôn hết lòng vì người mình yêu thương.",
      strengths: "Trách nhiệm gia đình son sắt, kiên nhẫn, điểm tựa bình yên cho bạn đời.",
      socialStyle: "Chân chất, giản dị, đáng tin cậy và luôn giữ trọn lời hứa."
    }
  },
  11: {
    male: {
      title: "Phúc Hậu An Nhiên • Khoan Dung Độ Lượng",
      coreTraits: "Hòa nhã, rộng rãi, hào phóng, có phúc lộc tự nhiên và phong thái lạc quan yêu đời.",
      strengths: "Tâm hồn rộng mở, không chấp nhặt, hưởng phúc lộc trời ban, cuộc sống an nhiên.",
      socialStyle: "Hào phóng, ấm áp, bạn bè quý mến vì tấm lòng chân thành."
    },
    female: {
      title: "Phúc Khí Dồi Dào • Hiền Thục Đại Cát",
      coreTraits: "Phúc hậu, dịu hiền, may mắn về đường con cái và tài lộc, gia đình luôn hòa thuận ấm no.",
      strengths: "Vượng phu ích tử, tâm tính thiện lương, mang lại may mắn cho gia đình.",
      socialStyle: "Vui vẻ, hòa đồng, bao dung và luôn mang lại cảm giác bình yên."
    }
  }
};

const MARRIAGE_BY_GENDER_DATA: Record<number, Record<"male" | "female", {
  overview: string; advice: string;
}>> = {
  0: {
    male: {
      overview: "Nam tuổi Tý tình cảm nồng nàn, sống có trách nhiệm với vợ con, luôn nỗ lực để gia đình sung túc.",
      advice: "Nên lắng nghe và sẻ chia nhiều hơn, tránh áp đặt suy nghĩ chủ quan lên bạn đời."
    },
    female: {
      overview: "Nữ tuổi Tý khéo léo vun vén, một lòng chăm lo tổ ấm, là chỗ dựa tinh thần tuyệt vời cho chồng.",
      advice: "Giữ tâm lý thoải mái, bớt lo toan vụn vặt để không khí gia đình luôn ngập tràn tiếng cười."
    }
  },
  1: {
    male: {
      overview: "Nam tuổi Sửu chung thủy, đáng tin cậy, coi trọng mái ấm và luôn là trụ cột kiên cường.",
      advice: "Cần học cách thể hiện tình cảm lãng mạn hơn qua lời nói, tránh sự khô khan cứng nhắc."
    },
    female: {
      overview: "Nữ tuổi Sửu giàu đức hy sinh, hiền thục nết na, hết lòng vì chồng con và gia đạo.",
      advice: "Dành thời gian chăm sóc bản thân nhiều hơn, chia sẻ bớt gánh nặng việc nhà cùng bạn đời."
    }
  },
  2: {
    male: {
      overview: "Nam tuổi Dần yêu đương mãnh liệt, luôn muốn bảo bọc che chở nhưng cái tôi khá cao.",
      advice: "Hạ bớt cái tôi, nhường nhịn bạn đời khi tranh cãi để gìn giữ hạnh phúc dài lâu."
    },
    female: {
      overview: "Nữ tuổi Dần độc lập, chung tình, luôn mong muốn bạn đời cùng chí hướng phát triển.",
      advice: "Mềm mỏng đúng lúc, tránh kiểm soát thái quá để người bạn đời cảm thấy thoải mái."
    }
  },
  3: {
    male: {
      overview: "Nam tuổi Mão dịu dàng, chu đáo, rất tâm lý và biết cách làm vui lòng người bạn đời.",
      advice: "Cần quyết đoán hơn trong các việc lớn của gia đình, không nên né tránh mâu thuẫn."
    },
    female: {
      overview: "Nữ tuổi Mão đoan trang, tinh tế, luôn mang lại sự êm ấm, thanh bình cho ngôi nhà.",
      advice: "Tự tin bộc lộ mong muốn của bản thân, không nên giấu kín tâm sự trong lòng."
    }
  },
  4: {
    male: {
      overview: "Nam tuổi Thìn phong độ, hào hiệp, luôn mong muốn đem lại vinh hoa cho gia đình.",
      advice: "Cân bằng thời gian giữa công việc và gia đình, đừng để sự bận rộn làm phai nhạt tình cảm."
    },
    female: {
      overview: "Nữ tuổi Thìn tài sắc, tự tin, luôn đồng hành cùng chồng vượt qua mọi sóng gió.",
      advice: "Dành sự tôn trọng và khích lệ cho bạn đời, tránh so sánh với người khác."
    }
  },
  5: {
    male: {
      overview: "Nam tuổi Tỵ chung tình sâu sắc, bảo vệ gia đình kỹ lưỡng, hết lòng vì người mình yêu.",
      advice: "Tránh tính hay nghi ngờ hoặc giữ trong lòng, cần thẳng thắn trò chuyện thấu hiểu."
    },
    female: {
      overview: "Nữ tuổi Tỵ duyên dáng quyến rũ, biết giữ gìn hạnh phúc và quản lý kinh tế tổ ấm tài tình.",
      advice: "Mở lòng tin tưởng bạn đời nhiều hơn để tình cảm vợ chồng luôn gắn kết keo sơn."
    }
  },
  6: {
    male: {
      overview: "Nam tuổi Ngọ phóng khoáng, vui vẻ, mang lại nhiều năng lượng tích cực cho hôn nhân.",
      advice: "Học cách kiên nhẫn và gắn bó hơn với không gian gia đình, giảm bớt tiệc tùng bên ngoài."
    },
    female: {
      overview: "Nữ tuổi Ngọ đảm đang, tích cực, luôn đồng cam cộng khổ cùng chồng lập nghiệp.",
      advice: "Giữ bình tĩnh khi nóng giận, 'chồng giận thì vợ bớt lời' sẽ giữ êm ấm thuận hòa."
    }
  },
  7: {
    male: {
      overview: "Nam tuổi Mùi ấm áp, nhân hậu, rất thương yêu vợ con và chăm chút cho tổ ấm.",
      advice: "Mạnh mẽ và kiên quyết hơn khi đứng trước các quyết định quan trọng của đời sống."
    },
    female: {
      overview: "Nữ tuổi Mùi hiền dịu, bao dung, tạo nên một mái ấm bình yên, ấm áp và hạnh phúc.",
      advice: "Tránh nhạy cảm quá mức, tin tưởng vào tình cảm chân thành của người bạn đời."
    }
  },
  8: {
    male: {
      overview: "Nam tuổi Thân hài hước, linh hoạt, luôn biết cách làm mới tình yêu và hóa giải căng thẳng.",
      advice: "Nghiêm túc hơn trong các cam kết hôn nhân, tạo sự tin cậy vững bền cho bạn đời."
    },
    female: {
      overview: "Nữ tuổi Thân thông minh, tháo vát, vừa giỏi việc xã hội vừa chu toàn việc nhà.",
      advice: "Lắng nghe tâm tư của bạn đời nhiều hơn, tránh tranh luận thắng thua trong gia đình."
    }
  },
  9: {
    male: {
      overview: "Nam tuổi Dậu chu toàn, ngăn nắp, có tinh thần trách nhiệm kinh tế cao với mái ấm.",
      advice: "Giảm bớt sự cầu toàn và tính chỉ trích vụn vặt, học cách bao dung với khuyết điểm nhỏ."
    },
    female: {
      overview: "Nữ tuổi Dậu hết lòng chăm sóc chồng con, khéo tay quán xuyến việc nhà chu tất.",
      advice: "Tránh cằn nhằn việc nhỏ, tạo không gian thư thái vui vẻ sau giờ làm việc cho cả hai."
    }
  },
  10: {
    male: {
      overview: "Nam tuổi Tuất chung thủy tuyệt đối, luôn bảo bọc che chở và sống rất có trách nhiệm.",
      advice: "Cởi mở chia sẻ cảm xúc nhiều hơn, tránh im lặng chịu đựng một mình."
    },
    female: {
      overview: "Nữ tuổi Tuất son sắt thủy chung, biết đồng cam cộng khổ, là điểm tựa bình yên của gia đình.",
      advice: "Lạc quan hơn trong suy nghĩ, tránh lo lắng thái quá về tương lai chưa tới."
    }
  },
  11: {
    male: {
      overview: "Nam tuổi Hợi bao dung, hiền hòa, không chấp nhặt, đem lại sự an vui và đủ đầy cho vợ con.",
      advice: "Chủ động hơn trong việc vun đắp tình cảm lãng mạn, rèn luyện sức khỏe đều đặn."
    },
    female: {
      overview: "Nữ tuổi Hợi vượng phu ích tử, tâm tính từ hòa, mang lại phúc khí ngập tràn cho ngôi nhà.",
      advice: "Biết cân bằng chi tiêu gia đình, giữ vững tinh thần lạc quan tự tin vốn có."
    }
  }
};

const LIFE_STAGES_BY_GENDER_DATA: Record<number, Record<"male" | "female", LifeStageFortune[]>> = {
  0: {
    male: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Tích Lũy Kinh Nghiệm & Khởi Sự",
        overview: "Thời kỳ nhiều biến động và trải nghiệm, đầu óc nhanh nhạy giúp bạn sớm nắm bắt thời cơ lập nghiệp.",
        careerFinance: "Tài chính lúc đầu thăng trầm, có những bài học quý báu về đầu tư và chọn đối tác.",
        familyLove: "Đường tình duyên có đôi chút lận đận ban đầu, sau 26 tuổi dần ổn định."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Gây Dựng Cơ Đồ & Phát Đạt",
        overview: "Giai đoạn hoàng kim nở rộ, kinh nghiệm dày dặn kết hợp mưu lược giúp bạn tạo dựng sự nghiệp vững chắc.",
        careerFinance: "Công danh thăng tiến vượt bậc, tài lộc dồi dào, tích lũy được điền sản lớn.",
        familyLove: "Gia đình yên ấm, vợ chồng đồng lòng phát triển kinh tế, con cái ngoan ngoãn."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Hưởng Phúc An Nhàn & Đức Lưu Quang",
        overview: "Cuộc sống viên mãn, thảnh thơi hưởng thành quả lao động cả đời, con cháu thành danh.",
        careerFinance: "Tài sản dồi dào, kinh tế vững chãi, chuyển giao kinh nghiệm cho thế hệ sau.",
        familyLove: "Gia đạo đề huề, con cháu sum vầy hiếu thảo, tâm an trí sáng."
      }
    ],
    female: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Học Hỏi & Khẳng Định Bản Thân",
        overview: "Tuổi trẻ chăm chỉ, tháo vát, sớm tự lập và có định hướng công việc rõ ràng.",
        careerFinance: "Thu nhập ổn định, biết chi tiêu tiết kiệm và tích lũy thông minh.",
        familyLove: "Được nhiều người theo đuổi, tình duyên tươi đẹp, sớm tìm được bến đỗ bình yên."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Vượng Phu Ích Tử & Sung Túc",
        overview: "Thời kỳ đỉnh cao của tài lộc và hạnh phúc, gia đình và sự nghiệp đều thăng hoa rực rỡ.",
        careerFinance: "Tay hòm chìa khóa tài tình, kinh tế gia đình ngày một phát đạt hưng vượng.",
        familyLove: "Chồng thành đạt, con cái chăm ngoan đỗ đạt, gia đạo luôn ấm êm hòa thuận."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Phúc Lộc Viên Mãn & An Nhiên",
        overview: "Tuổi già thảnh thơi, không phải lo âu về vật chất, hưởng trọn phúc đức trời ban.",
        careerFinance: "Điền sản sung túc, của ăn của để dồi dào.",
        familyLove: "Con cháu thành đạt hiếu thảo, gia tộc rạng rỡ danh tiếng."
      }
    ]
  }
};

// Generic fallback generator for life stages for other animals
function getLifeStagesForAnimal(chiIdx: number, gender: "male" | "female"): LifeStageFortune[] {
  if (LIFE_STAGES_BY_GENDER_DATA[chiIdx]?.[gender]) {
    return LIFE_STAGES_BY_GENDER_DATA[chiIdx][gender];
  }
  const isMale = gender === "male";
  return [
    {
      stage: "Tiền vận",
      ageRange: "Dưới 30 tuổi",
      title: isMale ? "Xông Pha Trải Nghiệm & Lập Thân" : "Rèn Giũa Tài Năng & Khởi Đầu Thuận Lợi",
      overview: "Thời kỳ học hỏi, tích lũy vốn sống và đối mặt với thử thách để tôi luyện bản lĩnh vững vàng.",
      careerFinance: "Tài chính bước đầu gây dựng, có lúc lên bổng xuống trầm nhưng tôi luyện ý chí kiên định.",
      familyLove: "Tình cảm tuổi trẻ nồng nhiệt, qua vài mối duyên sẽ tìm thấy người bạn đời đích thực."
    },
    {
      stage: "Trung vận",
      ageRange: "31 - 50 tuổi",
      title: isMale ? "Đại Vận Hanh Thông & Khẳng Định Vị Thế" : "Gia Đạo Hưng Vượng & Sự Nghiệp Viên Mãn",
      overview: "Giai đoạn hoàng kim rực rỡ nhất trong cuộc đời, thiên thời địa lợi nhân hòa đều tụ hội.",
      careerFinance: "Công việc kinh doanh thăng tiến, nắm giữ trọng trách, tiền tài tích lũy vững chãi.",
      familyLove: "Vợ chồng hòa hợp đồng tâm, con cái học hành đỗ đạt, gia đạo luôn tràn đầy tiếng cười."
    },
    {
      stage: "Hậu vận",
      ageRange: "Từ 51 tuổi trở đi",
      title: "An Nhàn Hưởng Phúc & Di Sản Trường Tồn",
      overview: "Cuộc sống an nhiên tự tại, hưởng thụ thành quả sự nghiệp, phúc lộc trường tồn.",
      careerFinance: "Của cải điền sản vững vàng, sống dư dả không bận tâm gánh nặng cơm áo gạo tiền.",
      familyLove: "Con cháu vinh hiển, gia tộc hưng thịnh, tinh thần thanh thản bình an."
    }
  ];
}

export function getSaoHan2026(lunarAge: number, gender: "male" | "female"): SaoHan2026Info {
  const saoNam = [
    { name: "La Hầu", type: "Hung tinh" as const, desc: "Chủ về khẩu thiệt thị phi, hao tài tốn của, bệnh mắt tai, cần đặc biệt lưu ý tháng Giêng và tháng 7 Âm lịch." },
    { name: "Thổ Tú", type: "Trung tinh" as const, desc: "Khởi đầu có chút trắc trở gian nan nhưng hậu vận hanh thông. Cần giữ gia đạo hòa thuận, kỵ tháng 4 và 8 Âm lịch." },
    { name: "Thủy Diệu", type: "Cát tinh" as const, desc: "Cát tinh chủ về tài lộc hưng thịnh, có hỷ sự bất ngờ, song cần chú ý khi đi lại vùng sông nước, cẩn trọng tháng 4 và 8." },
    { name: "Thái Bạch", type: "Hung tinh" as const, desc: "Hung tinh hao tài nặng nhất ('Thái Bạch quét sạch cửa nhà'). Cần giữ gìn tài chính chặt chẽ, tránh vay mượn mạo hiểm, kỵ tháng 5." },
    { name: "Thái Dương", type: "Cát tinh" as const, desc: "Đại cát tinh rực rỡ nhất cho Nam mạng. Vượng khí công danh thăng tiến, kinh doanh bội thu, may mắn đỉnh điểm tháng 6 và 10." },
    { name: "Vân Hớn", type: "Trung tinh" as const, desc: "Vận thế bình hòa, tránh nóng nảy và bảo thủ, giữ gìn lời ăn tiếng nói nơi công sở để tránh thị phi, cẩn trọng tháng 2 và 8." },
    { name: "Kế Đô", type: "Hung tinh" as const, desc: "Ám muội, buồn phiền, hao tài nhẹ, cần giữ tâm thái bình an và làm nhiều việc thiện tích đức, kỵ tháng 3 và 9." },
    { name: "Thái Âm", type: "Cát tinh" as const, desc: "Cát tinh chủ về danh lợi song toàn, được quý nhân nâng đỡ, toại nguyện ước mơ cầu tài, tháng 9 Âm lịch vượng phát nhất." },
    { name: "Mộc Đức", type: "Cát tinh" as const, desc: "Cát tinh may mắn về hôn nhân, tài lộc phát đạt, được bạn bè đối tác tương trợ, tháng Chạp (tháng 12) đắc tài đắc lộc." }
  ];

  const saoNu = [
    { name: "Kế Đô", type: "Hung tinh" as const, desc: "Hung tinh lớn đối với Nữ mạng, dễ vướng thị phi tình cảm, bệnh vặt, cần giữ tâm an tịnh, cẩn trọng tháng 3 và tháng 9 Âm lịch." },
    { name: "Vân Hớn", type: "Trung tinh" as const, desc: "Vận thế trung bình, cần chú ý sức khỏe sinh sản, việc bếp núc lửa nước, tránh khẩu thiệt tranh cãi, kỵ tháng 2 và 8." },
    { name: "Mộc Đức", type: "Cát tinh" as const, desc: "Cát tinh mang lại an vui, gia đạo hòa thuận êm ấm, có tin vui về con cái hoặc sự nghiệp, đặc biệt vượng tháng 10 và 12." },
    { name: "Thái Âm", type: "Cát tinh" as const, desc: "Đệ nhất cát tinh cho Nữ mạng, tài chính dồi dào, tình cảm thăng hoa, mưu sự lớn dễ thành công rực rỡ, tháng 9 Âm lịch may mắn nhất." },
    { name: "Thổ Tú", type: "Trung tinh" as const, desc: "Có chút trăn trở lo toan trong nội tâm, xuất hành cần cẩn trọng và giữ gìn hòa khí gia đình, kỵ tháng 4 và 8." },
    { name: "La Hầu", type: "Hung tinh" as const, desc: "Dễ gặp chuyện phiền muộn, cần đề phòng thị phi nói xấu sau lưng, giữ gìn sức khỏe tai mắt, kỵ tháng Giêng và tháng 7." },
    { name: "Thái Dương", type: "Cát tinh" as const, desc: "Cát tinh mang lại sự tự tin, tháo vát, công việc làm ăn buôn bán hanh thông, gia đạo bình an, vượng tháng 6 và 10." },
    { name: "Thái Bạch", type: "Hung tinh" as const, desc: "Hạn hao tài tốn của, tránh cho vay mượn hoặc đầu tư rủi ro cao, chú ý sức khỏe đường hô hấp, kỵ tháng 5 Âm lịch." },
    { name: "Thủy Diệu", type: "Cát tinh" as const, desc: "Cát tinh chủ về hỷ khí, tài lộc dồi dào, tình duyên êm đẹp, tuy nhiên tránh đi sông biển xa vào tháng 4 và 8." }
  ];

  const saoIdx = ((lunarAge - 10) % 9 + 9) % 9;
  const sao = gender === "male" ? saoNam[saoIdx] : saoNu[saoIdx];

  const hanList = [
    { name: "Huỳnh Tuyền", desc: "Hạn chủ về sức khỏe, kỵ đi lại đường sông nước, đề phòng bệnh đau đầu chóng mặt." },
    { name: "Tam Kheo", desc: "Hạn chủ về tay chân xương khớp, đau mắt, cần chú ý khi vận động thể thao hay đi lại ban đêm." },
    { name: "Ngũ Thổ (Ngũ Hộ)", desc: "Hạn về thị phi, tâm lý lo âu bất an, tránh tin người nhẹ dạ hay bảo lãnh người khác." },
    { name: "Thiên Tinh", desc: "Hạn chủ về ăn uống, ngộ độc thực phẩm, đề phòng kiện tụng hay tai bay vạ gió." },
    { name: "Toán Tận", desc: "Đại hạn về tiền bạc, cẩn trọng cướp giật, mất mát tài sản bất ngờ, không nên mang theo nhiều tiền bạc." },
    { name: "Thiên La", desc: "Hạn tâm lý, mất ngủ, áp lực tinh thần, cần giữ hòa khí vợ chồng tránh mâu thuẫn lục đục." },
    { name: "Địa Võng", desc: "Đề phòng tiểu nhân ghen ghét vu oan, tránh đi xa vào ban đêm hay dính líu việc bao đồng." },
    { name: "Diêm Vương", desc: "Kỵ phụ nữ ốm đau hoặc sinh nở, nhưng lại có lộc lớn cho người làm ăn kinh doanh buôn bán." }
  ];

  const hanIdx = ((lunarAge - 18) % 8 + 8) % 8;
  const han = hanList[hanIdx];

  const isGoodSao = sao.type === "Cát tinh";
  const yearOverview = `Năm 2026 (Bính Ngọ), tuổi mụ của bạn là ${lunarAge} tuổi. Bạn được chiếu mệnh bởi sao ${sao.name} (${sao.type}) và gặp hạn niên ${han.name}. ${isGoodSao ? "Vận trình năm nay có nhiều cát khí hanh thông, công việc và tài chính có nhiều bước tiến thuận lợi nếu biết nắm bắt thời cơ." : "Năm nay cần chú trọng dĩ hòa vi quý, thận trọng trong việc đầu tư lớn và chăm sóc sức khỏe gia đình để hóa nguy thành an."}`;

  const favorableMonths = isGoodSao ? [1, 4, 6, 8, 10, 12] : [2, 6, 10];
  const cautionMonths = sao.name === "Thái Bạch" ? [5] :
                        sao.name === "La Hầu" ? [1, 7] :
                        sao.name === "Kế Đô" ? [3, 9] :
                        sao.name === "Thổ Tú" ? [4, 8] : [5, 7];

  return {
    lunarAge,
    sao: sao.name,
    saoType: sao.type,
    saoDescription: sao.desc,
    han: han.name,
    hanDescription: han.desc,
    yearOverview,
    favorableMonths,
    cautionMonths
  };
}

export function getDetailedBirthFortune(profile: UserBirthProfile): DetailedBirthFortune {
  let solarDay = profile.day;
  let solarMonth = profile.month;
  let solarYear = profile.year;

  if (profile.isLunar) {
    const solarRes = convertLunarToSolar(profile.day, profile.month, profile.year, false);
    if (solarRes) {
      solarDay = solarRes.day;
      solarMonth = solarRes.month;
      solarYear = solarRes.year;
    }
  }

  const lunarDate = getLunarDate(solarDay, solarMonth, solarYear);
  const personalProfile = getPersonalProfile(profile.year);
  const isMale = profile.gender === "male";
  const cungPhi = isMale ? personalProfile.cungMenh.menhNam : personalProfile.cungMenh.menhNu;
  const cungPhiGroup = isMale ? personalProfile.cungMenh.nhomNam : personalProfile.cungMenh.nhomNu;
  const cungPhiElement = cungPhi.includes("Thủy") ? "Thủy" :
                         cungPhi.includes("Hỏa") ? "Hỏa" :
                         cungPhi.includes("Kim") ? "Kim" :
                         cungPhi.includes("Mộc") ? "Mộc" : "Thổ";

  const batTrachDirections = getBatTrachDetails(cungPhi);
  const westernZodiac = getWesternZodiac(solarDay, solarMonth);
  const numerology = calculateLifePathNumber(solarDay, solarMonth, solarYear);

  const chiIdx = personalProfile.chiIndex;
  const personality = PERSONALITY_BY_GENDER_DATA[chiIdx]?.[profile.gender] || {
    title: isMale ? "Quân Tử Trực Trọng" : "Thục Nữ Chu Toàn",
    coreTraits: "Thông minh, đĩnh đạc, có tinh thần cầu tiến và ý chí phấn đấu mạnh mẽ.",
    strengths: "Kiên nhẫn, chân thành, có tinh thần trách nhiệm cao đối với gia đình và xã hội.",
    socialStyle: "Dễ gần, biết lắng nghe và luôn nhận được sự tôn trọng từ bạn bè, đồng nghiệp."
  };

  const marriageData = MARRIAGE_BY_GENDER_DATA[chiIdx]?.[profile.gender];
  const loveMarriage = {
    overview: marriageData?.overview || "Tình duyên êm đẹp, đời sống hôn nhân viên mãn nếu biết thấu hiểu và sẻ chia cùng bạn đời.",
    bestAges: personalProfile.tamHop.concat([personalProfile.lucHop]),
    challengingAges: personalProfile.tuHanhXung,
    advice: marriageData?.advice || "Dĩ hòa vi quý, tôn trọng không gian riêng của người bạn đời sẽ giúp mái ấm mãi đong đầy hạnh phúc."
  };

  const lifeStages = getLifeStagesForAnimal(chiIdx, profile.gender);
  const lunarAge = 2026 - profile.year + 1;
  const saoHan2026 = getSaoHan2026(lunarAge, profile.gender);

  return {
    profile,
    solarDate: { day: solarDay, month: solarMonth, year: solarYear },
    lunarDate,
    personalProfile,
    cungPhi,
    cungPhiGroup,
    cungPhiElement,
    batTrachDirections,
    westernZodiac,
    numerology,
    personalityByGender: personality,
    loveMarriage,
    lifeStages,
    saoHan2026
  };
}
