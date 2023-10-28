package com.doubtnutapp.model

import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

data class FilterData(
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("title") val title: String?,
    @SerializedName("title_size") val titleSize: Int?,
    @SerializedName("title_color") val titleColor: String?,
    @SerializedName("submit_text") val submitText: String?,
    @SerializedName("submit_text_color") val submitTextColor: String?,
    @SerializedName("submit_text_size") val submitTextSize: Int?,
    @SerializedName("clear_text") val clearText: String?,
    @SerializedName("clear_text_color") val clearTextColor: String?,
    @SerializedName("clear_text_size") val clearTextSize: Float?,
    @SerializedName("is_clear_text_bold") val isClearTextBold: Boolean?,
    @SerializedName("is_submit_text_bold") val isSubmitTextBold: Boolean?,
)
