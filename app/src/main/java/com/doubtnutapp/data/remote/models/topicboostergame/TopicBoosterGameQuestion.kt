package com.doubtnutapp.data.remote.models.topicboostergame

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 1/3/21.
 */

@Keep
data class TopicBoosterGameQuestion(
    @SerializedName("class") val clazz: Int,
    @SerializedName("subject") val subject: String,
    @SerializedName("chapter") val chapter: String,
    @SerializedName("question_id") val questionId: Long,
    @SerializedName("question_text") val questionText: String,
    @SerializedName("options") val options: List<String>,
    @SerializedName("answer") val answer: String,
    @SerializedName("score") val score: Int,
    @SerializedName("expire") val expire: Int,
    @SerializedName("test_uuid") val testUuid: String,
    @SerializedName("bot_meta") val botMeta: BotMeta,
    @SerializedName("option_status_list") var optionStatusList: MutableList<Int>?,
    @SerializedName("solutions_playlist_id") val solutionsPlaylistId: String?
) {
    val answerInt: Int
        get() = getOptionNumberFromOption(answer)

    val optionStatusPairs: List<Pair<String, Int>>
        get() = options.zip(optionStatusList.orEmpty())
}

@Keep
data class BotMeta(
    @SerializedName("answer") val answer: String?,
    @SerializedName("response_time") val responseTime: Long,
) {
    val answerInt: Int?
        get() = if (answer.isNullOrBlank()) null else getOptionNumberFromOption(answer)
}

object OptionStatus {
    const val UNSELECTED = 0
    const val OPPONENT_SELECTED = 1
    const val CORRECT = 2
    const val INCORRECT = 3
}

private fun getOptionNumberFromOption(option: String): Int {
    return option.first().toLowerCase().toInt() - 'a'.toInt()
}
