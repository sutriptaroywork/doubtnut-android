package com.doubtnutapp.data.remote.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 26/05/20.
 */
@Keep
data class ApiPopUpDetail(
    @SerializedName("list") val list: List<ApiLiveQuizPopUp>
)

@Keep
data class ApiLiveQuizPopUp(
    @SerializedName("quiz_resource_id") val quizResourceId: Long?,
    @SerializedName("liveclass_resource_id") val liveClassResourceId: Long?,
    @SerializedName("live_at") val liveAt: Long?,
    @SerializedName("list") val list: List<LiveQuizQna>?,
    @SerializedName("ended") val ended: Boolean?,
    @SerializedName("publish_id") val publishId: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("detail_id") val detailId: String?,
    @SerializedName("data") val pollData: ApiPollData?
)

@Keep
data class ApiPollData(
    @SerializedName("publish_id") val publishId: String?,
    @SerializedName("question") val question: String?,
    @SerializedName("question_text_color") val questionTextColor: String?,
    @SerializedName("question_text_size") val questionTextSize: String?,
    @SerializedName("quiz_question_id") val quizQuestionId: String?,
    @SerializedName("expiry") val expiry: String?,
    @SerializedName("expiry_text_color") val expiryTextColor: String?,
    @SerializedName("expiry_text_size") val expiryTextSize: String?,
    @SerializedName("response_expiry") val responseExpiry: String?,
    @SerializedName("answer") val answer: String?,
    @SerializedName("show_close_btn") val showCloseBtn: Boolean?,
    @SerializedName("bg_color") val bgColor: String?,
    @SerializedName("items") val items: List<ApiPollOption>?
)

@Keep
data class ApiPollOption(
    @SerializedName("key") val key: String?,
    @SerializedName("value") val value: String?
)

@Keep
interface LiveClassPopUpItem {
    var liveAt: Long?
}
