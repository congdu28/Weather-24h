"use client";

import React, { useEffect, useRef } from "react";

interface Props {
  weatherCode: number;
  isDay: boolean;
  enabled?: boolean;
  isDarkTheme?: boolean;
}

interface RainParticle {
  x: number;
  y: number;
  speed: number;
  len: number;
  opacity: number;
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

export default function WeatherAtmosphereEffect({
  weatherCode,
  isDay,
  enabled = true,
  isDarkTheme = true
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

    // Weather type classification
    const isRain = (weatherCode >= 51 && weatherCode <= 67) || (weatherCode >= 80 && weatherCode <= 82);
    const isThunder = weatherCode >= 95;
    const isFog = weatherCode === 45 || weatherCode === 48;
    const isSunny = (weatherCode === 0 || weatherCode === 1) && isDay;
    const isClearNight = (weatherCode === 0 || weatherCode === 1) && !isDay;
    const isCloudy = weatherCode === 2 || weatherCode === 3;

    // Cached gradients to prevent memory allocation / GC stutter during scroll
    let cachedSunGrad: CanvasGradient | null = null;
    let cachedMoonGlow: CanvasGradient | null = null;
    let cachedCloudGrad: CanvasGradient | null = null;

    const initGradients = () => {
      if (isSunny) {
        const sunX = width * 0.5;
        const sunY = -40;
        cachedSunGrad = ctx.createRadialGradient(sunX, sunY, 15, sunX, sunY, width * 0.75);
        if (isDarkTheme) {
          cachedSunGrad.addColorStop(0, "rgba(255, 230, 150, 0.35)");
          cachedSunGrad.addColorStop(0.3, "rgba(255, 180, 50, 0.15)");
          cachedSunGrad.addColorStop(0.7, "rgba(255, 150, 30, 0.04)");
          cachedSunGrad.addColorStop(1, "rgba(255, 120, 0, 0)");
        } else {
          cachedSunGrad.addColorStop(0, "rgba(255, 245, 180, 0.50)");
          cachedSunGrad.addColorStop(0.3, "rgba(255, 200, 70, 0.22)");
          cachedSunGrad.addColorStop(0.7, "rgba(255, 170, 40, 0.06)");
          cachedSunGrad.addColorStop(1, "rgba(255, 140, 0, 0)");
        }
      }

      if (isClearNight) {
        const moonX = width * 0.82;
        const moonY = 90;
        cachedMoonGlow = ctx.createRadialGradient(moonX, moonY, 15, moonX, moonY, 140);
        cachedMoonGlow.addColorStop(0, "rgba(220, 235, 255, 0.50)");
        cachedMoonGlow.addColorStop(0.4, "rgba(180, 210, 255, 0.15)");
        cachedMoonGlow.addColorStop(1, "rgba(100, 150, 255, 0)");
      }

      if (isCloudy || isFog) {
        cachedCloudGrad = ctx.createRadialGradient(width * 0.5, height * 0.2, 50, width * 0.5, height * 0.2, 380);
        cachedCloudGrad.addColorStop(0, isDarkTheme ? "rgba(255, 255, 255, 0.08)" : "rgba(255, 255, 255, 0.35)");
        cachedCloudGrad.addColorStop(0.6, isDarkTheme ? "rgba(200, 215, 235, 0.03)" : "rgba(200, 220, 255, 0.15)");
        cachedCloudGrad.addColorStop(1, "rgba(200, 215, 235, 0)");
      }
    };

    initGradients();

    const handleResize = () => {
      if (!canvas) return;
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;
      initGradients();
    };
    window.addEventListener("resize", handleResize);

    // Scroll detection to throttle during active user scrolling
    let isScrolling = false;
    let scrollTimeout: ReturnType<typeof setTimeout> | null = null;
    const handleScroll = () => {
      isScrolling = true;
      if (scrollTimeout) clearTimeout(scrollTimeout);
      scrollTimeout = setTimeout(() => {
        isScrolling = false;
      }, 100);
    };
    window.addEventListener("scroll", handleScroll, { passive: true });

    // 1. Rain setup (tối ưu số lượng hạt để đạt 120fps mượt mà)
    const rainCount = isThunder ? 100 : isRain ? 75 : 0;
    const rainDrops: RainParticle[] = [];
    for (let i = 0; i < rainCount; i++) {
      rainDrops.push({
        x: Math.random() * (width + 250) - 100,
        y: Math.random() * height,
        speed: 16 + Math.random() * 12,
        len: 20 + Math.random() * 20,
        opacity: 0.35 + Math.random() * 0.4
      });
    }

    // 2. Stars setup
    const stars: StarParticle[] = [];
    if (isClearNight) {
      for (let i = 0; i < 60; i++) {
        stars.push({
          x: Math.random() * width,
          y: Math.random() * (height * 0.75),
          size: 0.8 + Math.random() * 1.6,
          baseOpacity: 0.25 + Math.random() * 0.6,
          twinkleSpeed: 0.02 + Math.random() * 0.03,
          phase: Math.random() * Math.PI * 2
        });
      }
    }

    // 3. Solar Dust Motes
    const motes: SolarMote[] = [];
    if (isSunny) {
      for (let i = 0; i < 28; i++) {
        motes.push({
          x: Math.random() * width,
          y: Math.random() * height,
          size: 1.2 + Math.random() * 2.2,
          vx: (Math.random() - 0.5) * 0.35,
          vy: -0.2 - Math.random() * 0.45,
          opacity: 0.25 + Math.random() * 0.5
        });
      }
    }

    // 4. Meteor
    const meteor = {
      active: false,
      x: 0,
      y: 0,
      vx: 0,
      vy: 0,
      len: 0,
      timer: 180
    };

    // 5. Lightning flash state
    let flashTimer = 0;
    let flashOpacity = 0;

    let time = 0;
    let lastFrameTime = performance.now();

    const render = (currentTime: number) => {
      // Throttle rendering: 60fps khi bình thường, 30fps khi đang cuộn nhanh để GPU ưu tiên cuộn mượt
      const frameDelta = currentTime - lastFrameTime;
      const targetDelta = isScrolling ? 32 : 16.6;

      if (frameDelta < targetDelta) {
        animationFrameId = requestAnimationFrame(render);
        return;
      }
      lastFrameTime = currentTime;

      time += 0.02;
      ctx.clearRect(0, 0, width, height);

      // SUNNY: Draw glowing sun orb & subtle rays
      if (isSunny) {
        const sunX = width * 0.5;
        const sunY = -40;

        if (cachedSunGrad) {
          ctx.fillStyle = cachedSunGrad;
          ctx.beginPath();
          ctx.arc(sunX, sunY, width * 0.75, 0, Math.PI * 2);
          ctx.fill();
        }

        // Solar motes floating
        ctx.fillStyle = isDarkTheme ? "rgba(255, 240, 180, 0.85)" : "rgba(255, 210, 90, 0.85)";
        for (const m of motes) {
          m.x += m.vx;
          m.y += m.vy;
          if (m.y < -10) {
            m.y = height + 10;
            m.x = Math.random() * width;
          }
          if (m.x < -10) m.x = width + 10;
          if (m.x > width + 10) m.x = -10;

          ctx.globalAlpha = m.opacity * (0.6 + Math.sin(time + m.x) * 0.4);
          ctx.beginPath();
          ctx.arc(m.x, m.y, m.size, 0, Math.PI * 2);
          ctx.fill();
        }
        ctx.globalAlpha = 1.0;
      }

      // CLEAR NIGHT: Draw glowing moon, stars and meteor
      if (isClearNight) {
        const moonX = width * 0.82;
        const moonY = 90;

        if (cachedMoonGlow) {
          ctx.fillStyle = cachedMoonGlow;
          ctx.beginPath();
          ctx.arc(moonX, moonY, 140, 0, Math.PI * 2);
          ctx.fill();
        }

        // Moon disc
        ctx.fillStyle = "rgba(240, 246, 255, 0.92)";
        ctx.beginPath();
        ctx.arc(moonX, moonY, 24, 0, Math.PI * 2);
        ctx.fill();

        // Stars twinkling
        for (const s of stars) {
          const alpha = s.baseOpacity + Math.sin(time * 3 + s.phase) * 0.25;
          ctx.fillStyle = `rgba(255, 255, 255, ${Math.max(0.1, Math.min(0.95, alpha))})`;
          ctx.beginPath();
          ctx.arc(s.x, s.y, s.size, 0, Math.PI * 2);
          ctx.fill();
        }

        // Meteor
        meteor.timer--;
        if (meteor.timer <= 0 && !meteor.active) {
          meteor.active = true;
          meteor.x = Math.random() * (width * 0.7);
          meteor.y = Math.random() * (height * 0.3);
          meteor.vx = 14 + Math.random() * 8;
          meteor.vy = 6 + Math.random() * 4;
          meteor.len = 70 + Math.random() * 50;
        }

        if (meteor.active) {
          ctx.strokeStyle = "rgba(255, 255, 255, 0.85)";
          ctx.lineWidth = 1.8;
          ctx.beginPath();
          ctx.moveTo(meteor.x, meteor.y);
          ctx.lineTo(meteor.x - meteor.vx * 3, meteor.y - meteor.vy * 3);
          ctx.stroke();

          meteor.x += meteor.vx;
          meteor.y += meteor.vy;

          if (meteor.x > width + 100 || meteor.y > height * 0.65) {
            meteor.active = false;
            meteor.timer = 250 + Math.floor(Math.random() * 300);
          }
        }
      }

      // THUNDER: Lightning flash logic
      if (isThunder) {
        flashTimer++;
        if (flashTimer % 220 === 0) {
          flashOpacity = 0.85;
        } else if (flashTimer % 220 === 6) {
          flashOpacity = 0.98;
        }

        if (flashOpacity > 0.02) {
          ctx.fillStyle = isDarkTheme ? `rgba(230, 235, 255, ${flashOpacity})` : `rgba(255, 255, 255, ${flashOpacity * 0.8})`;
          ctx.fillRect(0, 0, width, height);
          flashOpacity *= 0.82;
        }
      }

      // RAIN: Slanted falling raindrops
      if (isRain || isThunder) {
        ctx.strokeStyle = isDarkTheme
          ? isThunder
            ? "rgba(210, 230, 255, 0.55)"
            : "rgba(190, 225, 255, 0.45)"
          : "rgba(40, 100, 180, 0.55)";
        ctx.lineWidth = isDarkTheme ? 1.3 : 1.5;

        const windAngle = 0.28;
        for (const drop of rainDrops) {
          ctx.beginPath();
          ctx.moveTo(drop.x, drop.y);
          ctx.lineTo(drop.x + drop.len * windAngle, drop.y + drop.len);
          ctx.stroke();

          drop.y += drop.speed;
          drop.x += drop.speed * windAngle;

          if (drop.y > height) {
            drop.y = -drop.len;
            drop.x = Math.random() * (width + 250) - 100;
          }
        }
      }

      // CLOUDY: Drifting cloud forms
      if (isCloudy || isFog) {
        const cloudX = (time * 14) % (width + 600) - 300;
        ctx.save();
        ctx.translate(cloudX - width * 0.5, 0);
        if (cachedCloudGrad) {
          ctx.fillStyle = cachedCloudGrad;
          ctx.beginPath();
          ctx.arc(width * 0.5, height * 0.2, 380, 0, Math.PI * 2);
          ctx.fill();
        }
        ctx.restore();
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
  }, [weatherCode, isDay, enabled, isDarkTheme]);

  if (!enabled) return null;

  return (
    <canvas
      ref={canvasRef}
      className="fixed inset-0 pointer-events-none z-0 w-full h-full gpu-smooth"
    />
  );
}
