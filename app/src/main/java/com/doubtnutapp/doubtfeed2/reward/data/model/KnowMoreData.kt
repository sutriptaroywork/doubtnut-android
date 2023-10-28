package com.doubtnutapp.doubtfeed2.reward.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class KnowMoreData(
    @SerializedName("heading") val heading: String,
    @SerializedName("content") val contentList: List<KnowMoreItem>,
    @SerializedName("notes") val notes: KnowMoreItem,
)

@Keep
data class KnowMoreItem(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
)
