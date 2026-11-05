/**
 * Thuật toán Âm lịch Việt Nam (Múi giờ GMT+7 / Kinh tuyến 105° Đông)
 * Dựa trên thuật toán thiên văn học chính thống của TS. Hồ Ngọc Đức (Đại học Leipzig).
 * Tính toán ngoại tuyến 100%, không cần kết nối mạng.
 */

export enum CalendarEventType {
  LUNAR_FESTIVAL = "LUNAR_FESTIVAL",
  SOLAR_HOLIDAY = "SOLAR_HOLIDAY",
  BUDDHIST = "BUDDHIST",
  SOLAR_TERM = "SOLAR_TERM",
  MOON_PHASE = "MOON_PHASE"
}

export interface CalendarEvent {
  name: string;
  emoji: string;
  type: CalendarEventType;
  isMajor?: boolean;
  isPublicHoliday?: boolean;
  description?: string;
  displayName?: string;
}

export interface LunarDate {
  day: number;
  month: number;
  year: number;
  isLeapMonth: boolean;
  canDay: string;
  chiDay: string;
  canMonth: string;
  chiMonth: string;
  canYear: string;
  chiYear: string;
  solarTerm: string;
  isAuspiciousDay: boolean;
  auspiciousDayType: string;
  auspiciousHours: string[];
  festival: string | null;
  monthLength: number;
  events: CalendarEvent[];
  julianDay: number;
  solarDay: number;
  solarMonth: number;
  solarYear: number;
  // Computed helpers
  dayCanChi: string;
  monthCanChi: string;
  yearCanChi: string;
  fullCanChi: string;
  formattedLunarShort: string;
  formattedLunarMedium: string;
  formattedLunarFull: string;
  isFullMonth: boolean;
  monthName: string;
}

export interface MonthDayInfo {
  solarDay: number;
  solarMonth: number;
  solarYear: number;
  dayOfWeek: number; // 0: CN, 1: T2 .. 6: T7
  lunarDate: LunarDate;
  isToday: boolean;
}

const TIME_ZONE = 7.0;

export const CAN = ["Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý"];
export const CHI = ["Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"];

export const SOLAR_TERMS = [
  "Xuân Phân", "Thanh Minh", "Cốc Vũ", "Lập Hạ", "Tiểu Mãn", "Mang Chủng",
  "Hạ Chí", "Tiểu Thử", "Đại Thử", "Lập Thu", "Xử Thử", "Bạch Lộ",
  "Thu Phân", "Hàn Lộ", "Sương Giáng", "Lập Đông", "Tiểu Tuyết", "Đại Tuyết",
  "Đông Chí", "Tiểu Hàn", "Đại Hàn", "Lập Xuân", "Vũ Thủy", "Kinh Trập"
];

const YELLOW_PATH_TYPES = [
  "Thanh Long (Hoàng đạo)",
  "Minh Đường (Hoàng đạo)",
  "Thiên Hình (Hắc đạo)",
  "Chu Tước (Hắc đạo)",
  "Kim Quỹ (Hoàng đạo)",
  "Kim Đường (Hoàng đạo)",
  "Bạch Hổ (Hắc đạo)",
  "Ngọc Đường (Hoàng đạo)",
  "Thiên Lao (Hắc đạo)",
  "Huyền Vũ (Hắc đạo)",
  "Tư Mệnh (Hoàng đạo)",
  "Câu Trận (Hắc đạo)"
];

export const HOUR_RANGES = [
  "23-1h", "1-3h", "3-5h", "5-7h", "7-9h", "9-11h",
  "11-13h", "13-15h", "15-17h", "17-19h", "19-21h", "21-23h"
];

export function lunarMonthName(month: number): string {
  switch (month) {
    case 1: return "Tháng Giêng";
    case 11: return "Tháng Một";
    case 12: return "Tháng Chạp";
    default: return `Tháng ${month}`;
  }
}

