package com.doubtnutapp.data.remote.models.doubtfeed2

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 18/5/21.
 */

@Keep
data class DoubtFeedStatus(
    @SerializedName("is_doubt_feed_available") val isDoubtFeedAvailable: Boolean,
)
