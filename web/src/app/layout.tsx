import type { Metadata, Viewport } from "next";
import "./globals.css";

export const metadata: Metadata = {
  metadataBase: new URL("https://weather-24h.vercel.app"),
  title: "Thời Tiết 24h & Lịch Âm Việt Nam",
  description: "Dự báo thời tiết 24 giờ, 7 ngày, chất lượng không khí AQI, radar mưa và Lịch Vạn Niên Việt Nam chuẩn xác 100%.",
  applicationName: "Thời Tiết 24h",
  authors: [{ name: "Weather 24h Team" }],
  keywords: [
    "thời tiết 24h",
    "dự báo thời tiết",
    "lịch âm",
    "lịch việt nam",
    "lịch vạn niên",
    "hoàng sa",
    "trường sa",
    "biển đông",
    "weather 24h",
    "vietnam weather",
    "lunar calendar"
  ],
  manifest: "/manifest.json",
  icons: {
    icon: [
      { url: "/favicon.ico" },
      { url: "/icon.png", sizes: "192x192", type: "image/png" }
    ],
    shortcut: "/favicon.ico",
    apple: "/logo.png"
  },
  openGraph: {
    title: "Thời Tiết 24h & Lịch Âm Việt Nam",
    description: "Dự báo thời tiết 24 giờ, 7 ngày, chất lượng không khí AQI, radar mưa và Lịch Vạn Niên Việt Nam chuẩn xác. Khẳng định chủ quyền Hoàng Sa & Trường Sa của Việt Nam 🇻🇳.",
    url: "https://weather-24h.vercel.app",
    siteName: "Thời Tiết 24h",
    images: [
      {
        url: "/logo.png",
        width: 512,
        height: 512,
        alt: "Logo Thời Tiết 24h & Lịch Âm Việt Nam"
      }
    ],
    locale: "vi_VN",
    type: "website"
  },
  twitter: {
    card: "summary",
    title: "Thời Tiết 24h & Lịch Âm Việt Nam",
    description: "Dự báo thời tiết 24 giờ, 7 ngày, radar mưa và Lịch Vạn Niên Việt Nam chuẩn xác 100%.",
    images: ["/logo.png"]
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
