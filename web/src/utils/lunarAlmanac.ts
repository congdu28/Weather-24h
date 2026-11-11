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

export function elementLabel(el: FiveElement): string {
  return FIVE_ELEMENT_META[el]?.label || el;
}

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
  // Tổng các chữ số của năm sinh chia 9 lấy dư (0 tương ứng với 9)
  const rem = ((year % 9) + 9) % 9;
  const digitSum = rem === 0 ? 9 : rem;

  let cungNamIdx = (11 - digitSum) % 9 || 9;
  let cungNuIdx = (digitSum + 4) % 9 || 9;

  // Quy ước Bát Trạch Trung Cung (số 5): Nam quy về Khôn (2), Nữ quy về Cấn (8)
  if (cungNamIdx === 5) cungNamIdx = 2;
  if (cungNuIdx === 5) cungNuIdx = 8;

  const isDongTu = (cung: number) => [1, 9, 3, 4].includes(cung);

  const huongByCung: Record<number, string[]> = {
    1: ["Đông Nam (Sinh Khí)", "Chính Đông (Thiên Y)", "Chính Nam (Diên Niên)", "Chính Bắc (Phục Vị)"],
    2: ["Đông Bắc (Sinh Khí)", "Chính Tây (Thiên Y)", "Tây Bắc (Diên Niên)", "Tây Nam (Phục Vị)"],
    3: ["Chính Nam (Sinh Khí)", "Chính Bắc (Thiên Y)", "Đông Nam (Diên Niên)", "Chính Đông (Phục Vị)"],
    4: ["Chính Bắc (Sinh Khí)", "Chính Nam (Thiên Y)", "Chính Đông (Diên Niên)", "Đông Nam (Phục Vị)"],
    6: ["Chính Tây (Sinh Khí)", "Đông Bắc (Thiên Y)", "Tây Nam (Diên Niên)", "Tây Bắc (Phục Vị)"],
    7: ["Tây Bắc (Sinh Khí)", "Tây Nam (Thiên Y)", "Đông Bắc (Diên Niên)", "Chính Tây (Phục Vị)"],
    8: ["Tây Nam (Sinh Khí)", "Tây Bắc (Thiên Y)", "Chính Tây (Diên Niên)", "Đông Bắc (Phục Vị)"],
    9: ["Chính Đông (Sinh Khí)", "Đông Nam (Thiên Y)", "Chính Bắc (Diên Niên)", "Chính Nam (Phục Vị)"]
  };

  return {
    menhNam: CUNG_NAMES[cungNamIdx] || "Càn (Kim)",
    menhNu: CUNG_NAMES[cungNuIdx] || "Ly (Hỏa)",
    nhomNam: isDongTu(cungNamIdx) ? "Đông Tứ Mệnh" : "Tây Tứ Mệnh",
    nhomNu: isDongTu(cungNuIdx) ? "Đông Tứ Mệnh" : "Tây Tứ Mệnh",
    huongTotNam: huongByCung[cungNamIdx] || huongByCung[6],
    huongTotNu: huongByCung[cungNuIdx] || huongByCung[9]
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
    yinYangLabel: isYang
      ? `Dương mệnh (${CAN[canIdx]} • Hành ${elementLabel(elementOfCan(canIdx))})`
      : `Âm mệnh (${CAN[canIdx]} • Hành ${elementLabel(elementOfCan(canIdx))})`,
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
  const dayCanElLabel = elementLabel(dayCanEl);
  const userCanElLabel = elementLabel(userCanEl);

  if (isCanHarmony(dayCan, userCan)) {
    points += 2;
    notes.push(`✅ Thiên can ngũ hợp: Can ngày ${CAN[dayCan]} tương hợp với can tuổi ${CAN[userCan]}.`);
  } else if (isCanClash(dayCan, userCan)) {
    points -= 1;
    notes.push(`⚠️ Thiên can tương xung: Can ngày ${CAN[dayCan]} xung can tuổi ${CAN[userCan]}.`);
  } else if (elementGenerates(dayCanEl, userCanEl)) {
    points += 1;
    notes.push(`✅ Thiên can tương sinh: Can ngày ${CAN[dayCan]} (${dayCanElLabel}) sinh dưỡng can tuổi ${CAN[userCan]} (${userCanElLabel}).`);
  } else if (elementOvercomes(dayCanEl, userCanEl) && !isClash(userChi, dayChi)) {
    points -= 1;
    notes.push(`⚠️ Thiên can tương khắc: Can ngày ${CAN[dayCan]} (${dayCanElLabel}) khắc can tuổi ${CAN[userCan]} (${userCanElLabel}).`);
  }

  // 3. Ngũ Hành Nạp Âm
  const dayEl = almanac.napAmElement;
  const userEl = profile.element;
  const dayElLabel = elementLabel(dayEl);
  const userElLabel = elementLabel(userEl);

  if (elementGenerates(dayEl, userEl)) {
    points += 3;
    notes.push(`✅ Ngũ hành nạp âm: Ngày ${almanac.napAm} (${dayElLabel}) tương sinh bản mệnh ${profile.napAm} (${userElLabel}) – vượng khí đại cát.`);
  } else if (dayEl === userEl) {
    points += 1;
    notes.push(`✅ Ngũ hành nạp âm: Ngày ${almanac.napAm} tỷ hòa cùng hành ${userElLabel} với bản mệnh – thuận hòa vững bền.`);
  } else if (elementOvercomes(userEl, dayEl)) {
    points += 1;
    notes.push(`✅ Ngũ hành nạp âm: Bản mệnh ${userElLabel} (${profile.napAm}) chế khắc được hành ngày ${dayElLabel} (${almanac.napAm}) – nắm thế chủ động, đắc tài lộc.`);
  } else if (elementOvercomes(dayEl, userEl)) {
    points -= 3;
    notes.push(`⚠️ Ngũ hành nạp âm: Ngày ${almanac.napAm} (${dayElLabel}) khắc bản mệnh ${profile.napAm} (${userElLabel}) – dễ hao tổn nguyên khí, nên thận trọng.`);
  } else {
    notes.push(`ℹ️ Ngũ hành nạp âm: Bản mệnh ${userElLabel} (${profile.napAm}) sinh xuất cho hành ngày ${dayElLabel} (${almanac.napAm}) – hao khí nhẹ, nên giữ sức khỏe.`);
  }

  // 4. Âm Dương
  const isYangDay = ((jd + 9) % 10) % 2 === 0;
  if (profile.isYang !== isYangDay) {
    notes.push(`☯️ Âm Dương giao hòa: Tuổi ${profile.isYang ? "Dương" : "Âm"} gặp ngày ${isYangDay ? "Dương" : "Âm"} – cân bằng cát lợi.`);
  }

  const directClash = isClash(userChi, dayChi);
  const directHarm = isHarm(userChi, dayChi);
  const directPunish = isPunish(userChi, dayChi);

  let verdict = ActivityVerdict.NEUTRAL;
  if (directClash || points <= -3) {
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
    badgeColor = "rose";
    luckyAdvice = "Nên làm việc thiện, an dưỡng tinh thần, hoàn thành các công việc thường nhật dang dở.";
    if (directClash) {
      badgeLabel = `Xung tuổi (${points > 0 ? `+${points}` : points})`;
      cautionAdvice = `Ngày ${CHI[dayChi]} lục xung trực diện với tuổi ${CHI[userChi]} của gia chủ. Tránh xuất tiền lớn, ký hợp đồng kinh tế, cưới hỏi hay khởi sự việc trọng đại.`;
    } else if (directHarm) {
      badgeLabel = `Phạm Lục hại (${points})`;
      cautionAdvice = `Ngày ${CHI[dayChi]} tương hại (lục hại) với tuổi ${CHI[userChi]} kết hợp can/mệnh tương khắc. Đề phòng tiểu nhân thị phi, tránh cho vay mượn hoặc tranh cãi.`;
    } else if (directPunish) {
      badgeLabel = `Phạm Tương hình (${points})`;
      cautionAdvice = `Ngày ${CHI[dayChi]} phạm hình với tuổi ${CHI[userChi]} kèm ngũ hành bất lợi. Cần khiêm nhường, cẩn trọng giấy tờ pháp lý và đi lại.`;
    } else {
      badgeLabel = `Khắc mệnh (${points})`;
      cautionAdvice = `Ngũ hành ngày (${almanac.napAm}) khắc chế bản mệnh (${profile.napAm}). Hạn chế đầu tư mạo hiểm, chú ý giữ gìn sức khỏe.`;
    }
  } else if (points >= 5) {
    badgeLabel = `Đại hợp (+${points})`;
    badgeColor = "emerald";
    luckyAdvice = "Thời cơ đại cát hanh thông! Rất thuận lợi để xuất hành, gặp gỡ đối tác, ký kết, mở rộng kinh doanh hay cầu tài.";
    cautionAdvice = "Tránh tự phụ chủ quan, hãy tận dụng cát khí để giải quyết dứt điểm các kế hoạch quan trọng.";
  } else if (points > 0) {
    badgeLabel = `Hợp tuổi (+${points})`;
    badgeColor = "emerald";
    luckyAdvice = "Ngày sinh vượng cát tường. Tiến hành công việc làm ăn, trao đổi hợp tác hoặc đi lại đều gặp may mắn.";
    cautionAdvice = "Hạn chế nhận lời hứa hẹn quá sức và chú ý giờ xuất hành để tối ưu tài lộc.";
  } else if (points < 0) {
    badgeColor = "orange";
    luckyAdvice = "Tập trung giải quyết các việc nội bộ, củng cố gia đạo và chuẩn bị kế hoạch.";
    if (directHarm) {
      badgeLabel = `Lục hại nhẹ (${points})`;
      cautionAdvice = `Địa chi ngày ${CHI[dayChi]} lục hại với tuổi ${CHI[userChi]}, dù có yếu tố cát hỗ trợ nhưng vẫn nên thận trọng lời ăn tiếng nói.`;
    } else if (directPunish) {
      badgeLabel = `Tương hình nhẹ (${points})`;
      cautionAdvice = `Địa chi ngày ${CHI[dayChi]} tương hình với tuổi ${CHI[userChi]}, tránh nóng vội quyết định việc lớn.`;
    } else {
      badgeLabel = `Hơi khắc (${points})`;
      cautionAdvice = "Hạn chế quyết định vội vàng, thận trọng lời ăn tiếng nói nơi công sở.";
    }
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
  /** Chỉ số Canh Giờ Âm lịch (0 = Tý 23h-1h ... 11 = Hợi 21h-23h). undefined hoặc < 0 là chưa chọn giờ sinh */
  birthHour?: number;
}

export interface BirthHourOption {
  chiIndex: number;
  chi: string;
  range: string;
  periodLabel: string;
  emoji: string;
}

export const BIRTH_HOUR_OPTIONS: BirthHourOption[] = [
  { chiIndex: 0, chi: "Tý", range: "23:00 - 00:59", periodLabel: "Đêm khuya (Canh Ba)", emoji: "🐀" },
  { chiIndex: 1, chi: "Sửu", range: "01:00 - 02:59", periodLabel: "Rạng sáng (Canh Tư)", emoji: "🐂" },
  { chiIndex: 2, chi: "Dần", range: "03:00 - 04:59", periodLabel: "Tinh mơ (Canh Năm)", emoji: "🐅" },
  { chiIndex: 3, chi: "Mão", range: "05:00 - 06:59", periodLabel: "Bình minh (Mặt trời mọc)", emoji: "🐈" },
  { chiIndex: 4, chi: "Thìn", range: "07:00 - 08:59", periodLabel: "Buổi sáng sớm", emoji: "🐉" },
  { chiIndex: 5, chi: "Tỵ", range: "09:00 - 10:59", periodLabel: "Giữa buổi sáng", emoji: "🐍" },
  { chiIndex: 6, chi: "Ngọ", range: "11:00 - 12:59", periodLabel: "Giữa trưa (Chính Ngọ)", emoji: "🐎" },
  { chiIndex: 7, chi: "Mùi", range: "13:00 - 14:59", periodLabel: "Đầu giờ chiều", emoji: "🐐" },
  { chiIndex: 8, chi: "Thân", range: "15:00 - 16:59", periodLabel: "Xế chiều", emoji: "🐒" },
  { chiIndex: 9, chi: "Dậu", range: "17:00 - 18:59", periodLabel: "Hoàng hôn (Chạng vạng)", emoji: "🐓" },
  { chiIndex: 10, chi: "Tuất", range: "19:00 - 20:59", periodLabel: "Buổi tối (Canh Đầu)", emoji: "🐕" },
  { chiIndex: 11, chi: "Hợi", range: "21:00 - 22:59", periodLabel: "Đêm muộn (Canh Hai)", emoji: "🐖" }
];

export interface PillarDetail {
  pillarName: string;
  subTitle: string;
  canIndex: number;
  chiIndex: number;
  can: string;
  chi: string;
  canChi: string;
  canElement: FiveElement;
  chiElement: FiveElement;
  napAm: string;
  napAmElement: FiveElement;
  meaning: string;
}

export interface BirthHourFortune {
  hasBirthHour: boolean;
  hourChiIndex: number;
  hourChi: string;
  hourRange: string;
  hourPeriodLabel: string;
  hourCanChi: string;
  hourNapAm: string;
  hourNapAmElement: FiveElement;
  isHoangDaoHour: boolean;
  lyThuanPhong: LyThuanPhongInfo;
  destinyTitle: string;
  coreOverview: string;
  personalityTrait: string;
  careerWealth: string;
  childrenAndLateLife: string;
}

export interface FourPillarsBatTu {
  yearPillar: PillarDetail;
  monthPillar: PillarDetail;
  dayPillar: PillarDetail;
  hourPillar: PillarDetail | null;
  nhatChuCan: string;
  nhatChuElement: FiveElement;
  nhatChuStrength: "Thân Vượng" | "Thân Trung Hòa" | "Thân Nhược";
  nhatChuDescription: string;
  elementCounts: Record<FiveElement, number>;
  dominantElements: FiveElement[];
  lackingElements: FiveElement[];
  hyDungThan: FiveElement[];
  kyThan: FiveElement[];
  dungThanAdvice: string;
}

export interface DeepFortuneAspect {
  id: string;
  title: string;
  badge: string;
  icon: string;
  summary: string;
  details: string[];
  actionableAdvice: string;
}

export interface DeepFortuneAnalysis {
  modelVersion: string;
  thinkingMode: string;
  overallHeadline: string;
  overallScore: number;
  executiveSummary: string;
  aspects: DeepFortuneAspect[];
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
  fourPillars: FourPillarsBatTu;
  birthHourFortune: BirthHourFortune;
  deepAnalysis: DeepFortuneAnalysis;
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
  1: {
    title: "Người Tiên Phong Độc Lập & Dẫn Đầu Xu Thế",
    keywords: ["Tiên phong", "Độc lập", "Quyết đoán", "Lãnh đạo"],
    description: "Số 1 đại diện cho tinh thần thủ lĩnh bẩm sinh và ý chí tự lập mạnh mẽ. Bạn luôn dám mở lối đi riêng, kiên định với mục tiêu và có năng lực dẫn dắt tập thể vượt qua nghịch cảnh.",
    strengths: ["Tính tự chủ và bản lĩnh dẫn đầu cao", "Quyết đoán, dám nghĩ dám làm", "Sáng tạo độc bản, không đi theo lối mòn", "Ý chí vươn lên mạnh mẽ"],
    challenges: ["Cái tôi lớn, đôi khi bảo thủ", "Dễ ôm đồm công việc một mình", "Cần học cách lắng nghe và cộng tác mềm mỏng"],
    idealCareers: ["Nhà sáng lập, giám đốc điều hành (CEO)", "Quản lý dự án, trưởng nhóm đổi mới", "Kinh doanh tự do, chuyên gia tư vấn", "Chỉ huy, thể thao chuyên nghiệp"]
  },
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
  },
  33: {
    title: "Bậc Thầy Chữa Lành & Tình Yêu Vô Điều Kiện (Master Teacher)",
    keywords: ["Chữa lành", "Thấu cảm", "Giáo dưỡng", "Từ bi"],
    description: "Số 33/6 là Master Number của lòng từ bi và trí tuệ nâng đỡ tinh thần. Bạn lan tỏa nguồn năng lượng ấm áp, truyền cảm hứng hướng thiện và có khả năng chữa lành tâm hồn sâu sắc.",
    strengths: ["Trái tim nhân hậu và lòng vị tha hiếm có", "Khả năng truyền đạt, dẫn dắt tinh thần xuất sắc", "Trực giác nghệ thuật và thẩm mỹ tinh tế", "Điểm tựa bình yên cho gia đình và cộng đồng"],
    challenges: ["Dễ đặt gánh nặng cảm xúc của người khác lên vai", "Đôi khi quên chăm sóc nhu cầu bản thân", "Cần thiết lập ranh giới năng lượng lành mạnh"],
    idealCareers: ["Nhà giáo dục, diễn giả truyền cảm hứng", "Bác sĩ, chuyên gia tâm lý trị liệu", "Lãnh đạo tổ chức nhân đạo, xã hội", "Nghệ thuật chữa lành, sáng tác"]
  }
};

