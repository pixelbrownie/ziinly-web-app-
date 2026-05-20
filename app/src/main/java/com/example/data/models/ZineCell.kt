package com.example.data.models

data class ZineCell(
    val positionIndex: Int, // 0 to 7 matching the 4x2 grid
    val pageName: String,   // "Page 1", "Page 2", "Page 3", "Page 4", "Page 5", "Page 6", "Back", "Cover"
    val imagePath: String? = null, // ImageUri string, or preset illustration name
    val textOverlay: String = "",
    val backgroundColorHex: String = "#FFF9E5",
    val textColorHex: String = "#1A1A2E",
    val fontSizeSp: Float = 14f,
    val imageZoom: Float = 1f,
    val focalX: Float = 0.5f,
    val focalY: Float = 0.5f,
    val isFlipped: Boolean = false
)
