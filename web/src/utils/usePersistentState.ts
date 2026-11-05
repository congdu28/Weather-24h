"use client";

import { useState, useMemo, useCallback, useSyncExternalStore } from "react";

function subscribeStorage(callback: () => void) {
  if (typeof window === "undefined") {
    return () => {};
  }
  window.addEventListener("storage", callback);
  window.addEventListener("local-storage-update", callback);
  return () => {
    window.removeEventListener("storage", callback);
    window.removeEventListener("local-storage-update", callback);
  };
}

/**
 * Hook đồng bộ an toàn giữa localStorage và React 19 / Next.js SSR
 * Giúp triệt tiêu hoàn toàn lỗi Hydration Mismatch bằng cách trả về defaultValue khi SSR & Hydration,
 * sau đó tự động cập nhật giá trị đã lưu trên trình duyệt client.
 */
export function usePersistentState<T>(
  key: string,
  defaultValue: T
): [T, (val: T | ((prev: T) => T)) => void] {
  const [override, setOverride] = useState<T | null>(null);

  const rawStorage = useSyncExternalStore(
    subscribeStorage,
    () => {
      try {
        return localStorage.getItem(key);
      } catch {
        return null;
      }
    },
    () => null
  );

  const value = useMemo<T>(() => {
    if (override !== null) return override;
    if (rawStorage !== null) {
      try {
        return JSON.parse(rawStorage) as T;
      } catch {
        // ignore
      }
    }
    return defaultValue;
  }, [override, rawStorage, defaultValue]);

  const setValue = useCallback(
    (newVal: T | ((prev: T) => T)) => {
      setOverride((prevOverride) => {
        const current = prevOverride !== null ? prevOverride : value;
        const resolved = typeof newVal === "function" ? (newVal as (p: T) => T)(current) : newVal;
        try {
          localStorage.setItem(key, JSON.stringify(resolved));
          window.dispatchEvent(new Event("local-storage-update"));
        } catch {
          // ignore
        }
        return resolved;
      });
    },
    [key, value]
  );

  return [value, setValue];
}
