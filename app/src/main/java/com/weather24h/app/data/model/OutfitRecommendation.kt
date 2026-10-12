package com.weather24h.app.data.model

data class OutfitRecommendation(
    val summary: String,
    val top: String,
    val bottom: String,
    val footwear: String,
    val accessories: List<String>,
    val warningAlert: String? = null,
    val activityTip: String,
    val styleVibe: String,
    val isAIGenerated: Boolean = false
)
