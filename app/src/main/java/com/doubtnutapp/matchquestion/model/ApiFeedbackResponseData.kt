package com.doubtnutapp.matchquestion.model

import com.google.gson.annotations.SerializedName

data class ApiFeedbackResponseData(
    @SerializedName("title") val title: String? = "",
    @SerializedName("description") val description: String? = "",
    @SerializedName("text_color") val textColor: String? = "",
    @SerializedName("img_url") val imgUrl: String? = "",
    @SerializedName("bg_color") val bgColor: String? = "",
    @SerializedName("options") val options: ArrayList<ApiPopupData.Option>,
    @SerializedName("button_text") val buttonText: String? = "",
    val feedbackDialogAdapterType: FeedbackDialogAdapterType?

) {
    object Mapper {
        fun toFeedbackResponse(apiPopupData: ApiPopupData): ApiFeedbackResponseData {
            val feedbackAdapterType = when (apiPopupData.uiType) {
                "GRID" -> {
                    FeedbackDialogAdapterType.GRID
                }
                else -> {
                    FeedbackDialogAdapterType.VERTICAL
                }
            }
            return ApiFeedbackResponseData(
                apiPopupData.title,
                apiPopupData.description,
                apiPopupData.textColor,
                apiPopupData.imgUrl,
                apiPopupData.bgColor,
                apiPopupData.options,
                apiPopupData.buttonText,
                feedbackAdapterType
            )
        }
    }
}