// Astronomical helpers
export function jdFromDate(dd: number, mm: number, yy: number): number {
  const a = Math.floor((14 - mm) / 12);
  const y = yy + 4800 - a;
  const m = mm + 12 * a - 3;
  let jd = dd + Math.floor((153 * m + 2) / 5) + 365 * y + Math.floor(y / 4) - Math.floor(y / 100) + Math.floor(y / 400) - 32045;
  if (jd < 2299161) {
    jd = dd + Math.floor((153 * m + 2) / 5) + 365 * y + Math.floor(y / 4) - 32083;
  }
  return jd;
}

export function jdToDate(jd: number): { day: number; month: number; year: number } {
  const a = jd + 32044;
  const b = Math.floor((4 * a + 3) / 146097);
  const c = a - Math.floor((146097 * b) / 4);
  const d = Math.floor((4 * c + 3) / 1461);
  const e = c - Math.floor((1461 * d) / 4);
  const m = Math.floor((5 * e + 2) / 153);
  const day = e - Math.floor((153 * m + 2) / 5) + 1;
  const month = m + 3 - 12 * Math.floor(m / 10);
  const year = 100 * b + d - 4800 + Math.floor(m / 10);
  return { day, month, year };
}

function getSunLongitude(dayNumber: number, timeZone: number): number {
  const t = (dayNumber - 0.5 - timeZone / 24.0 - 2451545.0) / 36525.0;
  const t2 = t * t;
  const dr = Math.PI / 180.0;
  const l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2;
  const m = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2;
  const c = (1.914600 - 0.004817 * t - 0.000014 * t2) * Math.sin(dr * m) +
            (0.019993 - 0.000101 * t) * Math.sin(dr * 2 * m) +
            0.000290 * Math.sin(dr * 3 * m);
  const theta = l0 + c;
  const omega = 125.04 - 1934.136 * t;
  const lambda = theta - 0.00569 - 0.00478 * Math.sin(dr * omega);
  let lambdaDeg = lambda % 360.0;
  if (lambdaDeg < 0.0) lambdaDeg += 360.0;
  return lambdaDeg;
}

function getNewMoonDay(k: number, timeZone: number): number {
  const t = k / 1236.85;
  const t2 = t * t;
  const t3 = t2 * t;
  const dr = Math.PI / 180.0;
  const jd = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3;
  const m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3;
  const mprime = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3;
  const f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3;
  const c1 = (0.1734 - 0.000393 * t) * Math.sin(dr * m) + 0.0021 * Math.sin(dr * 2 * m);
  const c2 = -0.4068 * Math.sin(dr * mprime) + 0.0161 * Math.sin(dr * 2 * mprime);
  const c3 = -0.0004 * Math.sin(dr * 3 * mprime);
  const c4 = 0.0104 * Math.sin(dr * 2 * f) - 0.0051 * Math.sin(dr * (m + mprime));
  const c5 = -0.0074 * Math.sin(dr * (m - mprime)) + 0.0004 * Math.sin(dr * (2 * f + m));
  const c6 = -0.0004 * Math.sin(dr * (2 * f - m)) - 0.0006 * Math.sin(dr * (2 * f + mprime));
  const c7 = 0.0010 * Math.sin(dr * (2 * f - mprime)) + 0.0005 * Math.sin(dr * (2 * mprime + m));
  const deltaT = t < -11.0
    ? 0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t * t3
    : -0.000278 + 0.000265 * t + 0.000262 * t2;
  const jdn = jd + c1 + c2 + c3 + c4 + c5 + c6 + c7 - deltaT;
  return Math.floor(jdn + 0.5 + timeZone / 24.0);
}

function getLunarMonth11(yy: number, timeZone: number): number {
  const off = jdFromDate(31, 12, yy) - 2415021;
  const k = Math.floor(off / 29.530588853);
  let nm = getNewMoonDay(k, timeZone);
  const sunLong = getSunLongitude(nm, timeZone);
  if (sunLong >= 270.0) {
    nm = getNewMoonDay(k - 1, timeZone);
  }
  return nm;
}

