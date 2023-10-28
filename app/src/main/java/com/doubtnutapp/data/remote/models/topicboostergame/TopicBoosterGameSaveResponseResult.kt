package com.doubtnutapp.data.remote.models.topicboostergame

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 3/3/21.
 */

@Keep
data class TopicBoosterGameSaveResponseResult(@SerializedName("data") val data: String)
