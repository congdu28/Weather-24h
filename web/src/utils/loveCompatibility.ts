import { FiveElement, FIVE_ELEMENT_META, DetailedBirthFortune, UserBirthProfile, elementGenerates, elementLabel, elementOvercomes, getDetailedBirthFortune, isBreak, isCanClash, isCanHarmony, isClash, isHarm, isPunish, isSixHarmony, isTripleHarmony } from "./lunarAlmanac";

export interface LovePersonInput {
  name: string;
  gender: "male" | "female";
  day: number;
  month: number;
  year: number;
  birthHour: number; // 0..11, -1 nếu chưa rõ giờ
  isLunar: boolean;
}

export type CompatibilityStatus = "excellent" | "good" | "neutral" | "caution";

export interface LoveDimensionResult {
  id: string;
  title: string;
  icon: string;
  score: number;
  maxScore: number;
  status: CompatibilityStatus;
  badge: string;
  summary: string;
  details: string[];
  advice: string;
}

export interface LovePersonSummary extends DetailedBirthFortune {
  displayName: string;
}

export interface LoveCompatibilityResult {
  person1: LovePersonSummary;
  person2: LovePersonSummary;
  totalScore: number;
  verdictTitle: string;
  verdictBadge: string;
  summary: string;
  dimensions: LoveDimensionResult[];
  complementaryElements: string[];
  fengShuiAdvice: string[];
}

export interface DeepLoveAspect {
  id: string;
  title: string;
  badge: string;
  icon: string;
  summary: string;
  details: string[];
  advice: string;
}

export interface DeepLoveAiAnalysis {
  headline: string;
  compatibilityScore: number;
  executiveSummary: string;
  aspects: DeepLoveAspect[];
}

// Bảng Bát Trạch Cung Phi (Càn, Khảm, Cấn, Chấn, Tốn, Ly, Khôn, Đoài)
const CUNG_KEYS = ["Càn", "Khảm", "Cấn", "Chấn", "Tốn", "Ly", "Khôn", "Đoài"] as const;
type CungKey = (typeof CUNG_KEYS)[number];

interface BatTrachMatch {
  name: "Sinh Khí" | "Thiên Y" | "Diên Niên" | "Phục Vị" | "Tuyệt Mệnh" | "Ngũ Quỷ" | "Lục Sát" | "Họa Hại";
  type: "good" | "bad";
  score: number;
  meaning: string;
}

const BAT_TRACH_INFO: Record<BatTrachMatch["name"], Omit<BatTrachMatch, "name">> = {
  "Sinh Khí": {
    type: "good",
    score: 20,
    meaning: "Đại cát trong hôn nhân: vượng tài lộc, thăng tiến sự nghiệp, gia đạo hưng thịnh."
  },
  "Diên Niên": {
    type: "good",
    score: 19,
    meaning: "Rất tốt cho vợ chồng (Phúc Đức): tình cảm gắn bó bền chặt, hòa thuận, con cái hiếu thảo."
  },
  "Thiên Y": {
    type: "good",
    score: 18,
    meaning: "Cát lợi về sức khỏe, bình an, vợ chồng nâng đỡ nhau vượt qua sóng gió."
  },
  "Phục Vị": {
    type: "good",
    score: 15,
    meaning: "Bình ổn, nhẹ nhàng, hợp sự đồng hành bền bỉ và hỗ trợ tinh thần."
  },
  "Họa Hại": {
    type: "bad",
    score: 9,
    meaning: "Dễ phát sinh bất đồng nhỏ trong lời nói, cần nhường nhịn và chọn hướng nhà cát lợi để hóa giải."
  },
  "Lục Sát": {
    type: "bad",
    score: 8,
    meaning: "Tình cảm dễ chịu tác động từ bên ngoài hoặc hiểu lầm, cần minh bạch và giữ vững niềm tin."
  },
  "Ngũ Quỷ": {
    type: "bad",
    score: 6,
    meaning: "Dễ nóng nảy, khắc khẩu nếu không tiết chế cái tôi; nên dung hòa bằng Ngũ Hành và không gian sống."
  },
  "Tuyệt Mệnh": {
    type: "bad",
    score: 5,
    meaning: "Xung khắc mạnh theo Cung Phi Bát Trạch, cần chú ý hóa giải phong thủy (hướng bếp, hướng phòng ngủ, màu sắc) và lấy chữ Nhẫn làm đầu."
  }
};