function getLeapMonthOffset(a11: number, timeZone: number): number {
  const k = Math.floor((a11 - 2415021.076998695) / 29.530588853 + 0.5);
  let i = 1;
  let arc = Math.floor(getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone) / 30.0);
  let last: number;
  do {
    last = arc;
    i++;
    arc = Math.floor(getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone) / 30.0);
  } while (arc !== last && i < 14);
  return i - 1;
}

function convertSolar2Lunar(dd: number, mm: number, yy: number, timeZone: number): [number, number, number, number, number] {
  const dayNumber = jdFromDate(dd, mm, yy);
  const k = Math.floor((dayNumber - 2415021.076998695) / 29.530588853);
  let monthStart = getNewMoonDay(k + 1, timeZone);
  let nextMonthStart: number;
  if (monthStart > dayNumber) {
    nextMonthStart = monthStart;
    monthStart = getNewMoonDay(k, timeZone);
  } else {
    nextMonthStart = getNewMoonDay(k + 2, timeZone);
  }
  let a11 = getLunarMonth11(yy, timeZone);
  let b11 = a11;
  let lYear: number;
  if (a11 >= monthStart) {
    lYear = yy;
    a11 = getLunarMonth11(yy - 1, timeZone);
  } else {
    lYear = yy + 1;
    b11 = getLunarMonth11(yy + 1, timeZone);
  }
  const lDay = dayNumber - monthStart + 1;
  const diff = Math.floor((monthStart - a11) / 29);
  let lLeap = 0;
  let lMonth = diff + 11;
  if (b11 - a11 > 365) {
    const leapMonthDiff = getLeapMonthOffset(a11, timeZone);
    if (diff >= leapMonthDiff) {
      lMonth = diff + 10;
      if (diff === leapMonthDiff) {
        lLeap = 1;
      }
    }
  }
  if (lMonth > 12) {
    lMonth -= 12;
  }
  if (lMonth >= 11 && diff < 4) {
    lYear -= 1;
  }
  const monthLength = Math.max(29, Math.min(30, nextMonthStart - monthStart));
  return [lDay, lMonth, lYear, lLeap, monthLength];
}

export function getSolarTermIndex(jd: number): number {
  const sunLongitude = getSunLongitude(jd + 1, TIME_ZONE);
  return Math.floor(sunLongitude / 15.0) % 24;
}

export function getSolarMonthChiIndex(jd: number): number {
  const sunLongitude = getSunLongitude(jd + 1, TIME_ZONE);
  const sector = Math.floor(((sunLongitude - 315.0 + 360.0) % 360.0) / 30.0);
  return (sector + 2) % 12;
}

function calculateAuspiciousDay(chiMonth: string, chiDay: string): [boolean, string] {
  const monthChiIdx = Math.max(0, CHI.indexOf(chiMonth));
  const dayChiIdx = Math.max(0, CHI.indexOf(chiDay));

  let startChiIdx = 0;
  switch (monthChiIdx) {
    case 2: case 8: startChiIdx = 0; break;
    case 3: case 9: startChiIdx = 2; break;
    case 4: case 10: startChiIdx = 4; break;
    case 5: case 11: startChiIdx = 6; break;
    case 0: case 6: startChiIdx = 8; break;
    case 1: case 7: startChiIdx = 10; break;
    default: startChiIdx = 0;
  }

  const step = (dayChiIdx - startChiIdx + 12) % 12;
  const type = YELLOW_PATH_TYPES[step];
  const isAuspicious = type.includes("Hoàng đạo");
  return [isAuspicious, type];
}

export function auspiciousHourIndices(dayChiIdx: number): number[] {
  switch (dayChiIdx % 6) {
    case 0: return [0, 1, 3, 6, 8, 9];
    case 1: return [2, 3, 5, 8, 10, 11];
    case 2: return [0, 1, 4, 5, 7, 10];
    case 3: return [0, 2, 3, 6, 7, 9];
    case 4: return [2, 4, 5, 8, 9, 11];
    default: return [1, 4, 6, 7, 10, 11];
  }
}

