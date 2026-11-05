"use client";

import React, { useEffect, useRef } from "react";

interface Props {
  weatherCode: number;
  isDay: boolean;
  enabled?: boolean;
  isDarkTheme?: boolean;
  windSpeed?: number; // km/h
}

interface RainParticle {
  x: number;
  y: number;
  speed: number;
  len: number;
  opacity: number;
  layer: number; // 0 = background mist, 1 = foreground sharp drop
}

interface SplashParticle {
  x: number;
  y: number;
  vx: number;
  vy: number;
  radius: number;
  alpha: number;
  maxLife: number;
  life: number;
}

interface Cloud {
  x: number;
  y: number;
  speed: number;
  scale: number;
  opacity: number;
  puffs: { dx: number; dy: number; r: number }[];
}

interface WindStream {
  x: number;
  y: number;
  speed: number;
  length: number;
  amplitude: number;
  frequency: number;
  phase: number;
  opacity: number;
}

interface WindLeaf {
  x: number;
  y: number;
  vx: number;
  vy: number;
  size: number;
  rotZ: number;
  vRotZ: number;
  rotY: number;
  vRotY: number;
  color: string;
  wobbleSpeed: number;
  wobbleOffset: number;
}

interface StarParticle {
  x: number;
  y: number;
  size: number;
  baseOpacity: number;
  twinkleSpeed: number;
  phase: number;
}

interface SolarMote {
  x: number;
  y: number;
  size: number;
  vx: number;
  vy: number;
  opacity: number;
}

interface LightningFork {
  segments: { x1: number; y1: number; x2: number; y2: number; width: number }[];
  alpha: number;
}

