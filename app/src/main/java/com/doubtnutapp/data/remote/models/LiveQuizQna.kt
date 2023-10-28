package com.doubtnutapp.data.remote.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 05/05/20.
 */

@Keep
data class LiveQuizData(
    @SerializedName("quiz_resource_id") val quiz_resource_id: Long = 0,
    @SerializedName("liveclass_resource_id") val liveclass_resource_id: Long = 0,
    @SerializedName("live_at") override var liveAt: Long? = -1,
    @SerializedName("list") var list: List<LiveQuizQna> = mutableListOf(),
    @SerializedName("ended") val ended: Boolean = false
) : LiveClassPopUpItem

@Keep
data class LiveQuizQna(
    @SerializedName("quiz_question_id") val quiz_question_id: String = "",
    @SerializedName("question") val question: String = "",
    @SerializedName("options") val options: List<Option> = listOf(),
    @SerializedName("type") val type: Long = 0,
    @SerializedName("live_at") val liveAt: Long = 0,
    @SerializedName("is_answered") val isAnswered: Boolean = false,
    @SerializedName("is_shown") var isShown: Boolean = false,
    @SerializedName("expiry") val expiry: String = "",
    @SerializedName("response_expiry") val response_expiry: String = ""
)

@Keep
data class Option(
    @SerializedName("key") val key: String = "",
    @SerializedName("value") val value: String = ""
)