// Ma trận Bát Du Niên chuẩn (hàng = Cung 1, cột = Cung 2)
const BAT_DU_NIEN_MATRIX: Record<CungKey, Record<CungKey, BatTrachMatch["name"]>> = {
  "Càn": {
    "Càn": "Phục Vị", "Khảm": "Lục Sát", "Cấn": "Thiên Y", "Chấn": "Ngũ Quỷ",
    "Tốn": "Họa Hại", "Ly": "Tuyệt Mệnh", "Khôn": "Diên Niên", "Đoài": "Sinh Khí"
  },
  "Khảm": {
    "Càn": "Lục Sát", "Khảm": "Phục Vị", "Cấn": "Ngũ Quỷ", "Chấn": "Thiên Y",
    "Tốn": "Sinh Khí", "Ly": "Diên Niên", "Khôn": "Tuyệt Mệnh", "Đoài": "Họa Hại"
  },
  "Cấn": {
    "Càn": "Thiên Y", "Khảm": "Ngũ Quỷ", "Cấn": "Phục Vị", "Chấn": "Lục Sát",
    "Tốn": "Tuyệt Mệnh", "Ly": "Họa Hại", "Khôn": "Sinh Khí", "Đoài": "Diên Niên"
  },
  "Chấn": {
    "Càn": "Ngũ Quỷ", "Khảm": "Thiên Y", "Cấn": "Lục Sát", "Chấn": "Phục Vị",
    "Tốn": "Diên Niên", "Ly": "Sinh Khí", "Khôn": "Họa Hại", "Đoài": "Tuyệt Mệnh"
  },
  "Tốn": {
    "Càn": "Họa Hại", "Khảm": "Sinh Khí", "Cấn": "Tuyệt Mệnh", "Chấn": "Diên Niên",
    "Tốn": "Phục Vị", "Ly": "Thiên Y", "Khôn": "Ngũ Quỷ", "Đoài": "Lục Sát"
  },
  "Ly": {
    "Càn": "Tuyệt Mệnh", "Khảm": "Diên Niên", "Cấn": "Họa Hại", "Chấn": "Sinh Khí",
    "Tốn": "Thiên Y", "Ly": "Phục Vị", "Khôn": "Lục Sát", "Đoài": "Ngũ Quỷ"
  },
  "Khôn": {
    "Càn": "Diên Niên", "Khảm": "Tuyệt Mệnh", "Cấn": "Sinh Khí", "Chấn": "Họa Hại",
    "Tốn": "Ngũ Quỷ", "Ly": "Lục Sát", "Khôn": "Phục Vị", "Đoài": "Thiên Y"
  },
  "Đoài": {
    "Càn": "Sinh Khí", "Khảm": "Họa Hại", "Cấn": "Diên Niên", "Chấn": "Tuyệt Mệnh",
    "Tốn": "Lục Sát", "Ly": "Ngũ Quỷ", "Khôn": "Thiên Y", "Đoài": "Phục Vị"
  }
};

function extractCungKey(cungFull: string): CungKey {
  for (const key of CUNG_KEYS) {
    if (cungFull.includes(key)) return key;
  }
  return "Càn";
}

function evaluateMenhNapAm(p1: LovePersonSummary, p2: LovePersonSummary): LoveDimensionResult {
  const el1 = p1.personalProfile.element;
  const el2 = p2.personalProfile.element;
  const label1 = `${FIVE_ELEMENT_META[el1].emoji} ${elementLabel(el1)} (${p1.personalProfile.napAm})`;
  const label2 = `${FIVE_ELEMENT_META[el2].emoji} ${elementLabel(el2)} (${p2.personalProfile.napAm})`;

  let score = 14;
  let status: CompatibilityStatus = "good";
  let badge = "Bình Hòa";
  let summary = "";
  let advice = "";

  if (elementGenerates(el1, el2) || elementGenerates(el2, el1)) {
    score = 20;
    status = "excellent";
    badge = "Tương Sinh";
    const giver = elementGenerates(el1, el2) ? p1.displayName : p2.displayName;
    const receiver = elementGenerates(el1, el2) ? p2.displayName : p1.displayName;
    summary = `Bản mệnh ${giver} tương sinh cho ${receiver}, tạo nền tảng nâng đỡ tự nhiên và bền vững.`;
    advice = "Hai bạn có sự bồi đắp tự nhiên về năng lượng. Hãy trân trọng sự hy sinh thầm lặng của đối phương.";
  } else if (el1 === el2) {
    score = 16;
    status = "good";
    badge = "Đồng Hành (Lưỡng Mệnh)";
    summary = `Cả hai cùng thuộc hành ${elementLabel(el1)}, dễ đồng cảm về tư duy, nhịp sống và giá trị cốt lõi.`;
    advice = "Khi có chung tần số, hãy tạo thêm không gian mới mẻ để tránh lặp lại hoặc cùng nóng nảy một lúc.";
  } else if (elementOvercomes(el1, el2) || elementOvercomes(el2, el1)) {
    score = 8;
    status = "caution";
    badge = "Tương Khắc";
    const stronger = elementOvercomes(el1, el2) ? p1.displayName : p2.displayName;
    const weaker = elementOvercomes(el1, el2) ? p2.displayName : p1.displayName;
    summary = `Mệnh của ${stronger} khắc nhẹ mệnh ${weaker}, dễ xảy ra khác biệt quan điểm nếu thiếu lắng nghe.`;
    advice = "Nên dùng hành trung gian (hành được sinh bởi bên khắc và sinh cho bên bị khắc) trong màu sắc, trang phục hoặc nội thất để dung hòa.";
  } else {
    score = 14;
    status = "neutral";
    badge = "Bình Hòa";
    summary = "Hai bản mệnh không sinh không khắc trực tiếp, mối quan hệ phát triển phụ thuộc nhiều vào sự vun đắp thực tế.";
    advice = "Chủ động giao tiếp rõ ràng và đặt mục tiêu chung sẽ giúp tình cảm tiến triển vững chắc.";
  }

  return {
    id: "nap_am",
    title: "1. Ngũ Hành Bản Mệnh (Nạp Âm)",
    icon: "🔥",
    score,
    maxScore: 20,
    status,
    badge,
    summary,
    details: [
      `${p1.displayName}: ${label1}`,
      `${p2.displayName}: ${label2}`
    ],
    advice
  };
}

