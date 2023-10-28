package com.doubtnutapp.data.remote.models.revisioncorner

import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import com.doubtnutapp.data.remote.models.topicboostergame2.Tab
import com.google.gson.annotations.SerializedName

@Keep
data class ResultInfo(
    @SerializedName("title") val title: String,
    @SerializedName("has_tabs") val hasTabs: Boolean,
    @SerializedName("tabs") val tabs: List<Tab>?,
    @SerializedName("active_tab") val activeTabId: Int,
    @SerializedName("results") val results: List<Result>?,
    @SerializedName("next_page") val nextPage: Int?,
)

@Keep
data class Result(
    @SerializedName("time") val time: String,
    @SerializedName("marks") val marks: String,
    @SerializedName("topic") val topic: String,
    @SerializedName("cta_text") val ctaText: String,
    @SerializedName("deeplink") val deeplink: String,
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Result>() {

            override fun areItemsTheSame(oldItem: Result, newItem: Result): Boolean {
                return oldItem.time == newItem.time
            }

            override fun areContentsTheSame(oldItem: Result, newItem: Result): Boolean {
                return oldItem == newItem
            }
        }
    }
}
