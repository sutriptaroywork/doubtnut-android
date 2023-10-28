package com.doubtnutapp.doubtpecharcha.model

import com.google.gson.annotations.SerializedName

data class FeedbackUserListResponse(
    @SerializedName("feedback_data") val feedbackData: ArrayList<FeedbackItemData>,
    @SerializedName("options_data") val optionsData: ArrayList<OptionDataItem>
) {

    data class FeedbackItemData(
        @SerializedName("title") val title: String?,
        @SerializedName("user_id") val userId: String,
        @SerializedName("profile_image") val profileImage: String?,

        )

    data class OptionDataItem(
        @SerializedName("id") val id: Int?,
        @SerializedName("title") val title: String?,
        @SerializedName("options") val listOptions: ArrayList<String>,
    )

}