function evaluateCanChiYear(p1: LovePersonSummary, p2: LovePersonSummary): LoveDimensionResult {
  const y1 = p1.personalProfile;
  const y2 = p2.personalProfile;
  const canHarm = isCanHarmony(y1.canIndex, y2.canIndex);
  const canClash = isCanClash(y1.canIndex, y2.canIndex);
  const chiSix = isSixHarmony(y1.chiIndex, y2.chiIndex);
  const chiTri = isTripleHarmony(y1.chiIndex, y2.chiIndex);
  const chiClash = isClash(y1.chiIndex, y2.chiIndex);
  const chiHarm = isHarm(y1.chiIndex, y2.chiIndex);
  const chiPunish = isPunish(y1.chiIndex, y2.chiIndex);
  const chiBreak = isBreak(y1.chiIndex, y2.chiIndex);

  let score = 13;
  const notes: string[] = [
    `${p1.displayName}: Tuổi ${y1.canChi} (${y1.zodiacAnimal})`,
    `${p2.displayName}: Tuổi ${y2.canChi} (${y2.zodiacAnimal})`
  ];

  if (canHarm) {
    score += 4;
    notes.push("Thiên Can tương hợp: tư duy và định hướng tương lai dễ thống nhất.");
  } else if (canClash) {
    score -= 3;
    notes.push("Thiên Can tương xung: đôi lúc bất đồng trong cách ra quyết định.");
  }

  if (chiSix) {
    score += 5;
    notes.push("Địa Chi Lục Hợp: duyên phận gắn kết sâu sắc, dễ thấu hiểu nhau.");
  } else if (chiTri) {
    score += 4;
    notes.push("Địa Chi Tam Hợp: đồng điệu về lối sống, dễ hỗ trợ nhau phát triển.");
  } else if (chiClash) {
    score -= 5;
    notes.push("Địa Chi Lục Xung: dễ xảy ra va chạm quan điểm nếu cả hai cùng cứng rắn.");
  } else if (chiHarm) {
    score -= 3;
    notes.push("Địa Chi Lục Hại: dễ có hiểu lầm nhỏ tích tụ nếu không chia sẻ kịp thời.");
  } else if (chiPunish || chiBreak) {
    score -= 2;
    notes.push("Địa Chi Hình/Phá nhẹ: cần tiết chế cái tôi khi tranh luận.");
  } else {
    notes.push("Địa Chi bình hòa: không phạm xung khắc lớn.");
  }

  score = Math.max(4, Math.min(20, score));
  const status: CompatibilityStatus =
    score >= 17 ? "excellent" : score >= 13 ? "good" : score >= 9 ? "neutral" : "caution";
  const badge =
    chiSix ? "Lục Hợp" : chiTri ? "Tam Hợp" : chiClash ? "Lục Xung" : chiHarm ? "Lục Hại" : canHarm ? "Can Hợp" : "Bình Hòa";

  return {
    id: "can_chi",
    title: "2. Tuổi Can Chi & Con Giáp",
    icon: "🐉",
    score,
    maxScore: 20,
    status,
    badge,
    summary:
      score >= 17
        ? "Tuổi của hai bạn nằm trong cặp hợp tự nhiên, rất thuận lợi cho tình yêu lâu dài và hôn nhân."
        : score >= 12
        ? "Can Chi hài hòa, có nền tảng ổn định để cùng nhau xây dựng mối quan hệ bền vững."
        : "Tuổi có điểm xung khắc nhẹ theo truyền thống, nhưng hoàn toàn có thể hóa giải bằng sự thấu hiểu và tôn trọng.",
    details: notes,
    advice:
      chiClash || chiHarm
        ? "Khi mâu thuẫn xảy ra, một người nên chủ động hạ nhiệt trước để tránh tranh cãi kéo dài."
        : "Duy trì thói quen trò chuyện cởi mở giúp phát huy tối đa điểm hợp tuổi."
  };
}