export function calculateLifePathNumber(day: number, month: number, year: number): NumerologyInfo {
  function sumDigits(n: number): number {
    return String(Math.abs(n)).split("").reduce((acc, c) => acc + (parseInt(c, 10) || 0), 0);
  }
  function reduceComponent(n: number): number {
    let s = n;
    while (s > 9 && s !== 11 && s !== 22) {
      s = sumDigits(s);
    }
    return s;
  }
  const rd = reduceComponent(day);
  const rm = reduceComponent(month);
  const ry = reduceComponent(year);
  let total = rd + rm + ry;

  // Chuẩn trường phái Pythagoras Việt Nam: giữ số 10, 11, 22, 33 ở bước cộng tổng cuối
  while (total > 9 && total !== 10 && total !== 11 && total !== 22 && total !== 33) {
    total = sumDigits(total);
  }

  const fallback = NUMEROLOGY_DATA[10];
  const data = NUMEROLOGY_DATA[total] || fallback;

  return {
    lifePathNumber: total,
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
        overview: "Thời kỳ nhiều biến động và trải nghiệm, đầu óc nhanh nhạy giúp Nam tuổi Tý sớm nắm bắt thời cơ lập nghiệp.",
        careerFinance: "Tài chính lúc đầu thăng trầm, có những bài học quý báu về đầu tư và chọn đối tác.",
        familyLove: "Đường tình duyên có đôi chút thử thách ban đầu, từ sau 26 tuổi dần ổn định ấm êm."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Gây Dựng Cơ Đồ & Phát Đạt",
        overview: "Giai đoạn hoàng kim nở rộ, kinh nghiệm dày dặn kết hợp mưu lược giúp tạo dựng sự nghiệp vững chắc.",
        careerFinance: "Công danh thăng tiến vượt bậc, tài lộc dồi dào, tích lũy được điền sản lớn.",
        familyLove: "Gia đình yên ấm, vợ chồng đồng lòng phát triển kinh tế, con cái ngoan ngoãn."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Hưởng Phúc An Nhàn & Đức Lưu Quang",
        overview: "Cuộc sống viên mãn, thảnh thơi hưởng thành quả lao động cả đời, con cháu thành danh.",
        careerFinance: "Tài sản dồi dào, kinh tế vững chãi, chuyển giao cơ nghiệp cho thế hệ sau.",
        familyLove: "Gia đạo đề huề, con cháu sum vầy hiếu thảo, tâm an trí sáng."
      }
    ],
    female: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Học Hỏi & Khẳng Định Bản Thân",
        overview: "Tuổi trẻ chăm chỉ, tháo vát, Nữ tuổi Tý sớm tự lập và có định hướng công việc rõ ràng.",
        careerFinance: "Thu nhập ổn định, biết chi tiêu tiết kiệm và tích lũy thông minh.",
        familyLove: "Duyên dáng khéo léo, được nhiều người quý mến, sớm tìm được bến đỗ bình yên."
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
  },
  1: {
    male: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Bền Bỉ Rèn Chí & Xây Nền Móng",
        overview: "Nam tuổi Sửu đi lên bằng thực lực và sự kiên trì, tuổi trẻ vất vả sớm hôm nhưng nền tảng cực kỳ vững chắc.",
        careerFinance: "Tích tiểu thành đại, không thích đầu cơ mạo hiểm nên tài chính tăng trưởng đều đặn.",
        familyLove: "Chân thành và nghiêm túc trong tình cảm, kết hôn muộn một chút sẽ đại cát."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Khẳng Định Uy Tín & An Cư Lạc Nghiệp",
        overview: "Chữ tín và kinh nghiệm chuyên môn giúp bạn trở thành trụ cột không thể thay thế trong tổ chức.",
        careerFinance: "Vượng phát về điền trạch, bất động sản và các ngành kỹ thuật, sản xuất bền vững.",
        familyLove: "Tổ ấm vững chãi, vợ hiền con thảo, gia phong nề nếp chuẩn mực."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Phú Quý Thọ Khang & Vững Bền",
        overview: "Càng về già càng sung túc, hưởng thành quả từ những tài sản dài hạn đã gây dựng.",
        careerFinance: "Nhà cửa ruộng đất đề huề, hậu phương tài chính vững mạnh cho con cháu.",
        familyLove: "Gia đình đại đoàn viên, con cháu lễ phép hiếu thuận."
      }
    ],
    female: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Chăm Chỉ Cần Mẫn & Tự Lập",
        overview: "Nữ tuổi Sửu chịu thương chịu khó, sống thực tế và sớm gánh vác trách nhiệm phụ giúp gia đình.",
        careerFinance: "Làm việc cẩn trọng, tích lũy từng bước vững vàng, ít khi rơi vào cảnh thiếu thốn.",
        familyLove: "Tình cảm chân thật, đôi khi thiệt thòi vì nghĩ cho người khác quá nhiều."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Đảm Đang Giữ Lửa & Tài Lộc Nở Hoa",
        overview: "Công sức vun vén bắt đầu đơm hoa kết trái, cuộc sống vật chất và tinh thần đều bước sang trang mới.",
        careerFinance: "Tài vận hanh thông, có duyên tích lũy nhà đất và tài sản giá trị lâu dài.",
        familyLove: "Vợ chồng nghĩa tình sâu nặng, con cái ngoan hiền thành đạt."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Tâm An Phúc Hậu & Thanh Nhàn",
        overview: "Buông bỏ vất vả tuổi trẻ để tận hưởng tuổi già bình yên bên con cháu.",
        careerFinance: "Kinh tế độc lập, sung túc, được con cháu phụng dưỡng chu toàn.",
        familyLove: "Hưởng phúc mẫu nghi, gia đạo êm ấm thuận hòa."
      }
    ]
  },
  2: {
    male: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Xông Pha Mở Lối & Thử Thách Bản Lĩnh",
        overview: "Nam tuổi Dần mang chí lớn bốn phương, tuổi trẻ sôi nổi, dám thử sức ở những lĩnh vực khó khăn nhất.",
        careerFinance: "Kiếm tiền giỏi nhưng cũng dễ tiêu hao vì tính hào sảng; sau 28 tuổi mới bắt đầu giữ được tiền lớn.",
        familyLove: "Tình yêu nồng nhiệt, trải qua vài cung bậc cảm xúc mới tìm được người tri kỷ thấu hiểu."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Uy Quyền Lãnh Đạo & Sự Nghiệp Đỉnh Cao",
        overview: "Khi biết tiết chế sự nóng nảy bằng mưu lược, Nam tuổi Dần bước vào đại vận huy hoàng nhất.",
        careerFinance: "Nắm giữ vị trí lãnh đạo hoặc làm chủ doanh nghiệp, tài lộc bứt phá mạnh mẽ.",
        familyLove: "Là chỗ dựa vững chắc cho cả gia đình, con cái thông minh, mạnh mẽ."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Danh Vọng Vẹn Toàn & An Vui",
        overview: "Có uy vọng cao trong dòng tộc và xã hội, cuộc sống tuổi già phóng khoáng tự tại.",
        careerFinance: "Cơ ngơi bề thế, tài chính dư dả, vẫn giữ vai trò cố vấn uy tín.",
        familyLove: "Gia đình hưng thịnh, con cháu tự hào nối nghiệp."
      }
    ],
    female: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Độc Lập Mạnh Mẽ & Sớm Thành Danh",
        overview: "Nữ tuổi Dần thông minh, cá tính, không thích dựa dẫm và sớm khẳng định năng lực nơi công sở.",
        careerFinance: "Nhạy bén công việc, tự chủ tài chính từ rất sớm.",
        familyLove: "Cần sự thấu hiểu và tôn trọng từ bạn đời; kết hôn khi tâm lý đã chín chắn sẽ rất bền vững."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Bản Lĩnh Đương Đầu & Gia Đạo Vững Vàng",
        overview: "Vừa quyết đoán ngoài xã hội vừa chu toàn việc nhà, được đồng nghiệp và gia đình nể trọng.",
        careerFinance: "Thăng tiến nhanh, thu nhập cao, mát tay trong đầu tư kinh doanh.",
        familyLove: "Biết nhu cương đúng lúc giúp hôn nhân hòa hợp, con cái giỏi giang."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Thảnh Thơi Tự Tại & Phúc Lộc Trường Tồn",
        overview: "Sức khỏe dẻo dai, tinh thần minh mẫn, tận hưởng cuộc sống sung túc an vui.",
        careerFinance: "Tài sản tích lũy vững vàng, không phải bận tâm chuyện tiền bạc.",
        familyLove: "Con cháu hiếu thảo, quây quần ấm áp."
      }
    ]
  },
  3: {
    male: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Trí Tuệ Tinh Tế & Quý Nhân Dẫn Lối",
        overview: "Nam tuổi Mão nho nhã, khéo léo đắc nhân tâm nên từ trẻ đã có nhiều thầy giỏi, bạn tốt nâng đỡ.",
        careerFinance: "Thuận lợi trong học vấn, thi cử, các ngành trí thức, tài chính hoặc nghệ thuật.",
        familyLove: "Đào hoa lịch lãm, chuyện tình cảm êm đềm lãng mạn."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Ngoại Giao Đắc Thắng & Phú Quý Sinh Tài",
        overview: "Tài thương thuyết và sự cẩn trọng giúp bạn gặt hái thành tựu lớn mà không cần tranh đoạt gay gắt.",
        careerFinance: "Tài lộc tụ về từ nhiều nguồn, đầu tư chắc thắng, tích lũy tài sản bền vững.",
        familyLove: "Gia đạo êm ấm thuận hòa, vợ chồng tôn trọng và sẻ chia."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Thanh Cao Nhàn Nhã & Phúc Thọ Song Toàn",
        overview: "Cuộc sống tuổi già điềm đạm, thanh lịch, hưởng thú vui văn hóa tinh thần phong phú.",
        careerFinance: "Tiền bạc dư dả, lộc dưỡng già dồi dào.",
        familyLove: "Con cháu đỗ đạt khoa bảng, gia đình hòa thuận."
      }
    ],
    female: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Thanh Nhã Dịu Hiền & Thuận Đường Học Vấn",
        overview: "Nữ tuổi Mão thông tuệ, tinh tế, đi tới đâu cũng tạo thiện cảm và gặp may mắn.",
        careerFinance: "Công việc thuận buồm xuôi gió, sớm có khoản tích lũy riêng.",
        familyLove: "Tình duyên ngọt ngào, gặp được người bạn đời yêu thương chiều chuộng."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Tổ Ấm Bình Yên & Tài Lộc Đủ Đầy",
        overview: "Khéo léo dung hòa mọi mối quan hệ, sự nghiệp ổn định và gia đình luôn ngập tràn tiếng cười.",
        careerFinance: "Vượng lộc từ công việc chuyên môn và quản lý tài chính gia đình khôn ngoan.",
        familyLove: "Vợ chồng tâm đầu ý hợp, con cái ngoan hiền hiếu học."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "An Nhiên Hưởng Phúc & Trường Thọ",
        overview: "Tâm hồn trẻ trung, sức khỏe tốt, sống an vui trong tình yêu thương của con cháu.",
        careerFinance: "Tài chính vững vàng, cuộc sống sung túc đủ đầy.",
        familyLove: "Gia đạo bình an, con cháu thành nhân chi mỹ."
      }
    ]
  },
  4: {
    male: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Hoài Bão Lớn Lao & Sớm Lộ Tài Năng",
        overview: "Nam tuổi Thìn mang khí chất rồng bay, thông minh xuất chúng và sớm có khát vọng làm việc lớn.",
        careerFinance: "Có nhiều cơ hội thăng tiến sớm, dù đôi lúc thăng trầm do chủ quan nhưng nhanh chóng vực dậy.",
        familyLove: "Sức hút lớn, tình duyên phong phú, lập gia đình xong sự nghiệp càng vững."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Rồng Gặp Mây Hội & Đại Nghiệp Huy Hoàng",
        overview: "Giai đoạn thiên thời địa lợi nhân hòa hội tụ, tạo dựng công danh và vị thế xã hội rực rỡ.",
        careerFinance: "Tài lộc dồi dào như nước nguồn, thành công lớn trong quản lý, kinh doanh hoặc đầu tư.",
        familyLove: "Gia đình danh giá, vợ con được hưởng phúc lộc sung túc."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Vinh Hoa Phú Quý & Phúc Đức Vẹn Toàn",
        overview: "Uy danh lẫy lừng, cuộc sống tuổi già vương giả và được hậu thế kính ngưỡng.",
        careerFinance: "Điền sản lớn mạnh, để lại di sản vững chắc cho con cháu.",
        familyLove: "Con cháu hiển đạt, gia tộc hưng vượng."
      }
    ],
    female: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Tự Tin Tỏa Sáng & Vươn Lên Mạnh Mẽ",
        overview: "Nữ tuổi Thìn thông minh, năng động, có chí tiến thủ và không ngừng hoàn thiện bản thân.",
        careerFinance: "Sớm đạt thành tích nổi bật trong học tập và sự nghiệp, thu nhập tốt.",
        familyLove: "Được nhiều người ngưỡng mộ, chọn bạn đời kỹ lưỡng và xứng đôi."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Tài Sắc Song Toàn & Hưng Gia Vượng Nghiệp",
        overview: "Không chỉ thành công trong sự nghiệp riêng mà còn là cánh tay đắc lực giúp chồng làm nên nghiệp lớn.",
        careerFinance: "Nắm giữ tài sản lớn, kinh doanh nhạy bén, tiền bạc hanh thông.",
        familyLove: "Gia đạo sung túc, con cái thông minh xuất sắc."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Quý Phái An Nhàn & Hưởng Lộc Con Cháu",
        overview: "Cuộc sống tuổi già thanh quý, viên mãn cả về vật chất lẫn tinh thần.",
        careerFinance: "Của cải sung túc, an hưởng vinh hoa.",
        familyLove: "Con cháu thành danh, hiếu kính phụng dưỡng."
      }
    ]
  },
  5: {
    male: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Trầm Tĩnh Quan Sát & Tích Lũy Nội Lực",
        overview: "Nam tuổi Tỵ sâu sắc, kín đáo, tuổi trẻ chú trọng trau dồi chuyên môn và hoạch định chiến lược dài hạn.",
        careerFinance: "Biết nắm bắt cơ hội ngầm, sớm có tích lũy nhờ sự nhạy bén tài chính.",
        familyLove: "Khi đã yêu thì hết lòng sâu sắc, tìm kiếm người bạn đời tri âm tri kỷ."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Mưu Lược Tinh Tường & Tài Lộc Bứt Phá",
        overview: "Trực giác sắc bén giúp bạn đón đầu các xu hướng lớn, sự nghiệp thăng hoa vững chắc.",
        careerFinance: "Đặc biệt có lộc về đầu tư, tài chính, nghiên cứu và quản trị cấp cao.",
        familyLove: "Gia đình êm ấm, luôn bảo vệ và chăm lo chu đáo cho vợ con."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Minh Triết Thảnh Thơi & Phú Quý Dài Lâu",
        overview: "Sống cuộc đời ung dung, trí tuệ minh mẫn, hưởng thành quả từ sự chuẩn bị kỹ lưỡng.",
        careerFinance: "Tài sản tích lũy dồi dào, không lo biến động.",
        familyLove: "Gia đạo an hòa, con cháu thành đạt nối tiếp."
      }
    ],
    female: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Sắc Sảo Khéo Léo & Độc Lập Tài Chính",
        overview: "Nữ tuổi Tỵ vừa có nét duyên ngầm cuốn hút vừa có đầu óc thực tế, thông minh.",
        careerFinance: "Quản lý tiền bạc khôn ngoan, công việc tiến triển thuận lợi.",
        familyLove: "Tình duyên có sức hút lớn, khi lập gia đình rất chung thủy son sắt."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Vượng Tài Vượng Lộc & Giữ Lửa Tổ Ấm",
        overview: "Trực giác nhạy bén giúp bạn vừa phát triển sự nghiệp vừa quản lý kinh tế gia đình xuất sắc.",
        careerFinance: "Có lộc lớn về bất động sản, tích lũy kim ngân dồi dào.",
        familyLove: "Vợ chồng gắn kết, con cái chăm ngoan thành tài."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "An Khang Thịnh Vượng & Viên Mãn",
        overview: "Tuổi già thanh nhàn, quý phái, hưởng phúc lộc bền lâu.",
        careerFinance: "Của ăn của để vững vàng, cuộc sống dư dả.",
        familyLove: "Con cháu hiếu thuận, gia đình sum vầy."
      }
    ]
  },
  6: {
    male: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Tung Hoành Bốn Phương & Đam Mê Khám Phá",
        overview: "Nam tuổi Ngọ nhiệt huyết, phóng khoáng, tuổi trẻ đi nhiều biết rộng, sớm tự lập bươn chải.",
        careerFinance: "Nắm bắt cơ hội nhanh, làm ra tiền sớm nhưng cần rèn kỷ luật tiết kiệm.",
        familyLove: "Lãng mạn, sôi nổi, sau khi lập gia đình sẽ dần điềm đạm và trách nhiệm hơn."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Mã Đáo Thành Công & Sự Nghiệp Rạng Danh",
        overview: "Khi tìm đúng đường hướng, sức bền và tốc độ của tuổi Ngọ giúp bạn bứt phá ngoạn mục.",
        careerFinance: "Thương mại, kinh doanh, ngoại giao đều đại thắng; tài lộc dồi dào.",
        familyLove: "Gia đạo hưng vượng, vợ chồng đồng hành vượt mọi thử thách."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Tiêu Dao Tự Tại & Phúc Lộc Trường Thọ",
        overview: "Sức khỏe dồi dào, tinh thần lạc quan trẻ trung, tận hưởng tuổi già an vui.",
        careerFinance: "Kinh tế vững chãi, điền sản sung túc.",
        familyLove: "Con cháu thành đạt, gia đình rộn tiếng cười."
      }
    ],
    female: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Năng Động Tháo Vát & Tự Chủ Vươn Lên",
        overview: "Nữ tuổi Ngọ hoạt bát, thẳng thắn, làm việc gì cũng tràn đầy năng lượng tích cực.",
        careerFinance: "Sớm tự lập kinh tế, nhạy bén trong giao tiếp và công việc.",
        familyLove: "Tình cảm chân thành, nồng nhiệt, nên chọn bạn đời điềm đạm bao dung."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Đảm Việc Nước Giỏi Việc Nhà & Phát Đạt",
        overview: "Sự tháo vát và quyết đoán giúp bạn gặt hái thành công cả trên thương trường lẫn trong tổ ấm.",
        careerFinance: "Tài vận hanh thông, tích lũy được nhiều tài sản giá trị.",
        familyLove: "Gia đình hòa thuận, con cái năng động, thông minh."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Bình Yên Hưởng Phúc & An Nhàn",
        overview: "Tâm hồn thảnh thơi, không còn lo toan vất vả, vui vầy cùng con cháu.",
        careerFinance: "Tài chính dư dả, tuổi già sung túc.",
        familyLove: "Con cháu hiếu thảo, gia đạo ấm êm."
      }
    ]
  },
  7: {
    male: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Ôn Hòa Rèn Chí & Quý Nhân Tương Trợ",
        overview: "Nam tuổi Mùi điềm đạm, sâu sắc, sống tình nghĩa nên luôn có quý nhân xuất hiện giúp đỡ lúc khó khăn.",
        careerFinance: "Khởi đầu chậm mà chắc, hợp với các ngành chuyên môn sâu, nghệ thuật, quản lý hoặc dịch vụ.",
        familyLove: "Trọng tình cảm, luôn khao khát xây dựng một mái ấm bình yên."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Phúc Lộc Tụ Hội & Sự Nghiệp Vững Vàng",
        overview: "Càng trưởng thành càng bản lĩnh, uy tín cá nhân giúp bạn mở rộng cơ đồ bền vững.",
        careerFinance: "Lộc lá dồi dào, điền sản tăng thêm, công việc kinh doanh thuận lợi.",
        familyLove: "Người chồng, người cha mẫu mực; gia đạo êm ấm hạnh phúc."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Đức Dày Phúc Lớn & An Khang Thịnh Vượng",
        overview: "Nhờ sống thiện lương tích đức từ trẻ nên hậu vận tuổi Mùi cực kỳ sung túc, an nhàn.",
        careerFinance: "Của cải đầy kho, không bao giờ lo túng thiếu.",
        familyLove: "Con cháu hiếu thảo, thành danh, gia đình sum họp."
      }
    ],
    female: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Dịu Hiền Tinh Tế & Sớm Gặp Duyên Lành",
        overview: "Nữ tuổi Mùi nhân hậu, khéo léo, được mọi người yêu thương che chở.",
        careerFinance: "Công việc ổn định, có duyên với các lĩnh vực thẩm mỹ, giáo dục, y tế, tài chính.",
        familyLove: "Tình duyên thuận lợi, gặp được người chồng biết trân trọng và yêu thương."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Phúc Khí Vượng Gia & Tài Lộc Nở Rộ",
        overview: "Đức tính hiền hòa, đảm đang của bạn mang lại phong thủy đại cát cho cả gia đình.",
        careerFinance: "Kinh tế gia đình ngày càng khấm khá, có lộc nhà đất và tích lũy bền vững.",
        familyLove: "Vợ chồng hòa hợp, con cái ngoan ngoãn, hiếu thảo."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Viên Mãn Thanh Nhàn & Phúc Lộc Dồi Dào",
        overview: "Tuổi già an nhiên hưởng phúc, sức khỏe và tinh thần đều thư thái.",
        careerFinance: "Đầy đủ sung túc, được con cháu báo hiếu chu toàn.",
        familyLove: "Gia đạo đề huề, ấm áp yêu thương."
      }
    ]
  },
  8: {
    male: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Thông Tuệ Nhạy Bén & Đa Tài Ứng Biến",
        overview: "Nam tuổi Thân thông minh lanh lợi, học một hiểu mười, sớm bộc lộ tài năng xuất sắc.",
        careerFinance: "Nắm bắt cơ hội kiếm tiền rất nhanh, càng thử thách càng phát huy trí sáng tạo.",
        familyLove: "Đào hoa, hoạt ngôn, sau khi kết hôn sẽ tập trung mạnh mẽ cho sự nghiệp."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Xoay Chuyển Thời Cơ & Thành Tựu Vượt Bậc",
        overview: "Kinh nghiệm thực chiến kết hợp tư duy linh hoạt đưa bạn lên đỉnh cao công danh.",
        careerFinance: "Đại phát về thương mại, công nghệ, tài chính; tài sản tích lũy nhanh chóng.",
        familyLove: "Gia đình sung túc, con cái thông minh lanh lợi."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Minh Mẫn Giàu Sang & An Vui Tự Tại",
        overview: "Tuổi già vẫn giữ được trí tuệ sắc sảo, sống sung túc và truyền dạy kinh nghiệm quý cho con cháu.",
        careerFinance: "Tiền tài dư dả, nguồn thu thụ động vững vàng.",
        familyLove: "Gia đạo hưng thịnh, con cháu thành đạt."
      }
    ],
    female: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Linh Hoạt Sắc Sảo & Sớm Khẳng Định Mình",
        overview: "Nữ tuổi Thân thông minh, hiện đại, giao tiếp khéo léo nên đi đâu cũng tỏa sáng.",
        careerFinance: "Công việc thuận lợi, có khả năng kiếm tiền và quản lý tài chính độc lập.",
        familyLove: "Tình duyên sôi nổi, kết hôn với người tâm đầu ý hợp sẽ rất hạnh phúc."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Tài Đảm Song Toàn & Gia Nghiệp Hưng Long",
        overview: "Vừa nhạy bén nắm bắt cơ hội kinh doanh vừa quán xuyến tổ ấm chu toàn.",
        careerFinance: "Tài lộc vượng phát, mát tay trong đầu tư và tích lũy tài sản.",
        familyLove: "Vợ chồng đồng lòng, con cái học giỏi, gia đình ngập tràn niềm vui."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Trẻ Trung Sung Túc & Hưởng Trọn Phúc Lộc",
        overview: "Tinh thần luôn yêu đời, lạc quan, tận hưởng cuộc sống tuổi già đầy đủ tiện nghi.",
        careerFinance: "Kinh tế vững chắc, của để dành dồi dào.",
        familyLove: "Con cháu quây quần hiếu thuận."
      }
    ]
  },
  9: {
    male: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Kỷ Luật Chuyên Cần & Khẳng Định Năng Lực",
        overview: "Nam tuổi Dậu nguyên tắc, tỉ mỉ, có chí tiến thủ rõ ràng ngay từ khi mới lập thân.",
        careerFinance: "Nhờ làm việc có kế hoạch khoa học nên sớm đạt vị trí tốt và có tích lũy.",
        familyLove: "Chân thành, chu đáo, luôn tìm kiếm mối quan hệ nghiêm túc lâu dài."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Danh Chấn Bốn Phương & Tài Lộc Sung Túc",
        overview: "Sự chuẩn xác và chữ tín giúp bạn gặt hái thành công lớn trong quản lý, tài chính, kỹ thuật hoặc pháp lý.",
        careerFinance: "Tiền bạc dồi dào, đầu tư chắc chắn, gây dựng cơ ngơi khang trang.",
        familyLove: "Trụ cột kinh tế vững vàng, gia đình nề nếp hạnh phúc."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Phú Quý An Nhàn & Hậu Vận Vững Chãi",
        overview: "Mọi kế hoạch tích lũy từ trẻ phát huy hiệu quả, tuổi già sống ung dung tự tại.",
        careerFinance: "Tài sản sung túc, không phải lo nghĩ chuyện vật chất.",
        familyLove: "Con cháu thành danh, hiếu thảo và kính trọng."
      }
    ],
    female: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Đảm Đang Chu Toàn & Tự Lập Sớm",
        overview: "Nữ tuổi Dậu chăm chỉ, sắc sảo, làm việc gì cũng gọn gàng và có trách nhiệm cao.",
        careerFinance: "Giỏi kiếm tiền và rất biết cách quản lý chi tiêu hợp lý.",
        familyLove: "Tình cảm sâu sắc, hết lòng vì người mình yêu thương."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Vượng Phu Ích Tử & Kinh Tế Vững Vàng",
        overview: "Là người giữ tay hòm chìa khóa xuất sắc, đưa kinh tế gia đình ngày một đi lên.",
        careerFinance: "Có duyên với bất động sản, kinh doanh và tích lũy tài sản lớn.",
        familyLove: "Chồng con thành đạt nhờ bàn tay chăm sóc chu toàn của bạn."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "An Vui Thảnh Thơi & Phúc Đức Trọn Vẹn",
        overview: "Tuổi già bình yên, hưởng quả ngọt từ sự tần tảo và nuôi dạy con cái thành tài.",
        careerFinance: "Của ăn của để dư dả, cuộc sống an nhàn.",
        familyLove: "Gia đình sum vầy, con cháu hiếu nghĩa."
      }
    ]
  },
  10: {
    male: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Chính Trực Kiên Cường & Rèn Luyện Bản Lĩnh",
        overview: "Nam tuổi Tuất trọng nghĩa khí, trung thực, tuổi trẻ chăm chỉ cống hiến và xây dựng uy tín.",
        careerFinance: "Đi lên bằng năng lực thật, càng làm việc ở môi trường minh bạch càng nhanh thăng tiến.",
        familyLove: "Chung thủy, chân thành, khi lập gia đình luôn đặt vợ con lên trên hết."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Uy Tín Vang Xa & Sự Nghiệp Bền Vững",
        overview: "Sự tin cậy tuyệt đối từ cấp trên, đối tác và bạn bè giúp bạn nắm giữ những trọng trách lớn.",
        careerFinance: "Tài lộc tăng trưởng vững chắc, tích lũy được nhà cửa và tài sản giá trị.",
        familyLove: "Gia đạo bình yên, vợ chồng đồng cam cộng khổ, con cái hiếu thuận."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Tâm An Phúc Vượng & Hưởng Lộc Bền Lâu",
        overview: "Cuộc sống tuổi già thanh thản, được mọi người kính trọng vì đức độ.",
        careerFinance: "Kinh tế ổn định vững vàng, hậu vận sung túc.",
        familyLove: "Con cháu sum vầy, gia phong tốt đẹp."
      }
    ],
    female: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Hiền Lương Chăm Chỉ & Chân Thành",
        overview: "Nữ tuổi Tuất sống ngay thẳng, giàu lòng nhân ái, luôn nỗ lực học tập và làm việc.",
        careerFinance: "Thu nhập ổn định, biết tiết kiệm và lo xa cho tương lai.",
        familyLove: "Thủy chung son sắt, tìm được người bạn đời chân thành, trách nhiệm."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Điểm Tựa Gia Đình & Tài Lộc Hưng Thịnh",
        overview: "Sự kiên nhẫn và quán xuyến khéo léo của bạn giúp gia đình vượt qua mọi sóng gió để vươn lên.",
        careerFinance: "Công việc thuận lợi, tích lũy tài chính ngày càng vững mạnh.",
        familyLove: "Hôn nhân bền chặt, con cái ngoan ngoãn, học hành tấn tới."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Phúc Hậu Bình An & Viên Mãn",
        overview: "Hưởng tuổi già êm đềm, không vướng bận âu lo, sức khỏe dồi dào.",
        careerFinance: "Đầy đủ sung túc, lộc con cháu dồi dào.",
        familyLove: "Gia đình hòa thuận, con cháu hiếu thảo."
      }
    ]
  },
  11: {
    male: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Phúc Khí Tự Nhiên & Thuận Lợi Khởi Đầu",
        overview: "Nam tuổi Hợi nhân hậu, rộng lượng, đi tới đâu cũng gặp quý nhân phù trợ.",
        careerFinance: "Ít khi gặp cảnh ngặt nghèo, cơ hội công việc và tài lộc đến khá tự nhiên.",
        familyLove: "Hiền hòa, ấm áp, đường tình duyên thuận lợi êm đẹp."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Đại Lộc Hanh Thông & Gia Nghiệp Sung Túc",
        overview: "Chữ tín và tấm lòng bao dung giúp bạn thu hút đối tác lớn, sự nghiệp phát triển bền vững.",
        careerFinance: "Tiền tài dồi dào, có lộc lớn về kinh doanh, thương mại và điền sản.",
        familyLove: "Gia đình hạnh phúc, vợ con được hưởng cuộc sống ấm no đủ đầy."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "Viên Mãn Phú Quý & Trường Thọ Đa Phúc",
        overview: "Một trong những con giáp có hậu vận tốt đẹp nhất, sống an nhàn sung sướng.",
        careerFinance: "Của cải dư dả, phúc lộc vẹn toàn.",
        familyLove: "Con cháu sum vầy, hiếu thảo, gia tộc hưng thịnh."
      }
    ],
    female: [
      {
        stage: "Tiền vận",
        ageRange: "Dưới 30 tuổi",
        title: "Hiền Hòa Đôn Hậu & May Mắn Cát Tường",
        overview: "Nữ tuổi Hợi có số hưởng phúc, tính tình vui vẻ thiện lương nên luôn được yêu mến.",
        careerFinance: "Công việc hanh thông, tài chính ổn định vững vàng.",
        familyLove: "Tình duyên tốt đẹp, kết hôn cùng người chồng yêu thương, trách nhiệm."
      },
      {
        stage: "Trung vận",
        ageRange: "31 - 50 tuổi",
        title: "Vượng Phu Ích Tử & Tài Lộc Dồi Dào",
        overview: "Mang lại phúc khí lớn cho nhà chồng, kinh tế gia đình ngày một phất lên.",
        careerFinance: "Tích lũy được nhiều tài sản, tiền bạc rủng rỉnh.",
        familyLove: "Tổ ấm ngập tràn yêu thương, con cái ngoan hiền đỗ đạt."
      },
      {
        stage: "Hậu vận",
        ageRange: "Từ 51 tuổi trở đi",
        title: "An Nhiên Phú Quý & Phúc Đức Ngập Tràn",
        overview: "Tuổi già thanh thản, khỏe mạnh, hưởng trọn niềm vui bên con đàn cháu đống.",
        careerFinance: "Sung túc đủ đầy, không lo nghĩ vật chất.",
        familyLove: "Con cháu thành đạt, hiếu kính vẹn toàn."
      }
    ]
  }
};

