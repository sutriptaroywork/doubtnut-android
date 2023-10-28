package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiMatchedQuestionSource(
    @SerializedName("ocr_text") val ocrText: String?,
    @SerializedName("is_exact_match") val isExactMatch: Boolean?,
    @SerializedName("top_left") val topLeft: UiConfiguration?,
    @SerializedName("top_right") val topRight: UiConfiguration?,
    @SerializedName("bottom_left") val bottomLeft: UiConfiguration?,
    @SerializedName("bottom_center") val bottomCenter: UiConfiguration?,
    @SerializedName("bottom_right") val bottomRight: UiConfiguration?
)

@Keep
data class UiConfiguration(
    @SerializedName("text") val text: String?,
    @SerializedName("text_size") val textSize: String?,
    @SerializedName("is_bold") val isBold: Boolean?,
    @SerializedName("padding") val padding: UiConfigPadding?,
    @SerializedName("margin") val margin: UiConfigMargin?,
    @SerializedName("corner_radius") val cornerRadius: UiConfigCornerRadius?,
    @SerializedName("text_color") val textColor: String?,
    @SerializedName("stroke_color") val strokeColor: String?,
    @SerializedName("stroke_width") val strokeWidth: Int?,
    @SerializedName("text_gravity") val textGravity: Double?,
    @SerializedName("icon_link") val iconLink: String?,
    @SerializedName("icon_height") val iconHeight: Int?,
    @SerializedName("icon_width") val iconWidth: Int?,
    @SerializedName("background_color") val backgroundColor: String?,
    @SerializedName("width_percentage") val widthPercentage: Double?,
)

@Keep
data class UiConfigPadding(
    @SerializedName("top") val top: Int?,
    @SerializedName("left") val left: Int?,
    @SerializedName("right") val right: Int?,
    @SerializedName("bottom") val bottom: Int?,
)

@Keep
data class UiConfigMargin(
    @SerializedName("top") val top: Int?,
    @SerializedName("left") val left: Int?,
    @SerializedName("right") val right: Int?,
    @SerializedName("bottom") val bottom: Int?,
)

@Keep
data class UiConfigCornerRadius(
    @SerializedName("top_left") val topLeft: Double?,
    @SerializedName("top_right") val topRight: Double?,
    @SerializedName("bottom_right") val bottomRight: Double?,
    @SerializedName("bottom_left") val bottomLeft: Double?,
)