function evaluateCungPhi(p1: LovePersonSummary, p2: LovePersonSummary): LoveDimensionResult {
  const c1 = extractCungKey(p1.cungPhi);
  const c2 = extractCungKey(p2.cungPhi);
  const matchName = BAT_DU_NIEN_MATRIX[c1][c2];
  const info = BAT_TRACH_INFO[matchName];

  const status: CompatibilityStatus =
    info.score >= 18 ? "excellent" : info.score >= 14 ? "good" : info.score >= 9 ? "neutral" : "caution";

  return {
    id: "cung_phi",
    title: "3. Cung Phi Bát Trạch (Hôn Nhân)",
    icon: "🧭",
    score: info.score,
    maxScore: 20,
    status,
    badge: matchName,
    summary: `${p1.cungPhi} kết hợp ${p2.cungPhi} tạo thành cung ${matchName}. ${info.meaning}`,
    details: [
      `${p1.displayName}: ${p1.cungPhi} (${p1.cungPhiGroup})`,
      `${p2.displayName}: ${p2.cungPhi} (${p2.cungPhiGroup})`,
      `Kết quả Bát Du Niên: ${matchName} (${info.type === "good" ? "Cát" : "Cần hóa giải"})`
    ],
    advice:
      info.type === "good"
        ? "Cung Phi rất thuận cho việc cưới hỏi, chọn hướng nhà và bố trí phòng ngủ vợ chồng."
        : "Trong phong thủy Bát Trạch, có thể hóa giải bằng cách đặt hướng bếp theo cung tốt (Sinh Khí/Thiên Y) của gia chủ."
  };
}

function evaluateFourPillars(p1: LovePersonSummary, p2: LovePersonSummary): LoveDimensionResult {
  const d1 = p1.fourPillars.dayPillar;
  const d2 = p2.fourPillars.dayPillar;
  const h1 = p1.fourPillars.hourPillar;
  const h2 = p2.fourPillars.hourPillar;

  let score = 12;
  const details: string[] = [
    `Trụ Ngày (Nhật Chủ & Cung Phu Thê): ${p1.displayName} (${d1.canChi}) — ${p2.displayName} (${d2.canChi})`
  ];

  // So sánh Nhật Chủ (Can Ngày)
  if (isCanHarmony(d1.canIndex, d2.canIndex)) {
    score += 4;
    details.push("Nhật Chủ Thiên Can Hợp: tâm hồn gắn bó tự nhiên, dễ nhường nhịn nhau.");
  } else if (elementGenerates(d1.canElement, d2.canElement) || elementGenerates(d2.canElement, d1.canElement)) {
    score += 3;
    details.push("Nhật Chủ Tương Sinh: một người là chỗ dựa tinh thần vững chắc cho người còn lại.");
  } else if (isCanClash(d1.canIndex, d2.canIndex)) {
    score -= 2;
    details.push("Nhật Chủ có nét đối lập: cá tính mạnh, cần học cách thỏa hiệp trong sinh hoạt chung.");
  }

  // So sánh Chi Ngày (Cung Phu Thê)
  if (isSixHarmony(d1.chiIndex, d2.chiIndex) || isTripleHarmony(d1.chiIndex, d2.chiIndex)) {
    score += 3;
    details.push("Cung Phu Thê (Chi Ngày) Hợp: đời sống vợ chồng ấm áp, đồng điệu về cảm xúc.");
  } else if (isClash(d1.chiIndex, d2.chiIndex)) {
    score -= 2;
    details.push("Cung Phu Thê có xung nhẹ: cần chú ý cách giao tiếp trong chuyện gia đình.");
  }

  // So sánh Trụ Giờ (Hậu vận & Con cái)
  if (p1.birthHourFortune.hasBirthHour && p2.birthHourFortune.hasBirthHour && h1 && h2) {
    details.push(`Trụ Giờ (Hậu vận): ${p1.displayName} (${h1.canChi}) — ${p2.displayName} (${h2.canChi})`);
    if (isSixHarmony(h1.chiIndex, h2.chiIndex) || isTripleHarmony(h1.chiIndex, h2.chiIndex) || isCanHarmony(h1.canIndex, h2.canIndex)) {
      score += 2;
      details.push("Trụ Giờ tương hợp: càng về hậu vận càng gắn bó, thuận đường con cái.");
    }
  } else {
    details.push("Lưu ý: Một hoặc cả hai chưa nhập giờ sinh chính xác nên Trụ Giờ chỉ mang tính tham khảo.");
  }

  score = Math.max(5, Math.min(20, score));
  const status: CompatibilityStatus =
    score >= 17 ? "excellent" : score >= 13 ? "good" : score >= 9 ? "neutral" : "caution";

  return {
    id: "bat_tu",
    title: "4. Tứ Trụ Bát Tự (Ngày & Giờ Sinh)",
    icon: "📜",
    score,
    maxScore: 20,
    status,
    badge: score >= 16 ? "Nhật Chủ Hài Hòa" : score >= 12 ? "Ổn Định" : "Cần Bù Trừ",
    summary:
      score >= 16
        ? "Trụ Ngày và Trụ Giờ cho thấy sự kết nối sâu sắc ở cả tính cách cốt lõi lẫn hậu vận."
        : "Tứ trụ hai người có nét bổ sung cho nhau; hiểu rõ nhịp sinh hoạt của đối phương sẽ giúp bền lâu.",
    details,
    advice: "Trụ Ngày phản ánh đời sống hôn nhân thực tế — sự quan tâm đều đặn mỗi ngày quan trọng hơn những lời hứa lớn."
  };
}