function calculateAuspiciousHours(chiDay: string): string[] {
  const dayChiIdx = Math.max(0, CHI.indexOf(chiDay));
  return auspiciousHourIndices(dayChiIdx).map(idx => `${CHI[idx]} (${HOUR_RANGES[idx]})`);
}

function lunarFestival(d: number, m: number, monthLength: number): CalendarEvent | null {
  const t = CalendarEventType.LUNAR_FESTIVAL;
  if (d === 1 && m === 1) return { name: "Tết Nguyên Đán (Mùng 1)", emoji: "🌸", type: t, isMajor: true, isPublicHoliday: true, description: "Ngày đầu năm mới Âm lịch – Tết lớn nhất của người Việt." };
  if (d === 2 && m === 1) return { name: "Mùng 2 Tết", emoji: "🌸", type: t, isMajor: true, isPublicHoliday: true, description: "Mùng 1 Tết cha, mùng 2 Tết mẹ, mùng 3 Tết thầy." };
  if (d === 3 && m === 1) return { name: "Mùng 3 Tết", emoji: "🌸", type: t, isMajor: true, isPublicHoliday: true, description: "Ngày Tết thầy, hóa vàng tiễn ông bà." };
  if (d === 10 && m === 1) return { name: "Vía Thần Tài", emoji: "💰", type: t, isMajor: true, description: "Cầu may mắn, tài lộc cả năm." };
  if (d === 15 && m === 1) return { name: "Tết Nguyên Tiêu (Rằm tháng Giêng)", emoji: "🏮", type: t, isMajor: true, description: "Lễ Phật quanh năm không bằng Rằm tháng Giêng." };
  if (d === 3 && m === 3) return { name: "Tết Hàn Thực", emoji: "🥟", type: t, isMajor: true, description: "Ngày làm bánh trôi, bánh chay cúng tổ tiên." };
  if (d === 10 && m === 3) return { name: "Giỗ Tổ Hùng Vương", emoji: "👑", type: t, isMajor: true, isPublicHoliday: true, description: "Tưởng nhớ các Vua Hùng – nghỉ lễ chính thức." };
  if (d === 15 && m === 4) return { name: "Lễ Phật Đản", emoji: "🪷", type: t, isMajor: true, description: "Kỷ niệm ngày Đức Phật Thích Ca đản sinh." };
  if (d === 5 && m === 5) return { name: "Tết Đoan Ngọ", emoji: "🌾", type: t, isMajor: true, description: "Tết diệt sâu bọ, ăn hoa quả đầu mùa." };
  if (d === 15 && m === 7) return { name: "Lễ Vu Lan & Xá Tội Vong Nhân", emoji: "🏮", type: t, isMajor: true, description: "Mùa báo hiếu cha mẹ, cúng cô hồn." };
  if (d === 15 && m === 8) return { name: "Tết Trung Thu", emoji: "🥮", type: t, isMajor: true, description: "Tết đoàn viên, rước đèn phá cỗ thiếu nhi." };
  if (d === 23 && m === 12) return { name: "Tết Ông Công Ông Táo", emoji: "🐟", type: t, isMajor: true, description: "Tiễn Táo Quân về trời, thả cá chép." };
  if (d === monthLength && m === 12) return { name: "Tất Niên – Đêm Giao Thừa", emoji: "🎆", type: t, isMajor: true, isPublicHoliday: true, description: "Đón giao thừa chào đón năm mới." };
  return null;
}

