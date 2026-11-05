import type { Metadata, Viewport } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Thời Tiết 24h & Lịch Âm Việt Nam",
  description: "Dự báo thời tiết 24 giờ, 7 ngày, radar mưa thời gian thực và Lịch Vạn Niên Việt Nam chuẩn xác.",
  manifest: "/manifest.json",
  icons: {
    icon: [
      { url: "/favicon.ico" },
      { url: "/icon.png", sizes: "192x192", type: "image/png" }
    ],
    shortcut: "/favicon.ico",
    apple: "/logo.png"
  },
  appleWebApp: {
    capable: true,
    statusBarStyle: "black-translucent",
    title: "Thời Tiết 24h"
  }
};

export const viewport: Viewport = {
  themeColor: "#020617",
  width: "device-width",
  initialScale: 1,
  maximumScale: 1
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="vi" suppressHydrationWarning>
      <body
        suppressHydrationWarning
        className="min-h-screen antialiased selection:bg-amber-500 selection:text-black font-sans"
      >
        {children}
      </body>
    </html>
  );
}
