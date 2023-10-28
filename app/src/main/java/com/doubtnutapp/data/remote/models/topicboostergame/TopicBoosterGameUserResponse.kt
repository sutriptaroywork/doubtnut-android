package com.doubtnutapp.data.remote.models.topicboostergame

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 3/3/21.
 */

@Keep
data class TopicBoosterGameUserResponse(
    @SerializedName("test_uuid") val testUuid: String,
    @SerializedName("question_id") val questionId: Long,
    @SerializedName("is_correct") val isCorrect: Int,
    @SerializedName("subject") val subject: String?,
    @SerializedName("chapter") val chapter: String,
    @SerializedName("parent_question_id") val parentQuestionId: String,
    @SerializedName("score") val score: Int,
)