function solarHoliday(d: number, m: number): CalendarEvent | null {
  const t = CalendarEventType.SOLAR_HOLIDAY;
  const key = m * 100 + d;
  switch (key) {
    case 101: return { name: "Tết Dương lịch", emoji: "🎉", type: t, isMajor: true, isPublicHoliday: true, description: "Nghỉ lễ chính thức." };
    case 214: return { name: "Lễ Tình nhân (Valentine)", emoji: "💝", type: t, isMajor: true };
    case 308: return { name: "Quốc tế Phụ nữ", emoji: "💐", type: t, isMajor: true };
    case 430: return { name: "Giải phóng miền Nam", emoji: "🇻🇳", type: t, isMajor: true, isPublicHoliday: true, description: "Thống nhất đất nước." };
    case 501: return { name: "Quốc tế Lao động", emoji: "🛠️", type: t, isMajor: true, isPublicHoliday: true };
    case 601: return { name: "Quốc tế Thiếu nhi", emoji: "🎈", type: t, isMajor: true };
    case 902: return { name: "Quốc khánh", emoji: "🇻🇳", type: t, isMajor: true, isPublicHoliday: true, description: "Quốc khánh nước CHXHCN Việt Nam." };
    case 1020: return { name: "Ngày Phụ nữ Việt Nam", emoji: "💐", type: t, isMajor: true };
    case 1120: return { name: "Ngày Nhà giáo Việt Nam", emoji: "📚", type: t, isMajor: true };
    case 1224: return { name: "Đêm Giáng sinh", emoji: "🎄", type: t, isMajor: true };
    case 1225: return { name: "Lễ Giáng sinh", emoji: "🎄", type: t, isMajor: true };
    default: return null;
  }
}

function buildEvents(sDay: number, sMonth: number, lDay: number, lMonth: number, isLeap: boolean, monthLength: number, jd: number): CalendarEvent[] {
  const list: CalendarEvent[] = [];
  if (!isLeap) {
    const lFest = lunarFestival(lDay, lMonth, monthLength);
    if (lFest) list.push({ ...lFest, displayName: `${lFest.emoji} ${lFest.name}` });
  }
  const sHol = solarHoliday(sDay, sMonth);
  if (sHol) list.push({ ...sHol, displayName: `${sHol.emoji} ${sHol.name}` });

  const termToday = getSolarTermIndex(jd);
  const termYesterday = getSolarTermIndex(jd - 1);
  if (termToday !== termYesterday) {
    const name = SOLAR_TERMS[termToday];
    list.push({
      name: `Bắt đầu tiết ${name}`,
      emoji: "☀️",
      type: CalendarEventType.SOLAR_TERM,
      displayName: `☀️ Tiết ${name}`
    });
  }

  if (lDay === 15) {
    list.push({
      name: "Ngày Rằm (Vọng)",
      emoji: "🌕",
      type: CalendarEventType.MOON_PHASE,
      description: "Ngày trăng tròn, thắp hương, ăn chay, đi chùa.",
      displayName: "🌕 Ngày Rằm"
    });
  }
  if (lDay === 1) {
    list.push({
      name: "Ngày Mùng Một (Sóc)",
      emoji: "🌑",
      type: CalendarEventType.MOON_PHASE,
      description: "Đầu tháng Âm lịch, thắp hương cầu an.",
      displayName: "🌑 Ngày Mùng Một"
    });
  }
  return list;
}

