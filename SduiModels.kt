package com.example.ui.sdui

sealed interface SduiComponent

data class SduiText(
    val text: String,
    val sizeSp: Float? = null,
    val colorHex: String? = null,
    val isBold: Boolean = false
) : SduiComponent

data class SduiImage(
    val url: String,
    val heightDp: Float? = null,
    val cornerRadiusDp: Float? = null
) : SduiComponent

data class SduiButton(
    val text: String,
    val actionUrl: String? = null,
    val colorHex: String? = null
) : SduiComponent

data class SduiColumn(
    val children: List<SduiComponent>,
    val spacingDp: Float = 0f,
    val paddingDp: Float = 0f,
    val backgroundColorHex: String? = null
) : SduiComponent

data class SduiRow(
    val children: List<SduiComponent>,
    val spacingDp: Float = 0f,
    val paddingDp: Float = 0f
) : SduiComponent

data class SduiCard(
    val child: SduiComponent,
    val elevationDp: Float = 0f,
    val cornerRadiusDp: Float = 8f,
    val backgroundColorHex: String? = null
) : SduiComponent
