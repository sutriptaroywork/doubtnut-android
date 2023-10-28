package com.doubtnutapp.data.remote.models.quiztfs

import com.google.gson.annotations.SerializedName

/**
 * Created by Mehul Bisht on 26-08-2021
 */

data class LiveQuestionsData(
    @SerializedName("page_title") val pageTitle: String,
    @SerializedName("title") val title: String,
    @SerializedName("class_list") val classList: List<LiveQuestionsClass>,
    @SerializedName("medium_list") val mediumList: List<LiveQuestionsMedium>,
    @SerializedName("subject_data") val subjectData: LiveQuestionsSubjectData,
    @SerializedName("bottom_btn_title") val bottomButtonTitle: String,
    @SerializedName("bottom_image_url") val bottomImageUrl: String?,
    @SerializedName("footer_text") val footerText: String,
    @SerializedName("footer_deeplink") val footerDeeplink: String,
    @SerializedName("bottom_text") val bottomText: String
)