export function getLunarDate(dd: number, mm: number, yy: number): LunarDate {
  const [lDay, lMonth, lYear, lLeapRaw, monthLength] = convertSolar2Lunar(dd, mm, yy, TIME_ZONE);
  const isLeap = lLeapRaw === 1;
  const jd = jdFromDate(dd, mm, yy);

  const canYear = CAN[(lYear + 6) % 10];
  const chiYear = CHI[(lYear + 8) % 12];
  const canDay = CAN[(jd + 9) % 10];
  const chiDay = CHI[(jd + 1) % 12];
  const canMonth = CAN[(lYear * 12 + lMonth + 3) % 10];
  const chiMonth = CHI[(lMonth + 1) % 12];

  const termIdx = getSolarTermIndex(jd);
  const solarTerm = SOLAR_TERMS[termIdx];

  const [isAuspicious, auspiciousType] = calculateAuspiciousDay(chiMonth, chiDay);
  const auspiciousHours = calculateAuspiciousHours(chiDay);
  const events = buildEvents(dd, mm, lDay, lMonth, isLeap, monthLength, jd);
  const primaryFest = events.find(e => e.isMajor || e.type === CalendarEventType.LUNAR_FESTIVAL);

  const dayCanChi = `${canDay} ${chiDay}`;
  const monthCanChi = `${canMonth} ${chiMonth}`;
  const yearCanChi = `${canYear} ${chiYear}`;

  return {
    day: lDay,
    month: lMonth,
    year: lYear,
    isLeapMonth: isLeap,
    canDay,
    chiDay,
    canMonth,
    chiMonth,
    canYear,
    chiYear,
    solarTerm,
    isAuspiciousDay: isAuspicious,
    auspiciousDayType: auspiciousType,
    auspiciousHours,
    festival: primaryFest ? primaryFest.displayName || primaryFest.name : null,
    monthLength,
    events,
    julianDay: jd,
    solarDay: dd,
    solarMonth: mm,
    solarYear: yy,
    dayCanChi,
    monthCanChi,
    yearCanChi,
    fullCanChi: `Năm ${yearCanChi} • Tháng ${monthCanChi} • Ngày ${dayCanChi}`,
    formattedLunarShort: `${lDay}/${lMonth}${isLeap ? " (N)" : ""} ÂL`,
    formattedLunarMedium: `Ngày ${lDay}/${lMonth}${isLeap ? " (Nhuận)" : ""} ÂL (${dayCanChi})`,
    formattedLunarFull: `Ngày ${lDay} tháng ${lMonth}${isLeap ? " nhuận" : ""} năm ${yearCanChi}`,
    isFullMonth: monthLength >= 30,
    monthName: `${lunarMonthName(lMonth)}${isLeap ? " (nhuận)" : ""}`
  };
}

export function getTodayLunar(): LunarDate {
  const now = new Date();
  return getLunarDate(now.getDate(), now.getMonth() + 1, now.getFullYear());
}

export function convertLunarToSolar(lDay: number, lMonth: number, lYear: number, isLeap = false): { day: number; month: number; year: number } | null {
  if (lMonth < 1 || lMonth > 12 || lDay < 1 || lDay > 30) return null;
  let a11: number;
  let b11: number;
  if (lMonth < 11) {
    a11 = getLunarMonth11(lYear - 1, TIME_ZONE);
    b11 = getLunarMonth11(lYear, TIME_ZONE);
  } else {
    a11 = getLunarMonth11(lYear, TIME_ZONE);
    b11 = getLunarMonth11(lYear + 1, TIME_ZONE);
  }
  const k = Math.floor(0.5 + (a11 - 2415021.076998695) / 29.530588853);
  let off = lMonth - 11;
  if (off < 0) off += 12;
  if (b11 - a11 > 365) {
    const leapOff = getLeapMonthOffset(a11, TIME_ZONE);
    let leapMonth = leapOff - 2;
    if (leapMonth <= 0) leapMonth += 12;
    if (isLeap && lMonth !== leapMonth) return null;
    if (isLeap || off >= leapOff) off += 1;
  } else if (isLeap) {
    return null;
  }
  const monthStart = getNewMoonDay(k + off, TIME_ZONE);
  const nextMonthStart = getNewMoonDay(k + off + 1, TIME_ZONE);
  if (lDay > nextMonthStart - monthStart) return null;
  return jdToDate(monthStart + lDay - 1);
}

export function getMonthDays(year: number, month: number): MonthDayInfo[] {
  const daysInMonth = new Date(year, month, 0).getDate();
  const today = new Date();
  const todayD = today.getDate();
  const todayM = today.getMonth() + 1;
  const todayY = today.getFullYear();

  const result: MonthDayInfo[] = [];
  for (let d = 1; d <= daysInMonth; d++) {
    const dateObj = new Date(year, month - 1, d);
    const dayOfWeek = dateObj.getDay();
    const isToday = d === todayD && month === todayM && year === todayY;
    const lunar = getLunarDate(d, month, year);
    result.push({
      solarDay: d,
      solarMonth: month,
      solarYear: year,
      dayOfWeek,
      lunarDate: lunar,
      isToday
    });
  }
  return result;
}