function getLifeStagesForAnimal(chiIdx: number, gender: "male" | "female"): LifeStageFortune[] {
  const normalizedChi = ((chiIdx % 12) + 12) % 12;
  return LIFE_STAGES_BY_GENDER_DATA[normalizedChi][gender];
}

export function getSaoHan2026(lunarAge: number, gender: "male" | "female"): SaoHan2026Info {
  const safeAge = Math.max(1, lunarAge);
  const saoNam = [
    { name: "La Hầu", type: "Hung tinh" as const, desc: "Chủ về khẩu thiệt thị phi, công việc dễ gặp tranh cãi, cần giữ gìn sức khỏe mắt tai và cẩn trọng tháng Giêng, tháng 7 Âm lịch." },
    { name: "Thổ Tú", type: "Trung tinh" as const, desc: "Khởi đầu có chút thử thách nhưng kiên trì sẽ hanh thông. Cần giữ gia đạo hòa thuận, đề phòng tiểu nhân tháng 4 và tháng 8 Âm lịch." },
    { name: "Thủy Diệu", type: "Cát tinh" as const, desc: "Cát tinh chủ về tài lộc hưng thịnh, đi xa có lộc, gia đạo có hỷ sự; tuy nhiên cần cẩn trọng lời nói và sông nước vào tháng 4, tháng 8 Âm lịch." },
    { name: "Thái Bạch", type: "Hung tinh" as const, desc: "Hung tinh chủ về hao tài ('Thái Bạch quét sạch cửa nhà'). Cần quản lý tài chính chặt chẽ, tránh đầu tư mạo hiểm, kỵ nhất tháng 2, 5, 8 Âm lịch." },
    { name: "Thái Dương", type: "Cát tinh" as const, desc: "Đệ nhất cát tinh cho Nam mạng ('Thái Dương rực rỡ mặt trời'). Công danh thăng tiến, kinh doanh đắc lộc, đại vượng tháng 6 và tháng 10 Âm lịch." },
    { name: "Vân Hớn", type: "Trung tinh" as const, desc: "Vận thế bình hòa, công việc giữ mức ổn định. Cần kiềm chế nóng nảy, cẩn trọng giấy tờ pháp lý vào tháng 2 và tháng 8 Âm lịch." },
    { name: "Kế Đô", type: "Hung tinh" as const, desc: "Dễ sinh tâm lý lo âu, buồn phiền hoặc hao tài vặt; đi xa lại có tài lộc bất ngờ. Nên làm việc thiện tích đức, cẩn trọng tháng 3 và tháng 9 Âm lịch." },
    { name: "Thái Âm", type: "Cát tinh" as const, desc: "Cát tinh chủ về danh lợi lưỡng toàn, được quý nhân khác giới trợ giúp, vượng Bất động sản và tài chính, đại cát tháng 9, cần giữ sức khỏe tháng 11." },
    { name: "Mộc Đức", type: "Cát tinh" as const, desc: "Phúc tinh chiếu mệnh, hanh thông về công danh, hôn nhân và quý nhân nâng đỡ; đặc biệt đắc lộc vào tháng 10 và tháng Chạp (12 ÂL)." }
  ];

  const saoNu = [
    { name: "Kế Đô", type: "Hung tinh" as const, desc: "Hung tinh đối với Nữ mạng, dễ vướng thị phi tình cảm hoặc tâm tư buồn phiền. Nên giữ tâm an tịnh, hướng thiện, cẩn trọng tháng 3 và tháng 9 Âm lịch." },
    { name: "Vân Hớn", type: "Trung tinh" as const, desc: "Vận thế trung bình, cần chú ý sức khỏe huyết khí, thai sản, tránh nóng nảy tranh cãi trong nhà, lưu ý tháng 2 và tháng 8 Âm lịch." },
    { name: "Mộc Đức", type: "Cát tinh" as const, desc: "Cát tinh mang lại an vui, gia đạo ấm êm hòa thuận, công việc kinh doanh phát đạt, đại cát vào tháng 10 và tháng 12 Âm lịch." },
    { name: "Thái Âm", type: "Cát tinh" as const, desc: "Đệ nhất cát tinh cho Nữ mạng. Tài lộc dồi dào, tình duyên viên mãn, cầu danh cầu lợi đều toại nguyện, vượng nhất tháng 9 Âm lịch (lưu ý sức khỏe tháng 11)." },
    { name: "Thổ Tú", type: "Trung tinh" as const, desc: "Tâm lý đôi lúc lo âu, xuất hành đi xa chưa thuận, cần giữ gìn hòa khí gia đình và sức khỏe vào tháng 4 và tháng 8 Âm lịch." },
    { name: "La Hầu", type: "Hung tinh" as const, desc: "Chủ về khẩu thiệt thị phi, phiền muộn gia đạo, hao tài nhẹ. Cần giữ bình tĩnh, tránh nghe lời gièm pha, kỵ tháng Giêng và tháng 7 Âm lịch." },
    { name: "Thái Dương", type: "Cát tinh" as const, desc: "Cát tinh mang lại quý nhân nâng đỡ, công việc buôn bán hanh thông, tinh thần phấn chấn, vượng lộc tháng 6 và tháng 10 Âm lịch." },
    { name: "Thái Bạch", type: "Hung tinh" as const, desc: "Hạn hao tài tốn của, tránh cho vay mượn lớn hoặc đầu tư rủi ro cao, chú ý sức khỏe vào tháng 2, tháng 5 và tháng 8 Âm lịch." },
    { name: "Thủy Diệu", type: "Cát tinh" as const, desc: "Phúc lộc tinh chủ về tài lộc và tin vui gia đạo; tuy nhiên Nữ mạng cần giữ gìn lời ăn tiếng nói để tránh thị phi vào tháng 4 và tháng 8 Âm lịch." }
  ];

  // Cửu Diệu: Khởi từ 10 tuổi (hoặc 1 tuổi), chu kỳ 9 năm
  const saoIdx = ((safeAge - 10) % 9 + 9) % 9;
  const sao = gender === "male" ? saoNam[saoIdx] : saoNu[saoIdx];

  // Bát Hạn chuẩn Ngọc Hạp Thông Thư:
  // 0: Huỳnh Tuyền -> 1: Tam Kheo -> 2: Ngũ Mộ -> 3: Thiên Tinh -> 4: Toán Tận -> 5: Thiên La -> 6: Địa Võng -> 7: Diêm Vương
  const hanList = [
    { name: "Huỳnh Tuyền", desc: "Hạn chủ về sức khỏe, đề phòng bệnh đau đầu, huyết áp, chóng mặt; kỵ đi lại đường sông nước hoặc bảo lãnh tài chính." },
    { name: "Tam Kheo", desc: "Hạn nhỏ về tứ chi xương khớp, mỏi mắt; cần chú ý đi lại cẩn trọng và hạn chế thức khuya." },
    { name: "Ngũ Mộ", desc: "Hạn chủ về hao tài tốn của ('Ngũ Mộ hạn xấu tổn tài'); mua sắm hàng hóa cần rõ nguồn gốc, không nên cho vay mượn khó đòi." },
    { name: "Thiên Tinh", desc: "Hạn chủ về tiêu hóa, ăn uống và lời ăn tiếng nói; cần giữ chữ tín, cẩn trọng giấy tờ để tránh tranh chấp thị phi." },
    { name: "Toán Tận", desc: "Hạn về tiền bạc tài sản; đi lại cần cẩn trọng tư trang, tránh mang nhiều tài sản quý giá hoặc góp vốn mạo hiểm." },
    { name: "Thiên La", desc: "Hạn chủ về tâm lý lo âu, mất ngủ; vợ chồng cần thấu hiểu nhường nhịn nhau, giữ tâm an định thì mọi việc tự yên." },
    { name: "Địa Võng", desc: "Hạn chủ về lời ra tiếng vào, thị phi hiểu lầm; không nên đi muộn về khuya hay can dự chuyện bao đồng." },
    { name: "Diêm Vương", desc: "Người đau yếu hoặc phụ nữ thai sản cần chú trọng chăm sóc sức khỏe; ngược lại người khỏe mạnh làm ăn kinh doanh lại rất đắc tài lộc." }
  ];

  // Cách bấm độn Bát Hạn truyền thống:
  // Nam khởi 10 tuổi tại Huỳnh Tuyền (0) đi thuận; Nữ khởi 10 tuổi tại Toán Tận (4) đi nghịch.
  // Bấm hàng chục tiếp nối hàng đơn vị: số bước dịch chuyển từ mốc 10 tuổi = (hàng chục - 1) + hàng đơn vị.
  const tens = Math.max(1, Math.floor(safeAge / 10));
  const unit = safeAge % 10;
  const steps = (tens - 1) + unit;
  const hanIdx = gender === "male"
    ? ((steps % 8) + 8) % 8
    : ((4 - steps) % 8 + 8) % 8;
  const han = hanList[hanIdx];

  const isGoodSao = sao.type === "Cát tinh";
  const yearOverview = `Năm 2026 (Bính Ngọ), tính theo lịch Âm bạn ${safeAge} tuổi (tuổi mụ). Bạn được sao ${sao.name} (${sao.type}) chiếu mệnh và gặp niên hạn ${han.name}. ${isGoodSao ? "Vận trình năm nay đón nhận nhiều cát khí hanh thông, thuận lợi mở rộng công việc và tích lũy tài chính nếu biết nắm bắt thời cơ." : "Năm nay nên lấy sự ổn định làm trọng, dĩ hòa vi quý trong giao tiếp, quản lý tài chính chặt chẽ và chăm sóc sức khỏe để hóa giải vận hạn."}`;

  const favorableBySao: Record<string, number[]> = {
    "Thái Dương": [2, 6, 10, 12],
    "Thái Âm": [3, 6, 9, 10],
    "Mộc Đức": [1, 4, 10, 12],
    "Thủy Diệu": [1, 6, 10, 12],
    "Thổ Tú": [2, 6, 10, 12],
    "Vân Hớn": [3, 6, 9, 11],
    "La Hầu": [3, 6, 10, 12],
    "Kế Đô": [1, 6, 10, 12],
    "Thái Bạch": [1, 6, 10, 12]
  };

  const cautionBySao: Record<string, number[]> = {
    "Thái Dương": [4, 8],
    "Thái Âm": [11],
    "Mộc Đức": [2, 5],
    "Thủy Diệu": [4, 8],
    "Thổ Tú": [4, 8],
    "Vân Hớn": [2, 8],
    "La Hầu": [1, 7],
    "Kế Đô": [3, 9],
    "Thái Bạch": [2, 5, 8]
  };

  return {
    lunarAge: safeAge,
    sao: sao.name,
    saoType: sao.type,
    saoDescription: sao.desc,
    han: han.name,
    hanDescription: han.desc,
    yearOverview,
    favorableMonths: favorableBySao[sao.name] || [2, 6, 10],
    cautionMonths: cautionBySao[sao.name] || [5, 7]
  };
}