export default function WeatherAtmosphereEffect({
  weatherCode,
  isDay,
  enabled = true,
  isDarkTheme = true,
  windSpeed = 12
}: Props) {
  const bgCanvasRef = useRef<HTMLCanvasElement | null>(null);
  const fgCanvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    if (!enabled) return;

    const bgCanvas = bgCanvasRef.current;
    const fgCanvas = fgCanvasRef.current;
    if (!bgCanvas || !fgCanvas) return;

    const bgCtx = bgCanvas.getContext("2d", { alpha: true });
    const fgCtx = fgCanvas.getContext("2d", { alpha: true });
    if (!bgCtx || !fgCtx) return;

    let animationFrameId: number;
    let width = (bgCanvas.width = fgCanvas.width = window.innerWidth);
    let height = (bgCanvas.height = fgCanvas.height = window.innerHeight);

    // Weather condition types
    const isRain = (weatherCode >= 51 && weatherCode <= 67) || (weatherCode >= 80 && weatherCode <= 82);
    const isThunder = weatherCode >= 95;
    const isFog = weatherCode === 45 || weatherCode === 48;
    const isSunny = (weatherCode === 0 || weatherCode === 1) && isDay;
    const isClearNight = (weatherCode === 0 || weatherCode === 1) && !isDay;
    const isCloudy = weatherCode === 2 || weatherCode === 3 || isFog;

    // Wind dynamics
    const effectiveWindKmh = Math.max(5, windSpeed);
    const windDirectionFactor = 1; // Left to right
    const windSpeedNorm = Math.min(35, effectiveWindKmh) / 15; // Normalized wind multiplier (0.5 to 2.5)
    const windSlant = 0.22 * windSpeedNorm; // Rain angle

    // Resize handler
    const handleResize = () => {
      if (!bgCanvas || !fgCanvas) return;
      width = bgCanvas.width = fgCanvas.width = window.innerWidth;
      height = bgCanvas.height = fgCanvas.height = window.innerHeight;
      initClouds();
    };
    window.addEventListener("resize", handleResize);

    // ==============================================================
    // 1. MÂY (PARALLAX 3D PUFFY CLOUDS FOR BACKGROUND CANVAS)
    // ==============================================================
    let clouds: Cloud[] = [];
    const initClouds = () => {
      clouds = [];
      const cloudCount = weatherCode === 3 ? 9 : isCloudy ? 6 : isSunny ? 3 : 4;
      for (let i = 0; i < cloudCount; i++) {
        const layer = (i % 3) + 1; // 1 = far/slow, 2 = mid, 3 = near
        const speed = (0.2 + layer * 0.25) * windSpeedNorm;
        const scale = 0.7 + layer * 0.35;
        const y = Math.random() * (height * 0.55);
        const x = Math.random() * (width + 600) - 200;

        // Generate fluffy puffs cluster
        const puffs: { dx: number; dy: number; r: number }[] = [];
        const puffCount = 5 + Math.floor(Math.random() * 4);
        for (let p = 0; p < puffCount; p++) {
          puffs.push({
            dx: (p - puffCount / 2) * (28 * scale) + (Math.random() - 0.5) * 20,
            dy: (Math.random() - 0.5) * (18 * scale),
            r: (35 + Math.random() * 30) * scale
          });
        }

        clouds.push({
          x,
          y,
          speed,
          scale,
          opacity: isDarkTheme ? 0.12 + layer * 0.05 : 0.30 + layer * 0.12,
          puffs
        });
      }
    };
    initClouds();

    // ==============================================================
    // 2. MƯA & GIỌT NƯỚC BẮN (RAIN & SPLASHES FOR FOREGROUND)
    // ==============================================================
    const rainDrops: RainParticle[] = [];
    const splashes: SplashParticle[] = [];
    if (isRain || isThunder) {
      const dropCount = isThunder ? 160 : weatherCode >= 63 ? 140 : 100;
      for (let i = 0; i < dropCount; i++) {
        const isFg = Math.random() > 0.4;
        rainDrops.push({
          x: Math.random() * (width + 300) - 100,
          y: Math.random() * height,
          speed: (isFg ? 22 + Math.random() * 12 : 14 + Math.random() * 8) * (isThunder ? 1.25 : 1),
          len: (isFg ? 28 + Math.random() * 24 : 16 + Math.random() * 14),
          opacity: isFg ? 0.55 + Math.random() * 0.35 : 0.25 + Math.random() * 0.3,
          layer: isFg ? 1 : 0
        });
      }
    }

    const spawnSplash = (x: number, y: number) => {
      if (splashes.length > 50) return;
      const count = 2 + Math.floor(Math.random() * 3);
      for (let i = 0; i < count; i++) {
        splashes.push({
          x,
          y,
          vx: (Math.random() - 0.5) * 4 + windSlant * 2,
          vy: -2 - Math.random() * 3,
          radius: 1 + Math.random() * 1.5,
          alpha: 0.7,
          maxLife: 15 + Math.random() * 10,
          life: 0
        });
      }
    };

    // ==============================================================
    // 3. GIÓ: VỆT KHÍ LƯỢT SÓNG & LÁ CÂY CUỐN THEO GIÓ (WIND STREAMS & LEAVES)
    // ==============================================================
    const windStreams: WindStream[] = [];
    const windStreamCount = Math.max(5, Math.floor(effectiveWindKmh / 3.5));
    for (let i = 0; i < windStreamCount; i++) {
      windStreams.push({
        x: Math.random() * (width + 400) - 200,
        y: Math.random() * (height * 0.9),
        speed: (6 + Math.random() * 7) * windSpeedNorm,
        length: 120 + Math.random() * 180,
        amplitude: 12 + Math.random() * 16,
        frequency: 0.015 + Math.random() * 0.015,
        phase: Math.random() * Math.PI * 2,
        opacity: isDarkTheme ? 0.15 + Math.random() * 0.2 : 0.30 + Math.random() * 0.3
      });
    }

    const windLeaves: WindLeaf[] = [];
    const leafCount = Math.min(30, Math.floor(12 + effectiveWindKmh * 0.6));
    const leafColors = isDarkTheme
      ? ["#f59e0b", "#d97706", "#10b981", "#34d399", "#fbbf24"]
      : ["#d97706", "#b45309", "#059669", "#16a34a", "#f59e0b"];

    for (let i = 0; i < leafCount; i++) {
      windLeaves.push({
        x: Math.random() * (width + 200) - 100,
        y: Math.random() * height,
        vx: (3 + Math.random() * 5) * windSpeedNorm,
        vy: 0.8 + Math.random() * 1.8,
        size: 7 + Math.random() * 7,
        rotZ: Math.random() * Math.PI * 2,
        vRotZ: (Math.random() - 0.5) * 0.08,
        rotY: Math.random() * Math.PI,
        vRotY: 0.04 + Math.random() * 0.08,
        color: leafColors[Math.floor(Math.random() * leafColors.length)],
        wobbleSpeed: 0.02 + Math.random() * 0.03,
        wobbleOffset: Math.random() * Math.PI * 2
      });
    }

    // ==============================================================
    // 4. NẮNG: TIA NẮNG VÀNG ÓNG & BỤI NẮNG (SUNBEAMS & SOLAR MOTES)
    // ==============================================================
    const solarMotes: SolarMote[] = [];
    if (isSunny) {
      for (let i = 0; i < 45; i++) {
        solarMotes.push({
          x: Math.random() * width,
          y: Math.random() * height,
          size: 1.5 + Math.random() * 2.8,
          vx: (Math.random() - 0.5) * 0.4 + windSpeedNorm * 0.2,
          vy: -0.3 - Math.random() * 0.5,
          opacity: 0.3 + Math.random() * 0.6
        });
      }
    }

    // ==============================================================
    // 5. ĐÊM QUANG ĐÃNG & SAO BĂNG (STARS & METEOR)
    // ==============================================================
    const stars: StarParticle[] = [];
    if (isClearNight) {
      for (let i = 0; i < 85; i++) {
        stars.push({
          x: Math.random() * width,
          y: Math.random() * (height * 0.7),
          size: 0.8 + Math.random() * 2.0,
          baseOpacity: 0.3 + Math.random() * 0.6,
          twinkleSpeed: 0.03 + Math.random() * 0.04,
          phase: Math.random() * Math.PI * 2
        });
      }
    }

    const meteor = {
      active: false,
      x: 0,
      y: 0,
      vx: 0,
      vy: 0,
      len: 0,
      timer: 120
    };

    // ==============================================================
    // 6. SẤM SÉT (BRANCHED LIGHTNING FOR THUNDERSTORM)
    // ==============================================================
    let lightning: LightningFork | null = null;
    let lightningTimer = 180;
    let flashOpacity = 0;

    const generateLightning = () => {
      const startX = width * (0.2 + Math.random() * 0.6);
      const segments: { x1: number; y1: number; x2: number; y2: number; width: number }[] = [];

      let curX = startX;
      let curY = 0;
      const targetY = height * 0.65;

      while (curY < targetY) {
        const nextX = curX + (Math.random() - 0.5) * 45;
        const nextY = curY + 15 + Math.random() * 25;
        segments.push({ x1: curX, y1: curY, x2: nextX, y2: nextY, width: 2.2 });

        // Branch chance
        if (Math.random() > 0.6) {
          const branchLength = 2 + Math.floor(Math.random() * 3);
          let bx = curX;
          let by = curY;
          for (let b = 0; b < branchLength; b++) {
            const nbx = bx + (Math.random() - 0.4) * 35;
            const nby = by + 12 + Math.random() * 18;
            segments.push({ x1: bx, y1: by, x2: nbx, y2: nby, width: 1.2 });
            bx = nbx;
            by = nby;
          }
        }

        curX = nextX;
        curY = nextY;
      }

      lightning = { segments, alpha: 1.0 };
      flashOpacity = 0.95;
    };

    // ==============================================================
    // MAIN UNIFIED RENDER LOOP (60/120 FPS HIGH PERFORMANCE)
    // ==============================================================
    let time = 0;
    const render = () => {
      time += 0.02;

      bgCtx.clearRect(0, 0, width, height);
      fgCtx.clearRect(0, 0, width, height);

      // -------------------------------------------------------------
      // PART A: BACKGROUND CANVAS (z-0) - CELESTIAL BODIES & CLOUDS
      // -------------------------------------------------------------

      // A1. Sunny radiant sun disc
      if (isSunny) {
        const sunX = width * 0.78;
        const sunY = 70;
        const pulse = Math.sin(time * 1.5) * 6;

        const sunCorona = bgCtx.createRadialGradient(sunX, sunY, 15, sunX, sunY, 180 + pulse);
        sunCorona.addColorStop(0, "rgba(255, 235, 130, 0.95)");
        sunCorona.addColorStop(0.2, "rgba(255, 190, 60, 0.55)");
        sunCorona.addColorStop(0.6, "rgba(255, 140, 20, 0.18)");
        sunCorona.addColorStop(1, "rgba(255, 120, 0, 0)");

        bgCtx.fillStyle = sunCorona;
        bgCtx.beginPath();
        bgCtx.arc(sunX, sunY, 200 + pulse, 0, Math.PI * 2);
        bgCtx.fill();

        // Core sun disc
        bgCtx.fillStyle = "#fffbeb";
        bgCtx.beginPath();
        bgCtx.arc(sunX, sunY, 32, 0, Math.PI * 2);
        bgCtx.fill();
      }

      // A2. Clear Night glowing moon & twinkling stars
      if (isClearNight) {
        const moonX = width * 0.82;
        const moonY = 85;

        // Moon halo
        const moonHalo = bgCtx.createRadialGradient(moonX, moonY, 15, moonX, moonY, 130);
        moonHalo.addColorStop(0, "rgba(224, 242, 254, 0.70)");
        moonHalo.addColorStop(0.4, "rgba(186, 230, 253, 0.25)");
        moonHalo.addColorStop(1, "rgba(125, 211, 252, 0)");

        bgCtx.fillStyle = moonHalo;
        bgCtx.beginPath();
        bgCtx.arc(moonX, moonY, 130, 0, Math.PI * 2);
        bgCtx.fill();

        // Moon disc
        bgCtx.fillStyle = "#f8fafc";
        bgCtx.beginPath();
        bgCtx.arc(moonX, moonY, 26, 0, Math.PI * 2);
        bgCtx.fill();

        // Moon crescent shadow
        bgCtx.fillStyle = isDarkTheme ? "#020617" : "#0f172a";
        bgCtx.beginPath();
        bgCtx.arc(moonX + 9, moonY - 5, 22, 0, Math.PI * 2);
        bgCtx.fill();

        // Twinkling stars
        for (const s of stars) {
          const alpha = s.baseOpacity + Math.sin(time * 3 + s.phase) * 0.3;
          bgCtx.fillStyle = `rgba(255, 255, 255, ${Math.max(0.1, Math.min(1, alpha))})`;
          bgCtx.beginPath();
          bgCtx.arc(s.x, s.y, s.size, 0, Math.PI * 2);
          bgCtx.fill();
        }

        // Shooting star (Meteor)
        meteor.timer--;
        if (meteor.timer <= 0 && !meteor.active) {
          meteor.active = true;
          meteor.x = Math.random() * (width * 0.65);
          meteor.y = Math.random() * (height * 0.3);
          meteor.vx = 14 + Math.random() * 8;
          meteor.vy = 7 + Math.random() * 5;
          meteor.len = 90 + Math.random() * 50;
        }

        if (meteor.active) {
          bgCtx.strokeStyle = "rgba(255, 255, 255, 0.9)";
          bgCtx.lineWidth = 2;
          bgCtx.beginPath();
          bgCtx.moveTo(meteor.x, meteor.y);
          bgCtx.lineTo(meteor.x - meteor.vx * 3.5, meteor.y - meteor.vy * 3.5);
          bgCtx.stroke();

          meteor.x += meteor.vx;
          meteor.y += meteor.vy;

          if (meteor.x > width + 100 || meteor.y > height * 0.6) {
            meteor.active = false;
            meteor.timer = 180 + Math.floor(Math.random() * 240);
          }
        }
      }

      // A3. Puffy 3D Clouds drifting across sky
      for (const cloud of clouds) {
        cloud.x += cloud.speed;
        if (cloud.x > width + 400) {
          cloud.x = -400;
          cloud.y = Math.random() * (height * 0.55);
        }

        bgCtx.save();
        bgCtx.translate(cloud.x, cloud.y);
        for (const puff of cloud.puffs) {
          const grad = bgCtx.createRadialGradient(
            puff.dx - puff.r * 0.2,
            puff.dy - puff.r * 0.3,
            puff.r * 0.1,
            puff.dx,
            puff.dy,
            puff.r
          );

          if (isDarkTheme) {
            grad.addColorStop(0, `rgba(255, 255, 255, ${cloud.opacity * 1.2})`);
            grad.addColorStop(0.7, `rgba(180, 200, 230, ${cloud.opacity * 0.7})`);
            grad.addColorStop(1, "rgba(100, 130, 170, 0)");
          } else {
            grad.addColorStop(0, `rgba(255, 255, 255, ${cloud.opacity * 1.5})`);
            grad.addColorStop(0.7, `rgba(225, 238, 255, ${cloud.opacity * 0.9})`);
            grad.addColorStop(1, "rgba(200, 220, 245, 0)");
          }

          bgCtx.fillStyle = grad;
          bgCtx.beginPath();
          bgCtx.arc(puff.dx, puff.dy, puff.r, 0, Math.PI * 2);
          bgCtx.fill();
        }
        bgCtx.restore();
      }

      // -------------------------------------------------------------
      // PART B: FOREGROUND CANVAS (z-20) - SUNBEAMS, WIND, RAIN, LIGHTNING
      // -------------------------------------------------------------

      // B1. NẮNG: Volumetric Sweeping Sunbeams (Chùm tia nắng vàng óng)
      if (isSunny) {
        const sunX = width * 0.78;
        const sunY = 70;
        const beamCount = 7;
        const baseAngle = Math.PI * 0.72; // Shooting towards bottom-left

        fgCtx.save();
        for (let b = 0; b < beamCount; b++) {
          const angleOffset = (b - beamCount / 2) * 0.18 + Math.sin(time * 0.4 + b) * 0.05;
          const beamAngle = baseAngle + angleOffset;
          const beamLength = Math.max(width, height) * 1.3;
          const beamWidth = (65 + b * 20);

          const grad = fgCtx.createRadialGradient(sunX, sunY, 30, sunX, sunY, beamLength);
          const beamAlpha = (0.12 + Math.sin(time * 1.2 + b * 1.5) * 0.06) * (isDarkTheme ? 0.7 : 1.1);

          grad.addColorStop(0, `rgba(255, 240, 170, ${beamAlpha})`);
          grad.addColorStop(0.4, `rgba(255, 210, 100, ${beamAlpha * 0.6})`);
          grad.addColorStop(1, "rgba(255, 180, 50, 0)");

          fgCtx.fillStyle = grad;
          fgCtx.beginPath();
          fgCtx.moveTo(sunX, sunY);
          fgCtx.lineTo(
            sunX + Math.cos(beamAngle - 0.08) * beamLength,
            sunY + Math.sin(beamAngle - 0.08) * beamLength
          );
          fgCtx.lineTo(
            sunX + Math.cos(beamAngle + 0.08) * beamLength,
            sunY + Math.sin(beamAngle + 0.08) * beamLength
          );
          fgCtx.closePath();
          fgCtx.fill();
        }
        fgCtx.restore();

        // Shimmering Golden Solar Motes (Hạt bụi vàng óng ánh)
        fgCtx.fillStyle = isDarkTheme ? "rgba(255, 235, 150, 0.9)" : "rgba(245, 158, 11, 0.85)";
        for (const m of solarMotes) {
          m.x += m.vx;
          m.y += m.vy;
          if (m.y < -10) {
            m.y = height + 10;
            m.x = Math.random() * width;
          }
          if (m.x < -10) m.x = width + 10;
          if (m.x > width + 10) m.x = -10;

          const alpha = m.opacity * (0.6 + Math.sin(time * 2 + m.x) * 0.4);
          fgCtx.globalAlpha = Math.max(0.1, Math.min(1, alpha));
          fgCtx.beginPath();
          fgCtx.arc(m.x, m.y, m.size, 0, Math.PI * 2);
          fgCtx.fill();
        }
        fgCtx.globalAlpha = 1.0;
      }

      // B2. GIÓ: Flowing Wind Streamlines (Vệt luồng gió lướt hình sóng)
      for (const stream of windStreams) {
        stream.x += stream.speed;
        if (stream.x > width + stream.length) {
          stream.x = -stream.length - 100;
          stream.y = Math.random() * (height * 0.85);
        }

        fgCtx.save();
        fgCtx.strokeStyle = isDarkTheme
          ? `rgba(255, 255, 255, ${stream.opacity * 0.7})`
          : `rgba(14, 165, 233, ${stream.opacity * 0.85})`;
        fgCtx.lineWidth = 1.8;
        fgCtx.lineCap = "round";

        fgCtx.beginPath();
        const startX = stream.x;
        const endX = stream.x + stream.length;
        const steps = 14;
        const dx = stream.length / steps;

        for (let s = 0; s <= steps; s++) {
          const sx = startX + s * dx;
          const sy = stream.y + Math.sin(stream.phase + s * 0.4 + time * 3) * stream.amplitude;
          if (s === 0) fgCtx.moveTo(sx, sy);
          else fgCtx.lineTo(sx, sy);
        }
        fgCtx.stroke();
        fgCtx.restore();
      }

      // B3. GIÓ: Tumbling airborne leaves / petals (Lá cây chao lượn theo gió)
      for (const leaf of windLeaves) {
        leaf.x += leaf.vx;
        leaf.y += leaf.vy + Math.sin(time * 2 + leaf.wobbleOffset) * 0.8;
        leaf.rotZ += leaf.vRotZ;
        leaf.rotY += leaf.vRotY;

        if (leaf.x > width + 60) {
          leaf.x = -60;
          leaf.y = Math.random() * (height * 0.75);
        }
        if (leaf.y > height + 60) {
          leaf.y = -40;
          leaf.x = Math.random() * width;
        }

        fgCtx.save();
        fgCtx.translate(leaf.x, leaf.y);
        fgCtx.rotate(leaf.rotZ);
        fgCtx.scale(Math.cos(leaf.rotY), 1);

        fgCtx.fillStyle = leaf.color;
        fgCtx.globalAlpha = isDarkTheme ? 0.75 : 0.85;

        // Draw leaf shape
        fgCtx.beginPath();
        fgCtx.moveTo(0, -leaf.size);
        fgCtx.quadraticCurveTo(leaf.size * 0.6, 0, 0, leaf.size);
        fgCtx.quadraticCurveTo(-leaf.size * 0.6, 0, 0, -leaf.size);
        fgCtx.fill();

        // Leaf vein
        fgCtx.strokeStyle = "rgba(255, 255, 255, 0.4)";
        fgCtx.lineWidth = 0.8;
        fgCtx.beginPath();
        fgCtx.moveTo(0, -leaf.size * 0.8);
        fgCtx.lineTo(0, leaf.size * 0.8);
        fgCtx.stroke();

        fgCtx.restore();
      }
      fgCtx.globalAlpha = 1.0;

      // B4. MƯA: Fast Slanted Raindrops & Ground Splashes
      if (isRain || isThunder) {
        for (const drop of rainDrops) {
          fgCtx.strokeStyle = isDarkTheme
            ? drop.layer === 1
              ? `rgba(224, 242, 254, ${drop.opacity})`
              : `rgba(186, 230, 253, ${drop.opacity * 0.5})`
            : drop.layer === 1
              ? `rgba(2, 132, 199, ${drop.opacity * 0.95})`
              : `rgba(14, 165, 233, ${drop.opacity * 0.65})`;

          fgCtx.lineWidth = drop.layer === 1 ? 1.6 : 1.0;
          fgCtx.lineCap = "round";

          fgCtx.beginPath();
          fgCtx.moveTo(drop.x, drop.y);
          fgCtx.lineTo(drop.x + drop.len * windSlant, drop.y + drop.len);
          fgCtx.stroke();

          drop.y += drop.speed;
          drop.x += drop.speed * windSlant;

          if (drop.y > height - 20) {
            if (Math.random() > 0.65) spawnSplash(drop.x, height - 10);
            drop.y = -drop.len - Math.random() * 20;
            drop.x = Math.random() * (width + 300) - 100;
          }
        }

        // Render water splashes
        for (let i = splashes.length - 1; i >= 0; i--) {
          const sp = splashes[i];
          sp.x += sp.vx;
          sp.y += sp.vy;
          sp.vy += 0.2; // Gravity
          sp.life++;

          const alpha = sp.alpha * (1 - sp.life / sp.maxLife);
          if (alpha <= 0 || sp.life >= sp.maxLife) {
            splashes.splice(i, 1);
            continue;
          }

          fgCtx.fillStyle = isDarkTheme ? `rgba(224, 242, 254, ${alpha})` : `rgba(2, 132, 199, ${alpha})`;
          fgCtx.beginPath();
          fgCtx.arc(sp.x, sp.y, sp.radius, 0, Math.PI * 2);
          fgCtx.fill();
        }
      }

      // B5. SẤM SÉT: Dramatic Branched Lightning & Sky Flashes
      if (isThunder) {
        lightningTimer--;
        if (lightningTimer <= 0) {
          generateLightning();
          lightningTimer = 220 + Math.floor(Math.random() * 320);
        }

        if (lightning && lightning.alpha > 0.05) {
          fgCtx.save();
          fgCtx.strokeStyle = `rgba(255, 255, 255, ${lightning.alpha})`;
          fgCtx.shadowColor = "rgba(192, 132, 252, 0.9)";
          fgCtx.shadowBlur = 15;

          for (const seg of lightning.segments) {
            fgCtx.lineWidth = seg.width;
            fgCtx.beginPath();
            fgCtx.moveTo(seg.x1, seg.y1);
            fgCtx.lineTo(seg.x2, seg.y2);
            fgCtx.stroke();
          }
          fgCtx.restore();

          lightning.alpha *= 0.82;
        }

        // Ambient sky double-flash
        if (flashOpacity > 0.02) {
          fgCtx.fillStyle = isDarkTheme
            ? `rgba(243, 232, 255, ${flashOpacity * 0.75})`
            : `rgba(255, 255, 255, ${flashOpacity * 0.85})`;
          fgCtx.fillRect(0, 0, width, height);
          flashOpacity *= 0.80;
        }
      }

      animationFrameId = requestAnimationFrame(render);
    };

    animationFrameId = requestAnimationFrame(render);

    return () => {
      cancelAnimationFrame(animationFrameId);
      window.removeEventListener("resize", handleResize);
    };
  }, [weatherCode, isDay, enabled, isDarkTheme, windSpeed]);

  if (!enabled) return null;

  return (
    <>
      {/* 1. Background Canvas (z-0): Sun/Moon, Stars & 3D Puffy Parallax Clouds */}
      <canvas
        ref={bgCanvasRef}
        className="fixed inset-0 pointer-events-none z-0 w-full h-full gpu-smooth"
      />

      {/* 2. Foreground Canvas (z-20): Slanted Rain, Splashes, Wind Streamlines, Leaves, Sunbeams, Lightning */}
      <canvas
        ref={fgCanvasRef}
        className="fixed inset-0 pointer-events-none z-20 w-full h-full gpu-smooth"
      />
    </>
  );
}
