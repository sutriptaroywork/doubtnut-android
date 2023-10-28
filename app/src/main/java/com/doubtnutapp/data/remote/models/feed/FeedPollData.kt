package com.doubtnutapp.data.remote.models.feed

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FeedPollData(
    @SerializedName("poll_id") val pollId: Int,
    @SerializedName("result") val results: List<PollResult>,
    @SerializedName("options") val options: List<String>,
    @SerializedName("is_polled") val isPolled: Int,
    @SerializedName("total_polled_count") val totalPolledCount: Int
) : Parcelable

@Parcelize
data class PollResult(
    @SerializedName("option") val option: String,
    @SerializedName("value") val value: Int
) : Parcelable