/**
 * Ngũ Thử Độn (五鼠遁): Tính Thiên Can của Giờ sinh dựa trên Thiên Can của Ngày sinh
 * "Giáp Kỷ hoàn gia Giáp, Ất Canh Bính tác sơ, Bính Tân tùng Mậu khởi, Đinh Nhâm Canh Tý cư, Mậu Quý hà phương Mịch, Nhâm Tý thị chân đồ"
 */
export function getHourCanIndex(dayCanIdx: number, hourChiIdx: number): number {
  const normDayCan = ((dayCanIdx % 10) + 10) % 10;
  const normHourChi = ((hourChiIdx % 12) + 12) % 12;
  return ((normDayCan % 5) * 2 + normHourChi) % 10;
}

const HOUR_CHI_TRAITS: Record<number, {
  title: string;
  overview: string;
  personality: string;
  careerWealth: string;
  childrenLateLife: string;
}> = {
  0: {
    title: "Thủy Nhuận Thiên Hà • Minh Châu Xuất Hải",
    overview: "Sinh vào giờ Tý (23h-1h) là thời khắc Âm cực sinh Dương, vạn vật bắt đầu chuyển mình đón vận hội mới. Người sinh giờ này mang trí tuệ uyên bác, nhạy bén thời cuộc.",
    personality: "Thông minh sắc sảo, giàu mưu lược, khả năng quan sát tinh tường và độc lập tự chủ từ sớm.",
    careerWealth: "Có duyên với thương mại, tài chính, công nghệ hoặc quản trị. Trung vận dễ nắm bắt cơ hội lớn để bứt phá tài sản.",
    childrenLateLife: "Cung Tử Tức vượng khí, con cái hiếu thảo thông tuệ; hậu vận an nhàn, phúc lộc dồi dào."
  },
  1: {
    title: "Kim Ngưu Canh Điền • Kiên Tâm Đại Thành",
    overview: "Sinh vào giờ Sửu (1h-3h) là lúc đất trời tĩnh lặng tích tụ nguyên khí. Người sinh giờ Sửu có ý chí bền bỉ phi thường, làm việc gì cũng chắc chắn tới cùng.",
    personality: "Điềm tĩnh, thực tế, trọng chữ tín, nội tâm sâu sắc, không bao giờ khuất phục trước nghịch cảnh.",
    careerWealth: "Hợp điền sản, đầu tư dài hạn, kỹ thuật, xây dựng hoặc quản lý vận hành. Tiền bạc tích tụ ngày một dày theo năm tháng.",
    childrenLateLife: "Con cái ngoan ngoãn, tự lập sớm; tuổi già có của ăn của để và điền trạch vững vàng."
  },
  2: {
    title: "Bình Minh Hổ Khiếu • Quyền Uy Khai Vận",
    overview: "Sinh vào giờ Dần (3h-5h) là thời khắc dương khí bừng tỉnh nơi chân trời. Người sinh giờ Dần mang khí phách tiên phong, dám nghĩ dám làm.",
    personality: "Quyết đoán, nhiệt huyết, có tố chất thủ lĩnh bẩm sinh, thẳng thắn và giàu lòng nghĩa hiệp.",
    careerWealth: "Thuận lợi khởi nghiệp, lãnh đạo tổ chức, kinh doanh tự do hoặc mở rộng thị trường mới. Lộc phát mạnh từ tuổi 32 trở đi.",
    childrenLateLife: "Con cái mạnh mẽ, thành đạt, làm rạng danh gia tộc; hậu vận có danh vọng và uy tín cao."
  },
  3: {
    title: "Hồng Nhật Đông Thăng • Quý Nhân Tương Trợ",
    overview: "Sinh vào giờ Mão (5h-7h) khi mặt trời vừa ló rạng, vạn vật đón ánh bình minh tươi sáng. Bản mệnh sinh giờ này phúc khí thanh tao, đi đâu cũng gặp quý nhân.",
    personality: "Nhã nhặn, tinh tế, khéo léo đắc nhân tâm, tư duy sáng tạo và giàu lòng nhân ái.",
    careerWealth: "Ngoại giao rộng mở, rất hợp văn hóa, giáo dục, nghệ thuật, y dược, dịch vụ cao cấp hoặc tư vấn chiến lược.",
    childrenLateLife: "Gia đạo êm ấm thuận hòa, con cái hiếu thuận; hậu vận thanh nhàn, hưởng phúc trời ban."
  },
  4: {
    title: "Phi Long Tại Thiên • Hoài Bão Lớn Lao",
    overview: "Sinh vào giờ Thìn (7h-9h) khi nắng sớm rực rỡ bốn phương. Người sinh giờ Thìn mang tầm nhìn xa rộng, khí chất đĩnh đạc và khát vọng vươn lên đỉnh cao.",
    personality: "Tự tin, năng động, trọng danh dự, có khả năng quy tụ nhân tâm và giải quyết việc lớn.",
    careerWealth: "Sự nghiệp có bước nhảy vọt ngoạn mục ở trung vận, vượng cả Chính Quan lẫn Chính Tài, dễ nắm giữ trọng trách.",
    childrenLateLife: "Đường con cái hiển vinh, gia门 hưng thịnh; hậu vận phú quý song toàn."
  },
  5: {
    title: "Nam Phong Tụ Lộc • Trí Tuệ Thâm Sâu",
    overview: "Sinh vào giờ Tỵ (9h-11h) khi dương hỏa đang đà hưng vượng. Người sinh giờ Tỵ có trực giác nhạy bén, đầu óc phân tích sắc sảo và biết nắm bắt thời cơ vàng.",
    personality: "Khôn khéo, sâu sắc, hành sự kín kẽ, bề ngoài điềm đạm nhưng bên trong luôn có kế sách chu toàn.",
    careerWealth: "Giỏi đầu tư, kinh doanh thương mại, nghiên cứu chuyên sâu hoặc tài chính. Tài lộc đến từ cả công việc chính lẫn nguồn thu phụ.",
    childrenLateLife: "Con cái thông minh hiếu học, gia đình sung túc; tuổi già an vui viên mãn."
  },
  6: {
    title: "Nhật Chính Trung Thiên • Quang Minh Rực Rỡ",
    overview: "Sinh vào giờ Ngọ (11h-13h) là lúc mặt trời đứng bóng, dương khí cực thịnh. Bản mệnh sinh giờ Ngọ có tâm hồn hào sảng, chính trực và tràn đầy sinh lực.",
    personality: "Nhiệt thành, phóng khoáng, quang minh lỗi lạc, có sức truyền cảm hứng mạnh mẽ cho mọi người xung quanh.",
    careerWealth: "Danh tiếng dễ vang xa, hợp các ngành truyền thông, quản lý, kinh doanh, đối ngoại hoặc nghệ thuật biểu đạt.",
    childrenLateLife: "Hậu vận rực rỡ, con cháu đông vui thành đạt, cuộc sống tuổi già đầy đủ vật chất lẫn tinh thần."
  },
  7: {
    title: "Thổ Hậu Tàng Kim • Phúc Lộc Bền Lâu",
    overview: "Sinh vào giờ Mùi (13h-15h) khi nắng chiều dịu lại, đất đai ấm áp nuôi dưỡng vạn vật. Người sinh giờ Mùi có hậu phúc lớn, càng về sau càng thịnh vượng.",
    personality: "Điềm đạm, bao dung, chu đáo, kiên trì và có năng khiếu thẩm mỹ cũng như quản lý tài sản rất tốt.",
    careerWealth: "Tài vận ổn định, vượng điền trạch nhà đất, hợp bất động sản, y tế, giáo dục, ẩm thực hoặc quản trị tài chính.",
    childrenLateLife: "Gia đình hòa목, con cái hiếu nghĩa, hậu vận tích lũy được cơ ngơi vững chắc."
  },
  8: {
    title: "Kim Phong Tống Sảng • Ứng Biến Tài Tình",
    overview: "Sinh vào giờ Thân (15h-17h) khi gió thu mát lành, trí tuệ minh mẫn. Người sinh giờ Thân cực kỳ linh hoạt, đa tài và giỏi xoay chuyển cục diện.",
    personality: "Nhanh nhạy, hoạt ngôn, sáng tạo, thích khám phá cái mới và thích nghi nhanh với mọi môi trường.",
    careerWealth: "Đắc lộc phương xa hoặc qua giao thương quốc tế, công nghệ, thương mại điện tử, logistics, tài chính.",
    childrenLateLife: "Con cái năng động, tài giỏi; hậu vận thảnh thơi đi du lịch và tận hưởng thành quả."
  },
  9: {
    title: "Hoàng Hôn Tụ Bảo • Tinh Hoa Kết Trái",
    overview: "Sinh vào giờ Dậu (17h-19h) là thời khắc thu hoạch thành quả sau một ngày dài. Bản mệnh sinh giờ Dậu có số hưởng lộc tinh hoa, làm việc chu toàn tỉ mỉ.",
    personality: "Nguyên tắc, thẩm mỹ tinh tế, trọng chữ tín, cầu toàn và rất biết cách quản lý tiền bạc.",
    careerWealth: "Vượng Thiên Tài & Chính Tài, hợp tài chính ngân hàng, trang sức, luật, kiểm toán, nghệ thuật hoặc kinh doanh cao cấp.",
    childrenLateLife: "Cung Tử Tức tốt đẹp, con cái nên người; hậu vận an nhàn, tài khố sung túc."
  },
  10: {
    title: "Tinh Hà Chiếu Mệnh • Trung Nghĩa Vững Vàng",
    overview: "Sinh vào giờ Tuất (19h-21h) khi đèn hoa lên sáng, gia đình sum họp. Người sinh giờ Tuất sống rất trọng tình nghĩa, có trách nhiệm cao và trực giác tâm linh nhạy bén.",
    personality: "Chính trực, thủy chung, kiên định, biết lo xa và luôn là chỗ dựa vững chắc cho người thân.",
    careerWealth: "Sự nghiệp bền vững nhờ uy tín cá nhân, hợp quản lý, kỹ thuật, y dược, bất động sản hoặc dịch vụ cộng đồng.",
    childrenLateLife: "Vợ chồng đồng lòng, con cháu hiếu thảo quây quần; tuổi già bình an phúc thọ."
  },
  11: {
    title: "Phúc Hải Dung Nạp • An Nhiên Tự Tại",
    overview: "Sinh vào giờ Hợi (21h-23h) khi vạn vật nghỉ ngơi trong sự bình yên. Người sinh giờ Hợi có thiên tính hiền hòa, tấm lòng độ lượng nên dễ chiêu cảm phúc báo lớn.",
    personality: "Nhân hậu, khoan dung, lạc quan, sống tình cảm và có duyên ngầm thu hút quý nhân giúp đỡ.",
    careerWealth: "Lộc tự nhiên tìm đến, ít phải bon chen cực nhọc, hợp thương mại, ẩm thực, du lịch, tài chính hoặc sáng tạo.",
    childrenLateLife: "Con đàn cháu đống, gia đạo ấm êm, hậu vận hưởng trọn phúc lộc thọ."
  }
};

