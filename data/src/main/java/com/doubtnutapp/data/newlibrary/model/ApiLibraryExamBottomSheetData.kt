package com.doubtnutapp.data.newlibrary.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class ApiLibraryExamBottomSheetData(
    @SerializedName("widgets") val list: List<WidgetEntityModel<WidgetData, WidgetAction>>,
    @SerializedName("title") val title: String?,
    @SerializedName("title_text_size") val titleTextSize: Float?,
    @SerializedName("title_text_color") val titleTextColor: String?,
    @SerializedName("cta_text") val ctaText: String?,
    @SerializedName("cta_text_size") val ctaTextSize: Float?,
    @SerializedName("cta_text_color") val ctaTextColor: String?,
    @SerializedName("show_close_btn") val showCloseBtn: Boolean? = false,
)
