package com.doubtnutapp.common.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class BookCallData(
    @SerializedName("date_title")
    val dateTitle: String?,
    @SerializedName("date_items")
    val dateItems: List<DateItem>?,

    @SerializedName("time_title")
    val timeTitle: String?,
    @SerializedName("time_items")
    val timeItems: List<TimeItem>?,

    @SerializedName("cta_text")
    val ctaText: String?,
)

@Keep
data class DateItem(
    @SerializedName("id")
    val id: String?,
    @SerializedName("text_one")
    val text1: String?,
    @SerializedName("text_two")
    val text2: String?,
    @SerializedName("is_enabled")
    val isEnabled: Boolean?,
    @SerializedName("is_selected")
    var isSelected: Boolean?,
)

@Keep
data class TimeItem(
    @SerializedName("id")
    val id: String?,
    @SerializedName("text_one")
    val text1: String?,
    @SerializedName("is_enabled")
    val isEnabled: Boolean?,
    @SerializedName("is_selected")
    var isSelected: Boolean?,
)
