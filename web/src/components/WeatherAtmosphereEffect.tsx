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
  color: string;
}

interface StarParticle {
  x: number;
  y: number;
  size: number;
  baseOpacity: number;
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

export default function WeatherAtmosphereEffect({
  weatherCode,
  isDay,
  enabled = true,
  isDarkTheme = true,
  windSpeed = 12
}: Props) {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    if (!enabled) return;

    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d", { alpha: true, desynchronized: true });
    if (!ctx) return;

    let animationFrameId: number;
    let width = (canvas.width = window.innerWidth);
    let height = (canvas.height = window.innerHeight);

    // Weather condition flags
    const isRain = (weatherCode >= 51 && weatherCode <= 67) || (weatherCode >= 80 && weatherCode <= 82);
    const isThunder = weatherCode >= 95;
    const isFog = weatherCode === 45 || weatherCode === 48;
    const isSunny = (weatherCode === 0 || weatherCode === 1) && isDay;
    const isClearNight = (weatherCode === 0 || weatherCode === 1) && !isDay;
    const isCloudy = weatherCode === 2 || weatherCode === 3 || isFog;

    // Wind dynamics
    const effectiveWindKmh = Math.max(5, Math.min(45, windSpeed));
    const windSpeedNorm = effectiveWindKmh / 15;
    const windSlant = 0.22 * windSpeedNorm;

    // Resize handler
    const handleResize = () => {
      if (!canvas) return;
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;
      initClouds();
    };
    window.addEventListener("resize", handleResize);

    // -------------------------------------------------------------
    // SCROLL DETECTION: Throttle during active scrolling for 60/120fps
    // -------------------------------------------------------------
    let isScrolling = false;
    let scrollTimeout: ReturnType<typeof setTimeout> | null = null;
    const handleScroll = () => {
      isScrolling = true;
      if (scrollTimeout) clearTimeout(scrollTimeout);
      scrollTimeout = setTimeout(() => {
        isScrolling = false;
      }, 90);
    };
    window.addEventListener("scroll", handleScroll, { passive: true });

    // 1. CLOUDS (Streamlined, lightweight puff clusters)
    let clouds: Cloud[] = [];
    const initClouds = () => {
      clouds = [];
      const cloudCount = weatherCode === 3 ? 5 : isCloudy ? 4 : isSunny ? 2 : 3;
      for (let i = 0; i < cloudCount; i++) {
        const layer = (i % 2) + 1;
        const speed = (0.2 + layer * 0.2) * windSpeedNorm;
        const scale = 0.8 + layer * 0.3;
        const y = Math.random() * (height * 0.45);
        const x = Math.random() * (width + 400) - 200;

        const puffs: { dx: number; dy: number; r: number }[] = [];
        const puffCount = 4 + Math.floor(Math.random() * 3);
        for (let p = 0; p < puffCount; p++) {
          puffs.push({
            dx: (p - puffCount / 2) * (26 * scale),
            dy: (Math.random() - 0.5) * (14 * scale),
            r: (30 + Math.random() * 25) * scale
          });
        }

        clouds.push({
          x,
          y,
          speed,
          scale,
          opacity: isDarkTheme ? 0.12 + layer * 0.05 : 0.28 + layer * 0.08,
          puffs
        });
      }
    };
    initClouds();

    // 2. RAIN (Optimized drop count for crisp performance)
    const rainDrops: RainParticle[] = [];
    if (isRain || isThunder) {
      const dropCount = isThunder ? 90 : weatherCode >= 63 ? 80 : 55;
      for (let i = 0; i < dropCount; i++) {
        rainDrops.push({
          x: Math.random() * (width + 250) - 100,
          y: Math.random() * height,
          speed: 18 + Math.random() * 12,
          len: 22 + Math.random() * 20,
          opacity: 0.45 + Math.random() * 0.4
        });
      }
    }

    // 3. WIND STREAMS & LEAVES (Clean vector strokes)
    const windStreams: WindStream[] = [];
    const windStreamCount = Math.max(3, Math.min(6, Math.floor(effectiveWindKmh / 6)));
    for (let i = 0; i < windStreamCount; i++) {
      windStreams.push({
        x: Math.random() * (width + 300) - 150,
        y: Math.random() * (height * 0.85),
        speed: (5 + Math.random() * 5) * windSpeedNorm,
        length: 100 + Math.random() * 140,
        amplitude: 10 + Math.random() * 12,
        phase: Math.random() * Math.PI * 2,
        opacity: isDarkTheme ? 0.15 : 0.28
      });
    }

    const windLeaves: WindLeaf[] = [];
    const leafCount = Math.min(14, Math.floor(6 + effectiveWindKmh * 0.3));
    const leafColors = isDarkTheme
      ? ["#f59e0b", "#d97706", "#10b981", "#fbbf24"]
      : ["#d97706", "#b45309", "#059669", "#f59e0b"];

    for (let i = 0; i < leafCount; i++) {
      windLeaves.push({
        x: Math.random() * (width + 100) - 50,
        y: Math.random() * height,
        vx: (2.5 + Math.random() * 3.5) * windSpeedNorm,
        vy: 0.6 + Math.random() * 1.2,
        size: 6 + Math.random() * 5,
        rotZ: Math.random() * Math.PI * 2,
        vRotZ: (Math.random() - 0.5) * 0.06,
        color: leafColors[Math.floor(Math.random() * leafColors.length)]
      });
    }

    // 4. SUN (Volumetric rays & motes)
    const solarMotes: SolarMote[] = [];
    if (isSunny) {
      for (let i = 0; i < 24; i++) {
        solarMotes.push({
          x: Math.random() * width,
          y: Math.random() * height,
          size: 1.5 + Math.random() * 2,
          vx: (Math.random() - 0.5) * 0.3 + windSpeedNorm * 0.15,
          vy: -0.2 - Math.random() * 0.4,
          opacity: 0.3 + Math.random() * 0.5
        });
      }
    }

    // 5. NIGHT (Stars & Meteor)
    const stars: StarParticle[] = [];
    if (isClearNight) {
      for (let i = 0; i < 50; i++) {
        stars.push({
          x: Math.random() * width,
          y: Math.random() * (height * 0.6),
          size: 0.8 + Math.random() * 1.5,
          baseOpacity: 0.25 + Math.random() * 0.5,
          phase: Math.random() * Math.PI * 2
        });
      }
    }

    // 6. THUNDER LIGHTNING
    let flashOpacity = 0;
    let lightningTimer = 200;

    // -------------------------------------------------------------
    // MAIN RENDER LOOP (60/120 FPS HIGH PERFORMANCE)
    // -------------------------------------------------------------
    let time = 0;
    let lastRenderTime = performance.now();

    const render = (now: number) => {
      // If user is actively scrolling, throttle to save GPU for silky smooth 60/120fps scrolling
      if (isScrolling) {
        if (now - lastRenderTime < 33) {
          animationFrameId = requestAnimationFrame(render);
          return;
        }
      }
      lastRenderTime = now;

      time += 0.02;
      ctx.clearRect(0, 0, width, height);

      // A. CELESTIAL & BACKGROUND CLOUDS
      if (isSunny) {
        const sunX = width * 0.78;
        const sunY = 65;

        // Radiant Sun Disc
        const sunCorona = ctx.createRadialGradient(sunX, sunY, 15, sunX, sunY, 160);
        sunCorona.addColorStop(0, "rgba(255, 235, 130, 0.85)");
        sunCorona.addColorStop(0.3, "rgba(255, 190, 60, 0.35)");
        sunCorona.addColorStop(1, "rgba(255, 120, 0, 0)");
        ctx.fillStyle = sunCorona;
        ctx.beginPath();
        ctx.arc(sunX, sunY, 160, 0, Math.PI * 2);
        ctx.fill();

        // 5 Warm Sunbeams
        ctx.save();
        for (let b = 0; b < 5; b++) {
          const angle = Math.PI * 0.72 + (b - 2) * 0.16 + Math.sin(time * 0.3 + b) * 0.03;
          const beamLen = Math.max(width, height) * 1.2;
          const beamAlpha = 0.08 * (isDarkTheme ? 0.6 : 1);

          ctx.fillStyle = `rgba(255, 220, 110, ${beamAlpha})`;
          ctx.beginPath();
          ctx.moveTo(sunX, sunY);
          ctx.lineTo(sunX + Math.cos(angle - 0.06) * beamLen, sunY + Math.sin(angle - 0.06) * beamLen);
          ctx.lineTo(sunX + Math.cos(angle + 0.06) * beamLen, sunY + Math.sin(angle + 0.06) * beamLen);
          ctx.closePath();
          ctx.fill();
        }
        ctx.restore();

        // Solar motes
        ctx.fillStyle = isDarkTheme ? "rgba(255, 235, 150, 0.8)" : "rgba(245, 158, 11, 0.75)";
        for (const m of solarMotes) {
          m.x += m.vx;
          m.y += m.vy;
          if (m.y < -10) m.y = height + 10;
          if (m.x > width + 10) m.x = -10;
          ctx.beginPath();
          ctx.arc(m.x, m.y, m.size, 0, Math.PI * 2);
          ctx.fill();
        }
      }

      if (isClearNight) {
        const moonX = width * 0.82;
        const moonY = 80;

        // Moon disc
        ctx.fillStyle = "#f8fafc";
        ctx.beginPath();
        ctx.arc(moonX, moonY, 22, 0, Math.PI * 2);
        ctx.fill();

        // Moon crescent
        ctx.fillStyle = isDarkTheme ? "#020617" : "#0f172a";
        ctx.beginPath();
        ctx.arc(moonX + 8, moonY - 4, 19, 0, Math.PI * 2);
        ctx.fill();

        // Stars
        ctx.fillStyle = "#ffffff";
        for (const s of stars) {
          const alpha = s.baseOpacity + Math.sin(time * 2 + s.phase) * 0.25;
          ctx.globalAlpha = Math.max(0.1, Math.min(1, alpha));
          ctx.beginPath();
          ctx.arc(s.x, s.y, s.size, 0, Math.PI * 2);
          ctx.fill();
        }
        ctx.globalAlpha = 1.0;
      }

      // B. PUFFY DRIFTING CLOUDS
      for (const cloud of clouds) {
        cloud.x += cloud.speed;
        if (cloud.x > width + 300) cloud.x = -300;

        ctx.fillStyle = isDarkTheme
          ? `rgba(255, 255, 255, ${cloud.opacity})`
          : `rgba(255, 255, 255, ${cloud.opacity * 1.2})`;

        for (const puff of cloud.puffs) {
          ctx.beginPath();
          ctx.arc(cloud.x + puff.dx, cloud.y + puff.dy, puff.r, 0, Math.PI * 2);
          ctx.fill();
        }
      }

      // C. WIND STREAMLINES & AIRBORNE LEAVES
      ctx.lineWidth = 1.6;
      ctx.strokeStyle = isDarkTheme ? "rgba(255, 255, 255, 0.18)" : "rgba(14, 165, 233, 0.28)";
      for (const stream of windStreams) {
        stream.x += stream.speed;
        if (stream.x > width + stream.length) {
          stream.x = -stream.length - 80;
          stream.y = Math.random() * (height * 0.85);
        }

        ctx.beginPath();
        ctx.moveTo(stream.x, stream.y);
        ctx.quadraticCurveTo(
          stream.x + stream.length * 0.5,
          stream.y + Math.sin(time * 2.5 + stream.phase) * stream.amplitude,
          stream.x + stream.length,
          stream.y
        );
        ctx.stroke();
      }

      // Leaves
      for (const leaf of windLeaves) {
        leaf.x += leaf.vx;
        leaf.y += leaf.vy;
        leaf.rotZ += leaf.vRotZ;
        if (leaf.x > width + 40) leaf.x = -40;
        if (leaf.y > height + 40) leaf.y = -30;

        ctx.save();
        ctx.translate(leaf.x, leaf.y);
        ctx.rotate(leaf.rotZ);
        ctx.fillStyle = leaf.color;
        ctx.globalAlpha = isDarkTheme ? 0.75 : 0.85;

        ctx.beginPath();
        ctx.ellipse(0, 0, leaf.size, leaf.size * 0.45, 0, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();
      }
      ctx.globalAlpha = 1.0;

      // D. RAIN
      if (isRain || isThunder) {
        ctx.strokeStyle = isDarkTheme ? "rgba(224, 242, 254, 0.65)" : "rgba(2, 132, 199, 0.75)";
        ctx.lineWidth = 1.4;
        ctx.beginPath();

        for (const drop of rainDrops) {
          ctx.moveTo(drop.x, drop.y);
          ctx.lineTo(drop.x + drop.len * windSlant, drop.y + drop.len);

          drop.y += drop.speed;
          drop.x += drop.speed * windSlant;

          if (drop.y > height) {
            drop.y = -drop.len;
            drop.x = Math.random() * (width + 250) - 100;
          }
        }
        ctx.stroke();
      }

      // E. THUNDER FLASH
      if (isThunder) {
        lightningTimer--;
        if (lightningTimer <= 0) {
          flashOpacity = 0.9;
          lightningTimer = 220 + Math.floor(Math.random() * 250);
        }

        if (flashOpacity > 0.02) {
          ctx.fillStyle = isDarkTheme
            ? `rgba(243, 232, 255, ${flashOpacity * 0.6})`
            : `rgba(255, 255, 255, ${flashOpacity * 0.75})`;
          ctx.fillRect(0, 0, width, height);
          flashOpacity *= 0.82;
        }
      }

      animationFrameId = requestAnimationFrame(render);
    };

    animationFrameId = requestAnimationFrame(render);

    return () => {
      cancelAnimationFrame(animationFrameId);
      window.removeEventListener("resize", handleResize);
      window.removeEventListener("scroll", handleScroll);
      if (scrollTimeout) clearTimeout(scrollTimeout);
    };
  }, [weatherCode, isDay, enabled, isDarkTheme, windSpeed]);

  if (!enabled) return null;

  return (
    <canvas
      ref={canvasRef}
      className="fixed inset-0 pointer-events-none z-10 w-full h-full transform-gpu"
    />
  );
}
