package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiPopupData(
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("text_color") val textColor: String = "",
    @SerializedName("img_url") val imgUrl: String = "",
    @SerializedName("bg_color") val bgColor: String = "",
    @SerializedName("options") val options: ArrayList<Option>,
    @SerializedName("button_text") val buttonText: String,
    @SerializedName("UI_type") val uiType: String = "",
) {

    @Keep
    public data class Option(
        @SerializedName("id") val id: Int?,
        @SerializedName("type") val type: String = "",
        @SerializedName("display") val display: String = ""
    )
}