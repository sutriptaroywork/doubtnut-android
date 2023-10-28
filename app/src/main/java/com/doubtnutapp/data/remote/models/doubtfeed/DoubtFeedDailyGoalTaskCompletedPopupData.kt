package com.doubtnutapp.data.remote.models.doubtfeed

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 15/5/21.
 */

@Keep
data class DoubtFeedDailyGoalTaskCompletedPopupData(
    @SerializedName("next_pop_up_img_url") val imageUrl: String,
    @SerializedName("next_pop_up_heading_text") val headingText: String,
    @SerializedName("next_pop_up_sub_heading_text") val subHeadingText: String,
    @SerializedName("next_pop_up_button_text") val buttonText: String,
    @SerializedName("is_topic_done") val isTopicDone: Boolean,
    @SerializedName("new_heading") val newHeading: String?,
)