function evaluateWesternZodiac(p1: LovePersonSummary, p2: LovePersonSummary): LoveDimensionResult {
  const z1 = p1.westernZodiac;
  const z2 = p2.westernZodiac;

  const compatPairs: Record<string, string[]> = {
    "Lửa": ["Lửa", "Khí"],
    "Khí": ["Khí", "Lửa"],
    "Đất": ["Đất", "Nước"],
    "Nước": ["Nước", "Đất"]
  };

  const isSame = z1.name === z2.name;
  const isNaturalMatch = compatPairs[z1.element]?.includes(z2.element) ?? false;
  const score = isSame ? 8 : isNaturalMatch ? 10 : 6;
  const status: CompatibilityStatus = score >= 9 ? "excellent" : score >= 8 ? "good" : "neutral";

  return {
    id: "western_zodiac",
    title: "5. Cung Hoàng Đạo",
    icon: "✨",
    score,
    maxScore: 10,
    status,
    badge: isNaturalMatch ? `Hợp Nguyên Tố (${z1.element} - ${z2.element})` : `Bù Trừ (${z1.element} - ${z2.element})`,
    summary: isNaturalMatch
      ? `${z1.name} (${z1.element}) và ${z2.name} (${z2.element}) dễ bắt nhịp cảm xúc và chia sẻ năng lượng tích cực.`
      : `${z1.name} và ${z2.name} mang sắc thái tính cách khác biệt, giúp hai bạn mở rộng góc nhìn khi ở bên nhau.`,
    details: [
      `${p1.displayName}: ${z1.symbol} ${z1.name} (Nguyên tố ${z1.element})`,
      `${p2.displayName}: ${z2.symbol} ${z2.name} (Nguyên tố ${z2.element})`
    ],
    advice: "Tôn trọng nhịp cảm xúc riêng của mỗi cung giúp tình yêu luôn giữ được sự mới mẻ."
  };
}

function evaluateNumerology(p1: LovePersonSummary, p2: LovePersonSummary): LoveDimensionResult {
  const n1 = p1.numerology.lifePathNumber;
  const n2 = p2.numerology.lifePathNumber;
  const diff = Math.abs(n1 - n2);

  let score = 7;
  let badge = "Cân Bằng";
  if (n1 === n2) {
    score = 9;
    badge = "Đồng Điệu Số";
  } else if (
    (n1 === 2 || n1 === 6 || n1 === 9) && (n2 === 2 || n2 === 6 || n2 === 9)
  ) {
    score = 10;
    badge = "Tần Số Yêu Thương";
  } else if (diff <= 2 || n1 + n2 === 10 || n1 + n2 === 11) {
    score = 9;
    badge = "Hỗ Trợ Tốt";
  } else {
    score = 7;
    badge = "Đa Sắc Thái";
  }

  return {
    id: "numerology",
    title: "6. Thần Số Học (Số Chủ Đạo)",
    icon: "🔢",
    score,
    maxScore: 10,
    status: score >= 9 ? "excellent" : "good",
    badge,
    summary: `Số Chủ Đạo ${n1} và ${n2} tạo nên sự kết hợp ${score >= 9 ? "hài hòa về mục tiêu sống và cách thể hiện tình cảm" : "bổ sung nhiều trải nghiệm thú vị cho cả hai"}.`,
    details: [
      `${p1.displayName}: Số ${n1} (${p1.numerology.title})`,
      `${p2.displayName}: Số ${n2} (${p2.numerology.title})`
    ],
    advice: "Cùng thống nhất kế hoạch tài chính và định hướng tương lai sẽ giúp cặp số này phát huy sức mạnh."
  };
}

function computeComplementaryElements(p1: LovePersonSummary, p2: LovePersonSummary): string[] {
  const results: string[] = [];
  const counts1 = p1.fourPillars.elementCounts;
  const counts2 = p2.fourPillars.elementCounts;

  (Object.keys(counts1) as FiveElement[]).forEach((el) => {
    const label = `${FIVE_ELEMENT_META[el].emoji} ${elementLabel(el)}`;
    if (counts1[el] === 0 && counts2[el] >= 1) {
      results.push(`${p2.displayName} bổ sung hành ${label} đang khuyết cho ${p1.displayName}.`);
    }
    if (counts2[el] === 0 && counts1[el] >= 1) {
      results.push(`${p1.displayName} bổ sung hành ${label} đang khuyết cho ${p2.displayName}.`);
    }
  });

  // Kiểm tra Hỷ Dụng Thần tương trợ giữa 2 lá số
  const hy1 = p1.fourPillars.hyDungThan;
  const dom2 = p2.fourPillars.dominantElements;
  const matchedFor1 = hy1.filter((el) => dom2.includes(el));
  if (matchedFor1.length > 0) {
    results.push(
      `Hỷ Dụng Thần tương trợ: ${p2.displayName} vượng hành ${matchedFor1.map((e) => elementLabel(e)).join(", ")}, đúng vào Dụng Thần giúp cân bằng bản mệnh cho ${p1.displayName}.`
    );
  }

  const hy2 = p2.fourPillars.hyDungThan;
  const dom1 = p1.fourPillars.dominantElements;
  const matchedFor2 = hy2.filter((el) => dom1.includes(el));
  if (matchedFor2.length > 0) {
    results.push(
      `Hỷ Dụng Thần tương trợ: ${p1.displayName} vượng hành ${matchedFor2.map((e) => elementLabel(e)).join(", ")}, tiếp thêm vận khí tốt cho ${p2.displayName}.`
    );
  }

  if (results.length === 0) {
    results.push("Cả hai lá số đều có sự phân bổ Ngũ Hành tương đối cân bằng, hỗ trợ nhau ổn định.");
  }
  return results;
}

