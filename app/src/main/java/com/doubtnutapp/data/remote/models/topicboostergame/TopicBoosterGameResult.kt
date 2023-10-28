package com.doubtnutapp.data.remote.models.topicboostergame

import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 6/4/21.
 */

data class TopicBoosterGameResult(
    @SerializedName("reward_message") val rewardMessage: String?,
) {
    companion object {
        const val LOST = 0
        const val WON = 1
        const val TIED = 2
    }
}
