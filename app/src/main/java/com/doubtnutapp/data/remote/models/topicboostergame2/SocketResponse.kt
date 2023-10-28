package com.doubtnutapp.data.remote.models.topicboostergame2

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 25/06/21.
 */

@Keep
sealed class SocketResponse

@Keep
data class InviterOnline(
    @SerializedName("inviter_id") val inviterId: String?,
    @SerializedName("is_online") val isOnline: Boolean,
) : SocketResponse()

@Keep
data class GameBegin(
    @SerializedName("inviter_id") val inviterId: String?,
    @SerializedName("invitee_id") val inviteeId: String,
    @SerializedName("game_id") val gameId: String?,
    @SerializedName("can_game_start") val canGameStart: Boolean,
    @SerializedName("image") val image: String?,
    @SerializedName("name") val name: String?,
) : SocketResponse()

@Keep
data class QuestionSubmit(
    @SerializedName("q_no") val questionNumber: Int,
    @SerializedName("option") val option: Int,
    @SerializedName("score") val score: Int,
    @SerializedName("is_correct") val isCorrect: Boolean,
    @SerializedName("game_id") val gameId: String,
    @SerializedName("total_score") val totalScore: Int?,
    @SerializedName("correct_questions") var correctQuestionIds: List<Long>? = null,
) : SocketResponse()

@Keep
data class GameMessage(@SerializedName("message") val message: String) : SocketResponse()

@Keep
data class GameEmoji(@SerializedName("emoji") val emoji: String) : SocketResponse()