export function buildDeepLoveChapters(result: LoveCompatibilityResult): DeepLoveAiAnalysis {
  const { person1: p1, person2: p2 } = result;
  const d1 = p1.fourPillars.dayPillar;
  const d2 = p2.fourPillars.dayPillar;
  const h1 = p1.fourPillars.hourPillar;
  const h2 = p2.fourPillars.hourPillar;

  const hy1Str = p1.fourPillars.hyDungThan.map((e) => elementLabel(e)).join(", ");
  const hy2Str = p2.fourPillars.hyDungThan.map((e) => elementLabel(e)).join(", ");
  const huong1 = p1.profile.gender === "male" ? p1.personalProfile.cungMenh.huongTotNam : p1.personalProfile.cungMenh.huongTotNu;
  const huong2 = p2.profile.gender === "male" ? p2.personalProfile.cungMenh.huongTotNam : p2.personalProfile.cungMenh.huongTotNu;
  const commonDirections = huong1.filter((h) => huong2.includes(h));

  const cungPhiDim = result.dimensions.find((d) => d.id === "cung_phi");
  const napAmDim = result.dimensions.find((d) => d.id === "nap_am");
  const canChiDim = result.dimensions.find((d) => d.id === "can_chi");

  const aspects: DeepLoveAspect[] = [
    {
      id: "duyen_phan",
      title: "1. Sợi Duyên Phận & Sức Hút Ban Đầu",
      badge: `${p1.westernZodiac.element} • ${p2.westernZodiac.element}`,
      icon: "💕",
      summary: `${p1.displayName} (${p1.westernZodiac.name}, Số ${p1.numerology.lifePathNumber}) và ${p2.displayName} (${p2.westernZodiac.name}, Số ${p2.numerology.lifePathNumber}) kết nối qua sự giao thoa giữa tuổi ${p1.personalProfile.canChi} và ${p2.personalProfile.canChi}.`,
      details: [
        `Tuổi năm sinh ở mức ${canChiDim?.badge || "Bình Hòa"}: ${p1.displayName} thuộc nhóm Tam Hợp ${p1.personalProfile.tamHop.join("-")}, Lục Hợp ${p1.personalProfile.lucHop}; ${p2.displayName} thuộc nhóm Tam Hợp ${p2.personalProfile.tamHop.join("-")}.`,
        `Năng lượng Cung Hoàng Đạo (${p1.westernZodiac.symbol} ${p1.westernZodiac.name} & ${p2.westernZodiac.symbol} ${p2.westernZodiac.name}) tạo nên nhịp cảm xúc ${p1.westernZodiac.element === p2.westernZodiac.element ? "đồng điệu ngay từ những lần trò chuyện đầu" : "bù trừ thú vị, càng tìm hiểu càng khám phá thêm nhiều điểm mới"}.`
      ],
      advice: "Hãy giữ sự tò mò và lắng nghe cảm xúc thật của đối phương như những ngày đầu mới quen."
    },
    {
      id: "tinh_cach_giao_tiep",
      title: "2. Tính Cách Cốt Lõi & Cách Xử Lý Bất Đồng",
      badge: `Nhật Chủ ${p1.fourPillars.nhatChuCan} - ${p2.fourPillars.nhatChuCan}`,
      icon: "🗣️",
      summary: `Xét theo Nhật Chủ (Can Ngày sinh): ${p1.displayName} mang khí chất ${p1.fourPillars.nhatChuCan} (${p1.fourPillars.nhatChuStrength}), còn ${p2.displayName} mang khí chất ${p2.fourPillars.nhatChuCan} (${p2.fourPillars.nhatChuStrength}).`,
      details: [
        `${p1.displayName} có bản tính ${p1.fourPillars.nhatChuStrength === "Thân Vượng" ? "chủ động, quyết đoán và giàu năng lượng" : p1.fourPillars.nhatChuStrength === "Thân Nhược" ? "tinh tế, giàu cảm xúc và đề cao sự an toàn" : "ôn hòa, linh hoạt và biết cân bằng"}.`,
        `${p2.displayName} có bản tính ${p2.fourPillars.nhatChuStrength === "Thân Vượng" ? "mạnh mẽ, thẳng thắn và thích dẫn dắt" : p2.fourPillars.nhatChuStrength === "Thân Nhược" ? "nhạy bén, chu đáo và giàu sự thấu cảm" : "điềm tĩnh, khéo léo trong ứng xử"}.`
      ],
      advice: "Khi tranh luận, tránh nói lời lúc nóng giận; hãy cho nhau 15 phút lắng lại rồi trao đổi thẳng thắn trên tinh thần tôn trọng."
    },
    {
      id: "hon_nhan_giadao",
      title: "3. Đời Sống Hôn Nhân & Cung Phu Thê",
      badge: cungPhiDim?.badge || "Bát Trạch",
      icon: "🏡",
      summary: `Trụ Ngày (Cung Phu Thê) là ${d1.canChi} kết hợp ${d2.canChi}, đồng thời Cung Phi ${p1.cungPhi} và ${p2.cungPhi} tạo thành cung ${cungPhiDim?.badge || "Bình Hòa"}.`,
      details: [
        `Bản mệnh Nạp Âm ${p1.personalProfile.napAm} (${elementLabel(p1.personalProfile.element)}) và ${p2.personalProfile.napAm} (${elementLabel(p2.personalProfile.element)}) ở trạng thái ${napAmDim?.badge || "Bình Hòa"}.`,
        cungPhiDim?.summary || "Hai bạn cần cùng nhau xây dựng thói quen sinh hoạt chung ổn định."
      ],
      advice: cungPhiDim?.advice || "Cùng chia sẻ việc nhà và trách nhiệm gia đình là chìa khóa giữ hạnh phúc."
    },
    {
      id: "tai_loc_su_nghiep",
      title: "4. Tài Lộc, Sự Nghiệp & Hỷ Dụng Thần",
      badge: "Tài Vận Đôi",
      icon: "🪙",
      summary: `${p1.displayName} cần bổ sung hành ${hy1Str || elementLabel(p1.personalProfile.element)} để vượng vận, trong khi ${p2.displayName} cần hành ${hy2Str || elementLabel(p2.personalProfile.element)}.`,
      details: [
        ...result.complementaryElements,
        `Số Chủ Đạo ${p1.numerology.lifePathNumber} kết hợp Số ${p2.numerology.lifePathNumber} cho thấy ${p1.numerology.lifePathNumber === p2.numerology.lifePathNumber ? "cùng chung chí hướng phát triển" : "có thể phân công vai trò rõ ràng: một người hoạch định, một người triển khai"}.`
      ],
      advice: "Nên minh bạch tài chính chung và thống nhất mục tiêu tiết kiệm, đầu tư dài hạn."
    },
    {
      id: "con_cai_hau_van",
      title: "5. Đường Con Cái & Hậu Vận Về Sau",
      badge: !p1.fourPillars.hourPillar || !p2.fourPillars.hourPillar ? "Tham khảo" : "Trụ Giờ",
      icon: "🌱",
      summary:
        p1.birthHourFortune.hasBirthHour && p2.birthHourFortune.hasBirthHour && h1 && h2
          ? `Trụ Giờ (cung Tử Tức & Hậu vận) của ${p1.displayName} là ${h1.canChi} (${h1.napAm}) và ${p2.displayName} là ${h2.canChi} (${h2.napAm}).`
          : "Một hoặc cả hai chưa xác định chính xác giờ sinh nên phần hậu vận được luận dựa trên Trụ Ngày và Cung Phi tổng thể.",
      details: [
        `${p1.displayName} bước vào hậu vận ổn định hơn khi cân bằng được hành ${hy1Str || elementLabel(p1.personalProfile.element)}.`,
        `${p2.displayName} có nền tảng gia đạo vững vàng nếu duy trì sự gắn kết với gia đình hai bên.`
      ],
      advice: "Chăm sóc sức khỏe tinh thần và thể chất đều đặn từ sớm giúp hậu vận của cả hai luôn an yên."
    },
    {
      id: "hoa_giai_phong_thuy",
      title: "6. Phong Thủy Nhà Cửa & Hóa Giải Xung Khắc",
      badge: commonDirections.length > 0 ? `Hợp ${commonDirections[0]}` : "Dung hòa",
      icon: "🧭",
      summary:
        commonDirections.length > 0
          ? `Hai bạn có điểm chung về hướng cát lợi: ${commonDirections.join(", ")} — rất thuận để chọn hướng nhà, hướng cửa chính hoặc hướng giường ngủ.`
          : `${p1.displayName} (${p1.cungPhiGroup}) hợp hướng ${huong1.slice(0, 2).join(", ")}; ${p2.displayName} (${p2.cungPhiGroup}) hợp hướng ${huong2.slice(0, 2).join(", ")}.`,
      details: [
        commonDirections.length > 0
          ? `Ưu tiên bố trí giường ngủ hoặc bàn làm việc quay về hướng ${commonDirections[0]} để tăng sinh khí.`
          : `Vì hai bạn khác nhóm Đông/Tây Tứ Mệnh, nên chọn hướng cửa chính theo người trụ cột (${p1.profile.gender === "male" ? p1.displayName : p2.displayName}) và hướng bếp theo người còn lại để cân bằng.`,
        `Màu sắc dung hòa không gian sống: kết hợp nhẹ nhàng tông màu thuộc hành ${hy1Str || elementLabel(p1.personalProfile.element)} và ${hy2Str || elementLabel(p2.personalProfile.element)}.`
      ],
      advice: "Phong thủy tốt nhất trong gia đình luôn bắt đầu từ sự sạch sẽ, nhiều ánh sáng tự nhiên và lời nói ấm áp mỗi ngày."
    }
  ];

  return {
    headline: `${result.verdictTitle}: Luận Giải Chi Tiết ${p1.displayName} & ${p2.displayName}`,
    compatibilityScore: result.totalScore,
    executiveSummary: `Dựa trên đầy đủ 4 trụ Giờ - Ngày - Tháng - Năm sinh (${p1.fourPillars.yearPillar.canChi}/${p1.fourPillars.dayPillar.canChi} và ${p2.fourPillars.yearPillar.canChi}/${p2.fourPillars.dayPillar.canChi}), Cung Phi ${p1.cungPhi} - ${p2.cungPhi} và Hỷ Dụng Thần, hai bạn đạt ${result.totalScore}/100 điểm (${result.verdictBadge}).`,
    aspects
  };
}