export function buildFourPillarsAndHourFortune(
  lunarDate: LunarDate,
  solarDate: { day: number; month: number; year: number },
  personalProfile: PersonalProfile,
  gender: "male" | "female",
  cungPhi: string,
  cungPhiGroup: "Đông Tứ Mệnh" | "Tây Tứ Mệnh",
  westernZodiac: ZodiacSignInfo,
  numerology: NumerologyInfo,
  saoHan2026: SaoHan2026Info,
  birthHour?: number
): {
  fourPillars: FourPillarsBatTu;
  birthHourFortune: BirthHourFortune;
  deepAnalysis: DeepFortuneAnalysis;
} {
  const jd = lunarDate.julianDay > 0
    ? lunarDate.julianDay
    : jdFromDate(solarDate.day, solarDate.month, solarDate.year);

  const yearCanIdx = ((lunarDate.year + 6) % 10 + 10) % 10;
  const yearChiIdx = ((lunarDate.year + 8) % 12 + 12) % 12;

  const monthChiIdx = (lunarDate.month + 1) % 12;
  const monthCanIdx = ((yearCanIdx % 5) * 2 + 2 + (lunarDate.month - 1)) % 10;

  const dayCanIdx = (jd + 9) % 10;
  const dayChiIdx = (jd + 1) % 12;

  const makePillar = (
    pillarName: string,
    subTitle: string,
    canIdx: number,
    chiIdx: number,
    meaning: string
  ): PillarDetail => {
    const nAm = napAm(canIdx, chiIdx);
    return {
      pillarName,
      subTitle,
      canIndex: canIdx,
      chiIndex: chiIdx,
      can: CAN[canIdx],
      chi: CHI[chiIdx],
      canChi: `${CAN[canIdx]} ${CHI[chiIdx]}`,
      canElement: elementOfCan(canIdx),
      chiElement: elementOfChi(chiIdx),
      napAm: nAm,
      napAmElement: elementOfNapAm(nAm),
      meaning
    };
  };

  const yearPillar = makePillar(
    "Trụ Năm (Niên Trụ)",
    "Cội rễ Tổ tiên • Tiền vận (1 - 16 tuổi)",
    yearCanIdx,
    yearChiIdx,
    "Biểu trưng cho phúc đức gia tộc, nền tảng xuất thân và vận khí thuở thiếu thời."
  );

  const monthPillar = makePillar(
    "Trụ Tháng (Nguyệt Trụ)",
    "Cha mẹ & Huynh đệ • Thanh vận (17 - 32 tuổi)",
    monthCanIdx,
    monthChiIdx,
    "Quyết định khí tiết mùa sinh (Nguyệt Lệnh), môi trường lập nghiệp và sức bật tuổi trẻ."
  );

  const dayPillar = makePillar(
    "Trụ Ngày (Nhật Chủ)",
    "Bản mệnh & Bạn đời • Trung vận (33 - 48 tuổi)",
    dayCanIdx,
    dayChiIdx,
    "Thiên Can ngày là Nhật Chủ (bản thân), Địa Chi ngày là Cung Phu Thê (hôn nhân & nội tâm)."
  );

  const hasValidHour = typeof birthHour === "number" && birthHour >= 0 && birthHour <= 11;
  const effectiveHourChi = hasValidHour ? birthHour : 6; // Mặc định tham chiếu Giờ Ngọ nếu chưa chọn
  const hourCanIdx = getHourCanIndex(dayCanIdx, effectiveHourChi);

  const hourPillar = hasValidHour
    ? makePillar(
        "Trụ Giờ (Thời Trụ)",
        "Con cái & Thành tựu • Hậu vận (49+ tuổi)",
        hourCanIdx,
        effectiveHourChi,
        "Biểu trưng cho cung Tử Tức (con cái), phúc lộc tích lũy, tài sản hậu vận và tuổi già."
      )
    : null;

  // Đếm ngũ hành trong Bát Tự (Thiên Can + Địa Chi của các trụ)
  const counts: Record<FiveElement, number> = {
    [FiveElement.KIM]: 0,
    [FiveElement.MOC]: 0,
    [FiveElement.THUY]: 0,
    [FiveElement.HOA]: 0,
    [FiveElement.THO]: 0
  };

  const activePillars = hourPillar
    ? [yearPillar, monthPillar, dayPillar, hourPillar]
    : [yearPillar, monthPillar, dayPillar];

  activePillars.forEach((p) => {
    counts[p.canElement] += 1;
    counts[p.chiElement] += 1;
  });

  const nhatChuEl = dayPillar.canElement;
  const allElements = [
    FiveElement.KIM,
    FiveElement.MOC,
    FiveElement.THUY,
    FiveElement.HOA,
    FiveElement.THO
  ];

  // Tìm hành sinh cho Nhật Chủ (Ấn Tinh) và hành cùng Nhật Chủ (Tỷ Kiếp)
  const generatingToNhatChu = allElements.find((el) => elementGenerates(el, nhatChuEl)) || nhatChuEl;
  // Hành Nhật Chủ sinh ra (Thực Thương), hành Nhật Chủ khắc (Tài Tinh), hành khắc Nhật Chủ (Quan Sát)
  const generatedByNhatChu = allElements.find((el) => elementGenerates(nhatChuEl, el)) || nhatChuEl;
  const controlledByNhatChu = allElements.find((el) => elementOvercomes(nhatChuEl, el)) || nhatChuEl;
  const controllingNhatChu = allElements.find((el) => elementOvercomes(el, nhatChuEl)) || nhatChuEl;

  const supportScore =
    counts[nhatChuEl] +
    counts[generatingToNhatChu] +
    (monthPillar.chiElement === nhatChuEl || monthPillar.chiElement === generatingToNhatChu ? 1.5 : 0);
  const totalChars = activePillars.length * 2;

  let nhatChuStrength: "Thân Vượng" | "Thân Trung Hòa" | "Thân Nhược" = "Thân Trung Hòa";
  if (supportScore >= totalChars * 0.55) {
    nhatChuStrength = "Thân Vượng";
  } else if (supportScore <= totalChars * 0.38) {
    nhatChuStrength = "Thân Nhược";
  }

  // Hỷ Dụng Thần:
  // - Nếu Thân Nhược: cần Ấn Tinh (sinh扶) và Tỷ Kiếp (trợ thân)
  // - Nếu Thân Vượng: cần Thực Thương (tiết tú), Tài Tinh (hao thân đắc tài), Quan Sát (chế thân)
  // - Nếu Trung Hòa: ưu tiên hành bị khuyết (count === 0) hoặc hành sinh trợ bản mệnh
  const lackingElements = allElements.filter((el) => counts[el] === 0);
  const maxCount = Math.max(...allElements.map((el) => counts[el]));
  const dominantElements = allElements.filter((el) => counts[el] === maxCount);

  let hyDungThan: FiveElement[] = [];
  let kyThan: FiveElement[] = [];

  if (nhatChuStrength === "Thân Nhược") {
    hyDungThan = [generatingToNhatChu, nhatChuEl];
    kyThan = [controllingNhatChu, controlledByNhatChu];
  } else if (nhatChuStrength === "Thân Vượng") {
    hyDungThan = [generatedByNhatChu, controlledByNhatChu, controllingNhatChu].slice(0, 2);
    kyThan = [nhatChuEl, generatingToNhatChu];
  } else {
    hyDungThan = lackingElements.length > 0 ? lackingElements.slice(0, 2) : [generatingToNhatChu, controlledByNhatChu];
    kyThan = dominantElements.filter((el) => !hyDungThan.includes(el)).slice(0, 1);
  }

  const nhatChuElLabel = elementLabel(nhatChuEl);
  const dungThanLabels = hyDungThan.map(elementLabel).join(" & ");
  const lackingLabels = lackingElements.length > 0 ? lackingElements.map(elementLabel).join(", ") : "Không khuyết (Ngũ hành đủ 5 hành)";

  const nhatChuDescription =
    nhatChuStrength === "Thân Vượng"
      ? `Nhật Chủ ${dayPillar.can} (${nhatChuElLabel}) đắc lệnh/đắc trợ nên thuộc cách cục Thân Vượng: Nội lực dồi dào, ý chí tự chủ mạnh mẽ, dám đương đầu thử thách lớn.`
      : nhatChuStrength === "Thân Nhược"
      ? `Nhật Chủ ${dayPillar.can} (${nhatChuElLabel}) thuộc cách cục Thân Nhược: Tính cách tinh tế, linh hoạt, giàu sự thấu cảm; khi có quý nhân, đội ngũ hợp tác và bổ sung Dụng Thần (${dungThanLabels}) sẽ phát huy tối đa tài năng.`
      : `Nhật Chủ ${dayPillar.can} (${nhatChuElLabel}) đạt trạng thái Thân Trung Hòa quý hiếm: Ngũ hành cân bằng, tâm tính điềm đạm, cuộc đời ít biến động cực đoan và dễ đạt sự viên mãn.`;

  const dungThanAdvice = `Bát Tự của bạn có Nhật Chủ là ${dayPillar.can} (${nhatChuElLabel} - ${nhatChuStrength}), hành khuyết trong tứ trụ: ${lackingLabels}. Hỷ Dụng Thần quan trọng nhất để cân bằng mệnh cục và kích hoạt đại vận là hành ${dungThanLabels}. Nên ưu tiên trang phục, phụ kiện, phương hướng và đối tác mang năng lượng hành ${dungThanLabels}.`;

  const fourPillars: FourPillarsBatTu = {
    yearPillar,
    monthPillar,
    dayPillar,
    hourPillar,
    nhatChuCan: dayPillar.can,
    nhatChuElement: nhatChuEl,
    nhatChuStrength,
    nhatChuDescription,
    elementCounts: counts,
    dominantElements,
    lackingElements,
    hyDungThan,
    kyThan,
    dungThanAdvice
  };

  // Luận giải Giờ Sinh (Thời Trụ)
  const hourOption = BIRTH_HOUR_OPTIONS[effectiveHourChi] || BIRTH_HOUR_OPTIONS[6];
  const hdHoursOfDay = new Set(auspiciousHourIndices(dayChiIdx));
  const isHoangDaoBirthHour = hdHoursOfDay.has(effectiveHourChi);
  const ltpBirth = lyThuanPhong(lunarDate.day, lunarDate.month, effectiveHourChi);
  const hourNAp = napAm(hourCanIdx, effectiveHourChi);
  const hourTrait = HOUR_CHI_TRAITS[effectiveHourChi] || HOUR_CHI_TRAITS[6];

  const birthHourFortune: BirthHourFortune = {
    hasBirthHour: hasValidHour,
    hourChiIndex: hasValidHour ? effectiveHourChi : -1,
    hourChi: hourOption.chi,
    hourRange: hourOption.range,
    hourPeriodLabel: hourOption.periodLabel,
    hourCanChi: `${CAN[hourCanIdx]} ${CHI[effectiveHourChi]}`,
    hourNapAm: hourNAp,
    hourNapAmElement: elementOfNapAm(hourNAp),
    isHoangDaoHour: isHoangDaoBirthHour,
    lyThuanPhong: ltpBirth,
    destinyTitle: hourTrait.title,
    coreOverview: hasValidHour
      ? `${hourTrait.overview} Bạn sinh vào giờ ${CAN[hourCanIdx]} ${CHI[effectiveHourChi]} (${hourNAp}), thuộc ${isHoangDaoBirthHour ? "Giờ Hoàng Đạo đại cát" : "Giờ Hắc Đạo cần rèn luyện bản lĩnh"} và trúng cung "${ltpBirth.name}" của Lý Thuần Phong.`
      : `Hiện bạn chưa chọn Giờ Sinh cụ thể. Nếu bổ sung Giờ Sinh (Trụ Giờ - Thời Trụ), hệ thống sẽ hoàn thiện đủ 8 chữ Tứ Trụ Bát Tự để luận giải chính xác cung Tử Tức (con cái) và hậu vận từ 49 tuổi trở đi.`,
    personalityTrait: hourTrait.personality,
    careerWealth: hourTrait.careerWealth,
    childrenAndLateLife: hourTrait.childrenLateLife
  };

  // Tổng hợp Phân Tích Chuyên Sâu Toàn Diện (Gemini 3.8 Flash High • 8 Phương Diện)
  const genderLabel = gender === "male" ? "Nam mạng" : "Nữ mạng";
  const goodDirs = getBatTrachDetails(cungPhi)
    .filter((d) => d.isGood)
    .map((d) => `${d.direction} (${d.type})`)
    .join(", ");

  const deepAspects: DeepFortuneAspect[] = [
    {
      id: "battu_nguhanh",
      title: "1. Tứ Trụ Bát Tự & Cân Bằng Ngũ Hành (Hỷ Dụng Thần)",
      badge: `${nhatChuStrength} • Dụng thần ${dungThanLabels}`,
      icon: "☯️",
      summary: `${nhatChuDescription} ${dungThanAdvice}`,
      details: [
        `• Niên Trụ (Trụ Năm - ${yearPillar.canChi}): Mệnh ${yearPillar.napAm} (${elementLabel(yearPillar.napAmElement)}). Đại diện cho phúc phần tổ tiên và 16 năm đầu đời. Thiên can ${yearPillar.can} (${elementLabel(yearPillar.canElement)}) kết hợp Địa chi ${yearPillar.chi} (${elementLabel(yearPillar.chiElement)}) tạo nền tảng tự lập vững chắc.`,
        `• Nguyệt Trụ (Trụ Tháng - ${monthPillar.canChi}): Mệnh ${monthPillar.napAm} (${elementLabel(monthPillar.napAmElement)}). Nguyệt Lệnh tháng ${lunarDate.month} Âm lịch chi phối trực tiếp khí lực của mệnh cục trong giai đoạn lập nghiệp 17 - 32 tuổi.`,
        `• Nhật Trụ (Nhật Chủ - ${dayPillar.canChi}): Mệnh ${dayPillar.napAm} (${elementLabel(dayPillar.napAmElement)}). Nhật nguyên ${dayPillar.can} thuộc hành ${nhatChuElLabel} tọa trên chi ${dayPillar.chi} (${elementLabel(dayPillar.chiElement)}), phản ánh cốt cách nội tâm và cung Hôn Nhân.`,
        hasValidHour
          ? `• Thời Trụ (Trụ Giờ - ${birthHourFortune.hourCanChi}): Sinh giờ ${hourOption.chi} (${hourOption.range}), nạp âm ${birthHourFortune.hourNapAm}. Đây là cung Quả (kết quả hậu vận & con cái), bổ sung hành ${elementLabel(elementOfCan(hourCanIdx))} và ${elementLabel(elementOfChi(effectiveHourChi))} vào Bát Tự.`
          : `• Thời Trụ (Trụ Giờ): Đang khuyết thông tin giờ sinh. Hãy chọn Giờ Sinh để mở khóa trọn vẹn 8 chữ Bát Tự (Tứ Trụ).`
      ],
      actionableAdvice: `Kích hoạt Hỷ Dụng Thần hành ${dungThanLabels}: sử dụng vật dụng, trang phục tương ứng với hành ${dungThanLabels} và kết giao với người có Bát Tự vượng hành này để bổ khuyết mệnh cục.`
    },
    {
      id: "tinhcach_tamly",
      title: "2. Tâm Lý Chiều Sâu, Khí Chất & Thiên Phú Bẩm Sinh",
      badge: `Nhật chủ ${dayPillar.can} ${nhatChuElLabel} • Số ${numerology.lifePathNumber}`,
      icon: "🧠",
      summary: `Sự giao thoa giữa Nhật Chủ ${dayPillar.can} (${nhatChuElLabel}), bản mệnh ${personalProfile.napAm}, Số Chủ Đạo ${numerology.lifePathNumber} và cung ${westernZodiac.name} tạo nên một ${genderLabel} vừa có chiều sâu nội tâm phương Đông vừa có tư duy quyết đoán hiện đại.`,
      details: [
        `• Cốt cách Nhật Chủ (${dayPillar.can} - ${nhatChuElLabel}): ${
          nhatChuEl === FiveElement.MOC
            ? "Giàu lòng nhân ái, có ý chí vươn lên mạnh mẽ như cây đại thụ, trọng đạo lý và luôn hướng tới sự phát triển."
            : nhatChuEl === FiveElement.HOA
            ? "Nhiệt huyết, lễ nghĩa, thông minh sáng suốt, giàu sức truyền cảm hứng và luôn muốn tỏa sáng."
            : nhatChuEl === FiveElement.THO
            ? "Đôn hậu, trầm ổn, giữ chữ tín vàng ngọc, bao dung và là điểm tựa vững chãi cho tập thể."
            : nhatChuEl === FiveElement.KIM
            ? "Cương nghị, sắc bén, trọng nghĩa khí, làm việc có nguyên tắc rõ ràng và quyết đoán khi xử lý đại sự."
            : "Linh hoạt, trí tuệ thâm sâu, ứng biến khéo léo như dòng nước và có tầm nhìn chiến lược."
        }`,
        `• Ảnh hưởng Giờ Sinh (${hasValidHour ? `Giờ ${hourOption.chi} ${hourOption.range}` : "Tam Trụ"}): ${birthHourFortune.personalityTrait}`,
        `• Giao thoa Đông - Tây: Con số chủ đạo ${numerology.lifePathNumber} (${numerology.title}) kết hợp nguyên tố ${westernZodiac.element} của cung ${westernZodiac.name} giúp bạn sở hữu trực giác nhạy bén cùng khả năng tự phục hồi mạnh mẽ sau thử thách.`
      ],
      actionableAdvice: `Phát huy thế mạnh cốt lõi (${numerology.strengths.slice(0, 2).join(", ")}) và rèn luyện sự cân bằng cảm xúc để tránh tiêu hao năng lượng vào những chi tiết vụn vặt.`
    },
    {
      id: "sunghiep_congdanh",
      title: "3. Công Danh, Sự Nghiệp & Định Hướng Ngành Nghề Đại Cát",
      badge: `Vượng vận 30 - 50 tuổi`,
      icon: "🚀",
      summary: `Dựa trên Tứ Trụ Bát Tự và Dụng Thần ${dungThanLabels}, ${genderLabel} tuổi ${personalProfile.canChi} phát triển rực rỡ nhất khi kết hợp chuyên môn sâu với khả năng quản trị và xây dựng uy tín cá nhân.`,
      details: [
        `• Ngành nghề hợp Dụng Thần (${dungThanLabels}) & Thần Số Học: ${numerology.idealCareers.join(", ")}.`,
        `• Lộ trình thăng tiến theo Trụ Tháng (${monthPillar.canChi}) & Trụ Ngày (${dayPillar.canChi}): Giai đoạn 22 - 30 tuổi là thời kỳ tích lũy kinh nghiệm và mở rộng nhân mạch; từ 32 - 48 tuổi (Trung vận) bước vào đại vận hoàng kim, dễ đảm nhận vị trí quản lý hoặc làm chủ.`,
        `• Quý nhân trợ lực trong công việc: Hợp tác ăn ý nhất với người tuổi ${personalProfile.tamHop.join(", ")} (Tam Hợp) và tuổi ${personalProfile.lucHop} (Lục Hợp).`
      ],
      actionableAdvice: `Khi ký kết hợp đồng lớn hoặc khai trương dự án, hãy chọn ngày Hoàng Đạo thuộc hành ${dungThanLabels} và ngồi quay mặt về hướng Sinh Khí (${getBatTrachDetails(cungPhi)[0]?.direction || "Đông Nam"}).`
    },
    {
      id: "tailoc_dientrach",
      title: "4. Tài Lộc, Điền Trạch (Nhà Đất) & Chiến Lược Tích Sản",
      badge: `Tài tinh hành ${elementLabel(controlledByNhatChu)}`,
      icon: "💰",
      summary: `Trong Bát Tự của bạn, Tài Tinh thuộc hành ${elementLabel(controlledByNhatChu)}. ${birthHourFortune.careerWealth}`,
      details: [
        `• Cấu trúc Chính Tài & Thiên Tài: Bản mệnh ${personalProfile.napAm} kết hợp Nhật Chủ ${dayPillar.canChi} cho thấy dòng tiền chính đến từ năng lực chuyên môn vững vàng, đồng thời có lộc bất ngờ từ đầu tư hoặc bất động sản khi bước vào trung vận.`,
        `• Cung Điền Trạch (Bất động sản): Với Cung Phi ${cungPhi} (${cungPhiGroup}), bạn rất có duyên tích lũy nhà đất. Mua nhà, mở văn phòng hoặc đầu tư đất đai hướng ${goodDirs} sẽ giữ vững tài khố, gia sản sinh sôi.`,
        `• Quản trị rủi ro tài chính: Tránh cho vay mượn thiếu giấy tờ bảo đảm vào các năm/tháng xung tuổi (${personalProfile.tuHanhXung.join(", ")}) để bảo toàn thành quả.`
      ],
      actionableAdvice: `Áp dụng nguyên tắc phân bổ tài sản thành 3 phần: Tích sản dài hạn (bất động sản/vàng), Đầu tư phát triển sự nghiệp theo hành ${dungThanLabels}, và Quỹ dự phòng thanh khoản.`
    },
    {
      id: "tinhduyen_giadao",
      title: "5. Tình Duyên, Hôn Nhân & Phúc Đức Con Cái (Cung Tử Tức)",
      badge: `Cung Phu Thê chi ${dayPillar.chi}`,
      icon: "❤️",
      summary: `Nhật Chi (chi ngày sinh) là ${dayPillar.chi} (${elementLabel(dayPillar.chiElement)}) đóng vai trò Cung Phu Thê; Thời Trụ (${hasValidHour ? birthHourFortune.hourCanChi : "Giờ sinh"}) là Cung Tử Tức biểu thị đường con cái và phúc phần hậu vận.`,
      details: [
        `• Luận Cung Phu Thê (Ngày ${dayPillar.canChi}): Bạn là người trọng tình cảm, khi đã gắn bó thì hết lòng vun đắp cho tổ ấm. Người bạn đời lý tưởng thường có tính cách chín chắn, biết sẻ chia và hỗ trợ đắc lực cho sự nghiệp của bạn.`,
        `• Độ tuổi đại hợp lương duyên: Hòa hợp nhất với tuổi ${personalProfile.tamHop.concat([personalProfile.lucHop]).join(", ")}. Nếu kết duyên với tuổi thuộc nhóm ${personalProfile.tuHanhXung.join(", ")}, hai bên chỉ cần nhường nhịn và dùng phong thủy hướng bếp/giường ngủ chuẩn Bát Trạch là hóa giải êm đẹp.`,
        `• Luận Cung Tử Tức & Hậu Vận theo Giờ Sinh: ${birthHourFortune.childrenAndLateLife}`
      ],
      actionableAdvice: `Đặt giường ngủ quay về hướng Thiên Y (${getBatTrachDetails(cungPhi)[1]?.direction}) hoặc Diên Niên (${getBatTrachDetails(cungPhi)[2]?.direction}) để gia đạo luôn hòa thuận, gắn kết keo sơn.`
    },
    {
      id: "suckhoe_tangphu",
      title: "6. Sức Khỏe Đông Y & Tạng Phủ Theo Ngũ Hành Bát Tự",
      badge: `Chú ý hành ${lackingElements.length > 0 ? lackingElements.map(elementLabel).join(", ") : elementLabel(dominantElements[0])}`,
      icon: "🌿",
      summary: `Theo Y Lý Đông Phương, sự vượng suy của Ngũ Hành trong Bát Tự tương ứng trực tiếp với Ngũ Tạng: Kim (Phế/Hô hấp), Mộc (Can/Gan mật), Thủy (Thận/Thính giác), Hỏa (Tâm/Tim mạch), Thổ (Tỳ Vị/Tiêu hóa).`,
      details: [
        `• Tạng phủ cần lưu tâm: Do Bát Tự của bạn vượng hành ${dominantElements.map(elementLabel).join(", ")} và ${lackingElements.length > 0 ? `khuyết hành ${lackingElements.map(elementLabel).join(", ")}` : "cần cân bằng khí huyết"}, nên chú ý chăm sóc hệ ${
          lackingElements.includes(FiveElement.THUY) || dominantElements.includes(FiveElement.THO)
            ? "thận, tiết niệu, xương khớp và giấc ngủ sâu"
            : lackingElements.includes(FiveElement.HOA) || dominantElements.includes(FiveElement.THUY)
            ? "tuần hoàn tim mạch, huyết áp và giữ ấm cơ thể"
            : lackingElements.includes(FiveElement.MOC) || dominantElements.includes(FiveElement.KIM)
            ? "gan mật, thị lực mắt và giải tỏa căng thẳng thần kinh"
            : lackingElements.includes(FiveElement.KIM)
            ? "hô hấp, phổi, cổ họng và làn da"
            : "tiêu hóa, dạ dày (tỳ vị) và chế độ ăn uống đúng giờ"
        }.`,
        `• Nhịp sinh học theo Giờ Sinh: Hạn chế thức quá khuya sau 23h (Giờ Tý - lúc Đởm và Can tạo huyết), duy trì vận động ngoài trời buổi sáng để hấp thu dương khí.`,
        `• Lưu ý sức khỏe năm 2026: Niên hạn năm nay là hạn ${saoHan2026.han} — ${saoHan2026.hanDescription}`
      ],
      actionableAdvice: `Duy trì chế độ dinh dưỡng cân bằng ngũ sắc, uống đủ nước, tập thở sâu hoặc thể thao 30 phút mỗi ngày để điều hòa âm dương khí huyết.`
    },
    {
      id: "daivan_luunien2026",
      title: "7. Đại Vận Cuộc Đời & Chiến Lược Lưu Niên 2026 (Bính Ngọ)",
      badge: `Sao ${saoHan2026.sao} • Hạn ${saoHan2026.han}`,
      icon: "🔮",
      summary: `${saoHan2026.yearOverview}`,
      details: [
        `• Tương quan Lưu Niên 2026 (Bính Ngọ - Thiên Hà Thủy) với Bản Mệnh (${personalProfile.canChi} - ${personalProfile.napAm}): Năm 2026 mang nạp âm Thiên Hà Thủy (Nước trên trời), ${
          elementGenerates(FiveElement.THUY, personalProfile.element)
            ? "tương sinh trực tiếp cho bản mệnh của bạn — báo hiệu một năm có quý nhân phù trợ, cơ hội mở rộng tầm nhìn và đón nhận tin vui."
            : personalProfile.element === FiveElement.THUY
            ? "tỷ hòa cùng hành Thủy với bản mệnh — vận khí vững vàng, thuận lợi củng cố vị thế và liên kết đối tác."
            : elementOvercomes(personalProfile.element, FiveElement.THUY)
            ? "bản mệnh chế khắc được hành năm — chủ về năm nhiều việc phải gánh vác nhưng càng nỗ lực càng thu hoạch được tài lộc xứng đáng."
            : "đòi hỏi sự kiên nhẫn, lấy nhu thắng cương, chuẩn bị kỹ lưỡng trước mọi quyết định đầu tư lớn."
        }`,
        `• Nhịp vận 4 Quý năm 2026: Tháng đại cát thuận lợi nhất để bứt phá là các tháng ${saoHan2026.favorableMonths.map((m) => `${m} ÂL`).join(", ")}. Các tháng cần giữ tâm bình hòa, cẩn trọng chi tiêu là tháng ${saoHan2026.cautionMonths.map((m) => `${m} ÂL`).join(", ")}.`,
        `• Tầm nhìn Đại Vận 10 năm tới: Khi bước qua mỗi cột mốc 10 năm, sự kết hợp giữa Nhật Chủ ${dayPillar.canChi} và Thời Trụ ${birthHourFortune.hourCanChi} giúp hậu vận của bạn ngày càng ổn định và thịnh vượng.`
      ],
      actionableAdvice: `Đầu năm hoặc đầu mỗi tháng Âm lịch nên làm nhiều việc thiện, giúp đỡ người xung quanh để tăng trưởng phúc đức — 'Đức năng thắng số', hóa hung thành cát.`
    },
    {
      id: "phongthuy_caivan",
      title: "8. Cẩm Nang Phong Thủy Cải Vận Cá Nhân Hóa",
      badge: `Cung ${cungPhi} • ${cungPhiGroup}`,
      icon: "🧭",
      summary: `Tổng hợp pháp khí, màu sắc, phương hướng và khung giờ vàng dành riêng cho ${genderLabel} sinh ${hasValidHour ? `giờ ${hourOption.chi}, ` : ""}ngày ${solarDate.day}/${solarDate.month}/${solarDate.year}.`,
      details: [
        `• Màu sắc Hỷ Dụng Thần & Bản Mệnh: ${personalProfile.luckyColors}. Hạn chế lạm dụng màu: ${personalProfile.unluckyColors}.`,
        `• Phương hướng Đại Cát Bát Trạch (${cungPhi}): ${goodDirs}.`,
        `• Con số may mắn kích hoạt năng lượng: Số chủ đạo ${numerology.lifePathNumber} kết hợp các số ngũ hành ${personalProfile.luckyNumbers.join(", ")}.`,
        `• Khung giờ Hoàng Đạo hợp tuổi xuất hành/giao dịch: Ưu tiên các khung giờ Tam Hợp với tuổi ${CHI[personalProfile.chiIndex]} (Giờ ${personalProfile.tamHop.join(", Giờ ")}) và Giờ Lục Hợp (Giờ ${personalProfile.lucHop}).`
      ],
      actionableAdvice: `Bố trí không gian làm việc gọn gàng, nhiều ánh sáng tự nhiên, đặt một chậu cây xanh hoặc vật phẩm phong thủy hợp hành ${dungThanLabels} ở góc ${getBatTrachDetails(cungPhi)[0]?.direction || "Đông Nam"} để tụ khí sinh tài.`
    }
  ];

  const baseScore =
    78 +
    (isHoangDaoBirthHour ? 5 : 2) +
    (ltpBirth.isGood ? 4 : 1) +
    (saoHan2026.saoType === "Cát tinh" ? 5 : saoHan2026.saoType === "Trung tinh" ? 3 : 1) +
    (nhatChuStrength === "Thân Trung Hòa" ? 4 : 2);
  const overallScore = Math.min(98, Math.max(75, baseScore));

  const deepAnalysis: DeepFortuneAnalysis = {
    modelVersion: "gemini-3.8-flash",
    thinkingMode: "HIGH (Phân tích đa chiều Bát Tự • Tử Vi • Bát Trạch • Thần Số Học)",
    overallHeadline: `${genderLabel} ${personalProfile.canChi} (${personalProfile.napAm}) • Nhật Chủ ${dayPillar.canChi} • ${birthHourFortune.destinyTitle}`,
    overallScore,
    executiveSummary: `Lá số của bạn hội tụ ${hasValidHour ? `đầy đủ Tứ Trụ Bát Tự: Năm ${yearPillar.canChi} — Tháng ${monthPillar.canChi} — Ngày ${dayPillar.canChi} — Giờ ${birthHourFortune.hourCanChi}` : `Tam Trụ: Năm ${yearPillar.canChi} — Tháng ${monthPillar.canChi} — Ngày ${dayPillar.canChi}`}. Nhật Chủ ${dayPillar.can} (${nhatChuElLabel}) thuộc cách cục ${nhatChuStrength}, lấy hành ${dungThanLabels} làm Hỷ Dụng Thần. Kết hợp Cung Phi ${cungPhi} (${cungPhiGroup}) và Số Chủ Đạo ${numerology.lifePathNumber}, bạn là người có chiều sâu trí tuệ, tiền vận rèn luyện bản lĩnh, trung vận và hậu vận phát đạt bền vững cả về danh tiếng lẫn điền sản.`,
    aspects: deepAspects
  };

  return {
    fourPillars,
    birthHourFortune,
    deepAnalysis
  };
}

