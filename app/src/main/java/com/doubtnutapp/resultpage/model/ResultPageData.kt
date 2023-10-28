package com.doubtnutapp.resultpage.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class ResultPageData(
    @SerializedName("title")
    val title: String?,
    @SerializedName("title_text_size")
    val titleTextSize: String?,
    @SerializedName("title_text_color")
    val titleTextColor: String?,

    @SerializedName("bottom_data")
    val bottomData: ResultPageBottomData?,
    @SerializedName("widgets")
    val widgets: List<WidgetEntityModel<*, *>>?
)

@Keep
data class ResultPageBottomData(
    @SerializedName("title")
    val title: String?,
    @SerializedName("title_text_size")
    val titleTextSize: String?,
    @SerializedName("title_text_color")
    val titleTextColor: String?,
    @SerializedName("bg_color")
    val bgColor:String?,
    @SerializedName("deeplink")
    val deeplink:String?
)