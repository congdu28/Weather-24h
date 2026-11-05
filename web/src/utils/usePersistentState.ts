"use client";

import { useSyncExternalStore, useCallback } from "react";

// Bộ nhớ cache cục bộ để đảm bảo getSnapshot luôn trả về cùng một tham chiếu bộ nhớ (object reference)
// nếu chuỗi dữ liệu trong localStorage không hề thay đổi, tuân thủ tuyệt đối quy tắc của React 19 useSyncExternalStore.
const memoryCache = new Map<string, { raw: string | null; value: unknown }>();
const defaultCache = new Map<string, unknown>();

function subscribe(callback: () => void) {
  if (typeof window === "undefined") {
    return () => {};
  }
  window.addEventListener("storage", callback);
  window.addEventListener("weather24h-storage-update", callback);
  return () => {
    window.removeEventListener("storage", callback);
    window.removeEventListener("weather24h-storage-update", callback);
  };
}

function getStoredSnapshot<T>(key: string, defaultValue: T): T {
  if (typeof window === "undefined") {
    return defaultValue;
  }
  try {
    const raw = localStorage.getItem(key);
    const cached = memoryCache.get(key);
    if (cached && cached.raw === raw) {
      return cached.value as T;
    }
    if (raw !== null) {
      const parsed = JSON.parse(raw) as T;
      memoryCache.set(key, { raw, value: parsed });
      return parsed;
    }
  } catch {
    // ignore
  }
  return defaultValue;
}

/**
 * Hook đồng bộ an toàn giữa localStorage và React 19 / Next.js SSR.
 * - Khi SSR và Hydration pass đầu tiên: Sử dụng defaultValue ổn định để loại bỏ triệt để lỗi Hydration Mismatch.
 * - Sau khi mount: useSyncExternalStore tự động đọc dữ liệu đã lưu từ localStorage mà không gây cascading render.
 * - Khi setValue: Cập nhật trực tiếp localStorage và thông báo qua microtask ngoài luồng render, không bao giờ gây lỗi 'Cannot update a component while rendering'.
 */
export function usePersistentState<T>(
  key: string,
  defaultValue: T
): [T, (val: T | ((prev: T) => T)) => void] {
  if (!defaultCache.has(key)) {
    defaultCache.set(key, defaultValue);
  }

  const getSnapshot = useCallback((): T => {
    return getStoredSnapshot(key, defaultCache.get(key) as T);
  }, [key]);

  const getServerSnapshot = useCallback((): T => {
    return defaultCache.get(key) as T;
  }, [key]);

  const value = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);

  const setValue = useCallback(
    (newVal: T | ((prev: T) => T)) => {
      try {
        const current = getStoredSnapshot(key, defaultCache.get(key) as T);
        const resolved = typeof newVal === "function" ? (newVal as (p: T) => T)(current) : newVal;
        const serialized = JSON.stringify(resolved);
        localStorage.setItem(key, serialized);
        memoryCache.set(key, { raw: serialized, value: resolved });

        // Dispatch ngoài luồng render qua microtask để đảm bảo an toàn tuyệt đối
        if (typeof window !== "undefined") {
          queueMicrotask(() => {
            window.dispatchEvent(new Event("weather24h-storage-update"));
          });
        }
      } catch {
        // ignore
      }
    },
    [key]
  );

  return [value, setValue];
}
