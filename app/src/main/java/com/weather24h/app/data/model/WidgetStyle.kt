package com.weather24h.app.data.model

/**
 * Các phong cách thị giác đa dạng cho Widget màn hình chính (Widget Visual Styles)
 */
enum class WidgetStyle(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String
) {
    FROSTED_GLASS(
        id = "frosted_glass",
        title = "Kính mờ hiện đại",
        description = "Nền kính mờ tinh tế kết hợp viền bo tròn thời thượng",
        iconEmoji = "🪟"
    ),
    MINIMAL_CLEAN(
        id = "minimal_clean",
        title = "Tối giản không viền",
        description = "0% nền, số và biểu tượng nổi bật hòa vào hình nền điện thoại",
        iconEmoji = "✨"
    ),
    ADAPTIVE_COLOR(
        id = "adaptive_color",
        title = "Thích ứng thời tiết",
        description = "Tự động đổi sắc thái nền theo trạng thái trời thực tế",
        iconEmoji = "🎨"
    ),
    SOLID_DARK(
        id = "solid_dark",
        title = "Đen tuyền OLED",
        description = "Đen sâu tương phản cao, siêu tiết kiệm pin cho màn hình OLED",
        iconEmoji = "🖤"
    )
}