export function evaluateLoveCompatibility(input1: LovePersonInput, input2: LovePersonInput): LoveCompatibilityResult {
  const profile1: UserBirthProfile = {
    day: input1.day,
    month: input1.month,
    year: input1.year,
    gender: input1.gender,
    birthHour: input1.birthHour,
    isLunar: input1.isLunar
  };
  const profile2: UserBirthProfile = {
    day: input2.day,
    month: input2.month,
    year: input2.year,
    gender: input2.gender,
    birthHour: input2.birthHour,
    isLunar: input2.isLunar
  };

  const fortune1 = getDetailedBirthFortune(profile1);
  const fortune2 = getDetailedBirthFortune(profile2);

  const person1: LovePersonSummary = {
    ...fortune1,
    displayName: input1.name.trim() || (input1.gender === "male" ? "Bạn Nam" : "Bạn Nữ")
  };
  const person2: LovePersonSummary = {
    ...fortune2,
    displayName: input2.name.trim() || (input2.gender === "male" ? "Bạn Nam" : "Bạn Nữ")
  };

  const dimensions: LoveDimensionResult[] = [
    evaluateMenhNapAm(person1, person2),
    evaluateCanChiYear(person1, person2),
    evaluateCungPhi(person1, person2),
    evaluateFourPillars(person1, person2),
    evaluateWesternZodiac(person1, person2),
    evaluateNumerology(person1, person2)
  ];

  const totalScore = Math.max(
    25,
    Math.min(100, dimensions.reduce((sum, item) => sum + item.score, 0))
  );

  let verdictTitle = "Hữu Duyên Cần Vun Đắp";
  let verdictBadge = "Khá Hợp";
  if (totalScore >= 85) {
    verdictTitle = "Thiên Tác Chi Hợp";
    verdictBadge = "Đại Cát • Rất Hợp";
  } else if (totalScore >= 72) {
    verdictTitle = "Lương Duyên Bền Vững";
    verdictBadge = "Hợp Duyên";
  } else if (totalScore >= 58) {
    verdictTitle = "Hữu Duyên Cần Vun Đắp";
    verdictBadge = "Khá Hợp";
  } else {
    verdictTitle = "Thử Thách Cần Hóa Giải";
    verdictBadge = "Cần Dung Hòa";
  }

  const complementaryElements = computeComplementaryElements(person1, person2);
  const summary = `Tổng hợp Ngũ Hành Nạp Âm, Can Chi, Cung Phi Bát Trạch và Tứ Trụ Giờ-Ngày-Tháng-Năm sinh cho thấy ${person1.displayName} (${person1.personalProfile.canChi}) và ${person2.displayName} (${person2.personalProfile.canChi}) đạt mức độ tương hợp ${totalScore}/100 (${verdictBadge}).`;

  const fengShuiAdvice = [
    `Dung hòa bản mệnh: kết hợp màu sắc thuộc hành trung gian hoặc hành tương sinh của ${elementLabel(person1.personalProfile.element)} và ${elementLabel(person2.personalProfile.element)} trong phòng sinh hoạt chung.`,
    "Nếu Cung Phi có điểm chưa thuận, ưu tiên chọn hướng bếp và hướng bàn thờ theo cung tốt (Sinh Khí, Diên Niên, Thiên Y) của người trụ cột.",
    "Giữ nhịp giao tiếp chân thành: Tử Vi & Bát Tự chỉ là bản đồ tham chiếu, sự thấu hiểu mỗi ngày mới là yếu tố quyết định hạnh phúc lâu dài."
  ];

  return {
    person1,
    person2,
    totalScore,
    verdictTitle,
    verdictBadge,
    summary,
    dimensions,
    complementaryElements,
    fengShuiAdvice
  };
}
