package com.doubtnutapp.data.remote.models

import com.doubtnutapp.data.remote.models.feed.PollResult
import com.google.gson.annotations.SerializedName

data class PollingResultResponse(
    @SerializedName("poll_id") val pollId: Int,
    @SerializedName("result") val result: List<PollResult>
)
