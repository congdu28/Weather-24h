import {
  CAN,
  CHI,
  HOUR_RANGES,
  LunarDate,
  auspiciousHourIndices,
  jdFromDate
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