export function getDetailedBirthFortune(profile: UserBirthProfile): DetailedBirthFortune {
  let solarDay = profile.day;
  let solarMonth = Math.max(1, Math.min(12, profile.month));
  let solarYear = profile.year;

  if (profile.isLunar) {
    const clampedLunarDay = Math.max(1, Math.min(30, profile.day));
    // Thử đổi ngày Âm sang Dương; nếu người dùng chọn ngày 30 trong tháng thiếu (29 ngày), tự động lùi về 29 ÂL
    const solarRes =
      convertLunarToSolar(clampedLunarDay, solarMonth, solarYear, false) ||
      (clampedLunarDay === 30 ? convertLunarToSolar(29, solarMonth, solarYear, false) : null);
    if (solarRes) {
      solarDay = solarRes.day;
      solarMonth = solarRes.month;
      solarYear = solarRes.year;
    }
  } else {
    // Đảm bảo ngày Dương lịch hợp lệ với số ngày thực tế của tháng (VD: tháng 2 có 28/29 ngày)
    const maxSolarDays = new Date(solarYear, solarMonth, 0).getDate();
    solarDay = Math.max(1, Math.min(maxSolarDays, solarDay));
  }

  const lunarDate = getLunarDate(solarDay, solarMonth, solarYear);
  // QUAN TRỌNG: Tử Vi, Bát Trạch, Can Chi năm sinh, Nạp Âm, Con Giáp và Tuổi Mụ phải tính theo Năm Âm Lịch (lunarDate.year)!
  const lunarBirthYear = lunarDate.year;
  const personalProfile = getPersonalProfile(lunarBirthYear);
  const isMale = profile.gender === "male";
  const cungPhi = isMale ? personalProfile.cungMenh.menhNam : personalProfile.cungMenh.menhNu;
  const cungPhiGroup = isMale ? personalProfile.cungMenh.nhomNam : personalProfile.cungMenh.nhomNu;
  const cungPhiElement = cungPhi.includes("Thủy") ? "Thủy" :
                         cungPhi.includes("Hỏa") ? "Hỏa" :
                         cungPhi.includes("Kim") ? "Kim" :
                         cungPhi.includes("Mộc") ? "Mộc" : "Thổ";

  const batTrachDirections = getBatTrachDetails(cungPhi);
  // Cung Hoàng Đạo Phương Tây & Thần Số Học Pythagoras chuẩn quốc tế tính theo ngày Dương lịch
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
  const lunarAge = Math.max(1, 2026 - lunarBirthYear + 1);
  const saoHan2026 = getSaoHan2026(lunarAge, profile.gender);

  const { fourPillars, birthHourFortune, deepAnalysis } = buildFourPillarsAndHourFortune(
    lunarDate,
    { day: solarDay, month: solarMonth, year: solarYear },
    personalProfile,
    profile.gender,
    cungPhi,
    cungPhiGroup,
    westernZodiac,
    numerology,
    saoHan2026,
    profile.birthHour
  );

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
    saoHan2026,
    fourPillars,
    birthHourFortune,
    deepAnalysis
  };
}
