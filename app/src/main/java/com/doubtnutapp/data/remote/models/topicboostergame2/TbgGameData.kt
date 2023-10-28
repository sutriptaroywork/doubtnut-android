package com.doubtnutapp.data.remote.models.topicboostergame2

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 25/06/21.
 */

@Keep
data class TbgGameData(
    @SerializedName("game_id") val gameId: String,
    @SerializedName("questions") val questions: List<TbgQuestion>?,
    @SerializedName("can_game_start") val canGameStart: Boolean?,
    @SerializedName("chat_actions") val chatActions: ChatActions,
    @SerializedName("topic") val topic: String,
    @SerializedName("inviter_data", alternate = ["bot_data"]) val inviterData: InviterData?,
    @SerializedName("music_url") val musicUrl: String?,
    @SerializedName("unavailable_container") val unavailableData: UnavailableData?,
    @SerializedName("loading_screen_container") val loadingScreenContainer: LoadingScreenContainer?,
)

@Keep
data class TbgQuestion(
    @SerializedName("class") val clazz: Int,
    @SerializedName("subject") val subject: String,
    @SerializedName("chapter") val chapter: String,
    @SerializedName("question_id") val questionId: Long,
    @SerializedName("question_text") val questionText: String,
    @SerializedName("options") val options: List<String>,
    @SerializedName("answer") val answer: String,
    @SerializedName("base_score") val baseScore: Int,
    @SerializedName("max_score") val maxScore: Int,
    @SerializedName("fraction") private val _fraction: Int?,
    @SerializedName("multiplier") private val _multiplier: Int?,
    @SerializedName("expire") val expire: Int,
    @SerializedName("bot_meta") val botMeta: BotMeta?,
    @SerializedName("option_status_list") var optionStatusList: MutableList<Int>?,
    @SerializedName("solutions_playlist_id") val solutionsPlaylistId: String?,
    @SerializedName("bot_messages") val botMessages: List<BotMessage>,
    @SerializedName("bot_emoji") val botEmoji: List<BotMessage>,
) {
    val answerInt: Int
        get() = getOptionNumberFromOption(answer)

    inline val optionStatusPairs: List<Pair<String, Int>>
        get() = options.zip(optionStatusList.orEmpty())

    val fraction: Int
        get() = _fraction ?: expire / 6 // Divide the total duration in 6 parts to get fraction

    val multiplier: Int
        get() = _multiplier ?: 2
}

@Keep
data class BotMeta(
    @SerializedName("answer") val answer: String?,
    @SerializedName("response_time") val responseTime: Long,
) {
    val answerInt: Int?
        get() = if (answer.isNullOrBlank()) null else getOptionNumberFromOption(answer)
}

@Keep
data class BotMessage(
    @SerializedName("message") val message: String,
    @SerializedName("time") val time: Int,
)

@Keep
data class ChatActions(
    @SerializedName("messages") val messages: List<String>,
    @SerializedName("emoji") val emojis: List<String>,
)

@Keep
data class InviterData(
    @SerializedName("student_id") val studentId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String,
    @SerializedName("counter") val counter: String?,
    @SerializedName("background_color") val backgroundColor: String?,
)

@Keep
data class LoadingScreenContainer(
    @SerializedName("opponent_found") val opponentFound: String,
    @SerializedName("wait_title") val waitTitle: String,
    @SerializedName("waiting_time") val waitingTime: Long,
    @SerializedName("status_text") val statusText: String,
)

object OptionStatus {
    const val UNSELECTED = 0
    const val OPPONENT_SELECTED = 1
    const val CORRECT = 2
    const val INCORRECT = 3
}

private fun getOptionNumberFromOption(option: String): Int {
    return option.first().toLowerCase().toInt() - 'a'.toInt()
}
